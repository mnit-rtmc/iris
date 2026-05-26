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
use crate::card::{AncillaryData, Card, footer_html, uri_one};
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::device::DeviceReq;
use crate::error::Result;
use crate::fetch::Action;
use crate::geoloc::LocAnc;
use crate::item::ItemState;
use crate::start::select_item_map;
use crate::util::{
    ContainsLower, Doc, Fields, Input, Select, TextArea, opt_ref, opt_str,
};
use crate::view::View;
use hatmil::{Tree, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use std::fmt;
use wasm_bindgen::JsValue;
use web_sys::{Element, HtmlElement, HtmlInputElement};

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
        if let View::Setup(_edit) = view {
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
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(anc.cio.item_states(self).to_string());
        if let Some(num) = self.cam_num {
            div.span().class("info").cdata(format!("#{num}"));
        }
        String::from(tree)
    }

    /// store preset if button active, else recall
    fn recall_or_store_preset(&self, preset_num: u32) -> Vec<Action> {
        if let Some(toggle) =
            Doc::get().opt_elem::<HtmlElement>("preset-mode-toggle")
            && toggle.class_name() == "active"
        {
            // switch back to recall before storing
            self.toggle_preset_mode();
            return self.store_preset(preset_num);
        }
        self.recall_preset(preset_num)
    }

    /// recall action
    fn recall_preset(&self, preset_num: u32) -> Vec<Action> {
        let uri = uri_one(Res::Camera, &self.name);
        let mut fields = Fields::new();
        fields.insert_num("recall_preset", preset_num);
        let value = fields.into_value().to_string();
        vec![Action::Patch(uri, value.into())]
    }

    /// store action
    fn store_preset(&self, preset_num: u32) -> Vec<Action> {
        let uri = uri_one(Res::Camera, &self.name);
        let mut fields = Fields::new();
        fields.insert_num("store_preset", preset_num);
        let value = fields.into_value().to_string();
        vec![Action::Patch(uri, value.into())]
    }

    /// toggle the class of the preset mode button
    fn toggle_preset_mode(&self) {
        if let Some(toggle) =
            Doc::get().opt_elem::<HtmlElement>("preset-mode-toggle")
        {
            toggle.set_class_name(match toggle.class_name().as_str() {
                "default" => "active",
                _ => "default",
            });
        }
    }

    /// send PTZ command action
    fn send_ptz(&self, pan: f32, tilt: f32, zoom: f32) -> Vec<Action> {
        let mut speed = 1.0;
        if let Some(slider) =
            Doc::get().opt_elem::<HtmlInputElement>("ptz-speed")
            && let Ok(s) = slider.value().parse::<f32>()
        {
            speed = s;
        }
        let uri = uri_one(Res::Camera, &self.name);
        let mut fields = Fields::new();
        fields.insert_arr("ptz", vec![pan * speed, tilt * speed, zoom * speed]);
        let value = fields.into_value().to_string();
        vec![Action::Patch(uri, value.into())]
    }

    /// Send focus stop command action
    fn stop_focus(&self) -> Vec<Action> {
        self.device_req(DeviceReq::CameraFocusStop)
    }

    /// send iris stop command action
    fn stop_iris(&self) -> Vec<Action> {
        self.device_req(DeviceReq::CameraIrisStop)
    }

    /// send PTZ stop command action
    fn stop_ptz(&self) -> Vec<Action> {
        self.send_ptz(0.0, 0.0, 0.0)
    }

    /// Handles a mousedown event on the expanded card
    fn mouse_down(&self, id: &str) -> Vec<Action> {
        let mut actions = Vec::new();
        // Types of controls handled by MouseEvent
        let handled_types = vec!["iris", "focus", "ptz"];
        // Type of control being input, match element id before first hyphen
        let id_type = id.split("-").next();

        // First, mark proper controls as active and stop other controls if needed
        let doc = Doc::get();
        for t in handled_types {
            if let Some(controls) =
                doc.opt_elem::<Element>(&format!("{}-controls", t))
            {
                if Some(t) == id_type && id != format!("{}-auto", t) {
                    // Continuous input type, so mark active
                    controls.set_class_name("active");
                } else {
                    // Not being input, stop if active
                    if controls.class_name() == "active" {
                        controls.set_class_name("");
                        actions.extend(match t {
                            "focus" => self.stop_focus(),
                            "iris" => self.stop_iris(),
                            "ptz" => self.stop_ptz(),
                            _ => Vec::new(),
                        });
                    }
                }
            }
        }

        // Now append the actual action to do after clearing
        actions.extend(match id {
            "focus-near" => self.device_req(DeviceReq::CameraFocusNear),
            "focus-far" => self.device_req(DeviceReq::CameraFocusFar),
            "iris-open" => self.device_req(DeviceReq::CameraIrisOpen),
            "iris-close" => self.device_req(DeviceReq::CameraIrisClose),
            "ptz-pan-right" => self.send_ptz(1.0, 0.0, 0.0),
            "ptz-pan-left" => self.send_ptz(-1.0, 0.0, 0.0),
            "ptz-tilt-up" => self.send_ptz(0.0, 1.0, 0.0),
            "ptz-tilt-down" => self.send_ptz(0.0, -1.0, 0.0),
            "ptz-zoom-in" => self.send_ptz(0.0, 0.0, 1.0),
            "ptz-zoom-out" => self.send_ptz(0.0, 0.0, -1.0),
            _ => Vec::new(),
        });
        actions
    }

    /// Handles a mouseup event on the expanded card
    #[allow(clippy::single_match)]
    fn mouse_up(&self, id: &str) -> Vec<Action> {
        let mut actions = Vec::new();
        // Types of controls handled by MouseEvent
        let handled_types = vec!["iris", "focus", "ptz"];

        // First handle a "click" if release is trigger
        match id {
            "publish" => actions.extend(self.set_publish()),
            _ => (),
        }

        // Then stop any active controls
        let doc = Doc::get();
        for t in handled_types {
            if let Some(controls) =
                doc.opt_elem::<Element>(&format!("{}-controls", t))
                && controls.class_name() == "active"
            {
                controls.set_class_name("");
                actions.extend(match t {
                    "focus" => self.stop_focus(),
                    "iris" => self.stop_iris(),
                    "ptz" => self.stop_ptz(),
                    _ => Vec::new(),
                });
            }
        }
        actions
    }

    fn to_html_ptz_controls(
        &self,
        _anc: &CameraAnc,
        parent_row: &mut html::Div,
    ) {
        // Add PTZ controls
        let mut div = parent_row.div();
        div.id("ptz-controls");
        let mut row = div.div();
        row.class("row");
        row.button()
            .id("ptz-zoom-out")
            .r#type("button")
            .cdata("-")
            .close();
        row.button().id("ptz-zoom-in").r#type("button").cdata("+");
        row.close();
        row = div.div();
        row.class("row");
        row.button().id("ptz-tilt-up").r#type("button").cdata("↑");
        row.close();
        row = div.div();
        row.class("row");
        row.button()
            .id("ptz-pan-left")
            .r#type("button")
            .cdata("←")
            .close();
        row.span().id("ptz-joystick").close();
        row.button().id("ptz-pan-right").r#type("button").cdata("→");
        row.close();
        row = div.div();
        row.class("row");
        row.button().id("ptz-tilt-down").r#type("button").cdata("↓");
        row.close();
        div.input()
            .id("ptz-speed")
            .r#type("range")
            .min("0.05")
            .max("1.0")
            .step("0.05")
            .value("1.0");
        div.close();
    }

    /// Add lens controls to tree
    fn to_html_lens_controls(
        &self,
        _anc: &CameraAnc,
        parent_row: &mut html::Div,
    ) {
        let mut div = parent_row.div();
        div.class("lens-controls");

        let mut row = div.div();
        row.class("row");
        row.span().cdata("Focus").close();
        let mut focus_div = row.div();
        focus_div.id("focus-controls");
        focus_div
            .button()
            .id("focus-near")
            .r#type("button")
            .cdata("Near")
            .close();
        focus_div
            .button()
            .id("focus-far")
            .r#type("button")
            .cdata("Far")
            .close();
        focus_div
            .button()
            .id("focus-auto")
            .r#type("button")
            .cdata("Auto");
        row.close();

        row = div.div();
        row.class("row");
        row.span().cdata("Iris").close();
        let mut iris_div = row.div();
        iris_div.id("iris-controls");
        iris_div
            .button()
            .id("iris-open")
            .r#type("button")
            .cdata("Open")
            .close();
        iris_div
            .button()
            .id("iris-close")
            .r#type("button")
            .cdata("Close")
            .close();
        iris_div
            .button()
            .id("iris-auto")
            .r#type("button")
            .cdata("Auto");
        row.close();

        row = div.div();
        row.class("row");
        row.span().cdata("Wiper").close();
        row.button()
            .id("camera-wiper")
            .r#type("button")
            .cdata("Send");
        div.close();
    }

    /// Add preset controls to tree
    fn to_html_ptz_presets(
        &self,
        _anc: &CameraAnc,
        parent_row: &mut html::Div,
    ) {
        let mut div = parent_row.div();
        div.class("camera-presets")
            .button()
            .id("preset-mode-toggle")
            .class("default") // .active after click until store
            .r#type("button")
            .cdata("Store preset...")
            .close();
        let mut btns = div.div();
        btns.class("preset-buttons");
        for r in 0..=3 {
            let preset_num = r * 3 + 1;
            let btn_1 = format!("preset-{}", preset_num);
            let btn_2 = format!("preset-{}", preset_num + 1);
            let btn_3 = format!("preset-{}", preset_num + 2);
            let mut row = btns.div();
            row.class("row");
            row.button()
                .id(&btn_1)
                .r#type("button")
                .cdata(preset_num.to_string())
                .close();
            row.button()
                .id(&btn_2)
                .r#type("button")
                .cdata((preset_num + 1).to_string())
                .close();
            row.button()
                .id(&btn_3)
                .r#type("button")
                .cdata((preset_num + 2).to_string());
            row.close();
        }
        div.close();
    }

    /// Set the published status if changed
    fn set_publish(&self) -> Vec<Action> {
        let uri = uri_one(Res::Camera, &self.name);
        let mut fields = Fields::new();
        fields.changed_input("publish", self.publish);
        let value = fields.into_value().to_string();
        vec![Action::Patch(uri, value.into())]
    }

    /// Convert to Control HTML
    fn to_html_control(&self, anc: &CameraAnc) -> String {
        if let Some((lon, lat)) = anc.loc.lonlat() {
            select_item_map(Res::Camera, &self.name, lon, lat);
        }
        // FIXME: set selected video monitor to this camera
        let mut tree = Tree::new();
        self.title(View::Control, &mut tree.root::<html::Div>());

        let mut div = tree.root::<html::Div>();
        div.class("row");
        anc.cio.item_states(self).spans(&mut div.span());
        if let Some(num) = self.cam_num {
            div.span().class("info").cdata(format!("#{num}"));
        }
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.span()
            .class("info")
            .cdata_len(opt_ref(&self.location), 64);
        div.close();

        div = tree.root::<html::Div>();
        div.class("row");
        self.to_html_ptz_controls(anc, &mut div);
        self.to_html_lens_controls(anc, &mut div);
        self.to_html_ptz_presets(anc, &mut div);
        div.close();

        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("publish").cdata("Publish").close();
        let mut input = div.input();
        input.id("publish").r#type("checkbox");
        if self.publish {
            input.checked();
        }
        div.close();

        String::from(tree)
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
        let mut tree = Tree::new();
        self.title(View::Request, &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row");
        div.span().cdata("Reset/Reboot").close();
        div.button().id("rq_reset").r#type("button").cdata("Reboot");
        String::from(tree)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &CameraAnc, edit: bool) -> String {
        let mut tree = Tree::new();
        self.title(View::Setup(edit), &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
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
        div = tree.root::<html::Div>();
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
        anc.cio.controller_html(self, &mut tree.root::<html::Div>());
        anc.cio.pin_html(self.pin, &mut tree.root::<html::Div>());
        div = tree.root::<html::Div>();
        div.class("row");
        div.label()
            .r#for("encoder_type")
            .cdata("Encoder Type")
            .close();
        anc.encoder_type_html(self, &mut div.select());
        div.close();
        div = tree.root::<html::Div>();
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
        div = tree.root::<html::Div>();
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
        div = tree.root::<html::Div>();
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
        div = tree.root::<html::Div>();
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
        div = tree.root::<html::Div>();
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
        footer_html(View::Setup(edit), true, &mut tree.root::<html::Div>());
        String::from(tree)
    }
}

impl ControllerIo for Camera {
    /// Get controller name
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Card for Camera {
    type Ancillary = CameraAnc;

    /// Default item state
    const DEF_STATE: ItemState = ItemState::Online;

    /// Get the resource
    fn res() -> Res {
        Res::Camera
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[
            ItemState::Online,
            ItemState::Offline,
            ItemState::Fault,
            ItemState::Inactive,
        ]
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
    fn is_match(&self, search: &str, anc: &CameraAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || self.notes.contains_lower(search)
            || anc.cio.item_states(self).is_match(search)
            || self.check_number(search)
    }

    /// Get geo location name
    fn geoloc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &CameraAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Control => self.to_html_control(anc),
            View::Request => self.to_html_request(anc),
            View::Setup(edit) => self.to_html_setup(anc, edit),
            View::Location(edit) => anc.loc.to_html_loc(self, edit),
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
        fields.into_value().to_string()
    }

    /// Get changed fields on Location view
    fn changed_location(&self, anc: CameraAnc) -> String {
        anc.loc.changed_location()
    }

    /// Handle click event for a button on the card
    fn handle_click(&self, anc: CameraAnc, id: &str) -> Vec<Action> {
        if let Some(preset_str) = id.strip_prefix("preset-") {
            if let Ok(preset_num) = preset_str.parse::<u32>() {
                return self.recall_or_store_preset(preset_num);
            }
            if preset_str == "mode-toggle" {
                // no Action
                self.toggle_preset_mode();
            }
        }
        match id {
            "focus-auto" => self.device_req(DeviceReq::CameraFocusAuto),
            "iris-auto" => self.device_req(DeviceReq::CameraIrisAuto),
            "camera-wiper" => self.device_req(DeviceReq::CameraWiperOneShot),
            "rq_reset" => self.device_req(DeviceReq::ResetDevice),
            _ => self.handle_click_common(anc, id),
        }
    }

    /// Handle mouse event for an element on the card
    fn handle_mouse(
        &self,
        _anc: CameraAnc,
        id: &str,
        mouse_down: bool,
    ) -> Vec<Action> {
        if mouse_down {
            self.mouse_down(id)
        } else {
            self.mouse_up(id)
        }
    }
}
