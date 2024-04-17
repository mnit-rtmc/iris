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
use crate::alarm::Alarm;
use crate::beacon::Beacon;
use crate::cabinetstyle::CabinetStyle;
use crate::camera::Camera;
use crate::commconfig::CommConfig;
use crate::commlink::CommLink;
use crate::controller::Controller;
use crate::detector::Detector;
use crate::dms::Dms;
use crate::error::{Error, Result};
use crate::fetch::{Action, Uri};
use crate::flowstream::FlowStream;
use crate::gatearm::GateArm;
use crate::gatearmarray::GateArmArray;
use crate::geoloc::GeoLoc;
use crate::gps::Gps;
use crate::lanemarking::LaneMarking;
use crate::lcsarray::LcsArray;
use crate::lcsindication::LcsIndication;
use crate::modem::Modem;
use crate::permission::Permission;
use crate::rampmeter::RampMeter;
use crate::role::Role;
use crate::tagreader::TagReader;
use crate::user::User;
use crate::util::{Doc, HtmlStr};
use crate::videomonitor::VideoMonitor;
use crate::weathersensor::WeatherSensor;
use resources::Res;
use serde::de::DeserializeOwned;
use serde::Serialize;
use serde_json::map::Map;
use serde_json::Value;
use std::fmt;
use std::iter::empty;
use wasm_bindgen::JsValue;

/// CSS class for titles
const TITLE: &str = "title";

/// CSS class for names
pub const NAME: &str = "ob_name";

/// Compact "Create" card
const CREATE_COMPACT: &str = "<span class='create'>Create 🆕</span>";

/// Close button
const CLOSE_BUTTON: &str = "<button id='ob_close' type='button'>X</button>";

/// Location button
pub const LOC_BUTTON: &str =
    "<button id='ob_loc' type='button'>🗺️ Location</button>";

/// Delete button
const DEL_BUTTON: &str =
    "<button id='ob_delete' type='button'>🗑️ Delete</button>";

/// Edit button
pub const EDIT_BUTTON: &str =
    "<button id='ob_edit' type='button'>📝 Edit</button>";

/// Save button
const SAVE_BUTTON: &str = "<button id='ob_save' type='button'>🖍️ Save</button>";

