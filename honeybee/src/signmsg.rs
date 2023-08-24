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
use crate::files::{AtomicFile, Cache};
use crate::Result;
use anyhow::Context;
use gift::{Decoder, Encoder, Step};
use ntcip::dms::config::{MultiCfg, SignCfg, VmsCfg};
use ntcip::dms::font::{ifnt, FontTable};
use ntcip::dms::graphic::{Graphic, GraphicTable};
use ntcip::dms::multi::{Color, ColorScheme, JustificationPage};
use ntcip::dms::{Dms, Page};
use pix::bgr::SBgr8;
use pix::chan::Ch8;
use pix::el::Pixel;
use pix::gray::{Gray, Gray8};
use pix::rgb::{Rgba8p, SRgb8};
use pix::{Palette, Raster};
use serde_derive::Deserialize;
use std::collections::HashMap;
use std::fmt;
use std::fs::File;
use std::io::{BufReader, Write};
use std::path::{Path, PathBuf};
use std::time::Instant;

/// Maximum pixel width of DMS images
const PIX_WIDTH: u16 = 450;

/// Maximum pixel height of DMS images
const PIX_HEIGHT: u16 = 100;

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
#[allow(unused)]
#[derive(Deserialize)]
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
    module_width: Option<i32>,
    module_height: Option<i32>,
}

/// Data needed for rendering sign messages
struct MsgData {
    dms: Dms,
    configs: HashMap<String, SignConfig>,
}

/// Sign message
#[allow(unused)]
#[derive(Deserialize)]
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
        let mut path = PathBuf::new();
        path.push(dir);
        path.push("sign_config");
        let reader = BufReader::new(
            File::open(&path).with_context(|| format!("load {path:?}"))?,
        );
        let cfgs: Vec<SignConfig> = serde_json::from_reader(reader)?;
        let mut configs = HashMap::new();
        for config in cfgs {
            let cn = config.name.clone();
            configs.insert(cn, config);
        }
        Ok(configs)
    }

    /// Create NTCIP sign config
    fn sign_cfg(&self) -> SignCfg {
        SignCfg {
            sign_height: self.face_height as u16,
            sign_width: self.face_width as u16,
            horizontal_border: self.border_horiz as u16,
            vertical_border: self.border_vert as u16,
            ..Default::default()
        }
    }

    /// Create NTCIP VMS config
    fn vms_cfg(&self) -> VmsCfg {
        let fg = rgb_from_i32(self.monochrome_foreground);
        let bg = rgb_from_i32(self.monochrome_background);
        let monochrome_color = [fg.0, fg.1, fg.2, bg.0, bg.1, bg.2];
        VmsCfg {
            char_height_pixels: self.char_height as u8,
            char_width_pixels: self.char_width as u8,
            sign_height_pixels: self.pixel_height as u16,
            sign_width_pixels: self.pixel_width as u16,
            horizontal_pitch: self.pitch_horiz as u8,
            vertical_pitch: self.pitch_vert as u8,
            monochrome_color,
        }
    }

    /// Create NTCIP MULTI config
    fn multi_cfg(&self, dms: &Dms) -> MultiCfg {
        let default_font = self
            .default_font
            .as_ref()
            .and_then(|f| dms.font_definition().lookup_name(f))
            .map(|f| f.number)
            .unwrap_or(1);
        // Some IRIS defaults differ from NTCIP defaults
        MultiCfg {
            default_flash_on: 0,
            default_flash_off: 0,
            default_font,
            default_justification_page: JustificationPage::Top,
            default_page_on_time: 28,
            default_page_off_time: 0,
            color_scheme: self.color_scheme[..].into(),
            ..Default::default()
        }
    }
}

/// Load fonts from a JSON file
fn load_fonts(dir: &Path) -> Result<FontTable> {
    log::debug!("load_fonts");
    let mut path = PathBuf::new();
    path.push(dir);
    path.push("api");
    path.push("font");
    let mut cache = Cache::new(&path, "ifnt")?;
    let mut fonts = FontTable::default();
    path.push("_placeholder_.ifnt");
    for nm in cache.drain() {
        path.set_file_name(nm);
        let file =
            File::open(&path).with_context(|| format!("font {path:?}"))?;
        fonts.push(ifnt::read(file)?)?;
    }
    fonts.sort();
    Ok(fonts)
}

/// Load graphics from a JSON file
fn load_graphics(dir: &Path) -> Result<GraphicTable> {
    log::debug!("load_graphics");
    let mut path = PathBuf::new();
    path.push(dir);
    path.push("api");
    path.push("img");
    let mut cache = Cache::new(&path, "gif")?;
    let mut graphics = GraphicTable::default();
    path.push("_placeholder_.gif");
    for nm in cache.drain() {
        let number: u8 = nm
            .as_os_str()
            .to_str()
            .unwrap()
            .replace(|c: char| !c.is_numeric(), "")
            .parse()?;
        path.set_file_name(&nm);
        let file = File::open(&path)
            .with_context(|| format!("load_graphics {path:?}"))?;
        let reader = BufReader::new(file);
        if let Some(step) = Decoder::new(reader).into_steps().next() {
            let graphic = make_graphic(number, step?);
            graphics.push(graphic)?;
        }
    }
    graphics.sort();
    Ok(graphics)
}

