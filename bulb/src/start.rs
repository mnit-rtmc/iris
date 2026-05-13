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
use crate::asset::Asset;
use crate::card::{self, CardList, CardState};
use crate::eid;
use crate::error::{Error, Result};
use crate::fetch::Uri;
use crate::helper::spawn_future;
use crate::item::ItemState;
use crate::permission::Permission;
use crate::sse;
use crate::util::{self, Doc};
use crate::view::{CardView, View};
use chrono::{DateTime, Local};
use hatmil::css::{Prop, Rule, Sel};
use resources::Res;
use serde::Deserialize;
use std::collections::HashMap;
use std::time::Duration;
use wasm_bindgen::closure::Closure;
use wasm_bindgen::prelude::wasm_bindgen;
use wasm_bindgen::{JsCast, JsError};
use web_sys::{
    Element, Event, HtmlButtonElement, HtmlElement, HtmlInputElement,
    HtmlSelectElement, KeyboardEvent, MouseEvent, ScrollBehavior,
    ScrollIntoViewOptions, ScrollLogicalPosition, TransitionEvent, Window,
};

/// Layer groups
const GROUPS: &[&str] = &["tile", "tms"];

/// Rectangle X position
const RECT_X: f64 = 0.32;

/// Rectangle Y position
const RECT_Y: f64 = 0.5;

/// Binned station data
#[derive(Deserialize)]
struct StationData {
    /// Data collection time
    time_stamp: String,
    /// Binning period (s)
    #[allow(unused)]
    period: u32,
    /// Data samples
    samples: HashMap<String, [Option<u32>; 2]>,
}

/// Button attributes
struct ButtonAttrs {
    id: String,
    class_name: String,
    data_link: Option<String>,
    data_type: Option<String>,
}

/// Select item on map
pub fn select_item_map(res: Res, name: &str, lon: f64, lat: f64) {
    if !app::is_selected_item(res, name) {
        let zoom = selected_zoom(res).max(12);
        set_selected_item(res, name, zoom);
        spawn_future(do_select_item_map(zoom, lon, lat));
    }
}

/// Select item on map
async fn do_select_item_map(zoom: u32, lon: f64, lat: f64) -> Result<()> {
    if let Some(map_pane) = earthwyrm::MapPane::get() {
        map_pane.position(zoom, lon, lat, RECT_X, RECT_Y);
        Doc::get()
            .elem::<Element>("zoom-level")?
            .set_inner_html(&zoom.to_string());
        // FIXME: only call these when crossing zoom threshold
        update_map_states(Res::Incident, zoom, None).await?;
        update_map_states(Res::Dms, zoom, None).await?;
        update_map_states(Res::Lcs, zoom, None).await?;
        update_map_states(Res::Camera, zoom, None).await?;
        update_map_states(Res::RampMeter, zoom, None).await?;
        update_map_states(Res::Beacon, zoom, None).await?;
        update_map_states(Res::WeatherSensor, zoom, None).await?;
        update_map_states(Res::TagReader, zoom, None).await?;
        update_map_states(Res::Controller, zoom, None).await?;
        update_osm_style(zoom).await?;
    }
    Ok(())
}

/// Get zoom level for selected resource
fn selected_zoom(res: Res) -> u32 {
    let layer = format!("layer-{res}");
    Doc::get().input_parse::<u32>(&layer).unwrap_or(32)
}

/// Set selected item
fn set_selected_item(res: Res, name: &str, zoom: u32) {
    app::set_selected_item(res, name);
    set_selected_style(res, name, zoom);
}

/// Set selected item style
fn set_selected_style(res: Res, name: &str, zoom: u32) {
    if let Some(el) = Doc::get().opt_elem::<Element>("selected-style") {
        let sel = Sel::cls(format!("{}-{name}", res.as_str()));
        let prop = Prop::new().stroke("white").stroke_width(2);
        let mut css = Rule::new(sel, prop).to_string();
        let sel = Sel::cls("wyrm-tile").descendant(Sel::tp("use"));
        let prop = Prop::new().scale(zoom_scale(zoom));
        css.push_str(&Rule::new(sel, prop).to_string());
        el.set_inner_html(&css);
    }
}

/// Clear selected item style
fn clear_selected_style(zoom: u32) {
    if let Some(el) = Doc::get().opt_elem::<Element>("selected-style") {
        let sel = Sel::cls("wyrm-tile").descendant(Sel::tp("use"));
        let prop = Prop::new().scale(zoom_scale(zoom));
        let css = Rule::new(sel, prop).to_string();
        el.set_inner_html(&css);
    }
}

/// Get marker scale for a zoom level
fn zoom_scale(zoom: u32) -> &'static str {
    match zoom {
        1 => "0.006",
        2 => "0.012",
        3 => "0.025",
        4 => "0.05",
        5 => "0.1",
        6 => "0.2",
        7 => "0.3",
        8 => "0.4",
        9 => "0.5",
        10 => "0.6",
        11 => "0.8",
        _ => "1.0",
    }
}

/// Clear selected item
fn clear_selected_item(zoom: u32) {
    app::clear_selected_item();
    clear_selected_style(zoom);
}

