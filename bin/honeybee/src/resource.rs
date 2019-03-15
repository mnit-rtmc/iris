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
use gif::{Frame, Encoder, Repeat, SetParameter};
use postgres::{self, Connection};
use serde_json;
use std::collections::{HashMap, HashSet};
use std::fs::{File, rename, remove_file, read_dir};
use std::io::{BufReader, BufWriter, Write};
use std::path::{Path, PathBuf};
use std::sync::mpsc::Sender;
use std::time::Instant;
use crate::error::Error;
use crate::font::{Font, query_font, Graphic};
use crate::multi::{Color, ColorClassic, ColorScheme, LineJustification,
                   PageJustification, Rectangle};
use crate::raster::Raster;
use crate::render::{PageSplitter, State};

fn make_name(dir: &Path, n: &str) -> PathBuf {
    let mut p = PathBuf::new();
    p.push(dir);
    p.push(n);
    p
}

fn make_tmp_name(dir: &Path, n: &str) -> PathBuf {
    let mut b = String::new();
    b.push('.');
    b.push_str(n);
    make_name(dir, &b)
}

#[derive(PartialEq, Eq, Hash)]
pub enum Resource {
    Simple(&'static str, Option<&'static str>, &'static str),
    Font(&'static str),
    SignMsg(&'static str),
}

const CAMERA_RES: Resource = Resource::Simple(
"camera_pub", Some("camera"),
"SELECT row_to_json(r)::text FROM (\
    SELECT name, publish, location, lat, lon \
    FROM camera_view \
    ORDER BY name \
) r",
);

const DMS_RES: Resource = Resource::Simple(
"dms_pub", Some("dms"),
"SELECT row_to_json(r)::text FROM (\
    SELECT name, sign_config, sign_detail, roadway, road_dir, cross_street, \
           location, lat, lon \
    FROM dms_view \
    ORDER BY name \
) r",
);

const DMS_MSG_RES: Resource = Resource::Simple(
"dms_message", None,
"SELECT row_to_json(r)::text FROM (\
    SELECT name, msg_current, sources, duration, expire_time \
    FROM dms_message_view WHERE condition = 'Active' \
    ORDER BY name \
) r",
);

const INCIDENT_RES: Resource = Resource::Simple(
"incident", None,
"SELECT row_to_json(r)::text FROM (\
    SELECT name, event_date, description, road, direction, lane_type, \
           impact, confirmed, camera, detail, replaces, lat, lon \
    FROM incident_view \
    WHERE cleared = false \
) r",
);

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

const SIGN_DETAIL_RES: Resource = Resource::Simple(
"sign_detail", None,
"SELECT row_to_json(r)::text FROM (\
    SELECT name, dms_type, portable, technology, sign_access, legend, \
           beacon_type, hardware_make, hardware_model, software_make, \
           software_model, supported_tags, max_pages, max_multi_len \
    FROM sign_detail_view \
) r",
);

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

const TPIMS_DYN_RES: Resource = Resource::Simple(
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

const TPIMS_ARCH_RES: Resource = Resource::Simple(
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

const GRAPHIC_RES: Resource = Resource::Simple(
"graphic", None,
"SELECT row_to_json(r)::text FROM (\
    SELECT name, g_number, color_scheme, height, width, \
           transparent_color, pixels \
    FROM graphic_view \
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

pub trait Queryable {
    fn sql() -> &'static str;
    fn from_row(row: &postgres::rows::Row) -> Self;
}

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
    fn load(dir: &Path) -> Result<HashMap<String, SignConfig>, Error> {
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
    /// Get the horizontal border (mm).
    /// Sanity check included in case the sign vendor supplies stupid values.
    fn horizontal_border_mm(&self) -> f32 {
        let excess_mm = self.horizontal_excess_mm();
        let border_horiz = self.border_horiz as f32;
        border_horiz.min(0f32.max(excess_mm / 2f32))
    }
    /// Get the horizontal excess (mm).
    fn horizontal_excess_mm(&self) -> f32 {
        let face_width = self.face_width as f32;
        let pixels_mm = (self.pitch_horiz * (self.pixel_width - 1)) as f32;
        face_width - pixels_mm
    }
    /// Get the horizontal character offset (mm)
    fn char_offset_mm(&self, x: u32) -> f32 {
        if self.char_width > 1 {
            let gap = (x / self.char_width as u32) as f32;
            gap * self.char_gap_mm()
        } else {
            0f32
        }
    }
    /// Get the character gap (mm)
    fn char_gap_mm(&self) -> f32 {
        let excess_mm = self.horizontal_excess_mm();
        let border_mm = self.horizontal_border_mm() * 2f32;
        let gaps = self.char_gaps() as f32;
        if excess_mm > border_mm && gaps > 0f32 {
            (excess_mm - border_mm) / gaps
        } else {
            0f32
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
    fn pixel_x(&self, x: u32) -> f32 {
        let hb = self.horizontal_border_mm();
        let co = self.char_offset_mm(x);
        let pos = hb + co + (self.pitch_horiz * x as i32) as f32;
        pos / self.face_width as f32
    }
    /// Get the vertical border (mm).
    /// Sanity check included in case the sign vendor supplies stupid values.
    fn vertical_border_mm(&self) -> f32 {
        let excess_mm = self.vertical_excess_mm();
        let border_vert = self.border_vert as f32;
        border_vert.min(0f32.max(excess_mm / 2f32))
    }
    /// Get the vertical excess (mm).
    fn vertical_excess_mm(&self) -> f32 {
        let face_height = self.face_height as f32;
        let pixels_mm = (self.pitch_vert * (self.pixel_height - 1)) as f32;
        face_height - pixels_mm
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
            0f32
        }
    }
    /// Get the line gap (mm)
    fn line_gap_mm(&self) -> f32 {
        let excess_mm = self.vertical_excess_mm();
        let border_mm = self.vertical_border_mm() * 2f32;
        let gaps = self.line_gaps() as f32;
        if excess_mm > border_mm && gaps > 0f32 {
            (excess_mm - border_mm) / gaps
        } else {
            0f32
        }
    }
    /// Get the Y-position of a pixel on the sign (from 0 to 1)
    fn pixel_y(&self, y: u32) -> f32 {
        let vb = self.vertical_border_mm();
        let lo = self.line_offset_mm(y);
        let pos = vb + lo + (self.pitch_vert * y as i32) as f32;
        pos / self.face_height as f32
    }
}

#[derive(Serialize, Deserialize)]
pub struct SignDetail {
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
    configs : HashMap<String, SignConfig>,
    fonts   : HashMap<i32, Font>,
    graphics: HashMap<i32, Graphic>,
    gifs    : HashSet<PathBuf>,
    // FIXME: load DMS attributes: dms_default_justification_line,
    //        dms_default_jusitfication_page, dms_max_lines,
    //        dms_page_off_default_secs, dms_page_on_default_secs
}

impl MsgData {
    fn load(dir: &Path) -> Result<Self, Error> {
        debug!("MsgData::load");
        let configs = SignConfig::load(dir)?;
        let fonts = Font::load(dir)?;
        let graphics = Graphic::load(dir)?;
        let gifs = gif_listing(dir)?;
        Ok(MsgData {
            configs,
            fonts,
            graphics,
            gifs,
        })
    }
    /// Check if a .gif file is in the listing.
    ///
    /// Returns true if the file exists.
    fn check_gif_listing(&mut self, n: &PathBuf) -> bool {
        self.gifs.remove(n)
    }
    /// Delete out-of-date .gif files
    fn delete_gifs(&mut self, tx: &Sender<PathBuf>) -> Result<(), Error> {
        for p in self.gifs.drain() {
            info!("delete gif: {:?}", &p);
            if let Err(e) = remove_file(&p) {
                error!("{:?}", e);
            }
            tx.send(p)?;
        }
        Ok(())
    }
}

/// Lookup a listing of gif files
fn gif_listing(dir: &Path) -> Result<HashSet<PathBuf>, Error> {
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
fn fetch_sign_msg(s: &SignMessage, dir: &Path, tx: &Sender<PathBuf>,
    msg_data: &mut MsgData) -> Result<(), Error>
{
    let mut img = PathBuf::new();
    img.push(dir);
    img.push("img");
    let mut g = String::new();
    g.push_str(&s.name);
    g.push_str(&".gif");
    let n = make_name(&img.as_path(), &g);
    if !msg_data.check_gif_listing(&n) {
        let tn = make_tmp_name(&img.as_path(), &g);
        let f = BufWriter::new(File::create(&tn)?);
        let t = Instant::now();
        if let Err(e) = render_sign_msg(s, msg_data, f) {
            warn!("{},cfg={},multi={} {:?}", &s.name, s.sign_config, s.multi,e);
            remove_file(&tn)?;
            return Ok(());
        };
        rename(tn, &n)?;
        info!("{}.gif rendered in {:?}", &s.name, t.elapsed());
        tx.send(n)?;
    }
    Ok(())
}

/// Maximum pixel width of DMS images
const PIX_WIDTH: f32 = 450f32;

/// Maximum pixel height of DMS images
const PIX_HEIGHT: f32 = 100f32;

/// Calculate the size of rendered DMS
fn calculate_size(cfg: &SignConfig) -> Result<(u16, u16), Error> {
    let fw = cfg.face_width as f32;
    let fh = cfg.face_height as f32;
    if fw > 0f32 && fh > 0f32 {
        let sx = PIX_WIDTH / fw;
        let sy = PIX_HEIGHT / fh;
        let s = sx.min(sy);
        let w = (fw * s).round() as u16;
        let h = (fh * s).round() as u16;
        Ok((w, h))
    } else {
        Ok((PIX_WIDTH as u16, PIX_HEIGHT as u16))
    }
}

/// Make a .gif frame of sign face
fn make_face_frame(page: Raster, cfg: &SignConfig, w: u16, h: u16) -> Frame {
    let mut face = make_face_raster(page, &cfg, w, h);
    let pix = face.pixels();
    // FIXME: color quantization is very slow here
    Frame::from_rgb(w, h, &mut pix[..])
}

/// Make a raster of sign face
fn make_face_raster(page: Raster, cfg: &SignConfig, w: u16, h: u16) -> Raster {
    let dark = [20, 20, 0];
    let rgb = [0, 0, 0];
    let mut face = Raster::new(w.into(), h.into(), rgb);
    let ph = page.height();
    let pw = page.width();
    let sx = w as f32 / pw as f32;
    let sy = h as f32 / ph as f32;
    let s = sx.min(sy);
    debug!("face: {:?}, scale: {}", cfg.name, s);
    for y in 0..ph {
        let py = cfg.pixel_y(y) * h as f32;
        for x in 0..pw {
            let px = cfg.pixel_x(x) * w as f32;
            let clr = page.get_pixel(x, y);
            let sr = clr[0].max(clr[1]).max(clr[2]);
            // Clamp radius between 0.6 and 0.8 (blooming)
            let r = s * (sr as f32 / 255f32).max(0.6f32).min(0.8f32);
            let clr = if sr > 20 { clr } else { dark };
            face.circle(px, py, r, [clr[0], clr[1], clr[2]]);
        }
    }
    face
}

/// Render a sign message into a .gif file
fn render_sign_msg<W: Write>(s: &SignMessage, msg_data: &MsgData, mut f: W)
    -> Result<(), Error>
{
    let cfg = msg_data.configs.get(&s.sign_config);
    if cfg.is_none() {
        return Err(Error::Other(format!("Unknown config: {}", s.sign_config)));
    }
    let cfg = cfg.unwrap();
    let rs = create_render_state(s, msg_data)?;
    let (w, h) = calculate_size(cfg)?;
    let mut enc = Encoder::new(&mut f, w, h, &[])?;
    enc.set(Repeat::Infinite)?;
    for page in PageSplitter::new(rs, &s.multi) {
        let page = page?;
        let raster = page.render(&msg_data.fonts, &msg_data.graphics)?;
        let mut frame = make_face_frame(raster, &cfg, w, h);
        frame.delay = page.page_on_time_ds() * 10;
        enc.write_frame(&frame)?;
        let t = page.page_off_time_ds() * 10;
        if t > 0 {
            let raster = page.render_blank()?;
            let mut frame = make_face_frame(raster, &cfg, w, h);
            frame.delay = t;
            enc.write_frame(&frame)?;
        }
    }
    Ok(())
}

/// Create default render state for a sign message.
fn create_render_state(s: &SignMessage, msg_data: &MsgData)
    -> Result<State, Error>
{
    let cfg = msg_data.configs.get(&s.sign_config);
    if cfg.is_none() {
        return Err(Error::Other(format!("Unknown config: {}", s.sign_config)));
    }
    let cfg = cfg.unwrap();
    let color_scheme = ColorScheme::from_str(&cfg.color_scheme)?;
    let char_width = cfg.char_width as u8;
    let char_height = cfg.char_height as u8;
    let color_foreground: Color = match color_scheme {
        ColorScheme::Monochrome1Bit|
        ColorScheme::Monochrome8Bit => cfg.monochrome_foreground.into(),
        ColorScheme::ColorClassic|
        ColorScheme::Color24Bit     => ColorClassic::Amber.into(),
    };
    let page_background: Color = match color_scheme {
        ColorScheme::Monochrome1Bit|
        ColorScheme::Monochrome8Bit => cfg.monochrome_background.into(),
        ColorScheme::ColorClassic|
        ColorScheme::Color24Bit     => ColorClassic::Black.into(),
    };
    let page_on_time_ds = 20;   // FIXME
    let page_off_time_ds = 0;   // FIXME
    if cfg.pixel_width < 1 {
        return Err(Error::Other(format!("Invalid width: {}", cfg.pixel_width)));
    }
    if cfg.pixel_height < 1 {
        return Err(Error::Other(format!("Invalid height: {}", cfg.pixel_height)));
    }
    let text_rectangle = Rectangle::new(1, 1, cfg.pixel_width as u16,
        cfg.pixel_height as u16);
    let just_page = PageJustification::Top;    // FIXME
    let just_line = LineJustification::Center; // FIXME
    let fname = cfg.default_font.as_ref();
    if fname.is_none() {
        return Err(Error::Other(format!("No default font for {}", cfg.name)));
    }
    let fname = fname.unwrap();
    let font = msg_data.fonts.values().find(|f| &f.name == fname);
    if font.is_none() {
        return Err(Error::Other(format!("Unknown font: {}", fname)));
    }
    let font = (font.unwrap().f_number as u8, None);
    Ok(State::new(color_scheme,
                  char_width,
                  char_height,
                  color_foreground,
                  page_background,
                  page_on_time_ds,
                  page_off_time_ds,
                  text_rectangle,
                  just_page,
                  just_line,
                  font,
    ))
}

/// Query the sign messages.
///
/// * `conn` The database connection.
/// * `w` Writer for the file.
/// * `dir` Output file directory.
/// * `tx` Channel sender for resource file names.
fn query_sign_msg<W: Write>(conn: &Connection, mut w: W, dir: &Path,
    tx: &Sender<PathBuf>) -> Result<u32, Error>
{
    let mut msg_data = MsgData::load(dir)?;
    let mut c = 0;
    w.write("[".as_bytes())?;
    for row in &conn.query(SignMessage::sql(), &[])? {
        if c > 0 { w.write(",".as_bytes())?; }
        w.write("\n".as_bytes())?;
        let s = SignMessage::from_row(&row);
        w.write(serde_json::to_string(&s)?.as_bytes())?;
        fetch_sign_msg(&s, dir, tx, &mut msg_data)?;
        c += 1;
    }
    w.write("]\n".as_bytes())?;
    msg_data.delete_gifs(tx)?;
    Ok(c)
}

impl Resource {
    /// Fetch a file.
    ///
    /// * `conn` The database connection.
    /// * `w` Writer for the file.
    /// * `dir` Output file directory.
    /// * `tx` Channel sender for resource file names.
    fn fetch_file<W: Write>(&self, conn: &Connection, w: W, dir: &Path,
        tx: &Sender<PathBuf>) -> Result<u32, Error>
    {
        match self {
            Resource::Simple(_, _, sql) => query_simple(conn, sql, w),
            Resource::Font(_)           => query_font(conn, w),
            Resource::SignMsg(_)        => query_sign_msg(conn, w, dir, tx),
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
    /// Fetch the resource and send PathBuf(s) to a channel.
    ///
    /// * `conn` The database connection.
    /// * `dir` Output file directory.
    /// * `tx` Channel sender for resource file names.
    pub fn fetch(&self, conn: &Connection, dir: &str, tx: &Sender<PathBuf>)
        -> Result<u32, Error>
    {
        debug!("fetch: {:?}", self.name());
        let p = Path::new(dir);
        let tn = make_tmp_name(p, self.name());
        let n = make_name(p, self.name());
        let f = BufWriter::new(File::create(&tn)?);
        let c = self.fetch_file(conn, f, p, tx)?;
        rename(tn, &n)?;
        tx.send(n)?;
        Ok(c)
    }
}

/// All defined resources
pub const ALL: &[Resource] = &[
    CAMERA_RES,
    DMS_RES,
    DMS_MSG_RES,
    INCIDENT_RES,
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
pub fn lookup(n: &str) -> Option<&Resource> {
    ALL.iter().find(|r| r.matches(n))
}
