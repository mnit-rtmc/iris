// error.rs
//
// Copyright (c) 2019  Minnesota Department of Transportation
//
use base64::DecodeError;
use crate::multi::SyntaxError;
use gift::EncodeError;
use postgres;
use serde_json;
use std::error::Error as _;
use std::{fmt, io};

/// Enum for all honeybee errors
#[derive(Debug)]
pub enum Error {
    Io(io::Error),
    Pg(postgres::Error),
    MultiSyntax(SyntaxError),
    Base64Decode(DecodeError),
    SerdeJson(serde_json::Error),
    EncodeError(EncodeError),
    Other(String),
}

impl fmt::Display for Error {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        if let Some(src) = self.source() {
            fmt::Display::fmt(src, f)
        } else if let Error::Other(e) = self {
            write!(f, "Error {}", e)
        } else {
            unreachable!();
        }
    }
}

impl std::error::Error for Error {
    fn source(&self) -> Option<&(dyn std::error::Error + 'static)> {
        match self {
            Error::Io(e) => Some(e),
            Error::Pg(e) => Some(e),
            Error::MultiSyntax(e) => Some(e),
            Error::Base64Decode(e) => Some(e),
            Error::SerdeJson(e) => Some(e),
            Error::EncodeError(e) => Some(e),
            Error::Other(_) => None,
        }
    }
}

impl From<io::Error> for Error {
    fn from(e: io::Error) -> Self {
        Error::Io(e)
    }
}

impl From<postgres::Error> for Error {
    fn from(e: postgres::Error) -> Self {
        Error::Pg(e)
    }
}

impl From<SyntaxError> for Error {
    fn from(e: SyntaxError) -> Self {
        Error::MultiSyntax(e)
    }
}

impl From<DecodeError> for Error {
    fn from(e: DecodeError) -> Self {
        Error::Base64Decode(e)
    }
}

impl From<serde_json::Error> for Error {
    fn from(e: serde_json::Error) -> Self {
        Error::SerdeJson(e)
    }
}

impl From<EncodeError> for Error {
    fn from(e: EncodeError) -> Self {
        Error::EncodeError(e)
    }
}
