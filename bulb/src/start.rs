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
use crate::app::{self, DeferredAction, NotifyState};
use crate::card::{self, CardList, CardView, View};
use crate::error::{Error, Result};
use crate::fetch::Uri;
use crate::item::ItemState;
use crate::util::Doc;
use hatmil::Html;
use js_sys::JsString;
use resources::Res;
use std::error::Error as _;
use wasm_bindgen::JsCast;
use wasm_bindgen::prelude::*;
use wasm_bindgen_futures::spawn_local;
use web_sys::{
    CustomEvent, Element, Event, EventSource, HtmlButtonElement, HtmlElement,
    HtmlInputElement, HtmlSelectElement, MessageEvent, ScrollBehavior,
    ScrollIntoViewOptions, ScrollLogicalPosition, TransitionEvent, Window,
    console,
};

/// JavaScript result
pub type JsResult<T> = std::result::Result<T, JsValue>;

/// JavaScript imports
#[wasm_bindgen(module = "/static/glue.js")]
extern "C" {
    /// Update station data
    fn update_stat_sample(data: &JsValue);
    // Update TMS main item states
    fn update_item_states(data: &JsValue);
    // Fly map to given item
    fn fly_map_to(fid: &JsValue, lat: &JsValue, lng: &JsValue);
    // Enable/disable flying map
    fn fly_enable(enable: JsValue);
}

/// Button attributes
struct ButtonAttrs {
    id: String,
    class_name: String,
    data_link: Option<String>,
    data_type: Option<String>,
}

/// Show login form shade
fn show_login() {
    app::set_user(None);
    Doc::get()
        .elem::<HtmlElement>("sb_login")
        .set_class_name("show");
}

/// Hide login form shade
fn hide_login() {
    Doc::get()
        .elem::<HtmlElement>("sb_login")
        .set_class_name("");
}

/// Show a toast message
fn show_toast(msg: &str) {
    console::log_1(&format!("toast: {msg}").into());
    let t = Doc::get().elem::<HtmlElement>("sb_toast");
    t.set_inner_html(msg);
    t.set_class_name("show");
    app::defer_action(DeferredAction::HideToast, 3000);
}

/// Hide toast
fn hide_toast() {
    Doc::get()
        .elem::<HtmlElement>("sb_toast")
        .set_class_name("");
}

/// Fly map to specified item
pub fn fly_map_item(fid: &str, lat: f64, lon: f64) {
    let fid = JsValue::from_str(fid);
    let lat = JsValue::from_f64(lat);
    let lon = JsValue::from_f64(lon);
    fly_map_to(&fid, &lat, &lon);
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
    sidebar.set_inner_html(&sidebar_html());
    add_fullscreenchange_listener(&sidebar)?;
    add_change_listener(&sidebar)?;
    add_click_listener(&sidebar)?;
    add_input_listener(&sidebar)?;
    add_transition_listener(&doc.elem("sb_list"))?;
    add_interval_callback(&window)?;
    let mapid: HtmlElement = doc.elem("mapid");
    add_map_click_listener(&mapid)?;
    add_eventsource_listener();
    do_future(finish_init()).await;
    fetch_station_sample();
    Ok(())
}

/// Build sidebar HTML
fn sidebar_html() -> String {
    let mut html = Html::new();
    html.div().class("sb_row");
    html.label().for_("sb_resource").text("Resource").end();
    html.select().id("sb_resource").end();
    html.input()
        .id("sb_config")
        .type_("checkbox")
        .class("toggle");
    html.label().for_("sb_config").text("🧰").end();
    html.input()
        .id("sb_fullscreen")
        .type_("checkbox")
        .class("toggle");
    html.label().for_("sb_fullscreen").text(" ⛶ ").end();
    html.end(); /* div */
    html.div().class("sb_row");
    html.input()
        .id("sb_search")
        .type_("search")
        .size("16")
        .attr("placeholder", "🔍");
    html.select().id("sb_state").end();
    html.button()
        .id("sb_refresh")
        .type_("button")
        .text("⭮ ⚪")
        .end();
    html.end(); /* div */
    html.div().id("sb_list").end();
    html.div().id("sb_toast").end();
    html.div().id("sb_login").class("hide");
    html.div().class("form");
    html.div().class("row center");
    html.img().src("bulb/iris.svg");
    html.end(); /* div */
    html.div().class("row end");
    html.span().text("IRIS authentication required").end();
    html.end(); /* div */
    html.div().class("row end");
    html.label().for_("login_user").text("User name").end();
    html.input()
        .id("login_user")
        .type_("text")
        .attr("name", "username")
        .attr("autocomplete", "username")
        .required();
    html.end(); /* div */
    html.div().class("row end");
    html.label().for_("login_pass").text("Password").end();
    html.input()
        .id("login_pass")
        .type_("password")
        .attr("name", "password")
        .attr("autocomplete", "current-password")
        .required();
    html.end(); /* div */
    html.div().class("row end");
    html.button()
        .id("ob_login")
        .type_("button")
        .text("Login")
        .end();
    html.end(); /* div */
    html.end(); /* div (form) */
    html.div().id("sb_shade").end();
    html.end(); /* div (sb_login) */
    html.into()
}

