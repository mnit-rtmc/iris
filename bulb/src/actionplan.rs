// Copyright (C) 2025-2026  Minnesota Department of Transportation
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
use crate::error::Result;
use crate::fetch::Action;
use crate::item::{ItemState, ItemStates};
use crate::util::{
    ContainsLower, Doc, Fields, Input, Select, TextArea, opt_ref,
};
use hatmil::{Page, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use std::collections::BTreeSet;
use wasm_bindgen::JsValue;
use web_sys::HtmlSelectElement;

/// Plan phase
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct PlanPhase {
    pub name: String,
    pub selectable: bool,
    pub hold_time: Option<u32>,
    pub next_phase: Option<String>,
}

/// Device action
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct DeviceAction {
    pub name: String,
    pub action_plan: String,
    pub hashtag: String,
    pub phase: String,
    // secondary attributes
    pub msg_pattern: Option<String>,
    pub msg_priority: Option<u8>,
}

/// Hashtag resource
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct HashtagResource {
    pub hashtag: String,
    pub resource_n: String,
}

/// Time action
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct TimeAction {
    pub name: String,
    pub action_plan: String,
    pub day_plan: Option<String>,
    pub sched_date: Option<String>,
    pub time_of_day: String,
    pub phase: String,
}

impl TimeAction {
    fn to_html(&self) -> String {
        let mut page = Page::new();
        let mut tr = page.frag::<html::Tr>();
        if let Some(day_plan) = &self.day_plan {
            tr.td().cdata(day_plan).close();
        }
        if let Some(sched_date) = &self.sched_date {
            tr.td().cdata(sched_date).close();
        }
        tr.td().cdata(&self.time_of_day).close();
        tr.td().cdata("⇨ ").cdata(&self.phase).close();
        String::from(page)
    }
}

/// Action plan
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct ActionPlan {
    pub name: String,
    pub notes: Option<String>,
    pub active: bool,
    pub default_phase: String,
    pub phase: String,
    // secondary attributes
    pub sync_actions: Option<bool>,
    pub sticky: Option<bool>,
    pub ignore_auto_fail: Option<bool>,
}

/// Action plan ancillary data
#[derive(Default)]
pub struct ActionPlanAnc {
    assets: Vec<Asset>,
    pub phases: Vec<PlanPhase>,
    pub device_actions: Vec<DeviceAction>,
    pub hashtag_resources: Vec<HashtagResource>,
    pub time_actions: Vec<TimeAction>,
}

impl AncillaryData for ActionPlanAnc {
    type Primary = ActionPlan;

    /// Construct ancillary action plan data
    fn new(_pri: &ActionPlan, view: View) -> Self {
        let mut assets = Vec::new();
        if let View::Control = view {
            assets.push(Asset::TimeActions);
        }
        if let View::Search | View::Compact | View::Control = view {
            assets.push(Asset::HashtagResources);
        }
        if let View::Search | View::Compact | View::Control | View::Setup = view
        {
            assets.push(Asset::DeviceActions);
        }
        if let View::Control | View::Setup = view {
            assets.push(Asset::PlanPhases);
        }
        let phases = Vec::new();
        let device_actions = Vec::new();
        let hashtag_resources = Vec::new();
        let time_actions = Vec::new();
        ActionPlanAnc {
            assets,
            phases,
            device_actions,
            hashtag_resources,
            time_actions,
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &ActionPlan,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::PlanPhases => {
                self.phases = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::DeviceActions => {
                self.device_actions = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::HashtagResources => {
                self.hashtag_resources = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::TimeActions => {
                let mut actions: Vec<TimeAction> =
                    serde_wasm_bindgen::from_value(value)?;
                actions.retain(|ta| ta.action_plan == pri.name);
                self.time_actions = actions;
            }
            _ => unreachable!(),
        }
        Ok(())
    }
}

impl ActionPlanAnc {
    /// Get action plan phases
    fn phases<'a>(
        &'a self,
        pri: &'a ActionPlan,
    ) -> impl Iterator<Item = &'a str> {
        let mut phases = BTreeSet::new();
        phases.insert(&pri.default_phase[..]);
        for da in &self.device_actions {
            if da.action_plan == pri.name {
                phases.insert(&da.phase[..]);
            }
        }
        phases.into_iter()
    }

    /// Get device hashtags
    fn hashtags<'a>(
        &'a self,
        pri: &'a ActionPlan,
    ) -> impl Iterator<Item = &'a str> {
        let mut tags = BTreeSet::new();
        for da in &self.device_actions {
            if da.action_plan == pri.name {
                tags.insert(&da.hashtag[..]);
            }
        }
        tags.into_iter()
    }
}

