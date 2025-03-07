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
use crate::item::{ItemState, ItemStates};
use crate::start::fly_map_item;
use crate::util::{
    ContainsLower, Fields, HtmlStr, Input, OptVal, Select, TextArea,
};
use base64::{Engine as _, engine::general_purpose::STANDARD_NO_PAD as b64enc};
use gift::block::DisposalMethod;
use gift::{Encoder, Step};
use pix::matte::Matte8;
use pix::ops::SrcOver;
use pix::rgb::{Rgba8p, SRgb8};
use pix::{Palette, Raster};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use std::io::Write;
use wasm_bindgen::JsValue;
use web_sys::console;

/// Meter signal state for rendering GIF
#[derive(Clone, Copy, Debug)]
enum MeterState {
    Off,
    LowYellow,
    Green,
    Yellow,
    Red,
}

/// Meter Lock
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct MeterLock {
    pub reason: String,
    pub rate: Option<u32>,
    pub expires: Option<String>,
    pub user_id: Option<String>,
}

/// Meter Status
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct MeterStatus {
    pub rate: Option<u32>,
    pub queue: Option<String>,
    pub fault: Option<String>,
}

/// Ramp Meter
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct RampMeter {
    pub name: String,
    pub location: Option<String>,
    pub controller: Option<String>,
    pub notes: Option<String>,
    pub lock: Option<MeterLock>,
    pub status: Option<MeterStatus>,
    // secondary attributes
    pub geo_loc: Option<String>,
    pub pin: Option<u32>,
    pub beacon: Option<String>,
    pub preset: Option<String>,
    pub meter_type: Option<u32>,
    pub algorithm: Option<u32>,
    pub storage: Option<u32>,
    pub max_wait: Option<u32>,
    pub am_target: Option<u32>,
    pub pm_target: Option<u32>,
}

/// Meter Algorithms
#[derive(Debug, Deserialize)]
pub struct MeterAlgorithm {
    pub id: u32,
    pub description: String,
}

/// Meter Types
#[derive(Debug, Deserialize)]
pub struct MeterType {
    pub id: u32,
    pub description: String,
    #[allow(dead_code)]
    pub lanes: u32,
}

/// Ramp meter ancillary data
#[derive(Default)]
pub struct RampMeterAnc {
    cio: ControllerIoAnc<RampMeter>,
    loc: LocAnc<RampMeter>,
    meter_types: Vec<MeterType>,
    algorithms: Vec<MeterAlgorithm>,
}

impl RampMeterAnc {
    /// Create an HTML `select` element of meter types
    fn meter_types_html(&self, pri: &RampMeter) -> String {
        let mut html = String::new();
        html.push_str("<select id='meter_type'>");
        for tp in &self.meter_types {
            html.push_str("<option value='");
            html.push_str(&tp.id.to_string());
            html.push('\'');
            if Some(tp.id) == pri.meter_type {
                html.push_str(" selected");
            }
            html.push('>');
            html.push_str(&tp.description);
            html.push_str("</option>");
        }
        html.push_str("</select>");
        html
    }

    /// Create an HTML `select` element of metering algorithms
    fn algorithms_html(&self, pri: &RampMeter) -> String {
        let mut html = String::new();
        html.push_str("<select id='algorithm'>");
        for alg in &self.algorithms {
            html.push_str("<option value='");
            html.push_str(&alg.id.to_string());
            html.push('\'');
            if Some(alg.id) == pri.algorithm {
                html.push_str(" selected");
            }
            html.push('>');
            html.push_str(&alg.description);
            html.push_str("</option>");
        }
        html.push_str("</select>");
        html
    }
}

impl AncillaryData for RampMeterAnc {
    type Primary = RampMeter;

    /// Construct ancillary ramp meter data
    fn new(pri: &RampMeter, view: View) -> Self {
        let mut cio = ControllerIoAnc::new(pri, view);
        if let View::Setup = view {
            cio.assets.push(Asset::MeterAlgorithms);
            cio.assets.push(Asset::MeterTypes);
        }
        let loc = LocAnc::new(pri, view);
        RampMeterAnc {
            cio,
            loc,
            meter_types: Vec::new(),
            algorithms: Vec::new(),
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.cio.assets.pop().or_else(|| self.loc.assets.pop())
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &RampMeter,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::Controllers => self.cio.set_asset(pri, asset, value),
            Asset::MeterAlgorithms => {
                self.algorithms = serde_wasm_bindgen::from_value(value)?;
                Ok(())
            }
            Asset::MeterTypes => {
                self.meter_types = serde_wasm_bindgen::from_value(value)?;
                Ok(())
            }
            _ => self.loc.set_asset(pri, asset, value),
        }
    }
}

