/*
 * Copyright (C) 2018-2019  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
use base64::{Config, CharacterSet, LineWrap, decode_config_slice};
use postgres::{self, Connection};
use serde_json;
use std::collections::HashMap;
use std::fs::File;
use std::io::{BufReader, Write};
use std::path::{Path, PathBuf};
use crate::error::Error;
use crate::raster::{Raster, Rgb24};
use crate::resource::Queryable;
use crate::multi::{Color, ColorCtx, ColorScheme, SyntaxError};

/// Length of base64 output buffer for glyphs.
/// Encoded glyphs are restricted to 128 bytes.
const GLYPH_LEN: usize = (128 + 3) / 4 * 3;

/// A bitmap font glyph
#[derive(Serialize, Deserialize)]
pub struct Glyph {
    pub code_point : i32,
    width          : i32,
    pub pixels     : String,
}

impl Queryable for Glyph {
    /// Get the SQL to query all glyphs in a font
    fn sql() -> &'static str {
       "SELECT code_point, width, pixels \
        FROM glyph_view \
        WHERE font = ($1) \
        ORDER BY code_point"
    }
    /// Produce a glyph from one Row
    fn from_row(row: &postgres::rows::Row) -> Self {
        Glyph {
            code_point : row.get(0),
            width      : row.get(1),
            pixels     : row.get(2),
        }
    }
}

impl Glyph {
    /// Get the glyph width
    pub fn width(&self) -> u16 {
        self.width as u16
    }
}

/// A bitmap font
#[derive(Serialize, Deserialize)]
pub struct Font {
    pub name     : String,
    pub f_number : i32,
    height       : i32,
    width        : i32,
    line_spacing : i32,
    char_spacing : i32,
    glyphs       : Vec<Glyph>,
    version_id   : i32,
}

impl<'a> Font {
    /// Load fonts from a JSON file
    pub fn load(dir: &Path) -> Result<HashMap<i32, Font>, Error> {
        debug!("Font::load");
        let mut n = PathBuf::new();
        n.push(dir);
        n.push("font");
        let r = BufReader::new(File::open(&n)?);
        let mut fonts = HashMap::new();
        let j: Vec<Font> = serde_json::from_reader(r)?;
        for f in j {
            fonts.insert(f.f_number, f);
        }
        Ok(fonts)
    }
    /// Get font height
    pub fn height(&self) -> u16 {
        self.height as u16
    }
    /// Get line spacing
    pub fn line_spacing(&self) -> u16 {
        self.line_spacing as u16
    }
    /// Get character spacing
    pub fn char_spacing(&self) -> u16 {
        self.char_spacing as u16
    }
    /// Get glyph for a code point
    pub fn glyph(&'a self, cp: char) -> Result<&'a Glyph, SyntaxError> {
        match self.glyphs.iter().find(|g| g.code_point == cp as i32) {
            Some(g) => Ok(&g),
            None    => Err(SyntaxError::CharacterNotDefined(cp)),
        }
    }
    /// Render a text span
    pub fn render_text(&self, page: &mut Raster, text: &str, mut x: u32, y: u32,
        cs: u32, cf: Rgb24) -> Result<(), Error>
    {
        let h = self.height() as u32;
        debug!("span: {}, left: {}, top: {}, height: {}", text, x, y, h);
        let config = Config::new(CharacterSet::Standard, false, true,
            LineWrap::NoWrap);
        let mut buf = [0; GLYPH_LEN];
        for c in text.chars() {
            let g = self.glyph(c)?;
            let w = g.width() as u32;
            let n = decode_config_slice(&g.pixels, config, &mut buf)?;
            debug!("char: {}, width: {}, len: {}", c, w, n);
            for yy in 0..h {
                for xx in 0..w {
                    let p = yy * w + xx;
                    let by = p as usize / 8;
                    let bi = 7 - (p & 7);
                    let lit = ((buf[by] >> bi) & 1) != 0;
                    if lit {
                        page.set_pixel(x + xx, y + yy, cf);
                    }
                }
            }
            x += w + cs;
        }
        Ok(())
    }
}

impl Queryable for Font {
    /// Get the SQL to query all fonts
    fn sql() -> &'static str {
       "SELECT name, f_number, height, width, line_spacing, char_spacing, \
               version_id \
        FROM font_view \
        ORDER BY name"
    }
    /// Produce a font from one Row
    fn from_row(row: &postgres::rows::Row) -> Self {
        Font {
            name        : row.get(0),
            f_number    : row.get(1),
            height      : row.get(2),
            width       : row.get(3),
            line_spacing: row.get(4),
            char_spacing: row.get(5),
            version_id  : row.get(6),
            glyphs      : vec!(),
        }
    }
}

/// Query all fonts from DB
pub fn query_font<W: Write>(conn: &Connection, mut w: W) -> Result<u32, Error> {
    let mut c = 0;
    w.write("[".as_bytes())?;
    for row in &conn.query(Font::sql(), &[])? {
        if c > 0 { w.write(",".as_bytes())?; }
        w.write("\n".as_bytes())?;
        let mut f = Font::from_row(&row);
        for r2 in &conn.query(Glyph::sql(), &[&f.name])? {
            let g = Glyph::from_row(&r2);
            f.glyphs.push(g);
        }
        w.write(serde_json::to_string(&f)?.as_bytes())?;
        c += 1;
    }
    w.write("]\n".as_bytes())?;
    Ok(c)
}

/// An uncompressed graphic
#[derive(Serialize, Deserialize)]
pub struct Graphic {
    name             : String,
    g_number         : i32,
    color_scheme     : String,
    height           : i32,
    width            : i32,
    transparent_color: Option<i32>,
    pixels           : String,
}

/// Function to lookup a pixel from a graphic buffer
type PixFn = Fn(&Graphic, u32, u32, &ColorCtx, &[u8]) -> Option<Rgb24>;

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
    /// Get the number of bits per pixel
    fn bits_per_pixel(&self) -> u32 {
        match self.color_scheme[..].into() {
            ColorScheme::Monochrome1Bit => 1,
            ColorScheme::Monochrome8Bit => 8,
            ColorScheme::ColorClassic   => 8,
            ColorScheme::Color24Bit     => 24,
        }
    }
    /// Render a graphic onto a Raster
    pub fn onto_raster(&self, page: &mut Raster, x: u32, y: u32, ctx: &ColorCtx)
        -> Result<(), Error>
    {
        let x = x - 1; // x must be > 0
        let y = y - 1; // y must be > 0
        let w = self.width();
        let h = self.height();
        if x + w > page.width() || y + h > page.height() {
            // There is no GraphicTooBig syntax error
            return Err(SyntaxError::Other.into());
        }
        let buf = self.decode_base64()?;
        let pix_fn = self.pixel_fn();
        for yy in 0..h {
            for xx in 0..w {
                if let Some(clr) = pix_fn(self, xx, yy, ctx, &buf) {
                    page.set_pixel(x + xx, y + yy, clr);
                }
            }
        }
        Ok(())
    }
    /// Decode base64 data of graphic
    fn decode_base64(&self) -> Result<Vec<u8>, Error> {
        let config = Config::new(CharacterSet::Standard, false, true,
            LineWrap::NoWrap);
        let bpp = self.bits_per_pixel();
        let w = self.width();
        let h = self.height();
        let n = (w * h * bpp + 7) / 8;
        let mut buf = vec!(0; n as usize);
        let n = decode_config_slice(&self.pixels, config, &mut buf)?;
        debug!("graphic: {}, width: {}, height: {}, len: {}", self.g_number,
            w, h, n);
        Ok(buf)
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
        -> Option<Rgb24>
    {
        let p = y * self.width() + x;
        let by = p as usize / 8;
        let bi = 7 - (p & 7);
        let lit = ((buf[by] >> bi) & 1) != 0;
        match (lit, self.transparent_color) {
            (false, Some(0)) => None,
            (true, Some(1)) => None,
            (false, _) => Some(ctx.background_rgb().into()),
            (true, _) => Some(ctx.foreground_rgb().into()),
        }
    }
    /// Get one pixel of an 8-bit (monochrome or classic) color graphic
    fn pixel_8(&self, x: u32, y: u32, ctx: &ColorCtx, buf: &[u8])
        -> Option<Rgb24>
    {
        let p = y * self.width() + x;
        let v = buf[p as usize];
        if let Some(tc) = self.transparent_color {
            if tc == v as i32 {
                return None;
            }
        }
        match ctx.rgb(Color::Legacy(v)).ok() {
            Some(rgb) => Some(rgb.into()),
            None => {
                debug!("pixel_8 -- Bad color {}", v);
                None
            },
        }
    }
    /// Get one pixel of a 24-bit color graphic
    fn pixel_24(&self, x: u32, y: u32, _ctx: &ColorCtx, buf: &[u8])
        -> Option<Rgb24>
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
        Some(Rgb24::new(r, g, b))
    }
}
