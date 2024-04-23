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
use crate::app::{self, DeferredAction};
use crate::card::{self, CardList, CardView, View};
use crate::error::{Error, Result};
use crate::fetch::Uri;
use crate::item::ItemState;
use crate::util::Doc;
use js_sys::JsString;
use resources::Res;
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
    add_eventsource_listener();
    fill_resource_select().await;
    Ok(())
}

/// Fill resource select element
async fn fill_resource_select() {
    let doc = Doc::get();
    let config = doc.input_bool("sb_config");
    match card::fetch_resource(config).await {
        Ok(perm) => {
            let sb_resource = doc.elem::<HtmlSelectElement>("sb_resource");
            sb_resource.set_inner_html(&perm);
            app::set_initialized();
        }
        Err(Error::FetchResponseUnauthorized()) => show_login(),
        Err(e) => {
            console::log_1(&format!("fill_resource_select: {e:?}").into());
        }
    }
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
    fill_resource_select().await;
    let sb_search = Doc::get().elem::<HtmlInputElement>("sb_search");
    sb_search.set_value("");
    handle_resource_change().await;
}

/// Handle change to selected resource type
async fn handle_resource_change() {
    let res = resource_value();
    app::card_list(None);
    let sb_state = Doc::get().elem::<HtmlSelectElement>("sb_state");
    sb_state.set_inner_html(card::item_states(res));
    fetch_card_list().await;
    populate_card_list().await;
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
async fn fetch_card_list() {
    match fetch_card_list_x().await {
        Ok(_) => (),
        Err(Error::FetchResponseUnauthorized()) => show_login(),
        Err(e) => show_toast(&format!("View failed: {e}")),
    }
}

/// Fetch card list for selected resource type
async fn fetch_card_list_x() -> Result<()> {
    let mut cards = app::card_list(None);
    if cards.is_none() {
        let res = resource_value();
        cards = res.map(|res| CardList::new(res));
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
async fn populate_card_list() {
    match populate_card_list_x().await {
        Ok(_) => (),
        Err(Error::FetchResponseUnauthorized()) => show_login(),
        Err(e) => show_toast(&format!("View failed: {e}")),
    }
}

/// Populate `sb_list` with selected resource type
async fn populate_card_list_x() -> Result<()> {
    app::set_selected_card(None);
    let search = search_value();
    let doc = Doc::get();
    let config = doc.input_bool("sb_config");
    let html = build_card_list(&search, config).await?;
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
async fn build_card_list(search: &str, config: bool) -> Result<String> {
    match app::card_list(None) {
        Some(mut cards) => {
            cards.filter(search).await?;
            let html = cards.to_html(config).await?;
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
            "sb_search" | "sb_state" => spawn_local(search_card_list()),
            "sb_resource" => handle_sb_resource_ev(),
            _ => {
                let cv = app::selected_card();
                if let Some(cv) = cv {
                    spawn_local(handle_input(cv, id));
                }
            }
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

/// Update `sb_list` with search result
async fn search_card_list() {
    // NOTE: we _could_ compare hidden items between old/new lists
    //       and update HTML elements using set_attribute / set_class_name,
    //       but this is fast enough and doesn't cause any UI problems
    populate_card_list().await;
}

/// Handle an event from `sb_resource` select element
fn handle_sb_resource_ev() {
    let sb_search = Doc::get().elem::<HtmlInputElement>("sb_search");
    sb_search.set_value("");
    spawn_local(handle_resource_change());
}

/// Handle an input event on selected card
async fn handle_input(cv: CardView, id: String) {
    match card::handle_input(&cv, &id).await {
        Ok(c) if c => (),
        _ => {
            console::log_1(&format!("unknown id: {id}").into());
        }
    }
}

/// Add a `click` event listener to an element
fn add_click_listener(elem: &Element) -> JsResult<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        let target = e.target().unwrap().dyn_into::<Element>().unwrap();
        if target.is_instance_of::<HtmlButtonElement>() {
            handle_button_click_ev(&target);
        } else if let Some(card) = target.closest(".card").unwrap_throw() {
            handle_card_click_ev(&card);
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
            let cv = app::selected_card();
            if let Some(cv) = cv {
                let attrs = ButtonAttrs {
                    id,
                    class_name: target.class_name(),
                    data_link: target.get_attribute("data-link"),
                    data_type: target.get_attribute("data-type"),
                };
                spawn_local(handle_button_card(cv, attrs));
            }
        }
    }
}

/// Handle button click event with selected card
async fn handle_button_card(cv: CardView, attrs: ButtonAttrs) {
    match attrs.id.as_str() {
        "ob_close" => replace_card(cv.compact()).await,
        "ob_delete" => handle_delete(cv).await,
        "ob_edit" => replace_card(cv.view(View::Edit)).await,
        "ob_loc" => replace_card(cv.view(View::Location)).await,
        "ob_save" => handle_save(cv).await,
        _ => {
            if attrs.class_name == "go_link" {
                go_resource(attrs).await;
            } else {
                handle_button_cv(cv, &attrs.id).await;
            }
        }
    }
}

/// Replace the selected card element with another card type
async fn replace_card(cv: CardView) {
    match card::fetch_one(&cv).await {
        Ok(html) => {
            replace_card_html(&cv, &html);
            if cv.view.is_compact() {
                app::set_selected_card(None);
            } else {
                app::set_selected_card(Some(cv));
            }
        }
        Err(Error::FetchResponseUnauthorized()) => show_login(),
        Err(e) => {
            show_toast(&format!("fetch failed: {e}"));
            // Card list may be out-of-date; refresh
            app::defer_action(DeferredAction::RefreshList, 200);
        }
    }
}

/// Replace a card with provieded HTML
fn replace_card_html(cv: &CardView, html: &str) {
    let Some(elem) = Doc::get().try_elem::<HtmlElement>(&cv.id()) else {
        return;
    };
    elem.set_inner_html(html);
    if cv.view.is_compact() {
        elem.set_class_name("card");
    } else {
        elem.set_class_name("form");
        let mut opt = ScrollIntoViewOptions::new();
        opt.behavior(ScrollBehavior::Smooth)
            .block(ScrollLogicalPosition::Nearest);
        elem.scroll_into_view_with_scroll_into_view_options(&opt);
    }
}

/// Handle delete button click
async fn handle_delete(cv: CardView) {
    if app::delete_enabled() {
        match card::delete_one(&cv).await {
            Ok(_) => app::defer_action(DeferredAction::RefreshList, 1000),
            Err(Error::FetchResponseUnauthorized()) => show_login(),
            Err(e) => show_toast(&format!("Delete failed: {e}")),
        }
    }
}

/// Handle save button click
async fn handle_save(cv: CardView) {
    let rs = match cv.view {
        View::Create => save_create(cv).await,
        View::Edit | View::Status(_) => save_edit(cv).await,
        View::Location => save_location(cv).await,
        _ => Ok(()),
    };
    match rs {
        Err(Error::FetchResponseUnauthorized()) => show_login(),
        Err(Error::FetchResponseNotFound()) => {
            // Card list out-of-date; refresh
            app::defer_action(DeferredAction::RefreshList, 200);
        }
        Err(e) => show_toast(&format!("Save failed: {e}")),
        _ => (),
    }
}

/// Save a create view card
async fn save_create(cv: CardView) -> Result<()> {
    card::create_and_post(cv.res).await?;
    replace_card(cv.view(View::CreateCompact)).await;
    app::defer_action(DeferredAction::RefreshList, 1500);
    Ok(())
}

/// Save an edit view card
async fn save_edit(cv: CardView) -> Result<()> {
    card::patch_changed(&cv).await?;
    replace_card(cv.view(View::Compact)).await;
    Ok(())
}

/// Save a location view card
async fn save_location(cv: CardView) -> Result<()> {
    if let Some(geo_loc) = card::fetch_geo_loc(&cv).await? {
        let lv = CardView::new(Res::GeoLoc, geo_loc, cv.view);
        card::patch_changed(&lv).await?;
        replace_card(cv.view(View::Compact)).await;
    }
    Ok(())
}

/// Handle a button click on selected card
async fn handle_button_cv(cv: CardView, id: &str) {
    match card::handle_click(&cv, id).await {
        Ok(c) if !c => {
            console::log_1(&format!("unknown button: {id}").into());
        }
        Ok(_c) => (),
        Err(e) => show_toast(&format!("click failed: {e}")),
    }
}

/// Handle a `click` event within a card element
fn handle_card_click_ev(card: &Element) {
    if let Some(id) = card.get_attribute("id") {
        if let Some(name) = card.get_attribute("name") {
            if let Some(res) = resource_value() {
                spawn_local(click_card(res, name, id));
            }
        }
    }
}

/// Handle a card click event
async fn click_card(res: Res, name: String, id: String) {
    deselect_card().await;
    // FIXME: check if id are the same for old/new cards
    let config = Doc::get().input_bool("sb_config");
    let mut cv = CardView::new(res, name, View::Status(config));
    if id.ends_with('_') {
        cv = cv.view(View::Create);
    }
    replace_card(cv).await;
}

/// Deselect the currently selected card
async fn deselect_card() {
    let cv = app::set_selected_card(None);
    if let Some(cv) = cv {
        if !cv.view.is_compact() {
            replace_card(cv.compact()).await;
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
                app::set_user(Some(user));
                if !app::initialized() {
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
        let sb_search = doc.elem::<HtmlInputElement>("sb_search");
        sb_search.set_value(&link);
        handle_resource_change().await;
    }
}

/// Handle refresh button click
async fn handle_refresh() {
    fetch_card_list().await;
    populate_card_list().await;
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
            DeferredAction::RefreshList => spawn_local(handle_refresh()),
            DeferredAction::HideToast => hide_toast(),
            DeferredAction::SetRefreshText(txt) => set_refresh_text(txt),
        }
    }
}

/// Add event source listener for notifications
fn add_eventsource_listener() {
    let es = match EventSource::new("/iris/api/notify") {
        Ok(es) => es,
        Err(e) => {
            set_refresh_text("⭮ ⚪");
            console::log_1(&format!("SSE /iris/api/notify: {e:?}").into());
            // FIXME: defer an action to try again in a couple seconds
            return;
        }
    };
    set_refresh_text("⭮ ⚫");
    let onopen: Closure<dyn Fn(_)> = Closure::new(|_e: Event| {
        set_refresh_text("⭮ 🟢");
    });
    es.set_onopen(Some(onopen.as_ref().unchecked_ref()));
    onopen.forget();
    let onerror: Closure<dyn Fn(_)> = Closure::new(|_e: Event| {
        set_refresh_text("⚫ ⭮ ");
    });
    es.set_onerror(Some(onerror.as_ref().unchecked_ref()));
    onerror.forget();
    let onmessage: Closure<dyn Fn(_)> = Closure::new(|e: MessageEvent| {
        if let Ok(payload) = e.data().dyn_into::<JsString>() {
            spawn_local(handle_notify(String::from(payload)));
        }
    });
    es.set_onmessage(Some(onmessage.as_ref().unchecked_ref()));
    // can't drop closure, just forget it to make JS happy
    onmessage.forget();
}

/// Set refresh button text
fn set_refresh_text(txt: &str) {
    let sb_refresh = Doc::get().elem::<Element>("sb_refresh");
    sb_refresh.set_inner_html(txt);
}

/// Handle SSE notify from server
async fn handle_notify(payload: String) {
    console::log_1(&format!("payload: {payload}").into());
    let rname = resource_value().map_or("", |res| res.as_str());
    let (chan, _name) = match payload.split_once('$') {
        Some((a, b)) => (a, Some(b)),
        None => (payload.as_str(), None),
    };
    if chan != rname {
        console::log_1(&format!("unknown channel: {chan}").into());
        return;
    }
    set_refresh_text("⭮ 🟡");
    app::defer_action(DeferredAction::SetRefreshText("⭮ 🟢"), 500);
    update_card_list().await;
}

/// Update `sb_list` with changed result
async fn update_card_list() {
    let Some(mut cards) = app::card_list(None) else {
        handle_refresh().await;
        return;
    };
    let json = cards.json();
    app::card_list(Some(cards));
    fetch_card_list().await;
    match update_card_list_x(json).await {
        Ok(_) => (),
        Err(Error::FetchResponseUnauthorized()) => show_login(),
        Err(e) => show_toast(&format!("View failed: {e}")),
    }
}

/// Update `sb_list` with changed result
async fn update_card_list_x(json: String) -> Result<()> {
    let cards = app::card_list(None).unwrap();
    let cv = app::selected_card();
    for (id, html) in cards.changed_vec(json, &cv).await? {
        console::log_1(&format!("changed: {id}").into());
        if let Some(elem) = Doc::get().try_elem::<HtmlElement>(&id) {
            elem.set_inner_html(&html);
            // TODO: change class / hidden attr
        };
    }
    app::card_list(Some(cards));
    Ok(())
}
