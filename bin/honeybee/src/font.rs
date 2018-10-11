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
use failure::Error;
use postgres;
use postgres::{Connection};
use serde_json;
use std::collections::HashMap;
use std::fs::File;
use std::io::{BufReader,Write};
use std::path::{Path,PathBuf};
use resource::Queryable;

#[derive(Serialize,Deserialize)]
pub struct Glyph {
    code_point : i32,
    width      : i32,
    pixels     : String,
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

impl Font {
    pub fn load(dir: &Path) -> Result<HashMap<i32, Font>, Error> {
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
