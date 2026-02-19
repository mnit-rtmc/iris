// Copyright (C) 2018-2026  Minnesota Department of Transportation
//
//! rendzina is for rendering DMS sign messages to .gif files
#![forbid(unsafe_code)]

use fstr::FStr;
use gift::block::DisposalMethod;
use gift::{Decoder, Encoder, Step};
use ntcip::dms::config::{MultiCfg, SignCfg, VmsCfg};
use ntcip::dms::multi::{Color, ColorScheme, JustificationPage, SyntaxError};
use ntcip::dms::{Dms, Font, Graphic, Page, Pages, tfon};
use pix::bgr::SBgr8;
use pix::chan::Ch8;
use pix::el::Pixel;
use pix::gray::{Gray, Gray8};
use pix::rgb::{Rgba8p, SRgb8};
use pix::{Palette, Raster};
use serde::Deserialize;
use std::collections::HashMap;
use std::io::{Read, Write};

/// Rendzina error
#[derive(Debug, thiserror::Error)]
pub enum Error {
    #[error("I/O {0}")]
    Io(#[from] std::io::Error),

    #[error("Gift: {0}")]
    Gift(#[from] gift::Error),

    #[error("Missing graphic")]
    MissingGraphic,

    #[error("Json: {0}")]
    Json(#[from] serde_json::Error),

    #[error("Font: {0}")]
    Font(#[from] tfon::Error),

    #[error("Syntax: {0}")]
    Syntax(#[from] SyntaxError),
}

/// Result type
pub type Result<T> = std::result::Result<T, Error>;

/// Sign configuration (in IRIS)
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct SignConfig {
    pub name: String,
    pub face_width: i32,
    pub face_height: i32,
    pub border_horiz: i32,
    pub border_vert: i32,
    pub pitch_horiz: i32,
    pub pitch_vert: i32,
    pub pixel_width: i32,
    pub pixel_height: i32,
    pub char_width: i32,
    pub char_height: i32,
    pub monochrome_foreground: i32,
    pub monochrome_background: i32,
    pub color_scheme: String,
    pub default_font: u8,
    pub module_width: Option<i32>,
    pub module_height: Option<i32>,
    pub sign_count: u32,
}

impl SignConfig {
    /// Load all sign configurations from a JSON file
    pub fn load_all<R: Read>(reader: R) -> Result<HashMap<String, SignConfig>> {
        let cfgs: Vec<SignConfig> = serde_json::from_reader(reader)?;
        let mut configs = HashMap::new();
        for config in cfgs {
            let nm = config.name.clone();
            configs.insert(nm, config);
        }
        Ok(configs)
    }

    /// Create NTCIP sign config
    pub fn sign_cfg(&self) -> SignCfg {
        SignCfg {
            sign_height: self.face_height as u16,
            sign_width: self.face_width as u16,
            horizontal_border: self.border_horiz as u16,
            vertical_border: self.border_vert as u16,
            ..Default::default()
        }
    }

    /// Create NTCIP VMS config
    pub fn vms_cfg(&self) -> VmsCfg {
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
    pub fn multi_cfg(&self) -> MultiCfg {
        // Some IRIS defaults differ from NTCIP defaults
        MultiCfg {
            default_flash_on: 0,
            default_flash_off: 0,
            default_font: self.default_font,
            default_justification_page: JustificationPage::Top,
            default_page_on_time: 28,
            color_scheme: self.color_scheme[..].into(),
            ..Default::default()
        }
    }
}

/// Convert a RGB value to an (red, green, blue) tuple
fn rgb_from_i32(rgb: i32) -> (u8, u8, u8) {
    let r = (rgb >> 16) as u8;
    let g = (rgb >> 8) as u8;
    let b = rgb as u8;
    (r, g, b)
}

/// Load a font from a tfon file
pub fn load_font<R: Read>(reader: R) -> Result<Font> {
    Ok(tfon::read(reader)?)
}

/// Load a graphic from a GIF
pub fn load_graphic<R: Read>(reader: R, number: u8) -> Result<Graphic> {
    // load the first step only
    match Decoder::new(reader).into_steps().next() {
        Some(step) => {
            let step = step?;
            let name = FStr::from_str_lossy(&format!("G{number}"), 0);
            let raster: Raster<SBgr8> = Raster::with_raster(step.raster());
            let height = raster.height().try_into().unwrap();
            let width = raster.width().try_into().unwrap();
            let transparent_color =
                step.transparent_color().map(|_c| Color::Rgb(0, 0, 0));
            let slice: Box<[u8]> = raster.into();
            let bitmap: Vec<u8> = slice.into();
            Ok(Graphic {
                number,
                name,
                height,
                width,
                gtype: ColorScheme::Color24Bit,
                transparent_color,
                bitmap,
            })
        }
        None => Err(Error::MissingGraphic),
    }
}

/// Check if a MULTI string is renderable
pub fn is_renderable(dms: &Dms<256, 24, 32>, multi: &str) -> bool {
    !Pages::new(dms, multi).any(|p| p.is_err())
}

/// Render a sign message to a .gif file
pub fn render_multi<W: Write>(
    mut writer: W,
    dms: &Dms<256, 24, 32>,
    multi: &str,
    max_width: u16,
    max_height: u16,
    mod_size: Option<(u32, u32)>,
) -> Result<()> {
    let (width, height) = face_size(dms, max_width, max_height);
    let mut steps = Vec::new();
    for page in Pages::new(dms, multi) {
        let Page {
            mut raster,
            duration_ds,
        } = page?;
        if let Some((sx, sy)) = mod_size {
            render_modules(&mut raster, sx, sy);
        }
        let delay_cs = duration_ds * 10;
        let mut palette = make_palette(&raster);
        palette.set_threshold_fn(palette_threshold_rgb8_256);
        let face = make_face_raster(dms, raster, width, height);
        let indexed = palette.make_indexed(face);
        steps.push(
            // The CARS iOS client can't read .gif files with no graphic
            // control extension -- set DisposalMethod to Keep to include it
            Step::with_indexed(indexed, palette)
                .with_delay_time_cs(Some(delay_cs))
                .with_disposal_method(DisposalMethod::Keep),
        );
    }
    let mut enc = Encoder::new(&mut writer).into_step_enc();
    let len = steps.len();
    enc = if len > 1 { enc.with_loop_count(0) } else { enc };
    for step in steps {
        if len < 2 {
            enc.encode_step(&step.with_delay_time_cs(None))?;
        } else {
            enc.encode_step(&step)?;
        }
    }
    Ok(())
}

/// Render sign pixels to a .gif file
pub fn render_pixels<W: Write>(
    mut writer: W,
    dms: &Dms<256, 24, 32>,
    pix: &[u32],
    max_width: u16,
    max_height: u16,
) -> Result<()> {
    let (width, height) = face_size(dms, max_width, max_height);
    let width_pix = i32::from(dms.pixel_width());
    let height_pix = i32::from(dms.pixel_height());
    let w = u32::from(dms.pixel_width());
    let h = u32::from(dms.pixel_height());
    let mut stuck = Raster::with_clear(w, h);
    let mut clear = Raster::with_clear(w, h);
    let mut i = 0;
    for y in 0..height_pix {
        for x in 0..width_pix {
            *stuck.pixel_mut(x, y) = pixel_failure_color(pix[i]);
            *clear.pixel_mut(x, y) = pixel_failure_gray(pix[i]);
            i += 1;
        }
    }
    let mut enc = Encoder::new(&mut writer).into_step_enc().with_loop_count(0);
    let mut palette = make_palette(&stuck);
    let face = make_face_raster(dms, stuck, width, height);
    let indexed = palette.make_indexed(face);
    let step = Step::with_indexed(indexed, palette)
        .with_transparent_color(Some(0))
        .with_delay_time_cs(Some(50))
        .with_disposal_method(DisposalMethod::Background);
    enc.encode_step(&step)?;
    let mut palette = make_palette(&clear);
    let face = make_face_raster(dms, clear, width, height);
    let indexed = palette.make_indexed(face);
    let step = Step::with_indexed(indexed, palette)
        .with_transparent_color(Some(0))
        .with_delay_time_cs(Some(30))
        .with_disposal_method(DisposalMethod::Background);
    enc.encode_step(&step)?;
    Ok(())
}

/// Get pixel failure color
fn pixel_failure_color(fail: u32) -> SRgb8 {
    let bits = fail & 0x1F;
    if bits & 0x10 > 0 {
        SRgb8::new(32, 255, 255) // stuck off
    } else if bits & 0x08 > 0 {
        SRgb8::new(255, 32, 255) // mechanical error
    } else if bits & 0x04 > 0 {
        SRgb8::new(255, 255, 32) // electrical error
    } else if bits & 0x02 > 0 {
        SRgb8::new(32, 255, 32) // color error
    } else if bits & 0x01 > 0 {
        SRgb8::new(255, 255, 255) // stuck on
    } else {
        SRgb8::new(0, 0, 0) // no error (translucent)
    }
}

/// Get pixel failure as gray
fn pixel_failure_gray(fail: u32) -> SRgb8 {
    if fail & 0x1F > 0 {
        SRgb8::new(128, 128, 128) // some failure
    } else {
        SRgb8::new(0, 0, 0) // no error (translucent)
    }
}

/// Calculate size to render DMS "face"
pub fn face_size(
    dms: &Dms<256, 24, 32>,
    max_width: u16,
    max_height: u16,
) -> (u16, u16) {
    let fw = dms.face_width_mm();
    let fh = dms.face_height_mm();
    if fw > 0.0 && fh > 0.0 {
        let sx = f32::from(max_width) / fw;
        let sy = f32::from(max_height) / fh;
        if sx > sy {
            let w = (fw * sy).round() as u16;
            // Bump up to next even value
            let w = (w + 1) & 0b11111111_11111110;
            (w, max_height)
        } else {
            let h = (fh * sx).round() as u16;
            // Bump up to next even value
            let h = (h + 1) & 0b11111111_11111110;
            (max_width, h)
        }
    } else {
        (max_width, max_height)
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

/// Render pixel modules onto sign raster
fn render_modules(raster: &mut Raster<SRgb8>, sx: u32, sy: u32) {
    for y in 0..raster.height() {
        for x in 0..raster.width() {
            let md = (x / sx + y / sy) % 2;
            if md == 0 {
                let clr = raster.pixel_mut(x as i32, y as i32);
                if *clr == SRgb8::default() {
                    *clr = SRgb8::new(32, 32, 32);
                }
            }
        }
    }
}

/// Make a raster of sign face
fn make_face_raster(
    dms: &Dms<256, 24, 32>,
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
            let clr = raster.pixel(x as i32, y as i32);
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

/// Sample value of one fifth (1/5) covered (255)
const SAMPLE_FIFTH: u8 = 51;

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
                sam += SAMPLE_FIFTH;
            }
            if (xd - 0.4).powi(2) + (yd - 0.2).powi(2) < rad_sq {
                sam += SAMPLE_FIFTH;
            }
            if (xd + 0.3).powi(2) + (yd - 0.3).powi(2) < rad_sq {
                sam += SAMPLE_FIFTH;
            }
            if (xd - 0.2).powi(2) + (yd + 0.4).powi(2) < rad_sq {
                sam += SAMPLE_FIFTH;
            }
            if (xd + 0.3).powi(2) + (yd + 0.3).powi(2) < rad_sq {
                sam += SAMPLE_FIFTH;
            }
            if sam > 0 {
                let alpha = Ch8::from(sam);
                raster.pixel_mut(x, y).composite_channels_alpha(
                    &src,
                    pix::ops::SrcOver,
                    &alpha,
                );
            }
        }
    }
}

/// Get the difference threshold for SRgb8 with 256 capacity palette
fn palette_threshold_rgb8_256(v: usize) -> SRgb8 {
    let val = (v & 0xFF) as u8;
    SRgb8::new(val >> 3, val >> 3, val >> 2)
}
