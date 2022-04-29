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
use crate::resource::{
    disabled_attr, Card, View, EDIT_BUTTON, LOC_BUTTON, NAME,
};
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal, TextArea};
use mag::length::{m, mm};
use mag::temp::DegC;
use mag::time::s;
use serde::{Deserialize, Serialize};
use std::fmt;

/// Display Units
type TempUnit = mag::temp::DegF;
type DistUnit = mag::length::mi;
type RainUnit = mag::length::In;
type SpeedUnit = mag::time::h;

/// Air temp data
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct AirTemp {
    air_temp: Option<f32>,
}

/// Wind sensor data
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct WindSensor {
    avg_speed: Option<f32>,
    avg_direction: Option<u32>,
    spot_speed: Option<f32>,
    spot_direction: Option<u32>,
    gust_speed: Option<f32>,
    gust_direction: Option<u32>,
}

/// Weather Sensor Data
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct WeatherData {
    visibility_situation: Option<String>,
    visibility: Option<u32>,
    relative_humidity: Option<u32>,
    temperature_sensor: Option<Vec<AirTemp>>,
    dew_point_temp: Option<f32>,
    wet_bulb_temp: Option<f32>,
    /// Minimum air temp in last 24 hours (first sensor)
    min_air_temp: Option<f32>,
    /// Maximum air temp in last 24 hours (first sensor)
    max_air_temp: Option<f32>,
    precip_situation: Option<String>,
    precip_1_hour: Option<f32>,
    precip_3_hours: Option<f32>,
    precip_6_hours: Option<f32>,
    precip_12_hours: Option<f32>,
    precip_24_hours: Option<f32>,
    wind_sensor: Option<Vec<WindSensor>>,
    cloud_situation: Option<String>,
    total_sun: Option<u32>,
    solar_radiation: Option<i32>,
    instantaneous_terrestrial_radiation: Option<i32>,
    instantaneous_solar_radiation: Option<i32>,
    total_radiation: Option<i32>,
    total_radiation_period: Option<u32>,
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

/// Get direction and arrow from degrees
fn dir_arrow(deg: u32) -> Option<&'static str> {
    match deg {
        // 0° ± 22.5
        0..=22 | 338..=360 => Some("N ↓"),
        // 45° ±22.5
        23..=67 => Some("NE ↙"),
        // 90° ±22.5
        68..=112 => Some("E ←"),
        // 135° ±22.5
        113..=157 => Some("SE ↖"),
        // 180° ±22.5
        158..=202 => Some("S ↑"),
        // 225° ±22.5
        203..=247 => Some("SW ↗"),
        // 270° ±22.5
        248..=292 => Some("W →"),
        // 315° ±22.5
        293..=337 => Some("NW ↘"),
        _ => None,
    }
}

/// Get wind direction as HTML
fn wind_dir_html(deg: u32) -> String {
    let mut html = String::new();
    html.push_str("<span class='info'>");
    if let Some(arrow) = dir_arrow(deg) {
        html.push_str(arrow);
    }
    html.push_str("</span>");
    html
}

/// Format temperature quantity
fn format_temp(temp: f32) -> String {
    let temp = (f64::from(temp) * DegC).to::<TempUnit>();
    format!("{temp:.1}")
}

/// Get precipitation situation string (from NTCIP 1204)
fn precip_situation(situation: &str) -> &'static str {
    match situation {
        "noPrecipitation" => "🌂 No Precipitation",
        "unidentifiedSlight" => "🌧️ Slight precipitation",
        "unidentifiedModerate" => "🌧️ Moderate precipitation",
        "unidentifiedHeavy" => "🌧️ Heavy precipitation",
        "snowSlight" => "🌨️ Slight snow",
        "snowModerate" => "🌨️ Moderate snow",
        "snowHeavy" => "🌨️ Heavy snow",
        "rainSlight" => "🌧️ Slight rain",
        "rainModerate" => "🌧️ Moderate rain",
        "rainHeavy" => "🌧️ Heavy rain",
        "frozenPrecipitationSlight" => "🧊 Slight sleet",
        "frozenPrecipitationModerate" => "🧊 Moderate sleet",
        "frozenPrecipitationHeavy" => "🧊 Heavy sleet",
        _ => "🌧️ Precipitation",
    }
}

