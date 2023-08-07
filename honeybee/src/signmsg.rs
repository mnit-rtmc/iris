// signmsg.rs
//
// Copyright (C) 2018-2023  Minnesota Department of Transportation
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
//! The signmsg module is for rendering DMS sign messages to .gif files.
//!
use crate::resource::{make_backup_name, make_name};
use crate::Result;
use gift::{Encoder, Step};
use ntcip::dms::multi::{
    ColorClassic, ColorCtx, ColorScheme, JustificationLine, JustificationPage,
};
use ntcip::dms::{Font, FontCache, Graphic, GraphicCache, Pages};
use pix::{
    el::Pixel,
    gray::{Gray, Gray8},
    rgb::{Rgb, SRgb8},
    Palette, Raster,
};
use serde_derive::{Deserialize, Serialize};
use std::collections::{HashMap, HashSet};
use std::convert::TryInto;
use std::fmt;
use std::fs::{read_dir, remove_file, rename, File};
use std::io::{BufReader, BufWriter, Write};
use std::path::{Path, PathBuf};
use std::time::Instant;

/// Maximum pixel width of DMS images
const PIX_WIDTH: f32 = 450.0;

/// Maximum pixel height of DMS images
const PIX_HEIGHT: f32 = 100.0;

/// Unknown resource error
#[derive(Debug)]
pub struct UnknownResourceError(String);

impl fmt::Display for UnknownResourceError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "Unknown resource: {}", self.0)
    }
}

impl std::error::Error for UnknownResourceError {
    fn source(&self) -> Option<&(dyn std::error::Error + 'static)> {
        None
    }
}

impl UnknownResourceError {
    fn new(msg: String) -> Box<Self> {
        Box::new(UnknownResourceError(msg))
    }
}

/// Sign configuration
#[derive(Deserialize, Serialize)]
struct SignConfig {
    name: String,
    face_width: i32,
    face_height: i32,
    border_horiz: i32,
    border_vert: i32,
    pitch_horiz: i32,
    pitch_vert: i32,
    pixel_width: i32,
    pixel_height: i32,
    char_width: i32,
    char_height: i32,
    monochrome_foreground: i32,
    monochrome_background: i32,
    color_scheme: String,
    default_font: Option<String>,
}

/// Data needed for rendering sign messages
struct MsgData {
    configs: HashMap<String, SignConfig>,
    fonts: FontCache,
    graphics: GraphicCache,
}

/// Cache of image files
struct ImageCache {
    /// Image directory
    img_dir: PathBuf,
    /// Image extension
    ext: String,
    /// Cached image files
    files: HashSet<PathBuf>,
}

/// Sign message
#[derive(Deserialize, Serialize)]
struct SignMessage {
    name: String,
    sign_config: String,
    incident: Option<String>,
    multi: String,
    msg_owner: String,
    flash_beacon: bool,
    msg_priority: i32,
    duration: Option<i32>,
}

/// Convert a RGB value to an (red, green, blue) tuple
fn rgb_from_i32(rgb: i32) -> (u8, u8, u8) {
    let r = (rgb >> 16) as u8;
    let g = (rgb >> 8) as u8;
    let b = rgb as u8;
    (r, g, b)
}

impl SignConfig {
    /// Load sign configurations from a JSON file
    fn load(dir: &Path) -> Result<HashMap<String, SignConfig>> {
        log::debug!("SignConfig::load");
        let mut n = PathBuf::new();
        n.push(dir);
        n.push("sign_config");
        let r = BufReader::new(File::open(&n)?);
        let mut configs = HashMap::new();
        let j: Vec<SignConfig> = serde_json::from_reader(r)?;
        for c in j {
            let cn = c.name.clone();
            configs.insert(cn, c);
        }
        Ok(configs)
    }

    /// Get config name
    fn name(&self) -> &str {
        &self.name
    }

    /// Get default font
    fn default_font(&self) -> Option<&str> {
        self.default_font.as_deref()
    }

    /// Get the face width (mm)
    fn face_width_mm(&self) -> f32 {
        self.face_width.max(0) as f32
    }

    /// Get the width of the sign (pixels)
    fn pixel_width(&self) -> i32 {
        self.pixel_width.max(0)
    }

