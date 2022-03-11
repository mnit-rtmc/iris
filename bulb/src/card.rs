// Copyright (C) 2022  Minnesota Department of Transportation
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
use crate::alarm::Alarm;
use crate::cabinetstyle::CabinetStyle;
use crate::commconfig::CommConfig;
use crate::commlink::CommLink;
use crate::controller::Controller;
use crate::error::{Error, Result};
use crate::fetch::{fetch_delete, fetch_get, fetch_patch, fetch_post};
use crate::modem::Modem;
use crate::permission::Permission;
use crate::role::Role;
use crate::user::User;
use crate::util::Dom;
use percent_encoding::{utf8_percent_encode, NON_ALPHANUMERIC};
use serde::de::DeserializeOwned;
use serde_json::map::Map;
use serde_json::Value;
use std::fmt;
use wasm_bindgen::JsValue;
use web_sys::Document;

/// CSS class for titles
const TITLE: &str = "title";

/// CSS class for names
pub const NAME: &str = "ob_name";

/// Compact "Create" card
const CREATE_COMPACT: &str = "<span class='create'>Create 🆕</span>";

/// Type of card
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum CardType {
    /// Compact in list
    Compact,

    /// Create compact card
    CreateCompact,

    /// Create card
    Create,

    /// Status card
    Status,

    /// Edit card
    Edit,
}

impl CardType {
    /// Is the card compact?
    pub fn is_compact(self) -> bool {
        matches!(self, CardType::Compact | CardType::CreateCompact)
    }

    /// Is the card a create card?
    pub fn is_create(self) -> bool {
        matches!(self, CardType::Create | CardType::CreateCompact)
    }

    /// Get compact card type
    pub fn compact(self) -> Self {
        if self.is_create() {
            CardType::CreateCompact
        } else {
            CardType::Compact
        }
    }
}

/// A card can be displayed in a card list
pub trait Card: fmt::Display + DeserializeOwned {
    const TNAME: &'static str;
    const ENAME: &'static str;
    const UNAME: &'static str;
    const HAS_STATUS: bool = false;

    /// Create from a JSON value
    fn new(json: &JsValue) -> Result<Self> {
        Ok(json.into_serde::<Self>()?)
    }

    /// Get geo location of card
    fn geo_loc(&self) -> Option<&str> {
        None
    }

    /// Build form using JSON value
    fn build_card(json: JsValue, ct: CardType) -> Result<String> {
        let val = Self::new(&json)?;
        match ct {
            CardType::Compact => Ok(val.to_html_compact()),
            CardType::Status if Self::HAS_STATUS => Ok(val.status_card()),
            _ => Ok(val.edit_card()),
        }
    }

    /// Build a create card
    fn build_create_card(name: &str) -> String {
        let ename = Self::ENAME;
        let create = Self::html_create(name);
        format!(
            "<div class='row'>\
              <div class='{TITLE}'>{ename}</div>\
              <span class='{NAME}'>🆕 \
                <button id='ob_close' type='button'>X</button>\
              </span>\
            </div>\
            {create}
            <div class='row'>\
              <span></span>\
              <button id='ob_save' type='button'>🖍️ Save</button>\
            </div>"
        )
    }

    /// Get row for create card
    fn html_create(name: &str) -> String {
        format!(
            "<div class='row'>\
              <label for='create_name'>Name</label>\
              <input id='create_name' maxlength='24' size='24' value='{name}'/>\
            </div>"
        )
    }

    /// Get value to create a new object
    fn create_value(doc: &Document) -> Result<String> {
        if let Some(name) = doc.input_parse::<String>("create_name") {
            if !name.is_empty() {
                let mut obj = Map::new();
                obj.insert("name".to_string(), Value::String(name));
                return Ok(Value::Object(obj).to_string());
            }
        }
        Err(Error::NameMissing())
    }

    /// Build a status card
    fn status_card(&self) -> String {
        let ename = Self::ENAME;
        let status = self.to_html_status();
        let geo_loc = match self.geo_loc() {
            Some(geo_loc) => {
                format!(
                    "<button id='ob_loc' name='{geo_loc}' \
                    type='button'>🗺️ Location</button>"
                )
            }
            None => "".into(),
        };
        format!(
            "<div class='row'>\
              <div class='{TITLE}'>{ename}</div>\
              <span class='{NAME}'>{self} \
                <button id='ob_close' type='button'>X</button>\
              </span>\
            </div>\
            {status}\
            <div class='row'>\
              <span></span>\
              {geo_loc}\
              <button id='ob_edit' type='button'>📝 Edit</button>\
            </div>"
        )
    }