/// Application starting function
#[wasm_bindgen(start)]
pub async fn start() -> core::result::Result<(), JsError> {
    crate::panic::set_hook_once();
    wasm_log::init(wasm_log::Config::default());
    log::info!("Started");
    Ok(add_listeners()?)
}

/// Add event listeners
fn add_listeners() -> Result<()> {
    let window = web_sys::window().ok_or(Error::NoWindow())?;
    let doc = window.document().ok_or(Error::NoDocument())?;
    let doc = Doc(doc);
    let resource = doc.elem::<HtmlSelectElement>(eid::RESOURCE)?;
    resource.set_value("");
    let divider: HtmlElement = doc.elem("divider")?;
    add_click_listener(&divider)?;
    let sidebar: HtmlElement = doc.elem("sidebar")?;
    add_change_listener(&sidebar)?;
    let layer_menu: HtmlElement = doc.elem("layer-menu")?;
    add_change_listener(&layer_menu)?;
    add_click_listener(&sidebar)?;
    add_mouse_listener(&sidebar)?;
    add_input_listener(&sidebar)?;
    add_input_enter_listener(&doc.elem("login_pass")?)?;
    add_focus_listener(&sidebar)?;
    add_transition_listener(&doc.elem(eid::LIST)?)?;
    add_interval_callback(&window)?;
    if let Some(map_pane) = earthwyrm::MapPane::init(
        "map-pane",
        GROUPS,
        handle_map_click_ev,
        handle_map_zoom,
    ) {
        map_pane.position(10, -93.2, 44.95, RECT_X, RECT_Y);
        doc.elem::<Element>("zoom-level")?.set_inner_html("10");
        clear_selected_item(10);
    }
    spawn_future(finish_init());
    fetch_station_data();
    if let Some(doc_elem) = doc.doc_elem() {
        add_fullscreenchange_listener(&doc_elem)?;
    }
    Ok(())
}

/// Finish initialization
async fn finish_init() -> Result<()> {
    sse::add_listener();
    let user = Uri::from("/iris/api/login").get().await?;
    match user.as_string() {
        Some(user) => {
            app::set_user(Some(user));
            update_resource().await?;
            set_resource(None, "").await?;
            sse::post_req(None).await
        }
        None => {
            log::warn!("invalid user: {user:?}");
            Ok(())
        }
    }
}

/// Update resource select options
async fn update_resource() -> Result<()> {
    let access: Vec<Permission> = Asset::Access.uri().get_val().await?;
    let doc = Doc::get();
    if let Some(el) = doc.opt_elem::<Element>("opt_action_plan") {
        el.set_class_name(opt_class(&access, Res::ActionPlan));
    }
    if let Some(el) = doc.opt_elem::<Element>("opt_beacon") {
        el.set_class_name(opt_class(&access, Res::Beacon));
    }
    if let Some(el) = doc.opt_elem::<Element>("opt_camera") {
        el.set_class_name(opt_class(&access, Res::Camera));
    }
    if let Some(el) = doc.opt_elem::<Element>("opt_dms") {
        el.set_class_name(opt_class(&access, Res::Dms));
    }
    if let Some(el) = doc.opt_elem::<Element>("opt_gate_arm") {
        el.set_class_name(opt_class(&access, Res::GateArm));
    }
    if let Some(el) = doc.opt_elem::<Element>("opt_incident") {
        el.set_class_name(opt_class(&access, Res::Incident));
    }
    if let Some(el) = doc.opt_elem::<Element>("opt_lcs") {
        el.set_class_name(opt_class(&access, Res::Lcs));
    }
    if let Some(el) = doc.opt_elem::<Element>("opt_ramp_meter") {
        el.set_class_name(opt_class(&access, Res::RampMeter));
    }
    if let Some(el) = doc.opt_elem::<Element>("opt_video_monitor") {
        el.set_class_name(opt_class(&access, Res::VideoMonitor));
    }
    if let Some(el) = doc.opt_elem::<Element>("opt_weather_sensor") {
        el.set_class_name(opt_class(&access, Res::WeatherSensor));
    }
    if let Some(el) = doc.opt_elem::<Element>("opt_comm") {
        el.set_class_name(opt_class(&access, Res::CommConfig));
    }
    if let Some(el) = doc.opt_elem::<Element>("opt_road") {
        el.set_class_name(opt_class(&access, Res::Road));
    }
    if let Some(el) = doc.opt_elem::<Element>("opt_permission") {
        el.set_class_name(opt_class(&access, Res::Permission));
    }
    if let Some(el) = doc.opt_elem::<Element>("opt_system") {
        el.set_class_name(opt_class(&access, Res::SystemAttribute));
    }
    if let Some(el) = doc.opt_elem::<Element>("opt_toll_zone") {
        el.set_class_name(opt_class(&access, Res::TollZone));
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
fn add_fullscreenchange_listener(el: &Element) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|_e: Event| {
        let doc = Doc::get();
        if let Some(btn) = doc.opt_elem::<HtmlInputElement>("sb_fullscreen") {
            btn.set_checked(doc.is_fullscreen());
        }
    });
    el.add_event_listener_with_callback(
        "fullscreenchange",
        closure.as_ref().unchecked_ref(),
    )?;
    // can't drop closure, just forget it to make JS happy
    closure.forget();
    Ok(())
}

