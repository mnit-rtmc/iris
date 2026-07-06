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
use crate::item::{ItemState, ItemStates};
use crate::util::{ContainsLower, Fields, Input, opt_str};
use crate::view::View;
use hatmil::{Tree, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;

/// Message Line
#[derive(Debug, Default, Deserialize, PartialEq, Eq, PartialOrd, Ord)]
pub struct MsgLine {
    // NOTE: ordered to allow deriving PartialOrd / Ord
    pub line: u16,
    // NOTE: secondary
    pub rank: Option<u16>,
    pub multi: String,
    pub hashtag: String,
    pub name: String,
}

/// Ancillary event configuration data
#[derive(Debug, Default)]
pub struct MsgLineAnc;

impl AncillaryData for MsgLineAnc {
    type Primary = MsgLine;

    /// Construct ancillary message line data
    fn new(_pri: &MsgLine, _view: View) -> Self {
        MsgLineAnc
    }
}

impl MsgLine {
    /// Get item states
    fn item_states(&self) -> ItemStates<'static> {
        ItemState::Available.into()
    }

    /// Convert to compact HTML
    fn to_html_compact(&self, _anc: &MsgLineAnc) -> String {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.class("title row").cdata(&self.multi);
        String::from(tree)
    }

    /// Convert to setup HTML
    fn to_html_setup(&self, _anc: &MsgLineAnc, edit: bool) -> String {
        let mut tree = Tree::new();
        self.title(View::Setup(edit), &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row");
        div.span().class("info fill").cdata(&self.hashtag);
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("line").cdata("Ln").close();
        div.input()
            .id("line")
            .r#type("number")
            .min(1)
            .max(12)
            .size(2)
            .value(self.line);
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("rank").cdata("Rank").close();
        div.input()
            .id("rank")
            .r#type("number")
            .min(1)
            .max(99)
            .size(2)
            .value(opt_str(self.rank));
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("multi").cdata("MULTI").close();
        div.input()
            .id("multi")
            .class("multi")
            .autocorrect("off")
            .autocomplete("off")
            .spellcheck("false")
            .maxlength(64)
            .size(24)
            .value(&self.multi)
            .close();
        div.close();
        footer_html(View::Setup(edit), true, &mut tree.root::<html::Div>());
        String::from(tree)
    }
}

impl Card for MsgLine {
    type Ancillary = MsgLineAnc;

    /// Get the resource
    fn res() -> Res {
        Res::MsgLine
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[ItemState::Available]
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
        ItemState::Available
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, _anc: &MsgLineAnc) -> bool {
        self.name.contains_lower(search)
            || self.item_states().is_match(search)
            || self.hashtag.contains_lower(search)
            || self.multi.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &MsgLineAnc) -> String {
        match view {
            View::Setup(edit) => self.to_html_setup(anc, edit),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("line", self.line);
        fields.changed_input("rank", self.rank);
        fields.changed_input("multi", &self.multi);
        fields.into_value().to_string()
    }
}
