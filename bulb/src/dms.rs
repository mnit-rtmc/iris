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
use crate::actionplan::{ActionPlan, DeviceAction};
use crate::asset::Asset;
use crate::card::{AncillaryData, Card, View, uri_one};
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::device::DeviceReq;
use crate::error::Result;
use crate::fetch::{Action, Uri};
use crate::geoloc::{Loc, LocAnc};
use crate::item::{ItemState, ItemStates};
use crate::lock::LockReason;
use crate::msgpattern::{FontName, GraphicName, MsgLine, MsgPattern};
use crate::notes::contains_hashtag;
use crate::rend::Renderer;
use crate::rle::Table;
use crate::signmessage::SignMessage;
use crate::start::fly_map_item;
use crate::util::{ContainsLower, Doc, Fields, Input, TextArea, opt_ref};
use crate::word::Word;
use chrono::{DateTime, Local, format::SecondsFormat};
use hatmil::{Page, html};
use js_sys::{ArrayBuffer, Uint8Array};
use mag::temp::DegC;
use ntcip::dms::multi::{join_text, normalize as multi_normalize};
use ntcip::dms::{Font, FontTable, GraphicTable, MessagePattern, tfon};
use rendzina::{SignConfig, load_graphic};
use resources::Res;
use serde::Deserialize;
use serde_json::Value;
use std::borrow::Cow;
use std::collections::BTreeSet;
use std::fmt;
use std::iter::repeat;
use std::time::Duration;
use wasm_bindgen::{JsCast, JsValue};
use web_sys::{HtmlElement, HtmlInputElement, HtmlSelectElement};

/// NTCIP sign
type NtcipDms = ntcip::dms::Dms<256, 24, 32>;

/// Display Units
type TempUnit = mag::temp::DegF;

/// Photocell status
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Photocell {
    description: String,
    reading: String,
}

/// Power supply status
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct PowerSupply {
    description: String,
    supply_type: String,
    voltage: String,
}

/// DMS Lock
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct DmsLock {
    pub reason: String,
    pub multi: Option<String>,
    pub incident: Option<String>,
    pub expires: Option<String>,
    pub user_id: Option<String>,
    pub flash_beacon: Option<bool>,
    pub pixel_service: Option<bool>,
}

impl fmt::Display for DmsLock {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        // format as JSON for setting DMS lock
        write!(f, "{{\"reason\":\"{}\"", &self.reason)?;
        if let Some(multi) = &self.multi {
            write!(f, ",\"multi\":\"{multi}\"")?;
        }
        if let Some(expires) = &self.expires {
            write!(f, ",\"expires\":\"{expires}\"")?;
        }
        if let Some(user_id) = &self.user_id {
            write!(f, ",\"user_id\":\"{user_id}\"")?;
        }
        if let Some(true) = self.flash_beacon {
            write!(f, ",\"flash_beacon\":true")?;
        }
        if let Some(true) = self.pixel_service {
            write!(f, ",\"pixel_service\":true")?;
        }
        write!(f, "}}")
    }
}

impl DmsLock {
    /// Create a new DMS lock
    fn new(reason: &str) -> Self {
        DmsLock {
            reason: reason.to_string(),
            ..Default::default()
        }
    }

    /// Set the MULTI text
    fn with_multi(mut self, multi: Option<&str>) -> Self {
        self.multi = multi.map(|m| m.to_string());
        self
    }

    /// Set the lock duration
    fn with_duration(mut self, duration: Option<u32>) -> Self {
        self.expires = duration.map(|d| {
            let sec = Duration::from_secs(60 * u64::from(d));
            let now: DateTime<Local> = Local::now();
            (now + sec).to_rfc3339_opts(SecondsFormat::Secs, false)
        });
        self
    }

    /// Set the user name
    fn with_user(mut self, user: Option<&str>) -> Self {
        self.user_id = user.map(|u| u.to_string());
        self
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

    /// Encode into JSON Value
    fn json(&self) -> Value {
        let reason = LockReason::from(self.reason.as_str());
        match reason {
            LockReason::Unlocked => Value::Null,
            _ => Value::String(self.to_string()),
        }
    }
}

/// Sign status
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct SignStatus {
    faults: Option<String>,
    ambient_temps: Option<Vec<i32>>,
    housing_temps: Option<Vec<i32>>,
    cabinet_temps: Option<Vec<i32>>,
    light_output: Option<u32>,
    photocells: Option<Vec<Photocell>>,
    power_supplies: Option<Vec<PowerSupply>>,
    ldc_pot_base: Option<i32>,
    pixel_current_low: Option<i32>,
    pixel_current_high: Option<i32>,
}

/// Dms
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Dms {
    pub name: String,
    pub location: Option<String>,
    pub controller: Option<String>,
    pub notes: Option<String>,
    pub msg_current: Option<String>,
    pub lock: Option<DmsLock>,
    pub has_faults: Option<bool>,
    // secondary attributes
    pub pin: Option<u32>,
    pub static_graphic: Option<String>,
    pub preset: Option<String>,
    pub sign_config: Option<String>,
    pub sign_detail: Option<String>,
    pub msg_sched: Option<String>,
    pub geo_loc: Option<String>,
    pub status: Option<SignStatus>,
    pub pixel_failures: Option<String>,
}

/// DMS ancillary data
#[derive(Default)]
pub struct DmsAnc {
    cio: ControllerIoAnc<Dms>,
    loc: LocAnc<Dms>,
    messages: Vec<SignMessage>,
    configs: Vec<SignConfig>,
    compose_patterns: Vec<MsgPattern>,
    lines: Vec<MsgLine>,
    words: Vec<Word>,
    fonts: FontTable<256, 24>,
    graphics: GraphicTable<32>,
    device_actions: Vec<DeviceAction>,
    action_plans: Vec<ActionPlan>,
}

impl AncillaryData for DmsAnc {
    type Primary = Dms;

