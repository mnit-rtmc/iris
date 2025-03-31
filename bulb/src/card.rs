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
use crate::alarm::Alarm;
use crate::asset::Asset;
use crate::beacon::Beacon;
use crate::cabinetstyle::CabinetStyle;
use crate::camera::Camera;
use crate::commconfig::CommConfig;
use crate::commlink::CommLink;
use crate::controller::Controller;
use crate::detector::Detector;
use crate::dms::Dms;
use crate::domain::Domain;
use crate::error::{Error, Result};
use crate::fetch::{Action, Uri};
use crate::flowstream::FlowStream;
use crate::gatearm::GateArm;
use crate::gatearmarray::GateArmArray;
use crate::geoloc::Loc;
use crate::gps::Gps;
use crate::item::ItemState;
use crate::lcs::Lcs;
use crate::lcsstate::LcsState;
use crate::modem::Modem;
use crate::permission::Permission;
use crate::rampmeter::RampMeter;
use crate::role::Role;
use crate::signconfig::SignConfig;
use crate::tagreader::TagReader;
use crate::user::User;
use crate::util::{Doc, HtmlStr};
use crate::videomonitor::VideoMonitor;
use crate::weathersensor::WeatherSensor;
use futures::StreamExt;
use futures::stream::FuturesUnordered;
use resources::Res;
use serde::de::DeserializeOwned;
use serde_json::Value;
use serde_json::map::Map;
use std::borrow::Cow;
use std::fmt;
use std::iter::repeat;
use wasm_bindgen::JsValue;

/// Compact "Create" card
const CREATE_COMPACT: &str = "<span class='create'>Create 🆕</span>";

/// Save button
const SAVE_BUTTON: &str = "<button id='ob_save' type='button'>🖍️ Save</button>";

/// Card element view
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum View {
    /// Hidden view
    Hidden,
    /// Search view
    Search,
    /// Compact Create view
    CreateCompact,
    /// Create view
    Create,
    /// Compact view
    Compact,
    /// Control view
    Control,
    /// Setup view
    Setup,
    /// Status view
    Status,
    /// Location view
    Location,
    /// Request view
    Request,
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
        match self {
            View::Hidden
            | View::Search
            | View::CreateCompact
            | View::Compact => false,
            View::Create
            | View::Control
            | View::Setup
            | View::Status
            | View::Location
            | View::Request => true,
        }
    }

    /// Get compact view
    pub fn compact(self) -> Self {
        match self {
            View::Create => View::CreateCompact,
            _ => View::Compact,
        }
    }

    /// Get view as string slice
    pub fn as_str(self) -> &'static str {
        use View::*;
        match self {
            Hidden => "Hidden",
            Search => "Search",
            CreateCompact => "Create compact",
            Create => "🆕 Create",
            Compact => "⌄ Compact",
            Control => "🕹️ Control",
            Setup => "📝 Setup",
            Status => "☑️ Status",
            Location => "🗺️ Location",
            Request => "🙏 Request",
        }
    }
}

impl TryFrom<&str> for View {
    type Error = ();

    fn try_from(type_n: &str) -> std::result::Result<Self, Self::Error> {
        use View::*;
        match type_n {
            v if v == Hidden.as_str() => Ok(Hidden),
            v if v == Search.as_str() => Ok(Search),
            v if v == CreateCompact.as_str() => Ok(CreateCompact),
            v if v == Create.as_str() => Ok(Create),
            v if v == Compact.as_str() => Ok(Compact),
            v if v == Control.as_str() => Ok(Control),
            v if v == Setup.as_str() => Ok(Setup),
            v if v == Status.as_str() => Ok(Status),
            v if v == Location.as_str() => Ok(Location),
            v if v == Request.as_str() => Ok(Request),
            _ => Err(()),
        }
    }
}

/// Card view
#[derive(Clone, Debug, PartialEq, Eq)]
pub struct CardView {
    /// Resource type
    pub res: Res,
    /// Object name
    pub name: String,
    /// Card view
    pub view: View,
}

