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
use hatmil::{Page, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;

/// System Attribute
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct SystemAttr {
    pub name: String,
    pub value: String,
}

/// Ancillary system attribute data
#[derive(Debug, Default)]
pub struct SystemAttrAnc;

impl AncillaryData for SystemAttrAnc {
    type Primary = SystemAttr;

    /// Construct ancillary system attribute data
    fn new(_pri: &SystemAttr, _view: View) -> Self {
        SystemAttrAnc
    }
}

impl SystemAttr {
    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.cdata(self.name());
        String::from(page)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self) -> String {
        let mut page = Page::new();
        self.title(View::Setup, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("value").cdata("Value").close();
        let mut input = div.input();
        input.id("value").maxlength(64).size(24).value(&self.value);
        div.close();
        self.footer_html(true, &mut page.frag::<html::Div>());
        String::from(page)
    }
}

impl Card for SystemAttr {
    type Ancillary = SystemAttrAnc;

    /// Get the resource
    fn res() -> Res {
        Res::SystemAttribute
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
    fn is_match(&self, search: &str, _anc: &SystemAttrAnc) -> bool {
        self.name.contains_lower(search) || self.value.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &SystemAttrAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Setup => self.to_html_setup(),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("value", &self.value);
        fields.into_value().to_string()
    }
}
