// Copyright (C) 2026  Minnesota Department of Transportation
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
use crate::card::{AncillaryData, Card, footer_html};
use crate::item::ItemState;
use crate::util::{ContainsLower, Fields, Input};
use crate::view::View;
use hatmil::{Tree, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;

/// Event configuration
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct EventConfig {
    pub name: String,
    pub enable_store: bool,
    pub enable_purge: bool,
    pub purge_days: u32,
}

/// Ancillary event configuration data
#[derive(Debug, Default)]
pub struct EventConfigAnc;

impl AncillaryData for EventConfigAnc {
    type Primary = EventConfig;

    /// Construct ancillary event configuration data
    fn new(_pri: &EventConfig, _view: View) -> Self {
        EventConfigAnc
    }
}

impl EventConfig {
    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.cdata(self.name());
        String::from(tree)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, edit: bool) -> String {
        let mut tree = Tree::new();
        self.title(View::Setup(edit), &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("store").cdata("Enable Store").close();
        let mut input = div.input();
        input.id("store").r#type("checkbox");
        if self.enable_store {
            input.checked();
        }
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("purge").cdata("Enable Purge").close();
        let mut input = div.input();
        input.id("purge").r#type("checkbox");
        if self.enable_purge {
            input.checked();
        }
        div.close();
        // FIXME: add purge days
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("purge_days").cdata("Purge Days").close();
        div.input()
            .id("purge_days")
            .r#type("number")
            .min(0)
            .max(9999)
            .size(4)
            .value(self.purge_days);
        div.close();
        footer_html(View::Setup(edit), false, &mut tree.root::<html::Div>());
        String::from(tree)
    }
}

impl Card for EventConfig {
    type Ancillary = EventConfigAnc;

    /// Get the resource
    fn res() -> Res {
        Res::EventConfig
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[]
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
    fn is_match(&self, search: &str, _anc: &EventConfigAnc) -> bool {
        self.name.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &EventConfigAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Setup(edit) => self.to_html_setup(edit),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("store", self.enable_store);
        fields.changed_input("purge", self.enable_purge);
        fields.changed_input("purge_days", self.purge_days);
        fields.into_value().to_string()
    }
}
