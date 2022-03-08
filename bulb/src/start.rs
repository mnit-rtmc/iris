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
use crate::card::{Card, CardType};
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
use serde::{Deserialize, Serialize};
use std::cell::RefCell;
use wasm_bindgen::prelude::*;
use wasm_bindgen::JsCast;
use wasm_bindgen_futures::spawn_local;
use web_sys::{
    console, Document, Element, Event, HtmlButtonElement, HtmlElement,
    HtmlInputElement, HtmlSelectElement, ScrollBehavior, ScrollIntoViewOptions,
    ScrollLogicalPosition, TransitionEvent, Window,
};

/// JavaScript result
pub type JsResult<T> = std::result::Result<T, JsValue>;

/// ID of toast element
const TOAST_ID: &str = "sb_toast";

/// ID of login shade
const LOGIN_ID: &str = "sb_login";

/// Interval (ms) between ticks for deferred actions
const TICK_INTERVAL: i32 = 500;

/// Comm protocol
#[derive(Debug, Deserialize, Serialize)]
pub struct Protocol {
    pub id: u32,
    pub description: String,
}

/// Controller conditions
#[derive(Debug, Deserialize, Serialize)]
pub struct Condition {
    pub id: u32,
    pub description: String,
}

/// Deferred actions (called on set_interval)
#[derive(Clone, Copy, Debug, PartialEq)]
enum DeferredAction {
    SearchList,
    HideToast,
}

/// Global app state
#[derive(Default)]
struct State {
    /// Permission access
    access: Vec<Permission>,
    /// Comm protocols
    protocols: Vec<Protocol>,
    /// Controller conditions
    conditions: Vec<Condition>,
    /// Resource types
    resource_types: Vec<String>,
    /// Comm configs
    comm_configs: Vec<CommConfig>,
    /// Deferred actions (with tick number)
    deferred: Vec<(i32, DeferredAction)>,
    /// Timer tick count
    tick: i32,
    /// Selected card state
    selected_card: Option<CardState>,
    /// Delete action enabled (slider transition finished)
    delete_enabled: bool,
}

thread_local! {
    static STATE: RefCell<State> = RefCell::new(State::default());
}

impl State {
    /// Initialize global app state
    fn initialize(
        &mut self,
        mut access: Vec<Permission>,
        mut protocols: Vec<Protocol>,
        mut conditions: Vec<Condition>,
        mut resource_types: Vec<String>,
        mut comm_configs: Vec<CommConfig>,
    ) {
        self.access.append(&mut access);
        self.protocols.append(&mut protocols);
        self.conditions.append(&mut conditions);
        self.resource_types.append(&mut resource_types);
        self.comm_configs.append(&mut comm_configs);
    }

    /// Does state need initializing?
    fn needs_initializing(&self) -> bool {
        self.resource_types.is_empty()
    }

    /// Add ticks to current tick count
    fn plus_ticks(&self, t: i32) -> i32 {
        if let Some(t) = self.tick.checked_add(t) {
            t
        } else {
            0
        }
    }

    /// Schedule a deferred action
    fn schedule(&mut self, action: DeferredAction, timeout_ms: i32) {
        let tick =
            self.plus_ticks((timeout_ms + TICK_INTERVAL - 1) / TICK_INTERVAL);
        self.deferred.push((tick, action));
    }

    /// Get a deferred action
    fn action(&mut self) -> Option<DeferredAction> {
        for i in 0..self.deferred.len() {
            let (tick, action) = self.deferred[i];
            if tick <= self.tick {
                self.deferred.swap_remove(i);
                return Some(action);
            }
        }
        None
    }

    /// Clear deferred search actions
    fn clear_searches(&mut self) {
        self.deferred.retain(|(_, a)| *a != DeferredAction::SearchList)
    }
}

impl DeferredAction {
    /// Schedule with timeout
    fn schedule(self, timeout_ms: i32) {
        STATE.with(|rc| {
            let mut state = rc.borrow_mut();
            state.schedule(self, timeout_ms);
        });
    }

    /// Perform the action
    fn perform(self) {
        match self {
            Self::SearchList => search_list(),
            Self::HideToast => hide_toast(),
        }
    }
}

