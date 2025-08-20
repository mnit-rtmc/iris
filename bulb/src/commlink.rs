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
use crate::card::{AncillaryData, Card, View};
use crate::commconfig::CommConfig;
use crate::controller::Controller;
use crate::error::Result;
use crate::item::{ItemState, ItemStates};
use crate::util::{ContainsLower, Fields, Input, Select};
use hatmil::Html;
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// Comm link
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct CommLink {
    pub name: String,
    pub description: String,
    pub uri: String,
    pub comm_config: String,
    pub poll_enabled: bool,
    pub connected: bool,
}

/// Ancillary comm link data
#[derive(Debug, Default)]
pub struct CommLinkAnc {
    assets: Vec<Asset>,
    pub controllers: Vec<Controller>,
    pub comm_configs: Vec<CommConfig>,
}

impl AncillaryData for CommLinkAnc {
    type Primary = CommLink;

    /// Construct ancillary comm link data
    fn new(_pri: &CommLink, view: View) -> Self {
        let assets = match view {
            View::Status => vec![Asset::Controllers, Asset::CommConfigs],
            _ => vec![Asset::CommConfigs],
        };
        let controllers = Vec::new();
        let comm_configs = Vec::new();
        CommLinkAnc {
            assets,
            controllers,
            comm_configs,
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &CommLink,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::Controllers => {
                let mut controllers: Vec<Controller> =
                    serde_wasm_bindgen::from_value(value)?;
                controllers
                    .retain(|c| c.comm_link.as_deref() == Some(&pri.name));
                self.controllers = controllers;
            }
            Asset::CommConfigs => {
                self.comm_configs = serde_wasm_bindgen::from_value(value)?;
            }
            _ => unreachable!(),
        }
        Ok(())
    }
}

impl CommLinkAnc {
    /// Get comm config description
    fn comm_config_desc(&self, pri: &CommLink) -> &str {
        for config in &self.comm_configs {
            if pri.comm_config == config.name {
                return &config.description;
            }
        }
        ""
    }

    /// Build comm configs HTML
    fn comm_configs_html(&self, pri: &CommLink, html: &mut Html) {
        html.select().id("comm_config");
        for config in &self.comm_configs {
            let option = html.option().value(&config.name);
            if pri.comm_config == config.name {
                option.attr_bool("selected");
            }
            html.text(&config.description).end();
        }
        html.end(); /* select */
    }

    /// Build controller links HTML
    fn controllers_html(&self, html: &mut Html) {
        for ctrl in &self.controllers {
            ctrl.button_loc_html(html);
        }
    }
}

impl CommLink {
    /// Get item states
    fn item_states(&self) -> ItemStates<'_> {
        match (self.poll_enabled, self.connected) {
            (true, true) => ItemState::Available.into(),
            (true, false) => ItemState::Offline.into(),
            _ => ItemState::Inactive.into(),
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let mut html = Html::new();
        html.div()
            .class("title row")
            .text(self.name())
            .text(" ")
            .text(self.item_states().to_string())
            .end();
        html.div().class("info fill").text(&self.description);
        html.to_string()
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &CommLinkAnc) -> String {
        let mut html = self.title(View::Status);
        html.div().class("row");
        self.item_states().tooltips(&mut html);
        html.span().class("info end").text(&self.description).end();
        html.end(); /* div */
        html.div().class("row");
        html.span().text(anc.comm_config_desc(self)).end();
        html.end(); /* div */
        anc.controllers_html(&mut html);
        html.to_string()
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &CommLinkAnc) -> String {
        let mut html = self.title(View::Setup);
        html.div().class("row");
        html.label().r#for("description").text("Description").end();
        html.input()
            .id("description")
            .maxlength("32")
            .size("24")
            .value(&self.description);
        html.end(); /* div */
        html.div().class("row");
        html.label().r#for("uri").text("URI").end();
        html.input()
            .id("uri")
            .maxlength("256")
            .size("28")
            .value(&self.uri);
        html.end(); /* div */
        html.div().class("row");
        html.label().r#for("comm_config").text("Comm Config").end();
        anc.comm_configs_html(self, &mut html);
        html.end(); /* div */
        html.div().class("row");
        html.label()
            .r#for("poll_enabled")
            .text("Poll Enabled")
            .end();
        let enabled = html.input().id("poll_enabled").r#type("checkbox");
        if self.poll_enabled {
            enabled.checked();
        }
        html.end(); /* div */
        self.footer_html(true, &mut html);
        html.to_string()
    }
}

impl Card for CommLink {
    type Ancillary = CommLinkAnc;

    /// Display name
    const DNAME: &'static str = "🔗 Comm Link";

    /// Get the resource
    fn res() -> Res {
        Res::CommLink
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
    fn is_match(&self, search: &str, anc: &CommLinkAnc) -> bool {
        self.description.contains_lower(search)
            || self.name.contains_lower(search)
            || anc.comm_config_desc(self).contains_lower(search)
            || self.uri.contains_lower(search)
            || self.item_states().is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &CommLinkAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Status => self.to_html_status(anc),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("description", &self.description);
        fields.changed_input("uri", &self.uri);
        fields.changed_select("comm_config", &self.comm_config);
        fields.changed_input("poll_enabled", self.poll_enabled);
        fields.into_value().to_string()
    }
}
