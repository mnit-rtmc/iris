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
use crate::app;
use crate::error::Result;
use crate::util::{self, Doc};

use resources::Res;
use wasm_bindgen_futures::JsFuture;

use std::cell::Cell;
use std::rc::Rc;

use wasm_bindgen::JsCast;
use wasm_bindgen::closure::Closure;
use web_sys::{
    Blob, ErrorEvent, Event, HtmlElement, HtmlImageElement, Response,
};

/// Start a motion JPEG stream for a video player id and source URI
pub async fn start_stream(
    player_id: String,
    source: String,
    fps: i32,
) -> Result<()> {
    log::info!("Attempting to start new stream for \"{source}\"@{fps}fps...");
    if let Some(cv) = app::expanded_view()
        && cv.res == Res::Camera
    {
        let window = util::window()?;
        let resp = JsFuture::from(window.fetch_with_str(&source)).await?;
        let frame = resp.dyn_into::<Response>()?;
        let blob = frame.blob()?.await?;
        let blob = blob.dyn_into::<Blob>()?;
        // Validate that the URL returns an image
        // Checks type of blob, otherwise JPEG markers 0xFF 0xD8
        let bytes = js_sys::Uint8Array::from(blob.bytes().await?).to_vec();
        if !blob.type_().starts_with("image/")
            && !bytes.starts_with(&[0xFF, 0xD8])
        {
            log::warn!("Not an image URL: {source}");
            return Ok(());
        }
        let src = source.clone();
        let errored = Rc::new(Cell::new(false));
        let errored_c = errored.clone();
        let pl_id = player_id.clone();
        let get_image: Closure<dyn Fn()> = Closure::new(move || {
            if let Some(elem) = Doc::get().opt_elem::<HtmlImageElement>(&pl_id)
                && !errored_c.get()
            {
                // Append timestamp to force image update
                let ts = js_sys::Date::now() as u128;
                let symbol = if src.contains("?") { "&" } else { "?" };
                elem.set_src(&format!("{src}{symbol}t={ts}"));
            } else {
                // Have closure clear its own interval once there's no player
                if let Err(e) = app::stop_stream_interval(&src) {
                    log::error!("Couldn't stop stream interval: {e}");
                } else {
                    log::info!("Stopped stream interval");
                }
            }
        });
        let id = window
            .set_interval_with_callback_and_timeout_and_arguments_0(
                get_image.as_ref().unchecked_ref(),
                1000 / fps,
            )?;
        app::add_stream_interval_id(source, id);
        get_image.forget();

        add_error_listener(window, player_id, errored)?;
    }
    Ok(())
}

/// Listen for errors on image src, then set flag to stop stream interval
fn add_error_listener(
    window: web_sys::Window,
    player_id: String,
    errored: Rc<Cell<bool>>,
) -> Result<()> {
    let stop_stream: Closure<dyn Fn(_)> = Closure::new(move |ev: Event| {
        if let Ok(ev) = ev.dyn_into::<ErrorEvent>()
            && let Some(target) = ev.target()
            && let Ok(target) = target.dyn_into::<HtmlElement>()
            && target.id() == player_id
        {
            errored.set(true);
            log::error!("HTTP error received: {ev:#?}");
        }
    });
    window.add_event_listener_with_callback(
        "error",
        stop_stream.as_ref().unchecked_ref(),
    )?;
    stop_stream.forget();
    Ok(())
}
