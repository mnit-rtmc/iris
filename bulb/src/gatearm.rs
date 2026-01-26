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
use crate::asset::Asset;
use crate::card::{AncillaryData, Card, View};
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::error::Result;
use crate::geoloc::{Loc, LocAnc};
use crate::item::{ItemState, ItemStates};
use crate::util::{ContainsLower, Fields, Input, TextArea, opt_ref};
use hatmil::{Page, html};
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
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(self.item_states(anc).to_string())
            .close();
        div = page.frag::<html::Div>();
        div.class("info fill")
            .cdata_len(opt_ref(&self.location), 32);
        String::from(page)
    }

    /// Convert to Control HTML
    fn to_html_control(&self) -> String {
        let mut page = Page::new();
        self.title(View::Control, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        item_states(self.arm_state).tooltips(&mut div.span());
        div.close();
        div = page.frag::<html::Div>();
        div.class("info").cdata_len(opt_ref(&self.location), 64);
        String::from(page)
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &GateArmAnc) -> String {
        let mut page = Page::new();
        self.title(View::Status, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        self.item_states(anc).tooltips(&mut div.span());
        div.close();
        div = page.frag::<html::Div>();
        div.class("info").cdata_len(opt_ref(&self.location), 64);
        String::from(page)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &GateArmAnc) -> String {
        let mut page = Page::new();
        self.title(View::Setup, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("notes").cdata("Notes").close();
        div.textarea()
            .id("notes")
            .maxlength(255)
            .rows(2)
            .cols(24)
            .cdata(opt_ref(&self.notes))
            .close();
        div.close();
        anc.cio.controller_html(self, &mut page.frag::<html::Div>());
        anc.cio.pin_html(self.pin, &mut page.frag::<html::Div>());
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("opposing").cdata("Opposing").close();
        let mut input = div.input();
        input.id("opposing").r#type("checkbox");
        if let Some(true) = self.opposing {
            input.checked();
        }
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("downstream")
            .cdata("Downstream (#tag)")
            .close();
        div.input()
            .id("downstream")
            .maxlength(16)
            .size(16)
            .value(opt_ref(&self.downstream_hashtag));
        div.close();
        self.footer_html(true, &mut page.frag::<html::Div>());
        String::from(page)
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
