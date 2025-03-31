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
use crate::error::Result;
use crate::gatearm::{GateArmState, item_states};
use crate::geoloc::{Loc, LocAnc};
use crate::util::{ContainsLower, HtmlStr};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// Gate Arm Array
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct GateArmArray {
    pub name: String,
    pub location: Option<String>,
    pub notes: Option<String>,
    pub arm_state: u32,
    pub interlock: u32,
    // secondary attributes
    pub geo_loc: Option<String>,
}

/// Ancillary gate arm array data
#[derive(Debug, Default)]
pub struct GateArmArrayAnc {
    loc: LocAnc<GateArmArray>,
    pub states: Vec<GateArmState>,
}

impl AncillaryData for GateArmArrayAnc {
    type Primary = GateArmArray;

    /// Construct ancillary gate arm array data
    fn new(pri: &GateArmArray, view: View) -> Self {
        let mut loc = LocAnc::new(pri, view);
        if let View::Search | View::Control = view {
            loc.assets.push(Asset::GateArmStates);
        }
        let states = Vec::new();
        GateArmArrayAnc { loc, states }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.loc.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &GateArmArray,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        if let Asset::GateArmStates = asset {
            self.states = serde_wasm_bindgen::from_value(value)?;
        } else {
            self.loc.set_asset(pri, asset, value)?;
        }
        Ok(())
    }
}

impl Loc for GateArmArray {
    /// Get geo location name
    fn geoloc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }
}

impl GateArmArray {
    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let name = HtmlStr::new(self.name());
        let item_states = item_states(self.arm_state);
        let location = HtmlStr::new(&self.location).with_len(32);
        format!(
            "<div class='title row'>{name} {item_states}</div>\
            <div class='info fill'>{location}</div>"
        )
    }

    /// Convert to Control HTML
    fn to_html_control(&self) -> String {
        let title = self.title(View::Control);
        let item_states = item_states(self.arm_state).to_html();
        let location = HtmlStr::new(&self.location).with_len(64);
        format!(
            "{title}\
            <div class='row'>{item_states}</div>\
            <div class='info'>{location}</div>"
        )
    }
}

impl Card for GateArmArray {
    type Ancillary = GateArmArrayAnc;

    /// Display name
    const DNAME: &'static str = "⫭⫬ Gate Arm Array";

    /// Get the resource
    fn res() -> Res {
        Res::GateArmArray
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
    fn is_match(&self, search: &str, _anc: &GateArmArrayAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || item_states(self.arm_state).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &GateArmArrayAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Control => self.to_html_control(),
            View::Location => anc.loc.to_html_loc(self),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields on Location view
    fn changed_location(&self, anc: GateArmArrayAnc) -> String {
        anc.loc.changed_location()
    }
}
