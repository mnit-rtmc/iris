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
use crate::card::{AncillaryData, Card, footer_html, uri_one};
use crate::error::Result;
use crate::fetch::Action;
use crate::item::{ItemState, ItemStates};
use crate::notes::contains_hashtag;
use crate::planphase::PlanPhase;
use crate::util::{
    ContainsLower, Doc, Fields, Input, Select, TextArea, opt_ref,
};
use crate::view::View;
use hatmil::{Tree, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use std::collections::BTreeSet;
use wasm_bindgen::JsValue;
use web_sys::HtmlSelectElement;

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
    /// Make HTML table row
    fn table_row<'p>(&self, tr: &'p mut html::Tr<'p>) {
        if let Some(day_plan) = &self.day_plan {
            tr.td().cdata(day_plan).close();
        }
        if let Some(sched_date) = &self.sched_date {
            tr.td().cdata(sched_date).close();
        }
        tr.td().cdata(&self.time_of_day).close();
        tr.td().cdata("⇨ ").cdata(&self.phase).close();
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
        let assets = match view {
            View::SearchEv | View::Compact => {
                vec![Asset::HashtagResources, Asset::DeviceActions]
            }
            View::Control => {
                vec![
                    Asset::TimeActions,
                    Asset::HashtagResources,
                    Asset::DeviceActions,
                    Asset::PlanPhases,
                ]
            }
            View::Setup(_edit) => vec![Asset::DeviceActions, Asset::PlanPhases],
            _ => vec![],
        };
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

    /// Get device hashtags for a resource type
    fn hashtags<'a>(
        &'a self,
        pri: &'a ActionPlan,
        res: Res,
    ) -> impl Iterator<Item = &'a str> {
        let mut tags = BTreeSet::new();
        for da in &self.device_actions {
            if da.action_plan == pri.name
                && self.has_hashtag_res(&da.hashtag, res)
            {
                tags.insert(&da.hashtag[..]);
            }
        }
        tags.into_iter()
    }

    /// Check a device hashtag for a resource type
    fn has_hashtag_res(&self, hashtag: &str, res: Res) -> bool {
        for hr in &self.hashtag_resources {
            if hr.resource_n == res.as_str()
                && contains_hashtag(hashtag, &hr.hashtag)
            {
                return true;
            }
        }
        false
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
            if anc.hashtags(self, Res::Beacon).next().is_some() {
                states = states.with(ItemState::Beacon, "");
            }
            if anc.hashtags(self, Res::Camera).next().is_some() {
                states = states.with(ItemState::Camera, "");
            }
            if anc.hashtags(self, Res::Dms).next().is_some() {
                states = states.with(ItemState::Dms, "");
            }
            if anc.hashtags(self, Res::RampMeter).next().is_some() {
                states = states.with(ItemState::RampMeter, "");
            }
        } else {
            states = states.with(ItemState::Inactive, "");
        }
        states
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &ActionPlanAnc) -> String {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(self.item_states(anc).to_string());
        String::from(tree)
    }

    /// Convert to Control HTML
    fn to_html_control(&self, anc: &ActionPlanAnc) -> String {
        let mut tree = Tree::new();
        self.title(View::Control, &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row fill");
        self.item_states(anc).spans(&mut div.span());
        div.close();
        if let Some(notes) = self.notes.as_ref() {
            div = tree.root::<html::Div>();
            div.class("row");
            div.span().cdata(notes).close();
            div.close();
        }
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("phase").cdata("Phase").close();
        let mut select = div.select();
        select.id("phase");
        for p in anc.phases(self) {
            let mut option = select.option();
            if p == self.phase {
                option.selected();
            }
            option.cdata(p).close();
        }
        div.close();
        let tags = anc
            .hashtags(self, Res::Beacon)
            .collect::<Vec<_>>()
            .join(" ");
        if !tags.is_empty() {
            let mut details = tree.root::<html::Details>();
            details.summary().cdata("🔆 Beacon Hashtags").close();
            details.span().class("info").cdata(tags);
            details.close();
        }
        let tags = anc
            .hashtags(self, Res::Camera)
            .collect::<Vec<_>>()
            .join(" ");
        if !tags.is_empty() {
            let mut details = tree.root::<html::Details>();
            details.summary().cdata("🎥 Camera Hashtags").close();
            details.span().class("info").cdata(tags);
            details.close();
        }
        let tags = anc.hashtags(self, Res::Dms).collect::<Vec<_>>().join(" ");
        if !tags.is_empty() {
            let mut details = tree.root::<html::Details>();
            details.summary().cdata("⬛ DMS Hashtags").close();
            details.span().class("info").cdata(tags);
            details.close();
        }
        let tags = anc
            .hashtags(self, Res::RampMeter)
            .collect::<Vec<_>>()
            .join(" ");
        if !tags.is_empty() {
            let mut details = tree.root::<html::Details>();
            details.summary().cdata("🚦 Ramp Meter Hashtags").close();
            details.span().class("info").cdata(tags);
            details.close();
        }
        if !anc.time_actions.is_empty() {
            let mut details = tree.root::<html::Details>();
            details.summary().cdata("🗓️ Schedule").close();
            let mut table = details.table();
            for ta in &anc.time_actions {
                ta.table_row(&mut table.tr());
            }
            details.close();
        }
        String::from(tree)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &ActionPlanAnc, edit: bool) -> String {
        let mut tree = Tree::new();
        self.title(View::Setup(edit), &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("notes").cdata("Notes").close();
        div.textarea()
            .id("notes")
            .maxlength(128)
            .rows(3)
            .cols(22)
            .cdata(opt_ref(&self.notes))
            .close();
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("active").cdata("Active").close();
        let mut input = div.input();
        input.id("active").r#type("checkbox");
        if self.active {
            input.checked();
        }
        div.close();
        div = tree.root::<html::Div>();
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
        div = tree.root::<html::Div>();
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
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("sticky").cdata("Sticky").close();
        input = div.input();
        input.id("sticky").r#type("checkbox");
        if let Some(true) = self.sticky {
            input.checked();
        }
        div.close();
        div = tree.root::<html::Div>();
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
        footer_html(View::Setup(edit), true, &mut tree.root::<html::Div>());
        String::from(tree)
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
            View::Create => self.to_html_create(16),
            View::Control => self.to_html_control(anc),
            View::Setup(edit) => self.to_html_setup(anc, edit),
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

    /// Handle input event for an element on the card
    fn handle_input(&self, _anc: ActionPlanAnc, id: &str) -> Vec<Action> {
        if "phase" == id
            && let Some(el) = Doc::get().opt_elem::<HtmlSelectElement>("phase")
        {
            let phase = el.value();
            let mut fields = Fields::new();
            fields.insert_str("phase", &phase.to_string());
            let uri = uri_one(Res::ActionPlan, &self.name);
            let val = fields.into_value().to_string();
            return vec![Action::Patch(uri, val.into())];
        }
        Vec::new()
    }
}
