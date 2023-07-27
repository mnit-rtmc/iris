// Copyright (C) 2022-2023  Minnesota Department of Transportation
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
use crate::item::ItemState;
use crate::resource::{disabled_attr, Card, View, EDIT_BUTTON, NAME};
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal};
use serde::{Deserialize, Serialize};
use std::fmt;

/// Detector
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct Detector {
    pub name: String,
    pub label: Option<String>,
    pub notes: Option<String>,
    pub controller: Option<String>,
    // full attributes
    pub pin: Option<u32>,
    pub lane_code: Option<String>,
    pub lane_number: Option<u16>,
    pub abandoned: Option<bool>,
    pub force_fail: Option<bool>,
    pub auto_fail: Option<bool>,
    pub field_length: Option<f32>,
    pub fake: Option<String>,
}

type DetectorAnc = DeviceAnc<Detector>;

impl Detector {
    pub const RESOURCE_N: &'static str = "detector";

    /// Get the item state
    fn item_state(&self, anc: &DetectorAnc) -> ItemState {
        anc.item_state_opt(self).unwrap_or(ItemState::Available)
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &DetectorAnc) -> String {
        let item_state = self.item_state(anc);
        let label = HtmlStr::new(&self.label);
        let enabled = self.controller.is_some()
            || self
                .label
                .as_ref()
                .filter(|lbl| lbl.ends_with('G'))
                .is_some();
        let disabled = disabled_attr(enabled);
        format!(
            "<div class='{NAME} end'>{self} {item_state}</div>\
            <div class='info fill{disabled}'>{label}</div>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &DetectorAnc) -> String {
        let label = HtmlStr::new(&self.label).with_len(20);
        let ctrl_button = anc.controller_button();
        format!(
            "<div class='row'>\
              <span class='info'>{label}</span>\
            </div>\
            <div class='row'>\
              {ctrl_button}\
              {EDIT_BUTTON}\
            </div>"
        )
    }

    /// Convert to Edit HTML
    fn to_html_edit(&self) -> String {
        let controller = HtmlStr::new(&self.controller);
        let pin = OptVal(self.pin);
        format!(
            "<div class='row'>\
               <label for='controller'>Controller</label>\
               <input id='controller' maxlength='20' size='20' \
                      value='{controller}'>\
             </div>\
             <div class='row'>\
               <label for='pin'>Pin</label>\
               <input id='pin' type='number' min='1' max='104' \
                      size='8' value='{pin}'>\
             </div>"
        )
    }
}

impl fmt::Display for Detector {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl Device for Detector {
    /// Get controller
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Card for Detector {
    type Ancillary = DetectorAnc;

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &DetectorAnc) -> bool {
        self.name.contains_lower(search)
            || self.label.contains_lower(search)
            || self.item_state(anc).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &DetectorAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Compact => self.to_html_compact(anc),
            View::Status(_) => self.to_html_status(anc),
            View::Edit => self.to_html_edit(),
            _ => unreachable!(),
        }
    }

    /// Get changed fields from Edit form
    fn changed_fields(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }
}
