// Copyright (C) 2022-2026  Minnesota Department of Transportation
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
use crate::util::{ContainsLower, Fields, Input, TextArea, opt_ref};
use hatmil::{Page, html};
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

/// Build wind direction HTML
fn wind_dir_html<'p>(deg: u32, span: &'p mut html::Span<'p>) {
    span.class("info");
    if let Some(arrow) = dir_arrow(deg) {
        span.cdata(arrow);
    }
    span.close();
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

    /// Build weather data HTML
    fn build_html<'p>(
        &self,
        settings: Option<&WeatherSettings>,
        div: &'p mut html::Div<'p>,
    ) {
        if self.temperature_exists() {
            self.temperature_html(&mut div.details());
        }
        if self.atmospheric_exists() {
            self.atmospheric_html(&mut div.details());
        }
        if self.radiation_exists() {
            self.radiation_html(&mut div.details());
        }
        if let Some(wind_sensor) = &self.wind_sensor {
            self.wind_html(wind_sensor, &mut div.details());
        }
        if self.precip_exists() {
            self.precipitation_html(&mut div.details());
        }
        if let Some(data) = &self.pavement_sensor {
            pavement_html(pavement_settings(settings), data, &mut div.div());
        }
        if let Some(data) = &self.sub_surface_sensor {
            sub_surface_html(
                sub_surface_settings(settings),
                data,
                &mut div.div(),
            );
        }
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

    /// Build temperature HTML
    fn temperature_html<'p>(&self, details: &'p mut html::Details<'p>) {
        let mut summary = details.summary();
        summary.cdata("🌡️ ");
        if let Some(avg) = self.temperature_avg() {
            summary.cdata(format_temp(avg));
        }
        summary.close();
        let mut ul = details.ul();
        if let Some(sensor) = &self.temperature_sensor
            && sensor.len() > 1
        {
            for (i, temp) in sensor.iter().enumerate() {
                let mut li = ul.li();
                li.cdata("#").cdata(i).cdata(" Air ");
                if let Some(temp) = temp.air_temp {
                    li.cdata(format_temp(temp));
                }
                li.close();
            }
        }
        if let Some(temp) = self.min_air_temp {
            ul.li().cdata("24h low ").cdata(format_temp(temp)).close();
        }
        if let Some(temp) = self.max_air_temp {
            ul.li().cdata("24h high ").cdata(format_temp(temp)).close();
        }
        if let Some(temp) = self.dew_point_temp {
            ul.li().cdata("Dew point ").cdata(format_temp(temp)).close();
        }
        if let Some(temp) = self.wet_bulb_temp {
            ul.li().cdata("Wet bulb ").cdata(format_temp(temp)).close();
        }
        ul.close();
        details.close();
    }

    /// Build atmospheric HTML
    fn atmospheric_html<'p>(&self, details: &'p mut html::Details<'p>) {
        let mut summary = details.summary();
        summary.cdata(vis_situation(
            self.visibility_situation.as_deref().unwrap_or("unknown"),
        ));
        summary.close();
        let mut ul = details.ul();
        if let Some(visibility) = self.visibility {
            let v = (f64::from(visibility) * m).to::<DistUnit>();
            ul.li()
                .cdata("Visibility ")
                .cdata(format!("{v:.1}"))
                .close();
        }
        if let Some(rh) = self.relative_humidity {
            ul.li().cdata("RH ").cdata(rh).cdata("%").close();
        }
        if let Some(p) = self.atmospheric_pressure {
            let p = (p as f32) * PASCALS_TO_IN_HG;
            ul.li()
                .cdata("Barometer ")
                .cdata(format!("{p:.2}"))
                .cdata(" inHg");
        }
        details.close();
    }

    /// Build radiation data HTML
    fn radiation_html<'p>(&self, details: &'p mut html::Details<'p>) {
        let mut summary = details.summary();
        match self.cloud_situation.as_ref() {
            Some(cs) => summary.cdata(cloud_situation(cs)),
            None => summary.cdata("Sky"),
        };
        summary.close();
        let mut ul = details.ul();
        if let Some(sun) = self.total_sun {
            let d = format_duration(Duration::from_secs(60 * u64::from(sun)))
                .to_string();
            ul.li().cdata(d).cdata(" of sun").close();
        }
        if let Some(r) = &self.solar_radiation {
            ul.li()
                .cdata("Solar radiation: ")
                .cdata(*r)
                .cdata(" J/m²")
                .close();
        }
        if let Some(r) = &self.instantaneous_terrestrial_radiation {
            ul.li()
                .cdata("Instantaneous terrestrial: ")
                .cdata(*r)
                .cdata(" W/m²")
                .close();
        }
        if let Some(r) = &self.instantaneous_solar_radiation {
            ul.li()
                .cdata("Instantaneous solar: ")
                .cdata(*r)
                .cdata(" W/m²")
                .close();
        }
        if let Some(r) = &self.total_radiation {
            ul.li()
                .cdata("Total radiation: ")
                .cdata(*r)
                .cdata(" W/m²")
                .close();
            if let Some(p) = self.total_radiation_period {
                let d =
                    format_duration(Duration::from_secs(p.into())).to_string();
                ul.li().cdata("Total radiation period: ").cdata(d).close();
            }
        }
        details.close();
    }

    /// Build wind data HTML
    fn wind_html<'p>(
        &self,
        data: &[WindData],
        details: &'p mut html::Details<'p>,
    ) {
        let mut summary = details.summary();
        summary.cdata("🌬️ Wind");
        if let Some(ws) = data.iter().next() {
            if let Some(dir) = ws.avg_direction {
                summary.cdata(" 🧭 ");
                wind_dir_html(dir, &mut summary.span());
            }
            if let Some(speed) = ws.avg_speed {
                summary.cdata(" ");
                summary.cdata(format_speed(speed));
            }
        }
        summary.close();
        let mut ul = details.ul();
        for (i, ws) in data.iter().enumerate() {
            let num = if data.len() > 1 {
                Some(format!("#{i} "))
            } else {
                None
            };
            if i > 0 && (ws.avg_direction.is_some() || ws.avg_speed.is_some()) {
                let mut li = ul.li();
                if let Some(num) = &num {
                    li.cdata(num);
                }
                if let Some(dir) = ws.avg_direction {
                    li.cdata("Avg 🧭 ");
                    wind_dir_html(dir, &mut li.span());
                }
                if let Some(speed) = ws.avg_speed {
                    li.cdata(" ");
                    li.cdata(format_speed(speed));
                }
                li.close();
            }
            if ws.spot_direction.is_some() || ws.spot_speed.is_some() {
                let mut li = ul.li();
                if let Some(num) = &num {
                    li.cdata(num);
                }
                if let Some(dir) = ws.spot_direction {
                    li.cdata("Spot 🧭 ");
                    wind_dir_html(dir, &mut li.span());
                }
                if let Some(speed) = ws.spot_speed {
                    li.cdata(" ");
                    li.cdata(format_speed(speed));
                }
                li.close();
            }
            if ws.gust_direction.is_some() || ws.gust_speed.is_some() {
                let mut li = ul.li();
                if let Some(num) = &num {
                    li.cdata(num);
                }
                if let Some(dir) = ws.gust_direction {
                    li.cdata("Gust 🧭 ");
                    wind_dir_html(dir, &mut li.span());
                }
                if let Some(speed) = ws.gust_speed {
                    li.cdata(" ");
                    li.cdata(format_speed(speed));
                }
                li.close();
            }
        }
        details.close();
    }

    /// Build precipitation data HTML
    fn precipitation_html<'p>(&self, details: &'p mut html::Details<'p>) {
        let mut summary = details.summary();
        summary.cdata(precip_situation(
            self.precip_situation.as_deref().unwrap_or("unknown"),
        ));
        summary.close();
        let mut ul = details.ul();
        if let Some(precip) = self.precip_1_hour {
            ul.li().cdata("1h, ").cdata(format_depth(precip)).close();
        }
        if let Some(precip) = self.precip_3_hours {
            ul.li().cdata("3h, ").cdata(format_depth(precip)).close();
        }
        if let Some(precip) = self.precip_6_hours {
            ul.li().cdata("6h, ").cdata(format_depth(precip)).close();
        }
        if let Some(precip) = self.precip_12_hours {
            ul.li().cdata("12h, ").cdata(format_depth(precip)).close();
        }
        if let Some(precip) = self.precip_24_hours {
            ul.li().cdata("24h, ").cdata(format_depth(precip)).close();
        }
        details.close();
    }
}

