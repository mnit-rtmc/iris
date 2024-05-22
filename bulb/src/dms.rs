// Copyright (C) 2022-2024  Minnesota Department of Transportation
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
use crate::card::{AncillaryData, Card, View, EDIT_BUTTON, LOC_BUTTON, NAME};
use crate::device::{Device, DeviceAnc};
use crate::error::Result;
use crate::fetch::{Action, ContentType, Uri};
use crate::item::{ItemState, ItemStates};
use crate::util::{ContainsLower, Doc, Fields, HtmlStr, Input, OptVal};
use base64::{engine::general_purpose::STANDARD_NO_PAD as b64enc, Engine as _};
use fnv::FnvHasher;
use js_sys::{ArrayBuffer, Uint8Array};
use ntcip::dms::multi::{
    is_blank, join_text, normalize as multi_normalize, split as multi_split,
};
use ntcip::dms::{tfon, Font, FontTable, GraphicTable, MessagePattern};
use rendzina::{load_graphic, SignConfig};
use resources::Res;
use serde::{Deserialize, Serialize};
use std::borrow::Cow;
use std::cmp::Ordering;
use std::hash::{Hash, Hasher};
use std::iter::repeat;
use wasm_bindgen::{JsCast, JsValue};
use web_sys::{console, HtmlElement, HtmlSelectElement};

/// Ntcip DMS sign
type Sign = ntcip::dms::Dms<256, 24, 32>;

/// Low 1 message priority
const LOW_1: u32 = 1;

/// High 1 message priority
const HIGH_1: u32 = 11;

/// Send button
const SEND_BUTTON: &str = "<button id='mc_send' type='button'>Send</button>";

/// Blank button
const BLANK_BUTTON: &str = "<button id='mc_blank' type='button'>Blank</button>";

/// Photocell status
#[derive(Debug, Default, Deserialize, Serialize, PartialEq)]
pub struct Photocell {
    description: String,
    error: Option<String>,
    reading: Option<i32>,
}

/// Power supply status
#[derive(Debug, Default, Deserialize, Serialize, PartialEq)]
pub struct PowerSupply {
    description: String,
    supply_type: String,
    error: Option<String>,
    detail: String,
    voltage: Option<f32>,
}

/// Sign status
#[derive(Debug, Default, Deserialize, Serialize, PartialEq)]
pub struct SignStatus {
    faults: Option<String>,
    photocells: Option<Vec<Photocell>>,
    light_output: Option<u32>,
    power_supplies: Option<Vec<PowerSupply>>,
    cabinet_temp_min: Option<i32>,
    cabinet_temp_max: Option<i32>,
    ambient_temp_min: Option<i32>,
    ambient_temp_max: Option<i32>,
    housing_temp_min: Option<i32>,
    housing_temp_max: Option<i32>,
    ldc_pot_base: Option<i32>,
    pixel_current_low: Option<i32>,
    pixel_current_high: Option<i32>,
}

/// Stuck pixel bitmaps (Base64-encoded)
#[derive(Debug, Default, Deserialize, Serialize, PartialEq)]
pub struct StuckPixels {
    off: Option<String>,
    on: Option<String>,
}

/// Dms
#[derive(Debug, Default, Deserialize, Serialize, PartialEq)]
pub struct Dms {
    pub name: String,
    pub location: Option<String>,
    pub controller: Option<String>,
    pub notes: Option<String>,
    pub hashtags: Option<String>,
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
    pub stuck_pixels: Option<StuckPixels>,
}

/// Action value to patch "msg_user"
#[derive(Debug, Serialize)]
struct MsgUser<'a> {
    msg_user: &'a str,
}

/// Sign Message
#[derive(Debug, Default, Hash, Deserialize, Serialize)]
pub struct SignMessage {
    pub name: String,
    pub sign_config: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub incident: Option<String>,
    pub multi: String,
    pub msg_owner: String,
    pub flash_beacon: bool,
    pub msg_priority: u32,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub duration: Option<u32>,
}

/// Message Pattern
#[derive(Debug, Default, Deserialize, Serialize, PartialEq, Eq)]
pub struct MsgPattern {
    pub name: String,
    pub compose_hashtag: Option<String>,
    pub multi: String,
    pub flash_beacon: Option<bool>,
}

