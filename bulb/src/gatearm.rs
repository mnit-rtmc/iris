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
use crate::item::{ItemState, ItemStates};
use crate::util::{ContainsLower, Fields, HtmlStr, Input};
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
    // secondary attributes
    pub pin: Option<u32>,
}

/// Ancillary gate arm data
#[derive(Debug, Default)]
pub struct GateArmAnc {
    cio: ControllerIoAnc<GateArm>,
    pub states: Vec<GateArmState>,
}

/// Get gate arm item states
pub fn item_states(arm_state: u32) -> ItemStates<'static> {
    match arm_state {
        1 => ItemState::Fault.into(),
        2 => ItemState::Opening.into(),
        3 => ItemState::Open.into(),
        4 => ItemState::WarnClose.into(),
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
        cio.assets.push(Asset::GateArmStates);
        GateArmAnc {
            cio,
            states: Vec::new(),
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.cio.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &GateArm,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::GateArmStates => {
                self.states = serde_wasm_bindgen::from_value(value)?;
            }
            _ => self.cio.set_asset(pri, asset, value)?,
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
        let name = HtmlStr::new(self.name());
        let item_states = self.item_states(anc);
        let location = HtmlStr::new(&self.location).with_len(32);
        format!(
            "<div class='title row'>{name} {item_states}</div>\
            <div class='info fill'>{location}</div>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &GateArmAnc) -> String {
        let title = String::from(self.title(View::Status));
        let location = HtmlStr::new(&self.location).with_len(64);
        let mut html = Html::new();
        self.item_states(anc).tooltips(&mut html);
        let item_states = String::from(html);
        format!(
            "{title}\
            <div class='row'>{item_states}</div>\
            <div class='info'>{location}</div>"
        )
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &GateArmAnc) -> String {
        let mut html = self.title(View::Setup);
        anc.cio.controller_html(self, &mut html);
        anc.cio.pin_html(self.pin, &mut html);
        html.raw(self.footer(true));
        html.into()
    }
}

impl Card for GateArm {
    type Ancillary = GateArmAnc;

    /// Display name
    const DNAME: &'static str = "⫬ Gate Arm";

    /// All item states as html options
    const ITEM_STATES: &'static str = "<option value=''>all ↴\
         <option value='↗️'>↗️ opening\
         <option value='✔️'>✔️ open\
         <option value='‼️'>‼️ warn close\
         <option value='↘️'>↘️ closing\
         <option value='⛔'>⛔ closed\
         <option value='⚠️'>⚠️ fault\
         <option value='🔌'>🔌 offline\
         <option value='🔺'>🔺 inactive";

    /// Get the resource
    fn res() -> Res {
        Res::GateArm
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

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &GateArmAnc) -> bool {
        self.name.contains_lower(search)
            || self.item_states(anc).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &GateArmAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Status => self.to_html_status(anc),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }
}
