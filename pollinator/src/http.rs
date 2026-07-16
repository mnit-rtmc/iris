// Copyright (C) 2025-2026  Minnesota Department of Transportation
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
use bytes::Bytes;
use http_body_util::{BodyExt, Empty};
use hyper::body::{Body, Incoming};
use hyper::header::{AUTHORIZATION, HeaderValue};
use hyper::{Request, Response, Uri};
use hyper_rustls::{HttpsConnector, HttpsConnectorBuilder};
use hyper_util::client::legacy::{
    Client as HyperClient, connect::HttpConnector,
};
use hyper_util::rt::TokioExecutor;
use resin::{Error, Result};

/// HTTP client requestor
#[derive(Clone, Debug, Default)]
pub struct Client {
    /// URI address
    uri: String,
    /// Bearer token
    bearer_token: Option<String>,
}

impl Client {
    /// Make a new HTTP client
    pub fn new(uri: &str) -> Self {
        let uri = uri.to_string();
        Client {
            uri,
            bearer_token: None,
        }
    }

    /// Get scheme of URI
    pub fn scheme(&self) -> Result<String> {
        let uri = self.uri.parse::<Uri>()?;
        match uri.scheme_str() {
            Some(scheme) => Ok(scheme.to_string()),
            None => Ok("http".to_string()),
        }
    }

    /// Get host of URI
    pub fn host(&self) -> Result<String> {
        let uri = self.uri.parse::<Uri>()?;
        let host = uri.host().ok_or(Error::InvalidConfig("host"))?.to_string();
        Ok(host)
    }

    /// Get port of URI
    pub fn port(&self) -> Result<u16> {
        let uri = self.uri.parse::<Uri>()?;
        let port = uri.port_u16().unwrap_or_else(|| match uri.scheme_str() {
            Some("https") | Some("wss") => 443,
            _ => 80,
        });
        Ok(port)
    }

    /// Build URI from path, including required parts
    pub fn uri(&self, path: &str) -> Result<Uri> {
        let scheme = self.scheme()?;
        let host = self.host()?;
        let port = self.port()?;
        Ok(format!("{scheme}://{host}:{port}/{path}").parse::<Uri>()?)
    }

    /// Set bearer token
    pub fn set_bearer_token(&mut self, bearer_token: String) {
        self.bearer_token = Some(bearer_token);
    }

    /// Make a `GET` request
    pub async fn get(&self, path: &str) -> Result<Vec<u8>> {
        let uri = self.uri(path)?;
        log::debug!("GET {uri}");
        let client = build_client();
        let mut builder = Request::get(uri);
        if let Some(token) = &self.bearer_token {
            builder =
                builder.header(AUTHORIZATION, HeaderValue::from_str(token)?);
        }
        let req = builder.body(Empty::<Bytes>::new())?;
        let res = client.request(req).await?;
        let body = parse_response(res).await?;
        Ok(body)
    }

    /// Make an http `POST` request (JSON)
    pub async fn post(&self, path: &str, body: &str) -> Result<Vec<u8>> {
        let uri = self.uri(path)?;
        log::debug!("POST {uri}");
        let client = build_client();
        let req = Request::post(uri)
            .header("content-type", "application/json")
            .body(body.to_string())?;
        let res = client.request(req).await?;
        let body = parse_response(res).await?;
        Ok(body)
    }
}

/// Parse HTTP response
async fn parse_response(mut res: Response<Incoming>) -> Result<Vec<u8>> {
    let status = res.status();
    if !status.is_success() {
        log::debug!("status: {status:?}");
        log::debug!("headers: {:#?}", res.headers());
        return Err(Error::HttpStatus(status));
    }

    let mut body = Vec::<u8>::new();
    while let Some(next) = res.frame().await {
        let frame = next?;
        if let Some(chunk) = frame.data_ref() {
            body.extend(chunk);
        }
    }
    Ok(body)
}

/// Build Hyper client
fn build_client<B: Body + std::marker::Send>()
-> HyperClient<HttpsConnector<HttpConnector>, B>
where
    <B as Body>::Data: std::marker::Send,
{
    let https = HttpsConnectorBuilder::new()
        .with_native_roots()
        .unwrap_or(HttpsConnectorBuilder::new().with_webpki_roots())
        // HTTPS URLs handled with rustls, HTTP with lower-level connector:
        .https_or_http()
        .enable_http1()
        .build();
    HyperClient::builder(TokioExecutor::new()).build(https)
}
