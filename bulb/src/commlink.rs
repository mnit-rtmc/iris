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
use crate::card::{AncillaryData, Card, View};
use crate::commconfig::CommConfig;
use crate::controller::Controller;
use crate::error::Result;
use crate::item::{ItemState, ItemStates};
use crate::util::{ContainsLower, Fields, Input, Select};
use hatmil::{Page, html};
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
    fn comm_configs_html<'p>(
        &self,
        pri: &CommLink,
        select: &'p mut html::Select<'p>,
    ) {
        select.id("comm_config");
        for config in &self.comm_configs {
            let mut option = select.option();
            option.value(&config.name);
            if pri.comm_config == config.name {
                option.selected();
            }
            option.cdata(&config.description).close();
        }
        select.close();
    }

    /// Build controller links HTML
    fn controllers_html<'p>(&self, div: &'p mut html::Div<'p>) {
        for ctrl in &self.controllers {
            ctrl.button_loc_html(&mut div.div());
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
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(self.item_states().to_string())
            .close();
        div = page.frag::<html::Div>();
        div.class("info fill").cdata(&self.description);
        String::from(page)
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &CommLinkAnc) -> String {
        let mut page = Page::new();
        self.title(View::Status, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        self.item_states().tooltips(&mut div.span());
        div.span().class("info end").cdata(&self.description);
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.span().cdata(anc.comm_config_desc(self)).close();
        div.close();
        anc.controllers_html(&mut page.frag::<html::Div>());
        String::from(page)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &CommLinkAnc) -> String {
        let mut page = Page::new();
        self.title(View::Setup, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("description")
            .cdata("Description")
            .close();
        div.input()
            .id("description")
            .maxlength(32)
            .size(24)
            .value(&self.description);
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("uri").cdata("URI").close();
        div.input()
            .id("uri")
            .maxlength(256)
            .size(28)
            .value(&self.uri);
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("comm_config")
            .cdata("Comm Config")
            .close();
        anc.comm_configs_html(self, &mut div.select());
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("poll_enabled")
            .cdata("Poll Enabled")
            .close();
        let mut input = div.input();
        input.id("poll_enabled").r#type("checkbox");
        if self.poll_enabled {
            input.checked();
        }
        div.close();
        self.footer_html(true, &mut page.frag::<html::Div>());
        String::from(page)
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