impl fmt::Display for CardView {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        let id = self.id();
        let name = &self.name;
        let cn = self.view.class_name();
        write!(f, "id='{id}' name='{name}' class='{cn}'")
    }
}

impl CardView {
    /// Create a new card view
    pub fn new<N: Into<String>>(res: Res, name: N, view: View) -> Self {
        let name = name.into();
        CardView { res, name, view }
    }

    /// Get HTML element ID of card
    pub fn id(&self) -> String {
        let res = self.res;
        let nm = match self.view {
            View::CreateCompact | View::Create => "",
            _ => &self.name,
        };
        format!("{res}_{nm}")
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

    /// Construct ancillary data
    fn new(_pri: &Self::Primary, _view: View) -> Self;

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        None
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        _pri: &Self::Primary,
        _asset: Asset,
        _value: JsValue,
    ) -> Result<()> {
        Ok(())
    }
}

/// Default item states as html options
const ITEM_STATES: &str = "<option value=''>all ↴</option>\
     <option value='🔹'>🔹 available</option>\
     <option value='🔌'>🔌 offline</option>\
     <option value='🔻'>🔻 inactive</option>";

/// A card view of a resource
pub trait Card: Default + DeserializeOwned + PartialEq {
    type Ancillary: AncillaryData<Primary = Self>;

    /// Display name
    const DNAME: &'static str;

    /// All item states as html options
    const ITEM_STATES: &'static str = ITEM_STATES;

    /// Suggested name prefix
    const PREFIX: &'static str = "";

    /// Get the resource
    fn res() -> Res;

    /// Create from a JSON value
    fn new(json: JsValue) -> Result<Self> {
        Ok(serde_wasm_bindgen::from_value(json)?)
    }

    /// Get the name
    fn name(&self) -> Cow<str>;

    /// Set the name
    fn with_name(self, name: &str) -> Self;

    /// Get the main item state
    fn item_state_main(&self, _anc: &Self::Ancillary) -> ItemState {
        ItemState::Unknown
    }

    /// Check if a search string matches
    fn is_match(&self, _search: &str, _anc: &Self::Ancillary) -> bool {
        false
    }

    /// Convert to Create HTML
    fn to_html_create(&self, _anc: &Self::Ancillary) -> String {
        let name = HtmlStr::new(self.name());
        format!(
            "<div class='row'>\
              <label for='create_name'>Name</label>\
              <input id='create_name' maxlength='24' size='24' value='{name}'>\
            </div>"
        )
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, _anc: &Self::Ancillary) -> String;

    /// Get changed fields from Setup view
    fn changed_setup(&self) -> String {
        String::new()
    }

    /// Get changed fields on Location view
    fn changed_location(&self, _anc: Self::Ancillary) -> String {
        String::new()
    }

    /// Handle click event for a button on the card
    fn handle_click(&self, _anc: Self::Ancillary, _id: String) -> Vec<Action> {
        Vec::new()
    }

    /// Handle input event for an element on the card
    fn handle_input(&self, _anc: Self::Ancillary, _id: String) -> Vec<Action> {
        Vec::new()
    }

    /// Build card title
    fn title(&self, view: View) -> String {
        let name =
            format!("{} {}", Self::res().symbol(), HtmlStr::new(self.name()));
        let mut views = String::new();
        views.push_str("<select id='ob_view'>");
        for v in res_views(Self::res()) {
            if *v == view {
                views.push_str("<option selected>");
            } else {
                views.push_str("<option>");
            }
            views.push_str(v.as_str());
            views.push_str("</option>");
        }
        views.push_str("</select>");
        html_title_row(&[&name, &views], &[])
    }

    /// Build card footer
    fn footer(&self, delete: bool) -> String {
        let ob_delete = if delete {
            "<button id='ob_delete' type='button'>🗑️ Delete</button>"
        } else {
            ""
        };
        format!(
            "<div class='row'>\
              <span></span>\
              {ob_delete}\
              {SAVE_BUTTON}\
            </div>"
        )
    }
}

