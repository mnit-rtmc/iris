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
use crate::fetch::Uri;
use crate::start::handle_notification;
use crate::util::Doc;
use hatmil::{Page, html};
use js_sys::JsString;
use resources::Res;
use std::cell::RefCell;
use wasm_bindgen::JsCast;
use wasm_bindgen::prelude::*;
use web_sys::{Event, EventSource, HtmlButtonElement, MessageEvent};

/// Notification button state
#[derive(Clone, Copy, Debug, PartialEq)]
pub enum NotifyState {
    Starting,
    Disconnected,
    Connecting,
    Updating,
    Good,
}

impl NotifyState {
    /// Get symbol for a state
    pub const fn symbol(self) -> &'static str {
        match self {
            Self::Starting => "⚪",
            Self::Disconnected => "⚫",
            Self::Connecting => "🟠",
            Self::Updating => "🟡",
            Self::Good => "🟢",
        }
    }

    /// Get description of a state
    pub const fn description(self) -> &'static str {
        match self {
            Self::Starting => "Starting",
            Self::Disconnected => "Disconnected",
            Self::Connecting => "Connecting",
            Self::Updating => "Updating",
            Self::Good => "Good",
        }
    }

    /// Build state HTML
    pub fn build_html(self) -> String {
        // NOTE: these have &nbsp; to keep from splitting lines
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.class("tooltip").cdata("⭮ ").cdata(self.symbol());
        div.span().class("right").cdata(self.description());
        String::from(page)
    }

    /// Get button disabled value for a state
    pub const fn disabled(self) -> bool {
        match self {
            Self::Updating | Self::Good => true,
            _ => false,
        }
    }
}

/// SSE event callbacks
struct Callbacks {
    onopen: Closure<dyn Fn(Event)>,
    onerror: Closure<dyn Fn(Event)>,
    onmessage: Closure<dyn Fn(MessageEvent)>,
}

impl Callbacks {
    /// Create SSE callbacks
    fn new() -> Self {
        let onopen = Closure::new(|_e: Event| {
            set_notify_state(NotifyState::Connecting);
        });
        let onerror = Closure::new(|_e: Event| {
            set_notify_state(NotifyState::Disconnected);
        });
        let onmessage = Closure::new(|e: MessageEvent| {
            match e.data().dyn_into::<JsString>() {
                Ok(payload) => handle_notify(payload),
                Err(err) => handle_err(err),
            }
        });
        Callbacks {
            onopen,
            onerror,
            onmessage,
        }
    }
}

thread_local! {
    static CALLBACKS: RefCell<Callbacks> = RefCell::new(Callbacks::new());
}

/// Add SSE event source listener for notifications
pub fn add_listener() {
    let es = match EventSource::new("/iris/api/notify") {
        Ok(es) => es,
        Err(e) => {
            set_notify_state(NotifyState::Starting);
            log::warn!("SSE /iris/api/notify: {e:?}");
            app::defer_action(DeferredAction::MakeEventSource, 5000);
            return;
        }
    };
    CALLBACKS.with(|rc| {
        let callbacks = rc.borrow();
        es.set_onopen(Some(callbacks.onopen.as_ref().unchecked_ref()));
        es.set_onerror(Some(callbacks.onerror.as_ref().unchecked_ref()));
        es.set_onmessage(Some(callbacks.onmessage.as_ref().unchecked_ref()));
    });
}

/// POST a request for SSE notifications
pub async fn post_req(res: Option<Res>) {
    let uri = Uri::from("/iris/api/notify");
    let json = build_list(res);
    if let Err(e) = uri.post(&json.into()).await {
        log::warn!("/iris/api/notify POST: {e}");
    }
}

/// Build resource list for notifications
fn build_list(res: Option<Res>) -> String {
    // Always listen for resources with map markers
    let mut resources = vec![
        Res::Beacon.as_str(),
        Res::Camera.as_str(),
        Res::Dms.as_str(),
        Res::Lcs.as_str(),
        Res::RampMeter.as_str(),
        Res::WeatherSensor.as_str(),
    ];
    if let Some(r) = res {
        // Ensure selected `res` is last, since the map will redraw after
        // receiving that notification (js_set_selected)
        resources.retain(|rs| *rs != r.as_str());
        resources.push(r.as_str());
    }
    format!("[\"{}\"]", &resources.join("\",\""))
}

/// Set refresh button text
pub fn set_notify_state(ns: NotifyState) {
    let sb_refresh = Doc::get().elem::<HtmlButtonElement>("sb_refresh");
    sb_refresh.set_inner_html(&ns.build_html());
    sb_refresh.set_disabled(ns.disabled());
}

/// Handle SSE notify from server
fn handle_notify(payload: JsString) {
    set_notify_state(NotifyState::Updating);
    app::defer_action(DeferredAction::SetNotifyState(NotifyState::Good), 600);
    let mut chan: String = payload.into();
    let name = chan.find('$').map(|i| chan.split_off(i));
    handle_notification(chan, name);
}

/// Handle SSE notify error
fn handle_err(err: JsValue) {
    log::warn!("SSE payload: {err:?}");
    set_notify_state(NotifyState::Disconnected);
}
