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
use crate::util::ContainsLower;
use crate::view::View;
use hatmil::{Tree, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;

/// Day Plan
#[derive(Clone, Debug, Default, Deserialize, PartialEq)]
pub struct DayPlan {
    pub name: String,
    pub holidays: bool,
}

/// Ancillary DayPlan data
#[derive(Debug, Default)]
pub struct DayPlanAnc;

impl AncillaryData for DayPlanAnc {
    type Primary = DayPlan;

    /// Construct ancillary day plan data
    fn new(_pri: &DayPlan, _view: View) -> Self {
        DayPlanAnc
    }
}

impl DayPlan {
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
        div.label().r#for("holidays").cdata("Holidays").close();
        let mut input = div.input();
        input.id("holidays").r#type("checkbox").disabled();
        if self.holidays {
            input.checked();
        }
        div.close();
        footer_html(View::Setup(edit), true, &mut tree.root::<html::Div>());
        String::from(tree)
    }
}

impl Card for DayPlan {
    type Ancillary = DayPlanAnc;

    /// Get the resource
    fn res() -> Res {
        Res::DayPlan
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[ItemState::Planned]
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
        ItemState::Planned
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &DayPlanAnc) -> bool {
        self.name.contains_lower(search)
            || self.item_state_main(anc).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &DayPlanAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Setup(edit) => self.to_html_setup(edit),
            _ => self.to_html_compact(),
        }
    }
}
