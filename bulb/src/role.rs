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
use crate::domain::Domain;
use crate::error::Result;
use crate::item::ItemState;
use crate::permission::Permission;
use crate::util::{ContainsLower, Fields, Input};
use crate::view::View;
use hatmil::{Tree, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// Resource Type
#[derive(Debug, Default, Deserialize)]
pub struct ResourceType {
    pub name: String,
    pub base: Option<String>,
}

/// Role
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Role {
    pub name: String,
    pub enabled: bool,
    // secondary attributes
    pub domains: Option<Vec<String>>,
}

/// Ancillary role data
#[derive(Debug, Default)]
pub struct RoleAnc {
    assets: Vec<Asset>,
    pub resource_types: Vec<ResourceType>,
    pub permissions: Vec<Permission>,
    pub domains: Vec<Domain>,
}

impl AncillaryData for RoleAnc {
    type Primary = Role;

    /// Construct ancillary role data
    fn new(_pri: &Role, view: View) -> Self {
        let assets = match view {
            View::Setup => {
                vec![Asset::ResourceTypes, Asset::Permissions, Asset::Domains]
            }
            _ => Vec::new(),
        };
        let resource_types = Vec::new();
        let permissions = Vec::new();
        let domains = Vec::new();
        RoleAnc {
            assets,
            resource_types,
            permissions,
            domains,
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &Role,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::ResourceTypes => {
                self.resource_types = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::Permissions => {
                let mut permissions: Vec<Permission> =
                    serde_wasm_bindgen::from_value(value)?;
                permissions.retain(|p| p.role == pri.name);
                permissions.sort();
                self.permissions = permissions;
            }
            Asset::Domains => {
                self.domains = serde_wasm_bindgen::from_value(value)?;
            }
            _ => unreachable!(),
        }
        Ok(())
    }
}

impl RoleAnc {
    /// Make permissions HTML table
    fn permissions_html<'p>(&self, pri: &Role, div: &'p mut html::Div<'p>) {
        let mut details = div.details();
        details.summary().cdata("🗝️ Permissions").close();
        let mut table = details.table();
        for res in &self.resource_types {
            if res.base.is_some() {
                continue;
            }
            let mut first = true;
            for perm in &self.permissions {
                if perm.base_resource != res.name {
                    continue;
                }
                if first && perm.hashtag.is_some() {
                    let p = Permission::new(&res.name, &pri.name);
                    p.table_row(&mut table.tr());
                }
                perm.table_row(&mut table.tr());
                first = false;
            }
            if first {
                let p = Permission::new(&res.name, &pri.name);
                p.table_row(&mut table.tr());
            }
        }
        details.close();
    }

    /// Make domains HTML table
    fn domains_html<'p>(&self, pri: &Role, div: &'p mut html::Div<'p>) {
        let mut details = div.details();
        details.summary().cdata("🖧 Domains").close();
        if let Some(domains) = pri.domains.as_ref() {
            for dom in &self.domains {
                let assigned = domains.contains(&dom.name);
                dom.input_html(assigned, &mut details.div());
            }
        }
        details.close();
    }
}

impl Role {
    /// Get item state
    pub fn item_state(&self) -> ItemState {
        if self.enabled {
            ItemState::Available
        } else {
            ItemState::Prohibited
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let mut tree = Tree::new();
        tree.root::<html::Div>()
            .class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(self.item_state().to_string());
        String::from(tree)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &RoleAnc) -> String {
        let mut tree = Tree::new();
        self.title(View::Setup, &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row")
            .label()
            .r#for("enabled")
            .cdata("Enabled")
            .close();
        let mut input = div.input();
        input.id("enabled").r#type("checkbox");
        if self.enabled {
            input.checked();
        }
        div.close();
        div = tree.root::<html::Div>();
        anc.permissions_html(self, &mut div);
        div = tree.root::<html::Div>();
        anc.domains_html(self, &mut div);
        self.footer_html(true, &mut tree.root::<html::Div>());
        String::from(tree)
    }
}

impl Card for Role {
    type Ancillary = RoleAnc;

    /// Get the resource
    fn res() -> Res {
        Res::Role
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[ItemState::Available, ItemState::Prohibited]
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
    fn is_match(&self, search: &str, _anc: &RoleAnc) -> bool {
        self.name.contains_lower(search) || self.item_state().is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &RoleAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("enabled", self.enabled);
        fields.into_value().to_string()
    }
}
