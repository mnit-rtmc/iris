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
use crate::card::{self, CardList, uri_one_mjpeg};
use crate::click;
use crate::eid;
use crate::error::Result;
use crate::helper::spawn_future;
use crate::item::ItemState;
use crate::map;
use crate::mjpeg;
use crate::permission::{AccessLevel, Permission};
use crate::query::QueryState;
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

/// Login user and initialize elements
pub async fn login(user: String) -> Result<()> {
    app::set_user(Some(user.clone()));
    let access: Vec<_> = Asset::Access.uri().get_val().await?;
    initialize(Some(&user), &access)
}

/// Logout user and initialize elements
pub fn logout() -> Result<()> {
    app::set_user(None);
    let access = Vec::new();
    initialize(None, &access)
}

/// Initialize sidebar elements after login/logout
fn initialize(user: Option<&str>, access: &[Permission]) -> Result<()> {
    let doc = Doc::new()?;
    let title = doc.elem::<HtmlElement>("page-title")?;
    let html = match user {
        Some(user) => format!("IRIS: {user}"),
        None => "IRIS: not logged in".to_string(),
    };
    title.set_inner_html(&html);
    show_hide_res_opt(&doc, Res::ActionPlan, access);
    show_hide_res_opt(&doc, Res::Beacon, access);
    show_hide_res_opt(&doc, Res::Camera, access);
    show_hide_res_opt(&doc, Res::Dms, access);
    show_hide_res_opt(&doc, Res::GateArm, access);
    show_hide_res_opt(&doc, Res::Incident, access);
    show_hide_res_opt(&doc, Res::Lcs, access);
    show_hide_res_opt(&doc, Res::RampMeter, access);
    show_hide_res_opt(&doc, Res::VideoMonitor, access);
    show_hide_res_opt(&doc, Res::WeatherSensor, access);
    show_hide_res_opt(&doc, Res::CommConfig, access);
    show_hide_res_opt(&doc, Res::Road, access);
    show_hide_res_opt(&doc, Res::Permission, access);
    show_hide_res_opt(&doc, Res::SystemAttribute, access);
    show_hide_res_opt(&doc, Res::TollZone, access);
    Ok(())
}

