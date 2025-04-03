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
use crate::error::Result;
use crate::item::ItemState;
use crate::role::Role;
use crate::util::{ContainsLower, Fields, HtmlStr, Input, Select};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// User
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct User {
    pub name: String,
    pub full_name: String,
    pub role: Option<String>,
    pub enabled: bool,
    // secondary attributes
    pub dn: Option<String>,
}

/// Ancillary user data
#[derive(Debug)]
pub struct UserAnc {
    assets: Vec<Asset>,
    pub roles: Option<Vec<Role>>,
}

impl AncillaryData for UserAnc {
    type Primary = User;

    /// Construct ancillary user data
    fn new(_pri: &User, _view: View) -> Self {
        let assets = vec![Asset::Roles];
        let roles = None;
        UserAnc { assets, roles }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        _pri: &User,
        _asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        self.roles = Some(serde_wasm_bindgen::from_value(value)?);
        Ok(())
    }
}

impl UserAnc {
    /// Get item state
    fn item_state(&self, role: &str) -> ItemState {
        if let Some(roles) = &self.roles {
            for r in roles {
                if r.name == role {
                    return r.item_state();
                }
            }
        }
        ItemState::Inactive
    }

    /// Create an HTML `select` element of roles
    fn roles_html(&self, pri: &User) -> String {
        let mut html = String::new();
        html.push_str("<select id='role'>");
        html.push_str("<option></option>");
        if let Some(roles) = &self.roles {
            for role in roles {
                html.push_str("<option");
                if pri.role.as_ref() == Some(&role.name) {
                    html.push_str(" selected");
                }
                html.push('>');
                html.push_str(&role.name);
                html.push_str("</option>");
            }
        }
        html.push_str("</select>");
        html
    }
}

impl User {
    /// Get item state
    fn item_state(&self, anc: &UserAnc) -> ItemState {
        if self.enabled {
            if let Some(role) = &self.role {
                return anc.item_state(role);
            }
        }
        ItemState::Inactive
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &UserAnc) -> String {
        let name = HtmlStr::new(self.name());
        let item_state = self.item_state(anc);
        format!("<div class='title row'>{name} {item_state}</div>")
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &UserAnc) -> String {
        let title = self.title(View::Setup).build();
        let full_name = HtmlStr::new(&self.full_name);
        let dn = HtmlStr::new(&self.dn);
        let role = anc.roles_html(self);
        let enabled = if self.enabled { " checked" } else { "" };
        let footer = self.footer(true);
        format!(
            "{title}\
            <div class='row'>\
               <label for='full_name'>Full Name</label>\
               <input id='full_name' maxlength='31' size='20' \
                      value='{full_name}'>\
            </div>\
            <div class='row'>\
               <label for='dn'>Dn</label>\
               <input id='dn' maxlength='128' size='32' value='{dn}'>\
            </div>\
            <div class='row'>\
               <label for='role'>Role</label>\
               {role}\
            </div>\
            <div class='row'>\
              <label for='enabled'>Enabled</label>\
              <input id='enabled' type='checkbox'{enabled}>\
            </div>\
            {footer}"
        )
    }
}

impl Card for User {
    type Ancillary = UserAnc;

    /// Display name
    const DNAME: &'static str = "👤 User";

    /// All item states as html options
    const ITEM_STATES: &'static str = "<option value=''>all ↴\
         <option value='🔹'>🔹 available\
         <option value='🔻'>🔻 inactive";

    /// Get the resource
    fn res() -> Res {
        Res::User
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
    fn is_match(&self, search: &str, anc: &UserAnc) -> bool {
        self.name.contains(search)
            || self.full_name.contains_lower(search)
            || self.role.contains_lower(search)
            || self.item_state(anc).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &UserAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("full_name", &self.full_name);
        fields.changed_input("dn", &self.dn);
        fields.changed_select("role", &self.role);
        fields.changed_input("enabled", self.enabled);
        fields.into_value().to_string()
    }
}