/// Get all item states as html options
pub fn item_states(res: Option<Res>) -> &'static str {
    match res {
        Some(Res::Beacon) => Beacon::ITEM_STATES,
        Some(Res::CabinetStyle) => "",
        Some(Res::CommConfig) => "",
        Some(Res::Dms) => Dms::ITEM_STATES,
        Some(Res::Domain) => Domain::ITEM_STATES,
        Some(Res::GateArm | Res::GateArmArray) => GateArm::ITEM_STATES,
        Some(Res::Lcs) => Lcs::ITEM_STATES,
        Some(Res::Permission) => Permission::ITEM_STATES,
        Some(Res::RampMeter) => RampMeter::ITEM_STATES,
        Some(Res::Role) => Role::ITEM_STATES,
        Some(Res::SignConfig) => SignConfig::ITEM_STATES,
        Some(Res::User) => User::ITEM_STATES,
        Some(_) => ITEM_STATES,
        None => "",
    }
}

/// Get available views for a resource type
pub fn res_views(res: Res) -> &'static [View] {
    match res {
        Res::CabinetStyle
        | Res::CommConfig
        | Res::Domain
        | Res::FlowStream
        | Res::Gps
        | Res::LcsState
        | Res::Modem
        | Res::Permission
        | Res::Role
        | Res::SignConfig
        | Res::User => &[View::Compact, View::Setup],
        Res::GateArmArray => &[View::Compact, View::Control, View::Location],
        Res::Beacon | Res::Lcs | Res::RampMeter => {
            &[View::Compact, View::Control, View::Location, View::Setup]
        }
        Res::Camera => &[
            View::Compact,
            View::Control,
            View::Location,
            View::Request,
            View::Setup,
        ],
        Res::Dms => &[
            View::Compact,
            View::Control,
            View::Location,
            View::Request,
            View::Setup,
            View::Status,
        ],
        Res::Alarm
        | Res::CommLink
        | Res::Detector
        | Res::GateArm
        | Res::VideoMonitor => &[View::Compact, View::Status, View::Setup],
        Res::Controller | Res::TagReader | Res::WeatherSensor => {
            &[View::Compact, View::Status, View::Location, View::Setup]
        }
        _ => &[View::Compact],
    }
}

/// Get the URI of a resource (all)
fn uri_all(res: Res) -> Uri {
    let mut uri = Uri::from("/iris/api/");
    uri.push(res.as_str());
    uri
}

/// Get the URI of a resource (one)
pub fn uri_one(res: Res, name: &str) -> Uri {
    let mut uri = uri_all(res);
    uri.push(name);
    uri
}

/// Create a new object
pub async fn create_and_post(res: Res) -> Result<()> {
    let doc = Doc::get();
    let value = match res {
        Res::Permission => Permission::create_value(&doc)?,
        _ => create_value(&doc)?,
    };
    uri_all(res).post(&value.into()).await?;
    Ok(())
}

/// Create a name value
fn create_value(doc: &Doc) -> Result<String> {
    let name = doc
        .input_option_string("create_name")
        .ok_or(Error::ElemIdNotFound("create_name"))?;
    let mut obj = Map::new();
    obj.insert("name".to_string(), Value::String(name));
    Ok(Value::Object(obj).to_string())
}

/// Delete a resource by name
pub async fn delete_one(cv: &CardView) -> Result<()> {
    uri_one(cv.res, &cv.name).delete().await
}