/// Finish initialization
async fn finish_init() -> Result<()> {
    let user = Uri::from("/iris/api/login").get().await?;
    match user.as_string() {
        Some(user) => {
            app::set_user(Some(user));
            if !app::initialized() {
                fill_sb_resource().await?;
                app::set_initialized();
            }
        }
        None => console::log_1(&format!("invalid user: {user:?}").into()),
    }
    Ok(())
}

/// Fill resource select element
async fn fill_sb_resource() -> Result<()> {
    let doc = Doc::get();
    let config = doc.input_bool("sb_config");
    let perm = card::fetch_resource(config).await?;
    let sb_resource = doc.elem::<HtmlSelectElement>("sb_resource");
    sb_resource.set_inner_html(&perm);
    Ok(())
}

/// Add a "fullscreenchange" event listener to an element
fn add_fullscreenchange_listener(elem: &Element) -> JsResult<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|_e: Event| {
        let doc = Doc::get();
        let btn = doc.elem::<HtmlInputElement>("sb_fullscreen");
        btn.set_checked(doc.is_fullscreen());
    });
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
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        let target = e.target().unwrap().dyn_into::<Element>().unwrap();
        let id = target.id();
        match id.as_str() {
            "sb_config" => spawn_local(reload_resources()),
            "sb_fullscreen" => Doc::get().toggle_fullscreen(),
            _ => (),
        }
    });
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
    do_future(fill_sb_resource()).await;
    let sb_search = Doc::get().elem::<HtmlInputElement>("sb_search");
    sb_search.set_value("");
    handle_resource_change().await;
}

/// Handle a fallible future function
async fn do_future(future: impl Future<Output = Result<()>>) {
    match future.await {
        Ok(_) => (),
        Err(Error::FetchResponseUnauthorized()) => show_login(),
        Err(Error::FetchResponseNotFound()) => {
            // Card list may be out-of-date; refresh
            app::defer_action(DeferredAction::RefreshList, 200);
        }
        Err(Error::CardMismatch()) => {
            // Card list may be out-of-date; refresh
            app::defer_action(DeferredAction::RefreshList, 200);
        }
        Err(e) => {
            if let Some(se) = e.source() {
                console::log_1(&format!("{se}").into());
            }
            show_toast(&format!("Error: {e}"));
        }
    }
}

/// Handle change to selected resource type
async fn handle_resource_change() {
    let res = resource_value();
    app::card_list(None);
    let sb_state = Doc::get().elem::<HtmlSelectElement>("sb_state");
    let html = match res {
        Some(res) => card::item_states_html(res),
        None => String::new(),
    };
    sb_state.set_inner_html(&html);
    do_future(fetch_card_list()).await;
    do_future(populate_card_list()).await;
    let uri = Uri::from("/iris/api/notify");
    let rname = res.map_or("", |res| res.as_str());
    let json = if rname.is_empty() {
        "[]".to_string()
    } else {
        format!("[\"{rname}\"]")
    };
    if let Err(e) = uri.post(&json.into()).await {
        console::log_1(&format!("/iris/api/notify POST: {e}").into());
    }
}

/// Fetch card list for selected resource type
async fn fetch_card_list() -> Result<()> {
    let mut cards = app::card_list(None);
    if cards.is_none() {
        let res = resource_value();
        let config = Doc::get().input_bool("sb_config");
        cards = res.map(|res| CardList::new(res).config(config));
    }
    if let Some(cards) = &mut cards {
        cards.fetch().await?;
    }
    app::card_list(cards);
    Ok(())
}

/// Get the selected resource value
fn resource_value() -> Option<Res> {
    match Doc::get().select_parse::<String>("sb_resource") {
        Some(rname) => Res::try_from(rname.as_str()).ok(),
        None => None,
    }
}

