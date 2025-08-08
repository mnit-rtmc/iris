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
use crate::card::{AncillaryData, Card, View, uri_one};
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::device::DeviceReq;
use crate::error::Result;
use crate::fetch::{Action, Uri};
use crate::geoloc::{Loc, LocAnc};
use crate::item::{ItemState, ItemStates};
use crate::lock::LockReason;
use crate::notes::contains_hashtag;
use crate::sign::{self, NtcipSign};
use crate::signmessage::SignMessage;
use crate::start::fly_map_item;
use crate::util::{ContainsLower, Doc, Fields, Input, TextArea, opt_ref};
use chrono::{DateTime, Local, format::SecondsFormat};
use hatmil::Html;
use js_sys::{ArrayBuffer, Uint8Array};
use mag::temp::DegC;
use ntcip::dms::multi::{
    join_text, normalize as multi_normalize, split as multi_split,
};
use ntcip::dms::{Font, FontTable, GraphicTable, MessagePattern, tfon};
use rendzina::{SignConfig, load_graphic};
use resources::Res;
use serde::Deserialize;
use serde_json::Value;
use std::borrow::Cow;
use std::cmp::Ordering;
use std::fmt;
use std::iter::repeat;
use std::time::Duration;
use wasm_bindgen::{JsCast, JsValue};
use web_sys::{HtmlElement, HtmlInputElement, HtmlSelectElement, console};

/// Display Units
type TempUnit = mag::temp::DegF;

/// Expire select element
const EXPIRE_SELECT: &str = "<select id='mc_expire'>\
<option value=''>⏲️ \
<option value='5'>5 m\
<option value='10'>10 m\
<option value='15'>15 m\
<option value='30'>30 m\
<option value='60'>60 m\
<option value='90'>90 m\
<option value='120'>2 h\
<option value='180'>3 h\
<option value='240'>4 h\
<option value='300'>5 h\
<option value='360'>6 h\
<option value='480'>8 h\
<option value='600'>10 h\
<option value='720'>12 h\
<option value='960'>16 h\
<option value='1440'>24 h\
</select>";

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
    pub beacon: Option<String>,
    pub preset: Option<String>,
    pub sign_config: Option<String>,
    pub sign_detail: Option<String>,
    pub msg_sched: Option<String>,
    pub geo_loc: Option<String>,
    pub status: Option<SignStatus>,
    pub pix_failures: Option<String>,
}

/// Message Pattern
#[derive(Debug, Default, Deserialize, PartialEq, Eq)]
pub struct MsgPattern {
    pub name: String,
    pub compose_hashtag: Option<String>,
    pub multi: String,
    pub flash_beacon: Option<bool>,
    pub pixel_service: Option<bool>,
}

/// Message Line
#[derive(Debug, Default, Deserialize)]
#[allow(dead_code)]
pub struct MsgLine {
    pub name: String,
    pub msg_pattern: String,
    pub restrict_hashtag: Option<String>,
    pub line: u16,
    pub multi: String,
}

/// Word (for messages)
#[derive(Clone, Debug, Default, Deserialize)]
pub struct Word {
    pub name: String,
    pub abbr: Option<String>,
    pub allowed: bool,
}

/// Font name
#[derive(Debug, Default, Deserialize)]
#[allow(dead_code)]
pub struct FontName {
    pub font_number: u8,
    pub name: String,
}

/// Graphic name
#[derive(Debug, Default, Deserialize)]
#[allow(dead_code)]
pub struct GraphicName {
    pub number: u8,
    pub name: String,
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
}

impl PartialOrd for MsgPattern {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        Some(self.cmp(other))
    }
}

impl Ord for MsgPattern {
    fn cmp(&self, other: &Self) -> Ordering {
        if self == other {
            return Ordering::Equal;
        }
        // prefer patterns which can be conbined (shared)
        let self_combine = self.can_combine_shared_second();
        let other_combine = other.can_combine_shared_second();
        if self_combine && !other_combine {
            return Ordering::Less;
        } else if other_combine && !self_combine {
            return Ordering::Greater;
        }
        let len_ord = self.multi.len().cmp(&other.multi.len());
        if len_ord != Ordering::Equal {
            return len_ord;
        }
        let ms_ord = self.multi.cmp(&other.multi);
        if ms_ord != Ordering::Equal {
            ms_ord
        } else {
            self.name.cmp(&other.name)
        }
    }
}

