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
use js_sys::JsString;
use resources::Res;
use std::cell::RefCell;
use wasm_bindgen::JsCast;
use wasm_bindgen::prelude::*;
use web_sys::{Event, EventSource, HtmlElement, MessageEvent};

/// Notification button state
#[derive(Clone, Copy, Debug, PartialEq)]
pub enum NotifyState {
    /// Initial starting state
    Starting,
    /// Disconnected from SSE server
    Disconnected,
    /// Connecting to SSE server
    Connecting,
    /// Updating after event receipt
    Updating,
    /// Connected to SSE server
    Connected,
}

impl NotifyState {
    /// Get symbol for a state
    pub const fn symbol(self) -> &'static str {
        match self {
            Self::Starting => "⚪",
            Self::Disconnected => "⚫",
            Self::Connecting => "🟠",
            Self::Updating => "🟡",
            Self::Connected => "🟢",
        }
    }
}

/// SSE event listener
///
/// Closures stored here to prevent untimely dropping
#[allow(unused)]
struct Listener {
    /// EventSource
    source: EventSource,
    /// EventSource onopen callback
    onopen: Closure<dyn Fn(Event)>,
    /// EventSource onerror callback
    onerror: Closure<dyn Fn(Event)>,
    /// EventSource onmessage callback
    onmessage: Closure<dyn Fn(MessageEvent)>,
}

impl Listener {
    /// Create SSE listener
    fn new(path: &str) -> Option<Self> {
        let source = match EventSource::new(path) {
            Ok(es) => {
                log::info!("SSE EventSource: {path}");
                es
            }
            Err(e) => {
                log::warn!("SSE /iris/api/notify: {e:?}");
                set_notify_state(NotifyState::Starting);
                return None;
            }
        };
        let onopen = Closure::new(|e: Event| {
            log::info!("SSE event: {}", e.type_());
            set_notify_state(NotifyState::Connecting);
        });
        let onerror = Closure::new(|e: Event| {
            log::error!("SSE event: {}", e.type_());
            set_notify_state(NotifyState::Disconnected);
        });
        let onmessage = Closure::new(|e: MessageEvent| {
            match e.data().dyn_into::<JsString>() {
                Ok(payload) => handle_notify(payload),
                Err(err) => {
                    log::warn!("SSE payload: {err:?}");
                    set_notify_state(NotifyState::Disconnected);
                }
            }
        });
        source.set_onopen(Some(onopen.as_ref().unchecked_ref()));
        source.set_onerror(Some(onerror.as_ref().unchecked_ref()));
        source.set_onmessage(Some(onmessage.as_ref().unchecked_ref()));
        Some(Listener {
            source,
            onopen,
            onerror,
            onmessage,
        })
    }
}

thread_local! {
    /// Static listener to prevent dropping
    static LISTENER: RefCell<Option<Listener>> = const { RefCell::new(None) };
}

/// Add SSE event source listener for notifications
pub fn add_listener() {
    LISTENER.with(|rc| {
        let mut listener = rc.borrow_mut();
        if let Some(ref listener) = *listener {
            log::info!("SSE closing EventSource");
            listener.source.close();
        }
        *listener = Listener::new("/iris/api/notify");
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
    let sb_notify = Doc::get().elem::<HtmlElement>("sb_notify");
    sb_notify.set_inner_html(&ns.symbol());
    if NotifyState::Disconnected == ns {
        app::defer_action(DeferredAction::MakeEventSource, 5000);
    }
}

/// Handle SSE notify from server
fn handle_notify(payload: JsString) {
    set_notify_state(NotifyState::Updating);
    let data = String::from(payload);
    for chan in data.split('\n') {
        log::debug!("SSE message: {chan}");
        let mut chan = chan.to_string();
        let name = chan.find('$').map(|i| chan.split_off(i));
        handle_notification(chan, name);
    }
    app::defer_action(
        DeferredAction::SetNotifyState(NotifyState::Connected),
        600,
    );
}
