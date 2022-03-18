// Copyright (C) 2022  Minnesota Department of Transportation
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
use crate::error::Result;
use crate::resource::{AncillaryData, Card, View};
use crate::util::{ContainsLower, Dom, HtmlStr, OptVal};
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::borrow::{Borrow, Cow};
use std::fmt;
use wasm_bindgen::JsValue;
use web_sys::Document;

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
#[derive(Debug, Default, Deserialize, Serialize)]
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
    // full attributes
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
const DIRECTION_URI: &str = "/iris/direction";
const ROAD_MODIFIER_URI: &str = "/iris/road_modifier";

impl AncillaryData for GeoLocAnc {
    type Resource = GeoLoc;

    /// Get ancillary URI
    fn uri(&self, view: View, _res: &GeoLoc) -> Option<Cow<str>> {
        match (view, &self.roads, &self.directions, &self.modifiers) {
            (View::Edit, None, _, _) => Some(ROAD_URI.into()),
            (View::Edit, _, None, _) => Some(DIRECTION_URI.into()),
            (View::Edit, _, _, None) => Some(ROAD_MODIFIER_URI.into()),
            _ => None,
        }
    }

    /// Put ancillary JSON data
    fn set_json(
        &mut self,
        view: View,
        res: &GeoLoc,
        json: JsValue,
    ) -> Result<()> {
        if let Some(uri) = self.uri(view, res) {
            match uri.borrow() {
                ROAD_URI => self.roads = Some(json.into_serde::<Vec<Road>>()?),
                DIRECTION_URI => {
                    self.directions =
                        Some(json.into_serde::<Vec<Direction>>()?);
                }
                ROAD_MODIFIER_URI => {
                    self.modifiers =
                        Some(json.into_serde::<Vec<RoadModifier>>()?);
                }
                _ => (),
            }
        }
        Ok(())
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
        html.push_str("<select id='edit_mod'>");
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

impl fmt::Display for GeoLoc {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl Card for GeoLoc {
    const TNAME: &'static str = "Location";
    const ENAME: &'static str = "🗺️ Location";
    const UNAME: &'static str = "geo_loc";

    type Ancillary = GeoLocAnc;

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, _anc: &GeoLocAnc) -> bool {
        self.name.contains_lower(search)
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        "".into()
    }

    /// Convert to edit HTML
    fn to_html_edit(&self, anc: &GeoLocAnc) -> String {
        let roadway = anc.roads_html("edit_road", self.roadway.as_deref());
        let rdir = anc.directions_html("edit_rdir", self.road_dir);
        let xmod = anc.modifiers_html(self.cross_mod);
        let xstreet =
            anc.roads_html("edit_xstreet", self.cross_street.as_deref());
        let xdir = anc.directions_html("edit_xdir", self.cross_dir);
        let landmark = HtmlStr::new(&self.landmark);
        let lat = OptVal(self.lat);
        let lon = OptVal(self.lon);
        format!(
            "<div class='row'>\
              <label for='edit_road'>Roadway</label>\
              {roadway}\
              {rdir}\
            </div>\
            <div class='row'>\
              <label for='edit_xstreet'> </label>\
              {xmod}\
              {xstreet}\
              {xdir}\
            </div>\
            <div class='row'>\
              <label for='edit_lmark'>Landmark</label>\
              <input id='edit_lmark' maxlength='22' size='24' \
                     value='{landmark}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_lat'>Latitude</label>\
              <input id='edit_lat' type='number' step='0.00001' \
                     inputmode='decimal' value='{lat}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_lon'>Longitude</label>\
              <input id='edit_lon' type='number' step='0.00001' \
                     inputmode='decimal' value='{lon}'/>\
            </div>"
        )
    }

    /// Get changed fields from Edit form
    fn changed_fields(doc: &Document, json: &JsValue) -> Result<String> {
        let res = Self::new(json)?;
        let mut obj = Map::new();
        let roadway = doc
            .select_parse::<String>("edit_road")
            .filter(|r| !r.is_empty());
        if roadway != res.roadway {
            obj.insert("roadway".to_string(), OptVal(roadway).into());
        }
        if let Some(road_dir) = doc.select_parse::<u16>("edit_rdir") {
            if road_dir != res.road_dir {
                obj.insert("road_dir".to_string(), road_dir.into());
            }
        }
        let cross_street = doc
            .select_parse::<String>("edit_xstreet")
            .filter(|r| !r.is_empty());
        if cross_street != res.cross_street {
            obj.insert("cross_street".to_string(), OptVal(cross_street).into());
        }
        if let Some(cross_mod) = doc.select_parse::<u16>("edit_mod") {
            if cross_mod != res.cross_mod {
                obj.insert("cross_mod".to_string(), cross_mod.into());
            }
        }
        if let Some(cross_dir) = doc.select_parse::<u16>("edit_xdir") {
            if cross_dir != res.cross_dir {
                obj.insert("cross_dir".to_string(), cross_dir.into());
            }
        }
        let landmark = doc
            .input_parse::<String>("edit_lmark")
            .filter(|m| !m.is_empty());
        if landmark != res.landmark {
            obj.insert("landmark".to_string(), OptVal(landmark).into());
        }
        let lat = doc.input_parse::<f64>("edit_lat");
        if lat != res.lat {
            obj.insert("lat".to_string(), OptVal(lat).into());
        }
        let lon = doc.input_parse::<f64>("edit_lon");
        if lon != res.lon {
            obj.insert("lon".to_string(), OptVal(lon).into());
        }
        Ok(Value::Object(obj).to_string())
    }
}
