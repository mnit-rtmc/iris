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
use crate::card::{AncillaryData, Card, View, uri_one};
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::device::DeviceReq;
use crate::error::Result;
use crate::fetch::Action;
use crate::geoloc::{Loc, LocAnc};
use crate::item::{ItemState, ItemStates};
use crate::lock::LockReason;
use crate::start::fly_map_item;
use crate::util::{
    ContainsLower, Doc, Fields, Input, Select, TextArea, opt_ref, opt_str,
};
use base64::{Engine as _, engine::general_purpose::STANDARD_NO_PAD as b64enc};
use chrono::DateTime;
use gift::block::DisposalMethod;
use gift::{Encoder, Step};
use hatmil::{Page, html};
use pix::matte::Matte8;
use pix::ops::SrcOver;
use pix::rgb::{Rgba8p, SRgb8};
use pix::{Palette, Raster};
use resources::Res;
use serde::Deserialize;
use serde_json::Value;
use std::borrow::Cow;
use std::fmt;
use std::io::Write;
use wasm_bindgen::JsValue;
use web_sys::{HtmlSelectElement, console};

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
    /// Build meter types HTML
    fn meter_types_html<'p>(
        &self,
        pri: &RampMeter,
        select: &'p mut html::Select<'p>,
    ) {
        select.id("meter_type");
        for tp in &self.meter_types {
            let mut option = select.option();
            option.value(tp.id);
            if Some(tp.id) == pri.meter_type {
                option.selected();
            }
            option.cdata(&tp.description).close();
        }
        select.close();
    }

    /// Build metering algorithms HTML
    fn algorithms_html<'p>(
        &self,
        pri: &RampMeter,
        select: &'p mut html::Select<'p>,
    ) {
        select.id("algorithm");
        for alg in &self.algorithms {
            let mut option = select.option();
            option.value(alg.id);
            if Some(alg.id) == pri.algorithm {
                option.selected();
            }
            option.cdata(&alg.description).close();
        }
        select.close();
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
        *include_bytes!("../res/circle.bin"),
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

/// Build meter signal HTML
fn meter_html<'p>(buf: Vec<u8>, img: &'p mut html::Img<'p>) {
    const WIDTH: u32 = 24;
    const HEIGHT: u32 = 64;
    let mut src = "data:image/gif;base64,".to_owned();
    b64enc.encode_string(buf, &mut src);
    img.width(WIDTH).height(HEIGHT).src(&src);
}

impl fmt::Display for MeterLock {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        // format as JSON for setting meter lock
        write!(f, "{{\"reason\":\"{}\"", &self.reason)?;
        if let Some(rate) = self.rate {
            write!(f, ",\"rate\":{rate}")?;
        }
        if let Some(expires) = &self.expires {
            write!(f, ",\"expires\":\"{expires}\"")?;
        }
        if let Some(user_id) = &self.user_id {
            write!(f, ",\"user_id\":\"{user_id}\"")?;
        }
        write!(f, "}}")
    }
}

impl MeterLock {
    /// Create a new meter lock
    fn new(mut reason: LockReason, rate: Option<u32>, u: String) -> Self {
        // If turned on, reason must be incident/testing
        if rate.is_some() && reason.duration().is_none() {
            reason = LockReason::Testing;
        }
        let expires = rate.and(reason.make_expires());
        MeterLock {
            reason: reason.as_str().to_string(),
            rate,
            expires,
            user_id: Some(u),
        }
    }

    /// Encode into JSON Value
    fn json(&self) -> Value {
        let reason = LockReason::from(self.reason.as_str());
        match reason {
            LockReason::Unlocked => Value::Null,
            _ => Value::String(self.to_string()),
        }
    }

    /// Format lock expire time
    fn expires(&self) -> Option<String> {
        if let Some(expires) = &self.expires
            && let Ok(dt) = DateTime::parse_from_rfc3339(expires)
        {
            return Some(format!("⏲️ {}", &dt.format("%H:%M")));
        }
        None
    }
}

impl RampMeter {
    /// Get status rate
    fn status_rate(&self) -> Option<u32> {
        self.status.as_ref().and_then(|st| st.rate)
    }

