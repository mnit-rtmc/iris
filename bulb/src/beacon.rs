// Copyright (C) 2022-2025  Minnesota Department of Transportation
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
use crate::asset::Asset;
use crate::card::{AncillaryData, Card, View, uri_one};
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::error::Result;
use crate::fetch::Action;
use crate::geoloc::{Loc, LocAnc};
use crate::item::{ItemState, ItemStates};
use crate::start::fly_map_item;
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal, TextArea};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// Beacon States
#[derive(Debug, Deserialize)]
pub struct BeaconState {
    pub id: u32,
    pub description: String,
}

/// Beacon
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Beacon {
    pub name: String,
    pub location: Option<String>,
    pub message: String,
    pub notes: Option<String>,
    pub controller: Option<String>,
    pub state: u32,
    // secondary attributes
    pub geo_loc: Option<String>,
    pub pin: Option<u32>,
    pub verify_pin: Option<u32>,
    pub ext_mode: Option<bool>,
}

/// Beacon ancillary data
#[derive(Default)]
pub struct BeaconAnc {
    cio: ControllerIoAnc<Beacon>,
    loc: LocAnc<Beacon>,
    states: Option<Vec<BeaconState>>,
}

impl AncillaryData for BeaconAnc {
    type Primary = Beacon;

    /// Construct ancillary beacon data
    fn new(pri: &Beacon, view: View) -> Self {
        let mut cio = ControllerIoAnc::new(pri, view);
        cio.assets.push(Asset::BeaconStates);
        let loc = LocAnc::new(pri, view);
        let states = None;
        BeaconAnc { cio, loc, states }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.cio.assets.pop().or_else(|| self.loc.assets.pop())
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &Beacon,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::BeaconStates => {
                self.states = Some(serde_wasm_bindgen::from_value(value)?);
            }
            Asset::Controllers => {
                self.cio.set_asset(pri, asset, value)?;
            }
            _ => self.loc.set_asset(pri, asset, value)?,
        }
        Ok(())
    }
}

/// Flashing state class names
const CLASS_FLASHING: &str = "flashing";
const CLASS_NOT_FLASHING: &str = "not-flashing";

impl Beacon {
    /// Check if beacon is flashing
    fn flashing(&self) -> bool {
        // 4: FLASHING, 6: FAULT_STUCK_ON, 7: FLASHING_EXT
        matches!(self.state, 4 | 6 | 7)
    }

    /// Get item states
    fn item_states<'a>(&'a self, anc: &'a BeaconAnc) -> ItemStates<'a> {
        let mut states = anc.cio.item_states(self);
        if states.contains(ItemState::Available) {
            states = match self.state {
                2 => ItemState::Available.into(),
                4 => ItemState::Deployed.into(),
                5 => ItemState::Fault.into(),
                6 => ItemStates::from(ItemState::Deployed)
                    .with(ItemState::Fault, "stuck on"),
                7 => ItemStates::from(ItemState::Deployed)
                    .with(ItemState::External, "external flashing"),
                _ => ItemState::Unknown.into(),
            }
        }
        states
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
        let name = HtmlStr::new(self.name());
        let item_states = self.item_states(anc);
        let flashing = if self.flashing() {
            CLASS_FLASHING
        } else {
            CLASS_NOT_FLASHING
        };
        let message = HtmlStr::new(&self.message);
        format!(
            "<div class='title row'>{name} {item_states}</div>\
            <div class='beacon-container row center'>\
              <button id='ob_flashing' disabled></button>\
              <label for='ob_flashing' class='signal-housing'>\
                <span class='{flashing}'>🔆</span>\
              </label>\
              <span class='beacon-sign tiny'>{message}</span>\
              <label for='ob_flashing' class='signal-housing'>\
                <span class='{flashing} flash-delayed'>🔆</span>\
              </label>\
            </div>"
        )
    }