/// Populate `sb_list` with selected resource type
async fn populate_card_list() -> Result<()> {
    let doc = Doc::get();
    let search = search_value();
    let html = build_card_list(&search).await?;
    let sb_list = doc.elem::<Element>("sb_list");
    sb_list.set_inner_html(&html);
    Ok(())
}

/// Get value to search
fn search_value() -> String {
    let doc = Doc::get();
    let sb_search = doc.elem::<HtmlInputElement>("sb_search");
    let mut search = sb_search.value();
    if let Some(istate) = doc.select_parse::<String>("sb_state") {
        if ItemState::from_code(&istate).is_some() {
            search.push(' ');
            search.push_str(&istate);
        }
    }
    search
}

/// Build a filtered list of cards for a resource
async fn build_card_list(search: &str) -> Result<String> {
    match app::card_list(None) {
        Some(mut cards) => {
            cards.search(search);
            let html = cards.make_html().await?;
            update_item_states(&JsValue::from_str(cards.states_main()));
            app::card_list(Some(cards));
            Ok(html)
        }
        None => Ok(String::new()),
    }
}

/// Add an "input" event listener to an element
fn add_input_listener(elem: &Element) -> JsResult<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        let target = e.target().unwrap().dyn_into::<Element>().unwrap();
        let id = target.id();
        match id.as_str() {
            "sb_config" => (),
            "sb_search" | "sb_state" => spawn_local(do_future(handle_search())),
            "sb_resource" => handle_sb_resource_ev(),
            "ob_view" => handle_ob_view_ev(),
            _ => spawn_local(do_future(handle_input(id))),
        }
    });
    elem.add_event_listener_with_callback(
        "input",
        closure.as_ref().unchecked_ref(),
    )?;
    // can't drop closure, just forget it to make JS happy
    closure.forget();
    Ok(())
}

/// Handle search input
async fn handle_search() -> Result<()> {
    if let Some(cv) = app::form() {
        replace_card(cv.compact()).await?
    }
    search_card_list().await
}

/// Search card list for matching cards
async fn search_card_list() -> Result<()> {
    match app::card_list(None) {
        Some(mut cards) => {
            let search = search_value();
            cards.search(&search);
            let doc = Doc::get();
            for cv in cards.view_change().await? {
                let id = cv.id();
                if let Some(elem) = doc.try_elem::<Element>(&id) {
                    elem.set_class_name(cv.view.class_name());
                }
            }
            app::card_list(Some(cards));
        }
        None => console::log_1(&"search failed - no card list".into()),
    }
    Ok(())
}

/// Handle an event from `sb_resource` select element
fn handle_sb_resource_ev() {
    let sb_search = Doc::get().elem::<HtmlInputElement>("sb_search");
    sb_search.set_value("");
    spawn_local(handle_resource_change());
}

/// Handle an event from `ob_view` select element
fn handle_ob_view_ev() {
    if let Some(cv) = app::form() {
        if let Some(view) = ob_view_value() {
            spawn_local(do_future(replace_card(cv.view(view))));
        }
    }
}

/// Get the selected view value
fn ob_view_value() -> Option<View> {
    match Doc::get().select_parse::<String>("ob_view") {
        Some(view) => View::try_from(view.as_str()).ok(),
        None => None,
    }
}

/// Handle an input event on a form card
async fn handle_input(id: String) -> Result<()> {
    if let Some(cv) = app::form() {
        card::handle_input(&cv, id).await?;
    }
    Ok(())
}

/// Add a `click` event listener to an element
fn add_click_listener(elem: &Element) -> JsResult<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        let target = e.target().unwrap().dyn_into::<Element>().unwrap();
        if target.is_instance_of::<HtmlButtonElement>() {
            handle_button_click_ev(&target);
        } else if let Ok(Some(cc)) = target.closest(".card-compact") {
            handle_card_click_ev(&cc);
        }
    });
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
    match id.as_str() {
        "ob_login" => spawn_local(handle_login()),
        "sb_refresh" => spawn_local(handle_refresh()),
        _ => {
            let attrs = ButtonAttrs {
                id,
                class_name: target.class_name(),
                data_link: target.get_attribute("data-link"),
                data_type: target.get_attribute("data-type"),
            };
            spawn_local(handle_button_card(attrs));
        }
    }
}