/// Message Line
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct MsgLine {
    pub name: String,
    pub msg_pattern: String,
    pub restrict_hashtag: Option<String>,
    pub line: u16,
    pub multi: String,
}

/// Word (for messages)
#[derive(Clone, Debug, Default, Deserialize, Serialize)]
pub struct Word {
    pub name: String,
    pub abbr: Option<String>,
    pub allowed: bool,
}

/// Font name
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct FontName {
    pub font_number: u8,
    pub name: String,
}

/// Graphic name
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct GraphicName {
    pub number: u8,
    pub name: String,
}

/// DMS ancillary data
#[derive(Default)]
pub struct DmsAnc {
    dev: DeviceAnc<Dms>,
    messages: Vec<SignMessage>,
    configs: Vec<SignConfig>,
    compose_patterns: Vec<MsgPattern>,
    lines: Vec<MsgLine>,
    words: Vec<Word>,
    fnames: Vec<FontName>,
    fonts: FontTable<256, 24>,
    gnames: Vec<GraphicName>,
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

const SIGN_MSG_URI: &str = "/iris/sign_message";
const SIGN_CFG_URI: &str = "/iris/api/sign_config";
const MSG_PATTERN_URI: &str = "/iris/api/msg_pattern";
const MSG_LINE_URI: &str = "/iris/api/msg_line";
const WORD_URI: &str = "/iris/api/word";
const FONT_URI: &str = "/iris/api/font";
const GRAPHIC_URI: &str = "/iris/api/graphic";

impl AncillaryData for DmsAnc {
    type Primary = Dms;

    /// Get ancillary URI iterator
    fn uri_iter(
        &self,
        pri: &Self::Primary,
        view: View,
    ) -> Box<dyn Iterator<Item = Uri>> {
        let mut uris = Vec::new();
        // Have we been here before?
        if !self.fnames.is_empty() {
            for fname in &self.fnames {
                let mut uri = Uri::from("/iris/tfon/")
                    .with_content_type(ContentType::Text);
                uri.push(&fname.name);
                uri.add_extension(".tfon");
                uris.push(uri);
            }
            for gname in &self.gnames {
                let mut uri =
                    Uri::from("/iris/gif/").with_content_type(ContentType::Gif);
                uri.push(&gname.name);
                uri.add_extension(".gif");
                uris.push(uri);
            }
            return Box::new(uris.into_iter());
        }
        if let View::Compact | View::Search | View::Hidden = view {
            uris.push(SIGN_MSG_URI.into());
        }
        if let View::Status(_) = view {
            uris.push(SIGN_MSG_URI.into());
            uris.push(SIGN_CFG_URI.into());
            uris.push(MSG_PATTERN_URI.into());
            uris.push(MSG_LINE_URI.into());
            uris.push(WORD_URI.into());
            uris.push(FONT_URI.into());
            uris.push(GRAPHIC_URI.into());
        }
        Box::new(uris.into_iter().chain(self.dev.uri_iter(pri, view)))
    }

