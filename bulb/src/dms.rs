// Copyright (C) 2022-2023  Minnesota Department of Transportation
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
use crate::fetch::{ContentType, Uri};
use crate::item::{ItemState, ItemStates};
use crate::resource::{
    AncillaryData, Card, View, EDIT_BUTTON, LOC_BUTTON, NAME,
};
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal};
use base64::{engine::general_purpose::STANDARD_NO_PAD as b64enc, Engine as _};
use ntcip::dms::multi::join_text;
use ntcip::dms::{ifnt, FontTable, GraphicTable};
use rendzina::{load_graphic, SignConfig};
use serde::{Deserialize, Serialize};
use std::fmt;
use wasm_bindgen::JsValue;

/// Send button
const SEND_BUTTON: &str = "<button id='mc_send' type='button'>Send</button>";

/// Blank button
const BLANK_BUTTON: &str = "<button id='mc_blank' type='button'>Blank</button>";

/// Photocell status
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct Photocell {
    description: String,
    error: Option<String>,
    reading: Option<i32>,
}

/// Power supply status
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct PowerSupply {
    description: String,
    supply_type: String,
    error: Option<String>,
    detail: String,
    voltage: Option<f32>,
}

/// Sign status
#[derive(Debug, Default, Deserialize, Serialize)]
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
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct StuckPixels {
    off: Option<String>,
    on: Option<String>,
}

/// Dms
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct Dms {
    pub name: String,
    pub location: Option<String>,
    pub controller: Option<String>,
    pub notes: Option<String>,
    pub hashtags: Option<String>,
    pub msg_current: Option<String>,
    pub has_faults: Option<bool>,
    // full attributes
    pub pin: Option<u32>,
    pub sign_config: Option<String>,
    pub sign_detail: Option<String>,
    pub msg_sched: Option<String>,
    pub msg_user: Option<String>,
    pub geo_loc: Option<String>,
    pub status: Option<SignStatus>,
    pub stuck_pixels: Option<StuckPixels>,
}

/// Sign Message
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct SignMessage {
    pub name: String,
    pub sign_config: String,
    pub incident: Option<String>,
    pub multi: String,
    pub msg_owner: String,
    pub flash_beacon: bool,
    pub msg_priority: u32,
    pub duration: Option<u32>,
}

