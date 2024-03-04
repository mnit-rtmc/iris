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
    Alarm,
    Beacon,
    BeaconState,
    CabinetStyle,
    Camera,
    CameraPub,
    CommConfig,
    CommLink,
    CommProtocol,
    Condition,
    Controller,
    Detector,
    DetectorPub,
    Direction,
    Dms,
    DmsPub,
    DmsStat,
    FlowStream,
    FontList,
    GateArm,
    GateArmArray,
    GateArmInterlock,
    GateArmState,
    Gps,
    GraphicList,
    Incident,
    LaneMarking,
    LaneUseIndication,
    LcsArray,
    LcsIndication,
    LcsLock,
    Modem,
    MsgLine,
    MsgPattern,
    ParkingAreaPub,
    ParkingAreaDyn,
    ParkingAreaArch,
    Permission,
    Rnode,
    RampMeter,
    ResourceType,
    Road,
    RoadFull,
    RoadModifier,
    Role,
    SignConfig,
    SignDetail,
    SignMessage,
    SystemAttributePub,
    TagReader,
    User,
    VideoMonitor,
    WeatherSensor,
    WeatherSensorPub,
    Word,
}

/// Query a JSON resource.
///
/// * `client` The database connection.
/// * `sql` SQL query.
/// * `w` Writer to output resource.
async fn query_json<W>(
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
    for row in &client.query(query::RNODE_FULL, &[]).await? {
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
    for row in &client.query(query::ROAD_FULL, &[]).await? {
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
        use Resource::*;
        [
            Alarm,
            Beacon,
            BeaconState,
            CabinetStyle,
            Camera,
            CameraPub,
            CommConfig,
            CommLink,
            CommProtocol,
            Condition,
            Controller,
            Detector,
            DetectorPub,
            Direction,
            Dms,
            DmsPub,
            DmsStat,
            FlowStream,
            FontList,
            GateArm,
            GateArmArray,
            GateArmInterlock,
            GateArmState,
            Gps,
            GraphicList,
            Incident,
            LaneMarking,
            LaneUseIndication,
            LcsArray,
            LcsIndication,
            LcsLock,
            Modem,
            MsgLine,
            MsgPattern,
            ParkingAreaPub,
            ParkingAreaDyn,
            ParkingAreaArch,
            Permission,
            Rnode,
            RampMeter,
            ResourceType,
            Road,
            RoadFull,
            RoadModifier,
            Role,
            SignConfig,
            SignDetail,
            SignMessage,
            SystemAttributePub,
            TagReader,
            User,
            VideoMonitor,
            WeatherSensor,
            WeatherSensorPub,
            Word,
        ]
        .iter()
        .cloned()
    }

    /// Get the resource path
    const fn path(self) -> &'static str {
        use Resource::*;
        match self {
            Alarm => "api/alarm",
            Beacon => "api/beacon",
            BeaconState => "beacon_state",
            CabinetStyle => "api/cabinet_style",
            Camera => "api/camera",
            CameraPub => "camera_pub",
            CommConfig => "api/comm_config",
            CommLink => "api/comm_link",
            CommProtocol => "comm_protocol",
            Condition => "condition",
            Controller => "api/controller",
            Detector => "api/detector",
            DetectorPub => "detector_pub",
            Direction => "direction",
            Dms => "api/dms",
            DmsPub => "dms_pub",
            DmsStat => "dms_message",
            FlowStream => "api/flow_stream",
            FontList => "api/font",
            GateArm => "api/gate_arm",
            GateArmArray => "api/gate_arm_array",
            GateArmInterlock => "gate_arm_interlock",
            GateArmState => "gate_arm_state",
            Gps => "api/gps",
            GraphicList => "api/graphic",
            Incident => "incident",
            LaneMarking => "api/lane_marking",
            LaneUseIndication => "lane_use_indication",
            LcsArray => "api/lcs_array",
            LcsIndication => "api/lcs_indication",
            LcsLock => "lcs_lock",
            Modem => "api/modem",
            MsgLine => "api/msg_line",
            MsgPattern => "api/msg_pattern",
            ParkingAreaPub => "TPIMS_static",
            ParkingAreaDyn => "TPIMS_dynamic",
            ParkingAreaArch => "TPIMS_archive",
            Permission => "api/permission",
            Rnode => unreachable!(),
            RampMeter => "api/ramp_meter",
            ResourceType => "resource_type",
            Road => "api/road",
            RoadFull => unreachable!(),
            RoadModifier => "road_modifier",
            Role => "api/role",
            SignConfig => "api/sign_config",
            SignDetail => "api/sign_detail",
            SignMessage => "sign_message",
            SystemAttributePub => "system_attribute_pub",
            TagReader => "api/tag_reader",
            User => "api/user",
            VideoMonitor => "api/video_monitor",
            WeatherSensor => "api/weather_sensor",
            WeatherSensorPub => "rwis",
            Word => "api/word",
        }
    }

    /// Get the listen channel
    pub const fn listen(self) -> Option<&'static str> {
        use Resource::*;
        match self {
            Alarm => Some("alarm"),
            Beacon => Some("beacon"),
            CabinetStyle => Some("cabinet_style"),
            Camera | CameraPub => Some("camera"),
            CommConfig => Some("comm_config"),
            CommLink => Some("comm_link"),
            Controller => Some("controller"),
            Detector | DetectorPub => Some("detector"),
            Dms | DmsPub | DmsStat => Some("dms"),
            FlowStream => Some("flow_stream"),
            GateArm => Some("gate_arm"),
            GateArmArray => Some("gate_arm_array"),
            Gps => Some("gps"),
            Incident => Some("incident"),
            LaneMarking => Some("lane_marking"),
            LcsArray => Some("lcs_array"),
            LcsIndication => Some("lcs_indication"),
            Modem => Some("modem"),
            MsgLine => Some("msg_line"),
            MsgPattern => Some("msg_pattern"),
            ParkingAreaPub => Some("parking_area"),
            ParkingAreaDyn | ParkingAreaArch => Some("parking_area$1"),
            Permission => Some("permission"),
            Rnode => Some("r_node$1"),
            RampMeter => Some("ramp_meter"),
            Road | RoadFull => Some("road$1"),
            Role => Some("role"),
            SignConfig => Some("sign_config"),
            SignDetail => Some("sign_detail"),
            SignMessage => Some("sign_message"),
            SystemAttributePub => Some("system_attribute"),
            TagReader => Some("tag_reader"),
            User => Some("i_user"),
            VideoMonitor => Some("video_monitor"),
            WeatherSensor | WeatherSensorPub => Some("weather_sensor"),
            Word => Some("word"),
            _ => None,
        }
    }

    /// Get the SQL to query all
    const fn all_sql(self) -> &'static str {
        use Resource::*;
        match self {
            Alarm => query::ALARM_ALL,
            Beacon => query::BEACON_ALL,
            BeaconState => query::BEACON_STATE_LUT,
            CabinetStyle => query::CABINET_STYLE_ALL,
            Camera => query::CAMERA_ALL,
            CameraPub => query::CAMERA_PUB,
            CommConfig => query::COMM_CONFIG_ALL,
            CommLink => query::COMM_LINK_ALL,
            CommProtocol => query::COMM_PROTOCOL_LUT,
            Condition => query::CONDITION_LUT,
            Controller => query::CONTROLLER_ALL,
            Detector => query::DETECTOR_ALL,
            DetectorPub => query::DETECTOR_PUB,
            Direction => query::DIRECTION_LUT,
            Dms => query::DMS_ALL,
            DmsPub => query::DMS_PUB,
            DmsStat => query::DMS_STATUS,
            FlowStream => query::FLOW_STREAM_ALL,
            FontList => query::FONT_ALL,
            GateArm => query::GATE_ARM_ALL,
            GateArmArray => query::GATE_ARM_ARRAY_ALL,
            GateArmInterlock => query::GATE_ARM_INTERLOCK_LUT,
            GateArmState => query::GATE_ARM_STATE_LUT,
            Gps => query::GPS_ALL,
            GraphicList => query::GRAPHIC_ALL,
            Incident => query::INCIDENT_PUB,
            LaneMarking => query::LANE_MARKING_ALL,
            LaneUseIndication => query::LANE_USE_INDICATION_LUT,
            LcsArray => query::LCS_ARRAY_ALL,
            LcsIndication => query::LCS_INDICATION_ALL,
            LcsLock => query::LCS_LOCK_LUT,
            Modem => query::MODEM_ALL,
            MsgLine => query::MSG_LINE_ALL,
            MsgPattern => query::MSG_PATTERN_ALL,
            ParkingAreaPub => query::PARKING_AREA_PUB,
            ParkingAreaDyn => query::PARKING_AREA_DYN,
            ParkingAreaArch => query::PARKING_AREA_ARCH,
            Permission => query::PERMISSION_ALL,
            Rnode => query::RNODE_FULL,
            RampMeter => query::RAMP_METER_ALL,
            ResourceType => query::RESOURCE_TYPE_LUT,
            Road => query::ROAD_ALL,
            RoadFull => query::ROAD_FULL,
            RoadModifier => query::ROAD_MODIFIER_LUT,
            Role => query::ROLE_ALL,
            SignConfig => query::SIGN_CONFIG_ALL,
            SignDetail => query::SIGN_DETAIL_ALL,
            SignMessage => query::SIGN_MSG_PUB,
            SystemAttributePub => query::SYSTEM_ATTRIBUTE_PUB,
            TagReader => query::TAG_READER_ALL,
            User => query::USER_ALL,
            VideoMonitor => query::VIDEO_MONITOR_ALL,
            WeatherSensor => query::WEATHER_SENSOR_ALL,
            WeatherSensorPub => query::WEATHER_SENSOR_PUB,
            Word => query::WORD_ALL,
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
        use Resource::*;
        match self {
            Rnode => self.query_nodes(client, payload, segments).await,
            Road => self.query_roads(client, payload, segments).await,
            SignMessage => self.query_sign_msgs(client).await,
            _ => self.query_file(client, self.path()).await,
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
        let sql = self.all_sql();
        let count = query_json(client, sql, writer).await?;
        file.commit().await?;
        log::info!("{name}: wrote {count} rows in {:?}", t.elapsed());
        Ok(())
    }

    /// Query sign messages resource.
    async fn query_sign_msgs(self, client: &mut Client) -> Result<()> {
        self.query_file(client, self.path()).await?;
        render_all().await
    }
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
