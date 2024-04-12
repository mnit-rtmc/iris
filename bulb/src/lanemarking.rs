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
use crate::device::{Device, DeviceAnc};
use crate::resource::{
    inactive_attr, Card, View, EDIT_BUTTON, LOC_BUTTON, NAME,
};
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal, TextArea};
use resources::Res;
use serde::{Deserialize, Serialize};
use std::fmt;

/// Lane Marking
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct LaneMarking {
    pub name: String,
    pub location: Option<String>,
    pub notes: Option<String>,
    pub controller: Option<String>,
    pub deployed: bool,
    // secondary attributes
    pub geo_loc: Option<String>,
    pub pin: Option<u32>,
}

type LaneMarkingAnc = DeviceAnc<LaneMarking>;

impl LaneMarking {
    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &LaneMarkingAnc) -> String {
        let item_state = anc.item_state(self);
        let inactive = inactive_attr(self.controller.is_some());
        let location = HtmlStr::new(&self.location).with_len(32);
        format!(
            "<div class='{NAME} end'>{self} {item_state}</div>\
            <div class='info fill{inactive}'>{location}</div>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &LaneMarkingAnc) -> String {
        let location = HtmlStr::new(&self.location).with_len(64);
        let ctrl_button = anc.controller_button();
        format!(
            "<div class='row'>\
              <span class='info'>{location}</span>\
            </div>\
            <div class='row'>\
              {ctrl_button}\
              {LOC_BUTTON}\
              {EDIT_BUTTON}\
            </div>"
        )
    }

    /// Convert to Edit HTML
    fn to_html_edit(&self) -> String {
        let notes = HtmlStr::new(&self.notes);
        let controller = HtmlStr::new(&self.controller);
        let pin = OptVal(self.pin);
        format!(
            "<div class='row'>\
              <label for='notes'>Notes</label>\
              <textarea id='notes' maxlength='128' rows='2' \
                        cols='24'>{notes}</textarea>\
            </div>\
            <div class='row'>\
              <label for='controller'>Controller</label>\
              <input id='controller' maxlength='20' size='20' \
                     value='{controller}'>\
            </div>\
            <div class='row'>\
              <label for='pin'>Pin</label>\
              <input id='pin' type='number' min='1' max='104' \
                     size='8' value='{pin}'>\
            </div>"
        )
    }
}

impl fmt::Display for LaneMarking {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl Device for LaneMarking {
    /// Get controller
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Card for LaneMarking {
    type Ancillary = LaneMarkingAnc;

    /// Display name
    const DNAME: &'static str = "⛙ Lane Marking";

    /// Get the resource
    fn res() -> Res {
        Res::LaneMarking
    }

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
    fn is_match(&self, search: &str, _anc: &LaneMarkingAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || self.notes.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &LaneMarkingAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Compact => self.to_html_compact(anc),
            View::Status(_) => self.to_html_status(anc),
            View::Edit => self.to_html_edit(),
            _ => unreachable!(),
        }
    }

    /// Get changed fields from Edit form
    fn changed_fields(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_text_area("notes", &self.notes);
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }
}
