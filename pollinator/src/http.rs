// Copyright (C) 2025  Minnesota Department of Transportation
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
use hyper::body::Incoming;
use hyper::header::{AUTHORIZATION, HeaderValue};
use hyper::{Request, Response, Uri};
use hyper_util::rt::TokioIo;
use resin::{Error, Result};
use tokio::net::TcpStream;

/// HTTP client
#[derive(Debug, Default)]
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

    /// Get host and port of URI
    pub fn hostport(&self) -> Result<String> {
        let uri = self.uri.parse::<Uri>()?;
        let mut hostport =
            uri.host().ok_or(Error::InvalidConfiguration)?.to_string();
        let port = uri.port_u16().unwrap_or(80);
        hostport.push_str(&format!(":{port}"));
        Ok(hostport)
    }

    /// Set bearer token
    pub fn set_bearer_token(&mut self, bearer_token: String) {
        self.bearer_token = Some(bearer_token);
    }

    /// Make a `GET` request
    pub async fn get(&self, path: &str) -> Result<Vec<u8>> {
        let hostport = self.hostport()?;
        let stream = TcpStream::connect(&hostport).await?;
        let io = TokioIo::new(stream);
        let (mut sender, conn) =
            hyper::client::conn::http1::handshake(io).await?;
        let conn_task = tokio::spawn(conn);
        let uri = format!("http://{hostport}/{path}").parse::<Uri>()?;
        let mut builder = Request::get(uri);
        if let Some(token) = &self.bearer_token {
            builder =
                builder.header(AUTHORIZATION, HeaderValue::from_str(token)?);
        }
        let req = builder.body(Empty::<Bytes>::new())?;
        let res = sender.send_request(req).await?;
        let body = parse_response(res).await?;
        conn_task.await??;
        Ok(body)
    }

    /// Make an http `POST` request (JSON)
    pub async fn post(&self, path: &str, body: &str) -> Result<Vec<u8>> {
        let hostport = self.hostport()?;
        let stream = TcpStream::connect(&hostport).await?;
        let io = TokioIo::new(stream);
        let (mut sender, conn) =
            hyper::client::conn::http1::handshake(io).await?;
        let conn_task = tokio::spawn(conn);
        let uri = format!("http://{hostport}/{path}").parse::<Uri>()?;
        let req = Request::post(uri)
            .header("content-type", "application/json")
            .body(body.to_string())?;
        let res = sender.send_request(req).await?;
        let body = parse_response(res).await?;
        conn_task.await??;
        Ok(body)
    }
}

/// Parse HTTP response
async fn parse_response(mut res: Response<Incoming>) -> Result<Vec<u8>> {
    let status = res.status();
    if !status.is_success() {
        log::warn!("Status: {status:?}");
        log::warn!("Headers: {:#?}", res.headers());
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
