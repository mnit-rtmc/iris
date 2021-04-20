// common.rs
//
// Copyright (c) 2019-2021  Minnesota Department of Transportation
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
use async_std::io;
use serde::Serialize;
use tide::{Response, StatusCode};
use zip::result::ZipError;

/// Error enum
#[derive(Debug, thiserror::Error)]
pub enum Error {
    /// I/O error
    #[error("I/O {0}")]
    Io(#[from] io::Error),
    /// Serde JSON error
    #[error("JSON serialization")]
    Json(#[from] serde_json::Error),
    /// Invalid query
    #[error("Invalid query")]
    InvalidQuery,
    /// Invalid date
    #[error("Invalid date")]
    InvalidDate,
    /// Invalid data
    #[error("Invalid data")]
    InvalidData,
    /// Not found
    #[error("Not Found")]
    NotFound,
    /// Zip error
    #[error("Zip error")]
    Zip(#[from] ZipError),
}

pub type Result<T> = std::result::Result<T, Error>;

/// JSON body (array of values)
#[derive(Default)]
pub struct Body {
    body: String,
}

impl Error {
    /// Get HTTP status code
    fn status_code(&self) -> StatusCode {
        match self {
            Self::NotFound => StatusCode::NotFound,
            Self::InvalidQuery => StatusCode::BadRequest,
            Self::InvalidDate => StatusCode::BadRequest,
            _ => StatusCode::InternalServerError,
        }
    }
}

impl From<Error> for tide::Result {
    fn from(err: Error) -> Self {
        Ok(Response::builder(err.status_code())
            .body(err.to_string())
            .build())
    }
}

/// Build JSON response from data
impl Body {
    pub fn push<T: Serialize>(&mut self, value: T) -> Result<()> {
        if self.body.len() > 0 {
            self.body.push(',');
        } else {
            self.body.push('[');
        }
        let j = serde_json::to_string(&value)?;
        self.body.push_str(&j);
        Ok(())
    }
}

impl From<Body> for String {
    fn from(body: Body) -> Self {
        let mut v = body.body;
        if v.len() > 0 {
            v.push(']');
        } else {
            v.push_str("[]");
        }
        v
    }
}

impl From<Body> for tide::Result {
    fn from(body: Body) -> Self {
        Ok(Response::builder(StatusCode::Ok)
            .content_type("application/json")
            .body(String::from(body))
            .build())
    }
}