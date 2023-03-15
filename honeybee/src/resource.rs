// resource.rs
//
// Copyright (C) 2018-2023  Minnesota Department of Transportation
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
use std::collections::HashSet;
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
    make_name(dir, &format!("{n}~"))
}

/// Listen enum for postgres NOTIFY events
#[derive(PartialEq, Eq, Hash)]
enum Listen {
    /// Do not listen
    Nope,

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
            Listen::Nope => vec![],
            Listen::All(n) => vec![n],
            Listen::Include(n, _) => vec![n],
            Listen::Exclude(n, _) => vec![n],
            Listen::Two(n0, n1) => vec![n0, n1],
        }
    }

    /// Check if listening to a channel
    fn is_listening(&self, chan: &str, payload: &str) -> bool {
        match self {
            Listen::Nope => false,
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
    "api/camera",
    Listen::Exclude("camera", &["video_loss"]),
    "SELECT row_to_json(r)::text FROM (\
      SELECT c.name, location, controller, notes, cam_num, publish \
      FROM iris.camera c \
      LEFT JOIN geo_loc_view gl ON c.geo_loc = gl.name \
      ORDER BY cam_num, c.name\
    ) r",
);

/// Public Camera resource
const CAMERA_PUB_RES: Resource = Resource::Simple(
    "camera_pub",
    Listen::Exclude("camera", &["video_loss"]),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, publish, streamable, roadway, road_dir, cross_street, \
             location, lat, lon, ARRAY(\
               SELECT view_num \
               FROM iris.encoder_stream \
               WHERE encoder_type = c.encoder_type \
               AND view_num IS NOT NULL \
               ORDER BY view_num\
             ) AS views \
      FROM camera_view c \
      ORDER BY name\
    ) r",
);

/// Gate arm resource
const GATE_ARM_RES: Resource = Resource::Simple(
    "api/gate_arm",
    Listen::All("gate_arm"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT g.name, location, g.controller, g.notes, g.arm_state \
      FROM iris.gate_arm g \
      LEFT JOIN iris.gate_arm_array ga ON g.ga_array = ga.name \
      LEFT JOIN geo_loc_view gl ON ga.geo_loc = gl.name \
      ORDER BY name\
    ) r",
);

/// Gate arm array resource
const GATE_ARM_ARRAY_RES: Resource = Resource::Simple(
    "api/gate_arm_array",
    Listen::All("gate_arm_array"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT ga.name, location, notes, arm_state, interlock \
      FROM iris.gate_arm_array ga \
      LEFT JOIN geo_loc_view gl ON ga.geo_loc = gl.name \
      ORDER BY ga.name\
    ) r",
);

/// GPS resource
const GPS_RES: Resource = Resource::Simple(
    "api/gps",
    Listen::Exclude("gps", &["latest_poll", "latest_sample", "lat", "lon"]),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, controller, notes \
      FROM iris.gps \
      ORDER BY name\
    ) r",
);

/// LCS array resource
const LCS_ARRAY_RES: Resource = Resource::Simple(
    "api/lcs_array",
    Listen::All("lcs_array"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, notes, lcs_lock \
      FROM iris.lcs_array \
      ORDER BY name\
    ) r",
);

/// LCS indication resource
const LCS_INDICATION_RES: Resource = Resource::Simple(
    "api/lcs_indication",
    Listen::All("lcs_indication"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, controller, lcs, indication \
      FROM iris.lcs_indication \
      ORDER BY name\
    ) r",
);

/// Ramp meter resource
const RAMP_METER_RES: Resource = Resource::Simple(
    "api/ramp_meter",
    Listen::All("ramp_meter"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT m.name, location, controller, notes \
      FROM iris.ramp_meter m \
      LEFT JOIN geo_loc_view gl ON m.geo_loc = gl.name \
      ORDER BY m.name\
    ) r",
);

/// Tag reader resource
const TAG_READER_RES: Resource = Resource::Simple(
    "api/tag_reader",
    Listen::All("tag_reader"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT t.name, location, controller, notes \
      FROM iris.tag_reader t \
      LEFT JOIN geo_loc_view gl ON t.geo_loc = gl.name \
      ORDER BY t.name\
    ) r",
);