/// Populate `sb_list` with `tp` card types
async fn populate_list(tp: String, search: String) {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    let sb_list = doc.elem::<Element>("sb_list").unwrap_throw();
    match create_cards(tp, &search).await {
        Ok(cards) => sb_list.set_inner_html(&cards),
        Err(Error::FetchResponseUnauthorized()) => show_login(),
        Err(e) => show_toast(&format!("View failed: {}", e)),
    }
}

/// Create cards for `sb_list`
async fn create_cards(tp: String, search: &str) -> Result<String> {
    match tp.as_str() {
        Alarm::TNAME => try_build_cards::<Alarm>(search).await,
        CabinetStyle::TNAME => try_build_cards::<CabinetStyle>(search).await,
        CommConfig::TNAME => try_build_cards::<CommConfig>(search).await,
        CommLink::TNAME => try_build_cards::<CommLink>(search).await,
        Controller::TNAME => try_build_cards::<Controller>(search).await,
        Modem::TNAME => try_build_cards::<Modem>(search).await,
        Permission::TNAME => try_build_cards::<Permission>(search).await,
        Role::TNAME => try_build_cards::<Role>(search).await,
        User::TNAME => try_build_cards::<User>(search).await,
        _ => Ok("".into()),
    }
}

/// Try to build cards
async fn try_build_cards<C: Card>(search: &str) -> Result<String> {
    let json = fetch_get(&format!("/iris/api/{}", C::UNAME)).await?;
    let search = search.to_lowercase();
    let html = C::build_cards(&json, &search)?;
    Ok(html)
}

/// Handle a card click event
async fn click_card(tp: String, id: String, name: String) {
    match tp.as_str() {
        Alarm::TNAME => expand_card::<Alarm>(id, name).await,
        CabinetStyle::TNAME => expand_card::<CabinetStyle>(id, name).await,
        CommConfig::TNAME => expand_card::<CommConfig>(id, name).await,
        CommLink::TNAME => expand_card::<CommLink>(id, name).await,
        Controller::TNAME => expand_card::<Controller>(id, name).await,
        Modem::TNAME => expand_card::<Modem>(id, name).await,
        Permission::TNAME => expand_card::<Permission>(id, name).await,
        Role::TNAME => expand_card::<Role>(id, name).await,
        User::TNAME => expand_card::<User>(id, name).await,
        _ => (),
    }
}

/// Expand a card to a full form
async fn expand_card<C: Card>(id: String, name: String) {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    if id.ends_with('_') {
        let cs = CardState::new::<C>(name);
        cs.replace_card(&doc, CardType::Create);
    } else {
        match fetch_card::<C>(name).await {
            Ok(cs) => cs.replace_card(&doc, CardType::Status),
            Err(Error::FetchResponseUnauthorized()) => show_login(),
            Err(e) => {
                show_toast(&format!("Fetch failed: {}", e));
                // Card list may be out-of-date; refresh with search
                DeferredAction::SearchList.schedule(200);
            }
        }
    }
}

/// Fetch a card with a GET request
async fn fetch_card<C: Card>(name: String) -> Result<CardState> {
    let uri = name_uri::<C>(&name);
    let json = fetch_get(&uri).await?;
    Ok(CardState {
        tname: C::TNAME,
        uri,
        name,
        json: Some(json),
    })
}

/// Selected card state
#[derive(Clone)]
pub struct CardState {
    /// Type name
    tname: &'static str,
    /// Object URI
    uri: String,
    /// Object name
    name: String,
    /// JSON value of object
    json: Option<JsValue>,
}

impl CardState {
    /// Create a new blank card state
    fn new<C: Card>(name: String) -> Self {
        CardState {
            tname: C::TNAME,
            uri: "".into(),
            name,
            json: None,
        }
    }

    /// Get card element ID
    fn id(&self) -> String {
        if self.json.is_some() {
            format!("{}_{}", self.tname, &self.name)
        } else {
            format!("{}_", self.tname)
        }
    }