/// Add a "change" event listener to an element
fn add_change_listener(el: &Element) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        if let Some(Ok(target)) = e.target().map(|e| e.dyn_into::<Element>()) {
            let id = target.id();
            if id == "sb_fullscreen" {
                set_fullscreen();
            } else {
                spawn_future(handle_layer_zoom(id));
            }
        }
    });
    el.add_event_listener_with_callback(
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
    let checked = doc.input_bool("sb_fullscreen");
    doc.request_fullscreen(checked);
}

/// Handle layer zoom threshold change
async fn handle_layer_zoom(id: String) -> Result<()> {
    if let Some((layer, rname)) = id.split_once('-')
        && layer == "layer"
        && let Ok(res) = Res::try_from(rname)
    {
        let zoom = current_zoom();
        // FIXME: only call these when crossing zoom threshold
        update_map_states(res, zoom, None).await?;
    }
    Ok(())
}

/// Get current map zoom level
fn current_zoom() -> u32 {
    earthwyrm::MapPane::get().map(|mp| mp.zoom()).unwrap_or(0)
}

/// Get dependent resource row class name
fn row_class(show: bool) -> &'static str {
    if show { "sb_row_left" } else { "no-display" }
}

/// Handle change to selected resource type
async fn handle_resource_change(res: Option<Res>, search: &str) -> Result<()> {
    let doc = Doc::get();
    let sidebar = doc.elem::<HtmlElement>("sidebar")?;
    sidebar.set_class_name("wait");
    let sb_list = doc.elem::<Element>(eid::LIST)?;
    sb_list.set_inner_html("");
    let base = res.map(|r| r.base());
    if let Some(el) = doc.opt_elem::<Element>("res_plan_row") {
        el.set_class_name(row_class(base == Some(Res::ActionPlan)));
    }
    if let Some(el) = doc.opt_elem::<Element>("res_dms_row") {
        el.set_class_name(row_class(base == Some(Res::Dms)));
    }
    if let Some(el) = doc.opt_elem::<Element>("res_lcs_row") {
        el.set_class_name(row_class(base == Some(Res::Lcs)));
    }
    if let Some(el) = doc.opt_elem::<Element>("res_video_monitor_row") {
        el.set_class_name(row_class(base == Some(Res::VideoMonitor)));
    }
    if let Some(el) = doc.opt_elem::<Element>("res_comm_row") {
        el.set_class_name(row_class(base == Some(Res::CommConfig)));
    }
    if let Some(el) = doc.opt_elem::<Element>("res_road_row") {
        el.set_class_name(row_class(base == Some(Res::Road)));
    }
    if let Some(el) = doc.opt_elem::<Element>("res_permission_row") {
        el.set_class_name(row_class(base == Some(Res::Permission)));
    }
    if let Some(el) = doc.opt_elem::<Element>("res_system_row") {
        el.set_class_name(row_class(base == Some(Res::SystemAttribute)));
    }
    if let Some(res) = res {
        let id = format!("res_{}", res.as_str());
        if let Some(el) = doc.opt_elem::<HtmlInputElement>(&id) {
            el.set_checked(true);
        }
    }
    let sb_search = doc.elem::<HtmlInputElement>(eid::SEARCH)?;
    sb_search.set_value(search);
    let sb_state = doc.elem::<HtmlSelectElement>(eid::STATE)?;
    let html = match res {
        Some(res) => card::item_states_html(res),
        None => String::new(),
    };
    sb_state.set_inner_html(&html);
    let res = fetch_and_populate_cards(res).await;
    // Turn off "wait" style
    sidebar.set_class_name("");
    res
}

/// Get the selected resource value
fn selected_resource() -> Option<Res> {
    let doc = Doc::get();
    let rname = doc.select_parse::<String>(eid::RESOURCE);
    let res = Res::try_from(rname?.as_str()).ok()?;
    match res.base() {
        Res::ActionPlan if doc.input_bool("res_plan_phase") => {
            Some(Res::PlanPhase)
        }
        Res::Dms if doc.input_bool("res_msg_pattern") => Some(Res::MsgPattern),
        Res::Dms if doc.input_bool("res_sign_config") => Some(Res::SignConfig),
        Res::Dms if doc.input_bool("res_word") => Some(Res::Word),
        Res::Lcs if doc.input_bool("res_lcs_state") => Some(Res::LcsState),
        Res::VideoMonitor if doc.input_bool("res_monitor_style") => {
            Some(Res::MonitorStyle)
        }
        Res::VideoMonitor if doc.input_bool("res_flow_stream") => {
            Some(Res::FlowStream)
        }
        Res::CommConfig if doc.input_bool("res_comm_link") => {
            Some(Res::CommLink)
        }
        Res::CommConfig if doc.input_bool("res_controller") => {
            Some(Res::Controller)
        }
        Res::CommConfig if doc.input_bool("res_alarm") => Some(Res::Alarm),
        Res::CommConfig if doc.input_bool("res_gps") => Some(Res::Gps),
        //Res::Road if doc.input_bool("res_r_node") => Some(Res::Rnode),
        Res::Road if doc.input_bool("res_detector") => Some(Res::Detector),
        Res::Road if doc.input_bool("res_map_extent") => Some(Res::MapExtent),
        Res::Permission if doc.input_bool("res_user") => Some(Res::User),
        Res::Permission if doc.input_bool("res_role") => Some(Res::Role),
        Res::Permission if doc.input_bool("res_domain") => Some(Res::Domain),
        Res::Permission => None, // no permission cards
        Res::SystemAttribute if doc.input_bool("res_event_config") => {
            Some(Res::EventConfig)
        }
        Res::SystemAttribute if doc.input_bool("res_cabinet_style") => {
            Some(Res::CabinetStyle)
        }
        _ => Some(res),
    }
}

