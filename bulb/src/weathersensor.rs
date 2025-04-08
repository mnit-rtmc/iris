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
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::error::Result;
use crate::geoloc::{Loc, LocAnc};
use crate::item::ItemState;
use crate::start::fly_map_item;
use crate::util::{ContainsLower, Fields, HtmlStr, Input, TextArea};
use hatmil::Html;
use humantime::format_duration;
use mag::length::{m, mm};
use mag::temp::DegC;
use mag::time::s;
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use std::time::Duration;
use wasm_bindgen::JsValue;

/// Display Units
type TempUnit = mag::temp::DegF;
type DistUnit = mag::length::mi;
type DepthUnit = mag::length::In;
type SpeedUnit = mag::time::h;

/// Barometer conversion
const PASCALS_TO_IN_HG: f32 = 0.0002953;

/// Pavement sensor settings
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct PavementSettings {
    location: Option<String>,
    pavement_type: Option<String>,
    height: Option<f32>,
    exposure: Option<u32>,
    sensor_type: Option<String>,
}

/// Sub-surface sensor settings
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct SubSurfaceSettings {
    location: Option<String>,
    sub_surface_type: Option<String>,
    depth: Option<f32>,
}

/// Weather Sensor Settings
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct WeatherSettings {
    pavement_sensor: Option<Vec<PavementSettings>>,
    sub_surface_sensor: Option<Vec<SubSurfaceSettings>>,
}

/// Air temp data
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct AirTemp {
    air_temp: Option<f32>,
}

/// Wind sensor data
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct WindData {
    avg_speed: Option<f32>,
    avg_direction: Option<u32>,
    spot_speed: Option<f32>,
    spot_direction: Option<u32>,
    gust_speed: Option<f32>,
    gust_direction: Option<u32>,
}

/// Pavement sensor data
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct PavementData {
    surface_status: Option<String>,
    sensor_error: Option<String>,
    surface_temp: Option<f32>,
    pavement_temp: Option<f32>,
    freeze_point: Option<f32>,
    ice_or_water_depth: Option<f32>,
    salinity: Option<u32>,
    black_ice_signal: Option<String>,
    friction: Option<u32>,
}

/// Sub-surface sensor data
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct SubSurfaceData {
    sensor_error: Option<String>,
    temp: Option<f32>,
    moisture: Option<u32>,
}

/// Weather Sensor Data
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct WeatherData {
    visibility_situation: Option<String>,
    visibility: Option<u32>,
    relative_humidity: Option<u32>,
    atmospheric_pressure: Option<u32>,
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
    wind_sensor: Option<Vec<WindData>>,
    cloud_situation: Option<String>,
    total_sun: Option<u32>,
    solar_radiation: Option<i32>,
    instantaneous_terrestrial_radiation: Option<i32>,
    instantaneous_solar_radiation: Option<i32>,
    total_radiation: Option<i32>,
    total_radiation_period: Option<u32>,
    pavement_sensor: Option<Vec<PavementData>>,
    sub_surface_sensor: Option<Vec<SubSurfaceData>>,
}

/// Weather Sensor
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct WeatherSensor {
    pub name: String,
    pub location: Option<String>,
    pub site_id: Option<String>,
    pub alt_id: Option<String>,
    pub notes: Option<String>,
    pub geo_loc: Option<String>,
    pub controller: Option<String>,
    // secondary attributes
    pub pin: Option<u32>,
    pub settings: Option<WeatherSettings>,
    pub sample: Option<WeatherData>,
    pub sample_time: Option<String>,
}

/// Weather sensor ancillary data
#[derive(Default)]
pub struct WeatherSensorAnc {
    cio: ControllerIoAnc<WeatherSensor>,
    loc: LocAnc<WeatherSensor>,
}

impl AncillaryData for WeatherSensorAnc {
    type Primary = WeatherSensor;

