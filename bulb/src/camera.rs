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
use crate::card::{AncillaryData, Card, View, uri_one};
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::device::DeviceReq;
use crate::error::Result;
use crate::fetch::Action;
use crate::geoloc::{Loc, LocAnc};
use crate::item::ItemState;
use crate::start::fly_map_item;
use crate::util::{
    ContainsLower, Fields, Input, Select, TextArea, opt_ref, opt_str,
};
use hatmil::{Page, html};
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
    fn encoder_type_html<'p>(
        &self,
        pri: &Camera,
        select: &'p mut html::Select<'p>,
    ) {
        select.id("encoder_type");
        for tp in &self.enc_types {
            let mut option = select.option();
            option.value(&tp.name);
            if Some(&tp.name) == pri.encoder_type.as_ref() {
                option.selected();
            }
            option.cdata(tp.to_string()).close();
        }
        select.close();
    }
}

impl Camera {
    /// Search for camera number
    fn check_number(&self, search: &str) -> bool {
        let cam_num = opt_str(self.cam_num);
        match search.strip_prefix('#') {
            Some(s) => cam_num.starts_with(s),
            None => cam_num.contains(search),
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &CameraAnc) -> String {
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(anc.cio.item_states(self).to_string());
        if let Some(num) = self.cam_num {
            div.cdata(format!(" #{num}"));
        }
        String::from(page)
    }

    /// Convert to Control HTML
    fn to_html_control(&self, anc: &CameraAnc) -> String {
        if let Some((lat, lon)) = anc.loc.latlon() {
            fly_map_item(&self.name, lat, lon);
        }
        let mut page = Page::new();
        self.title(View::Control, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        anc.cio.item_states(self).tooltips(&mut div.span());
        if let Some(num) = self.cam_num {
            div.span().cdata(format!("#{num}")).close();
        }
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.span()
            .class("info")
            .cdata_len(opt_ref(&self.location), 64);
        String::from(page)
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
        let mut page = Page::new();
        self.title(View::Request, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.span().cdata("Reset/Reboot").close();
        div.button().id("rq_reset").r#type("button").cdata("Reboot");
        String::from(page)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &CameraAnc) -> String {
        let mut page = Page::new();
        self.title(View::Setup, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("cam_num").cdata("Cam Num").close();
        div.input()
            .id("cam_num")
            .r#type("number")
            .min(1)
            .max(9999)
            .size(8)
            .value(opt_str(self.cam_num));
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("notes").cdata("Notes").close();
        div.textarea()
            .id("notes")
            .maxlength(255)
            .rows(4)
            .cols(24)
            .cdata(opt_ref(&self.notes))
            .close();
        div.close();
        anc.cio.controller_html(self, &mut page.frag::<html::Div>());
        anc.cio.pin_html(self.pin, &mut page.frag::<html::Div>());
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("encoder_type")
            .cdata("Encoder Type")
            .close();
        anc.encoder_type_html(self, &mut div.select());
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("enc_address")
            .cdata("Enc. Address")
            .close();
        div.input()
            .id("enc_address")
            .r#type("text")
            .value(opt_ref(&self.enc_address));
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("enc_port")
            .cdata("Enc. Port Override")
            .close();
        div.input()
            .id("enc_port")
            .r#type("number")
            .min(1)
            .size(4)
            .value(opt_str(self.enc_port));
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("enc_mcast")
            .cdata("Multicast Address")
            .close();
        div.input()
            .id("enc_mcast")
            .r#type("text")
            .value(opt_ref(&self.enc_mcast));
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("enc_channel")
            .cdata("Encoder Channel")
            .close();
        div.input()
            .id("enc_channel")
            .r#type("number")
            .min(1)
            .size(8)
            .value(opt_str(self.enc_channel));
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("cam_template")
            .cdata("Camera Template")
            .close();
        div.span()
            .id("cam_template")
            .class("info")
            .cdata(opt_ref(&self.cam_template))
            .close();
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("publish").cdata("Publish").close();
        let mut input = div.input();
        input.id("publish").r#type("checkbox");
        if self.publish {
            input.checked();
        }
        div.close();
        self.footer_html(true, &mut page.frag::<html::Div>());
        String::from(page)
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
            ItemState::Available
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &CameraAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || self.notes.contains_lower(search)
            || anc.cio.item_states(self).is_match(search)
            || self.check_number(search)
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
        fields.changed_text_area("notes", &self.notes);
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
