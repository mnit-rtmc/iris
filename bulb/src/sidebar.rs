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
use crate::eid;
use crate::error::Result;
use crate::helper::spawn_future;
use crate::item::ItemState;
use crate::map;
use crate::permission::{AccessLevel, Permission};
use crate::query::QueryParam;
use crate::sse;
use crate::start;
use crate::util::{self, Doc};
use crate::view::{CardView, View};
use resources::Res;
use wasm_bindgen::JsCast;
use wasm_bindgen::closure::Closure;
use web_sys::{
    Element, Event, HtmlButtonElement, HtmlElement, HtmlInputElement,
    HtmlSelectElement, ScrollBehavior, ScrollIntoViewOptions,
    ScrollLogicalPosition, TransitionEvent,
};

/// Add event listeners
pub fn add_listeners() -> Result<()> {
    let doc = Doc::new()?;
    let divider: HtmlElement = doc.elem("divider")?;
    add_click_listener(&divider)?;
    let sidebar: HtmlElement = doc.elem("sidebar")?;
    add_click_listener(&sidebar)?;
    add_change_listener(&sidebar)?;
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
    app::set_connect_count(8);
    app::set_query(QueryParam::default());
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

/// Add a `click` event listener to an element
pub fn add_click_listener(el: &Element) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        if let Some(Ok(target)) = e.target().map(|e| e.dyn_into::<Element>()) {
            if target.is_instance_of::<HtmlButtonElement>() {
                handle_click_button(target.id());
            } else if let Ok(Some(cc)) = target.closest(".card-compact") {
                handle_click_card(&cc);
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
fn handle_click_button(id: String) {
    match id.as_str() {
        eid::LOGIN => spawn_future(start::handle_login()),
        eid::LOGOUT => spawn_future(start::handle_logout()),
        eid::SHOW_SIDEBAR => spawn_future(show_sidebar(true)),
        eid::HIDE_SIDEBAR => spawn_future(show_sidebar(false)),
        eid::ADD => spawn_future(show_create_card()),
        // handled by mouse event listener, prevent click:
        "ptz-pan-left" | "ptz-pan-right" | "ptz-tilt-up" | "ptz-tilt-down"
        | "ptz-zoom-in" | "ptz-zoom-out" | "focus-near" | "focus-far"
        | "iris-open" | "iris-close" => (),
        _ => {
            if let Some(cv) = app::expanded_view() {
                spawn_future(handle_button_card(cv, id));
            }
        }
    }
}

/// Show/hide sidebar
async fn show_sidebar(show: bool) -> Result<()> {
    let doc = Doc::new()?;
    if let Some(btn) = doc.opt_elem::<HtmlButtonElement>(eid::SHOW_SIDEBAR) {
        btn.set_disabled(show);
    }
    if let Some(btn) = doc.opt_elem::<HtmlButtonElement>(eid::HIDE_SIDEBAR) {
        btn.set_disabled(!show);
    }
    if show {
        util::show_elem("sidebar");
    } else {
        util::hide_elem("sidebar");
    }
    Ok(())
}

/// Show the create card
async fn show_create_card() -> Result<()> {
    let query = QueryParam::current_entry();
    if query.res().is_some() && app::can_edit_card() {
        set_query(query.with_sel("_")).await?;
    }
    Ok(())
}

/// Handle button click event on an expanded card
async fn handle_button_card(cv: CardView, id: String) -> Result<()> {
    if eid::DELETE == id {
        if app::delete_enabled() {
            cv.handle_delete().await?;
            let query = QueryParam::current_entry().with_sel("");
            set_query(query).await?;
        }
    } else if let Some(v) = cv.handle_click(&id).await?
        && !v.is_expanded()
    {
        let query = QueryParam::current_entry().with_sel("");
        set_query(query).await?;
    }
    Ok(())
}

/// Handle a `click` event within a card element
fn handle_click_card(el: &Element) {
    if let Some(name) = el.get_attribute("data-name") {
        let query = QueryParam::current_entry().with_sel(&name);
        if query.res().is_some() {
            spawn_future(set_query(query));
        }
    }
}

/// Add a "change" event listener to an element
fn add_change_listener(el: &Element) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        if let Some(Ok(target)) = e.target().map(|e| e.dyn_into::<Element>()) {
            let id = target.id();
            if id == eid::FULLSCREEN {
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
    let checked = doc.input_bool(eid::FULLSCREEN);
    doc.request_fullscreen(checked);
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
pub fn handle_res_change() {
    let query = QueryParam::new().with_res(selected_resource());
    spawn_future(set_query(query));
}

/// Get the selected resource value
fn selected_resource() -> Option<Res> {
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
        Res::Permission if doc.input_bool("res-user_id") => Some(Res::User),
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

/// Add a "fullscreenchange" event listener to an element
fn add_fullscreenchange_listener(el: &Element) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|_e: Event| {
        let doc = Doc::get();
        if let Some(btn) = doc.opt_elem::<HtmlInputElement>(eid::FULLSCREEN) {
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

/// Update controls to reflect query parameters (resource, selection)
pub async fn update_query(query: QueryParam) -> Result<()> {
    let doc = Doc::new()?;
    let sidebar = doc.elem::<HtmlElement>("sidebar")?;
    sidebar.set_class_name("wait");
    let rslt = do_update_query(doc, query).await;
    // Turn off "wait" style
    sidebar.set_class_name("");
    rslt
}

/// Update controls to reflect query parameters (resource, selection)
async fn do_update_query(doc: Doc, query: QueryParam) -> Result<()> {
    map::set_selected_style(query.clone());
    let res = query.res();
    let res_change = res != app::query().res();
    let sel_change = query.sel() != app::query().sel();
    app::set_query(query.clone());
    if res_change {
        let access: Vec<_> = Asset::Access.uri().get_val().await?;
        update_resources(&doc, res, &access)?;
        let sb_cards = doc.elem::<Element>(eid::CARDS)?;
        sb_cards.set_inner_html("");
        fetch_and_populate_cards(query.clone(), &access).await?;
        sse::post_req(res, &access).await?;
    }
    if let Some(res) = res
        && sel_change
    {
        let search = search_value()?;
        shrink_card(&search).await?;
        expand_card(query, res).await?;
    }
    Ok(())
}

/// Update resource elements for a query
fn update_resources(
    doc: &Doc,
    res: Option<Res>,
    access: &[Permission],
) -> Result<()> {
    let base = res.map(|r| r.base());
    let resource = doc.elem::<HtmlSelectElement>(eid::RESOURCE)?;
    resource.set_value(base.map(|r| r.as_str()).unwrap_or(""));
    show_hide_res_row(doc, base, Res::ActionPlan, access);
    show_hide_res_row(doc, base, Res::Camera, access);
    show_hide_res_row(doc, base, Res::Dms, access);
    show_hide_res_row(doc, base, Res::Lcs, access);
    show_hide_res_row(doc, base, Res::VideoMonitor, access);
    show_hide_res_row(doc, base, Res::CommConfig, access);
    show_hide_res_row(doc, base, Res::Road, access);
    show_hide_res_row(doc, base, Res::Permission, access);
    show_hide_res_row(doc, base, Res::SystemAttribute, access);
    show_hide_res_row(doc, base, Res::TollZone, access);
    if let Some(res) = res {
        let id = format!("res-{res}");
        if let Some(el) = doc.opt_elem::<HtmlInputElement>(&id) {
            el.set_checked(true);
        }
    }
    let sb_search = doc.elem::<HtmlInputElement>(eid::SEARCH)?;
    sb_search.set_value("");
    let sb_state = doc.elem::<HtmlSelectElement>(eid::STATE)?;
    let html = match res {
        Some(res) => card::item_states_html(res),
        None => String::new(),
    };
    sb_state.set_inner_html(&html);
    Ok(())
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

/// Shrink selected card
async fn shrink_card(search: &str) -> Result<()> {
    if let Some(cv) = app::expanded_view() {
        let cv = cv.compact();
        replace_card(cv, search).await?;
    }
    Ok(())
}

/// Expand selected card
async fn expand_card(query: QueryParam, res: Res) -> Result<()> {
    let sel = query.sel();
    if !sel.is_empty() {
        let edit = app::can_edit_card();
        if "_" == sel {
            if edit && let Some(Ok(nm)) = app::next_card_name() {
                let cv = CardView::new(res, nm).with_view(View::Create);
                replace_card(cv, "").await?;
            }
        } else {
            let cv = CardView::new(res, sel).expand(edit);
            replace_card(cv, "").await?;
        }
    }
    Ok(())
}

/// Fetch and populate card list
async fn fetch_and_populate_cards(
    query: QueryParam,
    access: &[Permission],
) -> Result<()> {
    let mut can_create = false;
    match query.res() {
        Some(res) => {
            let mut cards = CardList::new(res, access);
            cards.fetch_all().await?;
            can_create = res.has_create()
                && cards.access_level() == AccessLevel::Configure;
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
    app::set_expanded_view(None);
    if let Ok(sb_add) = Doc::get().elem::<Element>(eid::ADD) {
        let cls = if can_create { "" } else { "no-display" };
        sb_add.set_class_name(cls);
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

/// Set query parameters
pub async fn set_query(query: QueryParam) -> Result<()> {
    let window = util::window()?;
    let navigation = window.navigation();
    navigation.navigate(&format!("/iris/{query}"));
    Ok(())
}

/// Search card list for matching cards
async fn handle_search() -> Result<()> {
    let search = search_value()?;
    shrink_card(&search).await?;
    match app::card_list(None) {
        Some(cards) => {
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

/// Replace a card view element with another view
async fn replace_card(mut cv: CardView, search: &str) -> Result<()> {
    let html = cv.fetch_one(search).await?;
    replace_card_html(&cv, &html);
    app::set_expanded_view(Some(cv));
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
    if let Some(res) = app::query().res()
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
    let expanded = app::expanded_view();
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
    app::card_list(Some(cards));
    map::update_layer(res, &access).await?;
    Ok(true)
}
