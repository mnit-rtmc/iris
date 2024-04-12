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
use crate::error::{Error, Result};
use crate::fetch::Uri;
use crate::item::ItemState;
use crate::permission::permissions_html;
use crate::resource::{Resource, View};
use crate::util::Doc;
use js_sys::JsString;
use resources::Res;
use std::cell::RefCell;
use wasm_bindgen::prelude::*;
use wasm_bindgen::JsCast;
use wasm_bindgen_futures::spawn_local;
use web_sys::{
    console, Element, Event, EventSource, HtmlButtonElement, HtmlElement,
    HtmlInputElement, HtmlSelectElement, MessageEvent, ScrollBehavior,
    ScrollIntoViewOptions, ScrollLogicalPosition, TransitionEvent, Window,
};

/// JavaScript result
pub type JsResult<T> = std::result::Result<T, JsValue>;

/// Page sidebar
const SIDEBAR: &str = include_str!("sidebar.html");

/// ID of toast element
const TOAST_ID: &str = "sb_toast";

/// ID of login shade
const LOGIN_ID: &str = "sb_login";

/// Interval (ms) between ticks for deferred actions
const TICK_INTERVAL: i32 = 500;

/// Selected card state
#[derive(Clone, Debug)]
struct SelectedCard {
    /// Resource type
    res: Resource,
    /// Card view
    view: View,
    /// Object name
    name: String,
    /// Delete action enabled (slider transition finished)
    delete_enabled: bool,
}

/// Button attributes
struct ButtonAttrs {
    id: String,
    class_name: String,
    data_link: Option<String>,
    data_type: Option<String>,
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
    /// Have permissions been initialized?
    initialized: bool,
    /// Logged-in user name
    user: Option<String>,
    /// Deferred actions (with tick number)
    deferred: Vec<(i32, DeferredAction)>,
    /// Timer tick count
    tick: i32,
    /// Selected card
    selected_card: Option<SelectedCard>,
}

thread_local! {
    static STATE: RefCell<State> = RefCell::new(State::default());
}

impl State {
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
        self.deferred
            .retain(|(_, a)| *a != DeferredAction::SearchList)
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
            Self::SearchList => search_resource_list(),
            Self::HideToast => hide_toast(),
        }
    }
}

/// Search resource list using the value from "sb_search"
fn search_resource_list() {
    let doc = Doc::get();
    if let Some(rname) = doc.select_parse::<String>("sb_resource") {
        let res = Resource::try_from(rname.as_str()).ok();
        let value = search_value();
        spawn_local(populate_list(res, value));
    }
}

/// Populate `sb_list` with `res` card types
async fn populate_list(res: Option<Resource>, search: String) {
    STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        state.selected_card.take()
    });
    let doc = Doc::get();
    let sb_list = doc.elem::<Element>("sb_list");
    match res {
        Some(res) => {
            let config = doc.input_bool("sb_config");
            match res.fetch_cards(&search, config).await {
                Ok(cards) => sb_list.set_inner_html(&cards),
                Err(Error::FetchResponseUnauthorized()) => show_login(),
                Err(e) => show_toast(&format!("View failed: {e}")),
            }
        }
        None => sb_list.set_inner_html(""),
    }
}

/// Handle a card click event
async fn click_card(res: Resource, id: String, name: String) {
    deselect_card().await;
    if id.ends_with('_') {
        let cs = SelectedCard::new(res, View::Create, name);
        cs.replace_card(View::Create).await;
    } else {
        let config = Doc::get().input_bool("sb_config");
        let cs = SelectedCard::new(res, View::Status(config), name);
        cs.replace_card(View::Status(config)).await;
    }
}

/// Deselect the selected card
async fn deselect_card() {
    let cs = STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        state.selected_card.take()
    });
    if let Some(cs) = cs {
        let v = cs.view;
        if !v.is_compact() {
            let v = v.compact();
            cs.replace_card(v).await;
        }
    }
}

