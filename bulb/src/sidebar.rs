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
use crate::app;
use crate::asset::Asset;
use crate::card::{self, CardList};
use crate::click;
use crate::eid;
use crate::error::Result;
use crate::helper::spawn_future;
use crate::item::ItemState;
use crate::map;
use crate::permission::Permission;
use crate::sse;
use crate::util::{self, Doc};
use crate::view::{CardView, View};
use resources::Res;
use wasm_bindgen::JsCast;
use wasm_bindgen::closure::Closure;
use web_sys::{
    Element, Event, HtmlElement, HtmlInputElement, HtmlSelectElement,
    ScrollBehavior, ScrollIntoViewOptions, ScrollLogicalPosition,
    TransitionEvent,
};

/// Add event listeners
pub fn add_listeners() -> Result<()> {
    let doc = Doc::new()?;
    let resource = doc.elem::<HtmlSelectElement>(eid::RESOURCE)?;
    resource.set_value("");
    let divider: HtmlElement = doc.elem("divider")?;
    click::add_listener(&divider)?;
    let sidebar: HtmlElement = doc.elem("sidebar")?;
    add_change_listener(&sidebar)?;
    click::add_listener(&sidebar)?;
    add_input_listener(&sidebar)?;
    add_focus_listener(&sidebar)?;
    add_transition_listener(&doc.elem(eid::CARDS)?)?;
    if let Some(doc_elem) = doc.doc_elem() {
        add_fullscreenchange_listener(&doc_elem)?;
    }
    Ok(())
}

/// Initialize resource select options based on permissions
pub async fn init_resource() -> Result<()> {
    let access: Vec<Permission> = Asset::Access.uri().get_val().await?;
    let doc = Doc::new()?;
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
    if let Some(el) = doc.opt_elem::<Element>("opt_tolling") {
        el.set_class_name(opt_class(&access, Res::TollZone));
    }
    // FIXME: navigate reload current entry
    Ok(())
}

/// Check for view access to a (base) resource name
fn opt_class(access: &[Permission], res: Res) -> &'static str {
    if Permission::is_view_permitted(access, res) {
        ""
    } else {
        "no-display"
    }
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

/// Handle change to selected resource type
async fn handle_resource_change(res: Option<Res>, search: &str) -> Result<()> {
    let window = util::window()?;
    let navigation = window.navigation();
    let mut uri = String::from("/iris/");
    match (res, search.is_empty()) {
        (Some(res), true) => uri.push_str(&format!("?res={res}")),
        (Some(res), false) => uri.push_str(&format!("?res={res}&q={search}")),
        (None, true) => (),
        (None, false) => uri.push_str(&format!("?q={search}")),
    }
    navigation.navigate(&uri);
    Ok(())
}

/// Handle change to selected resource type
pub async fn update_resource(res: Option<Res>, search: String) -> Result<()> {
    let doc = Doc::new()?;
    let sidebar = doc.elem::<HtmlElement>("sidebar")?;
    sidebar.set_class_name("wait");
    let rslt = do_update_resource(res, search).await;
    // Turn off "wait" style
    sidebar.set_class_name("");
    rslt
}

/// Handle change to selected resource type
async fn do_update_resource(res: Option<Res>, search: String) -> Result<()> {
    let doc = Doc::new()?;
    let sb_cards = doc.elem::<Element>(eid::CARDS)?;
    sb_cards.set_inner_html("");
    let base = res.map(|r| r.base());
    if let Some(el) = doc.opt_elem::<Element>("res_plan_row") {
        el.set_class_name(row_class(base == Some(Res::ActionPlan)));
    }
    if let Some(el) = doc.opt_elem::<Element>("res_camera_row") {
        el.set_class_name(row_class(base == Some(Res::Camera)));
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
    if let Some(el) = doc.opt_elem::<Element>("res_toll_row") {
        el.set_class_name(row_class(base == Some(Res::TollZone)));
    }
    if let Some(res) = res {
        let id = format!("res_{res}");
        if let Some(el) = doc.opt_elem::<HtmlInputElement>(&id) {
            el.set_checked(true);
        }
    }
    let sb_search = doc.elem::<HtmlInputElement>(eid::SEARCH)?;
    sb_search.set_value(&search);
    let sb_state = doc.elem::<HtmlSelectElement>(eid::STATE)?;
    let html = match res {
        Some(res) => card::item_states_html(res, !search.is_empty()),
        None => String::new(),
    };
    sb_state.set_inner_html(&html);
    map::set_selected_style(None);
    let access: Vec<_> = Asset::Access.uri().get_val().await?;
    fetch_and_populate_cards(res, &access).await?;
    sse::post_req(res, &access).await
}

/// Get dependent resource row class name
fn row_class(show: bool) -> &'static str {
    if show { "sb_row_left" } else { "no-display" }
}

/// Fetch and populate card list
async fn fetch_and_populate_cards(
    res: Option<Res>,
    access: &[Permission],
) -> Result<()> {
    match res {
        Some(res) => {
            let mut cards = CardList::new(res, access);
            cards.fetch_all().await?;
            let search = search_value()?;
            let html = cards.build_html(&search).await?;
            let doc = Doc::new()?;
            let sb_cards = doc.elem::<Element>(eid::CARDS)?;
            sb_cards.set_inner_html(&html);
            app::card_list(Some(cards));
        }
        None => {
            app::card_list(None);
        }
    }
    Ok(())
}