    /// Construct ancillary weather sensor data
    fn new(pri: &WeatherSensor, view: View) -> Self {
        let cio = ControllerIoAnc::new(pri, view);
        let mut loc = LocAnc::new(pri, view);
        // Need geoloc to fly to location on map
        if let (View::Status, Some(nm)) = (view, pri.geoloc()) {
            loc.assets
                .push(Asset::GeoLoc(nm.to_string(), Res::WeatherSensor));
        }
        WeatherSensorAnc { cio, loc }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.cio.assets.pop().or_else(|| self.loc.assets.pop())
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &WeatherSensor,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        if let Asset::Controllers = asset {
            self.cio.set_asset(pri, asset, value)
        } else {
            self.loc.set_asset(pri, asset, value)
        }
    }
}

/// Get visibility situation string (from NTCIP 1204)
fn vis_situation(situation: &str) -> &'static str {
    match situation {
        "other" => "⛆ other visibility anomaly",
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
        _ => "Atmosphere",
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

/// Format depth quantity
fn format_depth(depth_mm: f32) -> String {
    let depth = (f64::from(depth_mm) * mm).to::<DepthUnit>();
    format!("{depth:.2}")
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
    }

    /// Get weather data as HTML
    fn to_html(&self, settings: Option<&WeatherSettings>) -> String {
        let mut html = String::new();
        if self.temperature_exists() {
            html.push_str(&self.temperature_html());
        }
        if self.atmospheric_exists() {
            html.push_str(&self.atmospheric_html());
        }
        if self.radiation_exists() {
            html.push_str(&self.radiation_html());
        }
        if let Some(wind_sensor) = &self.wind_sensor {
            html.push_str(&self.wind_html(wind_sensor));
        }
        if self.precip_exists() {
            html.push_str(&self.precipitation_html());
        }
        if let Some(data) = &self.pavement_sensor {
            html.push_str(&pavement_html(pavement_settings(settings), data));
        }
        if let Some(data) = &self.sub_surface_sensor {
            html.push_str(&sub_surface_html(
                sub_surface_settings(settings),
                data,
            ));
        }
        html
    }

    /// Get the average air temperature
    fn temperature_avg(&self) -> Option<f32> {
        match &self.temperature_sensor {
            Some(sensors) => sensors
                .iter()
                .filter_map(|at| at.air_temp.map(|t| (t, 1)))
                .reduce(|acc, (t, c)| (t + acc.0, c + acc.1))
                .map(|(total, count)| total / count as f32),
            None => None,
        }
    }

    /// Get temperature data as HTML
    fn temperature_html(&self) -> String {
        let mut html = String::new();
        html.push_str("<details><summary>🌡️ ");
        if let Some(avg) = self.temperature_avg() {
            html.push_str(&format_temp(avg));
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
            html.push_str("<li>24h low ");
            html.push_str(&format_temp(temp));
            html.push_str("</li>");
        }
        if let Some(temp) = self.max_air_temp {
            html.push_str("<li>24h high ");
            html.push_str(&format_temp(temp));
            html.push_str("</li>");
        }
        if let Some(temp) = self.dew_point_temp {
            html.push_str("<li>Dew point ");
            html.push_str(&format_temp(temp));
            html.push_str("</li>");
        }
        if let Some(temp) = self.wet_bulb_temp {
            html.push_str("<li>Wet bulb ");
            html.push_str(&format_temp(temp));
            html.push_str("</li>");
        }
        html.push_str("</ul></details>");
        html
    }

    /// Get atmospheric HTML
    fn atmospheric_html(&self) -> String {
        let mut html = String::new();
        html.push_str("<details><summary>");
        html.push_str(vis_situation(
            self.visibility_situation.as_deref().unwrap_or("unknown"),
        ));
        html.push_str("</summary><ul>");
        if let Some(visibility) = self.visibility {
            let vis = (f64::from(visibility) * m).to::<DistUnit>();
            html.push_str(&format!("<li>Visibility {vis:.1}</li>"));
        }
        if let Some(rh) = self.relative_humidity {
            html.push_str(&format!("<li>RH {rh}%</li>"));
        }
        if let Some(p) = self.atmospheric_pressure {
            let in_hg = (p as f32) * PASCALS_TO_IN_HG;
            html.push_str(&format!("<li>Barometer {in_hg:.2} inHg</li>"));
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
        if let Some(sun) = self.total_sun {
            let dur = format_duration(Duration::from_secs(60 * u64::from(sun)));
            html.push_str(&format!("<li>{dur} of sun</li>"));
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
            if let Some(p) = self.total_radiation_period {
                let dur = format_duration(Duration::from_secs(p.into()));
                html.push_str(&format!(
                    "<li>Total radiation period: {dur}</li>"
                ));
            }
        }
        html.push_str("</ul></details>");
        html
    }

    /// Get wind data as HTML
    fn wind_html(&self, data: &[WindData]) -> String {
        let mut html = String::new();
        html.push_str("<details><summary>🌬️ Wind");
        if let Some(ws) = data.iter().next() {
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
        for (i, ws) in data.iter().enumerate() {
            let li = if data.len() > 1 {
                format!("<li>#{i} ")
            } else {
                "<li>".into()
            };
            if i > 0 && (ws.avg_direction.is_some() || ws.avg_speed.is_some()) {
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

    /// Get precipitation data as HTML
    fn precipitation_html(&self) -> String {
        let mut html = String::new();
        html.push_str("<details><summary>");
        html.push_str(precip_situation(
            self.precip_situation.as_deref().unwrap_or("unknown"),
        ));
        html.push_str("</summary><ul>");
        if let Some(precip) = self.precip_1_hour {
            html.push_str("<li>1h, ");
            html.push_str(&format_depth(precip));
            html.push_str("</li>");
        }
        if let Some(precip) = self.precip_3_hours {
            html.push_str("<li>3h, ");
            html.push_str(&format_depth(precip));
            html.push_str("</li>");
        }
        if let Some(precip) = self.precip_6_hours {
            html.push_str("<li>6h, ");
            html.push_str(&format_depth(precip));
            html.push_str("</li>");
        }
        if let Some(precip) = self.precip_12_hours {
            html.push_str("<li>12h, ");
            html.push_str(&format_depth(precip));
            html.push_str("</li>");
        }
        if let Some(precip) = self.precip_24_hours {
            html.push_str("<li>24h, ");
            html.push_str(&format_depth(precip));
            html.push_str("</li>");
        }
        html.push_str("</ul></details>");
        html
    }
}

/// Get pavement settings
fn pavement_settings(
    settings: Option<&WeatherSettings>,
) -> &[PavementSettings] {
    if let Some(settings) = settings {
        if let Some(settings) = &settings.pavement_sensor {
            return settings;
        }
    }
    &[]
}

/// Get pavement data as HTML
fn pavement_html(
    settings: &[PavementSettings],
    data: &[PavementData],
) -> String {
    let len = settings.len().max(data.len());
    let mut html = String::new();
    for i in 0..len {
        html.push_str("<details><summary>Pavement ");
        if len > 1 {
            html.push_str(&format!("#{i} "));
        };
        if let Some(pd) = data.get(i) {
            if let Some(status) = &pd.surface_status {
                html.push_str(&HtmlStr::new(status).to_string());
                if pd.surface_temp.is_some() {
                    html.push_str(", ");
                }
            }
            if let Some(temp) = pd.surface_temp {
                html.push_str(&format_temp(temp));
            }
        }
        html.push_str("</summary><ul>");
        if let Some(pd) = data.get(i) {
            if let Some(err) = &pd.sensor_error {
                let err = HtmlStr::new(err);
                html.push_str(&format!("<li>{err} error</li>"));
            }
            if let Some(temp) = pd.pavement_temp {
                html.push_str("<li>Pavement ");
                html.push_str(&format_temp(temp));
                html.push_str("</li>");
            }
            if let Some(temp) = pd.freeze_point {
                html.push_str("<li>Freeze point ");
                html.push_str(&format_temp(temp));
                html.push_str("</li>");
            }
            if let Some(depth_m) = pd.ice_or_water_depth {
                html.push_str("<li>Water/ice depth ");
                html.push_str(&format_depth(depth_m * 1_000.0));
                html.push_str("</li>");
            }
            if let Some(salinity) = pd.salinity {
                html.push_str(&format!("<li>Salinity {salinity} ppm</li>"));
            }
            if let Some(signal) = &pd.black_ice_signal {
                let signal = HtmlStr::new(signal);
                html.push_str(&format!("<li>{signal}</li>"));
            }
            if let Some(friction) = &pd.friction {
                html.push_str(&format!(
                    "<li>Coef. of friction {friction}%</li>"
                ));
            }
        }
        if let Some(ps) = settings.get(i) {
            if let Some(loc) = &ps.location {
                if !loc.trim().is_empty() {
                    let loc = HtmlStr::new(loc);
                    html.push_str(&format!("<li>{loc}</li>"));
                }
            }
            if let Some(tp) = &ps.pavement_type {
                let tp = HtmlStr::new(tp);
                html.push_str(&format!("<li>{tp} pavement</li>"));
            }
            if let Some(tp) = &ps.sensor_type {
                let tp = HtmlStr::new(tp);
                html.push_str(&format!("<li>Type: {tp}</li>"));
            }
            if let Some(height) = ps.height {
                html.push_str(&format!("<li>Height {height} m</li>"));
            }
            if let Some(exposure) = ps.exposure {
                html.push_str(&format!("<li>Exposure {exposure}%</li>"));
            }
        }
        html.push_str("</ul></details>");
    }
    html
}

/// Get sub-surface settings
fn sub_surface_settings(
    settings: Option<&WeatherSettings>,
) -> &[SubSurfaceSettings] {
    if let Some(settings) = settings {
        if let Some(settings) = &settings.sub_surface_sensor {
            return settings;
        }
    }
    &[]
}

/// Get sub-surface data as HTML
fn sub_surface_html(
    settings: &[SubSurfaceSettings],
    data: &[SubSurfaceData],
) -> String {
    let len = settings.len().max(data.len());
    let mut html = String::new();
    for i in 0..len {
        html.push_str("<details><summary>Sub-surface ");
        if len > 1 {
            html.push_str(&format!("#{i} "));
        };
        if let Some(sd) = data.get(i) {
            if let Some(temp) = sd.temp {
                html.push_str(&format_temp(temp));
            }
        }
        html.push_str("</summary><ul>");
        if let Some(ss) = settings.get(i) {
            if let Some(loc) = &ss.location {
                if !loc.trim().is_empty() {
                    let loc = HtmlStr::new(loc);
                    html.push_str(&format!("<li>{loc}</li>"));
                }
            }
            if let Some(tp) = &ss.sub_surface_type {
                let tp = HtmlStr::new(tp);
                html.push_str(&format!("<li>Type: {tp}</li>"));
            }
            if let Some(depth) = ss.depth {
                html.push_str(&format!("<li>Depth {depth} m</li>"));
            }
        }
        if let Some(sd) = data.get(i) {
            if let Some(err) = &sd.sensor_error {
                let err = HtmlStr::new(err);
                html.push_str(&format!("<li>{err} error</li>"));
            }
            if let Some(moisture) = &sd.moisture {
                html.push_str(&format!("<li>Moisture {moisture}%</li>"));
            }
        }
        html.push_str("</ul></details>");
    }
    html
}

impl WeatherSensor {
    /// Get sample as HTML
    fn sample_html(&self) -> String {
        match &self.sample {
            Some(data) => data.to_html(self.settings.as_ref()),
            None => "".into(),
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &WeatherSensorAnc) -> String {
        let name = HtmlStr::new(self.name());
        let item_states = anc.cio.item_states(self);
        let location = HtmlStr::new(&self.location).with_len(32);
        format!(
            "<div class='title row'>{name} {item_states}</div>\
            <div class='info fill'>{location}</div>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &WeatherSensorAnc) -> String {
        if let Some((lat, lon)) = anc.loc.latlon() {
            fly_map_item(&self.name, lat, lon);
        }
        let title = String::from(self.title(View::Status));
        let mut html = Html::new();
        anc.cio.item_states(self).tooltips(&mut html);
        let item_states = String::from(html);
        let location = HtmlStr::new(&self.location).with_len(64);
        let site_id = HtmlStr::new(&self.site_id);
        let alt_id = HtmlStr::new(&self.alt_id);
        let mut html = format!(
            "{title}\
            <div class='row'>{item_states}</div>\
            <div class='row'>\
              <span class='info'>{location}</span>\
            </div>\
            <div class='row'>\
              <span class='info'>{site_id}</span>\
              <span class='info'>{alt_id}</span>\
            </div>"
        );
        if let Some(sample_time) = &self.sample_time {
            html.push_str(&format!(
                "<div class='row'>\
                  <span>Obs</span>\
                  <span class='info'>{sample_time}</span>\
                </div>"
            ));
        }
        html.push_str(&self.sample_html());
        html
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &WeatherSensorAnc) -> String {
        let title = String::from(self.title(View::Setup));
        let site_id = HtmlStr::new(&self.site_id);
        let alt_id = HtmlStr::new(&self.alt_id);
        let notes = HtmlStr::new(&self.notes);
        let mut html = Html::new();
        anc.cio.controller_html(self, &mut html);
        let controller = String::from(html);
        let mut html = Html::new();
        anc.cio.pin_html(self.pin, &mut html);
        let pin = String::from(html);
        let footer = self.footer(true);
        format!(
            "{title}\
            <div class='row'>\
              <label for='site_id'>Site ID</label>\
              <input id='site_id' maxlength='20' size='20' \
                     value='{site_id}'>\
            </div>\
            <div class='row'>\
              <label for='alt_id'>Alt ID</label>\
              <input id='alt_id' maxlength='20' size='20' \
                     value='{alt_id}'>\
            </div>\
            <div class='row'>\
              <label for='notes'>Notes</label>\
              <textarea id='notes' maxlength='64' rows='2' \
                        cols='26'>{notes}</textarea>\
            </div>\
            {controller}\
            {pin}\
            {footer}"
        )
    }
}

impl ControllerIo for WeatherSensor {
    /// Get controller name
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Loc for WeatherSensor {
    /// Get geo location name
    fn geoloc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }
}

impl Card for WeatherSensor {
    type Ancillary = WeatherSensorAnc;

    /// Display name
    const DNAME: &'static str = "🌦️ Weather Sensor";

    /// Get the resource
    fn res() -> Res {
        Res::WeatherSensor
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

    /// Get the main item state
    fn item_state_main(&self, anc: &Self::Ancillary) -> ItemState {
        let item_states = anc.cio.item_states(self);
        if item_states.is_match(ItemState::Inactive.code()) {
            ItemState::Inactive
        } else if item_states.is_match(ItemState::Offline.code()) {
            ItemState::Offline
        } else {
            ItemState::Available
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &WeatherSensorAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || self.site_id.contains_lower(search)
            || self.alt_id.contains_lower(search)
            || anc.cio.item_states(self).is_match(search)
            || self.notes.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &WeatherSensorAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Location => anc.loc.to_html_loc(self),
            View::Setup => self.to_html_setup(anc),
            View::Status => self.to_html_status(anc),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("site_id", &self.site_id);
        fields.changed_input("alt_id", &self.alt_id);
        fields.changed_text_area("notes", &self.notes);
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }

    /// Get changed fields on Location view
    fn changed_location(&self, anc: WeatherSensorAnc) -> String {
        anc.loc.changed_location()
    }
}