/// Fetch `sb_resource` access list
pub async fn fetch_resource(config: bool) -> Result<String> {
    let json = Uri::from("/iris/api/access").get().await?;
    let access: Vec<Permission> = serde_wasm_bindgen::from_value(json)?;
    let mut html = "<option/>".to_string();
    if config {
        add_option::<Alarm>(&access, &mut html);
    }
    add_option::<Beacon>(&access, &mut html);
    if config {
        add_option::<CabinetStyle>(&access, &mut html);
    }
    add_option::<Camera>(&access, &mut html);
    if config {
        add_option::<CommConfig>(&access, &mut html);
        add_option::<CommLink>(&access, &mut html);
        add_option::<Controller>(&access, &mut html);
        add_option::<Detector>(&access, &mut html);
    }
    add_option::<Dms>(&access, &mut html);
    if config {
        add_option::<Domain>(&access, &mut html);
        add_option::<FlowStream>(&access, &mut html);
        add_option::<GateArm>(&access, &mut html);
    }
    add_option::<GateArmArray>(&access, &mut html);
    if config {
        add_option::<Gps>(&access, &mut html);
    }
    add_option::<Lcs>(&access, &mut html);
    if config {
        add_option::<LcsState>(&access, &mut html);
        add_option::<Modem>(&access, &mut html);
        add_option::<Permission>(&access, &mut html);
    }
    add_option::<RampMeter>(&access, &mut html);
    if config {
        add_option::<Role>(&access, &mut html);
        add_option::<SignConfig>(&access, &mut html);
        add_option::<TagReader>(&access, &mut html);
        add_option::<User>(&access, &mut html);
    }
    add_option::<VideoMonitor>(&access, &mut html);
    add_option::<WeatherSensor>(&access, &mut html);
    Ok(html)
}

