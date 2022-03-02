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
use crate::card::{Card, NAME};
use crate::start::resource_types_html;
use crate::util::{Dom, HtmlStr};
use crate::Result;
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::fmt;
use wasm_bindgen::JsValue;
use web_sys::Document;

/// Permission
#[derive(Debug, Deserialize, Serialize)]
pub struct Permission {
    pub id: u32,
    pub role: String,
    pub resource_n: String,
    pub batch: Option<String>,
    pub access_n: u32,
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

impl Card for Permission {
    const TNAME: &'static str = "Permission";
    const ENAME: &'static str = "🗝️ Permission";
    const UNAME: &'static str = "permission";

    fn is_match(&self, tx: &str) -> bool {
        self.id.to_string().contains(tx)
            | access_str(self.access_n, true).contains(tx)
            | self.role.to_lowercase().contains(tx)
            | self.resource_n.contains(tx)
    }

    /// Get row for create card
    fn html_create(_name: &str) -> String {
        let resource = resource_types_html("");
        format!(
            "<div class='row'>\
              <label for='edit_role'>Role</label>\
              <input id='edit_role' maxlength='24' size='24'/>\
            </div>\
            <div class='row'>\
              <label for='edit_resource'>Resource</label>\
              {resource}\
            </div>"
        )
    }

    /// Get value to create a new object
    fn create_value(doc: &Document) -> Result<String> {
        let role = doc.input_parse::<String>("edit_role");
        let resource_n = doc.select_parse::<String>("edit_resource");
        if let (Some(role), Some(resource_n)) = (role, resource_n) {
            let mut obj = Map::new();
            obj.insert("role".to_string(), Value::String(role));
            obj.insert("resource_n".to_string(), Value::String(resource_n));
            return Ok(Value::Object(obj).to_string());
        }
        Err("parse error".into())
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
        let role = HtmlStr::new(&self.role);
        let resource = resource_types_html(&self.resource_n);
        let batch = HtmlStr::new(self.batch.as_ref());
        let access = access_html(self.access_n);
        format!(
            "<div class='row'>\
               <label for='edit_role'>Role</label>\
               <input id='edit_role' maxlength='15' size='15' \
                      value='{role}'/>\
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
        let mut obj = Map::new();
        Ok(Value::Object(obj).to_string())
    }
}
