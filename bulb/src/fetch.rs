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
use crate::error::{Error, Result};
use wasm_bindgen::prelude::*;
use wasm_bindgen::JsCast;
use wasm_bindgen_futures::JsFuture;
use web_sys::{console, Request, RequestInit, Response};

/// Fetch a GET request
pub async fn fetch_get(uri: &str) -> Result<JsValue> {
    let window = web_sys::window().unwrap_throw();
    let req = Request::new_with_str(uri).map_err(|e| {
        console::log_1(&e);
        Error::FetchRequest()
    })?;
    req.headers()
        .set("Accept", "application/json")
        .map_err(|e| {
            console::log_1(&e);
            Error::FetchRequest()
        })?;
    let resp = JsFuture::from(window.fetch_with_request(&req))
        .await
        .map_err(|e| {
            console::log_1(&e);
            Error::FetchRequest()
        })?;
    let resp: Response = resp.dyn_into().unwrap_throw();
    match resp.status() {
        200 => {
            let json = resp.json().map_err(|e| {
                console::log_1(&e);
                Error::FetchRequest()
            })?;
            JsFuture::from(json).await.map_err(|e| {
                console::log_1(&e);
                Error::FetchRequest()
            })
        }
        _ => {
            console::log_1(&resp);
            Err(Error::FetchResponse(resp.status()))
        }
    }
}

/// Perform a fetch request
async fn perform_fetch(
    method: &str,
    uri: &str,
    json: Option<&JsValue>,
) -> Result<Response> {
    let window = web_sys::window().unwrap_throw();
    let req = Request::new_with_str_and_init(
        uri,
        RequestInit::new().method(method).body(json),
    )
    .map_err(|e| {
        console::log_1(&e);
        Error::FetchRequest()
    })?;
    let resp = JsFuture::from(window.fetch_with_request(&req))
        .await
        .map_err(|e| {
            console::log_1(&e);
            Error::FetchRequest()
        })?;
    Ok(resp.dyn_into().unwrap_throw())
}

/// Fetch a PATCH request
pub async fn fetch_patch(uri: &str, json: &JsValue) -> Result<()> {
    let resp = perform_fetch("PATCH", uri, Some(json)).await?;
    match resp.status() {
        200 | 204 => Ok(()),
        _ => {
            console::log_1(&resp);
            Err(Error::FetchResponse(resp.status()))
        }
    }
}

/// Fetch a POST request
pub async fn fetch_post(uri: &str, json: &JsValue) -> Result<()> {
    let resp = perform_fetch("POST", uri, Some(json)).await?;
    match resp.status() {
        200 | 201 => Ok(()),
        _ => {
            console::log_1(&resp);
            Err(Error::FetchResponse(resp.status()))
        }
    }
}

/// Fetch a DELETE request
pub async fn fetch_delete(uri: &str) -> Result<()> {
    let resp = perform_fetch("DELETE", uri, None).await?;
    match resp.status() {
        200 | 204 => Ok(()),
        _ => {
            console::log_1(&resp);
            Err(Error::FetchResponse(resp.status()))
        }
    }
}
