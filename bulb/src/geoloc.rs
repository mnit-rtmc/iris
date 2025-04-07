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
use crate::util::{Fields, HtmlStr, Input, OptVal, Select};
use serde::Deserialize;
use std::marker::PhantomData;
use wasm_bindgen::JsValue;

/// Road definitions
#[derive(Debug, Deserialize)]
#[allow(dead_code)]
pub struct Road {
    pub name: String,
    pub abbrev: String,
    pub r_class: u16,
    pub direction: u16,
}

/// Roadway directions
#[derive(Debug, Deserialize)]
#[allow(dead_code)]
pub struct Direction {
    pub id: u16,
    pub direction: String,
    pub dir: String,
}

/// Roadway modifiers
#[derive(Debug, Deserialize)]
#[allow(dead_code)]
pub struct RoadModifier {
    pub id: u16,
    pub modifier: String,
    pub md: String,
}

/// Geo location data
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct GeoLoc {
    pub name: String,
    pub resource_n: String,
    pub roadway: Option<String>,
    pub road_dir: u16,
    pub cross_street: Option<String>,
    pub cross_dir: u16,
    pub cross_mod: u16,
    pub landmark: Option<String>,
    pub lat: Option<f64>,
    pub lon: Option<f64>,
}

/// Location resource
pub trait Loc {
    /// Get geo location name
    fn geoloc(&self) -> Option<&str> {
        None
    }
}

/// Ancillary location data
#[derive(Debug, Default)]
pub struct LocAnc<L> {
    pri: PhantomData<L>,
    pub assets: Vec<Asset>,
    geoloc: Option<GeoLoc>,
    roads: Vec<Road>,
    directions: Vec<Direction>,
    modifiers: Vec<RoadModifier>,
}

impl<L> AncillaryData for LocAnc<L>
where
    L: Loc + Card,
{
    type Primary = L;

    /// Construct ancillary location data
    fn new(pri: &L, view: View) -> Self {
        let mut assets = Vec::new();
        match (view, pri.geoloc()) {
            (View::Control, Some(nm)) => {
                assets.push(Asset::GeoLoc(nm.to_string(), L::res()));
            }
            (View::Location, Some(nm)) => {
                assets.push(Asset::GeoLoc(nm.to_string(), L::res()));
                assets.push(Asset::Directions);
                assets.push(Asset::Roads);
                assets.push(Asset::RoadModifiers);
            }
            _ => (),
        }
        LocAnc {
            pri: PhantomData,
            assets,
            geoloc: None,
            roads: Vec::new(),
            directions: Vec::new(),
            modifiers: Vec::new(),
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        _pri: &L,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::GeoLoc(_nm, _assoc) => {
                self.geoloc = Some(serde_wasm_bindgen::from_value(value)?);
            }
            Asset::Directions => {
                self.directions = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::RoadModifiers => {
                self.modifiers = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::Roads => {
                self.roads = serde_wasm_bindgen::from_value(value)?;
            }
            _ => unreachable!(),
        }
        Ok(())
    }
}

impl<L> LocAnc<L> {
    /// Get lat/lon of location
    pub fn latlon(&self) -> Option<(f64, f64)> {
        match &self.geoloc {
            Some(geoloc) => match (geoloc.lat, geoloc.lon) {
                (Some(lat), Some(lon)) => Some((lat, lon)),
                _ => None,
            },
            None => None,
        }
    }

    /// Create an HTML `select` element of roads
    fn roads_html(&self, id: &str, groad: Option<&str>) -> String {
        let mut html = String::new();
        html.push_str("<select id='");
        html.push_str(id);
        html.push_str("'><option></option>");
        for road in &self.roads {
            html.push_str("<option ");
            if let Some(groad) = groad {
                if groad == road.name {
                    html.push_str(" selected");
                }
            }
            html.push('>');
            html.push_str(&format!("{}", HtmlStr::new(&road.name)));
            html.push_str("</option>");
        }
        html.push_str("</select>");
        html
    }

    /// Create an HTML `select` element of road directions
    fn directions_html(&self, id: &str, dir: u16) -> String {
        let mut html = String::new();
        html.push_str("<select id='");
        html.push_str(id);
        html.push_str("'>");
        for direction in &self.directions {
            html.push_str("<option value='");
            html.push_str(&direction.id.to_string());
            html.push('\'');
            if dir == direction.id {
                html.push_str(" selected");
            }
            html.push('>');
            html.push_str(&direction.direction);
            html.push_str("</option>");
        }
        html.push_str("</select>");
        html
    }

    /// Create an HTML `select` element of road modifiers
    fn modifiers_html(&self, md: u16) -> String {
        let mut html = String::new();
        html.push_str("<select id='cross_mod'>");
        for modifier in &self.modifiers {
            html.push_str("<option value='");
            html.push_str(&modifier.id.to_string());
            html.push('\'');
            if md == modifier.id {
                html.push_str(" selected");
            }
            html.push('>');
            html.push_str(&modifier.modifier);
            html.push_str("</option>");
        }
        html.push_str("</select>");
        html
    }

    /// Convert to Location HTML
    pub fn to_html_loc<C>(&self, card: &C) -> String
    where
        C: Card,
    {
        let title = String::from(card.title(View::Location));
        let footer = card.footer(false);
        let html = match &self.geoloc {
            Some(geoloc) => self.to_html_location(geoloc),
            None => "Error: missing geo_loc!".to_string(),
        };
        format!("{title}{html}{footer}")
    }

    /// Convert to Location HTML
    fn to_html_location(&self, loc: &GeoLoc) -> String {
        let roadway = self.roads_html("roadway", loc.roadway.as_deref());
        let rdir = self.directions_html("road_dir", loc.road_dir);
        let xmod = self.modifiers_html(loc.cross_mod);
        let xstreet =
            self.roads_html("cross_street", loc.cross_street.as_deref());
        let xdir = self.directions_html("cross_dir", loc.cross_dir);
        let landmark = HtmlStr::new(&loc.landmark);
        let lat = OptVal(loc.lat);
        let lon = OptVal(loc.lon);
        format!(
            "<div class='row'>\
              <label for='roadway'>Roadway</label>\
              {roadway}\
              {rdir}\
            </div>\
            <div class='row'>\
              <label for='cross_street'> </label>\
              {xmod}\
              {xstreet}\
              {xdir}\
            </div>\
            <div class='row'>\
              <label for='landmark'>Landmark</label>\
              <input id='landmark' maxlength='22' size='24' \
                     value='{landmark}'>\
            </div>\
            <div class='row'>\
              <label for='lat'>Latitude</label>\
              <input id='lat' type='number' step='0.00001' \
                     inputmode='decimal' value='{lat}'>\
            </div>\
            <div class='row'>\
              <label for='lon'>Longitude</label>\
              <input id='lon' type='number' step='0.00001' \
                     inputmode='decimal' value='{lon}'>\
            </div>"
        )
    }

    /// Get changed fields from Location form
    pub fn changed_location(&self) -> String {
        match &self.geoloc {
            Some(geoloc) => geoloc.changed_location(),
            None => String::new(),
        }
    }
}

impl GeoLoc {
    /// Get changed fields from Location form
    fn changed_location(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_select("roadway", &self.roadway);
        fields.changed_select("road_dir", self.road_dir);
        fields.changed_select("cross_street", &self.cross_street);
        fields.changed_select("cross_mod", self.cross_mod);
        fields.changed_select("cross_dir", self.cross_dir);
        fields.changed_input("landmark", &self.landmark);
        fields.changed_input("lat", self.lat);
        fields.changed_input("lon", self.lon);
        fields.into_value().to_string()
    }
}