impl SelectedCard {
    /// Create a new blank selected card
    fn new(res: Resource, view: View, name: String) -> Self {
        SelectedCard {
            res,
            view,
            name,
            delete_enabled: false,
        }
    }

    /// Get card element ID
    fn id(&self) -> String {
        let res = Res::from(self.res);
        if self.view.is_create() {
            format!("{res}_")
        } else {
            format!("{res}_{}", &self.name)
        }
    }

    /// Replace a card element with another card type
    async fn replace_card(mut self, v: View) {
        let id = self.id();
        let doc = Doc::get();
        let elem = doc.elem::<HtmlElement>(&id);
        let res = self.res;
        match res.fetch_card(&self.name, v).await {
            Ok(html) => replace_card_html(&elem, v, &html),
            Err(Error::FetchResponseUnauthorized()) => {
                show_login();
                return;
            }
            Err(e) => {
                show_toast(&format!("Fetch failed: {e}"));
                // Card list may be out-of-date; refresh with search
                DeferredAction::SearchList.schedule(200);
                return;
            }
        }
        STATE.with(|rc| {
            let mut state = rc.borrow_mut();
            if v.is_compact() {
                state.selected_card.take();
            } else {
                self.view = v;
                state.selected_card.replace(self);
                state.clear_searches();
            }
        });
    }

    /// Save changed fields
    async fn save_changed(self) {
        let v = self.view;
        match v {
            View::Create => self.res_create().await,
            View::Edit | View::Status(_) => self.res_save_edit().await,
            View::Location => self.res_save_loc().await,
            _ => (),
        }
    }

    /// Create a new object from card
    async fn res_create(self) {
        let res = self.res;
        match res.create_and_post().await {
            Ok(_) => {
                self.replace_card(View::Create.compact()).await;
                DeferredAction::SearchList.schedule(1500);
            }
            Err(Error::FetchResponseUnauthorized()) => show_login(),
            Err(e) => show_toast(&format!("Create failed: {e}")),
        }
    }

    /// Save changed fields on Edit card
    async fn res_save_edit(self) {
        let res = self.res;
        match res.save(&self.name).await {
            Ok(_) => self.replace_card(View::Edit.compact()).await,
            Err(Error::FetchResponseUnauthorized()) => show_login(),
            Err(Error::FetchResponseNotFound()) => {
                // Card list out-of-date; refresh with search
                DeferredAction::SearchList.schedule(200);
            }
            Err(e) => show_toast(&format!("Save failed: {e}")),
        }
    }

    /// Save changed fields on Location card
    async fn res_save_loc(self) {
        let res = self.res;
        let geo_loc = res.fetch_geo_loc(&self.name).await;
        let result = match geo_loc {
            Ok(Some(geo_loc)) => Resource::GeoLoc.save(&geo_loc).await,
            Ok(None) => Ok(()),
            Err(e) => Err(e),
        };
        match result {
            Ok(_) => self.replace_card(View::Location.compact()).await,
            Err(Error::FetchResponseUnauthorized()) => show_login(),
            Err(e) => show_toast(&format!("Save failed: {e}")),
        }
    }

    /// Delete selected card / object
    async fn res_delete(self) {
        let res = self.res;
        match res.delete(&self.name).await {
            Ok(_) => DeferredAction::SearchList.schedule(1000),
            Err(Error::FetchResponseUnauthorized()) => show_login(),
            Err(e) => show_toast(&format!("Delete failed: {e}")),
        }
    }

    /// Handle a button click on selected card
    async fn handle_click(self, attrs: &ButtonAttrs) -> bool {
        let res = self.res;
        match res.handle_click(&self.name, &attrs.id).await {
            Ok(c) => c,
            Err(e) => {
                show_toast(&format!("Click failed: {e}"));
                false
            }
        }
    }

    /// Handle an input event on selected card
    async fn handle_input(self, id: &str) -> bool {
        let res = self.res;
        match res.handle_input(&self.name, id).await {
            Ok(c) => c,
            Err(_e) => false,
        }
    }
}

/// Show login form shade
fn show_login() {
    STATE.with(|rc| rc.borrow_mut().user = None);
    Doc::get()
        .elem::<HtmlElement>(LOGIN_ID)
        .set_class_name("show");
}

