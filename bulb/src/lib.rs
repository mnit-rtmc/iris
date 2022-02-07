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
    ScrollBehavior, ScrollIntoViewOptions, ScrollLogicalPosition, Window,
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

/// Comm protocol
#[derive(Debug, Deserialize, Serialize)]
pub struct Protocol {
    pub id: u32,
    pub description: String,
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
        200 => Ok(()),
        _ => Err(resp.status_text().into()),
    }
}

/// Populate `sb_list` with `tp` card types
async fn populate_list(tp: String, tx: String) {
    match tp.as_str() {
        Alarm::ENAME => populate_cards::<Alarm>(tx).await,
        CabinetStyle::ENAME => populate_cards::<CabinetStyle>(tx).await,
        CommConfig::ENAME => populate_cards::<CommConfig>(tx).await,
        CommLink::ENAME => populate_cards::<CommLink>(tx).await,
        Controller::ENAME => populate_cards::<Controller>(tx).await,
        Modem::ENAME => populate_cards::<Modem>(tx).await,
        _ => (),
    }
}

/// Populate cards in `sb_list`
async fn populate_cards<C: Card>(tx: String) {
    if let Err(e) = try_populate_cards::<C>(tx).await {
        // ⛔ 🔒 unauthorized (401) should be handled here
        console::log_1(&e);
    }
}

/// Try to populate cards in `sb_list`
async fn try_populate_cards<C: Card>(tx: String) -> Result<()> {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    let sb_list = doc.elem::<Element>("sb_list")?;
    if C::URI.is_empty() {
        sb_list.set_inner_html("");
    } else {
        let json = fetch_get(C::URI).await?;
        let tx = tx.to_lowercase();
        let html = C::build_cards(&json, &tx)?;
        sb_list.set_inner_html(&html);
    }
    Ok(())
}

/// Handle a card click event
async fn click_card(tp: String, name: String) {
    match tp.as_str() {
        Alarm::ENAME => expand_card::<Alarm>(name).await,
        CabinetStyle::ENAME => expand_card::<CabinetStyle>(name).await,
        CommConfig::ENAME => expand_card::<CommConfig>(name).await,
        CommLink::ENAME => expand_card::<CommLink>(name).await,
        Controller::ENAME => expand_card::<Controller>(name).await,
        Modem::ENAME => expand_card::<Modem>(name).await,
        _ => (),
    }
}

/// Expand a card to a full form
async fn expand_card<C: Card>(name: String) {
    if name.is_empty() {
        // todo: make "new" card?
        return;
    }
    let cs = fetch_card::<C>(name).await;
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    cs.replace_card(&doc, CardType::Status);
}

/// Fetch a card with a GET request
async fn fetch_card<C: Card>(name: String) -> CardState {
    let uri = name_uri::<C>(&name);
    let json = fetch_get(&uri).await.unwrap_throw();
    console::log_1(&json);
    CardState {
        tname: C::TNAME,
        uri,
        name,
        json,
    }
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
    json: JsValue,
}

impl CardState {
    /// Replace a card element with another card type
    fn replace_card(self, doc: &Document, ct: CardType) {
        let id = format!("{}_{}", self.tname, &self.name);
        match doc.elem::<HtmlElement>(&id) {
            Ok(elem) => match build_card(&self.tname, &self.json, ct) {
                Ok(html) => replace_card_html(&elem, ct, &html),
                Err(e) => console::log_1(&(&e).into()),
            },
            Err(e) => console::log_1(&(&e).into()),
        }
        STATE.with(|rc| {
            let mut state = rc.borrow_mut();
            state.selected.replace(self);
        });
    }

    /// Save changed fields on Edit form
    async fn save_changed(mut self) {
        let window = web_sys::window().unwrap_throw();
        let doc = window.document().unwrap_throw();
        match try_changed_fields(&self, &doc) {
            Ok(v) => {
                let json = v.into();
                if let Err(e) = fetch_patch(&window, &self.uri, &json).await {
                    console::log_1(&e)
                }
            }
            Err(e) => console::log_1(&e),
        }
        self.fetch_again().await;
        self.replace_card(&doc, CardType::Compact);
    }

    /// Fetch a card again with a GET request
    async fn fetch_again(&mut self) {
        let json = fetch_get(&self.uri).await.unwrap_throw();
        console::log_1(&json);
        self.json = json;
    }
}

/// Get the URI of an object
fn name_uri<C: Card>(name: &str) -> String {
    format!("{}/{}", C::URI, utf8_percent_encode(name, NON_ALPHANUMERIC))
}

