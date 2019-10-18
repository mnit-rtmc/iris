// font.rs
//
// Copyright (C) 2018-2019  Minnesota Department of Transportation
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
use crate::error::Error;
use crate::ntcip::multi::{Color, ColorCtx, ColorScheme, SyntaxError};
use pix::{Raster, Rgb8};
use std::collections::HashMap;
use std::fs::File;
use std::io::BufReader;
use std::path::{Path, PathBuf};

/// Convert BGR into Rgb8
fn bgr_to_rgb8(bgr: i32) -> Rgb8 {
    let r = (bgr >> 16) as u8;
    let g = (bgr >> 8) as u8;
    let b = (bgr >> 0) as u8;
    Rgb8::new(r, g, b)
}

/// A character for a bitmap font
#[derive(Deserialize, Serialize)]
pub struct Character {
    number: u16,
    width: u8,
    #[serde(with = "b64")]
    bitmap: Vec<u8>,
}

/// A bitmap font
#[derive(Deserialize, Serialize)]
pub struct Font {
    name: String,
    number: u8,
    height: u8,
    char_spacing: u8,
    line_spacing: u8,
    characters: Vec<Character>,
    version_id: i32,
}

impl Character {
    /// Get the width
    pub fn width(&self) -> u8 {
        self.width
    }
    /// Render the character to a raster
    fn render_char(&self, page: &mut Raster<Rgb8>, x: u32, y: u32, height: u32,
        cf: Rgb8)
    {
        let width = self.width.into();
        debug!("render_char: {} @ {},{}", self.number, x, y);
        let mut xx = 0;
        let mut yy = 0;
        for by in &self.bitmap {
            for bi in 0..8 {
                if by >> (7 - bi) & 1u8 != 0u8 {
                    page.set_pixel(x + xx, y + yy, cf);
                }
                xx += 1;
                if xx >= width {
                    xx = 0;
                    yy += 1;
                    if yy >= height {
                        break;
                    }
                }
            }
        }
    }
}

impl<'a> Font {
    /// Load fonts from a JSON file
    pub fn load(dir: &Path) -> Result<HashMap<u8, Font>, Error> {
        debug!("Font::load");
        let mut n = PathBuf::new();
        n.push(dir);
        n.push("font");
        let r = BufReader::new(File::open(&n)?);
        let mut fonts = HashMap::new();
        let j: Vec<Font> = serde_json::from_reader(r)?;
        for f in j {
            fonts.insert(f.number, f);
        }
        Ok(fonts)
    }
    /// Get font name
    pub fn name(&self) -> &str {
        &self.name
    }
    /// Get font number
    pub fn number(&self) -> u8 {
        self.number
    }
    /// Get font height
    pub fn height(&self) -> u8 {
        self.height
    }
    /// Get line spacing
    pub fn line_spacing(&self) -> u8 {
        self.line_spacing
    }
    /// Get character spacing
    pub fn char_spacing(&self) -> u8 {
        self.char_spacing
    }
    /// Get a character
    pub fn character(&'a self, ch: char) -> Result<&'a Character, SyntaxError> {
        let code_point: u32 = ch.into();
        if code_point <= std::u16::MAX.into() {
            let n = code_point as u16;
            if let Some(c) = self.characters.iter().find(|c| c.number == n) {
                return Ok(c)
            }
        }
        Err(SyntaxError::CharacterNotDefined(ch))
    }
    /// Calculate the width of a span of text
    pub fn text_width(&self, text: &str, cs: Option<u16>)
        -> Result<u16, SyntaxError>
    {
        let mut width = 0;
        let cs = cs.unwrap_or(self.char_spacing.into());
        for ch in text.chars() {
            let c = self.character(ch)?;
            if width > 0 {
                width += cs;
            }
            width += <u16>::from(c.width());
        }
        Ok(width)
    }
    /// Render a text span
    pub fn render_text(&self, page: &mut Raster<Rgb8>, text: &str, x: u32,
        y: u32, cs: u32, cf: Rgb8) -> Result<(), SyntaxError>
    {
        let height = self.height() as u32;
        debug!("span: {}, left: {}, top: {}, height: {}", text, x, y, height);
        let mut xx = 0;
        for ch in text.chars() {
            let c = self.character(ch)?;
            if xx > 0 {
                xx += cs;
            }
            c.render_char(page, x + xx, y, height, cf);
            xx += <u32>::from(c.width());
        }
        Ok(())
    }
}

/// An uncompressed graphic
#[derive(Deserialize, Serialize)]
pub struct Graphic {
    name: String,
    g_number: i32,
    color_scheme: String,
    height: i32,
    width: i32,
    transparent_color: Option<i32>,
    #[serde(with = "b64")]
    bitmap: Vec<u8>,
}

