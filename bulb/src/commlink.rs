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
use crate::commconfig::CommConfig;
use crate::error::Result;
use crate::util::{Dom, HtmlStr};
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::fmt;
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
    pub connected: bool,

    /// Ancillary comm config list
    pub comm_configs: Option<Vec<CommConfig>>,
}

impl fmt::Display for CommLink {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl CommLink {
    /// Get connected state to display
    fn connected(&self, long: bool) -> &'static str {
        match (self.poll_enabled, self.connected, long) {
            (true, true, false) => "👍",
            (true, true, true) => "online 👍",
            (true, false, false) => "🔌",
            (true, false, true) => "offline 🔌",
            (false, _, false) => "❓",
            (false, _, true) => "disabled ❓",
        }
    }

    /// Get comm config description
    fn comm_config_desc(&self) -> &str {
        if let Some(comm_configs) = &self.comm_configs {
            for config in comm_configs {
                if self.comm_config == config.name {
                    return &config.description;
                }
            }
        }
        ""
    }

    /// Create an HTML `select` element of comm configs
    fn comm_configs_html(&self) -> String {
        let mut html = String::new();
        html.push_str("<select id='edit_config'>");
        if let Some(comm_configs) = &self.comm_configs {
            for config in comm_configs {
                html.push_str("<option value='");
                html.push_str(&config.name);
                html.push('\'');
                if self.comm_config == config.name {
                    html.push_str(" selected");
                }
                html.push('>');
                html.push_str(&config.description);
                html.push_str("</option>");
            }
        }
        html.push_str("</select>");
        html
    }
}

impl Card for CommLink {
    const TNAME: &'static str = "Comm Link";
    const ENAME: &'static str = "🔗 Comm Link";
    const UNAME: &'static str = "comm_link";
    const HAS_STATUS: bool = true;

    /// Check if a search string matches
    fn is_match(&self, search: &str) -> bool {
        self.description.to_lowercase().contains(search)
            || self.name.to_lowercase().contains(search)
            || self.comm_config_desc().to_lowercase().contains(search)
            || self.uri.to_lowercase().contains(search)
            || self.connected(true).contains(search)
    }

    /// Get ancillary URI
    fn ancillary_uri(&self) -> Option<&str> {
        if self.comm_configs.is_none() {
            Some("/iris/api/comm_config")
        } else {
            None
        }
    }

    /// Put ancillary JSON data
    fn ancillary_json(&mut self, json: JsValue) -> Result<()> {
        let comm_configs = json.into_serde::<Vec<CommConfig>>()?;
        self.comm_configs = Some(comm_configs);
        Ok(())
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let connected = self.connected(false);
        let description = HtmlStr::new(&self.description).with_len(10);
        let disabled = disabled_attr(self.poll_enabled);
        format!(
            "<span>{connected}</span>\
            <span{disabled}>{description}…</span>\
            <span class='{NAME}'>{self}</span>"
        )
    }

    /// Convert to status HTML
    fn to_html_status(&self) -> String {
        let connected = self.connected(true);
        let disabled = if self.poll_enabled { "" } else { " disabled" };
        let description = HtmlStr::new(&self.description);
        let comm_config = self.comm_config_desc();
        let config = HtmlStr::new(comm_config);
        format!(
            "<div class='row'>\
              <span>{connected}</span>\
              <span></span>\
              <span{disabled}'>{description}</span>\
            </div>\
            <div class='row'>\
              <span>{config}</span>\
            </div>"
        )
    }

    /// Convert to edit HTML
    fn to_html_edit(&self) -> String {
        let description = HtmlStr::new(&self.description);
        let uri = HtmlStr::new(&self.uri);
        let enabled = if self.poll_enabled { " checked" } else { "" };
        let comm_configs = self.comm_configs_html();
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
              {comm_configs}\
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
        if let Some(comm_config) = doc.select_parse::<String>("edit_config") {
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
