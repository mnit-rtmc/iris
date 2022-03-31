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
use crate::resource::{
    disabled_attr, Card, View, EDIT_BUTTON, LOC_BUTTON, NAME,
};
use crate::util::{ContainsLower, Doc, HtmlStr, OptVal};
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::fmt;

/// Camera
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct Camera {
    pub name: String,
    pub cam_num: Option<u32>,
    pub location: Option<String>,
    pub controller: Option<String>,
    // full attributes
    pub geo_loc: Option<String>,
    pub pin: Option<u32>,
}

type CameraAnc = DeviceAnc<Camera>;

impl Camera {
    pub const RESOURCE_N: &'static str = "camera";

    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let location = HtmlStr::new(&self.location).with_len(12);
        let disabled = disabled_attr(self.controller.is_some());
        format!(
            "<span{disabled}>{location}</span>\
            <span class='{NAME}'>{self}</span>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &CameraAnc) -> String {
        let location = HtmlStr::new(&self.location).with_len(64);
        let ctrl_button = anc.controller_button();
        format!(
            "<div class='row'>\
              <span class='info'>{location}</span>\
            </div>\
            <div class='row'>\
              {ctrl_button}\
              {LOC_BUTTON}\
              {EDIT_BUTTON}\
            </div>"
        )
    }

    /// Convert to Edit HTML
    fn to_html_edit(&self) -> String {
        let cam_num = OptVal(self.cam_num);
        let controller = HtmlStr::new(&self.controller);
        let pin = OptVal(self.pin);
        format!(
            "<div class='row'>\
              <label for='edit_num'>Cam Num</label>\
              <input id='edit_num' type='number' min='1' max='9999' \
                     size='8' value='{cam_num}'/>\
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
}

impl fmt::Display for Camera {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl Device for Camera {
    /// Get controller
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Card for Camera {
    type Ancillary = CameraAnc;

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
    fn is_match(&self, search: &str, _anc: &CameraAnc) -> bool {
        self.name.contains_lower(search) || self.location.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &CameraAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Compact => self.to_html_compact(),
            View::Status => self.to_html_status(anc),
            View::Edit => self.to_html_edit(),
            _ => unreachable!(),
        }
    }

    /// Get changed fields from Edit form
    fn changed_fields(&self, doc: &Doc) -> String {
        let mut obj = Map::new();
        let cam_num = doc.input_parse::<u32>("edit_num");
        if cam_num != self.cam_num {
            obj.insert("cam_num".to_string(), OptVal(cam_num).into());
        }
        let ctrl = doc.input_option_string("edit_ctrl");
        if ctrl != self.controller {
            obj.insert("controller".to_string(), OptVal(ctrl).into());
        }
        let pin = doc.input_parse::<u32>("edit_pin");
        if pin != self.pin {
            obj.insert("pin".to_string(), OptVal(pin).into());
        }
        Value::Object(obj).to_string()
    }
}