/// Format precipitation quantity
fn format_precip(precip: f32) -> String {
    let precip = (f64::from(precip) * mm).to::<RainUnit>();
    format!("{precip:.2}")
}

/// Format wind speed quantity
fn format_speed(speed: f32) -> String {
    let speed = (f64::from(speed) * m / s).to::<DistUnit, SpeedUnit>();
    format!("{speed:.0}")
}

/// Get cloud situation string (from NTCIP 1204)
fn cloud_situation(situation: &str) -> &'static str {
    match situation {
        "overcast" => "☁️ Overcast",
        "cloudy" => "🌥️ Mostly cloudy",
        "partlyCloudy" => "⛅ Partly cloudy",
        "mostlyClear" => "🌤️ Mostly clear",
        "clear" => "☀️ Clear",
        _ => "☁️ Unknown",
    }
}

impl WeatherData {
    /// Check if atmospheric data exists
    fn atmospheric_exists(&self) -> bool {
        self.visibility_situation.is_some()
            || self.visibility.is_some()
            || self.relative_humidity.is_some()
    }

    /// Check if temperature data exists
    fn temperature_exists(&self) -> bool {
        self.temperature_sensor.is_some()
            || self.dew_point_temp.is_some()
            || self.wet_bulb_temp.is_some()
            || self.min_air_temp.is_some()
            || self.max_air_temp.is_some()
    }

    /// Check if precip data exists
    fn precip_exists(&self) -> bool {
        self.precip_situation.is_some()
            || self.precip_1_hour.is_some()
            || self.precip_3_hours.is_some()
            || self.precip_6_hours.is_some()
            || self.precip_12_hours.is_some()
            || self.precip_24_hours.is_some()
    }

    /// Check if radiation data exists
    fn radiation_exists(&self) -> bool {
        self.cloud_situation.is_some()
            || self.total_sun.is_some()
            || self.solar_radiation.is_some()
            || self.instantaneous_terrestrial_radiation.is_some()
            || self.instantaneous_solar_radiation.is_some()
            || self.total_radiation.is_some()
            || self.total_radiation_period.is_some()
    }

    /// Get weather data as HTML
    fn to_html(&self) -> String {
        let mut html = String::new();
        if self.atmospheric_exists() {
            html.push_str(&self.atmospheric_html());
        }
        if self.temperature_exists() {
            html.push_str(&self.temperature_html());
        }
        if let Some(wind_sensor) = &self.wind_sensor {
            html.push_str(&self.wind_html(wind_sensor));
        }
        if self.precip_exists() {
            html.push_str(&self.precipitation_html());
        }
        if self.radiation_exists() {
            html.push_str(&self.radiation_html());
        }
        html
    }

    /// Get atmospheric HTML
    fn atmospheric_html(&self) -> String {
        let mut html = String::new();
        html.push_str("<details><summary>Atmosphere ");
        if let Some(visibility_situation) = &self.visibility_situation {
            html.push_str(vis_situation(visibility_situation));
        }
        html.push_str("</summary><ul>");
        if let Some(visibility) = self.visibility {
            let vis = (f64::from(visibility) * m).to::<DistUnit>();
            html.push_str(&format!("<li>Visibility {vis:.1}</li>"));
        }
        if let Some(rh) = self.relative_humidity {
            html.push_str(&format!("<li>RH {rh}%</li>"));
        }
        html.push_str("</ul></details>");
        html
    }

