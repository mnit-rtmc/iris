// Copyright (C) 2022-2026  Minnesota Department of Transportation
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
use crate::actionplan::ActionPlan;
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
use crate::gps::Gps;
use crate::incident::Incident;
use crate::item::ItemState;
use crate::lcs::Lcs;
use crate::lcsstate::LcsState;
use crate::modem::Modem;
use crate::monitorstyle::MonitorStyle;
use crate::msgpattern::MsgPattern;
use crate::permission::Permission;
use crate::rampmeter::RampMeter;
use crate::role::Role;
use crate::signconfig::SignConfig;
use crate::systemattr::SystemAttr;
use crate::tagreader::TagReader;
use crate::user::User;
use crate::videomonitor::VideoMonitor;
use crate::view::{CardView, View};
use crate::weathersensor::WeatherSensor;
use crate::word::Word;
use futures::StreamExt;
use futures::stream::FuturesUnordered;
use hatmil::{Page, html};
use resources::Res;
use serde::de::DeserializeOwned;
use serde_json::Value;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// Card state
#[derive(Clone, Debug, PartialEq, Eq)]
pub struct CardState {
    /// Resource type
    pub res: Res,
    /// Object name
    pub name: String,
    /// Item state
    pub state: ItemState,
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

    /// Check if item state all
    fn is_all(&self) -> bool {
        match self {
            Search::Empty() => true,
            _ => false,
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

/// A card view of a resource
pub trait Card: Default + DeserializeOwned + PartialEq {
    type Ancillary: AncillaryData<Primary = Self>;

    /// Suggested name prefix
    const PREFIX: &'static str = "";

    /// Get the resource
    fn res() -> Res;

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[ItemState::Available, ItemState::Inactive]
    }

    /// Create from a JSON value
    fn new(json: JsValue) -> Result<Self> {
        Ok(serde_wasm_bindgen::from_value(json)?)
    }

    /// Get the name
    fn name(&self) -> Cow<'_, str>;

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
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("create_name").cdata("Name").close();
        div.input()
            .id("create_name")
            .maxlength(24)
            .size(24)
            .value(self.name());
        String::from(page)
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
    fn title<'p>(&self, view: View, div: &'p mut html::Div<'p>) {
        div.class("title row");
        div.span()
            .cdata(Self::res().symbol())
            .cdata(" ")
            .cdata(self.name())
            .close();
        self.views_html(view, &mut div.select());
        div.close();
    }

    /// Build views select
    fn views_html<'p>(&self, view: View, select: &'p mut html::Select<'p>) {
        select.id("ob_view");
        for v in res_views(Self::res()) {
            let mut option = select.option();
            if *v == view {
                option.selected();
            }
            option.cdata(v.as_str()).close();
        }
        select.close();
    }

    /// Build card footer HTML
    fn footer_html<'p>(&self, delete: bool, div: &'p mut html::Div<'p>) {
        div.class("row");
        div.span().close(); /* empty */
        if delete {
            div.button()
                .id("ob_delete")
                .r#type("button")
                .cdata("🗑️ Delete")
                .close();
        };
        div.button()
            .id("ob_save")
            .r#type("button")
            .cdata("🖍️ Save")
            .close();
        div.close();
    }
}

/// Build all item states HTML
pub fn item_states_html(res: Res) -> String {
    let mut page = Page::new();
    let mut option = page.frag::<html::Option>();
    option.value("").cdata("all ↴").close();
    for st in item_states_all(res) {
        let mut option = page.frag::<html::Option>();
        option.value(st.code());
        if Some(*st) == default_state(res) {
            option.selected();
        }
        option
            .cdata(st.code())
            .cdata(" ")
            .cdata(st.description())
            .close();
    }
    String::from(page)
}

/// Get the default item state for a resource
fn default_state(res: Res) -> Option<ItemState> {
    if item_states_all(res).contains(&ItemState::Operator) {
        Some(ItemState::Operator)
    } else if item_states_all(res).contains(&ItemState::Deployed) {
        Some(ItemState::Deployed)
    } else if item_states_all(res).contains(&ItemState::Available) {
        Some(ItemState::Available)
    } else {
        None
    }
}

