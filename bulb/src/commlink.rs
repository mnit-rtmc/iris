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
use crate::util::{Dom, HtmlStr, OptVal};
use crate::{comm_configs_html, get_comm_config_desc, Result};
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
    pub poll_enabled: bool,
    pub connected: bool,
    pub uri: Option<String>,
    pub comm_config: Option<String>,
}

impl CommLink {
    /// Get comm config description
    fn comm_config_desc(&self) -> String {
        match self.comm_config.as_ref() {
            Some(cc) => get_comm_config_desc(cc),
            _ => None,
        }
        .unwrap_or_else(|| "".to_string())
    }

    /// Get connected state to display
    fn connected(&self, long: bool) -> &'static str {
        match (self.poll_enabled, self.connected, long) {
            (true, true, false) => "👍",
            (true, true, true) => "👍 online",
            (true, false, false) => "🔌",
            (true, false, true) => "🔌 offline",
            (false, _, false) => "❓",
            (false, _, true) => "❓ disabled",
        }
    }
}

impl Card for CommLink {
    const TNAME: &'static str = "Comm Link";
    const ENAME: &'static str = "🔗 Comm Link";
    const HAS_STATUS: bool = true;
    const URI: &'static str = "/iris/api/comm_link";

    fn is_match(&self, tx: &str) -> bool {
        self.description.to_lowercase().contains(tx)
            || self.name.to_lowercase().contains(tx)
            || self.comm_config_desc().to_lowercase().contains(tx)
            || self
                .uri
                .as_deref()
                .unwrap_or("")
                .to_lowercase()
                .contains(tx)
            || self.connected(true).contains(tx)
    }

    fn name(&self) -> &str {
        &self.name
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let description = HtmlStr::new(&self.description).with_len(10);
        let connected = self.connected(false);
        let name = HtmlStr::new(&self.name);
        let disabled = disabled_attr(self.poll_enabled);
        format!(
            "<span{disabled}>{description}…</span>\
            <span>{connected}</span>\
            <span class='{NAME}'>{name}</span>"
        )
    }

    /// Convert to status HTML
    fn to_html_status(&self) -> String {
        let description = HtmlStr::new(&self.description);
        let comm_config = self.comm_config_desc();
        let config = HtmlStr::new(&comm_config);
        let connected = self.connected(true);
        let disabled = if self.poll_enabled { "" } else { " disabled" };
        format!(
            "<div class='row'>\
              <span class='info{disabled}'>{description}</span>\
            </div>\
            <div class='row'>\
              <span>{config}</span>\
              <span>    </span>\
              <span class='info'>{connected}</span>\
            </div>"
        )
    }

    /// Convert to edit HTML
    fn to_html_edit(&self) -> String {
        let description = HtmlStr::new(&self.description);
        let uri = HtmlStr::new(self.uri.as_ref());
        let enabled = if self.poll_enabled { " checked" } else { "" };
        let comm_configs = comm_configs_html(self.comm_config.as_deref());
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
        let uri = doc.input_parse::<String>("edit_uri");
        if uri != val.uri {
            obj.insert("uri".to_string(), OptVal(uri).into());
        }
        let comm_config = doc.select_parse::<String>("edit_config");
        if comm_config != val.comm_config {
            obj.insert("comm_config".to_string(), OptVal(comm_config).into());
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
