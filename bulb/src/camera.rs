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
use crate::card::{AncillaryData, Card, View, uri_one};
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::device::DeviceReq;
use crate::error::Result;
use crate::fetch::Action;
use crate::geoloc::{Loc, LocAnc};
use crate::item::ItemState;
use crate::start::fly_map_item;
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal, Select};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use std::fmt;
use wasm_bindgen::JsValue;

/// Encoder type
#[derive(Debug, Default, Deserialize, PartialEq, Eq, PartialOrd, Ord)]
pub struct EncoderType {
    pub make: String,
    pub model: String,
    pub config: String,
    // NOTE: last to allow deriving PartialOrd / Ord
    pub name: String,
}

impl fmt::Display for EncoderType {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.make)?;
        if !self.model.is_empty() {
            write!(f, " {}", self.model)?;
        }
        if !self.config.is_empty() {
            write!(f, " {}", self.config)?;
        }
        Ok(())
    }
}

/// Camera
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Camera {
    pub name: String,
    pub cam_num: Option<u32>,
    pub location: Option<String>,
    pub notes: Option<String>,
    pub controller: Option<String>,
    pub publish: bool,
    // secondary attributes
    pub geo_loc: Option<String>,
    pub pin: Option<u32>,
    pub cam_template: Option<String>,
    pub encoder_type: Option<String>,
    pub enc_address: Option<String>,
    pub enc_port: Option<u16>,
    pub enc_mcast: Option<String>,
    pub enc_channel: Option<u16>,
    pub video_loss: Option<bool>,
}

/// Camera ancillary data
#[derive(Default)]
pub struct CameraAnc {
    cio: ControllerIoAnc<Camera>,
    loc: LocAnc<Camera>,
    enc_types: Vec<EncoderType>,
}

impl AncillaryData for CameraAnc {
    type Primary = Camera;

    /// Construct ancillary camera data
    fn new(pri: &Camera, view: View) -> Self {
        let cio = ControllerIoAnc::new(pri, view);
        let mut loc = LocAnc::new(pri, view);
        if let (View::Status, Some(nm)) = (view, pri.geoloc()) {
            loc.assets.push(Asset::GeoLoc(nm.to_string(), Res::Camera));
        }
        if let View::Setup = view {
            loc.assets.push(Asset::EncoderTypes);
        }
        CameraAnc {
            cio,
            loc,
            enc_types: Vec::new(),
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.cio.assets.pop().or_else(|| self.loc.assets.pop())
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &Camera,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::Controllers => self.cio.set_asset(pri, asset, value),
            Asset::EncoderTypes => {
                self.enc_types = serde_wasm_bindgen::from_value(value)?;
                self.enc_types.sort();
                Ok(())
            }
            _ => self.loc.set_asset(pri, asset, value),
        }
    }
}

impl CameraAnc {
    /// Create an HTML `select` element of encoder type
    fn encoder_type_html(&self, pri: &Camera) -> String {
        let mut html = String::new();
        html.push_str("<select id='encoder_type'>");
        for tp in &self.enc_types {
            html.push_str("<option value='");
            html.push_str(&tp.name);
            html.push('\'');
            if Some(&tp.name) == pri.encoder_type.as_ref() {
                html.push_str(" selected");
            }
            html.push('>');
            html.push_str(&tp.to_string());
            html.push_str("</option>");
        }
        html.push_str("</select>");
        html
    }
}

