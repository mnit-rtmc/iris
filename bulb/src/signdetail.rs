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
use crate::util::{ContainsLower, opt_ref};
use crate::view::View;
use hatmil::{Tree, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;

/// Sign Detail
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct SignDetail {
    name: String,
    dms_type: String,
    portable: bool,
    technology: String,
    sign_access: String,
    legend: String,
    beacon_type: String,
    hardware_make: String,
    hardware_model: String,
    software_make: String,
    software_model: String,
    supported_tags: Option<String>,
    max_pages: u32,
    max_multi_len: u32,
    beacon_activation_flag: bool,
    pixel_service_flag: bool,
    sign_count: u32,
}

/// Ancillary sign detail
#[derive(Debug, Default)]
pub struct SignDetailAnc;

impl AncillaryData for SignDetailAnc {
    type Primary = SignDetail;

    /// Construct ancillary sign detail data
    fn new(_pri: &SignDetail, _view: View) -> Self {
        SignDetailAnc
    }
}

impl SignDetail {
    /// Get item states
    fn item_states(&self) -> ItemStates<'static> {
        if self.sign_count > 0 {
            ItemState::Available.into()
        } else {
            ItemState::Inactive.into()
        }
    }

    /// Convert to compact HTML
    fn to_html_compact(&self, _anc: &SignDetailAnc) -> String {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.class("title row")
            .cdata(&self.name)
            .cdata(" ")
            .cdata(self.item_states().to_string());
        String::from(tree)
    }

    /// Convert to setup HTML
    fn to_html_setup(&self, _anc: &SignDetailAnc, edit: bool) -> String {
        let mut tree = Tree::new();
        self.title(View::Setup(edit), &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row");
        div.span().cdata("DMS Type").close();
        div.span().class("info").cdata(&self.dms_type);
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.span().cdata("Portable").close();
        let mut input = div.input();
        input.r#type("checkbox").disabled();
        if self.portable {
            input.checked();
        }
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.span().cdata("Technology").close();
        div.span().class("info").cdata(&self.technology).close();
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.span().cdata("Sign Access").close();
        div.span().class("info").cdata(&self.sign_access).close();
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.span().cdata("Legend").close();
        div.span().class("info").cdata(&self.legend).close();
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.span().cdata("Beacon Type").close();
        div.span().class("info").cdata(&self.beacon_type).close();
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.span().cdata("Hardware Make").close();
        div.span().class("info").cdata(&self.hardware_make).close();
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.span().cdata("Hardware Model").close();
        div.span().class("info").cdata(&self.hardware_model).close();
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.span().cdata("Software Make").close();
        div.span().class("info").cdata(&self.software_make).close();
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.span().cdata("Software Model").close();
        div.span().class("info").cdata(&self.software_model).close();
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.span().cdata("Supported Tags").close();
        div.span()
            .class("info")
            .cdata(opt_ref(&self.supported_tags))
            .close();
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.span().cdata("Max Pages").close();
        div.span().class("info").cdata(self.max_pages).close();
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.span().cdata("Max MULTI Length").close();
        div.span().class("info").cdata(self.max_multi_len).close();
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.span().cdata("Beacon Activation Flag").close();
        let mut input = div.input();
        input.r#type("checkbox").disabled();
        if self.beacon_activation_flag {
            input.checked();
        }
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.span().cdata("Pixel Service Flag").close();
        let mut input = div.input();
        input.r#type("checkbox").disabled();
        if self.pixel_service_flag {
            input.checked();
        }
        div.close();
        // FIXME
        let can_delete = 0 == self.sign_count;
        footer_html(
            View::Setup(edit),
            can_delete,
            &mut tree.root::<html::Div>(),
        );
        String::from(tree)
    }
}

impl Card for SignDetail {
    type Ancillary = SignDetailAnc;

    /// Get the resource
    fn res() -> Res {
        Res::SignDetail
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
        let states = self.item_states();
        if states.contains(ItemState::Inactive) {
            ItemState::Inactive
        } else {
            ItemState::Available
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, _anc: &SignDetailAnc) -> bool {
        self.name.contains_lower(search) || self.item_states().is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &SignDetailAnc) -> String {
        match view {
            View::Setup(edit) => self.to_html_setup(anc, edit),
            _ => self.to_html_compact(anc),
        }
    }
}
