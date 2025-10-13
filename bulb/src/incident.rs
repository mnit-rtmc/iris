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
use crate::geoloc::Direction;
use crate::item::{ItemState, ItemStates};
use crate::start::fly_map_item;
use crate::util::{ContainsLower, Fields};
use hatmil::Html;
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// Incident detail
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct IncidentDetail {
    /// Detail name (short)
    name: String,
    /// Detail description (long)
    description: String,
}

/// Incident
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Incident {
    pub name: String,
    pub replaces: Option<String>,
    pub event_desc: u32,
    pub detail: Option<String>,
    pub road: String,
    pub dir: u16,
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
    directions: Vec<Direction>,
    details: Vec<IncidentDetail>,
}

impl AncillaryData for IncidentAnc {
    type Primary = Incident;

    /// Construct ancillary incident data
    fn new(_pri: &Incident, view: View) -> Self {
        let mut assets = Vec::new();
        match view {
            View::Search | View::Compact | View::Control => {
                assets.push(Asset::Directions);
                assets.push(Asset::IncDetails);
                //assets.push(Asset::Roads);
            }
            _ => (),
        }
        let directions = Vec::new();
        let details = Vec::new();
        IncidentAnc {
            assets,
            directions,
            details,
        }
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
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::Directions => {
                self.directions = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::IncDetails => {
                self.details = serde_wasm_bindgen::from_value(value)?;
            }
            _ => unreachable!(),
        }
        Ok(())
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

    /// Get incident location
    fn location(&self, anc: &IncidentAnc) -> String {
        let dir = anc
            .directions
            .get(usize::from(self.dir))
            .map(|d| &d.direction[..])
            .unwrap_or("");
        format!("{} {dir}", &self.road)
    }

    /// Get incident description
    fn description(&self, anc: &IncidentAnc) -> String {
        let st = self.item_state_main(anc);
        let desc = st.description();
        format!("{st} {desc} on {}", self.location(anc))
    }

    /// Get detail description
    fn detail<'a>(&self, anc: &'a IncidentAnc) -> &'a str {
        let st = self.item_state_main(anc);
        match (st, &self.detail) {
            (ItemState::Hazard, Some(dtl)) => anc
                .details
                .iter()
                .find(|d| d.name.as_str() == dtl)
                .map(|d| &d.description[..])
                .unwrap_or(""),
            _ => "",
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &IncidentAnc) -> String {
        let mut html = Html::new();
        html.div().class("title row").text(self.description(anc));
        html.end(); /* div */
        html.to_string()
    }

    /// Convert to Control HTML
    fn to_html_control(&self, anc: &IncidentAnc) -> String {
        if let Some((lat, lon)) = self.latlon() {
            fly_map_item(&self.name, lat, lon);
        }
        let mut html = Html::new();
        html.div().class("title row").text(self.description(anc));
        self.views_html(View::Control, &mut html);
        html.end(); /* div */
        html.div().class("row info").text(self.detail(anc)).end();
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
    fn is_match(&self, search: &str, anc: &IncidentAnc) -> bool {
        self.name.contains_lower(search)
            || self.item_states(anc).is_match(search)
            || self.description(anc).contains_lower(search)
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
