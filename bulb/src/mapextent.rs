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
use crate::util::{ContainsLower, Fields, Input, opt_ref};
use crate::view::View;
use hatmil::{Tree, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;

/// Map extent
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct MapExtent {
    pub name: String,
    pub hashtag: Option<String>,
    pub lon: f64,
    pub lat: f64,
    pub zoom: u16,
}

/// MapExtent ancillary data
#[derive(Debug, Default)]
pub struct MapExtentAnc;

impl AncillaryData for MapExtentAnc {
    type Primary = MapExtent;

    /// Construct ancillary map extent data
    fn new(_pri: &MapExtent, _view: View) -> Self {
        MapExtentAnc
    }
}

impl MapExtent {
    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(opt_ref(&self.hashtag));
        String::from(tree)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, edit: bool) -> String {
        let mut tree = Tree::new();
        self.title(View::Setup(edit), &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("hashtag").cdata("Hashtag").close();
        div.input()
            .id("hashtag")
            .maxlength(16)
            .size(16)
            .value(opt_ref(&self.hashtag));
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("lon").cdata("Longitude").close();
        div.input()
            .id("lon")
            .r#type("number")
            .step("any")
            .min("-180")
            .min("180")
            .value(self.lon);
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("lat").cdata("Latitude").close();
        div.input()
            .id("lat")
            .r#type("number")
            .step("any")
            .min("-90")
            .min("90")
            .value(self.lat);
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("zoom").cdata("Zoom").close();
        div.input()
            .id("zoom")
            .r#type("number")
            .step("1")
            .min("1")
            .max("18")
            .value(self.zoom);
        div.close();
        footer_html(View::Setup(edit), true, &mut tree.root::<html::Div>());
        String::from(tree)
    }
}

impl Card for MapExtent {
    type Ancillary = MapExtentAnc;

    /// Get the resource
    fn res() -> Res {
        Res::MapExtent
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
    fn is_match(&self, search: &str, _anc: &MapExtentAnc) -> bool {
        self.name.contains_lower(search) || self.hashtag.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &MapExtentAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Setup(edit) => self.to_html_setup(edit),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("hashtag", &self.hashtag);
        fields.changed_input("lon", self.lon);
        fields.changed_input("lat", self.lat);
        fields.changed_input("zoom", self.zoom);
        fields.into_value().to_string()
    }
}