    /// Replace a card element with another card type
    fn replace_card(self, doc: &Document, ct: CardType) {
        let id = self.id();
        match doc.elem::<HtmlElement>(&id) {
            Ok(elem) => {
                match build_card(self.tname, &self.name, &self.json, ct) {
                    Ok(html) => replace_card_html(&elem, ct, &html),
                    Err(Error::FetchResponseUnauthorized()) => {
                        show_login();
                        return;
                    }
                    Err(e) => {
                        show_toast(&format!("Build failed: {}", e));
                        return;
                    }
                }
            }
            Err(e) => console::log_1(&format!("{:?} {}", e, id).into()),
        }
        STATE.with(|rc| {
            let mut state = rc.borrow_mut();
            if ct != CardType::Compact {
                state.selected_card.replace(self);
                state.clear_searches();
            } else {
                state.selected_card.take();
            }
        });
    }

    /// Delete selected card / object
    async fn delete(self) {
        try_delete(&self.uri).await;
    }

    /// Save changed fields on Edit form
    async fn save_changed(mut self) {
        let window = web_sys::window().unwrap_throw();
        let doc = window.document().unwrap_throw();
        match &self.json {
            Some(json) => {
                let res = match try_changed_fields(self.tname, &doc, json) {
                    Ok(v) => fetch_patch(&self.uri, &v.into()).await,
                    Err(e) => Err(e),
                };
                match res {
                    Ok(_) => {
                        self.fetch_again().await;
                        self.replace_card(&doc, CardType::Compact);
                    }
                    Err(Error::FetchResponseUnauthorized()) => show_login(),
                    Err(e) => show_toast(&format!("Save failed: {}", e)),
                }
            }
            None => {
                if try_create_new(self.tname, &doc).await {
                    self.replace_card(&doc, CardType::Compact);
                }
            }
        }
    }

    /// Fetch a card again with a GET request
    async fn fetch_again(&mut self) {
        match fetch_get(&self.uri).await {
            Ok(json) => self.json = Some(json),
            // Card list out-of-date; refresh with search
            Err(_) => DeferredAction::SearchList.schedule(200),
        }
    }
}

/// Show login form shade
fn show_login() {
    get_element(LOGIN_ID).filter(|e| {
        e.set_class_name("show");
        false
    });
}

/// Hide login form shade
fn hide_login() {
    get_element(LOGIN_ID).filter(|e| {
        e.set_class_name("");
        false
    });
}

/// Show a toast message
fn show_toast(msg: &str) {
    console::log_1(&format!("toast: {msg}").into());
    get_element(TOAST_ID).filter(|t| {
        t.set_inner_html(msg);
        t.set_class_name("show");
        DeferredAction::HideToast.schedule(3000);
        false
    });
}

/// Get element by ID
fn get_element(id: &str) -> Option<HtmlElement> {
    if let Some(window) = web_sys::window() {
        if let Some(doc) = window.document() {
            return doc.elem(id).ok();
        }
    }
    None
}

/// Hide toast
fn hide_toast() {
    get_element(TOAST_ID).filter(|t| {
        t.set_class_name("");
        false
    });
}

/// Get the URI of an object
fn name_uri<C: Card>(name: &str) -> String {
    format!(
        "/iris/api/{}/{}",
        C::UNAME,
        utf8_percent_encode(name, NON_ALPHANUMERIC)
    )
}

/// Replace a card with provieded HTML
fn replace_card_html(elem: &HtmlElement, ct: CardType, html: &str) {
    elem.set_inner_html(html);
    if let CardType::Compact = ct {
        elem.set_class_name("card");
    } else {
        elem.set_class_name("form");
        let mut opt = ScrollIntoViewOptions::new();
        opt.behavior(ScrollBehavior::Smooth)
            .block(ScrollLogicalPosition::Nearest);
        elem.scroll_into_view_with_scroll_into_view_options(&opt);
    }
}

/// Try to delete an object
async fn try_delete(uri: &str) {
    match fetch_delete(uri).await {
        Ok(_) => {
            if let Some(window) = web_sys::window() {
                let doc = window.document().unwrap_throw();
                deselect_card(&doc);
            }
            DeferredAction::SearchList.schedule(1500);
        }
        Err(Error::FetchResponseUnauthorized()) => show_login(),
        Err(e) => show_toast(&format!("Delete failed: {}", e)),
    }
}

