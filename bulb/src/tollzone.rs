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
use crate::util::{ContainsLower, Fields, Input, opt_ref, opt_str};
use crate::view::View;
use hatmil::{Tree, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;

/// Toll Zone
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct TollZone {
    pub name: String,
    pub tollway: Option<String>,
    pub start_id: Option<String>,
    pub end_id: Option<String>,
    // secondary attributes
    pub alpha: Option<f32>,
    pub beta: Option<f32>,
    pub max_price: Option<f32>,
}

/// Toll Zone ancillary data
#[derive(Debug, Default)]
pub struct TollZoneAnc;

impl AncillaryData for TollZoneAnc {
    type Primary = TollZone;

    /// Construct ancillary toll zone data
    fn new(_pri: &TollZone, _view: View) -> Self {
        TollZoneAnc
    }
}

impl TollZone {
    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &TollZoneAnc) -> String {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(self.item_state_main(anc).to_string())
            .span()
            .class("info")
            .cdata(opt_ref(&self.tollway));
        String::from(tree)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, edit: bool) -> String {
        let mut tree = Tree::new();
        self.title(View::Setup(edit), &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("tollway").cdata("Tollway").close();
        div.input()
            .id("tollway")
            .maxlength(16)
            .size(16)
            .value(opt_ref(&self.tollway));
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label()
            .r#for("start_id")
            .cdata("Start ID (station)")
            .close();
        div.input()
            .id("start_id")
            .maxlength(10)
            .size(10)
            .value(opt_ref(&self.start_id));
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label()
            .r#for("end_id")
            .cdata("End ID (station)")
            .close();
        div.input()
            .id("end_id")
            .maxlength(10)
            .size(10)
            .value(opt_ref(&self.end_id));
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("alpha").cdata("Alpha").close();
        div.input()
            .id("alpha")
            .r#type("number")
            .step("0.00001")
            .inputmode("decimal")
            .value(opt_str(self.alpha));
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("beta").cdata("Beta").close();
        div.input()
            .id("beta")
            .r#type("number")
            .step("0.00001")
            .inputmode("decimal")
            .value(opt_str(self.beta));
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("max_price").cdata("Max Price").close();
        div.input()
            .id("max_price")
            .r#type("number")
            .step("0.25")
            .inputmode("decimal")
            .value(opt_str(self.max_price));
        div.close();
        footer_html(View::Setup(edit), true, &mut tree.root::<html::Div>());
        String::from(tree)
    }
}

impl Card for TollZone {
    type Ancillary = TollZoneAnc;

    /// Get the resource
    fn res() -> Res {
        Res::TollZone
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
        if self.start_id.is_some() && self.end_id.is_some() {
            ItemState::Available
        } else {
            ItemState::Inactive
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &TollZoneAnc) -> bool {
        self.name.contains_lower(search)
            || self.tollway.contains_lower(search)
            || self.item_state_main(anc).is_match(search)
            || self.start_id.contains_lower(search)
            || self.end_id.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &TollZoneAnc) -> String {
        match view {
            View::Create => self.to_html_create(20),
            View::Setup(edit) => self.to_html_setup(edit),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("tollway", &self.tollway);
        fields.changed_input("start_id", &self.start_id);
        fields.changed_input("end_id", &self.end_id);
        fields.changed_input("alpha", self.alpha);
        fields.changed_input("beta", self.beta);
        fields.changed_input("max_price", self.max_price);
        fields.into_value().to_string()
    }
}
