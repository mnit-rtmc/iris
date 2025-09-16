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
use crate::util::{ContainsLower, Fields, Input, TextArea, opt_ref};
use hatmil::Html;
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

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
    pub states: Vec<GateArmState>,
}

/// Get gate arm item states
pub fn item_states(arm_state: u32) -> ItemStates<'static> {
    match arm_state {
        1 => ItemState::Fault.into(),
        2 => ItemState::Opening.into(),
        3 => ItemState::Open.into(),
        5 => ItemState::Closing.into(),
        6 => ItemState::Closed.into(),
        _ => ItemState::Unknown.into(),
    }
}

impl AncillaryData for GateArmAnc {
    type Primary = GateArm;

    /// Construct ancillary gate arm data
    fn new(pri: &GateArm, view: View) -> Self {
        let mut cio = ControllerIoAnc::new(pri, view);
        if let View::Search | View::Control = view {
            cio.assets.push(Asset::GateArmStates);
        }
        let loc = LocAnc::new(pri, view);
        GateArmAnc {
            cio,
            loc,
            states: Vec::new(),
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

impl Loc for GateArm {
    /// Get geo location name
    fn geoloc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }
}

impl GateArm {
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
    fn to_html_control(&self) -> String {
        let mut html = self.title(View::Control);
        html.div().class("row");
        item_states(self.arm_state).tooltips(&mut html);
        html.end(); /* div */
        html.div()
            .class("info")
            .text_len(opt_ref(&self.location), 64);
        html.to_string()
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &GateArmAnc) -> String {
        let mut html = self.title(View::Status);
        html.div().class("row");
        self.item_states(anc).tooltips(&mut html);
        html.end(); /* div */
        html.div()
            .class("info")
            .text_len(opt_ref(&self.location), 64);
        html.to_string()
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &GateArmAnc) -> String {
        let mut html = self.title(View::Setup);
        html.div().class("row");
        html.label().r#for("notes").text("Notes").end();
        html.textarea()
            .id("notes")
            .maxlength("255")
            .attr("rows", "2")
            .attr("cols", "24")
            .text(opt_ref(&self.notes))
            .end();
        html.end(); /* div */
        anc.cio.controller_html(self, &mut html);
        anc.cio.pin_html(self.pin, &mut html);
        html.div().class("row");
        html.label().r#for("opposing").text("Opposing").end();
        let opposing = html.input().id("opposing").r#type("checkbox");
        if let Some(true) = self.opposing {
            opposing.checked();
        }
        html.end(); /* div */
        html.div().class("row");
        html.label()
            .r#for("downstream")
            .text("Downstream (#tag)")
            .end();
        html.input()
            .id("downstream")
            .maxlength("16")
            .size("16")
            .value(opt_ref(&self.downstream_hashtag));
        html.end(); /* div */
        self.footer_html(true, &mut html);
        html.to_string()
    }
}

impl Card for GateArm {
    type Ancillary = GateArmAnc;

    /// Display name
    const DNAME: &'static str = "⫬ Gate Arm";

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

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &GateArmAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || self.notes.contains_lower(search)
            || self.item_states(anc).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &GateArmAnc) -> String {
        match view {
            View::Control => self.to_html_control(),
            View::Create => self.to_html_create(anc),
            View::Location => anc.loc.to_html_loc(self),
            View::Status => self.to_html_status(anc),
            View::Setup => self.to_html_setup(anc),
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
        fields.into_value().to_string()
    }
}