    /// Set ancillary data
    fn set_data(
        &mut self,
        pri: &Self::Primary,
        uri: Uri,
        data: JsValue,
    ) -> Result<bool> {
        match uri.as_str() {
            SIGN_MSG_URI => {
                self.messages = serde_wasm_bindgen::from_value(data)?;
            }
            SIGN_CFG_URI => {
                self.configs = serde_wasm_bindgen::from_value(data)?;
            }
            MSG_PATTERN_URI => {
                let mut patterns: Vec<MsgPattern> =
                    serde_wasm_bindgen::from_value(data)?;
                patterns.retain(|p| {
                    p.compose_hashtag
                        .as_ref()
                        .is_some_and(|h| pri.has_hashtag(h))
                });
                patterns.sort();
                self.compose_patterns = patterns;
            }
            MSG_LINE_URI => {
                let mut lines: Vec<MsgLine> =
                    serde_wasm_bindgen::from_value(data)?;
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
            WORD_URI => {
                self.words = serde_wasm_bindgen::from_value(data)?;
            }
            FONT_URI => {
                self.fnames = serde_wasm_bindgen::from_value(data)?;
                return Ok(!self.fnames.is_empty());
            }
            GRAPHIC_URI => {
                self.gnames = serde_wasm_bindgen::from_value(data)?;
                return Ok(!self.gnames.is_empty());
            }
            _ => {
                if uri.as_str().ends_with(".tfon") {
                    let font: String = serde_wasm_bindgen::from_value(data)?;
                    let font = tfon::read(font.as_bytes())?;
                    if let Some(f) = self.fonts.font_mut(font.number) {
                        *f = font;
                    } else if let Some(f) = self.fonts.font_mut(0) {
                        *f = font;
                    }
                } else if uri.as_str().ends_with(".gif") {
                    if let Ok(number) = uri
                        .as_str()
                        .replace(|c: char| !c.is_numeric(), "")
                        .parse::<u8>()
                    {
                        let abuf = data.dyn_into::<ArrayBuffer>().unwrap();
                        let graphic = Uint8Array::new(&abuf).to_vec();
                        let graphic = load_graphic(&graphic[..], number)?;
                        if let Some(g) = self.graphics.graphic_mut(number) {
                            *g = graphic;
                        } else if let Some(g) = self.graphics.graphic_mut(0) {
                            *g = graphic;
                        }
                    } else {
                        console::log_1(
                            &format!("invalid graphic: {}", uri.as_str())
                                .into(),
                        );
                    }
                } else {
                    return self.dev.set_data(pri, uri, data);
                }
            }
        }
        Ok(false)
    }
}

impl SignMessage {
    /// Make a sign message
    fn new(cfg: &str, ms: &str, owner: String, priority: u32) -> Self {
        let mut sign_message = SignMessage {
            name: "usr_".to_string(),
            sign_config: cfg.to_string(),
            multi: ms.to_string(),
            msg_owner: owner,
            msg_priority: priority,
            ..Default::default()
        };
        let mut hasher = FnvHasher::default();
        sign_message.hash(&mut hasher);
        let hash = hasher.finish() as u32;
        sign_message.name = format!("usr_{hash:08X}");
        sign_message
    }

    /// Get message owner
    fn owner(&self) -> &str {
        &self.msg_owner
    }

    /// Get "system" owner
    fn system(&self) -> &str {
        self.owner().split(';').next().unwrap_or("").trim()
    }

    /// Get "sources" owner
    fn sources(&self) -> &str {
        self.owner().split(';').nth(1).unwrap_or("").trim()
    }

    /// Get "user" owner
    fn user(&self) -> &str {
        self.owner().split(';').nth(2).unwrap_or("").trim()
    }

