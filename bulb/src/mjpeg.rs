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
use wasm_bindgen::JsCast;
use wasm_bindgen::closure::Closure;
use web_sys::HtmlImageElement;

/// Start a motion JPEG stream for a given source URI and framerate
/// Attaches to mjpeg_player, so call only when Camera card is expanded
pub fn start_stream(source: String, fps: i32) -> Result<()> {
    if let Some(cv) = app::expanded_view()
        && cv.res == Res::Camera
    {
        let src = source.clone();
        let get_image: Closure<dyn Fn()> = Closure::new(move || {
            if let Some(elem) =
                Doc::get().opt_elem::<HtmlImageElement>("mjpeg_player")
            {
                // Append timestamp to force image update
                let ts = js_sys::Date::now() as u128;
                elem.set_src(&format!("{src}?t={ts}"));
            } else {
                // Have closure clear its own interval once there's no player
                log::info!(
                    "No element with id mjpeg_player, stopping stream interval"
                );
                if let Err(e) = app::stop_stream_interval(&src) {
                    log::error!("Couldn't stop stream interval: {e}");
                }
            }
        });
        let window = util::window()?;
        let id = window
            .set_interval_with_callback_and_timeout_and_arguments_0(
                get_image.as_ref().unchecked_ref(),
                1000 / fps,
            )?;
        app::add_stream_interval_id(source, id);
        get_image.forget();
    }
    Ok(())
}