/// Get value to search
fn search_value() -> Result<String> {
    let doc = Doc::get();
    let sb_search = doc.elem::<HtmlInputElement>(eid::SEARCH)?;
    let mut search = sb_search.value();
    if let Some(istate) = doc.select_parse::<String>(eid::STATE)
        && ItemState::from_code(&istate).is_some()
    {
        search.push(' ');
        search.push_str(&istate);
    }
    Ok(search)
}

/// Add an "input" event listener to an element
fn add_input_listener(el: &Element) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        if let Some(Ok(target)) = e.target().map(|e| e.dyn_into::<Element>()) {
            handle_input(target.id());
        }
    });
    el.add_event_listener_with_callback(
        "input",
        closure.as_ref().unchecked_ref(),
    )?;
    // can't drop closure, just forget it to make JS happy
    closure.forget();
    Ok(())
}

/// Handle an input event
fn handle_input(id: String) {
    match id.as_str() {
        "res_action_plan"
        | "res_plan_phase"
        | "res_day_plan"
        | "res_device_action"
        | "res_dms"
        | "res_msg_pattern"
        | "res_sign_config"
        | "res_word"
        | "res_lcs"
        | "res_lcs_state"
        | "res_video_monitor"
        | "res_monitor_style"
        | "res_flow_stream"
        | "res_comm_config"
        | "res_alarm"
        | "res_comm_link"
        | "res_controller"
        | "res_gps"
        | "res_road"
        | "res_detector"
        | "res_map_extent"
        | "res_r_node"
        | "res_permission"
        | "res_user"
        | "res_role"
        | "res_domain"
        | "res_system_attr"
        | "res_event_config"
        | "res_cabinet_style"
        | eid::RESOURCE => handle_res_change(),
        eid::SEARCH | eid::STATE => spawn_future(handle_search()),
        eid::VIEW => handle_card_view_ev(),
        _ => spawn_future(handle_input_other(id)),
    }
}

/// Handle resource change
fn handle_res_change() {
    let res = selected_resource();
    spawn_future(handle_resource_change(res, ""));
    spawn_future(sse::post_req(res));
}

/// Search card list for matching cards
async fn handle_search() -> Result<()> {
    match app::card_list(None) {
        Some(mut cards) => {
            let search = search_value()?;
            if let Some(cv) = cards.expanded_view() {
                replace_card(cv.compact(), &search).await?
            }
            let doc = Doc::get();
            for cv in cards.search_views(&search).await? {
                let id = cv.id();
                if let Some(el) = doc.opt_elem::<Element>(&id) {
                    el.set_class_name(cv.view.class_name());
                }
            }
            app::card_list(Some(cards));
        }
        None => log::warn!("search failed - no card list"),
    }
    Ok(())
}

/// Handle an event from card view select element
fn handle_card_view_ev() {
    if let Some(cv) = app::expanded_view()
        && let Some(view) = card_view_value()
    {
        spawn_future(replace_card(cv.view(view), ""));
    }
}

/// Get the selected view value
fn card_view_value() -> Option<View> {
    match Doc::get().select_parse::<String>(eid::VIEW) {
        Some(view) => View::try_from(view.as_str()).ok(),
        None => None,
    }
}

/// Handle an input event on an expanded card
async fn handle_input_other(id: String) -> Result<()> {
    if let Some(cv) = app::expanded_view() {
        cv.handle_input(&id).await?;
    }
    Ok(())
}

/// Add "focusin" / "focusout" event listeners to an element
fn add_focus_listener(el: &Element) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        if let Some(Ok(input)) =
            e.target().map(|e| e.dyn_into::<HtmlInputElement>())
        {
            spawn_future(handle_focus_events(input, e.type_()));
        }
    });
    el.add_event_listener_with_callback(
        "focusin",
        closure.as_ref().unchecked_ref(),
    )?;
    el.add_event_listener_with_callback(
        "focusout",
        closure.as_ref().unchecked_ref(),
    )?;
    // can't drop closure, just forget it to make JS happy
    closure.forget();
    Ok(())
}

