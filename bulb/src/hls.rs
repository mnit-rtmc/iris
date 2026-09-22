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
use crate::util::Doc;
use js_sys::{Array, Function, Reflect};
use resources::Res;
use wasm_bindgen::JsValue;
use web_sys::HtmlVideoElement;

/// Helper method to reflect JavaScript code on a generic object
fn call(target: &JsValue, func: &str, arg: &JsValue) -> Result<()> {
    let jsv_func = Reflect::get(target, &JsValue::from(func))?;
    let func = Function::from(jsv_func);
    let args = Array::new();
    args.push(&JsValue::from(arg));
    Reflect::apply(&func, target, &args)?;
    Ok(())
}

/// Start an HLS stream for a video player id and source URI
pub async fn start_stream(player_id: String, source: String) -> Result<()> {
    if let Some(cv) = app::expanded_view()
        && cv.res == Res::Camera
        && let Some(video) = Doc::get().opt_elem::<HtmlVideoElement>(&player_id)
    {
        if let Some(true) = js_sys::eval("Hls.isSupported()")?.as_bool() {
            let hls = js_sys::eval("new Hls()")?;
            call(&hls, "loadSource", &JsValue::from(source))?;
            call(&hls, "attachMedia", &JsValue::from(&video))?;
            video.play()?.await?;
        } else if !video
            .can_play_type("application/vnd.apple.mpegurl")
            .is_empty()
        {
            video.set_src(&source);
            video.play()?.await?;
        }
    }
    Ok(())
}
