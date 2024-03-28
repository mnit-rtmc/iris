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
use crate::restype::ResType;
use crate::segments::{RNode, Road, SegmentState};
use crate::signmsg::render_all;
use crate::sonar::Name;
use futures::{pin_mut, TryStreamExt};
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
    Font,
    GateArm,
    GateArmArray,
    GateArmInterlock,
    GateArmState,
    Gps,
    Graphic,
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
    RampMeter,
    ResourceType,
    Rnode,
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

impl Resource {
    /// Get iterator of all resource variants
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
            Font,
            GateArm,
            GateArmArray,
            GateArmInterlock,
            GateArmState,
            Gps,
            Graphic,
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
            RampMeter,
            ResourceType,
            Rnode,
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

    /// Get the resource type
    const fn res_type(self) -> ResType {
        use Resource::*;
        match self {
            Alarm => ResType::Alarm,
            Beacon => ResType::Beacon,
            BeaconState => ResType::BeaconState,
            CabinetStyle => ResType::CabinetStyle,
            Camera | CameraPub => ResType::Camera,
            CommConfig => ResType::CommConfig,
            CommLink => ResType::CommLink,
            CommProtocol => ResType::CommProtocol,
            Condition => ResType::Condition,
            Controller => ResType::Controller,
            Detector | DetectorPub => ResType::Detector,
            Direction => ResType::Direction,
            Dms | DmsPub | DmsStat => ResType::Dms,
            FlowStream => ResType::FlowStream,
            Font => ResType::Font,
            GateArm => ResType::GateArm,
            GateArmArray => ResType::GateArmArray,
            GateArmInterlock => ResType::GateArmInterlock,
            GateArmState => ResType::GateArmState,
            Gps => ResType::Gps,
            Graphic => ResType::Graphic,
            Incident => ResType::Incident,
            LaneMarking => ResType::LaneMarking,
            LaneUseIndication => ResType::LaneUseIndication,
            LcsArray => ResType::LcsArray,
            LcsIndication => ResType::LcsIndication,
            LcsLock => ResType::LcsLock,
            Modem => ResType::Modem,
            MsgLine => ResType::MsgLine,
            MsgPattern => ResType::MsgPattern,
            ParkingAreaPub | ParkingAreaDyn | ParkingAreaArch => {
                ResType::ParkingArea
            }
            Permission => ResType::Permission,
            RampMeter => ResType::RampMeter,
            ResourceType => ResType::ResourceType,
            Rnode => ResType::Rnode,
            Road | RoadFull => ResType::Road,
            RoadModifier => ResType::RoadModifier,
            Role => ResType::Role,
            SignConfig => ResType::SignConfig,
            SignDetail => ResType::SignDetail,
            SignMessage => ResType::SignMessage,
            SystemAttributePub => ResType::SystemAttribute,
            TagReader => ResType::TagReader,
            User => ResType::User,
            VideoMonitor => ResType::VideoMonitor,
            WeatherSensor | WeatherSensorPub => ResType::WeatherSensor,
            Word => ResType::Word,
        }
    }

