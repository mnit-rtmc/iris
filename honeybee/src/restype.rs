// restype.rs
//
// Copyright (C) 2024  Minnesota Department of Transportation
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
use crate::access::Access;
use crate::query;

/// Resource types
#[derive(Clone, Copy, Debug, Eq, Hash, PartialEq)]
pub enum ResType {
    Alarm,
    Beacon,
    BeaconState,
    CabinetStyle,
    Camera,
    CommConfig,
    CommLink,
    CommProtocol,
    Condition,
    Controller,
    ControllerIo,
    Detector,
    Direction,
    Dms,
    FlowStream,
    Font,
    GateArm,
    GateArmArray,
    GateArmInterlock,
    GateArmState,
    GeoLoc,
    Gps,
    Graphic,
    Incident,
    LaneMarking,
    LaneUseIndication,
    Lcs,
    LcsArray,
    LcsIndication,
    LcsLock,
    Modem,
    MsgLine,
    MsgPattern,
    ParkingArea,
    Permission,
    RampMeter,
    ResourceType,
    Rnode,
    Road,
    RoadModifier,
    Role,
    SignConfig,
    SignDetail,
    SignMessage,
    SystemAttribute,
    TagReader,
    User,
    VideoMonitor,
    WeatherSensor,
    Word,
}

impl TryFrom<&str> for ResType {
    type Error = ();

    fn try_from(type_n: &str) -> Result<Self, Self::Error> {
        Self::iter().find(|rt| rt.as_str() == type_n).ok_or(())
    }
}

impl ResType {
    /// Get iterator of all resource type variants
    pub fn iter() -> impl Iterator<Item = ResType> {
        use ResType::*;
        [
            Alarm,
            Beacon,
            BeaconState,
            CabinetStyle,
            Camera,
            CommConfig,
            CommLink,
            CommProtocol,
            Condition,
            Controller,
            ControllerIo,
            Detector,
            Direction,
            Dms,
            FlowStream,
            Font,
            GateArm,
            GateArmArray,
            GateArmInterlock,
            GateArmState,
            GeoLoc,
            Gps,
            Graphic,
            Incident,
            LaneMarking,
            LaneUseIndication,
            Lcs,
            LcsArray,
            LcsIndication,
            LcsLock,
            Modem,
            MsgLine,
            MsgPattern,
            ParkingArea,
            Permission,
            RampMeter,
            ResourceType,
            Rnode,
            Road,
            RoadModifier,
            Role,
            SignConfig,
            SignDetail,
            SignMessage,
            SystemAttribute,
            TagReader,
            User,
            VideoMonitor,
            WeatherSensor,
            Word,
        ]
        .iter()
        .cloned()
    }

