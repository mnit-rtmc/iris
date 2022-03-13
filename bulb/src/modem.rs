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
use crate::resource::{disabled_attr, Card};
use crate::util::{Dom, HtmlStr, OptVal};
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::fmt;
use wasm_bindgen::JsValue;
use web_sys::Document;

/// Modem
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct Modem {
    pub name: String,
    pub uri: Option<String>,
    pub config: Option<String>,
    pub timeout_ms: Option<u32>,
    pub enabled: bool,
}

impl fmt::Display for Modem {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl Card for Modem {
    const TNAME: &'static str = "Modem";
    const ENAME: &'static str = "🖀 Modem";
    const UNAME: &'static str = "modem";

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str) -> bool {
        self.name.to_lowercase().contains(search)
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let disabled = disabled_attr(self.enabled);
        format!("<span{disabled}>{self}</span>")
    }

    /// Convert to edit HTML
    fn to_html_edit(&self) -> String {
        let uri = HtmlStr::new(self.uri.as_ref());
        let config = HtmlStr::new(self.config.as_ref());
        let timeout_ms = OptVal(self.timeout_ms);
        let enabled = if self.enabled { " checked" } else { "" };
        format!(
            "<div class='row'>\
              <label for='edit_uri'>URI</label>\
              <input id='edit_uri' maxlength='64' size='30' \
                     value='{uri}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_config'>Config</label>\
              <input id='edit_config' maxlength='64' size='28' \
                     value='{config}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_timeout'>Timeout (ms)</label>\
              <input id='edit_timeout' type='number' min='0' size='8' \
                     max='90000' value='{timeout_ms}'/>\
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
        let uri = doc.input_parse::<String>("edit_uri");
        if uri != val.uri {
            obj.insert("uri".to_string(), OptVal(uri).into());
        }
        let config = doc.input_parse::<String>("edit_config");
        if config != val.config {
            obj.insert("config".to_string(), OptVal(config).into());
        }
        let timeout_ms = doc.input_parse::<u32>("edit_timeout");
        if timeout_ms != val.timeout_ms {
            obj.insert("timeout_ms".to_string(), OptVal(timeout_ms).into());
        }
        if let Some(enabled) = doc.input_bool("edit_enabled") {
            if enabled != val.enabled {
                obj.insert("enabled".to_string(), Value::Bool(enabled));
            }
        }
        Ok(Value::Object(obj).to_string())
    }
}
