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
use crate::card::{AncillaryData, Card};
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::error::Result;
use crate::geoloc::LocAnc;
use crate::item::ItemState;
use crate::start::select_item_map;
use crate::util::{ContainsLower, Fields, Input, TextArea, opt_ref};
use crate::view::View;
use hatmil::{Tree, html};
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
    pub notes: Option<String>,
    pub controller: Option<String>,
    // secondary attributes
    pub geo_loc: Option<String>,
    pub pin: Option<u32>,
    pub toll_zone: Option<String>,
    pub settings: Option<TagReaderSettings>,
}

/// Tag reader ancillary data
#[derive(Default)]
pub struct TagReaderAnc {
    cio: ControllerIoAnc<TagReader>,
    loc: LocAnc<TagReader>,
}

impl ProtocolSettings {
    /// Build settings HTML
    fn build_html<'p>(&self, nm: &str, details: &'p mut html::Details<'p>) {
        details.summary().cdata(nm).cdata(" Settings").close();
        if let Some(rf_atten_downlink_db) = self.rf_atten_downlink_db {
            let mut div = details.div();
            div.cdata("RF Atten Downlink")
                .span()
                .class("info")
                .cdata(rf_atten_downlink_db)
                .cdata(" dB");
            div.close();
        }
        if let Some(rf_atten_uplink_db) = self.rf_atten_uplink_db {
            let mut div = details.div();
            div.cdata("RF Atten Uplink")
                .span()
                .class("info")
                .cdata(rf_atten_uplink_db)
                .cdata(" dB");
            div.close();
        }
        if let Some(data_detect_db) = self.data_detect_db {
            let mut div = details.div();
            div.cdata("Data Detect")
                .span()
                .class("info")
                .cdata(data_detect_db)
                .cdata(" dB");
            div.close();
        }
        if let Some(seen_count) = self.seen_count {
            let mut div = details.div();
            div.cdata("Seen Count")
                .span()
                .class("info")
                .cdata(seen_count);
            div.close();
        }
        if let Some(unique_count) = self.unique_count {
            let mut div = details.div();
            div.cdata("Unique Count")
                .span()
                .class("info")
                .cdata(unique_count);
            div.close();
        }
        if let Some(uplink_source) = &self.uplink_source {
            let mut div = details.div();
            div.cdata("Uplink Source")
                .span()
                .class("info")
                .cdata(format!("{uplink_source:?}"));
            div.close();
        }
        if let Some(slot) = self.slot {
            let mut div = details.div();
            div.cdata("Slot").span().class("info").cdata(slot);
            div.close();
        }
        details.close();
    }
}

impl TagReaderSettings {
    /// Build settings HTML
    fn build_html<'p>(&self, div: &'p mut html::Div<'p>) {
        let mut details = div.details();
        details.summary().cdata("Settings").close();
        if let Some(ack_timeout) = self.ack_timeout {
            let mut div = details.div();
            div.cdata("Ack Timeout")
                .span()
                .class("info")
                .cdata(ack_timeout);
            div.close();
        }
        if let Some(rf_control) = &self.rf_control {
            let mut div = details.div();
            div.cdata("RF Control")
                .span()
                .class("info")
                .cdata(format!("{rf_control:?}"));
            div.close();
        }
        if let Some(downlink_freq_khz) = self.downlink_freq_khz {
            let mut div = details.div();
            div.cdata("Downlink Freq")
                .span()
                .class("info")
                .cdata(downlink_freq_khz)
                .cdata(" kHz");
            div.close();
        }
        if let Some(uplink_freq_khz) = self.uplink_freq_khz {
            let mut div = details.div();
            div.cdata("Uplink Freq")
                .span()
                .class("info")
                .cdata(uplink_freq_khz)
                .cdata(" kHz");
            div.close();
        }
        if let Some(line_loss_db) = self.line_loss_db {
            let mut div = details.div();
            div.cdata("Line Loss")
                .span()
                .class("info")
                .cdata(line_loss_db)
                .cdata(" dB");
            div.close();
        }
        if let Some(sync_mode) = &self.sync_mode {
            let mut div = details.div();
            div.cdata("Sync Mode")
                .span()
                .class("info")
                .cdata(format!("{sync_mode:?}"));
            div.close();
        }
        if let Some(slave_select_count) = self.slave_select_count {
            let mut div = details.div();
            div.cdata("Slave Select Count")
                .span()
                .class("info")
                .cdata(slave_select_count);
            div.close();
        }
        if let Some(mux_mode) = &self.mux_mode {
            let mut div = details.div();
            div.cdata("Mux Mode").span().class("info").cdata(mux_mode);
            div.close();
        }
        if let Some(antenna_channel) = &self.antenna_channel {
            let mut div = details.div();
            div.cdata("Antenna Channel")
                .span()
                .class("info")
                .cdata(format!("{antenna_channel:?}"));
            div.close();
        }
        details.close();
        if let Some(prot) = &self.sego {
            prot.build_html("SeGo", &mut div.details());
        }
        if let Some(prot) = &self.iag {
            prot.build_html("IAG", &mut div.details());
        }
        if let Some(prot) = &self._6c {
            prot.build_html("6C", &mut div.details());
        }
    }
}