    /// Construct ancillary DMS data
    fn new(pri: &Dms, view: View) -> Self {
        let mut cio = ControllerIoAnc::new(pri, view);
        if let View::Compact
        | View::Control
        | View::Hidden
        | View::Search
        | View::Status = view
        {
            cio.assets.push(Asset::SignMessages);
        }
        if let View::Control = view {
            cio.assets.push(Asset::SignConfigs);
            cio.assets.push(Asset::MsgPatterns);
            cio.assets.push(Asset::Words);
            cio.assets.push(Asset::Fonts);
            cio.assets.push(Asset::Graphics);
            cio.assets.push(Asset::DeviceActions);
        }
        if let View::Setup = view {
            cio.assets.push(Asset::SignConfigs);
        }
        let loc = LocAnc::new(pri, view);
        DmsAnc {
            cio,
            loc,
            ..Default::default()
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.cio.assets.pop().or_else(|| self.loc.assets.pop())
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &Dms,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::Controllers => {
                self.cio.set_asset(pri, asset, value)?;
            }
            Asset::SignMessages => {
                self.messages = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::SignConfigs => {
                self.configs = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::MsgPatterns => {
                let mut patterns: Vec<MsgPattern> =
                    serde_wasm_bindgen::from_value(value)?;
                patterns.retain(|p| {
                    p.compose_hashtag
                        .as_ref()
                        .is_some_and(|h| pri.has_hashtag(h))
                });
                patterns.sort();
                self.compose_patterns = patterns;
                // now that we have the patterns, let's fetch the lines
                self.cio.assets.push(Asset::MsgLines);
            }
            Asset::MsgLines => {
                let mut lines: Vec<MsgLine> =
                    serde_wasm_bindgen::from_value(value)?;
                // NOTE: patterns *must* be populated before this!
                lines.retain(|ln| {
                    self.has_compose_pattern(&ln.msg_pattern)
                        && (ln.restrict_hashtag.is_none()
                            || ln
                                .restrict_hashtag
                                .as_ref()
                                .is_some_and(|h| pri.has_hashtag(h)))
                });
                self.lines = lines;
            }
            Asset::Words => {
                self.words = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::Fonts => {
                let fnames: Vec<FontName> =
                    serde_wasm_bindgen::from_value(value)?;
                for fname in fnames {
                    self.cio.assets.push(Asset::Font(fname.name));
                }
            }
            Asset::Font(_nm) => {
                let font: String = serde_wasm_bindgen::from_value(value)?;
                let font = tfon::read(font.as_bytes())?;
                if let Some(f) = self.fonts.font_mut(font.number) {
                    *f = font;
                } else if let Some(f) = self.fonts.font_mut(0) {
                    *f = font;
                }
            }
            Asset::Graphics => {
                let gnames: Vec<GraphicName> =
                    serde_wasm_bindgen::from_value(value)?;
                for gname in gnames {
                    self.cio.assets.push(Asset::Graphic(gname.name));
                }
            }
            Asset::Graphic(nm) => {
                if let Ok(number) = nm
                    .as_str()
                    .replace(|c: char| !c.is_numeric(), "")
                    .parse::<u8>()
                {
                    let abuf = value.dyn_into::<ArrayBuffer>().unwrap();
                    let graphic = Uint8Array::new(&abuf).to_vec();
                    let graphic = load_graphic(&graphic[..], number)?;
                    if let Some(g) = self.graphics.graphic_mut(number) {
                        *g = graphic;
                    } else if let Some(g) = self.graphics.graphic_mut(0) {
                        *g = graphic;
                    }
                } else {
                    log::warn!("invalid graphic: {nm}");
                }
            }
            Asset::DeviceActions => {
                let mut act: Vec<DeviceAction> =
                    serde_wasm_bindgen::from_value(value)?;
                act.retain(|da| pri.has_hashtag(&da.hashtag));
                self.device_actions = act;
                // now that we have device actions, let's fetch the plans
                self.cio.assets.push(Asset::ActionPlans);
            }
            Asset::ActionPlans => {
                let mut plans: Vec<ActionPlan> =
                    serde_wasm_bindgen::from_value(value)?;
                plans.retain(|p| {
                    p.active
                        && self
                            .device_actions
                            .iter()
                            .any(|da| da.action_plan == p.name)
                });
                self.action_plans = plans;
            }
            _ => self.loc.set_asset(pri, asset, value)?,
        }
        Ok(())
    }
}

impl DmsAnc {
    /// Get a sign message by name
    fn sign_message(&self, msg: Option<&str>) -> Option<&SignMessage> {
        msg.and_then(|msg| self.messages.iter().find(|m| m.name == msg))
    }

    /// Get message item states
    fn msg_states(&self, msg: Option<&str>) -> ItemStates<'_> {
        self.sign_message(msg).map(|m| m.item_states()).unwrap_or(
            ItemStates::default().with(ItemState::Fault, "message unknown"),
        )
    }

    /// Find a sign config
    fn sign_config(&self, cfg: Option<&str>) -> Option<&SignConfig> {
        cfg.and_then(|cfg| self.configs.iter().find(|c| c.name == cfg))
    }

    /// Check for compose pattern
    fn has_compose_pattern(&self, pat: &str) -> bool {
        self.compose_patterns.iter().any(|p| p.name == pat)
    }

    /// Make line select elements
    fn make_lines_html<'p>(
        &self,
        dms: &NtcipDms,
        pat_def: &MsgPattern,
        ms_cur: &str,
        div: &'p mut html::Div<'p>,
    ) {
        // NOTE: this prevents lifetime from escaping
        let mut pat = pat_def;
        if self.pat_lines(pat).count() == 0 {
            let n_lines = MessagePattern::new(dms, &pat.multi).widths().count();
            match self.find_substitute(pat, n_lines) {
                Some(sub) => pat = sub,
                None => return,
            }
        }
        let widths = MessagePattern::new(dms, &pat_def.multi).widths();
        let cur_lines = MessagePattern::new(dms, &pat_def.multi)
            .lines(ms_cur)
            .chain(repeat(""));
        div.id("mc_lines").class("column");
        let mut rect_num = 0;
        for (i, ((width, font_num, rn), cur_line)) in
            widths.zip(cur_lines).enumerate()
        {
            let ln = 1 + i as u16;
            let mc_line = format!("mc_line{ln}");
            let mc_choice = format!("mc_choice{ln}");
            // NOTE: these labels are a workaround for a Firefox 147 bug:
            //       if "Save and autofill addresses" is enabled,
            //       setting onfocus on a second consecutive input !?!
            //       triggers autocomplete with saved street addresses
            div.label().hidden("hidden").close();
            let mut input = div.input();
            if rn != rect_num {
                input.class("mc_line_gap");
                rect_num = rn;
            }
            input
                .id(mc_line)
                .value(cur_line)
                .list(&mc_choice)
                .onfocus("this.value=''");
            let mut datalist = div.datalist();
            datalist.id(mc_choice);
            if let Some(font) = dms.font_definition().font(font_num) {
                for ml in self.pat_lines(pat) {
                    if ml.line == ln
                        && let Some(ms) =
                            self.line_multi(&ml.multi, width, font)
                    {
                        let mut option = datalist.option();
                        option.value(&ms).cdata(join_text(&ms, " ")).close();
                    }
                }
            }
            datalist.close();
        }
        div.close();
    }

    /// Find a substitute message pattern
    fn find_substitute(
        &self,
        pat: &MsgPattern,
        n_lines: usize,
    ) -> Option<&MsgPattern> {
        self.compose_patterns
            .iter()
            .find(|&mp| mp != pat && self.max_line(mp) == n_lines)
    }

    /// Get max line number of a pattern
    fn max_line(&self, pat: &MsgPattern) -> usize {
        self.pat_lines(pat)
            .map(|ml| usize::from(ml.line))
            .max()
            .unwrap_or_default()
    }

    /// Get iterator of lines in a message pattern
    fn pat_lines<'a>(
        &'a self,
        pat: &'a MsgPattern,
    ) -> impl Iterator<Item = &'a MsgLine> {
        self.lines.iter().filter(|ml| ml.msg_pattern == pat.name)
    }

