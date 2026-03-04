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
use crate::app::{self, DeferredAction};
use crate::card::{self, CardList, CardState};
use crate::error::{Error, Result};
use crate::fetch::Uri;
use crate::item::ItemState;
use crate::permission::Permission;
use crate::sse;
use crate::util::Doc;
use crate::view::{CardView, View};
use js_sys::JsString;
use resources::Res;
use std::error::Error as _;
use wasm_bindgen::JsCast;
use wasm_bindgen::prelude::*;
use wasm_bindgen_futures::spawn_local;
use web_sys::{
    CustomEvent, Element, Event, HtmlButtonElement, HtmlElement,
    HtmlInputElement, HtmlSelectElement, ScrollBehavior, ScrollIntoViewOptions,
    ScrollLogicalPosition, TransitionEvent, Window,
};

/// JavaScript result
pub type JsResult<T> = std::result::Result<T, JsValue>;

/// JavaScript imports
#[wasm_bindgen(module = "/res/glue.js")]
extern "C" {
    // Set the selected resource
    fn js_set_selected(res: &JsValue, name: &JsValue);
    // Update TMS main item states
    fn js_update_item_states(data: &JsValue);
    /// Update station data
    fn js_update_stat_sample(data: &JsValue);
    // Fly map to given item
    fn js_fly_map_to(fid: &JsValue, lat: &JsValue, lng: &JsValue);
    // Enable/disable flying map
    fn js_fly_enable(enable: JsValue);
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
    show_elem("sb_login");
}

/// Show an element
fn show_elem(id: &str) {
    Doc::get()
        .elem::<HtmlElement>(id)
        .set_class_name("show");
}

/// Hide an element
fn hide_elem(id: &str) {
    Doc::get()
        .elem::<HtmlElement>(id)
        .set_class_name("hidden");
}

/// Show a toast message
fn show_toast(msg: &str) {
    log::warn!("toast: {msg}");
    let t = Doc::get().elem::<HtmlElement>("sb_toast");
    t.set_inner_html(msg);
    t.set_class_name("show");
    app::defer_action(DeferredAction::HideToast, 3000);
}

/// Fly map to specified item
pub fn fly_map_item(fid: &str, lat: f64, lon: f64) {
    let fid = JsValue::from_str(fid);
    let lat = JsValue::from_f64(lat);
    let lon = JsValue::from_f64(lon);
    js_fly_map_to(&fid, &lat, &lon);
}

/// Application starting function
#[wasm_bindgen(start)]
pub async fn start() -> core::result::Result<(), JsError> {
    // this should be debug only
    console_error_panic_hook::set_once();
    wasm_log::init(wasm_log::Config::default().module_prefix("bulb"));
    log::info!("Started");
    add_listeners().await.unwrap_throw();
    Ok(())
}

/// Add event listeners
async fn add_listeners() -> JsResult<()> {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    let doc = Doc(doc);
    let divider: HtmlElement = doc.elem("divider");
    add_click_listener(&divider)?;
    let sidebar: HtmlElement = doc.elem("sidebar");
    add_change_listener(&sidebar)?;
    add_click_listener(&sidebar)?;
    add_input_listener(&sidebar)?;
    add_transition_listener(&doc.elem("sb_list"))?;
    add_interval_callback(&window)?;
    let map_pane: HtmlElement = doc.elem("map_pane");
    add_map_click_listener(&map_pane)?;
    sse::add_listener();
    do_future(finish_init()).await;
    fetch_station_sample();
    if let Some(doc_elem) = doc.doc_elem() {
        add_fullscreenchange_listener(&doc_elem)?;
    }
    Ok(())
}

/// Finish initialization
async fn finish_init() -> Result<()> {
    let user = Uri::from("/iris/api/login").get().await?;
    match user.as_string() {
        Some(user) => {
            app::set_user(Some(user));
            if !app::initialized() {
                update_sb_resource().await?;
                set_resource(None, "").await;
                sse::post_req(None).await;
                app::set_initialized();
            }
        }
        None => log::warn!("invalid user: {user:?}"),
    }
    Ok(())
}

