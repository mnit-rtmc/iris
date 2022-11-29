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
use crate::item::ItemState;
use crate::resource::{
    disabled_attr, AncillaryData, Card, View, EDIT_BUTTON, LOC_BUTTON, NAME,
};
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal};
use serde::{Deserialize, Serialize};
use std::borrow::{Borrow, Cow};
use std::fmt;
use wasm_bindgen::JsValue;

/// Photocell status
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct Photocell {
    description: String,
    status: String,
    reading: i32,
}

/// Power supply status
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct PowerSupply {
    description: String,
    supply_type: String,
    power_status: String,
    detail: String,
    voltage: Option<f32>,
}

/// Sign status
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct SignStatus {
    photocells: Option<Vec<Photocell>>,
    light_output: Option<u32>,
    power_supplies: Option<Vec<PowerSupply>>,
    cabinet_temp_min: Option<i32>,
    cabinet_temp_max: Option<i32>,
    ambient_temp_min: Option<i32>,
    ambient_temp_max: Option<i32>,
    housing_temp_min: Option<i32>,
    housing_temp_max: Option<i32>,
    ldc_pot_base: Option<i32>,
    pixel_current_low: Option<i32>,
    pixel_current_high: Option<i32>,
}

/// Dms
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct Dms {
    pub name: String,
    pub location: Option<String>,
    pub controller: Option<String>,
    // full attributes
    pub geo_loc: Option<String>,
    pub pin: Option<u32>,
    pub msg_current: Option<String>,
    pub status: Option<SignStatus>,
}

/// Sign Message
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct SignMessage {
    pub name: String,
    pub sign_config: String,
    pub incident: Option<String>,
    pub multi: String,
    pub beacon_enabled: bool,
    pub msg_combining: String,
    pub msg_priority: u32,
    pub sources: String,
    pub owner: Option<String>,
    pub duration: Option<u32>,
}

/// DMS ancillary data
#[derive(Default)]
pub struct DmsAnc {
    dev: DeviceAnc<Dms>,
    messages: Option<Vec<SignMessage>>,
}

const SIGN_MSG_URI: &str = "/iris/sign_message";

impl AncillaryData for DmsAnc {
    type Primary = Dms;

    /// Get next ancillary data URI
    fn next_uri(&self, view: View, pri: &Self::Primary) -> Option<Cow<str>> {
        self.dev
            .next_uri(view, pri)
            .or_else(|| match (view, &self.messages) {
                (View::Compact | View::Search | View::Status(_), None) => {
                    Some(SIGN_MSG_URI.into())
                }
                _ => None,
            })
    }

    /// Set ancillary JSON data
    fn set_json(
        &mut self,
        view: View,
        pri: &Self::Primary,
        json: JsValue,
    ) -> Result<()> {
        if let Some(uri) = self.next_uri(view, pri) {
            match uri.borrow() {
                SIGN_MSG_URI => {
                    self.messages = Some(serde_wasm_bindgen::from_value(json)?);
                }
                _ => self.dev.set_json(view, pri, json)?,
            }
        }
        Ok(())
    }
}

impl DmsAnc {
    /// Get message sources
    fn sources(&self, msg: Option<&str>) -> Option<&str> {
        match (&self.messages, msg) {
            (Some(messages), Some(msg)) => messages
                .iter()
                .find(|m| m.name == msg)
                .map(|m| &m.sources[..]),
            _ => None,
        }
    }

    /// Get item state
    fn item_state(&self, msg: Option<&str>) -> ItemState {
        self.sources(msg)
            .map(|src| {
                if src.contains("schedule") {
                    ItemState::Scheduled
                } else if !src.contains("blank") {
                    ItemState::Deployed
                } else {
                    ItemState::Available
                }
            })
            .unwrap_or(ItemState::Available)
    }
}

impl Dms {
    pub const RESOURCE_N: &'static str = "dms";

    /// Get item state
    fn item_state(&self, anc: &DmsAnc) -> ItemState {
        if anc.dev.is_active(self) {
            anc.item_state(self.msg_current.as_deref())
        } else {
            ItemState::Unknown
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &DmsAnc) -> String {
        let comm_state = anc.dev.comm_state(self);
        let item_state = self.item_state(anc);
        let location = HtmlStr::new(&self.location);
        let disabled = disabled_attr(self.controller.is_some());
        format!(
            "<div class='{NAME} end'>{comm_state} {self} {item_state}</div>\
            <div class='info fill{disabled}'>{location}</div>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &DmsAnc, config: bool) -> String {
        let location = HtmlStr::new(&self.location).with_len(64);
        let comm_state = anc.dev.comm_state(self);
        let comm_desc = comm_state.description();
        let item_state = self.item_state(anc);
        let item_desc = item_state.description();
        let mut status = format!(
            "<div class='info fill'>{location}</div>\
            <div class='row'>\
              <span>{comm_state} {comm_desc}</span>\
              <span>{item_state} {item_desc}</span>\
            </div>"
        );
        if let Some(msg_current) = &self.msg_current {
            status.push_str("<img class='message' src='/iris/img/");
            status.push_str(msg_current);
            status.push_str(".gif'>");
        }
        if config {
            status.push_str("<div class='row'>");
            status.push_str(&anc.dev.controller_button());
            status.push_str(LOC_BUTTON);
            status.push_str(EDIT_BUTTON);
            status.push_str("</div>");
        }
        status
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

impl fmt::Display for Dms {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl Device for Dms {
    /// Get controller
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Card for Dms {
    type Ancillary = DmsAnc;

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
    fn is_match(&self, search: &str, anc: &DmsAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || anc.dev.comm_state(self).is_match(search)
            || self.item_state(anc).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &DmsAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Compact => self.to_html_compact(anc),
            View::Status(config) => self.to_html_status(anc, config),
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
