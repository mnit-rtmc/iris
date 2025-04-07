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
use crate::error::{Error, Result};
use crate::item::ItemState;
use crate::role::Role;
use crate::util::{ContainsLower, Doc, Fields, HtmlStr, Input, Select};
use resources::Res;
use serde::Deserialize;
use serde_json::{Map, Value};
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// Permission
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Permission {
    pub name: String,
    pub role: String,
    pub base_resource: String,
    pub hashtag: Option<String>,
    pub access_level: u32,
}

/// Resource Type
#[derive(Debug, Default, Deserialize)]
pub struct ResourceType {
    pub name: String,
    pub base: Option<String>,
}

/// Ancillary permission data
#[derive(Debug)]
pub struct PermissionAnc {
    assets: Vec<Asset>,
    pub resource_types: Vec<ResourceType>,
    pub roles: Vec<Role>,
}

impl AncillaryData for PermissionAnc {
    type Primary = Permission;

    /// Construct ancillary permission data
    fn new(_pri: &Permission, view: View) -> Self {
        let assets = match view {
            View::Create | View::Setup => {
                vec![Asset::ResourceTypes, Asset::Roles]
            }
            _ => Vec::new(),
        };
        let resource_types = Vec::new();
        let roles = Vec::new();
        PermissionAnc {
            assets,
            resource_types,
            roles,
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        _pri: &Permission,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::ResourceTypes => {
                self.resource_types = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::Roles => {
                self.roles = serde_wasm_bindgen::from_value(value)?;
            }
            _ => unreachable!(),
        }
        Ok(())
    }
}

impl PermissionAnc {
    /// Create an HTML `select` element of resource types
    fn resource_types_html(&self, pri: &Permission) -> String {
        let mut html = String::new();
        html.push_str("<select id='base_resource'>");
        for resource_type in &self.resource_types {
            if resource_type.base.is_none() {
                html.push_str("<option");
                if pri.base_resource == resource_type.name {
                    html.push_str(" selected");
                }
                html.push('>');
                html.push_str(&resource_type.name);
                html.push_str("</option>");
            }
        }
        html.push_str("</select>");
        html
    }

    /// Create an HTML `select` element of roles
    fn roles_html(&self, pri: &Permission) -> String {
        let mut html = String::new();
        html.push_str("<select id='role'>");
        for role in &self.roles {
            html.push_str("<option");
            if pri.role == role.name {
                html.push_str(" selected");
            }
            html.push('>');
            html.push_str(&role.name);
            html.push_str("</option>");
        }
        html.push_str("</select>");
        html
    }
}

/// Get item state for an access level
fn item_state(access_level: u32) -> ItemState {
    match access_level {
        1 => ItemState::View,
        2 => ItemState::Operate,
        3 => ItemState::Manage,
        4 => ItemState::Configure,
        _ => ItemState::Unknown,
    }
}

/// Create an HTML `select` element of access level
fn access_level_html(selected: u32) -> String {
    let mut html = String::new();
    html.push_str("<select id='access_level'>");
    for access in 1..=4 {
        html.push_str("<option value='");
        html.push_str(&access.to_string());
        html.push('\'');
        if selected == access {
            html.push_str(" selected");
        }
        html.push('>');
        let item = item_state(access);
        html.push_str(item.code());
        html.push(' ');
        html.push_str(item.description());
        html.push_str("</option>");
    }
    html.push_str("</select>");
    html
}

impl Permission {
    /// Get value to create a new object
    pub fn create_value(doc: &Doc) -> Result<String> {
        let name = doc
            .input_option_string("create_name")
            .ok_or(Error::ElemIdNotFound("create_name"))?;
        let role = doc
            .select_parse::<String>("role")
            .ok_or(Error::ElemIdNotFound("role"))?;
        let base_resource = doc
            .select_parse::<String>("base_resource")
            .ok_or(Error::ElemIdNotFound("base_resource"))?;
        let mut obj = Map::new();
        obj.insert("name".to_string(), Value::String(name));
        obj.insert("role".to_string(), Value::String(role));
        obj.insert("base_resource".to_string(), Value::String(base_resource));
        Ok(Value::Object(obj).to_string())
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let name = &self.name;
        let role = HtmlStr::new(&self.role);
        let access = item_state(self.access_level);
        let resource = HtmlStr::new(&self.base_resource);
        let hashtag = HtmlStr::new(&self.hashtag);
        format!(
            "<div class='title row'>{role} {access} {name}</div>\
            <div class='info fill'>{resource}<span>{hashtag}</span></div>"
        )
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &PermissionAnc) -> String {
        let title = String::from(self.title(View::Setup));
        let role = anc.roles_html(self);
        let resource = anc.resource_types_html(self);
        let hashtag = HtmlStr::new(&self.hashtag);
        let access = access_level_html(self.access_level);
        let footer = self.footer(true);
        format!(
            "{title}\
            <div class='row'>\
               <label for='role'>Role</label>\
               {role}\
            </div>\
            <div class='row'>\
              <label for='base_resource'>Resource</label>\
              {resource}\
            </div>\
            <div class='row'>\
               <label for='hashtag'>Hashtag</label>\
               <input id='hashtag' maxlength='16' size='16' value='{hashtag}'>\
            </div>\
            <div class='row'>\
              <label for='access_level'>Access</label>\
              {access}\
            </div>\
            {footer}"
        )
    }
}

impl Card for Permission {
    type Ancillary = PermissionAnc;

    /// Display name
    const DNAME: &'static str = "🗝️ Permission";

    /// All item states as html options
    const ITEM_STATES: &'static str = "<option value=''>all ↴\
         <option value='👁️'>👁️ view\
         <option value='👉'>👉 operate\
         <option value='💡'>💡 manage\
         <option value='🔧'>🔧 configure";

    /// Suggested name prefix
    const PREFIX: &'static str = "prm";

    /// Get the resource
    fn res() -> Res {
        Res::Permission
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
    fn is_match(&self, search: &str, _anc: &PermissionAnc) -> bool {
        self.name.contains(search)
            || item_state(self.access_level).is_match(search)
            || self.role.contains_lower(search)
            || self.base_resource.contains(search)
    }

    /// Get row for Create card
    fn to_html_create(&self, anc: &PermissionAnc) -> String {
        let name = HtmlStr::new(self.name());
        let role = anc.roles_html(self);
        let resource = anc.resource_types_html(self);
        format!(
            "<div class='row'>\
              <label for='create_name'>Name</label>\
              <input id='create_name' maxlength='24' size='24' value='{name}'>\
            </div>\
            <div class='row'>\
              <label for='role'>Role</label>\
              {role}\
            </div>\
            <div class='row'>\
              <label for='base_resource'>Resource</label>\
              {resource}\
            </div>"
        )
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &PermissionAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_select("role", &self.role);
        fields.changed_select("base_resource", &self.base_resource);
        fields.changed_input("hashtag", &self.hashtag);
        fields.changed_select("access_level", self.access_level);
        fields.into_value().to_string()
    }
}
