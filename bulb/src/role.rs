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
    pub permissions: Vec<Permission>,
}

impl AncillaryData for RoleAnc {
    type Primary = Role;

    /// Construct ancillary role data
    fn new(_pri: &Role, view: View) -> Self {
        let assets = match view {
            View::Setup => vec![Asset::Permissions],
            _ => Vec::new(),
        };
        let permissions = Vec::new();
        RoleAnc {
            assets,
            permissions,
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
        _asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        let mut permissions: Vec<Permission> =
            serde_wasm_bindgen::from_value(value)?;
        permissions.retain(|p| p.role == pri.name);
        permissions.sort();
        self.permissions = permissions;
        Ok(())
    }
}

impl Role {
    /// Get item state
    pub fn item_state(&self) -> ItemState {
        if self.enabled {
            ItemState::Available
        } else {
            ItemState::Inactive
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
        if !anc.permissions.is_empty() {
            div = tree.root::<html::Div>();
            div.class("row").cdata("🗝️ Permissions").close();
            let mut table = tree.root::<html::Table>();
            for perm in &anc.permissions {
                table.raw(perm.to_html_row());
            }
            table.close();
        }
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
