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
use percent_encoding::{utf8_percent_encode, NON_ALPHANUMERIC};
use serde::{Deserialize, Serialize};
use std::cell::RefCell;
use wasm_bindgen::prelude::*;
use wasm_bindgen::JsCast;
use wasm_bindgen_futures::{spawn_local, JsFuture};
use web_sys::{
    console, Document, Element, Event, HtmlButtonElement, HtmlElement,
    HtmlInputElement, HtmlSelectElement, Request, RequestInit, Response,
    ScrollBehavior, ScrollIntoViewOptions, ScrollLogicalPosition,
    TransitionEvent, Window,
};

pub type Result<T> = std::result::Result<T, JsValue>;

mod alarm;
mod cabinetstyle;
mod card;
mod commconfig;
mod commlink;
mod controller;
mod modem;
mod util;

use alarm::Alarm;
use cabinetstyle::CabinetStyle;
use card::{Card, CardType};
use commconfig::CommConfig;
use commlink::CommLink;
use controller::Controller;
use modem::Modem;
use util::Dom;

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
#[derive(Clone, Copy, Debug)]
enum DeferredAction {
    SearchList,
    HideToast,
}

/// Global app state
#[derive(Default)]
struct State {
    /// Comm protocols
    protocols: Vec<Protocol>,
    /// Controller conditions
    conditions: Vec<Condition>,
    /// Deferred actions (with tick number)
    deferred: Vec<(i32, DeferredAction)>,
    /// Timer tick count
    tick: i32,
    /// Selected card state
    selected: Option<CardState>,
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
        mut protocols: Vec<Protocol>,
        mut conditions: Vec<Condition>,
    ) {
        self.protocols.append(&mut protocols);
        self.conditions.append(&mut conditions);
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

/// Fetch a GET request
async fn fetch_get(uri: &str) -> Result<JsValue> {
    let req = Request::new_with_str(uri)?;
    req.headers().set("Accept", "application/json")?;
    let window = web_sys::window().unwrap_throw();
    let resp = JsFuture::from(window.fetch_with_request(&req)).await?;
    let resp: Response = resp.dyn_into().unwrap_throw();
    match resp.status() {
        200 => Ok(JsFuture::from(resp.json()?).await?),
        _ => Err(resp.status_text().into()),
    }
}

/// Fetch a PATCH request
async fn fetch_patch(window: &Window, uri: &str, json: &JsValue) -> Result<()> {
    let req = Request::new_with_str_and_init(
        uri,
        RequestInit::new().method("PATCH").body(Some(json)),
    )?;
    let resp = JsFuture::from(window.fetch_with_request(&req)).await?;
    let resp: Response = resp.dyn_into().unwrap_throw();
    match resp.status() {
        200 | 204 => Ok(()),
        _ => Err(resp.status_text().into()),
    }
}

/// Fetch a POST request
async fn fetch_post(window: &Window, uri: &str, json: &JsValue) -> Result<()> {
    let req = Request::new_with_str_and_init(
        uri,
        RequestInit::new().method("POST").body(Some(json)),
    )?;
    let resp = JsFuture::from(window.fetch_with_request(&req)).await?;
    let resp: Response = resp.dyn_into().unwrap_throw();
    match resp.status() {
        200 | 201 => Ok(()),
        _ => Err(resp.status_text().into()),
    }
}

/// Fetch a DELETE request
async fn fetch_delete(window: &Window, uri: &str) -> Result<()> {
    let req = Request::new_with_str_and_init(
        uri,
        RequestInit::new().method("DELETE"),
    )?;
    let resp = JsFuture::from(window.fetch_with_request(&req)).await?;
    let resp: Response = resp.dyn_into().unwrap_throw();
    match resp.status() {
        200 | 204 => Ok(()),
        _ => Err(resp.status_text().into()),
    }
}

/// Populate `sb_list` with `tp` card types
async fn populate_list(tp: String, tx: String) {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    let sb_list = doc.elem::<Element>("sb_list").unwrap_throw();
    match create_cards(tp, tx).await {
        Ok(cards) => sb_list.set_inner_html(&cards),
        Err(e) => {
            // ⛔ 🔒 unauthorized (401) should be handled here
            console::log_1(&e);
        }
    }
}

/// Create cards for `sb_list`
async fn create_cards(tp: String, tx: String) -> Result<String> {
    match tp.as_str() {
        Alarm::TNAME => try_build_cards::<Alarm>(tx).await,
        CabinetStyle::TNAME => try_build_cards::<CabinetStyle>(tx).await,
        CommConfig::TNAME => try_build_cards::<CommConfig>(tx).await,
        CommLink::TNAME => try_build_cards::<CommLink>(tx).await,
        Controller::TNAME => try_build_cards::<Controller>(tx).await,
        Modem::TNAME => try_build_cards::<Modem>(tx).await,
        _ => Ok("".into()),
    }
}

/// Try to build cards
async fn try_build_cards<C: Card>(tx: String) -> Result<String> {
    let json = fetch_get(C::URI).await?;
    let tx = tx.to_lowercase();
    let html = C::build_cards(&json, &tx)?;
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
            Err(e) => {
                // Card list out-of-date; refresh with search
                console::log_1(&e);
                DeferredAction::SearchList.schedule(200);
            }
        }
    }
}