/// Make a graphic from a step
fn make_graphic(number: u8, step: Step) -> Graphic {
    let name = format!("g{number}");
    let raster: Raster<SBgr8> = Raster::with_raster(step.raster());
    let height = raster.height().try_into().unwrap();
    let width = raster.width().try_into().unwrap();
    let transparent_color =
        step.transparent_color().map(|_c| Color::Rgb(0, 0, 0));
    let slice: Box<[u8]> = raster.into();
    let bitmap: Vec<u8> = slice.into();
    Graphic {
        number,
        name,
        height,
        width,
        gtype: ColorScheme::Color24Bit,
        transparent_color,
        bitmap,
    }
}

impl MsgData {
    /// Load message data from a file path
    fn load(dir: &Path) -> Result<Self> {
        log::debug!("MsgData::load");
        let dms = Dms::builder()
            .with_font_definition(load_fonts(dir)?)
            .with_graphic_definition(load_graphics(dir)?)
            .build();
        let configs = SignConfig::load(dir)?;
        Ok(MsgData { dms, configs })
    }

    /// Lookup a config
    fn config(&self, msg: &SignMessage) -> Result<&SignConfig> {
        let cfg = &msg.sign_config;
        match self.configs.get(cfg) {
            Some(c) => Ok(c),
            None => Err(UnknownResourceError::new(format!("Config: {cfg}"))),
        }
    }

    /// Render sign message .gif
    fn render_sign_msg(
        &mut self,
        msg: &SignMessage,
        file: AtomicFile,
    ) -> Result<()> {
        log::debug!("render_sign_msg: {:?}", file.path());
        let t = Instant::now();
        let writer = file.writer()?;
        let cfg = self.config(msg)?;
        let multi_cfg = cfg.multi_cfg(&self.dms);
        self.dms = self
            .dms
            .clone()
            .into_builder()
            .with_sign_cfg(cfg.sign_cfg())
            .with_vms_cfg(cfg.vms_cfg())
            .with_multi_cfg(multi_cfg)
            .build();
        if let Err(e) = render_msg(&self.dms, &msg.multi, writer) {
            log::warn!("{:?}, multi={} {e:?}", file.path(), msg.multi);
            file.cancel()?;
            return Ok(());
        };
        log::info!("{:?} rendered in {:?}", file.path(), t.elapsed());
        Ok(())
    }
}

