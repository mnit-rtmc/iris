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
use crate::error::Result;
use crate::fetch::Action;
use crate::item::{ItemState, ItemStates};
use crate::msgpattern::MsgPattern;
use crate::msgpriority::MsgPriority;
use crate::notes::contains_hashtag;
use crate::planphase::PlanPhase;
use crate::util::{
    ContainsLower, Doc, Fields, Input, Select, TextArea, opt_ref,
};
use crate::view::View;
use hatmil::{Tree, html};
use jiff::{Zoned, civil::Date};
use resources::Res;
use serde::Deserialize;
use serde_json::Value;
use serde_json::map::Map;
use std::borrow::Cow;
use std::collections::BTreeSet;
use wasm_bindgen::JsValue;
use web_sys::{HtmlElement, HtmlSelectElement};

/// Device action
#[derive(Clone, Debug, Deserialize, PartialEq)]
pub struct DeviceAction {
    pub name: String,
    pub action_plan: String,
    pub hashtag: String,
    pub phase: String,
    pub msg_pattern: Option<String>,
    pub msg_priority: u8,
    pub sticky: bool,
    pub ignore_auto_fail: bool,
}

/// Hashtag resource
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct HashtagResource {
    pub hashtag: String,
    pub resource_n: String,
}

/// Phase action
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct PhaseAction {
    pub name: String,
    pub action_plan: String,
    pub day_plan: Option<String>,
    pub condition: u32,
    pub params: Option<String>,
    pub from_phase: Option<String>,
    pub to_phase: String,
}

impl DeviceAction {
    /// Create a new device action
    fn new(name: &str, ap: &str, phase: &str) -> Self {
        DeviceAction {
            name: name.to_string(),
            action_plan: ap.to_string(),
            hashtag: String::new(),
            phase: phase.to_string(),
            msg_pattern: None,
            msg_priority: MsgPriority::Medium1 as u8,
            sticky: false,
            ignore_auto_fail: false,
        }
    }

    /// Get ID for summary
    fn id_summary(&self) -> String {
        format!("{}-summary", self.name)
    }

    /// Get ID for hashtag `<input>`
    fn id_hashtag(&self) -> String {
        format!("{}-hashtag", self.name)
    }

    /// Get ID for phase `<select>`
    fn id_phase(&self) -> String {
        format!("{}-phase", self.name)
    }

    /// Get ID for msg_pattern `<select>`
    fn id_msg_pattern(&self) -> String {
        format!("{}-msg_pattern", self.name)
    }

    /// Get ID for msg_priority `<input>`
    fn id_msg_priority(&self) -> String {
        format!("{}-msg_priority", self.name)
    }

    /// Get ID for sticky `<input>`
    fn id_sticky(&self) -> String {
        format!("{}-sticky", self.name)
    }

    /// Get ID for ignore_auto_fail `<input>`
    fn id_ignore_auto_fail(&self) -> String {
        format!("{}-ignore_auto_fail", self.name)
    }

    /// Update from input elements
    fn update_inputs(&mut self) {
        let doc = Doc::get();
        self.hashtag = doc
            .input_parse::<String>(&self.id_hashtag())
            .unwrap_or_default();
        self.phase = doc
            .select_parse::<String>(&self.id_phase())
            .unwrap_or_default();
        self.msg_pattern = doc
            .select_parse::<String>(&self.id_msg_pattern())
            .filter(|p| !p.is_empty());
        self.msg_priority =
            doc.select_parse::<u8>(&self.id_msg_priority()).unwrap_or(6);
        self.sticky = doc
            .input_parse::<bool>(&self.id_sticky())
            .unwrap_or_default();
        self.ignore_auto_fail = doc
            .input_parse::<bool>(&self.id_ignore_auto_fail())
            .unwrap_or_default();
        if let Ok(el) = Doc::get().elem::<HtmlElement>(&self.id_summary()) {
            let mut tree = Tree::new();
            let mut summary = tree.root::<html::Summary>();
            summary.id(self.id_summary());
            summary.span().class("info").cdata(&self.hashtag).close();
            if let Some(msg_pattern) = &self.msg_pattern {
                summary.cdata(": ").cdata(msg_pattern);
            }
            el.set_outer_html(&String::from(tree));
        }
    }

