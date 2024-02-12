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
use crate::sonar::Error;

/// Resource types
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum ResType {
    Alarm,
    Beacon,
    CabinetStyle,
    Camera,
    CommConfig,
    CommLink,
    Controller,
    ControllerIo,
    Detector,
    Dms,
    FlowStream,
    Font,
    GateArm,
    GateArmArray,
    GeoLoc,
    Gps,
    Graphic,
    LaneMarking,
    Lcs,
    LcsArray,
    LcsIndication,
    Modem,
    MsgLine,
    MsgPattern,
    Permission,
    RampMeter,
    Role,
    SignConfig,
    SignDetail,
    SignMessage,
    TagReader,
    User,
    VideoMonitor,
    WeatherSensor,
    Word,
}

impl TryFrom<&str> for ResType {
    type Error = Error;

    fn try_from(type_n: &str) -> std::result::Result<Self, Error> {
        for variant in Self::iter() {
            if variant.as_str() == type_n {
                return Ok(variant);
            }
        }
        Err(Error::InvalidValue)?
    }
}

impl ResType {
    /// Get iterator of all resource type variants
    pub fn iter() -> impl Iterator<Item = ResType> {
        use ResType::*;
        [
            Alarm,
            Beacon,
            CabinetStyle,
            Camera,
            CommConfig,
            CommLink,
            Controller,
            ControllerIo,
            Detector,
            Dms,
            FlowStream,
            Font,
            GateArm,
            GateArmArray,
            GeoLoc,
            Gps,
            Graphic,
            LaneMarking,
            Lcs,
            LcsArray,
            LcsIndication,
            Modem,
            MsgLine,
            MsgPattern,
            Permission,
            RampMeter,
            Role,
            SignConfig,
            SignDetail,
            SignMessage,
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
            CabinetStyle => "cabinet_style",
            Camera => "camera",
            CommConfig => "comm_config",
            CommLink => "comm_link",
            Controller => "controller",
            ControllerIo => "controller_io",
            Detector => "detector",
            Dms => "dms",
            FlowStream => "flow_stream",
            Font => "font",
            GateArm => "gate_arm",
            GateArmArray => "gate_arm_array",
            GeoLoc => "geo_loc",
            Gps => "gps",
            Graphic => "graphic",
            LaneMarking => "lane_marking",
            Lcs => "lcs",
            LcsArray => "lcs_array",
            LcsIndication => "lcs_indication",
            Modem => "modem",
            MsgLine => "msg_line",
            MsgPattern => "msg_pattern",
            Permission => "permission",
            RampMeter => "ramp_meter",
            Role => "role",
            SignConfig => "sign_config",
            SignDetail => "sign_detail",
            SignMessage => "sign_message",
            TagReader => "tag_reader",
            User => "user",
            VideoMonitor => "video_monitor",
            WeatherSensor => "weather_sensor",
            Word => "word",
        }
    }

    /// Get verifier resource type
    pub const fn verifier(self) -> Self {
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

    /// Get SQL query string
    pub const fn sql_query(self) -> &'static str {
        use ResType::*;
        match self {
            Alarm => query::ALARM,
            Beacon => query::BEACON,
            CabinetStyle => query::CABINET_STYLE,
            Camera => query::CAMERA,
            CommConfig => query::COMM_CONFIG,
            CommLink => query::COMM_LINK,
            Controller => query::CONTROLLER,
            ControllerIo => query::CONTROLLER_IO,
            Detector => query::DETECTOR,
            Dms => query::DMS,
            FlowStream => query::FLOW_STREAM,
            Font => query::FONT,
            GateArm => query::GATE_ARM,
            GateArmArray => query::GATE_ARM_ARRAY,
            GeoLoc => query::GEO_LOC,
            Gps => query::GPS,
            Graphic => query::GRAPHIC,
            LaneMarking => query::LANE_MARKING,
            Lcs => "", // FIXME
            LcsArray => query::LCS_ARRAY,
            LcsIndication => query::LCS_INDICATION,
            Modem => query::MODEM,
            MsgLine => query::MSG_LINE,
            MsgPattern => query::MSG_PATTERN,
            Permission => query::PERMISSION,
            RampMeter => query::RAMP_METER,
            Role => query::ROLE,
            SignConfig => query::SIGN_CONFIG,
            SignDetail => query::SIGN_DETAIL,
            SignMessage => query::SIGN_MSG,
            TagReader => query::TAG_READER,
            User => query::USER,
            VideoMonitor => query::VIDEO_MONITOR,
            WeatherSensor => query::WEATHER_SENSOR,
            Word => query::WORD,
        }
    }

    /// Get required access to update an attribute
    pub fn access_attr(self, att: &str) -> Access {
        use ResType::*;
        match (self, att) {
            (Beacon, "flashing")
            | (Camera, "ptz")
            | (Camera, "recall_preset")
            | (Controller, "download")
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
        matches!(
            (self, att),
            (Alarm, "pin")
                | (Beacon, "pin")
                | (Beacon, "verify_pin")
                | (Detector, "pin")
                | (Dms, "pin")
                | (LaneMarking, "pin")
                | (RampMeter, "pin")
                | (WeatherSensor, "pin")
        )
    }
}
