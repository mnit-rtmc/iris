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
use crate::click;
use crate::error::Result;
use crate::fetch::Uri;
use crate::helper::spawn_future;
use crate::item::ItemState;
use crate::joystick;
use crate::sidebar;
use crate::sse;
use crate::util::{self, Doc};
use chrono::{DateTime, Local};
use earthwyrm::MapPane;
use hatmil::css::{Prop, Rule, Sel};
use resources::Res;
use serde::Deserialize;
use std::collections::HashMap;
use std::time::Duration;
use wasm_bindgen::closure::Closure;
use wasm_bindgen::prelude::wasm_bindgen;
use wasm_bindgen::{JsCast, JsError};
use web_sys::{
    Element, Event, GamepadEvent, HtmlElement, HtmlInputElement, KeyboardEvent,
    MouseEvent,
};

/// Map pane ID
const MAP_PANE: &str = "map-pane";

/// Layer groups
const GROUPS: &[&str] = &["tile", "tms"];

/// Anchor X position
const ANCHOR_X: f64 = 0.32;

/// Anchor Y position
const ANCHOR_Y: f64 = 0.5;

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

/// Select item on map
pub fn select_item_map(res: Res, name: &str, lon: f64, lat: f64) {
    if !app::is_selected_item(res, name) {
        set_selected_item(Some((res, name)));
        let zoom = selected_zoom(res).max(12);
        spawn_future(do_select_item_map(zoom, lon, lat));
    }
}

/// Select item on map
async fn do_select_item_map(zoom: u32, lon: f64, lat: f64) -> Result<()> {
    if let Some(map_pane) = MapPane::get(MAP_PANE) {
        map_pane.set_position(zoom, lon, lat);
        set_zoom_level(zoom);
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
pub fn set_selected_item(res_name: Option<(Res, &str)>) {
    if let Some(el) = Doc::get().opt_elem::<Element>("selected-style") {
        match res_name {
            Some((res, name)) => {
                app::set_selected_item(res, name);
                let sel = Sel::cls(format!("{}-{name}", res.as_str()));
                let prop = Prop::new().stroke("white").stroke_width(2);
                let css = Rule::new(sel, prop).to_string();
                el.set_inner_html(&css);
            }
            None => {
                app::clear_selected_item();
                el.set_inner_html("");
            }
        }
    }
}

/// Set the map zoom level
fn set_zoom_level(zoom: u32) {
    if let Some(el) = Doc::get().opt_elem::<Element>("marker-style") {
        let sel = Sel::cls("wyrm-tile").descendant(Sel::tp("use"));
        let prop = Prop::new().scale(zoom_scale(zoom));
        let css = Rule::new(sel, prop).to_string();
        el.set_inner_html(&css);
    }
    if let Some(el) = Doc::get().opt_elem::<Element>("zoom-level") {
        el.set_inner_html(&zoom.to_string());
    }
}

/// Get marker scale for a zoom level
fn zoom_scale(zoom: u32) -> &'static str {
    match zoom {
        1 => "0.003",
        2 => "0.006",
        3 => "0.012",
        4 => "0.025",
        5 => "0.05",
        6 => "0.1",
        7 => "0.2",
        8 => "0.3",
        9 => "0.4",
        10 => "0.5",
        11 => "0.6",
        12 => "0.8",
        _ => "1.0",
    }
}

/// Application starting function
#[wasm_bindgen(start)]
pub async fn start() -> core::result::Result<(), JsError> {
    crate::panic::set_hook_once();
    wasm_log::init(wasm_log::Config::new(log::Level::Info));
    log::info!("Started");
    Ok(add_listeners()?)
}

/// Add event listeners
fn add_listeners() -> Result<()> {
    let doc = Doc::new()?;
    sidebar::add_listeners()?;
    let layer_menu: HtmlElement = doc.elem("layer-menu")?;
    add_change_listener(&layer_menu)?;
    let body = doc.body()?;
    add_joystick_listener(&body)?;
    add_gamepad_listener()?;
    add_mouse_listener(&body)?;
    add_input_enter_listener(&doc.elem("login_pass")?)?;
    add_interval_callback()?;
    MapPane::new(MAP_PANE)
        .with_anchor(ANCHOR_X, ANCHOR_Y)
        .with_groups(GROUPS)
        .with_zoom_handler(handle_map_zoom)
        .with_click_handler(click::handle_map_click)
        .with_contextmenu_handler(click::handle_contextmenu)
        .register();
    if let Some(map_pane) = MapPane::get(MAP_PANE) {
        set_selected_item(None);
        map_pane.set_position(10, -93.2, 44.95);
        set_zoom_level(10);
    }
    spawn_future(finish_init());
    fetch_station_data();
    Ok(())
}

/// Finish initialization
pub async fn finish_init() -> Result<()> {
    sse::add_listener();
    let user = Uri::from("/iris/api/login").get().await?;
    match user.as_string() {
        Some(user) => {
            app::set_user(Some(user));
            sidebar::update_resource().await?;
            sidebar::set_resource(None, "").await?;
            sse::post_req(None).await
        }
        None => {
            log::warn!("invalid user: {user:?}");
            Ok(())
        }
    }
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
pub fn set_fullscreen() {
    let doc = Doc::get();
    let checked = doc.input_bool("sb_fullscreen");
    doc.request_fullscreen(checked);
}

/// Handle layer zoom threshold change
pub async fn handle_layer_zoom(id: String) -> Result<()> {
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
pub fn current_zoom() -> u32 {
    MapPane::get(MAP_PANE).map(|mp| mp.zoom()).unwrap_or(0)
}

/// Add enter/submit event listener to an element
fn add_input_enter_listener(el: &Element) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        if let (Some(Ok(target)), Ok(keydown_ev)) = (
            e.target().map(|e| e.dyn_into::<Element>()),
            e.dyn_into::<KeyboardEvent>(),
        ) && keydown_ev.key().as_str() == "Enter"
        {
            handle_input_enter(target.id());
        }
    });
    el.add_event_listener_with_callback(
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

/// Add a joystick event listener to an element
fn add_joystick_listener(el: &Element) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        if let Ok(mouse_event) = e.dyn_into::<MouseEvent>()
            && mouse_event.button() == 0
            && let Some(Ok(target)) =
                mouse_event.target().map(|e| e.dyn_into::<Element>())
        {
            let type_ = mouse_event.type_();
            if type_ == "mouseup" || type_ == "mousemove" {
                let sticks =
                    Doc::get().0.get_elements_by_class_name("joystick");
                for i in 0..sticks.length() {
                    if let Some(stick) = sticks.item(i) {
                        // x and y ignored by mouseup, but not mousemove
                        spawn_future(joystick::handle_mouse_event(
                            stick.id(),
                            type_.clone(),
                            mouse_event.x(),
                            mouse_event.y(),
                        ));
                    }
                }
            } else if Some("joystick")
                == target.get_attribute("class").as_deref()
            {
                spawn_future(joystick::handle_mouse_event(
                    target.id(),
                    type_,
                    mouse_event.x(),
                    mouse_event.y(),
                ));
            }
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
    el.add_event_listener_with_callback(
        "mousemove",
        closure.as_ref().unchecked_ref(),
    )?;
    closure.forget();
    Ok(())
}

/// Add the event listener for joystick/gamepad connections
fn add_gamepad_listener() -> Result<()> {
    // Starts polling a gamepad when connected, stops when disconnected
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        if let Ok(gamepad_event) = e.dyn_into::<GamepadEvent>()
            && let Some(gamepad) = gamepad_event.gamepad()
        {
            if gamepad_event.type_() == "gamepadconnected" {
                let _ = joystick::update_gamepad_status(true);
                if let Err(e) = joystick::start_gamepad_poll(gamepad) {
                    log::error!("Couldn't start polling due to {e:?}");
                }
            } else {
                let _ = joystick::update_gamepad_status(false);
                if let Err(e) = joystick::stop_gamepad_poll(gamepad) {
                    log::error!("Couldn't stop polling due to {e:?}");
                }
            }
        }
    });
    let window = util::window()?;
    window.add_event_listener_with_callback(
        "gamepaddisconnected",
        closure.as_ref().unchecked_ref(),
    )?;
    window.add_event_listener_with_callback(
        "gamepadconnected",
        closure.as_ref().unchecked_ref(),
    )?;
    closure.forget();
    Ok(())
}

