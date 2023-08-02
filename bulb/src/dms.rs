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
use crate::error::Result;
use crate::item::{ItemState, ItemStates};
use crate::resource::{
    AncillaryData, Card, View, EDIT_BUTTON, LOC_BUTTON, NAME,
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
    error: Option<String>,
    reading: Option<i32>,
}

/// Power supply status
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct PowerSupply {
    description: String,
    supply_type: String,
    error: Option<String>,
    detail: String,
    voltage: Option<f32>,
}

/// Sign status
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct SignStatus {
    faults: Option<String>,
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

/// Stuck pixel bitmaps (Base64-encoded)
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct StuckPixels {
    off: Option<String>,
    on: Option<String>,
}

/// Dms
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct Dms {
    pub name: String,
    pub location: Option<String>,
    pub controller: Option<String>,
    pub notes: Option<String>,
    pub hashtags: Option<String>,
    pub msg_current: Option<String>,
    pub has_faults: Option<bool>,
    // full attributes
    pub pin: Option<u32>,
    pub sign_config: Option<String>,
    pub sign_detail: Option<String>,
    pub msg_sched: Option<String>,
    pub msg_user: Option<String>,
    pub geo_loc: Option<String>,
    pub status: Option<SignStatus>,
    pub stuck_pixels: Option<StuckPixels>,
}

/// Sign Message
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct SignMessage {
    pub name: String,
    pub sign_config: String,
    pub incident: Option<String>,
    pub multi: String,
    pub msg_owner: String,
    pub flash_beacon: bool,
    pub msg_priority: u32,
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

impl SignMessage {
    /// Get message owner
    fn owner(&self) -> &str {
        &self.msg_owner
    }

    /// Get "system" owner
    fn system(&self) -> Option<&str> {
        self.owner().split(';').nth(0)
    }

    /// Get "sources" owner
    fn sources(&self) -> Option<&str> {
        self.owner().split(';').nth(1)
    }

    /// Get "user" owner
    fn user(&self) -> Option<&str> {
        self.owner().split(';').nth(2)
    }

    /// Get item states
    fn item_states(&self) -> ItemStates {
        self.sources()
            .map(|src| {
                let mut states = ItemStates::default();
                if src.contains("blank") {
                    states = states.with(ItemState::Available);
                }
                if src.contains("operator") {
                    states = states.with(ItemState::Deployed);
                }
                if src.contains("schedule") {
                    states = states.with(ItemState::Planned);
                }
                if src.contains("external") {
                    states = states.with(ItemState::External);
                }
                states
            })
            .unwrap_or(ItemState::Unknown.into())
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str) -> bool {
        // checks are ordered by "most likely to be searched"
        self.multi.contains_lower(search)
            || self.user().is_some_and(|u| u.contains_lower(search))
            || self.system().is_some_and(|s| s.contains_lower(search))
    }
}

impl DmsAnc {
    /// Find a sign message
    fn sign_message(&self, msg: Option<&str>) -> Option<&SignMessage> {
        msg.and_then(|msg| {
            self.messages
                .as_ref()
                .and_then(|msgs| msgs.iter().find(|m| m.name == msg))
        })
    }

    /// Get message item states
    fn msg_states(&self, msg: Option<&str>) -> ItemStates {
        self.sign_message(msg)
            .map(|m| m.item_states())
            .unwrap_or(ItemState::Unknown.into())
    }
}

impl Dms {
    pub const RESOURCE_N: &'static str = "dms";

    /// Get item states
    fn item_states(&self, anc: &DmsAnc) -> ItemStates {
        let state = anc.dev.item_state(self);
        let mut states = match state {
            ItemState::Available => anc.msg_states(self.msg_current.as_deref()),
            _ => state.into(),
        };
        if self.has_faults.is_some_and(|f| f) {
            states = states.with(ItemState::Fault);
        }
        states
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &DmsAnc) -> String {
        let item_states = self.item_states(anc);
        let mut html =
            format!("<div class='{NAME} end'>{self} {item_states}</div>");
        if let Some(msg_current) = &self.msg_current {
            html.push_str("<img class='message' src='/iris/img/");
            html.push_str(msg_current);
            html.push_str(".gif'>");
        }
        html
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &DmsAnc, config: bool) -> String {
        let location = HtmlStr::new(&self.location).with_len(64);
        let mut status = format!("<div class='info fill'>{location}</div>");
        if let Some(msg_current) = &self.msg_current {
            status.push_str("<img class='message' src='/iris/img/");
            status.push_str(msg_current);
            status.push_str(".gif'>");
        }
        status.push_str("<div class='end'>");
        status.push_str(&self.item_states(anc).description());
        status.push_str("</div>");
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
            || self
                .notes
                .as_ref()
                .is_some_and(|n| n.contains_lower(search))
            || self
                .hashtags
                .as_ref()
                .is_some_and(|h| h.contains_lower(search))
            || self.item_states(anc).is_match(search)
            || anc
                .sign_message(self.msg_current.as_deref())
                .is_some_and(|m| m.is_match(search))
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