    /// Get fault, if any
    fn fault(&self) -> Option<&str> {
        self.status.as_ref().and_then(|s| s.fault.as_deref())
    }

    /// Get item states
    fn item_states<'a>(&'a self, anc: &'a RampMeterAnc) -> ItemStates<'a> {
        let mut states = anc.cio.item_states(self);
        if states.contains(ItemState::Inactive) {
            return states;
        }
        if states.contains(ItemState::Available) {
            states = self.item_states_lock();
        }
        if let Some(fault) = self.fault() {
            states = states.with(ItemState::Fault, fault);
        }
        states
    }

    /// Get lock reason
    fn lock_reason(&self) -> LockReason {
        self.lock
            .as_ref()
            .map(|lk| LockReason::from(lk.reason.as_str()))
            .unwrap_or(LockReason::Unlocked)
    }

    /// Get lock rate
    fn lock_rate(&self) -> Option<u32> {
        self.lock.as_ref().and_then(|lk| lk.rate)
    }

    /// Make action to lock the meter
    fn make_lock_action(
        &self,
        reason: LockReason,
        rate: Option<u32>,
    ) -> Vec<Action> {
        let mut actions = Vec::with_capacity(1);
        if let Some(user) = crate::app::user() {
            let uri = uri_one(Res::RampMeter, &self.name);
            let lock = MeterLock::new(reason, rate, user).json();
            let val = format!("{{\"lock\":{lock}}}");
            actions.push(Action::Patch(uri, val.into()));
        }
        actions
    }

    /// Is shrinking queue allowed?
    fn is_shrink_allowed(&self) -> bool {
        self.lock_reason().is_deployable()
            && (self.lock_rate().is_some() || self.status_rate().is_some())
    }

    /// Is growing queue allowed?
    fn is_grow_allowed(&self) -> bool {
        self.lock_reason().is_deployable()
    }

    /// Make lock shrink action
    fn lock_shrink(&self) -> Vec<Action> {
        if let Some(rate) = self.lock_rate().or(self.status_rate()) {
            // FIXME: use system attributes
            let rt = (rate + 50).min(1714);
            if rt != rate {
                let reason = self.lock_reason();
                return self.make_lock_action(reason, Some(rt));
            }
        }
        Vec::new()
    }

    /// Make lock grow action
    fn lock_grow(&self) -> Vec<Action> {
        // FIXME: use system attributes
        let rate = self.lock_rate().or(self.status_rate()).unwrap_or(1714);
        let rt = (rate - 50).max(240);
        if rt != rate {
            let reason = self.lock_reason();
            self.make_lock_action(reason, Some(rt))
        } else {
            Vec::new()
        }
    }

    /// Create action to handle click on a device request button
    #[allow(clippy::vec_init_then_push)]
    fn device_req(&self, req: DeviceReq) -> Vec<Action> {
        let uri = uri_one(Res::RampMeter, &self.name);
        let mut actions = Vec::with_capacity(1);
        actions.push(Action::Patch(uri, req.to_string().into()));
        actions
    }

