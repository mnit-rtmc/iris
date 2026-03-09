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
use crate::card::{AncillaryData, Card};
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::error::Result;
use crate::util::{ContainsLower, Fields, Input, Select, opt_ref, opt_str};
use crate::view::View;
use hatmil::{Page, html};
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
    pub symbol: String,
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
        write!(f, "{} {}", self.symbol, self.description)
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
            symbol: "?".to_string(),
        }
    }

    /// Build indications HTML
    fn indications_html<'p>(
        &self,
        pri: &LcsState,
        select: &'p mut html::Select<'p>,
    ) {
        select.id("indication");
        for ind in &self.indications {
            let mut option = select.option();
            option.value(ind.id);
            if ind.id == pri.indication {
                option.selected();
            }
            option.cdata(ind.to_string()).close();
        }
        select.close();
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
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(anc.cio.item_states(self).to_string())
            .close();
        div = page.frag::<html::Div>();
        div.class("info row").cdata(&self.lcs);
        div.span().cdata(self.lane).close();
        div.span().cdata(anc.indication(self).symbol).close();
        String::from(page)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &LcsStateAnc) -> String {
        let mut page = Page::new();
        self.title(View::Setup, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.label().cdata("LCS").close();
        div.span().cdata(&self.lcs).close();
        div.close();
        anc.cio.controller_html(self, &mut page.frag::<html::Div>());
        anc.cio.pin_html(self.pin, &mut page.frag::<html::Div>());
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("lane").cdata("Lane").close();
        div.input()
            .id("lane")
            .r#type("number")
            .min(1)
            .max(9)
            .size(2)
            .value(self.lane);
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("indication").cdata("Indication").close();
        anc.indications_html(self, &mut div.select());
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("msg_pattern")
            .cdata("Msg Pattern")
            .close();
        div.input()
            .id("msg_pattern")
            .maxlength(20)
            .size(20)
            .value(opt_ref(&self.msg_pattern));
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("msg_num").cdata("Msg #").close();
        div.input()
            .id("msg_num")
            .r#type("number")
            .min(2)
            .max(65535)
            .size(5)
            .value(opt_str(self.msg_num));
        div.close();
        self.footer_html(true, &mut page.frag::<html::Div>());
        String::from(page)
    }
}

impl Card for LcsState {
    type Ancillary = LcsStateAnc;

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
