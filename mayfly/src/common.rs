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
use std::fmt::{Display, Write};
use tide::{Response, StatusCode};
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
    /// Not found
    #[error("Not found")]
    NotFound,
    /// File exists
    #[error("File exists")]
    FileExists,
    /// Zip error
    #[error("Zip error")]
    Zip(#[from] ZipError),
}

pub type Result<T> = std::result::Result<T, Error>;

/// JSON body (array of values)
#[derive(Default)]
pub struct Body {
    /// Max age in seconds
    max_age: Option<u64>,
    /// Response body
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
    /// Set the max-age
    pub fn with_max_age(mut self, max_age: Option<u64>) -> Self {
        self.max_age = max_age;
        self
    }

    /// Start a new JSON value
    pub fn start_value(&mut self) {
        if self.body.is_empty() {
            self.push('[');
        } else {
            self.push(',');
        }
    }

    /// Push a char
    pub fn push(&mut self, c: char) {
        self.body.push(c);
    }

    /// Write to body
    pub fn write<T: Display>(&mut self, value: T) {
        write!(self.body, "{}", value).unwrap();
    }
}

impl From<Body> for String {
    fn from(body: Body) -> Self {
        let mut v = body.body;
        if v.is_empty() {
            v.push_str("[]");
        } else {
            v.push(']');
        }
        v
    }
}

impl From<Body> for tide::Result {
    fn from(body: Body) -> Self {
        if let Some(max_age) = body.max_age {
            Ok(Response::builder(StatusCode::Ok)
                .content_type("application/json")
                .body(String::from(body))
                .header("cache-control", &format!("max-age={}", max_age))
                .build())
        } else {
            Ok(Response::builder(StatusCode::Ok)
                .content_type("application/json")
                .body(String::from(body))
                .build())
        }
    }
}