    /// Get line that fits on sign
    fn line_multi(
        &self,
        multi: &str,
        width: u16,
        font: &Font,
    ) -> Option<String> {
        // FIXME: handle line-allowed MULTI tags
        let mut line = String::from(multi);
        let mut ms = &line[..];
        loop {
            let Ok(w) = font.text_width(ms, None) else {
                break;
            };
            if w <= width {
                return Some(line);
            } else if let Some(abbrev) = self.abbreviate_text(ms) {
                line = abbrev;
                ms = &line[..];
            } else {
                break;
            }
        }
        None
    }

    /// Abbreviate message text
    fn abbreviate_text(&self, text: &str) -> Option<String> {
        let mut abbrev = Word::default();
        for w in text.split(' ') {
            let sc = w.len();
            // prefer to abbreviate longer words
            if sc > abbrev.name.len() {
                for word in &self.words {
                    if word.allowed
                        && word.name == w
                        && let Some(abbr) = &word.abbr
                        && !abbr.is_empty()
                        && abbr != w
                    {
                        abbrev = word.clone();
                    }
                }
            }
        }
        if !abbrev.name.is_empty() {
            let mut t = String::new();
            for w in text.split(' ') {
                if w == abbrev.name {
                    t.push_str(abbrev.abbr.as_ref().unwrap());
                } else {
                    t.push_str(w);
                }
                t.push(' ');
            }
            t.truncate(t.len() - 1);
            Some(t)
        } else {
            None
        }
    }

