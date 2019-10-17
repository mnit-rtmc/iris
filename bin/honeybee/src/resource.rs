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
use crate::error::Result;
use crate::font::fetch_font;
use crate::signmsg::render_all;
use postgres::Connection;
use std::fs::{File, rename};
use std::io::{BufWriter, Write};
use std::path::{Path, PathBuf};
use std::time::Instant;

/// Output directory to write JSON resources
static OUTPUT_DIR: &str = "/var/www/html/iris/";

/// Make a PathBuf from a Path and file name
pub fn make_name(dir: &Path, n: &str) -> PathBuf {
    let mut p = PathBuf::new();
    p.push(dir);
    p.push(n);
    p
}

/// Make a PathBuf for a temp file
pub fn make_tmp_name(dir: &Path, n: &str) -> PathBuf {
    let mut b = String::new();
    b.push('.');
    b.push_str(n);
    make_name(dir, &b)
}

/// Listen enum for postgres NOTIFY events
#[derive(PartialEq, Eq, Hash)]
enum Listen {
    /// Listen for all payloads.
    ///
    /// * channel name
    All(&'static str),
    /// Listen for a single payload.
    ///
    /// * channel name
    /// * payload to include
    Include(&'static str, &'static str),
    /// Listen while excluding payloads.
    ///
    /// * channel name
    /// * payloads to exclude
    Exclude(&'static str, &'static [&'static str]),
}

impl Listen {
    /// Get the LISTEN channel name
    fn channel_name(&self) -> &str {
        match self {
            Listen::All(n) => n,
            Listen::Include(n, _) => n,
            Listen::Exclude(n, _) => n,
        }
    }
    /// Check if listening to a channel
    fn is_listening(&self, chan: &str, payload: &str) -> bool {
        match self {
            Listen::All(n) => n == &chan,
            Listen::Include(n, inc) => {
                n == &chan && inc == &payload
            }
            Listen::Exclude(n, exc) => {
                n == &chan && !exc.contains(&payload)
            }
        }
    }
}

/// A resource which can be fetched from a database connection.
#[derive(PartialEq, Eq, Hash)]
enum Resource {
    /// Simple file resource.
    ///
    /// * File name.
    /// * Listen specification.
    /// * SQL query.
    Simple(&'static str, Listen, &'static str),
    /// Sign message resource.
    ///
    /// * File name.
    /// * Listen specification.
    /// * SQL query.
    SignMsg(&'static str, Listen, &'static str),
    /// Font file resource
    Font(),
}

/// R_Node resource
const R_NODE_RES: Resource = Resource::Simple(
"r_node", Listen::All("r_node"),
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
"camera_pub", Listen::Exclude("camera", &["video_loss"]),
"SELECT row_to_json(r)::text FROM (\
    SELECT name, publish, location, lat, lon \
    FROM camera_view \
    ORDER BY name \
) r",
);

/// DMS attribute resource
const DMS_ATTRIBUTE_RES: Resource = Resource::Simple(
"dms_attribute", Listen::All("system_attribute"),
"SELECT row_to_json(r)::text FROM (\
    SELECT name, value \
    FROM dms_attribute_view \
) r",
);

/// DMS resource
const DMS_RES: Resource = Resource::Simple(
"dms_pub", Listen::Exclude("dms", &["expire_time", "msg_sched", "msg_current"]),
"SELECT row_to_json(r)::text FROM (\
    SELECT name, sign_config, sign_detail, roadway, road_dir, cross_street, \
           location, lat, lon \
    FROM dms_view \
    ORDER BY name \
) r",
);

/// DMS message resource
const DMS_MSG_RES: Resource = Resource::Simple(
"dms_message", Listen::Include("dms", "msg_current"),
"SELECT row_to_json(r)::text FROM (\
    SELECT name, msg_current, sources, duration, expire_time \
    FROM dms_message_view WHERE condition = 'Active' \
    ORDER BY name \
) r",
);

/// Incident resource
const INCIDENT_RES: Resource = Resource::Simple(
"incident", Listen::All("incident"),
"SELECT row_to_json(r)::text FROM (\
    SELECT name, event_date, description, road, direction, lane_type, \
           impact, confirmed, camera, detail, replaces, lat, lon \
    FROM incident_view \
    WHERE cleared = false \
) r",
);

/// Sign configuration resource
const SIGN_CONFIG_RES: Resource = Resource::Simple(
"sign_config", Listen::All("sign_config"),
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
"sign_detail", Listen::All("sign_detail"),
"SELECT row_to_json(r)::text FROM (\
    SELECT name, dms_type, portable, technology, sign_access, legend, \
           beacon_type, hardware_make, hardware_model, software_make, \
           software_model, supported_tags, max_pages, max_multi_len \
    FROM sign_detail_view \
) r",
);

/// Static parking area resource
const TPIMS_STAT_RES: Resource = Resource::Simple(
"TPIMS_static", Listen::Include("parking_area", "time_stamp_static"),
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
const TPIMS_DYN_RES: Resource = Resource::Simple(
"TPIMS_dynamic", Listen::Include("parking_area", "time_stamp"),
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
const TPIMS_ARCH_RES: Resource = Resource::Simple(
"TPIMS_archive", Listen::Include("parking_area", "time_stamp"),
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
"graphic", Listen::All("graphic"),
"SELECT row_to_json(r)::text FROM (\
    SELECT name, g_number, color_scheme, height, width, \
           transparent_color, replace(pixels, E'\n', '') AS pixels \
    FROM graphic_view \
) r",
);

/// Sign message resource
const SIGN_MSG_RES: Resource = Resource::SignMsg(
"sign_message", Listen::All("sign_message"),
"SELECT row_to_json(r)::text FROM (\
    SELECT name, sign_config, incident, multi, beacon_enabled, \
           prefix_page, msg_priority, sources, owner, duration \
    FROM sign_message_view \
    ORDER BY name \
) r",
);

/// Font resource
const FONT_RES: Resource = Resource::Font();

/// Font listen value
const FONT_LISTEN: Listen = Listen::All("font");
const GLYPH_LISTEN: Listen = Listen::All("glyph");

/// All defined resources
const ALL: &[Resource] = &[
    CAMERA_RES,
    DMS_ATTRIBUTE_RES,
    DMS_RES,
    DMS_MSG_RES,
    FONT_RES,
    GRAPHIC_RES,
    INCIDENT_RES,
    R_NODE_RES,
    SIGN_CONFIG_RES,
    SIGN_DETAIL_RES,
    SIGN_MSG_RES,
    TPIMS_STAT_RES,
    TPIMS_DYN_RES,
    TPIMS_ARCH_RES,
];

/// Fetch a simple resource.
///
/// * `conn` The database connection.
/// * `sql` SQL query.
/// * `w` Writer to output resource.
fn fetch_simple<W: Write>(conn: &Connection, sql: &str, mut w: W)
    -> Result<u32>
{
    let mut c = 0;
    w.write("[".as_bytes())?;
    for row in &conn.query(sql, &[])? {
        if c > 0 {
            w.write(",".as_bytes())?;
        }
        w.write("\n".as_bytes())?;
        let j: String = row.get(0);
        w.write(j.as_bytes())?;
        c += 1;
    }
    if c > 0 {
        w.write("\n".as_bytes())?;
    }
    w.write("]\n".as_bytes())?;
    Ok(c)
}

impl Resource {
    /// Check if a resource is listening to a channel
    fn is_listening(&self, chan: &str, payload: &str) -> bool {
        match self {
            Resource::Font() => {
                FONT_LISTEN.is_listening(chan, payload) ||
                GLYPH_LISTEN.is_listening(chan, payload)
            }
            _ => self.listen().is_listening(chan, payload),
        }
    }
    /// Get the listen value
    fn listen(&self) -> &Listen {
        match self {
            Resource::Simple(_, lsn, _) => &lsn,
            Resource::SignMsg(_, lsn, _) => &lsn,
            Resource::Font() => &FONT_LISTEN,
        }
    }
    /// Fetch to a writer.
    ///
    /// * `conn` The database connection.
    /// * `w` Writer for the file.
    fn fetch_writer<W: Write>(&self, conn: &Connection, w: W) -> Result<u32> {
        match self {
            Resource::Simple(_, _, sql) => fetch_simple(conn, sql, w),
            Resource::SignMsg(_, _, sql) => fetch_simple(conn, sql, w),
            Resource::Font() => fetch_font(conn, w),
        }
    }
    /// Fetch a file resource from a connection.
    ///
    /// * `conn` The database connection.
    /// * `name` File name.
    fn fetch_file(&self, conn: &Connection, name: &str) -> Result<()> {
        debug!("fetch: {:?}", name);
        let t = Instant::now();
        let p = Path::new(OUTPUT_DIR);
        let tn = make_tmp_name(p, name);
        let n = make_name(p, name);
        let writer = BufWriter::new(File::create(&tn)?);
        let c = self.fetch_writer(conn, writer)?;
        rename(tn, &n)?;
        info!("{}: wrote {} rows in {:?}", name, c, t.elapsed());
        Ok(())
    }
    /// Fetch sign messages resource.
    fn fetch_sign_msgs(&self, conn: &Connection, name: &str) -> Result<()> {
        self.fetch_file(conn, name)?;
        // FIXME: spawn another thread for this?
        render_all(Path::new(OUTPUT_DIR))
    }
    /// Fetch the resource from a connection.
    ///
    /// * `conn` The database connection.
    fn fetch(&self, conn: &Connection) -> Result<()> {
        // FIXME: for r_nodes, build corridors and store in earthwyrm db
        match self {
            Resource::Simple(n, _, _) => self.fetch_file(conn, n),
            Resource::SignMsg(n, _, _) => self.fetch_sign_msgs(conn, n),
            Resource::Font() => self.fetch_file(conn, "font"),
        }
    }
}

/// Listen for notifications on all channels we need to monitor.
///
/// * `conn` Database connection.
pub fn listen_all(conn: &Connection) -> Result<()> {
    for r in ALL {
        conn.execute("LISTEN $1", &[&r.listen().channel_name()])?;
    }
    // Also LISTEN to glpyh channel (for font resource)
    conn.execute("LISTEN $1", &[&GLYPH_LISTEN.channel_name()])?;
    Ok(())
}

/// Fetch all resources.
///
/// * `conn` The database connection.
pub fn fetch_all(conn: &Connection) -> Result<()> {
    for r in ALL {
        r.fetch(&conn)?;
    }
    Ok(())
}

/// Handle a channel notification.
///
/// * `conn` The database connection.
/// * `chan` Channel name.
/// * `payload` Notification payload.
pub fn notify(conn: &Connection, chan: &str, payload: &str) -> Result<()> {
    trace!("notification: ({}, {})", &chan, &payload);
    let mut found = false;
    for r in ALL {
        if r.is_listening(chan, payload) {
            found = true;
            r.fetch(&conn)?;
        }
    }
    if !found {
        warn!("unknown resource: ({}, {})", &chan, &payload);
    }
    Ok(())
}
