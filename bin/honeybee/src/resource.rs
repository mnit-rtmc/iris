// resource.rs
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
use crate::error::{Error, Result};
use crate::font::{Font, query_font, Graphic};
use crate::multi::{ColorClassic, ColorScheme, LineJustification,
                   PageJustification, Rectangle};
use crate::signmsg;
use postgres::{Connection, rows::Row};
use std::collections::{HashMap, HashSet};
use std::convert::TryInto;
use std::fs::{File, rename, remove_file, read_dir};
use std::io::{BufReader, BufWriter, Write};
use std::path::{Path, PathBuf};
use std::time::Instant;

/// Make a PathBuf from a Path and file name
fn make_name(dir: &Path, n: &str) -> PathBuf {
    let mut p = PathBuf::new();
    p.push(dir);
    p.push(n);
    p
}

/// Make a PathBuf for a temp file
fn make_tmp_name(dir: &Path, n: &str) -> PathBuf {
    let mut b = String::new();
    b.push('.');
    b.push_str(n);
    make_name(dir, &b)
}

/// A resource can produce a JSON array of records.
#[derive(PartialEq, Eq, Hash)]
pub enum Resource {
    /// Simple file resource
    Simple(&'static str, Option<&'static str>, &'static str),
    /// Font resource
    Font(&'static str),
    /// Sign message resource
    SignMsg(&'static str),
}

/// R_Node resource
const R_NODE_RES: Resource = Resource::Simple(
"r_node", None,
"SELECT row_to_json(r)::text FROM (\
    SELECT name, roadway, road_dir, cross_mod, cross_street, cross_dir, \
           landmark, lat, lon, node_type, pickable, above, transition, lanes, \
           attach_side, shift, active, abandoned, station_id, speed_limit, \
           notes \
    FROM r_node_view \
) r",
);

/// Camera resource
const CAMERA_RES: Resource = Resource::Simple(
"camera_pub", Some("camera"),
"SELECT row_to_json(r)::text FROM (\
    SELECT name, publish, location, lat, lon \
    FROM camera_view \
    ORDER BY name \
) r",
);

/// DMS attribute resource
pub const DMS_ATTRIBUTE_RES: Resource = Resource::Simple(
"dms_attribute", None,
"SELECT row_to_json(r)::text FROM (\
    SELECT name, value \
    FROM dms_attribute_view \
) r",
);

/// DMS resource
const DMS_RES: Resource = Resource::Simple(
"dms_pub", Some("dms"),
"SELECT row_to_json(r)::text FROM (\
    SELECT name, sign_config, sign_detail, roadway, road_dir, cross_street, \
           location, lat, lon \
    FROM dms_view \
    ORDER BY name \
) r",
);

/// DMS message resource
pub const DMS_MSG_RES: Resource = Resource::Simple(
"dms_message", None,
"SELECT row_to_json(r)::text FROM (\
    SELECT name, msg_current, sources, duration, expire_time \
    FROM dms_message_view WHERE condition = 'Active' \
    ORDER BY name \
) r",
);

/// Incident resource
const INCIDENT_RES: Resource = Resource::Simple(
"incident", None,
"SELECT row_to_json(r)::text FROM (\
    SELECT name, event_date, description, road, direction, lane_type, \
           impact, confirmed, camera, detail, replaces, lat, lon \
    FROM incident_view \
    WHERE cleared = false \
) r",
);

/// Sign configuration resource
const SIGN_CONFIG_RES: Resource = Resource::Simple(
"sign_config", None,
"SELECT row_to_json(r)::text FROM (\
    SELECT name, face_width, face_height, border_horiz, border_vert, \
           pitch_horiz, pitch_vert, pixel_width, pixel_height, \
           char_width, char_height, monochrome_foreground, \
           monochrome_background, color_scheme, default_font \
    FROM sign_config_view \
) r",
);

/// Sign detail resource
const SIGN_DETAIL_RES: Resource = Resource::Simple(
"sign_detail", None,
"SELECT row_to_json(r)::text FROM (\
    SELECT name, dms_type, portable, technology, sign_access, legend, \
           beacon_type, hardware_make, hardware_model, software_make, \
           software_model, supported_tags, max_pages, max_multi_len \
    FROM sign_detail_view \
) r",
);

/// Static parking area resource
const TPIMS_STAT_RES: Resource = Resource::Simple(
"TPIMS_static", Some("parking_area"),
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

/// Dynamic parking area resource
pub const TPIMS_DYN_RES: Resource = Resource::Simple(
"TPIMS_dynamic", Some("parking_area_dynamic"),
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

/// Archive parking area resource
pub const TPIMS_ARCH_RES: Resource = Resource::Simple(
"TPIMS_archive", Some("parking_area_archive"),
"SELECT row_to_json(r)::text FROM (\
    SELECT site_id AS \"siteId\", to_char(time_stamp AT TIME ZONE 'UTC', \
           'YYYY-mm-dd\"T\"HH24:MI:SSZ') AS \"timeStamp\", \
           to_char(time_stamp_static AT TIME ZONE 'UTC', \
           'YYYY-mm-dd\"T\"HH24:MI:SSZ') AS \"timeStampStatic\", \
           reported_available AS \"reportedAvailable\", \
           trend, open, trust_data AS \"trustData\", capacity, \
           last_verification_check AS \"lastVerificationCheck\", \
           verification_check_amplitude AS \"verificationCheckAmplitude\", \
           low_threshold AS \"lowThreshold\", \
           true_available AS \"trueAvailable\" \
    FROM parking_area_view \
) r",
);

/// Graphic resource
const GRAPHIC_RES: Resource = Resource::Simple(
"graphic", None,
"SELECT row_to_json(r)::text FROM (\
    SELECT name, g_number, color_scheme, height, width, \
           transparent_color, replace(pixels, E'\n', '') AS pixels \
    FROM graphic_view \
) r",
);

/// Font resource
pub const FONT_RES: Resource = Resource::Font("font");

/// Sign message resource
const SIGN_MSG_RES: Resource = Resource::SignMsg("sign_message");

/// All defined resources
pub const ALL: &[Resource] = &[
    CAMERA_RES,
    DMS_ATTRIBUTE_RES,
    DMS_RES,
    DMS_MSG_RES,
    INCIDENT_RES,
    R_NODE_RES,
    SIGN_CONFIG_RES,
    SIGN_DETAIL_RES,
    TPIMS_STAT_RES,
    TPIMS_DYN_RES,
    TPIMS_ARCH_RES,
    GRAPHIC_RES,
    FONT_RES,
    SIGN_MSG_RES,
];

/// Lookup a resource by name (or alternate name)
pub fn lookup(n: &str) -> Option<&'static Resource> {
    ALL.iter().find(|r| r.matches(n))
}

/// Query a simple resource.
///
/// * `conn` The database connection.
/// * `sql` SQL query.
/// * `w` Writer to output resource.
fn query_simple<W: Write>(conn: &Connection, sql: &str, mut w: W)
    -> Result<u32>
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

/// DMS attribute
#[derive(Serialize, Deserialize)]
struct DmsAttribute {
    name  : String,
    value : String,
}

impl DmsAttribute {
    /// Load DMS attributes from a JSON file
    fn load(dir: &Path) -> Result<HashMap<String, DmsAttribute>> {
        debug!("DmsAttribute::load");
        let mut n = PathBuf::new();
        n.push(dir);
        n.push("dms_attribute");
        let r = BufReader::new(File::open(&n)?);
        let mut attrs = HashMap::new();
        let j: Vec<DmsAttribute> = serde_json::from_reader(r)?;
        for da in j {
            let an = da.name.clone();
            attrs.insert(an, da);
        }
        Ok(attrs)
    }
}

/// Sign configuration
#[derive(Serialize, Deserialize)]
pub struct SignConfig {
    name        : String,
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
    monochrome_foreground: i32,
    monochrome_background: i32,
    color_scheme: String,
    default_font: Option<String>,
}

impl SignConfig {
    /// Load sign configurations from a JSON file
    fn load(dir: &Path) -> Result<HashMap<String, SignConfig>> {
        debug!("SignConfig::load");
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
    pub fn name(&self) -> &str {
        &self.name
    }
    /// Get default font
    pub fn default_font(&self) -> Option<&str> {
        self.default_font.as_ref().map(String::as_str)
    }
    /// Get the horizontal border (mm).
    /// Sanity check included in case the sign vendor supplies stupid values.
    fn horizontal_border_mm(&self) -> f32 {
        let excess_mm = self.horizontal_excess_mm();
        let border_horiz = self.border_horiz as f32;
        border_horiz.min(0_f32.max(excess_mm / 2.0))
    }
    /// Get the face width (mm)
    pub fn face_width(&self) -> f32 {
        self.face_width as f32
    }
    /// Get the face height (mm)
    pub fn face_height(&self) -> f32 {
        self.face_height as f32
    }
    /// Get the horizontal excess (mm).
    fn horizontal_excess_mm(&self) -> f32 {
        let pixels_mm = (self.pitch_horiz * (self.pixel_width - 1)) as f32;
        self.face_width() - pixels_mm
    }
    /// Get the character height
    pub fn char_height(&self) -> Result<u8> {
        Ok(self.char_height.try_into()?)
    }
    /// Get the character width
    pub fn char_width(&self) -> Result<u8> {
        Ok(self.char_width.try_into()?)
    }
    /// Get the horizontal character offset (mm)
    fn char_offset_mm(&self, x: u32) -> f32 {
        if self.char_width > 1 {
            let gap = (x / self.char_width as u32) as f32;
            gap * self.char_gap_mm()
        } else {
            0.0
        }
    }
    /// Get the character gap (mm)
    fn char_gap_mm(&self) -> f32 {
        let excess_mm = self.horizontal_excess_mm();
        let border_mm = self.horizontal_border_mm() * 2.0;
        let gaps = self.char_gaps() as f32;
        if excess_mm > border_mm && gaps > 0.0 {
            (excess_mm - border_mm) / gaps
        } else {
            0.0
        }
    }
    /// Get the number of gaps between characters
    fn char_gaps(&self) -> i32 {
        if self.char_width > 1 && self.pixel_width > self.char_width {
            (self.pixel_width / self.char_width) - 1
        } else {
            0
        }
    }
    /// Get the X-position of a pixel on the sign (from 0 to 1)
    pub fn pixel_x(&self, x: u32) -> f32 {
        let hb = self.horizontal_border_mm();
        let co = self.char_offset_mm(x);
        let pos = hb + co + (self.pitch_horiz * x as i32) as f32;
        pos / self.face_width()
    }
    /// Get the vertical border (mm).
    /// Sanity check included in case the sign vendor supplies stupid values.
    fn vertical_border_mm(&self) -> f32 {
        let excess_mm = self.vertical_excess_mm();
        let border_vert = self.border_vert as f32;
        border_vert.min(0_f32.max(excess_mm / 2.0))
    }
    /// Get the vertical excess (mm).
    fn vertical_excess_mm(&self) -> f32 {
        let pixels_mm = (self.pitch_vert * (self.pixel_height - 1)) as f32;
        self.face_height() - pixels_mm
    }
    /// Get the number of gaps between lines
    fn line_gaps(&self) -> i32 {
        if self.char_height > 1 && self.pixel_height > self.char_height {
            (self.pixel_height / self.char_height) - 1
        } else {
            0
        }
    }
    /// Get the vertical line offset (mm)
    fn line_offset_mm(&self, y: u32) -> f32 {
        if self.char_height > 1 {
            let gap = (y / self.char_height as u32) as f32;
            gap * self.line_gap_mm()
        } else {
            0.0
        }
    }
    /// Get the line gap (mm)
    fn line_gap_mm(&self) -> f32 {
        let excess_mm = self.vertical_excess_mm();
        let border_mm = self.vertical_border_mm() * 2.0;
        let gaps = self.line_gaps() as f32;
        if excess_mm > border_mm && gaps > 0.0 {
            (excess_mm - border_mm) / gaps
        } else {
            0.0
        }
    }
    /// Get the Y-position of a pixel on the sign (from 0 to 1)
    pub fn pixel_y(&self, y: u32) -> f32 {
        let vb = self.vertical_border_mm();
        let lo = self.line_offset_mm(y);
        let pos = vb + lo + (self.pitch_vert * y as i32) as f32;
        pos / self.face_height()
    }
    /// Get the color scheme value
    pub fn color_scheme(&self) -> ColorScheme {
        self.color_scheme[..].into()
    }
    /// Get the default foreground color
    pub fn foreground_default(&self) -> i32 {
        match self.color_scheme() {
            ColorScheme::ColorClassic |
            ColorScheme::Color24Bit => ColorClassic::Amber.rgb(),
            _ => self.monochrome_foreground,
        }
    }
    /// Get the default background color
    pub fn background_default(&self) -> i32 {
        match self.color_scheme() {
            ColorScheme::ColorClassic |
            ColorScheme::Color24Bit => ColorClassic::Black.rgb(),
            _ => self.monochrome_background,
        }
    }
    /// Get the default text rectangle
    pub fn text_rect_default(&self) -> Result<Rectangle> {
        let w = self.pixel_width.try_into()?;
        let h = self.pixel_height.try_into()?;
        Ok(Rectangle::new(1, 1, w, h))
    }
}

/// Sign detail
#[derive(Serialize, Deserialize)]
struct SignDetail {
    name           : String,
    dms_type       : String,
    portable       : bool,
    technology     : String,
    sign_access    : String,
    legend         : String,
    beacon_type    : String,
    hardware_make  : String,
    hardware_model : String,
    software_make  : String,
    software_model : String,
    supported_tags : i32,
    max_pages      : i32,
    max_multi_len  : i32,
}

/// Sign message
#[derive(Serialize)]
pub struct SignMessage {
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

/// A trait to query a DB and produce a struct from each returned Row.
pub trait Queryable {
    /// Get SQL query string
    fn sql() -> &'static str;
    /// Create Self from a Row
    fn from_row(row: &Row) -> Self;
}

impl Queryable for SignMessage {
    /// Get the SQL to query all sign messages
    fn sql() -> &'static str {
        "SELECT name, sign_config, incident, multi, beacon_enabled, \
                prefix_page, msg_priority, sources, owner, duration \
        FROM sign_message_view \
        ORDER BY name"
    }
    /// Produce a sign message from one Row
    fn from_row(row: &Row) -> Self {
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

impl SignMessage {
    /// Get the MULTI string
    pub fn multi(&self) -> &str {
        &self.multi
    }
}

/// Data needed for rendering sign messages
pub struct MsgData {
    attrs   : HashMap<String, DmsAttribute>,
    configs : HashMap<String, SignConfig>,
    fonts   : HashMap<i32, Font>,
    graphics: HashMap<i32, Graphic>,
    gifs    : HashSet<PathBuf>,
}

impl MsgData {
    /// Load message data from a file path
    fn load(dir: &Path) -> Result<Self> {
        debug!("MsgData::load");
        let attrs = DmsAttribute::load(dir)?;
        let configs = SignConfig::load(dir)?;
        let fonts = Font::load(dir)?;
        let graphics = Graphic::load(dir)?;
        let gifs = gif_listing(dir)?;
        Ok(MsgData {
            attrs,
            configs,
            fonts,
            graphics,
            gifs,
        })
    }
    /// Lookup a config
    pub fn config(&self, s: &SignMessage) -> Result<&SignConfig> {
        let cfg = &s.sign_config;
        match self.configs.get(cfg) {
            Some(c) => Ok(c),
            None => Err(Error::UnknownResource(format!("Config: {}", cfg))),
        }
    }
    /// Check if a .gif file is in the listing.
    ///
    /// Returns true if the file exists.
    fn check_gif_listing(&mut self, n: &PathBuf) -> bool {
        self.gifs.remove(n)
    }
    /// Delete out-of-date .gif files
    fn delete_gifs(&mut self) -> Result<()> {
        for p in self.gifs.drain() {
            info!("delete gif: {:?}", &p);
            if let Err(e) = remove_file(&p) {
                error!("{:?}", e);
            }
        }
        Ok(())
    }
    /// Get an attribute (seconds) in u8 deciseconds
    fn get_as_ds(&self, name: &str) -> Option<u8> {
        if let Some(attr) = self.attrs.get(name) {
            if let Ok(sec) = attr.name.parse::<f32>() {
                let ds = sec * 10.0;
                if ds >= 0.0 && ds <= 255.0 {
                    return Some(ds as u8);
                }
            }
        }
        debug!("MsgData::get_as_ds, {} missing!", name);
        None
    }
    /// Get the default page on time
    pub fn page_on_default_ds(&self) -> u8 {
        self.get_as_ds("dms_page_on_default_secs").unwrap_or(20)
    }
    /// Get the default page off time
    pub fn page_off_default_ds(&self) -> u8 {
        self.get_as_ds("dms_page_off_default_secs").unwrap_or(0)
    }
    /// Get the default page justification
    pub fn page_justification_default(&self) -> PageJustification {
        const ATTR: &str = "dms_default_justification_page";
        if let Some(attr) = self.attrs.get(ATTR) {
            if let Some(pj) = PageJustification::new(&attr.name) {
                return pj;
            }
        }
        debug!("MsgData::page_justification_default, {} missing!", ATTR);
        PageJustification::Top
    }
    /// Get the default line justification
    pub fn line_justification_default(&self) -> LineJustification {
        const ATTR: &str = "dms_default_justification_line";
        if let Some(attr) = self.attrs.get(ATTR) {
            if let Some(lj) = LineJustification::new(&attr.name) {
                return lj;
            }
        }
        debug!("MsgData::line_justification_default, {} missing!", ATTR);
        LineJustification::Center
    }
    /// Get the default font number
    pub fn font_default(&self, fname: Option<&str>) -> Result<u8> {
        match fname {
            Some(fname) => {
                match self.fonts.values().find(|f| &f.name == fname) {
                    Some(font) => Ok(font.f_number.try_into()?),
                    None => {
                        Err(Error::UnknownResource(format!("Font: {}", fname)))
                    },
                }
            },
            None => Ok(1),
        }
    }
    /// Get font mapping
    pub fn fonts(&self) -> &HashMap<i32, Font> {
        &self.fonts
    }
    /// Get graphic mapping
    pub fn graphics(&self) -> &HashMap<i32, Graphic> {
        &self.graphics
    }
}

/// Lookup a listing of gif files
fn gif_listing(dir: &Path) -> Result<HashSet<PathBuf>> {
    let mut gifs = HashSet::new();
    let mut img = PathBuf::new();
    img.push(dir);
    img.push("img");
    if img.is_dir() {
        for f in read_dir(img)? {
            let f = f?;
            if f.file_type()?.is_file() {
                let p = f.path();
                let b = if let Some(ext) = p.extension() { ext == "gif" }
                        else { false };
                if b {
                    gifs.insert(p);
                }
            }
        }
    }
    Ok(gifs)
}

/// Check and fetch one sign message (into a .gif file).
fn fetch_sign_msg(s: &SignMessage, dir: &Path, msg_data: &mut MsgData)
    -> Result<()>
{
    let mut img = PathBuf::new();
    img.push(dir);
    img.push("img");
    let mut g = String::new();
    g.push_str(&s.name);
    g.push_str(&".gif");
    let n = make_name(&img.as_path(), &g);
    debug!("fetch: {:?}", n);
    if !msg_data.check_gif_listing(&n) {
        let tn = make_tmp_name(&img.as_path(), &g);
        let f = BufWriter::new(File::create(&tn)?);
        let t = Instant::now();
        if let Err(e) = signmsg::render(s, msg_data, f) {
            warn!("{},cfg={},multi={} {:?}", &s.name, s.sign_config, s.multi,e);
            remove_file(&tn)?;
            return Ok(());
        };
        rename(tn, &n)?;
        info!("{}.gif rendered in {:?}", &s.name, t.elapsed());
    }
    Ok(())
}

/// Query the sign messages.
///
/// * `conn` The database connection.
/// * `w` Writer for the file.
/// * `dir` Output file directory.
fn query_sign_msg<W: Write>(conn: &Connection, mut w: W, dir: &Path)
    -> Result<u32>
{
    let mut msg_data = MsgData::load(dir)?;
    let mut c = 0;
    w.write("[".as_bytes())?;
    for row in &conn.query(SignMessage::sql(), &[])? {
        if c > 0 { w.write(",".as_bytes())?; }
        w.write("\n".as_bytes())?;
        let s = SignMessage::from_row(&row);
        w.write(serde_json::to_string(&s)?.as_bytes())?;
        fetch_sign_msg(&s, dir, &mut msg_data)?;
        c += 1;
    }
    w.write("]\n".as_bytes())?;
    msg_data.delete_gifs()?;
    Ok(c)
}

impl Resource {
    /// Fetch a file.
    ///
    /// * `conn` The database connection.
    /// * `w` Writer for the file.
    /// * `dir` Output file directory.
    fn fetch_file<W: Write>(&self, conn: &Connection, w: W, dir: &Path)
        -> Result<u32>
    {
        match self {
            Resource::Simple(_, _, sql) => query_simple(conn, sql, w),
            Resource::Font(_)           => query_font(conn, w),
            Resource::SignMsg(_)        => query_sign_msg(conn, w, dir),
        }
    }
    /// Get the resource name
    pub fn name(&self) -> &str {
        match self {
            Resource::Simple(name, _, _) => name,
            Resource::Font(name)         => name,
            Resource::SignMsg(name)      => name,
        }
    }
    /// Check if a name (or alternate name) matches
    fn matches(&self, n: &str) -> bool {
        n == self.name() || self.matches_alt(n)
    }
    /// Check if an alternate resource name matches
    fn matches_alt(&self, n: &str) -> bool {
        match self {
            Resource::Simple(_, Some(a), _) => { n == *a },
            _ => false,
        }
    }
    /// Fetch the resource from a connection.
    ///
    /// * `conn` The database connection.
    /// * `dir` Output file directory.
    pub fn fetch(&self, conn: &Connection, dir: &str) -> Result<u32> {
        debug!("fetch: {:?}", self.name());
        let p = Path::new(dir);
        let tn = make_tmp_name(p, self.name());
        let n = make_name(p, self.name());
        let f = BufWriter::new(File::create(&tn)?);
        let c = self.fetch_file(conn, f, p)?;
        rename(tn, &n)?;
        Ok(c)
    }
}
