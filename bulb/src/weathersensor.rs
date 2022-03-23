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
use crate::device::{Device, DeviceAnc};
use crate::error::Result;
use crate::resource::{disabled_attr, Card, NAME};
use crate::util::{ContainsLower, Dom, HtmlStr, OptVal};
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::fmt;
use wasm_bindgen::JsValue;
use web_sys::Document;

/// Weather Sensor Data
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct WeatherData {
    min_air_temp: Option<f32>,
    max_air_temp: Option<f32>,
    wet_bulb_temp: Option<f32>,
    dew_point_temp: Option<f32>,
    visibility: Option<u32>,
    visibility_situation: Option<String>,
    avg_wind_dir: Option<u32>,
    avg_wind_speed: Option<f32>,
    spot_wind_dir: Option<u32>,
    spot_wind_speed: Option<f32>,
    gust_wind_dir: Option<u32>,
    gust_wind_speed: Option<f32>,
}

/// Weather Sensor
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct WeatherSensor {
    pub name: String,
    pub location: Option<String>,
    pub site_id: Option<String>,
    pub alt_id: Option<String>,
    pub notes: String,
    pub geo_loc: Option<String>,
    pub controller: Option<String>,
    // full attributes
    pub pin: Option<u32>,
    pub settings: Option<Value>,
    pub sample: Option<WeatherData>,
    pub sample_time: Option<String>,
}

type WeatherSensorAnc = DeviceAnc<WeatherSensor>;

/// Get atmospheric situation string
fn situation_str(situation: &str) -> &'static str {
    match situation {
        "clear" => "☀️ / 🌃 clear",
        "fogNotPatchy" => "🌫️ fog",
        "patchyFog" => "🌁 patchy fog",
        "other" => "⛆ other",
        _ => "❓ unknown",
    }
}

/// Get a direction arrow from degrees
fn dir_arrow(deg: u32) -> Option<&'static str> {
    match deg {
        // 0° ± 22.5
        0..=22 | 338..=360 => Some("↓"),
        // 45° ±22.5
        23..=67 => Some("↙"),
        // 90° ±22.5
        68..=123 => Some("←"),
        // 135° ±22.5
        113..=157 => Some("↖"),
        // 180° ±22.5
        158..=202 => Some("↑"),
        // 225° ±22.5
        203..=247 => Some("↗"),
        // 270° ±22.5
        248..=292 => Some("→"),
        // 315° ±22.5
        293..=337 => Some("↘"),
        _ => None,
    }
}

/// Get wind direction as HTML
fn wind_dir_html(deg: u32) -> String {
    let mut html = String::new();
    html.push_str("<span class='info'>");
    if let Some(arrow) = dir_arrow(deg) {
        html.push_str(arrow);
        html.push(' ');
    }
    html.push_str(&deg.to_string());
    html.push_str("°</span>");
    html
}

impl WeatherData {
    /// Get weather data as HTML
    fn to_html(&self) -> String {
        let mut html = String::new();
        if self.min_air_temp.is_some()
            || self.max_air_temp.is_some()
            || self.wet_bulb_temp.is_some()
            || self.dew_point_temp.is_some()
        {
            html.push_str(&self.temp_html());
        }
        if self.visibility_situation.is_some() || self.visibility.is_some() {
            html.push_str(&self.atmospheric_html());
        }
        if self.avg_wind_dir.is_some()
            || self.avg_wind_speed.is_some()
            || self.spot_wind_dir.is_some()
            || self.spot_wind_speed.is_some()
            || self.gust_wind_dir.is_some()
            || self.gust_wind_speed.is_some()
        {
            html.push_str(&self.wind_html());
        }
        html
    }

    /// Get temp data as HTML
    fn temp_html(&self) -> String {
        let mut html = String::new();
        html.push_str("<div class='row'>");
        html.push_str("<span>🌡️</span><span>");
        match (self.min_air_temp, self.max_air_temp) {
            (Some(min_air_temp), Some(max_air_temp)) => {
                html.push_str("Air <span class='info'>");
                html.push_str(&min_air_temp.to_string());
                html.push_str("…");
                html.push_str(&max_air_temp.to_string());
                html.push_str(" ℃</span>");
            }
            (Some(air_temp), None) | (None, Some(air_temp)) => {
                html.push_str("Air <span class='info'>");
                html.push_str(&air_temp.to_string());
                html.push_str(" ℃</span>");
            }
            _ => (),
        }
        if let Some(wet_bulb_temp) = self.wet_bulb_temp {
            html.push_str(" WB <span class='info'>");
            html.push_str(&wet_bulb_temp.to_string());
            html.push_str(" ℃</span>");
        }
        if let Some(dew_point_temp) = self.dew_point_temp {
            html.push_str(" DP <span class='info'>");
            html.push_str(&dew_point_temp.to_string());
            html.push_str(" ℃</span>");
        }
        html.push_str("</span></div>");
        html
    }

    /// Get atmospheric HTML
    fn atmospheric_html(&self) -> String {
        let mut html = String::new();
        html.push_str("<div class='row left'>");
        if let (Some(visibility_situation), Some(visibility)) =
            (&self.visibility_situation, self.visibility)
        {
            html.push_str("<span>");
            html.push_str(situation_str(visibility_situation));
            html.push_str(", visibility <span class='info'>");
            html.push_str(&visibility.to_string());
            html.push_str(" m</span>");
            html.push_str("</span>");
        }
        html.push_str("</div>");
        html
    }