/// Handle button click event on a form card
async fn handle_button_card(attrs: ButtonAttrs) {
    if let Some(cv) = app::form() {
        match attrs.id.as_str() {
            "ob_delete" => do_future(handle_delete(cv)).await,
            "ob_save" => do_future(handle_save(cv)).await,
            _ => {
                if attrs.class_name == "go_link" {
                    go_resource(attrs).await;
                } else {
                    handle_button_cv(cv, attrs.id).await;
                }
            }
        }
    }
}

/// Replace a card view element with another view
async fn replace_card(cv: CardView) -> Result<()> {
    let html = card::fetch_one(&cv).await?;
    replace_card_html(&cv, &html);
    app::set_view(cv);
    Ok(())
}

/// Replace a card with provided HTML
fn replace_card_html(cv: &CardView, html: &str) {
    let Some(elem) = Doc::get().try_elem::<HtmlElement>(&cv.id()) else {
        console::log_1(
            &format!("replace_card_html: {} not found", cv.id()).into(),
        );
        return;
    };
    elem.set_inner_html(html);
    elem.set_class_name(cv.view.class_name());
    if cv.view.is_form() {
        let opt = ScrollIntoViewOptions::new();
        opt.set_behavior(ScrollBehavior::Smooth);
        opt.set_block(ScrollLogicalPosition::Nearest);
        elem.scroll_into_view_with_scroll_into_view_options(&opt);
    }
}

/// Handle delete button click
async fn handle_delete(cv: CardView) -> Result<()> {
    if app::delete_enabled() {
        card::delete_one(&cv).await?;
    }
    Ok(())
}

/// Handle save button click
async fn handle_save(cv: CardView) -> Result<()> {
    match cv.view {
        View::Create => save_create(cv).await,
        View::Setup | View::Location => save_changed(cv).await,
        _ => Ok(()),
    }
}

/// Save a create view card
async fn save_create(cv: CardView) -> Result<()> {
    card::create_and_post(cv.res).await?;
    replace_card(cv.view(View::CreateCompact)).await
}

/// Save changed values on Setup / Location card
async fn save_changed(cv: CardView) -> Result<()> {
    card::patch_changed(&cv).await?;
    replace_card(cv.view(View::Compact)).await
}

/// Handle a button click on a form card
async fn handle_button_cv(cv: CardView, id: String) {
    match card::handle_click(&cv, id).await {
        Ok(_) => (),
        Err(e) => show_toast(&format!("click failed: {e}")),
    }
}

/// Handle a `click` event within a card element
fn handle_card_click_ev(elem: &Element) {
    if let Some(id) = elem.get_attribute("id") {
        if let Some(name) = elem.get_attribute("name") {
            if let Some(res) = resource_value() {
                spawn_local(do_future(click_card(res, name, id)));
            }
        }
    }
}

/// Handle a card click event
async fn click_card(res: Res, name: String, id: String) -> Result<()> {
    if let Some(cv) = app::form() {
        replace_card(cv.compact()).await?;
    }
    // FIXME: check if id are the same for old/new cards
    let mut view = *card::res_views(res).get(1).unwrap_or(&View::Compact);
    if id.ends_with('_') {
        view = View::Create;
    }
    let cv = CardView::new(res, &name, view);
    replace_card(cv).await?;
    fly_enable(JsValue::TRUE);
    Ok(())
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
                do_future(finish_init()).await;
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
        let sb_search = doc.elem::<HtmlInputElement>("sb_search");
        sb_search.set_value(&link);
        handle_resource_change().await;
    }
}

/// Handle refresh button click
async fn handle_refresh() {
    do_future(fetch_card_list()).await;
    do_future(populate_card_list()).await;
}

/// Add transition event listener to an element
fn add_transition_listener(elem: &Element) -> JsResult<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(handle_transition_ev);
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
                    app::set_delete_enabled(&ev.type_() == "transitionend");
                }
            }
        }
    }
}

/// Add callback for regular interval checks
fn add_interval_callback(window: &Window) -> JsResult<()> {
    let closure: Closure<dyn Fn()> = Closure::new(tick_interval);
    window.set_interval_with_callback_and_timeout_and_arguments_0(
        closure.as_ref().unchecked_ref(),
        app::TICK_INTERVAL,
    )?;
    closure.forget();
    Ok(())
}