/// Try to create a new object
async fn try_create_new(tp: &str, doc: &Document) -> bool {
    match create_new(tp, doc).await {
        Ok(_) => {
            DeferredAction::SearchList.schedule(1500);
            true
        }
        Err(Error::FetchResponseUnauthorized()) => {
            show_login();
            false
        }
        Err(e) => {
            show_toast(&format!("Create failed: {}", e));
            false
        }
    }
}

/// Create a new object
async fn create_new(tp: &str, doc: &Document) -> Result<()> {
    match tp {
        Alarm::TNAME => do_create::<Alarm>(doc).await,
        CabinetStyle::TNAME => do_create::<CabinetStyle>(doc).await,
        CommConfig::TNAME => do_create::<CommConfig>(doc).await,
        CommLink::TNAME => do_create::<CommLink>(doc).await,
        Controller::TNAME => do_create::<Controller>(doc).await,
        Modem::TNAME => do_create::<Modem>(doc).await,
        Permission::TNAME => do_create::<Permission>(doc).await,
        Role::TNAME => do_create::<Role>(doc).await,
        User::TNAME => do_create::<User>(doc).await,
        _ => unreachable!(),
    }
}

/// Create a new object
async fn do_create<C: Card>(doc: &Document) -> Result<()> {
    let value = C::create_value(doc)?;
    let json = value.into();
    fetch_post(&format!("/iris/api/{}", C::UNAME), &json).await?;
    Ok(())
}

/// Try to retrieve changed fields on edit form
fn try_changed_fields(
    tp: &str,
    doc: &Document,
    json: &JsValue,
) -> Result<String> {
    match tp {
        Alarm::TNAME => Alarm::changed_fields(doc, json),
        CabinetStyle::TNAME => CabinetStyle::changed_fields(doc, json),
        CommConfig::TNAME => CommConfig::changed_fields(doc, json),
        CommLink::TNAME => CommLink::changed_fields(doc, json),
        Controller::TNAME => Controller::changed_fields(doc, json),
        Modem::TNAME => Modem::changed_fields(doc, json),
        Permission::TNAME => Permission::changed_fields(doc, json),
        Role::TNAME => Role::changed_fields(doc, json),
        User::TNAME => User::changed_fields(doc, json),
        _ => unreachable!(),
    }
}

/// Build card using JSON value
fn build_card(
    tp: &str,
    name: &str,
    json: &Option<JsValue>,
    ct: CardType,
) -> Result<String> {
    match tp {
        Alarm::TNAME => Alarm::build_card(name, json, ct),
        CabinetStyle::TNAME => CabinetStyle::build_card(name, json, ct),
        CommConfig::TNAME => CommConfig::build_card(name, json, ct),
        CommLink::TNAME => CommLink::build_card(name, json, ct),
        Controller::TNAME => Controller::build_card(name, json, ct),
        Modem::TNAME => Modem::build_card(name, json, ct),
        Permission::TNAME => Permission::build_card(name, json, ct),
        Role::TNAME => Role::build_card(name, json, ct),
        User::TNAME => User::build_card(name, json, ct),
        _ => Ok("".into()),
    }
}

/// Set global allocator to `wee_alloc`
#[global_allocator]
static ALLOC: wee_alloc::WeeAlloc = wee_alloc::WeeAlloc::INIT;

/// Page sidebar
const SIDEBAR: &str = include_str!("sidebar.html");

/// Application starting function
#[wasm_bindgen(start)]
pub async fn start() -> core::result::Result<(), JsError> {
    // this should be debug only
    console_error_panic_hook::set_once();
    add_sidebar().unwrap_throw();
    initialize_state().await;
    Ok(())
}

/// Add sidebar HTML and event listeners
fn add_sidebar() -> JsResult<()> {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    let sidebar: HtmlElement = doc.elem("sidebar")?;
    sidebar.set_inner_html(SIDEBAR);
    add_click_event_listener(&sidebar)?;
    let sb_resource: HtmlSelectElement = doc.elem("sb_resource")?;
    add_select_event_listener(&sb_resource, handle_sb_resource_ev)?;
    add_input_event_listener(&doc.elem("sb_search")?)?;
    add_transition_event_listener(&doc.elem("sb_list")?)?;
    add_interval_callback(&window).unwrap_throw();
    Ok(())
}