/// Video monitor resource
const VIDEO_MONITOR_RES: Resource = Resource::Simple(
    "api/video_monitor",
    Listen::Exclude("video_monitor", &["camera"]),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, mon_num, controller, notes \
      FROM iris.video_monitor \
      ORDER BY mon_num, name\
    ) r",
);

/// Flow stream resource
const FLOW_STREAM_RES: Resource = Resource::Simple(
    "api/flow_stream",
    Listen::Exclude("flow_stream", &["status"]),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, controller \
      FROM iris.flow_stream \
      ORDER BY name\
    ) r",
);

/// Detector resource
const DETECTOR_RES: Resource = Resource::Simple(
    "api/detector",
    Listen::Exclude("detector", &["auto_fail"]),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, label, controller, notes \
      FROM detector_view \
      ORDER BY regexp_replace(name, '[0-9]', '', 'g'), \
              (regexp_replace(name, '[^0-9]', '', 'g') || '0')::INTEGER\
    ) r",
);

/// Public Detector resource
const DETECTOR_PUB_RES: Resource = Resource::Simple(
    "detector_pub",
    Listen::Exclude("detector", &["auto_fail"]),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, r_node, cor_id, lane_number, lane_code, field_length \
      FROM detector_view\
    ) r",
);

/// DMS resource
const DMS_RES: Resource = Resource::Simple(
    "api/dms",
    Listen::Exclude("dms", &["msg_user", "msg_sched", "expire_time"]),
    "SELECT row_to_json(r)::text FROM (\
      SELECT d.name, controller, location, notes, msg_current \
      FROM iris.dms d \
      LEFT JOIN geo_loc_view gl ON d.geo_loc = gl.name \
      ORDER BY d.name\
    ) r",
);

/// Public DMS resource
const DMS_PUB_RES: Resource = Resource::Simple(
    "dms_pub",
    Listen::Exclude(
        "dms",
        &["msg_user", "msg_sched", "msg_current", "expire_time", "status"],
    ),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, sign_config, sign_detail, roadway, road_dir, \
             cross_street, location, lat, lon \
      FROM dms_view \
      ORDER BY name\
    ) r",
);

/// DMS status resource
///
/// NOTE: the `sources` attribute is deprecated,
///       but required by external systems (for now)
const DMS_STAT_RES: Resource = Resource::Simple(
    "dms_message",
    Listen::Include("dms", &["msg_current"]),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, msg_current, \
             replace(substring(msg_owner FROM 'IRIS; ([^;]*).*'), '+', ', ') \
             AS sources, failed, duration, expire_time \
      FROM dms_message_view WHERE condition = 'Active' \
      ORDER BY name\
    ) r",
);

/// Font resource
const FONT_RES: Resource = Resource::Simple(
    "font",
    Listen::Two("font", "glyph"),
    "SELECT row_to_json(f)::text FROM (\
      SELECT f_number AS number, name, height, char_spacing, line_spacing, \
             array(SELECT row_to_json(c) FROM (\
               SELECT code_point AS number, width, \
                      replace(pixels, E'\n', '') AS bitmap \
               FROM iris.glyph \
               WHERE font = ft.name \
               ORDER BY code_point \
             ) AS c) \
           AS characters, version_id \
      FROM iris.font ft ORDER BY name\
    ) AS f",
);

/// Graphic resource
const GRAPHIC_RES: Resource = Resource::Simple(
    "graphic",
    Listen::All("graphic"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT g_number AS number, name, height, width, color_scheme, \
             transparent_color, replace(pixels, E'\n', '') AS bitmap \
      FROM graphic_view \
      WHERE g_number < 256\
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
    Listen::Exclude("alarm", &["pin", "trigger_time"]),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, description, controller, state \
      FROM iris.alarm \
      ORDER BY description\
    ) r",
);

/// Beacon state LUT resource
const BEACON_STATE_RES: Resource = Resource::Simple(
    "beacon_state",
    Listen::Nope,
    "SELECT row_to_json(r)::text FROM (\
      SELECT id, description \
      FROM iris.beacon_state \
      ORDER BY id\
    ) r",
);

