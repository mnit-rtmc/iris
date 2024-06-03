// Copyright (C) 2024  Minnesota Department of Transportation
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
use crate::card::{AncillaryData, Card, View, NAME};
use crate::item::ItemState;
use crate::util::{ContainsLower, Fields, HtmlStr, Input};
use resources::Res;
use serde::{Deserialize, Serialize};
use std::borrow::Cow;

/// Domain
#[derive(Debug, Default, Deserialize, Serialize, PartialEq)]
pub struct Domain {
    pub name: String,
    pub enabled: bool,
    // secondary
    pub cidr: Option<String>,
}

/// Ancillary domain data
#[derive(Debug, Default)]
pub struct DomainAnc;

impl AncillaryData for DomainAnc {
    type Primary = Domain;
}

impl Domain {
    /// Get item state
    pub fn item_state(&self) -> ItemState {
        if self.enabled {
            ItemState::Available
        } else {
            ItemState::Inactive
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let name = self.name();
        let item_state = self.item_state();
        format!("<div class='{NAME} end'>{name} {item_state}</div>")
    }

    /// Convert to Edit HTML
    fn to_html_edit(&self) -> String {
        let cidr = HtmlStr::new(&self.cidr);
        let enabled = if self.enabled { " checked" } else { "" };
        format!(
            "<div class='row'>\
               <label for='cidr'>CIDR</label>\
               <input id='cidr' maxlength='64' size='32' value='{cidr}'>\
            </div>\
            <div class='row'>\
              <label for='enabled'>Enabled</label>\
              <input id='enabled' type='checkbox'{enabled}>\
            </div>"
        )
    }
}

impl Card for Domain {
    type Ancillary = DomainAnc;

    /// Display name
    const DNAME: &'static str = "🖧 Domain";

    /// All item states as html options
    const ITEM_STATES: &'static str = "<option value=''>all ↴\
         <option value='🔹'>🔹 available\
         <option value='▪️'>▪️ inactive";

    /// Get the resource
    fn res() -> Res {
        Res::Domain
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
    fn is_match(&self, search: &str, _anc: &DomainAnc) -> bool {
        self.name.contains_lower(search) || self.item_state().is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &DomainAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Edit => self.to_html_edit(),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Edit form
    fn changed_fields(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("cidr", &self.cidr);
        fields.changed_input("enabled", self.enabled);
        fields.into_value().to_string()
    }
}