/// Add a mouse event listener to an element
fn add_mouse_listener(el: &Element) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        if let Ok(mouse_event) = e.dyn_into::<MouseEvent>()
            && mouse_event.button() == 0
            && let Some(Ok(target)) =
                mouse_event.target().map(|e| e.dyn_into::<Element>())
        {
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
        | (Some("iris"), _)
        | (Some("ptz"), _)
        | (Some("publish"), _) => id,
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

/// Handle login button press
pub async fn handle_login() -> Result<()> {
    let doc = Doc::new()?;
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
pub async fn handle_logout() -> Result<()> {
    let uri = Uri::from("/iris/api/login");
    uri.delete().await?;
    Ok(())
}

/// Add callback for regular interval checks
fn add_interval_callback() -> Result<()> {
    let window = util::window()?;
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
            DeferredAction::RefreshList => sidebar::handle_res_change(),
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
    if let Some(el) = Doc::new()?.opt_elem::<Element>("segment-style") {
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

/// Handle map zoom
fn handle_map_zoom(zoom: u32) {
    spawn_future(do_handle_map_zoom(zoom));
}

/// Handle map zoom
async fn do_handle_map_zoom(zoom: u32) -> Result<()> {
    set_zoom_level(zoom);
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

/// Update map item states
pub async fn update_map_states(
    res: Res,
    zoom: u32,
    cards: Option<&CardList>,
) -> Result<()> {
    // NOTE: resource must have locations
    let doc = Doc::new()?;
    if let Some(el) = doc.opt_elem::<Element>(&format!("{res}-style")) {
        let displayed = is_layer_displayed(res, zoom);
        let css = if displayed {
            let states_all = card::item_states_all(res);
            let items = match cards {
                Some(cards) => cards.states_main().await?,
                None => {
                    let access = Asset::Access.uri().get_val().await?;
                    let mut cards = CardList::new(res, access);
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
    (sidebar::selected_resource() == Some(res)) || zoom >= selected_zoom(res)
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
                let s = Sel::cls(format!("{}-{}", cs.res.as_str(), cs.name));
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
    let doc = Doc::new()?;
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
