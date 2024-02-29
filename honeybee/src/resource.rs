// resource.rs
//
// Copyright (C) 2018-2024  Minnesota Department of Transportation
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
use crate::files::AtomicFile;
use crate::segments::{RNode, Road, SegMsg};
use crate::signmsg::render_all;
use std::path::Path;
use std::time::Instant;
use tokio::io::{AsyncWrite, AsyncWriteExt};
use tokio::sync::mpsc::UnboundedSender;
use tokio_postgres::Client;

/// A resource which can be queried from a database connection.
#[derive(Clone, Copy, Debug, PartialEq, Eq, Hash)]
pub enum Resource {
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
/// Query a simple resource.
///
/// * `client` The database connection.
/// * `sql` SQL query.
/// * `w` Writer to output resource.
async fn query_simple<W>(
    client: &mut Client,
    sql: &str,
    mut w: W,
) -> Result<u32>
where
    W: AsyncWrite + Unpin,
{
    let mut c = 0;
    w.write_all(b"[").await?;
    for row in &client.query(sql, &[]).await? {
        if c > 0 {
            w.write_all(b",").await?;
        }
        w.write_all(b"\n").await?;
        let j: String = row.get(0);
        w.write_all(j.as_bytes()).await?;
        c += 1;
    }
    if c > 0 {
        w.write_all(b"\n").await?;
    }
    w.write_all(b"]\n").await?;
    Ok(c)
}

/// Query all r_nodes.
///
/// * `client` The database connection.
/// * `sender` Sender for segment messages.
async fn query_all_nodes(
    client: &mut Client,
    sender: &UnboundedSender<SegMsg>,
) -> Result<()> {
    log::trace!("query_all_nodes");
    sender.send(SegMsg::Order(false))?;
    for row in &client.query(RNode::SQL_ALL, &[]).await? {
        sender.send(SegMsg::UpdateNode(RNode::from_row(row)))?;
    }
    sender.send(SegMsg::Order(true))?;
    Ok(())
}

/// Query one r_node.
///
/// * `client` The database connection.
/// * `name` RNode name.
/// * `sender` Sender for segment messages.
async fn query_one_node(
    client: &mut Client,
    name: &str,
    sender: &UnboundedSender<SegMsg>,
) -> Result<()> {
    log::trace!("query_one_node: {name}");
    let rows = &client.query(RNode::SQL_ONE, &[&name]).await?;
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

/// Query all roads.
///
/// * `client` The database connection.
/// * `sender` Sender for segment messages.
async fn query_all_roads(
    client: &mut Client,
    sender: &UnboundedSender<SegMsg>,
) -> Result<()> {
    log::trace!("query_all_roads");
    for row in &client.query(Road::SQL_ALL, &[]).await? {
        sender.send(SegMsg::UpdateRoad(Road::from_row(row)))?;
    }
    Ok(())
}

/// Query one road.
///
/// * `client` The database connection.
/// * `name` Road name.
/// * `sender` Sender for segment messages.
async fn query_one_road(
    client: &mut Client,
    name: &str,
    sender: &UnboundedSender<SegMsg>,
) -> Result<()> {
    log::trace!("query_one_road: {name}");
    let rows = &client.query(Road::SQL_ONE, &[&name]).await?;
    if let Some(row) = rows.iter().next() {
        sender.send(SegMsg::UpdateRoad(Road::from_row(row)))?;
    }
    Ok(())
}

impl Resource {
    /// Get iterator of all resource type variants
    pub fn iter() -> impl Iterator<Item = Resource> {
        [
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
            GRAPHIC_LIST_RES,
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
        ]
        .iter()
        .cloned()
    }

    /// Get the listen value
    pub fn listen(self) -> Option<&'static str> {
        match self {
            Resource::RNode() => Some("r_node$1"),
            Resource::Road() => Some("road$1"),
            Resource::Simple(_, lsn, _) => lsn,
            Resource::SignMsg(_) => Some("sign_message"),
        }
    }

    /// Get the SQL value
    fn sql(self) -> &'static str {
        match self {
            Resource::RNode() => unreachable!(),
            Resource::Road() => unreachable!(),
            Resource::Simple(_, _, sql) => sql,
            Resource::SignMsg(sql) => sql,
        }
    }

