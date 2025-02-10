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
use crate::card::{AncillaryData, Card, View};
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::error::Result;
use crate::geoloc::{Loc, LocAnc};
use crate::item::{ItemState, ItemStates};
use crate::start::fly_map_item;
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal, TextArea};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// Meter Lock
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct MeterLock {
    pub reason: String,
    pub rate: Option<u32>,
    pub expires: Option<String>,
    pub user_id: Option<String>,
}

/// Meter Status
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct MeterStatus {
    pub rate: Option<u32>,
    pub queue: Option<String>,
    pub fault: Option<String>,
}

/// Ramp Meter
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct RampMeter {
    pub name: String,
    pub location: Option<String>,
    pub controller: Option<String>,
    pub notes: Option<String>,
    pub lock: Option<MeterLock>,
    pub status: Option<MeterStatus>,
    // secondary attributes
    pub geo_loc: Option<String>,
    pub pin: Option<u32>,
    pub beacon: Option<String>,
    pub preset: Option<String>,
    pub meter_type: Option<u32>,
    pub storage: Option<u32>,
    pub max_wait: Option<u32>,
    pub algorithm: Option<u32>,
    pub am_target: Option<u32>,
    pub pm_target: Option<u32>,
}

/// Ramp meter ancillary data
#[derive(Default)]
pub struct RampMeterAnc {
    cio: ControllerIoAnc<RampMeter>,
    loc: LocAnc<RampMeter>,
}

impl AncillaryData for RampMeterAnc {
    type Primary = RampMeter;

    /// Construct ancillary ramp meter data
    fn new(pri: &RampMeter, view: View) -> Self {
        let cio = ControllerIoAnc::new(pri, view);
        let loc = LocAnc::new(pri, view);
        RampMeterAnc { cio, loc }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.cio.assets.pop().or_else(|| self.loc.assets.pop())
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &RampMeter,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        if let Asset::Controllers = asset {
            self.cio.set_asset(pri, asset, value)
        } else {
            self.loc.set_asset(pri, asset, value)
        }
    }
}

impl RampMeter {
    /// Get fault, if any
    fn fault(&self) -> Option<&str> {
        if let Some(status) = &self.status {
            if let Some(fault) = &status.fault {
                return Some(fault);
            }
        }
        None
    }

    /// Get item states
    fn item_states<'a>(&'a self, anc: &'a RampMeterAnc) -> ItemStates<'a> {
        let mut states = anc.cio.item_states(self);
        if let Some(fault) = self.fault() {
            states = states.with(ItemState::Fault, fault);
        }
        states
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &RampMeterAnc) -> String {
        let name = HtmlStr::new(self.name());
        let item_states = self.item_states(anc);
        let location = HtmlStr::new(&self.location).with_len(32);
        format!(
            "<div class='title row'>{name} {item_states}</div>\
            <div class='info fill'>{location}</div>"
        )
    }

    /// Convert to Control HTML
    fn to_html_control(&self, anc: &RampMeterAnc) -> String {
        if let Some((lat, lon)) = anc.loc.latlon() {
            fly_map_item(&self.name, lat, lon);
        }
        let title = self.title(View::Control);
        let item_states = self.item_states(anc).to_html();
        let location = HtmlStr::new(&self.location).with_len(64);
        format!(
            "{title}\
            <div class='row fill'>\
              <span>{item_states}</span>\
            </div>\
            <div class='row'>\
              <span class='info'>{location}</span>\
            </div>"
        )
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &RampMeterAnc) -> String {
        let title = self.title(View::Setup);
        let notes = HtmlStr::new(&self.notes);
        let controller = anc.cio.controller_html(self);
        let pin = anc.cio.pin_html(self.pin);
        let storage = OptVal(self.storage);
        let max_wait = OptVal(self.max_wait);
        let am_target = OptVal(self.am_target);
        let pm_target = OptVal(self.pm_target);
        let footer = self.footer(true);
        format!(
            "{title}\
            <div class='row'>\
              <label for='notes'>Notes</label>\
              <textarea id='notes' maxlength='255' rows='4' \
                        cols='24'>{notes}</textarea>\
            </div>\
            {controller}\
            {pin}\
            <div class='row'>\
              <label for='storage'>Storage (ft)</label>\
              <input id='storage' type='number' min='1' max='5000' \
                     size='8' value='{storage}'>\
            </div>\
            <div class='row'>\
              <label for='max_wait'>Max Wait (s)</label>\
              <input id='max_wait' type='number' min='1' max='600' \
                     size='8' value='{max_wait}'>\
            </div>\
            <div class='row'>\
              <label for='am_target'>AM Target</label>\
              <input id='am_target' type='number' min='0' max='2000' \
                     size='8' value='{am_target}'>\
            </div>\
            <div class='row'>\
              <label for='pm_target'>PM Target</label>\
              <input id='pm_target' type='number' min='0' max='2000' \
                     size='8' value='{pm_target}'>\
            </div>\
            {footer}"
        )
    }
}

impl ControllerIo for RampMeter {
    /// Get controller name
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Loc for RampMeter {
    /// Get geo location name
    fn geoloc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }
}

impl Card for RampMeter {
    type Ancillary = RampMeterAnc;

    /// Display name
    const DNAME: &'static str = "🚦 Ramp Meter";

    /// All item states as html options
    const ITEM_STATES: &'static str = "<option value=''>all ↴\
         <option value='🔹'>🔹 available\
         <option value='🔶' selected>🔶 deployed\
         <option value='⚠️'>⚠️ fault\
         <option value='🔌'>🔌 offline\
         <option value='▪️'>▪️ inactive";

    /// Get the resource
    fn res() -> Res {
        Res::RampMeter
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
        } else if item_states.is_match(ItemState::Deployed.code()) {
            ItemState::Deployed
        } else if item_states.is_match(ItemState::Offline.code()) {
            ItemState::Offline
        } else {
            ItemState::Available
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &RampMeterAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || self
                .notes
                .as_ref()
                .is_some_and(|n| n.contains_lower(search))
            || self.item_states(anc).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &RampMeterAnc) -> String {
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
        fields.changed_text_area("notes", &self.notes);
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.changed_input("storage", self.storage);
        fields.changed_input("max_wait", self.max_wait);
        fields.changed_input("am_target", self.am_target);
        fields.changed_input("pm_target", self.pm_target);
        fields.into_value().to_string()
    }

    /// Get changed fields on Location view
    fn changed_location(&self, anc: RampMeterAnc) -> String {
        anc.loc.changed_location()
    }
}
