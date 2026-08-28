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
use crate::eid;
use crate::error::Result;
use crate::fetch::Uri;
use crate::permission::{AccessLevel, Permission};
use crate::sidebar;
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
    /// Connecting to SSE server
    Connecting,
    /// Connected to SSE server
    Connected,
    /// Updating after event receipt
    Updating,
    /// Reconnecting to SSE server
    Reconnecting,
    /// Disconnected from SSE server
    Disconnected,
}

impl NotifyState {
    /// Get symbol for a state
    pub const fn symbol(self) -> &'static str {
        match self {
            Self::Starting => "⚪",
            Self::Connecting => "🟠",
            Self::Connected => "🟢",
            Self::Updating => "🟡",
            Self::Reconnecting => "🔴",
            Self::Disconnected => "⚫",
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
                log::info!("SSE new: {path}");
                es
            }
            Err(e) => {
                log::warn!("SSE /iris/api/notify: {e:?}");
                set_notify_state(NotifyState::Starting);
                return None;
            }
        };
        let onopen = Closure::new(|_e: Event| {
            log::info!("SSE open");
            set_notify_state(NotifyState::Connecting);
        });
        let onerror = Closure::new(|_e: Event| {
            log::info!("SSE error");
            set_notify_state(NotifyState::Disconnected);
        });
        let onmessage = Closure::new(|e: MessageEvent| {
            match e.data().dyn_into::<JsString>() {
                Ok(payload) => handle_notify(payload),
                Err(err) => {
                    log::warn!("SSE message err: {err:?}");
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
pub async fn post_req(res: Option<Res>, access: &[Permission]) -> Result<()> {
    let json = build_list(res, access);
    let uri = Uri::from("/iris/api/notify");
    uri.post(&json.into()).await.inspect_err(|e| {
        log::warn!("/iris/api/notify POST: {e}");
    })
}

/// Build resource list for notifications
fn build_list(res: Option<Res>, access: &[Permission]) -> String {
    let mut resources = String::from("[\"");
    // Always listen for resources with map markers
    for r in [
        Res::Beacon,
        Res::Camera,
        Res::Controller,
        Res::Dms,
        Res::GateArm,
        Res::Incident,
        Res::Lcs,
        Res::RampMeter,
        Res::TagReader,
        Res::WeatherSensor,
    ] {
        if Some(r) != res
            && Permission::access_level_max(access, r) > AccessLevel::None
        {
            resources.push_str(r.as_str());
            resources.push_str("\",\"");
        }
    }
    match res {
        Some(r) => {
            resources.push_str(r.as_str());
            resources.push('"');
        }
        None => {
            if resources.ends_with('"') {
                resources.pop();
            }
            if resources.ends_with(',') {
                resources.pop();
            }
        }
    }
    resources.push(']');
    resources
}

/// Set refresh button text
pub fn set_notify_state(mut ns: NotifyState) {
    match ns {
        NotifyState::Disconnected => {
            let count = app::connect_count() + 1;
            if count < 8 {
                ns = NotifyState::Reconnecting;
                app::defer_action(DeferredAction::MakeEventSource, 5000);
                app::set_connect_count(count);
            } else {
                if let Err(e) = sidebar::logout() {
                    log::warn!("set_notify_state logout: {e:?}");
                }
            }
        }
        NotifyState::Connected => {
            app::set_connect_count(0);
        }
        _ => (),
    }
    if let Some(sb_notify) = Doc::get().opt_elem::<HtmlElement>(eid::NOTIFY) {
        sb_notify.set_inner_html(ns.symbol());
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
        sidebar::handle_notification(chan, name);
    }
    app::defer_action(
        DeferredAction::SetNotifyState(NotifyState::Connected),
        600,
    );
}