    /// Create action to lock a sign
    fn lock_action(
        self,
        uri: Uri,
        multi: &str,
        duration: Option<u32>,
    ) -> Vec<Action> {
        let mut actions = Vec::with_capacity(1);
        let Some(user) = crate::app::user() else {
            return actions;
        };
        let lock = DmsLock::new(LockReason::Situation.as_str())
            .with_multi(Some(multi))
            .with_duration(duration)
            .with_user(Some(&user))
            .json();
        let val = format!("{{\"lock\":{lock}}}");
        actions.push(Action::Patch(uri, val.into()));
        actions
    }

    /// Create actions to blank a sign message
    fn blank_actions(self, uri: Uri) -> Vec<Action> {
        let mut actions = Vec::with_capacity(1);
        let val = format!("{{\"lock\":{}}}", Value::Null);
        actions.push(Action::Patch(uri, val.into()));
        actions
    }

    /// Get action plan phases
    fn phases<'a>(
        &'a self,
        plan: &'a ActionPlan,
    ) -> impl Iterator<Item = &'a str> {
        let mut phases = BTreeSet::new();
        phases.insert(&plan.default_phase[..]);
        for da in &self.device_actions {
            if da.action_plan == plan.name {
                phases.insert(&da.phase[..]);
            }
        }
        phases.into_iter()
    }
}

/// All hashtags for dedicated purpose
const DEDICATED: &[&str] = &[
    "#LaneUse",
    "#Parking",
    "#Tolling",
    "#TravelTime",
    "#Wayfinding",
    "#Safety",
    "#Vsl",
    "#Hidden",
];

impl Dms {
    /// Get multi of current message
    fn current_multi<'a>(&'a self, anc: &'a DmsAnc) -> &'a str {
        anc.sign_message(self.msg_current.as_deref())
            .map(|m| &m.multi[..])
            .unwrap_or("")
    }

    /// Get user of current message
    fn current_user<'a>(&'a self, anc: &'a DmsAnc) -> &'a str {
        anc.sign_message(self.msg_current.as_deref())
            .map(|m| m.user())
            .unwrap_or("")
    }

    /// Get name of current message .gif
    fn msg_current_gif(&self) -> Option<String> {
        self.msg_current
            .as_ref()
            .map(|msg| format!("/iris/img/{msg}.gif"))
    }

    /// Check if DMS has a given hashtag
    fn has_hashtag(&self, hashtag: &str) -> bool {
        match &self.notes {
            Some(notes) => contains_hashtag(notes, hashtag),
            None => false,
        }
    }