/// Handle focusin / focusout events
async fn handle_focus_events(
    input: HtmlInputElement,
    tp: String,
) -> Result<()> {
    let id = input.id();
    // DMS message composer line input
    if id.as_str().starts_with("mc_line") {
        match tp.as_str() {
            "focusin" => input.set_value(""),
            "focusout" => {
                if input.value().is_empty()
                    && let Some(ms) = input.get_attribute("data-cur")
                {
                    input.set_value(&ms);
                    handle_input_other(id).await?;
                }
            }
            _ => (),
        }
    }
    Ok(())
}

/// Add a `click` event listener to an element
fn add_click_listener(el: &Element) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        if let Some(Ok(target)) = e.target().map(|e| e.dyn_into::<Element>()) {
            if target.is_instance_of::<HtmlButtonElement>() {
                handle_button_click_ev(&target);
            } else if let Ok(Some(cc)) = target.closest(".card-compact") {
                handle_card_click_ev(&cc);
            }
        }
    });
    el.add_event_listener_with_callback(
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
        eid::LOGIN => spawn_future(handle_login()),
        eid::LOGOUT => spawn_future(handle_logout()),
        "show_sidebar" => spawn_future(handle_show_sidebar(true)),
        "hide_sidebar" => spawn_future(handle_show_sidebar(false)),
        // handled by mouse event listener, prevent click:
        "ptz-pan-left"|"ptz-pan-right"
            |"ptz-tilt-up"|"ptz-tilt-down"
            |"ptz-zoom-in"|"ptz-zoom-out"
            |"focus-near"|"focus-far"
            |"iris-open"|"iris-close" => (),
        _ => {
            let attrs = ButtonAttrs {
                id,
                class_name: target.class_name(),
                data_link: target.get_attribute("data-link"),
                data_type: target.get_attribute("data-type"),
            };
            spawn_future(handle_button_card(attrs));
        }
    }
}

/// Handle a show/hide sidebar button click
async fn handle_show_sidebar(show: bool) -> Result<()> {
    let doc = Doc::get();
    if let Some(btn) = doc.opt_elem::<HtmlButtonElement>("show_sidebar") {
        btn.set_disabled(show);
    }
    if let Some(btn) = doc.opt_elem::<HtmlButtonElement>("hide_sidebar") {
        btn.set_disabled(!show);
    }
    if show {
        util::show_elem("sidebar");
    } else {
        util::hide_elem("sidebar");
    }
    Ok(())
}

/// Add enter/submit event listener to an element
fn add_input_enter_listener(elem: &Element) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        if let (Some(Ok(target)), Ok(keydown_ev)) = (
            e.target().map(|e| e.dyn_into::<Element>()),
            e.dyn_into::<KeyboardEvent>(),
        ) && keydown_ev.key().as_str() == "Enter"
        {
            handle_input_enter(target.id());
        }
    });
    elem.add_event_listener_with_callback(
        "keydown",
        closure.as_ref().unchecked_ref(),
    )?;
    closure.forget();
    Ok(())
}

/// Handle an input enter/submit event
fn handle_input_enter(id: String) {
    if id.as_str() == "login_pass" {
        spawn_future(handle_login());
    }
}

/// Add a mouse event listener to an element
fn add_mouse_listener(el: &Element) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        if let Ok(mouse_event) = e.dyn_into::<MouseEvent>()
        && mouse_event.button() == 0
        && let Some(Ok(target)) = mouse_event.target().map(|e| e.dyn_into::<Element>()) {
            handle_mouse_ev(&target, &mouse_event.type_() == "mousedown");
        }
    });
    el.add_event_listener_with_callback(
        "mousedown",
        closure.as_ref().unchecked_ref(),
    )?;
    el.add_event_listener_with_callback(
        "mouseup",
        closure.as_ref().unchecked_ref(),
    )?;
    closure.forget();
    Ok(())
}

/// Handle a mouse event
fn handle_mouse_ev(target: &Element, mouse_down: bool) {
    let mut id = target.id();
    let mut parts = id.split("-");
    id = match (parts.next(), parts.next()) {
        // focus/iris auto buttons are on click, not mousedown/up
        (_, Some("auto")) => String::new(),
        (Some("focus"), _)
            |(Some("iris"), _)
            |(Some("ptz"), _)
            |(Some("publish"), _) => id,
        _ => String::new(),
    };
    spawn_future(handle_mouse_card(id, mouse_down));
}

/// Handle a mouse event on an expanded card
async fn handle_mouse_card(id: String, mouse_down: bool) -> Result<()> {
    if let Some(cv) = app::expanded_view() {
        match id.as_str() {
            // mouse on invalid target, so always release mouse
            "" => cv.handle_mouse(id.as_str(), false).await?,
            _ => cv.handle_mouse(id.as_str(), mouse_down).await?,
        }
    }
    Ok(())
}

/// Handle button click event on an expanded card
async fn handle_button_card(attrs: ButtonAttrs) -> Result<()> {
    if let Some(cv) = app::expanded_view() {
        if attrs.class_name == "go_link" {
            go_resource(attrs).await?;
        } else if eid::DELETE == attrs.id {
            if app::delete_enabled() {
                cv.handle_delete().await?;
                replace_card(cv.view(View::Hidden), "").await?;
            }
        } else if let Some(v) = cv.handle_click(&attrs.id).await? {
            replace_card(cv.view(v), "").await?;
        }
    }
    Ok(())
}

