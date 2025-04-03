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
use crate::lcsstate::LcsState;
use crate::start::fly_map_item;
use crate::util::{
    ContainsLower, Fields, HtmlStr, Input, OptVal, Select, TextArea,
};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use std::fmt;
use wasm_bindgen::JsValue;

/// LCS types
#[derive(Debug, Deserialize)]
#[allow(dead_code)]
pub struct LcsType {
    pub id: u32,
    pub description: String,
}

/// LCS lock reason
#[derive(Clone, Copy, Debug, PartialEq)]
enum LockReason {
    Unlocked,
    Incident,
    Testing,
    Indication,
    Maintenance,
    Construction,
}

/// LCS Lock
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct LcsLock {
    pub reason: String,
    pub indications: Option<Vec<u32>>,
    pub expires: Option<String>,
    pub user_id: Option<String>,
}

/// LCS Status
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct LcsStatus {
    pub indications: Option<Vec<u32>>,
    pub faults: Option<String>,
}

/// LCS Array
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Lcs {
    pub name: String,
    pub location: Option<String>,
    pub controller: Option<String>,
    pub notes: Option<String>,
    pub lock: Option<LcsLock>,
    pub status: Option<LcsStatus>,
    // secondary attributes
    pub geo_loc: Option<String>,
    pub lcs_type: Option<u32>,
    pub pin: Option<u32>,
    pub preset: Option<String>,
    pub shift: Option<u16>,
}

/// Ancillary LCS array data
#[derive(Debug, Default)]
pub struct LcsAnc {
    cio: ControllerIoAnc<Lcs>,
    loc: LocAnc<Lcs>,
    lcs_states: Vec<LcsState>,
    pub lcs_types: Vec<LcsType>,
}

impl LcsAnc {
    /// Create an HTML `select` element of LCS types
    fn lcs_types_html(&self, pri: &Lcs) -> String {
        let mut html = String::new();
        html.push_str("<select id='lcs_type'>");
        for tp in &self.lcs_types {
            html.push_str("<option value='");
            html.push_str(&tp.id.to_string());
            html.push('\'');
            if Some(tp.id) == pri.lcs_type {
                html.push_str(" selected");
            }
            html.push('>');
            html.push_str(&tp.description);
            html.push_str("</option>");
        }
        html.push_str("</select>");
        html
    }

    /// Check if an LCS has a given lane/indication
    fn has_indication(&self, pri: &Lcs, lane: u16, ind: u32) -> bool {
        self.lcs_states.iter().any(|st| {
            st.lcs == pri.name && st.lane == lane && st.indication == ind
        })
    }
}

impl AncillaryData for LcsAnc {
    type Primary = Lcs;

    /// Construct ancillary LCS array data
    fn new(pri: &Lcs, view: View) -> Self {
        let mut cio = ControllerIoAnc::new(pri, view);
        match view {
            View::Control => cio.assets.push(Asset::LcsStates),
            View::Setup => cio.assets.push(Asset::LcsTypes),
            _ => (),
        }
        let loc = LocAnc::new(pri, view);
        LcsAnc {
            cio,
            loc,
            lcs_states: Vec::new(),
            lcs_types: Vec::new(),
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.cio.assets.pop().or_else(|| self.loc.assets.pop())
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &Lcs,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::Controllers => self.cio.set_asset(pri, asset, value)?,
            Asset::LcsStates => {
                self.lcs_states = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::LcsTypes => {
                self.lcs_types = serde_wasm_bindgen::from_value(value)?;
            }
            _ => self.loc.set_asset(pri, asset, value)?,
        }
        Ok(())
    }
}

impl From<&str> for LockReason {
    fn from(r: &str) -> Self {
        match r {
            "incident" => Self::Incident,
            "testing" => Self::Testing,
            "indication" => Self::Indication,
            "maintenance" => Self::Maintenance,
            "construction" => Self::Construction,
            _ => Self::Unlocked,
        }
    }
}

impl fmt::Display for LockReason {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.as_str())
    }
}

