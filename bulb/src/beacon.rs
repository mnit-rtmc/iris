// Copyright (C) 2022  Minnesota Department of Transportation
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
use crate::device::{Device, DeviceAnc};
use crate::resource::{
    disabled_attr, Card, View, EDIT_BUTTON, LOC_BUTTON, NAME,
};
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal, TextArea};
use serde::{Deserialize, Serialize};
use std::fmt;

/// Beacon
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct Beacon {
    pub name: String,
    pub location: Option<String>,
    pub message: String,
    pub notes: String,
    pub controller: Option<String>,
    pub flashing: bool,
    // full attributes
    pub geo_loc: Option<String>,
    pub pin: Option<u32>,
    pub verify_pin: Option<u32>,
}

type BeaconAnc = DeviceAnc<Beacon>;

impl fmt::Display for Beacon {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl Beacon {
    pub const RESOURCE_N: &'static str = "beacon";

    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let location = HtmlStr::new(&self.location).with_len(12);
        let disabled = disabled_attr(self.controller.is_some());
        format!(
            "<span{disabled}>{location}</span>\
            <span class='{NAME}'>{self}</span>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &BeaconAnc, config: bool) -> String {
        let location = HtmlStr::new(&self.location).with_len(64);
        let message = HtmlStr::new(&self.message);
        let mut status = format!(
            "<div class='row'>\
              <span class='info'>{location}</span>\
            </div>\
            <div class='row center'>\
              <span class='blink-a'>🔆</span>\
              <span class='sign'>{message}</span>\
              <span class='blink-b'>🔆</span>\
            </div>"
        );
        if config {
            status.push_str("<div class='row'>");
            status.push_str(&anc.controller_button());
            status.push_str(LOC_BUTTON);
            status.push_str(EDIT_BUTTON);
            status.push_str("</div>");
        }
        status
    }

    /// Convert to Edit HTML
    fn to_html_edit(&self) -> String {
        let message = HtmlStr::new(&self.message);
        let notes = HtmlStr::new(&self.notes);
        let controller = HtmlStr::new(&self.controller);
        let pin = OptVal(self.pin);
        let verify_pin = OptVal(self.verify_pin);
        format!(
            "<div class='row'>\
              <label for='message'>Message</label>\
              <textarea id='message' maxlength='128' rows='3' \
                        cols='24'>{message}</textarea>\
            </div>\
            <div class='row'>\
              <label for='notes'>Notes</label>\
              <textarea id='notes' maxlength='128' rows='2' \
                        cols='24'>{notes}</textarea>\
            </div>\
            <div class='row'>\
              <label for='controller'>Controller</label>\
              <input id='controller' maxlength='20' size='20' \
                     value='{controller}'/>\
            </div>\
            <div class='row'>\
              <label for='pin'>Pin</label>\
              <input id='pin' type='number' min='1' max='104' \
                     size='8' value='{pin}'/>\
            </div>\
            <div class='row'>\
              <label for='verify_pin'>Verify Pin</label>\
              <input id='verify_pin' type='number' min='1' max='104' \
                     size='8' value='{verify_pin}'/>\
            </div>"
        )
    }
}

impl Device for Beacon {
    /// Get controller
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Card for Beacon {
    type Ancillary = BeaconAnc;

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Get geo location name
    fn geo_loc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, _anc: &BeaconAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || self.message.contains_lower(search)
            || self.notes.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &BeaconAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Compact => self.to_html_compact(),
            View::Status(config) => self.to_html_status(anc, config),
            View::Edit => self.to_html_edit(),
            _ => unreachable!(),
        }
    }

    /// Get changed fields from Edit form
    fn changed_fields(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_text_area("message", &self.message);
        fields.changed_text_area("notes", &self.notes);
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.changed_input("verify_pin", self.verify_pin);
        fields.into_value().to_string()
    }
}