/// Replace a card view element with another view
async fn replace_card(mut cv: CardView, search: &str) -> Result<()> {
    let html = cv.fetch_one(search).await?;
    replace_card_html(&cv, &html);
    app::set_view(cv);
    Ok(())
}

/// Replace a card with provided HTML
fn replace_card_html(cv: &CardView, html: &str) {
    let Some(el) = Doc::get().opt_elem::<HtmlElement>(&cv.id()) else {
        log::warn!("element {} not found", cv.id());
        return;
    };
    el.set_inner_html(html);
    el.set_class_name(cv.view.class_name());
    if cv.view.is_expanded() {
        let opt = ScrollIntoViewOptions::new();
        opt.set_behavior(ScrollBehavior::Smooth);
        opt.set_block(ScrollLogicalPosition::Nearest);
        el.scroll_into_view_with_scroll_into_view_options(&opt);
    }
}

/// Handle a `click` event within a card element
fn handle_card_click_ev(el: &Element) {
    if let Some(id) = el.get_attribute("id")
        && let Some(name) = el.get_attribute("data-name")
        && let Some(res) = selected_resource()
    {
        spawn_future(click_card(res, name, id));
    }
}

/// Handle a card click event
async fn click_card(res: Res, name: String, id: String) -> Result<()> {
    if let Some(cv) = app::expanded_view() {
        let search = search_value()?;
        replace_card(cv.compact(), &search).await?;
    }
    // Expand to the second view (1) for this resource
    let mut view = *card::res_views(res).get(1).unwrap_or(&View::Compact);
    if id.ends_with('_') && id.len() == res.as_str().len() + 1 {
        view = View::Create;
    }
    let cv = CardView::new(res, &name, view);
    replace_card(cv, "").await?;
    Ok(())
}

/// Handle login button press
async fn handle_login() -> Result<()> {
    let window = web_sys::window().ok_or(Error::NoWindow())?;
    let doc = window.document().ok_or(Error::NoDocument())?;
    let doc = Doc(doc);
    if let (Some(user), Some(pass)) = (
        doc.input_parse::<String>("login_user"),
        doc.input_parse::<String>("login_pass"),
    ) {
        let loading_bar = doc.opt_elem::<HtmlElement>("ob_login_loading_bar");
        if let Some(l) = &loading_bar {
            l.set_class_name("loading_bar active")
        }
        let uri = Uri::from("/iris/api/login");
        let js = format!("{{\"username\":\"{user}\",\"password\":\"{pass}\"}}");
        let el = doc.elem::<HtmlInputElement>("login_pass")?;
        el.set_value("");
        util::hide_elem("sb_auth_panel");
        uri.post(&js.into()).await?;
        // hide/deactivate loading bar
        if let Some(l) = &loading_bar {
            l.set_class_name("loading_bar")
        }
        finish_init().await
    } else {
        Ok(())
    }
}

/// Handle logout button press
async fn handle_logout() -> Result<()> {
    let uri = Uri::from("/iris/api/login");
    uri.delete().await?;
    Ok(())
}

/// Go to resource from target's `data-link` attribute
async fn go_resource(attrs: ButtonAttrs) -> Result<()> {
    if let (Some(link), Some(rname)) = (attrs.data_link, attrs.data_type)
        && let Ok(res) = Res::try_from(rname.as_str())
    {
        set_resource(Some(res), &link).await?;
        sse::post_req(Some(res)).await
    } else {
        Ok(())
    }
}

/// Set selected resource
async fn set_resource(res: Option<Res>, search: &str) -> Result<()> {
    let resource = Doc::get().elem::<HtmlSelectElement>(eid::RESOURCE)?;
    let base = res.map(|r| r.base().as_str()).unwrap_or("");
    resource.set_value(base);
    handle_resource_change(res, search).await
}

/// Fetch and populate card list
async fn fetch_and_populate_cards(res: Option<Res>) -> Result<()> {
    match res {
        Some(res) => {
            let access: Vec<Permission> = Asset::Access.uri().get_val().await?;
            let mut cards = CardList::new(res, &access);
            cards.fetch_all().await?;
            let search = search_value()?;
            let html = cards.build_html(&search).await?;
            let doc = Doc::get();
            let sb_list = doc.elem::<Element>(eid::LIST)?;
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
fn add_transition_listener(el: &Element) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(handle_transition_ev);
    el.add_event_listener_with_callback(
        "transitionstart",
        closure.as_ref().unchecked_ref(),
    )?;
    el.add_event_listener_with_callback(
        "transitioncancel",
        closure.as_ref().unchecked_ref(),
    )?;
    el.add_event_listener_with_callback(
        "transitionend",
        closure.as_ref().unchecked_ref(),
    )?;
    closure.forget();
    Ok(())
}

/// Handle a `transition*` event
fn handle_transition_ev(ev: Event) {
    if let Some(target) = ev.target()
        && let Ok(target) = target.dyn_into::<Element>()
        && let Ok(ev) = ev.dyn_into::<TransitionEvent>()
    {
        // delete slider is a "left" property transition
        if target.id() == eid::DELETE && ev.property_name() == "left" {
            app::set_delete_enabled(&ev.type_() == "transitionend");
        }
    }
}

/// Add callback for regular interval checks
fn add_interval_callback(window: &Window) -> Result<()> {
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
            DeferredAction::FetchStationData => fetch_station_data(),
            DeferredAction::HideToast => util::hide_elem("sb_toast"),
            DeferredAction::RefreshList => handle_res_change(),
            DeferredAction::MakeEventSource => sse::add_listener(),
            DeferredAction::SetNotifyState(ns) => sse::set_notify_state(ns),
        }
    }
}