/// Add option to access select
fn add_option<C: Card>(access: &[Permission], html: &mut String) {
    for perm in access {
        if perm.hashtag.is_none() {
            let res = C::res();
            if perm.base_resource == res.base().as_str() {
                html.push_str("<option value='");
                html.push_str(res.as_str());
                html.push_str("'>");
                html.push_str(C::DNAME);
                html.push_str("</option>");
            }
        }
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
    views: Vec<CardView>,
    /// Main item states for all cards, as a JSON map
    states_main: String,
}

impl CardList {
    /// Create a new card list
    pub fn new(res: Res) -> Self {
        let config = false;
        let search = Search::Empty();
        let json = String::new();
        let views = Vec::new();
        let states_main = String::new();
        CardList {
            res,
            config,
            search,
            json,
            views,
            states_main,
        }
    }

    /// Set config mode
    pub fn config(mut self, config: bool) -> Self {
        // sign configs cannot be created manually
        if self.res != Res::SignConfig {
            self.config = config;
        }
        self
    }

    /// Set search term
    pub fn search(&mut self, search: &str) {
        self.search = Search::new(search);
    }

    /// Take current JSON value
    pub fn json(&mut self) -> String {
        std::mem::take(&mut self.json)
    }

    /// Get main item states
    pub fn states_main(&self) -> &str {
        &self.states_main
    }

    /// Get form card (if any)
    pub fn form(&self) -> Option<CardView> {
        self.views.iter().find(|cv| cv.view.is_form()).cloned()
    }

    /// Set card view
    pub fn set_view(&mut self, cv: CardView) {
        for vv in &mut self.views {
            if vv.name == cv.name {
                vv.view = cv.view;
                break;
            }
        }
    }

    /// Fetch card list
    pub async fn fetch(&mut self) -> Result<()> {
        let json = uri_all(self.res).get().await?;
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
            Res::Domain => self.make_html_x::<Domain>().await,
            Res::FlowStream => self.make_html_x::<FlowStream>().await,
            Res::GateArm => self.make_html_x::<GateArm>().await,
            Res::GateArmArray => self.make_html_x::<GateArmArray>().await,
            Res::Gps => self.make_html_x::<Gps>().await,
            Res::Lcs => self.make_html_x::<Lcs>().await,
            Res::LcsState => self.make_html_x::<LcsState>().await,
            Res::Modem => self.make_html_x::<Modem>().await,
            Res::Permission => self.make_html_x::<Permission>().await,
            Res::RampMeter => self.make_html_x::<RampMeter>().await,
            Res::Role => self.make_html_x::<Role>().await,
            Res::SignConfig => self.make_html_x::<SignConfig>().await,
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
        let anc = fetch_ancillary(&C::default(), View::Search).await?;
        self.views.clear();
        let mut html = String::new();
        html.push_str("<ul class='cards'>");
        if self.config {
            let cv = CardView::new(
                C::res(),
                Self::next_name(&cards),
                View::CreateCompact,
            );
            html.push_str(&format!("<li {cv}>{CREATE_COMPACT}</li>"));
            self.views.push(cv);
        }
        for pri in &cards {
            let view = if self.search.is_match(pri, &anc) {
                View::Compact
            } else {
                View::Hidden
            };
            let cv = CardView::new(C::res(), pri.name(), view);
            html.push_str(&format!("<li {cv}>"));
            html.push_str(&pri.to_html(view, &anc));
            html.push_str("</li>");
            self.views.push(cv);
        }
        html.push_str("</ul>");
        self.states_main = build_item_states(&cards, &anc);
        Ok(html)
    }

    /// Get next suggested name
    fn next_name<C: Card>(obs: &[C]) -> String {
        let prefix = C::PREFIX;
        if prefix.is_empty() {
            return String::new();
        }
        let mut num = 1;
        for ob in obs {
            if let Some((pre, suffix)) = ob.name().split_once('_') {
                if pre == prefix {
                    if let Ok(n) = suffix.parse::<u32>() {
                        num = num.max(n + 1);
                    }
                }
            }
        }
        format!("{prefix}_{num}")
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
            Res::Domain => self.view_change_x::<Domain>().await,
            Res::FlowStream => self.view_change_x::<FlowStream>().await,
            Res::GateArm => self.view_change_x::<GateArm>().await,
            Res::GateArmArray => self.view_change_x::<GateArmArray>().await,
            Res::Gps => self.view_change_x::<Gps>().await,
            Res::Lcs => self.view_change_x::<Lcs>().await,
            Res::LcsState => self.view_change_x::<LcsState>().await,
            Res::Modem => self.view_change_x::<Modem>().await,
            Res::Permission => self.view_change_x::<Permission>().await,
            Res::RampMeter => self.view_change_x::<RampMeter>().await,
            Res::Role => self.view_change_x::<Role>().await,
            Res::SignConfig => self.view_change_x::<SignConfig>().await,
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
        let anc = fetch_ancillary(&C::default(), View::Search).await?;
        let mut changes = Vec::new();
        let mut views = Vec::with_capacity(self.views.len());
        let mut old_views = self.views.drain(..);
        if self.config {
            if let Some(cv) = old_views.next() {
                views.push(cv);
            }
        }
        for pri in serde_json::from_str::<Vec<C>>(&self.json)? {
            let vv = old_views.next().unwrap_or(CardView::new(
                C::res(),
                pri.name(),
                View::Compact,
            ));
            let view = if vv.view.is_form() {
                vv.view
            } else if self.search.is_match(&pri, &anc) {
                View::Compact
            } else {
                View::Hidden
            };
            let cv = CardView::new(C::res(), pri.name(), view);
            if vv != cv {
                changes.push(cv.clone());
            }
            views.push(cv);
        }
        drop(old_views);
        self.views = views;
        Ok(changes)
    }

    /// Get a Vec of changed cards
    pub async fn changed_vec(
        &mut self,
        json: String,
    ) -> Result<Vec<(CardView, String)>> {
        match self.res {
            Res::Alarm => self.changed::<Alarm>(json).await,
            Res::Beacon => self.changed::<Beacon>(json).await,
            Res::CabinetStyle => self.changed::<CabinetStyle>(json).await,
            Res::Camera => self.changed::<Camera>(json).await,
            Res::CommConfig => self.changed::<CommConfig>(json).await,
            Res::CommLink => self.changed::<CommLink>(json).await,
            Res::Controller => self.changed::<Controller>(json).await,
            Res::Detector => self.changed::<Detector>(json).await,
            Res::Dms => self.changed::<Dms>(json).await,
            Res::Domain => self.changed::<Domain>(json).await,
            Res::FlowStream => self.changed::<FlowStream>(json).await,
            Res::GateArm => self.changed::<GateArm>(json).await,
            Res::GateArmArray => self.changed::<GateArmArray>(json).await,
            Res::Gps => self.changed::<Gps>(json).await,
            Res::Lcs => self.changed::<Lcs>(json).await,
            Res::LcsState => self.changed::<LcsState>(json).await,
            Res::Modem => self.changed::<Modem>(json).await,
            Res::Permission => self.changed::<Permission>(json).await,
            Res::RampMeter => self.changed::<RampMeter>(json).await,
            Res::Role => self.changed::<Role>(json).await,
            Res::TagReader => self.changed::<TagReader>(json).await,
            Res::User => self.changed::<User>(json).await,
            Res::VideoMonitor => self.changed::<VideoMonitor>(json).await,
            Res::WeatherSensor => self.changed::<WeatherSensor>(json).await,
            _ => unreachable!(),
        }
    }

    /// Make a Vec of changed cards
    async fn changed<C: Card>(
        &mut self,
        json: String,
    ) -> Result<Vec<(CardView, String)>> {
        // Use default value for ancillary data lookup
        let cards0 = serde_json::from_str::<Vec<C>>(&json)?.into_iter();
        let cards1 = serde_json::from_str::<Vec<C>>(&self.json)?;
        let anc = fetch_ancillary(&C::default(), View::Search).await?;
        let mut values = Vec::new();
        let mut views = self.views.iter();
        if self.config {
            // skip "Create" card
            views.next();
        }
        for (c0, c1) in cards0.zip(&cards1) {
            let cv = views.next();
            if c0.name() != c1.name() {
                return Err(Error::CardMismatch());
            }
            if c0 != *c1 {
                let cv = match cv {
                    Some(cv) => cv.clone(),
                    None => CardView::new(C::res(), c1.name(), View::Compact),
                };
                let html = if cv.view.is_form() {
                    fetch_one(&cv).await?
                } else {
                    c1.to_html(cv.view, &anc)
                };
                values.push((cv, html));
            }
        }
        self.states_main = build_item_states(&cards1, &anc);
        Ok(values)
    }
}

/// Fetch ancillary data
async fn fetch_ancillary<C: Card>(pri: &C, view: View) -> Result<C::Ancillary> {
    let mut anc = C::Ancillary::new(pri, view);
    let mut futures = FuturesUnordered::new();
    while let Some(asset) = anc.asset() {
        futures.push(asset.fetch());
    }
    while let Some(res) = futures.next().await {
        if let Some((asset, value)) = res? {
            anc.set_asset(pri, asset, value)?;
            // check for more new assets
            while let Some(asset) = anc.asset() {
                futures.push(asset.fetch());
            }
        }
    }
    Ok(anc)
}

/// Build item states JSON object
fn build_item_states<C: Card>(cards: &[C], anc: &C::Ancillary) -> String {
    let mut states = String::new();
    states.push('{');
    for pri in cards {
        if states.len() > 1 {
            states.push(',');
        }
        states.push('"');
        states.push_str(&pri.name());
        states.push_str("\":\"");
        states.push_str(pri.item_state_main(anc).code());
        states.push('"');
    }
    states.push('}');
    states
}

/// Fetch a card for a given view
pub async fn fetch_one(cv: &CardView) -> Result<String> {
    let html = match cv.view {
        View::Create => {
            let html = fetch_one_res(cv).await?;
            html_card_create(cv.res, &html)
        }
        View::CreateCompact => CREATE_COMPACT.to_string(),
        _ => fetch_one_res(cv).await?,
    };
    Ok(html)
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
        Res::Domain => fetch_one_x::<Domain>(cv).await,
        Res::FlowStream => fetch_one_x::<FlowStream>(cv).await,
        Res::GateArm => fetch_one_x::<GateArm>(cv).await,
        Res::GateArmArray => fetch_one_x::<GateArmArray>(cv).await,
        Res::Gps => fetch_one_x::<Gps>(cv).await,
        Res::Lcs => fetch_one_x::<Lcs>(cv).await,
        Res::LcsState => fetch_one_x::<LcsState>(cv).await,
        Res::Modem => fetch_one_x::<Modem>(cv).await,
        Res::Permission => fetch_one_x::<Permission>(cv).await,
        Res::RampMeter => fetch_one_x::<RampMeter>(cv).await,
        Res::Role => fetch_one_x::<Role>(cv).await,
        Res::SignConfig => fetch_one_x::<SignConfig>(cv).await,
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
        fetch_primary::<C>(cv).await?
    };
    let anc = fetch_ancillary(&pri, cv.view).await?;
    Ok(pri.to_html(cv.view, &anc))
}