/// Beacon resource
const BEACON_RES: Resource = Resource::Simple(
    "api/beacon",
    Listen::All("beacon"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT b.name, location, controller, message, notes, state \
      FROM iris.beacon b \
      LEFT JOIN geo_loc_view gl ON b.geo_loc = gl.name \
      ORDER BY name\
    ) r",
);

/// Lane marking resource
const LANE_MARKING_RES: Resource = Resource::Simple(
    "api/lane_marking",
    Listen::All("lane_marking"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT m.name, location, controller, notes, deployed \
      FROM iris.lane_marking m \
      LEFT JOIN geo_loc_view gl ON m.geo_loc = gl.name \
      ORDER BY name\
    ) r",
);

/// Cabinet style resource
const CABINET_STYLE_RES: Resource = Resource::Simple(
    "api/cabinet_style",
    Listen::All("cabinet_style"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name \
      FROM iris.cabinet_style \
      ORDER BY name\
    ) r",
);

/// Comm protocol LUT resource
const COMM_PROTOCOL_RES: Resource = Resource::Simple(
    "comm_protocol",
    Listen::Nope,
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
      SELECT name, description \
      FROM iris.comm_config \
      ORDER BY description\
    ) r",
);

/// Comm link resource
const COMM_LINK_RES: Resource = Resource::Simple(
    "api/comm_link",
    Listen::All("comm_link"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, description, uri, comm_config, poll_enabled, connected \
      FROM iris.comm_link \
      ORDER BY regexp_replace(name, '[0-9]', '', 'g'), \
              (regexp_replace(name, '[^0-9]', '', 'g') || '0')::INTEGER\
    ) r",
);

/// Controller condition LUT resource
const CONDITION_RES: Resource = Resource::Simple(
    "condition",
    Listen::Nope,
    "SELECT row_to_json(r)::text FROM (\
      SELECT id, description \
      FROM iris.condition \
      ORDER BY description\
    ) r",
);

/// Direction LUT resource
const DIRECTION_RES: Resource = Resource::Simple(
    "direction",
    Listen::Nope,
    "SELECT row_to_json(r)::text FROM (\
      SELECT id, direction, dir \
      FROM iris.direction \
      ORDER BY id\
    ) r",
);

/// Roadway resource
const ROADWAY_RES: Resource = Resource::Simple(
    "api/road",
    Listen::All("road"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, abbrev, r_class, direction \
      FROM iris.road \
      ORDER BY name\
    ) r",
);

/// Road modifier LUT resource
const ROAD_MODIFIER_RES: Resource = Resource::Simple(
    "road_modifier",
    Listen::Nope,
    "SELECT row_to_json(r)::text FROM (\
      SELECT id, modifier, mod AS md \
      FROM iris.road_modifier \
      ORDER BY id\
    ) r",
);

/// Gate arm interlock LUT resource
const GATE_ARM_INTERLOCK_RES: Resource = Resource::Simple(
    "gate_arm_interlock",
    Listen::Nope,
    "SELECT row_to_json(r)::text FROM (\
      SELECT id, description \
      FROM iris.gate_arm_interlock \
      ORDER BY id\
    ) r",
);

/// Gate arm state LUT resource
const GATE_ARM_STATE_RES: Resource = Resource::Simple(
    "gate_arm_state",
    Listen::Nope,
    "SELECT row_to_json(r)::text FROM (\
      SELECT id, description \
      FROM iris.gate_arm_state \
      ORDER BY id\
    ) r",
);

/// Lane use indication LUT resource
const LANE_USE_INDICATION_RES: Resource = Resource::Simple(
    "lane_use_indication",
    Listen::Nope,
    "SELECT row_to_json(r)::text FROM (\
      SELECT id, description \
      FROM iris.lane_use_indication \
      ORDER BY id\
    ) r",
);

/// LCS lock LUT resource
const LCS_LOCK_RES: Resource = Resource::Simple(
    "lcs_lock",
    Listen::Nope,
    "SELECT row_to_json(r)::text FROM (\
      SELECT id, description \
      FROM iris.lcs_lock \
      ORDER BY id\
    ) r",
);