/// Initialize app state
async fn initialize_state() {
    match do_initialize().await {
        Ok(()) => {
            let window = web_sys::window().unwrap_throw();
            let doc = window.document().unwrap_throw();
            if let Ok(r) = doc.elem::<HtmlElement>("sb_resource") {
                r.set_inner_html(&types_html());
            }
        }
        Err(_e) => console::log_1(&"State not initialized".into()),
    }
}

/// Initialize app state
async fn do_initialize() -> core::result::Result<(), JsError> {
    let json = fetch_get("/iris/api/access").await?;
    let access = json.into_serde::<Vec<Permission>>()?;
    let json = fetch_get("/iris/comm_protocol").await?;
    let protocols = json.into_serde::<Vec<Protocol>>()?;
    let json = fetch_get("/iris/condition").await?;
    let conditions = json.into_serde::<Vec<Condition>>()?;
    let json = fetch_get("/iris/resource_type").await?;
    let resource_types = json.into_serde::<Vec<String>>()?;
    let json = fetch_get(&format!("/iris/api/{}", CommConfig::UNAME)).await?;
    let comm_configs = json.into_serde::<Vec<CommConfig>>()?;
    STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        state.initialize(
            access,
            protocols,
            conditions,
            resource_types,
            comm_configs,
        );
    });
    Ok(())
}

/// Create types `select` element
fn types_html() -> String {
    let mut html = "<option/>".to_string();
    STATE.with(|rc| {
        let state = rc.borrow();
        for permission in &state.access {
            if permission.batch.is_none() {
                add_option::<Alarm>(permission, &mut html);
                add_option::<CabinetStyle>(permission, &mut html);
                add_option::<CommConfig>(permission, &mut html);
                add_option::<CommLink>(permission, &mut html);
                add_option::<Controller>(permission, &mut html);
                add_option::<Modem>(permission, &mut html);
                add_option::<Permission>(permission, &mut html);
                add_option::<Role>(permission, &mut html);
                add_option::<User>(permission, &mut html);
            }
        }
    });
    html
}

/// Add option to access select
fn add_option<C: Card>(perm: &Permission, html: &mut String) {
    if perm.resource_n == C::UNAME.to_lowercase() {
        html.push_str("<option value='");
        html.push_str(C::TNAME);
        html.push_str("'>");
        html.push_str(C::ENAME);
        html.push_str("</option>");
    }
}

/// Create an HTML `select` element of comm protocols
pub fn protocols_html(selected: Option<u32>) -> String {
    STATE.with(|rc| {
        let state = rc.borrow();
        let mut html = String::new();
        html.push_str("<select id='edit_protocol'>");
        for protocol in &state.protocols {
            html.push_str("<option value='");
            html.push_str(&protocol.id.to_string());
            html.push('\'');
            if let Some(p) = selected {
                if p == protocol.id {
                    html.push_str(" selected");
                }
            }
            html.push('>');
            html.push_str(&protocol.description);
            html.push_str("</option>");
        }
        html.push_str("</select>");
        html
    })
}

/// Get a condition by ID
pub fn get_condition(id: u32) -> Option<String> {
    STATE.with(|rc| {
        let state = rc.borrow();
        for condition in &state.conditions {
            if id == condition.id {
                return Some(condition.description.clone());
            }
        }
        None
    })
}

/// Create an HTML `select` element of controller conditions
pub fn conditions_html(selected: u32) -> String {
    STATE.with(|rc| {
        let state = rc.borrow();
        let mut html = String::new();
        html.push_str("<select id='edit_condition'>");
        for condition in &state.conditions {
            html.push_str("<option value='");
            html.push_str(&condition.id.to_string());
            html.push('\'');
            if selected == condition.id {
                html.push_str(" selected");
            }
            html.push('>');
            html.push_str(&condition.description);
            html.push_str("</option>");
        }
        html.push_str("</select>");
        html
    })
}

/// Create an HTML `select` element of resource types
pub fn resource_types_html(selected: &str) -> String {
    STATE.with(|rc| {
        let state = rc.borrow();
        let mut html = String::new();
        html.push_str("<select id='edit_resource'>");
        for resource_type in &state.resource_types {
            html.push_str("<option");
            if selected == resource_type {
                html.push_str(" selected");
            }
            html.push('>');
            html.push_str(resource_type);
            html.push_str("</option>");
        }
        html.push_str("</select>");
        html
    })
}

