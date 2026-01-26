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
use crate::card::{AncillaryData, Card, View};
use crate::item::{ItemState, ItemStates};
use crate::util::{ContainsLower, Fields, Input, opt_ref, opt_str};
use hatmil::{Page, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;

/// Modem
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Modem {
    pub name: String,
    pub uri: Option<String>,
    pub config: Option<String>,
    pub timeout_ms: Option<u32>,
    pub enabled: bool,
}

/// Modem ancillary data
#[derive(Debug, Default)]
pub struct ModemAnc;

impl AncillaryData for ModemAnc {
    type Primary = Modem;

    /// Construct ancillary modem data
    fn new(_pri: &Modem, _view: View) -> Self {
        ModemAnc
    }
}

impl Modem {
    /// Get item states
    fn item_states(&self) -> ItemStates<'_> {
        if self.enabled {
            ItemState::Available.into()
        } else {
            ItemState::Inactive.into()
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(self.item_states().to_string());
        String::from(page)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self) -> String {
        let mut page = Page::new();
        self.title(View::Setup, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("uri").cdata("URI").close();
        div.input()
            .id("uri")
            .maxlength(64)
            .size(30)
            .value(opt_ref(&self.uri));
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("config").cdata("Config").close();
        div.input()
            .id("config")
            .maxlength(64)
            .size(28)
            .value(opt_ref(&self.config));
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("timeout_ms")
            .cdata("Timeout (ms)")
            .close();
        div.input()
            .id("timeout_ms")
            .r#type("number")
            .min(0)
            .max(90000)
            .size(8)
            .value(opt_str(self.timeout_ms));
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("enabled").cdata("Enabled").close();
        let mut input = div.input();
        input.id("enabled").r#type("checkbox");
        if self.enabled {
            input.checked();
        }
        String::from(page)
    }
}

impl Card for Modem {
    type Ancillary = ModemAnc;

    /// Display name
    const DNAME: &'static str = "🖀 Modem";

    /// Get the resource
    fn res() -> Res {
        Res::Modem
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
    fn is_match(&self, search: &str, _anc: &ModemAnc) -> bool {
        self.name.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &ModemAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Setup => self.to_html_setup(),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("uri", &self.uri);
        fields.changed_input("config", &self.config);
        fields.changed_input("timeout_ms", self.timeout_ms);
        fields.changed_input("enabled", self.enabled);
        fields.into_value().to_string()
    }
}
