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
use hyper::StatusCode;
use hyper::header::InvalidHeaderValue;
use std::string::FromUtf8Error;

/// Pollinator errors
#[derive(Debug, thiserror::Error)]
pub enum Error {
    #[error("Hyper {0}")]
    Hyper(#[from] hyper::Error),

    #[error("Header {0}")]
    Header(#[from] InvalidHeaderValue),

    #[error("HTTP {0}")]
    Http(#[from] hyper::http::Error),

    #[error("Authentication failed")]
    AuthFailed(),

    #[error("HTTP status {0}")]
    HttpStatus(StatusCode),

    #[error("Join {0}")]
    Join(#[from] tokio::task::JoinError),

    #[error("Invalid URI {0}")]
    InvalidUri(#[from] hyper::http::uri::InvalidUri),

    #[error("Tungstenite {0}")]
    Tungstenite(#[from] tungstenite::Error),

    #[error("Utf-8 {0}")]
    FromUtf8(#[from] FromUtf8Error),

    #[error("JSON {0}")]
    SerdeJson(#[from] serde_json::Error),

    #[error("IO {0}")]
    Io(#[from] std::io::Error),

    #[error("Stream Closed")]
    StreamClosed,
}
