// error.rs
//
// Copyright (c) 2019-2025  Minnesota Department of Transportation
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
use axum::response::{IntoResponse, Response};
use http::StatusCode;
use std::io::{self, ErrorKind};
use zip::result::ZipError;

/// Error enum
#[derive(Debug, thiserror::Error)]
pub enum Error {
    /// Invalid query parameter
    #[error("Invalid {0} query")]
    InvalidQuery(&'static str),

    /// Invalid traffic data
    #[error("Invalid {0} data")]
    InvalidData(&'static str),

    /// Serde serialization error
    #[error("Serde {0}")]
    Serde(#[from] serde_json::Error),

    /// Tokio join error
    #[error("Join {0}")]
    Join(#[from] tokio::task::JoinError),

    /// I/O error
    #[error("I/O {0}")]
    Io(#[from] io::Error),

    /// Zip error
    #[error("Zip {0}")]
    Zip(#[from] ZipError),
}

/// Result type
pub type Result<T> = std::result::Result<T, Error>;

impl IntoResponse for Error {
    fn into_response(self) -> Response {
        let status = match self {
            Self::InvalidQuery(_) => StatusCode::BAD_REQUEST,
            Self::Io(e) if e.kind() == ErrorKind::TimedOut => {
                StatusCode::GATEWAY_TIMEOUT
            }
            Self::Io(e) if e.kind() == ErrorKind::NotFound => {
                StatusCode::NOT_FOUND
            }
            Self::Zip(ZipError::FileNotFound) => StatusCode::NOT_FOUND,
            _ => {
                log::warn!("Error processing request: {self}");
                StatusCode::INTERNAL_SERVER_ERROR
            }
        };
        (status, status.canonical_reason().unwrap_or("WTF")).into_response()
    }
}
