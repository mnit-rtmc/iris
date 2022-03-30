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
use mag::length::{m, mm, Unit as _};
use mag::quan::Unit as _;
use mag::temp::DegC;
use mag::time::{s, Unit as _};
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::fmt;
use wasm_bindgen::JsValue;
use web_sys::Document;

/// Display Units
type TempUnit = mag::temp::DegF;
type DistUnit = mag::length::mi;
type RainUnit = mag::length::In;
type SpeedUnit = mag::time::h;

/// Air temp data
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct AirTemp {
    air_temp: f32,
}

/// Weather Sensor Data
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct WeatherData {
    temperature_sensor: Option<Vec<AirTemp>>,
    wet_bulb_temp: Option<f32>,
    dew_point_temp: Option<f32>,
    /// Minimum air temp in last 24 hours (first sensor)
    min_air_temp: Option<f32>,
    /// Maximum air temp in last 24 hours (first sensor)
    max_air_temp: Option<f32>,
    visibility_situation: Option<String>,
    visibility: Option<u32>,
    relative_humidity: Option<u32>,
    avg_wind_dir: Option<u32>,
    avg_wind_speed: Option<f32>,
    spot_wind_dir: Option<u32>,
    spot_wind_speed: Option<f32>,
    gust_wind_dir: Option<u32>,
    gust_wind_speed: Option<f32>,
    precip_1_hour: Option<f32>,
    precip_3_hours: Option<f32>,
    precip_6_hours: Option<f32>,
    precip_12_hours: Option<f32>,
    precip_24_hours: Option<f32>,
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

/// Get visibility situation string (from NTCIP 1204)
fn vis_situation(situation: &str) -> &'static str {
    match situation {
        "other" => "⛆ other",
        "clear" => "🔭 clear",
        "fogNotPatchy" => "🌫️ fog",
        "patchyFog" => "🌁 patchy fog",
        "blowingSnow" => "❄️ snow",
        "smoke" => "🚬 smoke",
        "seaSpray" => "💦 sea spray",
        "vehicleSpray" => "💦 spray",
        "blowingDustOrSand" => "💨 dust",
        "sunGlare" => "🕶️ sun glare",
        "swarmOfInsects" => "🦗 swarm", // seriously?!?
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
        68..=112 => Some("←"),
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

/// Format temperature quantity
fn temp_format(temp: f32) -> String {
    let temp = (f64::from(temp) * DegC).to::<TempUnit>().value;
    format!("<span class='info'>{temp:.1}</span>")
}

/// Format precipitation quantity
fn precip_format(precip: f32) -> String {
    let precip = (f64::from(precip) * mm).to::<RainUnit>().quantity;
    format!("{precip:.2}")
        .trim_end_matches('0')
        .trim_end_matches('.')
        .to_string()
}

/// Format wind speed quantity
fn speed_format(speed: f32) -> String {
    let speed = (f64::from(speed) * m / s)
        .to::<DistUnit, SpeedUnit>()
        .quantity;
    format!("{speed:.0}")
}

impl WeatherData {
    /// Get weather data as HTML
    fn to_html(&self) -> String {
        let mut html = String::new();
        if self.visibility_situation.is_some()
            || self.visibility.is_some()
            || self.relative_humidity.is_some()
        {
            html.push_str(&self.atmospheric_html());
        }
        if self.temperature_sensor.is_some()
            || self.dew_point_temp.is_some()
            || self.wet_bulb_temp.is_some()
            || self.min_air_temp.is_some()
            || self.max_air_temp.is_some()
        {
            html.push_str(&self.temp_html());
        }
        if self.precip_1_hour.is_some()
            || self.precip_3_hours.is_some()
            || self.precip_6_hours.is_some()
            || self.precip_12_hours.is_some()
            || self.precip_24_hours.is_some()
        {
            html.push_str(&self.precipitation_html());
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
        html.push_str("<table><tr><th>🌡️<th>Air<th>DP<th>WB<th>Min24<th>Max24");
        html.push_str("<tr><td>");
        html.push_str(TempUnit::LABEL);
        html.push_str("<td>");
        if let Some(temperature_sensor) = &self.temperature_sensor {
            if !temperature_sensor.is_empty() {
                let n_temps = temperature_sensor.len() as f32;
                let temp = temperature_sensor
                    .iter()
                    .map(|at| at.air_temp)
                    .sum::<f32>()
                    / n_temps;
                html.push_str(&temp_format(temp));
            }
        }
        html.push_str("<td>");
        if let Some(temp) = self.dew_point_temp {
            html.push_str(&temp_format(temp));
        }
        html.push_str("<td>");
        if let Some(temp) = self.wet_bulb_temp {
            html.push_str(&temp_format(temp));
        }
        html.push_str("<td>");
        if let Some(temp) = self.min_air_temp {
            html.push_str(&temp_format(temp));
        }
        html.push_str("<td>");
        if let Some(temp) = self.max_air_temp {
            html.push_str(&temp_format(temp));
        }
        html.push_str("</table></div>");
        html
    }

    /// Get atmospheric HTML
    fn atmospheric_html(&self) -> String {
        let mut html = String::new();
        html.push_str("<div class='row left'>");
        if let Some(visibility_situation) = &self.visibility_situation {
            html.push_str(vis_situation(visibility_situation));
            if self.visibility.is_some() || self.relative_humidity.is_some() {
                html.push_str(", ");
            }
        }
        if let Some(visibility) = self.visibility {
            html.push_str("vis <span class='info'>");
            let vis = (f64::from(visibility) * m).to::<DistUnit>();
            html.push_str(&format!("{vis:.1}"));
            html.push_str("</span>");
            if self.relative_humidity.is_some() {
                html.push_str(", ");
            }
        }
        if let Some(rh) = self.relative_humidity {
            html.push_str(&format!("RH <span class='info'>{rh}%</span>"));
        }
        html.push_str("</div>");
        html
    }

    /// Get precipitation data as HTML
    fn precipitation_html(&self) -> String {
        let mut html = String::new();
        html.push_str(
            "<table><tr><th>🌧️ Precip<th>1h<th>3h<th>6h<th>12h<th>24h",
        );
        html.push_str(&format!("<tr><td>Total ({})<td>", RainUnit::LABEL));
        if let Some(precip) = self.precip_1_hour {
            html.push_str(&precip_format(precip));
        }
        html.push_str("<td>");
        if let Some(precip) = self.precip_3_hours {
            html.push_str(&precip_format(precip));
        }
        html.push_str("<td>");
        if let Some(precip) = self.precip_6_hours {
            html.push_str(&precip_format(precip));
        }
        html.push_str("<td>");
        if let Some(precip) = self.precip_12_hours {
            html.push_str(&precip_format(precip));
        }
        html.push_str("<td>");
        if let Some(precip) = self.precip_24_hours {
            html.push_str(&precip_format(precip));
        }
        html.push_str("</table>");
        html
    }

    /// Get wind data as HTML
    fn wind_html(&self) -> String {
        let mut html = String::new();
        html.push_str("<table><tr><th>🌬️ Wind<th>Avg<th>Spot<th>Gust");
        html.push_str("<tr><td>Dir 🧭<td>");
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
        html.push_str("<tr><td>Speed (");
        html.push_str(DistUnit::LABEL);
        html.push('/');
        html.push_str(SpeedUnit::LABEL);
        html.push_str(")<td>");
        if let Some(avg_wind_speed) = self.avg_wind_speed {
            html.push_str("<span class='info'>");
            html.push_str(&speed_format(avg_wind_speed));
            html.push_str("</span>");
        }
        html.push_str("<td>");
        if let Some(spot_wind_speed) = self.spot_wind_speed {
            html.push_str("<span class='info'>");
            html.push_str(&speed_format(spot_wind_speed));
            html.push_str("</span>");
        }
        html.push_str("<td>");
        if let Some(gust_wind_speed) = self.gust_wind_speed {
            html.push_str("<span class='info'>");
            html.push_str(&speed_format(gust_wind_speed));
            html.push_str("</span>");
        }
        html.push_str("</table>");
        html
    }
}

impl WeatherSensor {
    pub const RESOURCE_N: &'static str = "weather_sensor";

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
    fn to_html_compact(&self, _anc: &WeatherSensorAnc) -> String {
        let location = HtmlStr::new(&self.location).with_len(12);
        let disabled = disabled_attr(self.controller.is_some());
        format!(
            "<span{disabled}>{location}</span>\
            <span class='{NAME}'>{self}</span>"
        )
    }

    /// Convert to status HTML
    fn to_html_status(&self, _anc: &WeatherSensorAnc) -> String {
        let location = HtmlStr::new(&self.location).with_len(64);
        let site_id = HtmlStr::new(&self.site_id);
        let alt_id = HtmlStr::new(&self.alt_id);
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
            <div class='row'>\
              <span>Obs</span>\
              <span class='info'>{sample_time}</span>\
            </div>\
            {sample}"
        )
    }

    /// Convert to edit HTML
    fn to_html_edit(&self, anc: &WeatherSensorAnc) -> String {
        let ctrl_loc = anc.controller_loc_html();
        let site_id = HtmlStr::new(&self.site_id);
        let alt_id = HtmlStr::new(&self.alt_id);
        let notes = HtmlStr::new(&self.notes);
        let controller = HtmlStr::new(&self.controller);
        let pin = OptVal(self.pin);
        format!(
            "{ctrl_loc}\
            <div class='row'>\
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