/// Get value to search
pub fn search_value() -> Result<String> {
    let doc = Doc::new()?;
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
        | "res_camera"
        | "res_encoder_type"
        | "res_dms"
        | "res_msg_pattern"
        | "res_msg_line"
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
        | "res_user"
        | "res_role"
        | "res_domain"
        | "res_system_attr"
        | "res_event_config"
        | "res_cabinet_style"
        | "res_tag_reader"
        | "res_toll_zone"
        | eid::RESOURCE => handle_res_change(),
        eid::SEARCH | eid::STATE => spawn_future(handle_search()),
        eid::VIEW => handle_card_view_ev(),
        _ => spawn_future(handle_input_other(id)),
    }
}

/// Handle selected resource change
fn handle_res_change() {
    let res = selected_resource();
    spawn_future(handle_resource_change(res, ""));
}

/// Get the selected resource value
pub fn selected_resource() -> Option<Res> {
    let doc = Doc::get();
    let rname = doc.select_parse::<String>(eid::RESOURCE);
    let res = Res::try_from(rname?.as_str()).ok()?;
    match res.base() {
        Res::ActionPlan if doc.input_bool("res_plan_phase") => {
            Some(Res::PlanPhase)
        }
        Res::ActionPlan if doc.input_bool("res_day_plan") => Some(Res::DayPlan),
        Res::Camera if doc.input_bool("res_encoder_type") => {
            Some(Res::EncoderType)
        }
        Res::Dms if doc.input_bool("res_msg_pattern") => Some(Res::MsgPattern),
        Res::Dms if doc.input_bool("res_msg_line") => Some(Res::MsgLine),
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
        Res::Permission => Some(Res::User), // no permission cards
        Res::SystemAttribute if doc.input_bool("res_event_config") => {
            Some(Res::EventConfig)
        }
        Res::SystemAttribute if doc.input_bool("res_cabinet_style") => {
            Some(Res::CabinetStyle)
        }
        Res::TollZone if doc.input_bool("res_tag_reader") => {
            Some(Res::TagReader)
        }
        _ => Some(res),
    }
}

/// Refresh resource list (after errors)
pub fn refresh_res_list() {
    handle_res_change();
}

/// Search card list for matching cards
async fn handle_search() -> Result<()> {
    match app::card_list(None) {
        Some(mut cards) => {
            let search = search_value()?;
            if let Some(cv) = cards.expanded_view() {
                replace_card(cv.compact(), &search).await?
            }
            let doc = Doc::new()?;
            for cv in cards.search_views(&search).await? {
                let id = cv.id();
                if let Some(el) = doc.opt_elem::<Element>(id) {
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
        spawn_future(replace_card(cv.with_view(view), ""));
    }
}

/// Get the selected view value
fn card_view_value() -> Option<View> {
    match Doc::get().select_parse::<String>(eid::VIEW) {
        Some(view) => match View::try_from(view.as_str()) {
            Ok(View::Setup(_edit)) => {
                let edit = app::can_edit_card();
                Some(View::Setup(edit))
            }
            Ok(View::Location(_edit)) => {
                let edit = app::can_edit_card();
                Some(View::Location(edit))
            }
            Ok(view) => Some(view),
            Err(_) => None,
        },
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

/// Replace a card view element with another view
pub async fn replace_card(mut cv: CardView, search: &str) -> Result<()> {
    let html = cv.fetch_one(search).await?;
    replace_card_html(&cv, &html);
    app::set_view(cv);
    Ok(())
}

/// Replace a card with provided HTML
fn replace_card_html(cv: &CardView, html: &str) {
    let Some(el) = Doc::get().opt_elem::<HtmlElement>(cv.id()) else {
        log::warn!("element {} not found", cv.id());
        return;
    };
    el.set_inner_html(html);
    el.set_class_name(cv.view.class_name());
    if cv.view.is_expanded() {
        let opt = ScrollIntoViewOptions::new();
        opt.set_behavior(ScrollBehavior::Instant);
        opt.set_block(ScrollLogicalPosition::Nearest);
        el.scroll_into_view_with_scroll_into_view_options(&opt);
    }
}

/// Set selected resource
pub async fn set_resource(res: Option<Res>, search: &str) -> Result<()> {
    let resource = Doc::new()?.elem::<HtmlSelectElement>(eid::RESOURCE)?;
    let base = res.map(|r| r.base().as_str()).unwrap_or("");
    resource.set_value(base);
    handle_resource_change(res, search).await
}

/// Add transition event listener to an element
fn add_transition_listener(el: &Element) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(handle_transition);
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
fn handle_transition(ev: Event) {
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
    if let Ok(res) = Res::try_from(chan.as_str()) {
        let access: Vec<_> = Asset::Access.uri().get_val().await?;
        map::update_layer(res, &access).await?;
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
    let access: Vec<_> = Asset::Access.uri().get_val().await?;
    let mut cards = CardList::new(res, &access).with_json(old_json);
    cards.fetch_all().await?;
    let search = search_value()?;
    for (cv, html) in cards.changed_html(&search).await? {
        if let Some(ev) = &expanded
            && cv.name() == ev.name()
        {
            // update expanded card (Control cards only)
            ev.handle_update().await?;
        } else {
            replace_card_html(&cv, &html);
        }
    }
    if let Some(cv) = expanded {
        cards.set_view(cv);
    }
    app::card_list(Some(cards));
    map::update_layer(res, &access).await?;
    Ok(true)
}
