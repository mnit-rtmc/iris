// Copyright (C) 2022-2024  Minnesota Department of Transportation
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
use crate::card::{
    inactive_attr, AncillaryData, Card, View, EDIT_BUTTON, NAME,
};
use crate::controller::Controller;
use crate::error::Result;
use crate::fetch::Uri;
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal};
use resources::Res;
use serde::{Deserialize, Serialize};
use std::fmt;
use std::iter::once;
use wasm_bindgen::JsValue;

/// Gate arm states
#[derive(Debug, Deserialize, Serialize)]
pub struct GateArmState {
    pub id: u32,
    pub description: String,
}

/// Gate Arm
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct GateArm {
    pub name: String,
    pub location: Option<String>,
    pub controller: Option<String>,
    pub notes: Option<String>,
    pub arm_state: u32,
    // secondary attributes
    pub pin: Option<u32>,
}

/// Ancillary gate arm data
#[derive(Debug, Default)]
pub struct GateArmAnc {
    pub controller: Option<Controller>,
    pub states: Option<Vec<GateArmState>>,
}

/// Get arm warn state
pub fn warn_state(arm_state: u32) -> &'static str {
    match arm_state {
        1 => "‼️",    // fault
        2 => "⚠️",    // opening
        3 => "✔️",    // open
        4 => "⚠️",    // warn_close
        5 => "⚠️ ⛔", // closing
        6 => "⛔",   // closed
        _ => "❓",   // unknown
    }
}

impl GateArmAnc {
    fn controller_button(&self) -> String {
        match &self.controller {
            Some(ctrl) => ctrl.button_html(),
            None => "<span></span>".into(),
        }
    }

    /// Get arm state description
    fn arm_state(&self, pri: &GateArm) -> &str {
        if let Some(states) = &self.states {
            for state in states {
                if pri.arm_state == state.id {
                    return &state.description;
                }
            }
        }
        ""
    }
}

const GATE_ARM_STATE_URI: &str = "/iris/lut/gate_arm_state";

impl AncillaryData for GateArmAnc {
    type Primary = GateArm;

    /// Get URI iterator
    fn uri_iter(
        &self,
        pri: &GateArm,
        view: View,
    ) -> Box<dyn Iterator<Item = Uri>> {
        match (view, &pri.controller()) {
            (View::Status(_), Some(ctrl)) => {
                let mut uri = Uri::from("/iris/api/controller/");
                uri.push(ctrl);
                Box::new(once(uri))
            }
            _ => Box::new(once(GATE_ARM_STATE_URI.into())),
        }
    }

    /// Put ancillary data
    fn set_data(
        &mut self,
        _pri: &GateArm,
        uri: Uri,
        data: JsValue,
    ) -> Result<bool> {
        if uri.as_str() == GATE_ARM_STATE_URI {
            self.states = Some(serde_wasm_bindgen::from_value(data)?);
        } else {
            self.controller = Some(serde_wasm_bindgen::from_value(data)?);
        }
        Ok(false)
    }
}

impl GateArm {
    /// Get controller
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let warn = warn_state(self.arm_state);
        let inactive = inactive_attr(self.controller.is_some());
        let location = HtmlStr::new(&self.location).with_len(32);
        format!(
            "<div class='{NAME} end'>{self} {warn}</div>\
            <div class='info fill{inactive}'>{location}</div>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &GateArmAnc) -> String {
        let location = HtmlStr::new(&self.location).with_len(64);
        let warn = warn_state(self.arm_state);
        let arm_state = HtmlStr::new(anc.arm_state(self));
        let ctrl_button = anc.controller_button();
        format!(
            "<div class='info'>{location}</div>\
            <div>{warn} {arm_state}</div>\
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

impl fmt::Display for GateArm {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl Card for GateArm {
    type Ancillary = GateArmAnc;

    /// Display name
    const DNAME: &'static str = "⫬ Gate Arm";

    /// Get the resource
    fn res() -> Res {
        Res::GateArm
    }

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &GateArmAnc) -> bool {
        self.name.contains_lower(search) || anc.arm_state(self).contains(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &GateArmAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Compact => self.to_html_compact(),
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
