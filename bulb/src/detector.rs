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
use crate::item::{ItemState, ItemStates};
use crate::util::{
    ContainsLower, Fields, Input, Select, TextArea, opt_ref, opt_str,
};
use crate::view::View;
use hatmil::{Tree, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// Detector
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Detector {
    pub name: String,
    pub label: Option<String>,
    pub notes: Option<String>,
    pub controller: Option<String>,
    pub abandoned: Option<bool>,
    pub force_fail: Option<bool>,
    pub auto_fail: Option<bool>,
    // secondary attributes
    pub pin: Option<u32>,
    pub lane_code: Option<String>,
    pub lane_number: Option<u16>,
    pub field_length: Option<f32>,
    pub fake: Option<String>,
}

/// Lane code
#[derive(Debug, Deserialize)]
pub struct LaneCode {
    pub lcode: String,
    pub description: String,
}

/// Detector ancillary data
#[derive(Default)]
pub struct DetectorAnc {
    cio: ControllerIoAnc<Detector>,
    lane_codes: Vec<LaneCode>,
}

impl DetectorAnc {
    /// Build lane codes HTML
    fn lane_codes_html<'p>(
        &self,
        pri: &Detector,
        select: &'p mut html::Select<'p>,
    ) {
        select.id("lane_code");
        for lc in &self.lane_codes {
            let mut option = select.option();
            option.value(&lc.lcode);
            if Some(&lc.lcode) == pri.lane_code.as_ref() {
                option.selected();
            }
            option.cdata(&lc.description).close();
        }
        select.close();
    }
}

impl AncillaryData for DetectorAnc {
    type Primary = Detector;

    /// Construct ancillary detector data
    fn new(pri: &Detector, view: View) -> Self {
        let mut cio = ControllerIoAnc::new(pri, view);
        if let View::Setup = view {
            cio.assets.push(Asset::LaneCodes);
        }
        DetectorAnc {
            cio,
            lane_codes: Vec::new(),
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.cio.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &Detector,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::LaneCodes => {
                self.lane_codes = serde_wasm_bindgen::from_value(value)?;
                Ok(())
            }
            _ => self.cio.set_asset(pri, asset, value),
        }
    }
}

impl Detector {
    /// Get item states
    fn item_states<'a>(&'a self, anc: &'a DetectorAnc) -> ItemStates<'a> {
        let mut states = anc.cio.item_states(self);
        if self.abandoned.unwrap_or(false) {
            states = states.with(ItemState::Inactive, "abandoned");
        }
        if self.force_fail.unwrap_or(false) {
            states = states.with(ItemState::Fault, "force fail");
        }
        if self.auto_fail.unwrap_or(false) {
            states = states.with(ItemState::Fault, "auto fail");
        }
        states
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &DetectorAnc) -> String {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(self.item_states(anc).to_string());
        div.span().class("info fill").cdata(opt_ref(&self.label));
        String::from(tree)
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &DetectorAnc) -> String {
        let mut tree = Tree::new();
        self.title(View::Status, &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row");
        div.span().class("info fill").cdata(opt_ref(&self.label));
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        self.item_states(anc).spans(&mut div.span());
        div.close();
        String::from(tree)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &DetectorAnc) -> String {
        let mut tree = Tree::new();
        self.title(View::Setup, &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("notes").cdata("Notes").close();
        div.textarea()
            .id("notes")
            .maxlength(32)
            .rows(2)
            .cols(24)
            .cdata(opt_ref(&self.notes))
            .close();
        div.close();
        anc.cio.controller_html(self, &mut tree.root::<html::Div>());
        anc.cio.pin_html(self.pin, &mut tree.root::<html::Div>());
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("lane_code").cdata("Lane").close();
        anc.lane_codes_html(self, &mut div.select());
        div.input()
            .id("lane_number")
            .r#type("number")
            .min(1)
            .max(8)
            .size(2)
            .value(opt_str(self.lane_number));
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("abandoned").cdata("Abandoned").close();
        let mut input = div.input();
        input.id("abandoned").r#type("checkbox");
        if let Some(true) = self.abandoned {
            input.checked();
        }
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("force_fail").cdata("Force Fail").close();
        let mut input = div.input();
        input.id("force_fail").r#type("checkbox");
        if let Some(true) = self.force_fail {
            input.checked();
        }
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("auto_fail").cdata("Auto Fail").close();
        let mut input = div.input();
        input.id("auto_fail").r#type("checkbox").disabled();
        if let Some(true) = self.auto_fail {
            input.checked();
        }
        div.close();
        self.footer_html(true, &mut tree.root::<html::Div>());
        String::from(tree)
    }
}

impl ControllerIo for Detector {
    /// Get controller name
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Card for Detector {
    type Ancillary = DetectorAnc;

    /// Default item state
    const DEF_STATE: ItemState = ItemState::Online;

    /// Get the resource
    fn res() -> Res {
        Res::Detector
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[
            ItemState::Online,
            ItemState::Fault,
            ItemState::Offline,
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
        let states = self.item_states(anc);
        if states.contains(ItemState::Inactive) {
            ItemState::Inactive
        } else if states.contains(ItemState::Offline) {
            ItemState::Offline
        } else if states.contains(ItemState::Fault) {
            ItemState::Fault
        } else {
            ItemState::Online
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &DetectorAnc) -> bool {
        self.name.contains_lower(search)
            || self.label.contains_lower(search)
            || self.item_states(anc).is_match(search)
            || self.abandoned.unwrap_or(false)
                && "abandoned".contains_lower(search)
            || self.force_fail.unwrap_or(false)
                && "force fail".contains_lower(search)
            || self.auto_fail.unwrap_or(false)
                && "auto fail".contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &DetectorAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Status => self.to_html_status(anc),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_text_area("notes", &self.notes);
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.changed_select("lane_code", &self.lane_code);
        fields.changed_input("lane_number", self.lane_number);
        fields.changed_input("abandoned", self.abandoned);
        fields.changed_input("force_fail", self.force_fail);
        fields.into_value().to_string()
    }
}
