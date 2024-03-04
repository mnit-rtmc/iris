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
            if let Some(chan) = variant.listen_min() {
                if chan == type_n {
                    return Ok(variant);
                }
            }
            if let Some(chan) = variant.listen_full() {
                if chan == type_n {
                    return Ok(variant);
                }
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

    /// Get the channel to listen for minimal attributes
    pub const fn listen_min(self) -> Option<&'static str> {
        use ResType::*;
        match self {
            Alarm => Some("alarm"),
            Beacon => Some("beacon"),
            CabinetStyle => Some("cabinet_style"),
            Camera => Some("camera"),
            CommConfig => Some("comm_config"),
            CommLink => Some("comm_link"),
            Controller => Some("controller"),
            Detector => Some("detector"),
            Dms => Some("dms"),
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
            ParkingArea => Some("parking_area"),
            Permission => Some("permission"),
            RampMeter => Some("ramp_meter"),
            Role => Some("role"),
            SignConfig => Some("sign_config"),
            SignDetail => Some("sign_detail"),
            SignMessage => Some("sign_message"),
            SystemAttribute => Some("system_attribute"),
            TagReader => Some("tag_reader"),
            User => Some("i_user"),
            VideoMonitor => Some("video_monitor"),
            WeatherSensor => Some("weather_sensor"),
            Word => Some("word"),
            _ => None,
        }
    }

    /// Get the channel to listen for full attributes
    pub const fn listen_full(self) -> Option<&'static str> {
        use ResType::*;
        match self {
            Beacon => Some("beacon$1"),
            Camera => Some("camera$1"),
            CommLink => Some("comm_link$1"),
            Controller => Some("controller$1"),
            Detector => Some("detector$1"),
            Dms => Some("dms$1"),
            GateArmArray => Some("gate_arm_array$1"),
            ParkingArea => Some("parking_area$1"),
            RampMeter => Some("ramp_meter$1"),
            Rnode => Some("r_node$1"),
            Road => Some("road$1"),
            TagReader => Some("tag_reader$1"),
            VideoMonitor => Some("video_monitor$1"),
            WeatherSensor => Some("weather_sensor$1"),
            _ => None,
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
