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
use crate::asset::Asset;
use crate::card::{AncillaryData, Card};
use crate::error::Result;
use crate::item::ItemState;
use crate::util::{ContainsLower, Fields, Input};
use crate::view::View;
use hatmil::{Tree, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// Roadway directions
#[derive(Debug, Deserialize, PartialEq)]
pub struct Direction {
    pub id: u16,
    pub direction: String,
    pub dir: String,
}

/// Roadway class
#[derive(Debug, Deserialize, PartialEq)]
pub struct RoadClass {
    pub id: u16,
    pub description: String,
    pub grade: String,
    pub scale: f32,
}

/// Road definition
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Road {
    pub name: String,
    pub abbrev: String,
    pub r_class: u16,
    pub direction: u16,
}

/// Road ancillary data
#[derive(Debug, Default)]
pub struct RoadAnc {
    assets: Vec<Asset>,
    directions: Vec<Direction>,
    road_classes: Vec<RoadClass>,
}

impl AncillaryData for RoadAnc {
    type Primary = Road;

    /// Construct ancillary road data
    fn new(_pri: &Road, _view: View) -> Self {
        let assets = vec![Asset::Directions, Asset::RoadClasses];
        let directions = Vec::new();
        let road_classes = Vec::new();
        RoadAnc {
            assets,
            directions,
            road_classes,
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        _pri: &Road,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::Directions => {
                self.directions = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::RoadClasses => {
                self.road_classes = serde_wasm_bindgen::from_value(value)?;
            }
            _ => unreachable!(),
        }
        Ok(())
    }
}

impl Direction {
    /// Build road directions HTML
    pub fn all_html<'p>(
        all: &[Self],
        id: &str,
        dir: u16,
        select: &'p mut html::Select<'p>,
    ) {
        select.id(id);
        for direction in all {
            let mut option = select.option();
            option.value(direction.id);
            if dir == direction.id {
                option.selected();
            }
            option.cdata(&direction.direction).close();
        }
        select.close();
    }
}

impl RoadClass {
    /// Build road class HTML
    pub fn all_html<'p>(
        all: &[Self],
        id: &str,
        cls: u16,
        select: &'p mut html::Select<'p>,
    ) {
        select.id(id);
        for r_class in all {
            let mut option = select.option();
            option.value(r_class.id);
            if cls == r_class.id {
                option.selected();
            }
            option.cdata(&r_class.description).close();
        }
        select.close();
    }
}

impl Road {
    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.class("title row").cdata(self.name());
        String::from(tree)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &RoadAnc) -> String {
        let mut tree = Tree::new();
        self.title(View::Setup, &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("abbrev").cdata("Abbreviation").close();
        div.input()
            .id("abbrev")
            .maxlength(6)
            .size(6)
            .value(&self.abbrev);
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("r_class").cdata("Road class").close();
        RoadClass::all_html(
            &anc.road_classes,
            "r_class",
            self.r_class,
            &mut div.select(),
        );
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("direction").cdata("Direction").close();
        Direction::all_html(
            &anc.directions,
            "direction",
            self.direction,
            &mut div.select(),
        );
        div.close();
        self.footer_html(true, &mut tree.root::<html::Div>());
        String::from(tree)
    }
}

impl Card for Road {
    type Ancillary = RoadAnc;

    /// Get the resource
    fn res() -> Res {
        Res::Road
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
    fn is_match(&self, search: &str, _anc: &RoadAnc) -> bool {
        self.name.contains_lower(search) || self.abbrev.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &RoadAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("abbrev", &self.abbrev);
        fields.changed_input("r_class", self.r_class);
        fields.changed_input("direction", self.direction);
        fields.into_value().to_string()
    }
}