/// Process a tick interval
fn tick_interval() {
    app::tick_tock();
    while let Some(action) = app::next_action() {
        match action {
            DeferredAction::FetchStationData => fetch_station_sample(),
            DeferredAction::HideToast => hide_toast(),
            DeferredAction::MakeEventSource => add_eventsource_listener(),
            DeferredAction::RefreshList => spawn_local(handle_refresh()),
            DeferredAction::SetNotifyState(ns) => set_notify_state(ns),
        }
    }
}

/// Fetch station sample data
fn fetch_station_sample() {
    app::defer_action(DeferredAction::FetchStationData, 30_000);
    spawn_local(do_future(do_fetch_station_sample()));
}

/// Actually fetch station sample data
async fn do_fetch_station_sample() -> Result<()> {
    let stat = Uri::from("/iris/station_sample").get().await?;
    update_stat_sample(&stat);
    Ok(())
}

/// Add a `click` event listener to the map element
fn add_map_click_listener(elem: &Element) -> JsResult<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|ce: CustomEvent| match ce
        .detail()
        .dyn_into::<JsString>()
    {
        Ok(name) => {
            let name = String::from(name);
            if let Some(res) = resource_value() {
                spawn_local(do_future(select_card_map(res, name)));
            }
        }
        Err(e) => console::log_1(&format!("tmsevent: {e:?}").into()),
    });
    elem.add_event_listener_with_callback(
        "tmsevent",
        closure.as_ref().unchecked_ref(),
    )?;
    // can't drop closure, just forget it to make JS happy
    closure.forget();
    Ok(())
}

/// Select a card from a map marker click
async fn select_card_map(res: Res, name: String) -> Result<()> {
    let id = format!("{res}_{name}");
    fly_enable(JsValue::FALSE);
    click_card(res, name, id).await?;
    search_card_list().await
}

/// Add event source listener for notifications
fn add_eventsource_listener() {
    let es = match EventSource::new("/iris/api/notify") {
        Ok(es) => es,
        Err(e) => {
            set_notify_state(NotifyState::Starting);
            console::log_1(&format!("SSE /iris/api/notify: {e:?}").into());
            app::defer_action(DeferredAction::MakeEventSource, 5000);
            return;
        }
    };
    set_notify_state(NotifyState::Disconnected);
    let onopen: Closure<dyn Fn(_)> = Closure::new(|_e: Event| {
        set_notify_state(NotifyState::Connecting);
    });
    es.set_onopen(Some(onopen.as_ref().unchecked_ref()));
    onopen.forget();
    let onerror: Closure<dyn Fn(_)> = Closure::new(|_e: Event| {
        set_notify_state(NotifyState::Disconnected);
    });
    es.set_onerror(Some(onerror.as_ref().unchecked_ref()));
    onerror.forget();
    let onmessage: Closure<dyn Fn(_)> = Closure::new(|e: MessageEvent| {
        set_notify_state(NotifyState::Good);
        if let Ok(payload) = e.data().dyn_into::<JsString>() {
            spawn_local(handle_notify(String::from(payload)));
        }
    });
    es.set_onmessage(Some(onmessage.as_ref().unchecked_ref()));
    // can't drop closure, just forget it to make JS happy
    onmessage.forget();
}

/// Set refresh button text
fn set_notify_state(ns: NotifyState) {
    let sb_refresh = Doc::get().elem::<HtmlButtonElement>("sb_refresh");
    sb_refresh.set_inner_html(&ns.build_html());
    sb_refresh.set_disabled(ns.disabled());
}

/// Handle SSE notify from server
async fn handle_notify(payload: String) {
    let rname = resource_value().map_or("", |res| res.as_str());
    let (chan, _name) = match payload.split_once('$') {
        Some((a, b)) => (a, Some(b)),
        None => (payload.as_str(), None),
    };
    if chan != rname {
        console::log_1(&format!("unknown channel: {chan}").into());
        return;
    }
    set_notify_state(NotifyState::Updating);
    app::defer_action(DeferredAction::SetNotifyState(NotifyState::Good), 600);
    do_future(update_card_list()).await;
}

/// Update `sb_list` with changed result
async fn update_card_list() -> Result<()> {
    let Some(mut cards) = app::card_list(None) else {
        fetch_card_list().await?;
        return populate_card_list().await;
    };
    let json = cards.json();
    app::card_list(Some(cards));
    fetch_card_list().await?;
    let mut cards = app::card_list(None).unwrap();
    for (cv, html) in cards.changed_vec(json).await? {
        replace_card_html(&cv, &html);
    }
    update_item_states(&JsValue::from_str(cards.states_main()));
    app::card_list(Some(cards));
    search_card_list().await
}
