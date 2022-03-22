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
use crate::beacon::Beacon;
use crate::cabinetstyle::CabinetStyle;
use crate::commconfig::CommConfig;
use crate::commlink::CommLink;
use crate::controller::Controller;
use crate::error::{Error, Result};
use crate::fetch::{fetch_delete, fetch_get, fetch_patch, fetch_post};
use crate::geoloc::GeoLoc;
use crate::lanemarking::LaneMarking;
use crate::modem::Modem;
use crate::permission::Permission;
use crate::role::Role;
use crate::user::User;
use crate::util::{Dom, HtmlStr};
use crate::weathersensor::WeatherSensor;
use percent_encoding::{utf8_percent_encode, NON_ALPHANUMERIC};
use serde::de::DeserializeOwned;
use serde_json::map::Map;
use serde_json::Value;
use std::borrow::{Borrow, Cow};
use std::fmt;
use wasm_bindgen::JsValue;
use web_sys::Document;

/// CSS class for titles
const TITLE: &str = "title";

/// CSS class for names
pub const NAME: &str = "ob_name";

/// Compact "Create" card
const CREATE_COMPACT: &str = "<span class='create'>Create 🆕</span>";

/// Search term
enum Search {
    /// Empty search (matches anything)
    Empty(),
    /// Normal search
    Normal(String),
    /// Exact (multi-word) search
    Exact(String),
}

/// Card View
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum View {
    /// Compact Create view
    CreateCompact,

    /// Create view
    Create,

    /// Compact view
    Compact,

    /// Status view
    Status,

    /// Edit view
    Edit,

    /// Location view
    Location,

    /// Search view
    Search,
}

impl Search {
    /// Create a new search term
    fn new(se: &str) -> Self {
        let se = se.to_lowercase();
        if se.is_empty() {
            Search::Empty()
        } else if se.starts_with('"') && se.ends_with('"') {
            Search::Exact(se.trim_matches('"').to_string())
        } else {
            Search::Normal(se)
        }
    }

    /// Test if a card matches the search
    fn is_match<C: Card>(&self, res: &C, anc: &C::Ancillary) -> bool {
        match self {
            Search::Empty() => true,
            Search::Normal(se) => se.split(' ').all(|s| res.is_match(s, anc)),
            Search::Exact(se) => res.is_match(se, anc),
        }
    }
}

impl View {
    /// Is the view compact?
    pub fn is_compact(self) -> bool {
        matches!(self, View::Compact | View::CreateCompact)
    }

    /// Is the view a create view?
    pub fn is_create(self) -> bool {
        matches!(self, View::Create | View::CreateCompact)
    }

    /// Get compact view
    pub fn compact(self) -> Self {
        if self.is_create() {
            View::CreateCompact
        } else {
            View::Compact
        }
    }
}

/// Ancillary card view data
pub trait AncillaryData: Default {
    type Resource;

    /// Get ancillary URI
    fn uri(&self, _view: View, _res: &Self::Resource) -> Option<Cow<str>> {
        None
    }

    /// Set ancillary JSON data
    fn set_json(
        &mut self,
        _view: View,
        _res: &Self::Resource,
        _json: JsValue,
    ) -> Result<()> {
        Ok(())
    }
}

/// A card can be displayed in a card list
pub trait Card: Default + fmt::Display + DeserializeOwned {
    const TNAME: &'static str;
    const ENAME: &'static str;
    const UNAME: &'static str;
    const HAS_STATUS: bool = false;

    type Ancillary: AncillaryData<Resource = Self>;

    /// Create from a JSON value
    fn new(json: &JsValue) -> Result<Self> {
        Ok(json.into_serde::<Self>()?)
    }

    /// Set the name
    fn with_name(self, name: &str) -> Self;

    /// Get next suggested name
    fn next_name(_obs: &[Self]) -> String {
        "".into()
    }

    /// Get geo location name
    fn geo_loc(&self) -> Option<&str> {
        None
    }

