// Copyright (C) 2022  Minnesota Department of Transportation
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
use crate::error::Result;
use crate::resource::{disabled_attr, AncillaryData, Card, View};
use crate::role::Role;
use crate::util::{ContainsLower, Fields, HtmlStr, Input, Select};
use serde::{Deserialize, Serialize};
use std::borrow::Cow;
use std::fmt;
use wasm_bindgen::JsValue;

/// User
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct User {
    pub name: String,
    pub full_name: String,
    pub role: Option<String>,
    pub enabled: bool,
}

/// Ancillary user data
#[derive(Debug, Default)]
pub struct UserAnc {
    pub roles: Option<Vec<Role>>,
}

impl AncillaryData for UserAnc {
    type Primary = User;

    /// Get next ancillary URI
    fn next_uri(&self, view: View, _pri: &User) -> Option<Cow<str>> {
        match (view, &self.roles) {
            (View::Edit, None) => Some("/iris/api/role".into()),
            _ => None,
        }
    }

    /// Put ancillary JSON data
    fn set_json(
        &mut self,
        _view: View,
        _pri: &User,
        json: JsValue,
    ) -> Result<()> {
        self.roles = Some(serde_wasm_bindgen::from_value(json)?);
        Ok(())
    }
}

impl UserAnc {
    /// Create an HTML `select` element of roles
    fn roles_html(&self, pri: &User) -> String {
        let mut html = String::new();
        html.push_str("<select id='role'>");
        html.push_str("<option></option>");
        if let Some(roles) = &self.roles {
            for role in roles {
                html.push_str("<option");
                if pri.role.as_ref() == Some(&role.name) {
                    html.push_str(" selected");
                }
                html.push('>');
                html.push_str(&role.name);
                html.push_str("</option>");
            }
        }
        html.push_str("</select>");
        html
    }
}

impl User {
    pub const RESOURCE_N: &'static str = "user";

    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let disabled = disabled_attr(self.enabled && self.role.is_some());
        format!("<div class='{disabled}'>{self}</div>")
    }

    /// Convert to Edit HTML
    fn to_html_edit(&self, anc: &UserAnc) -> String {
        let full_name = HtmlStr::new(&self.full_name);
        let role = anc.roles_html(self);
        let enabled = if self.enabled { " checked" } else { "" };
        format!(
            "<div class='row'>\
               <label for='full_name'>Full Name</label>\
               <input id='full_name' maxlength='31' size='20' \
                      value='{full_name}'>\
            </div>\
            <div class='row'>\
               <label for='role'>Role</label>\
               {role}\
            </div>\
            <div class='row'>\
              <label for='enabled'>Enabled</label>\
              <input id='enabled' type='checkbox'{enabled}>\
            </div>"
        )
    }
}

impl fmt::Display for User {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.name)
    }
}

impl Card for User {
    type Ancillary = UserAnc;

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, _anc: &UserAnc) -> bool {
        self.name.contains(search)
            || self.full_name.contains_lower(search)
            || self.role.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &UserAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Compact => self.to_html_compact(),
            View::Edit => self.to_html_edit(anc),
            _ => unreachable!(),
        }
    }

    /// Get changed fields from Edit form
    fn changed_fields(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("full_name", &self.full_name);
        fields.changed_select("role", &self.role);
        fields.changed_input("enabled", self.enabled);
        fields.into_value().to_string()
    }
}
