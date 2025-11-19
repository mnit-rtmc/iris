use hyper::StatusCode;
use hyper::header::InvalidHeaderValue;
use std::string::FromUtf8Error;

/// Pollinator errors
#[derive(Debug, thiserror::Error)]
pub enum Error {
    #[error("Hyper {0}")]
    Hyper(#[from] hyper::Error),

    #[error("Header {0}")]
    Header(#[from] InvalidHeaderValue),

    #[error("HTTP {0}")]
    Http(#[from] hyper::http::Error),

    #[error("HTTP status {0}")]
    HttpStatus(StatusCode),

    #[error("Join {0}")]
    Join(#[from] tokio::task::JoinError),

    #[error("Invalid URI {0}")]
    InvalidUri(#[from] hyper::http::uri::InvalidUri),

    #[error("Tungstenite {0}")]
    Tungstenite(#[from] tungstenite::Error),

    #[error("Utf-8 {0}")]
    FromUtf8(#[from] FromUtf8Error),

    #[error("JSON {0}")]
    SerdeJson(#[from] serde_json::Error),

    #[error("IO {0}")]
    Io(#[from] std::io::Error),

    #[error("Stream Closed")]
    StreamClosed,
}