    /// Get name as string slice
    pub const fn as_str(self) -> &'static str {
        use ResType::*;
        match self {
            Alarm => "alarm",
            Beacon => "beacon",
            BeaconState => "beacon_state",
            CabinetStyle => "cabinet_style",
            Camera => "camera",
            CommConfig => "comm_config",
            CommLink => "comm_link",
            CommProtocol => "comm_protocol",
            Condition => "condition",
            Controller => "controller",
            ControllerIo => "controller_io",
            Detector => "detector",
            Direction => "direction",
            Dms => "dms",
            FlowStream => "flow_stream",
            Font => "font",
            GateArm => "gate_arm",
            GateArmInterlock => "gate_arm_interlock",
            GateArmState => "gate_arm_state",
            GateArmArray => "gate_arm_array",
            GeoLoc => "geo_loc",
            Gps => "gps",
            Graphic => "graphic",
            Incident => "incident",
            LaneUseIndication => "lane_use_indication",
            LaneMarking => "lane_marking",
            Lcs => "lcs",
            LcsArray => "lcs_array",
            LcsIndication => "lcs_indication",
            LcsLock => "lcs_lock",
            Modem => "modem",
            MsgLine => "msg_line",
            MsgPattern => "msg_pattern",
            ParkingArea => "parking_area",
            Permission => "permission",
            RampMeter => "ramp_meter",
            ResourceType => "resource_type",
            Rnode => "r_node",
            Road => "road",
            RoadModifier => "road_modifier",
            Role => "role",
            SignConfig => "sign_config",
            SignDetail => "sign_detail",
            SignMessage => "sign_message",
            SystemAttribute => "system_attribute",
            TagReader => "tag_reader",
            User => "user_id",
            VideoMonitor => "video_monitor",
            WeatherSensor => "weather_sensor",
            Word => "word",
        }
    }

    /// Get "channel" for LUT resources (no postgres NOTIFY)
    pub const fn lut_channel(self) -> Option<&'static str> {
        use ResType::*;
        match self {
            BeaconState | CommProtocol | Condition | Direction | Font
            | GateArmInterlock | GateArmState | Graphic | LaneUseIndication
            | LcsLock | ResourceType | RoadModifier => Some(self.as_str()),
            _ => None,
        }
    }

    /// Get the channel to listen for attributes
    pub const fn listen(self) -> Option<&'static str> {
        use ResType::*;
        match self {
            Alarm | Beacon | CabinetStyle | Camera | CommConfig | CommLink
            | Controller | Detector | Dms | FlowStream | GateArm
            | GateArmArray | Gps | Incident | LaneMarking | LcsArray
            | LcsIndication | Modem | MsgLine | MsgPattern | ParkingArea
            | Permission | RampMeter | Rnode | Road | Role | SignConfig
            | SignDetail | SignMessage | SystemAttribute | TagReader | User
            | VideoMonitor | WeatherSensor | Word => Some(self.as_str()),
            _ => None,
        }
    }

    /// Get the SQL query one record
    pub const fn one_sql(self) -> &'static str {
        use ResType::*;
        match self {
            Alarm => query::ALARM_ONE,
            Beacon => query::BEACON_ONE,
            CabinetStyle => query::CABINET_STYLE_ONE,
            Camera => query::CAMERA_ONE,
            CommConfig => query::COMM_CONFIG_ONE,
            CommLink => query::COMM_LINK_ONE,
            Controller => query::CONTROLLER_ONE,
            ControllerIo => query::CONTROLLER_IO_ONE,
            Detector => query::DETECTOR_ONE,
            Dms => query::DMS_ONE,
            FlowStream => query::FLOW_STREAM_ONE,
            Font => query::FONT_ONE,
            GateArm => query::GATE_ARM_ONE,
            GateArmArray => query::GATE_ARM_ARRAY_ONE,
            GeoLoc => query::GEO_LOC_ONE,
            Gps => query::GPS_ONE,
            Graphic => query::GRAPHIC_ONE,
            LaneMarking => query::LANE_MARKING_ONE,
            LcsArray => query::LCS_ARRAY_ONE,
            LcsIndication => query::LCS_INDICATION_ONE,
            Modem => query::MODEM_ONE,
            MsgLine => query::MSG_LINE_ONE,
            MsgPattern => query::MSG_PATTERN_ONE,
            Permission => query::PERMISSION_ONE,
            RampMeter => query::RAMP_METER_ONE,
            Role => query::ROLE_ONE,
            SignConfig => query::SIGN_CONFIG_ONE,
            SignDetail => query::SIGN_DETAIL_ONE,
            SignMessage => query::SIGN_MSG_ONE,
            TagReader => query::TAG_READER_ONE,
            User => query::USER_ONE,
            VideoMonitor => query::VIDEO_MONITOR_ONE,
            WeatherSensor => query::WEATHER_SENSOR_ONE,
            Word => query::WORD_ONE,
            _ => unimplemented!(),
        }
    }

    /// Get dependent resource type
    pub const fn dependent(self) -> Self {
        use ResType::*;
        match self {
            // Camera resources
            FlowStream => Camera,
            // DMS resources
            Font | Graphic | MsgLine | MsgPattern | SignConfig | SignDetail
            | SignMessage | Word => Dms,
            // Gate arm resources
            GateArmArray => GateArm,
            // LCS resources
            LcsArray | LcsIndication | LaneMarking => Lcs,
            // associated controller
            ControllerIo => Controller,
            _ => self,
        }
    }

    /// Get required access to update an attribute
    pub fn access_attr(self, att: &str) -> Access {
        use ResType::*;
        match (self, att) {
            (Beacon, "flashing")
            | (Camera, "ptz")
            | (Camera, "recall_preset")
            | (Controller, "device_req")
            | (Detector, "field_length")
            | (Detector, "force_fail")
            | (Dms, "msg_user")
            | (LaneMarking, "deployed") => Access::Operate,
            (Beacon, "message")
            | (Beacon, "notes")
            | (Beacon, "preset")
            | (Camera, "store_preset")
            | (CommConfig, "timeout_ms")
            | (CommConfig, "idle_disconnect_sec")
            | (CommConfig, "no_response_disconnect_sec")
            | (CommLink, "poll_enabled")
            | (Controller, "condition")
            | (Controller, "notes")
            | (Detector, "abandoned")
            | (Detector, "notes")
            | (Dms, "device_req")
            | (LaneMarking, "notes")
            | (Modem, "enabled")
            | (Modem, "timeout_ms")
            | (Role, "enabled")
            | (User, "enabled")
            | (WeatherSensor, "site_id")
            | (WeatherSensor, "alt_id")
            | (WeatherSensor, "notes") => Access::Manage,
            _ => Access::Configure,
        }
    }

    /// Check if resource type / attribute should be patched first
    pub fn patch_first_pass(self, att: &str) -> bool {
        use ResType::*;
        match (self, att) {
            (Alarm, "pin")
            | (Beacon, "pin")
            | (Beacon, "verify_pin")
            | (Detector, "pin")
            | (Dms, "pin")
            | (LaneMarking, "pin")
            | (RampMeter, "pin")
            | (WeatherSensor, "pin") => true,
            _ => false,
        }
    }
}