    /// Get the horizontal border (mm)
    ///
    /// Sanity checked in case vendor supplied stupid values.
    fn border_horiz_mm(&self) -> f32 {
        let pix = self.pixel_width() + self.gap_char_count();
        let min_width = (pix as f32) * self.pitch_horiz_mm();
        let extra = (self.face_width_mm() - min_width).max(0.0);
        if self.gap_char_count() > 0 {
            let border = self.border_horiz.max(0) as f32;
            border.min(extra / 2.0)
        } else {
            // Ignore border_horiz if there are no character gaps
            extra / 2.0
        }
    }

    /// Get the pixel width (mm)
    fn pixel_width_mm(&self) -> f32 {
        self.face_width_mm() - self.border_horiz_mm() * 2.0
    }

    /// Get the horizontal pitch
    fn pitch_horiz(&self) -> i32 {
        self.pitch_horiz.max(0)
    }

    /// Get the horizontal pitch (mm)
    ///
    /// Sanity checked in case vendor supplied stupid values.
    fn pitch_horiz_mm(&self) -> f32 {
        let pitch = self.pitch_horiz() as f32;
        let pix = self.pixel_width() + self.gap_char_count();
        if pix > 0 {
            pitch.min(self.face_width_mm() / pix as f32)
        } else {
            pitch
        }
    }

    /// Get the number of gaps between characters
    fn gap_char_count(&self) -> i32 {
        if self.char_width > 1 && self.pixel_width() > self.char_width {
            (self.pixel_width() - 1) / self.char_width
        } else {
            0
        }
    }

    /// Get the X-position of a pixel on the sign (from 0 to 1)
    fn pixel_x(&self, x: i32) -> f32 {
        let border = self.border_horiz_mm();
        let offset = self.char_offset_mm(x);
        let x = x as f32 + 0.5; // shift to center of pixel
        let pos = border + offset + x * self.pitch_horiz_mm();
        pos / self.face_width_mm()
    }

    /// Get the horizontal character offset (mm)
    fn char_offset_mm(&self, x: i32) -> f32 {
        if self.char_width > 1 {
            let char_num = (x / self.char_width) as f32;
            char_num * self.gap_char_mm()
        } else {
            0.0
        }
    }

    /// Get the character gap (mm)
    fn gap_char_mm(&self) -> f32 {
        let gaps = self.gap_char_count();
        if gaps > 0 {
            self.excess_char_mm() / gaps as f32
        } else {
            0.0
        }
    }

    /// Get excess width for character gaps (mm)
    fn excess_char_mm(&self) -> f32 {
        let pix_mm = self.pitch_horiz_mm() * self.pixel_width() as f32;
        (self.pixel_width_mm() - pix_mm).max(0.0)
    }

    /// Get the face height (mm)
    fn face_height_mm(&self) -> f32 {
        self.face_height.max(0) as f32
    }

    /// Get the height of the sign (pixels)
    fn pixel_height(&self) -> i32 {
        self.pixel_height.max(0)
    }

    /// Get the vertical border (mm)
    ///
    /// Sanity checked in case vendor supplied stupid values.
    fn border_vert_mm(&self) -> f32 {
        let pix = self.pixel_height() + self.gap_line_count();
        let min_height = (pix as f32) * self.pitch_vert_mm();
        let extra = (self.face_height_mm() - min_height).max(0.0);
        if self.gap_line_count() > 0 {
            let border = self.border_vert.max(0) as f32;
            border.min(extra / 2.0)
        } else {
            // Ignore border_vert if there are no line gaps
            extra / 2.0
        }
    }

    /// Get the pixel height (mm)
    fn pixel_height_mm(&self) -> f32 {
        self.face_height_mm() - self.border_vert_mm() * 2.0
    }

    /// Get the vertical pitch
    fn pitch_vert(&self) -> i32 {
        self.pitch_vert.max(0)
    }

    /// Get the vertical pitch (mm)
    ///
    /// Sanity checked in case vendor supplied stupid values.
    fn pitch_vert_mm(&self) -> f32 {
        let pitch = self.pitch_vert() as f32;
        let pix = self.pixel_height() + self.gap_line_count();
        if pix > 0 {
            pitch.min(self.face_height_mm() / pix as f32)
        } else {
            pitch
        }
    }

