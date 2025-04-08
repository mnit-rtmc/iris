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
use crate::util::{ContainsLower, Fields, Input, Select, opt_ref, opt_str};
use hatmil::Html;
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
    /// Build encoder types HTML
    fn encoder_type_html(&self, pri: &Camera, html: &mut Html) {
        html.select().id("encoder_type");
        for tp in &self.enc_types {
            let option = html.option().value(&tp.name);
            if Some(&tp.name) == pri.encoder_type.as_ref() {
                option.attr_bool("selected");
            }
            html.text(tp.to_string()).end();
        }
        html.end(); /* select */
    }
}

impl Camera {
    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &CameraAnc) -> String {
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

    /// Convert to Control HTML
    fn to_html_control(&self, anc: &CameraAnc) -> String {
        if let Some((lat, lon)) = anc.loc.latlon() {
            fly_map_item(&self.name, lat, lon);
        }
        let mut html = self.title(View::Control);
        html.div().class("row");
        anc.cio.item_states(self).tooltips(&mut html);
        html.end(); /* div */
        html.div().class("row");
        html.span()
            .class("info")
            .text_len(opt_ref(&self.location), 64);
        html.into()
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
        let mut html = self.title(View::Request);
        html.div().class("row");
        html.span().text("Reset/Reboot").end();
        html.button().id("rq_reset").type_("button").text("Reboot");
        html.into()
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &CameraAnc) -> String {
        let mut html = self.title(View::Setup);
        html.div().class("row");
        html.label().for_("cam_num").text("Cam Num").end();
        html.input()
            .id("cam_num")
            .type_("number")
            .min("1")
            .max("9999")
            .size("8")
            .value(opt_str(self.cam_num));
        html.end();
        html.div().class("row");
        html.label().for_("notes").text("Notes").end();
        html.textarea()
            .id("notes")
            .maxlength("255")
            .attr("rows", "4")
            .attr("cols", "24")
            .text(opt_ref(&self.notes))
            .end();
        html.end(); /* div */
        anc.cio.controller_html(self, &mut html);
        anc.cio.pin_html(self.pin, &mut html);
        html.div().class("row");
        html.label().for_("encoder_type").text("Encoder Type").end();
        anc.encoder_type_html(self, &mut html);
        html.end(); /* div */
        html.div().class("row");
        html.label().for_("enc_address").text("Enc. Address").end();
        html.input()
            .id("enc_address")
            .type_("text")
            .value(opt_ref(&self.enc_address));
        html.end(); /* div */
        html.div().class("row");
        html.label()
            .for_("enc_port")
            .text("Enc. Port Override")
            .end();
        html.input()
            .id("enc_port")
            .type_("number")
            .min("1")
            .size("4")
            .value(opt_str(self.enc_port));
        html.end(); /* div */
        html.div().class("row");
        html.label()
            .for_("enc_mcast")
            .text("Multicast Address")
            .end();
        html.input()
            .id("enc_mcast")
            .type_("text")
            .value(opt_ref(&self.enc_mcast));
        html.end(); /* div */
        html.div().class("row");
        html.label()
            .for_("enc_channel")
            .text("Encoder Channel")
            .end();
        html.input()
            .id("enc_channel")
            .type_("number")
            .min("1")
            .size("8")
            .value(opt_str(self.enc_channel));
        html.end(); /* div */
        html.div().class("row");
        html.label()
            .for_("cam_template")
            .text("Camera Template")
            .end();
        html.span()
            .id("cam_template")
            .class("info")
            .text(opt_ref(&self.cam_template))
            .end();
        html.end(); /* div */
        html.div().class("row");
        html.label().for_("publish").text("Publish").end();
        let publish = html.input().id("publish").type_("checkbox");
        if self.publish {
            publish.checked();
        }
        html.end(); /* div */
        html.raw(self.footer(true));
        html.into()
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