/// Get a comm config by name
pub fn get_comm_config_desc(name: &str) -> Option<String> {
    STATE.with(|rc| {
        let state = rc.borrow();
        for config in &state.comm_configs {
            if name == config.name {
                return Some(config.description.clone());
            }
        }
        None
    })
}

/// Create an HTML `select` element of comm configs
pub fn comm_configs_html(selected: &str) -> String {
    STATE.with(|rc| {
        let state = rc.borrow();
        let mut html = String::new();
        html.push_str("<select id='edit_config'>");
        for config in &state.comm_configs {
            html.push_str("<option value='");
            html.push_str(&config.name);
            html.push('\'');
            if selected == config.name {
                html.push_str(" selected");
            }
            html.push('>');
            html.push_str(&config.description);
            html.push_str("</option>");
        }
        html.push_str("</select>");
        html
    })
}

/// Handle an event from "sb_resource" `select` element
fn handle_sb_resource_ev(tp: String) {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        state.selected_card.take()
    });
    if let Ok(input) = doc.elem::<HtmlInputElement>("sb_search") {
        input.set_value("");
    }
    spawn_local(populate_list(tp, "".into()));
}

/// Search list using the value from "sb_search"
fn search_list() {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    if let Ok(input) = doc.elem::<HtmlInputElement>("sb_search") {
        let search = input.value();
        deselect_card(&doc);
        if let Some(tp) = doc.select_parse::<String>("sb_resource") {
            spawn_local(populate_list(tp, search));
        }
    }
}

/// Add an "input" event listener to a `select` element
fn add_select_event_listener(
    elem: &HtmlSelectElement,
    handle_ev: fn(String),
) -> JsResult<()> {
    let closure = Closure::wrap(Box::new(move |e: Event| {
        let value = e
            .current_target()
            .unwrap()
            .dyn_into::<HtmlSelectElement>()
            .unwrap()
            .value();
        handle_ev(value);
    }) as Box<dyn FnMut(_)>);
    elem.add_event_listener_with_callback(
        "input",
        closure.as_ref().unchecked_ref(),
    )?;
    // can't drop closure, just forget it to make JS happy
    closure.forget();
    Ok(())
}

/// Add an "input" event listener to an element
fn add_input_event_listener(elem: &HtmlInputElement) -> JsResult<()> {
    let closure = Closure::wrap(Box::new(|_e: Event| {
        search_list();
    }) as Box<dyn Fn(_)>);
    elem.add_event_listener_with_callback(
        "input",
        closure.as_ref().unchecked_ref(),
    )?;
    // can't drop closure, just forget it to make JS happy
    closure.forget();
    Ok(())
}

/// Add a `click` event listener to an element
fn add_click_event_listener(elem: &Element) -> JsResult<()> {
    let closure = Closure::wrap(Box::new(|e: Event| {
        let value = e.target().unwrap().dyn_into::<Element>().unwrap();
        handle_click_ev(&value);
    }) as Box<dyn FnMut(_)>);
    elem.add_event_listener_with_callback(
        "click",
        closure.as_ref().unchecked_ref(),
    )?;
    // can't drop closure, just forget it to make JS happy
    closure.forget();
    Ok(())
}

/// Handle a `click` event from a target element
fn handle_click_ev(target: &Element) {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    if target.is_instance_of::<HtmlButtonElement>() {
        handle_button_click_ev(&doc, target);
    } else if let Some(card) = target.closest(".card").unwrap_throw() {
        if let Some(id) = card.get_attribute("id") {
            if let Some(name) = card.get_attribute("name") {
                if let Some(tp) = doc.select_parse::<String>("sb_resource") {
                    deselect_card(&doc);
                    spawn_local(click_card(tp, id, name));
                }
            }
        }
    }
}