/// Get pavement settings
fn pavement_settings(
    settings: Option<&WeatherSettings>,
) -> &[PavementSettings] {
    if let Some(settings) = settings
        && let Some(settings) = &settings.pavement_sensor
    {
        return settings;
    }
    &[]
}

/// Get pavement data as HTML
fn pavement_html<'p>(
    settings: &[PavementSettings],
    data: &[PavementData],
    div: &'p mut html::Div<'p>,
) {
    let len = settings.len().max(data.len());
    for i in 0..len {
        let mut details = div.details();
        let mut summary = details.summary();
        summary.cdata("Pavement ");
        if len > 1 {
            summary.cdata(format!("#{i} "));
        };
        if let Some(pd) = data.get(i) {
            if let Some(status) = &pd.surface_status {
                summary.cdata(status);
                if pd.surface_temp.is_some() {
                    summary.cdata(", ");
                }
            }
            if let Some(temp) = pd.surface_temp {
                summary.cdata(format_temp(temp));
            }
        }
        summary.close();
        let mut ul = details.ul();
        if let Some(pd) = data.get(i) {
            if let Some(err) = &pd.sensor_error {
                ul.li().cdata(err).cdata(" error").close();
            }
            if let Some(temp) = pd.pavement_temp {
                ul.li().cdata("Pavement ").cdata(format_temp(temp)).close();
            }
            if let Some(temp) = pd.freeze_point {
                ul.li()
                    .cdata("Freeze point ")
                    .cdata(format_temp(temp))
                    .close();
            }
            if let Some(depth_m) = pd.ice_or_water_depth {
                let d = format_depth(depth_m * 1_000.0);
                ul.li().cdata("Water/ice depth ").cdata(d).close();
            }
            if let Some(salinity) = pd.salinity {
                let sl = salinity;
                ul.li().cdata("Salinity ").cdata(sl).cdata(" ppm").close();
            }
            if let Some(signal) = &pd.black_ice_signal {
                ul.li().cdata(signal).close();
            }
            if let Some(friction) = &pd.friction {
                let f = friction;
                ul.li()
                    .cdata("Coef. of friction ")
                    .cdata(*f)
                    .cdata("%")
                    .close();
            }
        }
        if let Some(ps) = settings.get(i) {
            if let Some(loc) = &ps.location
                && !loc.trim().is_empty()
            {
                ul.li().cdata(loc).close();
            }
            if let Some(tp) = &ps.pavement_type {
                ul.li().cdata(tp).cdata(" pavement").close();
            }
            if let Some(tp) = &ps.sensor_type {
                ul.li().cdata("Type: ").cdata(tp).close();
            }
            if let Some(height) = ps.height {
                let h = format!("{height:.2}");
                ul.li().cdata("Height ").cdata(h).cdata(" m").close();
            }
            if let Some(exposure) = ps.exposure {
                let e = exposure;
                ul.li().cdata("Exposure ").cdata(e).cdata("%").close();
            }
        }
        details.close();
    }
    div.close();
}

