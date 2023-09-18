// Copyright (C) 2022-2023  Minnesota Department of Transportation
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
use crate::item::ItemState;
use crate::resource::{disabled_attr, Card, View, EDIT_BUTTON, NAME};
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal};
use serde::{Deserialize, Serialize};
use std::fmt;

/// Alarm
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct Alarm {
    pub name: String,
    pub description: String,
    pub controller: Option<String>,
    pub state: bool,
    // full attributes
    pub pin: Option<u32>,
    pub trigger_time: Option<String>,
}

type AlarmAnc = DeviceAnc<Alarm>;

impl Alarm {
    pub const RESOURCE_N: &'static str = "alarm";

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
        let disabled = disabled_attr(self.controller.is_some());
        let item_state = self.item_state(anc);
        let description = HtmlStr::new(&self.description);
        format!(
            "<div class='{NAME} end'>{self} {item_state}</div>\
            <div class='info fill{disabled}'>{description}</div>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &AlarmAnc) -> String {
        let description = HtmlStr::new(&self.description);
        let item_state = self.item_state(anc);
        let item_desc = item_state.description();
        let trigger_time = self.trigger_time.as_deref().unwrap_or("-");
        let ctrl_button = anc.controller_button();
        format!(
            "<div class='row'>\
              <span class='info full'>{description}</span>\
              <span class='full'>{item_state} {item_desc}</span>\
            </div>\
            <div class='row'>\
              <span>Triggered</span>\
              <span class='info'>{trigger_time}</span>\
            </div>\
            <div class='row'>\
              {ctrl_button}\
              {EDIT_BUTTON}\
            </div>"
        )
    }

    /// Convert to Edit HTML
    fn to_html_edit(&self) -> String {
        let description = HtmlStr::new(&self.description);
        let controller = HtmlStr::new(&self.controller);
        let pin = OptVal(self.pin);
        format!(
            "<div class='row'>\
              <label for='description'>Description</label>\
              <input id='description' maxlength='24' size='24' \
                     value='{description}'>\
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

impl fmt::Display for Alarm {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl Device for Alarm {
    /// Get controller
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Card for Alarm {
    type Ancillary = AlarmAnc;

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
            View::Compact => self.to_html_compact(anc),
            View::Status(_) => self.to_html_status(anc),
            View::Edit => self.to_html_edit(),
            _ => unreachable!(),
        }
    }

    /// Get changed fields from Edit form
    fn changed_fields(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("description", &self.description);
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }
}
