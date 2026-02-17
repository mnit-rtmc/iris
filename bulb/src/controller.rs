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
use crate::cabinetstyle::CabinetStyle;
use crate::card::{AncillaryData, Card, View};
use crate::commconfig::CommConfig;
use crate::commlink::CommLink;
use crate::error::Result;
use crate::geoloc::{Loc, LocAnc};
use crate::item::{ItemState, ItemStates};
use crate::util::{ContainsLower, Fields, Input, Select, TextArea, opt_ref};
use hatmil::{Page, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// Controller conditions
#[derive(Debug, Deserialize)]
pub struct Condition {
    pub id: u32,
    pub description: String,
}

/// Comm States
#[derive(Debug, Deserialize, PartialEq)]
pub struct CommState {
    pub id: u32,
    pub description: String,
}

/// Controller IO
#[derive(Debug, Deserialize)]
pub struct Io {
    pub pin: u32,
    pub resource_n: String,
    pub name: String,
}

/// Optional setup data
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Setup {
    pub model: Option<String>,
    pub serial_num: Option<String>,
    pub version: Option<String>,
}

/// Optional status data
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct ControllerStatus {
    pub faults: Option<String>,
}

/// Controller
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Controller {
    pub name: String,
    pub location: Option<String>,
    pub comm_link: Option<String>,
    pub drop_id: u32,
    pub cabinet_style: Option<String>,
    pub condition: u32,
    pub notes: Option<String>,
    pub setup: Option<Setup>,
    pub status: Option<ControllerStatus>,
    pub fail_time: Option<String>,
    // secondary attributes
    pub comm_state: Option<u32>,
    pub geo_loc: Option<String>,
    pub password: Option<String>,
}

/// Ancillary controller data
#[derive(Debug, Default)]
pub struct ControllerAnc {
    loc: LocAnc<Controller>,
    conditions: Vec<Condition>,
    states: Vec<CommState>,
    pub cabinet_styles: Vec<CabinetStyle>,
    pub comm_links: Vec<CommLink>,
    pub comm_configs: Vec<CommConfig>,
    pub controller_io: Vec<Io>,
}

impl AncillaryData for ControllerAnc {
    type Primary = Controller;

    /// Construct ancillary controller data
    fn new(pri: &Controller, view: View) -> Self {
        let mut loc = LocAnc::new(pri, view);
        match view {
            View::Search => {
                loc.assets.push(Asset::Conditions);
                loc.assets.push(Asset::CommStates);
                loc.assets.push(Asset::CommLinks);
                loc.assets.push(Asset::CommConfigs);
            }
            View::Status => {
                loc.assets.push(Asset::Conditions);
                loc.assets.push(Asset::CommStates);
                loc.assets.push(Asset::CommLinks);
                loc.assets.push(Asset::CommConfigs);
                loc.assets.push(Asset::ControllerIo(pri.name.to_string()));
            }
            View::Setup => {
                loc.assets.push(Asset::Conditions);
                loc.assets.push(Asset::CabinetStyles);
            }
            _ => (),
        };
        ControllerAnc {
            loc,
            ..Default::default()
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.loc.asset()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &Controller,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::Conditions => {
                self.conditions = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::CommStates => {
                self.states = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::CabinetStyles => {
                self.cabinet_styles = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::CommConfigs => {
                self.comm_configs = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::CommLinks => {
                self.comm_links = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::ControllerIo(_ctrl) => {
                self.controller_io = serde_wasm_bindgen::from_value(value)?;
            }
            _ => self.loc.set_asset(pri, asset, value)?,
        }
        Ok(())
    }
}

impl ControllerAnc {
    /// Get condition description
    fn condition(&self, pri: &Controller) -> &str {
        for condition in &self.conditions {
            if pri.condition == condition.id {
                return &condition.description;
            }
        }
        ""
    }

    /// Get comm state description
    fn comm_state(&self, pri: &Controller) -> &str {
        if let Some(cs) = pri.comm_state {
            for state in &self.states {
                if cs == state.id {
                    return &state.description;
                }
            }
        }
        ""
    }

    /// Build controller conditions HTML
    fn conditions_html<'p>(
        &self,
        pri: &Controller,
        select: &'p mut html::Select<'p>,
    ) {
        select.id("condition");
        for condition in &self.conditions {
            let mut option = select.option();
            option.value(condition.id);
            if pri.condition == condition.id {
                option.selected();
            }
            option.cdata(&condition.description).close();
        }
        select.close();
    }

    /// Build cabinet styles HTML
    fn cabinet_styles_html<'p>(
        &self,
        pri: &Controller,
        select: &'p mut html::Select<'p>,
    ) {
        select.id("cabinet_style");
        select.option().close(); /* empty */
        for cabinet_style in &self.cabinet_styles {
            let mut option = select.option();
            if let Some(cab) = &pri.cabinet_style
                && cab == &cabinet_style.name
            {
                option.selected();
            }
            option.cdata(&cabinet_style.name).close();
        }
        select.close();
    }

    /// Get the comm config
    fn comm_config(&self, pri: &Controller) -> &str {
        if let Some(comm_link) = &pri.comm_link
            && let Some(cl) =
                &self.comm_links.iter().find(|cl| &cl.name == comm_link)
            && let Some(comm_config) = &self
                .comm_configs
                .iter()
                .find(|cc| cc.name == cl.comm_config)
        {
            return &comm_config.description[..];
        }
        ""
    }

    /// Build IO pins HTML
    fn io_pins_html<'p>(&self, div: &'p mut html::Div<'p>) {
        if !self.controller_io.is_empty() {
            let mut ul = div.ul();
            ul.class("pins");
            for cio in &self.controller_io {
                cio.button_link_html(&mut ul.li());
            }
            ul.close();
        }
    }
}

impl Io {
    /// Build controller IO link button HTML
    fn button_link_html<'p>(&self, li: &'p mut html::Li<'p>) {
        if let Ok(res) = Res::try_from(self.resource_n.as_str()) {
            li.class("row");
            li.span().cdata("#").cdata(self.pin).close();
            let mut span = li.span();
            span.cdata(res.symbol());
            span.button()
                .r#type("button")
                .class("go_link")
                .data_("link", &self.name)
                .data_("type", res.as_str())
                .cdata(&self.name)
                .close();
            li.close();
        }
    }
}

impl Loc for Controller {
    /// Get geo location name
    fn geoloc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }
}

impl Controller {
    /// Is controller active?
    pub fn is_active(&self) -> bool {
        // condition 1 is "Active"
        self.condition == 1
    }

    /// Get item states
    pub fn item_states(&self) -> ItemStates<'_> {
        match (self.is_active(), &self.fail_time) {
            (true, Some(fail_time)) => {
                ItemStates::default().with(ItemState::Offline, fail_time)
            }
            (true, None) => ItemState::Available.into(),
            (false, _) => ItemState::Inactive.into(),
        }
    }

    /// Get controller `link:drop`
    fn link_drop(&self) -> String {
        let comm_link = self.comm_link.as_deref().unwrap_or("");
        format!("{comm_link}:{}", self.drop_id)
    }

    /// Get controller model
    fn model(&self) -> Option<&str> {
        self.setup.as_ref().and_then(|s| s.model.as_deref())
    }

    /// Get firmware version
    fn version(&self) -> Option<&str> {
        self.setup.as_ref().and_then(|s| s.version.as_deref())
    }

    /// Get serial number
    fn serial_num(&self) -> Option<&str> {
        self.setup.as_ref().and_then(|s| s.serial_num.as_deref())
    }

    /// Build controller button HTML
    pub fn button_html<'p>(&self, button: &'p mut html::Button<'p>) {
        button
            .r#type("button")
            .class("go_link")
            .data_("link", self.link_drop())
            .data_("type", Res::Controller.as_str())
            .cdata(self.link_drop())
            .close();
    }

    /// Build button and location HTML
    pub fn button_loc_html<'p>(&self, div: &'p mut html::Div<'p>) {
        div.class("row start");
        self.button_html(&mut div.button());
        div.span()
            .class("info")
            .cdata_len(opt_ref(&self.location), 32)
            .close();
        div.close();
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(self.item_states().to_string())
            .close();
        div = page.frag::<html::Div>();
        div.class("info fill").cdata(self.link_drop());
        String::from(page)
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &ControllerAnc) -> String {
        let mut page = Page::new();
        self.title(View::Status, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        self.item_states().tooltips(&mut div.span());
        div.span().cdata(anc.condition(self)).close();
        let mut span = div.span();
        span.button()
            .r#type("button")
            .class("go_link")
            .data_("link", opt_ref(&self.comm_link))
            .data_("type", Res::CommLink.as_str())
            .cdata(opt_ref(&self.comm_link))
            .close();
        span.cdata(":").cdata(self.drop_id);
        div.close();
        div = page.frag::<html::Div>();
        div.class("info end").cdata(anc.comm_config(self)).close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.span().cdata_len(opt_ref(&self.location), 64).close();
        div.span().class("into").cdata(opt_ref(&self.notes)).close();
        div.close();
        if let Some(model) = self.model() {
            div = page.frag::<html::Div>();
            div.class("row fill");
            div.span().cdata("Model").close();
            div.span().class("info").cdata_len(model, 32).close();
            div.close();
        }
        if let Some(version) = self.version() {
            div = page.frag::<html::Div>();
            div.class("row fill");
            div.span().cdata("Version").close();
            div.span().class("info").cdata_len(version, 32).close();
            div.close();
        }
        if let Some(serial_num) = self.serial_num() {
            div = page.frag::<html::Div>();
            div.class("row fill");
            div.span().cdata("S/N").close();
            div.span().class("info").cdata_len(serial_num, 32).close();
            div.close();
        }
        if let Some(fail_time) = &self.fail_time {
            div = page.frag::<html::Div>();
            div.class("row");
            div.span().cdata("Fail Time").close();
            div.span().class("info").cdata(fail_time).close();
            div.close();
        }
        let state = anc.comm_state(self);
        if !state.is_empty() {
            div = page.frag::<html::Div>();
            div.class("row");
            div.span().cdata("Comm State").close();
            div.span().class("info").cdata(state).close();
            div.close();
        }
        anc.io_pins_html(&mut page.frag::<html::Div>());
        String::from(page)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &ControllerAnc) -> String {
        let mut page = Page::new();
        self.title(View::Setup, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("comm_link").cdata("Comm Link").close();
        div.input()
            .id("comm_link")
            .maxlength(20)
            .size(20)
            .value(opt_ref(&self.comm_link));
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("drop_id").cdata("Drop ID").close();
        div.input()
            .id("drop_id")
            .r#type("number")
            .min(0)
            .max(65535)
            .size(6)
            .value(self.drop_id);
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("cabinet_style")
            .cdata("Cabinet Style")
            .close();
        anc.cabinet_styles_html(self, &mut div.select());
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("condition").cdata("Condition").close();
        anc.conditions_html(self, &mut div.select());
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("notes").cdata("Notes").close();
        div.textarea()
            .id("notes")
            .maxlength(128)
            .rows(2)
            .cols(26)
            .cdata(opt_ref(&self.notes))
            .close();
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("password").cdata("Password").close();
        div.input()
            .id("password")
            .maxlength(32)
            .size(26)
            .value(opt_ref(&self.password));
        div.close();
        self.footer_html(true, &mut page.frag::<html::Div>());
        String::from(page)
    }
}

impl Card for Controller {
    type Ancillary = ControllerAnc;

    /// Suggested name prefix
    const PREFIX: &'static str = "ctl";

    /// Get the resource
    fn res() -> Res {
        Res::Controller
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[
            ItemState::Available,
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

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &ControllerAnc) -> bool {
        self.name.contains_lower(search)
            || self.link_drop().contains_lower(search)
            || self.item_states().is_match(search)
            || anc.condition(self).contains_lower(search)
            || anc.comm_config(self).contains_lower(search)
            || self.location.contains_lower(search)
            || self.notes.contains_lower(search)
            || self.cabinet_style.contains_lower(search)
            || self.version().contains_lower(search)
            || anc.comm_state(self).contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &ControllerAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Location => anc.loc.to_html_loc(self),
            View::Setup => self.to_html_setup(anc),
            View::Status => self.to_html_status(anc),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("comm_link", &self.comm_link);
        fields.changed_input("drop_id", self.drop_id);
        fields.changed_select("cabinet_style", &self.cabinet_style);
        fields.changed_select("condition", self.condition);
        fields.changed_text_area("notes", &self.notes);
        fields.changed_input("password", &self.password);
        fields.into_value().to_string()
    }

    /// Get changed fields on Location view
    fn changed_location(&self, anc: ControllerAnc) -> String {
        anc.loc.changed_location()
    }
}