/// Resource type LUT resource
const RESOURCE_TYPE_RES: Resource = Resource::Simple(
    "resource_type",
    Listen::Nope,
    "SELECT to_json(r.name)::text FROM (\
      SELECT name \
      FROM iris.resource_type \
      ORDER BY name\
    ) r",
);

/// Controller resource
const CONTROLLER_RES: Resource = Resource::Simple(
    "api/controller",
    Listen::All("controller"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT c.name, location, comm_link, drop_id, cabinet_style, condition, \
             notes, setup, fail_time \
      FROM iris.controller c \
      LEFT JOIN geo_loc_view gl ON c.geo_loc = gl.name \
      ORDER BY COALESCE(regexp_replace(comm_link, '[0-9]', '', 'g'), ''), \
              (regexp_replace(comm_link, '[^0-9]', '', 'g') || '0')::INTEGER, \
               drop_id\
    ) r",
);

/// Weather sensor resource
const WEATHER_SENSOR_RES: Resource = Resource::Simple(
    "api/weather_sensor",
    Listen::Exclude("weather_sensor", &["pin", "settings", "sample"]),
    "SELECT row_to_json(r)::text FROM (\
      SELECT ws.name, site_id, alt_id, location, controller, notes \
      FROM iris.weather_sensor ws \
      LEFT JOIN geo_loc_view gl ON ws.geo_loc = gl.name \
      ORDER BY name\
    ) r",
);

/// RWIS resource
const RWIS_RES: Resource = Resource::Simple(
    "rwis",
    Listen::All("weather_sensor"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT ws.name, location, lat, lon, settings, sample, sample_time \
      FROM iris.weather_sensor ws \
      LEFT JOIN geo_loc_view gl ON ws.geo_loc = gl.name \
      ORDER BY name\
    ) r",
);

/// Modem resource
const MODEM_RES: Resource = Resource::Simple(
    "api/modem",
    Listen::All("modem"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, enabled \
      FROM iris.modem \
      ORDER BY name\
    ) r",
);

/// Permission resource
const PERMISSION_RES: Resource = Resource::Simple(
    "api/permission",
    Listen::All("permission"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT id, role, resource_n, batch, access_n \
      FROM iris.permission \
      ORDER BY role, resource_n, id\
    ) r",
);

/// Role resource
const ROLE_RES: Resource = Resource::Simple(
    "api/role",
    Listen::All("role"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, enabled \
      FROM iris.role \
      ORDER BY name\
    ) r",
);

/// User resource
const USER_RES: Resource = Resource::Simple(
    "api/user",
    Listen::All("i_user"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, full_name, role, enabled \
      FROM iris.i_user \
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
      FROM sign_config_view\
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
      FROM sign_detail_view\
    ) r",
);

/// Sign message resource
const SIGN_MSG_RES: Resource = Resource::SignMsg(
    "sign_message",
    Listen::All("sign_message"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, sign_config, incident, multi, msg_owner, flash_beacon, \
             msg_priority, duration \
      FROM sign_message_view \
      ORDER BY name\
    ) r",
);

/// Public System attribute resource
const SYSTEM_ATTRIBUTE_PUB_RES: Resource = Resource::Simple(
    "system_attribute_pub",
    Listen::All("system_attribute"),
    "SELECT jsonb_object_agg(name, value)::text \
      FROM iris.system_attribute \
      WHERE name LIKE 'dms\\_%' OR name LIKE 'map\\_%'",
);

/// Static parking area resource
const TPIMS_STAT_RES: Resource = Resource::Simple(
    "TPIMS_static",
    Listen::Include("parking_area", &["time_stamp_static"]),
    "SELECT row_to_json(r)::text FROM (\
      SELECT site_id AS \"siteId\", \
             to_char(time_stamp_static AT TIME ZONE 'UTC', \
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
      FROM parking_area_view\
    ) r",
);