/// Fetch binned station data
fn fetch_station_data() {
    log::debug!("fetch_station_data");
    app::defer_action(DeferredAction::FetchStationData, 30_000);
    spawn_future(do_fetch_station_data());
}

/// Actually fetch binned station data
async fn do_fetch_station_data() -> Result<()> {
    if let Some(el) = Doc::get().opt_elem::<Element>("segment-style") {
        let data = StationData::fetch().await?;
        let css = data.make_style();
        el.set_inner_html(&css);
    }
    Ok(())
}

impl StationData {
    /// Fetch current station data
    async fn fetch() -> Result<Self> {
        let stat = Uri::from("/iris/station_sample").get().await?;
        Ok(serde_wasm_bindgen::from_value(stat)?)
    }

    /// Make station segment style
    fn make_style(&self) -> String {
        let now: DateTime<Local> = Local::now();
        let oldest = now - Duration::from_secs(300);
        match DateTime::parse_from_rfc3339(&self.time_stamp) {
            Ok(dt) if dt > oldest && dt < now => self.do_make_style(),
            _ => {
                log::warn!("bad station_sample timestamp: {}", self.time_stamp);
                String::new()
            }
        }
    }

    /// Make station segment style
    fn do_make_style(&self) -> String {
        let len = self.samples.len();
        let mut style = String::with_capacity(32 * (len + 1));
        style.push_str(".wyrm-segment { fill: #aaa; }\n");
        for (sid, data) in &self.samples {
            let flow = data.first();
            let speed = data.get(1);
            if let (Some(Some(fl)), Some(Some(sp))) = (flow, speed) {
                let density = ((*fl as f32) / (*sp as f32)).round() as u32;
                style.push_str(".segment-");
                style.push_str(sid);
                style.push_str(" { fill: ");
                style.push_str(density_color(density));
                style.push_str("; }\n");
            }
        }
        style
    }
}

/// Get color based on density (veh/mi)
fn density_color(density: u32) -> &'static str {
    match density {
        0 => "#aaa",
        1..30 => "#2c2",
        30..50 => "#fc0",
        50..200 => "#d00",
        200.. => "#c0f",
    }
}

/// Handle a `click` event
fn handle_map_click_ev(ev: Event) {
    // Is it within a map `g` or `path` element
    if let Some(Ok(target)) = ev.target().map(|e| e.dyn_into::<Element>())
        && let Ok(Some(gm)) = target.closest("g,path")
        && let Some(cls) = gm.get_attribute("class")
        && let Some((rname, nm)) = cls.split_once('-')
    {
        let res = Res::try_from(rname).ok();
        spawn_future(select_card_map(res, nm.to_string()));
    }
}

/// Select a card from a map marker click
async fn select_card_map(res: Option<Res>, name: String) -> Result<()> {
    let clear = name.is_empty()
        || match (res, &name) {
            (Some(res), name) => app::is_selected_item(res, name),
            (None, _name) => true,
        };
    if clear {
        clear_selected_item(current_zoom());
        if let Some(cv) = app::expanded_view() {
            let search = search_value()?;
            replace_card(cv.compact(), &search).await?;
        }
        return Ok(());
    }
    let changed = res != selected_resource();
    if let Some(res) = res {
        let zoom = current_zoom();
        set_selected_item(res, &name, zoom);
        if changed {
            set_resource(Some(res), "").await?;
        }
        let id = format!("{res}_{name}");
        click_card(res, name, id).await?;
    }
    if changed {
        sse::post_req(res).await
    } else {
        Ok(())
    }
}

/// Handle map zoom
fn handle_map_zoom(zoom: u32) {
    spawn_future(do_handle_map_zoom(zoom));
}

/// Handle map zoom
async fn do_handle_map_zoom(zoom: u32) -> Result<()> {
    Doc::get()
        .elem::<Element>("zoom-level")?
        .set_inner_html(&zoom.to_string());
    match app::selected_item() {
        Some((res, name)) => set_selected_style(res, &name, zoom),
        None => clear_selected_style(zoom),
    }
    // FIXME: only call these when crossing zoom threshold
    update_map_states(Res::Incident, zoom, None).await?;
    update_map_states(Res::Dms, zoom, None).await?;
    update_map_states(Res::Lcs, zoom, None).await?;
    update_map_states(Res::Camera, zoom, None).await?;
    update_map_states(Res::RampMeter, zoom, None).await?;
    update_map_states(Res::Beacon, zoom, None).await?;
    update_map_states(Res::WeatherSensor, zoom, None).await?;
    update_map_states(Res::TagReader, zoom, None).await?;
    update_map_states(Res::Controller, zoom, None).await?;
    update_osm_style(zoom).await?;
    Ok(())
}