    /// Check if a search string matches
    fn is_match(&self, _search: &str, _anc: &Self::Ancillary) -> bool {
        false
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String;

    /// Get row for create card
    fn to_html_create(&self, _anc: &Self::Ancillary) -> String {
        format!(
            "<div class='row'>\
              <label for='create_name'>Name</label>\
              <input id='create_name' maxlength='24' size='24' value='{self}'/>\
            </div>"
        )
    }

    /// Convert to status HTML
    fn to_html_status(&self, _anc: &Self::Ancillary) -> String {
        unreachable!()
    }

    /// Convert to edit HTML
    fn to_html_edit(&self, _anc: &Self::Ancillary) -> String {
        unreachable!()
    }

    /// Get changed fields from Edit form
    fn changed_fields(doc: &Document, json: &JsValue) -> Result<String>;

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
fn uri_name(uname: &'static str, name: &str) -> String {
    let nm = utf8_percent_encode(name, NON_ALPHANUMERIC);
    format!("/iris/api/{uname}/{nm}")
}

/// Fetch a JSON object by name
async fn fetch_json(uname: &'static str, name: &str) -> Result<JsValue> {
    let uri = uri_name(uname, name);
    Ok(fetch_get(&uri).await?)
}

/// Fetch JSON resource
async fn fetch_res<C: Card>(name: &str) -> Result<C> {
    let json = fetch_json(C::UNAME, name).await?;
    Ok(C::new(&json)?)
}

/// Fetch card list for a resource type
pub async fn res_list(res: &str, search: &str) -> Result<String> {
    match res {
        Alarm::TNAME => res_build_list::<Alarm>(search).await,
        Beacon::TNAME => res_build_list::<Beacon>(search).await,
        CabinetStyle::TNAME => res_build_list::<CabinetStyle>(search).await,
        CommConfig::TNAME => res_build_list::<CommConfig>(search).await,
        CommLink::TNAME => res_build_list::<CommLink>(search).await,
        Controller::TNAME => res_build_list::<Controller>(search).await,
        LaneMarking::TNAME => res_build_list::<LaneMarking>(search).await,
        Modem::TNAME => res_build_list::<Modem>(search).await,
        Permission::TNAME => res_build_list::<Permission>(search).await,
        Role::TNAME => res_build_list::<Role>(search).await,
        User::TNAME => res_build_list::<User>(search).await,
        WeatherSensor::TNAME => res_build_list::<WeatherSensor>(search).await,
        _ => Ok("".into()),
    }
}

/// Fetch JSON array and build card list
async fn res_build_list<C: Card>(search: &str) -> Result<String> {
    let json = fetch_get(&format!("/iris/api/{}", C::UNAME)).await?;
    let search = Search::new(search);
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
    // Use default value for ancillary data lookup
    let res = C::default();
    let anc = fetch_ancillary(&res, View::Search).await?;
    for res in obs.iter().filter(|res| search.is_match(*res, &anc)) {
        html.push_str(&format!(
            "<li id='{tname}_{res}' name='{res}' class='card'>"
        ));
        html.push_str(&res.to_html_compact());
        html.push_str("</li>");
    }
    html.push_str("</ul>");
    Ok(html)
}

/// Get a card for a resource type
pub async fn res_get(res: &str, name: &str, view: View) -> Result<String> {
    match res {
        Alarm::TNAME => res_build_card::<Alarm>(name, view).await,
        Beacon::TNAME => res_build_card::<Beacon>(name, view).await,
        CabinetStyle::TNAME => res_build_card::<CabinetStyle>(name, view).await,
        CommConfig::TNAME => res_build_card::<CommConfig>(name, view).await,
        CommLink::TNAME => res_build_card::<CommLink>(name, view).await,
        Controller::TNAME => res_build_card::<Controller>(name, view).await,
        GeoLoc::TNAME => res_build_card::<GeoLoc>(name, view).await,
        LaneMarking::TNAME => res_build_card::<LaneMarking>(name, view).await,
        Modem::TNAME => res_build_card::<Modem>(name, view).await,
        Permission::TNAME => res_build_card::<Permission>(name, view).await,
        Role::TNAME => res_build_card::<Role>(name, view).await,
        User::TNAME => res_build_card::<User>(name, view).await,
        WeatherSensor::TNAME => {
            res_build_card::<WeatherSensor>(name, view).await
        }
        _ => Ok("".into()),
    }
}

/// Fetch and build a card
async fn res_build_card<C: Card>(name: &str, view: View) -> Result<String> {
    match view {
        View::CreateCompact => Ok(CREATE_COMPACT.into()),
        View::Create => {
            let res = C::default().with_name(name);
            let anc = fetch_ancillary(&res, view).await?;
            Ok(html_card_create(C::ENAME, &res.to_html_create(&anc)))
        }
        View::Compact => {
            let res = fetch_res::<C>(name).await?;
            Ok(res.to_html_compact())
        }
        View::Status if C::HAS_STATUS => {
            let res = fetch_res::<C>(name).await?;
            let anc = fetch_ancillary(&res, view).await?;
            Ok(html_card_status(
                C::ENAME,
                name,
                &res.to_html_status(&anc),
                res.geo_loc().is_some(),
            ))
        }
        View::Location => {
            let res = fetch_res::<C>(name).await?;
            if let Some(geo_loc) = res.geo_loc() {
                let res = fetch_res::<GeoLoc>(geo_loc).await?;
                let anc = fetch_ancillary(&res, View::Edit).await?;
                Ok(html_card_edit(
                    GeoLoc::ENAME,
                    geo_loc,
                    &res.to_html_edit(&anc),
                ))
            } else {
                Err(Error::NameMissing())
            }
        }
        _ => {
            let res = fetch_res::<C>(name).await?;
            let anc = fetch_ancillary(&res, View::Edit).await?;
            Ok(html_card_edit(C::ENAME, name, &res.to_html_edit(&anc)))
        }
    }
}

/// Fetch ancillary data
async fn fetch_ancillary<C: Card>(res: &C, view: View) -> Result<C::Ancillary> {
    let mut anc = C::Ancillary::default();
    while let Some(uri) = anc.uri(view, res) {
        let json = fetch_get(uri.borrow()).await?;
        anc.set_json(view, res, json)?;
    }
    Ok(anc)
}

/// Build a create card
fn html_card_create(ename: &'static str, create: &str) -> String {
    format!(
        "<div class='row'>\
          <span class='{TITLE}'>{ename}</span>\
          <span class='{TITLE}'>Create</span>\
          <span class='{NAME}'>🆕</span>\
          <button id='ob_close' type='button'>X</button>\
        </div>\
        {create}
        <div class='row'>\
          <span></span>\
          <button id='ob_save' type='button'>🖍️ Save</button>\
        </div>"
    )
}

/// Build a status card
fn html_card_status(
    ename: &'static str,
    name: &str,
    status: &str,
    has_location: bool,
) -> String {
    let name = HtmlStr::new(name);
    let geo_loc = if has_location {
        "<button id='ob_loc' type='button'>🗺️ Location</button>"
    } else {
        ""
    };
    format!(
        "<div class='row'>\
          <span class='{TITLE}'>{ename}</span>\
          <span class='{TITLE}'>Status</span>\
          <span class='{NAME}'>{name}</span>\
          <button id='ob_close' type='button'>X</button>\
        </div>\
        {status}\
        <div class='row'>\
          <span></span>\
          {geo_loc}\
          <button id='ob_edit' type='button'>📝 Edit</button>\
        </div>"
    )
}

/// Build an edit card
fn html_card_edit(ename: &'static str, name: &str, edit: &str) -> String {
    let name = HtmlStr::new(name);
    format!(
        "<div class='row'>\
          <span class='{TITLE}'>{ename}</span>\
          <span class='{TITLE}'>Edit</span>\
          <span class='{NAME}'>{name}</span>\
          <button id='ob_close' type='button'>X</button>\
        </div>\
        {edit}\
        <div class='row'>\
          <span></span>\
          <button id='ob_delete' type='button'>🗑️ Delete</button>\
          <button id='ob_save' type='button'>🖍️ Save</button>\
        </div>"
    )
}

/// Create new resource from create card
pub async fn res_create(res: &str) -> Result<()> {
    match res {
        Alarm::TNAME => create_and_post::<Alarm>().await,
        Beacon::TNAME => create_and_post::<Beacon>().await,
        CabinetStyle::TNAME => create_and_post::<CabinetStyle>().await,
        CommConfig::TNAME => create_and_post::<CommConfig>().await,
        CommLink::TNAME => create_and_post::<CommLink>().await,
        Controller::TNAME => create_and_post::<Controller>().await,
        LaneMarking::TNAME => create_and_post::<LaneMarking>().await,
        Modem::TNAME => create_and_post::<Modem>().await,
        Permission::TNAME => create_and_post::<Permission>().await,
        Role::TNAME => create_and_post::<Role>().await,
        User::TNAME => create_and_post::<User>().await,
        WeatherSensor::TNAME => create_and_post::<WeatherSensor>().await,
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
        Beacon::TNAME => fetch_save_card::<Beacon>(name).await,
        CabinetStyle::TNAME => fetch_save_card::<CabinetStyle>(name).await,
        CommConfig::TNAME => fetch_save_card::<CommConfig>(name).await,
        CommLink::TNAME => fetch_save_card::<CommLink>(name).await,
        Controller::TNAME => fetch_save_card::<Controller>(name).await,
        GeoLoc::TNAME => fetch_save_card::<GeoLoc>(name).await,
        LaneMarking::TNAME => fetch_save_card::<LaneMarking>(name).await,
        Modem::TNAME => fetch_save_card::<Modem>(name).await,
        Permission::TNAME => fetch_save_card::<Permission>(name).await,
        Role::TNAME => fetch_save_card::<Role>(name).await,
        User::TNAME => fetch_save_card::<User>(name).await,
        WeatherSensor::TNAME => fetch_save_card::<WeatherSensor>(name).await,
        _ => Ok(()),
    }
}

/// Save changed fields on card
async fn fetch_save_card<C: Card>(name: &str) -> Result<()> {
    if let Some(window) = web_sys::window() {
        if let Some(doc) = window.document() {
            let uri = uri_name(C::UNAME, name);
            let json = fetch_get(&uri).await?;
            let changed = C::changed_fields(&doc, &json)?;
            fetch_patch(&uri, &changed.into()).await?;
        }
    }
    Ok(())
}

/// Save changed fields on card
pub async fn res_save_loc(res: &str, name: &str) -> Result<()> {
    let geo_loc = match res {
        Beacon::TNAME => fetch_geo_loc::<Beacon>(name).await?,
        Controller::TNAME => fetch_geo_loc::<Controller>(name).await?,
        LaneMarking::TNAME => fetch_geo_loc::<LaneMarking>(name).await?,
        WeatherSensor::TNAME => fetch_geo_loc::<WeatherSensor>(name).await?,
        _ => None,
    };
    match geo_loc {
        Some(geo_loc) => fetch_save_card::<GeoLoc>(&geo_loc).await,
        None => Ok(()),
    }
}

/// Fetch geo location of a resource
async fn fetch_geo_loc<C: Card>(name: &str) -> Result<Option<String>> {
    let res = fetch_res::<C>(name).await?;
    match res.geo_loc() {
        Some(geo_loc) => Ok(Some(geo_loc.to_string())),
        None => Ok(None),
    }
}

/// Delete card resource
pub async fn res_delete(res: &str, name: &str) -> Result<()> {
    match res {
        Alarm::TNAME => res_delete_card::<Alarm>(name).await,
        Beacon::TNAME => res_delete_card::<Beacon>(name).await,
        CabinetStyle::TNAME => res_delete_card::<CabinetStyle>(name).await,
        CommConfig::TNAME => res_delete_card::<CommConfig>(name).await,
        CommLink::TNAME => res_delete_card::<CommLink>(name).await,
        Controller::TNAME => res_delete_card::<Controller>(name).await,
        LaneMarking::TNAME => res_delete_card::<LaneMarking>(name).await,
        Modem::TNAME => res_delete_card::<Modem>(name).await,
        Permission::TNAME => res_delete_card::<Permission>(name).await,
        Role::TNAME => res_delete_card::<Role>(name).await,
        User::TNAME => res_delete_card::<User>(name).await,
        WeatherSensor::TNAME => res_delete_card::<WeatherSensor>(name).await,
        _ => Ok(()),
    }
}

/// Delete card resource
async fn res_delete_card<C: Card>(name: &str) -> Result<()> {
    let uri = uri_name(C::UNAME, name);
    fetch_delete(&uri).await
}

/// Get resource TNAME from UNAME
pub fn uname_to_tname(uname: &str) -> &'static str {
    match uname {
        Alarm::UNAME => Alarm::TNAME,
        Beacon::UNAME => Beacon::TNAME,
        CabinetStyle::UNAME => CabinetStyle::TNAME,
        CommConfig::UNAME => CommConfig::TNAME,
        CommLink::UNAME => CommLink::TNAME,
        Controller::UNAME => Controller::TNAME,
        LaneMarking::UNAME => LaneMarking::TNAME,
        Modem::UNAME => Modem::TNAME,
        Permission::UNAME => Permission::TNAME,
        Role::UNAME => Role::TNAME,
        User::UNAME => User::TNAME,
        WeatherSensor::UNAME => WeatherSensor::TNAME,
        _ => "Unknown",
    }
}
