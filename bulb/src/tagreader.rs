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
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::error::Result;
use crate::geoloc::{Loc, LocAnc};
use crate::util::{ContainsLower, Fields, Input, opt_ref};
use hatmil::Html;
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// RF Control
#[derive(Debug, Deserialize, PartialEq)]
#[serde(rename_all = "snake_case")]
pub enum RfControl {
    Sense,
    Continuous,
}

/// Synchronization Mode
#[derive(Debug, Deserialize, PartialEq)]
#[serde(rename_all = "snake_case")]
pub enum SyncMode {
    Slave,
    Master,
    GpsSecondary,
    GpsPrimary,
}

/// Antenna Channel
#[derive(Debug, Deserialize, PartialEq)]
#[serde(rename_all = "snake_case")]
pub enum AntennaChannel {
    Channel0,
    Channel1,
    Channel2,
    Channel3,
    DisableManualControl,
}

/// Source
#[derive(Debug, Deserialize, PartialEq)]
#[serde(rename_all = "snake_case")]
pub enum Source {
    Downlink,
    Uplink,
}

/// Protocol Settings
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct ProtocolSettings {
    rf_atten_downlink_db: Option<u32>,
    rf_atten_uplink_db: Option<u32>,
    data_detect_db: Option<u32>,
    seen_count: Option<u32>,
    unique_count: Option<u32>,
    uplink_source: Option<Source>,
    slot: Option<u32>,
}

/// Tag Reader Settings
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct TagReaderSettings {
    ack_timeout: Option<u32>,
    rf_control: Option<RfControl>,
    downlink_freq_khz: Option<u32>,
    uplink_freq_khz: Option<u32>,
    line_loss_db: Option<u32>,
    sync_mode: Option<SyncMode>,
    slave_select_count: Option<u32>,
    mux_mode: Option<String>,
    antenna_channel: Option<AntennaChannel>,
    sego: Option<ProtocolSettings>,
    iag: Option<ProtocolSettings>,
    _6c: Option<ProtocolSettings>,
}

/// Tag Reader
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct TagReader {
    pub name: String,
    pub location: Option<String>,
    pub controller: Option<String>,
    // secondary attributes
    pub geo_loc: Option<String>,
    pub pin: Option<u32>,
    pub settings: Option<TagReaderSettings>,
}

/// Tag reader ancillary data
#[derive(Default)]
pub struct TagReaderAnc {
    cio: ControllerIoAnc<TagReader>,
    loc: LocAnc<TagReader>,
}

impl AncillaryData for TagReaderAnc {
    type Primary = TagReader;

    /// Construct ancillary tag reader data
    fn new(pri: &TagReader, view: View) -> Self {
        let cio = ControllerIoAnc::new(pri, view);
        let loc = LocAnc::new(pri, view);
        TagReaderAnc { cio, loc }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.cio.assets.pop().or_else(|| self.loc.assets.pop())
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &TagReader,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        if let Asset::Controllers = asset {
            self.cio.set_asset(pri, asset, value)
        } else {
            self.loc.set_asset(pri, asset, value)
        }
    }
}

impl TagReader {
    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &TagReaderAnc) -> String {
        let mut html = Html::new();
        html.div()
            .class("title row")
            .text(self.name())
            .text(" ")
            .text(anc.cio.item_states(self).to_string())
            .end();
        html.div()
            .class("info fill")
            .text_len(opt_ref(&self.location), 32);
        html.into()
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &TagReaderAnc) -> String {
        let mut html = self.title(View::Status);
        html.div();
        anc.cio.item_states(self).tooltips(&mut html);
        html.end(); /* div */
        html.div().class("row");
        html.span()
            .class("info")
            .text_len(opt_ref(&self.location), 64);
        html.into()
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &TagReaderAnc) -> String {
        let mut html = self.title(View::Setup);
        anc.cio.controller_html(self, &mut html);
        anc.cio.pin_html(self.pin, &mut html);
        html.into()
    }
}

impl ControllerIo for TagReader {
    /// Get controller name
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Loc for TagReader {
    /// Get geo location name
    fn geoloc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }
}

impl Card for TagReader {
    type Ancillary = TagReaderAnc;

    /// Display name
    const DNAME: &'static str = "🏷️ Tag Reader";

    /// Get the resource
    fn res() -> Res {
        Res::TagReader
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
    fn is_match(&self, search: &str, anc: &TagReaderAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || anc.cio.item_states(self).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &TagReaderAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Location => anc.loc.to_html_loc(self),
            View::Setup => self.to_html_setup(anc),
            View::Status => self.to_html_status(anc),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }

    /// Get changed fields on Location view
    fn changed_location(&self, anc: TagReaderAnc) -> String {
        anc.loc.changed_location()
    }
}
