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

/// Graft error
#[derive(Debug, thiserror::Error)]
pub enum Error {
    /// Sonar error
    #[error("Sonar {0}")]
    Sonar(#[from] SonarError),

    /// Postgres error
    #[error("Postgres {0}")]
    Postgres(#[from] tokio_postgres::Error),

    /// BB8 run error
    #[error("BB8 run error")]
    Bb8,
}

impl<E> From<bb8::RunError<E>> for Error {
    fn from(_err: bb8::RunError<E>) -> Self {
        // FIXME: do the needful
        Self::Bb8
    }
}

/// Graft result
pub type Result<T> = std::result::Result<T, Error>;
