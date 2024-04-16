// access.rs
//
// Copyright (C) 2021-2024  Minnesota Department of Transportation
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
use crate::sonar::Error;
use resources::Res;

/// Access for permission records
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum Access {
    View,
    Operate,
    Manage,
    Configure,
}

impl Access {
    /// Create access from level
    pub const fn new(level: i32) -> Option<Self> {
        match level {
            1 => Some(Self::View),
            2 => Some(Self::Operate),
            3 => Some(Self::Manage),
            4 => Some(Self::Configure),
            _ => None,
        }
    }

    /// Get access level
    pub const fn level(self) -> i32 {
        match self {
            Access::View => 1,
            Access::Operate => 2,
            Access::Manage => 3,
            Access::Configure => 4,
        }
    }

    /// Check for access to a resource
    pub const fn check(self, rhs: Self) -> Result<(), Error> {
        if self.level() >= rhs.level() {
            Ok(())
        } else {
            Err(Error::Forbidden)
        }
    }
}

/// Get access needed to update a resource/attribute
impl From<(Res, &str)> for Access {
    fn from((res, att): (Res, &str)) -> Self {
        if required_operate(res, att) {
            Access::Operate
        } else if required_manage(res, att) {
            Access::Manage
        } else {
            Access::Configure
        }
    }
}

/// Check if Operate access is required to update a resource/attribute
fn required_operate(res: Res, att: &str) -> bool {
    use Res::*;
    match (res, att) {
        (Beacon, "state")
        | (Camera, "ptz")
        | (Camera, "recall_preset")
        | (Controller, "device_req")
        | (Detector, "field_length")
        | (Detector, "force_fail")
        | (Dms, "msg_user")
        | (LaneMarking, "deployed")
        | (LcsArray, "lcs_lock")
        | (RampMeter, "m_lock")
        | (RampMeter, "rate") => true,
        _ => false,
    }
}

/// Check if Manage access is required to update a resource/attribute
fn required_manage(res: Res, att: &str) -> bool {
    use Res::*;
    match (res, att) {
        (Beacon, "message")
        | (Beacon, "notes")
        | (Beacon, "preset")
        | (Camera, "notes")
        | (Camera, "publish")
        | (Camera, "streamable")
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
        | (Dms, "notes")
        | (Dms, "preset")
        | (LaneMarking, "notes")
        | (LcsArray, "notes")
        | (Modem, "enabled")
        | (Modem, "timeout_ms")
        | (RampMeter, "notes")
        | (RampMeter, "storage")
        | (RampMeter, "max_wait")
        | (RampMeter, "algorithm")
        | (RampMeter, "am_target")
        | (RampMeter, "pm_target")
        | (Role, "enabled")
        | (User, "enabled")
        | (WeatherSensor, "site_id")
        | (WeatherSensor, "alt_id")
        | (WeatherSensor, "notes") => true,
        _ => false,
    }
}