    /// Get wind data as HTML
    fn wind_html(&self) -> String {
        let mut html = String::new();
        html.push_str("<table><tr><th>🌬️ Wind<th>Avg<th>Spot<th>Gust");
        html.push_str("<tr><td>🧭 Dir<td>");
        if let Some(avg_wind_dir) = self.avg_wind_dir {
            html.push_str(&wind_dir_html(avg_wind_dir));
        }
        html.push_str("<td>");
        if let Some(spot_wind_dir) = self.spot_wind_dir {
            html.push_str(&wind_dir_html(spot_wind_dir));
        }
        html.push_str("<td>");
        if let Some(gust_wind_dir) = self.gust_wind_dir {
            html.push_str(&wind_dir_html(gust_wind_dir));
        }
        html.push_str("<tr><td>Speed (m/s)<td>");
        if let Some(avg_wind_speed) = self.avg_wind_speed {
            html.push_str("<span class='info'>");
            html.push_str(&avg_wind_speed.to_string());
            html.push_str("</span>");
        }
        html.push_str("<td>");
        if let Some(spot_wind_speed) = self.spot_wind_speed {
            html.push_str("<span class='info'>");
            html.push_str(&spot_wind_speed.to_string());
            html.push_str("</span>");
        }
        html.push_str("<td>");
        if let Some(gust_wind_speed) = self.gust_wind_speed {
            html.push_str("<span class='info'>");
            html.push_str(&gust_wind_speed.to_string());
            html.push_str("</span>");
        }
        html.push_str("</table>");
        html
    }
}

impl WeatherSensor {
    /// Get sample as HTML
    fn sample_html(&self) -> String {
        match &self.sample {
            Some(data) => data.to_html(),
            None => "".into(),
        }
    }
}

impl fmt::Display for WeatherSensor {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl Device for WeatherSensor {
    /// Get controller
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Card for WeatherSensor {
    const TNAME: &'static str = "Weather Sensor";
    const SYMBOL: &'static str = "🌦️";
    const ENAME: &'static str = "🌦️ Weather Sensor";
    const UNAME: &'static str = "weather_sensor";
    const HAS_STATUS: bool = true;

    type Ancillary = WeatherSensorAnc;

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Get geo location name
    fn geo_loc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, _anc: &WeatherSensorAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || self.site_id.contains_lower(search)
            || self.alt_id.contains_lower(search)
            || self.notes.contains_lower(search)
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let location = HtmlStr::new(&self.location).with_len(12);
        let disabled = disabled_attr(self.controller.is_some());
        format!(
            "<span{disabled}>{location}</span>\
            <span class='{NAME}'>{self}</span>"
        )
    }

    /// Convert to status HTML
    fn to_html_status(&self, anc: &WeatherSensorAnc) -> String {
        let location = HtmlStr::new(&self.location).with_len(64);
        let site_id = HtmlStr::new(&self.site_id);
        let alt_id = HtmlStr::new(&self.alt_id);
        let controller = anc.controller_html();
        let sample_time = self.sample_time.as_deref().unwrap_or("-");
        let sample = self.sample_html();
        format!(
            "<div class='row'>\
              <span class='info'>{location}</span>\
            </div>\
            <div class='row'>\
              <span class='info'>{site_id}</span>\
              <span class='info'>{alt_id}</span>\
            </div>\
            {controller}\
            <div class='row'>\
              <span>Time</span>\
              <span class='info'>{sample_time}</span>\
            </div>\
            {sample}"
        )
    }

    /// Convert to edit HTML
    fn to_html_edit(&self, _anc: &WeatherSensorAnc) -> String {
        let site_id = HtmlStr::new(&self.site_id);
        let alt_id = HtmlStr::new(&self.alt_id);
        let notes = HtmlStr::new(&self.notes);
        let controller = HtmlStr::new(&self.controller);
        let pin = OptVal(self.pin);
        format!(
            "<div class='row'>\
              <label for='edit_site'>Site ID</label>\
              <input id='edit_site' maxlength='20' size='20' \
                     value='{site_id}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_alt'>Alt ID</label>\
              <input id='edit_alt' maxlength='20' size='20' \
                     value='{alt_id}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_notes'>Notes</label>\
              <textarea id='edit_notes' maxlength='64' rows='2' \
                        cols='26'>{notes}</textarea>\
            </div>\
            <div class='row'>\
              <label for='edit_ctrl'>Controller</label>\
              <input id='edit_ctrl' maxlength='20' size='20' \
                     value='{controller}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_pin'>Pin</label>\
              <input id='edit_pin' type='number' min='1' max='104' \
                     size='8' value='{pin}'/>\
            </div>"
        )
    }

    /// Get changed fields from Edit form
    fn changed_fields(doc: &Document, json: &JsValue) -> Result<String> {
        let val = Self::new(json)?;
        let mut obj = Map::new();
        let site_id = doc.input_parse::<String>("edit_site");
        if site_id != val.site_id {
            obj.insert("site_id".to_string(), OptVal(site_id).into());
        }
        let alt_id = doc.input_parse::<String>("edit_alt");
        if alt_id != val.alt_id {
            obj.insert("alt_id".to_string(), OptVal(alt_id).into());
        }
        if let Some(notes) = doc.text_area_parse::<String>("edit_notes") {
            if notes != val.notes {
                obj.insert("notes".to_string(), Value::String(notes));
            }
        }
        let ctrl = doc
            .input_parse::<String>("edit_ctrl")
            .filter(|c| !c.is_empty());
        if ctrl != val.controller {
            obj.insert("controller".to_string(), OptVal(ctrl).into());
        }
        let pin = doc.input_parse::<u32>("edit_pin");
        if pin != val.pin {
            obj.insert("pin".to_string(), OptVal(pin).into());
        }
        Ok(Value::Object(obj).to_string())
    }
}