/// Message Pattern
#[derive(Debug, Default, Deserialize, Serialize)]
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
#[derive(Debug, Default, Deserialize, Serialize)]
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
    fonts: FontTable<24>,
    gnames: Vec<GraphicName>,
    graphics: GraphicTable<32>,
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
                uris.push(
                    Uri::from(format!("/iris/api/ifnt/{}.ifnt", fname.name))
                        .with_content_type(ContentType::Text),
                );
            }
            for gname in &self.gnames {
                uris.push(
                    Uri::from(format!("/iris/api/gif/{}.gif", gname.name))
                        .with_content_type(ContentType::Gif),
                );
            }
            return Box::new(uris.into_iter());
        }
        if let View::Compact | View::Search = view {
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
                if uri.as_str().ends_with(".ifnt") {
                    let font: String = serde_wasm_bindgen::from_value(data)?;
                    let font = ifnt::read(font.as_bytes())?;
                    if let Some(f) = self.fonts.lookup_mut(font.number) {
                        *f = font;
                    } else if let Some(f) = self.fonts.lookup_mut(0) {
                        *f = font;
                    }
                } else if uri.as_str().ends_with(".gif") {
                    let graphic: Vec<u8> =
                        serde_wasm_bindgen::from_value(data)?;
                    if let Ok(number) = uri
                        .as_str()
                        .replace(|c: char| !c.is_numeric(), "")
                        .parse::<u8>()
                    {
                        let graphic = load_graphic(&graphic[..], number)?;
                        if let Some(g) =
                            self.graphics.lookup_mut(graphic.number)
                        {
                            *g = graphic;
                        } else if let Some(g) = self.graphics.lookup_mut(0) {
                            *g = graphic;
                        }
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
    /// Get message owner
    fn owner(&self) -> &str {
        &self.msg_owner
    }

    /// Get "system" owner
    fn system(&self) -> &str {
        self.owner().split(';').next().unwrap_or("")
    }

    /// Get "sources" owner
    fn sources(&self) -> &str {
        self.owner().split(';').nth(1).unwrap_or("")
    }

    /// Get "user" owner
    fn user(&self) -> &str {
        self.owner().split(';').nth(2).unwrap_or("")
    }

    /// Get item states
    fn item_states(&self) -> ItemStates {
        let sources = self.sources();
        let mut states = ItemStates::default();
        if sources.contains("blank") {
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
        if sources.is_empty() {
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

    /// Make a line select element
    fn make_line(&self, ln: u16) -> Option<String> {
        if self.lines.iter().any(|l| l.line == ln) {
            let mut html = String::new();
            html.push_str(&format!("<select id='mc_line{ln}'>"));
            html.push_str("<option></option>");
            for l in &self.lines {
                if ln == l.line {
                    let multi = &l.multi;
                    html.push_str("<option value='");
                    html.push_str(multi);
                    html.push_str("'>");
                    html.push_str(&join_text(multi, " "));
                    html.push_str("</option>");
                }
            }
            html.push_str("</select>");
            Some(html)
        } else {
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
    pub const RESOURCE_N: &'static str = "dms";

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
            // full attribute doesn't match minimal has_faults?!
            Some("has_faults")
        } else {
            None
        }
    }

    /// Get all item states as html options
    pub fn item_state_options() -> &'static str {
        "<option value=''>all ↴</option>\
         <option value='🔹'>🔹 available</option>\
         <option value='🔶'>🔶 deployed</option>\
         <option value='🕗'>🕗 planned</option>\
         <option value='👽'>👽 external</option>\
         <option value='🎯'>🎯 dedicated</option>\
         <option value='⚠️'>⚠️ fault</option>\
         <option value='🔌'>🔌 offline</option>\
         <option value='🔻'>🔻 disabled</option>"
    }

    /// Get item states
    fn item_states<'a>(&'a self, anc: &'a DmsAnc) -> ItemStates<'a> {
        let state = anc.dev.item_state(self);
        let mut states = match state {
            ItemState::Disabled => return ItemState::Disabled.into(),
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
        let item_states = self.item_states(anc);
        let mut html =
            format!("<div class='{NAME} end'>{self} {item_states}</div>");
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
            return None;
        }
        let Some(cfg) = anc.sign_config(self.sign_config.as_deref()) else {
            return None;
        };
        let dms = ntcip::dms::Dms::builder()
            .with_font_definition(anc.fonts.clone())
            .with_sign_cfg(cfg.sign_cfg())
            .with_vms_cfg(cfg.vms_cfg())
            .with_multi_cfg(cfg.multi_cfg())
            .build()
            .ok()?;
        let mut html = String::new();
        html.push_str("<div id='mc_grid'>");
        if let Some(pat) = anc.compose_patterns.first() {
            let mut buf = Vec::with_capacity(4096);
            rendzina::render(&mut buf, &dms, &pat.multi, Some(240), Some(80))
                .unwrap();
            html.push_str("<img id='mc_preview' src='data:image/gif;base64,");
            b64enc.encode_string(buf, &mut html);
            html.push_str("'/>");
        }
        html.push_str("<select id='mc_pattern'>");
        for pat in &anc.compose_patterns {
            html.push_str("<option>");
            html.push_str(&pat.name);
            html.push_str("</option>");
        }
        html.push_str("</select>");
        html.push_str("<div id='mc_lines' class='column'>");
        let mut ln = 1;
        while let Some(line) = anc.make_line(ln) {
            html.push_str(&line);
            ln += 1;
        }
        html.push_str("</div>");
        html.push_str(SEND_BUTTON);
        html.push_str(BLANK_BUTTON);
        html.push_str("</div>");
        Some(html)
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
}

impl fmt::Display for Dms {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
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
            View::Compact => self.to_html_compact(anc),
            View::Status(config) => self.to_html_status(anc, config),
            View::Edit => self.to_html_edit(),
            _ => unreachable!(),
        }
    }

    /// Get changed fields from Edit form
    fn changed_fields(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }
}
