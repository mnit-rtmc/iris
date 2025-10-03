// Copyright (C) 2022-2025  Minnesota Department of Transportation
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
use hatmil::Html;
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
        let mut html = Html::new();
        html.div()
            .class("end")
            .text(self.name())
            .text(" ")
            .text(anc.item_states(self).to_string());
        html.to_string()
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &GpsAnc) -> String {
        let mut html = self.title(View::Setup);
        html.div().class("row");
        html.label().r#for("notes").text("Notes").end();
        html.textarea()
            .id("notes")
            .maxlength(255)
            .attr("rows", 4)
            .attr("cols", 24)
            .text(opt_ref(&self.notes))
            .end();
        html.end(); /* div */
        html.div().class("row");
        html.label().r#for("geo_loc").text("Device Loc").end();
        html.input()
            .id("geo_loc")
            .maxlength(20)
            .size(20)
            .value(opt_ref(&self.geo_loc))
            .end();
        html.end(); /* div */
        anc.controller_html(self, &mut html);
        anc.pin_html(self.pin, &mut html);
        self.footer_html(true, &mut html);
        html.to_string()
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

    /// Display name
    const DNAME: &'static str = "🌐 Gps";

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

    /// Check if a search string matches
    fn is_match(&self, search: &str, _anc: &GpsAnc) -> bool {
        self.name.contains_lower(search) || self.notes.contains_lower(search)
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
