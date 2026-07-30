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
use crate::card;
use crate::eid;
use crate::error::Result;
use crate::helper::spawn_future;
use crate::sidebar;
use crate::sse;
use crate::start;
use crate::util::{self, Doc};
use crate::view::{CardView, View};
use resources::Res;
use wasm_bindgen::JsCast;
use wasm_bindgen::closure::Closure;
use web_sys::{Element, Event, HtmlButtonElement, HtmlElement};

/// Button attributes
struct ButtonAttrs {
    /// Element ID
    id: String,
    /// Data-link attribute
    data_link: Option<String>,
    /// Data-type attribute
    data_type: Option<String>,
}

impl ButtonAttrs {
    /// Create button attributes
    fn new(id: String, target: &Element) -> Self {
        let mut data_link = None;
        let mut data_type = None;
        if let Some("go_link") = target.get_attribute("class").as_deref() {
            data_link = target.get_attribute("data-link");
            data_type = target.get_attribute("data-type");
        }
        ButtonAttrs {
            id,
            data_link,
            data_type,
        }
    }

    /// Check if data link
    fn is_link(&self) -> bool {
        self.data_link.is_some() && self.data_type.is_some()
    }

    /// Handle button click event on an expanded card
    async fn handle_button_card(self) -> Result<()> {
        if let Some(cv) = app::expanded_view() {
            if self.is_link() {
                self.go_resource().await?;
            } else if eid::DELETE == self.id {
                if app::delete_enabled() {
                    cv.handle_delete().await?;
                    sidebar::replace_card(cv.with_view(View::Hidden), "")
                        .await?;
                }
            } else if let Some(v) = cv.handle_click(&self.id).await? {
                sidebar::replace_card(cv.with_view(v), "").await?;
            }
        }
        Ok(())
    }

    /// Go to resource from target's `data-link` attribute
    async fn go_resource(self) -> Result<()> {
        if let (Some(link), Some(rname)) = (self.data_link, self.data_type)
            && let Ok(res) = Res::try_from(rname.as_str())
        {
            sidebar::set_resource(Some(res), &link).await?;
            sse::post_req(Some(res)).await
        } else {
            Ok(())
        }
    }
}

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
        "show_sidebar" => spawn_future(handle_show_sidebar(true)),
        "hide_sidebar" => spawn_future(handle_show_sidebar(false)),
        // handled by mouse event listener, prevent click:
        "ptz-pan-left" | "ptz-pan-right" | "ptz-tilt-up" | "ptz-tilt-down"
        | "ptz-zoom-in" | "ptz-zoom-out" | "focus-near" | "focus-far"
        | "iris-open" | "iris-close" => (),
        _ => {
            let attrs = ButtonAttrs::new(id, target);
            spawn_future(attrs.handle_button_card());
        }
    }
}

/// Handle a `click` event for non-button target
fn handle_ev(target: &Element) {
    let id = target.id();
    if eid::MONITOR == id.as_str() {
        app::set_vid_mon(None);
        if let Ok(t) = Doc::get().elem::<HtmlElement>(eid::MONITOR) {
            t.set_inner_html("📺");
            // FIXME: set resource to video?
        }
    }
}

/// Handle a show/hide sidebar button click
async fn handle_show_sidebar(show: bool) -> Result<()> {
    let doc = Doc::new()?;
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

/// Handle a `click` event within a card element
fn handle_ev_card(el: &Element) {
    if let Some(id) = el.get_attribute("id")
        && let Some(name) = el.get_attribute("data-name")
        && let Some(res) = sidebar::selected_resource()
    {
        spawn_future(click_card(res, name, id));
    }
}

/// Handle a card click event
pub async fn click_card(res: Res, name: String, id: String) -> Result<()> {
    if let Some(cv) = app::expanded_view() {
        let search = sidebar::search_value()?;
        sidebar::replace_card(cv.compact(), &search).await?;
    }
    let view = if id.ends_with('_') && id.len() == res.as_str().len() + 1 {
        View::Create
    } else {
        let edit = app::can_edit_card();
        // Expand to the second view (1) for the resource
        *card::res_views(res, edit).get(1).unwrap_or(&View::Compact)
    };
    let cv = CardView::new(res, &name, view);
    sidebar::replace_card(cv, "").await?;
    Ok(())
}