impl MeterState {
    /// Get red indication color
    fn red(self) -> Rgba8p {
        match self {
            MeterState::Red => Rgba8p::new(255, 0, 0, 255),
            _ => Rgba8p::new(16, 0, 0, 255),
        }
    }

    /// Get yellow indication color
    fn yellow(self) -> Rgba8p {
        match self {
            MeterState::Yellow => Rgba8p::new(255, 160, 0, 255),
            MeterState::LowYellow => Rgba8p::new(128, 80, 0, 255),
            _ => Rgba8p::new(16, 10, 0, 255),
        }
    }

    /// Get green indication color
    fn green(self) -> Rgba8p {
        match self {
            MeterState::Green => Rgba8p::new(0, 224, 0, 255),
            _ => Rgba8p::new(0, 14, 0, 255),
        }
    }
}

/// Make a raster palette
fn make_palette(raster: &Raster<SRgb8>) -> Palette {
    let mut palette = Palette::new(256);
    palette.set_entry(SRgb8::default());
    for pixel in raster.pixels() {
        palette.set_entry(*pixel);
    }
    palette
}

/// Make a raster of a meter signal
fn make_meter_signal(state: MeterState) -> Raster<SRgb8> {
    let mut raster = Raster::<Rgba8p>::with_clear(48, 128);
    let circle = Raster::<Matte8>::with_u8_buffer(
        32,
        32,
        *include_bytes!("../static/circle.bin"),
    );
    raster.composite_matte((8, 8, 32, 32), &circle, (), state.red(), SrcOver);
    raster.composite_matte(
        (8, 48, 32, 32),
        &circle,
        (),
        state.yellow(),
        SrcOver,
    );
    raster.composite_matte(
        (8, 88, 32, 32),
        &circle,
        (),
        state.green(),
        SrcOver,
    );
    Raster::<SRgb8>::with_raster(&raster)
}

/// Make a GIF step for a meter state
fn make_step(state: MeterState, hold: u16) -> Step {
    let raster = make_meter_signal(state);
    let mut palette = make_palette(&raster);
    let indexed = palette.make_indexed(raster);
    Step::with_indexed(indexed, palette)
        .with_delay_time_cs(Some(hold))
        .with_disposal_method(DisposalMethod::Keep)
}

/// Encode a GIF of the meter `off` (flashing yellow)
fn encode_meter_off<W: Write>(enc: Encoder<W>) -> Result<()> {
    let mut enc = enc.into_step_enc().with_loop_count(0);
    enc.encode_step(&make_step(MeterState::Off, 50))?;
    enc.encode_step(&make_step(MeterState::LowYellow, 50))?;
    Ok(())
}

/// Encode a GIF of meter 1 (left) cycling
fn encode_meter_1<W: Write>(enc: Encoder<W>, red_cs: u16) -> Result<()> {
    let mut enc = enc.into_step_enc().with_loop_count(0);
    enc.encode_step(&make_step(MeterState::Green, 130))?;
    enc.encode_step(&make_step(MeterState::Yellow, 70))?;
    enc.encode_step(&make_step(MeterState::Red, red_cs))?;
    enc.encode_step(&make_step(MeterState::Red, 200 + red_cs))?;
    Ok(())
}

/// Encode a GIF of meter 2 (right) cycling
fn encode_meter_2<W: Write>(enc: Encoder<W>, red_cs: u16) -> Result<()> {
    let mut enc = enc.into_step_enc().with_loop_count(0);
    enc.encode_step(&make_step(MeterState::Red, 200 + red_cs))?;
    enc.encode_step(&make_step(MeterState::Green, 130))?;
    enc.encode_step(&make_step(MeterState::Yellow, 70))?;
    enc.encode_step(&make_step(MeterState::Red, red_cs))?;
    Ok(())
}

