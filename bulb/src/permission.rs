// Copyright (C) 2022-2026  Minnesota Department of Transportation
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
use crate::item::ItemState;
use crate::notes::contains_hashtag;
use resources::Res;
use serde::Deserialize;
use serde_json::Value;
use serde_json::map::Map;
use std::cmp::Ordering;

/// Permission access level for a resource type
#[derive(Clone, Copy, Debug, PartialEq, Eq, PartialOrd, Ord)]
pub enum AccessLevel {
    /// Prohibited access level
    None = 0,
    /// View access level
    View = 1,
    /// Operate access level
    Operate = 2,
    /// Manage access level
    Manage = 3,
    /// Configure access level
    Configure = 4,
}

impl From<u32> for AccessLevel {
    fn from(v: u32) -> Self {
        match v {
            1 => Self::View,
            2 => Self::Operate,
            3 => Self::Manage,
            4 => Self::Configure,
            _ => Self::None,
        }
    }
}

impl AccessLevel {
    /// Get item state for an access level
    pub fn item_state(self) -> ItemState {
        match self {
            AccessLevel::View => ItemState::View,
            AccessLevel::Operate => ItemState::Operate,
            AccessLevel::Manage => ItemState::Manage,
            AccessLevel::Configure => ItemState::Configure,
            _ => ItemState::Prohibited,
        }
    }
}

/// Permission
#[derive(Clone, Debug, Default, Deserialize, PartialEq, Eq)]
pub struct Permission {
    pub name: String,
    pub role: String,
    pub base_resource: String,
    pub hashtag: Option<String>,
    pub access_level: u32,
}

impl PartialOrd for Permission {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        Some(self.cmp(other))
    }
}

impl Ord for Permission {
    fn cmp(&self, other: &Self) -> Ordering {
        let ord = self.base_resource.cmp(&other.base_resource);
        if let Ordering::Equal = ord {
            match (&self.hashtag, &other.hashtag) {
                (Some(_), None) => return Ordering::Greater,
                (None, Some(_)) => return Ordering::Less,
                (Some(a), Some(b)) => {
                    let a = a.to_lowercase();
                    let b = b.to_lowercase();
                    let ord = a.cmp(&b);
                    if Ordering::Equal != ord {
                        return ord;
                    }
                }
                _ => (),
            }
            return self.name.cmp(&other.name);
        }
        ord
    }
}

impl Permission {
    /// Create a new permission
    pub fn new(name: String, role: &str, base_resource: &str) -> Self {
        Permission {
            name,
            role: role.to_string(),
            base_resource: base_resource.to_string(),
            hashtag: None,
            access_level: AccessLevel::None as u32,
        }
    }

    /// Convert to JSON value (for POST)
    pub fn value(&self) -> Value {
        let mut obj = Map::new();
        obj.insert("name".to_string(), Value::String(self.name.to_string()));
        obj.insert("role".to_string(), Value::String(self.role.to_string()));
        obj.insert(
            "base_resource".to_string(),
            Value::String(self.base_resource.to_string()),
        );
        Value::Object(obj)
    }

    /// Get access level
    pub fn access_level(&self) -> AccessLevel {
        self.access_level.into()
    }

    /// Get access level for a given resource type
    pub fn access_level_for(&self, res: Res) -> AccessLevel {
        if res.base().as_str() == self.base_resource {
            self.access_level()
        } else {
            AccessLevel::None
        }
    }

    /// Check access for a resource with notes containing hashtags
    pub fn check_access(&self, res: Res, notes: Option<&str>) -> bool {
        res.base().as_str() == self.base_resource
            && self
                .hashtag
                .as_deref()
                .is_none_or(|ht| notes.is_some_and(|n| contains_hashtag(n, ht)))
    }

    /// Get access level from permissions and notes (checking hashtags)
    pub fn access_notes(
        perms: &[Self],
        res: Res,
        notes: Option<&str>,
    ) -> AccessLevel {
        let mut access_level = AccessLevel::None;
        for perm in perms {
            if perm.check_access(res, notes) {
                access_level = access_level.max(perm.access_level());
            }
        }
        access_level
    }
}