    /// Convert to status HTML
    fn to_html_status(&self) -> String {
        unreachable!()
    }

    /// Build an edit card
    fn edit_card(&self) -> String {
        let ename = Self::ENAME;
        let edit = self.to_html_edit();
        format!(
            "<div class='row'>\
              <div class='{TITLE}'>{ename}</div>\
              <span class='{NAME}'>{self} \
                <button id='ob_close' type='button'>X</button>\
              </span>\
            </div>\
            {edit}\
            <div class='row'>\
              <span></span>\
              <button id='ob_delete' type='button'>🗑️ Delete</button>\
              <button id='ob_save' type='button'>🖍️ Save</button>\
            </div>"
        )
    }

    /// Convert to edit HTML
    fn to_html_edit(&self) -> String {
        unreachable!()
    }

    /// Get changed fields from Edit form
    fn changed_fields(doc: &Document, json: &JsValue) -> Result<String>;

    /// Check if a search string matches
    fn is_match(&self, _search: &str) -> bool {
        false
    }

    /// Get next suggested name
    fn next_name(_obs: &[Self]) -> String {
        "".into()
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String;
}

/// Get attribute for disabled cards
pub fn disabled_attr(enabled: bool) -> &'static str {
    if enabled {
        ""
    } else {
        " class='disabled'"
    }
}

/// Get the URI of an object
fn uri_name(uname: &str, name: &str) -> String {
    let nm = utf8_percent_encode(name, NON_ALPHANUMERIC);
    format!("/iris/api/{uname}/{nm}")
}

/// Fetch card list for a resource type
pub async fn res_list(res: &str, search: &str) -> Result<String> {
    match res {
        Alarm::TNAME => res_build_list::<Alarm>(search).await,
        CabinetStyle::TNAME => res_build_list::<CabinetStyle>(search).await,
        CommConfig::TNAME => res_build_list::<CommConfig>(search).await,
        CommLink::TNAME => res_build_list::<CommLink>(search).await,
        Controller::TNAME => res_build_list::<Controller>(search).await,
        Modem::TNAME => res_build_list::<Modem>(search).await,
        Permission::TNAME => res_build_list::<Permission>(search).await,
        Role::TNAME => res_build_list::<Role>(search).await,
        User::TNAME => res_build_list::<User>(search).await,
        _ => Ok("".into()),
    }
}

/// Fetch JSON array and build card list
async fn res_build_list<C: Card>(search: &str) -> Result<String> {
    let json = fetch_get(&format!("/iris/api/{}", C::UNAME)).await?;
    let search = search.to_lowercase();
    let tname = C::TNAME;
    let mut html = String::new();
    html.push_str("<ul class='cards'>");
    let obs = json.into_serde::<Vec<C>>()?;
    let next_name = C::next_name(&obs);
    // the "Create" card has id "{tname}_" and next available name
    html.push_str(&format!(
        "<li id='{tname}_' name='{next_name}' class='card'>\
            {CREATE_COMPACT}\
        </li>"
    ));
    for ob in obs.iter().filter(|ob| {
        search.is_empty() || search.split(' ').all(|s| ob.is_match(s))
    }) {
        html.push_str(&format!(
            "<li id='{tname}_{ob}' name='{ob}' class='card'>"
        ));
        html.push_str(&ob.to_html_compact());
        html.push_str("</li>");
    }
    html.push_str("</ul>");
    Ok(html)
}

/// Get a card for a resource type
pub async fn res_get(res: &str, name: &str, ct: CardType) -> Result<String> {
    match res {
        Alarm::TNAME => res_build_card::<Alarm>(name, ct).await,
        CabinetStyle::TNAME => res_build_card::<CabinetStyle>(name, ct).await,
        CommConfig::TNAME => res_build_card::<CommConfig>(name, ct).await,
        CommLink::TNAME => res_build_card::<CommLink>(name, ct).await,
        Controller::TNAME => res_build_card::<Controller>(name, ct).await,
        Modem::TNAME => res_build_card::<Modem>(name, ct).await,
        Permission::TNAME => res_build_card::<Permission>(name, ct).await,
        Role::TNAME => res_build_card::<Role>(name, ct).await,
        User::TNAME => res_build_card::<User>(name, ct).await,
        _ => Ok("".into()),
    }
}

