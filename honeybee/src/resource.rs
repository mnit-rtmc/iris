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
use crate::files::AtomicFile;
use crate::segments::{RNode, Road, SegMsg};
use crate::signmsg::render_all;
use crate::Result;
use fstr::FStr;
use gift::{Encoder, Step};
use ntcip::dms::multi::Color;
use ntcip::dms::{tfon, CharacterEntry, Font, Graphic};
use pix::{rgb::SRgb8, Palette};
use postgres::Client;
use serde_derive::Deserialize;
use std::collections::HashSet;
use std::io::Write;
use std::path::Path;
use std::sync::mpsc::Sender;
use std::time::Instant;

/// Glyph from font
#[derive(Deserialize)]
struct Glyph {
    code_point: u16,
    width: u8,
    #[serde(with = "super::base64")]
    bitmap: Vec<u8>,
}

/// Font resource
#[derive(Deserialize)]
struct FontRes {
    f_number: u8,
    name: String,
    height: u8,
    #[allow(dead_code)]
    width: u8,
    char_spacing: u8,
    line_spacing: u8,
    glyphs: Vec<Glyph>,
}

/// Graphic resource
#[derive(Deserialize)]
struct GraphicRes {
    number: u8,
    name: String,
    height: u8,
    width: u16,
    color_scheme: String,
    transparent_color: Option<i32>,
    #[serde(with = "super::base64")]
    bitmap: Vec<u8>,
}

impl From<Glyph> for CharacterEntry {
    fn from(gl: Glyph) -> Self {
        CharacterEntry {
            number: gl.code_point,
            width: gl.width,
            bitmap: gl.bitmap,
        }
    }
}

impl<const C: usize> From<FontRes> for Font<C> {
    fn from(fr: FontRes) -> Self {
        let mut glyphs = fr.glyphs.into_iter();
        Font {
            number: fr.f_number,
            name: FStr::from_str_lossy(&fr.name, 0),
            height: fr.height,
            char_spacing: fr.char_spacing,
            line_spacing: fr.line_spacing,
            characters: std::array::from_fn(|_i| match glyphs.next() {
                Some(glyph) => CharacterEntry::from(glyph),
                None => CharacterEntry::default(),
            }),
        }
    }
}

impl From<GraphicRes> for Graphic {
    fn from(gr: GraphicRes) -> Self {
        let transparent_color = match gr.transparent_color {
            Some(tc) => {
                let red = (tc >> 16) as u8;
                let green = (tc >> 8) as u8;
                let blue = tc as u8;
                Some(Color::Rgb(red, green, blue))
            }
            _ => None,
        };
        Graphic {
            number: gr.number,
            name: FStr::from_str_lossy(&gr.name, 0),
            height: gr.height,
            width: gr.width,
            gtype: gr.color_scheme[..].into(),
            transparent_color,
            bitmap: gr.bitmap,
        }
    }
}

