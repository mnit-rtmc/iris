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
use crate::error::Result;
use crate::resource::{AncillaryData, Card, View, NAME};
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal, Select};
use serde::{Deserialize, Serialize};
use std::borrow::Cow;
use std::fmt;
use wasm_bindgen::JsValue;

/// Time units for period selections
#[derive(Clone, Copy, Debug)]
enum TimeUnit {
    Sec,
    Min,
    Hr,
}

impl fmt::Display for TimeUnit {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            Self::Sec => write!(f, "s"),
            Self::Min => write!(f, "m"),
            Self::Hr => write!(f, "h"),
        }
    }
}

/// Time periods for poll / disconnect values
#[derive(Clone, Copy, Debug)]
struct Period {
    value: u32,
    unit: TimeUnit,
}

impl Period {
    /// Create a new period
    const fn new(value: u32, unit: TimeUnit) -> Self {
        Self { value, unit }
    }

    /// Get the seconds in the period
    fn seconds(self) -> u32 {
        match self.unit {
            TimeUnit::Sec => self.value,
            TimeUnit::Min => self.value * 60,
            TimeUnit::Hr => self.value * 3600,
        }
    }
}

impl fmt::Display for Period {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{} {}", self.value, self.unit)
    }
}

/// Available periods for selection
const PERIODS: &[Period] = &[
    Period::new(0, TimeUnit::Sec),
    Period::new(5, TimeUnit::Sec),
    Period::new(6, TimeUnit::Sec),
    Period::new(10, TimeUnit::Sec),
    Period::new(15, TimeUnit::Sec),
    Period::new(20, TimeUnit::Sec),
    Period::new(30, TimeUnit::Sec),
    Period::new(60, TimeUnit::Sec),
    Period::new(90, TimeUnit::Sec),
    Period::new(2, TimeUnit::Min),
    Period::new(4, TimeUnit::Min),
    Period::new(5, TimeUnit::Min),
    Period::new(10, TimeUnit::Min),
    Period::new(15, TimeUnit::Min),
    Period::new(20, TimeUnit::Min),
    Period::new(30, TimeUnit::Min),
    Period::new(60, TimeUnit::Min),
    Period::new(2, TimeUnit::Hr),
    Period::new(4, TimeUnit::Hr),
    Period::new(8, TimeUnit::Hr),
    Period::new(12, TimeUnit::Hr),
    Period::new(24, TimeUnit::Hr),
];

/// Make `option` elements for an HTML period `select`
fn period_options(periods: &[Period], seconds: Option<u32>) -> String {
    let mut html = String::new();
    for period in periods {
        let sec = period.seconds();
        html.push_str("<option value='");
        html.push_str(&sec.to_string());
        html.push('\'');
        if let Some(s) = seconds {
            if s == sec {
                html.push_str(" selected");
            }
        }
        html.push('>');
        html.push_str(&period.to_string());
        html.push_str("</option>");
    }
    html
}

/// Comm protocol
#[derive(Debug, Deserialize, Serialize)]
pub struct Protocol {
    pub id: u32,
    pub description: String,
}

/// Comm configuration
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct CommConfig {
    pub name: String,
    pub description: String,
    // full attributes
    pub protocol: Option<u32>,
    pub modem: Option<bool>,
    pub timeout_ms: Option<u32>,
    pub poll_period_sec: Option<u32>,
    pub long_poll_period_sec: Option<u32>,
    pub idle_disconnect_sec: Option<u32>,
    pub no_response_disconnect_sec: Option<u32>,
}

/// Ancillary comm configuration
#[derive(Debug, Default)]
pub struct CommConfigAnc {
    pub protocols: Option<Vec<Protocol>>,
}

impl AncillaryData for CommConfigAnc {
    type Primary = CommConfig;

    /// Get next ancillary URI
    fn next_uri(&self, view: View, _pri: &CommConfig) -> Option<Cow<str>> {
        match (view, &self.protocols) {
            (View::Edit, None) => Some("/iris/comm_protocol".into()),
            _ => None,
        }
    }

    /// Put ancillary JSON data
    fn set_json(
        &mut self,
        _view: View,
        _pri: &CommConfig,
        json: JsValue,
    ) -> Result<()> {
        let protocols = json.into_serde::<Vec<Protocol>>()?;
        self.protocols = Some(protocols);
        Ok(())
    }
}