/// Get sub-surface settings
fn sub_surface_settings(
    settings: Option<&WeatherSettings>,
) -> &[SubSurfaceSettings] {
    if let Some(settings) = settings
        && let Some(settings) = &settings.sub_surface_sensor
    {
        return settings;
    }
    &[]
}

/// Build sub-surface data HTML
fn sub_surface_html<'p>(
    settings: &[SubSurfaceSettings],
    data: &[SubSurfaceData],
    div: &'p mut html::Div<'p>,
) {
    let len = settings.len().max(data.len());
    for i in 0..len {
        let mut details = div.details();
        let mut summary = details.summary();
        summary.cdata("Sub-surface ");
        if len > 1 {
            summary.cdata(format!("#{i} "));
        };
        if let Some(sd) = data.get(i)
            && let Some(temp) = sd.temp
        {
            summary.cdata(format_temp(temp));
        }
        summary.close();
        let mut ul = details.ul();
        if let Some(ss) = settings.get(i) {
            if let Some(loc) = &ss.location {
                let loc = loc.trim();
                if !loc.is_empty() {
                    ul.li().cdata(loc).close();
                }
            }
            if let Some(tp) = &ss.sub_surface_type {
                ul.li().cdata("Type: ").cdata(tp).close();
            }
            if let Some(depth) = ss.depth {
                let d = format!("{depth:.2}");
                ul.li().cdata("Depth ").cdata(d).cdata(" m").close();
            }
        }
        if let Some(sd) = data.get(i) {
            if let Some(err) = &sd.sensor_error {
                ul.li().cdata(err).cdata(" error").close();
            }
            if let Some(moisture) = &sd.moisture {
                let mo = moisture;
                ul.li().cdata("Moisture ").cdata(*mo).cdata("%").close();
            }
        }
        details.close();
    }
    div.close();
}

