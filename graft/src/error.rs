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
use crate::sonar::Error as SonarError;
use http::StatusCode;
use std::time::SystemTimeError;

/// Graft error
#[derive(Debug, thiserror::Error)]
pub enum Error {
    /// Unauthorized request
    #[error("Unauthorized")]
    Unauthorized,

    /// Invalid ETag error
    #[error("Invalid ETag")]
    InvalidETag,

    /// System time error
    #[error("Time {0}")]
    SystemTime(#[from] SystemTimeError),

    /// Sonar error
    #[error("Sonar {0}")]
    Sonar(#[from] SonarError),

    /// IO error
    #[error("IO {0}")]
    Io(#[from] std::io::Error),

    /// Postgres error
    #[error("Postgres {0}")]
    Postgres(#[from] tokio_postgres::Error),

    /// Tower sessions
    #[error("Session {0}")]
    Session(#[from] tower_sessions::session::Error),

    /// Bb8 run error
    #[error("Bb8 run error")]
    Bb8(String),
}

impl<E: std::fmt::Debug> From<bb8::RunError<E>> for Error {
    fn from(err: bb8::RunError<E>) -> Self {
        Self::Bb8(format!("{err:?}"))
    }
}

impl From<Error> for StatusCode {
    fn from(err: Error) -> Self {
        match err {
            Error::Unauthorized => StatusCode::UNAUTHORIZED,
            Error::Sonar(e) => e.into(),
            _ => StatusCode::INTERNAL_SERVER_ERROR,
        }
    }
}

/// Graft result
pub type Result<T> = std::result::Result<T, Error>;
