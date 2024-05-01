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
use crate::card::{inactive_attr, AncillaryData, Card, View};
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal};
use resources::Res;
use serde::{Deserialize, Serialize};
use std::borrow::Cow;

/// Modem
#[derive(Debug, Default, Deserialize, Serialize, PartialEq)]
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
}

impl Modem {
    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let name = HtmlStr::new(self.name());
        let inactive = inactive_attr(self.enabled);
        format!("<div class='{inactive}'>{name}</div>")
    }

    /// Convert to Edit HTML
    fn to_html_edit(&self) -> String {
        let uri = HtmlStr::new(&self.uri);
        let config = HtmlStr::new(&self.config);
        let timeout_ms = OptVal(self.timeout_ms);
        let enabled = if self.enabled { " checked" } else { "" };
        format!(
            "<div class='row'>\
              <label for='uri'>URI</label>\
              <input id='uri' maxlength='64' size='30' value='{uri}'>\
            </div>\
            <div class='row'>\
              <label for='config'>Config</label>\
              <input id='config' maxlength='64' size='28' value='{config}'>\
            </div>\
            <div class='row'>\
              <label for='timeout_ms'>Timeout (ms)</label>\
              <input id='timeout_ms' type='number' min='0' size='8' \
                     max='90000' value='{timeout_ms}'>\
            </div>\
            <div class='row'>\
              <label for='enabled'>Enabled</label>\
              <input id='enabled' type='checkbox'{enabled}>\
            </div>"
        )
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
            View::Edit => self.to_html_edit(),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Edit form
    fn changed_fields(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("uri", &self.uri);
        fields.changed_input("config", &self.config);
        fields.changed_input("timeout_ms", self.timeout_ms);
        fields.changed_input("enabled", self.enabled);
        fields.into_value().to_string()
    }
}
