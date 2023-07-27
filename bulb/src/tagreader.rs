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
use crate::resource::{
    disabled_attr, Card, View, EDIT_BUTTON, LOC_BUTTON, NAME,
};
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal};
use serde::{Deserialize, Serialize};
use std::fmt;

/// RF Control
#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "snake_case")]
pub enum RfControl {
    Sense,
    Continuous,
}

/// Synchronization Mode
#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "snake_case")]
pub enum SyncMode {
    Slave,
    Master,
    GpsSecondary,
    GpsPrimary,
}

/// Antenna Channel
#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "snake_case")]
pub enum AntennaChannel {
    Channel0,
    Channel1,
    Channel2,
    Channel3,
    DisableManualControl,
}

/// Source
#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "snake_case")]
pub enum Source {
    Downlink,
    Uplink,
}

/// Protocol Settings
#[derive(Debug, Default, Deserialize, Serialize)]
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
#[derive(Debug, Default, Deserialize, Serialize)]
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
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct TagReader {
    pub name: String,
    pub location: Option<String>,
    pub controller: Option<String>,
    // full attributes
    pub geo_loc: Option<String>,
    pub pin: Option<u32>,
    pub settings: Option<TagReaderSettings>,
}

type TagReaderAnc = DeviceAnc<TagReader>;

impl TagReader {
    pub const RESOURCE_N: &'static str = "tag_reader";

    /// Get the item state
    fn item_state(&self, anc: &TagReaderAnc) -> ItemState {
        anc.item_state_opt(self).unwrap_or(ItemState::Available)
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &TagReaderAnc) -> String {
        let item_state = self.item_state(anc);
        let location = HtmlStr::new(&self.location).with_len(32);
        let disabled = disabled_attr(self.controller.is_some());
        format!(
            "<div class='{NAME} end'>{self} {item_state}</div>\
            <div class='info fill{disabled}'>{location}</div>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &TagReaderAnc) -> String {
        let location = HtmlStr::new(&self.location).with_len(64);
        let ctrl_button = anc.controller_button();
        format!(
            "<div class='row'>\
              <span class='info'>{location}</span>\
            </div>\
            <div class='row'>\
              {ctrl_button}\
              {LOC_BUTTON}\
              {EDIT_BUTTON}\
            </div>"
        )
    }

    /// Convert to Edit HTML
    fn to_html_edit(&self) -> String {
        let controller = HtmlStr::new(&self.controller);
        let pin = OptVal(self.pin);
        format!(
            "<div class='row'>\
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

impl fmt::Display for TagReader {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl Device for TagReader {
    /// Get controller
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Card for TagReader {
    type Ancillary = TagReaderAnc;

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
            || self.item_state(anc).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &TagReaderAnc) -> String {
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
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }
}
