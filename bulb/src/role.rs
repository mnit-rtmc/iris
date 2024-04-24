// Copyright (C) 2022-2024  Minnesota Department of Transportation
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
use crate::card::{inactive_attr, AncillaryData, Card, View};
use crate::util::{ContainsLower, Fields, Input};
use resources::Res;
use serde::{Deserialize, Serialize};
use std::fmt;

/// Role
#[derive(Debug, Default, Deserialize, Serialize, PartialEq)]
pub struct Role {
    pub name: String,
    pub enabled: bool,
}

/// Ancillary role data
#[derive(Debug, Default)]
pub struct RoleAnc;

impl AncillaryData for RoleAnc {
    type Primary = Role;
}

impl Role {
    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let inactive = inactive_attr(self.enabled);
        format!("<div class='fill{inactive}'>{self}</div>")
    }

    /// Convert to Edit HTML
    fn to_html_edit(&self) -> String {
        let enabled = if self.enabled { " checked" } else { "" };
        format!(
            "<div class='row'>\
              <label for='enabled'>Enabled</label>\
              <input id='enabled' type='checkbox'{enabled}>\
            </div>"
        )
    }
}

impl fmt::Display for Role {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.name)
    }
}

impl Card for Role {
    type Ancillary = RoleAnc;

    /// Display name
    const DNAME: &'static str = "💪 Role";

    /// Get the resource
    fn res() -> Res {
        Res::Role
    }

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, _anc: &RoleAnc) -> bool {
        self.name.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &RoleAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Edit => self.to_html_edit(),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Edit form
    fn changed_fields(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("enabled", self.enabled);
        fields.into_value().to_string()
    }
}