    /// Get temperature data as HTML
    fn temperature_html(&self) -> String {
        let mut html = String::new();
        html.push_str("<details><summary>🌡️ ");
        if let Some(temperature_sensor) = &self.temperature_sensor {
            let n_temps = temperature_sensor
                .iter()
                .filter(|at| at.air_temp.is_some())
                .count();
            if n_temps > 0 {
                let total = temperature_sensor
                    .iter()
                    .filter_map(|at| at.air_temp)
                    .sum::<f32>();
                html.push_str(&format_temp(total / n_temps as f32));
            }
        }
        html.push_str("</summary><ul>");
        if let Some(sensor) = &self.temperature_sensor {
            if sensor.len() > 1 {
                for (i, temp) in sensor.iter().enumerate() {
                    html.push_str(&format!("<li>#{i} Air "));
                    if let Some(temp) = temp.air_temp {
                        html.push_str(&format_temp(temp));
                    }
                    html.push_str("</li>");
                }
            }
        }
        if let Some(temp) = self.min_air_temp {
            html.push_str(&format!("<li>Min 24-hour "));
            html.push_str(&format_temp(temp));
            html.push_str(&format!("</li>"));
        }
        if let Some(temp) = self.max_air_temp {
            html.push_str(&format!("<li>Max 24-hour "));
            html.push_str(&format_temp(temp));
            html.push_str(&format!("</li>"));
        }
        if let Some(temp) = self.dew_point_temp {
            html.push_str(&format!("<li>Dew point "));
            html.push_str(&format_temp(temp));
            html.push_str(&format!("</li>"));
        }
        if let Some(temp) = self.wet_bulb_temp {
            html.push_str(&format!("<li>Wet bulb "));
            html.push_str(&format_temp(temp));
            html.push_str(&format!("</li>"));
        }
        html.push_str("</ul></details>");
        html
    }

    /// Get precipitation data as HTML
    fn precipitation_html(&self) -> String {
        let mut html = String::new();
        html.push_str("<details><summary>");
        html.push_str(precip_situation(
            &self.precip_situation.as_deref().unwrap_or("unknown"),
        ));
        html.push_str("</summary><ul>");
        if let Some(precip) = self.precip_1_hour {
            html.push_str("<li>1h, ");
            html.push_str(&format_precip(precip));
            html.push_str("</li>");
        }
        if let Some(precip) = self.precip_3_hours {
            html.push_str("<li>3h, ");
            html.push_str(&format_precip(precip));
            html.push_str("</li>");
        }
        if let Some(precip) = self.precip_6_hours {
            html.push_str("<li>6h, ");
            html.push_str(&format_precip(precip));
            html.push_str("</li>");
        }
        if let Some(precip) = self.precip_12_hours {
            html.push_str("<li>12h, ");
            html.push_str(&format_precip(precip));
            html.push_str("</li>");
        }
        if let Some(precip) = self.precip_24_hours {
            html.push_str("<li>24h, ");
            html.push_str(&format_precip(precip));
            html.push_str("</li>");
        }
        html.push_str("</ul></details>");
        html
    }

    /// Get wind data as HTML
    fn wind_html(&self, wind_sensor: &[WindSensor]) -> String {
        let mut html = String::new();
        html.push_str("<details><summary>🌬️ Wind");
        if let Some(ws) = wind_sensor.iter().next() {
            if let Some(dir) = ws.avg_direction {
                html.push_str(" 🧭 ");
                html.push_str(&wind_dir_html(dir));
            }
            if let Some(speed) = ws.avg_speed {
                html.push(' ');
                html.push_str(&format_speed(speed));
            }
        }
        html.push_str("</summary><ul>");
        for (i, ws) in wind_sensor.iter().enumerate() {
            let li = if wind_sensor.len() > 1 {
                format!("<li>#{i} ")
            } else {
                format!("<li>")
            };
            if i > 0 {
                if ws.avg_direction.is_some() || ws.avg_speed.is_some() {
                    html.push_str(&li);
                    if let Some(dir) = ws.avg_direction {
                        html.push_str("Avg 🧭 ");
                        html.push_str(&wind_dir_html(dir));
                    }
                    if let Some(speed) = ws.avg_speed {
                        html.push(' ');
                        html.push_str(&format_speed(speed));
                    }
                    html.push_str("</li>");
                }
            }
            if ws.spot_direction.is_some() || ws.spot_speed.is_some() {
                html.push_str(&li);
                if let Some(dir) = ws.spot_direction {
                    html.push_str("Spot 🧭 ");
                    html.push_str(&wind_dir_html(dir));
                }
                if let Some(speed) = ws.spot_speed {
                    html.push(' ');
                    html.push_str(&format_speed(speed));
                }
                html.push_str("</li>");
            }
            if ws.gust_direction.is_some() || ws.gust_speed.is_some() {
                html.push_str(&li);
                if let Some(dir) = ws.gust_direction {
                    html.push_str("Gust 🧭 ");
                    html.push_str(&wind_dir_html(dir));
                }
                if let Some(speed) = ws.gust_speed {
                    html.push(' ');
                    html.push_str(&format_speed(speed));
                }
                html.push_str("</li>");
            }
        }
        html.push_str("</ul></details>");
        html
    }