    /// Get the resource path
    const fn path(self) -> &'static str {
        use Resource::*;
        match self {
            Alarm => "api/alarm",
            Beacon => "api/beacon",
            BeaconState => "lut/beacon_state",
            CabinetStyle => "api/cabinet_style",
            Camera => "api/camera",
            CameraPub => "camera_pub",
            CommConfig => "api/comm_config",
            CommLink => "api/comm_link",
            CommProtocol => "lut/comm_protocol",
            Condition => "lut/condition",
            Controller => "api/controller",
            Detector => "api/detector",
            DetectorPub => "detector_pub",
            Direction => "lut/direction",
            Dms => "api/dms",
            DmsPub => "dms_pub",
            DmsStat => "dms_message",
            FlowStream => "api/flow_stream",
            Font => "api/font",
            GateArm => "api/gate_arm",
            GateArmArray => "api/gate_arm_array",
            GateArmInterlock => "lut/gate_arm_interlock",
            GateArmState => "lut/gate_arm_state",
            Gps => "api/gps",
            Graphic => "api/graphic",
            Incident => "incident",
            LaneMarking => "api/lane_marking",
            LaneUseIndication => "lut/lane_use_indication",
            LcsArray => "api/lcs_array",
            LcsIndication => "api/lcs_indication",
            LcsLock => "lut/lcs_lock",
            Modem => "api/modem",
            MsgLine => "api/msg_line",
            MsgPattern => "api/msg_pattern",
            ParkingAreaPub => "TPIMS_static",
            ParkingAreaDyn => "TPIMS_dynamic",
            ParkingAreaArch => "TPIMS_archive",
            Permission => "api/permission",
            RampMeter => "api/ramp_meter",
            ResourceType => "lut/resource_type",
            Rnode => unreachable!(),
            Road => "api/road",
            RoadFull => unreachable!(),
            RoadModifier => "lut/road_modifier",
            Role => "api/role",
            SignConfig => "api/sign_config",
            SignDetail => "api/sign_detail",
            SignMessage => "sign_message",
            SystemAttributePub => "system_attribute_pub",
            TagReader => "api/tag_reader",
            User => "api/user_id",
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
            BeaconState | CommProtocol | Condition | Direction | Font
            | GateArmInterlock | GateArmState | Graphic | LaneUseIndication
            | LcsLock | ResourceType | RoadModifier => {
                self.res_type().lut_channel()
            }
            _ => self.res_type().listen(),
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
            Font => query::FONT_ALL,
            GateArm => query::GATE_ARM_ALL,
            GateArmArray => query::GATE_ARM_ARRAY_ALL,
            GateArmInterlock => query::GATE_ARM_INTERLOCK_LUT,
            GateArmState => query::GATE_ARM_STATE_LUT,
            Gps => query::GPS_ALL,
            Graphic => query::GRAPHIC_ALL,
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
            RampMeter => query::RAMP_METER_ALL,
            ResourceType => query::RESOURCE_TYPE_LUT,
            Rnode => query::RNODE_FULL,
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

    /// Check if all_sql produces JSON
    const fn all_sql_json(self) -> bool {
        use Resource::*;
        match self {
            ResourceType | SystemAttributePub => false,
            _ => true,
        }
    }

    /// Handle a notification event.
    ///
    /// * `client` Database connection.
    /// * `segments` Segment state.
    /// * `nm` Notify event name.
    pub async fn notify(
        client: &mut Client,
        segments: &mut SegmentState,
        nm: &Name,
    ) -> Result<()> {
        log::info!("Resource::notify: {nm}");
        for res in Resource::iter() {
            if nm.res_type == res.res_type() {
                match nm.object_n() {
                    Some(obj_n) => {
                        res.query_one(client, segments, obj_n).await?
                    }
                    None => res.query_all(client, segments).await?,
                }
            }
        }
        Ok(())
    }

    /// Query one record of the resource.
    ///
    /// * `client` The database connection.
    /// * `segments` Segment state.
    /// * `name` Resource name.
    async fn query_one(
        self,
        client: &mut Client,
        segments: &mut SegmentState,
        obj_n: &str,
    ) -> Result<()> {
        log::trace!("query_one: {self:?} {obj_n}");
        use Resource::*;
        match self {
            Rnode => query_one_node(client, segments, obj_n).await,
            RoadFull => query_one_road(client, segments, obj_n).await,
            DmsStat | ParkingAreaDyn | ParkingAreaArch | WeatherSensorPub => {
                self.query_file(client, self.path()).await
            }
            _ => Ok(()),
        }
    }

    /// Query all records of the resource.
    ///
    /// * `client` The database connection.
    /// * `segments` Segment state.
    async fn query_all(
        self,
        client: &mut Client,
        segments: &mut SegmentState,
    ) -> Result<()> {
        use Resource::*;
        match self {
            Rnode => query_all_nodes(client, segments).await,
            RoadFull => query_all_roads(client, segments).await,
            SignMessage => self.query_sign_msgs(client).await,
            _ => self.query_file(client, self.path()).await,
        }
    }

    /// Query a file resource.
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
        let sql = if self.all_sql_json() {
            format!(
                "SELECT json_strip_nulls(row_to_json(r))::text FROM ({sql}) r"
            )
        } else {
            sql.to_string()
        };
        let count = query_json(client, &sql, writer).await?;
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

/// Query a JSON resource.
///
/// * `client` Database connection.
/// * `sql` SQL query.
/// * `w` Writer to output resource.
async fn query_json<W>(client: &mut Client, sql: &str, mut w: W) -> Result<u32>
where
    W: AsyncWrite + Unpin,
{
    let mut c = 0;
    w.write_all(b"[").await?;
    let params: &[&str] = &[];
    let it = client.query_raw(sql, params).await?;
    pin_mut!(it);
    while let Some(row) = it.try_next().await? {
        match c {
            0 => w.write_all(b"\n").await?,
            _ => w.write_all(b",\n").await?,
        }
        let j: String = row.get(0);
        w.write_all(j.as_bytes()).await?;
        c += 1;
    }
    match c {
        0 => w.write_all(b"]\n").await?,
        _ => w.write_all(b"\n]\n").await?,
    }
    w.flush().await?;
    Ok(c)
}

/// Query all r_nodes.
///
/// * `client` Database connection.
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
/// * `client` Database connection.
/// * `segments` Segment state.
/// * `name` RNode name.
async fn query_one_node(
    client: &mut Client,
    segments: &mut SegmentState,
    name: &str,
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
/// * `client` Database connection.
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
/// * `client` Database connection.
/// * `segments` Segment state.
/// * `name` Road name.
async fn query_one_road(
    client: &mut Client,
    segments: &mut SegmentState,
    name: &str,
) -> Result<()> {
    log::trace!("query_one_road: {name}");
    let rows = &client.query(query::ROAD_ONE, &[&name]).await?;
    if let Some(row) = rows.iter().next() {
        segments.update_road(Road::from_row(row)).await?;
    }
    Ok(())
}