impl WeatherSensor {
    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &WeatherSensorAnc) -> String {
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(anc.cio.item_states(self).to_string())
            .close();
        div = page.frag::<html::Div>();
        div.class("info fill")
            .cdata_len(opt_ref(&self.location), 32);
        String::from(page)
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &WeatherSensorAnc) -> String {
        if let Some((lat, lon)) = anc.loc.latlon() {
            fly_map_item(&self.name, lat, lon);
        }
        let mut page = Page::new();
        self.title(View::Status, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        anc.cio.item_states(self).tooltips(&mut div.span());
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.span()
            .class("info")
            .cdata_len(opt_ref(&self.location), 64)
            .close();
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.span()
            .class("info")
            .cdata(opt_ref(&self.site_id))
            .close();
        div.span()
            .class("info")
            .cdata(opt_ref(&self.alt_id))
            .close();
        div.close();
        if let Some(sample_time) = &self.sample_time {
            div = page.frag::<html::Div>();
            div.class("row");
            div.span().cdata("Obs").close();
            div.span().class("info").cdata(sample_time).close();
            div.close();
        }
        if let Some(data) = &self.sample {
            data.build_html(
                self.settings.as_ref(),
                &mut page.frag::<html::Div>(),
            );
        }
        String::from(page)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &WeatherSensorAnc) -> String {
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        self.title(View::Setup, &mut div);
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("site_id").cdata("Site ID").close();
        div.input()
            .id("site_id")
            .maxlength(20)
            .size(20)
            .value(opt_ref(&self.site_id));
        div.close();
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("alt_id").cdata("Alt ID").close();
        div.input()
            .id("alt_id")
            .maxlength(20)
            .size(20)
            .value(opt_ref(&self.alt_id));
        div.close();
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("notes").cdata("Notes").close();
        div.textarea()
            .id("notes")
            .maxlength(64)
            .rows(2)
            .cols(26)
            .cdata(opt_ref(&self.notes))
            .close();
        div.close();
        anc.cio.controller_html(self, &mut page.frag::<html::Div>());
        anc.cio.pin_html(self.pin, &mut page.frag::<html::Div>());
        self.footer_html(true, &mut page.frag::<html::Div>());
        String::from(page)
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

    /// Get the resource
    fn res() -> Res {
        Res::WeatherSensor
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[
            ItemState::Available,
            ItemState::Offline,
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
    fn item_state_main(&self, anc: &Self::Ancillary) -> ItemState {
        let states = anc.cio.item_states(self);
        if states.contains(ItemState::Inactive) {
            ItemState::Inactive
        } else if states.contains(ItemState::Offline) {
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
