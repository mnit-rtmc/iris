// Copyright (C) 2022-2026  Minnesota Department of Transportation
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
use crate::actionplan::ActionPlan;
use crate::asset::Asset;
use crate::card::{AncillaryData, Card, footer_html};
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::devaction::DeviceAction;
use crate::error::Result;
use crate::geoloc::LocAnc;
use crate::item::{ItemState, ItemStates};
use crate::map;
use crate::notes::contains_hashtag;
use crate::query::QueryParam;
use crate::util::{ContainsLower, Doc, Fields, Input, TextArea, opt_ref};
use crate::view::View;
use hatmil::{Tree, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use std::collections::BTreeSet;
use wasm_bindgen::JsValue;
use web_sys::HtmlElement;

/// Gate arm states
#[derive(Debug, Deserialize)]
#[allow(dead_code)]
pub struct GateArmState {
    pub id: u32,
    pub description: String,
}

/// Gate Arm
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct GateArm {
    pub name: String,
    pub location: Option<String>,
    pub controller: Option<String>,
    pub notes: Option<String>,
    pub arm_state: u32,
    pub interlock: u32,
    // secondary attributes
    pub geo_loc: Option<String>,
    pub pin: Option<u32>,
    pub preset: Option<String>,
    pub opposing: Option<bool>,
    pub downstream_hashtag: Option<String>,
}

/// Ancillary gate arm data
#[derive(Debug, Default)]
pub struct GateArmAnc {
    cio: ControllerIoAnc<GateArm>,
    loc: LocAnc<GateArm>,
    states: Vec<GateArmState>,
    device_actions: Vec<DeviceAction>,
    action_plans: Vec<ActionPlan>,
}

/// Get gate arm item states
pub fn item_states(arm_state: u32) -> ItemStates<'static> {
    match arm_state {
        1 => ItemState::Fault.into(),
        2 => ItemState::Opening.into(),
        3 => ItemState::Open.into(),
        4 => ItemState::Closing.into(),
        5 => ItemState::Closed.into(),
        _ => ItemState::Unknown.into(),
    }
}

impl GateArmAnc {
    /// Get action plan phases
    fn phases<'a>(
        &'a self,
        plan: &'a ActionPlan,
    ) -> impl Iterator<Item = &'a str> {
        let mut phases = BTreeSet::new();
        phases.insert(&plan.default_phase[..]);
        for da in &self.device_actions {
            if da.action_plan == plan.name {
                phases.insert(&da.phase[..]);
            }
        }
        phases.into_iter()
    }
}

impl AncillaryData for GateArmAnc {
    type Primary = GateArm;

    /// Construct ancillary gate arm data
    fn new(pri: &GateArm, view: View) -> Self {
        let mut cio = ControllerIoAnc::new(pri, view);
        if let View::SearchEv | View::Control = view {
            cio.assets.push(Asset::GateArmStates);
        }
        if let View::Control = view {
            cio.assets.push(Asset::DeviceActions);
        }
        let loc = LocAnc::new(pri, view);
        GateArmAnc {
            cio,
            loc,
            ..Default::default()
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.cio.assets.pop().or_else(|| self.loc.assets.pop())
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &GateArm,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::Controllers => self.cio.set_asset(pri, asset, value)?,
            Asset::GateArmStates => {
                self.states = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::DeviceActions => {
                let mut act: Vec<DeviceAction> =
                    serde_wasm_bindgen::from_value(value)?;
                act.retain(|da| pri.has_hashtag(&da.hashtag));
                self.device_actions = act;
                // now that we have device actions, let's fetch the plans
                self.cio.assets.push(Asset::ActionPlans);
            }
            Asset::ActionPlans => {
                let mut plans: Vec<ActionPlan> =
                    serde_wasm_bindgen::from_value(value)?;
                plans.retain(|p| {
                    p.active
                        && self
                            .device_actions
                            .iter()
                            .any(|da| da.action_plan == p.name)
                });
                self.action_plans = plans;
            }
            _ => self.loc.set_asset(pri, asset, value)?,
        }
        Ok(())
    }
}

impl ControllerIo for GateArm {
    /// Get controller name
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl GateArm {
    /// Check if gate arm has a given hashtag
    fn has_hashtag(&self, hashtag: &str) -> bool {
        match &self.notes {
            Some(notes) => contains_hashtag(notes, hashtag),
            None => false,
        }
    }

    /// Get item states
    fn item_states<'a>(&'a self, anc: &'a GateArmAnc) -> ItemStates<'a> {
        let states = anc.cio.item_states(self);
        if states.contains(ItemState::Available) {
            item_states(self.arm_state)
        } else {
            states
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &GateArmAnc) -> String {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(self.item_states(anc).to_string())
            .close();
        div = tree.root::<html::Div>();
        div.class("info fill")
            .cdata_len(opt_ref(&self.location), 32);
        String::from(tree)
    }

    /// Convert to Control HTML
    fn to_html_control(&self, anc: &GateArmAnc) -> String {
        if let Some((lon, lat)) = anc.loc.lonlat() {
            map::present_item(Res::GateArm, &self.name, lon, lat);
        }
        let mut tree = Tree::new();
        self.title(View::Control, &mut tree.root::<html::Div>());
        self.render_state_row(anc, &mut tree.root::<html::Div>());
        self.render_location_row(&mut tree.root::<html::Div>());
        if self.item_state_main(anc) != ItemState::Inactive {
            self.render_action_plans(anc, &mut tree.root::<html::Details>());
        }
        String::from(tree)
    }

    /// Render item state row as an HTML div element
    fn render_state_row<'p>(
        &self,
        anc: &GateArmAnc,
        div: &'p mut html::Div<'p>,
    ) {
        div.id("state-row").class("row fill");
        self.item_states(anc).spans(&mut div.span());
        div.close();
    }

    /// Render location row as an HTML div element
    fn render_location_row<'p>(&self, div: &'p mut html::Div<'p>) {
        div.id("location-row")
            .class("info fill")
            .cdata_len(opt_ref(&self.location), 64)
            .close();
    }

