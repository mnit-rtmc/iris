// Copyright (C) 2022-2026  Minnesota Department of Transportation
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
use crate::card::{Card, View};
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::item::ItemState;
use crate::util::{ContainsLower, Fields, Input, TextArea, opt_ref};
use hatmil::{Page, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;

/// GPS
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Gps {
    pub name: String,
    pub controller: Option<String>,
    pub notes: Option<String>,
    // secondary attributes
    pub pin: Option<u32>,
    pub geo_loc: Option<String>,
}

type GpsAnc = ControllerIoAnc<Gps>;

impl Gps {
    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &GpsAnc) -> String {
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.class("end")
            .cdata(self.name())
            .cdata(" ")
            .cdata(anc.item_states(self).to_string());
        String::from(page)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &GpsAnc) -> String {
        let mut page = Page::new();
        self.title(View::Setup, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("notes").cdata("Notes").close();
        div.textarea()
            .id("notes")
            .maxlength(255)
            .rows(4)
            .cols(24)
            .cdata(opt_ref(&self.notes))
            .close();
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("geo_loc").cdata("Device Loc").close();
        div.input()
            .id("geo_loc")
            .maxlength(20)
            .size(20)
            .value(opt_ref(&self.geo_loc))
            .close();
        div.close();
        anc.controller_html(self, &mut page.frag::<html::Div>());
        anc.pin_html(self.pin, &mut page.frag::<html::Div>());
        self.footer_html(true, &mut page.frag::<html::Div>());
        String::from(page)
    }
}

impl ControllerIo for Gps {
    /// Get controller name
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Card for Gps {
    type Ancillary = GpsAnc;

    /// Get the resource
    fn res() -> Res {
        Res::Gps
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[ItemState::Available, ItemState::Inactive]
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
    fn item_state_main(&self, anc: &Self::Ancillary) -> ItemState {
        let states = anc.item_states(self);
        if states.contains(ItemState::Inactive) {
            ItemState::Inactive
        } else {
            ItemState::Available
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &GpsAnc) -> bool {
        self.name.contains_lower(search)
            || self.notes.contains_lower(search)
            || anc.item_states(self).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &GpsAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_text_area("notes", &self.notes);
        fields.changed_input("geo_loc", &self.geo_loc);
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }
}
