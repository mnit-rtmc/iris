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
use crate::error::{Error, Result};
use crate::resource::{Card, NAME};
use crate::role::Role;
use crate::util::{Dom, HtmlStr, OptVal};
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::fmt;
use wasm_bindgen::JsValue;
use web_sys::Document;

/// Permission
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct Permission {
    pub id: u32,
    pub role: String,
    pub resource_n: String,
    pub batch: Option<String>,
    pub access_n: u32,

    /// Ancillary data
    pub resource_types: Option<Vec<String>>,
    pub roles: Option<Vec<Role>>,
}

impl fmt::Display for Permission {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.id)
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
        (3, true) => "💡 plan",
        (4, false) => "🔧",
        (4, true) => "🔧 configure",
        _ => "❓",
    }
}

/// Create an HTML `select` element of access
fn access_html(selected: u32) -> String {
    let mut html = String::new();
    html.push_str("<select id='edit_access'>");
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

impl Permission {
    /// Create an HTML `select` element of resource types
    fn resource_types_html(&self) -> String {
        let mut html = String::new();
        html.push_str("<select id='edit_resource'>");
        if let Some(resource_types) = &self.resource_types {
            for resource_type in resource_types {
                html.push_str("<option");
                if &self.resource_n == resource_type {
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
    fn roles_html(&self) -> String {
        let mut html = String::new();
        html.push_str("<select id='edit_role'>");
        if let Some(roles) = &self.roles {
            for role in roles {
                html.push_str("<option");
                if self.role == role.name {
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

impl Card for Permission {
    const TNAME: &'static str = "Permission";
    const ENAME: &'static str = "🗝️ Permission";
    const UNAME: &'static str = "permission";

    /// Set the name
    fn with_name(self, _name: &str) -> Self {
        self
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str) -> bool {
        self.id.to_string().contains(search)
            || access_str(self.access_n, true).contains(search)
            || self.role.to_lowercase().contains(search)
            || self.resource_n.contains(search)
    }

    /// Get ancillary URI
    fn ancillary_uri(&self) -> Option<&str> {
        match (&self.resource_types, &self.roles) {
            (None, _) => Some("/iris/resource_type"),
            (_, None) => Some("/iris/api/role"),
            _ => None,
        }
    }

    /// Put ancillary JSON data
    fn ancillary_json(&mut self, json: JsValue) -> Result<()> {
        match &self.resource_types {
            None => {
                let resource_types = json.into_serde::<Vec<String>>()?;
                self.resource_types = Some(resource_types);
            }
            _ => {
                let roles = json.into_serde::<Vec<Role>>()?;
                self.roles = Some(roles);
            }
        }
        Ok(())
    }

    /// Get row for create card
    fn to_html_create(&self) -> String {
        let role = self.roles_html();
        let resource = self.resource_types_html();
        format!(
            "<div class='row'>\
              <label for='edit_role'>Role</label>\
              {role}\
            </div>\
            <div class='row'>\
              <label for='edit_resource'>Resource</label>\
              {resource}\
            </div>"
        )
    }

    /// Get value to create a new object
    fn create_value(doc: &Document) -> Result<String> {
        let role = doc.select_parse::<String>("edit_role");
        let resource_n = doc.select_parse::<String>("edit_resource");
        if let (Some(role), Some(resource_n)) = (role, resource_n) {
            let mut obj = Map::new();
            obj.insert("role".to_string(), Value::String(role));
            obj.insert("resource_n".to_string(), Value::String(resource_n));
            return Ok(Value::Object(obj).to_string());
        }
        Err(Error::Parse())
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let access = access_str(self.access_n, false);
        let role = HtmlStr::new(&self.role).with_len(4);
        let resource = HtmlStr::new(&self.resource_n).with_len(8);
        format!(
            "<span>{access}{role}…{resource}</span>\
            <span class='{NAME}'>{self}</span>"
        )
    }

    /// Convert to edit HTML
    fn to_html_edit(&self) -> String {
        let role = self.roles_html();
        let resource = self.resource_types_html();
        let batch = HtmlStr::new(self.batch.as_ref());
        let access = access_html(self.access_n);
        format!(
            "<div class='row'>\
               <label for='edit_role'>Role</label>\
               {role}\
            </div>\
            <div class='row'>\
              <label for='edit_resource'>Resource</label>\
              {resource}\
            </div>\
            <div class='row'>\
               <label for='edit_batch'>Batch</label>\
               <input id='edit_batch' maxlength='16' size='16' \
                      value='{batch}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_access'>Access</label>\
              {access}\
            </div>"
        )
    }

    /// Get changed fields from Edit form
    fn changed_fields(doc: &Document, json: &JsValue) -> Result<String> {
        let val = Self::new(json)?;
        let mut obj = Map::new();
        if let Some(role) = doc.select_parse::<String>("edit_role") {
            if role != val.role {
                obj.insert("role".to_string(), Value::String(role));
            }
        }
        if let Some(resource_n) = doc.select_parse::<String>("edit_resource") {
            if resource_n != val.resource_n {
                obj.insert("resource_n".to_string(), Value::String(resource_n));
            }
        }
        let batch = doc
            .input_parse::<String>("edit_batch")
            .filter(|b| !b.is_empty());
        if batch != val.batch {
            obj.insert("batch".to_string(), OptVal(batch).into());
        }
        if let Some(access_n) = doc.select_parse::<u32>("edit_access") {
            if access_n != val.access_n {
                obj.insert(
                    "access_n".to_string(),
                    Value::Number(access_n.into()),
                );
            }
        }
        Ok(Value::Object(obj).to_string())
    }
}

/// Build a `select` element of access permissions
pub fn permissions_html(access: Vec<Permission>) -> String {
    let mut html = "<option/>".to_string();
    for perm in &access {
        if perm.batch.is_none() {
            add_option::<crate::alarm::Alarm>(perm, &mut html);
            add_option::<crate::cabinetstyle::CabinetStyle>(perm, &mut html);
            add_option::<crate::commconfig::CommConfig>(perm, &mut html);
            add_option::<crate::commlink::CommLink>(perm, &mut html);
            add_option::<crate::controller::Controller>(perm, &mut html);
            add_option::<crate::modem::Modem>(perm, &mut html);
            add_option::<Permission>(perm, &mut html);
            add_option::<crate::role::Role>(perm, &mut html);
            add_option::<crate::user::User>(perm, &mut html);
        }
    }
    html
}

/// Add option to access select
fn add_option<C: Card>(perm: &Permission, html: &mut String) {
    if perm.resource_n == C::UNAME.to_lowercase() {
        html.push_str("<option value='");
        html.push_str(C::TNAME);
        html.push_str("'>");
        html.push_str(C::ENAME);
        html.push_str("</option>");
    }
}