    /// Get the number of gaps between lines
    fn gap_line_count(&self) -> i32 {
        if self.char_height > 1 && self.pixel_height() > self.char_height {
            (self.pixel_height() - 1) / self.char_height
        } else {
            0
        }
    }

    /// Get the Y-position of a pixel on the sign (from 0 to 1)
    fn pixel_y(&self, y: i32) -> f32 {
        let border = self.border_vert_mm();
        let offset = self.line_offset_mm(y);
        let y = y as f32 + 0.5; // shift to center of pixel
        let pos = border + offset + y * self.pitch_vert_mm();
        pos / self.face_height_mm()
    }

    /// Get the vertical line offset (mm)
    fn line_offset_mm(&self, y: i32) -> f32 {
        if self.char_height > 1 {
            let line_num = (y / self.char_height) as f32;
            line_num * self.gap_line_mm()
        } else {
            0.0
        }
    }

    /// Get the line gap (mm)
    fn gap_line_mm(&self) -> f32 {
        let gaps = self.gap_line_count();
        if gaps > 0 {
            self.excess_line_mm() / gaps as f32
        } else {
            0.0
        }
    }

    /// Get excess height for line gaps (mm).
    fn excess_line_mm(&self) -> f32 {
        let pix_mm = self.pitch_vert_mm() * self.pixel_height() as f32;
        (self.pixel_height_mm() - pix_mm).max(0.0)
    }

    /// Get the character width as u8
    fn char_width(&self) -> Result<u8> {
        Ok(self.char_width.try_into()?)
    }

    /// Get the character height as u8
    fn char_height(&self) -> Result<u8> {
        Ok(self.char_height.try_into()?)
    }

    /// Get the color scheme value
    fn color_scheme(&self) -> ColorScheme {
        self.color_scheme[..].into()
    }

    /// Get the default foreground color
    fn foreground_default_rgb(&self) -> (u8, u8, u8) {
        match self.color_scheme() {
            ColorScheme::ColorClassic | ColorScheme::Color24Bit => {
                ColorClassic::Amber.rgb()
            }
            _ => rgb_from_i32(self.monochrome_foreground),
        }
    }

    /// Get the default background color
    fn background_default_rgb(&self) -> (u8, u8, u8) {
        match self.color_scheme() {
            ColorScheme::ColorClassic | ColorScheme::Color24Bit => {
                ColorClassic::Black.rgb()
            }
            _ => rgb_from_i32(self.monochrome_background),
        }
    }

    /// Render a sign message to a Vec of steps
    fn render_sign_config<W: Write>(
        &self,
        mut writer: W,
        multi: &str,
        msg_data: &MsgData,
    ) -> Result<()> {
        let mut palette = Palette::new(256);
        palette.set_threshold_fn(palette_threshold_rgb8_256);
        palette.set_entry(SRgb8::default());
        let mut steps = Vec::new();
        let pages = self.pages(msg_data, multi)?;
        let (w, h) = self.calculate_size();
        for page in pages {
            let (raster, delay_ds) = page?;
            let delay = delay_ds * 10;
            let step = self.make_face_step(raster, &mut palette, w, h, delay);
            steps.push(step);
        }
        let mut enc = Encoder::new(&mut writer).into_step_enc();
        enc = if steps.len() > 1 {
            enc.with_loop_count(0)
        } else {
            enc
        };
        for step in steps {
            enc.encode_step(&step)?;
        }
        Ok(())
    }