impl AncillaryData for TagReaderAnc {
    type Primary = TagReader;

    /// Construct ancillary tag reader data
    fn new(pri: &TagReader, view: View) -> Self {
        let cio = ControllerIoAnc::new(pri, view);
        let mut loc = LocAnc::new(pri, view);
        if let View::Status = view {
            loc.assets
                .push(Asset::GeoLoc(pri.name.to_string(), Res::TagReader));
        }
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
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(anc.cio.item_states(self).to_string())
            .close();
        div = tree.root::<html::Div>();
        div.class("info fill")
            .cdata_len(opt_ref(&self.location), 32);
        String::from(tree)
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &TagReaderAnc) -> String {
        if let Some((lon, lat)) = anc.loc.lonlat() {
            select_item_map(Res::TagReader, &self.name, lon, lat);
        }
        let mut tree = Tree::new();
        self.title(View::Status, &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        anc.cio.item_states(self).spans(&mut div.span());
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.span()
            .class("info")
            .cdata_len(opt_ref(&self.location), 64);
        div.close();
        if let Some(settings) = &self.settings {
            settings.build_html(&mut tree.root::<html::Div>());
        }
        String::from(tree)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &TagReaderAnc, edit: bool) -> String {
        let mut tree = Tree::new();
        self.title(View::Setup(edit), &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("notes").cdata("Notes").close();
        div.textarea()
            .id("notes")
            .maxlength(255)
            .rows(3)
            .cols(22)
            .cdata(opt_ref(&self.notes));
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("toll_zone").cdata("Toll Zone").close();
        div.input()
            .id("toll_zone")
            .maxlength(20)
            .size(20)
            .value(opt_ref(&self.toll_zone));
        div.close();
        anc.cio.controller_html(self, &mut tree.root::<html::Div>());
        anc.cio.pin_html(self.pin, &mut tree.root::<html::Div>());
        String::from(tree)
    }
}

impl ControllerIo for TagReader {
    /// Get controller name
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Card for TagReader {
    type Ancillary = TagReaderAnc;

    /// Default item state
    const DEF_STATE: ItemState = ItemState::Online;

    /// Get the resource
    fn res() -> Res {
        Res::TagReader
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[ItemState::Online, ItemState::Offline, ItemState::Inactive]
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

    /// Get the main item state
    fn item_state_main(&self, anc: &Self::Ancillary) -> ItemState {
        let states = anc.cio.item_states(self);
        if states.contains(ItemState::Inactive) {
            ItemState::Inactive
        } else if states.contains(ItemState::Offline) {
            ItemState::Offline
        } else {
            ItemState::Online
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &TagReaderAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || self.notes.contains_lower(search)
            || anc.cio.item_states(self).is_match(search)
    }

    /// Get geo location name
    fn geoloc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &TagReaderAnc) -> String {
        match view {
            View::Create => self.to_html_create(20),
            View::Status => self.to_html_status(anc),
            View::Setup(edit) => self.to_html_setup(anc, edit),
            View::Location(edit) => anc.loc.to_html_loc(self, edit),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_text_area("notes", &self.notes);
        fields.changed_input("toll_zone", &self.toll_zone);
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }

    /// Get changed fields on Location view
    fn changed_location(&self, anc: TagReaderAnc) -> String {
        anc.loc.changed_location()
    }
}