impl Camera {
    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &CameraAnc) -> String {
        let name = HtmlStr::new(self.name());
        let item_states = anc.cio.item_states(self);
        let location = HtmlStr::new(&self.location).with_len(32);
        format!(
            "<div class='title row'>{name} {item_states}</div>\
            <div class='info fill'>{location}</div>"
        )
    }

    /// Convert to Control HTML
    fn to_html_control(&self, anc: &CameraAnc) -> String {
        if let Some((lat, lon)) = anc.loc.latlon() {
            fly_map_item(&self.name, lat, lon);
        }
        let title = self.title(View::Control).build();
        let item_states = anc.cio.item_states(self).to_html();
        let location = HtmlStr::new(&self.location).with_len(64);
        format!(
            "{title}\
            <div class='row'>{item_states}</div>\
            <div class='row'>\
              <span class='info'>{location}</span>\
            </div>"
        )
    }

    /// Create action to handle click on a device request button
    #[allow(clippy::vec_init_then_push)]
    fn device_req(&self, req: DeviceReq) -> Vec<Action> {
        let uri = uri_one(Res::Camera, &self.name);
        let mut fields = Fields::new();
        fields.insert_num("device_request", req as u32);
        let value = fields.into_value().to_string();
        let mut actions = Vec::with_capacity(1);
        actions.push(Action::Patch(uri, value.into()));
        actions
    }

    /// Convert to Request HTML
    fn to_html_request(&self, _anc: &CameraAnc) -> String {
        let title = self.title(View::Request).build();
        format!(
            "{title}\
            <div class='row'>\
              <span>Reset/Reboot</span>\
              <span>\
                <button id='rq_reset' type='button'>Reboot</button>\
              </span>\
            </div>"
        )
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &CameraAnc) -> String {
        let title = self.title(View::Setup).build();
        let cam_num = OptVal(self.cam_num);
        let notes = HtmlStr::new(&self.notes);
        let controller = anc.cio.controller_html(self);
        let pin = anc.cio.pin_html(self.pin);
        let encoder_type = anc.encoder_type_html(self);
        let enc_address = HtmlStr::new(&self.enc_address);
        let enc_port = OptVal(self.enc_port);
        let enc_mcast = HtmlStr::new(&self.enc_mcast);
        let enc_channel = OptVal(self.enc_channel);
        let cam_template = HtmlStr::new(&self.cam_template);
        let publish = if self.publish { " checked" } else { "" };
        let footer = self.footer(true);
        format!(
            "{title}\
            <div class='row'>\
              <label for='cam_num'>Cam Num</label>\
              <input id='cam_num' type='number' min='1' max='9999' \
                     size='8' value='{cam_num}'>\
            </div>\
            <div class='row'>\
              <label for='notes'>Notes</label>\
              <textarea id='notes' maxlength='255' rows='4' \
                        cols='24'>{notes}</textarea>\
            </div>\
            {controller}\
            {pin}\
            <div class='row'>\
              <label for='encoder_type'>Encoder Type</label>\
              <span class='info' id='encoder_type'>{encoder_type}</span>\
            </div>\
            <div class='row'>\
              <label for='enc_address'>Enc. Address</label>\
              <input id='enc_address' type='text' value='{enc_address}'>\
            </div>\
            <div class='row'>\
              <label for='enc_port'>Enc. Port Override</label>\
              <input id='enc_port' type='number' min='1' size='4' \
                     value='{enc_port}'>\
            </div>\
            <div class='row'>\
              <label for='enc_mcast'>Multicast Address</label>\
              <input id='enc_mcast' type='text' value='{enc_mcast}'>\
            </div>\
            <div class='row'>\
              <label for='enc_channel'>Encoder Channel</label>\
              <input id='enc_channel' type='number' min='1' size='8' \
                     value='{enc_channel}'>\
            </div>\
            <div class='row'>\
              <label for='cam_template'>Camera Template</label>\
              <span class='info' id='cam_template'>{cam_template}</span>\
            </div>\
            <div class='row'>\
              <label for='publish'>Publish</label>\
              <input id='publish' type='checkbox'{publish}>\
            </div>\
            {footer}"
        )
    }
}

impl ControllerIo for Camera {
    /// Get controller name
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Loc for Camera {
    /// Get geo location name
    fn geoloc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }
}

impl Card for Camera {
    type Ancillary = CameraAnc;

    /// Display name
    const DNAME: &'static str = "🎥 Camera";

    /// Get the resource
    fn res() -> Res {
        Res::Camera
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

    /// Get the main item state
    fn item_state_main(&self, anc: &Self::Ancillary) -> ItemState {
        let item_states = anc.cio.item_states(self);
        if item_states.is_match(ItemState::Inactive.code()) {
            ItemState::Inactive
        } else if item_states.is_match(ItemState::Offline.code()) {
            ItemState::Offline
        } else {
            ItemState::Available
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &CameraAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || self
                .notes
                .as_ref()
                .is_some_and(|n| n.contains_lower(search))
            || anc.cio.item_states(self).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &CameraAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Control => self.to_html_control(anc),
            View::Location => anc.loc.to_html_loc(self),
            View::Request => self.to_html_request(anc),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("cam_num", self.cam_num);
        fields.changed_input("notes", &self.notes);
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.changed_select("encoder_type", &self.encoder_type);
        fields.changed_input("enc_address", &self.enc_address);
        fields.changed_input("enc_port", self.enc_port);
        fields.changed_input("enc_mcast", &self.enc_mcast);
        fields.changed_input("enc_channel", self.enc_channel);
        fields.changed_input("publish", self.publish);
        fields.into_value().to_string()
    }

    /// Get changed fields on Location view
    fn changed_location(&self, anc: CameraAnc) -> String {
        anc.loc.changed_location()
    }

    /// Handle click event for a button on the card
    fn handle_click(&self, _anc: CameraAnc, id: String) -> Vec<Action> {
        match id.as_str() {
            "rq_reset" => self.device_req(DeviceReq::ResetDevice),
            _ => Vec::new(),
        }
    }
}
