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
    /// Hyper error
    #[error("Hyper {0}")]
    Hyper(#[from] hyper::Error),

    /// Invalid header error
    #[error("Header {0}")]
    Header(#[from] InvalidHeaderValue),

    /// HTTP error
    #[error("HTTP {0}")]
    Http(#[from] hyper::http::Error),

    /// HTTP status error
    #[error("HTTP status {0}")]
    HttpStatus(StatusCode),

    /// Authentication failed error
    #[error("Authentication failed")]
    AuthFailed(),

    /// Task join error
    #[error("Join {0}")]
    Join(#[from] tokio::task::JoinError),

    /// Invalid URI error
    #[error("Invalid URI {0}")]
    InvalidUri(#[from] hyper::http::uri::InvalidUri),

    /// Bb8 run error
    #[error("Bb8 run error")]
    Bb8(String),

    /// Postgres error
    #[error("Postgres {0}")]
    Postgres(#[from] tokio_postgres::Error),

    /// Tungstenite error
    #[error("Tungstenite {0}")]
    Tungstenite(#[from] tungstenite::Error),

    /// UTF-8 conversion error
    #[error("Utf-8 {0}")]
    FromUtf8(#[from] FromUtf8Error),

    /// JSON deserializing error
    #[error("JSON {0}")]
    SerdeJson(#[from] serde_json::Error),

    /// I/O error
    #[error("IO {0}")]
    Io(#[from] std::io::Error),

    /// Stream closed error
    #[error("Stream Closed")]
    StreamClosed,
}

impl<E: std::fmt::Debug> From<bb8::RunError<E>> for Error {
    fn from(err: bb8::RunError<E>) -> Self {
        Self::Bb8(format!("{err:?}"))
    }
}

/// Pollinator result
pub type Result<T> = std::result::Result<T, Error>;