/// Fetch a card with a GET request
async fn fetch_card<C: Card>(name: String) -> Result<CardState> {
    let uri = name_uri::<C>(&name);
    let json = fetch_get(&uri).await?;
    console::log_1(&json);
    Ok(CardState {
        tname: C::TNAME,
        uri,
        name,
        json: Some(json),
    })
}

/// Selected card state
#[derive(Clone)]
struct CardState {
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
                    Err(e) => {
                        console::log_1(&(&e).into());
                        return;
                    }
                }
            }
            Err(e) => console::log_1(&format!("{:?} {}", e, id).into()),
        }
        STATE.with(|rc| {
            let mut state = rc.borrow_mut();
            if ct != CardType::Compact {
                state.selected.replace(self);
            } else {
                state.selected.take();
            }
        });
    }

    /// Delete selected card / object
    async fn delete(self) {
        if let Some(window) = web_sys::window() {
            let doc = window.document().unwrap_throw();
            deselect_card(&doc);
            try_delete(&window, &self.uri).await;
        }
    }

    /// Save changed fields on Edit form
    async fn save_changed(mut self) {
        let window = web_sys::window().unwrap_throw();
        let doc = window.document().unwrap_throw();
        match &self.json {
            Some(json) => {
                match try_changed_fields(self.tname, &doc, json) {
                    Ok(v) => save_changed_fields(&self.uri, &v).await,
                    Err(e) => {
                        // this should only happen if the JSON is not valid
                        console::log_1(&e);
                    }
                }
                self.fetch_again().await;
            }
            None => try_create_new(self.tname, &doc).await,
        }
        self.replace_card(&doc, CardType::Compact);
    }

    /// Fetch a card again with a GET request
    async fn fetch_again(&mut self) {
        match fetch_get(&self.uri).await {
            Ok(json) => {
                console::log_1(&json);
                self.json = Some(json);
            }
            Err(e) => {
                // Card list out-of-date; refresh with search
                console::log_1(&e);
                DeferredAction::SearchList.schedule(200);
            }
        }
    }
}

/// Save changed fields
async fn save_changed_fields(uri: &str, v: &str) {
    if let Some(window) = web_sys::window() {
        let json = v.into();
        if let Err(e) = fetch_patch(&window, uri, &json).await {
            console::log_1(&e);
            show_toast("Save failed!");
        }
    }
}

/// Show a toast message
fn show_toast(msg: &str) {
    get_toast().filter(|t| {
        t.set_inner_html(msg);
        t.set_class_name("show");
        DeferredAction::HideToast.schedule(3000);
        false
    });
}

/// Get toast element
fn get_toast() -> Option<HtmlElement> {
    if let Some(window) = web_sys::window() {
        if let Some(doc) = window.document() {
            return doc.elem("sb_toast").ok();
        }
    }
    None
}

/// Hide toast
fn hide_toast() {
    get_toast().filter(|t| {
        t.set_class_name("");
        false
    });
}

/// Get the URI of an object
fn name_uri<C: Card>(name: &str) -> String {
    format!("{}/{}", C::URI, utf8_percent_encode(name, NON_ALPHANUMERIC))
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
async fn try_delete(window: &Window, uri: &str) {
    match fetch_delete(window, uri).await {
        Ok(_) => DeferredAction::SearchList.schedule(1500),
        Err(e) => {
            console::log_1(&e);
            show_toast("Delete failed!");
        }
    }
}

/// Try to create a new object
async fn try_create_new(tp: &str, doc: &Document) {
    match create_new(tp, doc).await {
        Ok(_) => DeferredAction::SearchList.schedule(1500),
        Err(e) => {
            console::log_1(&e);
            show_toast("Create failed!");
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
        _ => unreachable!(),
    }
}

/// Create a new object
async fn do_create<C: Card>(doc: &Document) -> Result<()> {
    let value = C::create_value(doc)?;
    let json = value.into();
    console::log_1(&json);
    let window = web_sys::window().unwrap_throw();
    fetch_post(&window, C::URI, &json).await
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
        _ => Ok("".into()),
    }
}

/// Set global allocator to `wee_alloc`
#[global_allocator]
static ALLOC: wee_alloc::WeeAlloc = wee_alloc::WeeAlloc::INIT;

/// Application starting function
#[wasm_bindgen(start)]
pub async fn start() -> Result<()> {
    // this should be debug only
    console_error_panic_hook::set_once();

    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();

    let json = fetch_get("/iris/api/comm_protocol").await?;
    let protocols = json.into_serde::<Vec<Protocol>>().unwrap_throw();
    let json = fetch_get("/iris/api/condition").await?;
    let conditions = json.into_serde::<Vec<Condition>>().unwrap_throw();
    STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        state.initialize(protocols, conditions);
    });

    let sb_type: HtmlSelectElement = doc.elem("sb_type")?;
    sb_type.set_inner_html(&types_html());
    add_select_event_listener(&sb_type, handle_sb_type_ev)?;
    add_input_event_listener(&doc.elem("sb_input")?)?;
    add_click_event_listener(&doc.elem("sb_list")?)?;
    add_transition_event_listener(&doc.elem("sb_list")?)?;
    add_interval_callback(&window)?;
    Ok(())
}

