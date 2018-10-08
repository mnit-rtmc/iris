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
use std::fs::{File,rename};
use std::io::{BufReader,BufWriter,Write};
use std::path::PathBuf;
use std::sync::mpsc::Sender;

#[derive(PartialEq,Eq,Hash)]
pub enum Resource {
    Simple(&'static str, &'static str),
    Font(&'static str),
    SignMsg(&'static str),
}

const CAMERA_RES: Resource = Resource::Simple(
"camera_pub",
"SELECT row_to_json(r)::text FROM (\
    SELECT name, publish, location, lat, lon \
    FROM camera_view \
    ORDER BY name \
) r",
);

const DMS_RES: Resource = Resource::Simple(
"dms_pub",
"SELECT row_to_json(r)::text FROM (\
    SELECT name, sign_config, roadway, road_dir, cross_street, \
           location, lat, lon \
    FROM dms_view \
    ORDER BY name \
) r",
);

const DMS_MSG_RES: Resource = Resource::Simple(
"dms_message",
"SELECT row_to_json(r)::text FROM (\
    SELECT name, msg_current, multi, sources, duration, expire_time \
    FROM dms_message_view WHERE condition = 'Active' \
    ORDER BY name \
) r",
);

const INCIDENT_RES: Resource = Resource::Simple(
"incident",
"SELECT row_to_json(r)::text FROM (\
    SELECT name, event_date, description, road, direction, lane_type, \
           impact, confirmed, camera, detail, replaces, lat, lon \
    FROM incident_view \
    WHERE cleared = false \
) r",
);

const SIGN_CONFIG_RES: Resource = Resource::Simple(
"sign_config",
"SELECT row_to_json(r)::text FROM (\
    SELECT name, dms_type, portable, technology, sign_access, legend, \
           beacon_type, face_width, face_height, border_horiz, \
           border_vert, pitch_horiz, pitch_vert, pixel_width, \
           pixel_height, char_width, char_height, color_scheme, \
           monochrome_foreground, monochrome_background, default_font \
    FROM sign_config_view \
) r",
);

const TPIMS_STAT_RES: Resource = Resource::Simple(
"TPIMS_static",
"SELECT row_to_json(r)::text FROM (\
    SELECT site_id AS \"siteId\", to_char(time_stamp_static AT TIME ZONE 'UTC', \
           'YYYY-mm-dd\"T\"HH24:MI:SSZ') AS \"timeStamp\", \
           relevant_highway AS \"relevantHighway\", \
           reference_post AS \"referencePost\", exit_id AS \"exitId\", \
           road_dir AS \"directionOfTravel\", facility_name AS name, \
           json_build_object('latitude', lat, 'longitude', lon, \
           'streetAdr', street_adr, 'city', city, 'state', state, \
           'zip', zip, 'timeZone', time_zone) AS location, \
           ownership, capacity, \
           string_to_array(amenities, ', ') AS amenities, \
           array_remove(ARRAY[camera_image_base_url || camera_1, \
           camera_image_base_url || camera_2, \
           camera_image_base_url || camera_3], NULL) AS images, \
           ARRAY[]::text[] AS logos \
    FROM parking_area_view \
) r",
);

const TPIMS_DYN_RES: Resource = Resource::Simple(
"TPIMS_dynamic",
"SELECT row_to_json(r)::text FROM (\
    SELECT site_id AS \"siteId\", to_char(time_stamp AT TIME ZONE 'UTC', \
           'YYYY-mm-dd\"T\"HH24:MI:SSZ') AS \"timeStamp\", \
           to_char(time_stamp_static AT TIME ZONE 'UTC', \
           'YYYY-mm-dd\"T\"HH24:MI:SSZ') AS \"timeStampStatic\", \
           reported_available AS \"reportedAvailable\", \
           trend, open, trust_data AS \"trustData\", capacity \
    FROM parking_area_view \
) r",
);

const FONT_RES: Resource = Resource::Font(
"font",
);

const SIGN_MSG_RES: Resource = Resource::SignMsg(
"sign_message",
);

fn query_simple<W: Write>(conn: &Connection, sql: &str, mut w: W)
    -> Result<u32, Error>
{
    let mut c = 0;
    w.write("[".as_bytes())?;
    for row in &conn.query(sql, &[])? {
        if c > 0 { w.write(",".as_bytes())?; }
        w.write("\n".as_bytes())?;
        let j: String = row.get(0);
        w.write(j.as_bytes())?;
        c += 1;
    }
    if c > 0 { w.write("\n".as_bytes())?; }
    w.write("]\n".as_bytes())?;
    Ok(c)
}

trait Queryable {
    fn sql() -> &'static str;
    fn from_row(row: &postgres::rows::Row) -> Self;
}

#[derive(Serialize,Deserialize)]
pub struct SignConfig {
    name        : String,
    dms_type    : String,
    portable    : bool,
    technology  : String,
    sign_access : String,
    legend      : String,
    beacon_type : String,
    face_width  : i32,
    face_height : i32,
    border_horiz: i32,
    border_vert : i32,
    pitch_horiz : i32,
    pitch_vert  : i32,
    pixel_width : i32,
    pixel_height: i32,
    char_width  : i32,
    char_height : i32,
    color_scheme: String,
    monochrome_foreground: i32,
    monochrome_background: i32,
    default_font: Option<String>,
}

impl SignConfig {
    fn load(dir: &str) -> Result<HashMap<String, SignConfig>, Error> {
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
}

impl Queryable for SignConfig {
     fn sql() -> &'static str {
       "SELECT name, dms_type, portable, technology, sign_access, legend, \
               beacon_type, face_width, face_height, border_horiz, border_vert, \
               pitch_horiz, pitch_vert, pixel_width, pixel_height, char_width, \
               char_height, color_scheme, monochrome_foreground, \
               monochrome_background, default_font \
        FROM sign_config_view"
     }
     fn from_row(row: &postgres::rows::Row) -> Self {
        SignConfig {
            name        : row.get(0),
            dms_type    : row.get(1),
            portable    : row.get(2),
            technology  : row.get(3),
            sign_access : row.get(4),
            legend      : row.get(5),
            beacon_type : row.get(6),
            face_width  : row.get(7),
            face_height : row.get(8),
            border_horiz: row.get(9),
            border_vert : row.get(10),
            pitch_horiz : row.get(11),
            pitch_vert  : row.get(12),
            pixel_width : row.get(13),
            pixel_height: row.get(14),
            char_width  : row.get(15),
            char_height : row.get(16),
            color_scheme: row.get(17),
            monochrome_foreground: row.get(18),
            monochrome_background: row.get(19),
            default_font: row.get(20),
        }
    }
}

#[derive(Serialize,Deserialize)]
struct Glyph {
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
struct Font {
    name         : String,
    f_number     : i32,
    height       : i32,
    width        : i32,
    line_spacing : i32,
    char_spacing : i32,
    glyphs       : Vec<Glyph>,
    version_id   : i32,
}

impl Font {
    fn load(dir: &str) -> Result<HashMap<i32, Font>, Error> {
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

fn query_font<W: Write>(conn: &Connection, mut w: W) -> Result<u32, Error> {
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

#[derive(Serialize)]
struct SignMessage {
    name          : String,
    sign_config   : String,
    incident      : Option<String>,
    multi         : String,
    beacon_enabled: bool,
    prefix_page   : bool,
    msg_priority  : i32,
    sources       : String,
    owner         : Option<String>,
    duration      : Option<i32>,
}

impl Queryable for SignMessage {
    fn sql() -> &'static str {
        "SELECT name, sign_config, incident, multi, beacon_enabled, \
                prefix_page, msg_priority, sources, owner, duration \
        FROM sign_message_view \
        ORDER BY name"
    }
    fn from_row(row: &postgres::rows::Row) -> Self {
        SignMessage {
            name          : row.get(0),
            sign_config   : row.get(1),
            incident      : row.get(2),
            multi         : row.get(3),
            beacon_enabled: row.get(4),
            prefix_page   : row.get(5),
            msg_priority  : row.get(6),
            sources       : row.get(7),
            owner         : row.get(8),
            duration      : row.get(9),
        }
    }
}

struct MsgData {
    configs: HashMap<String, SignConfig>,
    fonts: HashMap<i32, Font>,
    // FIXME: also need graphics
}

impl MsgData {
    fn load(dir: &str) -> Result<Self, Error> {
        let configs = SignConfig::load(dir)?;
        let fonts = Font::load(dir)?;
        Ok(MsgData {
            configs,
            fonts,
        })
    }
}

/// Check and fetch one sign message (into a .gif file).
fn fetch_sign_msg(s: &SignMessage, dir: &str, tx: &Sender<PathBuf>,
    msg_data: &MsgData) -> Result<(), Error>
{
    let cfg = msg_data.configs.get(&s.sign_config)
                              .ok_or_else(|| format_err!("No config"))?;
    println!("config: {}", cfg.name);
    let fonts = &msg_data.fonts;
    for (f_num, f) in fonts {
        println!("font: {} {:?}", f_num, f.name);
    }
    println!("sign_msg: {}", s.name);
    Ok(())
}

/// Query the sign messages.
///
/// * `conn` The database connection.
/// * `w` Writer for the file.
/// * `dir` Output file directory.
/// * `tx` Channel sender for resource file names.
fn query_sign_msg<W: Write>(conn: &Connection, mut w: W, dir: &str,
    tx: &Sender<PathBuf>) -> Result<u32, Error>
{
    let msg_data = MsgData::load(dir)?;
    let mut c = 0;
    w.write("[".as_bytes())?;
    for row in &conn.query(SignMessage::sql(), &[])? {
        if c > 0 { w.write(",".as_bytes())?; }
        w.write("\n".as_bytes())?;
        let mut s = SignMessage::from_row(&row);
        w.write(serde_json::to_string(&s)?.as_bytes())?;
        fetch_sign_msg(&s, dir, tx, &msg_data)?;
        c += 1;
    }
    w.write("]\n".as_bytes())?;
    Ok(c)
}

impl Resource {
    /// Fetch a file.
    ///
    /// * `conn` The database connection.
    /// * `w` Writer for the file.
    /// * `dir` Output file directory.
    /// * `tx` Channel sender for resource file names.
    fn fetch_file<W: Write>(&self, conn: &Connection, w: W, dir: &str,
        tx: &Sender<PathBuf>) -> Result<u32,Error>
    {
        match self {
            Resource::Simple(_, sql) => query_simple(conn, sql, w),
            Resource::Font(_)        => query_font(conn, w),
            Resource::SignMsg(_)     => query_sign_msg(conn, w, dir, tx),
        }
    }
    fn name(&self) -> &str {
        match self {
            Resource::Simple(name, _) => name,
            Resource::Font(name)      => name,
            Resource::SignMsg(name)   => name,
        }
    }
    fn make_name(&self, dir: &str) -> PathBuf {
        let mut t = PathBuf::new();
        t.push(dir);
        t.push(self.name());
        t
    }
    fn make_tmp_name(&self, dir: &str) -> PathBuf {
        let mut n = String::new();
        n.push('.');
        n.push_str(self.name());
        let mut t = PathBuf::new();
        t.push(dir);
        t.push(n);
        t
    }
    /// Fetch the resource and send PathBuf(s) to a channel.
    ///
    /// * `conn` The database connection.
    /// * `dir` Output file directory.
    /// * `tx` Channel sender for resource file names.
    pub fn fetch(&self, conn: &Connection, dir: &str, tx: &Sender<PathBuf>)
        -> Result<u32, Error>
    {
        let tn = self.make_tmp_name(dir);
        let n = self.make_name(dir);
        let f = BufWriter::new(File::create(&tn)?);
        let c = self.fetch_file(conn, f, dir, tx)?;
        rename(tn, &n)?;
        tx.send(n)?;
        Ok(c)
    }
}

pub fn lookup_resource(n: &str) -> Option<Resource> {
    match n {
        "camera_pub"           => Some(CAMERA_RES),
        "dms_pub"              => Some(DMS_RES),
        "dms_message"          => Some(DMS_MSG_RES),
        "incident"             => Some(INCIDENT_RES),
        "sign_config"          => Some(SIGN_CONFIG_RES),
        "parking_area" |
        "TPIMS_static"         => Some(TPIMS_STAT_RES),
        "parking_area_dynamic"|
        "TPIMS_dynamic"        => Some(TPIMS_DYN_RES),
        "font"                 => Some(FONT_RES),
        "sign_message"         => Some(SIGN_MSG_RES),
        _                      => None,
    }
}

pub const ALL: [&'static str; 9] = [
    "camera_pub",
    "dms_pub",
    "dms_message",
    "incident",
    "sign_config",
    "parking_area",
    "parking_area_dynamic",
    "font",
    "sign_message",
];