/// Make meter signal as html
fn meter_html(buf: Vec<u8>) -> String {
    let width = 24;
    let height = 64;
    let mut html = String::new();
    html.push_str("<img");
    html.push_str(" width='");
    html.push_str(&width.to_string());
    html.push_str("' height='");
    html.push_str(&height.to_string());
    html.push_str("' ");
    html.push_str("src='data:image/gif;base64,");
    b64enc.encode_string(buf, &mut html);
    html.push('\'');
    html.push_str("/>");
    html
}

impl RampMeter {
    /// Get fault, if any
    fn fault(&self) -> Option<&str> {
        self.status.as_ref().and_then(|s| s.fault.as_deref())
    }

    /// Get item states
    fn item_states<'a>(&'a self, anc: &'a RampMeterAnc) -> ItemStates<'a> {
        const LOCKED: &str = "locked";
        let mut states = anc.cio.item_states(self);
        match (&self.status, &self.lock) {
            (Some(st), None) => {
                if st.rate.is_some() {
                    states = states.with(ItemState::Planned, "metering");
                }
            }
            (Some(st), Some(lock)) => match (st.rate, lock.reason.as_str()) {
                (Some(_rt), "incident") => {
                    states = states.with(ItemState::Incident, LOCKED);
                }
                (Some(_rt), _) => {
                    states = states.with(ItemState::Deployed, LOCKED);
                }
                _ => {
                    states = states.with(ItemState::Available, LOCKED);
                }
            },
            _ => (),
        }
        if let Some(fault) = self.fault() {
            states = states.with(ItemState::Fault, fault);
        }
        states
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &RampMeterAnc) -> String {
        let name = HtmlStr::new(self.name());
        let item_states = self.item_states(anc);
        let location = HtmlStr::new(&self.location).with_len(32);
        format!(
            "<div class='title row'>{name} {item_states}</div>\
            <div class='info fill'>{location}</div>"
        )
    }

    /// Convert to Control HTML
    fn to_html_control(&self, anc: &RampMeterAnc) -> String {
        if let Some((lat, lon)) = anc.loc.latlon() {
            fly_map_item(&self.name, lat, lon);
        }
        let title = self.title(View::Control);
        let item_states = self.item_states(anc).to_html();
        let location = HtmlStr::new(&self.location).with_len(64);
        let mut rate = "".to_string();
        let mut queue = "".to_string();
        let mut meter1 = "".to_string();
        let mut meter2 = "".to_string();
        if let Some(s) = &self.status {
            if let Some(r) = s.rate {
                let c = 3_600.0 / (r as f32);
                rate = format!("{r} veh/hr ({c:.1} s)");
                let ds = (c * 10.0).round() as i32;
                // between 0.1 and 50.0 seconds
                if ds > 0 && ds < 500 {
                    let red_cs = ds as u16 * 10;
                    let mut buf = Vec::with_capacity(4096);
                    match encode_meter_1(Encoder::new(&mut buf), red_cs) {
                        Ok(()) => meter1 = meter_html(buf),
                        Err(e) => console::log_1(
                            &format!("encode_meter_1: {e:?}").into(),
                        ),
                    }
                    let mut buf = Vec::with_capacity(4096);
                    match encode_meter_2(Encoder::new(&mut buf), red_cs) {
                        Ok(()) => meter2 = meter_html(buf),
                        Err(e) => console::log_1(
                            &format!("encode_meter_2: {e:?}").into(),
                        ),
                    }
                }
            }
            if let Some(value) =
                s.queue.as_ref().and_then(|q| match q.as_str() {
                    "empty" => Some(8),
                    "exists" => Some(50),
                    "full" => Some(100),
                    _ => None,
                })
            {
                queue = format!(
                    "queue <meter min='0' optimum='0' low='25' \
                    high='75' max='100' value='{value}'></meter>"
                );
            }
        }
        if meter1.is_empty() || meter2.is_empty() {
            let mut buf = Vec::with_capacity(4096);
            match encode_meter_off(Encoder::new(&mut buf)) {
                Ok(()) => {
                    meter1 = meter_html(buf);
                    meter2 = meter1.clone();
                }
                Err(e) => {
                    console::log_1(&format!("encode_meter_off: {e:?}").into())
                }
            }
        }
        format!(
            "{title}\
            <div class='row fill'>\
              <span>{item_states}</span>\
            </div>\
            <div class='row'>\
              <span class='info'>{location}</span>\
            </div>\
            <div class='row center'>\
              {meter1}\
              <div class='column'>\
                <span>{rate}</span>\
                <span>{queue}</span>\
                <span>\
                  <button id='q_grow' type='button'>⏫ Grow</button>\
                  <button id='q_shrink' type='button'>⏬ Shrink</button>\
                </span>\
              </div>\
              {meter2}\
            </div>"
        )
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &RampMeterAnc) -> String {
        let title = self.title(View::Setup);
        let notes = HtmlStr::new(&self.notes);
        let controller = anc.cio.controller_html(self);
        let pin = anc.cio.pin_html(self.pin);
        let meter_types = anc.meter_types_html(self);
        let algorithms = anc.algorithms_html(self);
        let storage = OptVal(self.storage);
        let max_wait = OptVal(self.max_wait);
        let am_target = OptVal(self.am_target);
        let pm_target = OptVal(self.pm_target);
        let footer = self.footer(true);
        format!(
            "{title}\
            <div class='row'>\
              <label for='notes'>Notes</label>\
              <textarea id='notes' maxlength='255' rows='4' \
                        cols='24'>{notes}</textarea>\
            </div>\
            {controller}\
            {pin}\
            <div class='row'>\
              <label for='meter_type'>Meter Type</label>\
              {meter_types}\
            </div>\
            <div class='row'>\
              <label for='algorithm'>Algorithm</label>\
              {algorithms}\
            </div>\
            <div class='row'>\
              <label for='storage'>Storage (ft)</label>\
              <input id='storage' type='number' min='1' max='5000' \
                     size='8' value='{storage}'>\
            </div>\
            <div class='row'>\
              <label for='max_wait'>Max Wait (s)</label>\
              <input id='max_wait' type='number' min='1' max='600' \
                     size='8' value='{max_wait}'>\
            </div>\
            <div class='row'>\
              <label for='am_target'>AM Target</label>\
              <input id='am_target' type='number' min='0' max='2000' \
                     size='8' value='{am_target}'>\
            </div>\
            <div class='row'>\
              <label for='pm_target'>PM Target</label>\
              <input id='pm_target' type='number' min='0' max='2000' \
                     size='8' value='{pm_target}'>\
            </div>\
            {footer}"
        )
    }
}