/// Fetch primary JSON resource
async fn fetch_primary<C: Card>(cv: &CardView) -> Result<C> {
    let uri = uri_one(C::res(), &cv.name);
    let json = uri.get().await?;
    C::new(json)
}

/// Patch changed fields on Setup / Location view
pub async fn patch_changed(cv: &CardView) -> Result<()> {
    match cv.view {
        View::Setup => patch_setup(cv).await,
        View::Location => patch_loc(cv).await,
        _ => unreachable!(),
    }
}

/// Patch changed fields from a Setup view
async fn patch_setup(cv: &CardView) -> Result<()> {
    match cv.res {
        Res::Alarm => patch_setup_x::<Alarm>(cv).await,
        Res::Beacon => patch_setup_x::<Beacon>(cv).await,
        Res::CabinetStyle => patch_setup_x::<CabinetStyle>(cv).await,
        Res::Camera => patch_setup_x::<Camera>(cv).await,
        Res::CommConfig => patch_setup_x::<CommConfig>(cv).await,
        Res::CommLink => patch_setup_x::<CommLink>(cv).await,
        Res::Controller => patch_setup_x::<Controller>(cv).await,
        Res::Detector => patch_setup_x::<Detector>(cv).await,
        Res::Dms => patch_setup_x::<Dms>(cv).await,
        Res::Domain => patch_setup_x::<Domain>(cv).await,
        Res::FlowStream => patch_setup_x::<FlowStream>(cv).await,
        Res::GateArm => patch_setup_x::<GateArm>(cv).await,
        Res::GateArmArray => patch_setup_x::<GateArmArray>(cv).await,
        Res::Gps => patch_setup_x::<Gps>(cv).await,
        Res::Lcs => patch_setup_x::<Lcs>(cv).await,
        Res::LcsState => patch_setup_x::<LcsState>(cv).await,
        Res::Modem => patch_setup_x::<Modem>(cv).await,
        Res::Permission => patch_setup_x::<Permission>(cv).await,
        Res::RampMeter => patch_setup_x::<RampMeter>(cv).await,
        Res::Role => patch_setup_x::<Role>(cv).await,
        Res::SignConfig => patch_setup_x::<SignConfig>(cv).await,
        Res::TagReader => patch_setup_x::<TagReader>(cv).await,
        Res::User => patch_setup_x::<User>(cv).await,
        Res::VideoMonitor => patch_setup_x::<VideoMonitor>(cv).await,
        Res::WeatherSensor => patch_setup_x::<WeatherSensor>(cv).await,
        _ => unreachable!(),
    }
}