    /// Create pages for a sign config.
    fn pages<'a>(
        &self,
        msg_data: &'a MsgData,
        multi: &'a str,
    ) -> Result<Pages<'a>> {
        let width = self.pixel_width.try_into()?;
        let height = self.pixel_height.try_into()?;
        let color_scheme = self.color_scheme();
        let fg_default = self.foreground_default_rgb();
        let bg_default = self.background_default_rgb();
        let color_ctx = ColorCtx::new(color_scheme, fg_default, bg_default);
        let char_width = self.char_width()?;
        let char_height = self.char_height()?;
        let fname = self.default_font();
        let font_num = msg_data.font_default(fname)?;
        Ok(Pages::builder(width, height)
            .with_color_ctx(color_ctx)
            .with_char_size(char_width, char_height)
            .with_page_on_time_ds(28)
            .with_page_off_time_ds(0)
            .with_justification_page(JustificationPage::Top)
            .with_justification_line(JustificationLine::Center)
            .with_font_num(font_num)
            .with_fonts(Some(msg_data.fonts()))
            .with_graphics(Some(msg_data.graphics()))
            .build(multi))
    }

    /// Calculate the size of rendered DMS
    fn calculate_size(&self) -> (u16, u16) {
        let fw = self.face_width_mm();
        let fh = self.face_height_mm();
        if fw > 0.0 && fh > 0.0 {
            let sx = PIX_WIDTH / fw;
            let sy = PIX_HEIGHT / fh;
            let s = sx.min(sy);
            let w = (fw * s).round() as u16;
            let h = (fh * s).round() as u16;
            (w, h)
        } else {
            (PIX_WIDTH as u16, PIX_HEIGHT as u16)
        }
    }

    /// Make a .gif step of sign face
    fn make_face_step(
        &self,
        page: Raster<SRgb8>,
        palette: &mut Palette,
        w: u16,
        h: u16,
        delay: u16,
    ) -> Step {
        let raster = self.make_face_raster(page, palette, w, h);
        Step::with_indexed(raster, palette.clone())
            .with_delay_time_cs(Some(delay))
    }

    /// Make a raster of sign face
    fn make_face_raster(
        &self,
        page: Raster<SRgb8>,
        palette: &mut Palette,
        w: u16,
        h: u16,
    ) -> Raster<Gray8> {
        let dark = SRgb8::new(20, 20, 0);
        let mut face = Raster::with_clear(w.into(), h.into());
        let ph = page.height();
        let pw = page.width();
        let sx = w as f32 / pw as f32;
        let sy = h as f32 / ph as f32;
        let s = sx.min(sy);
        log::debug!("face: {:?}, scale: {}", self.name(), s);
        for y in 0..ph {
            let py = self.pixel_y(y as i32) * h as f32;
            for x in 0..pw {
                let px = self.pixel_x(x as i32) * w as f32;
                let rgb = page.pixel(x as i32, y as i32);
                let sr = u8::from(Gray::value(rgb.convert::<Gray8>()));
                // Clamp radius between 0.6 and 0.8 (blooming)
                let r = s * (sr as f32 / 255.0).clamp(0.6, 0.8);
                let clr = if sr > 20 { rgb } else { dark };
                render_circle(&mut face, palette, px, py, r, clr);
            }
        }
        face
    }
}

/// Load fonts from a JSON file
fn load_fonts(dir: &Path) -> Result<FontCache> {
    log::debug!("load_fonts");
    let mut n = PathBuf::new();
    n.push(dir);
    n.push("font");
    let r = BufReader::new(File::open(&n)?);
    let mut fonts = FontCache::default();
    let j: Vec<Font> = serde_json::from_reader(r)?;
    for f in j {
        fonts.insert(f);
    }
    Ok(fonts)
}

/// Load graphics from a JSON file
fn load_graphics(dir: &Path) -> Result<GraphicCache> {
    log::debug!("load_graphics");
    let mut n = PathBuf::new();
    n.push(dir);
    n.push("graphic");
    let r = BufReader::new(File::open(&n)?);
    let mut graphics = GraphicCache::default();
    let j: Vec<Graphic> = serde_json::from_reader(r)?;
    for g in j {
        graphics.insert(g);
    }
    Ok(graphics)
}

impl MsgData {
    /// Load message data from a file path
    fn load(dir: &Path) -> Result<Self> {
        log::debug!("MsgData::load");
        let configs = SignConfig::load(dir)?;
        let fonts = load_fonts(dir)?;
        let graphics = load_graphics(dir)?;
        Ok(MsgData {
            configs,
            fonts,
            graphics,
        })
    }

    /// Lookup a config
    fn config(&self, s: &SignMessage) -> Result<&SignConfig> {
        let cfg = &s.sign_config;
        match self.configs.get(cfg) {
            Some(c) => Ok(c),
            None => Err(UnknownResourceError::new(format!("Config: {cfg}"))),
        }
    }

