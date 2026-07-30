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
use crate::sidebar;
use crate::sse;
use crate::util::{self, Doc};
use wasm_bindgen::closure::Closure;
use wasm_bindgen::prelude::wasm_bindgen;
use wasm_bindgen::{JsCast, JsError};
use web_sys::{
    Element, Event, GamepadEvent, HtmlElement, HtmlInputElement, KeyboardEvent,
    MouseEvent,
};

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
    map::add_listeners()?;
    sidebar::add_listeners()?;
    let body = doc.body()?;
    add_joystick_listener(&body)?;
    add_gamepad_listener()?;
    add_mouse_listener(&body)?;
    add_input_enter_listener(&doc.elem("login_pass")?)?;
    add_interval_callback()?;
    spawn_future(finish_init());
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

/// Set fullscreen mode
pub fn set_fullscreen() {
    let doc = Doc::get();
    let checked = doc.input_bool("sb_fullscreen");
    doc.request_fullscreen(checked);
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
            DeferredAction::FetchStationData => map::fetch_station_data(),
            DeferredAction::HideToast => util::hide_elem("sb_toast"),
            DeferredAction::RefreshList => sidebar::handle_res_change(),
            DeferredAction::MakeEventSource => sse::add_listener(),
            DeferredAction::SetNotifyState(ns) => sse::set_notify_state(ns),
        }
    }
}
