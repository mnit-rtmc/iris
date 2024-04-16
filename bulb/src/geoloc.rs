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
use crate::card::{AncillaryData, Card, View};
use crate::error::Result;
use crate::fetch::Uri;
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal, Select};
use resources::Res;
use serde::{Deserialize, Serialize};
use std::fmt;
use std::iter::empty;
use wasm_bindgen::JsValue;

/// Road definitions
#[derive(Debug, Deserialize, Serialize)]
pub struct Road {
    pub name: String,
    pub abbrev: String,
    pub r_class: u16,
    pub direction: u16,
}

/// Roadway directions
#[derive(Debug, Deserialize, Serialize)]
pub struct Direction {
    pub id: u16,
    pub direction: String,
    pub dir: String,
}

/// Roadway modifiers
#[derive(Debug, Deserialize, Serialize)]
pub struct RoadModifier {
    pub id: u16,
    pub modifier: String,
    pub md: String,
}

/// Geo location
#[derive(Debug, Default, Deserialize, Serialize, PartialEq)]
pub struct GeoLoc {
    pub name: String,
    pub roadway: Option<String>,
    pub road_dir: u16,
    pub cross_street: Option<String>,
    pub cross_dir: u16,
    pub cross_mod: u16,
    pub landmark: Option<String>,
    pub lat: Option<f64>,
    pub lon: Option<f64>,
    // secondary attributes
    pub resource_n: Option<String>,
}

/// Ancillary location data
#[derive(Debug, Default)]
pub struct GeoLocAnc {
    pub roads: Option<Vec<Road>>,
    pub directions: Option<Vec<Direction>>,
    pub modifiers: Option<Vec<RoadModifier>>,
}

const ROAD_URI: &str = "/iris/api/road";
const DIRECTION_URI: &str = "/iris/lut/direction";
const ROAD_MODIFIER_URI: &str = "/iris/lut/road_modifier";

impl AncillaryData for GeoLocAnc {
    type Primary = GeoLoc;

    /// Get URI iterator
    fn uri_iter(
        &self,
        _pri: &GeoLoc,
        view: View,
    ) -> Box<dyn Iterator<Item = Uri>> {
        match view {
            View::Edit => Box::new(
                [
                    ROAD_URI.into(),
                    DIRECTION_URI.into(),
                    ROAD_MODIFIER_URI.into(),
                ]
                .into_iter(),
            ),
            _ => Box::new(empty()),
        }
    }

    /// Put ancillary data
    fn set_data(
        &mut self,
        _pri: &GeoLoc,
        uri: Uri,
        data: JsValue,
    ) -> Result<bool> {
        match uri.as_str() {
            ROAD_URI => {
                self.roads = Some(serde_wasm_bindgen::from_value(data)?)
            }
            DIRECTION_URI => {
                self.directions = Some(serde_wasm_bindgen::from_value(data)?);
            }
            ROAD_MODIFIER_URI => {
                self.modifiers = Some(serde_wasm_bindgen::from_value(data)?);
            }
            _ => (),
        }
        Ok(false)
    }
}

impl GeoLocAnc {
    /// Create an HTML `select` element of roads
    fn roads_html(&self, id: &str, groad: Option<&str>) -> String {
        let mut html = String::new();
        html.push_str("<select id='");
        html.push_str(id);
        html.push_str("'><option></option>");
        if let Some(roads) = &self.roads {
            for road in roads {
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
        if let Some(directions) = &self.directions {
            for direction in directions {
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
        }
        html.push_str("</select>");
        html
    }

    /// Create an HTML `select` element of road modifiers
    fn modifiers_html(&self, md: u16) -> String {
        let mut html = String::new();
        html.push_str("<select id='cross_mod'>");
        if let Some(modifiers) = &self.modifiers {
            for modifier in modifiers {
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
        }
        html.push_str("</select>");
        html
    }
}

impl GeoLoc {
    /// Convert to Edit HTML
    fn to_html_edit(&self, anc: &GeoLocAnc) -> String {
        let roadway = anc.roads_html("roadway", self.roadway.as_deref());
        let rdir = anc.directions_html("road_dir", self.road_dir);
        let xmod = anc.modifiers_html(self.cross_mod);
        let xstreet =
            anc.roads_html("cross_street", self.cross_street.as_deref());
        let xdir = anc.directions_html("cross_dir", self.cross_dir);
        let landmark = HtmlStr::new(&self.landmark);
        let lat = OptVal(self.lat);
        let lon = OptVal(self.lon);
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
}

impl fmt::Display for GeoLoc {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl Card for GeoLoc {
    type Ancillary = GeoLocAnc;

    /// Display name
    const DNAME: &'static str = "🗺️ Location";

    /// Get the resource
    fn res() -> Res {
        Res::GeoLoc
    }

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, _anc: &GeoLocAnc) -> bool {
        self.name.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &GeoLocAnc) -> String {
        match view {
            View::Edit => self.to_html_edit(anc),
            _ => unreachable!(),
        }
    }

    /// Get changed fields from Edit form
    fn changed_fields(&self) -> String {
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