    /// Get the default font number
    fn font_default(&self, fname: Option<&str>) -> Result<u8> {
        match fname {
            Some(fname) => match self.fonts.lookup_name(fname) {
                Some(font) => Ok(font.number()),
                None => {
                    Err(UnknownResourceError::new(format!("Font: {fname}")))
                }
            },
            None => Ok(1),
        }
    }

    /// Get font mapping
    fn fonts(&self) -> &FontCache {
        &self.fonts
    }

    /// Get graphic mapping
    fn graphics(&self) -> &GraphicCache {
        &self.graphics
    }
}

impl SignMessage {
    /// Load sign messages from a JSON file
    fn load_all(dir: &Path) -> Result<Vec<SignMessage>> {
        log::debug!("SignMessage::load_all");
        let mut n = PathBuf::new();
        n.push(dir);
        n.push("sign_message");
        let r = BufReader::new(File::open(&n)?);
        Ok(serde_json::from_reader(r)?)
    }

    /// Get the MULTI string
    fn multi(&self) -> &str {
        &self.multi
    }

    /// Fetch sign message .gif if it is not in the image cache.
    ///
    /// * `msg_data` Data required to render messages.
    /// * `images` Image cache.
    fn fetch(&self, msg_data: &MsgData, images: &mut ImageCache) -> Result<()> {
        let name = images.make_name(&self.name);
        log::debug!("SignMessage::fetch: {:?}", name);
        if !images.contains(&name) {
            let backup = images.make_backup_name(&self.name);
            let writer = BufWriter::new(File::create(&backup)?);
            let t = Instant::now();
            if let Err(e) = self.render_sign_msg(msg_data, writer) {
                log::warn!(
                    "{}, cfg={}, multi={} {:?}",
                    &self.name,
                    self.sign_config,
                    self.multi,
                    e
                );
                remove_file(&backup)?;
                return Ok(());
            };
            rename(backup, name)?;
            log::info!("{}.gif rendered in {:?}", &self.name, t.elapsed());
        }
        Ok(())
    }

    /// Render into a .gif file
    fn render_sign_msg<W: Write>(
        &self,
        msg_data: &MsgData,
        writer: W,
    ) -> Result<()> {
        let cfg = msg_data.config(self)?;
        cfg.render_sign_config(writer, self.multi(), msg_data)
    }
}

impl ImageCache {
    /// Create a set of image files
    fn new(dir: &Path, ext: &str) -> Result<Self> {
        let mut img_dir = PathBuf::new();
        img_dir.push(dir);
        img_dir.push("img");
        let files = ImageCache::files(img_dir.as_path(), ext)?;
        let ext = ext.to_string();
        Ok(ImageCache {
            img_dir,
            ext,
            files,
        })
    }

    /// Lookup a listing of files with a given extension
    fn files(img_dir: &Path, ext: &str) -> Result<HashSet<PathBuf>> {
        let mut files = HashSet::new();
        if img_dir.is_dir() {
            for f in read_dir(img_dir)? {
                let f = f?;
                if f.file_type()?.is_file() {
                    let p = f.path();
                    if let Some(e) = p.extension() {
                        if e == ext {
                            files.insert(p);
                        }
                    }
                }
            }
        }
        Ok(files)
    }

    /// Make image file name
    fn dir_name(&self, name: &str) -> (&Path, String) {
        (self.img_dir.as_path(), format!("{}.{}", name, self.ext))
    }

    /// Make image file name
    fn make_name(&self, name: &str) -> PathBuf {
        let (dir, n) = self.dir_name(name);
        make_name(dir, &n)
    }

    /// Make backup image file name
    fn make_backup_name(&self, name: &str) -> PathBuf {
        let (dir, n) = self.dir_name(name);
        make_backup_name(dir, &n)
    }

    /// Check if an image exists
    fn contains(&mut self, n: &Path) -> bool {
        self.files.remove(n)
    }

    /// Remove expired image files
    fn remove_expired(&mut self) {
        for p in self.files.drain() {
            log::info!("remove_expired: {:?}", &p);
            if let Err(e) = remove_file(&p) {
                log::error!("{:?}", e);
            }
        }
    }
}