    /// Get radiation data as HTML
    fn radiation_html(&self) -> String {
        let mut html = String::new();
        html.push_str("<details><summary>");
        match &self.cloud_situation {
            Some(situation) => html.push_str(cloud_situation(situation)),
            None => html.push_str("Sky"),
        }
        html.push_str("</summary><ul>");
        if let Some(sun) = &self.total_sun {
            html.push_str(&format!("<li>{sun} minutes of sun</li>"));
        }
        if let Some(rad) = &self.solar_radiation {
            html.push_str(&format!("<li>Solar radiation: {rad} J/m²</li>"));
        }
        if let Some(rad) = &self.instantaneous_terrestrial_radiation {
            html.push_str(&format!(
                "<li>Instantaneous terrestrial: {rad} W/m²</li>"
            ));
        }
        if let Some(rad) = &self.instantaneous_solar_radiation {
            html.push_str(&format!("<li>Instantaneous solar: {rad} W/m²</li>"));
        }
        if let Some(rad) = &self.total_radiation {
            html.push_str(&format!("<li>Total radiation: {rad} W/m²</li>"));
        }
        if let Some(p) = &self.total_radiation_period {
            html.push_str(&format!("<li>Total radiation period: {p} s</li>"));
        }
        html.push_str("</ul></details>");
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

    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let location = HtmlStr::new(&self.location).with_len(12);
        let disabled = disabled_attr(self.controller.is_some());
        format!(
            "<span{disabled}>{location}</span>\
            <span class='{NAME}'>{self}</span>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &WeatherSensorAnc, config: bool) -> String {
        let location = HtmlStr::new(&self.location).with_len(64);
        let site_id = HtmlStr::new(&self.site_id);
        let alt_id = HtmlStr::new(&self.alt_id);
        let sample_time = self.sample_time.as_deref().unwrap_or("-");
        let sample = self.sample_html();
        let mut status = format!(
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
        );
        if config {
            status.push_str("<div class='row'>");
            status.push_str(&anc.controller_button());
            status.push_str(LOC_BUTTON);
            status.push_str(EDIT_BUTTON);
            status.push_str("</div>");
        }
        status
    }

    /// Convert to Edit HTML
    fn to_html_edit(&self) -> String {
        let site_id = HtmlStr::new(&self.site_id);
        let alt_id = HtmlStr::new(&self.alt_id);
        let notes = HtmlStr::new(&self.notes);
        let controller = HtmlStr::new(&self.controller);
        let pin = OptVal(self.pin);
        format!(
            "<div class='row'>\
              <label for='site_id'>Site ID</label>\
              <input id='site_id' maxlength='20' size='20' \
                     value='{site_id}'/>\
            </div>\
            <div class='row'>\
              <label for='alt_id'>Alt ID</label>\
              <input id='alt_id' maxlength='20' size='20' \
                     value='{alt_id}'/>\
            </div>\
            <div class='row'>\
              <label for='notes'>Notes</label>\
              <textarea id='notes' maxlength='64' rows='2' \
                        cols='26'>{notes}</textarea>\
            </div>\
            <div class='row'>\
              <label for='controller'>Controller</label>\
              <input id='controller' maxlength='20' size='20' \
                     value='{controller}'/>\
            </div>\
            <div class='row'>\
              <label for='pin'>Pin</label>\
              <input id='pin' type='number' min='1' max='104' \
                     size='8' value='{pin}'/>\
            </div>"
        )
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

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &WeatherSensorAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Compact => self.to_html_compact(),
            View::Status(config) => self.to_html_status(anc, config),
            View::Edit => self.to_html_edit(),
            _ => unreachable!(),
        }
    }

    /// Get changed fields from Edit form
    fn changed_fields(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("site_id", &self.site_id);
        fields.changed_input("alt_id", &self.alt_id);
        fields.changed_text_area("notes", &self.notes);
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }
}