/// Hide login form shade
fn hide_login() {
    Doc::get().elem::<HtmlElement>(LOGIN_ID).set_class_name("");
}

/// Show a toast message
fn show_toast(msg: &str) {
    console::log_1(&format!("toast: {msg}").into());
    let t = Doc::get().elem::<HtmlElement>(TOAST_ID);
    t.set_inner_html(msg);
    t.set_class_name("show");
    DeferredAction::HideToast.schedule(3000);
}

/// Hide toast
fn hide_toast() {
    Doc::get().elem::<HtmlElement>(TOAST_ID).set_class_name("");
}

/// Replace a card with provieded HTML
fn replace_card_html(elem: &HtmlElement, v: View, html: &str) {
    elem.set_inner_html(html);
    if v.is_compact() {
        elem.set_class_name("card");
    } else {
        elem.set_class_name("form");
        let mut opt = ScrollIntoViewOptions::new();
        opt.behavior(ScrollBehavior::Smooth)
            .block(ScrollLogicalPosition::Nearest);
        elem.scroll_into_view_with_scroll_into_view_options(&opt);
    }
}

/// Application starting function
#[wasm_bindgen(start)]
pub async fn start() -> core::result::Result<(), JsError> {
    // this should be debug only
    console_error_panic_hook::set_once();
    add_sidebar().await.unwrap_throw();
    Ok(())
}

/// Add sidebar HTML and event listeners
async fn add_sidebar() -> JsResult<()> {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    let doc = Doc(doc);
    let sidebar: HtmlElement = doc.elem("sidebar");
    sidebar.set_inner_html(SIDEBAR);
    add_fullscreenchange_listener(&sidebar)?;
    add_change_listener(&sidebar)?;
    add_click_listener(&sidebar)?;
    add_input_listener(&sidebar)?;
    add_transition_listener(&doc.elem("sb_list"))?;
    add_interval_callback(&window)?;
    add_eventsource_listener()?;
    fill_resource_select().await;
    Ok(())
}

/// Fill resource select element
async fn fill_resource_select() {
    let doc = Doc::get();
    let config = doc.input_bool("sb_config");
    match fetch_access_list(config).await {
        Ok(perm) => {
            let sb_resource = doc.elem::<HtmlSelectElement>("sb_resource");
            sb_resource.set_inner_html(&perm);
            STATE.with(|rc| rc.borrow_mut().initialized = true);
        }
        Err(Error::FetchResponseUnauthorized()) => show_login(),
        Err(e) => {
            console::log_1(&format!("fill_resource_select: {e:?}").into());
        }
    }
}

/// Fetch permission access list
async fn fetch_access_list(config: bool) -> Result<String> {
    let json = Uri::from("/iris/api/access").get().await?;
    let permissions = serde_wasm_bindgen::from_value(json)?;
    Ok(permissions_html(permissions, config))
}

/// Add a "fullscreenchange" event listener to an element
fn add_fullscreenchange_listener(elem: &Element) -> JsResult<()> {
    let closure = Closure::wrap(Box::new(|_e: Event| {
        let doc = Doc::get();
        let btn = doc.elem::<HtmlInputElement>("sb_fullscreen");
        btn.set_checked(doc.is_fullscreen());
    }) as Box<dyn Fn(_)>);
    elem.add_event_listener_with_callback(
        "change",
        closure.as_ref().unchecked_ref(),
    )?;
    // can't drop closure, just forget it to make JS happy
    closure.forget();
    Ok(())
}

/// Add a "change" event listener to an element
fn add_change_listener(elem: &Element) -> JsResult<()> {
    let closure = Closure::wrap(Box::new(|e: Event| {
        let target = e.target().unwrap().dyn_into::<Element>().unwrap();
        let id = target.id();
        match id.as_str() {
            "sb_config" => spawn_local(reload_resources()),
            "sb_fullscreen" => Doc::get().toggle_fullscreen(),
            _ => (),
        }
    }) as Box<dyn Fn(_)>);
    elem.add_event_listener_with_callback(
        "change",
        closure.as_ref().unchecked_ref(),
    )?;
    // can't drop closure, just forget it to make JS happy
    closure.forget();
    Ok(())
}

