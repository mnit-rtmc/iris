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
use crate::cabinetstyle::CabinetStyle;
use crate::card::{AncillaryData, Card, View};
use crate::commconfig::CommConfig;
use crate::commlink::CommLink;
use crate::error::Result;
use crate::geoloc::{Loc, LocAnc};
use crate::item::{ItemState, ItemStates};
use crate::util::{ContainsLower, Fields, Input, Select, TextArea, opt_ref};
use hatmil::Html;
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
    pub drop_id: u16,
    pub cabinet_style: Option<String>,
    pub condition: u32,
    pub notes: Option<String>,
    pub setup: Option<Setup>,
    pub status: Option<ControllerStatus>,
    pub fail_time: Option<String>,
    // secondary attributes
    pub geo_loc: Option<String>,
    pub password: Option<String>,
}

/// Ancillary controller data
#[derive(Debug, Default)]
pub struct ControllerAnc {
    loc: LocAnc<Controller>,
    pub conditions: Vec<Condition>,
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
                loc.assets.push(Asset::CommLinks);
                loc.assets.push(Asset::CommConfigs);
            }
            View::Status => {
                loc.assets.push(Asset::Conditions);
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
            Asset::CabinetStyles => {
                self.cabinet_styles = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::CommConfigs => {
                self.comm_configs = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::CommLinks => {
                self.comm_links = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::Conditions => {
                self.conditions = serde_wasm_bindgen::from_value(value)?;
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

    /// Build controller conditions HTML
    fn conditions_html(&self, pri: &Controller, html: &mut Html) {
        html.select().id("condition");
        for condition in &self.conditions {
            let option = html.option().value(condition.id.to_string());
            if pri.condition == condition.id {
                option.attr_bool("selected");
            }
            html.text(&condition.description).end();
        }
        html.end(); /* select */
    }

    /// Build cabinet styles HTML
    fn cabinet_styles_html(&self, pri: &Controller, html: &mut Html) {
        html.select().id("cabinet_style");
        html.option().end(); /* empty */
        for cabinet_style in &self.cabinet_styles {
            let option = html.option();
            if let Some(cab) = &pri.cabinet_style
                && cab == &cabinet_style.name
            {
                option.attr_bool("selected");
            }
            html.text(&cabinet_style.name).end();
        }
        html.end(); /* select */
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
    fn io_pins_html(&self, html: &mut Html) {
        if !self.controller_io.is_empty() {
            html.ul().class("pins");
            for cio in &self.controller_io {
                cio.button_link_html(html);
            }
            html.end(); /* ul */
        }
    }
}

impl Io {
    /// Build controller IO link button HTML
    fn button_link_html(&self, html: &mut Html) {
        if let Ok(res) = Res::try_from(self.resource_n.as_str()) {
            html.li().class("row");
            html.span().text("#").text(self.pin.to_string()).end();
            html.span().text(res.symbol());
            html.button()
                .type_("button")
                .class("go_link")
                .attr("data-link", &self.name)
                .attr("data-type", res.as_str())
                .text(&self.name)
                .end();
            html.end(); /* span */
            html.end(); /* li */
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
        match (self.is_active(), self.fail_time.is_some()) {
            (true, true) => ItemStates::default()
                .with(ItemState::Offline, "FIXME: since fail time"),
            (true, false) => ItemState::Available.into(),
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
    pub fn button_html(&self, html: &mut Html) {
        html.button()
            .type_("button")
            .class("go_link")
            .attr("data-link", self.link_drop())
            .attr("data-type", Res::Controller.as_str())
            .text(self.link_drop())
            .end();
    }

    /// Build button and location HTML
    pub fn button_loc_html(&self, html: &mut Html) {
        html.div().class("row start");
        self.button_html(html);
        html.span()
            .class("info")
            .text_len(opt_ref(&self.location), 32)
            .end();
        html.end(); /* div */
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let mut html = Html::new();
        html.div()
            .class("title row")
            .text(self.name())
            .text(" ")
            .text(self.item_states().to_string())
            .end();
        html.div().class("info fill").text(self.link_drop());
        html.to_string()
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &ControllerAnc) -> String {
        let mut html = self.title(View::Status);
        html.div().class("row");
        html.span();
        self.item_states().tooltips(&mut html);
        html.end(); /* span */
        html.span().text(anc.condition(self)).end();
        html.span();
        html.button()
            .type_("button")
            .class("go_link")
            .attr("data-link", opt_ref(&self.comm_link))
            .attr("data-type", Res::CommLink.as_str())
            .text(opt_ref(&self.comm_link))
            .end();
        html.text(":").text(self.drop_id.to_string());
        html.end(); /* span */
        html.end(); /* div */
        html.div()
            .class("info end")
            .text(anc.comm_config(self))
            .end();
        html.div().class("row");
        html.span().text_len(opt_ref(&self.location), 64).end();
        html.span().class("into").text(opt_ref(&self.notes)).end();
        html.end(); /* div */
        if let Some(model) = self.model() {
            html.div().class("row fill");
            html.span().text("Model").end();
            html.span().class("info").text_len(model, 32).end();
            html.end(); /* div */
        }
        if let Some(version) = self.version() {
            html.div().class("row fill");
            html.span().text("Version").end();
            html.span().class("info").text_len(version, 32).end();
            html.end(); /* div */
        }
        if let Some(serial_num) = self.serial_num() {
            html.div().class("row fill");
            html.span().text("S/N").end();
            html.span().class("info").text_len(serial_num, 32).end();
            html.end(); /* div */
        }
        if let Some(fail_time) = &self.fail_time {
            html.div().class("row");
            html.span().text("Fail Time").end();
            html.span().class("info").text(fail_time).end();
            html.end(); /* div */
        }
        anc.io_pins_html(&mut html);
        html.to_string()
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &ControllerAnc) -> String {
        let mut html = self.title(View::Setup);
        html.div().class("row");
        html.label().for_("comm_link").text("Comm Link").end();
        html.input()
            .id("comm_link")
            .maxlength("20")
            .size("20")
            .value(opt_ref(&self.comm_link));
        html.end(); /* div */
        html.div().class("row");
        html.label().for_("drop_id").text("Drop ID").end();
        html.input()
            .id("drop_id")
            .type_("number")
            .min("0")
            .max("65535")
            .size("6")
            .value(self.drop_id.to_string());
        html.end(); /* div */
        html.div().class("row");
        html.label()
            .for_("cabinet_style")
            .text("Cabinet Style")
            .end();
        anc.cabinet_styles_html(self, &mut html);
        html.end(); /* div */
        html.div().class("row");
        html.label().for_("condition").text("Condition").end();
        anc.conditions_html(self, &mut html);
        html.end(); /* div */
        html.div().class("row");
        html.label().for_("notes").text("Notes").end();
        html.textarea()
            .id("notes")
            .maxlength("128")
            .attr("rows", "2")
            .attr("cols", "26")
            .text(opt_ref(&self.notes))
            .end();
        html.end(); /* div */
        html.div().class("row");
        html.label().for_("password").text("Password").end();
        html.input()
            .id("password")
            .maxlength("32")
            .size("26")
            .value(opt_ref(&self.password));
        html.end(); /* div */
        self.footer_html(true, &mut html);
        html.to_string()
    }
}

impl Card for Controller {
    type Ancillary = ControllerAnc;

    /// Display name
    const DNAME: &'static str = "🎛️ Controller";

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
            || self.version().unwrap_or("").contains_lower(search)
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
