// error.rs
//
// Copyright (c) 2019  Minnesota Department of Transportation
//
use base64::DecodeError;
use gift::EncodeError;
use ntcip::dms::multi::SyntaxError;
use std::error::Error as _;
use std::{fmt, io};
use std::num::TryFromIntError;

/// Enum for all honeybee errors
#[derive(Debug)]
pub enum Error {
    Io(io::Error),
    Pg(postgres::Error),
    MultiSyntax(SyntaxError),
    Base64Decode(DecodeError),
    EncodeError(EncodeError),
    SerdeJson(serde_json::Error),
    TryFromInt(TryFromIntError),
    UnknownResource(String),
}

/// Result type
pub type Result<T> = std::result::Result<T, Error>;

impl fmt::Display for Error {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        if let Some(src) = self.source() {
            fmt::Display::fmt(src, f)
        } else if let Error::UnknownResource(e) = self {
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
            Error::TryFromInt(e) => Some(e),
            Error::UnknownResource(_) => None,
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

impl From<TryFromIntError> for Error {
    fn from(e: TryFromIntError) -> Self {
        Error::TryFromInt(e)
    }
}