/// Update resource select options
async fn update_sb_resource() -> Result<()> {
    let access: Vec<Permission> =
        Uri::from("/iris/api/access").get_val().await?;
    let doc = Doc::get();
    if let Some(elem) = doc.try_elem::<Element>("opt_action_plan") {
        elem.set_class_name(opt_class(&access, Res::ActionPlan));
    }
    if let Some(elem) = doc.try_elem::<Element>("opt_beacon") {
        elem.set_class_name(opt_class(&access, Res::Beacon));
    }
    if let Some(elem) = doc.try_elem::<Element>("opt_camera") {
        elem.set_class_name(opt_class(&access, Res::Camera));
    }
    if let Some(elem) = doc.try_elem::<Element>("opt_dms") {
        elem.set_class_name(opt_class(&access, Res::Dms));
    }
    if let Some(elem) = doc.try_elem::<Element>("opt_gate_arm") {
        elem.set_class_name(opt_class(&access, Res::GateArm));
    }
    if let Some(elem) = doc.try_elem::<Element>("opt_incident") {
        elem.set_class_name(opt_class(&access, Res::Incident));
    }
    if let Some(elem) = doc.try_elem::<Element>("opt_lcs") {
        elem.set_class_name(opt_class(&access, Res::Lcs));
    }
    if let Some(elem) = doc.try_elem::<Element>("opt_ramp_meter") {
        elem.set_class_name(opt_class(&access, Res::RampMeter));
    }
    if let Some(elem) = doc.try_elem::<Element>("opt_video_monitor") {
        elem.set_class_name(opt_class(&access, Res::VideoMonitor));
    }
    if let Some(elem) = doc.try_elem::<Element>("opt_weather_sensor") {
        elem.set_class_name(opt_class(&access, Res::WeatherSensor));
    }
    if let Some(elem) = doc.try_elem::<Element>("opt_controller") {
        elem.set_class_name(opt_class(&access, Res::Controller));
    }
    if let Some(elem) = doc.try_elem::<Element>("opt_detector") {
        elem.set_class_name(opt_class(&access, Res::Detector));
    }
    if let Some(elem) = doc.try_elem::<Element>("opt_permission") {
        elem.set_class_name(opt_class(&access, Res::Permission));
    }
    if let Some(elem) = doc.try_elem::<Element>("opt_system") {
        elem.set_class_name(opt_class(&access, Res::SystemAttribute));
    }
    if let Some(elem) = doc.try_elem::<Element>("opt_toll_zone") {
        elem.set_class_name(opt_class(&access, Res::TollZone));
    }
    Ok(())
}

/// Check for view access to a (base) resource name
fn opt_class(access: &[Permission], res: Res) -> &'static str {
    for perm in access {
        if perm.base_resource == res.as_str() {
            return "";
        }
    }
    "no-display"
}

/// Add a "fullscreenchange" event listener to an element
fn add_fullscreenchange_listener(elem: &Element) -> JsResult<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|_e: Event| {
        let doc = Doc::get();
        let btn = doc.elem::<HtmlInputElement>("sb_fullscreen");
        btn.set_checked(doc.is_fullscreen());
    });
    elem.add_event_listener_with_callback(
        "fullscreenchange",
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
        if id.as_str() == "sb_fullscreen" {
            set_fullscreen();
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

/// Set fullscreen mode
fn set_fullscreen() {
    let doc = Doc::get();
    let btn = doc.elem::<HtmlInputElement>("sb_fullscreen");
    let checked = btn.checked();
    doc.request_fullscreen(checked);
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
                log::warn!("source: {se}");
            }
            show_toast(&format!("Error: {e}"));
        }
    }
}

/// Get dependent resource row class name
fn row_class(show: bool) -> &'static str {
    if show { "sb_row_left" } else { "no-display" }
}

