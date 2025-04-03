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
use crate::util::{ContainsLower, Fields, HtmlStr, Input, Select};
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

    /// Create an HTML `select` element of comm configs
    fn comm_configs_html(&self, pri: &CommLink) -> String {
        let mut html = String::new();
        html.push_str("<select id='comm_config'>");
        for config in &self.comm_configs {
            html.push_str("<option value='");
            html.push_str(&config.name);
            html.push('\'');
            if pri.comm_config == config.name {
                html.push_str(" selected");
            }
            html.push('>');
            html.push_str(&config.description);
            html.push_str("</option>");
        }
        html.push_str("</select>");
        html
    }

    /// Build controller links as HTML
    fn controllers_html(&self) -> String {
        let mut html = String::new();
        for ctrl in &self.controllers {
            html.push_str(&ctrl.button_loc_html());
        }
        html
    }
}

impl CommLink {
    /// Get item states
    fn item_states(&self) -> ItemStates {
        match (self.poll_enabled, self.connected) {
            (true, true) => ItemState::Available.into(),
            (true, false) => ItemState::Offline.into(),
            _ => ItemState::Inactive.into(),
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let name = HtmlStr::new(self.name());
        let item_states = self.item_states();
        let description = HtmlStr::new(&self.description);
        format!(
            "<div class='title row'>{name} {item_states}</div>\
            <div class='info fill'>{description}</div>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &CommLinkAnc) -> String {
        let title = self.title(View::Status).build();
        let item_states = self.item_states().to_html();
        let description = HtmlStr::new(&self.description);
        let comm_config = anc.comm_config_desc(self);
        let config = HtmlStr::new(comm_config);
        let controllers = anc.controllers_html();
        format!(
            "{title}\
            <div class='row'>\
              <span>{item_states}</span>\
              <span class='info end'>{description}</span>\
            </div>\
            <div class='row'>\
              <span>{config}</span>\
            </div>\
            {controllers}"
        )
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &CommLinkAnc) -> String {
        let title = self.title(View::Setup).build();
        let description = HtmlStr::new(&self.description);
        let uri = HtmlStr::new(&self.uri);
        let enabled = if self.poll_enabled { " checked" } else { "" };
        let comm_configs = anc.comm_configs_html(self);
        let footer = self.footer(true);
        format!(
            "{title}\
            <div class='row'>\
              <label for='description'>Description</label>\
              <input id='description' maxlength='32' size='24' \
                     value='{description}'>\
            </div>\
            <div class='row'>\
              <label for='uri'>URI</label>\
              <input id='uri' maxlength='256' size='28' value='{uri}'>\
            </div>\
            <div class='row'>\
              <label for='comm_config'>Comm Config</label>\
              {comm_configs}\
            </div>\
            <div class='row'>\
              <label for='poll_enabled'>Poll Enabled</label>\
              <input id='poll_enabled' type='checkbox'{enabled}>\
            </div>\
            {footer}"
        )
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

    /// Get the name
    fn name(&self) -> Cow<str> {
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
