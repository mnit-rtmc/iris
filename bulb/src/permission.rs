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

/// Permission
#[derive(Debug, Default, Deserialize, PartialEq, Eq, PartialOrd, Ord)]
pub struct Permission {
    // NOTE: field order affects derived PartialOrd / Ord
    pub base_resource: String,
    pub hashtag: Option<String>,
    pub access_level: u32,
    pub name: String,
    pub role: String,
}

/// Get item state for an access level
fn item_state(access_level: u32) -> ItemState {
    match access_level {
        1 => ItemState::View,
        2 => ItemState::Operate,
        3 => ItemState::Manage,
        4 => ItemState::Configure,
        _ => ItemState::Unknown,
    }
}

impl Permission {
    /// Create a new permission
    pub fn new(base_resource: &str, role: &str) -> Self {
        Permission {
            base_resource: base_resource.to_string(),
            hashtag: None,
            access_level: 0,
            name: "fake permission".to_string(),
            role: role.to_string(),
        }
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
        access_level_html(self.access_level, &mut td.select());
        tr.close();
    }
}

/// Create an HTML `select` element of access level
fn access_level_html<'p>(selected: u32, select: &'p mut html::Select<'p>) {
    select.id("access_level");
    select
        .option()
        .value("0".to_string())
        .cdata("🚫 none")
        .close();
    for access in 1..=4 {
        let mut option = select.option();
        option.value(access.to_string());
        if selected == access {
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
}