/// Dynamic parking area resource
const TPIMS_DYN_RES: Resource = Resource::Simple(
    "TPIMS_dynamic",
    Listen::Include("parking_area", &["time_stamp"]),
    "SELECT row_to_json(r)::text FROM (\
      SELECT site_id AS \"siteId\", \
             to_char(time_stamp AT TIME ZONE 'UTC', \
                     'YYYY-mm-dd\"T\"HH24:MI:SSZ') AS \"timeStamp\", \
             to_char(time_stamp_static AT TIME ZONE 'UTC', \
                     'YYYY-mm-dd\"T\"HH24:MI:SSZ') AS \"timeStampStatic\", \
             reported_available AS \"reportedAvailable\", \
             trend, open, trust_data AS \"trustData\", capacity \
      FROM parking_area_view\
    ) r",
);

/// Archive parking area resource
const TPIMS_ARCH_RES: Resource = Resource::Simple(
    "TPIMS_archive",
    Listen::Include("parking_area", &["time_stamp"]),
    "SELECT row_to_json(r)::text FROM (\
      SELECT site_id AS \"siteId\",
             to_char(time_stamp AT TIME ZONE 'UTC', \
                     'YYYY-mm-dd\"T\"HH24:MI:SSZ') AS \"timeStamp\", \
             to_char(time_stamp_static AT TIME ZONE 'UTC', \
                     'YYYY-mm-dd\"T\"HH24:MI:SSZ') AS \"timeStampStatic\", \
             reported_available AS \"reportedAvailable\", \
             trend, open, trust_data AS \"trustData\", capacity, \
             last_verification_check AS \"lastVerificationCheck\", \
             verification_check_amplitude AS \"verificationCheckAmplitude\", \
             low_threshold AS \"lowThreshold\", \
             true_available AS \"trueAvailable\" \
      FROM parking_area_view\
    ) r",
);

/// All defined resources
const ALL: &[Resource] = &[
    SYSTEM_ATTRIBUTE_PUB_RES, // System attributes must be loaded first
    ROAD_RES,                 // Roads must be loaded before R_Nodes
    ALARM_RES,
    BEACON_STATE_RES,
    BEACON_RES,
    LANE_MARKING_RES,
    CABINET_STYLE_RES,
    COMM_PROTOCOL_RES,
    COMM_CONFIG_RES,
    COMM_LINK_RES,
    CONDITION_RES,
    DIRECTION_RES,
    ROADWAY_RES,
    ROAD_MODIFIER_RES,
    GATE_ARM_INTERLOCK_RES,
    GATE_ARM_STATE_RES,
    LANE_USE_INDICATION_RES,
    LCS_LOCK_RES,
    RESOURCE_TYPE_RES,
    CONTROLLER_RES,
    WEATHER_SENSOR_RES,
    RWIS_RES,
    MODEM_RES,
    PERMISSION_RES,
    ROLE_RES,
    USER_RES,
    CAMERA_RES,
    CAMERA_PUB_RES,
    GATE_ARM_RES,
    GATE_ARM_ARRAY_RES,
    GPS_RES,
    LCS_ARRAY_RES,
    LCS_INDICATION_RES,
    RAMP_METER_RES,
    TAG_READER_RES,
    VIDEO_MONITOR_RES,
    FLOW_STREAM_RES,
    DMS_RES,
    DMS_PUB_RES,
    DMS_STAT_RES,
    FONT_RES,
    GRAPHIC_RES,
    INCIDENT_RES,
    R_NODE_RES,
    DETECTOR_RES,
    DETECTOR_PUB_RES,
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
        rename(backup, n)?;
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
    let mut channels = HashSet::new();
    for res in ALL {
        for channel in res.listen().channel_names() {
            channels.insert(channel);
        }
    }
    for channel in channels {
        let listen = format!("LISTEN {channel}");
        client.execute(&listen[..], &[])?;
    }
    Ok(())
}

/// Fetch all resources.
///
/// * `client` The database connection.
/// * `sender` Sender for segment messages.
pub fn fetch_all(client: &mut Client, sender: &Sender<SegMsg>) -> Result<()> {
    for res in ALL {
        res.fetch(client, "", sender)?;
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
    for res in ALL {
        if res.listen().is_listening(chan, payload) {
            found = true;
            res.fetch(client, payload, sender)?;
        } else if res.listen().is_listening_payload(chan, payload) {
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
        .any(|res| res.listen().is_listening_payload(chan, payload))
}
