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
use crate::camera::Camera;
use crate::commconfig::CommConfig;
use crate::commlink::CommLink;
use crate::controller::Controller;
use crate::error::{Error, Result};
use crate::fetch::{fetch_delete, fetch_get, fetch_patch, fetch_post};
use crate::geoloc::GeoLoc;
use crate::lanemarking::LaneMarking;
use crate::modem::Modem;
use crate::permission::Permission;
use crate::rampmeter::RampMeter;
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

/// Resource types
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum Resource {
    Alarm,
    Beacon,
    CabinetStyle,
    Camera,
    CommConfig,
    CommLink,
    Controller,
    GeoLoc,
    LaneMarking,
    Modem,
    Permission,
    RampMeter,
    Role,
    User,
    WeatherSensor,
    Unknown,
}

impl Resource {
    /// Lookup a resource by name
    pub fn from_name(res: &str) -> Self {
        match res {
            Alarm::RESOURCE_N => Self::Alarm,
            Beacon::RESOURCE_N => Self::Beacon,
            CabinetStyle::RESOURCE_N => Self::CabinetStyle,
            Camera::RESOURCE_N => Self::Camera,
            CommConfig::RESOURCE_N => Self::CommConfig,
            CommLink::RESOURCE_N => Self::CommLink,
            Controller::RESOURCE_N => Self::Controller,
            GeoLoc::RESOURCE_N => Self::GeoLoc,
            LaneMarking::RESOURCE_N => Self::LaneMarking,
            Modem::RESOURCE_N => Self::Modem,
            Permission::RESOURCE_N => Self::Permission,
            RampMeter::RESOURCE_N => Self::RampMeter,
            Role::RESOURCE_N => Self::Role,
            User::RESOURCE_N => Self::User,
            WeatherSensor::RESOURCE_N => Self::WeatherSensor,
            _ => Self::Unknown,
        }
    }

