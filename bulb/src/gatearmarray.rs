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
use crate::card::{AncillaryData, Card, View};
use crate::error::Result;
use crate::fetch::Uri;
use crate::gatearm::item_state;
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
            View::Search | View::Control => {
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
        let item = item_state(self.arm_state);
        let location = HtmlStr::new(&self.location).with_len(32);
        format!(
            "<div class='title row'>{name} {item}</div>\
            <div class='info fill'>{location}</div>"
        )
    }

    /// Convert to Control HTML
    fn to_html_control(&self) -> String {
        let title = self.title(View::Control);
        let location = HtmlStr::new(&self.location).with_len(64);
        let item = item_state(self.arm_state);
        let desc = item.description();
        format!(
            "{title}\
            <div class='info'>{location}</div>\
            <div class='info'>{item} {desc}</div>"
        )
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
    fn is_match(&self, search: &str, _anc: &GateArmArrayAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || item_state(self.arm_state).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &GateArmArrayAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Control => self.to_html_control(),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_fields(&self) -> String {
        let fields = Fields::new();
        fields.into_value().to_string()
    }
}
