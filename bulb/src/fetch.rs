// Copyright (C) 2022-2024  Minnesota Department of Transportation
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
use std::borrow::{Borrow, Cow};
use wasm_bindgen::prelude::*;
use wasm_bindgen::JsCast;
use wasm_bindgen_futures::JsFuture;
use web_sys::{console, Blob, Headers, Request, RequestInit, Response};

/// Fetchable content types
#[derive(Copy, Clone, Debug, Eq, PartialEq)]
pub enum ContentType {
    Json,
    Text,
    Gif,
}

/// Uniform resource identifier
#[derive(Clone, Debug)]
pub struct Uri {
    cow: Cow<'static, str>,
    content_type: ContentType,
}

/// Fetch action
pub enum Action {
    Patch(Uri, JsValue),
    Post(Uri, JsValue),
    #[allow(dead_code)]
    Delete(Uri),
}

impl ContentType {
    /// Get string slice usable in Accept header
    fn as_str(self) -> &'static str {
        match self {
            ContentType::Json => "application/json",
            ContentType::Text => "text/plain",
            ContentType::Gif => "image/gif",
        }
    }
}

impl From<String> for Uri {
    fn from(s: String) -> Self {
        Uri {
            cow: Cow::Owned(s),
            content_type: ContentType::Json,
        }
    }
}

impl From<&'static str> for Uri {
    fn from(s: &'static str) -> Self {
        Uri {
            cow: Cow::Borrowed(s),
            content_type: ContentType::Json,
        }
    }
}

impl Uri {
    /// Set the content type
    pub fn with_content_type(mut self, content_type: ContentType) -> Self {
        self.content_type = content_type;
        self
    }

    /// Get URI as string slice
    pub fn as_str(&self) -> &str {
        self.cow.borrow()
    }

    /// Fetch using "GET" method
    pub async fn get(&self) -> Result<JsValue> {
        let resp = get_response(self).await.map_err(|e| {
            console::log_1(&e);
            Error::FetchRequest()
        })?;
        resp_status(resp.status())?;
        match self.content_type {
            ContentType::Json => wait_promise(resp.json()).await,
            ContentType::Text => wait_promise(resp.text()).await,
            ContentType::Gif => {
                let blob = wait_promise(resp.blob()).await?;
                let blob = blob.dyn_into::<Blob>().unwrap();
                wait_promise(Ok(blob.array_buffer())).await
            }
        }
    }

    /// Fetch using "PATCH" method
    pub async fn patch(&self, json: &JsValue) -> Result<()> {
        let resp = perform_fetch("PATCH", self.as_str(), Some(json)).await?;
        resp_status(resp.status())
    }

    /// Fetch using "POST" method
    pub async fn post(&self, json: &JsValue) -> Result<()> {
        let resp = perform_fetch("POST", self.as_str(), Some(json)).await?;
        resp_status(resp.status())
    }

    /// Fetch using "DELETE" method
    pub async fn delete(&self) -> Result<()> {
        let resp = perform_fetch("DELETE", self.as_str(), None).await?;
        resp_status(resp.status())
    }
}

/// Fetch a GET response
async fn get_response(uri: &Uri) -> std::result::Result<Response, JsValue> {
    let window = web_sys::window().unwrap_throw();
    let req = Request::new_with_str(uri.as_str())?;
    req.headers().set("Accept", uri.content_type.as_str())?;
    let resp = JsFuture::from(window.fetch_with_request(&req)).await?;
    Ok(resp.dyn_into::<Response>().unwrap_throw())
}

/// Wait for a JS promise
async fn wait_promise(
    data: std::result::Result<js_sys::Promise, JsValue>,
) -> Result<JsValue> {
    let data = data.map_err(|e| {
        console::log_1(&e);
        Error::FetchRequest()
    })?;
    JsFuture::from(data).await.map_err(|e| {
        console::log_1(&e);
        Error::FetchRequest()
    })
}

/// Perform a fetch request
async fn perform_fetch(
    method: &str,
    uri: &str,
    json: Option<&JsValue>,
) -> Result<Response> {
    let window = web_sys::window().unwrap_throw();
    let headers = Headers::new().unwrap_throw();
    if json.is_some() {
        headers
            .set("Content-Type", "application/json")
            .unwrap_throw();
    } else {
        headers.set("Content-Type", "text/plain").unwrap_throw();
    }
    let req = Request::new_with_str_and_init(
        uri,
        RequestInit::new()
            .method(method)
            .body(json)
            .headers(&headers),
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

/// Check for errors in response status code
fn resp_status(sc: u16) -> Result<()> {
    match sc {
        200 | 201 | 202 | 204 => Ok(()),
        401 => Err(Error::FetchResponseUnauthorized()),
        403 => Err(Error::FetchResponseForbidden()),
        404 => Err(Error::FetchResponseNotFound()),
        409 => Err(Error::FetchResponseConflict()),
        422 => Err(Error::FetchResponseUnprocessable()),
        _ => Err(Error::FetchResponseOther(sc)),
    }
}

impl Action {
    /// Perform fetch action
    pub async fn perform(&self) -> Result<()> {
        match self {
            Action::Patch(uri, val) => uri.patch(val).await,
            Action::Post(uri, val) => uri.post(val).await,
            Action::Delete(uri) => uri.delete().await,
        }
    }
}
