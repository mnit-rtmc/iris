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
use crate::device::{Device, DeviceAnc};
use crate::error::Result;
use crate::resource::{disabled_attr, Card, NAME};
use crate::util::{ContainsLower, Dom, HtmlStr, OptVal};
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::fmt;
use wasm_bindgen::JsValue;
use web_sys::Document;

/// Lane Marking
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct LaneMarking {
    pub name: String,
    pub location: Option<String>,
    pub notes: String,
    pub controller: Option<String>,
    pub deployed: bool,
    // full attributes
    pub geo_loc: Option<String>,
    pub pin: Option<u32>,
}

type LaneMarkingAnc = DeviceAnc<LaneMarking>;

impl LaneMarking {
    pub const RESOURCE_N: &'static str = "lane_marking";
}

impl fmt::Display for LaneMarking {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl Device for LaneMarking {
    /// Get controller
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Card for LaneMarking {
    const HAS_STATUS: bool = true;

    type Ancillary = LaneMarkingAnc;

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Get geo location name
    fn geo_loc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, _anc: &LaneMarkingAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || self.notes.contains_lower(search)
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let location = HtmlStr::new(&self.location).with_len(12);
        let disabled = disabled_attr(self.controller.is_some());
        format!(
            "<span{disabled}>{location}</span>\
            <span class='{NAME}'>{self}</span>"
        )
    }

    /// Convert to status HTML
    fn to_html_status(&self, _anc: &LaneMarkingAnc) -> String {
        let location = HtmlStr::new(&self.location).with_len(64);
        format!(
            "<div class='row'>\
              <span class='info'>{location}</span>\
            </div>"
        )
    }

    /// Convert to edit HTML
    fn to_html_edit(&self, anc: &LaneMarkingAnc) -> String {
        let ctrl_loc = anc.controller_loc_html();
        let notes = HtmlStr::new(&self.notes);
        let controller = HtmlStr::new(&self.controller);
        let pin = OptVal(self.pin);
        format!(
            "{ctrl_loc}\
            <div class='row'>\
              <label for='edit_notes'>Notes</label>\
              <textarea id='edit_notes' maxlength='128' rows='2' \
                        cols='24'>{notes}</textarea>\
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
        if let Some(notes) = doc.text_area_parse::<String>("edit_notes") {
            if notes != val.notes {
                obj.insert("notes".to_string(), Value::String(notes));
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