    /// Get one dedicated hashtag, if defined
    fn dedicated(&self) -> Option<&'static str> {
        DEDICATED.iter().find(|tag| self.has_hashtag(tag)).copied()
    }

    /// Get faults, if any
    fn faults(&self) -> Option<&str> {
        if let Some(true) = self.has_faults {
            if let Some(status) = &self.status
                && let Some(faults) = &status.faults
            {
                return Some(faults);
            }
            // secondary attribute doesn't match primary has_faults?!
            Some("has_faults")
        } else {
            None
        }
    }

    /// Get item states
    fn item_states<'a>(&'a self, anc: &'a DmsAnc) -> ItemStates<'a> {
        let mut states = anc.cio.item_states(self);
        if states.contains(ItemState::Inactive) {
            return states;
        }
        if states.contains(ItemState::Available) {
            states = anc.msg_states(self.msg_current.as_deref());
        }
        if let Some(dedicated) = self.dedicated() {
            states = states.with(ItemState::Dedicated, dedicated);
        }
        if let Some(faults) = self.faults() {
            states = states.with(ItemState::Fault, faults);
        }
        states
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &DmsAnc) -> String {
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.class("title row");
        div.span()
            .cdata(self.name())
            .cdata(" ")
            .cdata(self.item_states(anc).to_string())
            .close();
        div.span().class("info").cdata(self.user(anc)).close();
        div.close();
        if let Some(gif) = self.msg_current_gif() {
            let multi = self.current_multi(anc);
            let mut rend = Renderer::new()
                .with_class("sign_message")
                .with_gif(&gif)
                .with_max_width(240)
                .with_max_height(80);
            rend.render_multi(multi, &mut page.frag::<html::Img>());
        }
        div = page.frag::<html::Div>();
        div.class("info fill");
        div.cdata_len(opt_ref(&self.location), 64);
        String::from(page)
    }

    /// Get user to display
    fn user(&self, anc: &DmsAnc) -> String {
        let user = self.current_user(anc);
        match crate::app::user() {
            Some(u) if u == user => format!("👤 {user}"),
            _ => {
                if user != "AUTO" {
                    user.to_string()
                } else {
                    "".to_string()
                }
            }
        }
    }

    /// Convert to Control HTML
    fn to_html_control(&self, anc: &DmsAnc) -> String {
        if let Some((lat, lon)) = anc.loc.latlon() {
            fly_map_item(&self.name, lat, lon);
        }
        let mut page = Page::new();
        self.title(View::Control, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row fill");
        self.item_states(anc).tooltips(&mut div.span());
        if let Some(lock) = &self.lock
            && let Some(expires) = lock.expires()
        {
            div.span().cdata(expires);
        }
        div.close();
        div = page.frag::<html::Div>();
        div.id("sign_msg");
        let mut rend = Renderer::new()
            .with_class("sign_message")
            .with_max_width(450)
            .with_max_height(100);
        let gif = self.msg_current_gif();
        let dms = self.make_dms(anc);
        if let Some(gif) = &gif {
            rend = rend.with_gif(gif);
        }
        if let Some(dms) = &dms {
            rend = rend.with_dms(dms)
        }
        rend.render_multi(self.current_multi(anc), &mut div.img());
        if let Some(pix) = self.failed_pixels(anc) {
            rend.render_pixels(&pix[..], &mut div.img());
        }
        div.close();
        div = page.frag::<html::Div>();
        div.class("info fill")
            .cdata_len(opt_ref(&self.location), 64)
            .close();
        self.message_composer_html(anc, &mut page.frag::<html::Div>());
        self.action_plans_html(anc, &mut page.frag::<html::Details>());
        String::from(page)
    }

    /// Build message composer HTML
    fn message_composer_html<'p>(
        &self,
        anc: &DmsAnc,
        div: &'p mut html::Div<'p>,
    ) {
        if anc.compose_patterns.is_empty() {
            log::warn!("{}: No compose patterns", self.name);
            return;
        }
        let Some(dms) = self.make_dms(anc) else {
            return;
        };
        let pat_def = self.pattern_default(anc);
        let multi = pat_def.map(|pat| &pat.multi[..]).unwrap_or("");
        div.id("mc_grid");
        let mut rend = Renderer::new()
            .with_dms(&dms)
            .with_id("mc_pixels")
            .with_class("preview")
            .with_max_width(240)
            .with_max_height(80);
        if let Some(pix) = self.failed_pixels(anc) {
            rend.render_pixels(&pix[..], &mut div.img());
        }
        // now, render the MULTI on top
        rend = rend.with_id("mc_preview");
        rend.render_multi(multi, &mut div.img());
        let mut select = div.select();
        select.id("mc_pattern");
        for pat in &anc.compose_patterns {
            let mut option = select.option();
            if let Some(p) = pat_def
                && p.name == pat.name
            {
                option.selected();
            }
            option.cdata(&pat.name).close();
        }
        select.close();
        if let Some(pat) = pat_def {
            anc.make_lines_html(
                &dms,
                pat,
                self.current_multi(anc),
                &mut div.div(),
            );
        }
        make_expire_select(&mut div.select());
        div.button()
            .id("mc_send")
            .r#type("button")
            .cdata("Send")
            .close();
        div.button()
            .id("mc_blank")
            .r#type("button")
            .cdata("Blank")
            .close();
        div.close();
    }

    /// Decode failed pixels
    fn failed_pixels(&self, anc: &DmsAnc) -> Option<Vec<u32>> {
        let pf = self.pixel_failures.as_deref()?;
        let cfg = anc.sign_config(self.sign_config.as_deref())?;
        let rle = Table::new(String::from(pf));
        let pix: Vec<_> = rle.iter().collect();
        if pix.len() == (cfg.pixel_width * cfg.pixel_height) as usize
            && pix.iter().any(|p| *p != 0)
        {
            Some(pix)
        } else {
            None
        }
    }

    /// Get the pattern which should be selected by default
    fn pattern_default<'a>(&self, anc: &'a DmsAnc) -> Option<&'a MsgPattern> {
        let multi = self.current_multi(anc);
        let mut best: Option<&MsgPattern> = None;
        for pat in &anc.compose_patterns {
            if pat.multi == multi {
                return Some(pat);
            }
            if best.is_none() {
                best = Some(pat);
            }
        }
        best
    }

    /// Make an NTCIP sign
    fn make_dms(&self, anc: &DmsAnc) -> Option<NtcipDms> {
        let cfg = anc.sign_config(self.sign_config.as_deref())?;
        NtcipDms::builder()
            .with_font_definition(anc.fonts.clone())
            .with_graphic_definition(anc.graphics.clone())
            .with_sign_cfg(cfg.sign_cfg())
            .with_vms_cfg(cfg.vms_cfg())
            .with_multi_cfg(cfg.multi_cfg())
            .build()
            .ok()
    }

    /// Build action plans HTML
    fn action_plans_html<'p>(
        &self,
        anc: &DmsAnc,
        details: &'p mut html::Details<'p>,
    ) {
        if anc.action_plans.is_empty() {
            return;
        }
        details.summary().cdata("📋 Action Plans").close();
        let mut div = details.div();
        div.class("row fill");
        div.span().cdata("Name").close();
        div.span().cdata("Phase").close();
        div.close();
        for act in &anc.action_plans {
            let mut div = details.div();
            div.class("row fill");
            div.span().class("info").cdata(&act.name).close();
            let mut span = div.span();
            let mut select = span.select();
            select.id("phase").disabled();
            for p in anc.phases(act) {
                let mut option = select.option();
                if p == act.phase {
                    option.selected();
                }
                option.cdata(p).close();
            }
            select.close();
            span.close();
            div.close();
        }
        details.close();
    }

    // Get selected message pattern
    fn selected_pattern<'a>(&self, anc: &'a DmsAnc) -> Option<&'a MsgPattern> {
        let doc = Doc::get();
        let pat_name = doc.elem::<HtmlSelectElement>("mc_pattern").value();
        let pat = anc.compose_patterns.iter().find(|p| p.name == pat_name);
        if pat.is_none() {
            log::warn!("pattern not found: {pat_name}");
        }
        pat
    }

    // Get selected lines
    fn selected_lines(&self) -> Vec<String> {
        let doc = Doc::get();
        let mut lines = Vec::new();
        while let Some(line) = doc.try_elem::<HtmlInputElement>(&format!(
            "mc_line{}",
            lines.len() + 1
        )) {
            lines.push(line.value());
        }
        lines
    }

    /// Get selected MULTI message
    fn selected_multi(&self, anc: &DmsAnc) -> Option<String> {
        let pat = self.selected_pattern(anc)?;
        let dms = self.make_dms(anc)?;
        let lines = self.selected_lines();
        let multi = MessagePattern::new(&dms, &pat.multi)
            .fill(lines.iter().map(|l| &l[..]));
        Some(multi_normalize(&multi))
    }

    /// Get selected message duration
    fn selected_duration(&self) -> Option<u32> {
        Doc::get().select_parse::<u32>("mc_expire")
    }

    /// Create actions to handle click on "Send" button
    fn send_actions(&self, anc: DmsAnc) -> Vec<Action> {
        match &self.selected_multi(&anc) {
            Some(ms) => anc.lock_action(
                uri_one(Res::Dms, &self.name),
                ms,
                self.selected_duration(),
            ),
            None => Vec::new(),
        }
    }

    /// Create actions to handle click on "Blank" button
    fn blank_actions(&self, anc: DmsAnc) -> Vec<Action> {
        anc.blank_actions(uri_one(Res::Dms, &self.name))
    }

    /// Create action to handle click on a device request button
    #[allow(clippy::vec_init_then_push)]
    fn device_req(&self, req: DeviceReq) -> Vec<Action> {
        let uri = uri_one(Res::Dms, &self.name);
        let mut actions = Vec::with_capacity(1);
        actions.push(Action::Patch(uri, req.to_string().into()));
        actions
    }

    /// Convert to Request HTML
    fn to_html_request(&self, _anc: &DmsAnc) -> String {
        let work = "http://example.com"; // FIXME
        let mut page = Page::new();
        self.title(View::Request, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.span().cdata("Current Message").close();
        div.button()
            .id("rq_msg_query")
            .r#type("button")
            .cdata("Query")
            .close();
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.span().cdata("Current Status").close();
        div.button()
            .id("rq_status_query")
            .r#type("button")
            .cdata("Query")
            .close();
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.span().cdata("Pixel Errors").close();
        let mut span = div.span();
        span.button()
            .id("rq_pixel_test")
            .r#type("button")
            .cdata("Test")
            .close();
        span.button()
            .id("rq_pixel_clear")
            .r#type("button")
            .cdata("Clear")
            .close();
        span.button()
            .id("rq_pixel_query")
            .r#type("button")
            .cdata("Query")
            .close();
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.span().cdata("Settings").close();
        span = div.span();
        span.button()
            .id("rq_settings_send")
            .r#type("button")
            .cdata("Send")
            .close();
        span.button()
            .id("rq_settings_query")
            .r#type("button")
            .cdata("Query")
            .close();
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.span().cdata("Configuration").close();
        span = div.span();
        span.button()
            .id("rq_config_reset")
            .r#type("button")
            .cdata("Reset")
            .close();
        span.button()
            .id("rq_config_query")
            .r#type("button")
            .cdata("Query")
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
    fn to_html_setup(&self, anc: &DmsAnc) -> String {
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
        self.sign_config_html(anc, &mut page.frag::<html::Div>());
        // FIXME: add sign_detail button
        self.footer_html(true, &mut page.frag::<html::Div>());
        String::from(page)
    }

    /// Make sign config row as HTML
    fn sign_config_html<'p>(&self, anc: &DmsAnc, div: &'p mut html::Div<'p>) {
        div.class("row");
        div.label().cdata("Sign Config").close();
        match anc.sign_config(self.sign_config.as_deref()) {
            Some(cfg) => {
                div.button()
                    .r#type("button")
                    .class("go_link")
                    .data_("link", &cfg.name)
                    .data_("type", Res::SignConfig.as_str())
                    .cdata(&cfg.name)
                    .close();
            }
            None => {
                div.span().close(); /* empty */
            }
        }
        div.close();
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &DmsAnc) -> String {
        let mut page = Page::new();
        self.title(View::Status, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        self.item_states(anc).tooltips(&mut div.span());
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.span()
            .class("info")
            .cdata_len(opt_ref(&self.location), 64)
            .close();
        div.close();
        self.temp_html(&mut page.frag::<html::Div>());
        self.light_html(&mut page.frag::<html::Div>());
        self.power_html(&mut page.frag::<html::Div>());
        String::from(page)
    }

    /// Build temperature status HTML
    fn temp_html<'p>(&self, div: &'p mut html::Div<'p>) {
        if let Some(status) = &self.status {
            div.cdata("🌡️ ");
            div.b().cdata("Temperature").close();
            let mut ul = div.ul();
            if let Some(temps) = &status.ambient_temps {
                temp_range_html("Ambient", temps, &mut ul);
            }
            if let Some(temps) = &status.housing_temps {
                temp_range_html("Housing", temps, &mut ul);
            }
            if let Some(temps) = &status.cabinet_temps {
                temp_range_html("Cabinet", temps, &mut ul);
            }
        }
        div.close();
    }

    /// Build light status HTML
    fn light_html<'p>(&self, div: &'p mut html::Div<'p>) {
        if let Some(status) = &self.status {
            div.cdata("🔅 ").b().cdata("Light Output").close();
            if let Some(light) = &status.light_output {
                div.meter().max(100).value(*light).close();
                div.cdata("🔆 ").cdata(*light).cdata("%");
            }
            if let Some(photocells) = &status.photocells {
                let mut table = div.table();
                for (i, photocell) in photocells.iter().enumerate() {
                    let mut tr = table.tr();
                    tr.td().cdata(i + 1).close();
                    tr.td().cdata_len(&photocell.description, 20).close();
                    let mut td = tr.td();
                    let reading = &photocell.reading;
                    match reading.parse::<f32>() {
                        Ok(_r) => {
                            td.meter().max(100).value(reading).close();
                            td.cdata("☀️ ").cdata(reading).cdata("%");
                        }
                        Err(_e) => {
                            td.class("fault").cdata_len(reading, 16);
                        }
                    }
                    tr.close();
                }
            }
        }
        div.close();
    }

    /// Build power supply status HTML
    fn power_html<'p>(&self, div: &'p mut html::Div<'p>) {
        if let Some(status) = &self.status
            && let Some(power_supplies) = &status.power_supplies
        {
            div.cdata("⚡ ");
            div.b().cdata("Power").close();
            let mut table = div.table();
            for (i, supply) in power_supplies.iter().enumerate() {
                let mut tr = table.tr();
                tr.td().cdata(i + 1).close();
                tr.td().cdata_len(&supply.description, 20).close();
                let mut td = tr.td();
                let voltage = &supply.voltage;
                match voltage.parse::<f32>() {
                    Ok(v) => {
                        if v <= 0.0 {
                            td.class("fault");
                        }
                        td.cdata(voltage).cdata("V");
                    }
                    Err(_e) => {
                        td.class("fault").cdata_len(voltage, 16);
                    }
                }
                td.close();
                tr.td().cdata_len(&supply.supply_type, 12).close();
                tr.close();
            }
        }
        div.close();
    }
}

