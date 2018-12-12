/*
 * Copyright (C) 2018  Minnesota Department of Transportation
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
use base64::{Config,CharacterSet,LineWrap,decode_config_slice};
use failure::Error;
use postgres;
use postgres::{Connection};
use serde_json;
use std::collections::HashMap;
use std::fs::File;
use std::io::{BufReader,Write};
use std::path::{Path,PathBuf};
use raster::Raster;
use resource::Queryable;
use multi::{ColorClassic,ColorScheme,SyntaxError};

#[derive(Serialize,Deserialize)]
pub struct Glyph {
    pub code_point : i32,
    width          : i32,
    pub pixels     : String,
}

impl Queryable for Glyph {
    fn sql() -> &'static str {
       "SELECT code_point, width, pixels \
        FROM glyph_view \
        WHERE font = ($1) \
        ORDER BY code_point"
    }
    fn from_row(row: &postgres::rows::Row) -> Self {
        Glyph {
            code_point : row.get(0),
            width      : row.get(1),
            pixels     : row.get(2),
        }
    }
}

impl Glyph {
    pub fn width(&self) -> u16 {
        self.width as u16
    }
}

#[derive(Serialize,Deserialize)]
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
    pub fn height(&self) -> u16 {
        self.height as u16
    }
    pub fn line_spacing(&self) -> u16 {
        self.line_spacing as u16
    }
    pub fn char_spacing(&self) -> u16 {
        self.char_spacing as u16
    }
    pub fn glyph(&'a self, cp: char) -> Result<&'a Glyph, SyntaxError> {
        match self.glyphs.iter().find(|g| g.code_point == cp as i32) {
            Some(g) => Ok(&g),
            None    => Err(SyntaxError::CharacterNotDefined(cp)),
        }
    }
}

impl Queryable for Font {
    fn sql() -> &'static str {
       "SELECT name, f_number, height, width, line_spacing, char_spacing, \
               version_id \
        FROM font_view \
        ORDER BY name"
    }
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

#[derive(Serialize,Deserialize)]
pub struct Graphic {
    name             : String,
    g_number         : i32,
    color_scheme     : String,
    height           : i32,
    width            : i32,
    transparent_color: Option<i32>,
    pixels           : String,
}

impl Graphic {
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
    pub fn width(&self) -> u32 {
        self.width as u32
    }
    pub fn height(&self) -> u32 {
        self.height as u32
    }
    fn bits_per_pixel(&self) -> Result<u32, Error> {
        let cs = ColorScheme::from_str(&self.color_scheme)?;
        Ok(match cs {
            ColorScheme::Monochrome1Bit => 1,
            ColorScheme::Monochrome8Bit => 8,
            ColorScheme::ColorClassic   => 8,
            ColorScheme::Color24Bit     => 24,
        })
    }
    /// Render a graphic
    pub fn render(&self, page: &mut Raster, cf: [u8;3], x: u32, y: u32)
        -> Result<(), Error>
    {
        let config = Config::new(CharacterSet::Standard, false, true,
            LineWrap::NoWrap);
        let cs = ColorScheme::from_str(&self.color_scheme)?;
        let bpp = self.bits_per_pixel()?;
        let w = self.width();
        let h = self.height();
        if x + w > page.width() || y + h > page.height() {
            // There is no GraphicTooBig syntax error
            return Err(SyntaxError::Other.into());
        }
        let n = (w * h * bpp + 7) / 8;
        let mut buf = vec!(0; n as usize);
        let n = decode_config_slice(&self.pixels, config, &mut buf)?;
        debug!("graphic: {}, width: {}, height: {}, len: {}", self.g_number,
            w, h, n);
        for yy in 0..h {
            for xx in 0..w {
                if let Some(clr) = self.get_pixel(cs, &buf, xx, yy, cf) {
                    page.set_pixel(x + xx - 1, y + yy - 1, clr);
                }
            }
        }
        Ok(())
    }
    fn get_pixel(&self, cs: ColorScheme, buf: &[u8], x: u32, y: u32, cf: [u8;3])
        -> Option<[u8;3]>
    {
        match cs {
            ColorScheme::Monochrome1Bit => self.get_pixel_1(buf, x, y, cf),
            ColorScheme::Monochrome8Bit => self.get_pixel_8(buf, x, y),
            ColorScheme::ColorClassic   => self.get_pixel_classic(buf, x, y),
            ColorScheme::Color24Bit     => self.get_pixel_24(buf, x, y),
        }
    }
    fn get_pixel_1(&self, buf: &[u8], x: u32, y: u32, cf: [u8;3])
        -> Option<[u8;3]>
    {
        let p = y * self.width() + x;
        let by = p as usize / 8;
        let bi = 7 - (p & 7);
        let lit = ((buf[by] >> bi) & 1) != 0;
        // FIXME: background color?
        let cb = [0, 0, 0];
        if lit {
            Some(cf)
        } else {
            if let Some(tc) = self.transparent_color {
                let r = (tc >> 16 & 0xFF) as u8;
                let g = (tc >>  8 & 0xFF) as u8;
                let b = (tc >>  0 & 0xFF) as u8;
                if [r,g,b] == cb { None } else { Some(cb) }
            } else {
                Some(cb)
            }
        }
    }
    fn get_pixel_8(&self, buf: &[u8], x: u32, y: u32) -> Option<[u8;3]> {
        let p = y * self.width() + x;
        let v = buf[p as usize];
        Some([v, v, v])
    }
    fn get_pixel_classic(&self, buf: &[u8], x: u32, y: u32) -> Option<[u8;3]> {
        let p = y * self.width() + x;
        let v = buf[p as usize];
        // FIXME: improve error handling
        let c = ColorClassic::from_u8(v).unwrap();
        Some(c.rgb())
    }
    fn get_pixel_24(&self, buf: &[u8], x: u32, y: u32) -> Option<[u8;3]> {
        let p = (y * self.width() + x) * 3;
        let r = buf[(p + 0) as usize];
        let g = buf[(p + 1) as usize];
        let b = buf[(p + 2) as usize];
        Some([r, g, b])
    }
}