    /// Get item states
    fn item_states(&self) -> ItemStates {
        let blank = is_blank(&self.multi);
        let sources = self.sources();
        let mut states = ItemStates::default();
        if sources.contains("blank") || blank {
            states = states.with(ItemState::Available, "");
        }
        if sources.contains("operator") {
            states = states.with(ItemState::Deployed, self.user());
        }
        if sources.contains("schedule") {
            states = states.with(ItemState::Planned, self.user());
        }
        if sources.contains("external") {
            states = states.with(ItemState::External, "");
        }
        if sources.is_empty() && !blank {
            states = states.with(ItemState::External, self.system());
        }
        states
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str) -> bool {
        // checks are ordered by "most likely to be searched"
        self.multi.contains_lower(search)
            || self.user().contains_lower(search)
            || self.system().contains_lower(search)
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
        sign: &Sign,
        pat: Option<&MsgPattern>,
        ms_cur: &str,
    ) -> String {
        let mut html = String::new();
        html.push_str("<div id='mc_lines' class='column'>");
        if let Some(pat) = pat {
            let widths = MessagePattern::new(sign, &pat.multi).widths();
            let cur_lines = MessagePattern::new(sign, &pat.multi)
                .lines(ms_cur)
                .chain(repeat(""));
            let mut rect_num = 0;
            for (i, ((width, font_num, rn), cur_line)) in
                widths.zip(cur_lines).enumerate()
            {
                let ln = 1 + i as u16;
                html.push_str("<select id='mc_line");
                html.push_str(&ln.to_string());
                html.push('\'');
                if rn != rect_num {
                    html.push_str(" class='mc_line_gap'");
                    rect_num = rn;
                }
                html.push_str("><option>");
                if let Some(font) = sign.font_definition().font(font_num) {
                    for l in &self.lines {
                        if l.msg_pattern == pat.name && ln == l.line {
                            self.append_line(
                                &l.multi, width, font, cur_line, &mut html,
                            )
                        }
                    }
                }
                html.push_str("</select>");
            }
        }
        html.push_str("</div>");
        html
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
            Some(msg) => {
                let mut actions = Vec::with_capacity(1);
                if let Some(action) = msg_user_action(uri, &msg.name) {
                    actions.push(action);
                }
                actions
            }
            None => {
                let mut actions = Vec::with_capacity(2);
                if let Ok(val) = serde_json::to_string(&msg) {
                    let post = Uri::from("/iris/api/sign_message");
                    actions.push(Action::Post(post, val.into()));
                    if let Some(action) = msg_user_action(uri, &msg.name) {
                        actions.push(action);
                    }
                }
                actions
            }
        }
    }
}

/// Create a msg_user patch action
fn msg_user_action(uri: Uri, msg_name: &str) -> Option<Action> {
    match serde_json::to_string(&MsgUser { msg_user: msg_name }) {
        Ok(val) => Some(Action::Patch(uri, val.into())),
        Err(e) => {
            console::log_1(&format!("err: {e:?}").into());
            None
        }
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

    /// Check if DMS has a given hashtag
    fn has_hashtag(&self, hashtag: &str) -> bool {
        match &self.hashtags {
            Some(hashtags) => {
                hashtags.split(' ').any(|h| hashtag.eq_ignore_ascii_case(h))
            }
            None => false,
        }
    }

    /// Get one dedicated hashtag, if defined
    fn dedicated(&self) -> Option<&str> {
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
        let state = anc.dev.item_state(self);
        let mut states = match state {
            ItemState::Inactive => return ItemState::Inactive.into(),
            ItemState::Available => anc.msg_states(self.msg_current.as_deref()),
            ItemState::Offline => ItemStates::default()
                .with(ItemState::Offline, "FIXME: since fail time"),
            _ => state.into(),
        };
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
        let mut html =
            format!("<div class='{NAME} end'>{name} {item_states}</div>");
        if let Some(msg_current) = &self.msg_current {
            html.push_str("<img class='message' src='/iris/img/");
            html.push_str(msg_current);
            html.push_str(".gif'>");
        }
        html
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &DmsAnc, config: bool) -> String {
        let location = HtmlStr::new(&self.location).with_len(64);
        let mut status = format!("<div class='info fill'>{location}</div>");
        if let Some(msg_current) = &self.msg_current {
            status.push_str("<img class='message' src='/iris/img/");
            status.push_str(msg_current);
            status.push_str(".gif'>");
        }
        status.push_str("<div class='end'>");
        status.push_str(&self.item_states(anc).to_html());
        status.push_str("</div>");
        if let Some(pats) = &self.compose_patterns(anc) {
            status.push_str(pats);
        }
        if config {
            status.push_str("<div class='row'>");
            status.push_str(&anc.dev.controller_button());
            status.push_str(LOC_BUTTON);
            status.push_str(EDIT_BUTTON);
            status.push_str("</div>");
        }
        status
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
        let mut html = String::new();
        html.push_str("<div id='mc_grid'>");
        let pat_def = self.pattern_default(anc);
        let multi = pat_def.map(|pat| &pat.multi[..]).unwrap_or("");
        render_preview(&mut html, &sign, multi);
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
        html.push_str(&anc.make_lines(&sign, pat_def, self.current_multi(anc)));
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

    /// Convert to Edit HTML
    fn to_html_edit(&self) -> String {
        let controller = HtmlStr::new(&self.controller);
        let pin = OptVal(self.pin);
        format!(
            "<div class='row'>\
              <label for='controller'>Controller</label>\
              <input id='controller' maxlength='20' size='20' \
                     value='{controller}'>\
            </div>\
            <div class='row'>\
              <label for='pin'>Pin</label>\
              <input id='pin' type='number' min='1' max='104' \
                     size='8' value='{pin}'>\
            </div>"
        )
    }

    /// Make an ntcip sign
    fn make_sign(&self, anc: &DmsAnc) -> Option<Sign> {
        let cfg = anc.sign_config(self.sign_config.as_deref())?;
        match ntcip::dms::Dms::builder()
            .with_font_definition(anc.fonts.clone())
            .with_graphic_definition(anc.graphics.clone())
            .with_sign_cfg(cfg.sign_cfg())
            .with_vms_cfg(cfg.vms_cfg())
            .with_multi_cfg(cfg.multi_cfg())
            .build()
        {
            Ok(sign) => Some(sign),
            Err(e) => {
                console::log_1(&format!("make_sign: {e:?}").into());
                None
            }
        }
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
        while let Some(line) = doc.try_elem::<HtmlSelectElement>(&format!(
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
        let multi = MessagePattern::new(&sign, &pat.multi)
            .fill(lines.iter().map(|l| &l[..]));
        Some(multi_normalize(&multi))
    }

    /// Create actions to handle click on "Send" button
    fn send_actions(&self, anc: DmsAnc) -> Vec<Action> {
        if let Some(cfg) = &self.sign_config {
            if let Some(ms) = &self.selected_multi(&anc) {
                match sign_msg_owner(HIGH_1) {
                    Some(owner) => {
                        return anc.sign_msg_actions(
                            Dms::uri_name(&self.name),
                            SignMessage::new(cfg, ms, owner, HIGH_1),
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
                Dms::uri_name(&self.name),
                SignMessage::new(cfg, "", owner, LOW_1),
            ),
            _ => Vec::new(),
        }
    }
}

impl Device for Dms {
    /// Get controller
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Card for Dms {
    type Ancillary = DmsAnc;

    /// Display name
    const DNAME: &'static str = "⬛ Dms";

    /// All item states as html options
    const ITEM_STATES: &'static str = "<option value=''>all ↴\
         <option value='🔹'>🔹 available\
         <option value='🔶'>🔶 deployed\
         <option value='🗓️'>🗓️ planned\
         <option value='👽'>👽 external\
         <option value='🎯'>🎯 dedicated\
         <option value='⚠️'>⚠️ fault\
         <option value='🔌'>🔌 offline\
         <option value='▪️'>▪️ inactive";

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

    /// Get geo location name
    fn geo_loc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
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
            || self.has_hashtag(search)
            || self.item_states(anc).is_match(search)
            || anc
                .sign_message(self.msg_current.as_deref())
                .is_some_and(|m| m.is_match(search))
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &DmsAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Status(config) => self.to_html_status(anc, config),
            View::Edit => self.to_html_edit(),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Edit form
    fn changed_fields(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }

    /// Handle click event for a button on the card
    fn handle_click(&self, anc: DmsAnc, id: String) -> Vec<Action> {
        if &id == "mc_send" {
            self.send_actions(anc)
        } else if &id == "mc_blank" {
            self.blank_actions(anc)
        } else {
            Vec::new()
        }
    }

    /// Handle input event for an element on the card
    fn handle_input(&self, anc: DmsAnc, id: String) {
        let Some(pat) = self.selected_pattern(&anc) else {
            return;
        };
        let Some(sign) = self.make_sign(&anc) else {
            return;
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
        let multi = MessagePattern::new(&sign, &pat.multi)
            .fill(lines.iter().map(|l| &l[..]));
        let multi = multi_normalize(&multi);
        // update mc_preview image element
        let mut html = String::new();
        render_preview(&mut html, &sign, &multi);
        let preview = Doc::get().elem::<HtmlElement>("mc_preview");
        preview.set_outer_html(&html);
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

/// Render sign preview image
fn render_preview(html: &mut String, sign: &Sign, multi: &str) {
    html.push_str("<img id='mc_preview' width='240' height='80' ");
    let mut buf = Vec::with_capacity(4096);
    match rendzina::render(&mut buf, sign, multi, Some(240), Some(80)) {
        Ok(()) => {
            html.push_str("src='data:image/gif;base64,");
            b64enc.encode_string(buf, html);
            html.push_str("'/>");
        }
        Err(e) => {
            console::log_1(&format!("render_preview: {e:?}").into());
            html.push_str("src=''/>");
        }
    }
}