/// A resource which can be fetched from a database connection.
#[derive(Clone, Copy, Debug, PartialEq, Eq, Hash)]
enum Resource {
    /// Font resource.
    ///
    /// * SQL query.
    Font(&'static str),

    /// Graphic resource.
    ///
    /// * SQL query.
    Graphic(&'static str),

    /// RNode resource.
    RNode(),

    /// Road resource.
    Road(),

    /// Simple file resource.
    ///
    /// * File name.
    /// * Listen channel.
    /// * SQL query.
    Simple(&'static str, Option<&'static str>, &'static str),

    /// Sign message resource.
    ///
    /// * SQL query.
    SignMsg(&'static str),
}

/// Camera resource
const CAMERA_RES: Resource = Resource::Simple(
    "api/camera",
    Some("camera"),
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
    Some("camera"),
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
    Some("gate_arm"),
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
    Some("gate_arm_array"),
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
    Some("gps"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, controller, notes \
      FROM iris.gps \
      ORDER BY name\
    ) r",
);

/// LCS array resource
const LCS_ARRAY_RES: Resource = Resource::Simple(
    "api/lcs_array",
    Some("lcs_array"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, notes, lcs_lock \
      FROM iris.lcs_array \
      ORDER BY name\
    ) r",
);

/// LCS indication resource
const LCS_INDICATION_RES: Resource = Resource::Simple(
    "api/lcs_indication",
    Some("lcs_indication"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, controller, lcs, indication \
      FROM iris.lcs_indication \
      ORDER BY name\
    ) r",
);

/// Ramp meter resource
const RAMP_METER_RES: Resource = Resource::Simple(
    "api/ramp_meter",
    Some("ramp_meter"),
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
    Some("tag_reader"),
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
    Some("video_monitor"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, mon_num, controller, notes \
      FROM iris.video_monitor \
      ORDER BY mon_num, name\
    ) r",
);

/// Flow stream resource
const FLOW_STREAM_RES: Resource = Resource::Simple(
    "api/flow_stream",
    Some("flow_stream"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, controller \
      FROM iris.flow_stream \
      ORDER BY name\
    ) r",
);

/// Detector resource
const DETECTOR_RES: Resource = Resource::Simple(
    "api/detector",
    Some("detector"),
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
    Some("detector"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, r_node, cor_id, lane_number, lane_code, field_length \
      FROM detector_view\
    ) r",
);

/// DMS resource
const DMS_RES: Resource = Resource::Simple(
    "api/dms",
    Some("dms"),
    "SELECT json_strip_nulls(row_to_json(r))::text FROM (\
      SELECT d.name, location, msg_current, \
             NULLIF(char_length(status->>'faults') > 0, false) AS has_faults, \
             NULLIF(notes, '') AS notes, hashtags, controller \
      FROM iris.dms d \
      LEFT JOIN geo_loc_view gl ON d.geo_loc = gl.name \
      LEFT JOIN (\
        SELECT dms, string_agg(hashtag, ' ' ORDER BY hashtag) AS hashtags \
        FROM iris.dms_hashtag \
        GROUP BY dms\
      ) h ON d.name = h.dms \
      ORDER BY d.name\
    ) r",
);

/// Public DMS resource
const DMS_PUB_RES: Resource = Resource::Simple(
    "dms_pub",
    Some("dms"),
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
    Some("dms"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, msg_current, \
             replace(substring(msg_owner FROM 'IRIS; ([^;]*).*'), '+', ', ') \
             AS sources, failed, duration, expire_time \
      FROM dms_message_view WHERE condition = 'Active' \
      ORDER BY name\
    ) r",
);

/// Font list resource
const FONT_LIST_RES: Resource = Resource::Simple(
    "api/font",
    None,
    "SELECT row_to_json(r)::text FROM (\
      SELECT f_number AS font_number, name \
      FROM iris.font ORDER BY f_number\
    ) r",
);

/// Font resource
const FONT_RES: Resource = Resource::Font(
    "SELECT row_to_json(f)::text FROM (\
      SELECT f_number, name, height, width, char_spacing, line_spacing, \
             array(SELECT row_to_json(c) FROM (\
               SELECT code_point, width, \
                      replace(pixels, E'\n', '') AS bitmap \
               FROM iris.glyph \
               WHERE font = ft.name \
               ORDER BY code_point \
             ) AS c) \
           AS glyphs \
      FROM iris.font ft ORDER BY name\
    ) AS f",
);

/// Graphic list resource
const GRAPHIC_LIST_RES: Resource = Resource::Simple(
    "api/graphic",
    None,
    "SELECT row_to_json(r)::text FROM (\
      SELECT g_number AS number, 'G' || g_number AS name \
      FROM iris.graphic \
      WHERE g_number < 256 \
      ORDER BY number\
    ) r",
);

/// Graphic resource
const GRAPHIC_RES: Resource = Resource::Graphic(
    "SELECT row_to_json(r)::text FROM (\
      SELECT g_number AS number, name, height, width, color_scheme, \
             transparent_color, replace(pixels, E'\n', '') AS bitmap \
      FROM graphic_view \
      WHERE g_number < 256\
    ) r",
);

/// Message line resource
const MSG_LINE_RES: Resource = Resource::Simple(
    "api/msg_line",
    Some("msg_line"),
    "SELECT json_strip_nulls(row_to_json(r))::text FROM (\
      SELECT name, msg_pattern, line, multi, restrict_hashtag \
      FROM iris.msg_line \
      ORDER BY msg_pattern, line, rank, multi, restrict_hashtag\
    ) r",
);

/// Message pattern resource
const MSG_PATTERN_RES: Resource = Resource::Simple(
    "api/msg_pattern",
    Some("msg_pattern"),
    "SELECT json_strip_nulls(row_to_json(r))::text FROM (\
      SELECT name, multi, compose_hashtag \
      FROM iris.msg_pattern \
      ORDER BY name\
    ) r",
);

/// Word resource
const WORD_RES: Resource = Resource::Simple(
    "api/word",
    Some("word"),
    "SELECT json_strip_nulls(row_to_json(r))::text FROM (\
      SELECT name, abbr, allowed \
      FROM iris.word \
      ORDER BY name\
    ) r",
);

/// Incident resource
const INCIDENT_RES: Resource = Resource::Simple(
    "incident",
    Some("incident"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, event_date, description, road, direction, lane_type, \
             impact, confirmed, camera, detail, replaces, lat, lon \
      FROM incident_view \
      WHERE cleared = false\
    ) r",
);

/// RNode resource
const R_NODE_RES: Resource = Resource::RNode();

/// Road resource
const ROAD_RES: Resource = Resource::Road();

/// Alarm resource
const ALARM_RES: Resource = Resource::Simple(
    "api/alarm",
    Some("alarm"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, description, controller, state \
      FROM iris.alarm \
      ORDER BY description\
    ) r",
);

/// Beacon state LUT resource
const BEACON_STATE_RES: Resource = Resource::Simple(
    "beacon_state",
    None,
    "SELECT row_to_json(r)::text FROM (\
      SELECT id, description \
      FROM iris.beacon_state \
      ORDER BY id\
    ) r",
);

/// Beacon resource
const BEACON_RES: Resource = Resource::Simple(
    "api/beacon",
    Some("beacon"),
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
    Some("lane_marking"),
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
    Some("cabinet_style"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name \
      FROM iris.cabinet_style \
      ORDER BY name\
    ) r",
);

/// Comm protocol LUT resource
const COMM_PROTOCOL_RES: Resource = Resource::Simple(
    "comm_protocol",
    None,
    "SELECT row_to_json(r)::text FROM (\
      SELECT id, description \
      FROM iris.comm_protocol \
      ORDER BY description\
    ) r",
);

/// Comm configuration resource
const COMM_CONFIG_RES: Resource = Resource::Simple(
    "api/comm_config",
    Some("comm_config"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, description \
      FROM iris.comm_config \
      ORDER BY description\
    ) r",
);

/// Comm link resource
const COMM_LINK_RES: Resource = Resource::Simple(
    "api/comm_link",
    Some("comm_link"),
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
    None,
    "SELECT row_to_json(r)::text FROM (\
      SELECT id, description \
      FROM iris.condition \
      ORDER BY description\
    ) r",
);

/// Direction LUT resource
const DIRECTION_RES: Resource = Resource::Simple(
    "direction",
    None,
    "SELECT row_to_json(r)::text FROM (\
      SELECT id, direction, dir \
      FROM iris.direction \
      ORDER BY id\
    ) r",
);

/// Roadway resource
const ROADWAY_RES: Resource = Resource::Simple(
    "api/road",
    Some("road$1"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, abbrev, r_class, direction \
      FROM iris.road \
      ORDER BY name\
    ) r",
);

/// Road modifier LUT resource
const ROAD_MODIFIER_RES: Resource = Resource::Simple(
    "road_modifier",
    None,
    "SELECT row_to_json(r)::text FROM (\
      SELECT id, modifier, mod AS md \
      FROM iris.road_modifier \
      ORDER BY id\
    ) r",
);

/// Gate arm interlock LUT resource
const GATE_ARM_INTERLOCK_RES: Resource = Resource::Simple(
    "gate_arm_interlock",
    None,
    "SELECT row_to_json(r)::text FROM (\
      SELECT id, description \
      FROM iris.gate_arm_interlock \
      ORDER BY id\
    ) r",
);

/// Gate arm state LUT resource
const GATE_ARM_STATE_RES: Resource = Resource::Simple(
    "gate_arm_state",
    None,
    "SELECT row_to_json(r)::text FROM (\
      SELECT id, description \
      FROM iris.gate_arm_state \
      ORDER BY id\
    ) r",
);

/// Lane use indication LUT resource
const LANE_USE_INDICATION_RES: Resource = Resource::Simple(
    "lane_use_indication",
    None,
    "SELECT row_to_json(r)::text FROM (\
      SELECT id, description \
      FROM iris.lane_use_indication \
      ORDER BY id\
    ) r",
);

/// LCS lock LUT resource
const LCS_LOCK_RES: Resource = Resource::Simple(
    "lcs_lock",
    None,
    "SELECT row_to_json(r)::text FROM (\
      SELECT id, description \
      FROM iris.lcs_lock \
      ORDER BY id\
    ) r",
);

/// Resource type LUT resource
const RESOURCE_TYPE_RES: Resource = Resource::Simple(
    "resource_type",
    None,
    "SELECT to_json(r.name)::text FROM (\
      SELECT name \
      FROM iris.resource_type \
      ORDER BY name\
    ) r",
);

/// Controller resource
const CONTROLLER_RES: Resource = Resource::Simple(
    "api/controller",
    Some("controller"),
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
    Some("weather_sensor"),
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
    Some("weather_sensor"),
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
    Some("modem"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, enabled \
      FROM iris.modem \
      ORDER BY name\
    ) r",
);

/// Permission resource
const PERMISSION_RES: Resource = Resource::Simple(
    "api/permission",
    Some("permission"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT id, role, resource_n, hashtag, access_n \
      FROM iris.permission \
      ORDER BY role, resource_n, id\
    ) r",
);

/// Role resource
const ROLE_RES: Resource = Resource::Simple(
    "api/role",
    Some("role"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, enabled \
      FROM iris.role \
      ORDER BY name\
    ) r",
);

/// User resource
const USER_RES: Resource = Resource::Simple(
    "api/user",
    Some("i_user"),
    "SELECT row_to_json(r)::text FROM (\
      SELECT name, full_name, role, enabled \
      FROM iris.i_user \
      ORDER BY name\
    ) r",
);

/// Sign configuration resource
const SIGN_CONFIG_RES: Resource = Resource::Simple(
    "api/sign_config",
    Some("sign_config"),
    "SELECT json_strip_nulls(row_to_json(r))::text FROM (\
      SELECT name, face_width, face_height, border_horiz, border_vert, \
             pitch_horiz, pitch_vert, pixel_width, pixel_height, \
             char_width, char_height, monochrome_foreground, \
             monochrome_background, color_scheme, default_font, \
             module_width, module_height \
      FROM sign_config_view\
    ) r",
);

/// Sign detail resource
const SIGN_DETAIL_RES: Resource = Resource::Simple(
    "api/sign_detail",
    Some("sign_detail"),
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
    Some("system_attribute"),
    "SELECT jsonb_object_agg(name, value)::text \
      FROM iris.system_attribute \
      WHERE name LIKE 'dms\\_%' OR name LIKE 'map\\_%'",
);

/// Static parking area resource
const TPIMS_STAT_RES: Resource = Resource::Simple(
    "TPIMS_static",
    Some("parking_area"),
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
    Some("parking_area$1"),
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
    Some("parking_area$1"),
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
    FONT_LIST_RES,
    FONT_RES,
    GRAPHIC_LIST_RES,
    GRAPHIC_RES,
    MSG_LINE_RES,
    MSG_PATTERN_RES,
    WORD_RES,
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
    log::debug!("fetch_all_nodes");
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
    log::debug!("fetch_one_node: {}", name);
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
    log::debug!("fetch_all_roads");
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
    log::debug!("fetch_one_road: {}", name);
    let rows = &client.query(Road::SQL_ONE, &[&name])?;
    if let Some(row) = rows.iter().next() {
        sender.send(SegMsg::UpdateRoad(Road::from_row(row)))?;
    }
    Ok(())
}

impl Resource {
    /// Get the listen value
    fn listen(self) -> Option<&'static str> {
        match self {
            Resource::Font(_) => None,
            Resource::Graphic(_) => None,
            Resource::RNode() => Some("r_node$1"),
            Resource::Road() => Some("road$1"),
            Resource::Simple(_, lsn, _) => lsn,
            Resource::SignMsg(_) => Some("sign_message"),
        }
    }

    /// Get the SQL value
    fn sql(self) -> &'static str {
        match self {
            Resource::Font(sql) => sql,
            Resource::Graphic(sql) => sql,
            Resource::RNode() => unreachable!(),
            Resource::Road() => unreachable!(),
            Resource::Simple(_, _, sql) => sql,
            Resource::SignMsg(sql) => sql,
        }
    }

    /// Fetch the resource from a connection.
    ///
    /// * `client` The database connection.
    /// * `payload` Postgres NOTIFY payload.
    /// * `sender` Sender for segment messages.
    fn fetch(
        self,
        client: &mut Client,
        payload: &str,
        sender: &Sender<SegMsg>,
    ) -> Result<()> {
        match self {
            Resource::Font(_) => self.fetch_fonts(client),
            Resource::Graphic(_) => self.fetch_graphics(client),
            Resource::RNode() => self.fetch_nodes(client, payload, sender),
            Resource::Road() => self.fetch_roads(client, payload, sender),
            Resource::Simple(n, _, _) => self.fetch_file(client, n),
            Resource::SignMsg(_) => self.fetch_sign_msgs(client),
        }
    }

    /// Fetch r_node resource from a connection.
    ///
    /// * `client` The database connection.
    /// * `payload` Postgres NOTIFY payload.
    /// * `sender` Sender for segment messages.
    fn fetch_nodes(
        self,
        client: &mut Client,
        payload: &str,
        sender: &Sender<SegMsg>,
    ) -> Result<()> {
        // empty payload is used at startup
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
        self,
        client: &mut Client,
        payload: &str,
        sender: &Sender<SegMsg>,
    ) -> Result<()> {
        // empty payload is used at startup
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
    fn fetch_file(self, client: &mut Client, name: &str) -> Result<()> {
        log::debug!("fetch_file: {:?}", name);
        let t = Instant::now();
        let dir = Path::new("");
        let file = AtomicFile::new(dir, name)?;
        let writer = file.writer()?;
        let sql = self.sql();
        let count = fetch_simple(client, sql, writer)?;
        drop(file);
        log::info!("{}: wrote {} rows in {:?}", name, count, t.elapsed());
        Ok(())
    }

    /// Fetch font resources
    fn fetch_fonts(self, client: &mut Client) -> Result<()> {
        log::debug!("fetch_fonts");
        let t = Instant::now();
        let dir = Path::new("api/tfon");
        let mut count = 0;
        let sql = self.sql();
        for row in &client.query(sql, &[])? {
            let font: FontRes = serde_json::from_str(row.get(0))?;
            let font: Font<256> = font.into();
            let name = format!("{}.tfon", font.name.slice_to_terminator('\0'));
            let file = AtomicFile::new(dir, &name)?;
            let writer = file.writer()?;
            if let Err(e) = tfon::write(writer, &font) {
                log::error!("fetch_fonts {name}: {e:?}");
                let _res = file.cancel();
            }
            count += 1;
        }
        log::info!("fetch_fonts: wrote {count} rows in {:?}", t.elapsed());
        Ok(())
    }

    /// Fetch graphics resource.
    fn fetch_graphics(self, client: &mut Client) -> Result<()> {
        log::debug!("fetch_graphics");
        let t = Instant::now();
        let dir = Path::new("api/gif");
        let mut count = 0;
        let sql = self.sql();
        for row in &client.query(sql, &[])? {
            write_graphic(dir, serde_json::from_str(row.get(0))?)?;
            count += 1;
        }
        log::info!("fetch_graphics: wrote {count} rows in {:?}", t.elapsed());
        Ok(())
    }

    /// Fetch sign messages resource.
    fn fetch_sign_msgs(self, client: &mut Client) -> Result<()> {
        self.fetch_file(client, "sign_message")?;
        // FIXME: spawn another thread for this?
        render_all(Path::new(""))
    }
}

/// Write a graphic image to a file
fn write_graphic(dir: &Path, graphic: GraphicRes) -> Result<()> {
    let graphic = Graphic::from(graphic);
    let name = format!("G{}.gif", graphic.number);
    let raster = graphic.to_raster();
    let mut palette = Palette::new(256);
    if let Some(Color::Rgb(red, green, blue)) = graphic.transparent_color {
        palette.set_entry(SRgb8::new(red, green, blue));
        log::debug!("write_graphic: {name}, transparent {red},{green},{blue}");
    }
    palette.set_threshold_fn(palette_threshold_rgb8_256);
    let indexed = palette.make_indexed(raster);
    log::debug!("write_graphic: {name}, {}", palette.len());
    let file = AtomicFile::new(dir, &name)?;
    let mut writer = file.writer()?;
    let mut enc = Encoder::new(&mut writer).into_step_enc();
    let step = Step::with_indexed(indexed, palette).with_transparent_color(
        // transparent color always palette index 0
        graphic.transparent_color.map(|_| 0),
    );
    enc.encode_step(&step)?;
    Ok(())
}

/// Get the difference threshold for SRgb8 with 256 capacity palette
fn palette_threshold_rgb8_256(v: usize) -> SRgb8 {
    let val = (v & 0xFF) as u8;
    SRgb8::new(val >> 5, val >> 5, val >> 4)
}

/// Listen for notifications on all channels we need to monitor.
///
/// * `client` Database connection.
pub fn listen_all(client: &mut Client) -> Result<()> {
    let mut channels = HashSet::new();
    for res in ALL {
        if let Some(lsn) = res.listen() {
            channels.insert(lsn);
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
        log::debug!("fetch_all: {res:?}");
        res.fetch(client, "", sender)?;
    }
    Ok(())
}

/// Handle a channel notification.
///
/// * `client` The database connection.
/// * `chan` Channel name.
/// * `payload` Postgres NOTIFY payload.
/// * `sender` Sender for segment messages.
pub fn notify(
    client: &mut Client,
    chan: &str,
    payload: &str,
    sender: &Sender<SegMsg>,
) -> Result<()> {
    log::info!("notify: {chan} {payload}");
    let mut found = false;
    for res in ALL {
        if let Some(lsn) = res.listen() {
            if lsn == chan {
                found = true;
                res.fetch(client, payload, sender)?;
            }
        }
    }
    if !found {
        log::warn!("unknown resource: {chan} {payload}");
    }
    Ok(())
}

/// Check if any resource is listening to a channel
pub fn is_listening(chan: &str) -> bool {
    ALL.iter().any(|res| res.listen() == Some(chan))
}
