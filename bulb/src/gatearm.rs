// Copyright (C) 2022-2024  Minnesota Department of Transportation
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
use crate::card::{inactive_attr, AncillaryData, Card, View};
use crate::controller::Controller;
use crate::error::Result;
use crate::item::ItemState;
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal};
use resources::Res;
use serde::{Deserialize, Serialize};
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// Gate arm states
#[derive(Debug, Deserialize, Serialize)]
pub struct GateArmState {
    pub id: u32,
    pub description: String,
}

/// Gate Arm
#[derive(Debug, Default, Deserialize, Serialize, PartialEq)]
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
    assets: Vec<Asset>,
    pub controller: Option<Controller>,
    pub states: Option<Vec<GateArmState>>,
}

/// Get gate arm item state
pub fn item_state(arm_state: u32) -> ItemState {
    match arm_state {
        1 => ItemState::Fault,
        2 => ItemState::Opening,
        3 => ItemState::Open,
        4 => ItemState::WarnClose,
        5 => ItemState::Closing,
        6 => ItemState::Closed,
        _ => ItemState::Unknown,
    }
}

impl GateArmAnc {
    fn controller_button(&self) -> String {
        match &self.controller {
            Some(ctrl) => ctrl.button_html(),
            None => "<span></span>".into(),
        }
    }
}

impl AncillaryData for GateArmAnc {
    type Primary = GateArm;

    /// Construct ancillary gate arm data
    fn new(pri: &GateArm, view: View) -> Self {
        let assets = match (view, &pri.controller()) {
            (View::Status, Some(ctrl)) => {
                vec![Asset::Controller(ctrl.to_string())]
            }
            _ => vec![Asset::GateArmStates],
        };
        let controller = None;
        let states = None;
        GateArmAnc {
            assets,
            controller,
            states,
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        _pri: &GateArm,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::GateArmStates => {
                self.states = Some(serde_wasm_bindgen::from_value(value)?);
            }
            Asset::Controller(_) => {
                self.controller = Some(serde_wasm_bindgen::from_value(value)?);
            }
            _ => unreachable!(),
        }
        Ok(())
    }
}

impl GateArm {
    /// Get controller
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let name = HtmlStr::new(self.name());
        let item = item_state(self.arm_state);
        let inactive = inactive_attr(self.controller.is_some());
        let location = HtmlStr::new(&self.location).with_len(32);
        format!(
            "<div class='title row'>{name} {item}</div>\
            <div class='info fill{inactive}'>{location}</div>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self) -> String {
        let title = self.title(View::Status);
        let location = HtmlStr::new(&self.location).with_len(64);
        let item = item_state(self.arm_state);
        let desc = item.description();
        format!(
            "{title}\
            <div class='info'>{location}</div>\
            <div>{item} {desc}</div>"
        )
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &GateArmAnc) -> String {
        let title = self.title(View::Setup);
        let ctl_btn = anc.controller_button();
        let controller = HtmlStr::new(&self.controller);
        let pin = OptVal(self.pin);
        let footer = self.footer(true);
        format!(
            "{title}\
            <div class='row'>\
              <label for='controller'>Controller</label>\
              <input id='controller' maxlength='20' size='20' \
                     value='{controller}'>\
              {ctl_btn}\
            </div>\
            <div class='row'>\
              <label for='pin'>Pin</label>\
              <input id='pin' type='number' min='1' max='104' \
                     size='8' value='{pin}'>\
            </div>\
            {footer}"
        )
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
         <option value='▪️'>▪️ inactive";

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
    fn is_match(&self, search: &str, _anc: &GateArmAnc) -> bool {
        self.name.contains_lower(search)
            || item_state(self.arm_state).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &GateArmAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Status => self.to_html_status(),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_fields(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }
}
