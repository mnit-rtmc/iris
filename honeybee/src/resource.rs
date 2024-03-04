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
use crate::query;
use crate::segments::{RNode, Road, SegmentState};
use crate::signmsg::render_all;
use std::path::Path;
use std::time::Instant;
use tokio::io::{AsyncWrite, AsyncWriteExt};
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
    query::CAMERA_ALL,
);

/// Public Camera resource
const CAMERA_PUB_RES: Resource = Resource::Simple(
    "camera_pub",
    Some("camera"),
    query::CAMERA_PUB,
);

/// Gate arm resource
const GATE_ARM_RES: Resource = Resource::Simple(
    "api/gate_arm",
    Some("gate_arm"),
    query::GATE_ARM_ALL,
);

/// Gate arm array resource
const GATE_ARM_ARRAY_RES: Resource = Resource::Simple(
    "api/gate_arm_array",
    Some("gate_arm_array"),
    query::GATE_ARM_ARRAY_ALL,
);

/// GPS resource
const GPS_RES: Resource = Resource::Simple(
    "api/gps",
    Some("gps"),
    query::GPS_ALL,
);

/// LCS array resource
const LCS_ARRAY_RES: Resource = Resource::Simple(
    "api/lcs_array",
    Some("lcs_array"),
    query::LCS_ARRAY_ALL,
);

/// LCS indication resource
const LCS_INDICATION_RES: Resource = Resource::Simple(
    "api/lcs_indication",
    Some("lcs_indication"),
    query::LCS_INDICATION_ALL,
);

/// Ramp meter resource
const RAMP_METER_RES: Resource = Resource::Simple(
    "api/ramp_meter",
    Some("ramp_meter"),
    query::RAMP_METER_ALL,
);

/// Tag reader resource
const TAG_READER_RES: Resource = Resource::Simple(
    "api/tag_reader",
    Some("tag_reader"),
    query::TAG_READER_ALL,
);

/// Video monitor resource
const VIDEO_MONITOR_RES: Resource = Resource::Simple(
    "api/video_monitor",
    Some("video_monitor"),
    query::VIDEO_MONITOR_ALL,
);

/// Flow stream resource
const FLOW_STREAM_RES: Resource = Resource::Simple(
    "api/flow_stream",
    Some("flow_stream"),
    query::FLOW_STREAM_ALL,
);

/// Detector resource
const DETECTOR_RES: Resource = Resource::Simple(
    "api/detector",
    Some("detector"),
    query::DETECTOR_ALL,
);

/// Public Detector resource
const DETECTOR_PUB_RES: Resource = Resource::Simple(
    "detector_pub",
    Some("detector"),
    query::DETECTOR_PUB,
);

/// DMS resource
const DMS_RES: Resource = Resource::Simple(
    "api/dms",
    Some("dms"),
    query::DMS_ALL,
);

/// Public DMS resource
const DMS_PUB_RES: Resource = Resource::Simple(
    "dms_pub",
    Some("dms"),
    query::DMS_PUB,
);

/// DMS status resource
const DMS_STAT_RES: Resource = Resource::Simple(
    "dms_message",
    Some("dms"),
    query::DMS_STATUS,
);

/// Font list resource
const FONT_LIST_RES: Resource = Resource::Simple(
    "api/font",
    None,
    query::FONT_ALL,
);

/// Graphic list resource
const GRAPHIC_LIST_RES: Resource = Resource::Simple(
    "api/graphic",
    None,
    query::GRAPHIC_ALL,
);

/// Message line resource
const MSG_LINE_RES: Resource = Resource::Simple(
    "api/msg_line",
    Some("msg_line"),
    query::MSG_LINE_ALL,
);

/// Message pattern resource
const MSG_PATTERN_RES: Resource = Resource::Simple(
    "api/msg_pattern",
    Some("msg_pattern"),
    query::MSG_PATTERN_ALL,
);

/// Word resource
const WORD_RES: Resource = Resource::Simple(
    "api/word",
    Some("word"),
    query::WORD_ALL,
);

/// Incident resource
const INCIDENT_RES: Resource = Resource::Simple(
    "incident",
    Some("incident"),
    query::INCIDENT_PUB,
);

/// RNode resource
const R_NODE_RES: Resource = Resource::RNode();

/// Road resource
const ROAD_RES: Resource = Resource::Road();

/// Alarm resource
const ALARM_RES: Resource = Resource::Simple(
    "api/alarm",
    Some("alarm"),
    query::ALARM_ALL,
);

/// Beacon state LUT resource
const BEACON_STATE_RES: Resource = Resource::Simple(
    "beacon_state",
    None,
    query::BEACON_STATE_LUT,
);

/// Beacon resource
const BEACON_RES: Resource = Resource::Simple(
    "api/beacon",
    Some("beacon"),
    query::BEACON_ALL,
);

