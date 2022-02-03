// resource.rs
//
// Copyright (C) 2018-2022  Minnesota Department of Transportation
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
use crate::segments::{RNode, Road, SegMsg};
use crate::signmsg::render_all;
use crate::Result;
use postgres::Client;
use std::fs::{rename, File};
use std::io::{BufWriter, Write};
use std::path::{Path, PathBuf};
use std::sync::mpsc::Sender;
use std::time::Instant;

/// Make a PathBuf from a Path and file name
pub fn make_name(dir: &Path, n: &str) -> PathBuf {
    let mut p = PathBuf::new();
    p.push(dir);
    p.push(n);
    p
}

/// Make a PathBuf for a backup file
pub fn make_backup_name(dir: &Path, n: &str) -> PathBuf {
    make_name(dir, &format!("{}~", n))
}

/// Listen enum for postgres NOTIFY events
#[derive(PartialEq, Eq, Hash)]
enum Listen {
    /// Listen for all payloads.
    ///
    /// * channel name
    All(&'static str),

    /// Listen for specific payloads.
    ///
    /// * channel name
    /// * payloads to include
    Include(&'static str, &'static [&'static str]),

    /// Listen while excluding payloads.
    ///
    /// * channel name
    /// * payloads to exclude
    Exclude(&'static str, &'static [&'static str]),

    /// Listen for all payloads on two channels.
    ///
    /// * first channel name
    /// * second channel name
    Two(&'static str, &'static str),
}

impl Listen {
    /// Get the LISTEN channel name
    fn channel_names(&self) -> Vec<&str> {
        match self {
            Listen::All(n) => vec![n],
            Listen::Include(n, _) => vec![n],
            Listen::Exclude(n, _) => vec![n],
            Listen::Two(n0, n1) => vec![n0, n1],
        }
    }

    /// Check if listening to a channel
    fn is_listening(&self, chan: &str, payload: &str) -> bool {
        match self {
            Listen::All(n) => n == &chan,
            Listen::Include(n, inc) => n == &chan && inc.contains(&payload),
            Listen::Exclude(n, exc) => n == &chan && !exc.contains(&payload),
            Listen::Two(n0, n1) => n0 == &chan || n1 == &chan,
        }
    }

    /// Check if listening to a channel / payload
    fn is_listening_payload(&self, chan: &str, payload: &str) -> bool {
        if let Listen::Exclude(n, exc) = self {
            n == &chan && exc.contains(&payload)
        } else {
            self.is_listening(chan, payload)
        }
    }
}

/// A resource which can be fetched from a database connection.
#[derive(PartialEq, Eq, Hash)]
enum Resource {
    /// RNode resource.
    ///
    /// * Listen specification.
    RNode(Listen),

    /// Road resource.
    ///
    /// * Listen specification.
    Road(Listen),

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
}

/// Camera resource
const CAMERA_RES: Resource = Resource::Simple(
    "camera_pub",
    Listen::Exclude("camera", &["video_loss"]),
    "SELECT row_to_json(r)::text FROM (\
    SELECT name, publish, streamable, roadway, road_dir, cross_street,\
           location, lat, lon \
    FROM camera_view \
    ORDER BY name \
) r",
);

/// Detector resource
const DETECTOR_RES: Resource = Resource::Simple(
    "detector",
    Listen::Exclude("detector", &["auto_fail"]),
    "SELECT row_to_json(r)::text FROM (\
    SELECT name, r_node, cor_id, lane_number, lane_code, field_length \
    FROM detector_view \
) r",
);

/// DMS resource
const DMS_RES: Resource = Resource::Simple(
    "dms_pub",
    Listen::Exclude("dms", &["expire_time", "msg_sched", "msg_current"]),
    "SELECT row_to_json(r)::text FROM (\
    SELECT name, sign_config, sign_detail, roadway, road_dir, cross_street, \
           location, lat, lon \
    FROM dms_view \
    ORDER BY name \
) r",
);

/// DMS status resource
const DMS_STAT_RES: Resource = Resource::Simple(
    "dms_message",
    Listen::Include("dms", &["msg_current"]),
    "SELECT row_to_json(r)::text FROM (\
    SELECT name, msg_current, failed, sources, duration, expire_time \
    FROM dms_message_view WHERE condition = 'Active' \
    ORDER BY name \
) r",
);

/// Font resource
const FONT_RES: Resource = Resource::Simple(
    "font",
    Listen::Two("font", "glyph"),
    "SELECT row_to_json(f)::text FROM (\
    SELECT f_number AS number, name, height, char_spacing, line_spacing, \
           array(SELECT row_to_json(c) FROM \
               (SELECT code_point AS number, width, replace(pixels, E'\n', '') \
                AS bitmap \
                FROM iris.glyph \
                WHERE font = ft.name \
                ORDER BY code_point \
               ) AS c) \
           AS characters, version_id \
    FROM iris.font ft ORDER BY name) AS f",
);

/// Graphic resource
const GRAPHIC_RES: Resource = Resource::Simple(
    "graphic",
    Listen::All("graphic"),
    "SELECT row_to_json(r)::text FROM (\
    SELECT g_number AS number, name, height, width, color_scheme, \
           transparent_color, replace(pixels, E'\n', '') AS bitmap \
    FROM graphic_view \
    WHERE g_number < 256 \
) r",
);

/// Incident resource
const INCIDENT_RES: Resource = Resource::Simple(
    "incident",
    Listen::All("incident"),
    "SELECT row_to_json(r)::text FROM (\
    SELECT name, event_date, description, road, direction, lane_type, \
           impact, confirmed, camera, detail, replaces, lat, lon \
    FROM incident_view \
    WHERE cleared = false\
) r",
);

/// RNode resource
const R_NODE_RES: Resource = Resource::RNode(Listen::All("r_node"));

/// Road resource
const ROAD_RES: Resource = Resource::Road(Listen::All("road"));

/// Alarm resource
const ALARM_RES: Resource = Resource::Simple(
    "api/alarm",
    Listen::All("alarm"),
    "SELECT row_to_json(r)::text FROM (\
    SELECT name, description, controller, pin, state, trigger_time \
    FROM iris.alarm \
    ORDER BY description\
) r",
);

/// Cabinet style resource
const CABINET_STYLE_RES: Resource = Resource::Simple(
    "api/cabinet_style",
    Listen::All("cabinet_style"),
    "SELECT row_to_json(r)::text FROM (\
    SELECT name, police_panel_pin_1, police_panel_pin_2, watchdog_reset_pin_1, \
           watchdog_reset_pin_2, dip \
    FROM iris.cabinet_style \
    ORDER BY name\
) r",
);

/// Comm protocol LUT resource
const COMM_PROTOCOL_RES: Resource = Resource::Simple(
    "api/comm_protocol",
    Listen::All("comm_protocol"), // no notifications for LUT
    "SELECT row_to_json(r)::text FROM (\
    SELECT id, description \
    FROM iris.comm_protocol \
    ORDER BY description\
) r",
);

/// Comm configuration resource
const COMM_CONFIG_RES: Resource = Resource::Simple(
    "api/comm_config",
    Listen::All("comm_config"),
    "SELECT row_to_json(r)::text FROM (\
    SELECT name, description, protocol, modem, timeout_ms, \
           poll_period_sec, long_poll_period_sec, idle_disconnect_sec, \
           no_response_disconnect_sec \
    FROM iris.comm_config \
    ORDER BY description\
) r",
);

/// Comm link resource
const COMM_LINK_RES: Resource = Resource::Simple(
    "api/comm_link",
    Listen::Exclude("comm_link", &["connected"]),
    "SELECT row_to_json(r)::text FROM (\
    SELECT name, description, uri, poll_enabled, comm_config \
    FROM iris.comm_link \
    ORDER BY regexp_replace(name, '[0-9]', '', 'g'), \
            (regexp_replace(name, '[^0-9]', '', 'g') || '0')::INTEGER\
) r",
);

/// Comm link status resource
const COMM_LINK_STAT_RES: Resource = Resource::Simple(
    "api/comm_link_stat",
    Listen::Include("comm_link", &["connected"]),
    "SELECT row_to_json(r)::text FROM (\
    SELECT name, connected \
    FROM iris.comm_link\
) r",
);

/// Controller condition LUT resource
const CONDITION_RES: Resource = Resource::Simple(
    "api/condition",
    Listen::All("condition"), // no notifications for LUT
    "SELECT row_to_json(r)::text FROM (\
    SELECT id, description \
    FROM iris.condition \
    ORDER BY description\
) r",
);

/// Controller resource
const CONTROLLER_RES: Resource = Resource::Simple(
    "api/controller",
    Listen::Exclude("controller", &["fail_time"]),
    "SELECT row_to_json(r)::text FROM (\
    SELECT name, drop_id, comm_link, cabinet_style, geo_loc, condition, \
           notes, password, version \
    FROM iris.controller \
    ORDER BY regexp_replace(comm_link, '[0-9]', '', 'g'), \
            (regexp_replace(comm_link, '[^0-9]', '', 'g') || '0')::INTEGER, \
             drop_id\
) r",
);

/// Controller status resource
const CONTROLLER_STAT_RES: Resource = Resource::Simple(
    "api/controller_stat",
    Listen::Include("controller", &["fail_time"]),
    "SELECT row_to_json(r)::text FROM (\
    SELECT name, fail_time \
    FROM iris.controller \
) r",
);

/// Modem resource
const MODEM_RES: Resource = Resource::Simple(
    "api/modem",
    Listen::All("modem"),
    "SELECT row_to_json(r)::text FROM (\
    SELECT name, uri, config, timeout_ms, enabled \
    FROM iris.modem \
    ORDER BY name\
) r",
);

/// Sign configuration resource
const SIGN_CONFIG_RES: Resource = Resource::Simple(
    "sign_config",
    Listen::All("sign_config"),
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
    "sign_detail",
    Listen::All("sign_detail"),
    "SELECT row_to_json(r)::text FROM (\
    SELECT name, dms_type, portable, technology, sign_access, legend, \
           beacon_type, hardware_make, hardware_model, software_make, \
           software_model, supported_tags, max_pages, max_multi_len, \
           beacon_activation_flag, pixel_service_flag \
    FROM sign_detail_view \
) r",
);

/// Sign message resource
const SIGN_MSG_RES: Resource = Resource::SignMsg(
    "sign_message",
    Listen::All("sign_message"),
    "SELECT row_to_json(r)::text FROM (\
    SELECT name, sign_config, incident, multi, beacon_enabled, \
           msg_combining, msg_priority, sources, owner, duration \
    FROM sign_message_view \
    ORDER BY name \
) r",
);

/// System attribute resource
const SYSTEM_ATTRIBUTE_RES: Resource = Resource::Simple(
    "system_attribute",
    Listen::All("system_attribute"),
    "SELECT jsonb_object_agg(name, value)::text \
    FROM iris.system_attribute \
    WHERE name LIKE 'dms\\_%' OR name LIKE 'map\\_%'",
);

/// Static parking area resource
const TPIMS_STAT_RES: Resource = Resource::Simple(
"TPIMS_static", Listen::Include("parking_area", &["time_stamp_static"]),
"SELECT row_to_json(r)::text FROM (\
    SELECT site_id AS \"siteId\", to_char(time_stamp_static AT TIME ZONE 'UTC', \
           'YYYY-mm-dd\"T\"HH24:MI:SSZ') AS \"timeStamp\", \
           relevant_highway AS \"relevantHighway\", \
           reference_post AS \"referencePost\", exit_id AS \"exitID\", \
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
    "TPIMS_dynamic",
    Listen::Include("parking_area", &["time_stamp"]),
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
    "TPIMS_archive",
    Listen::Include("parking_area", &["time_stamp"]),
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

/// All defined resources
const ALL: &[Resource] = &[
    SYSTEM_ATTRIBUTE_RES, // System attributes must be loaded first
    ROAD_RES,             // Roads must be loaded before R_Nodes
    ALARM_RES,
    CABINET_STYLE_RES,
    COMM_PROTOCOL_RES,
    COMM_CONFIG_RES,
    COMM_LINK_RES,
    COMM_LINK_STAT_RES,
    CONDITION_RES,
    CONTROLLER_RES,
    CONTROLLER_STAT_RES,
    MODEM_RES,
    CAMERA_RES,
    DMS_RES,
    DMS_STAT_RES,
    FONT_RES,
    GRAPHIC_RES,
    INCIDENT_RES,
    R_NODE_RES,
    DETECTOR_RES,
    SIGN_CONFIG_RES,
    SIGN_DETAIL_RES,
    SIGN_MSG_RES,
    TPIMS_STAT_RES,
    TPIMS_DYN_RES,
    TPIMS_ARCH_RES,
];

/// Fetch a simple resource.
///
/// * `client` The database connection.
/// * `sql` SQL query.
/// * `w` Writer to output resource.
fn fetch_simple<W: Write>(
    client: &mut Client,
    sql: &str,
    mut w: W,
) -> Result<u32> {
    let mut c = 0;
    w.write_all(b"[")?;
    for row in &client.query(sql, &[])? {
        if c > 0 {
            w.write_all(b",")?;
        }
        w.write_all(b"\n")?;
        let j: String = row.get(0);
        w.write_all(j.as_bytes())?;
        c += 1;
    }
    if c > 0 {
        w.write_all(b"\n")?;
    }
    w.write_all(b"]\n")?;
    Ok(c)
}

/// Fetch all r_nodes.
///
/// * `client` The database connection.
/// * `sender` Sender for segment messages.
fn fetch_all_nodes(client: &mut Client, sender: &Sender<SegMsg>) -> Result<()> {
    debug!("fetch_all_nodes");
    sender.send(SegMsg::Order(false))?;
    for row in &client.query(RNode::SQL_ALL, &[])? {
        sender.send(SegMsg::UpdateNode(RNode::from_row(row)))?;
    }
    sender.send(SegMsg::Order(true))?;
    Ok(())
}

/// Fetch one r_node.
///
/// * `client` The database connection.
/// * `name` RNode name.
/// * `sender` Sender for segment messages.
fn fetch_one_node(
    client: &mut Client,
    name: &str,
    sender: &Sender<SegMsg>,
) -> Result<()> {
    debug!("fetch_one_node: {}", name);
    let rows = &client.query(RNode::SQL_ONE, &[&name])?;
    if rows.len() == 1 {
        for row in rows.iter() {
            sender.send(SegMsg::UpdateNode(RNode::from_row(row)))?;
        }
    } else {
        assert!(rows.is_empty());
        sender.send(SegMsg::RemoveNode(name.to_string()))?;
    }
    Ok(())
}

/// Fetch all roads.
///
/// * `client` The database connection.
/// * `sender` Sender for segment messages.
fn fetch_all_roads(client: &mut Client, sender: &Sender<SegMsg>) -> Result<()> {
    debug!("fetch_all_roads");
    for row in &client.query(Road::SQL_ALL, &[])? {
        sender.send(SegMsg::UpdateRoad(Road::from_row(row)))?;
    }
    Ok(())
}

/// Fetch one road.
///
/// * `client` The database connection.
/// * `name` Road name.
/// * `sender` Sender for segment messages.
fn fetch_one_road(
    client: &mut Client,
    name: &str,
    sender: &Sender<SegMsg>,
) -> Result<()> {
    debug!("fetch_one_road: {}", name);
    let rows = &client.query(Road::SQL_ONE, &[&name])?;
    if let Some(row) = rows.iter().next() {
        sender.send(SegMsg::UpdateRoad(Road::from_row(row)))?;
    }
    Ok(())
}

impl Resource {
    /// Get the listen value
    fn listen(&self) -> &Listen {
        match self {
            Resource::RNode(lsn) => lsn,
            Resource::Road(lsn) => lsn,
            Resource::Simple(_, lsn, _) => lsn,
            Resource::SignMsg(_, lsn, _) => lsn,
        }
    }

    /// Fetch the resource from a connection.
    ///
    /// * `client` The database connection.
    /// * `payload` Postgres NOTIFY payload.
    /// * `sender` Sender for segment messages.
    fn fetch(
        &self,
        client: &mut Client,
        payload: &str,
        sender: &Sender<SegMsg>,
    ) -> Result<()> {
        match self {
            Resource::RNode(_) => self.fetch_nodes(client, payload, sender),
            Resource::Road(_) => self.fetch_roads(client, payload, sender),
            Resource::Simple(n, _, _) => self.fetch_file(client, n),
            Resource::SignMsg(n, _, _) => self.fetch_sign_msgs(client, n),
        }
    }

    /// Fetch r_node resource from a connection.
    ///
    /// * `client` The database connection.
    /// * `payload` Postgres NOTIFY payload.
    /// * `sender` Sender for segment messages.
    fn fetch_nodes(
        &self,
        client: &mut Client,
        payload: &str,
        sender: &Sender<SegMsg>,
    ) -> Result<()> {
        if payload.is_empty() {
            fetch_all_nodes(client, sender)
        } else {
            fetch_one_node(client, payload, sender)
        }
    }

    /// Fetch road resource from a connection.
    ///
    /// * `client` The database connection.
    /// * `payload` Postgres NOTIFY payload.
    /// * `sender` Sender for segment messages.
    fn fetch_roads(
        &self,
        client: &mut Client,
        payload: &str,
        sender: &Sender<SegMsg>,
    ) -> Result<()> {
        if payload.is_empty() {
            fetch_all_roads(client, sender)
        } else {
            fetch_one_road(client, payload, sender)
        }
    }

    /// Fetch a file resource from a connection.
    ///
    /// * `client` The database connection.
    /// * `name` File name.
    fn fetch_file(&self, client: &mut Client, name: &str) -> Result<()> {
        debug!("fetch_file: {:?}", name);
        let t = Instant::now();
        let dir = Path::new("");
        let backup = make_backup_name(dir, name);
        let n = make_name(dir, name);
        let writer = BufWriter::new(File::create(&backup)?);
        let count = self.fetch_writer(client, writer)?;
        rename(backup, &n)?;
        info!("{}: wrote {} rows in {:?}", name, count, t.elapsed());
        Ok(())
    }

    /// Fetch to a writer.
    ///
    /// * `client` The database connection.
    /// * `w` Writer for the file.
    fn fetch_writer<W: Write>(&self, client: &mut Client, w: W) -> Result<u32> {
        match self {
            Resource::RNode(_) => unreachable!(),
            Resource::Road(_) => unreachable!(),
            Resource::Simple(_, _, sql) => fetch_simple(client, sql, w),
            Resource::SignMsg(_, _, sql) => fetch_simple(client, sql, w),
        }
    }

    /// Fetch sign messages resource.
    fn fetch_sign_msgs(&self, client: &mut Client, name: &str) -> Result<()> {
        self.fetch_file(client, name)?;
        // FIXME: spawn another thread for this?
        render_all(Path::new(""))
    }
}

/// Listen for notifications on all channels we need to monitor.
///
/// * `client` Database connection.
pub fn listen_all(client: &mut Client) -> Result<()> {
    for r in ALL {
        for lsn in r.listen().channel_names() {
            let listen = format!("LISTEN {}", lsn);
            client.execute(&listen[..], &[])?;
        }
    }
    Ok(())
}

/// Fetch all resources.
///
/// * `client` The database connection.
/// * `sender` Sender for segment messages.
pub fn fetch_all(client: &mut Client, sender: &Sender<SegMsg>) -> Result<()> {
    for r in ALL {
        r.fetch(client, "", sender)?;
    }
    Ok(())
}

/// Handle a channel notification.
///
/// * `client` The database connection.
/// * `chan` Channel name.
/// * `payload` Notification payload.
/// * `sender` Sender for segment messages.
pub fn notify(
    client: &mut Client,
    chan: &str,
    payload: &str,
    sender: &Sender<SegMsg>,
) -> Result<()> {
    info!("notify: {}, {}", &chan, &payload);
    let mut found = false;
    for r in ALL {
        if r.listen().is_listening(chan, payload) {
            found = true;
            r.fetch(client, payload, sender)?;
        } else if r.listen().is_listening_payload(chan, payload) {
            found = true;
        }
    }
    if !found {
        warn!("unknown resource: ({}, {})", &chan, &payload);
    }
    Ok(())
}

/// Check if any resource is listening to a channel / payload
pub fn is_listening_payload(chan: &str, payload: &str) -> bool {
    ALL.iter()
        .any(|r| r.listen().is_listening_payload(chan, payload))
}
