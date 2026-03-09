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
use crate::card::{AncillaryData, Card};
use crate::item::ItemState;
use crate::util::{ContainsLower, Fields, Input, opt_ref};
use crate::view::View;
use hatmil::{Page, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;

/// Word
#[derive(Clone, Debug, Default, Deserialize, PartialEq)]
pub struct Word {
    pub name: String,
    pub allowed: bool,
    pub abbr: Option<String>,
}

/// Ancillary Word data
#[derive(Debug, Default)]
pub struct WordAnc;

impl AncillaryData for WordAnc {
    type Primary = Word;

    /// Construct ancillary word data
    fn new(_pri: &Word, _view: View) -> Self {
        WordAnc
    }
}

impl Word {
    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.cdata(self.name());
        if self.allowed {
            if let Some(abbr) = &self.abbr
                && !abbr.is_empty()
            {
                div.cdata(" ➡ ");
                div.cdata(opt_ref(&self.abbr));
            }
        } else {
            div.cdata(" 🚫");
        }
        String::from(page)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self) -> String {
        let mut page = Page::new();
        self.title(View::Setup, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("allowed").cdata("Allowed").close();
        let mut input = div.input();
        input.id("allowed").r#type("checkbox");
        if self.allowed {
            input.checked();
        }
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("abbr").cdata("Abbreviation").close();
        let mut input = div.input();
        input
            .id("abbr")
            .maxlength(12)
            .size(12)
            .value(opt_ref(&self.abbr));
        div.close();
        self.footer_html(true, &mut page.frag::<html::Div>());
        String::from(page)
    }
}

impl Card for Word {
    type Ancillary = WordAnc;

    /// Get the resource
    fn res() -> Res {
        Res::Word
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[ItemState::Available, ItemState::Prohibited]
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

    /// Get the main item state
    fn item_state_main(&self, _anc: &Self::Ancillary) -> ItemState {
        if self.allowed {
            ItemState::Available
        } else {
            ItemState::Prohibited
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &WordAnc) -> bool {
        self.name.contains_lower(search)
            || self.item_state_main(anc).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &WordAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Setup => self.to_html_setup(),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("allowed", self.allowed);
        fields.changed_input("abbr", &self.abbr);
        fields.into_value().to_string()
    }
}
