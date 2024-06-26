// Copyright (C) 2022-2024  Minnesota Department of Transportation
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
use crate::card::{inactive_attr, Card, View};
use crate::device::{Device, DeviceAnc};
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal};
use resources::Res;
use serde::{Deserialize, Serialize};
use std::borrow::Cow;

/// Detector
#[derive(Debug, Default, Deserialize, Serialize, PartialEq)]
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

type DetectorAnc = DeviceAnc<Detector>;

impl Detector {
    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &DetectorAnc) -> String {
        let name = HtmlStr::new(self.name());
        let item_state = anc.item_state(self);
        let label = HtmlStr::new(&self.label);
        let active = self.controller.is_some()
            || self
                .label
                .as_ref()
                .filter(|lbl| lbl.ends_with('G'))
                .is_some();
        let inactive = inactive_attr(active);
        format!(
            "<div class='title row'>{name} {item_state}</div>\
            <div class='info fill{inactive}'>{label}</div>"
        )
    }

    /// Convert to Control HTML
    fn to_html_control(&self) -> String {
        let title = self.title(View::Control);
        let label = HtmlStr::new(&self.label).with_len(20);
        format!(
            "{title}\
            <div class='row'>\
              <span class='info'>{label}</span>\
            </div>"
        )
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &DetectorAnc) -> String {
        let title = self.title(View::Setup);
        let controller = anc.controller_html();
        let pin = OptVal(self.pin);
        let footer = self.footer(true);
        format!(
            "{title}\
            {controller}\
            <div class='row'>\
              <label for='pin'>Pin</label>\
              <input id='pin' type='number' min='1' max='104' \
                     size='8' value='{pin}'>\
            </div>\
            {footer}"
        )
    }
}

impl Device for Detector {
    /// Get controller
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
            || anc.item_state(self).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &DetectorAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Control => self.to_html_control(),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_fields(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }
}