/// Get slice of all item states for a resource
fn item_states_all(res: Res) -> &'static [ItemState] {
    match res {
        Res::ActionPlan => ActionPlan::item_states_all(),
        Res::Alarm => Alarm::item_states_all(),
        Res::Beacon => Beacon::item_states_all(),
        Res::Camera => Camera::item_states_all(),
        Res::CommLink => CommLink::item_states_all(),
        Res::Controller => Controller::item_states_all(),
        Res::Detector => Detector::item_states_all(),
        Res::Dms => Dms::item_states_all(),
        Res::Domain => Domain::item_states_all(),
        Res::GateArm => GateArm::item_states_all(),
        Res::Gps => Gps::item_states_all(),
        Res::Incident => Incident::item_states_all(),
        Res::Lcs => Lcs::item_states_all(),
        Res::MsgPattern => MsgPattern::item_states_all(),
        Res::Permission => Permission::item_states_all(),
        Res::RampMeter => RampMeter::item_states_all(),
        Res::Role => Role::item_states_all(),
        Res::SignConfig => SignConfig::item_states_all(),
        Res::SystemAttribute => SystemAttr::item_states_all(),
        Res::TagReader => TagReader::item_states_all(),
        Res::User => User::item_states_all(),
        Res::VideoMonitor => VideoMonitor::item_states_all(),
        Res::WeatherSensor => WeatherSensor::item_states_all(),
        Res::Word => Word::item_states_all(),
        _ => &[],
    }
}

/// Get available views for a resource type
pub fn res_views(res: Res) -> &'static [View] {
    match res {
        Res::ActionPlan | Res::Incident => {
            &[View::Compact, View::Control, View::Setup]
        }
        Res::CabinetStyle
        | Res::CommConfig
        | Res::Domain
        | Res::FlowStream
        | Res::Gps
        | Res::LcsState
        | Res::Modem
        | Res::MonitorStyle
        | Res::MsgPattern
        | Res::Permission
        | Res::Role
        | Res::SignConfig
        | Res::SystemAttribute
        | Res::User
        | Res::Word => &[View::Compact, View::Setup],
        Res::Beacon | Res::GateArm | Res::Lcs => {
            &[View::Compact, View::Control, View::Location, View::Setup]
        }
        Res::Camera | Res::RampMeter => &[
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
        Res::Alarm | Res::CommLink | Res::Detector | Res::VideoMonitor => {
            &[View::Compact, View::Status, View::Setup]
        }
        Res::Controller | Res::TagReader | Res::WeatherSensor => {
            &[View::Compact, View::Status, View::Location, View::Setup]
        }
        _ => &[View::Compact],
    }
}

/// Get the URI of a resource (all)
pub fn uri_all(res: Res) -> Uri {
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
    /// Old JSON list
    old_json: String,
    /// JSON list of cards
    json: String,
    /// Views in order of JSON list
    views: Vec<CardView>,
}

impl CardList {
    /// Create a new card list
    pub fn new(res: Res) -> Self {
        let old_json = String::new();
        let json = String::new();
        let views = Vec::new();
        CardList {
            res,
            old_json,
            json,
            views,
        }
    }

    /// Get selected resource type
    pub fn res(&self) -> Res {
        self.res
    }

    /// Set JSON of all cards
    pub fn with_json(mut self, json: String) -> Self {
        self.old_json = json;
        self
    }

    /// Get JSON of all cards
    pub fn json(&self) -> &str {
        &self.json
    }

    /// Get a `Vec` of all cards
    fn cards<C: Card>(&self, old: bool) -> Result<Vec<C>> {
        let json = if old { &self.old_json } else { &self.json };
        let cards: Vec<C> = serde_json::from_str(json)?;
        Ok(cards)
    }

