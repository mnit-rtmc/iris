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
use crate::lcsstate::LcsState;
use crate::lock::LockReason;
use crate::start::fly_map_item;
use crate::util::{
    ContainsLower, Doc, Fields, Input, Select, TextArea, opt_ref, opt_str,
};
use chrono::DateTime;
use hatmil::Html;
use resources::Res;
use serde::Deserialize;
use serde_json::Value;
use std::borrow::Cow;
use std::fmt;
use wasm_bindgen::JsValue;
use web_sys::{HtmlButtonElement, HtmlSelectElement};

/// LCS types
#[derive(Debug, Deserialize)]
#[allow(dead_code)]
pub struct LcsType {
    pub id: u32,
    pub description: String,
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

impl fmt::Display for LcsLock {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        // format as JSON for setting LCS lock
        write!(f, "{{\"reason\":\"{}\"", &self.reason)?;
        if let Some(ind) = &self.indications {
            write!(f, ",\"indications\":[")?;
            let mut first = true;
            for i in ind {
                if first {
                    write!(f, "{i}")?;
                    first = false;
                } else {
                    write!(f, ",{i}")?;
                }
            }
            write!(f, "]")?;
        }
        if let Some(expires) = &self.expires {
            write!(f, ",\"expires\":\"{expires}\"")?;
        }
        if let Some(user_id) = &self.user_id {
            write!(f, ",\"user_id\":\"{user_id}\"")?;
        }
        write!(f, "}}")
    }
}

impl LcsLock {
    /// Create a new LCS lock
    fn new(mut reason: LockReason, ind: Option<&[u32]>, u: String) -> Self {
        // If turned on, reason must be incident/testing
        if ind.is_some() && reason.duration().is_none() {
            reason = LockReason::Testing;
        }
        let indications = ind.map(|i| i.to_vec());
        let expires = ind.and(reason.make_expires());
        LcsLock {
            reason: reason.as_str().to_string(),
            indications,
            expires,
            user_id: Some(u),
        }
    }

    /// Encode into JSON Value
    fn json(&self) -> Value {
        let reason = LockReason::from(self.reason.as_str());
        match reason {
            LockReason::Unlocked => Value::Null,
            _ => Value::String(self.to_string()),
        }
    }

    /// Format lock expire time
    fn expires(&self) -> Option<String> {
        if let Some(expires) = &self.expires
            && let Ok(dt) = DateTime::parse_from_rfc3339(expires)
        {
            return Some(format!("⏲️ {}", &dt.format("%H:%M")));
        }
        None
    }
}

