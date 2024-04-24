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

/// Card element view
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum View {
    /// Hidden view
    Hidden,
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
    /// Get view class name
    pub const fn class_name(self) -> &'static str {
        match self {
            View::Hidden | View::Search => "card-hidden",
            View::CreateCompact | View::Compact => "card-compact",
            _ => "card-form",
        }
    }

    /// Is the view a form?
    pub fn is_form(self) -> bool {
        matches!(
            self,
            View::Create | View::Status(_) | View::Edit | View::Location
        )
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

/// Card view
#[derive(Clone, Debug)]
pub struct CardView {
    /// Resource type
    pub res: Res,
    /// Object name
    pub name: String,
    /// Card view
    pub view: View,
}

impl CardView {
    /// Create a new card view
    pub fn new(res: Res, name: String, view: View) -> Self {
        CardView { res, name, view }
    }

    /// Get HTML element ID of card
    pub fn id(&self) -> String {
        let res = self.res;
        if self.view.is_create() {
            format!("{res}_")
        } else {
            format!("{res}_{}", &self.name)
        }
    }

    /// Set the view to compact
    pub fn compact(mut self) -> Self {
        self.view = self.view.compact();
        self
    }

    /// Set the view
    pub fn view(mut self, v: View) -> Self {
        self.view = v;
        self
    }
}

/// Search term
#[derive(Clone)]
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

    /// Get the card ID
    fn id(&self) -> String {
        format!("{}_{self}", Self::res())
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
pub fn item_states(res: Option<Res>) -> &'static str {
    match res {
        Some(Res::Beacon) => Beacon::ITEM_STATES,
        Some(Res::Dms) => Dms::ITEM_STATES,
        Some(_) => ITEM_STATES,
        None => "",
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
pub async fn delete_one(cv: &CardView) -> Result<()> {
    let mut uri = uri_res(cv.res);
    uri.push(&cv.name);
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
///
/// | Cause    | Initiator | Fetch | List     | SSE  |
/// |----------|-----------|-------|----------|------|
/// | Resource | User      | Yes   | Populate | POST |
/// | Refresh  | User      | Yes   | Populate |      |
/// | Search   | User      | No    | Update   |      |
/// | Notify   | System    | Yes   | Update   |      |
///
#[derive(Clone)]
pub struct CardList {
    /// Resource type
    res: Res,
    /// Config mode
    config: bool,
    /// Search term
    search: Search,
    /// JSON list of cards
    json: String,
    /// Views in order of JSON list
    views: Vec<View>,
}

impl CardList {
    /// Create a new card list
    pub fn new(res: Res) -> Self {
        let config = false;
        let search = Search::Empty();
        let json = String::new();
        let views = Vec::new();
        CardList {
            res,
            config,
            search,
            json,
            views,
        }
    }

    /// Set config flag
    pub fn config(&mut self, config: bool) {
        self.config = config;
    }

    /// Set search term
    pub fn search(&mut self, search: &str) {
        self.search = Search::new(search);
    }

    /// Take current JSON value
    pub fn json(&mut self) -> String {
        std::mem::take(&mut self.json)
    }

    /// Fetch card list
    pub async fn fetch(&mut self) -> Result<()> {
        let json = uri_res(self.res).get().await?;
        let json: Value = serde_wasm_bindgen::from_value(json)?;
        self.json = json.to_string();
        Ok(())
    }

    /// Make HTML view of card list
    pub async fn make_html(&mut self) -> Result<String> {
        match self.res {
            Res::Alarm => self.make_html_x::<Alarm>().await,
            Res::Beacon => self.make_html_x::<Beacon>().await,
            Res::CabinetStyle => self.make_html_x::<CabinetStyle>().await,
            Res::Camera => self.make_html_x::<Camera>().await,
            Res::CommConfig => self.make_html_x::<CommConfig>().await,
            Res::CommLink => self.make_html_x::<CommLink>().await,
            Res::Controller => self.make_html_x::<Controller>().await,
            Res::Detector => self.make_html_x::<Detector>().await,
            Res::Dms => self.make_html_x::<Dms>().await,
            Res::FlowStream => self.make_html_x::<FlowStream>().await,
            Res::GateArm => self.make_html_x::<GateArm>().await,
            Res::GateArmArray => self.make_html_x::<GateArmArray>().await,
            Res::Gps => self.make_html_x::<Gps>().await,
            Res::LaneMarking => self.make_html_x::<LaneMarking>().await,
            Res::LcsArray => self.make_html_x::<LcsArray>().await,
            Res::LcsIndication => self.make_html_x::<LcsIndication>().await,
            Res::Modem => self.make_html_x::<Modem>().await,
            Res::Permission => self.make_html_x::<Permission>().await,
            Res::RampMeter => self.make_html_x::<RampMeter>().await,
            Res::Role => self.make_html_x::<Role>().await,
            Res::TagReader => self.make_html_x::<TagReader>().await,
            Res::User => self.make_html_x::<User>().await,
            Res::VideoMonitor => self.make_html_x::<VideoMonitor>().await,
            Res::WeatherSensor => self.make_html_x::<WeatherSensor>().await,
            _ => unreachable!(),
        }
    }

    /// Make HTML view of card list
    async fn make_html_x<C: Card>(&mut self) -> Result<String> {
        let cards: Vec<C> = serde_json::from_str(&self.json)?;
        // Use default value for ancillary data lookup
        let pri = C::default();
        let anc = fetch_ancillary(View::Search, &pri).await?;
        let rname = C::res().as_str();
        let mut html = String::new();
        html.push_str("<ul class='cards'>");
        if self.config {
            let cn = View::CreateCompact.class_name();
            let next_name = C::next_name(&cards);
            // the "Create" card has id "{rname}_" and next available name
            html.push_str(&format!(
                "<li id='{rname}_' name='{next_name}' class='{cn}'>\
                    {CREATE_COMPACT}\
                </li>"
            ));
        }
        self.views.clear();
        for pri in cards {
            let view = if self.search.is_match(&pri, &anc) {
                View::Compact
            } else {
                View::Hidden
            };
            self.views.push(view);
            let cn = view.class_name();
            let name = pri.to_string();
            html.push_str(&format!(
                "<li id='{rname}_{name}' name='{name}' class='{cn}'>"
            ));
            html.push_str(&pri.to_html(view, &anc));
            html.push_str("</li>");
        }
        html.push_str("</ul>");
        Ok(html)
    }

    /// Get a list of cards whose view has changed
    pub async fn view_change(&mut self) -> Result<Vec<CardView>> {
        match self.res {
            Res::Alarm => self.view_change_x::<Alarm>().await,
            Res::Beacon => self.view_change_x::<Beacon>().await,
            Res::CabinetStyle => self.view_change_x::<CabinetStyle>().await,
            Res::Camera => self.view_change_x::<Camera>().await,
            Res::CommConfig => self.view_change_x::<CommConfig>().await,
            Res::CommLink => self.view_change_x::<CommLink>().await,
            Res::Controller => self.view_change_x::<Controller>().await,
            Res::Detector => self.view_change_x::<Detector>().await,
            Res::Dms => self.view_change_x::<Dms>().await,
            Res::FlowStream => self.view_change_x::<FlowStream>().await,
            Res::GateArm => self.view_change_x::<GateArm>().await,
            Res::GateArmArray => self.view_change_x::<GateArmArray>().await,
            Res::Gps => self.view_change_x::<Gps>().await,
            Res::LaneMarking => self.view_change_x::<LaneMarking>().await,
            Res::LcsArray => self.view_change_x::<LcsArray>().await,
            Res::LcsIndication => self.view_change_x::<LcsIndication>().await,
            Res::Modem => self.view_change_x::<Modem>().await,
            Res::Permission => self.view_change_x::<Permission>().await,
            Res::RampMeter => self.view_change_x::<RampMeter>().await,
            Res::Role => self.view_change_x::<Role>().await,
            Res::TagReader => self.view_change_x::<TagReader>().await,
            Res::User => self.view_change_x::<User>().await,
            Res::VideoMonitor => self.view_change_x::<VideoMonitor>().await,
            Res::WeatherSensor => self.view_change_x::<WeatherSensor>().await,
            _ => unreachable!(),
        }
    }

    /// Get a list of cards whose view has changed
    async fn view_change_x<C: Card>(&mut self) -> Result<Vec<CardView>> {
        // Use default value for ancillary data lookup
        let pri = C::default();
        let anc = fetch_ancillary(View::Search, &pri).await?;
        let mut changes = Vec::new();
        let mut views = Vec::with_capacity(self.views.len());
        let mut old_views = self.views.iter();
        for pri in serde_json::from_str::<Vec<C>>(&self.json)? {
            let view = if self.search.is_match(&pri, &anc) {
                View::Compact
            } else {
                View::Hidden
            };
            let v = old_views.next().unwrap_or(&View::Compact);
            if *v != view {
                let name = pri.to_string();
                changes.push(CardView::new(C::res(), name, view));
            }
            views.push(view);
        }
        self.views = views;
        Ok(changes)
    }

    /// Get a Vec of changed cards
    pub async fn changed_vec(
        &self,
        json: String,
        cv: &Option<CardView>,
    ) -> Result<Vec<(String, String)>> {
        match self.res {
            Res::Alarm => self.changed::<Alarm>(json, cv).await,
            Res::Beacon => self.changed::<Beacon>(json, cv).await,
            Res::CabinetStyle => self.changed::<CabinetStyle>(json, cv).await,
            Res::Camera => self.changed::<Camera>(json, cv).await,
            Res::CommConfig => self.changed::<CommConfig>(json, cv).await,
            Res::CommLink => self.changed::<CommLink>(json, cv).await,
            Res::Controller => self.changed::<Controller>(json, cv).await,
            Res::Detector => self.changed::<Detector>(json, cv).await,
            Res::Dms => self.changed::<Dms>(json, cv).await,
            Res::FlowStream => self.changed::<FlowStream>(json, cv).await,
            Res::GateArm => self.changed::<GateArm>(json, cv).await,
            Res::GateArmArray => self.changed::<GateArmArray>(json, cv).await,
            Res::Gps => self.changed::<Gps>(json, cv).await,
            Res::LaneMarking => self.changed::<LaneMarking>(json, cv).await,
            Res::LcsArray => self.changed::<LcsArray>(json, cv).await,
            Res::LcsIndication => self.changed::<LcsIndication>(json, cv).await,
            Res::Modem => self.changed::<Modem>(json, cv).await,
            Res::Permission => self.changed::<Permission>(json, cv).await,
            Res::RampMeter => self.changed::<RampMeter>(json, cv).await,
            Res::Role => self.changed::<Role>(json, cv).await,
            Res::TagReader => self.changed::<TagReader>(json, cv).await,
            Res::User => self.changed::<User>(json, cv).await,
            Res::VideoMonitor => self.changed::<VideoMonitor>(json, cv).await,
            Res::WeatherSensor => self.changed::<WeatherSensor>(json, cv).await,
            _ => unreachable!(),
        }
    }

    /// Make a Vec of changed cards
    async fn changed<C: Card>(
        &self,
        json: String,
        cv: &Option<CardView>,
    ) -> Result<Vec<(String, String)>> {
        // Use default value for ancillary data lookup
        let pri = C::default();
        let anc = fetch_ancillary(View::Search, &pri).await?;
        let cards0 = serde_json::from_str::<Vec<C>>(&json)?.into_iter();
        let cards1 = serde_json::from_str::<Vec<C>>(&self.json)?.into_iter();
        let mut values = Vec::new();
        for (c0, c1) in cards0.zip(cards1) {
            let id0 = c0.id();
            let id1 = c1.id();
            if id0 != id1 {
                return Err(Error::CardMismatch());
            }
            if c0 != c1 {
                let mut view = View::Compact;
                if let Some(cv) = cv {
                    if id1 == cv.id() {
                        view = cv.view;
                    }
                }
                values.push((id1, c1.to_html(view, &anc)));
            }
        }
        Ok(values)
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
pub async fn fetch_one(cv: &CardView) -> Result<String> {
    match cv.view {
        View::CreateCompact => Ok(CREATE_COMPACT.into()),
        View::Create => {
            let html = fetch_one_res(cv).await?;
            Ok(html_card_create(cv.res, &html))
        }
        View::Compact => fetch_one_res(cv).await,
        View::Location => match fetch_geo_loc(cv).await? {
            Some(geo_loc) => card_location(&geo_loc).await,
            None => unreachable!(),
        },
        View::Status(_config) if has_status(cv.res) => {
            let html = fetch_one_res(cv).await?;
            Ok(html_card_status(cv.res, &cv.name, &html))
        }
        _ => {
            let cv = cv.clone().view(View::Edit);
            let html = fetch_one_res(&cv).await?;
            Ok(html_card_edit(cv.res, &cv.name, &html, DEL_BUTTON))
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
async fn fetch_one_res(cv: &CardView) -> Result<String> {
    match cv.res {
        Res::Alarm => fetch_one_x::<Alarm>(cv).await,
        Res::Beacon => fetch_one_x::<Beacon>(cv).await,
        Res::CabinetStyle => fetch_one_x::<CabinetStyle>(cv).await,
        Res::Camera => fetch_one_x::<Camera>(cv).await,
        Res::CommConfig => fetch_one_x::<CommConfig>(cv).await,
        Res::CommLink => fetch_one_x::<CommLink>(cv).await,
        Res::Controller => fetch_one_x::<Controller>(cv).await,
        Res::Detector => fetch_one_x::<Detector>(cv).await,
        Res::Dms => fetch_one_x::<Dms>(cv).await,
        Res::FlowStream => fetch_one_x::<FlowStream>(cv).await,
        Res::GateArm => fetch_one_x::<GateArm>(cv).await,
        Res::GateArmArray => fetch_one_x::<GateArmArray>(cv).await,
        Res::GeoLoc => fetch_one_x::<GeoLoc>(cv).await,
        Res::Gps => fetch_one_x::<Gps>(cv).await,
        Res::LaneMarking => fetch_one_x::<LaneMarking>(cv).await,
        Res::LcsArray => fetch_one_x::<LcsArray>(cv).await,
        Res::LcsIndication => fetch_one_x::<LcsIndication>(cv).await,
        Res::Modem => fetch_one_x::<Modem>(cv).await,
        Res::Permission => fetch_one_x::<Permission>(cv).await,
        Res::RampMeter => fetch_one_x::<RampMeter>(cv).await,
        Res::Role => fetch_one_x::<Role>(cv).await,
        Res::TagReader => fetch_one_x::<TagReader>(cv).await,
        Res::User => fetch_one_x::<User>(cv).await,
        Res::VideoMonitor => fetch_one_x::<VideoMonitor>(cv).await,
        Res::WeatherSensor => fetch_one_x::<WeatherSensor>(cv).await,
        _ => unreachable!(),
    }
}

/// Fetch a card view
async fn fetch_one_x<C: Card>(cv: &CardView) -> Result<String> {
    let pri = if cv.view == View::Create {
        C::default().with_name(&cv.name)
    } else {
        fetch_primary::<C>(&cv.name).await?
    };
    let anc = fetch_ancillary(cv.view, &pri).await?;
    Ok(pri.to_html(cv.view, &anc))
}

/// Fetch primary JSON resource
async fn fetch_primary<C: Card>(name: &str) -> Result<C> {
    let json = C::uri_name(name).get().await?;
    C::new(json)
}

/// Fetch geo location name (if any)
pub async fn fetch_geo_loc(cv: &CardView) -> Result<Option<String>> {
    match cv.res {
        Res::Beacon => geo_loc::<Beacon>(cv).await,
        Res::Camera => geo_loc::<Camera>(cv).await,
        Res::Controller => geo_loc::<Controller>(cv).await,
        Res::Dms => geo_loc::<Dms>(cv).await,
        Res::GateArmArray => geo_loc::<GateArmArray>(cv).await,
        Res::GeoLoc => Ok(Some(cv.name.to_string())),
        Res::LaneMarking => geo_loc::<LaneMarking>(cv).await,
        Res::RampMeter => geo_loc::<RampMeter>(cv).await,
        Res::TagReader => geo_loc::<TagReader>(cv).await,
        Res::WeatherSensor => geo_loc::<WeatherSensor>(cv).await,
        _ => Ok(None),
    }
}

/// Fetch geo location name
async fn geo_loc<C: Card>(cv: &CardView) -> Result<Option<String>> {
    let pri = fetch_primary::<C>(&cv.name).await?;
    match pri.geo_loc() {
        Some(geo_loc) => Ok(Some(geo_loc.to_string())),
        None => Ok(None),
    }
}

/// Patch changed fields on card
pub async fn patch_changed(cv: &CardView) -> Result<()> {
    match cv.res {
        Res::Alarm => patch_changed_x::<Alarm>(cv).await,
        Res::Beacon => patch_changed_x::<Beacon>(cv).await,
        Res::CabinetStyle => patch_changed_x::<CabinetStyle>(cv).await,
        Res::Camera => patch_changed_x::<Camera>(cv).await,
        Res::CommConfig => patch_changed_x::<CommConfig>(cv).await,
        Res::CommLink => patch_changed_x::<CommLink>(cv).await,
        Res::Controller => patch_changed_x::<Controller>(cv).await,
        Res::Detector => patch_changed_x::<Detector>(cv).await,
        Res::Dms => patch_changed_x::<Dms>(cv).await,
        Res::FlowStream => patch_changed_x::<FlowStream>(cv).await,
        Res::GateArm => patch_changed_x::<GateArm>(cv).await,
        Res::GateArmArray => patch_changed_x::<GateArmArray>(cv).await,
        Res::GeoLoc => patch_changed_x::<GeoLoc>(cv).await,
        Res::Gps => patch_changed_x::<Gps>(cv).await,
        Res::LaneMarking => patch_changed_x::<LaneMarking>(cv).await,
        Res::LcsArray => patch_changed_x::<LcsArray>(cv).await,
        Res::LcsIndication => patch_changed_x::<LcsIndication>(cv).await,
        Res::Modem => patch_changed_x::<Modem>(cv).await,
        Res::Permission => patch_changed_x::<Permission>(cv).await,
        Res::RampMeter => patch_changed_x::<RampMeter>(cv).await,
        Res::Role => patch_changed_x::<Role>(cv).await,
        Res::TagReader => patch_changed_x::<TagReader>(cv).await,
        Res::User => patch_changed_x::<User>(cv).await,
        Res::VideoMonitor => patch_changed_x::<VideoMonitor>(cv).await,
        Res::WeatherSensor => patch_changed_x::<WeatherSensor>(cv).await,
        _ => unreachable!(),
    }
}

/// Patch changed fields from an Edit view
async fn patch_changed_x<C: Card>(cv: &CardView) -> Result<()> {
    let pri = fetch_primary::<C>(&cv.name).await?;
    let changed = pri.changed_fields();
    if !changed.is_empty() {
        C::uri_name(&cv.name).patch(&changed.into()).await?;
    }
    Ok(())
}

/// Handle click event for a button owned by the resource
pub async fn handle_click(cv: &CardView, id: &str) -> Result<bool> {
    match cv.res {
        Res::Beacon => handle_click_x::<Beacon>(cv, id).await,
        Res::Dms => handle_click_x::<Dms>(cv, id).await,
        _ => Ok(false),
    }
}

/// Handle click event for a button on a card
async fn handle_click_x<C: Card>(cv: &CardView, id: &str) -> Result<bool> {
    let pri = fetch_primary::<C>(&cv.name).await?;
    let anc = fetch_ancillary(View::Status(false), &pri).await?;
    let uri = C::uri_name(&cv.name);
    for action in pri.handle_click(anc, id, uri) {
        action.perform().await?;
    }
    Ok(true)
}

/// Handle input event for an element owned by the resource
pub async fn handle_input(cv: &CardView, id: &str) -> Result<bool> {
    match cv.res {
        Res::Dms => handle_input_x::<Dms>(&cv.name, id).await,
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
    let cv = CardView::new(Res::GeoLoc, name.to_string(), View::Edit);
    let html = fetch_one_res(&cv).await?;
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
