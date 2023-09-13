// Copyright (C) 2022-2023  Minnesota Department of Transportation
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
use crate::fetch::Uri;
use crate::resource::{AncillaryData, Card, Resource, View, NAME};
use crate::role::Role;
use crate::util::{ContainsLower, Doc, Fields, HtmlStr, Input, Select};
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::fmt;
use wasm_bindgen::JsValue;

/// Permission
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct Permission {
    pub id: u32,
    pub role: String,
    pub resource_n: String,
    pub hashtag: Option<String>,
    pub access_n: u32,
}

/// Ancillary permission data
#[derive(Debug, Default)]
pub struct PermissionAnc {
    pub resource_types: Option<Vec<String>>,
    pub roles: Option<Vec<Role>>,
}

const RESOURCE_TYPE_URI: &str = "/iris/resource_type";
const ROLE_URI: &str = "/iris/api/role";

impl AncillaryData for PermissionAnc {
    type Primary = Permission;

    /// Get next ancillary URI
    fn next_uri(&self, view: View, _pri: &Permission) -> Option<Uri> {
        match (view, &self.resource_types, &self.roles) {
            (View::Create | View::Edit, None, _) => {
                Some(RESOURCE_TYPE_URI.into())
            }
            (View::Create | View::Edit, _, None) => Some(ROLE_URI.into()),
            _ => None,
        }
    }

    /// Put ancillary JSON data
    fn set_json(
        &mut self,
        view: View,
        pri: &Permission,
        json: JsValue,
    ) -> Result<()> {
        if let Some(uri) = self.next_uri(view, pri) {
            match uri.borrow() {
                RESOURCE_TYPE_URI => {
                    self.resource_types =
                        Some(serde_wasm_bindgen::from_value(json)?);
                }
                _ => {
                    self.roles = Some(serde_wasm_bindgen::from_value(json)?);
                }
            }
        }
        Ok(())
    }
}

impl PermissionAnc {
    /// Create an HTML `select` element of resource types
    fn resource_types_html(&self, pri: &Permission) -> String {
        let mut html = String::new();
        html.push_str("<select id='resource_n'>");
        if let Some(resource_types) = &self.resource_types {
            for resource_type in resource_types {
                html.push_str("<option");
                if &pri.resource_n == resource_type {
                    html.push_str(" selected");
                }
                html.push('>');
                html.push_str(resource_type);
                html.push_str("</option>");
            }
        }
        html.push_str("</select>");
        html
    }