impl ControllerIo for RampMeter {
    /// Get controller name
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Loc for RampMeter {
    /// Get geo location name
    fn geoloc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }
}

impl Card for RampMeter {
    type Ancillary = RampMeterAnc;

    /// Display name
    const DNAME: &'static str = "🚦 Ramp Meter";

    /// All item states as html options
    const ITEM_STATES: &'static str = "<option value=''>all ↴\
         <option value='🔹'>🔹 available\
         <option value='🗓️'>🗓️ planned\
         <option value='🚨'>🚨 incident\
         <option value='🔶'>🔶 deployed\
         <option value='⚠️'>⚠️ fault\
         <option value='🔌'>🔌 offline\
         <option value='▪️'>▪️ inactive";

    /// Get the resource
    fn res() -> Res {
        Res::RampMeter
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
        let item_states = self.item_states(anc);
        if item_states.is_match(ItemState::Inactive.code()) {
            ItemState::Inactive
        } else if item_states.is_match(ItemState::Offline.code()) {
            ItemState::Offline
        } else if item_states.is_match(ItemState::Planned.code()) {
            ItemState::Planned
        } else if item_states.is_match(ItemState::Incident.code())
            || item_states.is_match(ItemState::Deployed.code())
        {
            ItemState::Deployed
        } else {
            ItemState::Available
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &RampMeterAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || self
                .notes
                .as_ref()
                .is_some_and(|n| n.contains_lower(search))
            || self.item_states(anc).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &RampMeterAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Control => self.to_html_control(anc),
            View::Location => anc.loc.to_html_loc(self),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_text_area("notes", &self.notes);
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.changed_select("meter_type", self.meter_type);
        fields.changed_select("algorithm", self.algorithm);
        fields.changed_input("storage", self.storage);
        fields.changed_input("max_wait", self.max_wait);
        fields.changed_input("am_target", self.am_target);
        fields.changed_input("pm_target", self.pm_target);
        fields.into_value().to_string()
    }

    /// Get changed fields on Location view
    fn changed_location(&self, anc: RampMeterAnc) -> String {
        anc.loc.changed_location()
    }
}