impl CommConfigAnc {
    /// Create an HTML `select` element of comm protocols
    fn protocols_html(&self, pri: &CommConfig) -> String {
        let mut html = String::new();
        html.push_str("<select id='protocol'>");
        if let Some(protocols) = &self.protocols {
            for protocol in protocols {
                html.push_str("<option value='");
                html.push_str(&protocol.id.to_string());
                html.push('\'');
                if let Some(p) = pri.protocol {
                    if p == protocol.id {
                        html.push_str(" selected");
                    }
                }
                html.push('>');
                html.push_str(&protocol.description);
                html.push_str("</option>");
            }
        }
        html.push_str("</select>");
        html
    }
}

impl CommConfig {
    pub const RESOURCE_N: &'static str = "comm_config";

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let description = HtmlStr::new(&self.description);
        format!(
            "<div class='{NAME} end'>{self}</div>\
            <div class='info fill'>{description}</div>"
        )
    }

    /// Convert to edit HTML
    fn to_html_edit(&self, anc: &CommConfigAnc) -> String {
        let description = HtmlStr::new(&self.description);
        let protocols = anc.protocols_html(self);
        let modem = if let Some(true) = self.modem {
            " checked"
        } else {
            ""
        };
        let timeout_ms = OptVal(self.timeout_ms);
        let poll_periods = period_options(&PERIODS[1..], self.poll_period_sec);
        let long_periods =
            period_options(&PERIODS[1..], self.long_poll_period_sec);
        let idle_periods = period_options(PERIODS, self.idle_disconnect_sec);
        let no_resp_periods =
            period_options(PERIODS, self.no_response_disconnect_sec);
        format!(
            "<div class='row'>\
              <label for='description'>Description</label>\
              <input id='description' maxlength='20' size='20' \
                     value='{description}'/>\
            </div>\
            <div class='row'>\
              <label for='protocol'>Protocol</label>\
              {protocols}\
            </div>\
            <div class='row'>\
              <label for='modem'>Modem</label>\
              <input id='modem' type='checkbox'{modem}/>\
            </div>\
            <div class='row'>\
              <label for='timeout_ms'>Timeout (ms)</label>\
              <input id='timeout_ms' type='number' min='0' size='8' \
                     max='20000' step='50' value='{timeout_ms}'/>\
            </div>\
            <div class='row'>\
              <label for='poll_period_sec'>Poll Period</label>\
              <select id='poll_period_sec'>{poll_periods}</select>\
            </div>\
            <div class='row'>\
              <label for='long_poll_period_sec'>Long Poll Period</label>\
              <select id='long_poll_period_sec'>{long_periods}</select>\
            </div>\
            <div class='row'>\
              <label for='idle_disconnect_sec'>Idle Disconnect</label>\
              <select id='idle_disconnect_sec'>{idle_periods}</select>\
            </div>\
            <div class='row'>\
              <label for='no_response_disconnect_sec'>No Response Disconnect</label>\
              <select id='no_response_disconnect_sec'>{no_resp_periods}</select>\
            </div>"
        )
    }
}

impl fmt::Display for CommConfig {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl Card for CommConfig {
    type Ancillary = CommConfigAnc;

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, _anc: &CommConfigAnc) -> bool {
        self.description.contains_lower(search)
            || self.name.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &CommConfigAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Compact => self.to_html_compact(),
            View::Edit => self.to_html_edit(anc),
            _ => unreachable!(),
        }
    }

    /// Get next suggested name
    fn next_name(obs: &[Self]) -> String {
        let mut num = 1;
        for ob in obs {
            if let Some(("cfg", suffix)) = ob.name.split_once('_') {
                if let Ok(n) = suffix.parse::<u32>() {
                    num = num.max(n + 1);
                }
            }
        }
        format!("cfg_{num}")
    }

    /// Get changed fields from Edit form
    fn changed_fields(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("description", &self.description);
        fields.changed_select("protocol", self.protocol);
        fields.changed_input("modem", self.modem);
        fields.changed_input("timeout_ms", self.timeout_ms);
        fields.changed_select("poll_period_sec", self.poll_period_sec);
        fields
            .changed_select("long_poll_period_sec", self.long_poll_period_sec);
        fields.changed_select("idle_disconnect_sec", self.idle_disconnect_sec);
        fields.changed_select(
            "no_response_disconnect_sec",
            self.no_response_disconnect_sec,
        );
        fields.into_value().to_string()
    }
}
