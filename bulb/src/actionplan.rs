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
use crate::card::{AncillaryData, Card, footer_html, uri_all, uri_one};
use crate::dayplan::{DayMatcher, DayPlan};
use crate::devaction::DeviceAction;
use crate::error::Result;
use crate::fetch::Action;
use crate::item::{ItemState, ItemStates};
use crate::msgpattern::MsgPattern;
use crate::notes::contains_hashtag;
use crate::phaseaction::PhaseAction;
use crate::planphase::PlanPhase;
use crate::util::{
    ContainsLower, Doc, Fields, Input, Select, TextArea, opt_ref,
};
use crate::view::View;
use hatmil::{Tree, html};
use jiff::{Zoned, civil::Date};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use std::collections::BTreeSet;
use wasm_bindgen::JsValue;
use web_sys::HtmlSelectElement;

/// Hashtag resource
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct HashtagResource {
    pub hashtag: String,
    pub resource_n: String,
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
}

/// Action plan ancillary data
#[derive(Default)]
pub struct ActionPlanAnc {
    assets: Vec<Asset>,
    view: Option<View>,
    pub phases: Vec<PlanPhase>,
    pub day_plans: Vec<DayPlan>,
    pub day_matchers: Vec<DayMatcher>,
    pub device_actions: Vec<DeviceAction>,
    pub hashtag_resources: Vec<HashtagResource>,
    pub phase_actions: Vec<PhaseAction>,
    pub msg_patterns: Vec<MsgPattern>,
    pub next_device_action: String,
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
                    Asset::DayPlans,
                    Asset::DayMatchers,
                    Asset::DeviceActions,
                    Asset::HashtagResources,
                    Asset::PhaseActions,
                    Asset::PlanPhases,
                ]
            }
            View::Setup(_edit) => {
                vec![
                    Asset::DeviceActions,
                    Asset::PlanPhases,
                    Asset::MsgPatterns,
                ]
            }
            _ => vec![],
        };
        ActionPlanAnc {
            assets,
            view: Some(view),
            ..Default::default()
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
            Asset::DayPlans => {
                self.day_plans = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::DayMatchers => {
                self.day_matchers = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::DeviceActions => {
                let mut actions: Vec<DeviceAction> =
                    serde_wasm_bindgen::from_value(value)?;
                if let Some(View::Control) | Some(View::Setup(_)) = self.view {
                    actions.retain(|da| da.action_plan == pri.name);
                    self.next_device_action =
                        pri.next_device_action_name(&actions);
                }
                self.device_actions = actions;
            }
            Asset::HashtagResources => {
                self.hashtag_resources = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::PhaseActions => {
                let mut actions: Vec<PhaseAction> =
                    serde_wasm_bindgen::from_value(value)?;
                actions.retain(|pa| pa.action_plan == pri.name);
                self.phase_actions = actions;
            }
            Asset::MsgPatterns => {
                let mut patterns: Vec<MsgPattern> =
                    serde_wasm_bindgen::from_value(value)?;
                patterns.sort();
                self.msg_patterns = patterns;
            }
            _ => unreachable!(),
        }
        Ok(())
    }
}

impl ActionPlanAnc {
    /// Check if a day plan is active on a day (not holiday)
    fn is_day_plan_active(&self, nm: &str, day: &Date) -> bool {
        for dp in &self.day_plans {
            if dp.name == nm {
                for dm in &self.day_matchers {
                    if dm.day_plan == nm {
                        if dm.matches(day) {
                            return !dp.holidays;
                        } else {
                            return dp.holidays;
                        }
                    }
                }
                return dp.holidays;
            }
        }
        false
    }

    /// Check if a phase action is active on a given day
    fn is_phase_action_active(&self, pa: &PhaseAction, day: &Date) -> bool {
        if let Some(dp) = &pa.day_plan
            && !self.is_day_plan_active(dp, day)
        {
            return false;
        }
        pa.is_active(day)
    }

    /// Check if any phase actions are active for a given daay
    fn has_active_phase_actions(&self, day: &Date) -> bool {
        self.phase_actions
            .iter()
            .any(|pa| self.is_phase_action_active(pa, day))
    }

    /// Get action plan phases
    fn plan_phases<'a>(
        &'a self,
        pri: &'a ActionPlan,
    ) -> impl Iterator<Item = &'a str> {
        let mut phases = BTreeSet::new();
        phases.insert(&pri.default_phase[..]);
        for da in &self.device_actions {
            phases.insert(&da.phase[..]);
        }
        phases.into_iter()
    }

    /// Get device hashtags for a resource type
    fn res_hashtags(&self, res: Res) -> impl Iterator<Item = &str> {
        let mut tags = BTreeSet::new();
        for da in &self.device_actions {
            if self.has_hashtag_res(&da.hashtag, res) {
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
    /// Create next available device action name
    fn next_device_action_name(&self, actions: &[DeviceAction]) -> String {
        let nm = &self.name;
        let mut num = 1;
        for da in actions {
            if let Some((pre, suffix)) = da.name.rsplit_once('_')
                && pre == nm
                && let Ok(n) = suffix.parse::<u32>()
            {
                num = num.max(n + 1);
            }
        }
        format!("{nm}_{num}")
    }

    /// Get device hashtags for a resource type
    fn has_device_hashtag(&self, anc: &ActionPlanAnc, res: Res) -> bool {
        for da in &anc.device_actions {
            if da.action_plan == self.name
                && anc.has_hashtag_res(&da.hashtag, res)
            {
                return true;
            }
        }
        false
    }

    /// Get item state
    fn item_states(&self, anc: &ActionPlanAnc) -> ItemStates<'_> {
        let mut states = ItemStates::default();
        if self.active {
            if self.phase == self.default_phase {
                states = states.with(ItemState::Available, "");
            } else {
                states = states.with(ItemState::Deployed, "");
            }
            if self.has_device_hashtag(anc, Res::Beacon) {
                states = states.with(ItemState::Beacon, "");
            }
            if self.has_device_hashtag(anc, Res::Camera) {
                states = states.with(ItemState::Camera, "");
            }
            if self.has_device_hashtag(anc, Res::Dms) {
                states = states.with(ItemState::Dms, "");
            }
            if self.has_device_hashtag(anc, Res::GateArm) {
                states = states.with(ItemState::GateArm, "");
            }
            if self.has_device_hashtag(anc, Res::RampMeter) {
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
        for p in anc.plan_phases(self) {
            let mut option = select.option();
            if p == self.phase {
                option.selected();
            }
            option.cdata(p).close();
        }
        div.close();
        let today = Zoned::now().date();
        if anc.has_active_phase_actions(&today) {
            let mut details = tree.root::<html::Details>();
            details
                .open()
                .summary()
                .cdata("🗓️ Today's Schedule")
                .close();
            let mut table = details.table();
            for pa in &anc.phase_actions {
                if anc.is_phase_action_active(pa, &today) {
                    pa.table_row(&mut table.tr());
                }
            }
            details.close();
        }
        let tags = anc.res_hashtags(Res::Beacon).collect::<Vec<_>>().join(" ");
        if !tags.is_empty() {
            let mut details = tree.root::<html::Details>();
            details.summary().cdata("🔆 Beacon Hashtags").close();
            details.span().class("info").cdata(tags);
            details.close();
        }
        let tags = anc.res_hashtags(Res::Camera).collect::<Vec<_>>().join(" ");
        if !tags.is_empty() {
            let mut details = tree.root::<html::Details>();
            details.summary().cdata("🎥 Camera Hashtags").close();
            details.span().class("info").cdata(tags);
            details.close();
        }
        let tags = anc.res_hashtags(Res::Dms).collect::<Vec<_>>().join(" ");
        if !tags.is_empty() {
            let mut details = tree.root::<html::Details>();
            details.summary().cdata("⬛ DMS Hashtags").close();
            details.span().class("info").cdata(tags);
            details.close();
        }
        let tags = anc.res_hashtags(Res::GateArm).collect::<Vec<_>>().join(" ");
        if !tags.is_empty() {
            let mut details = tree.root::<html::Details>();
            details.summary().cdata("⫬ Gate Arm Hashtags").close();
            details.span().class("info").cdata(tags);
            details.close();
        }
        let tags = anc
            .res_hashtags(Res::RampMeter)
            .collect::<Vec<_>>()
            .join(" ");
        if !tags.is_empty() {
            let mut details = tree.root::<html::Details>();
            details.summary().cdata("🚦 Ramp Meter Hashtags").close();
            details.span().class("info").cdata(tags);
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
            .rows(2)
            .cols(22)
            .cdata(opt_ref(&self.notes))
            .close();
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
        div.label().r#for("active").cdata("Active").close();
        let mut input = div.input();
        input.id("active").r#type("checkbox");
        if self.active {
            input.checked();
        }
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
        if !anc.device_actions.is_empty() {
            div = tree.root::<html::Div>();
            div.class("row").cdata("Device Actions").close();
        }
        for da in &anc.device_actions {
            let mut details = tree.root::<html::Details>();
            da.details_html(&anc.phases, &anc.msg_patterns, &mut details);
        }
        let da = DeviceAction::new(
            &anc.next_device_action,
            &self.name,
            &self.default_phase,
        );
        let mut details = tree.root::<html::Details>();
        da.details_html(&anc.phases, &anc.msg_patterns, &mut details);
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
            ItemState::GateArm,
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
        fields.into_value().to_string()
    }

    /// Handle input event for an element on the card
    #[allow(clippy::field_reassign_with_default)]
    fn handle_input(&self, anc: ActionPlanAnc, id: &str) -> Vec<Action> {
        // Control card only
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
        // Setup card only
        for da in &anc.device_actions {
            let mut nda = da.clone();
            nda.update_from_inputs();
            if nda.update_class(id) {
                break;
            }
        }
        let mut da = DeviceAction::new(
            &anc.next_device_action,
            &self.name,
            &self.default_phase,
        );
        da.update_from_inputs();
        da.update_class(id);
        Vec::new()
    }

    /// Handle click event for the save button
    #[allow(clippy::field_reassign_with_default)]
    fn handle_save(&self, anc: Self::Ancillary) -> Vec<Action> {
        let mut actions = Vec::new();
        for da in &anc.device_actions {
            let mut nda = da.clone();
            nda.update_from_inputs();
            if !nda.is_valid() {
                let uri = uri_one(Res::DeviceAction, &da.name);
                actions.push(Action::Delete(uri));
                continue;
            }
            if nda != *da {
                let fields = nda.changed_fields(da);
                let uri = uri_one(Res::DeviceAction, &da.name);
                let val = fields.into_value().to_string();
                actions.push(Action::Patch(uri, val.into()));
            }
        }
        let da = DeviceAction::new(
            &anc.next_device_action,
            &self.name,
            &self.default_phase,
        );
        let mut nda = da.clone();
        nda.update_from_inputs();
        if nda.is_valid() {
            let post_uri = uri_all(Res::DeviceAction);
            let patch_uri = uri_one(Res::DeviceAction, &nda.name);
            let mut fields = nda.changed_fields(&da);
            fields.insert_str("name", &nda.name);
            let value = nda.value().to_string();
            actions.push(Action::Post(post_uri, value.into()));
            let changed = fields.into_value().to_string();
            actions.push(Action::Patch(patch_uri, changed.into()));
        }
        actions
    }
}
