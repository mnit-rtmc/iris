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
use crate::card::{disabled_attr, Card};
use crate::error::Result;
use crate::util::{Dom, HtmlStr, OptVal};
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::fmt;
use wasm_bindgen::JsValue;
use web_sys::Document;

/// User
#[derive(Debug, Deserialize, Serialize)]
pub struct User {
    pub name: String,
    pub full_name: String,
    pub role: Option<String>,
    pub enabled: bool,
}

impl fmt::Display for User {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.name)
    }
}

impl Card for User {
    const TNAME: &'static str = "User";
    const ENAME: &'static str = "👤 User";
    const UNAME: &'static str = "user";

    /// Check if a search string matches
    fn is_match(&self, search: &str) -> bool {
        self.name.contains(search)
            || self.full_name.to_lowercase().contains(search)
            || self
                .role
                .as_deref()
                .unwrap_or("")
                .to_lowercase()
                .contains(search)
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let disabled = disabled_attr(self.enabled && self.role.is_some());
        format!("<span{disabled}>{self}</span>")
    }

    /// Convert to edit HTML
    fn to_html_edit(&self) -> String {
        let full_name = HtmlStr::new(&self.full_name);
        let role = HtmlStr::new(self.role.as_ref());
        let enabled = if self.enabled { " checked" } else { "" };
        format!(
            "<div class='row'>\
               <label for='edit_full'>Full Name</label>\
               <input id='edit_full' maxlength='31' size='20' \
                      value='{full_name}'/>\
            </div>\
            <div class='row'>\
               <label for='edit_role'>Role</label>\
               <input id='edit_role' maxlength='15' size='15' \
                      value='{role}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_enabled'>Enabled</label>\
              <input id='edit_enabled' type='checkbox'{enabled}/>\
            </div>"
        )
    }

    /// Get changed fields from Edit form
    fn changed_fields(doc: &Document, json: &JsValue) -> Result<String> {
        let val = Self::new(json)?;
        let mut obj = Map::new();
        if let Some(full_name) = doc.input_parse::<String>("edit_full") {
            if full_name != val.full_name {
                obj.insert("full_name".to_string(), Value::String(full_name));
            }
        }
        let role = doc
            .input_parse::<String>("edit_role")
            .filter(|r| !r.is_empty());
        if role != val.role {
            obj.insert("role".to_string(), OptVal(role).into());
        }
        if let Some(enabled) = doc.input_bool("edit_enabled") {
            if enabled != val.enabled {
                obj.insert("enabled".to_string(), Value::Bool(enabled));
            }
        }
        Ok(Value::Object(obj).to_string())
    }
}
