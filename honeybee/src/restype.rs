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

/// Resource types
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
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
        for variant in Self::iter() {
            if variant.as_str() == type_n {
                return Ok(variant);
            }
        }
        Err(())
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

    /// Get resource type as string slice
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
            GateArmArray => "gate_arm_array",
            GateArmInterlock => "gate_arm_interlock",
            GateArmState => "gate_arm_state",
            GeoLoc => "geo_loc",
            Gps => "gps",
            Graphic => "graphic",
            Incident => "incident",
            LaneMarking => "lane_marking",
            LaneUseIndication => "lane_use_indication",
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
            User => "user",
            VideoMonitor => "video_monitor",
            WeatherSensor => "weather_sensor",
            Word => "word",
        }
    }

    /// Check if resource type is a lookup table
    pub const fn is_lut(self) -> bool {
        use ResType::*;
        matches!(
            self,
            BeaconState
                | CommProtocol
                | Condition
                | Direction
                | GateArmInterlock
                | GateArmState
                | LaneUseIndication
                | LcsLock
                | ResourceType
                | Rnode
                | RoadModifier
        )
    }
}
