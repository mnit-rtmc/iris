// Copyright (C) 2025  Minnesota Department of Transportation
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
use chrono::{DateTime, Local, format::SecondsFormat};
use std::fmt;
use std::time::Duration;

/// Device lock reason
#[derive(Clone, Copy, Debug, PartialEq)]
pub enum LockReason {
    Unlocked,
    Incident,
    Situation,
    Testing,
    KnockedDown,
    Indication,
    Maintenance,
    Construction,
    Reserve,
}

impl From<&str> for LockReason {
    fn from(r: &str) -> Self {
        match r {
            "incident" => Self::Incident,
            "situation" => Self::Situation,
            "testing" => Self::Testing,
            "knocked down" => Self::KnockedDown,
            "indication" => Self::Indication,
            "maintenance" => Self::Maintenance,
            "construction" => Self::Construction,
            "reserve" => Self::Reserve,
            _ => Self::Unlocked,
        }
    }
}

impl fmt::Display for LockReason {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.as_str())
    }
}

impl LockReason {
    /// Get a slice containing all reasons
    pub fn all() -> &'static [LockReason] {
        &[
            LockReason::Unlocked,
            LockReason::Incident,
            LockReason::Situation,
            LockReason::Testing,
            LockReason::KnockedDown,
            LockReason::Indication,
            LockReason::Maintenance,
            LockReason::Construction,
            LockReason::Reserve,
        ]
    }

    /// Get lock reason as a string slice
    pub fn as_str(self) -> &'static str {
        use LockReason::*;
        match self {
            Unlocked => "unlocked",
            Incident => "incident",
            Situation => "situation",
            Testing => "testing",
            KnockedDown => "knocked down",
            Indication => "indication",
            Maintenance => "maintenance",
            Construction => "construction",
            Reserve => "reserve",
        }
    }

    /// Check if device lock is deployable
    pub fn is_deployable(self) -> bool {
        use LockReason::*;
        matches!(self, Unlocked | Incident | Situation | Testing)
    }

    /// Get lock duration
    pub fn duration(self) -> Option<Duration> {
        match self {
            LockReason::Incident => Some(Duration::from_secs(30 * 60)),
            LockReason::Situation => Some(Duration::from_secs(60 * 60)),
            LockReason::Testing => Some(Duration::from_secs(5 * 60)),
            _ => None,
        }
    }

    /// Make expire time
    pub fn make_expires(self) -> Option<String> {
        self.duration().map(|d| {
            let now: DateTime<Local> = Local::now();
            (now + d).to_rfc3339_opts(SecondsFormat::Secs, false)
        })
    }
}