/// Patch changed fields from a Setup view
async fn patch_setup_x<C: Card>(cv: &CardView) -> Result<()> {
    let pri = fetch_primary::<C>(cv).await?;
    let changed = pri.changed_setup();
    if !changed.is_empty() {
        uri_one(C::res(), &cv.name).patch(&changed.into()).await?;
    }
    Ok(())
}

/// Patch changed fields from a Location view
async fn patch_loc(cv: &CardView) -> Result<()> {
    match cv.res {
        Res::Beacon => patch_loc_x::<Beacon>(cv).await,
        Res::Camera => patch_loc_x::<Camera>(cv).await,
        Res::Controller => patch_loc_x::<Controller>(cv).await,
        Res::Dms => patch_loc_x::<Dms>(cv).await,
        Res::GateArmArray => patch_loc_x::<GateArmArray>(cv).await,
        Res::RampMeter => patch_loc_x::<RampMeter>(cv).await,
        Res::TagReader => patch_loc_x::<TagReader>(cv).await,
        Res::WeatherSensor => patch_loc_x::<WeatherSensor>(cv).await,
        _ => unreachable!(),
    }
}

/// Patch changed fields from a Location view
async fn patch_loc_x<C>(cv: &CardView) -> Result<()>
where
    C: Card + Loc,
{
    let pri = fetch_primary::<C>(cv).await?;
    if let Some(geoloc) = pri.geoloc() {
        let anc = fetch_ancillary(&pri, View::Location).await?;
        let changed = pri.changed_location(anc);
        if !changed.is_empty() {
            let mut uri = uri_one(Res::GeoLoc, geoloc);
            uri.query("res", cv.res.as_str());
            uri.patch(&changed.into()).await?;
        }
    }
    Ok(())
}