/// Card View
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum View {
    /// Compact Create view
    CreateCompact,
    /// Create view
    Create,
    /// Compact view
    Compact,
    /// Status view (with config flag)
    Status(bool),
    /// Edit view
    Edit,
    /// Location view
    Location,
    /// Search view
    Search,
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

/// Search term
enum Search {
    /// Empty search (matches anything)
    Empty(),
    /// Normal search
    Normal(String),
    /// Exact (multi-word) search
    Exact(String),
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

/// Ancillary card view data
pub trait AncillaryData {
    type Primary;

    /// Get URI iterator
    fn uri_iter(
        &self,
        _pri: &Self::Primary,
        _view: View,
    ) -> Box<dyn Iterator<Item = Uri>> {
        Box::new(empty())
    }

    /// Set ancillary data
    fn set_data(
        &mut self,
        _pri: &Self::Primary,
        _uri: Uri,
        _data: JsValue,
    ) -> Result<bool> {
        Ok(false)
    }
}

/// Default item states as html options
const ITEM_STATES: &str = "<option value=''>all ↴</option>\
     <option value='🔹'>🔹 available</option>\
     <option value='🔌'>🔌 offline</option>\
     <option value='▪️'>▪️ inactive</option>";

/// A card view of a resource
pub trait Card:
    Default + fmt::Display + DeserializeOwned + Serialize + PartialEq
{
    type Ancillary: AncillaryData<Primary = Self> + Default;

    /// Display name
    const DNAME: &'static str;

    /// All item states as html options
    const ITEM_STATES: &'static str = ITEM_STATES;

    /// Get the resource
    fn res() -> Res;

    /// Get the URI of an object
    fn uri_name(name: &str) -> Uri {
        let mut uri = uri_res(Self::res());
        uri.push(name);
        uri
    }

    /// Create from a JSON value
    fn new(json: JsValue) -> Result<Self> {
        Ok(serde_wasm_bindgen::from_value(json)?)
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

    /// Convert to Create HTML
    fn to_html_create(&self, _anc: &Self::Ancillary) -> String {
        format!(
            "<div class='row'>\
              <label for='create_name'>Name</label>\
              <input id='create_name' maxlength='24' size='24' value='{self}'>\
            </div>"
        )
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, _anc: &Self::Ancillary) -> String;

    /// Get changed fields from Edit form
    fn changed_fields(&self) -> String;

    /// Handle click event for a button on the card
    fn handle_click(
        &self,
        _anc: Self::Ancillary,
        _id: &str,
        _uri: Uri,
    ) -> Vec<Action> {
        Vec::new()
    }

    /// Handle input event for an element on the card
    fn handle_input(&self, _anc: Self::Ancillary, _id: &str) {
        // ignore by default
    }
}

/// Get all item states as html options
pub fn item_states(res: Res) -> &'static str {
    match res {
        Res::Dms => Dms::ITEM_STATES,
        _ => ITEM_STATES,
    }
}

/// Get the URI of a resource
fn uri_res(res: Res) -> Uri {
    let mut uri = Uri::from("/iris/api/");
    uri.push(res.as_str());
    uri
}

/// Create a new object
pub async fn create_and_post(res: Res) -> Result<()> {
    let doc = Doc::get();
    let value = match res {
        Res::Permission => Permission::create_value(&doc)?,
        _ => create_value(&doc)?,
    };
    uri_res(res).post(&value.into()).await?;
    Ok(())
}

/// Create a name value
fn create_value(doc: &Doc) -> Result<String> {
    if let Some(name) = doc.input_option_string("create_name") {
        let mut obj = Map::new();
        obj.insert("name".to_string(), Value::String(name));
        return Ok(Value::Object(obj).to_string());
    }
    Err(Error::NameMissing())
}

/// Delete a resource by name
pub async fn delete_one(res: Res, name: &str) -> Result<()> {
    let mut uri = uri_res(res);
    uri.push(name);
    uri.delete().await
}

/// Fetch `sb_resource` access list
pub async fn fetch_resource(config: bool) -> Result<String> {
    let json = Uri::from("/iris/api/access").get().await?;
    let access: Vec<Permission> = serde_wasm_bindgen::from_value(json)?;
    let mut html = "<option/>".to_string();
    for perm in &access {
        if perm.hashtag.is_none() {
            if config {
                add_option::<Alarm>(perm, &mut html);
            }
            add_option::<Beacon>(perm, &mut html);
            if config {
                add_option::<CabinetStyle>(perm, &mut html);
            }
            add_option::<Camera>(perm, &mut html);
            if config {
                add_option::<CommConfig>(perm, &mut html);
                add_option::<CommLink>(perm, &mut html);
                add_option::<Controller>(perm, &mut html);
                add_option::<Detector>(perm, &mut html);
            }
            add_option::<Dms>(perm, &mut html);
            if config {
                add_option::<FlowStream>(perm, &mut html);
                add_option::<GateArm>(perm, &mut html);
            }
            add_option::<GateArmArray>(perm, &mut html);
            if config {
                add_option::<Gps>(perm, &mut html);
            }
            add_option::<LaneMarking>(perm, &mut html);
            add_option::<LcsArray>(perm, &mut html);
            if config {
                add_option::<LcsIndication>(perm, &mut html);
                add_option::<Modem>(perm, &mut html);
                add_option::<Permission>(perm, &mut html);
            }
            add_option::<RampMeter>(perm, &mut html);
            if config {
                add_option::<Role>(perm, &mut html);
                add_option::<TagReader>(perm, &mut html);
                add_option::<User>(perm, &mut html);
            }
            add_option::<VideoMonitor>(perm, &mut html);
            add_option::<WeatherSensor>(perm, &mut html);
        }
    }
    Ok(html)
}

/// Add option to access select
fn add_option<C: Card>(perm: &Permission, html: &mut String) {
    let res = C::res();
    if perm.resource_n == res.dependent().as_str() {
        html.push_str("<option value='");
        html.push_str(res.as_str());
        html.push_str("'>");
        html.push_str(C::DNAME);
        html.push_str("</option>");
    }
}

/// Card list for one resource type
pub struct CardList {
    /// Resource type
    res: Res,
    /// JSON list of cards
    json: String,
}

impl CardList {
    /// Fetch card list for a resource type
    pub async fn fetch(res: Res) -> Result<Self> {
        let json = uri_res(res).get().await?;
        let json: Value = serde_wasm_bindgen::from_value(json)?;
        let json = json.to_string();
        Ok(CardList { res, json })
    }

    /// Filter card list with a search term
    pub async fn filter(&self, search: &str) -> Result<Self> {
        match self.res {
            Res::Alarm => self.filter_x::<Alarm>(search).await,
            Res::Beacon => self.filter_x::<Beacon>(search).await,
            Res::CabinetStyle => self.filter_x::<CabinetStyle>(search).await,
            Res::Camera => self.filter_x::<Camera>(search).await,
            Res::CommConfig => self.filter_x::<CommConfig>(search).await,
            Res::CommLink => self.filter_x::<CommLink>(search).await,
            Res::Controller => self.filter_x::<Controller>(search).await,
            Res::Detector => self.filter_x::<Detector>(search).await,
            Res::Dms => self.filter_x::<Dms>(search).await,
            Res::FlowStream => self.filter_x::<FlowStream>(search).await,
            Res::GateArm => self.filter_x::<GateArm>(search).await,
            Res::GateArmArray => self.filter_x::<GateArmArray>(search).await,
            Res::Gps => self.filter_x::<Gps>(search).await,
            Res::LaneMarking => self.filter_x::<LaneMarking>(search).await,
            Res::LcsArray => self.filter_x::<LcsArray>(search).await,
            Res::LcsIndication => self.filter_x::<LcsIndication>(search).await,
            Res::Modem => self.filter_x::<Modem>(search).await,
            Res::Permission => self.filter_x::<Permission>(search).await,
            Res::RampMeter => self.filter_x::<RampMeter>(search).await,
            Res::Role => self.filter_x::<Role>(search).await,
            Res::TagReader => self.filter_x::<TagReader>(search).await,
            Res::User => self.filter_x::<User>(search).await,
            Res::VideoMonitor => self.filter_x::<VideoMonitor>(search).await,
            Res::WeatherSensor => self.filter_x::<WeatherSensor>(search).await,
            _ => unreachable!(),
        }
    }

    /// Filter card list with a search term
    async fn filter_x<C: Card>(&self, search: &str) -> Result<Self> {
        let mut cards: Vec<C> = serde_json::from_str(&self.json)?;
        // Use default value for ancillary data lookup
        let pri = C::default();
        let anc = fetch_ancillary(View::Search, &pri).await?;
        let search = Search::new(search);
        cards.retain(|pri| search.is_match(pri, &anc));
        let json = serde_json::to_string(&cards)?;
        Ok(CardList {
            res: self.res,
            json,
        })
    }

    /// Convert card list to HTML view
    pub async fn to_html(&self, config: bool) -> Result<String> {
        match self.res {
            Res::Alarm => self.to_html_x::<Alarm>(config).await,
            Res::Beacon => self.to_html_x::<Beacon>(config).await,
            Res::CabinetStyle => self.to_html_x::<CabinetStyle>(config).await,
            Res::Camera => self.to_html_x::<Camera>(config).await,
            Res::CommConfig => self.to_html_x::<CommConfig>(config).await,
            Res::CommLink => self.to_html_x::<CommLink>(config).await,
            Res::Controller => self.to_html_x::<Controller>(config).await,
            Res::Detector => self.to_html_x::<Detector>(config).await,
            Res::Dms => self.to_html_x::<Dms>(config).await,
            Res::FlowStream => self.to_html_x::<FlowStream>(config).await,
            Res::GateArm => self.to_html_x::<GateArm>(config).await,
            Res::GateArmArray => self.to_html_x::<GateArmArray>(config).await,
            Res::Gps => self.to_html_x::<Gps>(config).await,
            Res::LaneMarking => self.to_html_x::<LaneMarking>(config).await,
            Res::LcsArray => self.to_html_x::<LcsArray>(config).await,
            Res::LcsIndication => self.to_html_x::<LcsIndication>(config).await,
            Res::Modem => self.to_html_x::<Modem>(config).await,
            Res::Permission => self.to_html_x::<Permission>(config).await,
            Res::RampMeter => self.to_html_x::<RampMeter>(config).await,
            Res::Role => self.to_html_x::<Role>(config).await,
            Res::TagReader => self.to_html_x::<TagReader>(config).await,
            Res::User => self.to_html_x::<User>(config).await,
            Res::VideoMonitor => self.to_html_x::<VideoMonitor>(config).await,
            Res::WeatherSensor => self.to_html_x::<WeatherSensor>(config).await,
            _ => unreachable!(),
        }
    }

    /// Convert card list to HTML view
    async fn to_html_x<C: Card>(&self, config: bool) -> Result<String> {
        let cards: Vec<C> = serde_json::from_str(&self.json)?;
        // Use default value for ancillary data lookup
        let pri = C::default();
        let anc = fetch_ancillary(View::Search, &pri).await?;
        let rname = C::res().as_str();
        let mut html = String::new();
        html.push_str("<ul class='cards'>");
        if config {
            let next_name = C::next_name(&cards);
            // the "Create" card has id "{rname}_" and next available name
            html.push_str(&format!(
                "<li id='{rname}_' name='{next_name}' class='card'>\
                    {CREATE_COMPACT}\
                </li>"
            ));
        }
        for pri in cards {
            html.push_str(&format!(
                "<li id='{rname}_{pri}' name='{pri}' class='card'>"
            ));
            html.push_str(&pri.to_html(View::Compact, &anc));
            html.push_str("</li>");
        }
        html.push_str("</ul>");
        Ok(html)
    }
}

/// Fetch ancillary data
async fn fetch_ancillary<C: Card>(view: View, pri: &C) -> Result<C::Ancillary> {
    let mut anc = C::Ancillary::default();
    let mut more = true;
    while more {
        more = false;
        for uri in anc.uri_iter(pri, view) {
            match uri.get().await {
                Ok(data) => {
                    if anc.set_data(pri, uri, data)? {
                        more = true;
                    }
                }
                Err(Error::FetchResponseNotFound())
                | Err(Error::FetchResponseForbidden()) => {
                    // Ok, move on to the next one
                }
                Err(e) => return Err(e),
            }
        }
    }
    Ok(anc)
}

/// Fetch a card for a given view
pub async fn fetch_one(res: Res, name: &str, view: View) -> Result<String> {
    match view {
        View::CreateCompact => Ok(CREATE_COMPACT.into()),
        View::Create => {
            let html = fetch_one_res(res, View::Create, name).await?;
            Ok(html_card_create(res, &html))
        }
        View::Compact => fetch_one_res(res, View::Compact, name).await,
        View::Location => match fetch_geo_loc(res, name).await? {
            Some(geo_loc) => card_location(&geo_loc).await,
            None => unreachable!(),
        },
        View::Status(config) if has_status(res) => {
            let html = fetch_one_res(res, View::Status(config), name).await?;
            Ok(html_card_status(res, name, &html))
        }
        _ => {
            let html = fetch_one_res(res, View::Edit, name).await?;
            Ok(html_card_edit(res, name, &html, DEL_BUTTON))
        }
    }
}

/// Check if a resource has a Status view
fn has_status(res: Res) -> bool {
    matches!(
        res,
        Res::Alarm
            | Res::Beacon
            | Res::Camera
            | Res::CommLink
            | Res::Controller
            | Res::Detector
            | Res::Dms
            | Res::FlowStream
            | Res::GateArm
            | Res::GateArmArray
            | Res::GeoLoc
            | Res::Gps
            | Res::LaneMarking
            | Res::LcsArray
            | Res::LcsIndication
            | Res::RampMeter
            | Res::TagReader
            | Res::VideoMonitor
            | Res::WeatherSensor
    )
}

/// Fetch a card view
async fn fetch_one_res(res: Res, view: View, name: &str) -> Result<String> {
    match res {
        Res::Alarm => fetch_one_x::<Alarm>(view, name).await,
        Res::Beacon => fetch_one_x::<Beacon>(view, name).await,
        Res::CabinetStyle => fetch_one_x::<CabinetStyle>(view, name).await,
        Res::Camera => fetch_one_x::<Camera>(view, name).await,
        Res::CommConfig => fetch_one_x::<CommConfig>(view, name).await,
        Res::CommLink => fetch_one_x::<CommLink>(view, name).await,
        Res::Controller => fetch_one_x::<Controller>(view, name).await,
        Res::Detector => fetch_one_x::<Detector>(view, name).await,
        Res::Dms => fetch_one_x::<Dms>(view, name).await,
        Res::FlowStream => fetch_one_x::<FlowStream>(view, name).await,
        Res::GateArm => fetch_one_x::<GateArm>(view, name).await,
        Res::GateArmArray => fetch_one_x::<GateArmArray>(view, name).await,
        Res::GeoLoc => fetch_one_x::<GeoLoc>(view, name).await,
        Res::Gps => fetch_one_x::<Gps>(view, name).await,
        Res::LaneMarking => fetch_one_x::<LaneMarking>(view, name).await,
        Res::LcsArray => fetch_one_x::<LcsArray>(view, name).await,
        Res::LcsIndication => fetch_one_x::<LcsIndication>(view, name).await,
        Res::Modem => fetch_one_x::<Modem>(view, name).await,
        Res::Permission => fetch_one_x::<Permission>(view, name).await,
        Res::RampMeter => fetch_one_x::<RampMeter>(view, name).await,
        Res::Role => fetch_one_x::<Role>(view, name).await,
        Res::TagReader => fetch_one_x::<TagReader>(view, name).await,
        Res::User => fetch_one_x::<User>(view, name).await,
        Res::VideoMonitor => fetch_one_x::<VideoMonitor>(view, name).await,
        Res::WeatherSensor => fetch_one_x::<WeatherSensor>(view, name).await,
        _ => unreachable!(),
    }
}

/// Fetch a card view
async fn fetch_one_x<C: Card>(view: View, name: &str) -> Result<String> {
    let pri = if view == View::Create {
        C::default().with_name(name)
    } else {
        fetch_primary::<C>(name).await?
    };
    let anc = fetch_ancillary(view, &pri).await?;
    Ok(pri.to_html(view, &anc))
}

/// Fetch primary JSON resource
async fn fetch_primary<C: Card>(name: &str) -> Result<C> {
    let json = C::uri_name(name).get().await?;
    C::new(json)
}

/// Fetch geo location name (if any)
pub async fn fetch_geo_loc(res: Res, name: &str) -> Result<Option<String>> {
    match res {
        Res::Beacon => geo_loc::<Beacon>(name).await,
        Res::Camera => geo_loc::<Camera>(name).await,
        Res::Controller => geo_loc::<Controller>(name).await,
        Res::Dms => geo_loc::<Dms>(name).await,
        Res::GateArmArray => geo_loc::<GateArmArray>(name).await,
        Res::GeoLoc => Ok(Some(name.into())),
        Res::LaneMarking => geo_loc::<LaneMarking>(name).await,
        Res::RampMeter => geo_loc::<RampMeter>(name).await,
        Res::TagReader => geo_loc::<TagReader>(name).await,
        Res::WeatherSensor => geo_loc::<WeatherSensor>(name).await,
        _ => Ok(None),
    }
}

/// Fetch geo location name
async fn geo_loc<C: Card>(name: &str) -> Result<Option<String>> {
    let pri = fetch_primary::<C>(name).await?;
    match pri.geo_loc() {
        Some(geo_loc) => Ok(Some(geo_loc.to_string())),
        None => Ok(None),
    }
}

/// Patch changed fields on card
pub async fn patch_changed(res: Res, name: &str) -> Result<()> {
    match res {
        Res::Alarm => patch_changed_x::<Alarm>(name).await,
        Res::Beacon => patch_changed_x::<Beacon>(name).await,
        Res::CabinetStyle => patch_changed_x::<CabinetStyle>(name).await,
        Res::Camera => patch_changed_x::<Camera>(name).await,
        Res::CommConfig => patch_changed_x::<CommConfig>(name).await,
        Res::CommLink => patch_changed_x::<CommLink>(name).await,
        Res::Controller => patch_changed_x::<Controller>(name).await,
        Res::Detector => patch_changed_x::<Detector>(name).await,
        Res::Dms => patch_changed_x::<Dms>(name).await,
        Res::FlowStream => patch_changed_x::<FlowStream>(name).await,
        Res::GateArm => patch_changed_x::<GateArm>(name).await,
        Res::GateArmArray => patch_changed_x::<GateArmArray>(name).await,
        Res::GeoLoc => patch_changed_x::<GeoLoc>(name).await,
        Res::Gps => patch_changed_x::<Gps>(name).await,
        Res::LaneMarking => patch_changed_x::<LaneMarking>(name).await,
        Res::LcsArray => patch_changed_x::<LcsArray>(name).await,
        Res::LcsIndication => patch_changed_x::<LcsIndication>(name).await,
        Res::Modem => patch_changed_x::<Modem>(name).await,
        Res::Permission => patch_changed_x::<Permission>(name).await,
        Res::RampMeter => patch_changed_x::<RampMeter>(name).await,
        Res::Role => patch_changed_x::<Role>(name).await,
        Res::TagReader => patch_changed_x::<TagReader>(name).await,
        Res::User => patch_changed_x::<User>(name).await,
        Res::VideoMonitor => patch_changed_x::<VideoMonitor>(name).await,
        Res::WeatherSensor => patch_changed_x::<WeatherSensor>(name).await,
        _ => unreachable!(),
    }
}

/// Patch changed fields from an Edit view
async fn patch_changed_x<C: Card>(name: &str) -> Result<()> {
    let pri = fetch_primary::<C>(name).await?;
    let changed = pri.changed_fields();
    if !changed.is_empty() {
        C::uri_name(name).patch(&changed.into()).await?;
    }
    Ok(())
}

/// Handle click event for a button owned by the resource
pub async fn handle_click(res: Res, name: &str, id: &str) -> Result<bool> {
    match res {
        Res::Beacon => handle_click_x::<Beacon>(name, id).await,
        Res::Dms => handle_click_x::<Dms>(name, id).await,
        _ => Ok(false),
    }
}

/// Handle click event for a button on a card
async fn handle_click_x<C: Card>(name: &str, id: &str) -> Result<bool> {
    let pri = fetch_primary::<C>(name).await?;
    let anc = fetch_ancillary(View::Status(false), &pri).await?;
    let uri = C::uri_name(name);
    for action in pri.handle_click(anc, id, uri) {
        action.perform().await?;
    }
    Ok(true)
}

/// Handle input event for an element owned by the resource
pub async fn handle_input(res: Res, name: &str, id: &str) -> Result<bool> {
    match res {
        Res::Dms => handle_input_x::<Dms>(name, id).await,
        _ => Ok(false),
    }
}

/// Handle input event for an element on a card
async fn handle_input_x<C: Card>(name: &str, id: &str) -> Result<bool> {
    let pri = fetch_primary::<C>(name).await?;
    let anc = fetch_ancillary(View::Status(false), &pri).await?;
    pri.handle_input(anc, id);
    Ok(true)
}

/// Fetch a Location card
async fn card_location(name: &str) -> Result<String> {
    let html = fetch_one_res(Res::GeoLoc, View::Edit, name).await?;
    Ok(html_card_edit(Res::GeoLoc, name, &html, ""))
}

/// Get resource display
const fn display_res(res: Res) -> &'static str {
    match res {
        Res::Alarm => Alarm::DNAME,
        Res::Beacon => Beacon::DNAME,
        Res::CabinetStyle => CabinetStyle::DNAME,
        Res::Camera => Camera::DNAME,
        Res::CommConfig => CommConfig::DNAME,
        Res::CommLink => CommLink::DNAME,
        Res::Controller => Controller::DNAME,
        Res::Detector => Detector::DNAME,
        Res::Dms => Dms::DNAME,
        Res::FlowStream => FlowStream::DNAME,
        Res::GateArm => GateArm::DNAME,
        Res::GateArmArray => GateArmArray::DNAME,
        Res::GeoLoc => GeoLoc::DNAME,
        Res::Gps => Gps::DNAME,
        Res::LaneMarking => LaneMarking::DNAME,
        Res::LcsArray => LcsArray::DNAME,
        Res::LcsIndication => LcsIndication::DNAME,
        Res::Modem => Modem::DNAME,
        Res::Permission => Permission::DNAME,
        Res::RampMeter => RampMeter::DNAME,
        Res::Role => Role::DNAME,
        Res::TagReader => TagReader::DNAME,
        Res::User => User::DNAME,
        Res::VideoMonitor => VideoMonitor::DNAME,
        Res::WeatherSensor => WeatherSensor::DNAME,
        _ => unimplemented!(),
    }
}

/// Build a create card
fn html_card_create(res: Res, create: &str) -> String {
    let display = display_res(res);
    format!(
        "<div class='row'>\
          <span class='{TITLE}'>{display}</span>\
          <span class='{TITLE}'>Create</span>\
          <span class='{NAME}'>🆕</span>\
          {CLOSE_BUTTON}\
        </div>\
        {create}
        <div class='row end'>\
          {SAVE_BUTTON}\
        </div>"
    )
}

/// Build an edit card
fn html_card_edit(
    res: Res,
    name: &str,
    edit: &str,
    delete: &'static str,
) -> String {
    let display = display_res(res);
    let name = HtmlStr::new(name);
    format!(
        "<div class='row'>\
          <span class='{TITLE}'>{display}</span>\
          <span class='{TITLE}'>Edit</span>\
          <span class='{NAME}'>{name}</span>\
          {CLOSE_BUTTON}\
        </div>\
        {edit}\
        <div class='row'>\
          <span></span>\
          {delete}\
          {SAVE_BUTTON}\
        </div>"
    )
}

/// Build a status card
fn html_card_status(res: Res, name: &str, status: &str) -> String {
    let display = display_res(res);
    let name = HtmlStr::new(name);
    format!(
        "<div class='row'>\
          <span class='{TITLE}'>{display}</span>\
          <span class='{TITLE}'>Status</span>\
          <span class='{NAME}'>{name}</span>\
          {CLOSE_BUTTON}\
        </div>\
        {status}"
    )
}

/// Get attribute for inactive cards
pub fn inactive_attr(active: bool) -> &'static str {
    if active {
        ""
    } else {
        " inactive"
    }
}
