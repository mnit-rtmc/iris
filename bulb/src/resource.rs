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
pub const CLOSE_BUTTON: &str = "<button id='ob_close' type='button'>X</button>";

/// Location button
pub const LOC_BUTTON: &str =
    "<button id='ob_loc' type='button'>🗺️ Location</button>";

/// Delete button
pub const DEL_BUTTON: &str =
    "<button id='ob_delete' type='button'>🗑️ Delete</button>";

/// Edit button
pub const EDIT_BUTTON: &str =
    "<button id='ob_edit' type='button'>📝 Edit</button>";

/// Save button
pub const SAVE_BUTTON: &str =
    "<button id='ob_save' type='button'>🖍️ Save</button>";

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
    Detector,
    Dms,
    FlowStream,
    GateArm,
    GateArmArray,
    GeoLoc,
    Gps,
    LaneMarking,
    Lcs,
    LcsArray,
    LcsIndication,
    Modem,
    Permission,
    RampMeter,
    Role,
    TagReader,
    User,
    VideoMonitor,
    WeatherSensor,
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
    /// Status view (with config flag)
    Status(bool),
    /// Edit view
    Edit,
    /// Location view
    Location,
    /// Search view
    Search,
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
const ITEM_STATE_OPTIONS: &'static str = "<option value=''>all ↴</option>\
     <option value='🔹'>🔹 available</option>\
     <option value='🔌'>🔌 offline</option>\
     <option value='▪️'>▪️ inactive</option>";

/// A card view of a resource
pub trait Card: Default + fmt::Display + DeserializeOwned {
    type Ancillary: AncillaryData<Primary = Self> + Default;

    /// Display name
    const DNAME: &'static str;

    /// All item states as html options
    const ITEM_STATE_OPTIONS: &'static str = ITEM_STATE_OPTIONS;

    /// Get the resource
    fn res() -> Res;