/// Reload resource select element
async fn reload_resources() {
    fill_resource_select().await;
    search_resource_list();
}

/// Get value to search
fn search_value() -> String {
    let doc = Doc::get();
    let search = doc.elem::<HtmlInputElement>("sb_search");
    let mut value = search.value();
    if let Some(istate) = doc.select_parse::<String>("sb_state") {
        if ItemState::from_code(&istate).is_some() {
            value.push(' ');
            value.push_str(&istate);
        }
    }
    value
}

/// Add an "input" event listener to an element
fn add_input_listener(elem: &Element) -> JsResult<()> {
    let closure = Closure::wrap(Box::new(|e: Event| {
        let target = e.target().unwrap().dyn_into::<Element>().unwrap();
        let id = target.id();
        match id.as_str() {
            "sb_search" | "sb_state" => search_resource_list(),
            "sb_resource" => {
                handle_sb_resource_ev(
                    target.dyn_into::<HtmlSelectElement>().unwrap().value(),
                );
            }
            _ => {
                let cs = STATE.with(|rc| rc.borrow().selected_card.clone());
                if let Some(cs) = cs {
                    spawn_local(handle_input_card(id, cs));
                }
            }
        }
    }) as Box<dyn Fn(_)>);
    elem.add_event_listener_with_callback(
        "input",
        closure.as_ref().unchecked_ref(),
    )?;
    // can't drop closure, just forget it to make JS happy
    closure.forget();
    Ok(())
}

/// Handle an event from "sb_resource" `select` element
fn handle_sb_resource_ev(rname: String) {
    let doc = Doc::get();
    let search = doc.elem::<HtmlInputElement>("sb_search");
    search.set_value("");
    let res = Resource::try_from(rname.as_str()).ok();
    let sb_state = doc.elem::<HtmlSelectElement>("sb_state");
    match res {
        Some(res) => sb_state.set_inner_html(res.item_state_options()),
        None => sb_state.set_inner_html(""),
    }
    let value = search_value();
    spawn_local(populate_list(res, value));
    spawn_local(post_notify(rname));
}

/// POST selected resource name to notify endpoint
async fn post_notify(rname: String) {
    let uri = Uri::from("/iris/api/notify");
    let js = format!("[\"{rname}\"]");
    match uri.post(&js.into()).await {
        Ok(_) => {
            console::log_1(&format!("/iris/api/notify POST {rname}").into())
        }
        Err(e) => {
            console::log_1(&format!("/iris/api/notify POST failed {e}").into());
        }
    }
}

/// Handle input event with selected card
async fn handle_input_card(id: String, cs: SelectedCard) {
    if !cs.handle_input(&id).await {
        console::log_1(&format!("unknown id: {id}").into());
    }
}

/// Add a `click` event listener to an element
fn add_click_listener(elem: &Element) -> JsResult<()> {
    let closure = Closure::wrap(Box::new(|e: Event| {
        let target = e.target().unwrap().dyn_into::<Element>().unwrap();
        if target.is_instance_of::<HtmlButtonElement>() {
            handle_button_click_ev(&target);
        } else if let Some(card) = target.closest(".card").unwrap_throw() {
            if let Some(id) = card.get_attribute("id") {
                if let Some(name) = card.get_attribute("name") {
                    let doc = Doc::get();
                    if let Some(rname) =
                        doc.select_parse::<String>("sb_resource")
                    {
                        if let Ok(res) = Resource::try_from(rname.as_str()) {
                            spawn_local(click_card(res, id, name));
                        }
                    }
                }
            }
        }
    }) as Box<dyn FnMut(_)>);
    elem.add_event_listener_with_callback(
        "click",
        closure.as_ref().unchecked_ref(),
    )?;
    // can't drop closure, just forget it to make JS happy
    closure.forget();
    Ok(())
}