    /// Get resource name
    pub const fn rname(&self) -> &'static str {
        match self {
            Self::Alarm => Alarm::RESOURCE_N,
            Self::Beacon => Beacon::RESOURCE_N,
            Self::CabinetStyle => CabinetStyle::RESOURCE_N,
            Self::Camera => Camera::RESOURCE_N,
            Self::CommConfig => CommConfig::RESOURCE_N,
            Self::CommLink => CommLink::RESOURCE_N,
            Self::Controller => Controller::RESOURCE_N,
            Self::GeoLoc => GeoLoc::RESOURCE_N,
            Self::LaneMarking => LaneMarking::RESOURCE_N,
            Self::Modem => Modem::RESOURCE_N,
            Self::Permission => Permission::RESOURCE_N,
            Self::RampMeter => RampMeter::RESOURCE_N,
            Self::Role => Role::RESOURCE_N,
            Self::User => User::RESOURCE_N,
            Self::WeatherSensor => WeatherSensor::RESOURCE_N,
            Self::Unknown => "",
        }
    }

    /// Lookup display name
    pub const fn dname(&self) -> &'static str {
        match self {
            Self::Alarm => "📢 Alarm",
            Self::Beacon => "🔆 Beacon",
            Self::CabinetStyle => "🗄️ Cabinet Style",
            Self::Camera => "🎥 Camera",
            Self::CommConfig => "📡 Comm Config",
            Self::CommLink => "🔗 Comm Link",
            Self::Controller => "🎛️ Controller",
            Self::GeoLoc => "🗺️ Location",
            Self::LaneMarking => "⛙ Lane Marking",
            Self::Modem => "🖀 Modem",
            Self::Permission => "🗝️ Permission",
            Self::RampMeter => "🚦 Ramp Meter",
            Self::Role => "💪 Role",
            Self::User => "👤 User",
            Self::WeatherSensor => "🌦️ Weather Sensor",
            Self::Unknown => "❓ Unknown",
        }
    }

    /// Get the URI of an object
    fn uri_name(&self, name: &str) -> String {
        let rname = self.rname();
        let name = utf8_percent_encode(name, NON_ALPHANUMERIC);
        format!("/iris/api/{rname}/{name}")
    }

    /// Delete a resource by name
    async fn delete(&self, name: &str) -> Result<()> {
        let uri = self.uri_name(name);
        fetch_delete(&uri).await
    }

    /// Lookup resource symbol
    pub fn symbol(&self) -> &'static str {
        self.dname().split_whitespace().next().unwrap()
    }

    /// Fetch and build a card
    async fn build_card<C: Card>(
        &self,
        name: &str,
        view: View,
    ) -> Result<String> {
        match view {
            View::CreateCompact => Ok(CREATE_COMPACT.into()),
            View::Create => {
                let pri = C::default().with_name(name);
                let anc = fetch_ancillary(view, &pri).await?;
                Ok(html_card_create(self.dname(), &pri.to_html_create(&anc)))
            }
            View::Compact => {
                let pri = fetch_primary::<C>(self.rname(), name).await?;
                Ok(pri.to_html_compact())
            }
            View::Status if C::HAS_STATUS => {
                let pri = fetch_primary::<C>(self.rname(), name).await?;
                let anc = fetch_ancillary(view, &pri).await?;
                Ok(html_card_status(
                    self.dname(),
                    name,
                    &pri.to_html_status(&anc),
                    pri.geo_loc().is_some(),
                ))
            }
            View::Location => {
                let pri = fetch_primary::<C>(self.rname(), name).await?;
                if let Some(geo_loc) = pri.geo_loc() {
                    let pri = fetch_primary::<GeoLoc>(
                        Resource::GeoLoc.rname(),
                        geo_loc,
                    )
                    .await?;
                    let anc = fetch_ancillary(View::Edit, &pri).await?;
                    Ok(html_card_edit(
                        Resource::GeoLoc.dname(),
                        geo_loc,
                        &pri.to_html_edit(&anc),
                    ))
                } else {
                    Err(Error::NameMissing())
                }
            }
            _ => {
                let pri = fetch_primary::<C>(self.rname(), name).await?;
                let anc = fetch_ancillary(View::Edit, &pri).await?;
                Ok(html_card_edit(self.dname(), name, &pri.to_html_edit(&anc)))
            }
        }
    }

    /// Save changed fields on card
    async fn fetch_save(&self, name: &str) -> Result<()> {
        if let Some(window) = web_sys::window() {
            if let Some(doc) = window.document() {
                let uri = self.uri_name(name);
                let json = fetch_get(&uri).await?;
                let changed = self.changed_fields(&doc, &json)?;
                fetch_patch(&uri, &changed.into()).await?;
            }
        }
        Ok(())
    }

    /// Get changed fields from an edit view
    fn changed_fields(&self, doc: &Document, json: &JsValue) -> Result<String> {
        match self {
            Resource::Alarm => Alarm::changed_fields(doc, json),
            Resource::Beacon => Beacon::changed_fields(doc, json),
            Resource::CabinetStyle => CabinetStyle::changed_fields(doc, json),
            Resource::Camera => Camera::changed_fields(doc, json),
            Resource::CommConfig => CommConfig::changed_fields(doc, json),
            Resource::CommLink => CommLink::changed_fields(doc, json),
            Resource::Controller => Controller::changed_fields(doc, json),
            Resource::GeoLoc => GeoLoc::changed_fields(doc, json),
            Resource::LaneMarking => LaneMarking::changed_fields(doc, json),
            Resource::Modem => Modem::changed_fields(doc, json),
            Resource::Permission => Permission::changed_fields(doc, json),
            Resource::RampMeter => RampMeter::changed_fields(doc, json),
            Resource::Role => Role::changed_fields(doc, json),
            Resource::User => User::changed_fields(doc, json),
            Resource::WeatherSensor => WeatherSensor::changed_fields(doc, json),
            _ => Ok("".into()),
        }
    }

    /// Create a new object
    async fn create_and_post(&self) -> Result<()> {
        if let Some(window) = web_sys::window() {
            if let Some(doc) = window.document() {
                let value = match self {
                    Resource::Permission => Permission::create_value(&doc)?,
                    _ => self.create_value(&doc)?,
                };
                let json = value.into();
                fetch_post(&format!("/iris/api/{}", self.rname()), &json)
                    .await?;
            }
        }
        Ok(())
    }

    /// Create a name value
    fn create_value(&self, doc: &Document) -> Result<String> {
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
    fn is_match<C: Card>(&self, pri: &C, anc: &C::Ancillary) -> bool {
        match self {
            Search::Empty() => true,
            Search::Normal(se) => se.split(' ').all(|s| pri.is_match(s, anc)),
            Search::Exact(se) => pri.is_match(se, anc),
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
    type Primary;

    /// Get ancillary URI
    fn uri(&self, _view: View, _pri: &Self::Primary) -> Option<Cow<str>> {
        None
    }

    /// Set ancillary JSON data
    fn set_json(
        &mut self,
        _view: View,
        _pri: &Self::Primary,
        _json: JsValue,
    ) -> Result<()> {
        Ok(())
    }
}

/// A card can be displayed in a card list
pub trait Card: Default + fmt::Display + DeserializeOwned {
    const HAS_STATUS: bool = false;

    type Ancillary: AncillaryData<Primary = Self>;

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
fn uri_name(rname: &'static str, name: &str) -> String {
    let nm = utf8_percent_encode(name, NON_ALPHANUMERIC);
    format!("/iris/api/{rname}/{nm}")
}

/// Fetch a JSON object by name
async fn fetch_json(rname: &'static str, name: &str) -> Result<JsValue> {
    let uri = uri_name(rname, name);
    Ok(fetch_get(&uri).await?)
}

/// Fetch primary JSON resource
async fn fetch_primary<C: Card>(rname: &'static str, name: &str) -> Result<C> {
    let json = fetch_json(rname, name).await?;
    Ok(C::new(&json)?)
}

/// Fetch card list for a resource type
pub async fn res_list(rname: &str, search: &str) -> Result<String> {
    let res = Resource::from_name(rname);
    let rname = res.rname();
    match res {
        Resource::Alarm => res_build_list::<Alarm>(rname, search).await,
        Resource::Beacon => res_build_list::<Beacon>(rname, search).await,
        Resource::CabinetStyle => {
            res_build_list::<CabinetStyle>(rname, search).await
        }
        Resource::Camera => res_build_list::<Camera>(rname, search).await,
        Resource::CommConfig => {
            res_build_list::<CommConfig>(rname, search).await
        }
        Resource::CommLink => res_build_list::<CommLink>(rname, search).await,
        Resource::Controller => {
            res_build_list::<Controller>(rname, search).await
        }
        Resource::LaneMarking => {
            res_build_list::<LaneMarking>(rname, search).await
        }
        Resource::Modem => res_build_list::<Modem>(rname, search).await,
        Resource::Permission => {
            res_build_list::<Permission>(rname, search).await
        }
        Resource::RampMeter => res_build_list::<RampMeter>(rname, search).await,
        Resource::Role => res_build_list::<Role>(rname, search).await,
        Resource::User => res_build_list::<User>(rname, search).await,
        Resource::WeatherSensor => {
            res_build_list::<WeatherSensor>(rname, search).await
        }
        _ => Ok("".into()),
    }
}

/// Fetch JSON array and build card list
async fn res_build_list<C: Card>(
    rname: &'static str,
    search: &str,
) -> Result<String> {
    let json = fetch_get(&format!("/iris/api/{}", rname)).await?;
    let search = Search::new(search);
    let mut html = String::new();
    html.push_str("<ul class='cards'>");
    let obs = json.into_serde::<Vec<C>>()?;
    let next_name = C::next_name(&obs);
    // the "Create" card has id "{rname}_" and next available name
    html.push_str(&format!(
        "<li id='{rname}_' name='{next_name}' class='card'>\
            {CREATE_COMPACT}\
        </li>"
    ));
    // Use default value for ancillary data lookup
    let pri = C::default();
    let anc = fetch_ancillary(View::Search, &pri).await?;
    for pri in obs.iter().filter(|pri| search.is_match(*pri, &anc)) {
        html.push_str(&format!(
            "<li id='{rname}_{pri}' name='{pri}' class='card'>"
        ));
        html.push_str(&pri.to_html_compact());
        html.push_str("</li>");
    }
    html.push_str("</ul>");
    Ok(html)
}

/// Get a card for a resource type
pub async fn res_get(rname: &str, name: &str, view: View) -> Result<String> {
    let res = Resource::from_name(rname);
    match res {
        Resource::Alarm => res.build_card::<Alarm>(name, view).await,
        Resource::Beacon => res.build_card::<Beacon>(name, view).await,
        Resource::CabinetStyle => {
            res.build_card::<CabinetStyle>(name, view).await
        }
        Resource::Camera => res.build_card::<Camera>(name, view).await,
        Resource::CommConfig => res.build_card::<CommConfig>(name, view).await,
        Resource::CommLink => res.build_card::<CommLink>(name, view).await,
        Resource::Controller => res.build_card::<Controller>(name, view).await,
        Resource::GeoLoc => res.build_card::<GeoLoc>(name, view).await,
        Resource::LaneMarking => {
            res.build_card::<LaneMarking>(name, view).await
        }
        Resource::Modem => res.build_card::<Modem>(name, view).await,
        Resource::Permission => res.build_card::<Permission>(name, view).await,
        Resource::RampMeter => res.build_card::<RampMeter>(name, view).await,
        Resource::Role => res.build_card::<Role>(name, view).await,
        Resource::User => res.build_card::<User>(name, view).await,
        Resource::WeatherSensor => {
            res.build_card::<WeatherSensor>(name, view).await
        }
        _ => Ok("".into()),
    }
}

/// Fetch ancillary data
async fn fetch_ancillary<C: Card>(view: View, pri: &C) -> Result<C::Ancillary> {
    let mut anc = C::Ancillary::default();
    while let Some(uri) = anc.uri(view, pri) {
        let json = fetch_get(uri.borrow()).await?;
        anc.set_json(view, pri, json)?;
    }
    Ok(anc)
}

/// Build a create card
fn html_card_create(dname: &'static str, create: &str) -> String {
    format!(
        "<div class='row'>\
          <span class='{TITLE}'>{dname}</span>\
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
    dname: &'static str,
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
          <span class='{TITLE}'>{dname}</span>\
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
fn html_card_edit(dname: &'static str, name: &str, edit: &str) -> String {
    let name = HtmlStr::new(name);
    format!(
        "<div class='row'>\
          <span class='{TITLE}'>{dname}</span>\
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
pub async fn res_create(rname: &str) -> Result<()> {
    let res = Resource::from_name(rname);
    res.create_and_post().await
}

/// Save changed fields on card
pub async fn res_save(rname: &str, name: &str) -> Result<()> {
    let res = Resource::from_name(rname);
    res.fetch_save(name).await
}

/// Save changed fields on card
pub async fn res_save_loc(rname: &str, name: &str) -> Result<()> {
    let res = Resource::from_name(rname);
    let rname = res.rname();
    let geo_loc = match res {
        Resource::Beacon => fetch_geo_loc::<Beacon>(rname, name).await?,
        Resource::Camera => fetch_geo_loc::<Camera>(rname, name).await?,
        Resource::Controller => {
            fetch_geo_loc::<Controller>(rname, name).await?
        }
        Resource::LaneMarking => {
            fetch_geo_loc::<LaneMarking>(rname, name).await?
        }
        Resource::RampMeter => fetch_geo_loc::<RampMeter>(rname, name).await?,
        Resource::WeatherSensor => {
            fetch_geo_loc::<WeatherSensor>(rname, name).await?
        }
        _ => None,
    };
    match geo_loc {
        Some(geo_loc) => Resource::GeoLoc.fetch_save(&geo_loc).await,
        None => Ok(()),
    }
}

/// Fetch geo location of a resource
async fn fetch_geo_loc<C: Card>(
    rname: &'static str,
    name: &str,
) -> Result<Option<String>> {
    let pri = fetch_primary::<C>(rname, name).await?;
    match pri.geo_loc() {
        Some(geo_loc) => Ok(Some(geo_loc.to_string())),
        None => Ok(None),
    }
}

/// Delete card resource
pub async fn res_delete(rname: &str, name: &str) -> Result<()> {
    let res = Resource::from_name(rname);
    res.delete(name).await
}
