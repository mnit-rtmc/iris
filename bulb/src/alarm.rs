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
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::item::ItemState;
use crate::util::{ContainsLower, Fields, HtmlStr, Input};
use resources::Res;
use serde::{Deserialize, Serialize};
use std::borrow::Cow;

/// Alarm
#[derive(Debug, Default, Deserialize, Serialize, PartialEq)]
pub struct Alarm {
    pub name: String,
    pub description: String,
    pub controller: Option<String>,
    pub state: bool,
    // secondary attributes
    pub pin: Option<u32>,
    pub trigger_time: Option<String>,
}

type AlarmAnc = ControllerIoAnc<Alarm>;

impl Alarm {
    /// Get the item state
    fn item_state(&self, anc: &AlarmAnc) -> ItemState {
        let item_state = anc.item_state(self);
        match item_state {
            ItemState::Available => {
                if self.state {
                    ItemState::Fault
                } else {
                    ItemState::Available
                }
            }
            _ => item_state,
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &AlarmAnc) -> String {
        let name = HtmlStr::new(self.name());
        let inactive = inactive_attr(self.controller.is_some());
        let item_state = self.item_state(anc);
        let description = HtmlStr::new(&self.description);
        format!(
            "<div class='title row'>{name} {item_state}</div>\
            <div class='info fill{inactive}'>{description}</div>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &AlarmAnc) -> String {
        let title = self.title(View::Status);
        let description = HtmlStr::new(&self.description);
        let item_state = self.item_state(anc);
        let item_desc = item_state.description();
        let trigger_time = self.trigger_time.as_deref().unwrap_or("-");
        format!(
            "{title}\
            <div class='row'>\
              <span class='info full'>{description}</span>\
              <span class='full'>{item_state} {item_desc}</span>\
            </div>\
            <div class='row'>\
              <span>Triggered</span>\
              <span class='info'>{trigger_time}</span>\
            </div>"
        )
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &AlarmAnc) -> String {
        let title = self.title(View::Setup);
        let description = HtmlStr::new(&self.description);
        let controller = anc.controller_html(self);
        let pin = anc.pin_html(self.pin);
        format!(
            "{title}\
            <div class='row'>\
              <label for='description'>Description</label>\
              <input id='description' maxlength='24' size='24' \
                     value='{description}'>\
            </div>\
            {controller}\
            {pin}"
        )
    }
}

impl ControllerIo for Alarm {
    /// Get controller
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Card for Alarm {
    type Ancillary = AlarmAnc;

    /// Display name
    const DNAME: &'static str = "📢 Alarm";

    /// Get the resource
    fn res() -> Res {
        Res::Alarm
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
    fn is_match(&self, search: &str, anc: &AlarmAnc) -> bool {
        self.description.contains_lower(search)
            || self.name.contains_lower(search)
            || self.item_state(anc).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &AlarmAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Status => self.to_html_status(anc),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_fields(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("description", &self.description);
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }
}