    /// Render action plans as HTML
    fn render_action_plans<'p>(
        &self,
        anc: &GateArmAnc,
        details: &'p mut html::Details<'p>,
    ) {
        if anc.action_plans.is_empty() {
            return;
        }
        details.summary().cdata("📋 Action Plans").close();
        let mut div = details.div();
        div.class("row fill");
        div.span().cdata("Name").close();
        div.span().cdata("Phase").close();
        div.close();
        for act in &anc.action_plans {
            let mut div = details.div();
            div.class("row fill");
            let query = QueryParam::new()
                .with_res(Res::ActionPlan)
                .with_sel(&act.name);
            div.a().href(query.to_string()).cdata(&act.name).close();
            let mut span = div.span();
            let mut select = span.select();
            select.id("phase").disabled();
            for p in anc.phases(act) {
                let mut option = select.option();
                if p == act.phase {
                    option.selected();
                }
                option.cdata(p).close();
            }
            select.close();
            span.close();
            div.close();
        }
        details.close();
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &GateArmAnc, edit: bool) -> String {
        let mut tree = Tree::new();
        self.title(View::Setup(edit), &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("notes").cdata("Notes").close();
        div.textarea()
            .id("notes")
            .maxlength(255)
            .rows(2)
            .cols(22)
            .cdata(opt_ref(&self.notes))
            .close();
        div.close();
        anc.cio.controller_html(self, &mut tree.root::<html::Div>());
        anc.cio.pin_html(self.pin, &mut tree.root::<html::Div>());
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("opposing").cdata("Opposing").close();
        let mut input = div.input();
        input.id("opposing").r#type("checkbox");
        if let Some(true) = self.opposing {
            input.checked();
        }
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label()
            .r#for("downstream")
            .cdata("Downstream (#Tag)")
            .close();
        div.input()
            .id("downstream")
            .maxlength(16)
            .size(16)
            .value(opt_ref(&self.downstream_hashtag));
        div.close();
        footer_html(View::Setup(edit), true, &mut tree.root::<html::Div>());
        String::from(tree)
    }
}

impl Card for GateArm {
    type Ancillary = GateArmAnc;

    /// Get the resource
    fn res() -> Res {
        Res::GateArm
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[
            ItemState::Opening,
            ItemState::Open,
            ItemState::Closing,
            ItemState::Closed,
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
        let states = self.item_states(anc);
        if states.contains(ItemState::Inactive) {
            ItemState::Inactive
        } else if states.contains(ItemState::Offline) {
            ItemState::Offline
        } else if states.contains(ItemState::Open) {
            ItemState::Open
        } else if states.contains(ItemState::Closed) {
            ItemState::Closed
        } else if states.contains(ItemState::Opening) {
            ItemState::Opening
        } else if states.contains(ItemState::Closing) {
            ItemState::Closing
        } else {
            ItemState::Fault
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &GateArmAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || self.notes.contains_lower(search)
            || self.item_states(anc).is_match(search)
    }

    /// Get geo location name
    fn geoloc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &GateArmAnc) -> String {
        match view {
            View::Create => self.to_html_create(20),
            View::Control => self.to_html_control(anc),
            View::Setup(edit) => self.to_html_setup(anc, edit),
            View::Location(edit) => anc.loc.to_html_loc(self, edit),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields on Location view
    fn changed_location(&self, anc: GateArmAnc) -> String {
        anc.loc.changed_location()
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_text_area("notes", &self.notes);
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.changed_input("opposing", self.opposing);
        fields.changed_input("downstream_hashtag", &self.downstream_hashtag);
        fields.into()
    }

    /// Handle updating a card in response to an SSE notification
    fn handle_update(&self, anc: GateArmAnc) {
        let doc = Doc::get();
        if let Some(row) = doc.opt_elem::<HtmlElement>("state-row") {
            let mut tree = Tree::new();
            self.render_state_row(&anc, &mut tree.root::<html::Div>());
            row.set_outer_html(&String::from(tree));
        }
        if let Some(row) = doc.opt_elem::<HtmlElement>("location-row") {
            let mut tree = Tree::new();
            self.render_location_row(&mut tree.root::<html::Div>());
            row.set_outer_html(&String::from(tree));
        }
    }
}
