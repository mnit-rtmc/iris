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
use crate::util::{ContainsLower, Fields, Input, Select, opt_ref, opt_str};
use hatmil::Html;
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use std::fmt;
use wasm_bindgen::JsValue;

/// LCS indications
#[derive(Clone, Debug, Deserialize)]
pub struct LcsIndication {
    pub id: u32,
    pub description: String,
}

/// LCS State
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct LcsState {
    pub name: String,
    pub controller: Option<String>,
    pub lcs: String,
    pub lane: u16,
    pub indication: u32,
    // secondary attributes
    pub pin: Option<u32>,
    pub msg_pattern: Option<String>,
    pub msg_num: Option<u32>,
}

/// Ancillary LCS indication data
#[derive(Debug)]
pub struct LcsStateAnc {
    cio: ControllerIoAnc<LcsState>,
    pub indications: Vec<LcsIndication>,
}

impl fmt::Display for LcsIndication {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{} {}", self.symbol(), self.description)
    }
}

impl LcsIndication {
    fn symbol(&self) -> char {
        // FIXME: these should be in the LUT
        match self.id {
            1 => '⍽',
            2 => '↓',
            3 => '⇣',
            4 => '✕',
            5 => '✖',
            6 => '》',
            7 => '《',
            8 => '⤷',
            9 => '⤶',
            10 => '◊',
            11 => 'A',
            12 => 'L',
            _ => '?',
        }
    }
}

impl LcsStateAnc {
    /// Get indication description
    fn indication(&self, pri: &LcsState) -> LcsIndication {
        for indication in &self.indications {
            if pri.indication == indication.id {
                return indication.clone();
            }
        }
        LcsIndication {
            id: 0,
            description: "Unknown".to_string(),
        }
    }

    /// Build indications HTML
    fn indications_html(&self, pri: &LcsState, html: &mut Html) {
        html.select().id("indication");
        for ind in &self.indications {
            let option = html.option().value(ind.id);
            if ind.id == pri.indication {
                option.attr_bool("selected");
            }
            html.text(ind.to_string()).end();
        }
        html.end(); /* select */
    }
}

impl AncillaryData for LcsStateAnc {
    type Primary = LcsState;

    /// Construct ancillary LCS state data
    fn new(pri: &LcsState, view: View) -> Self {
        let mut cio = ControllerIoAnc::new(pri, view);
        cio.assets.push(Asset::LcsIndications);
        LcsStateAnc {
            cio,
            indications: Vec::new(),
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.cio.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &LcsState,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::LcsIndications => {
                self.indications = serde_wasm_bindgen::from_value(value)?;
            }
            _ => self.cio.set_asset(pri, asset, value)?,
        }
        Ok(())
    }
}

impl ControllerIo for LcsState {
    /// Get controller name
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl LcsState {
    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &LcsStateAnc) -> String {
        let mut html = Html::new();
        html.div()
            .class("title row")
            .text(self.name())
            .text(" ")
            .text(anc.cio.item_states(self).to_string())
            .end();
        html.div().class("info row").text(&self.lcs);
        html.span().text(self.lane).end();
        html.span()
            .text(anc.indication(self).symbol())
            .end();
        html.to_string()
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &LcsStateAnc) -> String {
        let mut html = self.title(View::Setup);
        html.div().class("row");
        html.label().text("LCS").end();
        html.span().text(&self.lcs).end();
        html.end(); /* div */
        anc.cio.controller_html(self, &mut html);
        anc.cio.pin_html(self.pin, &mut html);
        html.div().class("row");
        html.label().r#for("lane").text("Lane").end();
        html.input()
            .id("lane")
            .r#type("number")
            .min(1)
            .max(9)
            .size(2)
            .value(self.lane);
        html.end(); /* div */
        html.div().class("row");
        html.label().r#for("indication").text("Indication").end();
        anc.indications_html(self, &mut html);
        html.end(); /* div */
        html.div().class("row");
        html.label().r#for("msg_pattern").text("Msg Pattern").end();
        html.input()
            .id("msg_pattern")
            .maxlength(20)
            .size(20)
            .value(opt_ref(&self.msg_pattern));
        html.end(); /* div */
        html.div().class("row");
        html.label().r#for("msg_num").text("Msg #").end();
        html.input()
            .id("msg_num")
            .r#type("number")
            .min(2)
            .max(65535)
            .size(5)
            .value(opt_str(self.msg_num));
        html.end(); /* div */
        self.footer_html(true, &mut html);
        html.to_string()
    }
}

impl Card for LcsState {
    type Ancillary = LcsStateAnc;

    /// Display name
    const DNAME: &'static str = "🠟 LCS State";

    /// Get the resource
    fn res() -> Res {
        Res::LcsState
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

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &LcsStateAnc) -> bool {
        self.name.contains_lower(search)
            || anc.cio.item_states(self).is_match(search)
            || self.lcs.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &LcsStateAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.changed_input("lane", self.lane);
        fields.changed_select("indication", self.indication);
        fields.changed_input("msg_pattern", &self.msg_pattern);
        fields.changed_select("msg_num", self.msg_num);
        fields.into_value().to_string()
    }
}