/// Show or hide a resource option
fn show_hide_res_opt(doc: &Doc, res: Res, access: &[Permission]) {
    if let Some(el) = doc.opt_elem::<Element>(&format!("opt-{res}")) {
        let cls = if Permission::is_view_permitted(access, res) {
            ""
        } else {
            "no-display"
        };
        el.set_class_name(cls);
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

/// Update controls to reflect query state (resource, selection, etc.)
pub async fn update_query(query: QueryState) -> Result<()> {
    let doc = Doc::new()?;
    let sidebar = doc.elem::<HtmlElement>("sidebar")?;
    sidebar.set_class_name("wait");
    let rslt = do_update_query(doc, query).await;
    // Turn off "wait" style
    sidebar.set_class_name("");
    rslt
}

/// Update controls to reflect query state (resource, selection, etc.)
async fn do_update_query(doc: Doc, query: QueryState) -> Result<()> {
    let access: Vec<_> = Asset::Access.uri().get_val().await?;
    let res = query.res();
    let base = res.map(|r| r.base());
    let resource = doc.elem::<HtmlSelectElement>(eid::RESOURCE)?;
    resource.set_value(base.map(|r| r.as_str()).unwrap_or(""));
    let sb_cards = doc.elem::<Element>(eid::CARDS)?;
    sb_cards.set_inner_html("");
    show_hide_res_row(&doc, base, Res::ActionPlan, &access);
    show_hide_res_row(&doc, base, Res::Camera, &access);
    show_hide_res_row(&doc, base, Res::Dms, &access);
    show_hide_res_row(&doc, base, Res::Lcs, &access);
    show_hide_res_row(&doc, base, Res::VideoMonitor, &access);
    show_hide_res_row(&doc, base, Res::CommConfig, &access);
    show_hide_res_row(&doc, base, Res::Road, &access);
    show_hide_res_row(&doc, base, Res::Permission, &access);
    show_hide_res_row(&doc, base, Res::SystemAttribute, &access);
    show_hide_res_row(&doc, base, Res::TollZone, &access);
    if let Some(res) = res {
        let id = format!("res_{res}");
        if let Some(el) = doc.opt_elem::<HtmlInputElement>(&id) {
            el.set_checked(true);
        }
    }
    let sb_search = doc.elem::<HtmlInputElement>(eid::SEARCH)?;
    sb_search.set_value(query.q());
    let sb_state = doc.elem::<HtmlSelectElement>(eid::STATE)?;
    let html = match res {
        Some(res) => card::item_states_html(res, !query.q().is_empty()),
        None => String::new(),
    };
    sb_state.set_inner_html(&html);
    map::set_selected_style(query.clone());
    fetch_and_populate_cards(query, &access).await?;
    sse::post_req(res, &access).await
}

/// Show or hide a resource row
fn show_hide_res_row(
    doc: &Doc,
    base: Option<Res>,
    res: Res,
    access: &[Permission],
) {
    if let Some(el) = doc.opt_elem::<Element>(&format!("row-{res}")) {
        let cls = if base == Some(res)
            && Permission::access_level_max(access, res) >= AccessLevel::Operate
        {
            "sb_row_left"
        } else {
            "no-display"
        };
        el.set_class_name(cls);
    }
}

/// Fetch and populate card list
async fn fetch_and_populate_cards(
    query: QueryState,
    access: &[Permission],
) -> Result<()> {
    match query.res() {
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
        eid::SEARCH | eid::STATE => spawn_future(handle_search()),
        eid::VIEW => handle_card_view_ev(),
        eid::RESOURCE => handle_res_change(),
        _ if let Some(("res", rname)) = id.split_once('-')
            && Res::try_from(rname).is_ok() =>
        {
            handle_res_change()
        }
        _ => spawn_future(handle_input_other(id)),
    }
}

/// Handle selected resource change
fn handle_res_change() {
    let query = QueryState::new().with_res(selected_resource());
    spawn_future(change_query_state(query));
}

/// Get the selected resource value
pub fn selected_resource() -> Option<Res> {
    let doc = Doc::get();
    let rname = doc.select_parse::<String>(eid::RESOURCE);
    let res = Res::try_from(rname?.as_str()).ok()?;
    match res.base() {
        Res::ActionPlan if doc.input_bool("res-plan_phase") => {
            Some(Res::PlanPhase)
        }
        Res::ActionPlan if doc.input_bool("res-day_plan") => Some(Res::DayPlan),
        Res::Camera if doc.input_bool("res-encoder_type") => {
            Some(Res::EncoderType)
        }
        Res::Dms if doc.input_bool("res-msg_pattern") => Some(Res::MsgPattern),
        Res::Dms if doc.input_bool("res-msg_line") => Some(Res::MsgLine),
        Res::Dms if doc.input_bool("res-sign_config") => Some(Res::SignConfig),
        Res::Dms if doc.input_bool("res-word") => Some(Res::Word),
        Res::Lcs if doc.input_bool("res-lcs_state") => Some(Res::LcsState),
        Res::VideoMonitor if doc.input_bool("res-monitor_style") => {
            Some(Res::MonitorStyle)
        }
        Res::VideoMonitor if doc.input_bool("res-flow_stream") => {
            Some(Res::FlowStream)
        }
        Res::CommConfig if doc.input_bool("res-comm_link") => {
            Some(Res::CommLink)
        }
        Res::CommConfig if doc.input_bool("res-controller") => {
            Some(Res::Controller)
        }
        Res::CommConfig if doc.input_bool("res-alarm") => Some(Res::Alarm),
        Res::CommConfig if doc.input_bool("res-gps") => Some(Res::Gps),
        //Res::Road if doc.input_bool("res-r_node") => Some(Res::Rnode),
        Res::Road if doc.input_bool("res-detector") => Some(Res::Detector),
        Res::Road if doc.input_bool("res-map_extent") => Some(Res::MapExtent),
        Res::Permission if doc.input_bool("res-user") => Some(Res::User),
        Res::Permission if doc.input_bool("res-role") => Some(Res::Role),
        Res::Permission if doc.input_bool("res-domain") => Some(Res::Domain),
        Res::Permission => Some(Res::User), // no permission cards
        Res::SystemAttribute if doc.input_bool("res-event_config") => {
            Some(Res::EventConfig)
        }
        Res::SystemAttribute if doc.input_bool("res-cabinet_style") => {
            Some(Res::CabinetStyle)
        }
        Res::TollZone if doc.input_bool("res-tag_reader") => {
            Some(Res::TagReader)
        }
        _ => Some(res),
    }
}

/// Change query state
async fn change_query_state(query: QueryState) -> Result<()> {
    let window = util::window()?;
    let navigation = window.navigation();
    navigation.navigate(&format!("/iris/{query}"));
    Ok(())
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
    let cv_clone = cv.clone();
    let html = cv.fetch_one(search).await?;
    replace_card_html(&cv, &html);
    app::set_view(cv);
    let res = cv_clone.res;
    let name = cv_clone.name();
    if res == Res::Camera && cv_clone.view == View::Control {
        let uri = uri_one_mjpeg(Res::Camera, name);
        let uri = uri.as_str();
        mjpeg::start_stream(uri.to_owned(), 30)?;
    }
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

/// Set query state
pub async fn set_query(query: QueryState) -> Result<()> {
    change_query_state(query).await
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
