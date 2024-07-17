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
use crate::card::{Card, View};
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::item::ItemStates;
use crate::util::{ContainsLower, Fields, HtmlStr, Input};
use resources::Res;
use serde::{Deserialize, Serialize};
use std::borrow::Cow;

/// RF Control
#[derive(Debug, Deserialize, Serialize, PartialEq)]
#[serde(rename_all = "snake_case")]
pub enum RfControl {
    Sense,
    Continuous,
}

/// Synchronization Mode
#[derive(Debug, Deserialize, Serialize, PartialEq)]
#[serde(rename_all = "snake_case")]
pub enum SyncMode {
    Slave,
    Master,
    GpsSecondary,
    GpsPrimary,
}

/// Antenna Channel
#[derive(Debug, Deserialize, Serialize, PartialEq)]
#[serde(rename_all = "snake_case")]
pub enum AntennaChannel {
    Channel0,
    Channel1,
    Channel2,
    Channel3,
    DisableManualControl,
}

/// Source
#[derive(Debug, Deserialize, Serialize, PartialEq)]
#[serde(rename_all = "snake_case")]
pub enum Source {
    Downlink,
    Uplink,
}

/// Protocol Settings
#[derive(Debug, Default, Deserialize, Serialize, PartialEq)]
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
#[derive(Debug, Default, Deserialize, Serialize, PartialEq)]
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
#[derive(Debug, Default, Deserialize, Serialize, PartialEq)]
pub struct TagReader {
    pub name: String,
    pub location: Option<String>,
    pub controller: Option<String>,
    // secondary attributes
    pub geo_loc: Option<String>,
    pub pin: Option<u32>,
    pub settings: Option<TagReaderSettings>,
}

type TagReaderAnc = ControllerIoAnc<TagReader>;

impl TagReader {
    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &TagReaderAnc) -> String {
        let name = HtmlStr::new(self.name());
        let item_states = ItemStates::from(anc.item_state(self));
        let location = HtmlStr::new(&self.location).with_len(32);
        format!(
            "<div class='title row'>{name} {item_states}</div>\
            <div class='info fill'>{location}</div>"
        )
    }

    /// Convert to Control HTML
    fn to_html_control(&self) -> String {
        let title = self.title(View::Control);
        let location = HtmlStr::new(&self.location).with_len(64);
        format!(
            "{title}\
            <div class='row'>\
              <span class='info'>{location}</span>\
            </div>"
        )
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &TagReaderAnc) -> String {
        let title = self.title(View::Setup);
        let controller = anc.controller_html(self);
        let pin = anc.pin_html(self.pin);
        format!(
            "{title}\
            {controller}\
            {pin}"
        )
    }
}

impl ControllerIo for TagReader {
    /// Get controller
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
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

    /// Get geo location name
    fn geo_loc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &TagReaderAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || anc.item_state(self).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &TagReaderAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Control => self.to_html_control(),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_fields(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }
}