    /// Get the URI of an object
    fn uri_name(name: &str) -> Uri {
        let mut uri = res_uri(Self::res());
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

impl TryFrom<&str> for Resource {
    type Error = ();

    fn try_from(resource_n: &str) -> std::result::Result<Self, Self::Error> {
        Self::iter()
            .find(|res| Res::from(*res).as_str() == resource_n)
            .ok_or(())
    }
}

impl From<Resource> for Res {
    fn from(res: Resource) -> Self {
        use Resource::*;
        match res {
            Alarm => Res::Alarm,
            Beacon => Res::Beacon,
            CabinetStyle => Res::CabinetStyle,
            Camera => Res::Camera,
            CommConfig => Res::CommConfig,
            CommLink => Res::CommLink,
            Controller => Res::Controller,
            Detector => Res::Detector,
            Dms => Res::Dms,
            FlowStream => Res::FlowStream,
            GateArm => Res::GateArm,
            GateArmArray => Res::GateArmArray,
            GeoLoc => Res::GeoLoc,
            Gps => Res::Gps,
            LaneMarking => Res::LaneMarking,
            Lcs => Res::Lcs,
            LcsArray => Res::LcsArray,
            LcsIndication => Res::LcsIndication,
            Modem => Res::Modem,
            Permission => Res::Permission,
            RampMeter => Res::RampMeter,
            Role => Res::Role,
            TagReader => Res::TagReader,
            User => Res::User,
            VideoMonitor => Res::VideoMonitor,
            WeatherSensor => Res::WeatherSensor,
        }
    }
}

impl Resource {
    /// Get iterator of all resource variants
    pub fn iter() -> impl Iterator<Item = Self> {
        use Resource::*;
        [
            Alarm,
            Beacon,
            CabinetStyle,
            Camera,
            CommConfig,
            CommLink,
            Controller,
            Detector,
            Dms,
            FlowStream,
            GateArm,
            GateArmArray,
            GeoLoc,
            Gps,
            LaneMarking,
            Lcs,
            LcsArray,
            LcsIndication,
            Modem,
            Permission,
            RampMeter,
            Role,
            TagReader,
            User,
            VideoMonitor,
            WeatherSensor,
        ]
        .iter()
        .cloned()
    }

    /// Handle click event for a button owned by the resource
    pub async fn handle_click(self, name: &str, id: &str) -> Result<bool> {
        match self {
            Self::Beacon => handle_click::<Beacon>(name, id).await,
            Self::Dms => handle_click::<Dms>(name, id).await,
            _ => Ok(false),
        }
    }

    /// Handle input event for an element owned by the resource
    pub async fn handle_input(self, name: &str, id: &str) -> Result<bool> {
        match self {
            Self::Dms => handle_input::<Dms>(name, id).await,
            _ => Ok(false),
        }
    }

    /// Get all item states as html options
    pub fn item_state_options(self) -> &'static str {
        match self {
            Self::Dms => Dms::ITEM_STATE_OPTIONS,
            _ => ITEM_STATE_OPTIONS,
        }
    }
}

/// Get the URI of a resource
fn res_uri(res: Res) -> Uri {
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
    res_uri(res).post(&value.into()).await?;
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
pub async fn delete_card_res(res: Res, name: &str) -> Result<()> {
    let mut uri = res_uri(res);
    uri.push(name);
    uri.delete().await
}

/// Fetch JSON resource array list
async fn fetch_list<C: Card>() -> Result<(Vec<C>, C::Ancillary)> {
    let json = res_uri(C::res()).get().await?;
    let obs = serde_wasm_bindgen::from_value(json)?;
    // Use default value for ancillary data lookup
    let pri = C::default();
    let anc = fetch_ancillary(View::Search, &pri).await?;
    Ok((obs, anc))
}

/// Fetch card list for a resource type
pub async fn fetch_cards_res(
    res: Res,
    search: &str,
    config: bool,
) -> Result<String> {
    match res {
        Res::Alarm => fetch_cards::<Alarm>(search, config).await,
        Res::Beacon => fetch_cards::<Beacon>(search, config).await,
        Res::CabinetStyle => fetch_cards::<CabinetStyle>(search, config).await,
        Res::Camera => fetch_cards::<Camera>(search, config).await,
        Res::CommConfig => fetch_cards::<CommConfig>(search, config).await,
        Res::CommLink => fetch_cards::<CommLink>(search, config).await,
        Res::Controller => fetch_cards::<Controller>(search, config).await,
        Res::Detector => fetch_cards::<Detector>(search, config).await,
        Res::Dms => fetch_cards::<Dms>(search, config).await,
        Res::FlowStream => fetch_cards::<FlowStream>(search, config).await,
        Res::GateArm => fetch_cards::<GateArm>(search, config).await,
        Res::GateArmArray => fetch_cards::<GateArmArray>(search, config).await,
        Res::Gps => fetch_cards::<Gps>(search, config).await,
        Res::LaneMarking => fetch_cards::<LaneMarking>(search, config).await,
        Res::LcsArray => fetch_cards::<LcsArray>(search, config).await,
        Res::LcsIndication => {
            fetch_cards::<LcsIndication>(search, config).await
        }
        Res::Modem => fetch_cards::<Modem>(search, config).await,
        Res::Permission => fetch_cards::<Permission>(search, config).await,
        Res::RampMeter => fetch_cards::<RampMeter>(search, config).await,
        Res::Role => fetch_cards::<Role>(search, config).await,
        Res::TagReader => fetch_cards::<TagReader>(search, config).await,
        Res::User => fetch_cards::<User>(search, config).await,
        Res::VideoMonitor => fetch_cards::<VideoMonitor>(search, config).await,
        Res::WeatherSensor => {
            fetch_cards::<WeatherSensor>(search, config).await
        }
        _ => Ok("".into()),
    }
}

/// Fetch card list as HTML
async fn fetch_cards<C: Card>(search: &str, config: bool) -> Result<String> {
    let (obs, anc) = fetch_list().await?;
    let rname = C::res().as_str();
    let search = Search::new(search);
    let mut html = String::new();
    html.push_str("<ul class='cards'>");
    if config {
        let next_name = C::next_name(&obs);
        // the "Create" card has id "{rname}_" and next available name
        html.push_str(&format!(
            "<li id='{rname}_' name='{next_name}' class='card'>\
                {CREATE_COMPACT}\
            </li>"
        ));
    }
    for pri in obs.iter().filter(|pri| search.is_match(*pri, &anc)) {
        html.push_str(&format!(
            "<li id='{rname}_{pri}' name='{pri}' class='card'>"
        ));
        html.push_str(&pri.to_html(View::Compact, &anc));
        html.push_str("</li>");
    }
    html.push_str("</ul>");
    Ok(html)
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

/// Fetch primary JSON resource
async fn fetch_primary<C: Card>(name: &str) -> Result<C> {
    let json = C::uri_name(name).get().await?;
    C::new(json)
}

/// Fetch a card for a given view
pub async fn fetch_card(res: Res, name: &str, view: View) -> Result<String> {
    match view {
        View::CreateCompact => Ok(CREATE_COMPACT.into()),
        View::Create => {
            let html = card_view_res(res, View::Create, name).await?;
            Ok(html_card_create(res, &html))
        }
        View::Compact => card_view_res(res, View::Compact, name).await,
        View::Location => match fetch_geo_loc(res, name).await? {
            Some(geo_loc) => card_location(&geo_loc).await,
            None => unreachable!(),
        },
        View::Status(config) if has_status(res) => {
            let html = card_view_res(res, View::Status(config), name).await?;
            Ok(html_card_status(res, name, &html))
        }
        _ => {
            let html = card_view_res(res, View::Edit, name).await?;
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

/// Fetch a card view
async fn card_view_res(res: Res, view: View, name: &str) -> Result<String> {
    match res {
        Res::Alarm => card_view::<Alarm>(view, name).await,
        Res::Beacon => card_view::<Beacon>(view, name).await,
        Res::CabinetStyle => card_view::<CabinetStyle>(view, name).await,
        Res::Camera => card_view::<Camera>(view, name).await,
        Res::CommConfig => card_view::<CommConfig>(view, name).await,
        Res::CommLink => card_view::<CommLink>(view, name).await,
        Res::Controller => card_view::<Controller>(view, name).await,
        Res::Detector => card_view::<Detector>(view, name).await,
        Res::Dms => card_view::<Dms>(view, name).await,
        Res::FlowStream => card_view::<FlowStream>(view, name).await,
        Res::GateArm => card_view::<GateArm>(view, name).await,
        Res::GateArmArray => card_view::<GateArmArray>(view, name).await,
        Res::GeoLoc => card_view::<GeoLoc>(view, name).await,
        Res::Gps => card_view::<Gps>(view, name).await,
        Res::LaneMarking => card_view::<LaneMarking>(view, name).await,
        Res::LcsArray => card_view::<LcsArray>(view, name).await,
        Res::LcsIndication => card_view::<LcsIndication>(view, name).await,
        Res::Modem => card_view::<Modem>(view, name).await,
        Res::Permission => card_view::<Permission>(view, name).await,
        Res::RampMeter => card_view::<RampMeter>(view, name).await,
        Res::Role => card_view::<Role>(view, name).await,
        Res::TagReader => card_view::<TagReader>(view, name).await,
        Res::User => card_view::<User>(view, name).await,
        Res::VideoMonitor => card_view::<VideoMonitor>(view, name).await,
        Res::WeatherSensor => card_view::<WeatherSensor>(view, name).await,
        _ => unreachable!(),
    }
}

/// Fetch a card view
async fn card_view<C: Card>(view: View, name: &str) -> Result<String> {
    let pri = if view == View::Create {
        C::default().with_name(name)
    } else {
        fetch_primary::<C>(name).await?
    };
    let anc = fetch_ancillary(view, &pri).await?;
    Ok(pri.to_html(view, &anc))
}

/// Save changed fields on card
pub async fn save_card_res(res: Res, name: &str) -> Result<()> {
    match res {
        Res::Alarm => save_card::<Alarm>(name).await,
        Res::Beacon => save_card::<Beacon>(name).await,
        Res::CabinetStyle => save_card::<CabinetStyle>(name).await,
        Res::Camera => save_card::<Camera>(name).await,
        Res::CommConfig => save_card::<CommConfig>(name).await,
        Res::CommLink => save_card::<CommLink>(name).await,
        Res::Controller => save_card::<Controller>(name).await,
        Res::Detector => save_card::<Detector>(name).await,
        Res::Dms => save_card::<Dms>(name).await,
        Res::FlowStream => save_card::<FlowStream>(name).await,
        Res::GateArm => save_card::<GateArm>(name).await,
        Res::GateArmArray => save_card::<GateArmArray>(name).await,
        Res::GeoLoc => save_card::<GeoLoc>(name).await,
        Res::Gps => save_card::<Gps>(name).await,
        Res::LaneMarking => save_card::<LaneMarking>(name).await,
        Res::LcsArray => save_card::<LcsArray>(name).await,
        Res::LcsIndication => save_card::<LcsIndication>(name).await,
        Res::Modem => save_card::<Modem>(name).await,
        Res::Permission => save_card::<Permission>(name).await,
        Res::RampMeter => save_card::<RampMeter>(name).await,
        Res::Role => save_card::<Role>(name).await,
        Res::TagReader => save_card::<TagReader>(name).await,
        Res::User => save_card::<User>(name).await,
        Res::VideoMonitor => save_card::<VideoMonitor>(name).await,
        Res::WeatherSensor => save_card::<WeatherSensor>(name).await,
        _ => unreachable!(),
    }
}

/// Fetch changed fields from an Edit view
async fn save_card<C: Card>(name: &str) -> Result<()> {
    let pri = fetch_primary::<C>(name).await?;
    let changed = pri.changed_fields();
    if !changed.is_empty() {
        C::uri_name(name).patch(&changed.into()).await?;
    }
    Ok(())
}

/// Handle click event for a button on a card
async fn handle_click<C: Card>(name: &str, id: &str) -> Result<bool> {
    let pri = fetch_primary::<C>(name).await?;
    let anc = fetch_ancillary(View::Status(false), &pri).await?;
    let uri = C::uri_name(name);
    for action in pri.handle_click(anc, id, uri) {
        action.perform().await?;
    }
    Ok(true)
}

/// Handle input event for an element on a card
async fn handle_input<C: Card>(name: &str, id: &str) -> Result<bool> {
    let pri = fetch_primary::<C>(name).await?;
    let anc = fetch_ancillary(View::Status(false), &pri).await?;
    pri.handle_input(anc, id);
    Ok(true)
}

/// Fetch a Location card
async fn card_location(name: &str) -> Result<String> {
    let html = card_view_res(Res::GeoLoc, View::Edit, name).await?;
    Ok(html_card_edit(Res::GeoLoc, name, &html, ""))
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

/// Get attribute for inactive cards
pub fn inactive_attr(active: bool) -> &'static str {
    if active {
        ""
    } else {
        " inactive"
    }
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