    /// Get expanded view (if any)
    pub fn expanded_view(&self) -> Option<CardView> {
        self.views.iter().find(|cv| cv.view.is_expanded()).cloned()
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
    pub async fn fetch_all(&mut self) -> Result<()> {
        let json = uri_all(self.res).get().await?;
        let json: Value = serde_wasm_bindgen::from_value(json)?;
        self.json = json.to_string();
        Ok(())
    }

    /// Get main item states
    pub async fn states_main(&self) -> Result<Vec<CardState>> {
        cards_meth!(self, states_main_x)
    }

    /// Get main item states
    async fn states_main_x<C: Card>(&self) -> Result<Vec<CardState>> {
        let cards = self.cards::<C>(false)?;
        // Use default value for ancillary data lookup
        let anc = fetch_ancillary(&C::default(), View::Search).await?;
        let res = C::res();
        let mut states = Vec::new();
        for pri in &cards {
            states.push(CardState {
                res,
                name: pri.name().to_string(),
                state: pri.item_state_main(&anc),
            });
        }
        Ok(states)
    }

    /// Search card views
    async fn search_card_views<C: Card>(
        &mut self,
        search: &str,
        anc: &C::Ancillary,
    ) -> Result<()> {
        let cards = self.cards::<C>(false)?;
        let res = C::res();
        let search = Search::new(search);
        let mut views = Vec::with_capacity(cards.len());
        // "Create" card
        let view = if search.is_all()
            && res != Res::SignConfig
            && res != Res::SystemAttribute
        {
            View::CreateCompact
        } else {
            View::Hidden
        };
        let cv = CardView::new(res, Self::next_name(&cards), view);
        views.push(cv);
        for pri in &cards {
            let view = if search.is_match(pri, anc) {
                View::Compact
            } else {
                View::Hidden
            };
            let cv = CardView::new(res, pri.name(), view);
            views.push(cv);
        }
        self.views = views;
        Ok(())
    }

    /// Build HTML of full card list
    pub async fn build_html(&mut self, search: &str) -> Result<String> {
        cards_meth!(self, build_html_x, search)
    }

    /// Build HTML of full card list
    async fn build_html_x<C: Card>(&mut self, search: &str) -> Result<String> {
        let cards = self.cards::<C>(false)?;
        // Use default value for ancillary data lookup
        let anc = fetch_ancillary(&C::default(), View::Search).await?;
        self.search_card_views::<C>(search, &anc).await?;
        let mut views = self.views.iter();
        let mut page = Page::new();
        let mut ul = page.frag::<html::Ul>();
        ul.class("cards");
        if let Some(cv) = views.next() {
            let mut li = ul.li();
            li.id(cv.id())
                .data_("name", &cv.name)
                .class(cv.view.class_name());
            li.span().class("create").cdata("Create 🆕").close();
            li.close();
        }
        for (cv, pri) in views.zip(cards) {
            if cv.name != pri.name() {
                return Err(Error::CardMismatch());
            }
            let mut li = ul.li();
            li.id(cv.id())
                .data_("name", &cv.name)
                .class(cv.view.class_name());
            li.raw(pri.to_html(cv.view, &anc));
            li.close();
        }
        ul.close();
        Ok(String::from(page))
    }

    /// Get next suggested name
    fn next_name<C: Card>(obs: &[C]) -> String {
        let prefix = C::PREFIX;
        if prefix.is_empty() {
            return String::new();
        }
        let mut num = 1;
        for ob in obs {
            if let Some((pre, suffix)) = ob.name().split_once('_')
                && pre == prefix
                && let Ok(n) = suffix.parse::<u32>()
            {
                num = num.max(n + 1);
            }
        }
        format!("{prefix}_{num}")
    }

    /// Search for all card views
    pub async fn search_views(
        &mut self,
        search: &str,
    ) -> Result<Vec<CardView>> {
        cards_meth!(self, search_views_x, search)
    }

    /// Search for all card views
    async fn search_views_x<C: Card>(
        &mut self,
        search: &str,
    ) -> Result<Vec<CardView>> {
        // Use default value for ancillary data lookup
        let anc = fetch_ancillary(&C::default(), View::Search).await?;
        self.search_card_views::<C>(search, &anc).await?;
        Ok(self.views.clone())
    }

    /// Get HTML of changed cards
    pub async fn changed_html(
        &mut self,
        search: &str,
    ) -> Result<Vec<(CardView, String)>> {
        cards_meth!(self, changed_html_x, search)
    }

    /// Get HTML of changed cards
    async fn changed_html_x<C: Card>(
        &mut self,
        search: &str,
    ) -> Result<Vec<(CardView, String)>> {
        // Use default value for ancillary data lookup
        let anc = fetch_ancillary(&C::default(), View::Search).await?;
        self.search_card_views::<C>(search, &anc).await?;
        let cards0 = self.cards::<C>(true)?.into_iter();
        let cards1 = self.cards::<C>(false)?;
        let mut values = Vec::new();
        let mut views = self.views.iter();
        // skip "Create" card view
        let _cv = views.next();
        for (c0, c1) in cards0.zip(&cards1) {
            if c0.name() != c1.name() {
                return Err(Error::CardMismatch());
            }
            let Some(cv) = views.next() else {
                return Err(Error::CardMismatch());
            };
            if c0 != *c1 {
                let cv = cv.clone();
                let html = c1.to_html(cv.view, &anc);
                values.push((cv, html));
            }
        }
        Ok(values)
    }
}

/// Fetch ancillary data
pub async fn fetch_ancillary<C: Card>(
    pri: &C,
    view: View,
) -> Result<C::Ancillary> {
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
