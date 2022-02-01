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
use crate::{Card, CardType, HtmlStr, NAME};
use serde::{Deserialize, Serialize};

/// Comm configuration
#[derive(Debug, Deserialize, Serialize)]
pub struct CommConfig {
    pub name: String,
    pub description: String,
    pub protocol: u32,
    pub modem: bool,
    pub timeout_ms: u32,
    pub poll_period_sec: u32,
    pub long_poll_period_sec: u32,
    pub idle_disconnect_sec: u32,
    pub no_response_disconnect_sec: u32,
}

impl CommConfig {
    fn to_compact_html(&self) -> String {
        let description = HtmlStr(&self.description);
        let name = HtmlStr(&self.name);
        format!(
            "<span>{description}</span>\
            <span class='{NAME}'>{name}</span>"
        )
    }

    fn to_edit_html(&self) -> String {
        let description = HtmlStr(&self.description);
        let timeout_ms = self.timeout_ms;
        let poll_period_sec = self.poll_period_sec;
        let long_poll_period_sec = self.long_poll_period_sec;
        let idle_disconnect_sec = self.idle_disconnect_sec;
        let no_response_disconnect_sec = self.no_response_disconnect_sec;
        format!(
            "<div class='row'>\
              <label for='form_description'>Description</label>\
              <input id='form_description' maxlength='20' size='20' \
                     value='{description}'/>\
            </div>\
            <div class='row'>\
              <label for='form_timeout'>Timeout (ms)</label>\
              <input id='form_timeout' type='number' min='0' size='8' \
                     max='20000' value='{timeout_ms}'/>\
            </div>\
            <div class='row'>\
              <label for='form_poll_period'>Poll Period (s)</label>\
              <input id='form_poll_period' type='number' min='0' \
                     size='8' value='{poll_period_sec}'/>\
            </div>\
            <div class='row'>\
              <label for='form_long_poll'>Long Poll Period (s)</label>\
              <input id='form_long_poll' type='number' min='0' \
                     size='8' value='{long_poll_period_sec}'/>\
            </div>\
            <div class='row'>\
              <label for='form_idle'>Idle Disconnect (s)</label>\
              <input id='form_idle' type='number' min='0' size='8' \
                     value='{idle_disconnect_sec}'/>\
            </div>\
            <div class='row'>\
              <label for='form_no_resp'>No Response Disconnect (s)\
              </label>\
              <input id='form_no_resp' type='number' min='0' size='8' \
                     value='{no_response_disconnect_sec}'/>\
            </div>"
        )
    }
}

impl Card for CommConfig {
    const ENAME: &'static str = "📡 Comm Config";

    fn is_match(&self, tx: &str) -> bool {
        self.description.to_lowercase().contains(tx)
            || self.name.to_lowercase().contains(tx)
    }

    fn name(&self) -> &str {
        &self.name
    }

    fn to_html(&self, ct: CardType) -> String {
        match ct {
            CardType::Compact => self.to_compact_html(),
            CardType::Edit => self.to_edit_html(),
            _ => unreachable!(),
        }
    }
}
