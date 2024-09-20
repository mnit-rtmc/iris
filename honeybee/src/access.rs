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
use crate::error::{Error, Result};
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
    pub const fn check(self, rhs: Self) -> Result<()> {
        if self.level() >= rhs.level() {
            Ok(())
        } else {
            Err(Error::Forbidden)
        }
    }

    /// Get access required to PATCH a resource/attribute
    pub fn required_patch(res: Res, att: &str) -> Self {
        if required_patch_operate(res, att) {
            Access::Operate
        } else if required_patch_manage(res, att) {
            Access::Manage
        } else {
            Access::Configure
        }
    }

    /// Get access required to POST a resource
    pub fn required_post(res: Res) -> Self {
        if required_post_operate(res) {
            Access::Operate
        } else if required_post_manage(res) {
            Access::Manage
        } else {
            Access::Configure
        }
    }
}

/// Check if Operate access is required to PATCH a resource/attribute
fn required_patch_operate(res: Res, att: &str) -> bool {
    match (res, att) {
        (Res::ActionPlan, "phase")
        | (Res::Beacon, "state")
        | (Res::Camera, "ptz")
        | (Res::Camera, "publish")
        | (Res::Camera, "recall_preset")
        | (Res::Controller, "device_request")
        | (Res::Detector, "field_length")
        | (Res::Detector, "force_fail")
        | (Res::Dms, "msg_user")
        | (Res::GateArmArray, "arm_state")
        | (Res::LaneMarking, "deployed")
        | (Res::LcsArray, "lcs_lock")
        | (Res::RampMeter, "m_lock")
        | (Res::RampMeter, "rate")
        | (Res::VideoMonitor, "camera")
        | (Res::VideoMonitor, "play_list") => true,
        _ => false,
    }
}

/// Check if Manage access is required to PATCH a resource/attribute
fn required_patch_manage(res: Res, att: &str) -> bool {
    match (res, att) {
        (Res::ActionPlan, "notes")
        | (Res::ActionPlan, "sync_actions")
        | (Res::ActionPlan, "sticky")
        | (Res::ActionPlan, "ignore_auto_fail")
        | (Res::ActionPlan, "active")
        | (Res::ActionPlan, "default_phase")
        | (Res::Beacon, "message")
        | (Res::Beacon, "notes")
        | (Res::Beacon, "preset")
        | (Res::Camera, "notes")
        | (Res::Camera, "store_preset")
        | (Res::CommConfig, "timeout_ms")
        | (Res::CommConfig, "idle_disconnect_sec")
        | (Res::CommConfig, "no_response_disconnect_sec")
        | (Res::CommLink, "poll_enabled")
        | (Res::Controller, "condition")
        | (Res::Controller, "notes")
        | (Res::Detector, "abandoned")
        | (Res::Detector, "notes")
        | (Res::Dms, "device_request")
        | (Res::Dms, "notes")
        | (Res::Dms, "preset")
        | (Res::Domain, "enabled")
        | (Res::GateArm, "notes")
        | (Res::GateArmArray, "notes")
        | (Res::LaneMarking, "notes")
        | (Res::LcsArray, "notes")
        | (Res::Modem, "enabled")
        | (Res::Modem, "timeout_ms")
        | (Res::MsgLine, "restrict_hashtag")
        | (Res::MsgLine, "rank")
        | (Res::MsgPattern, "compose_hashtag")
        | (Res::MsgPattern, "flash_beacon")
        | (Res::RampMeter, "notes")
        | (Res::RampMeter, "storage")
        | (Res::RampMeter, "max_wait")
        | (Res::RampMeter, "algorithm")
        | (Res::RampMeter, "am_target")
        | (Res::RampMeter, "pm_target")
        | (Res::Role, "enabled")
        | (Res::User, "enabled")
        | (Res::User, "password")
        | (Res::VideoMonitor, "notes")
        | (Res::VideoMonitor, "restricted")
        | (Res::VideoMonitor, "monitor_style")
        | (Res::WeatherSensor, "device_request")
        | (Res::WeatherSensor, "site_id")
        | (Res::WeatherSensor, "alt_id")
        | (Res::WeatherSensor, "notes") => true,
        _ => false,
    }
}

/// Check if Operate access is required to POST a resource
fn required_post_operate(res: Res) -> bool {
    use Res::*;
    match res {
        SignMessage => true,
        _ => false,
    }
}

/// Check if Manage access is required to POST a resource
fn required_post_manage(_res: Res) -> bool {
    false
}
