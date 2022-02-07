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
use crate::util::{Dom, HtmlStr};
use crate::{protocols_html, Result};
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use wasm_bindgen::JsValue;
use web_sys::Document;

/// Comm configuration
#[derive(Debug, Deserialize, Serialize)]
pub struct CommConfig {
    pub name: String,
    pub description: String,
    pub protocol: u32,
    pub modem: bool,
    pub timeout_ms: u32,
    pub poll_period_sec: u32,
    pub long_poll_period_sec: u32,
    pub idle_disconnect_sec: u32,
    pub no_response_disconnect_sec: u32,
}

impl Card for CommConfig {
    const TNAME: &'static str = "Comm Config";
    const ENAME: &'static str = "📡 Comm Config";
    const URI: &'static str = "/iris/api/comm_config";

    fn is_match(&self, tx: &str) -> bool {
        self.description.to_lowercase().contains(tx)
            || self.name.to_lowercase().contains(tx)
    }

    fn name(&self) -> &str {
        &self.name
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let description = HtmlStr(&self.description);
        let name = HtmlStr(&self.name);
        format!(
            "<span>{description}</span>\
            <span class='{NAME}'>{name}</span>"
        )
    }

    /// Convert to edit HTML
    fn to_html_edit(&self) -> String {
        let description = HtmlStr(&self.description);
        let protocols = protocols_html(self.protocol);
        let modem = if self.modem { " checked" } else { "" };
        let timeout_ms = self.timeout_ms;
        let poll_period_sec = self.poll_period_sec;
        let long_poll_period_sec = self.long_poll_period_sec;
        let idle_disconnect_sec = self.idle_disconnect_sec;
        let no_response_disconnect_sec = self.no_response_disconnect_sec;
        format!(
            "<div class='row'>\
              <label for='edit_desc'>Description</label>\
              <input id='edit_desc' maxlength='20' size='20' \
                     value='{description}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_protocol'>Protocol</label>\
              {protocols}\
            </div>\
            <div class='row'>\
              <label for='edit_modem'>Modem</label>\
              <input id='edit_modem' type='checkbox'{modem}/>\
            </div>\
            <div class='row'>\
              <label for='edit_timeout'>Timeout (ms)</label>\
              <input id='edit_timeout' type='number' min='0' size='8' \
                     max='20000' value='{timeout_ms}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_poll'>Poll Period (s)</label>\
              <input id='edit_poll' type='number' min='0' \
                     size='8' value='{poll_period_sec}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_long'>Long Poll Period (s)</label>\
              <input id='edit_long' type='number' min='0' \
                     size='8' value='{long_poll_period_sec}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_idle'>Idle Disconnect (s)</label>\
              <input id='edit_idle' type='number' min='0' size='8' \
                     value='{idle_disconnect_sec}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_no_resp'>No Response Disconnect (s)</label>\
              <input id='edit_no_resp' type='number' min='0' size='8' \
                     value='{no_response_disconnect_sec}'/>\
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
        if let Some(protocol) = doc.select_parse::<u32>("edit_protocol") {
            if protocol != val.protocol {
                obj.insert(
                    "protocol".to_string(),
                    Value::Number(protocol.into()),
                );
            }
        }
        if let Some(modem) = doc.input_bool("edit_modem") {
            if modem != val.modem {
                obj.insert("modem".to_string(), Value::Bool(modem));
            }
        }
        if let Some(timeout_ms) = doc.input_parse::<u32>("edit_timeout") {
            if timeout_ms != val.timeout_ms {
                obj.insert(
                    "timeout_ms".to_string(),
                    Value::Number(timeout_ms.into()),
                );
            }
        }
        if let Some(poll_period_sec) = doc.input_parse::<u32>("edit_poll") {
            if poll_period_sec != val.poll_period_sec {
                obj.insert(
                    "poll_period_sec".to_string(),
                    Value::Number(poll_period_sec.into()),
                );
            }
        }
        if let Some(long_poll_period_sec) = doc.input_parse::<u32>("edit_long")
        {
            if long_poll_period_sec != val.long_poll_period_sec {
                obj.insert(
                    "long_poll_period_sec".to_string(),
                    Value::Number(long_poll_period_sec.into()),
                );
            }
        }
        if let Some(idle_disconnect_sec) = doc.input_parse::<u32>("edit_idle") {
            if idle_disconnect_sec != val.idle_disconnect_sec {
                obj.insert(
                    "idle_disconnect_sec".to_string(),
                    Value::Number(idle_disconnect_sec.into()),
                );
            }
        }
        if let Some(no_response_disconnect_sec) =
            doc.input_parse::<u32>("edit_no_resp")
        {
            if no_response_disconnect_sec != val.no_response_disconnect_sec {
                obj.insert(
                    "no_response_disconnect_sec".to_string(),
                    Value::Number(no_response_disconnect_sec.into()),
                );
            }
        }
        Ok(Value::Object(obj).to_string())
    }
}
