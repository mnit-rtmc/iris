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
use fallible_iterator::FallibleIterator;
use postgres;
use postgres::{Connection, TlsMode};
use serde_json;
use std::collections::HashSet;
use std::fs::{File,rename};
use std::path::PathBuf;
use std::io::{BufWriter,Write};
use std::time::{Duration,Instant};

//static OUTPUT_DIR: &str = "/var/www/html/iris/";
static OUTPUT_DIR: &str = "iris";

fn make_name(n: &str) -> PathBuf {
    let mut t = PathBuf::new();
    t.push(OUTPUT_DIR);
    t.push(n);
    t
}

fn make_tmp_name(n: &str) -> PathBuf {
    let mut nm = String::new();
    nm.push('.');
    nm.push_str(n);
    make_name(&nm)
}

enum Resource {
    Simple(&'static str, &'static str),
    Font(&'static str),
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
           monochrome_foreground, monochrome_background \
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

#[derive(Serialize)]
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

#[derive(Serialize)]
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

impl Resource {
    fn fetch<W: Write>(&self, conn: &Connection, w: W) -> Result<u32,Error>{
        match self {
            Resource::Simple(_, sql) => query_simple(conn, sql, w),
            Resource::Font(_)        => query_font(conn, w),
        }
    }
    fn name(&self) -> &str {
        match self {
            Resource::Simple(name, _) => name,
            Resource::Font(name)      => name,
        }
    }
    fn fetch_file(&self, conn: &Connection) -> Result<u32, Error> {
        let tn = make_tmp_name(self.name());
        let f = BufWriter::new(File::create(&tn)?);
        let c = self.fetch(conn, f)?;
        rename(tn, make_name(self.name()))?;
        Ok(c)
    }
}

fn lookup_resource(n: &str) -> Option<Resource> {
    match n {
        "camera_pub"           => Some(CAMERA_RES),
        "dms_pub"              => Some(DMS_RES),
        "dms_message"          => Some(DMS_MSG_RES),
        "incident"             => Some(INCIDENT_RES),
        "sign_config"          => Some(SIGN_CONFIG_RES),
        "parking_area_static"  => Some(TPIMS_STAT_RES),
        "parking_area_dynamic" => Some(TPIMS_DYN_RES),
        "font"                 => Some(FONT_RES),
        _                      => None,
    }
}

pub fn start(uds: String) -> Result<(), Error> {
    let conn = Connection::connect(uds, TlsMode::None)?;
    // The postgresql crate sets the session time zone to UTC.
    // We need to set it back to LOCAL time zone, so that row_to_json
    // can format properly (for incidents, etc).
    conn.execute("SET TIME ZONE 'US/Central'", &[])?;
    conn.execute("LISTEN tms", &[])?;
    // Initialize all the json files
    for r in ["camera_pub", "dms_pub", "dms_message", "incident", "sign_config",
              "parking_area_static", "parking_area_dynamic", "font"].iter()
    {
        query_json_timed(&conn, r)?;
    }
    notify_loop(&conn)
}

fn query_json_timed(conn: &Connection, n: &str)
    -> Result<(), Error>
{
    let s = Instant::now();
    if let Some(c) = fetch_resource_file(&conn, &n)? {
        println!("{}: wrote {} rows in {:?}", &n, c, s.elapsed());
    } else {
        println!("{}: unknown resource", &n);
    }
    Ok(())
}

fn fetch_resource_file(conn: &Connection, n: &str)
    -> Result<Option<u32>, Error>
{
    if let Some(r) = lookup_resource(n) {
        Ok(Some(r.fetch_file(&conn)?))
    } else {
        Ok(None)
    }
}

fn notify_loop(conn: &Connection) -> Result<(), Error> {
    let nots = conn.notifications();
    let mut s = HashSet::new();
    loop {
        for n in nots.timeout_iter(Duration::from_millis(300)).iterator() {
            let n = n?;
            s.insert(n.payload);
        }
        for n in s.drain() {
            query_json_timed(&conn, &n)?;
        }
    }
}