    /// Check if device action is valid
    fn is_valid(&self) -> bool {
        self.hashtag.len() > 1
            && self.hashtag.starts_with('#')
            && self.hashtag[1..].chars().all(|c| c.is_alphanumeric())
    }

    /// Update table row class with valid state
    fn update_valid(&self, id: &str) -> bool {
        if id == self.id_hashtag() {
            if let Some(el) = Doc::get().opt_elem::<HtmlElement>(&self.name) {
                el.set_class_name(self.class_name());
            }
            true
        } else {
            false
        }
    }

    /// Get row element class name
    fn class_name(&self) -> &'static str {
        if self.is_valid() { "" } else { "invalid" }
    }

    /// Build HTML hashtag row
    fn hashtag_row<'p>(&self, div: &'p mut html::Div<'p>) {
        let id = self.id_hashtag();
        div.label().r#for(&id).cdata("Device #Tag").close();
        let mut input = div.input();
        input.id(id).maxlength(16).value(&self.hashtag);
        div.close();
    }

    /// Build HTML phase row
    fn phase_row<'p>(&self, anc: &ActionPlanAnc, div: &'p mut html::Div<'p>) {
        let id = self.id_phase();
        div.label().r#for(&id).cdata("Phase").close();
        let mut select = div.select();
        select.id(id);
        for p in &anc.phases {
            let mut option = select.option();
            if p.name == self.phase {
                option.selected();
            }
            option.cdata(&p.name).close();
        }
        select.close();
        div.close();
    }

    /// Build HTML msg_pattern row
    fn msg_pattern_row<'p>(
        &self,
        anc: &ActionPlanAnc,
        div: &'p mut html::Div<'p>,
    ) {
        let id = self.id_msg_pattern();
        div.label().r#for(&id).cdata("Msg Pattern").close();
        let mut select = div.select();
        select.id(id);
        let mut option = select.option();
        if self.msg_pattern.is_none() {
            option.selected();
        }
        option.close();
        for p in &anc.msg_patterns {
            let mut option = select.option();
            if Some(p.name.as_str()) == self.msg_pattern.as_deref() {
                option.selected();
            }
            option.cdata(&p.name).close();
        }
        select.close();
        div.close();
    }

    /// Build HTML msg_priority row
    fn msg_priority_row<'p>(&self, div: &'p mut html::Div<'p>) {
        let prio = MsgPriority::try_from(self.msg_priority)
            .unwrap_or(MsgPriority::Medium1);
        let id = self.id_msg_priority();
        div.label().r#for(&id).cdata("Priority").close();
        let mut select = div.select();
        select.id(id);
        for p in 1..=15 {
            if let Ok(p) = MsgPriority::try_from(p) {
                let mut option = select.option();
                if p == prio {
                    option.selected();
                }
                option.cdata(p.as_str()).close();
            }
        }
        select.close();
        div.close();
    }

    /// Build HTML sticky row
    fn sticky_row<'p>(&self, div: &'p mut html::Div<'p>) {
        let id = self.id_sticky();
        div.label().r#for(&id).cdata("Sticky").close();
        let mut input = div.input();
        input.id(id).r#type("checkbox");
        if self.sticky {
            input.checked();
        }
        div.close();
    }

    /// Build HTML ignore_auto_fail row
    fn ignore_auto_fail_row<'p>(&self, div: &'p mut html::Div<'p>) {
        let id = self.id_ignore_auto_fail();
        div.label().r#for(&id).cdata("Ignore Auto-Fail").close();
        let mut input = div.input();
        input.id(id).r#type("checkbox");
        if self.ignore_auto_fail {
            input.checked();
        }
        div.close();
    }

    /// Make HTML details
    fn details<'p>(
        &self,
        anc: &ActionPlanAnc,
        details: &'p mut html::Details<'p>,
    ) {
        details.id(&self.name).class(self.class_name());
        let mut summary = details.summary();
        summary.id(self.id_summary());
        summary.span().class("info").cdata(&self.hashtag).close();
        if let Some(msg_pattern) = &self.msg_pattern {
            summary.cdata(": ").cdata(msg_pattern);
        }
        summary.close();
        self.hashtag_row(&mut details.div());
        self.phase_row(anc, &mut details.div());
        self.msg_pattern_row(anc, &mut details.div());
        self.msg_priority_row(&mut details.div());
        self.sticky_row(&mut details.div());
        self.ignore_auto_fail_row(&mut details.div());
        details.close();
    }

    /// Get set of changed fields
    fn changed_fields(&self, da: &Self) -> Fields {
        let mut fields = Fields::new();
        if self.hashtag != da.hashtag {
            fields.insert_str("hashtag", &self.hashtag);
        }
        if self.phase != da.phase {
            fields.insert_str("phase", &self.phase);
        }
        if self.msg_pattern != da.msg_pattern {
            fields.insert_opt_str("msg_pattern", self.msg_pattern.as_deref());
        }
        if self.msg_priority != da.msg_priority {
            fields.insert_num("msg_priority", self.msg_priority);
        }
        if self.sticky != da.sticky {
            fields.insert_bool("sticky", self.sticky);
        }
        if self.ignore_auto_fail != da.ignore_auto_fail {
            fields.insert_bool("ignore_auto_fail", self.ignore_auto_fail);
        }
        fields
    }

    /// Convert to JSON value (for POST)
    fn value(&self) -> Value {
        let mut obj = Map::new();
        obj.insert("name".to_string(), Value::String(self.name.to_string()));
        obj.insert(
            "action_plan".to_string(),
            Value::String(self.action_plan.to_string()),
        );
        Value::Object(obj)
    }
}

