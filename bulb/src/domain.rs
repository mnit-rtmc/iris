// Copyright (C) 2024-2026  Minnesota Department of Transportation
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
use crate::fetch::Action;
use crate::item::ItemState;
use crate::util::{ContainsLower, Doc, Fields, Input, opt_ref};
use cidr::IpCidr;
use hatmil::{Page, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use std::str::FromStr;
use web_sys::{HtmlButtonElement, HtmlInputElement};

/// Domain
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Domain {
    pub name: String,
    pub enabled: bool,
    // secondary
    pub block: Option<String>,
}

/// Ancillary domain data
#[derive(Debug, Default)]
pub struct DomainAnc;

impl AncillaryData for DomainAnc {
    type Primary = Domain;

    /// Construct ancillary domain data
    fn new(_pri: &Domain, _view: View) -> Self {
        DomainAnc
    }
}

impl Domain {
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
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(self.item_state().to_string());
        String::from(page)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self) -> String {
        let mut page = Page::new();
        self.title(View::Setup, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("block").cdata("Block (CIDR)").close();
        div.input()
            .id("block")
            .maxlength(42)
            .size(24)
            .value(opt_ref(&self.block));
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("enabled").cdata("Enabled").close();
        let mut input = div.input();
        input.id("enabled").r#type("checkbox");
        if self.enabled {
            input.checked();
        }
        div.close();
        self.footer_html(true, &mut page.frag::<html::Div>());
        String::from(page)
    }
}

impl Card for Domain {
    type Ancillary = DomainAnc;

    /// Get the resource
    fn res() -> Res {
        Res::Domain
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
    fn is_match(&self, search: &str, _anc: &DomainAnc) -> bool {
        self.name.contains_lower(search) || self.item_state().is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &DomainAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Setup => self.to_html_setup(),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("block", &self.block);
        fields.changed_input("enabled", self.enabled);
        fields.into_value().to_string()
    }

    /// Handle input event for an element on the card
    fn handle_input(&self, _anc: DomainAnc, id: String) -> Vec<Action> {
        if &id == "block" {
            let doc = Doc::get();
            let block = doc.elem::<HtmlInputElement>("block");
            let ob_save = doc.elem::<HtmlButtonElement>("ob_save");
            match IpCidr::from_str(&block.value()) {
                Ok(_) => {
                    block.set_custom_validity("");
                    block.set_class_name("");
                    ob_save.set_disabled(false);
                }
                Err(e) => {
                    block.set_custom_validity(&e.to_string());
                    block.set_class_name("invalid");
                    ob_save.set_disabled(true);
                }
            }
            block.report_validity();
        }
        Vec::new()
    }
}
