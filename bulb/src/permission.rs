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
use hatmil::html;
use resources::Res;
use serde::Deserialize;
use serde_json::Value;
use serde_json::map::Map;
use std::cmp::Ordering;

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

/// Get item state for an access level
fn item_state(access_level: u32) -> ItemState {
    match access_level {
        1 => ItemState::View,
        2 => ItemState::Operate,
        3 => ItemState::Manage,
        4 => ItemState::Configure,
        _ => ItemState::Prohibited,
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
            access_level: 0,
        }
    }

    /// Convert to JSON value (for POST)
    pub fn into_value(self) -> Value {
        let mut obj = Map::new();
        obj.insert("name".to_string(), Value::String(self.name));
        obj.insert("role".to_string(), Value::String(self.role));
        obj.insert(
            "base_resource".to_string(),
            Value::String(self.base_resource),
        );
        Value::Object(obj)
    }

    /// Get access level for a given resource type
    pub fn access_level_for(&self, res: Res) -> u32 {
        if res.base().as_str() == self.base_resource {
            self.access_level
        } else {
            0
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

    /// Convert to HTML table row
    pub fn table_row<'p>(&self, tr: &'p mut html::Tr<'p>) {
        match self.hashtag.as_ref() {
            Some(hashtag) => tr.td().class("member").cdata(hashtag).close(),
            None => tr.td().cdata(&self.base_resource).close(),
        };
        let mut td = tr.td();
        let mut select = td.select();
        select.id(&self.name);
        for access in 0..=4 {
            let mut option = select.option();
            option.value(access.to_string());
            if access == self.access_level {
                option.selected();
            }
            let item = item_state(access);
            option
                .cdata(item.code())
                .cdata(" ")
                .cdata(item.description())
                .close();
        }
        select.close();
        tr.close();
    }
}
