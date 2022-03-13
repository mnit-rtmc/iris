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
use crate::controller::Controller;
use crate::error::Result;
use crate::resource::{disabled_attr, Card, NAME};
use crate::util::{Dom, HtmlStr, OptVal};
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::fmt;
use wasm_bindgen::JsValue;
use web_sys::Document;

/// Alarm
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct Alarm {
    pub name: String,
    pub description: String,
    pub controller: Option<String>,
    pub state: bool,
    pub pin: Option<u32>,
    pub trigger_time: Option<String>,
}

impl Alarm {
    /// Get the alarm state to display
    fn state(&self, long: bool) -> &'static str {
        match (self.controller.is_some(), self.state, long) {
            (true, false, false) => "👍",
            (true, false, true) => "clear 👍",
            (true, true, false) => "😧",
            (true, true, true) => "triggered 😧",
            (false, _, true) => "unknown ❓",
            _ => "❓",
        }
    }
}

impl fmt::Display for Alarm {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl Card for Alarm {
    const TNAME: &'static str = "Alarm";
    const ENAME: &'static str = "🚨 Alarm";
    const UNAME: &'static str = "alarm";
    const HAS_STATUS: bool = true;

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str) -> bool {
        self.description.to_lowercase().contains(search)
            || self.name.to_lowercase().contains(search)
            || self.state(true).contains(search)
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let state = self.state(false);
        let description = HtmlStr::new(&self.description);
        let disabled = disabled_attr(self.controller.is_some());
        format!(
            "<span>{state}</span>\
            <span{disabled}>{description}</span>\
            <span class='{NAME}'>{self}</span>"
        )
    }

    /// Convert to status HTML
    fn to_html_status(&self) -> String {
        let tname = Controller::TNAME;
        let description = HtmlStr::new(&self.description);
        let state = self.state(true);
        let controller = HtmlStr::new(self.controller.as_ref());
        let trigger_time = self.trigger_time.as_deref().unwrap_or("-");
        format!(
            "<div class='row'>\
              <span class='info'>{description}</span>\
              <span class='info'>{state}</span>\
            </div>\
            <div class='row'>\
              <label>Controller</label>\
              <button class='go_link' type='button' \
                      data-link='{controller}' data-type='{tname}'>🖇️</button>\
            </div>\
            <div class='row'>\
              <span>Triggered</span>\
              <span class='info'>{trigger_time}</span>\
            </div>"
        )
    }

    /// Convert to edit HTML
    fn to_html_edit(&self) -> String {
        let description = HtmlStr::new(&self.description);
        let controller = HtmlStr::new(self.controller.as_ref());
        let pin = OptVal(self.pin);
        format!(
            "<div class='row'>\
              <label for='edit_desc'>Description</label>\
              <input id='edit_desc' maxlength='24' size='24' \
                     value='{description}'/>\
             </div>\
             <div class='row'>\
               <label for='edit_ctrl'>Controller</label>\
               <input id='edit_ctrl' maxlength='20' size='20' \
                      value='{controller}'/>\
             </div>\
             <div class='row'>\
               <label for='edit_pin'>Pin</label>\
               <input id='edit_pin' type='number' min='1' max='104' \
                      size='8' value='{pin}'/>\
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
        let ctrl = doc
            .input_parse::<String>("edit_ctrl")
            .filter(|c| !c.is_empty());
        if ctrl != val.controller {
            obj.insert("controller".to_string(), OptVal(ctrl).into());
        }
        let pin = doc.input_parse::<u32>("edit_pin");
        if pin != val.pin {
            obj.insert("pin".to_string(), OptVal(pin).into());
        }
        Ok(Value::Object(obj).to_string())
    }
}
