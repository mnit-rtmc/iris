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
use crate::error::Error;
use bytes::Bytes;
use http_body_util::{BodyExt, Empty};
use hyper::header::{AUTHORIZATION, HeaderValue};
use hyper::{Request, Uri};
use hyper_util::rt::TokioIo;
use tokio::net::TcpStream;

/// HTTP client
#[derive(Debug, Default)]
pub struct Client {
    /// Host name / IP address
    host: String,
    /// Bearer token
    bearer_token: Option<String>,
}

impl Client {
    /// Make a new HTTP client
    pub fn new(host: &str) -> Self {
        let host = host.to_string();
        Client {
            host,
            bearer_token: None,
        }
    }

    /// Get the hostname / IP address
    pub fn host(&self) -> &str {
        &self.host
    }

    /// Set bearer token
    pub fn set_bearer_token(&mut self, bearer_token: String) {
        self.bearer_token = Some(bearer_token);
    }

    /// Make a `GET` request
    pub async fn get(&self, path: &str) -> Result<Vec<u8>, Error> {
        let addr = format!("{}:80", self.host);
        let stream = TcpStream::connect(addr).await?;
        let io = TokioIo::new(stream);
        let (mut sender, conn) =
            hyper::client::conn::http1::handshake(io).await?;
        let conn_task = tokio::spawn(conn);
        let uri = format!("http://{}/{path}", self.host).parse::<Uri>()?;
        let mut builder = Request::get(uri);
        if let Some(token) = &self.bearer_token {
            builder =
                builder.header(AUTHORIZATION, HeaderValue::from_str(token)?);
        }
        let req = builder.body(Empty::<Bytes>::new())?;

        let mut res = sender.send_request(req).await?;
        let status = res.status();
        if !status.is_success() {
            eprintln!("Headers: {:#?}\n", res.headers());
            return Err(Error::HttpStatus(status));
        }
        conn_task.await??;

        let mut body = Vec::<u8>::new();
        while let Some(next) = res.frame().await {
            let frame = next?;
            if let Some(chunk) = frame.data_ref() {
                body.extend(chunk);
            }
        }
        Ok(body)
    }

    /// Make an http `POST` request (JSON)
    pub async fn post(&self, path: &str, body: &str) -> Result<Vec<u8>, Error> {
        let addr = format!("{}:80", self.host);
        let stream = TcpStream::connect(addr).await?;
        let io = TokioIo::new(stream);
        let (mut sender, conn) =
            hyper::client::conn::http1::handshake(io).await?;
        let conn_task = tokio::spawn(conn);
        let uri = format!("http://{}/{path}", self.host).parse::<Uri>()?;
        let req = Request::post(uri)
            .header("content-type", "application/json")
            .body(body.to_string())?;

        let mut res = sender.send_request(req).await?;
        let status = res.status();
        if !status.is_success() {
            eprintln!("Headers: {:#?}\n", res.headers());
            return Err(Error::HttpStatus(status));
        }
        conn_task.await??;

        let mut body = Vec::<u8>::new();
        while let Some(next) = res.frame().await {
            let frame = next?;
            if let Some(chunk) = frame.data_ref() {
                body.extend(chunk);
            }
        }
        Ok(body)
    }
}
