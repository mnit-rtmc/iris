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
use crate::asset::Asset;
use crate::card::{AncillaryData, Card, View};
use crate::error::Result;
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal, Select};
use resources::Res;
use serde::Deserialize;
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
#[derive(Debug, Deserialize)]
pub struct Protocol {
    pub id: u32,
    pub description: String,
}

/// Comm configuration
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct CommConfig {
    pub name: String,
    pub description: String,
    // secondary attributes
    pub protocol: Option<u32>,
    pub timeout_ms: Option<u32>,
    pub retry_threshold: Option<u32>,
    pub poll_period_sec: Option<u32>,
    pub long_poll_period_sec: Option<u32>,
    pub idle_disconnect_sec: Option<u32>,
    pub no_response_disconnect_sec: Option<u32>,
}

/// Ancillary comm configuration
#[derive(Debug, Default)]
pub struct CommConfigAnc {
    assets: Vec<Asset>,
    pub protocols: Option<Vec<Protocol>>,
}

impl AncillaryData for CommConfigAnc {
    type Primary = CommConfig;

    /// Construct ancillary comm config data
    fn new(_pri: &CommConfig, view: View) -> Self {
        let assets = match view {
            View::Setup => vec![Asset::CommProtocols],
            _ => Vec::new(),
        };
        let protocols = None;
        CommConfigAnc { assets, protocols }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        _pri: &CommConfig,
        _asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        self.protocols = Some(serde_wasm_bindgen::from_value(value)?);
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
    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let name = HtmlStr::new(self.name());
        let description = HtmlStr::new(&self.description);
        format!(
            "<div class='title row'>{name}</div>\
            <div class='info fill'>{description}</div>"
        )
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &CommConfigAnc) -> String {
        let title = String::from(self.title(View::Setup));
        let description = HtmlStr::new(&self.description);
        let protocols = anc.protocols_html(self);
        let timeout_ms = OptVal(self.timeout_ms);
        let retry_threshold = OptVal(self.retry_threshold);
        let poll_periods = period_options(&PERIODS[1..], self.poll_period_sec);
        let long_periods =
            period_options(&PERIODS[1..], self.long_poll_period_sec);
        let idle_periods = period_options(PERIODS, self.idle_disconnect_sec);
        let no_resp_periods =
            period_options(PERIODS, self.no_response_disconnect_sec);
        let footer = self.footer(true);
        format!(
            "{title}\
            <div class='row'>\
              <label for='description'>Description</label>\
              <input id='description' maxlength='20' size='20' \
                     value='{description}'>\
            </div>\
            <div class='row'>\
              <label for='protocol'>Protocol</label>\
              {protocols}\
            </div>\
            <div class='row'>\
              <label for='timeout_ms'>Timeout (ms)</label>\
              <input id='timeout_ms' type='number' min='0' size='8' \
                     max='20000' step='50' value='{timeout_ms}'>\
            </div>\
            <div class='row'>\
              <label for='retry_threshold'>Retry Threshold</label>\
              <input id='retry_threshold' type='number' min='0' size='2' \
                     max='8' value='{retry_threshold}'>\
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
            </div>\
            {footer}"
        )
    }
}

impl Card for CommConfig {
    type Ancillary = CommConfigAnc;

    /// Display name
    const DNAME: &'static str = "📡 Comm Config";

    /// Suggested name prefix
    const PREFIX: &'static str = "cfg";

    /// Get the resource
    fn res() -> Res {
        Res::CommConfig
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
    fn is_match(&self, search: &str, _anc: &CommConfigAnc) -> bool {
        self.description.contains_lower(search)
            || self.name.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &CommConfigAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("description", &self.description);
        fields.changed_select("protocol", self.protocol);
        fields.changed_input("timeout_ms", self.timeout_ms);
        fields.changed_input("retry_threshold", self.retry_threshold);
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