/// Handle click event for a button owned by the resource
pub async fn handle_click(cv: &CardView, id: String) -> Result<()> {
    if cv.view != View::Control && cv.view != View::Request {
        return Ok(());
    }
    match cv.res {
        Res::Beacon => handle_click_x::<Beacon>(cv, id).await,
        Res::Camera => handle_click_x::<Camera>(cv, id).await,
        Res::Dms => handle_click_x::<Dms>(cv, id).await,
        _ => Ok(()),
    }
}

/// Handle click event for a button on a card
async fn handle_click_x<C: Card>(cv: &CardView, id: String) -> Result<()> {
    let pri = fetch_primary::<C>(cv).await?;
    let anc = fetch_ancillary(&pri, cv.view).await?;
    for action in pri.handle_click(anc, id) {
        action.perform().await?;
    }
    Ok(())
}

/// Handle input event for an element owned by the resource
pub async fn handle_input(cv: &CardView, id: String) -> Result<()> {
    if cv.view != View::Control {
        return Ok(());
    }
    match cv.res {
        Res::Dms => handle_input_x::<Dms>(cv, id).await,
        Res::Domain => handle_input_x::<Domain>(cv, id).await,
        _ => Ok(()),
    }
}

/// Handle input event for an element on a card
async fn handle_input_x<C: Card>(cv: &CardView, id: String) -> Result<()> {
    let pri = fetch_primary::<C>(cv).await?;
    let anc = fetch_ancillary(&pri, View::Control).await?;
    for action in pri.handle_input(anc, id) {
        action.perform().await?;
    }
    Ok(())
}

/// Build a create card
fn html_card_create(res: Res, create: &str) -> String {
    let name = format!("{} 🆕", res.symbol());
    let mut views = String::new();
    views.push_str("<select id='ob_view'>");
    views.push_str("<option>");
    views.push_str(View::CreateCompact.as_str());
    views.push_str("<option selected>");
    views.push_str(View::Create.as_str());
    views.push_str("</select>");
    let mut html = html_title_row(&[&name, &views], &[]);
    html.push_str(create);
    html.push_str("<div class='row end'>");
    html.push_str(SAVE_BUTTON);
    html.push_str("</div>");
    html
}

/// Build an HTML title row (div) from a slice of spans
pub fn html_title_row(spans: &[&str], cls: &[&str]) -> String {
    let mut row = String::from("<div class='title row'>");
    for (span, c) in spans.iter().zip(cls.iter().chain(repeat(&""))) {
        if !c.is_empty() {
            row.push_str("<span class='");
            row.push_str(c);
            row.push_str("'>");
        } else {
            row.push_str("<span>");
        }
        row.push_str(span);
        row.push_str("</span>");
    }
    row.push_str("</div>");
    row
}