/// Render an attenuated circle.
///
/// * `raster` Indexed raster.
/// * `palette` Global color palette.
/// * `cx` X-Center of circle.
/// * `cy` Y-Center of circle.
/// * `r` Radius of circle.
/// * `clr` Color of circle.
fn render_circle(
    raster: &mut Raster<Gray8>,
    palette: &mut Palette,
    cx: f32,
    cy: f32,
    r: f32,
    clr: SRgb8,
) {
    let x0 = (cx - r).floor().max(0.0) as u32;
    let x1 = (cx + r).ceil().min(raster.width() as f32) as u32;
    let y0 = (cy - r).floor().max(0.0) as u32;
    let y1 = (cy + r).ceil().min(raster.height() as f32) as u32;
    let rs = r.powi(2);
    for y in y0..y1 {
        let yd = (cy - y as f32 - 0.5).abs();
        let ys = yd.powi(2);
        for x in x0..x1 {
            let xd = (cx - x as f32 - 0.5).abs();
            let xs = xd.powi(2);
            let mut ds = xs + ys;
            // If center is within this pixel, make it brighter
            if ds < 1.0 {
                ds = ds.powi(2);
            }
            // compare distance squared with radius squared
            let drs = ds / rs;
            let v = 1.0 - drs.powi(2).min(1.0);
            if v > 0.0 {
                // blend with existing pixel
                let i = u8::from(Gray::value(raster.pixel(x as i32, y as i32)));
                if let Some(p) = palette.entry(i as usize) {
                    // TODO: add a blending operation for this
                    let red = (Rgb::red(clr) * v).max(Rgb::red(p));
                    let green = (Rgb::green(clr) * v).max(Rgb::green(p));
                    let blue = (Rgb::blue(clr) * v).max(Rgb::blue(p));
                    let rgb = SRgb8::new(red, green, blue);
                    if let Some(d) = palette.set_entry(rgb) {
                        *raster.pixel_mut(x as i32, y as i32) =
                            Gray8::new(d as u8);
                    } else {
                        log::warn!("Blending failed -- color palette full!");
                    }
                } else {
                    log::warn!("Index not found in color palette!");
                }
            }
        }
    }
}

/// Get the difference threshold for SRgb8 with 256 capacity palette
fn palette_threshold_rgb8_256(v: usize) -> SRgb8 {
    let i = match v as u8 {
        0x00..=0x0F => 0,
        0x10..=0x1E => 1,
        0x1F..=0x2D => 2,
        0x2E..=0x3B => 3,
        0x3C..=0x49 => 4,
        0x4A..=0x56 => 5,
        0x57..=0x63 => 6,
        0x64..=0x6F => 7,
        0x70..=0x7B => 8,
        0x7C..=0x86 => 9,
        0x87..=0x91 => 10,
        0x92..=0x9B => 11,
        0x9C..=0xA5 => 12,
        0xA6..=0xAE => 13,
        0xAF..=0xB7 => 14,
        0xB8..=0xBF => 15,
        0xC0..=0xC7 => 16,
        0xC8..=0xCE => 17,
        0xCF..=0xD5 => 18,
        0xD6..=0xDB => 19,
        0xDC..=0xE1 => 20,
        0xE2..=0xE6 => 21,
        0xE7..=0xEB => 22,
        0xEC..=0xEF => 23,
        0xF0..=0xF3 => 24,
        0xF4..=0xF6 => 25,
        0xF7..=0xF9 => 26,
        0xFA..=0xFB => 27,
        0xFC..=0xFD => 28,
        0xFE..=0xFE => 29,
        0xFF..=0xFF => 30,
    };
    SRgb8::new(i * 4, i * 4, i * 5)
}

/// Fetch all sign messages.
///
/// * `dir` Output file directory.
pub fn render_all(dir: &Path) -> Result<()> {
    let msg_data = MsgData::load(dir)?;
    let mut images = ImageCache::new(dir, "gif")?;
    let sign_msgs = SignMessage::load_all(dir)?;
    for sign_msg in sign_msgs {
        sign_msg.fetch(&msg_data, &mut images)?;
    }
    images.remove_expired();
    Ok(())
}
