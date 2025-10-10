// Copyright (C) 2025  Minnesota Department of Transportation
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
use crate::item::{ItemState, ItemStates};
use crate::start::fly_map_item;
use crate::util::{ContainsLower, Fields};
use hatmil::Html;
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// Incident
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Incident {
    pub name: String,
    pub replaces: Option<String>,
    pub event_desc: u32,
    pub cleared: bool,
    pub confirmed: bool,
    // secondary attributes
    pub event_date: Option<String>,
    pub lat: Option<f64>,
    pub lon: Option<f64>,
    pub user_id: Option<String>,
}

/// Incident ancillary data
#[derive(Default)]
pub struct IncidentAnc {
    assets: Vec<Asset>,
}

impl AncillaryData for IncidentAnc {
    type Primary = Incident;

    /// Construct ancillary incident data
    fn new(_pri: &Incident, _view: View) -> Self {
        let assets = Vec::new();
        IncidentAnc { assets }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        _pri: &Incident,
        asset: Asset,
        _value: JsValue,
    ) -> Result<()> {
        match asset {
            _ => todo!(),
        }
    }
}

impl Incident {
    /// Get item state
    fn item_states(&self, anc: &IncidentAnc) -> ItemStates<'_> {
        ItemStates::default().with(self.item_state_main(anc), "")
    }

    /// Get lat/lon of incident
    fn latlon(&self) -> Option<(f64, f64)> {
        match (self.lat, self.lon) {
            (Some(lat), Some(lon)) => Some((lat, lon)),
            _ => None,
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &IncidentAnc) -> String {
        let mut html = Html::new();
        html.div()
            .class("end")
            .text(self.name())
            .text(" ")
            .text(self.item_states(anc).to_string());
        html.to_string()
    }

    /// Convert to Control HTML
    fn to_html_control(&self, _anc: &IncidentAnc) -> String {
        if let Some((lat, lon)) = self.latlon() {
            fly_map_item(&self.name, lat, lon);
        }
        let mut html = self.title(View::Control);
        html.div().class("row");
        html.end(); /* div */
        self.footer_html(true, &mut html);
        html.to_string()
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, _anc: &IncidentAnc) -> String {
        let mut html = self.title(View::Setup);
        html.div().class("row");
        html.end(); /* div */
        html.div().class("row");
        html.end(); /* div */
        self.footer_html(true, &mut html);
        html.to_string()
    }
}

impl Card for Incident {
    type Ancillary = IncidentAnc;

    /// Display name
    const DNAME: &'static str = "🚨 Incident";

    /// Get the resource
    fn res() -> Res {
        Res::Incident
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[
            ItemState::Incident,
            ItemState::Crash,
            ItemState::Stall,
            ItemState::Hazard,
            ItemState::Roadwork,
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
    fn item_state_main(&self, _anc: &IncidentAnc) -> ItemState {
        match (self.cleared, self.event_desc) {
            (true, _) => ItemState::Inactive,
            (_, 21) => ItemState::Crash,
            (_, 22) => ItemState::Stall,
            (_, 23) => ItemState::Hazard,
            (_, 24) => ItemState::Roadwork,
            _ => ItemState::Incident,
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, _anc: &IncidentAnc) -> bool {
        self.name.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &IncidentAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Control => self.to_html_control(anc),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let fields = Fields::new();
        // FIXME
        fields.into_value().to_string()
    }
}