impl LockReason {
    /// Get lock reason as a string slice
    fn as_str(&self) -> &'static str {
        use LockReason::*;
        match self {
            Unlocked => "unlocked",
            Incident => "incident",
            Testing => "testing",
            Indication => "indication",
            Maintenance => "maintenance",
            Construction => "construction",
        }
    }
}

impl ControllerIo for Lcs {
    /// Get controller name
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Loc for Lcs {
    /// Get geo location name
    fn geoloc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }
}

impl Lcs {
    /// Get faults, if any
    fn faults(&self) -> Option<&str> {
        self.status.as_ref().and_then(|s| s.faults.as_deref())
    }

    /// Get item states
    fn item_states<'a>(&'a self, anc: &'a LcsAnc) -> ItemStates<'a> {
        let mut states = anc.cio.item_states(self);
        if states.contains(ItemState::Available) {
            states = self.item_states_lock();
        }
        if let Some(faults) = self.faults() {
            states = states.with(ItemState::Fault, faults);
        }
        states
    }

    /// Get lock reason
    fn lock_reason(&self) -> LockReason {
        self.lock
            .as_ref()
            .map(|lk| LockReason::from(lk.reason.as_str()))
            .unwrap_or(LockReason::Unlocked)
    }

    /// Create an HTML `select` element of lock reasons
    fn lock_reason_html(&self) -> String {
        let reason = self.lock_reason();
        let mut html = String::new();
        html.push_str("<span>");
        html.push(match reason {
            LockReason::Unlocked => '🔓',
            _ => '🔒',
        });
        html.push_str("<select id='lock_reason'>");
        for r in [
            LockReason::Unlocked,
            LockReason::Incident,
            LockReason::Testing,
            LockReason::Indication,
            LockReason::Maintenance,
            LockReason::Construction,
        ] {
            html.push_str("<option");
            if r == reason {
                html.push_str(" selected");
            }
            html.push('>');
            html.push_str(r.as_str());
            html.push_str("</option>");
        }
        html.push_str("</select></span>");
        html
    }

    /// Check if the LCS is deployed
    fn is_deployed(&self) -> bool {
        self.status.as_ref().is_some_and(|st| {
            st.indications
                .as_ref()
                .is_some_and(|ind| ind.iter().all(|li| *li > 1))
        })
    }

    /// Get current indications
    fn indications(&self) -> &[u32] {
        self.status
            .as_ref()
            .and_then(|st| st.indications.as_ref().map(|ind| &ind[..]))
            .unwrap_or(&[0])
    }

    /// Create an HTML indications element
    fn indications_html(&self, anc: &LcsAnc) -> String {
        let mut html = String::new();
        html.push_str("<div class='row center'>");
        for (ln, ind) in self.indications().iter().enumerate().rev() {
            let ln = (ln + 1) as u16;
            html.push_str("<div class='column'>");
            html.push_str("<span class='lcs ");
            match ind {
                1 => html.push_str("lcs_dark'>⍽"),
                2 => html.push_str("lcs_lane_open'>↓"),
                3 => html.push_str("lcs_use_caution'>⇣"),
                4 => html.push_str("lcs_lane_closed_ahead'>✕"),
                5 => html.push_str("lcs_lane_closed'>✖"),
                _ => html.push_str("lcs_unknown'>?"),
            }
            html.push_str("</span>");
            html.push_str("<select>");
            html.push_str(
                "<button><selectedcontent></selectedcontent></button>",
            );
            // FIXME: use customizable select elements once browsers have
            //        support for them: https://caniuse.com/selectlist
            html.push_str("<option value='1' class='lcs lcs_dark'> </option>");
            if anc.has_indication(self, ln, 2) {
                html.push_str(
                    "<option value='2' class='lcs lcs_lane_open'>↓</option>",
                );
            }
            if anc.has_indication(self, ln, 3) {
                html.push_str(
                    "<option value='3' class='lcs lcs_use_caution'>⇣</option>",
                );
            }
            if anc.has_indication(self, ln, 4) {
                html.push_str("<option value='4' class='lcs lcs_lane_closed_ahead'>✕</option>");
            }
            if anc.has_indication(self, ln, 5) {
                html.push_str(
                    "<option value='5' class='lcs lcs_lane_closed'>✖</option>",
                );
            }
            html.push_str("</select>");
            html.push_str("</div>");
        }
        html.push_str("<div class='column'>");
        html.push_str(&self.lock_reason_html());
        html.push_str("<span>");
        html.push_str("<button id='mc_send' type='button'>Send</button>");
        html.push_str("<button id='mc_blank' type='button'>Blank</button>");
        html.push_str("</span>");
        html.push_str("</div>");
        html.push_str("</div>");
        html
    }

