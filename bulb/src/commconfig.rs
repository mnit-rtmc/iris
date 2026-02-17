// Copyright (C) 2022-2026  Minnesota Department of Transportation
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
use crate::util::{ContainsLower, Fields, Input, Select, opt_str};
use hatmil::{Page, html};
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

/// Build periods HTML
fn periods_html<'p>(
    id: &str,
    seconds: Option<u32>,
    periods: &[Period],
    select: &'p mut html::Select<'p>,
) {
    select.id(id);
    for period in periods {
        let sec = period.seconds();
        let mut option = select.option();
        option.value(sec);
        if let Some(s) = seconds
            && s == sec
        {
            option.selected();
        }
        option.cdata(period.to_string()).close();
    }
    select.close();
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
    pub pollinator: Option<bool>,
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
    /// Build comm protocols HTML
    fn protocols_html<'p>(
        &self,
        pri: &CommConfig,
        select: &'p mut html::Select<'p>,
    ) {
        select.id("protocol");
        if let Some(protocols) = &self.protocols {
            for protocol in protocols {
                let mut option = select.option();
                option.value(protocol.id);
                if let Some(p) = pri.protocol
                    && p == protocol.id
                {
                    option.selected();
                }
                option.cdata(&protocol.description).close();
            }
        }
        select.close();
    }
}

impl CommConfig {
    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.class("title row").cdata(self.name()).close();
        div = page.frag::<html::Div>();
        div.class("info fill").cdata(&self.description);
        String::from(page)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &CommConfigAnc) -> String {
        let mut page = Page::new();
        self.title(View::Setup, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("description")
            .cdata("Description")
            .close();
        div.input()
            .id("description")
            .maxlength(20)
            .size(20)
            .value(&self.description);
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("pollinator").cdata("Pollinator").close();
        let mut input = div.input();
        input.id("pollinator").r#type("checkbox");
        if let Some(true) = self.pollinator {
            input.checked();
        }
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("protocol").cdata("Protocol").close();
        anc.protocols_html(self, &mut div.select());
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("timeout_ms")
            .cdata("Timeout (ms)")
            .close();
        div.input()
            .id("timeout_ms")
            .r#type("number")
            .min(0)
            .max(20000)
            .size(8)
            .step("50")
            .value(opt_str(self.timeout_ms));
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("retry_threshold")
            .cdata("Retry Threshold")
            .close();
        div.input()
            .id("retry_threshold")
            .r#type("number")
            .min(0)
            .max(8)
            .size(2)
            .value(opt_str(self.retry_threshold));
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("poll_period_sec")
            .cdata("Poll Period")
            .close();
        periods_html(
            "poll_period_sec",
            self.poll_period_sec,
            &PERIODS[1..],
            &mut div.select(),
        );
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("long_poll_period_sec")
            .cdata("Long Poll Period")
            .close();
        periods_html(
            "long_poll_period_sec",
            self.long_poll_period_sec,
            &PERIODS[1..],
            &mut div.select(),
        );
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("idle_disconnect_sec")
            .cdata("Idle Disconnect")
            .close();
        periods_html(
            "idle_disconnect_sec",
            self.idle_disconnect_sec,
            PERIODS,
            &mut div.select(),
        );
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("no_response_disconnect_sec")
            .cdata("No Response Disconnect")
            .close();
        periods_html(
            "no_response_disconnect_sec",
            self.no_response_disconnect_sec,
            PERIODS,
            &mut div.select(),
        );
        div.close();
        self.footer_html(true, &mut page.frag::<html::Div>());
        String::from(page)
    }
}

impl Card for CommConfig {
    type Ancillary = CommConfigAnc;

    /// Suggested name prefix
    const PREFIX: &'static str = "cfg";

    /// Get the resource
    fn res() -> Res {
        Res::CommConfig
    }

    /// Get the name
    fn name(&self) -> Cow<'_, str> {
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
        fields.changed_input("pollinator", self.pollinator);
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
