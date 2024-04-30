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
use crate::card::{AncillaryData, Card, View, EDIT_BUTTON, LOC_BUTTON, NAME};
use crate::error::Result;
use crate::fetch::Uri;
use crate::gatearm::warn_state;
use crate::util::{ContainsLower, Fields, HtmlStr};
use resources::Res;
use serde::{Deserialize, Serialize};
use std::borrow::Cow;
use std::iter::{empty, once};
use wasm_bindgen::JsValue;

/// Gate arm states
///
/// FIXME: share with gatearm module
#[derive(Debug, Deserialize, Serialize)]
pub struct GateArmState {
    pub id: u32,
    pub description: String,
}

/// Gate Arm Array
#[derive(Debug, Default, Deserialize, Serialize, PartialEq)]
pub struct GateArmArray {
    pub name: String,
    pub location: Option<String>,
    pub notes: Option<String>,
    pub arm_state: u32,
    pub interlock: u32,
    // secondary attributes
    pub geo_loc: Option<String>,
}

/// Ancillary gate arm array data
#[derive(Debug, Default)]
pub struct GateArmArrayAnc {
    pub states: Option<Vec<GateArmState>>,
}

impl GateArmArrayAnc {
    /// Get arm state description
    fn arm_state(&self, pri: &GateArmArray) -> &str {
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

impl AncillaryData for GateArmArrayAnc {
    type Primary = GateArmArray;

    /// Get ancillary URI iterator
    fn uri_iter(
        &self,
        _pri: &GateArmArray,
        view: View,
    ) -> Box<dyn Iterator<Item = Uri>> {
        match view {
            View::Search | View::Status(_) => {
                Box::new(once(GATE_ARM_STATE_URI.into()))
            }
            _ => Box::new(empty()),
        }
    }

    /// Put ancillary data
    fn set_data(
        &mut self,
        _pri: &GateArmArray,
        _uri: Uri,
        data: JsValue,
    ) -> Result<bool> {
        self.states = Some(serde_wasm_bindgen::from_value(data)?);
        Ok(false)
    }
}

impl GateArmArray {
    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let name = HtmlStr::new(self.name());
        let warn = warn_state(self.arm_state);
        let location = HtmlStr::new(&self.location).with_len(32);
        format!(
            "<div class='{NAME} end'>{name} {warn}</div>\
            <div class='info fill'>{location}</div>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &GateArmArrayAnc, config: bool) -> String {
        let location = HtmlStr::new(&self.location).with_len(64);
        let warn = warn_state(self.arm_state);
        let arm_state = HtmlStr::new(anc.arm_state(self));
        let mut status = format!(
            "<div class='info'>{location}</div>\
            <div class='info'>{warn} {arm_state}</div>"
        );
        if config {
            status.push_str("<div class='row'>");
            status.push_str("<span></span>");
            status.push_str(LOC_BUTTON);
            status.push_str(EDIT_BUTTON);
            status.push_str("</div>");
        }
        status
    }

    /// Convert to Edit HTML
    fn to_html_edit(&self) -> String {
        String::new()
    }
}

impl Card for GateArmArray {
    type Ancillary = GateArmArrayAnc;

    /// Display name
    const DNAME: &'static str = "⫭⫬ Gate Arm Array";

    /// Get the resource
    fn res() -> Res {
        Res::GateArmArray
    }

    /// Get the name
    fn name(&self) -> Cow<str> {
        Cow::Borrowed(&self.name)
    }

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
    fn is_match(&self, search: &str, anc: &GateArmArrayAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || anc.arm_state(self).contains(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &GateArmArrayAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Status(config) => self.to_html_status(anc, config),
            View::Edit => self.to_html_edit(),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Edit form
    fn changed_fields(&self) -> String {
        let fields = Fields::new();
        fields.into_value().to_string()
    }
}