impl MsgPattern {
    // Check if pattern can combine (shared) in second position
    fn can_combine_shared_second(&self) -> bool {
        let mut it = multi_split(&self.multi);
        // check that:
        // - the first value is a text rectangle
        // - the same text rectangle starts every page
        // - there are no other text rectangles
        if let Some(first) = it.next() {
            if first.starts_with("[tr") {
                let mut tr_this_page = true;
                for val in it {
                    if tr_this_page {
                        if val.starts_with("[tr") {
                            return false;
                        } else if val == "[np]" {
                            tr_this_page = false;
                        }
                    } else if val == first {
                        tr_this_page = true;
                    } else {
                        return false;
                    }
                }
                return tr_this_page;
            }
        }
        false
    }
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
                    console::log_1(&format!("invalid graphic: {nm}").into());
                }
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
    fn make_lines_html(
        &self,
        sign: &NtcipSign,
        pat_def: &MsgPattern,
        ms_cur: &str,
        html: &mut Html,
    ) {
        // NOTE: this prevents lifetime from escaping
        let mut pat = pat_def;
        if self.pat_lines(pat).count() == 0 {
            let n_lines =
                MessagePattern::new(&sign.dms, &pat.multi).widths().count();
            match self.find_substitute(pat, n_lines) {
                Some(sub) => pat = sub,
                None => return,
            }
        }
        let widths = MessagePattern::new(&sign.dms, &pat_def.multi).widths();
        let cur_lines = MessagePattern::new(&sign.dms, &pat_def.multi)
            .lines(ms_cur)
            .chain(repeat(""));
        html.div().id("mc_lines").class("column");
        let mut rect_num = 0;
        for (i, ((width, font_num, rn), cur_line)) in
            widths.zip(cur_lines).enumerate()
        {
            let ln = 1 + i as u16;
            let mc_line = format!("mc_line{ln}");
            let mc_choice = format!("mc_choice{ln}");
            let input = html.input().id(mc_line).attr("list", &mc_choice);
            if rn != rect_num {
                input.class("mc_line_gap");
                rect_num = rn;
            }
            html.datalist().id(mc_choice);
            if let Some(font) = sign.dms.font_definition().font(font_num) {
                for ml in self.pat_lines(pat) {
                    if ml.line == ln {
                        self.line_html(&ml.multi, width, font, cur_line, html)
                    }
                }
            }
            html.end(); /* datalist */
        }
        html.end(); /* div */
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

    /// Build line HTML
    fn line_html(
        &self,
        multi: &str,
        width: u16,
        font: &Font,
        cur_line: &str,
        html: &mut Html,
    ) {
        // FIXME: handle line-allowed MULTI tags
        let mut ms = multi;
        let mut line;
        loop {
            let Ok(w) = font.text_width(ms, None) else {
                return;
            };
            if w <= width {
                let option = html.option().value(ms);
                if ms == cur_line {
                    option.attr_bool("selected");
                }
                html.text(join_text(ms, " ")).end();
                break;
            } else if let Some(abbrev) = self.abbreviate_text(ms) {
                line = abbrev;
                ms = &line[..];
            } else {
                break;
            }
        }
    }

    /// Abbreviate message text
    fn abbreviate_text(&self, text: &str) -> Option<String> {
        let mut abbrev = Word::default();
        for w in text.split(' ') {
            let sc = w.len();
            // prefer to abbreviate longer words
            if sc > abbrev.name.len() {
                for word in &self.words {
                    if word.allowed && word.name == w {
                        if let Some(ab) = &word.abbr {
                            if ab != w {
                                abbrev = word.clone();
                            }
                        }
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
            if let Some(status) = &self.status {
                if let Some(faults) = &status.faults {
                    return Some(faults);
                }
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
        let mut html = Html::new();
        html.div().class("title row");
        html.span()
            .text(self.name())
            .text(" ")
            .text(self.item_states(anc).to_string())
            .end();
        html.span().class("info").text(self.user(anc)).end();
        html.end(); /* div */
        if let Some(gif) = self.msg_current_gif() {
            html.img().class("message").src(gif);
        }
        html.div().class("info fill");
        html.text_len(opt_ref(&self.location), 64);
        html.to_string()
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
        let mut html = self.title(View::Control);
        html.div().class("row fill");
        self.item_states(anc).tooltips(&mut html);
        html.span();
        if let Some(lock) = &self.lock
            && let Some(expires) = lock.expires()
        {
            html.text(expires);
        }
        html.end(); /* span */
        html.end(); /* div */
        if let Some(gif) = self.msg_current_gif() {
            html.img().class("message").src(gif);
        }
        html.div().class("info fill");
        html.text_len(opt_ref(&self.location), 64);
        html.end(); /* div */
        self.message_composer_html(anc, &mut html);
        html.to_string()
    }

    /// Build message composer HTML
    fn message_composer_html(&self, anc: &DmsAnc, html: &mut Html) {
        if anc.compose_patterns.is_empty() {
            console::log_1(
                &format!("{}: No compose patterns", self.name).into(),
            );
            return;
        }
        let Some(sign) = self.make_sign(anc) else {
            return;
        };
        let sign = Some(sign);
        let pat_def = self.pattern_default(anc);
        let multi = pat_def.map(|pat| &pat.multi[..]).unwrap_or("");
        html.div().id("mc_grid");
        html.raw(sign::render(&sign, multi, 240, 80, None));
        html.select().id("mc_pattern");
        for pat in &anc.compose_patterns {
            let option = html.option();
            if let Some(p) = pat_def {
                if p.name == pat.name {
                    option.attr_bool("selected");
                }
            }
            html.text(&pat.name).end();
        }
        html.end(); /* select */
        if let Some(pat) = pat_def {
            anc.make_lines_html(
                #[allow(clippy::unnecessary_literal_unwrap)]
                &sign.unwrap(),
                pat,
                self.current_multi(anc),
                html,
            );
        }
        html.raw(EXPIRE_SELECT);
        html.button()
            .id("mc_send")
            .type_("button")
            .text("Send")
            .end();
        html.button()
            .id("mc_blank")
            .type_("button")
            .text("Blank")
            .end();
        html.end(); /* div */
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

    /// Make an ntcip sign
    fn make_sign(&self, anc: &DmsAnc) -> Option<NtcipSign> {
        let cfg = anc.sign_config(self.sign_config.as_deref())?;
        NtcipSign::new(cfg, anc.fonts.clone(), anc.graphics.clone())
            .map(|sign| sign.with_id("mc_preview"))
    }

    // Get selected message pattern
    fn selected_pattern<'a>(&self, anc: &'a DmsAnc) -> Option<&'a MsgPattern> {
        let doc = Doc::get();
        let pat_name = doc.elem::<HtmlSelectElement>("mc_pattern").value();
        let pat = anc.compose_patterns.iter().find(|p| p.name == pat_name);
        if pat.is_none() {
            console::log_1(&format!("pattern not found: {pat_name}").into());
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
        let sign = self.make_sign(anc)?;
        let lines = self.selected_lines();
        let multi = MessagePattern::new(&sign.dms, &pat.multi)
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
        let mut html = self.title(View::Request);
        html.div().class("row");
        html.span().text("Current Message").end();
        html.button()
            .id("rq_msg_query")
            .type_("button")
            .text("Query");
        html.end().end(); /* button, div */
        html.div().class("row");
        html.span().text("Current Status").end();
        html.button()
            .id("rq_status_query")
            .type_("button")
            .text("Query");
        html.end().end(); /* button, div */
        html.div().class("row");
        html.span().text("Pixel Errors").end();
        html.span();
        html.button()
            .id("rq_pixel_text")
            .type_("button")
            .text("Test")
            .end();
        html.button()
            .id("rq_pixel_query")
            .type_("button")
            .text("Query");
        html.end().end().end(); /* button, span, div */
        html.div().class("row");
        html.span().text("Settings").end();
        html.span();
        html.button()
            .id("rq_settings_send")
            .type_("button")
            .text("Send");
        html.end();
        html.button()
            .id("rq_settings_query")
            .type_("button")
            .text("Query");
        html.end().end().end(); /* button, span, div */
        html.div().class("row");
        html.span().text("Configuration").end();
        html.span();
        html.button()
            .id("rq_config_reset")
            .type_("button")
            .text("Reset");
        html.end();
        html.button()
            .id("rq_config_query")
            .type_("button")
            .text("Query");
        html.end().end().end(); /* button, span, div */
        html.div().class("row");
        html.span().text("Work Request").end();
        html.a()
            .href(work)
            .attr("target", "_blank")
            .attr("rel", "noopener noreferrer")
            .text("🔗 ")
            .text(self.name());
        html.to_string()
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &DmsAnc) -> String {
        let mut html = self.title(View::Setup);
        html.div().class("row");
        html.label().for_("notes").text("Notes").end();
        html.textarea()
            .id("notes")
            .maxlength("255")
            .attr("rows", "4")
            .attr("cols", "24")
            .text(opt_ref(&self.notes))
            .end();
        html.end(); /* div */
        anc.cio.controller_html(self, &mut html);
        anc.cio.pin_html(self.pin, &mut html);
        self.footer_html(true, &mut html);
        html.to_string()
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &DmsAnc) -> String {
        let mut html = self.title(View::Status);
        html.div();
        self.item_states(anc).tooltips(&mut html);
        html.end(); /* div */
        html.div().class("row");
        html.span()
            .class("info")
            .text_len(opt_ref(&self.location), 64)
            .end();
        html.end(); /* div */
        self.temp_html(&mut html);
        self.light_html(&mut html);
        self.power_html(&mut html);
        html.to_string()
    }

    /// Build temperature status HTML
    fn temp_html(&self, html: &mut Html) {
        if let Some(status) = &self.status {
            html.div().text("🌡️ ").b().text("Temperature").end().end();
            html.ul();
            if let Some(temps) = &status.ambient_temps {
                temp_range_html("Ambient", temps, html);
            }
            if let Some(temps) = &status.housing_temps {
                temp_range_html("Housing", temps, html);
            }
            if let Some(temps) = &status.cabinet_temps {
                temp_range_html("Cabinet", temps, html);
            }
            html.end(); /* ul */
        }
    }

    /// Build light status HTML
    fn light_html(&self, html: &mut Html) {
        if let Some(status) = &self.status {
            html.div().text("🔅 ").b().text("Light Output").end();
            if let Some(light) = &status.light_output {
                let light = light.to_string();
                html.meter().max("100").value(&light).end();
                html.text("🔆 ").text(light).text("%");
            }
            html.end(); /* div */
            if let Some(photocells) = &status.photocells {
                html.table();
                for (i, photocell) in photocells.iter().enumerate() {
                    html.tr();
                    html.td().text((i + 1).to_string()).end();
                    html.td().text_len(&photocell.description, 20).end();
                    let td = html.td();
                    let reading = &photocell.reading;
                    match reading.parse::<f32>() {
                        Ok(_r) => {
                            html.meter().max("100").value(reading).end();
                            html.text("☀️ ").text(reading).text("%");
                        }
                        Err(_e) => {
                            td.class("fault").text_len(reading, 16);
                        }
                    }
                    html.end().end(); /* td, tr */
                }
                html.end(); /* table */
            }
        }
    }

    /// Build power supply status HTML
    fn power_html(&self, html: &mut Html) {
        if let Some(status) = &self.status {
            if let Some(power_supplies) = &status.power_supplies {
                html.div().text("⚡ ").b().text("Power").end().end();
                html.table();
                for (i, supply) in power_supplies.iter().enumerate() {
                    html.tr();
                    html.td().text((i + 1).to_string()).end();
                    html.td().text_len(&supply.description, 20).end();
                    let td = html.td();
                    let voltage = &supply.voltage;
                    match voltage.parse::<f32>() {
                        Ok(v) => {
                            if v <= 0.0 {
                                td.class("fault");
                            }
                            html.text(voltage).text("V");
                        }
                        Err(_e) => {
                            td.class("fault").text_len(voltage, 16);
                        }
                    }
                    html.end(); /* td */
                    html.td().text_len(&supply.supply_type, 12).end();
                    html.end(); /* tr */
                }
                html.end(); /* table */
            }
        }
    }
}

/// Build temperature range HTML
fn temp_range_html(label: &str, temps: &[i32], html: &mut Html) {
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
        html.li().text(label).text(t).end();
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

    /// Display name
    const DNAME: &'static str = "⬛ Dms";

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
        let item_states = self.item_states(anc);
        if item_states.is_match(ItemState::Inactive.code()) {
            ItemState::Inactive
        } else if item_states.is_match(ItemState::Dedicated.code()) {
            ItemState::Dedicated
        } else if item_states.is_match(ItemState::Offline.code()) {
            ItemState::Offline
        } else if item_states.is_match(ItemState::Deployed.code()) {
            ItemState::Deployed
        } else if item_states.is_match(ItemState::Planned.code()) {
            ItemState::Planned
        } else if item_states.is_match(ItemState::External.code()) {
            ItemState::External
        } else if item_states.is_match(ItemState::Fault.code()) {
            ItemState::Fault
        } else {
            ItemState::Available
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &DmsAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || self
                .notes
                .as_ref()
                .is_some_and(|n| n.contains_lower(search))
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
        let Some(sign) = self.make_sign(&anc) else {
            return Vec::new();
        };
        let lines = if &id == "mc_pattern" {
            // update mc_lines element
            let mut html = Html::new();
            anc.make_lines_html(&sign, pat, "", &mut html);
            let mc_lines = Doc::get().elem::<HtmlElement>("mc_lines");
            mc_lines.set_outer_html(&html.to_string());
            Vec::new()
        } else {
            self.selected_lines()
        };
        let multi = MessagePattern::new(&sign.dms, &pat.multi)
            .fill(lines.iter().map(|l| &l[..]));
        let multi = multi_normalize(&multi);
        // update mc_preview image element
        let html = sign::render(&Some(sign), &multi, 240, 80, None);
        let preview = Doc::get().elem::<HtmlElement>("mc_preview");
        preview.set_outer_html(&html);
        Vec::new()
    }
}
