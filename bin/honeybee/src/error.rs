// error.rs
//
// Copyright (c) 2019 Minnesota Department of Transportation
//
use base64::DecodeError;
use postgres;
use serde_json;
use ssh2;
use std::error::Error as _;
use std::{fmt, io};
use std::path::PathBuf;
use std::sync::mpsc::{SendError, RecvError, TryRecvError};
use crate::multi::SyntaxError;

/// Enum for all honeybee errors
#[derive(Debug)]
pub enum Error {
    Io(io::Error),
    Pg(postgres::Error),
    MultiSyntax(SyntaxError),
    Base64Decode(DecodeError),
    SerdeJson(serde_json::Error),
    Ssh(ssh2::Error),
    MpscSend(SendError<PathBuf>),
    MpscRecv(RecvError),
    MpscTryRecv(TryRecvError),
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
            Error::Ssh(e) => Some(e),
            Error::MpscSend(e) => Some(e),
            Error::MpscRecv(e) => Some(e),
            Error::MpscTryRecv(e) => Some(e),
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

impl From<ssh2::Error> for Error {
    fn from(e: ssh2::Error) -> Self {
        Error::Ssh(e)
    }
}

impl From<SendError<PathBuf>> for Error {
    fn from(e: SendError<PathBuf>) -> Self {
        Error::MpscSend(e)
    }
}

impl From<RecvError> for Error {
    fn from(e: RecvError) -> Self {
        Error::MpscRecv(e)
    }
}

impl From<TryRecvError> for Error {
    fn from(e: TryRecvError) -> Self {
        Error::MpscTryRecv(e)
    }
}
