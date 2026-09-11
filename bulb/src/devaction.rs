// Copyright (C) 2025-2026  Minnesota Department of Transportation
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
use crate::msgpriority::MsgPriority;
use crate::util::Fields;
use serde::Deserialize;
use serde_json::Value;
use serde_json::map::Map;

/// Device action
#[derive(Clone, Debug, Deserialize, PartialEq)]
pub struct DeviceAction {
    pub name: String,
    pub action_plan: String,
    pub hashtag: String,
    pub phase: String,
    pub msg_pattern: Option<String>,
    pub msg_priority: u8,
    pub sticky: bool,
    pub ignore_auto_fail: bool,
}

impl DeviceAction {
    /// Create a new device action
    pub fn new(name: &str, ap: &str, phase: &str) -> Self {
        DeviceAction {
            name: name.to_string(),
            action_plan: ap.to_string(),
            hashtag: String::new(),
            phase: phase.to_string(),
            msg_pattern: None,
            msg_priority: MsgPriority::Medium1 as u8,
            sticky: false,
            ignore_auto_fail: false,
        }
    }

    /// Check if device action is valid
    pub fn is_valid(&self) -> bool {
        self.hashtag.len() > 1
            && self.hashtag.starts_with('#')
            && self.hashtag[1..].chars().all(|c| c.is_alphanumeric())
    }

    /// Get set of changed fields
    pub fn changed_fields(&self, da: &Self) -> Fields {
        let mut fields = Fields::new();
        if self.hashtag != da.hashtag {
            fields.insert_str("hashtag", &self.hashtag);
        }
        if self.phase != da.phase {
            fields.insert_str("phase", &self.phase);
        }
        if self.msg_pattern != da.msg_pattern {
            fields.insert_opt_str("msg_pattern", self.msg_pattern.as_deref());
        }
        if self.msg_priority != da.msg_priority {
            fields.insert_num("msg_priority", self.msg_priority);
        }
        if self.sticky != da.sticky {
            fields.insert_bool("sticky", self.sticky);
        }
        if self.ignore_auto_fail != da.ignore_auto_fail {
            fields.insert_bool("ignore_auto_fail", self.ignore_auto_fail);
        }
        fields
    }

    /// Convert to JSON value (for POST)
    pub fn value(&self) -> Value {
        let mut obj = Map::new();
        obj.insert("name".to_string(), Value::String(self.name.to_string()));
        obj.insert(
            "action_plan".to_string(),
            Value::String(self.action_plan.to_string()),
        );
        Value::Object(obj)
    }
}
