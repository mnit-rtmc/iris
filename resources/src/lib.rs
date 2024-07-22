// lib.rs
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
use std::fmt;

/// Enumeration of resource types
#[derive(Clone, Copy, Debug, Eq, Hash, PartialEq)]
pub enum Res {
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
    Domain,
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
    IncAdvice,
    IncDescriptor,
    IncLocator,
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
    TollZone,
    User,
    VideoMonitor,
    WeatherSensor,
    Word,
}

impl fmt::Display for Res {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.as_str())
    }
}

impl TryFrom<&str> for Res {
    type Error = ();

    fn try_from(type_n: &str) -> Result<Self, Self::Error> {
        Self::iter().find(|res| res.as_str() == type_n).ok_or(())
    }
}

impl Res {
    /// Get iterator of all resource variants
    pub fn iter() -> impl Iterator<Item = Res> {
        use Res::*;
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
            Domain,
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
            IncAdvice,
            IncDescriptor,
            IncLocator,
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
            TollZone,
            User,
            VideoMonitor,
            WeatherSensor,
            Word,
        ]
        .iter()
        .cloned()
    }

    /// Get resource name as string slice
    pub const fn as_str(self) -> &'static str {
        use Res::*;
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
            Domain => "domain",
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
            IncAdvice => "inc_advice",
            IncDescriptor => "inc_descriptor",
            IncLocator => "inc_locator",
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
            TollZone => "toll_zone",
            User => "user_id",
            VideoMonitor => "video_monitor",
            WeatherSensor => "weather_sensor",
            Word => "word",
        }
    }

    /// Get resource symbol as a string slice
    pub const fn symbol(self) -> &'static str {
        use Res::*;
        match self {
            Alarm => "📢",
            Beacon => "🔆",
            CabinetStyle => "🗄️ ",
            Camera => "🎥",
            CommConfig => "📡",
            CommLink => "🔗",
            Controller => "🎛️ ",
            Detector => "🚗⬚",
            Dms => "⬛",
            Domain => "🖧 ",
            FlowStream => "🎞️ ",
            GateArm => "⫬",
            GateArmArray => "⫭⫬",
            GeoLoc => "🗺️ ",
            Gps => "🌐",
            Incident => "🚨",
            LaneMarking => "⛙",
            LcsArray => "🡇🡇 ",
            LcsIndication => "🡇 ",
            Modem => "🖀 ",
            Permission => "🗝️ ",
            RampMeter => "🚦",
            Role => "💪",
            TagReader => "🏷️ ",
            TollZone => "💲",
            User => "👤",
            VideoMonitor => "📺",
            WeatherSensor => "🌦️ ",
            _ => "❓",
        }
    }

    /// Check if resource is a look-up table
    pub const fn is_lut(self) -> bool {
        use Res::*;
        #[allow(clippy::match_like_matches_macro)]
        match self {
            BeaconState | CommProtocol | Condition | Direction | Font
            | GateArmInterlock | GateArmState | Graphic | LaneUseIndication
            | LcsLock | ResourceType | RoadModifier => true,
            _ => false,
        }
    }

    /// Check if resource has a notification channel
    pub const fn has_channel(self) -> bool {
        use Res::*;
        #[allow(clippy::match_like_matches_macro)]
        match self {
            Alarm | Beacon | CabinetStyle | Camera | CommConfig | CommLink
            | Controller | Detector | Dms | Domain | FlowStream | GateArm
            | GateArmArray | Gps | Incident | LaneMarking | LcsArray
            | LcsIndication | Modem | MsgLine | MsgPattern | ParkingArea
            | Permission | RampMeter | Rnode | Road | Role | SignConfig
            | SignDetail | SignMessage | SystemAttribute | TagReader | User
            | VideoMonitor | WeatherSensor | Word => true,
            _ => false,
        }
    }

    /// Get base resource for permission checks
    pub const fn base(self) -> Self {
        use Res::*;
        match self {
            // Camera resources
            FlowStream | VideoMonitor => Camera,
            // Controller resources
            Alarm | CommLink | ControllerIo | Gps | Modem => Controller,
            // Detector resources
            Rnode | Road => Detector,
            // DMS resources
            Font | Graphic | MsgLine | MsgPattern | SignConfig | SignDetail
            | SignMessage | Word => Dms,
            // Gate arm resources
            GateArmArray => GateArm,
            // Incident resources
            IncAdvice | IncDescriptor | IncLocator => Incident,
            // LCS resources
            LcsArray | LcsIndication | LaneMarking => Lcs,
            // Permission resources
            Domain | User | Role => Permission,
            // System attribute resources
            CabinetStyle | CommConfig => SystemAttribute,
            // Toll zone resources
            TagReader => TollZone,
            // Others
            _ => self,
        }
    }
}