/// Handle change to selected resource type
async fn handle_resource_change(res: Option<Res>, search: &str) {
    let doc = Doc::get();
    let sidebar = doc.elem::<HtmlElement>("sidebar");
    sidebar.set_class_name("wait");
    let sb_list = doc.elem::<Element>("sb_list");
    sb_list.set_inner_html("");
    let base = res.map(|r| r.base());
    if let Some(elem) = doc.try_elem::<Element>("res_dms_row") {
        elem.set_class_name(row_class(base == Some(Res::Dms)));
    }
    if let Some(elem) = doc.try_elem::<Element>("res_lcs_row") {
        elem.set_class_name(row_class(base == Some(Res::Lcs)));
    }
    if let Some(elem) = doc.try_elem::<Element>("res_video_monitor_row") {
        elem.set_class_name(row_class(base == Some(Res::VideoMonitor)));
    }
    if let Some(elem) = doc.try_elem::<Element>("res_controller_row") {
        elem.set_class_name(row_class(base == Some(Res::Controller)));
    }
    if let Some(elem) = doc.try_elem::<Element>("res_permission_row") {
        elem.set_class_name(row_class(base == Some(Res::Permission)));
    }
    if let Some(elem) = doc.try_elem::<Element>("res_system_row") {
        elem.set_class_name(row_class(base == Some(Res::SystemAttribute)));
    }
    if let Some(res) = res {
        let id = format!("res_{}", res.as_str());
        if let Some(elem) = doc.try_elem::<HtmlInputElement>(&id) {
            elem.set_checked(true);
        }
    }
    let sb_search = doc.elem::<HtmlInputElement>("sb_search");
    sb_search.set_value(search);
    let sb_state = doc.elem::<HtmlSelectElement>("sb_state");
    let html = match res {
        Some(res) => card::item_states_html(res),
        None => String::new(),
    };
    sb_state.set_inner_html(&html);
    do_future(fetch_and_populate_cards(res)).await;
    // Turn off "wait" style
    sidebar.set_class_name("");
}

/// Get the selected resource value
fn selected_resource() -> Option<Res> {
    let doc = Doc::get();
    let rname = doc.select_parse::<String>("sb_resource");
    let res = Res::try_from(rname?.as_str()).ok()?;
    match res.base() {
        Res::Dms
            if doc.elem::<HtmlInputElement>("res_msg_pattern").checked() =>
        {
            Some(Res::MsgPattern)
        }
        Res::Dms
            if doc.elem::<HtmlInputElement>("res_sign_config").checked() =>
        {
            Some(Res::SignConfig)
        }
        Res::Dms if doc.elem::<HtmlInputElement>("res_word").checked() => {
            Some(Res::Word)
        }
        Res::Lcs if doc.elem::<HtmlInputElement>("res_lcs_state").checked() => {
            Some(Res::LcsState)
        }
        Res::VideoMonitor
            if doc.elem::<HtmlInputElement>("res_monitor_style").checked() =>
        {
            Some(Res::MonitorStyle)
        }
        Res::VideoMonitor => {
            match doc.try_elem::<HtmlInputElement>("res_flow_stream") {
                Some(input) => input.checked().then_some(Res::FlowStream),
                None => Some(res),
            }
        }
        Res::Controller
            if doc.elem::<HtmlInputElement>("res_comm_link").checked() =>
        {
            Some(Res::CommLink)
        }
        Res::Controller
            if doc.elem::<HtmlInputElement>("res_alarm").checked() =>
        {
            Some(Res::Alarm)
        }
        Res::Controller
            if doc.elem::<HtmlInputElement>("res_gps").checked() =>
        {
            Some(Res::Gps)
        }
        Res::Controller
            if doc.elem::<HtmlInputElement>("res_modem").checked() =>
        {
            Some(Res::Modem)
        }
        Res::SystemAttribute
            if doc.elem::<HtmlInputElement>("res_comm_config").checked() =>
        {
            Some(Res::CommConfig)
        }
        Res::SystemAttribute
            if doc.elem::<HtmlInputElement>("res_cabinet_style").checked() =>
        {
            Some(Res::CabinetStyle)
        }
        Res::Permission
            if doc.elem::<HtmlInputElement>("res_user").checked() =>
        {
            Some(Res::User)
        }
        Res::Permission
            if doc.elem::<HtmlInputElement>("res_role").checked() =>
        {
            Some(Res::Role)
        }
        Res::Permission
            if doc.elem::<HtmlInputElement>("res_domain").checked() =>
        {
            Some(Res::Domain)
        }
        _ => Some(res),
    }
}

