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
use crate::util::{input_parse, HtmlStr};
use crate::{ElemCast, Result};
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use wasm_bindgen::JsValue;
use web_sys::{Document, HtmlInputElement};

/// Alarm
#[derive(Debug, Deserialize, Serialize)]
pub struct Alarm {
    pub name: String,
    pub description: String,
    pub controller: Option<String>,
    pub pin: u32,
    pub state: bool,
    pub trigger_time: Option<String>,
}

impl Card for Alarm {
    const TNAME: &'static str = "Alarm";
    const ENAME: &'static str = "⚠ Alarm";
    const HAS_STATUS: bool = true;
    const URI: &'static str = "/iris/api/alarm";

    fn is_match(&self, tx: &str) -> bool {
        self.description.to_lowercase().contains(tx)
            || self.name.to_lowercase().contains(tx)
            || {
                let state = if self.state { "triggered" } else { "clear" };
                state.contains(tx)
            }
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

    /// Convert to status HTML
    fn to_html_status(&self) -> String {
        let description = HtmlStr(&self.description);
        let state = if self.state {
            "triggered 😧"
        } else {
            "clear 🙂"
        };
        let trigger_time = self.trigger_time.as_deref().unwrap_or("-");
        format!(
            "<div class='row'>\
              <span>Description</span>\
              <span class='info'>{description}</span>\
            </div>\
            <div class='row'>\
              <span>State</span>\
              <span class='info'>{state}</span>\
            </div>\
            <div class='row'>\
              <span>Trigger Time</span>\
              <span class='info'>{trigger_time}</span>\
            </div>"
        )
    }

    /// Convert to edit HTML
    fn to_html_edit(&self) -> String {
        let description = HtmlStr(&self.description);
        let controller = HtmlStr(self.controller.as_ref());
        let pin = self.pin;
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
        let desc = doc.elem::<HtmlInputElement>("edit_desc")?.value();
        if desc != val.description {
            obj.insert("description".to_string(), Value::String(desc));
        }
        let ctrl = doc.elem::<HtmlInputElement>("edit_ctrl")?.value();
        let ctrl = if ctrl.is_empty() { None } else { Some(ctrl) };
        if ctrl != val.controller {
            obj.insert(
                "controller".to_string(),
                match ctrl {
                    Some(ctrl) => Value::String(ctrl),
                    None => Value::Null,
                },
            );
        }
        if let Some(pin) = input_parse::<u32>(doc, "edit_pin") {
            if pin != val.pin {
                obj.insert("pin".to_string(), Value::Number(pin.into()));
            }
        }
        Ok(Value::Object(obj).to_string())
    }
}