    /// Get item states from status/lock
    fn item_states_lock(&self) -> ItemStates<'_> {
        let deployed = self.status_rate().is_some();
        let reason = self.lock_reason();
        let states = if reason == LockReason::Incident {
            ItemStates::default()
                .with(ItemState::Incident, LockReason::Incident.as_str())
        } else {
            ItemStates::default()
        };
        match (deployed, reason) {
            (true, LockReason::Unlocked) => states
                .with(ItemState::Deployed, "metering")
                .with(ItemState::Planned, "metering"),
            (true, _) => states
                .with(ItemState::Deployed, "metering")
                .with(ItemState::Locked, reason.as_str()),
            (false, LockReason::Unlocked) => ItemState::Available.into(),
            (false, _) => states.with(ItemState::Locked, reason.as_str()),
        }
    }

    /// Build meter image HTML
    fn meter_image_html<'p>(&self, num: u32, img: &'p mut html::Img<'p>) {
        if let Some(r) = self.status_rate() {
            let c = 3_600.0 / (r as f32);
            let ds = (c * 10.0).round() as i32;
            // between 0.1 and 50.0 seconds
            if ds > 0 && ds < 500 {
                let red_cs = ds as u16 * 10;
                let mut buf = Vec::with_capacity(4096);
                let res = if num == 1 {
                    encode_meter_1(Encoder::new(&mut buf), red_cs)
                } else {
                    encode_meter_2(Encoder::new(&mut buf), red_cs)
                };
                match res {
                    Ok(()) => {
                        meter_html(buf, img);
                        return;
                    }
                    Err(e) => {
                        console::log_1(&format!("encode_meter: {e:?}").into())
                    }
                }
            }
        }
        let mut buf = Vec::with_capacity(4096);
        match encode_meter_off(Encoder::new(&mut buf)) {
            Ok(()) => meter_html(buf, img),
            Err(e) => {
                console::log_1(&format!("encode_meter_off: {e:?}").into())
            }
        }
    }

    /// Build metering rate HTML
    fn rate_html<'p>(&self, span: &'p mut html::Span<'p>) {
        match self.status_rate() {
            Some(r) => {
                let c = 3_600.0 / (r as f32);
                span.cdata(format!("⏱️ {c:.1} s ({r} veh/hr)"));
            }
            None => {
                span.class("hidden").cdata("⏱️ 0.0 s (N/A veh/hr)");
            }
        }
        span.close();
    }

    /// Build lock reason HTML
    fn lock_reason_html<'p>(&self, span: &'p mut html::Span<'p>) {
        let reason = self.lock_reason();
        span.cdata(match reason {
            LockReason::Unlocked => "🔓",
            _ => "🔒",
        });
        let mut select = span.select();
        select.id("lk_reason");
        for r in LockReason::all_meter() {
            let mut option = select.option();
            if *r == reason {
                option.selected();
            }
            option.cdata(r.as_str()).close();
        }
        select.close();
        span.close();
    }

    /// Build shrink/grow buttons HTML
    fn shrink_grow_html<'p>(&self, span: &'p mut html::Span<'p>) {
        let mut button = span.button();
        button.id("lk_shrink").r#type("button");
        if !self.is_shrink_allowed() {
            button.disabled();
        }
        button.cdata("Shrink ↩").close();
        button = span.button();
        button.id("lk_grow").r#type("button");
        if !self.is_grow_allowed() {
            button.disabled();
        }
        button.cdata("Grow ↪").close();
        span.close();
    }

    /// Build queue HTML
    fn queue_html<'p>(&self, span: &'p mut html::Span<'p>) {
        let value = self.status.as_ref().and_then(|s| {
            s.queue.as_ref().and_then(|q| match q.as_str() {
                "empty" => Some(8),
                "exists" => Some(50),
                "full" => Some(100),
                _ => None,
            })
        });
        if value.is_none() {
            span.class("hidden");
        }
        span.cdata("🚗 queue ");
        let value = value.unwrap_or(0);
        span.meter()
            .min(0)
            .optimum(0)
            .low(25)
            .high(75)
            .max(100)
            .value(value)
            .close();
        span.close();
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &RampMeterAnc) -> String {
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(self.item_states(anc).to_string())
            .close();
        div = page.frag::<html::Div>();
        div.class("info fill")
            .cdata_len(opt_ref(&self.location), 32);
        String::from(page)
    }

    /// Convert to Control HTML
    fn to_html_control(&self, anc: &RampMeterAnc) -> String {
        if let Some((lat, lon)) = anc.loc.latlon() {
            fly_map_item(&self.name, lat, lon);
        }
        let mut page = Page::new();
        self.title(View::Control, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row fill");
        self.item_states(anc).tooltips(&mut div.span());
        let mut span = div.span();
        if let Some(lock) = &self.lock
            && let Some(expires) = lock.expires()
        {
            span.cdata(expires);
        }
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.span()
            .class("info")
            .cdata_len(opt_ref(&self.location), 64)
            .close();
        div.close();
        div = page.frag::<html::Div>();
        div.class("row center");
        self.meter_image_html(1, &mut div.img());
        let mut div2 = div.div();
        div2.class("column");
        self.rate_html(&mut div2.span());
        self.lock_reason_html(&mut div2.span());
        self.shrink_grow_html(&mut div2.span());
        self.queue_html(&mut div2.span());
        div2.close();
        self.meter_image_html(2, &mut div.img());
        String::from(page)
    }

    /// Convert to Request HTML
    fn to_html_request(&self, _anc: &RampMeterAnc) -> String {
        let work = "http://example.com"; // FIXME
        let mut page = Page::new();
        self.title(View::Request, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.span().cdata("Settings").close();
        div.button()
            .id("rq_settings")
            .r#type("button")
            .cdata("Send")
            .close();
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.span().cdata("Work Request").close();
        div.a()
            .href(work)
            .target("_blank")
            .rel("noopener noreferrer")
            .cdata("🔗 ")
            .cdata(self.name());
        String::from(page)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &RampMeterAnc) -> String {
        let mut page = Page::new();
        self.title(View::Setup, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("notes").cdata("Notes").close();
        div.textarea()
            .id("notes")
            .maxlength(255)
            .rows(4)
            .cols(24)
            .cdata(opt_ref(&self.notes))
            .close();
        div.close();
        anc.cio.controller_html(self, &mut page.frag::<html::Div>());
        anc.cio.pin_html(self.pin, &mut page.frag::<html::Div>());
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("meter_type").cdata("Type").close();
        anc.meter_types_html(self, &mut div.select());
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("algorithm").cdata("Algorithm").close();
        anc.algorithms_html(self, &mut div.select());
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("storage").cdata("Storage (ft)").close();
        div.input()
            .id("storage")
            .r#type("number")
            .min(1)
            .max(5000)
            .size(8)
            .value(opt_str(self.storage));
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("max_wait").cdata("Max Wait (s)").close();
        div.input()
            .id("max_wait")
            .r#type("number")
            .min(1)
            .max(600)
            .size(8)
            .value(opt_str(self.max_wait));
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("am_target").cdata("AM Target").close();
        div.input()
            .id("am_target")
            .r#type("number")
            .min(0)
            .max(2000)
            .size(8)
            .value(opt_str(self.am_target));
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("pm_target").cdata("PM Target").close();
        div.input()
            .id("pm_target")
            .r#type("number")
            .min(0)
            .max(2000)
            .size(8)
            .value(opt_str(self.pm_target));
        div.close();
        self.footer_html(true, &mut page.frag::<html::Div>());
        String::from(page)
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

    /// Get the resource
    fn res() -> Res {
        Res::RampMeter
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[
            ItemState::Available,
            ItemState::Deployed,
            ItemState::Planned,
            ItemState::Incident,
            ItemState::Locked,
            ItemState::Fault,
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
        let states = self.item_states(anc);
        if states.contains(ItemState::Inactive) {
            ItemState::Inactive
        } else if states.contains(ItemState::Offline) {
            ItemState::Offline
        } else if states.contains(ItemState::Deployed) {
            ItemState::Deployed
        } else if states.contains(ItemState::Planned) {
            ItemState::Planned
        } else if states.contains(ItemState::Locked) {
            ItemState::Locked
        } else {
            ItemState::Available
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &RampMeterAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || self.notes.contains_lower(search)
            || self.item_states(anc).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &RampMeterAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Control => self.to_html_control(anc),
            View::Location => anc.loc.to_html_loc(self),
            View::Request => self.to_html_request(anc),
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

    /// Handle click event for a button on the card
    fn handle_click(&self, _anc: RampMeterAnc, id: String) -> Vec<Action> {
        match id.as_str() {
            "lk_shrink" => self.lock_shrink(),
            "lk_grow" => self.lock_grow(),
            "rq_settings" => self.device_req(DeviceReq::SendSettings),
            _ => Vec::new(),
        }
    }

    /// Handle input event for an element on the card
    fn handle_input(&self, _anc: RampMeterAnc, id: String) -> Vec<Action> {
        if &id == "lk_reason" {
            let r = Doc::get().elem::<HtmlSelectElement>("lk_reason").value();
            let reason = LockReason::from(&r[..]);
            let rate = if reason.duration().is_some() {
                self.lock_rate().or(self.status_rate()).or(Some(1714))
            } else {
                None
            };
            return self.make_lock_action(reason, rate);
        }
        Vec::new()
    }
}