impl ActionPlan {
    /// Get item state
    fn item_states(&self, anc: &ActionPlanAnc) -> ItemStates<'_> {
        let mut states = ItemStates::default();
        if self.active {
            if self.phase == self.default_phase {
                states = states.with(ItemState::Available, "");
            } else {
                states = states.with(ItemState::Deployed, "");
            }
            for da in &anc.device_actions {
                if da.action_plan == self.name {
                    for hr in &anc.hashtag_resources {
                        if hr.hashtag == da.hashtag {
                            if hr.resource_n == "beacon" {
                                states = states.with(ItemState::Beacon, "");
                            }
                            if hr.resource_n == "camera" {
                                states = states.with(ItemState::Camera, "");
                            }
                            if hr.resource_n == "dms" {
                                states = states.with(ItemState::Dms, "");
                            }
                            if hr.resource_n == "ramp_meter" {
                                states = states.with(ItemState::RampMeter, "");
                            }
                        }
                    }
                }
            }
        } else {
            states = states.with(ItemState::Inactive, "");
        }
        states
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &ActionPlanAnc) -> String {
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(self.item_states(anc).to_string());
        String::from(page)
    }

    /// Convert to Control HTML
    fn to_html_control(&self, anc: &ActionPlanAnc) -> String {
        let mut page = Page::new();
        self.title(View::Control, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row fill");
        self.item_states(anc).tooltips(&mut div.span());
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        let tags = anc.hashtags(self).collect::<Vec<_>>().join(" ");
        div.span().class("info").cdata(tags).close();
        let mut span = div.span();
        span.label().r#for("phase").cdata("Phase").close();
        let mut select = span.select();
        select.id("phase");
        for p in anc.phases(self) {
            let mut option = select.option();
            if p == self.phase {
                option.selected();
            }
            option.cdata(p).close();
        }
        div.close();
        if !anc.time_actions.is_empty() {
            div = page.frag::<html::Div>();
            div.class("row").cdata("Schedule 🗓️").close();
            let mut table = page.frag::<html::Table>();
            for ta in &anc.time_actions {
                table.raw(ta.to_html());
            }
            table.close();
        }
        String::from(page)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &ActionPlanAnc) -> String {
        let mut page = Page::new();
        self.title(View::Setup, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("notes").cdata("Notes").close();
        div.textarea()
            .id("notes")
            .maxlength(128)
            .rows(3)
            .cols(24)
            .cdata(opt_ref(&self.notes))
            .close();
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("active").cdata("Active").close();
        let mut input = div.input();
        input.id("active").r#type("checkbox");
        if self.active {
            input.checked();
        }
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("default_phase")
            .cdata("Default Phase")
            .close();
        let mut select = div.select();
        select.id("default_phase");
        for p in &anc.phases {
            let mut option = select.option();
            if p.name == self.default_phase {
                option.selected();
            }
            option.cdata(&p.name).close();
        }
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("sync_actions")
            .cdata("Sync Actions")
            .close();
        let mut input = div.input();
        input.id("sync_actions").r#type("checkbox");
        if let Some(true) = self.sync_actions {
            input.checked();
        }
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("sticky").cdata("Sticky").close();
        input = div.input();
        input.id("sticky").r#type("checkbox");
        if let Some(true) = self.sticky {
            input.checked();
        }
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("ignore_auto_fail")
            .cdata("Ignore Auto-Fail")
            .close();
        input = div.input();
        input.id("ignore_auto_fail").r#type("checkbox");
        if let Some(true) = self.ignore_auto_fail {
            input.checked();
        }
        div.close();
        self.footer_html(true, &mut page.frag::<html::Div>());
        String::from(page)
    }
}

impl Card for ActionPlan {
    type Ancillary = ActionPlanAnc;

    /// Get the resource
    fn res() -> Res {
        Res::ActionPlan
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[
            ItemState::Available,
            ItemState::Deployed,
            ItemState::Beacon,
            ItemState::Camera,
            ItemState::Dms,
            ItemState::RampMeter,
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
    fn item_state_main(&self, anc: &ActionPlanAnc) -> ItemState {
        let states = self.item_states(anc);
        if states.contains(ItemState::Inactive) {
            ItemState::Inactive
        } else if states.contains(ItemState::Deployed) {
            ItemState::Deployed
        } else {
            ItemState::Available
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &ActionPlanAnc) -> bool {
        self.name.contains_lower(search)
            || self.item_states(anc).is_match(search)
            || self.notes.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &ActionPlanAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Control => self.to_html_control(anc),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_text_area("notes", &self.notes);
        fields.changed_input("active", self.active);
        fields.changed_select("default_phase", &self.default_phase);
        fields.changed_input("sync_actions", self.sync_actions);
        fields.changed_input("sticky", self.sticky);
        fields.changed_input("ignore_auto_fail", self.ignore_auto_fail);
        fields.into_value().to_string()
    }

    /// Handle click event for a button on the card
    fn handle_click(&self, _anc: ActionPlanAnc, _id: String) -> Vec<Action> {
        Vec::new()
    }

    /// Handle input event for an element on the card
    fn handle_input(&self, _anc: ActionPlanAnc, id: String) -> Vec<Action> {
        if &id == "phase" {
            let doc = Doc::get();
            let phase = doc.elem::<HtmlSelectElement>("phase").value();
            let mut fields = Fields::new();
            fields.insert_str("phase", &phase.to_string());
            let uri = uri_one(Res::ActionPlan, &self.name);
            let val = fields.into_value().to_string();
            return vec![Action::Patch(uri, val.into())];
        }
        Vec::new()
    }
}
