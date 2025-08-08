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
use crate::item::ItemState;
use crate::util::{ContainsLower, Fields, Input};
use hatmil::Html;
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;

/// Role
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Role {
    pub name: String,
    pub enabled: bool,
    // secondary attributes
    pub domains: Option<Vec<String>>,
}

/// Ancillary role data
#[derive(Debug, Default)]
pub struct RoleAnc;

impl AncillaryData for RoleAnc {
    type Primary = Role;

    /// Construct ancillary role data
    fn new(_pri: &Role, _view: View) -> Self {
        RoleAnc
    }
}

impl Role {
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
        let mut html = Html::new();
        html.div()
            .class("title row")
            .text(self.name())
            .text(" ")
            .text(self.item_state().to_string());
        html.to_string()
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self) -> String {
        let mut html = self.title(View::Setup);
        html.div().class("row");
        html.label().for_("enabled").text("Enabled").end();
        let input = html.input().id("enabled").type_("checkbox");
        if self.enabled {
            input.checked();
        }
        html.end(); /* div */
        self.footer_html(true, &mut html);
        html.to_string()
    }
}

impl Card for Role {
    type Ancillary = RoleAnc;

    /// Display name
    const DNAME: &'static str = "💪 Role";

    /// Get the resource
    fn res() -> Res {
        Res::Role
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
    fn is_match(&self, search: &str, _anc: &RoleAnc) -> bool {
        self.name.contains_lower(search) || self.item_state().is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &RoleAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Setup => self.to_html_setup(),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("enabled", self.enabled);
        fields.into_value().to_string()
    }
}
