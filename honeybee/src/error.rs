// error.rs
//
// Copyright (C) 2021-2024  Minnesota Department of Transportation
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
use http::StatusCode;
use std::io::ErrorKind;
use std::time::SystemTimeError;

/// Honeybee error
#[derive(Debug, thiserror::Error)]
pub enum Error {
    /// Resin error
    #[error("Resin {0}")]
    Resin(#[from] resin::Error),

    /// Unauthenticated request
    #[error("Unauthenticated")]
    Unauthenticated,

    /// Forbidden (permission denied)
    #[error("Forbidden")]
    Forbidden,

    /// Unexpected sonar response received
    #[error("unexpected sonar response")]
    UnexpectedResponse,

    /// Not found
    #[error("not found")]
    NotFound,

    /// Conflict (name already exists)
    #[error("name conflict")]
    Conflict,

    /// Invalid value (invalid characters, etc)
    #[error("invalid value")]
    InvalidValue,

    /// Timed out
    #[error("timed out")]
    TimedOut,

    /// Invalid ETag error
    #[error("Invalid ETag")]
    InvalidETag,

    /// System time error
    #[error("Time {0}")]
    SystemTime(#[from] SystemTimeError),

    /// IO error
    #[error("IO {0}")]
    Io(#[from] std::io::Error),

    /// Postgres error
    #[error("Postgres {0}")]
    Postgres(#[from] tokio_postgres::Error),

    /// Tokio join error
    #[error("Join {0}")]
    Join(#[from] tokio::task::JoinError),

    /// Loam error
    #[error("failed to read/write loam file: {0}")]
    Loam(#[from] loam::Error),

    /// Tower sessions
    #[error("Session {0}")]
    Session(#[from] tower_sessions::session::Error),

    /// Unknown resource
    #[error("Unknown resource {0}")]
    UnknownResource(String),

    /// Rendzina error
    #[error("Rendzina {0}")]
    Rendzina(#[from] rendzina::Error),

    /// NTCIP sign error
    #[error("NTCIP {0}")]
    Sign(#[from] ntcip::dms::SignError),

    /// Serde JSON
    #[error("Json {0}")]
    Json(#[from] serde_json::Error),
}

impl From<Error> for StatusCode {
    fn from(err: Error) -> Self {
        match err {
            Error::Unauthenticated => StatusCode::UNAUTHORIZED,
            Error::Forbidden => StatusCode::FORBIDDEN,
            Error::UnexpectedResponse => StatusCode::INTERNAL_SERVER_ERROR,
            Error::NotFound => StatusCode::NOT_FOUND,
            Error::Conflict => StatusCode::CONFLICT,
            Error::InvalidValue => StatusCode::BAD_REQUEST,
            Error::TimedOut => StatusCode::GATEWAY_TIMEOUT,
            Error::Io(e) => {
                if e.kind() == ErrorKind::TimedOut {
                    StatusCode::GATEWAY_TIMEOUT
                } else {
                    StatusCode::INTERNAL_SERVER_ERROR
                }
            }
            _ => StatusCode::INTERNAL_SERVER_ERROR,
        }
    }
}

/// Honeybee result
pub type Result<T> = std::result::Result<T, Error>;