/// Make expire select element
fn make_expire_select<'p>(select: &'p mut html::Select<'p>) {
    select.id("mc_expire");
    select.option().value("").cdata("⏲️ ").close();
    select.option().value("5").cdata("5 m").close();
    select.option().value("10").cdata("10 m").close();
    select.option().value("15").cdata("15 m").close();
    select.option().value("30").cdata("30 m").close();
    select.option().value("60").cdata("60 m").close();
    select.option().value("90").cdata("90 m").close();
    select.option().value("120").cdata("2 h").close();
    select.option().value("180").cdata("3 h").close();
    select.option().value("240").cdata("4 h").close();
    select.option().value("300").cdata("5 h").close();
    select.option().value("360").cdata("6 h").close();
    select.option().value("480").cdata("8 h").close();
    select.option().value("600").cdata("10 h").close();
    select.option().value("720").cdata("12 h").close();
    select.option().value("960").cdata("16 h").close();
    select.option().value("1440").cdata("24 h").close();
    select.close();
}

/// Build temperature range HTML
fn temp_range_html(label: &str, temps: &[i32], ul: &mut html::Ul) {
    let mut mn = None;
    let mut mx = None;
    for &temp in temps {
        match (mn, mx) {
            (Some(t0), Some(t1)) => {
                mn = Some(temp.min(t0));
                mx = Some(temp.max(t1));
            }
            _ => {
                mn = Some(temp);
                mx = Some(temp);
            }
        }
    }
    if let (Some(mn), Some(mx)) = (mn, mx) {
        let t = if mn == mx {
            format!(" {:.1}", (f64::from(mn) * DegC).to::<TempUnit>())
        } else {
            format!(
                " {:.1}…{:.1}",
                (f64::from(mn) * DegC).to::<TempUnit>(),
                (f64::from(mx) * DegC).to::<TempUnit>(),
            )
        };
        ul.li().cdata(label).cdata(t).close();
    }
}

