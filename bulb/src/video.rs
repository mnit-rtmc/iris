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
use crate::card::uri_one_mjpeg;
use crate::error::Result;
use crate::util::{self, Doc};
use crate::{hls, mjpeg};
use percent_encoding::{NON_ALPHANUMERIC, utf8_percent_encode};
use resources::Res;
use wasm_bindgen::JsCast;
use wasm_bindgen::prelude::ScopedClosure;
use wasm_bindgen_futures::spawn_local;
use web_sys::{
    HtmlButtonElement, HtmlElement, HtmlInputElement, HtmlSelectElement,
};

/// Get the source URL for a given camera name and protocol
fn get_source(cname: &str, proto: &str) -> Result<String> {
    let baseurl = if let Some(elem) =
        Doc::get().opt_elem::<HtmlInputElement>("video_baseurl")
    {
        elem.value()
    } else {
        "".to_owned()
    };
    Ok(if baseurl.contains("{}") || baseurl.contains("/") {
        baseurl.replace(
            "{}",
            &utf8_percent_encode(cname, NON_ALPHANUMERIC).to_string(),
        )
    } else if proto == "mjpeg" {
        let url = uri_one_mjpeg(Res::Camera, cname);
        if !baseurl.is_empty() {
            format!("{}?stream={}", url.as_str(), baseurl)
        } else {
            url.as_str().to_owned()
        }
    } else {
        baseurl
    })
}

/// Start a stream for a given camera and protocol
pub async fn start_stream(cname: String, proto: &str) -> Result<()> {
    let source = get_source(&cname, proto)?;
    let player_id = format!("video_player_{cname}");
    log::info!("Starting {proto} stream for {cname} ({source})");
    match proto {
        "mjpeg" => mjpeg::start_stream(player_id, source, 30).await?,
        "hls" => hls::start_stream(player_id, source).await?,
        _ => log::warn!("Unsupported video protocol"),
    };
    Ok(())
}

/// Update a video stream for a given camera and protocol
async fn update_stream(cname: String, proto: String) {
    // Replace player with proper type (img or video)
    let doc = Doc::get();
    let player_id = &format!("video_player_{cname}");
    if let Some(elem) = doc.opt_elem::<HtmlElement>(player_id)
        && let Some(parent) = elem.parent_element()
    {
        // Remove player first, so any update intervals clear themselves (MJPEG)
        elem.remove();
        util::sleep(500).await;
        let elem_type = if proto == "mjpeg" { "img" } else { "video" };
        if let Ok(vid) = doc.0.create_element(elem_type) {
            vid.set_id(player_id);
            vid.set_class_name("video_player");
            if let Err(e) = parent.append_child(&vid) {
                log::error!("Couldn't append video player: {e:?}");
            }
        } else {
            log::error!("Couldn't create {elem_type} element for #{player_id}");
        }
    }

    if let Err(e) = start_stream(cname.clone(), &proto).await {
        log::error!("Couldn't start {proto} stream: {e:?}");
    } else {
        log::info!("Started new {proto} stream for {cname}");
    }
}

/// onclick for the video_update button, to stream using the URL or camera name
/// Passes the protocol selected by the dropdown element
pub fn add_update_listener(cname: String) -> Result<()> {
    let doc = Doc::get();
    let update_button = doc.elem::<HtmlButtonElement>("video_update")?;

    let closure = ScopedClosure::<dyn Fn()>::new(move || {
        let proto = if let Some(elem) =
            doc.opt_elem::<HtmlSelectElement>("video_protocol")
        {
            elem.value()
        } else {
            "mjpeg".to_owned()
        };
        spawn_local(update_stream(cname.clone(), proto.clone()));
    });

    update_button.add_event_listener_with_callback(
        "click",
        closure.as_ref().unchecked_ref(),
    )?;
    closure.forget();

    Ok(())
}