/// Handle a `click` event with a button target
fn handle_button_click_ev(target: &Element) {
    let id = target.id();
    if id == "ob_login" {
        spawn_local(handle_login());
        return;
    }
    let cs = STATE.with(|rc| rc.borrow().selected_card.clone());
    if let Some(cs) = cs {
        let attrs = ButtonAttrs {
            id,
            class_name: target.class_name(),
            data_link: target.get_attribute("data-link"),
            data_type: target.get_attribute("data-type"),
        };
        spawn_local(handle_button_card(attrs, cs));
    }
}

/// Handle button click event with selected card
async fn handle_button_card(attrs: ButtonAttrs, cs: SelectedCard) {
    match attrs.id.as_str() {
        "ob_close" => {
            let v = cs.view.compact();
            cs.replace_card(v).await;
        }
        "ob_delete" => {
            if cs.delete_enabled {
                cs.res_delete().await;
            }
        }
        "ob_edit" => cs.replace_card(View::Edit).await,
        "ob_loc" => cs.replace_card(View::Location).await,
        "ob_save" => cs.save_changed().await,
        _ => {
            if attrs.class_name == "go_link" {
                go_resource(attrs).await;
            } else if !cs.handle_click(&attrs).await {
                console::log_1(
                    &format!("unknown button: {}", &attrs.id).into(),
                );
            }
        }
    }
}

/// Handle login button press
async fn handle_login() {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    let doc = Doc(doc);
    if let (Some(user), Some(pass)) = (
        doc.input_parse::<String>("login_user"),
        doc.input_parse::<String>("login_pass"),
    ) {
        let uri = Uri::from("/iris/api/login");
        let js = format!("{{\"username\":\"{user}\",\"password\":\"{pass}\"}}");
        match uri.post(&js.into()).await {
            Ok(_) => {
                let pass = doc.elem::<HtmlInputElement>("login_pass");
                pass.set_value("");
                hide_login();
                STATE.with(|rc| rc.borrow_mut().user = Some(user));
                if !STATE.with(|rc| rc.borrow().initialized) {
                    fill_resource_select().await;
                }
            }
            Err(e) => show_toast(&format!("Login failed: {e}")),
        }
    }
}

/// Go to resource from target's `data-link` attribute
async fn go_resource(attrs: ButtonAttrs) {
    let doc = Doc::get();
    if let (Some(link), Some(rname)) = (attrs.data_link, attrs.data_type) {
        let sb_resource = doc.elem::<HtmlSelectElement>("sb_resource");
        sb_resource.set_value(&rname);
        let search = doc.elem::<HtmlInputElement>("sb_search");
        search.set_value(&link);
        let res = Resource::try_from(rname.as_str()).ok();
        populate_list(res, link).await;
    }
}

/// Add transition event listener to an element
fn add_transition_listener(elem: &Element) -> JsResult<()> {
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

/// Handle a `transition*` event from `sb_list` child element
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
        if let Some(selected_card) = &mut state.selected_card {
            selected_card.delete_enabled = enabled;
        }
    });
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

/// Add event source listener for notifications
fn add_eventsource_listener() -> JsResult<()> {
    let es = EventSource::new("/iris/api/notify")?;
    let onmessage = Closure::wrap(Box::new(|me: MessageEvent| {
        if let Ok(payload) = me.data().dyn_into::<JsString>() {
            update_resource_cards(payload);
        }
    }) as Box<dyn FnMut(MessageEvent)>);
    es.set_onmessage(Some(onmessage.as_ref().unchecked_ref()));
    // can't drop closure, just forget it to make JS happy
    onmessage.forget();
    Ok(())
}

/// Update resource cards in `sb_list`
fn update_resource_cards(payload: JsString) {
    console::log_1(&format!("payload: {payload:?}").into());
    // TODO: fetch updated list for resource
    // TODO: update existing resource cards
    search_resource_list();
}

/// Get logged-in user name
pub fn user() -> Option<String> {
    STATE.with(|rc| rc.borrow().user.clone())
}
