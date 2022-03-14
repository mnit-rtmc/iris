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
use crate::resource::{AncillaryData, Card};
use crate::util::{Dom, HtmlStr, OptVal};
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::fmt;
use wasm_bindgen::JsValue;
use web_sys::Document;

/// Cabinet Style
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct CabinetStyle {
    pub name: String,
    pub police_panel_pin_1: Option<u32>,
    pub police_panel_pin_2: Option<u32>,
    pub watchdog_reset_pin_1: Option<u32>,
    pub watchdog_reset_pin_2: Option<u32>,
    pub dip: Option<u32>,
}

/// Ancillary Cabinet Style data
#[derive(Debug, Default)]
pub struct CabinetStyleAnc;

impl AncillaryData for CabinetStyleAnc {
    type Resource = CabinetStyle;
}

impl fmt::Display for CabinetStyle {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl Card for CabinetStyle {
    const TNAME: &'static str = "Cabinet Style";
    const ENAME: &'static str = "🗄️ Cabinet Style";
    const UNAME: &'static str = "cabinet_style";

    type Ancillary = CabinetStyleAnc;

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, _res: &CabinetStyleAnc) -> bool {
        self.name.to_lowercase().contains(search)
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        format!("<span>{self}</span>")
    }

    /// Convert to edit HTML
    fn to_html_edit(&self, _anc: &CabinetStyleAnc) -> String {
        let police_panel_pin_1 = OptVal(self.police_panel_pin_1);
        let police_panel_pin_2 = OptVal(self.police_panel_pin_2);
        let watchdog_reset_pin_1 = OptVal(self.watchdog_reset_pin_1);
        let watchdog_reset_pin_2 = OptVal(self.watchdog_reset_pin_2);
        let dip = OptVal(self.dip);
        format!(
            "<div class='row'>\
              <label for='edit_pp1'>Police Panel Pin 1</label>\
              <input id='edit_pp1' type='number' min='1' max='104' \
                     size='8' value='{police_panel_pin_1}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_pp2'>Police Panel Pin 2</label>\
              <input id='edit_pp2' type='number' min='1' max='104' \
                     size='8' value='{police_panel_pin_2}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_wr1'>Watchdog Reset Pin 1</label>\
              <input id='edit_wr1' type='number' min='1' max='104' \
                     size='8' value='{watchdog_reset_pin_1}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_wr2'>Watchdog Reset Pin 2</label>\
              <input id='edit_wr2' type='number' min='1' max='104' \
                     size='8' value='{watchdog_reset_pin_2}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_dip'>Dip</label>\
              <input id='edit_dip' type='number' min='0' max='255' \
                     size='8' value='{dip}'/>\
            </div>"
        )
    }

    /// Get changed fields from Edit form
    fn changed_fields(doc: &Document, json: &JsValue) -> Result<String> {
        let val = Self::new(json)?;
        let mut obj = Map::new();
        let pin = doc.input_parse("edit_pp1");
        if pin != val.police_panel_pin_1 {
            obj.insert("police_panel_pin_1".to_string(), OptVal(pin).into());
        }
        let pin = doc.input_parse("edit_pp2");
        if pin != val.police_panel_pin_2 {
            obj.insert("police_panel_pin_2".to_string(), OptVal(pin).into());
        }
        let pin = doc.input_parse("edit_wr1");
        if pin != val.watchdog_reset_pin_1 {
            obj.insert("watchdog_reset_pin_1".to_string(), OptVal(pin).into());
        }
        let pin = doc.input_parse("edit_wr2");
        if pin != val.watchdog_reset_pin_2 {
            obj.insert("watchdog_reset_pin_2".to_string(), OptVal(pin).into());
        }
        let dip = doc.input_parse("edit_dip");
        if dip != val.dip {
            obj.insert("dip".to_string(), OptVal(dip).into());
        }
        Ok(Value::Object(obj).to_string())
    }
}
