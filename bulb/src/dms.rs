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
use crate::notes::contains_hashtag;
use crate::sign::{self, NtcipSign};
use crate::signmessage::SignMessage;
use crate::start::fly_map_item;
use crate::util::{ContainsLower, Doc, Fields, HtmlStr, Input, TextArea};
use chrono::DateTime;
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
use std::borrow::Cow;
use std::cmp::Ordering;
use std::iter::repeat;
use wasm_bindgen::{JsCast, JsValue};
use web_sys::{HtmlElement, HtmlInputElement, HtmlSelectElement, console};

/// Display Units
type TempUnit = mag::temp::DegF;

/// Low 1 message priority
const LOW_1: u32 = 1;

/// High 1 message priority
const HIGH_1: u32 = 11;

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

/// Send button
const SEND_BUTTON: &str = "<button id='mc_send' type='button'>Send</button>";

/// Blank button
const BLANK_BUTTON: &str = "<button id='mc_blank' type='button'>Blank</button>";

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
    pub has_faults: Option<bool>,
    // secondary attributes
    pub pin: Option<u32>,
    pub static_graphic: Option<String>,
    pub beacon: Option<String>,
    pub preset: Option<String>,
    pub sign_config: Option<String>,
    pub sign_detail: Option<String>,
    pub msg_sched: Option<String>,
    pub msg_user: Option<String>,
    pub geo_loc: Option<String>,
    pub status: Option<SignStatus>,
    pub expire_time: Option<String>,
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
    /// Find a sign message
    fn find_sign_msg(&self, msg: &SignMessage) -> Option<&SignMessage> {
        self.messages.iter().find(|m| {
            m.sign_config == msg.sign_config
                && m.incident == msg.incident
                && m.multi == msg.multi
                && m.msg_owner == msg.msg_owner
                && m.flash_beacon == msg.flash_beacon
                && m.pixel_service == msg.pixel_service
                && m.msg_priority == msg.msg_priority
                && m.duration == msg.duration
        })
    }

    /// Get a sign message by name
    fn sign_message(&self, msg: Option<&str>) -> Option<&SignMessage> {
        msg.and_then(|msg| self.messages.iter().find(|m| m.name == msg))
    }

    /// Get message item states
    fn msg_states(&self, msg: Option<&str>) -> ItemStates {
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
    fn make_lines(
        &self,
        sign: &NtcipSign,
        pat: Option<&MsgPattern>,
        ms_cur: &str,
    ) -> String {
        let mut html = String::new();
        html.push_str("<div id='mc_lines' class='column'>");
        if let Some(pat) = pat {
            html.push_str(&self.make_lines_div(sign, pat, ms_cur));
        }
        html.push_str("</div>");
        html
    }

    /// Make line select elements
    fn make_lines_div(
        &self,
        sign: &NtcipSign,
        pat: &MsgPattern,
        ms_cur: &str,
    ) -> String {
        // NOTE: this prevents lifetime from escaping
        let mut pat = pat;
        let mut html = String::new();
        let widths = MessagePattern::new(&sign.dms, &pat.multi).widths();
        let cur_lines = MessagePattern::new(&sign.dms, &pat.multi)
            .lines(ms_cur)
            .chain(repeat(""));
        if self.pat_lines(pat).count() == 0 {
            let n_lines =
                MessagePattern::new(&sign.dms, &pat.multi).widths().count();
            match self.find_substitute(pat, n_lines) {
                Some(sub) => pat = sub,
                None => return html,
            }
        }
        let mut rect_num = 0;
        for (i, ((width, font_num, rn), cur_line)) in
            widths.zip(cur_lines).enumerate()
        {
            let ln = 1 + i as u16;
            let line = ln.to_string();
            html.push_str("<input id='mc_line");
            html.push_str(&line);
            html.push_str("' list='mc_choice");
            html.push_str(&line);
            html.push('\'');
            if rn != rect_num {
                html.push_str(" class='mc_line_gap'");
                rect_num = rn;
            }
            html.push_str("><datalist id='mc_choice");
            html.push_str(&line);
            html.push_str("'>");
            if let Some(font) = sign.dms.font_definition().font(font_num) {
                for ml in self.pat_lines(pat) {
                    if ml.line == ln {
                        self.append_line(
                            &ml.multi, width, font, cur_line, &mut html,
                        )
                    }
                }
            }
            html.push_str("</datalist>");
        }
        html
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

    /// Append a line as an option element
    fn append_line(
        &self,
        multi: &str,
        width: u16,
        font: &Font,
        cur_line: &str,
        html: &mut String,
    ) {
        // FIXME: handle line-allowed MULTI tags
        let mut ms = multi;
        let mut line;
        loop {
            let Ok(w) = font.text_width(ms, None) else {
                return;
            };
            if w <= width {
                html.push_str("<option value='");
                html.push_str(ms);
                if ms == cur_line {
                    html.push_str("' selected>");
                } else {
                    html.push_str("'>");
                }
                html.push_str(&join_text(ms, " "));
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

    /// Create actions to activate a sign message
    fn sign_msg_actions(self, uri: Uri, msg: SignMessage) -> Vec<Action> {
        match self.find_sign_msg(&msg) {
            #[allow(clippy::vec_init_then_push)]
            Some(msg) => {
                let mut actions = Vec::with_capacity(1);
                actions.push(msg_user_action(uri, &msg.name));
                actions
            }
            None => {
                let mut actions = Vec::with_capacity(2);
                if let Ok(val) = serde_json::to_string(&msg) {
                    let post = Uri::from("/iris/api/sign_message");
                    actions.push(Action::Post(post, val.into()));
                    actions.push(msg_user_action(uri, &msg.name));
                }
                actions
            }
        }
    }
}

/// Create a msg_user patch action
fn msg_user_action(uri: Uri, msg_name: &str) -> Action {
    let val = format!("{{\"msg_user\":\"{msg_name}\"}}");
    Action::Patch(uri, val.into())
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

/// Build an HTML title row (div) from a slice of spans
fn html_title_row(spans: &[&str], cls: &[&str]) -> String {
    let mut row = String::from("<div class='title row'>");
    for (span, c) in spans.iter().zip(cls.iter().chain(repeat(&""))) {
        if !c.is_empty() {
            row.push_str("<span class='");
            row.push_str(c);
            row.push_str("'>");
        } else {
            row.push_str("<span>");
        }
        row.push_str(span);
        row.push_str("</span>");
    }
    row.push_str("</div>");
    row
}

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
        let name = HtmlStr::new(self.name());
        let item_states = self.item_states(anc);
        let nm = format!("{name} {item_states}");
        let user = self.user(anc);
        let mut html = html_title_row(&[&nm, &user], &["", "info"]);
        if let Some(msg_current) = &self.msg_current {
            html.push_str("<img class='message' src='/iris/img/");
            html.push_str(msg_current);
            html.push_str(".gif'>");
        }
        html.push_str("<div class='info fill'>");
        html.push_str(&HtmlStr::new(&self.location).with_len(64).to_string());
        html.push_str("</div>");
        html
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
        let mut html = String::from(self.title(View::Control));
        html.push_str("<div class='row fill'>");
        let mut h = Html::new();
        self.item_states(anc).tooltips(&mut h);
        html.push_str(&String::from(h));
        html.push_str("<span>");
        if let Some(expire_time) = &self.expire_time {
            match DateTime::parse_from_rfc3339(expire_time) {
                Ok(dt) => html.push_str(&format!("⏲️ {}", &dt.format("%H:%M"))),
                _ => html.push_str("expires"),
            }
        }
        html.push_str("</span>");
        html.push_str("</div>");
        if let Some(msg_current) = &self.msg_current {
            html.push_str("<img class='message' src='/iris/img/");
            html.push_str(msg_current);
            html.push_str(".gif'>");
        }
        html.push_str("<div class='info fill'>");
        html.push_str(&HtmlStr::new(&self.location).with_len(64).to_string());
        html.push_str("</div>");
        if let Some(pats) = &self.compose_patterns(anc) {
            html.push_str(pats);
        }
        html
    }

    /// Build compose pattern HTML
    fn compose_patterns(&self, anc: &DmsAnc) -> Option<String> {
        if anc.compose_patterns.is_empty() {
            console::log_1(
                &format!("{}: No compose patterns", self.name).into(),
            );
            return None;
        }
        let sign = self.make_sign(anc)?;
        let sign = Some(sign);
        let mut html = String::new();
        html.push_str("<div id='mc_grid'>");
        let pat_def = self.pattern_default(anc);
        let multi = pat_def.map(|pat| &pat.multi[..]).unwrap_or("");
        html.push_str(&sign::render(&sign, multi, 240, 80, None));
        html.push_str("<select id='mc_pattern'>");
        for pat in &anc.compose_patterns {
            html.push_str("<option");
            if let Some(p) = pat_def {
                if p.name == pat.name {
                    html.push_str(" selected");
                }
            }
            html.push('>');
            html.push_str(&pat.name);
        }
        html.push_str("</select>");
        html.push_str(&anc.make_lines(
            #[allow(clippy::unnecessary_literal_unwrap)]
            &sign.unwrap(),
            pat_def,
            self.current_multi(anc),
        ));
        html.push_str(EXPIRE_SELECT);
        html.push_str(SEND_BUTTON);
        html.push_str(BLANK_BUTTON);
        html.push_str("</div>");
        Some(html)
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
        if let Some(cfg) = &self.sign_config {
            if let Some(ms) = &self.selected_multi(&anc) {
                match sign_msg_owner(HIGH_1) {
                    Some(owner) => {
                        let duration = self.selected_duration();
                        return anc.sign_msg_actions(
                            uri_one(Res::Dms, &self.name),
                            SignMessage::new(cfg, ms, owner, HIGH_1, duration),
                        );
                    }
                    None => console::log_1(&"no app user!".into()),
                };
            }
        }
        Vec::new()
    }

    /// Create actions to handle click on "Blank" button
    fn blank_actions(&self, anc: DmsAnc) -> Vec<Action> {
        match (&self.sign_config, sign_msg_owner(LOW_1)) {
            (Some(cfg), Some(owner)) => anc.sign_msg_actions(
                uri_one(Res::Dms, &self.name),
                SignMessage::new(cfg, "", owner, LOW_1, None),
            ),
            _ => Vec::new(),
        }
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
        let title = String::from(self.title(View::Request));
        let name = HtmlStr::new(self.name());
        let work = "http://example.com"; // FIXME
        format!(
            "{title}\
            <div class='row'>\
              <span>Current Message</span>\
              <button id='rq_msg_query' type='button'>Query</button>\
            </div>\
            <div class='row'>\
              <span>Current Status</span>\
              <button id='rq_status_query' type='button'>Query</button>\
            </div>\
            <div class='row'>\
              <span>Pixel Errors</span>\
              <span>\
                <button id='rq_pixel_test' type='button'>Test</button>\
                <button id='rq_pixel_query' type='button'>Query</button>\
              </span>\
            </div>\
            <div class='row'>\
              <span>Settings</span>\
              <span>\
                <button id='rq_settings_send' type='button'>Send</button>\
                <button id='rq_settings_query' type='button'>Query</button>\
              </span>\
            </div>\
            <div class='row'>\
              <span>Configuration</span>\
              <span>\
                <button id='rq_config_reset' type='button'>Reset</button>\
                <button id='rq_config_query' type='button'>Query</button>\
              </span>\
            </div>\
            <div class='row'>\
              <span>Work Request</span>
              <a href='{work}' target='_blank' rel='noopener noreferrer'>\
                🔗 {name}\
              </a>\
            </div>"
        )
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &DmsAnc) -> String {
        let title = String::from(self.title(View::Setup));
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
              <label for='notes'>Notes</label>\
              <textarea id='notes' maxlength='255' rows='4' \
                        cols='24'>{notes}</textarea>\
            </div>\
            {controller}\
            {pin}\
            {footer}"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &DmsAnc) -> String {
        let title = String::from(self.title(View::Status));
        let mut html = Html::new();
        self.item_states(anc).tooltips(&mut html);
        let item_states = String::from(html);
        let location = HtmlStr::new(&self.location).with_len(64);
        let mut html = format!(
            "{title}\
            <div>{item_states}</div>\
            <div class='row'>\
              <span class='info'>{location}</span>\
            </div>"
        );
        html.push_str(&self.temp_html());
        html.push_str(&self.light_html());
        html.push_str(&self.power_html());
        html
    }

    /// Get temperature status as HTML
    fn temp_html(&self) -> String {
        let mut html = String::new();
        if let Some(status) = &self.status {
            html.push_str("<div>🌡️ <b>Temperature</b></div><ul>");
            if let Some(temps) = &status.ambient_temps {
                html.push_str(&temp_range("Ambient", temps));
            }
            if let Some(temps) = &status.housing_temps {
                html.push_str(&temp_range("Housing", temps));
            }
            if let Some(temps) = &status.cabinet_temps {
                html.push_str(&temp_range("Cabinet", temps));
            }
            html.push_str("</ul>");
        }
        html
    }

    /// Get light status as HTML
    fn light_html(&self) -> String {
        let mut html = String::new();
        if let Some(status) = &self.status {
            html.push_str("<div>🔅 <b>Light Output</b>");
            if let Some(light) = &status.light_output {
                let light = light.to_string();
                html.push_str(" <meter max='100' value='");
                html.push_str(&light);
                html.push_str("'></meter>🔆 ");
                html.push_str(&light);
                html.push('%');
            }
            html.push_str("</div>");
            if let Some(photocells) = &status.photocells {
                html.push_str("<table>");
                for (i, photocell) in photocells.iter().enumerate() {
                    html.push_str("<tr><td>");
                    html.push_str(&(i + 1).to_string());
                    html.push_str("<td>");
                    html.push_str(
                        &HtmlStr::new(&photocell.description)
                            .with_len(20)
                            .to_string(),
                    );
                    let reading = &photocell.reading;
                    match reading.parse::<f32>() {
                        Ok(_r) => {
                            html.push_str("<td><meter max='100' value='");
                            html.push_str(reading);
                            html.push_str("'></meter>☀️ ");
                            html.push_str(reading);
                            html.push('%');
                        }
                        Err(_e) => {
                            html.push_str("<td class='fault'>");
                            html.push_str(
                                &HtmlStr::new(reading).with_len(16).to_string(),
                            );
                        }
                    }
                }
                html.push_str("</table>");
            }
        }
        html
    }

    /// Get power supply status as HTML
    fn power_html(&self) -> String {
        let mut html = String::new();
        if let Some(status) = &self.status {
            if let Some(power_supplies) = &status.power_supplies {
                html.push_str("<div>⚡ <b>Power</b></div><table>");
                for (i, supply) in power_supplies.iter().enumerate() {
                    html.push_str("<tr><td>");
                    html.push_str(&(i + 1).to_string());
                    html.push_str("<td>");
                    html.push_str(
                        &HtmlStr::new(&supply.description)
                            .with_len(20)
                            .to_string(),
                    );
                    let voltage = &supply.voltage;
                    match voltage.parse::<f32>() {
                        Ok(v) => {
                            if v > 0.0 {
                                html.push_str("<td>");
                            } else {
                                html.push_str("<td class='fault'>");
                            }
                            html.push_str(voltage);
                            html.push('V');
                        }
                        Err(_e) => {
                            html.push_str("<td class='fault'>");
                            html.push_str(
                                &HtmlStr::new(voltage).with_len(16).to_string(),
                            );
                        }
                    }
                    html.push_str("<td>");
                    html.push_str(
                        &HtmlStr::new(&supply.supply_type)
                            .with_len(12)
                            .to_string(),
                    );
                }
                html.push_str("</table>");
            }
        }
        html
    }
}

/// Format a temperature range from a Vec
fn temp_range(label: &str, temps: &[i32]) -> String {
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
    match (mn, mx) {
        (Some(mn), Some(mx)) => {
            if mn == mx {
                let t = (f64::from(mn) * DegC).to::<TempUnit>();
                format!("<li><div>{label} {t:.1}</div>")
            } else {
                let mn = (f64::from(mn) * DegC).to::<TempUnit>();
                let mx = (f64::from(mx) * DegC).to::<TempUnit>();
                format!("<li><div>{label} {mn:.1}…{mx:.1}</div>")
            }
        }
        _ => String::new(),
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

    /// All item states as html options
    const ITEM_STATES: &'static str = "<option value=''>all ↴\
         <option value='🔹'>🔹 available\
         <option value='🔶' selected>🔶 deployed\
         <option value='🗓️'>🗓️ planned\
         <option value='🚨'>🚨 incident\
         <option value='👽'>👽 external\
         <option value='🎯'>🎯 dedicated\
         <option value='⚠️'>⚠️ fault\
         <option value='🔌'>🔌 offline\
         <option value='🔺'>🔺 inactive";

    /// Get the resource
    fn res() -> Res {
        Res::Dms
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
            let html = anc.make_lines(&sign, Some(pat), "");
            let mc_lines = Doc::get().elem::<HtmlElement>("mc_lines");
            mc_lines.set_outer_html(&html);
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

/// Make sign message owner string
fn sign_msg_owner(priority: u32) -> Option<String> {
    crate::app::user().map(|user| {
        let sources = if priority == LOW_1 {
            "blank"
        } else {
            "operator"
        };
        format!("IRIS; {sources}; {user}")
    })
}
