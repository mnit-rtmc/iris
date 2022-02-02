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
use crate::card::{Card, CardType, NAME};
use crate::util::HtmlStr;
use serde::{Deserialize, Serialize};

/// Alarm
#[derive(Debug, Deserialize, Serialize)]
pub struct Alarm {
    pub name: String,
    pub description: String,
    pub controller: Option<String>,
    pub pin: u32,
    pub state: bool,
    pub trigger_time: Option<String>,
}

impl Alarm {
    fn to_compact_html(&self) -> String {
        let description = HtmlStr(&self.description);
        let name = HtmlStr(&self.name);
        format!(
            "<span>{description}</span>\
            <span class='{NAME}'>{name}</span>"
        )
    }

    fn to_status_html(&self) -> String {
        let description = HtmlStr(&self.description);
        let state = if self.state {
            "triggered 😧"
        } else {
            "clear 🙂"
        };
        let trigger_time = self.trigger_time.as_deref().unwrap_or("-");
        format!(
            "<div class='row'>\
              <span>Description</span>\
              <span class='info'>{description}</span>\
            </div>\
            <div class='row'>\
              <span>State</span>\
              <span class='info'>{state}</span>\
            </div>\
            <div class='row'>\
              <span>Trigger Time</span>\
              <span class='info'>{trigger_time}</span>\
            </div>"
        )
    }

    fn to_edit_html(&self) -> String {
        let description = HtmlStr(&self.description);
        let controller = HtmlStr(self.controller.as_ref());
        let pin = self.pin;
        format!(
            "<div class='row'>\
               <label for='form_description'>Description</label>\
               <input id='form_description' maxlength='24' size='24' \
                      value='{description}'/>\
             </div>\
             <div class='row'>\
               <label for='form_controller'>Controller</label>\
               <input id='form_controller' maxlength='20' size='20' \
                      value='{controller}'/>\
             </div>\
             <div class='row'>\
               <label for='form_pin'>Pin</label>\
               <input id='form_pin' type='number' min='1' max='104' \
                      size='8' value='{pin}'/>\
             </div>"
        )
    }
}

impl Card for Alarm {
    const TNAME: &'static str = "Alarm";
    const ENAME: &'static str = "⚠ Alarm";
    const HAS_STATUS: bool = true;
    const URI: &'static str = "/iris/api/alarm";

    fn is_match(&self, tx: &str) -> bool {
        self.description.to_lowercase().contains(tx)
            || self.name.to_lowercase().contains(tx)
            || {
                let state = if self.state { "triggered" } else { "clear" };
                state.contains(tx)
            }
    }

    fn name(&self) -> &str {
        &self.name
    }

    fn to_html(&self, ct: CardType) -> String {
        match ct {
            CardType::Compact => self.to_compact_html(),
            CardType::Status => self.to_status_html(),
            CardType::Edit => self.to_edit_html(),
        }
    }
}