/// Get value to search
fn search_value() -> String {
    let doc = Doc::get();
    let sb_search = doc.elem::<HtmlInputElement>("sb_search");
    let mut search = sb_search.value();
    if let Some(istate) = doc.select_parse::<String>("sb_state")
        && ItemState::from_code(&istate).is_some()
    {
        search.push(' ');
        search.push_str(&istate);
    }
    search
}

/// Add an "input" event listener to an element
fn add_input_listener(elem: &Element) -> JsResult<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        let target = e.target().unwrap().dyn_into::<Element>().unwrap();
        let id = target.id();
        match id.as_str() {
            "res_dms" | "res_msg_pattern" | "res_sign_config" | "res_word"
            | "res_lcs" | "res_lcs_state" | "res_video_monitor"
            | "res_monitor_style" | "res_flow_stream" | "res_controller"
            | "res_comm_link" | "res_alarm" | "res_gps" | "res_modem"
            | "res_system_attr" | "res_comm_config" | "res_cabinet_style"
            | "res_permission" | "res_user" | "res_role" | "res_domain"
            | "sb_resource" => handle_res_change(),
            "sb_search" | "sb_state" => spawn_local(do_future(handle_search())),
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

/// Handle resource change
fn handle_res_change() {
    let res = selected_resource();
    spawn_local(handle_resource_change(res, ""));
    spawn_local(sse::post_req(res));
}

/// Search card list for matching cards
async fn handle_search() -> Result<()> {
    match app::card_list(None) {
        Some(mut cards) => {
            if let Some(cv) = cards.expanded_view() {
                replace_card(cv.compact()).await?
            }
            let search = search_value();
            let doc = Doc::get();
            for cv in cards.search_views(&search).await? {
                let id = cv.id();
                if let Some(elem) = doc.try_elem::<Element>(&id) {
                    elem.set_class_name(cv.view.class_name());
                }
            }
            app::card_list(Some(cards));
        }
        None => log::warn!("search failed - no card list"),
    }
    Ok(())
}

/// Handle an event from `ob_view` select element
fn handle_ob_view_ev() {
    if let Some(cv) = app::expanded_view()
        && let Some(view) = ob_view_value()
    {
        spawn_local(do_future(replace_card(cv.view(view))));
    }
}

/// Get the selected view value
fn ob_view_value() -> Option<View> {
    match Doc::get().select_parse::<String>("ob_view") {
        Some(view) => View::try_from(view.as_str()).ok(),
        None => None,
    }
}

/// Handle an input event on an expanded card
async fn handle_input(id: String) -> Result<()> {
    if let Some(cv) = app::expanded_view() {
        cv.handle_input(id).await?;
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
        "show_sidebar" => spawn_local(handle_show_sidebar(true)),
        "hide_sidebar" => spawn_local(handle_show_sidebar(false)),
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

/// Handle a show/hide sidebar button click
async fn handle_show_sidebar(show: bool) {
    if show {
        show_elem("sidebar");
    } else {
        hide_elem("sidebar");
    }
}

/// Handle button click event on an epanded card
async fn handle_button_card(attrs: ButtonAttrs) {
    if let Some(cv) = app::expanded_view() {
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
    let html = cv.fetch_one().await?;
    replace_card_html(&cv, &html);
    app::set_view(cv);
    Ok(())
}

/// Replace a card with provided HTML
fn replace_card_html(cv: &CardView, html: &str) {
    let Some(elem) = Doc::get().try_elem::<HtmlElement>(&cv.id()) else {
        log::warn!("element {} not found", cv.id());
        return;
    };
    elem.set_inner_html(html);
    elem.set_class_name(cv.view.class_name());
    if cv.view.is_expanded() {
        let opt = ScrollIntoViewOptions::new();
        opt.set_behavior(ScrollBehavior::Smooth);
        opt.set_block(ScrollLogicalPosition::Nearest);
        elem.scroll_into_view_with_scroll_into_view_options(&opt);
    }
}

/// Handle delete button click
async fn handle_delete(cv: CardView) -> Result<()> {
    if app::delete_enabled() {
        cv.delete_one().await?;
        // FIXME: hide card view
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
    cv.create_and_post().await?;
    replace_card(cv.view(View::CreateCompact)).await
}

/// Save changed values on Setup / Location card
async fn save_changed(cv: CardView) -> Result<()> {
    cv.patch_changed().await?;
    replace_card(cv.view(View::Compact)).await
}

/// Handle a button click on an expanded card
async fn handle_button_cv(cv: CardView, id: String) {
    match cv.handle_click(id).await {
        Ok(_) => (),
        Err(e) => show_toast(&format!("click failed: {e}")),
    }
}

/// Handle a `click` event within a card element
fn handle_card_click_ev(elem: &Element) {
    if let Some(id) = elem.get_attribute("id")
        && let Some(name) = elem.get_attribute("data-name")
        && let Some(res) = selected_resource()
    {
        spawn_local(do_future(click_card(res, name, id)));
    }
}

/// Handle a card click event
async fn click_card(res: Res, name: String, id: String) -> Result<()> {
    if let Some(cv) = app::expanded_view() {
        replace_card(cv.compact()).await?;
    }
    // FIXME: check if id are the same for old/new cards
    let mut view = *card::res_views(res).get(1).unwrap_or(&View::Compact);
    if id.ends_with('_') {
        view = View::Create;
    }
    let cv = CardView::new(res, &name, view);
    replace_card(cv).await?;
    js_set_selected(
        &JsValue::from_str(res.as_str()),
        &JsValue::from_str(&name),
    );
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
                hide_elem("sb_login");
                do_future(finish_init()).await;
            }
            Err(e) => show_toast(&format!("Login failed: {e}")),
        }
    }
}

/// Go to resource from target's `data-link` attribute
async fn go_resource(attrs: ButtonAttrs) {
    if let (Some(link), Some(rname)) = (attrs.data_link, attrs.data_type)
        && let Ok(res) = Res::try_from(rname.as_str())
    {
        set_resource(Some(res), &link).await;
        sse::post_req(Some(res)).await;
    }
}

/// Set selected resource
async fn set_resource(res: Option<Res>, search: &str) {
    let doc = Doc::get();
    let sb_resource = doc.elem::<HtmlSelectElement>("sb_resource");
    let base = res.map(|r| r.base().as_str()).unwrap_or("");
    sb_resource.set_value(base);
    handle_resource_change(res, search).await;
}

/// Handle refresh full card list
async fn handle_refresh() {
    let res = selected_resource();
    do_future(fetch_and_populate_cards(res)).await;
}

/// Fetch and populate card list
async fn fetch_and_populate_cards(res: Option<Res>) -> Result<()> {
    match res {
        Some(res) => {
            let search = search_value();
            let mut cards = CardList::new(res);
            cards.fetch_all().await?;
            let html = cards.build_html(&search).await?;
            let doc = Doc::get();
            let sb_list = doc.elem::<Element>("sb_list");
            sb_list.set_inner_html(&html);
            app::card_list(Some(cards));
        }
        None => {
            app::card_list(None);
        }
    }
    Ok(())
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
    if let Some(target) = ev.target()
        && let Ok(target) = target.dyn_into::<Element>()
        && let Ok(ev) = ev.dyn_into::<TransitionEvent>()
    {
        // delete slider is a "left" property transition
        if target.id() == "ob_delete" && ev.property_name() == "left" {
            app::set_delete_enabled(&ev.type_() == "transitionend");
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
            DeferredAction::HideToast => hide_elem("sb_toast"),
            DeferredAction::RefreshList => spawn_local(handle_refresh()),
            DeferredAction::MakeEventSource => sse::add_listener(),
            DeferredAction::SetNotifyState(ns) => sse::set_notify_state(ns),
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
    js_update_stat_sample(&stat);
    Ok(())
}

/// Add a `click` event listener to the map element
fn add_map_click_listener(elem: &Element) -> JsResult<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|ce: CustomEvent| match ce
        .detail()
        .dyn_into::<JsString>()
    {
        Ok(name) => spawn_local(do_future(select_card_map(name.into()))),
        Err(e) => log::warn!("tmsevent: {e:?}"),
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
async fn select_card_map(name: String) -> Result<()> {
    if name.is_empty() {
        if let Some(cv) = app::expanded_view() {
            replace_card(cv.compact()).await?;
        }
        return Ok(());
    }
    let res = app::name_res(&name);
    let changed = res != selected_resource();
    if let Some(res) = res {
        if changed {
            set_resource(Some(res), "").await;
        }
        let id = format!("{res}_{name}");
        js_fly_enable(JsValue::FALSE);
        click_card(res, name, id).await?;
        js_fly_enable(JsValue::TRUE);
    }
    if changed {
        sse::post_req(res).await;
    }
    Ok(())
}

/// Handle SSE notification
pub fn handle_notification(chan: String, name: Option<String>) {
    spawn_local(do_future(do_handle_notification(chan, name)));
}

/// Handle SSE notification
async fn do_handle_notification(
    chan: String,
    _name: Option<String>,
) -> Result<()> {
    if let Some(res) = selected_resource()
        && res.as_str() == chan
    {
        update_card_list(res).await?;
    }
    if let Ok(res) = Res::try_from(chan.as_str())
        && res.has_location()
    {
        let mut cards = CardList::new(res);
        cards.fetch_all().await?;
        update_map_states(&cards).await?;
    }
    Ok(())
}

/// Update `sb_list` with changed result
async fn update_card_list(res: Res) -> Result<()> {
    let Some(old_cards) = app::card_list(None) else {
        return Ok(());
    };
    if old_cards.res() != res {
        js_set_selected(
            &JsValue::from_str(res.as_str()),
            &JsValue::from_str(""),
        );
        return Ok(());
    }
    let old_json = old_cards.json().to_string();
    let expanded = old_cards.expanded_view();
    app::card_list(Some(old_cards));
    let search = search_value();
    let mut cards = CardList::new(res).with_json(old_json);
    cards.fetch_all().await?;
    for (cv, html) in cards.changed_html(&search).await? {
        if let Some(ev) = &expanded
            && cv.name == ev.name
        {
            // FIXME: is user editing the form?
        } else {
            replace_card_html(&cv, &html);
        }
    }
    update_map_states(&cards).await?;
    if let Some(cv) = expanded {
        cards.set_view(cv);
    }
    app::card_list(Some(cards));
    Ok(())
}

/// Update map item states
async fn update_map_states(cards: &CardList) -> Result<()> {
    let items = cards.states_main().await?;
    let json = item_states_json(&items);
    app::set_resources(items);
    js_update_item_states(&json);
    Ok(())
}

/// Build item states JSON object
fn item_states_json(states: &[CardState]) -> JsValue {
    let mut json = String::new();
    json.push('{');
    for st in states {
        if json.len() > 1 {
            json.push(',');
        }
        json.push('"');
        json.push_str(&st.name);
        json.push_str("\":\"");
        json.push_str(st.state.code());
        json.push('"');
    }
    json.push('}');
    JsValue::from_str(&json)
}