/// Fetch and build a card
async fn res_build_card<C: Card>(name: &str, ct: CardType) -> Result<String> {
    match ct {
        CardType::CreateCompact => Ok(CREATE_COMPACT.into()),
        CardType::Create => Ok(C::build_create_card(name)),
        _ => {
            let uri = uri_name(C::UNAME, name);
            let json = fetch_get(&uri).await?;
            C::build_card(json, ct)
        }
    }
}

/// Create new resource from create card
pub async fn res_create(res: &str) -> Result<()> {
    match res {
        Alarm::TNAME => create_and_post::<Alarm>().await,
        CabinetStyle::TNAME => create_and_post::<CabinetStyle>().await,
        CommConfig::TNAME => create_and_post::<CommConfig>().await,
        CommLink::TNAME => create_and_post::<CommLink>().await,
        Controller::TNAME => create_and_post::<Controller>().await,
        Modem::TNAME => create_and_post::<Modem>().await,
        Permission::TNAME => create_and_post::<Permission>().await,
        Role::TNAME => create_and_post::<Role>().await,
        User::TNAME => create_and_post::<User>().await,
        _ => Ok(()),
    }
}

/// Create a new object
async fn create_and_post<C: Card>() -> Result<()> {
    if let Some(window) = web_sys::window() {
        if let Some(doc) = window.document() {
            let value = C::create_value(&doc)?;
            let json = value.into();
            fetch_post(&format!("/iris/api/{}", C::UNAME), &json).await?;
        }
    }
    Ok(())
}

/// Save changed fields on card
pub async fn res_save(res: &str, name: &str) -> Result<()> {
    match res {
        Alarm::TNAME => fetch_save_card::<Alarm>(name).await,
        CabinetStyle::TNAME => fetch_save_card::<CabinetStyle>(name).await,
        CommConfig::TNAME => fetch_save_card::<CommConfig>(name).await,
        CommLink::TNAME => fetch_save_card::<CommLink>(name).await,
        Controller::TNAME => fetch_save_card::<Controller>(name).await,
        Modem::TNAME => fetch_save_card::<Modem>(name).await,
        Permission::TNAME => fetch_save_card::<Permission>(name).await,
        Role::TNAME => fetch_save_card::<Role>(name).await,
        User::TNAME => fetch_save_card::<User>(name).await,
        _ => Ok(()),
    }
}

/// Save changed fields on card
async fn fetch_save_card<C: Card>(name: &str) -> Result<()> {
    if let Some(window) = web_sys::window() {
        if let Some(doc) = window.document() {
            let uri = uri_name(C::UNAME, name);
            let json = fetch_get(&uri).await?;
            let v = C::changed_fields(&doc, &json)?;
            fetch_patch(&uri, &v.into()).await?;
        }
    }
    Ok(())
}

/// Delete card resource
pub async fn res_delete(res: &str, name: &str) -> Result<()> {
    match res {
        Alarm::TNAME => res_delete_card::<Alarm>(name).await,
        CabinetStyle::TNAME => res_delete_card::<CabinetStyle>(name).await,
        CommConfig::TNAME => res_delete_card::<CommConfig>(name).await,
        CommLink::TNAME => res_delete_card::<CommLink>(name).await,
        Controller::TNAME => res_delete_card::<Controller>(name).await,
        Modem::TNAME => res_delete_card::<Modem>(name).await,
        Permission::TNAME => res_delete_card::<Permission>(name).await,
        Role::TNAME => res_delete_card::<Role>(name).await,
        User::TNAME => res_delete_card::<User>(name).await,
        _ => Ok(()),
    }
}

/// Delete card resource
async fn res_delete_card<C: Card>(name: &str) -> Result<()> {
    let uri = uri_name(C::UNAME, name);
    fetch_delete(&uri).await
}
