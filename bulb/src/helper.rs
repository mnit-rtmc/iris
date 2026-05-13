// Copyright (C) 2026  Minnesota Department of Transportation
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
use crate::error::{Error, Result};
use crate::util::{self, Doc};
use std::error::Error as _;
use wasm_bindgen_futures::spawn_local;
use web_sys::HtmlElement;

/// Spawn a fallible future function
pub fn spawn_future(future: impl Future<Output = Result<()>> + 'static) {
    spawn_local(do_future(future));
}

/// Handle a fallible future function
async fn do_future(future: impl Future<Output = Result<()>>) {
    match future.await {
        Ok(_) => (),
        Err(Error::FetchResponseUnauthorized()) => show_auth(),
        Err(Error::FetchResponseNotFound()) => {
            // Card list may be out-of-date; refresh
            app::defer_action(DeferredAction::RefreshList, 200);
        }
        Err(Error::CardMismatch()) => {
            // Card list may be out-of-date; refresh
            app::defer_action(DeferredAction::RefreshList, 200);
        }
        Err(e) => {
            log::warn!("err: {e}");
            if let Some(se) = e.source() {
                log::warn!("source: {se}");
            }
            show_toast(&format!("Error: {e}"));
        }
    }
}

/// Show auth panel shade
fn show_auth() {
    app::set_user(None);
    util::show_elem("sb_auth_panel");
}

/// Show a toast message
fn show_toast(msg: &str) {
    log::warn!("toast: {msg}");
    if let Some(el) = Doc::get().opt_elem::<HtmlElement>("sb_toast") {
        el.set_inner_html(msg);
        el.set_class_name("show");
    }
    app::defer_action(DeferredAction::HideToast, 3000);
}
