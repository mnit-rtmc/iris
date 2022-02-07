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
use crate::card::{disabled_attr, Card, NAME};
use crate::util::{Dom, HtmlStr};
use crate::Result;
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use wasm_bindgen::JsValue;
use web_sys::Document;

/// Comm link
#[derive(Debug, Deserialize, Serialize)]
pub struct CommLink {
    pub name: String,
    pub description: String,
    pub uri: String,
    pub comm_config: String,
    pub poll_enabled: bool,
    pub connected: Option<bool>,
}

impl Card for CommLink {
    const TNAME: &'static str = "Comm Link";
    const ENAME: &'static str = "🔗 Comm Link";
    const HAS_STATUS: bool = true;
    const URI: &'static str = "/iris/api/comm_link";

    fn is_match(&self, tx: &str) -> bool {
        self.description.to_lowercase().contains(tx)
            || self.name.to_lowercase().contains(tx)
            || self.comm_config.to_lowercase().contains(tx)
            || self.uri.to_lowercase().contains(tx)
        // TODO: check comm_config protocol
    }

    fn name(&self) -> &str {
        &self.name
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let description = HtmlStr(&self.description);
        let name = HtmlStr(&self.name);
        let disabled = disabled_attr(self.poll_enabled);
        format!(
            "<span{disabled}>{description}</span>\
            <span class='{NAME}'>{name}</span>"
        )
    }

    /// Convert to status HTML
    fn to_html_status(&self) -> String {
        let description = HtmlStr(&self.description);
        let connected = if *self.connected.as_ref().unwrap_or(&false) {
            "Yes (online)"
        } else {
            "No (offline)"
        };
        format!(
            "<div class='row'>\
              <span>Description</span>\
              <span class='info'>{description}</span>\
            </div>\
            <div class='row'>\
              <span>Connected</span>\
              <span class='info'>{connected}</span>\
            </div>"
        )
    }

    /// Convert to edit HTML
    fn to_html_edit(&self) -> String {
        let description = HtmlStr(&self.description);
        let uri = HtmlStr(&self.uri);
        let enabled = if self.poll_enabled { " checked" } else { "" };
        let comm_config = HtmlStr(&self.comm_config);
        format!(
            "<div class='row'>\
              <label for='edit_desc'>Description</label>\
              <input id='edit_desc' maxlength='32' size='24' \
                     value='{description}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_uri'>URI</label>\
              <input id='edit_uri' maxlength='256' size='28' \
                     value='{uri}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_config'>Comm Config</label>\
              <input id='edit_config' maxlength='10' size='10' \
                     value='{comm_config}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_enabled'>Poll Enabled</label>\
              <input id='edit_enabled' type='checkbox'{enabled}/>\
            </div>"
        )
    }

    /// Get changed fields from Edit form
    fn changed_fields(doc: &Document, json: &JsValue) -> Result<String> {
        let val = Self::new(json)?;
        let mut obj = Map::new();
        if let Some(desc) = doc.input_parse::<String>("edit_desc") {
            if desc != val.description {
                obj.insert("description".to_string(), Value::String(desc));
            }
        }
        if let Some(uri) = doc.input_parse::<String>("edit_uri") {
            if uri != val.uri {
                obj.insert("uri".to_string(), Value::String(uri));
            }
        }
        if let Some(comm_config) = doc.input_parse::<String>("edit_config") {
            if comm_config != val.comm_config {
                obj.insert(
                    "comm_config".to_string(),
                    Value::String(comm_config),
                );
            }
        }
        if let Some(poll_enabled) = doc.input_bool("edit_enabled") {
            if poll_enabled != val.poll_enabled {
                obj.insert(
                    "poll_enabled".to_string(),
                    Value::Bool(poll_enabled),
                );
            }
        }
        Ok(Value::Object(obj).to_string())
    }
}
