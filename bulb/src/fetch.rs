// Copyright (C) 2022  Minnesota Department of Transportation
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
use crate::Result;
use wasm_bindgen::prelude::*;
use wasm_bindgen::JsCast;
use wasm_bindgen_futures::JsFuture;
use web_sys::{Request, RequestInit, Response};

/// Fetch a GET request
pub async fn fetch_get(uri: &str) -> Result<JsValue> {
    let window = web_sys::window().unwrap_throw();
    let req = Request::new_with_str(uri)?;
    req.headers().set("Accept", "application/json")?;
    let resp = JsFuture::from(window.fetch_with_request(&req)).await?;
    let resp: Response = resp.dyn_into().unwrap_throw();
    match resp.status() {
        200 => Ok(JsFuture::from(resp.json()?).await?),
        _ => Err(resp.status_text().into()),
    }
}

/// Fetch a PATCH request
pub async fn fetch_patch(uri: &str, json: &JsValue) -> Result<()> {
    let window = web_sys::window().unwrap_throw();
    let req = Request::new_with_str_and_init(
        uri,
        RequestInit::new().method("PATCH").body(Some(json)),
    )?;
    let resp = JsFuture::from(window.fetch_with_request(&req)).await?;
    let resp: Response = resp.dyn_into().unwrap_throw();
    match resp.status() {
        200 | 204 => Ok(()),
        _ => Err(resp.status_text().into()),
    }
}

/// Fetch a POST request
pub async fn fetch_post(uri: &str, json: &JsValue) -> Result<()> {
    let window = web_sys::window().unwrap_throw();
    let req = Request::new_with_str_and_init(
        uri,
        RequestInit::new().method("POST").body(Some(json)),
    )?;
    let resp = JsFuture::from(window.fetch_with_request(&req)).await?;
    let resp: Response = resp.dyn_into().unwrap_throw();
    match resp.status() {
        200 | 201 => Ok(()),
        _ => Err(resp.status_text().into()),
    }
}

/// Fetch a DELETE request
pub async fn fetch_delete(uri: &str) -> Result<()> {
    let window = web_sys::window().unwrap_throw();
    let req = Request::new_with_str_and_init(
        uri,
        RequestInit::new().method("DELETE"),
    )?;
    let resp = JsFuture::from(window.fetch_with_request(&req)).await?;
    let resp: Response = resp.dyn_into().unwrap_throw();
    match resp.status() {
        200 | 204 => Ok(()),
        _ => Err(resp.status_text().into()),
    }
}