/// Function to lookup a pixel from a graphic buffer
type PixFn = dyn Fn(&Graphic, u32, u32, &ColorCtx, &[u8]) -> Option<Rgb8>;

impl Graphic {
    /// Load graphics from a JSON file
    pub fn load(dir: &Path) -> Result<HashMap<i32, Graphic>, Error> {
        debug!("Graphic::load");
        let mut n = PathBuf::new();
        n.push(dir);
        n.push("graphic");
        let r = BufReader::new(File::open(&n)?);
        let mut graphics = HashMap::new();
        let j: Vec<Graphic> = serde_json::from_reader(r)?;
        for g in j {
            let gn = g.g_number;
            graphics.insert(gn, g);
        }
        Ok(graphics)
    }
    /// Get the graphic width
    pub fn width(&self) -> u32 {
        self.width as u32
    }
    /// Get the graphic height
    pub fn height(&self) -> u32 {
        self.height as u32
    }
    /// Render a graphic onto a Raster
    pub fn onto_raster(&self, page: &mut Raster<Rgb8>, x: u32, y: u32,
        ctx: &ColorCtx) -> Result<(), SyntaxError>
    {
        let x = x - 1; // x must be > 0
        let y = y - 1; // y must be > 0
        let w = self.width();
        let h = self.height();
        if x + w > page.width() || y + h > page.height() {
            // There is no GraphicTooBig syntax error
            return Err(SyntaxError::Other.into());
        }
        let pix_fn = self.pixel_fn();
        for yy in 0..h {
            for xx in 0..w {
                if let Some(clr) = pix_fn(self, xx, yy, ctx, &self.bitmap) {
                    page.set_pixel(x + xx, y + yy, clr);
                }
            }
        }
        Ok(())
    }
    /// Get pixel lookup function for the color scheme
    fn pixel_fn(&self) -> &PixFn {
        match self.color_scheme[..].into() {
            ColorScheme::Monochrome1Bit => &Graphic::pixel_1,
            ColorScheme::Monochrome8Bit |
            ColorScheme::ColorClassic => &Graphic::pixel_8,
            ColorScheme::Color24Bit => &Graphic::pixel_24,
        }
    }
    /// Get one pixel of a monochrome 1-bit graphic
    fn pixel_1(&self, x: u32, y: u32, ctx: &ColorCtx, buf: &[u8])
        -> Option<Rgb8>
    {
        let p = y * self.width() + x;
        let by = p as usize / 8;
        let bi = 7 - (p & 7);
        let lit = ((buf[by] >> bi) & 1) != 0;
        match (lit, self.transparent_color) {
            (false, Some(0)) => None,
            (true, Some(1)) => None,
            (false, _) => Some(bgr_to_rgb8(ctx.background_bgr())),
            (true, _) => Some(bgr_to_rgb8(ctx.foreground_bgr())),
        }
    }
    /// Get one pixel of an 8-bit (monochrome or classic) color graphic
    fn pixel_8(&self, x: u32, y: u32, ctx: &ColorCtx, buf: &[u8])
        -> Option<Rgb8>
    {
        let p = y * self.width() + x;
        let v = buf[p as usize];
        if let Some(tc) = self.transparent_color {
            if tc == v as i32 {
                return None;
            }
        }
        match ctx.rgb(Color::Legacy(v)) {
            Some(rgb) => Some(rgb.into()),
            None => {
                debug!("pixel_8 -- Bad color {}", v);
                None
            },
        }
    }
    /// Get one pixel of a 24-bit color graphic
    fn pixel_24(&self, x: u32, y: u32, _ctx: &ColorCtx, buf: &[u8])
        -> Option<Rgb8>
    {
        let p = 3 * (y * self.width() + x) as usize;
        let r = buf[p + 0];
        let g = buf[p + 1];
        let b = buf[p + 2];
        if let Some(tc) = self.transparent_color {
            let rgb = ((r as i32) << 16) + ((g as i32) << 8) + b as i32;
            if rgb == tc {
                return None;
            }
        }
        Some(Rgb8::new(r, g, b))
    }
}

/// Serialize base64 fields
mod b64 {
    use base64::display::Base64Display;
    use serde::{Serializer, de, Deserialize, Deserializer};

    pub fn serialize<S>(bytes: &[u8], serializer: S) -> Result<S::Ok, S::Error>
        where S: Serializer
    {
        serializer.collect_str(&Base64Display::with_config(bytes,
            base64::STANDARD))
    }

    pub fn deserialize<'de, D>(deserializer: D) -> Result<Vec<u8>, D::Error>
        where D: Deserializer<'de>
    {
        let s = <&str>::deserialize(deserializer)?;
        base64::decode(s).map_err(de::Error::custom)
    }
}