impl LcsAnc {
    /// Build LCS types HTML
    fn lcs_types_html(&self, pri: &Lcs, html: &mut Html) {
        html.select().id("lcs_type");
        for tp in &self.lcs_types {
            let option = html.option().value(tp.id.to_string());
            if Some(tp.id) == pri.lcs_type {
                option.attr_bool("selected");
            }
            html.text(&tp.description).end();
        }
        html.end(); /* select */
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

    /// Build lock reason HTML
    fn lock_reason_html(&self, html: &mut Html) {
        let reason = self.lock_reason();
        html.span();
        html.text(match reason {
            LockReason::Unlocked => "🔓",
            _ => "🔒",
        });
        html.select().id("lk_reason");
        for r in LockReason::all_lcs() {
            let option = html.option();
            if *r == reason {
                option.attr_bool("selected");
            }
            html.text(r.as_str()).end();
        }
        html.end().end();
    }

    /// Get lock indications
    fn lock_indications(&self) -> Option<&[u32]> {
        self.lock
            .as_ref()
            .and_then(|lk| lk.indications.as_ref().map(|ind| &ind[..]))
    }

    /// Make action to lock the LCS
    fn make_lock_action(
        &self,
        reason: LockReason,
        ind: Option<&[u32]>,
    ) -> Vec<Action> {
        let mut actions = Vec::with_capacity(1);
        if let Some(user) = crate::app::user() {
            let uri = uri_one(Res::Lcs, &self.name);
            let lock = LcsLock::new(reason, ind, user).json();
            let val = format!("{{\"lock\":{lock}}}");
            actions.push(Action::Patch(uri, val.into()));
        }
        actions
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

    /// Build indications HTML
    fn indications_html(&self, anc: &LcsAnc, html: &mut Html) {
        let enabled = self.lock_reason().is_deployable();
        html.div().class("row center");
        let indications = self.indications();
        let lock_indications = self.lock_indications().unwrap_or(&[0]);
        let len = indications.len().max(lock_indications.len());
        for ln in (0..len).rev() {
            let ind = *indications.get(ln).unwrap_or(&0);
            let lki = *lock_indications.get(ln).unwrap_or(&1);
            let ln = (ln + 1) as u16;
            let ind_id = format!("ind_{ln}");
            let span = html.div().class("column").span();
            match ind {
                1 => span.class("lcs lcs_dark").text("⍽"),
                2 => span.class("lcs lcs_lane_open").text("↓"),
                3 => span.class("lcs lcs_use_caution").text("⇣"),
                4 => span.class("lcs lcs_lane_closed_ahead").text("✕"),
                5 => span.class("lcs lcs_lane_closed").text("✖"),
                _ => span.class("lcs lcs_unknown").text("?"),
            };
            html.end(); /* span */
            let select = html.select().id(ind_id);
            if !enabled {
                select.attr_bool("disabled");
            }
            html.button().end();
            // FIXME: use customizable select elements once browsers have
            //        support for them: https://caniuse.com/selectlist
            html.option().class("lcs lcs_dark").value("1");
            html.text(" ").end();
            if anc.has_indication(self, ln, 2) {
                let opt = html.option().class("lcs lcs_lane_open").value("2");
                if lki == 2 {
                    opt.attr_bool("selected");
                }
                html.text("↓").end();
            }
            if anc.has_indication(self, ln, 3) {
                let opt = html.option().class("lcs lcs_use_caution").value("3");
                if lki == 3 {
                    opt.attr_bool("selected");
                }
                html.text("⇣").end();
            }
            if anc.has_indication(self, ln, 4) {
                let opt =
                    html.option().class("lcs lcs_lane_closed_ahead").value("4");
                if lki == 4 {
                    opt.attr_bool("selected");
                }
                html.text("✕").end();
            }
            if anc.has_indication(self, ln, 5) {
                let opt = html.option().class("lcs lcs_lane_closed").value("5");
                if lki == 5 {
                    opt.attr_bool("selected");
                }
                html.text("✖").end();
            }
            html.end(); /* select */
            html.end(); /* div */
        }
        html.div().class("column");
        self.lock_reason_html(html);
        html.span();
        let send = html.button().id("lk_send").type_("button");
        send.attr_bool("disabled");
        html.text("Send").end();
        let blank = html.button().id("lk_blank").type_("button");
        if !enabled || !self.is_deployed() {
            blank.attr_bool("disabled");
        }
        html.text("Blank").end();
        html.end(); /* span */
        html.end(); /* div */
        html.end(); /* div */
    }

    /// Get selected indications
    fn selected_indications(&self) -> Vec<u32> {
        let mut indications = Vec::new();
        let len = self.indications().len();
        for ln in 1..len + 1 {
            let id = format!("ind_{ln}");
            let ind = Doc::get().select_parse::<u32>(&id).unwrap_or(0);
            indications.push(ind);
        }
        indications
    }

    /// Selected indications updated
    fn update_indications(&self) {
        // Disable send button if any selected indications are DARK
        let disable = self.selected_indications().iter().any(|i| *i <= 1);
        let lk_send = Doc::get().elem::<HtmlButtonElement>("lk_send");
        lk_send.set_disabled(disable);
    }

    /// Make lock send action
    fn lock_send(&self) -> Vec<Action> {
        let indications = self.selected_indications();
        if indications.iter().all(|i| *i > 1) {
            let r = Doc::get().elem::<HtmlSelectElement>("lk_reason").value();
            let mut reason = LockReason::from(&r[..]);
            if reason.duration().is_none() {
                reason = LockReason::Incident;
            }
            self.make_lock_action(reason, Some(&indications))
        } else {
            Vec::new()
        }
    }

    /// Make lock blank action
    fn lock_blank(&self) -> Vec<Action> {
        self.make_lock_action(LockReason::Unlocked, None)
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
        let mut html = Html::new();
        html.div()
            .class("title row")
            .text(self.name())
            .text(" ")
            .text(self.item_states(anc).to_string())
            .end();
        html.div()
            .class("info fill")
            .text_len(opt_ref(&self.location), 32);
        html.to_string()
    }

    /// Convert to Control HTML
    fn to_html_control(&self, anc: &LcsAnc) -> String {
        if let Some((lat, lon)) = anc.loc.latlon() {
            fly_map_item(&self.name, lat, lon);
        }
        let mut html = self.title(View::Control);
        html.div().class("row fill");
        self.item_states(anc).tooltips(&mut html);
        html.span();
        if let Some(lock) = &self.lock
            && let Some(expires) = lock.expires()
        {
            html.text(expires);
        }
        html.end(); /* span */
        html.end(); /* div */
        html.div().class("row");
        html.span()
            .class("info")
            .text_len(opt_ref(&self.location), 64)
            .end();
        html.end(); /* div */
        self.indications_html(anc, &mut html);
        html.to_string()
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &LcsAnc) -> String {
        let mut html = self.title(View::Setup);
        html.div().class("row");
        html.label().for_("notes").text("Notes").end();
        html.textarea()
            .id("notes")
            .maxlength("255")
            .attr("rows", "4")
            .attr("cols", "24")
            .text(opt_ref(&self.notes))
            .end();
        html.end(); /* div */
        anc.cio.controller_html(self, &mut html);
        anc.cio.pin_html(self.pin, &mut html);
        html.div().class("row");
        html.label().for_("lcs_type").text("LCS Type").end();
        anc.lcs_types_html(self, &mut html);
        html.end(); /* div */
        html.div().class("row");
        html.label().for_("shift").text("Lane Shift").end();
        html.input()
            .id("shift")
            .type_("number")
            .min("1")
            .max("9")
            .size("2")
            .value(opt_str(self.shift));
        html.end(); /* div */
        self.footer_html(true, &mut html);
        html.to_string()
    }
}

impl Card for Lcs {
    type Ancillary = LcsAnc;

    /// Display name
    const DNAME: &'static str = "🠟✖🠟 LCS";

    /// Get the resource
    fn res() -> Res {
        Res::Lcs
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[
            ItemState::Available,
            ItemState::Deployed,
            ItemState::Locked,
            ItemState::Fault,
            ItemState::Offline,
            ItemState::Inactive,
        ]
    }

    /// Get the name
    fn name(&self) -> Cow<'_, str> {
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

    /// Handle click event for a button on the card
    fn handle_click(&self, _anc: LcsAnc, id: String) -> Vec<Action> {
        match id.as_str() {
            "lk_send" => self.lock_send(),
            "lk_blank" => self.lock_blank(),
            _ => Vec::new(),
        }
    }

    /// Handle input event for an element on the card
    fn handle_input(&self, _anc: LcsAnc, id: String) -> Vec<Action> {
        if &id == "lk_reason" {
            let r = Doc::get().elem::<HtmlSelectElement>("lk_reason").value();
            let reason = LockReason::from(&r[..]);
            if reason.duration().is_some() {
                if self.is_deployed() {
                    let indications = self.selected_indications();
                    if indications.iter().all(|i| *i > 1) {
                        let ind = Some(&indications[..]);
                        return self.make_lock_action(reason, ind);
                    }
                }
            } else {
                return self.make_lock_action(reason, None);
            };
        }
        if id.starts_with("ind_") {
            self.update_indications();
        }
        Vec::new()
    }
}