    /// Create an HTML `select` element of roles
    fn roles_html(&self, pri: &Permission) -> String {
        let mut html = String::new();
        html.push_str("<select id='role'>");
        if let Some(roles) = &self.roles {
            for role in roles {
                html.push_str("<option");
                if pri.role == role.name {
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

/// Get access to display
fn access_str(access_n: u32, long: bool) -> &'static str {
    match (access_n, long) {
        (1, false) => "👁️",
        (1, true) => "👁️ view",
        (2, false) => "👉",
        (2, true) => "👉 operate",
        (3, false) => "💡",
        (3, true) => "💡 manage",
        (4, false) => "🔧",
        (4, true) => "🔧 configure",
        _ => "❓",
    }
}

/// Create an HTML `select` element of access
fn access_html(selected: u32) -> String {
    let mut html = String::new();
    html.push_str("<select id='access_n'>");
    for access_n in 1..=4 {
        html.push_str("<option value='");
        html.push_str(&access_n.to_string());
        html.push('\'');
        if selected == access_n {
            html.push_str(" selected");
        }
        html.push('>');
        html.push_str(access_str(access_n, true));
        html.push_str("</option>");
    }
    html.push_str("</select>");
    html
}

/// Build a `select` element of access permissions
pub fn permissions_html(access: Vec<Permission>, config: bool) -> String {
    let mut html = "<option/>".to_string();
    for perm in &access {
        if perm.hashtag.is_none() {
            if config {
                add_option(Resource::Alarm, perm, &mut html);
            }
            add_option(Resource::Beacon, perm, &mut html);
            if config {
                add_option(Resource::CabinetStyle, perm, &mut html);
            }
            add_option(Resource::Camera, perm, &mut html);
            if config {
                add_option(Resource::CommConfig, perm, &mut html);
                add_option(Resource::CommLink, perm, &mut html);
                add_option(Resource::Controller, perm, &mut html);
                add_option(Resource::Detector, perm, &mut html);
            }
            add_option(Resource::Dms, perm, &mut html);
            if config {
                add_option(Resource::FlowStream, perm, &mut html);
                add_option(Resource::GateArm, perm, &mut html);
            }
            add_option(Resource::GateArmArray, perm, &mut html);
            if config {
                add_option(Resource::Gps, perm, &mut html);
            }
            add_option(Resource::LaneMarking, perm, &mut html);
            add_option(Resource::LcsArray, perm, &mut html);
            if config {
                add_option(Resource::LcsIndication, perm, &mut html);
                add_option(Resource::Modem, perm, &mut html);
                add_option(Resource::Permission, perm, &mut html);
            }
            add_option(Resource::RampMeter, perm, &mut html);
            if config {
                add_option(Resource::Role, perm, &mut html);
                add_option(Resource::TagReader, perm, &mut html);
                add_option(Resource::User, perm, &mut html);
            }
            add_option(Resource::VideoMonitor, perm, &mut html);
            add_option(Resource::WeatherSensor, perm, &mut html);
        }
    }
    html
}

/// Add option to access select
fn add_option(res: Resource, perm: &Permission, html: &mut String) {
    if perm.resource_n == res.rname() {
        html.push_str("<option value='");
        html.push_str(res.rname());
        html.push_str("'>");
        html.push_str(res.dname());
        html.push_str("</option>");
    }
}

impl Permission {
    pub const RESOURCE_N: &'static str = "permission";

    /// Get value to create a new object
    pub fn create_value(doc: &Doc) -> Result<String> {
        let role = doc.select_parse::<String>("role");
        let resource_n = doc.select_parse::<String>("resource_n");
        if let (Some(role), Some(resource_n)) = (role, resource_n) {
            let mut obj = Map::new();
            obj.insert("role".to_string(), Value::String(role));
            obj.insert("resource_n".to_string(), Value::String(resource_n));
            return Ok(Value::Object(obj).to_string());
        }
        Err(Error::Parse())
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let role = HtmlStr::new(&self.role);
        let access = access_str(self.access_n, false);
        let resource = HtmlStr::new(&self.resource_n);
        format!(
            "<div class='{NAME} end'>{role} {access} {self}</div>\
            <div class='info fill'>{resource}</div>"
        )
    }

    /// Convert to Edit HTML
    fn to_html_edit(&self, anc: &PermissionAnc) -> String {
        let role = anc.roles_html(self);
        let resource = anc.resource_types_html(self);
        let hashtag = HtmlStr::new(&self.hashtag);
        let access = access_html(self.access_n);
        format!(
            "<div class='row'>\
               <label for='role'>Role</label>\
               {role}\
            </div>\
            <div class='row'>\
              <label for='resource_n'>Resource</label>\
              {resource}\
            </div>\
            <div class='row'>\
               <label for='hashtag'>Hashtag</label>\
               <input id='hashtag' maxlength='16' size='16' value='{hashtag}'>\
            </div>\
            <div class='row'>\
              <label for='access_n'>Access</label>\
              {access}\
            </div>"
        )
    }
}

impl fmt::Display for Permission {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.id)
    }
}

impl Card for Permission {
    type Ancillary = PermissionAnc;

    /// Set the name
    fn with_name(self, _name: &str) -> Self {
        self
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, _anc: &PermissionAnc) -> bool {
        self.id.to_string().contains(search)
            || access_str(self.access_n, true).contains(search)
            || self.role.contains_lower(search)
            || self.resource_n.contains(search)
    }

    /// Get row for Create card
    fn to_html_create(&self, anc: &PermissionAnc) -> String {
        let role = anc.roles_html(self);
        let resource = anc.resource_types_html(self);
        format!(
            "<div class='row'>\
              <label for='role'>Role</label>\
              {role}\
            </div>\
            <div class='row'>\
              <label for='resource_n'>Resource</label>\
              {resource}\
            </div>"
        )
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &PermissionAnc) -> String {
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
        fields.changed_select("role", &self.role);
        fields.changed_select("resource_n", &self.resource_n);
        fields.changed_input("hashtag", &self.hashtag);
        fields.changed_select("access_n", self.access_n);
        fields.into_value().to_string()
    }
}
