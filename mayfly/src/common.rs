// common.rs
//
// Copyright (c) 2019-2024  Minnesota Department of Transportation
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
use std::io;
use zip::result::ZipError;

/// Error enum
#[derive(Debug, thiserror::Error)]
pub enum Error {
    /// I/O error
    #[error("I/O {0}")]
    Io(#[from] io::Error),

    /// Invalid query
    #[error("Invalid query")]
    InvalidQuery,

    /// Invalid date
    #[error("Invalid date")]
    InvalidDate,

    /// Invalid data
    #[error("Invalid data")]
    InvalidData,

    /// Invalid stamp
    #[error("Invalid stamp")]
    InvalidStamp,

    /// Tokio join error
    #[error("Join {0}")]
    Join(#[from] tokio::task::JoinError),

    /// Not found
    #[error("Not found")]
    NotFound,

    /// File exists
    #[error("File exists")]
    FileExists,

    /// Serde JSON
    #[error("Json {0}")]
    Json(#[from] serde_json::Error),

    /// Zip error
    #[error("Zip error")]
    Zip(#[from] ZipError),
}

/// Result type
pub type Result<T> = std::result::Result<T, Error>;

impl IntoResponse for Error {
    fn into_response(self) -> Response {
        let status = match self {
            Self::NotFound => StatusCode::NOT_FOUND,
            Self::InvalidQuery => StatusCode::BAD_REQUEST,
            Self::InvalidDate => StatusCode::BAD_REQUEST,
            _ => StatusCode::INTERNAL_SERVER_ERROR,
        };
        (status, status.canonical_reason().unwrap_or("WTF")).into_response()
    }
}
