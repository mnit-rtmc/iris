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
use crate::eid;
use crate::error::Result;
use crate::helper::spawn_future;
use crate::query::QueryParam;
use crate::sidebar;
use crate::start;
use crate::util::{self, Doc};
use resources::Res;
use wasm_bindgen::JsCast;
use wasm_bindgen::closure::Closure;
use web_sys::{Element, Event, HtmlButtonElement, HtmlElement};

/// Add a `click` event listener to an element
pub fn add_listener(el: &Element) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        if let Some(Ok(target)) = e.target().map(|e| e.dyn_into::<Element>()) {
            if target.is_instance_of::<HtmlButtonElement>() {
                handle_ev_button(&target);
            } else if let Ok(Some(cc)) = target.closest(".card-compact") {
                handle_ev_card(&cc);
            } else {
                handle_ev(&target);
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
fn handle_ev_button(target: &Element) {
    let id = target.id();
    match id.as_str() {
        eid::LOGIN => spawn_future(start::handle_login()),
        eid::LOGOUT => spawn_future(start::handle_logout()),
        eid::SHOW_SIDEBAR => spawn_future(handle_show_sidebar(true)),
        eid::HIDE_SIDEBAR => spawn_future(handle_show_sidebar(false)),
        // handled by mouse event listener, prevent click:
        "ptz-pan-left" | "ptz-pan-right" | "ptz-tilt-up" | "ptz-tilt-down"
        | "ptz-zoom-in" | "ptz-zoom-out" | "focus-near" | "focus-far"
        | "iris-open" | "iris-close" => (),
        _ => {
            spawn_future(handle_button_card(id));
        }
    }
}

/// Handle button click event on an expanded card
async fn handle_button_card(id: String) -> Result<()> {
    if let Some(cv) = app::expanded_view() {
        if eid::DELETE == id {
            if app::delete_enabled() {
                cv.handle_delete().await?;
                let query = QueryParam::current_entry().with_sel("");
                sidebar::set_query(query).await?;
            }
        } else if let Some(v) = cv.handle_click(&id).await?
            && !v.is_expanded()
        {
            let query = QueryParam::current_entry().with_sel("");
            sidebar::set_query(query).await?;
        }
    }
    Ok(())
}

/// Handle a `click` event for non-button target
fn handle_ev(target: &Element) {
    let id = target.id();
    if eid::MONITOR == id.as_str() {
        app::set_vid_mon(None);
        if let Ok(t) = Doc::get().elem::<HtmlElement>(eid::MONITOR) {
            t.set_inner_html("📺");
            let query = QueryParam::new().with_res(Some(Res::VideoMonitor));
            spawn_future(sidebar::set_query(query));
        }
    }
}

/// Handle a show/hide sidebar button click
async fn handle_show_sidebar(show: bool) -> Result<()> {
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

/// Handle a `click` event within a card element
fn handle_ev_card(el: &Element) {
    if let Some(name) = el.get_attribute("data-name") {
        let query = QueryParam::current_entry().with_sel(&name);
        if query.res().is_some() {
            spawn_future(click_card(query));
        }
    }
}

/// Handle a card click event
async fn click_card(query: QueryParam) -> Result<()> {
    if query.res().is_some() {
        sidebar::set_query(query).await?;
    }
    Ok(())
}