/// Create types `select` element
fn types_html() -> String {
    let (alarm_tname, alarm_ename) = (Alarm::TNAME, Alarm::ENAME);
    let (cab_tname, cab_ename) = (CabinetStyle::TNAME, CabinetStyle::ENAME);
    let (cc_tname, cc_ename) = (CommConfig::TNAME, CommConfig::ENAME);
    let (cl_tname, cl_ename) = (CommLink::TNAME, CommLink::ENAME);
    let (ctr_tname, ctr_ename) = (Controller::TNAME, Controller::ENAME);
    let (mdm_tname, mdm_ename) = (Modem::TNAME, Modem::ENAME);
    format!(
        "<option/>\
        <optgroup label='🧰 Maintenance'>\
          <option value='{alarm_tname}'>{alarm_ename}</option>
          <option value='{cab_tname}'>{cab_ename}</option>
          <option value='{cc_tname}'>{cc_ename}</option>
          <option value='{cl_tname}'>{cl_ename}</option>
          <option value='{ctr_tname}'>{ctr_ename}</option>
          <option value='{mdm_tname}'>{mdm_ename}</option>
        </optgroup>"
    )
}

/// Create an HTML `select` element of comm protocols
pub fn protocols_html(selected: u32) -> String {
    STATE.with(|rc| {
        let state = rc.borrow();
        let mut html = String::new();
        html.push_str("<select id='edit_protocol'>");
        for protocol in &state.protocols {
            html.push_str("<option value='");
            html.push_str(&protocol.id.to_string());
            html.push('\'');
            if selected == protocol.id {
                html.push_str(" selected");
            }
            html.push('>');
            html.push_str(&protocol.description);
            html.push_str("</option>");
        }
        html.push_str("</select>");
        html
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

/// Handle an event from "sb_type" `select` element
fn handle_sb_type_ev(tp: String) {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        state.selected.take()
    });
    if let Ok(input) = doc.elem::<HtmlInputElement>("sb_input") {
        input.set_value("");
    }
    spawn_local(populate_list(tp, "".into()));
}

/// Search list using the value from "sb_input"
fn search_list() {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    if let Ok(input) = doc.elem::<HtmlInputElement>("sb_input") {
        let value = input.value();
        deselect_card(&doc);
        if let Some(tp) = doc.select_parse::<String>("sb_type") {
            spawn_local(populate_list(tp, value));
        }
    }
}

/// Add an "input" event listener to a `select` element
fn add_select_event_listener(
    elem: &HtmlSelectElement,
    handle_ev: fn(String),
) -> Result<()> {
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
fn add_input_event_listener(elem: &HtmlInputElement) -> Result<()> {
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

/// Add a "click" event listener to an element
fn add_click_event_listener(elem: &Element) -> Result<()> {
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

/// Handle an event from "sb_list" `click` element
fn handle_click_ev(elem: &Element) {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    if elem.is_instance_of::<HtmlButtonElement>() {
        handle_button_click_ev(&doc, elem);
    } else if let Some(card) = elem.closest(".card").unwrap_throw() {
        if let Some(id) = card.get_attribute("id") {
            if let Some(name) = card.get_attribute("name") {
                if let Some(tp) = doc.select_parse::<String>("sb_type") {
                    deselect_card(&doc);
                    spawn_local(click_card(tp, id, name));
                }
            }
        }
    }
}

/// Handle a click event with a button target
fn handle_button_click_ev(doc: &Document, elem: &Element) {
    let cs = STATE.with(|rc| rc.borrow().selected.clone());
    if let Some(cs) = cs {
        match elem.id() {
            id if id == "ob_close" => cs.replace_card(doc, CardType::Compact),
            id if id == "ob_delete" => {
                if STATE.with(|rc| rc.borrow().delete_enabled) {
                    spawn_local(cs.delete());
                }
            }
            id if id == "ob_edit" => cs.replace_card(doc, CardType::Edit),
            id if id == "ob_save" => spawn_local(cs.save_changed()),
            id => console::log_1(&format!("unknown button: {}", id).into()),
        }
    }
}

/// Add transition event listener to an element
fn add_transition_event_listener(elem: &Element) -> Result<()> {
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
        state.selected.take()
    });
    if let Some(cs) = cs {
        cs.replace_card(doc, CardType::Compact);
    }
}

/// Add callback for regular interval checks
fn add_interval_callback(window: &Window) -> Result<()> {
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
