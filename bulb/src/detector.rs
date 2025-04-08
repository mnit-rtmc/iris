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
use crate::util::{ContainsLower, Fields, HtmlStr, Input};
use hatmil::Html;
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
        let name = HtmlStr::new(self.name());
        let item_states = anc.item_states(self);
        let label = HtmlStr::new(&self.label);
        format!(
            "<div class='title row'>{name} {item_states}</div>\
            <div class='info fill'>{label}</div>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &DetectorAnc) -> String {
        let title = String::from(self.title(View::Status));
        let mut html = Html::new();
        anc.item_states(self).tooltips(&mut html);
        let item_states = String::from(html);
        let label = HtmlStr::new(&self.label).with_len(20);
        format!(
            "{title}\
            <div class='row'>{item_states}</div>\
            <div class='row'>\
              <span class='info'>{label}</span>\
            </div>"
        )
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &DetectorAnc) -> String {
        let mut html = self.title(View::Setup);
        anc.controller_html(self, &mut html);
        anc.pin_html(self.pin, &mut html);
        html.raw(self.footer(true));
        html.into()
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

    /// Display name
    const DNAME: &'static str = "🚗⬚ Detector";

    /// Get the resource
    fn res() -> Res {
        Res::Detector
    }

    /// Get the name
    fn name(&self) -> Cow<str> {
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