/// Lane marking resource
const LANE_MARKING_RES: Resource = Resource::Simple(
    "api/lane_marking",
    Some("lane_marking"),
    query::LANE_MARKING_ALL,
);

/// Cabinet style resource
const CABINET_STYLE_RES: Resource = Resource::Simple(
    "api/cabinet_style",
    Some("cabinet_style"),
    query::CABINET_STYLE_ALL,
);

/// Comm protocol LUT resource
const COMM_PROTOCOL_RES: Resource = Resource::Simple(
    "comm_protocol",
    None,
    query::COMM_PROTOCOL_LUT,
);

/// Comm configuration resource
const COMM_CONFIG_RES: Resource = Resource::Simple(
    "api/comm_config",
    Some("comm_config"),
    query::COMM_CONFIG_ALL,
);

/// Comm link resource
const COMM_LINK_RES: Resource = Resource::Simple(
    "api/comm_link",
    Some("comm_link"),
    query::COMM_LINK_ALL,
);

/// Controller condition LUT resource
const CONDITION_RES: Resource = Resource::Simple(
    "condition",
    None,
    query::CONDITION_LUT,
);

/// Direction LUT resource
const DIRECTION_RES: Resource = Resource::Simple(
    "direction",
    None,
    query::DIRECTION_LUT,
);

/// Roadway resource
const ROADWAY_RES: Resource = Resource::Simple(
    "api/road",
    Some("road$1"),
    query::ROAD_ALL,
);

/// Road modifier LUT resource
const ROAD_MODIFIER_RES: Resource = Resource::Simple(
    "road_modifier",
    None,
    query::ROAD_MODIFIER_LUT,
);

/// Gate arm interlock LUT resource
const GATE_ARM_INTERLOCK_RES: Resource = Resource::Simple(
    "gate_arm_interlock",
    None,
    query::GATE_ARM_INTERLOCK_LUT,
);

/// Gate arm state LUT resource
const GATE_ARM_STATE_RES: Resource = Resource::Simple(
    "gate_arm_state",
    None,
    query::GATE_ARM_STATE_LUT,
);

/// Lane use indication LUT resource
const LANE_USE_INDICATION_RES: Resource = Resource::Simple(
    "lane_use_indication",
    None,
    query::LANE_USE_INDICATION_LUT,
);

/// LCS lock LUT resource
const LCS_LOCK_RES: Resource = Resource::Simple(
    "lcs_lock",
    None,
    query::LCS_LOCK_LUT,
);

/// Resource type LUT resource
const RESOURCE_TYPE_RES: Resource = Resource::Simple(
    "resource_type",
    None,
    query::RESOURCE_TYPE_LUT,
);

/// Controller resource
const CONTROLLER_RES: Resource = Resource::Simple(
    "api/controller",
    Some("controller"),
    query::CONTROLLER_ALL,
);

/// Weather sensor resource
const WEATHER_SENSOR_RES: Resource = Resource::Simple(
    "api/weather_sensor",
    Some("weather_sensor"),
    query::WEATHER_SENSOR_ALL,
);

/// RWIS resource
const RWIS_RES: Resource = Resource::Simple(
    "rwis",
    Some("weather_sensor"),
    query::WEATHER_SENSOR_PUB,
);

/// Modem resource
const MODEM_RES: Resource = Resource::Simple(
    "api/modem",
    Some("modem"),
    query::MODEM_ALL,
);

/// Permission resource
const PERMISSION_RES: Resource = Resource::Simple(
    "api/permission",
    Some("permission"),
    query::PERMISSION_ALL,
);

/// Role resource
const ROLE_RES: Resource = Resource::Simple(
    "api/role",
    Some("role"),
    query::ROLE_ALL,
);

/// User resource
const USER_RES: Resource = Resource::Simple(
    "api/user",
    Some("i_user"),
    query::USER_ALL,
);

/// Sign configuration resource
const SIGN_CONFIG_RES: Resource = Resource::Simple(
    "api/sign_config",
    Some("sign_config"),
    query::SIGN_CONFIG_ALL,
);

/// Sign detail resource
const SIGN_DETAIL_RES: Resource = Resource::Simple(
    "api/sign_detail",
    Some("sign_detail"),
    query::SIGN_DETAIL_ALL,
);

/// Sign message resource
const SIGN_MSG_RES: Resource = Resource::SignMsg(
    query::SIGN_MSG_PUB,
);

/// Public System attribute resource
const SYSTEM_ATTRIBUTE_PUB_RES: Resource = Resource::Simple(
    "system_attribute_pub",
    Some("system_attribute"),
    query::SYSTEM_ATTRIBUTE_PUB,
);

/// Static parking area resource
const TPIMS_STAT_RES: Resource = Resource::Simple(
    "TPIMS_static",
    Some("parking_area"),
    query::PARKING_AREA_PUB,
);

