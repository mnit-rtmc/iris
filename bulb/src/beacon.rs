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
use crate::fetch::Uri;
use crate::item::{ItemState, ItemStates};
use crate::resource::{
    disabled_attr, AncillaryData, Card, View, EDIT_BUTTON, LOC_BUTTON, NAME,
};
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal, TextArea};
use serde::{Deserialize, Serialize};
use std::fmt;
use std::iter::once;
use wasm_bindgen::JsValue;

/// Beacon States
#[derive(Debug, Deserialize, Serialize)]
pub struct BeaconState {
    pub id: u32,
    pub description: String,
}

/// Beacon
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct Beacon {
    pub name: String,
    pub location: Option<String>,
    pub message: String,
    pub notes: String,
    pub controller: Option<String>,
    pub state: u32,
    // full attributes
    pub geo_loc: Option<String>,
    pub pin: Option<u32>,
    pub verify_pin: Option<u32>,
    pub ext_mode: Option<bool>,
}

/// Beacon ancillary data
#[derive(Default)]
pub struct BeaconAnc {
    dev: DeviceAnc<Beacon>,
    states: Option<Vec<BeaconState>>,
}

const BEACON_STATE_URI: &str = "/iris/beacon_state";

impl AncillaryData for BeaconAnc {
    type Primary = Beacon;

    /// Get ancillary URI iterator
    fn uri_iter(
        &self,
        pri: &Self::Primary,
        view: View,
    ) -> Box<dyn Iterator<Item = Uri>> {
        Box::new(
            once(BEACON_STATE_URI.into()).chain(self.dev.uri_iter(pri, view)),
        )
    }

    /// Set ancillary data
    fn set_data(
        &mut self,
        pri: &Self::Primary,
        uri: Uri,
        data: JsValue,
    ) -> Result<bool> {
        if uri.as_str() == BEACON_STATE_URI {
            self.states = Some(serde_wasm_bindgen::from_value(data)?);
        } else {
            self.dev.set_data(pri, uri, data)?;
        }
        Ok(false)
    }
}

impl fmt::Display for Beacon {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

/// Flashing state class names
const CLASS_FLASHING: &str = "flashing";
const CLASS_NOT_FLASHING: &str = "not-flashing";

impl Beacon {
    pub const RESOURCE_N: &'static str = "beacon";

    /// Check if beacon is flashing
    fn flashing(&self) -> bool {
        // 4: FLASHING, 6: FAULT_STUCK_ON, 7: FLASHING_EXT
        matches!(self.state, 4 | 6 | 7)
    }

    /// Get item states
    fn item_states(&self, anc: &BeaconAnc) -> ItemStates {
        let state = anc.dev.item_state(self);
        match state {
            ItemState::Available => match self.state {
                2 => ItemState::Available.into(),
                4 => ItemState::Deployed.into(),
                5 => ItemState::Fault.into(),
                6 => ItemStates::from(ItemState::Deployed)
                    .with(ItemState::Fault, "stuck on"),
                7 => ItemStates::from(ItemState::Deployed)
                    .with(ItemState::External, "external flashing"),
                _ => ItemState::Unknown.into(),
            },
            _ => state.into(),
        }
    }

    /// Get beacon state
    fn beacon_state<'a>(&'a self, anc: &'a BeaconAnc) -> &'a str {
        match &anc.states {
            Some(states) => states
                .iter()
                .find(|s| s.id == self.state)
                .map(|s| &s.description[..])
                .unwrap_or("Unknown"),
            _ => "Unknown",
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &BeaconAnc) -> String {
        let item_states = self.item_states(anc);
        let disabled = disabled_attr(self.controller.is_some());
        let location = HtmlStr::new(&self.location).with_len(32);
        format!(
            "<div class='{NAME} end'>{self} {item_states}</div>\
            <div class='info fill{disabled}'>{location}</div>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &BeaconAnc, config: bool) -> String {
        let location = HtmlStr::new(&self.location).with_len(64);
        let item_states = self.item_states(anc).to_html();
        let flashing = if self.flashing() {
            CLASS_FLASHING
        } else {
            CLASS_NOT_FLASHING
        };
        let beacon_state = self.beacon_state(anc);
        let message = HtmlStr::new(&self.message);
        let mut status = format!(
            "<div class='row'>\
              <span class='info'>{location}</span>\
            </div>\
            <div class='row'>{item_states}</div>\
            <div class='beacon-container row center'>\
              <button id='ob_flashing'></button>\
              <label for='ob_flashing' class='beacon'>\
                <span class='{flashing}'>🔆</span>\
              </label>\
              <span class='beacon-sign'>{message}</span>\
              <label for='ob_flashing' class='beacon'>\
                <span class='{flashing} flash-delayed'>🔆</span>\
              </label>\
            </div>\
            <div class='row center'>\
              <span>{beacon_state}</span>\
            </div>"
        );
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
        let message = HtmlStr::new(&self.message);
        let notes = HtmlStr::new(&self.notes);
        let controller = HtmlStr::new(&self.controller);
        let pin = OptVal(self.pin);
        let verify_pin = OptVal(self.verify_pin);
        let ext_mode = if self.ext_mode.unwrap_or(false) {
            " checked"
        } else {
            ""
        };
        format!(
            "<div class='row'>\
              <label for='message'>Message</label>\
              <textarea id='message' maxlength='128' rows='3' \
                        cols='24'>{message}</textarea>\
            </div>\
            <div class='row'>\
              <label for='notes'>Notes</label>\
              <textarea id='notes' maxlength='128' rows='2' \
                        cols='24'>{notes}</textarea>\
            </div>\
            <div class='row'>\
              <label for='controller'>Controller</label>\
              <input id='controller' maxlength='20' size='20' \
                     value='{controller}'>\
            </div>\
            <div class='row'>\
              <label for='pin'>Pin</label>\
              <input id='pin' type='number' min='1' max='104' \
                     size='8' value='{pin}'>\
            </div>\
            <div class='row'>\
              <label for='verify_pin'>Verify Pin</label>\
              <input id='verify_pin' type='number' min='1' max='104' \
                     size='8' value='{verify_pin}'>\
            </div>\
            <div class='row'>\
              <label for='ext_mode'>Ext Mode</label>\
              <input id='ext_mode' type='checkbox'{ext_mode}>\
            </div>"
        )
    }
}

impl Device for Beacon {
    /// Get controller
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Card for Beacon {
    type Ancillary = BeaconAnc;

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
    fn is_match(&self, search: &str, anc: &BeaconAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || self.item_states(anc).is_match(search)
            || self.message.contains_lower(search)
            || self.notes.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &BeaconAnc) -> String {
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
        fields.changed_text_area("message", &self.message);
        fields.changed_text_area("notes", &self.notes);
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.changed_input("verify_pin", self.verify_pin);
        fields.changed_input("ext_mode", self.ext_mode);
        fields.into_value().to_string()
    }

    /// Handle click event for a button on the card
    fn click_changed(&self, id: &str) -> String {
        if id == "ob_flashing" {
            let mut fields = Fields::new();
            match self.state {
                // DARK (2) => FLASHING_REQ (3)
                2 => fields.insert_num("state", 3),
                // FLASHING (4) or FAULT_NO_VERIFY (5) => DARK_REQ (1)
                4 | 5 => fields.insert_num("state", 1),
                _ => (),
            }
            fields.into_value().to_string()
        } else {
            "".into()
        }
    }
}