/// Replace a card with provieded HTML
fn replace_card_html(elem: &HtmlElement, ct: CardType, html: &str) {
    elem.set_inner_html(&html);
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

/// Try to retrieve changed fields on edit form
fn try_changed_fields(cs: &CardState, doc: &Document) -> Result<String> {
    match cs.tname {
        Alarm::TNAME => Alarm::changed_fields(doc, &cs.json),
        CabinetStyle::TNAME => CabinetStyle::changed_fields(doc, &cs.json),
        CommConfig::TNAME => CommConfig::changed_fields(doc, &cs.json),
        CommLink::TNAME => CommLink::changed_fields(doc, &cs.json),
        Controller::TNAME => Controller::changed_fields(doc, &cs.json),
        Modem::TNAME => Modem::changed_fields(doc, &cs.json),
        _ => unreachable!(),
    }
}

/// Build card using JSON value
fn build_card(tp: &str, json: &JsValue, ct: CardType) -> Result<String> {
    match tp {
        Alarm::TNAME => Alarm::build_card(json, ct),
        CabinetStyle::TNAME => CabinetStyle::build_card(json, ct),
        CommConfig::TNAME => CommConfig::build_card(json, ct),
        CommLink::TNAME => CommLink::build_card(json, ct),
        Controller::TNAME => Controller::build_card(json, ct),
        Modem::TNAME => Modem::build_card(json, ct),
        _ => Ok("".into()),
    }
}

/// Global app state
#[derive(Default)]
struct State {
    protocols: Vec<Protocol>,
    selected: Option<CardState>,
}

thread_local! {
    static STATE: RefCell<State> = RefCell::new(State::default());
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

    let json = fetch_get(&"/iris/api/comm_protocol").await?;
    let mut protocols = json.into_serde::<Vec<Protocol>>().unwrap_throw();
    STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        state.protocols.append(&mut protocols);
    });

    let sb_type: HtmlSelectElement = doc.elem("sb_type")?;
    let opt = doc.create_element("option")?;
    opt.append_with_str_1("")?;
    sb_type.append_child(&opt)?;
    let group = doc.create_element("optgroup")?;
    group.set_attribute("label", "🧰 Maintenance")?;
    add_option::<Alarm>(&doc, &group)?;
    add_option::<CabinetStyle>(&doc, &group)?;
    add_option::<CommConfig>(&doc, &group)?;
    add_option::<CommLink>(&doc, &group)?;
    add_option::<Controller>(&doc, &group)?;
    add_option::<Modem>(&doc, &group)?;
    sb_type.append_child(&group)?;
    add_select_event_listener(&sb_type, handle_sb_type_ev)?;
    let sb_input = doc.elem("sb_input")?;
    add_input_event_listener(&sb_input, handle_search_ev)?;
    add_click_event_listener(&doc.elem("sb_list")?)?;
    Ok(())
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

/// Add an option element to a group
fn add_option<C: Card>(doc: &Document, group: &Element) -> Result<()> {
    let opt = doc.create_element("option")?;
    opt.append_with_str_1(C::ENAME)?;
    group.append_child(&opt)?;
    Ok(())
}

/// Handle an event from "sb_type" `select` element
fn handle_sb_type_ev(tp: String) {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    deselect_card(&doc).unwrap_throw();
    let input: HtmlInputElement = doc.elem("sb_input").unwrap_throw();
    input.set_value("");
    spawn_local(populate_list(tp, "".into()));
}

/// Handle an event from "sb_input" `input` element
fn handle_search_ev(tx: String) {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    deselect_card(&doc).unwrap_throw();
    let tp = selected_type(&doc).unwrap_throw();
    spawn_local(populate_list(tp, tx));
}

/// Get the selected type
fn selected_type(doc: &Document) -> Result<String> {
    let sb_type: HtmlSelectElement = doc.elem("sb_type")?;
    Ok(sb_type.value())
}

/// Add an "input" event listener to an element
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
fn add_input_event_listener(
    elem: &HtmlInputElement,
    handle_ev: fn(String),
) -> Result<()> {
    let closure = Closure::wrap(Box::new(move |e: Event| {
        let value = e
            .current_target()
            .unwrap()
            .dyn_into::<HtmlInputElement>()
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
        if let Some(name) = card.get_attribute("name") {
            let tp = selected_type(&doc).unwrap_throw();
            deselect_card(&doc).unwrap_throw();
            spawn_local(click_card(tp, name));
        }
    }
}

/// Handle a click event with a button target
fn handle_button_click_ev(doc: &Document, elem: &Element) {
    let cs = STATE.with(|rc| rc.borrow().selected.clone());
    if let Some(cs) = cs {
        match elem.id() {
            id if id == "ob_close" => cs.replace_card(&doc, CardType::Compact),
            id if id == "ob_delete" => todo!(),
            id if id == "ob_edit" => cs.replace_card(&doc, CardType::Edit),
            id if id == "ob_status" => cs.replace_card(&doc, CardType::Status),
            id if id == "ob_save" => spawn_local(cs.save_changed()),
            id => console::log_1(&id.into()),
        }
    }
}

/// Deselect the selected card
fn deselect_card(doc: &Document) -> Result<()> {
    let cs = STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        state.selected.take()
    });
    if let Some(cs) = cs {
        cs.replace_card(doc, CardType::Compact);
    }
    Ok(())
}