    /// Convert to Control HTML
    fn to_html_control(&self, anc: &BeaconAnc) -> String {
        if let Some((lat, lon)) = anc.loc.latlon() {
            fly_map_item(&self.name, lat, lon);
        }
        let title = String::from(self.title(View::Control));
        let item_states = self.item_states(anc).to_html();
        let location = HtmlStr::new(&self.location).with_len(64);
        let flashing = if self.flashing() {
            CLASS_FLASHING
        } else {
            CLASS_NOT_FLASHING
        };
        let beacon_state = self.beacon_state(anc);
        let message = HtmlStr::new(&self.message);
        format!(
            "{title}\
            <div class='row'>{item_states}</div>\
            <div class='row'>\
              <span class='info'>{location}</span>\
            </div>\
            <div class='beacon-container row center'>\
              <button id='ob_flashing'></button>\
              <label for='ob_flashing' class='beacon signal-housing'>\
                <span class='{flashing}'>🔆</span>\
              </label>\
              <span class='beacon-sign'>{message}</span>\
              <label for='ob_flashing' class='beacon signal-housing'>\
                <span class='{flashing} flash-delayed'>🔆</span>\
              </label>\
            </div>\
            <div class='row center'>\
              <span>{beacon_state}</span>\
            </div>"
        )
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &BeaconAnc) -> String {
        let title = String::from(self.title(View::Setup));
        let message = HtmlStr::new(&self.message);
        let notes = HtmlStr::new(&self.notes);
        let controller = anc.cio.controller_html(self);
        let pin = anc.cio.pin_html(self.pin);
        let verify_pin = OptVal(self.verify_pin);
        let ext_mode = if self.ext_mode.unwrap_or(false) {
            " checked"
        } else {
            ""
        };
        let footer = self.footer(true);
        format!(
            "{title}\
            <div class='row'>\
              <label for='message'>Message</label>\
              <textarea id='message' maxlength='128' rows='3' \
                        cols='24'>{message}</textarea>\
            </div>\
            <div class='row'>\
              <label for='notes'>Notes</label>\
              <textarea id='notes' maxlength='128' rows='2' \
                        cols='24'>{notes}</textarea>\
            </div>\
            {controller}\
            {pin}\
            <div class='row'>\
              <label for='verify_pin'>Verify Pin</label>\
              <input id='verify_pin' type='number' min='1' max='104' \
                     size='8' value='{verify_pin}'>\
            </div>\
            <div class='row'>\
              <label for='ext_mode'>Ext Mode</label>\
              <input id='ext_mode' type='checkbox'{ext_mode}>\
            </div>\
            {footer}"
        )
    }
}

impl ControllerIo for Beacon {
    /// Get controller name
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Loc for Beacon {
    /// Get geo location name
    fn geoloc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }
}

impl Card for Beacon {
    type Ancillary = BeaconAnc;

    /// Display name
    const DNAME: &'static str = "🔆 Beacon";

    /// All item states as html options
    const ITEM_STATES: &'static str = "<option value=''>all ↴\
         <option value='🔹'>🔹 available\
         <option value='🔶'>🔶 deployed\
         <option value='👽'>👽 external\
         <option value='⚠️'>⚠️ fault\
         <option value='🔌'>🔌 offline\
         <option value='🔺'>🔺 inactive";

    /// Get the resource
    fn res() -> Res {
        Res::Beacon
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

    /// Get the main item state
    fn item_state_main(&self, anc: &Self::Ancillary) -> ItemState {
        let item_states = self.item_states(anc);
        if item_states.is_match(ItemState::Inactive.code()) {
            ItemState::Inactive
        } else if item_states.is_match(ItemState::Offline.code()) {
            ItemState::Offline
        } else if item_states.is_match(ItemState::Deployed.code()) {
            ItemState::Deployed
        } else if item_states.is_match(ItemState::Fault.code()) {
            ItemState::Fault
        } else {
            ItemState::Available
        }
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
            View::Control => self.to_html_control(anc),
            View::Location => anc.loc.to_html_loc(self),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_text_area("message", &self.message);
        fields.changed_text_area("notes", &self.notes);
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.changed_input("verify_pin", self.verify_pin);
        fields.changed_input("ext_mode", self.ext_mode);
        fields.into_value().to_string()
    }

    /// Get changed fields on Location view
    fn changed_location(&self, anc: BeaconAnc) -> String {
        anc.loc.changed_location()
    }

    /// Handle click event for a button on the card
    fn handle_click(&self, _anc: BeaconAnc, id: String) -> Vec<Action> {
        if &id == "ob_flashing" {
            let mut fields = Fields::new();
            match self.state {
                // DARK (2) => FLASHING_REQ (3)
                2 => fields.insert_num("state", 3),
                // FLASHING (4) or FAULT_NO_VERIFY (5) => DARK_REQ (1)
                4 | 5 => fields.insert_num("state", 1),
                _ => (),
            }
            let uri = uri_one(Res::Beacon, &self.name);
            let val = fields.into_value().to_string();
            vec![Action::Patch(uri, val.into())]
        } else {
            Vec::new()
        }
    }
}