    /// Query the resource from a connection.
    ///
    /// * `client` The database connection.
    /// * `payload` Postgres NOTIFY payload.
    /// * `sender` Sender for segment messages.
    async fn query(
        self,
        client: &mut Client,
        payload: &str,
        sender: &UnboundedSender<SegMsg>,
    ) -> Result<()> {
        match self {
            Resource::RNode() => {
                self.query_nodes(client, payload, sender).await
            }
            Resource::Road() => self.query_roads(client, payload, sender).await,
            Resource::Simple(n, _, _) => self.query_file(client, n).await,
            Resource::SignMsg(_) => self.query_sign_msgs(client).await,
        }
    }

    /// Query r_node resource from a connection.
    ///
    /// * `client` The database connection.
    /// * `payload` Postgres NOTIFY payload.
    /// * `sender` Sender for segment messages.
    async fn query_nodes(
        self,
        client: &mut Client,
        payload: &str,
        sender: &UnboundedSender<SegMsg>,
    ) -> Result<()> {
        // empty payload is used at startup
        if payload.is_empty() {
            query_all_nodes(client, sender).await
        } else {
            query_one_node(client, payload, sender).await
        }
    }

    /// Query road resource from a connection.
    ///
    /// * `client` The database connection.
    /// * `payload` Postgres NOTIFY payload.
    /// * `sender` Sender for segment messages.
    async fn query_roads(
        self,
        client: &mut Client,
        payload: &str,
        sender: &UnboundedSender<SegMsg>,
    ) -> Result<()> {
        // empty payload is used at startup
        if payload.is_empty() {
            query_all_roads(client, sender).await
        } else {
            query_one_road(client, payload, sender).await
        }
    }

    /// Query a file resource from a connection.
    ///
    /// * `client` The database connection.
    /// * `name` File name.
    async fn query_file(self, client: &mut Client, name: &str) -> Result<()> {
        log::trace!("query_file: {name:?}");
        let t = Instant::now();
        let dir = Path::new("");
        let file = AtomicFile::new(dir, name).await?;
        let writer = file.writer().await?;
        let sql = self.sql();
        let count = query_simple(client, sql, writer).await?;
        file.commit().await?;
        log::info!("{name}: wrote {count} rows in {:?}", t.elapsed());
        Ok(())
    }

    /// Query sign messages resource.
    async fn query_sign_msgs(self, client: &mut Client) -> Result<()> {
        self.query_file(client, "sign_message").await?;
        // FIXME: spawn another thread for this?
        render_all(Path::new("")).await
    }
}

/// Query all resources.
///
/// * `client` The database connection.
/// * `sender` Sender for segment messages.
pub async fn query_all(client: &mut Client) -> Result<()> {
    for res in Resource::iter() {
        log::trace!("query_all: {res:?}");
        res.query(client, "", sender).await?;
    }
    Ok(())
}

/// Handle a channel notification.
///
/// * `client` The database connection.
/// * `chan` Channel name.
/// * `payload` Postgres NOTIFY payload.
/// * `sender` Sender for segment messages.
pub async fn notify(
    client: &mut Client,
    chan: &str,
    payload: &str,
    sender: &UnboundedSender<SegMsg>,
) -> Result<()> {
    log::info!("notify: {chan} {payload}");
    let mut found = false;
    for res in Resource::iter() {
        if let Some(lsn) = res.listen() {
            if lsn == chan {
                found = true;
                res.query(client, payload, sender).await?;
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
    Resource::iter().any(|res| res.listen() == Some(chan))
}