/// Handle a `click` event with a button target
fn handle_button_click_ev(doc: &Document, target: &Element) {
    let id = target.id();
    if id == "ob_login" {
        spawn_local(handle_login());
        return;
    }
    let cs = STATE.with(|rc| rc.borrow().selected_card.clone());
    if let Some(cs) = cs {
        match id.as_str() {
            "ob_close" => cs.replace_card(doc, CardType::Compact),
            "ob_delete" => {
                if STATE.with(|rc| rc.borrow().delete_enabled) {
                    spawn_local(cs.delete());
                }
            }
            "ob_edit" => cs.replace_card(doc, CardType::Edit),
            "ob_save" => spawn_local(cs.save_changed()),
            _ => {
                if target.class_name() == "go_link" {
                    go_resource(doc, target);
                } else {
                    console::log_1(&format!("unknown button: {}", id).into());
                }
            }
        }
    }
}

/// Handle login button press
async fn handle_login() {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    if let (Some(user), Some(pass)) = (
        doc.input_parse::<String>("login_user"),
        doc.input_parse::<String>("login_pass"),
    ) {
        let js = format!("{{\"username\":\"{user}\",\"password\":\"{pass}\"}}");
        let js = js.into();
        match fetch_post("/iris/api/login", &js).await {
            Ok(_) => {
                if let Ok(pass) = doc.elem::<HtmlInputElement>("login_pass") {
                    pass.set_value("");
                }
                hide_login();
                if STATE.with(|rc| rc.borrow().needs_initializing()) {
                    initialize_state().await;
                }
            }
            Err(e) => show_toast(&format!("Login failed: {}", e)),
        }
    }
}

/// Go to resource from target's `data-link` attribute
fn go_resource(doc: &Document, target: &Element) {
    if let (Some(link), Some(tp)) = (
        target.get_attribute("data-link"),
        target.get_attribute("data-type"),
    ) {
        if let Ok(sb_resource) = doc.elem::<HtmlSelectElement>("sb_resource") {
            sb_resource.set_value(&tp);
            if let Ok(input) = doc.elem::<HtmlInputElement>("sb_search") {
                input.set_value(&link);
                spawn_local(populate_list(tp, link));
            }
        }
    }
}

/// Add transition event listener to an element
fn add_transition_event_listener(elem: &Element) -> JsResult<()> {
    let closure =
        Closure::wrap(Box::new(handle_transition_ev) as Box<dyn FnMut(_)>);
    elem.add_event_listener_with_callback(
        "transitionstart",
        closure.as_ref().unchecked_ref(),
    )?;
    elem.add_event_listener_with_callback(
        "transitioncancel",
        closure.as_ref().unchecked_ref(),
    )?;
    elem.add_event_listener_with_callback(
        "transitionend",
        closure.as_ref().unchecked_ref(),
    )?;
    closure.forget();
    Ok(())
}

/// Handle a `transition*` event from "sb_list" child element
fn handle_transition_ev(ev: Event) {
    if let Some(target) = ev.target() {
        if let Ok(target) = target.dyn_into::<Element>() {
            if let Ok(ev) = ev.dyn_into::<TransitionEvent>() {
                // delete slider is a "left" property transition
                if target.id() == "ob_delete" && ev.property_name() == "left" {
                    set_delete_enabled(&ev.type_() == "transitionend");
                }
            }
        }
    }
}

/// Set delete action enabled/disabled
fn set_delete_enabled(enabled: bool) {
    STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        state.delete_enabled = enabled;
    });
}

/// Deselect the selected card
fn deselect_card(doc: &Document) {
    let cs = STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        state.selected_card.take()
    });
    if let Some(cs) = cs {
        cs.replace_card(doc, CardType::Compact);
    }
}

/// Add callback for regular interval checks
fn add_interval_callback(window: &Window) -> JsResult<()> {
    let closure = Closure::wrap(Box::new(|| {
        tick_interval();
    }) as Box<dyn Fn()>);
    window.set_interval_with_callback_and_timeout_and_arguments_0(
        closure.as_ref().unchecked_ref(),
        TICK_INTERVAL,
    )?;
    closure.forget();
    Ok(())
}

/// Process a tick interval
fn tick_interval() {
    STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        // don't need to count ticks if nothing's deferred
        if state.deferred.is_empty() {
            return;
        }
        state.tick = state.plus_ticks(1);
    });
    while let Some(action) = STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        state.action()
    }) {
        action.perform();
    }
}