/// Render a sign message into a .gif file
fn render_msg<W: Write>(dms: &Dms, multi: &str, mut writer: W) -> Result<()> {
    let (width, height) = face_size(dms);
    let mut steps = Vec::new();
    for page in dms.render_pages(multi) {
        let Page {
            raster,
            duration_ds,
        } = page?;
        let delay_cs = duration_ds * 10;
        let mut palette = make_palette(&raster);
        let face = make_face_raster(dms, raster, width, height);
        let indexed = make_indexed(face, &mut palette);
        steps.push(
            Step::with_indexed(indexed, palette)
                .with_delay_time_cs(Some(delay_cs)),
        );
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

/// Calculate size to render DMS "face"
fn face_size(dms: &Dms) -> (u16, u16) {
    let fw = dms.face_width_mm();
    let fh = dms.face_height_mm();
    if fw > 0.0 && fh > 0.0 {
        let sx = f32::from(PIX_WIDTH) / fw;
        let sy = f32::from(PIX_HEIGHT) / fh;
        if sx > sy {
            let w = (fw * sy).round() as u16;
            // Bump up to next even value
            let w = (w + 1) & 0b11111111_11111110;
            (w, PIX_HEIGHT)
        } else {
            let h = (fh * sx).round() as u16;
            // Bump up to next even value
            let h = (h + 1) & 0b11111111_11111110;
            (PIX_WIDTH, h)
        }
    } else {
        (PIX_WIDTH, PIX_HEIGHT)
    }
}

/// Make a raster of sign face
fn make_face_raster(
    dms: &Dms,
    raster: Raster<SRgb8>,
    width: u16,
    height: u16,
) -> Raster<SRgb8> {
    let mut face = Raster::<Rgba8p>::with_clear(width.into(), height.into());
    let sx = face.width() as f32 / raster.width() as f32;
    let sy = face.height() as f32 / raster.height() as f32;
    let scale = sx.min(sy);
    // Render each LED onto the closest pixel of face raster
    for y in 0..raster.height() {
        let py = dms.pixel_y(y, height.into()) as i32;
        for x in 0..raster.width() {
            let px = dms.pixel_x(x, width.into()) as i32;
            let mut clr = raster.pixel(x as i32, y as i32);
            if clr == SRgb8::default() {
                clr = SRgb8::new(26, 26, 26);
            }
            *face.pixel_mut(px, py) = clr.convert();
            if px + 1 < width.into() && py + 1 < height.into() {
                *face.pixel_mut(px + 1, py + 1) = clr.convert();
            }
        }
    }
    // Render blooming effect for lit LEDs
    for y in 0..raster.height() {
        let py = dms.pixel_y(y, height.into());
        for x in 0..raster.width() {
            let clr = raster.pixel(x as i32, y as i32);
            if clr != SRgb8::default() {
                let px = dms.pixel_x(x, width.into());
                // calculate blooming "brightness"
                let bloom = u8::from(Gray::value(clr.convert::<Gray8>()));
                // clamp blooming radius between 0.5 and 0.75
                let rad = scale * (bloom as f32 / 255.0).clamp(0.5, 0.75);
                render_bloom(&mut face, px, py, rad, clr);
            }
        }
    }
    Raster::<SRgb8>::with_raster(&face)
}

/// Render blooming LED.
///
/// * `raster` Face raster.
/// * `cx` X-Center of LED.
/// * `cy` Y-Center of LED.
/// * `rad` Radius of blooming.
/// * `clr` LED color.
fn render_bloom(
    raster: &mut Raster<Rgba8p>,
    cx: f32,
    cy: f32,
    rad: f32,
    clr: SRgb8,
) {
    let src: Rgba8p = clr.convert();
    let x0 = (cx - rad).floor().max(0.0) as i32;
    let x1 = (cx + rad).ceil().min(raster.width() as f32) as i32;
    let y0 = (cy - rad).floor().max(0.0) as i32;
    let y1 = (cy + rad).ceil().min(raster.height() as f32) as i32;
    let rad_sq = rad.powi(2);
    for y in y0..y1 {
        let yd = y as f32 - cy;
        for x in x0..x1 {
            let xd = x as f32 - cx;
            let mut sam: u8 = 0;
            if xd.powi(2) + yd.powi(2) < rad_sq {
                sam += 51;
            }
            if (xd - 0.4).powi(2) + (yd - 0.2).powi(2) < rad_sq {
                sam += 51;
            }
            if (xd + 0.3).powi(2) + (yd - 0.3).powi(2) < rad_sq {
                sam += 51;
            }
            if (xd - 0.2).powi(2) + (yd + 0.4).powi(2) < rad_sq {
                sam += 51;
            }
            if (xd + 0.3).powi(2) + (yd + 0.3).powi(2) < rad_sq {
                sam += 51;
            }
            if sam > 0 {
                let alpha = Ch8::from(sam);
                raster
                    .pixel_mut(x, y)
                    .composite_channels_alpha(&src, pix::ops::Plus, &alpha);
            }
        }
    }
}

/// Make a palette from colors in a raster
fn make_palette(raster: &Raster<SRgb8>) -> Palette {
    let mut palette = Palette::new(256);
    palette.set_entry(SRgb8::default());
    for pixel in raster.pixels() {
        palette.set_entry(*pixel);
    }
    palette
}

/// Make an indexed raster
fn make_indexed(face: Raster<SRgb8>, palette: &mut Palette) -> Raster<Gray8> {
    palette.set_threshold_fn(palette_threshold_rgb8_256);
    let mut indexed = Raster::with_clear(face.width(), face.height());
    for y in 0..face.height() as i32 {
        for x in 0..face.width() as i32 {
            if let Some(e) = palette.set_entry(face.pixel(x, y)) {
                *indexed.pixel_mut(x, y) = Gray8::new(e as u8);
            } else {
                log::warn!("Blending failed -- color palette full!");
            }
        }
    }
    indexed
}

/// Get the difference threshold for SRgb8 with 256 capacity palette
fn palette_threshold_rgb8_256(v: usize) -> SRgb8 {
    let val = (v & 0xFF) as u8;
    SRgb8::new(val >> 3, val >> 3, val >> 2)
}

impl SignMessage {
    /// Load sign messages from a JSON file
    fn load_all(dir: &Path) -> Result<Vec<SignMessage>> {
        log::debug!("SignMessage::load_all");
        let mut path = PathBuf::new();
        path.push(dir);
        path.push("sign_message");
        let file =
            File::open(&path).with_context(|| format!("load_all {path:?}"))?;
        let reader = BufReader::new(file);
        Ok(serde_json::from_reader(reader)?)
    }
}

/// Fetch all sign messages.
///
/// * `dir` Output file directory.
pub fn render_all(dir: &Path) -> Result<()> {
    let mut msg_data = MsgData::load(dir)?;
    let mut path = PathBuf::new();
    path.push(dir);
    path.push("img");
    let mut cache = Cache::new(path.as_path(), "gif")?;
    for sign_msg in SignMessage::load_all(dir)? {
        let mut name = sign_msg.name.clone();
        name.push_str(".gif");
        if cache.contains(&name) {
            cache.keep(&name);
        } else {
            let file = cache.file(&name)?;
            msg_data.render_sign_msg(&sign_msg, file)?;
        }
    }
    Ok(())
}
