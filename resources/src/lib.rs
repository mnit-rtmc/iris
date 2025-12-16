// lib.rs
//
// Copyright (C) 2024-2025  Minnesota Department of Transportation
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
    ActionPlan,
    Alarm,
    Beacon,
    BeaconState,
    CabinetStyle,
    Camera,
    CameraPreset,
    CommConfig,
    CommLink,
    CommProtocol,
    CommState,
    Condition,
    Controller,
    ControllerIo,
    DayMatcher,
    DayPlan,
    Detector,
    DeviceAction,
    Direction,
    Dms,
    Domain,
    EncoderStream,
    EncoderType,
    Encoding,
    EventConfig,
    EventDescription,
    FlowStream,
    Font,
    GateArm,
    GateArmInterlock,
    GateArmState,
    GeoLoc,
    Gps,
    Graphic,
    Hashtag,
    Incident,
    IncAdvice,
    IncDescriptor,
    IncDetail,
    IncImpact,
    IncLocator,
    IncRange,
    LaneCode,
    Lcs,
    LcsIndication,
    LcsState,
    LcsType,
    MeterAlgorithm,
    MeterType,
    Modem,
    MonitorStyle,
    MsgLine,
    MsgPattern,
    ParkingArea,
    Permission,
    PlanPhase,
    PlayList,
    RampMeter,
    ResourceType,
    Rnode,
    RnodeTransition,
    RnodeType,
    Road,
    RoadAffix,
    RoadClass,
    RoadModifier,
    Role,
    SignConfig,
    SignDetail,
    SignMessage,
    SystemAttribute,
    TagReader,
    TimeAction,
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
            ActionPlan,
            Alarm,
            Beacon,
            BeaconState,
            CabinetStyle,
            Camera,
            CameraPreset,
            CommConfig,
            CommLink,
            CommProtocol,
            CommState,
            Condition,
            Controller,
            ControllerIo,
            DayMatcher,
            DayPlan,
            Detector,
            DeviceAction,
            Direction,
            Dms,
            Domain,
            EncoderStream,
            EncoderType,
            Encoding,
            EventConfig,
            EventDescription,
            FlowStream,
            Font,
            GateArm,
            GateArmInterlock,
            GateArmState,
            GeoLoc,
            Gps,
            Graphic,
            Hashtag,
            Incident,
            IncAdvice,
            IncDescriptor,
            IncDetail,
            IncImpact,
            IncLocator,
            IncRange,
            LaneCode,
            Lcs,
            LcsIndication,
            LcsState,
            LcsType,
            MeterAlgorithm,
            MeterType,
            Modem,
            MonitorStyle,
            MsgLine,
            MsgPattern,
            ParkingArea,
            Permission,
            PlanPhase,
            PlayList,
            RampMeter,
            ResourceType,
            Rnode,
            RnodeTransition,
            RnodeType,
            Road,
            RoadAffix,
            RoadClass,
            RoadModifier,
            Role,
            SignConfig,
            SignDetail,
            SignMessage,
            SystemAttribute,
            TagReader,
            TimeAction,
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
            ActionPlan => "action_plan",
            Alarm => "alarm",
            Beacon => "beacon",
            BeaconState => "beacon_state",
            CabinetStyle => "cabinet_style",
            Camera => "camera",
            CameraPreset => "camera_preset",
            CommConfig => "comm_config",
            CommLink => "comm_link",
            CommProtocol => "comm_protocol",
            CommState => "comm_state",
            Condition => "condition",
            Controller => "controller",
            ControllerIo => "controller_io",
            DayMatcher => "day_matcher",
            DayPlan => "day_plan",
            Detector => "detector",
            DeviceAction => "device_action",
            Direction => "direction",
            Dms => "dms",
            Domain => "domain",
            EncoderStream => "encoder_stream",
            EncoderType => "encoder_type",
            EventConfig => "event_config",
            EventDescription => "event_description",
            Encoding => "encoding",
            FlowStream => "flow_stream",
            Font => "font",
            GateArm => "gate_arm",
            GateArmInterlock => "gate_arm_interlock",
            GateArmState => "gate_arm_state",
            GeoLoc => "geo_loc",
            Gps => "gps",
            Graphic => "graphic",
            Hashtag => "hashtag",
            Incident => "incident",
            IncAdvice => "inc_advice",
            IncDescriptor => "inc_descriptor",
            IncDetail => "inc_detail",
            IncImpact => "inc_impact",
            IncLocator => "inc_locator",
            IncRange => "inc_range",
            LaneCode => "lane_code",
            Lcs => "lcs",
            LcsIndication => "lcs_indication",
            LcsState => "lcs_state",
            LcsType => "lcs_type",
            MeterAlgorithm => "meter_algorithm",
            MeterType => "meter_type",
            Modem => "modem",
            MonitorStyle => "monitor_style",
            MsgLine => "msg_line",
            MsgPattern => "msg_pattern",
            ParkingArea => "parking_area",
            Permission => "permission",
            PlanPhase => "plan_phase",
            PlayList => "play_list",
            RampMeter => "ramp_meter",
            ResourceType => "resource_type",
            Rnode => "r_node",
            RnodeTransition => "r_node_transition",
            RnodeType => "r_node_type",
            Road => "road",
            RoadAffix => "road_affix",
            RoadClass => "road_class",
            RoadModifier => "road_modifier",
            Role => "role",
            SignConfig => "sign_config",
            SignDetail => "sign_detail",
            SignMessage => "sign_message",
            SystemAttribute => "system_attribute",
            TagReader => "tag_reader",
            TimeAction => "time_action",
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
            ActionPlan => "📋",
            Alarm => "📢",
            Beacon => "🔆",
            CabinetStyle => "🗄️ ",
            Camera => "🎥",
            CameraPreset => "🎥📌",
            CommConfig => "📡",
            CommLink => "🔗",
            Controller => "🎛️ ",
            DayMatcher => "🗓️",
            DayPlan => "🗓️",
            Detector => "🚗⬚",
            DeviceAction => "➡️",
            Dms => "⬛",
            Domain => "🖧 ",
            EncoderStream => "〜",
            EncoderType => "🎥📽️",
            EventConfig => "📜",
            FlowStream => "🎞️ ",
            GateArm => "⫬",
            GeoLoc => "🗺️ ",
            Gps => "🌐",
            Incident => "🚨",
            IncAdvice => "🚨❗",
            IncDescriptor => "🚨❓",
            IncDetail => "🚨➕",
            IncLocator => "🚨🗺️",
            Lcs => "🠟✖🠟",
            LcsState => "🠟",
            Modem => "🖀 ",
            MonitorStyle => "FIXME",
            Permission => "🗝️ ",
            PlanPhase => "🪜",
            PlayList => "FIXME",
            RampMeter => "🚦",
            RoadAffix => "🛣️ ",
            Role => "💪",
            SignConfig => "📐",
            TagReader => "🏷️ ",
            TimeAction => "⏰",
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
            BeaconState | CommProtocol | CommState | Condition | Direction
            | Encoding | EventDescription | Font | GateArmInterlock
            | GateArmState | Graphic | IncImpact | IncRange | LaneCode
            | LcsIndication | LcsType | MeterAlgorithm | MeterType
            | ResourceType | RnodeTransition | RnodeType | RoadClass
            | RoadModifier => true,
            _ => false,
        }
    }

    /// Check if resource has a notification channel
    pub const fn has_channel(self) -> bool {
        use Res::*;
        #[allow(clippy::match_like_matches_macro)]
        match self {
            ActionPlan | Alarm | Beacon | CabinetStyle | Camera
            | CameraPreset | CommConfig | CommLink | Controller
            | DayMatcher | DayPlan | Detector | DeviceAction | Dms | Domain
            | EncoderStream | EncoderType | EventConfig | FlowStream
            | GateArm | Gps | Hashtag | Incident | IncAdvice
            | IncDescriptor | IncDetail | IncLocator | Lcs | LcsState
            | Modem | MonitorStyle | MsgLine | MsgPattern | ParkingArea
            | Permission | PlanPhase | PlayList | RampMeter | Rnode | Road
            | RoadAffix | Role | SignConfig | SignDetail | SignMessage
            | SystemAttribute | TagReader | TimeAction | TollZone | User
            | VideoMonitor | WeatherSensor | Word => true,
            _ => false,
        }
    }

    /// Get base resource for permission checks
    pub const fn base(self) -> Self {
        use Res::*;
        match self {
            // Action plan resources
            DayMatcher | DayPlan | DeviceAction | Hashtag | PlanPhase
            | TimeAction => ActionPlan,
            // Camera resources
            CameraPreset | EncoderStream | EncoderType => Camera,
            // Controller resources
            // (ControllerIo is a special case
            // to GET request all pins for one controller).
            Alarm | CommLink | ControllerIo | GeoLoc | Gps | Modem => {
                Controller
            }
            // Detector resources
            Rnode | Road => Detector,
            // DMS resources
            Font | Graphic | MsgLine | MsgPattern | SignConfig | SignDetail
            | SignMessage | Word => Dms,
            // Incident resources
            IncAdvice | IncDescriptor | IncDetail | IncLocator | RoadAffix => {
                Incident
            }
            // LCS resources
            LcsState => Lcs,
            // Permission resources
            Domain | User | Role => Permission,
            // System attribute resources
            CabinetStyle | CommConfig | EventConfig => SystemAttribute,
            // Toll zone resources
            TagReader => TollZone,
            // Video monitor resources
            FlowStream | MonitorStyle | PlayList => VideoMonitor,
            // Others
            _ => self,
        }
    }
}