/// Action conditions
const CONDITIONS: &[&str] = &["⏳", "⏰", "🚗", "🌦️", "📢"];

impl PhaseAction {
    /// Check if a phase action is active on a given day
    fn is_active(&self, day: &Date) -> bool {
        if 1 == self.condition
            && let Some(params) = &self.params
            && let Ok(dt) = params.parse::<Date>()
        {
            dt == *day
        } else {
            true
        }
    }

    /// Make HTML table row
    fn table_row<'p>(&self, tr: &'p mut html::Tr<'p>) {
        let params = self.params.as_deref().unwrap_or("");
        match self.condition {
            0 => {
                tr.td().cdata(params).cdata(" sec").close();
            }
            1 if let Some((_d, t)) = params.split_once('T') => {
                tr.td().cdata(t).close();
            }
            _ => {
                tr.td().cdata(params).close();
            }
        };
        tr.td().cdata(CONDITIONS[self.condition as usize]).close();
        match &self.from_phase {
            Some(from_phase) => tr.td().cdata(from_phase).close(),
            None => tr.td().class("info").cdata("*any*").close(),
        };
        tr.td().cdata("⇨").close();
        tr.td().cdata(&self.to_phase).close();
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
    pub next_name: String,
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
                    self.next_name = pri.next_action_name(&actions);
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
    fn next_action_name(&self, actions: &[DeviceAction]) -> String {
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
            da.details(anc, &mut details);
        }
        let da =
            DeviceAction::new(&anc.next_name, &self.name, &self.default_phase);
        let mut details = tree.root::<html::Details>();
        da.details(anc, &mut details);
        // FIXME: add phase action table
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
            nda.update_inputs();
            if nda.update_valid(id) {
                break;
            }
        }
        let mut da =
            DeviceAction::new(&anc.next_name, &self.name, &self.default_phase);
        da.update_inputs();
        da.update_valid(id);
        Vec::new()
    }

    /// Handle click event for the save button
    #[allow(clippy::field_reassign_with_default)]
    fn handle_save(&self, anc: Self::Ancillary) -> Vec<Action> {
        let mut actions = Vec::new();
        for da in &anc.device_actions {
            let mut nda = da.clone();
            nda.update_inputs();
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
        let da =
            DeviceAction::new(&anc.next_name, &self.name, &self.default_phase);
        let mut nda = da.clone();
        nda.update_inputs();
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