impl ControllerIo for Dms {
    /// Get controller name
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Loc for Dms {
    /// Get geo location name
    fn geoloc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }
}

impl Card for Dms {
    type Ancillary = DmsAnc;

    /// Get the resource
    fn res() -> Res {
        Res::Dms
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[
            ItemState::Available,
            ItemState::Deployed,
            ItemState::Planned,
            ItemState::Incident,
            ItemState::External,
            ItemState::Dedicated,
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
        } else if states.contains(ItemState::Dedicated) {
            ItemState::Dedicated
        } else if states.contains(ItemState::Offline) {
            ItemState::Offline
        } else if states.contains(ItemState::Deployed) {
            ItemState::Deployed
        } else if states.contains(ItemState::Planned) {
            ItemState::Planned
        } else if states.contains(ItemState::External) {
            ItemState::External
        } else {
            ItemState::Available
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &DmsAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || self.notes.contains_lower(search)
            || self.item_states(anc).is_match(search)
            || anc
                .sign_message(self.msg_current.as_deref())
                .is_some_and(|m| m.is_match(search))
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &DmsAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Control => self.to_html_control(anc),
            View::Location => anc.loc.to_html_loc(self),
            View::Request => self.to_html_request(anc),
            View::Setup => self.to_html_setup(anc),
            View::Status => self.to_html_status(anc),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_text_area("notes", &self.notes);
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }

    /// Get changed fields on Location view
    fn changed_location(&self, anc: DmsAnc) -> String {
        anc.loc.changed_location()
    }

    /// Handle click event for a button on the card
    fn handle_click(&self, anc: DmsAnc, id: String) -> Vec<Action> {
        match id.as_str() {
            "mc_send" => self.send_actions(anc),
            "mc_blank" => self.blank_actions(anc),
            "rq_msg_query" => self.device_req(DeviceReq::QueryMessage),
            "rq_status_query" => self.device_req(DeviceReq::QueryStatus),
            "rq_pixel_test" => self.device_req(DeviceReq::TestPixels),
            "rq_pixel_clear" => self.device_req(DeviceReq::ResetStatus),
            "rq_pixel_query" => self.device_req(DeviceReq::QueryPixelFailures),
            "rq_settings_send" => self.device_req(DeviceReq::SendSettings),
            "rq_settings_query" => self.device_req(DeviceReq::QuerySettings),
            "rq_config_reset" => self.device_req(DeviceReq::ResetDevice),
            "rq_config_query" => self.device_req(DeviceReq::QueryConfiguration),
            _ => Vec::new(),
        }
    }

    /// Handle input event for an element on the card
    fn handle_input(&self, anc: DmsAnc, id: String) -> Vec<Action> {
        let Some(pat) = self.selected_pattern(&anc) else {
            return Vec::new();
        };
        let Some(dms) = self.make_dms(&anc) else {
            return Vec::new();
        };
        let lines = if &id == "mc_pattern" {
            // update mc_lines element
            let mut page = Page::new();
            anc.make_lines_html(&dms, pat, "", &mut page.frag::<html::Div>());
            let mc_lines = Doc::get().elem::<HtmlElement>("mc_lines");
            mc_lines.set_outer_html(&String::from(page));
            Vec::new()
        } else {
            self.selected_lines()
        };
        let multi = MessagePattern::new(&dms, &pat.multi)
            .fill(lines.iter().map(|l| &l[..]));
        let multi = multi_normalize(&multi);
        // update mc_preview image element
        let mut page = Page::new();
        let mut rend = Renderer::new()
            .with_dms(&dms)
            .with_id("mc_preview")
            .with_class("preview")
            .with_max_width(240)
            .with_max_height(80);
        let mut img = page.frag::<html::Img>();
        rend.render_multi(&multi, &mut img);
        let preview = Doc::get().elem::<HtmlElement>("mc_preview");
        preview.set_outer_html(&String::from(page));
        Vec::new()
    }
}
