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
use crate::util::{ContainsLower, Fields, Input};
use crate::view::View;
use hatmil::{Tree, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;

/// Plan phase
#[derive(Clone, Debug, Default, Deserialize, PartialEq)]
pub struct PlanPhase {
    pub name: String,
    pub selectable: bool,
}

/// Ancillary PlanPhase data
#[derive(Debug, Default)]
pub struct PlanPhaseAnc;

impl AncillaryData for PlanPhaseAnc {
    type Primary = PlanPhase;

    /// Construct ancillary plan phase data
    fn new(_pri: &PlanPhase, _view: View) -> Self {
        PlanPhaseAnc
    }
}

impl PlanPhase {
    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.cdata(self.name());
        if self.selectable {
            div.cdata(" ✔️");
        } else {
            div.cdata(" 🚫");
        }
        String::from(tree)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self) -> String {
        let mut tree = Tree::new();
        self.title(View::Setup, &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("selectable").cdata("Selectable").close();
        let mut input = div.input();
        input.id("selectable").r#type("checkbox");
        if self.selectable {
            input.checked();
        }
        div.close();
        self.footer_html(true, &mut tree.root::<html::Div>());
        String::from(tree)
    }
}

impl Card for PlanPhase {
    type Ancillary = PlanPhaseAnc;

    /// Get the resource
    fn res() -> Res {
        Res::PlanPhase
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
        if self.selectable {
            ItemState::Available
        } else {
            ItemState::Prohibited
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &PlanPhaseAnc) -> bool {
        self.name.contains_lower(search)
            || self.item_state_main(anc).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &PlanPhaseAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Setup => self.to_html_setup(),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("selectable", self.selectable);
        fields.into_value().to_string()
    }
}