/// Dynamic parking area resource
const TPIMS_DYN_RES: Resource = Resource::Simple(
    "TPIMS_dynamic",
    Some("parking_area$1"),
    query::PARKING_AREA_DYN,
);

/// Archive parking area resource
const TPIMS_ARCH_RES: Resource = Resource::Simple(
    "TPIMS_archive",
    Some("parking_area$1"),
    query::PARKING_AREA_ARCH,
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
/// * `segments` Segment state.
async fn query_all_nodes(
    client: &mut Client,
    segments: &mut SegmentState,
) -> Result<()> {
    log::trace!("query_all_nodes");
    segments.set_ordered(false).await?;
    for row in &client.query(query::RNODE_ALL_FULL, &[]).await? {
        segments.update_node(RNode::from_row(row));
    }
    segments.set_ordered(true).await?;
    Ok(())
}

/// Query one r_node.
///
/// * `client` The database connection.
/// * `name` RNode name.
/// * `segments` Segment state.
async fn query_one_node(
    client: &mut Client,
    name: &str,
    segments: &mut SegmentState,
) -> Result<()> {
    log::trace!("query_one_node: {name}");
    let rows = &client.query(query::RNODE_ONE, &[&name]).await?;
    if rows.len() == 1 {
        for row in rows.iter() {
            segments.update_node(RNode::from_row(row));
        }
    } else {
        assert!(rows.is_empty());
        segments.remove_node(name);
    }
    Ok(())
}

/// Query all roads.
///
/// * `client` The database connection.
/// * `segments` Segment state.
async fn query_all_roads(
    client: &mut Client,
    segments: &mut SegmentState,
) -> Result<()> {
    log::trace!("query_all_roads");
    for row in &client.query(query::ROAD_ALL_FULL, &[]).await? {
        segments.update_road(Road::from_row(row)).await?;
    }
    Ok(())
}

/// Query one road.
///
/// * `client` The database connection.
/// * `name` Road name.
/// * `segments` Segment state.
async fn query_one_road(
    client: &mut Client,
    name: &str,
    segments: &mut SegmentState,
) -> Result<()> {
    log::trace!("query_one_road: {name}");
    let rows = &client.query(query::ROAD_ONE, &[&name]).await?;
    if let Some(row) = rows.iter().next() {
        segments.update_road(Road::from_row(row)).await?;
    }
    Ok(())
}

impl Resource {
    /// Get iterator of all resource type variants
    pub fn iter() -> impl Iterator<Item = Resource> {
        [
            SYSTEM_ATTRIBUTE_PUB_RES,
            ROAD_RES,
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
    /// * `segments` Segment state.
    async fn query(
        self,
        client: &mut Client,
        payload: &str,
        segments: &mut SegmentState,
    ) -> Result<()> {
        match self {
            Resource::RNode() => {
                self.query_nodes(client, payload, segments).await
            }
            Resource::Road() => {
                self.query_roads(client, payload, segments).await
            }
            Resource::Simple(n, _, _) => self.query_file(client, n).await,
            Resource::SignMsg(_) => self.query_sign_msgs(client).await,
        }
    }

    /// Query r_node resource from a connection.
    ///
    /// * `client` The database connection.
    /// * `payload` Postgres NOTIFY payload.
    /// * `segments` Segment state.
    async fn query_nodes(
        self,
        client: &mut Client,
        payload: &str,
        segments: &mut SegmentState,
    ) -> Result<()> {
        // empty payload is used at startup
        if payload.is_empty() {
            query_all_nodes(client, segments).await
        } else {
            query_one_node(client, payload, segments).await
        }
    }

    /// Query road resource from a connection.
    ///
    /// * `client` The database connection.
    /// * `payload` Postgres NOTIFY payload.
    /// * `segments` Segment state.
    async fn query_roads(
        self,
        client: &mut Client,
        payload: &str,
        segments: &mut SegmentState,
    ) -> Result<()> {
        // empty payload is used at startup
        if payload.is_empty() {
            query_all_roads(client, segments).await
        } else {
            query_one_road(client, payload, segments).await
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
        render_all().await
    }
}

/// Query all resources.
///
/// * `client` The database connection.
/// * `segments` Segment state.
pub async fn query_all(client: &mut Client) -> Result<()> {
    for res in Resource::iter() {
        log::trace!("query_all: {res:?}");
        //res.query(client, "", segments).await?;
    }
    Ok(())
}

/// Handle a channel notification.
///
/// * `client` The database connection.
/// * `chan` Channel name.
/// * `payload` Postgres NOTIFY payload.
/// * `segments` Segment state.
pub async fn notify(
    client: &mut Client,
    chan: &str,
    payload: &str,
    segments: &mut SegmentState,
) -> Result<()> {
    log::info!("notify: {chan} {payload}");
    let mut found = false;
    for res in Resource::iter() {
        if let Some(lsn) = res.listen() {
            if lsn == chan {
                found = true;
                res.query(client, payload, segments).await?;
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