/// Handle SSE notification
pub fn handle_notification(chan: String, name: Option<String>) {
    spawn_future(do_handle_notification(chan, name));
}

/// Handle SSE notification
async fn do_handle_notification(
    chan: String,
    _name: Option<String>,
) -> Result<()> {
    // Has the selected resource list updated?
    if let Some(res) = selected_resource()
        && res.as_str() == chan
        && update_card_list(res).await?
    {
        return Ok(());
    }
    if let Ok(res) = Res::try_from(chan.as_str())
        && res.has_location()
    {
        let zoom = current_zoom();
        update_map_states(res, zoom, None).await?;
    }
    Ok(())
}

/// Update card list with changed result
async fn update_card_list(res: Res) -> Result<bool> {
    let Some(old_cards) = app::card_list(None) else {
        return Ok(false);
    };
    if old_cards.res() != res {
        return Ok(false);
    }
    let old_json = old_cards.json().to_string();
    let expanded = old_cards.expanded_view();
    app::card_list(Some(old_cards));
    let access: Vec<Permission> = Asset::Access.uri().get_val().await?;
    let mut cards = CardList::new(res, &access).with_json(old_json);
    cards.fetch_all().await?;
    let search = search_value()?;
    for (cv, html) in cards.changed_html(&search).await? {
        if let Some(ev) = &expanded
            && cv.name == ev.name
        {
            // update expanded card (Control cards only)
            ev.handle_update().await?;
        } else {
            replace_card_html(&cv, &html);
        }
    }
    if res.has_location() {
        let zoom = current_zoom();
        update_map_states(res, zoom, Some(&cards)).await?;
    }
    if let Some(cv) = expanded {
        cards.set_view(cv);
    }
    app::card_list(Some(cards));
    Ok(true)
}

/// Update map item states
async fn update_map_states(
    res: Res,
    zoom: u32,
    cards: Option<&CardList>,
) -> Result<()> {
    // NOTE: resource must have locations
    let doc = Doc::get();
    if let Some(el) = doc.opt_elem::<Element>(&format!("{res}-style")) {
        let displayed = is_layer_displayed(res, zoom);
        let css = if displayed {
            let states_all = card::item_states_all(res);
            let items = match cards {
                Some(cards) => cards.states_main().await?,
                None => {
                    let access: Vec<Permission> =
                        Asset::Access.uri().get_val().await?;
                    let mut cards = CardList::new(res, &access);
                    cards.fetch_all().await?;
                    cards.states_main().await?
                }
            };
            item_states_css(states_all, &items)
        } else {
            let sel = Sel::cls(format!("wyrm-{res}"));
            let prop = Prop::new().display("none");
            Rule::new(sel, prop).to_string()
        };
        el.set_inner_html(&css);
    }
    if let Some(el) = doc.opt_elem::<Element>(&format!("layer-{res}")) {
        let mut prop = Prop::new();
        if zoom < selected_zoom(res) {
            prop = prop.background_color("#aaa");
        }
        el.set_attribute("style", &String::from(prop))?;
    }
    Ok(())
}

/// Check if a resource layer is displayed
fn is_layer_displayed(res: Res, zoom: u32) -> bool {
    (selected_resource() == Some(res)) || zoom >= selected_zoom(res)
}

/// Build resource item states style
fn item_states_css(
    states_all: &'static [ItemState],
    card_states: &[CardState],
) -> String {
    let mut css = String::with_capacity(32 * card_states.len());
    for st in states_all {
        let mut sel: Option<Sel> = None;
        for cs in card_states {
            if cs.state == *st {
                let s = Sel::cls(format!("{}-{}", cs.res.as_str(), &cs.name));
                sel = Some(match sel {
                    Some(sel) => sel.list(s),
                    None => s,
                });
            }
        }
        if let Some(sel) = sel {
            let prop = Prop::new().fill(st.fill_css());
            css.push_str(&Rule::new(sel, prop).to_string());
        }
    }
    css
}

/// Update map OSM style
async fn update_osm_style(zoom: u32) -> Result<()> {
    let doc = Doc::get();
    let displayed = zoom >= doc.input_parse::<u32>("layer-osm").unwrap_or(32);
    let css = if displayed {
        ""
    } else {
        ".wyrm-county,.wyrm-city,.wyrm-lake,.wyrm-river,.wyrm-pond,\
         .wyrm-wetland,.wyrm-motorway,.wyrm-trunk,.wyrm-primary,\
         .wyrm-secondary { display: none; }"
    };
    doc.elem::<Element>("osm-style")?.set_inner_html(css);
    let mut prop = Prop::new();
    if !displayed {
        prop = prop.background_color("#aaa");
    }
    doc.elem::<Element>("layer-osm")?
        .set_attribute("style", &String::from(prop))?;
    Ok(())
}
