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
use crate::card::{Card, NAME};
use crate::start::protocols_html;
use crate::util::{Dom, HtmlStr, OptVal};
use crate::Result;
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::fmt;
use wasm_bindgen::JsValue;
use web_sys::Document;

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

/// Comm configuration
#[derive(Debug, Deserialize, Serialize)]
pub struct CommConfig {
    pub name: String,
    pub description: String,
    pub protocol: Option<u32>,
    pub modem: Option<bool>,
    pub timeout_ms: Option<u32>,
    pub poll_period_sec: Option<u32>,
    pub long_poll_period_sec: Option<u32>,
    pub idle_disconnect_sec: Option<u32>,
    pub no_response_disconnect_sec: Option<u32>,
}

impl Card for CommConfig {
    const TNAME: &'static str = "Comm Config";
    const ENAME: &'static str = "📡 Comm Config";
    const UNAME: &'static str = "comm_config";

    fn is_match(&self, tx: &str) -> bool {
        self.description.to_lowercase().contains(tx)
            || self.name.to_lowercase().contains(tx)
    }

    fn name(&self) -> &str {
        &self.name
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

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let description = HtmlStr::new(&self.description);
        let name = HtmlStr::new(&self.name);
        format!(
            "<span>{description}</span>\
            <span class='{NAME}'>{name}</span>"
        )
    }

    /// Convert to edit HTML
    fn to_html_edit(&self) -> String {
        let description = HtmlStr::new(&self.description);
        let protocols = protocols_html(self.protocol);
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
              <label for='edit_desc'>Description</label>\
              <input id='edit_desc' maxlength='20' size='20' \
                     value='{description}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_protocol'>Protocol</label>\
              {protocols}\
            </div>\
            <div class='row'>\
              <label for='edit_modem'>Modem</label>\
              <input id='edit_modem' type='checkbox'{modem}/>\
            </div>\
            <div class='row'>\
              <label for='edit_timeout'>Timeout (ms)</label>\
              <input id='edit_timeout' type='number' min='0' size='8' \
                     max='20000' value='{timeout_ms}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_poll'>Poll Period</label>\
              <select id='edit_poll'>{poll_periods}</select>\
            </div>\
            <div class='row'>\
              <label for='edit_long'>Long Poll Period</label>\
              <select id='edit_long'>{long_periods}</select>\
            </div>\
            <div class='row'>\
              <label for='edit_idle'>Idle Disconnect</label>\
              <select id='edit_idle'>{idle_periods}</select>\
            </div>\
            <div class='row'>\
              <label for='edit_no_resp'>No Response Disconnect</label>\
              <select id='edit_no_resp'>{no_resp_periods}</select>\
            </div>"
        )
    }

    /// Get changed fields from Edit form
    fn changed_fields(doc: &Document, json: &JsValue) -> Result<String> {
        let val = Self::new(json)?;
        let mut obj = Map::new();
        if let Some(desc) = doc.input_parse::<String>("edit_desc") {
            if desc != val.description {
                obj.insert("description".to_string(), Value::String(desc));
            }
        }
        let protocol = doc.select_parse::<u32>("edit_protocol");
        if protocol != val.protocol {
            obj.insert("protocol".to_string(), OptVal(protocol).into());
        }
        let modem = doc.input_bool("edit_modem");
        if modem != val.modem {
            obj.insert("modem".to_string(), OptVal(modem).into());
        }
        let timeout_ms = doc.input_parse::<u32>("edit_timeout");
        if timeout_ms != val.timeout_ms {
            obj.insert("timeout_ms".to_string(), OptVal(timeout_ms).into());
        }
        let poll_period_sec = doc.select_parse::<u32>("edit_poll");
        if poll_period_sec != val.poll_period_sec {
            obj.insert(
                "poll_period_sec".to_string(),
                OptVal(poll_period_sec).into(),
            );
        }
        let long_poll_period_sec = doc.select_parse::<u32>("edit_long");
        if long_poll_period_sec != val.long_poll_period_sec {
            obj.insert(
                "long_poll_period_sec".to_string(),
                OptVal(long_poll_period_sec).into(),
            );
        }
        let idle_disconnect_sec = doc.select_parse::<u32>("edit_idle");
        if idle_disconnect_sec != val.idle_disconnect_sec {
            obj.insert(
                "idle_disconnect_sec".to_string(),
                OptVal(idle_disconnect_sec).into(),
            );
        }
        let no_response_disconnect_sec =
            doc.select_parse::<u32>("edit_no_resp");
        if no_response_disconnect_sec != val.no_response_disconnect_sec {
            obj.insert(
                "no_response_disconnect_sec".to_string(),
                OptVal(no_response_disconnect_sec).into(),
            );
        }
        Ok(Value::Object(obj).to_string())
    }
}