    /// Get item states from status/lock
    fn item_states_lock(&self) -> ItemStates<'_> {
        let deployed = self.is_deployed();
        let reason = self.lock_reason();
        let states = ItemStates::default();
        match (deployed, reason) {
            (true, LockReason::Unlocked) => {
                states.with(ItemState::Deployed, "deployed")
            }
            (true, _) => states
                .with(ItemState::Deployed, "deployed")
                .with(ItemState::Locked, reason.as_str()),
            (false, LockReason::Unlocked) => ItemState::Available.into(),
            (false, _) => states.with(ItemState::Locked, reason.as_str()),
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &LcsAnc) -> String {
        let name = HtmlStr::new(self.name());
        let item_states = self.item_states(anc);
        let location = HtmlStr::new(&self.location).with_len(32);
        format!(
            "<div class='title row'>{name} {item_states}</div>\
            <div class='info fill'>{location}</div>"
        )
    }

    /// Convert to Control HTML
    fn to_html_control(&self, anc: &LcsAnc) -> String {
        if let Some((lat, lon)) = anc.loc.latlon() {
            fly_map_item(&self.name, lat, lon);
        }
        let title = self.title(View::Control).build();
        let item_states = self.item_states(anc).to_html();
        let location = HtmlStr::new(&self.location).with_len(64);
        let indications = self.indications_html(anc);
        format!(
            "{title}\
            <div class='row fill'>\
              <span>{item_states}</span>\
            </div>\
            <div class='row'>\
              <span class='info'>{location}</span>\
            </div>\
            {indications}"
        )
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &LcsAnc) -> String {
        let title = self.title(View::Setup).build();
        let notes = HtmlStr::new(&self.notes);
        let controller = anc.cio.controller_html(self);
        let pin = anc.cio.pin_html(self.pin);
        let lcs_types = anc.lcs_types_html(self);
        let shift = OptVal(self.shift);
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
              <label for='lcs_type'>LCS Type</label>\
              {lcs_types}\
            </div>\
            <div class='row'>\
              <label for='shift'>Lane Shift</label>\
              <input id='shift' type='number' min='1' max='9' \
                     size='2' value='{shift}'>\
            </div>\
            {footer}"
        )
    }
}

impl Card for Lcs {
    type Ancillary = LcsAnc;

    /// Display name
    const DNAME: &'static str = "🠟✖🠟 LCS";

    /// All item states as html options
    const ITEM_STATES: &'static str = "<option value='' selected>all ↴\
         <option value='🔹'>🔹 available\
         <option value='🔶'>🔶 deployed\
         <option value='🔒'>🔒 locked\
         <option value='⚠️'>⚠️ fault\
         <option value='🔌'>🔌 offline\
         <option value='🔺'>🔺 inactive";

    /// Get the resource
    fn res() -> Res {
        Res::Lcs
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
        let item_states = anc.cio.item_states(self);
        if item_states.is_match(ItemState::Inactive.code()) {
            ItemState::Inactive
        } else if item_states.is_match(ItemState::Offline.code()) {
            ItemState::Offline
        } else if item_states.is_match(ItemState::Deployed.code()) {
            ItemState::Deployed
        } else if item_states.is_match(ItemState::Fault.code())
            || item_states.is_match(ItemState::Locked.code())
        {
            ItemState::Fault
        } else {
            ItemState::Available
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &LcsAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || self
                .notes
                .as_ref()
                .is_some_and(|n| n.contains_lower(search))
            || self.item_states(anc).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &LcsAnc) -> String {
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
        fields.changed_select("lcs_type", self.lcs_type);
        fields.changed_input("shift", self.shift);
        fields.into_value().to_string()
    }
}
