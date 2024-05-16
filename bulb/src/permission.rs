// Copyright (C) 2022-2024  Minnesota Department of Transportation
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
use crate::card::{AncillaryData, Card, View, NAME};
use crate::error::{Error, Result};
use crate::fetch::Uri;
use crate::item::ItemState;
use crate::role::Role;
use crate::util::{ContainsLower, Doc, Fields, HtmlStr, Input, Select};
use resources::Res;
use serde::{Deserialize, Serialize};
use serde_json::{Map, Value};
use std::borrow::Cow;
use std::iter::empty;
use wasm_bindgen::JsValue;

/// Permission
#[derive(Debug, Default, Deserialize, Serialize, PartialEq)]
pub struct Permission {
    pub id: u32,
    pub role: String,
    pub resource_n: String,
    pub hashtag: Option<String>,
    pub access_n: u32,
}

/// Resource Type
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct ResourceType {
    pub name: String,
    pub base: bool,
}

/// Ancillary permission data
#[derive(Debug, Default)]
pub struct PermissionAnc {
    pub resource_types: Option<Vec<ResourceType>>,
    pub roles: Option<Vec<Role>>,
}

const RESOURCE_TYPE_URI: &str = "/iris/lut/resource_type";
const ROLE_URI: &str = "/iris/api/role";

impl AncillaryData for PermissionAnc {
    type Primary = Permission;

    /// Get URI iterator
    fn uri_iter(
        &self,
        _pri: &Permission,
        view: View,
    ) -> Box<dyn Iterator<Item = Uri>> {
        match view {
            View::Create | View::Edit => Box::new(
                [RESOURCE_TYPE_URI.into(), ROLE_URI.into()].into_iter(),
            ),
            _ => Box::new(empty()),
        }
    }

    /// Put ancillary data
    fn set_data(
        &mut self,
        _pri: &Permission,
        uri: Uri,
        data: JsValue,
    ) -> Result<bool> {
        if uri.as_str() == RESOURCE_TYPE_URI {
            self.resource_types = Some(serde_wasm_bindgen::from_value(data)?);
        } else {
            self.roles = Some(serde_wasm_bindgen::from_value(data)?);
        }
        Ok(false)
    }
}

impl PermissionAnc {
    /// Create an HTML `select` element of resource types
    fn resource_types_html(&self, pri: &Permission) -> String {
        let mut html = String::new();
        html.push_str("<select id='resource_n'>");
        if let Some(resource_types) = &self.resource_types {
            for resource_type in resource_types {
                if resource_type.base {
                    html.push_str("<option");
                    if pri.resource_n == resource_type.name {
                        html.push_str(" selected");
                    }
                    html.push('>');
                    html.push_str(&resource_type.name);
                    html.push_str("</option>");
                }
            }
        }
        html.push_str("</select>");
        html
    }

    /// Create an HTML `select` element of roles
    fn roles_html(&self, pri: &Permission) -> String {
        let mut html = String::new();
        html.push_str("<select id='role'>");
        if let Some(roles) = &self.roles {
            for role in roles {
                html.push_str("<option");
                if pri.role == role.name {
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

/// Get item state for an access value
fn item_state(access_n: u32) -> ItemState {
    match access_n {
        1 => ItemState::View,
        2 => ItemState::Operate,
        3 => ItemState::Manage,
        4 => ItemState::Configure,
        _ => ItemState::Unknown,
    }
}

/// Create an HTML `select` element of access
fn access_html(selected: u32) -> String {
    let mut html = String::new();
    html.push_str("<select id='access_n'>");
    for access_n in 1..=4 {
        html.push_str("<option value='");
        html.push_str(&access_n.to_string());
        html.push('\'');
        if selected == access_n {
            html.push_str(" selected");
        }
        html.push('>');
        let item = item_state(access_n);
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
        let role = doc.select_parse::<String>("role");
        let resource_n = doc.select_parse::<String>("resource_n");
        if let (Some(role), Some(resource_n)) = (role, resource_n) {
            let mut obj = Map::new();
            obj.insert("role".to_string(), Value::String(role));
            obj.insert("resource_n".to_string(), Value::String(resource_n));
            return Ok(Value::Object(obj).to_string());
        }
        Err(Error::Parse())
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let id = self.id;
        let role = HtmlStr::new(&self.role);
        let access = item_state(self.access_n);
        let resource = HtmlStr::new(&self.resource_n);
        format!(
            "<div class='{NAME} end'>{role} {access} {id}</div>\
            <div class='info fill'>{resource}</div>"
        )
    }

    /// Convert to Edit HTML
    fn to_html_edit(&self, anc: &PermissionAnc) -> String {
        let role = anc.roles_html(self);
        let resource = anc.resource_types_html(self);
        let hashtag = HtmlStr::new(&self.hashtag);
        let access = access_html(self.access_n);
        format!(
            "<div class='row'>\
               <label for='role'>Role</label>\
               {role}\
            </div>\
            <div class='row'>\
              <label for='resource_n'>Resource</label>\
              {resource}\
            </div>\
            <div class='row'>\
               <label for='hashtag'>Hashtag</label>\
               <input id='hashtag' maxlength='16' size='16' value='{hashtag}'>\
            </div>\
            <div class='row'>\
              <label for='access_n'>Access</label>\
              {access}\
            </div>"
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

    /// Get the resource
    fn res() -> Res {
        Res::Permission
    }

    /// Get the name
    fn name(&self) -> Cow<str> {
        Cow::Owned(self.id.to_string())
    }

    /// Set the name
    fn with_name(self, _name: &str) -> Self {
        self
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, _anc: &PermissionAnc) -> bool {
        self.id.to_string().contains(search)
            || item_state(self.access_n).is_match(search)
            || self.role.contains_lower(search)
            || self.resource_n.contains(search)
    }

    /// Get row for Create card
    fn to_html_create(&self, anc: &PermissionAnc) -> String {
        let role = anc.roles_html(self);
        let resource = anc.resource_types_html(self);
        format!(
            "<div class='row'>\
              <label for='role'>Role</label>\
              {role}\
            </div>\
            <div class='row'>\
              <label for='resource_n'>Resource</label>\
              {resource}\
            </div>"
        )
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &PermissionAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Edit => self.to_html_edit(anc),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Edit form
    fn changed_fields(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_select("role", &self.role);
        fields.changed_select("resource_n", &self.resource_n);
        fields.changed_input("hashtag", &self.hashtag);
        fields.changed_select("access_n", self.access_n);
        fields.into_value().to_string()
    }
}
