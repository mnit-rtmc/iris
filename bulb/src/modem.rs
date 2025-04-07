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
use crate::card::{AncillaryData, Card, View};
use crate::item::{ItemState, ItemStates};
use crate::util::{ContainsLower, Fields, Input, opt_ref, opt_str};
use hatmil::Html;
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
    fn item_states(&self) -> ItemStates {
        if self.enabled {
            ItemState::Available.into()
        } else {
            ItemState::Inactive.into()
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let mut html = Html::new();
        html.div()
            .class("title row")
            .text(self.name())
            .text(" ")
            .text(self.item_states().to_string());
        html.into()
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self) -> String {
        let mut html = self.title(View::Setup);
        html.div().class("row");
        html.label().for_("uri").text("URI").end();
        html.input()
            .id("uri")
            .maxlength("64")
            .size("30")
            .value(opt_ref(&self.uri));
        html.end(); /* div */
        html.div().class("row");
        html.label().for_("config").text("Config").end();
        html.input()
            .id("config")
            .maxlength("64")
            .size("28")
            .value(opt_ref(&self.config));
        html.end(); /* div */
        html.div().class("row");
        html.label().for_("timeout_ms").text("Timeout (ms)").end();
        html.input()
            .id("timeout_ms")
            .type_("number")
            .min("0")
            .max("90000")
            .size("8")
            .value(opt_str(self.timeout_ms));
        html.end(); /* div */
        html.div().class("row");
        html.label().for_("enabled").text("Enabled").end();
        let enabled = html.input().id("enabled").type_("checkbox");
        if self.enabled {
            enabled.attr_bool("checked");
        }
        html.into()
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
    fn name(&self) -> Cow<str> {
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
