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
use crate::util::{ContainsLower, Fields, Input, opt_ref};
use hatmil::{Page, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;

/// Detector
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Detector {
    pub name: String,
    pub label: Option<String>,
    pub notes: Option<String>,
    pub controller: Option<String>,
    // secondary attributes
    pub pin: Option<u32>,
    pub lane_code: Option<String>,
    pub lane_number: Option<u16>,
    pub abandoned: Option<bool>,
    pub force_fail: Option<bool>,
    pub auto_fail: Option<bool>,
    pub field_length: Option<f32>,
    pub fake: Option<String>,
}

type DetectorAnc = ControllerIoAnc<Detector>;

impl Detector {
    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &DetectorAnc) -> String {
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(anc.item_states(self).to_string())
            .close();
        div = page.frag::<html::Div>();
        div.class("info fill").cdata(opt_ref(&self.label));
        String::from(page)
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &DetectorAnc) -> String {
        let mut page = Page::new();
        self.title(View::Status, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        anc.item_states(self).tooltips(&mut div.span());
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.span()
            .class("info")
            .cdata_len(opt_ref(&self.label), 20)
            .close();
        String::from(page)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &DetectorAnc) -> String {
        let mut page = Page::new();
        self.title(View::Setup, &mut page.frag::<html::Div>());
        anc.controller_html(self, &mut page.frag::<html::Div>());
        anc.pin_html(self.pin, &mut page.frag::<html::Div>());
        self.footer_html(true, &mut page.frag::<html::Div>());
        String::from(page)
    }
}

impl ControllerIo for Detector {
    /// Get controller name
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Card for Detector {
    type Ancillary = DetectorAnc;

    /// Get the resource
    fn res() -> Res {
        Res::Detector
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[
            ItemState::Available,
            ItemState::Offline,
            ItemState::Inactive,
        ]
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
    fn is_match(&self, search: &str, anc: &DetectorAnc) -> bool {
        self.name.contains_lower(search)
            || self.label.contains_lower(search)
            || anc.item_states(self).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &DetectorAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Status => self.to_html_status(anc),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }
}
