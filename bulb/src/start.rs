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
use crate::error::Result;
use crate::fetch::Uri;
use crate::helper::spawn_future;
use crate::joystick;
use crate::map;
use crate::query::QueryState;
use crate::sidebar;
use crate::sse;
use crate::util::{self, Doc};
use wasm_bindgen::closure::Closure;
use wasm_bindgen::prelude::wasm_bindgen;
use wasm_bindgen::{JsCast, JsError};
use web_sys::{
    Element, Event, GamepadEvent, HtmlElement, HtmlInputElement, KeyboardEvent,
    MouseEvent, NavigateEvent,
};

/// Mouse event type
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum MouseEventTp {
    Down,
    Move,
    Up,
}

impl TryFrom<&MouseEvent> for MouseEventTp {
    type Error = ();

    fn try_from(me: &MouseEvent) -> std::result::Result<Self, Self::Error> {
        match me.type_().as_str() {
            "mousedown" => Ok(Self::Down),
            "mousemove" => Ok(Self::Move),
            "mouseup" => Ok(Self::Up),
            _ => Err(()),
        }
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
    add_interval_callback()?;
    add_navigate_callback()?;
    map::add_listeners()?;
    sidebar::add_listeners()?;
    let doc = Doc::new()?;
    let body = doc.body()?;
    add_mouse_listener(&body)?;
    add_joystick_listener(&body)?;
    add_gamepad_listener()?;
    add_input_enter_listener(&doc.elem("login_pass")?)?;
    spawn_future(finish_init());
    Ok(())
}

/// Finish initialization
async fn finish_init() -> Result<()> {
    sse::add_listener();
    match Uri::from("/iris/api/login").get().await {
        Ok(user) => {
            let Some(user) = user.as_string() else {
                log::warn!("finish_init: bad JS string");
                return Ok(());
            };
            sidebar::login(user).await?;
            trigger_reload()?;
            Ok(())
        }
        Err(err) => {
            if let Err(e) = trigger_reload() {
                log::warn!("finish_init: {e:?}");
            }
            Err(err)
        }
    }
}

/// Trigger reload (on Navigation API)
fn trigger_reload() -> Result<()> {
    let window = util::window()?;
    let navigation = window.navigation();
    // NOTE: reload will trigger a NavigateEvent for handle_navigate
    navigation.reload();
    Ok(())
}

/// Add callback for regular interval checks
fn add_interval_callback() -> Result<()> {
    let window = util::window()?;
    let closure: Closure<dyn Fn()> = Closure::new(handle_tick_interval);
    window.set_interval_with_callback_and_timeout_and_arguments_0(
        closure.as_ref().unchecked_ref(),
        app::TICK_INTERVAL,
    )?;
    closure.forget();
    Ok(())
}

/// Handle a tick interval
fn handle_tick_interval() {
    app::tick_tock();
    while let Some(action) = app::next_action() {
        match action {
            DeferredAction::FetchStationData => map::fetch_station_data(),
            DeferredAction::HideToast => util::hide_elem("sb_toast"),
            DeferredAction::RefreshList => sidebar::refresh_res_list(),
            DeferredAction::MakeEventSource => sse::add_listener(),
            DeferredAction::SetNotifyState(ns) => sse::set_notify_state(ns),
        }
    }
}

/// Add callback for navigate events
fn add_navigate_callback() -> Result<()> {
    let window = util::window()?;
    let navigation = window.navigation();
    let closure: Closure<dyn Fn(_)> = Closure::new(handle_navigate);
    navigation.add_event_listener_with_callback(
        "navigate",
        closure.as_ref().unchecked_ref(),
    )?;
    closure.forget();
    Ok(())
}

/// Handle a navigate event (new URL)
fn handle_navigate(ev: NavigateEvent) {
    log::trace!("handle_navigate: {ev:?}");
    if ev.can_intercept() && !ev.hash_change() {
        let url = ev.destination().url();
        log::debug!("navigate to: {url}");
        let _ = ev.intercept();
        if let Ok(query) = url.parse::<QueryState>() {
            spawn_future(sidebar::update_query(query));
        }
    }
}

/// Add a mouse event listener to an element
fn add_mouse_listener(el: &Element) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        if let Ok(me) = e.dyn_into::<MouseEvent>()
            && let Some(Ok(target)) =
                me.target().map(|e| e.dyn_into::<Element>())
            && let Ok(tp) = MouseEventTp::try_from(&me)
        {
            handle_mouse_ev(&target, tp, me.button());
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
fn handle_mouse_ev(target: &Element, tp: MouseEventTp, button: i16) {
    if MouseEventTp::Down == tp {
        map::dismiss_context_menu();
    }
    if button == 0 {
        spawn_future(handle_mouse_card(target.id(), tp));
    }
}

/// Handle a mouse event on an expanded card
async fn handle_mouse_card(id: String, tp: MouseEventTp) -> Result<()> {
    if let Some(cv) = app::expanded_view() {
        cv.handle_mouse(id.as_str(), tp).await?;
    }
    Ok(())
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
    sidebar::logout()?;
    trigger_reload()
}
