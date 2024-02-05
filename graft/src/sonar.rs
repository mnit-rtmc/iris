// sonar.rs
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
use crate::restype::ResType;
use crate::tls;
use convert_case::{Case, Casing};
use http::StatusCode;
use percent_encoding::percent_decode_str;
use rustls::pki_types::ServerName;
use std::fmt;
use std::net::ToSocketAddrs;
use std::time::Duration;
use tokio::io::{self, AsyncReadExt, AsyncWriteExt, ErrorKind};
use tokio::net::TcpStream;
use tokio::time::timeout;
use tokio_rustls::client::TlsStream;

/// Sonar protocol error
#[derive(Debug, thiserror::Error)]
pub enum Error {
    /// Unexpected response received
    #[error("unexpected response")]
    UnexpectedResponse,

    /// Invalid value (invalid characters, etc)
    #[error("invalid value")]
    InvalidValue,

    /// Conflict (name already exists)
    #[error("name conflict")]
    Conflict,

    /// Forbidden (permission denied)
    #[error("forbidden")]
    Forbidden,

    /// Not found
    #[error("not found")]
    NotFound,

    /// Timed out
    #[error("timed out")]
    TimedOut,

    /// I/O error
    #[error("I/O {0}")]
    IO(#[from] std::io::Error),
}

impl From<Error> for StatusCode {
    fn from(e: Error) -> Self {
        match e {
            Error::UnexpectedResponse => StatusCode::INTERNAL_SERVER_ERROR,
            Error::InvalidValue => StatusCode::BAD_REQUEST,
            Error::Conflict => StatusCode::CONFLICT,
            Error::Forbidden => StatusCode::FORBIDDEN,
            Error::NotFound => StatusCode::NOT_FOUND,
            Error::TimedOut => StatusCode::GATEWAY_TIMEOUT,
            Error::IO(e) => {
                if e.kind() == io::ErrorKind::NotFound {
                    StatusCode::NOT_FOUND
                } else if e.kind() == io::ErrorKind::TimedOut {
                    StatusCode::GATEWAY_TIMEOUT
                } else {
                    StatusCode::INTERNAL_SERVER_ERROR
                }
            }
        }
    }
}

impl Error {
    /// Parse a SHOW message received from server
    fn parse_show(msg: &str) -> Self {
        // gross, but no point in changing SHOW messages now!
        let msg = msg.to_lowercase();
        if msg.starts_with("permission") {
            Self::Forbidden
        } else if msg.starts_with("invalid name") {
            // this should really have been "Unknown name", not "Invalid name"
            Self::NotFound
        } else if msg.starts_with("invalid")
            || msg.starts_with("bad")
            || msg.starts_with("must not" /* contain upper-case ... */)
        {
            Self::InvalidValue
        } else if msg.starts_with("name already exists")
            || msg.starts_with("must be removed")
            || msg.starts_with("cannot")
            || msg.starts_with("already")
            || msg.starts_with("unavailable pin")
               // SQL constraint on delete
            || msg.contains("foreign key")
               // "Drop X exists"
            || msg.contains("exists")
        {
            Self::Conflict
        } else {
            log::warn!("SHOW {}", msg);
            Self::UnexpectedResponse
        }
    }
}

/// Sonar result
pub type Result<T> = std::result::Result<T, Error>;

/// Invalid characters for SONAR names
const INVALID_CHARS: &[char] = &['\0', '/', '\u{001e}', '\u{001f}'];

/// Check if a character in a Sonar name is invalid
fn invalid_char(c: char) -> bool {
    INVALID_CHARS.contains(&c)
}

/// Sonar type / object name
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct Name {
    /// Resource type
    pub res_type: ResType,

    /// Full name (type / object)
    name: String,
}

impl From<ResType> for Name {
    fn from(res_type: ResType) -> Self {
        Name {
            res_type,
            name: res_type.as_str().to_string(),
        }
    }
}

impl fmt::Display for Name {
    fn fmt(
        &self,
        f: &mut fmt::Formatter<'_>,
    ) -> std::result::Result<(), fmt::Error> {
        write!(f, "{}", self.name)
    }
}

impl Name {
    /// Create a new name
    pub fn new(type_n: &str) -> Result<Self> {
        Ok(Name::from(ResType::try_from(type_n)?))
    }

    /// Set object name (with validation, not percent-encoded)
    pub fn obj_raw(mut self, obj_n: &str) -> Result<Self> {
        if obj_n.len() > 64 || obj_n.contains(invalid_char) {
            Err(Error::InvalidValue)?
        } else {
            let type_n = self.type_n();
            self.name = format!("{type_n}/{obj_n}");
            Ok(self)
        }
    }

    /// Set object name from percent-encoded value
    pub fn obj(self, obj_n: &str) -> Result<Self> {
        self.obj_raw(
            &percent_decode_str(obj_n)
                .decode_utf8()
                .or(Err(Error::InvalidValue))?,
        )
    }

    /// Get resource type name
    pub fn type_n(&self) -> &'static str {
        self.res_type.as_str()
    }

    /// Get object name
    pub fn object_n(&self) -> Result<&str> {
        self.name.splitn(2, '/').nth(1).ok_or(Error::InvalidValue)
    }

    /// Make a Sonar attribute (with validation)
    pub fn attr(&self, att: &str) -> Result<String> {
        if att.len() > 64 || att.contains(invalid_char) {
            Err(Error::InvalidValue)?
        } else if self.res_type == ResType::Controller && att == "drop_id" {
            Ok(format!("{self}/drop"))
        } else if self.res_type == ResType::SignMessage {
            // sign_message attributes are in snake case
            Ok(format!("{self}/{att}"))
        } else {
            // most IRIS attributes are in camel case (Java)
            Ok(format!("{self}/{}", att.to_case(Case::Camel)))
        }
    }
}

/// Sonar message
#[allow(dead_code)]
#[derive(Debug)]
enum Message<'a> {
    /// Client login request
    Login(&'a str, &'a str),

    /// Client quit request
    Quit(),

    /// Client enumerate request
    Enumerate(&'a str),

    /// Client ignore request
    Ignore(&'a str),

    /// Client password change request
    Password(&'a str, &'a str),

    /// Object create request (client) / list (server)
    Object(&'a str),

    /// Attribute set request (client) / change (server)
    Attribute(&'a str, &'a str),

    /// Object remove request (client) / change (server)
    Remove(&'a str),

    /// Server type change
    Type(&'a str),

    /// Server show message
    Show(&'a str),
}

impl<'a> Message<'a> {
    /// Decode message in a buffer
    pub fn decode(buf: &'a [u8]) -> Option<(Self, usize)> {
        if let Some(rec_sep) = buf.iter().position(|b| *b == b'\x1E') {
            if let Some(msg) = Self::decode_one(&buf[..rec_sep]) {
                return Some((msg, rec_sep));
            }
        }
        None
    }

    /// Decode one message
    fn decode_one(buf: &'a [u8]) -> Option<Self> {
        let msg: Vec<&[u8]> = buf.split(|b| *b == b'\x1F').collect();
        if msg.is_empty() {
            return None;
        }
        let code = msg[0];
        match code {
            b"s" if msg.len() == 2 => {
                let txt = std::str::from_utf8(msg[1]).unwrap();
                Some(Message::Show(txt))
            }
            b"t" if msg.len() == 1 => Some(Message::Type("")),
            b"t" if msg.len() == 2 => {
                let nm = std::str::from_utf8(msg[1]).unwrap();
                Some(Message::Type(nm))
            }
            b"o" if msg.len() == 2 => {
                let nm = std::str::from_utf8(msg[1]).unwrap();
                Some(Message::Object(nm))
            }
            b"a" if msg.len() == 3 => {
                let nm = std::str::from_utf8(msg[1]).unwrap();
                let a = std::str::from_utf8(msg[2]).unwrap();
                Some(Message::Attribute(nm, a))
            }
            b"r" if msg.len() == 2 => {
                let nm = std::str::from_utf8(msg[1]).unwrap();
                Some(Message::Remove(nm))
            }
            _ => None,
        }
    }

    /// Encode the message to a buffer
    pub fn encode(&'a self, buf: &mut Vec<u8>) {
        match self {
            Message::Login(name, pword) => {
                buf.push(b'l');
                buf.push(b'\x1F');
                buf.extend(name.as_bytes());
                buf.push(b'\x1F');
                buf.extend(pword.as_bytes());
            }
            Message::Password(old, new) => {
                buf.push(b'p');
                buf.push(b'\x1F');
                buf.extend(old.as_bytes());
                buf.push(b'\x1F');
                buf.extend(new.as_bytes());
            }
            Message::Quit() => buf.push(b'q'),
            Message::Enumerate(nm) => {
                buf.push(b'e');
                buf.push(b'\x1F');
                buf.extend(nm.as_bytes());
            }
            Message::Ignore(nm) => {
                buf.push(b'i');
                buf.push(b'\x1F');
                buf.extend(nm.as_bytes());
            }
            Message::Object(nm) => {
                buf.push(b'o');
                buf.push(b'\x1F');
                buf.extend(nm.as_bytes());
            }
            Message::Attribute(nm, a) => {
                buf.push(b'a');
                buf.push(b'\x1F');
                buf.extend(nm.as_bytes());
                buf.push(b'\x1F');
                buf.extend(a.as_bytes());
            }
            Message::Remove(nm) => {
                buf.push(b'r');
                buf.push(b'\x1F');
                buf.extend(nm.as_bytes());
            }
            _ => unimplemented!(),
        }
        buf.push(b'\x1E');
    }
}

/// Connection to a sonar server
pub struct Connection {
    /// TLS encrypted stream
    tls_stream: TlsStream<TcpStream>,
    /// Network timeout
    timeout: Duration,
    /// Received data buffer
    rx_buf: Vec<u8>,
    /// Offset into buffer
    offset: usize,
    /// Count of bytes in buffer
    count: usize,
}

impl Connection {
    /// Create a new connection to a sonar server
    pub async fn new(host: &str, port: u16) -> Result<Self> {
        let addr = (host, port)
            .to_socket_addrs()?
            .next()
            .ok_or_else(|| io::Error::from(ErrorKind::NotFound))?;
        let tcp_stream = TcpStream::connect(&addr).await?;
        let connector = tls::connector();
        let domain = ServerName::try_from(host)
            .map_err(|_| {
                io::Error::new(io::ErrorKind::InvalidInput, "invalid dnsname")
            })?
            .to_owned();
        let tls_stream = connector.connect(domain, tcp_stream).await?;
        Ok(Connection {
            tls_stream,
            timeout: Duration::from_secs(5),
            rx_buf: vec![0; 32_768],
            offset: 0,
            count: 0,
        })
    }

    /// Send a message to the server
    pub async fn send(&mut self, req: &[u8]) -> Result<()> {
        match timeout(self.timeout, self.tls_stream.write_all(req)).await {
            Ok(res) => Ok(res?),
            Err(_e) => Err(Error::TimedOut),
        }
    }

    /// Receive a message from the server
    async fn recv<F>(&mut self, mut callback: F) -> Result<()>
    where
        F: FnMut(Message) -> Result<()>,
    {
        loop {
            match Message::decode(self.received()) {
                Some((res, c)) => {
                    let r = callback(res);
                    self.consume(c);
                    return r;
                }
                None => match timeout(self.timeout, self.read()).await {
                    Ok(res) => res?,
                    Err(_e) => return Err(Error::TimedOut),
                },
            }
        }
    }

    /// Check for an error message from the server
    async fn check_error(&mut self, ms: u64) -> Result<()> {
        if let Ok(res) = timeout(Duration::from_millis(ms), self.read()).await {
            res?;
        }
        match Message::decode(self.received()) {
            Some((Message::Show(txt), _)) => Err(Error::parse_show(txt)),
            Some((_res, _c)) => Err(Error::UnexpectedResponse),
            None => Ok(()),
        }
    }

    /// Get buffer of received data
    fn received(&self) -> &[u8] {
        &self.rx_buf[self.offset..self.offset + self.count]
    }

    /// Consume buffered data
    fn consume(&mut self, c: usize) {
        if c < self.count {
            self.count -= c;
            self.offset += c + 1;
        } else {
            self.count = 0;
            self.offset = 0;
        }
    }

    /// Read data from the server
    async fn read(&mut self) -> io::Result<()> {
        let offset = self.offset;
        self.count += self.tls_stream.read(&mut self.rx_buf[offset..]).await?;
        Ok(())
    }

    /// Login to the server
    pub async fn login(&mut self, name: &str, pword: &str) -> Result<String> {
        let mut msg = String::new();
        let mut buf = vec![];
        Message::Login(name, pword).encode(&mut buf);
        self.send(&buf[..]).await?;
        self.recv(|m| match m {
            Message::Type("") => Ok(()),
            Message::Show(txt) => Err(Error::parse_show(txt)),
            _ => Err(Error::UnexpectedResponse),
        })
        .await?;
        self.recv(|m| match m {
            Message::Show(txt) => {
                msg.push_str(txt);
                Ok(())
            }
            _ => Err(Error::UnexpectedResponse),
        })
        .await?;
        log::info!("Logged in from {}", msg);
        Ok(msg)
    }

    /// Create an object
    pub async fn create_object(&mut self, nm: &str) -> Result<()> {
        let mut buf = vec![];
        Message::Object(nm).encode(&mut buf);
        self.check_error(10).await?;
        self.send(&buf[..]).await?;
        self.check_error(100).await?;
        Ok(())
    }

    /// Update an object
    pub async fn update_object(&mut self, nm: &str, a: &str) -> Result<()> {
        let mut buf = vec![];
        Message::Attribute(nm, a).encode(&mut buf);
        self.check_error(10).await?;
        self.send(&buf[..]).await?;
        self.check_error(100).await?;
        Ok(())
    }

    /// Remove an object
    pub async fn remove_object(&mut self, nm: &str) -> Result<()> {
        let mut buf = vec![];
        Message::Remove(nm).encode(&mut buf);
        self.check_error(10).await?;
        self.send(&buf[..]).await?;
        self.check_error(100).await?;
        Ok(())
    }

    /// Enumerate an object
    pub async fn enumerate_object<F>(
        &mut self,
        nm: &str,
        mut callback: F,
    ) -> Result<()>
    where
        F: FnMut(&str, &str) -> Result<()>,
    {
        let mut buf = vec![];
        Message::Enumerate(nm).encode(&mut buf);
        self.check_error(10).await?;
        self.send(&buf[..]).await?;
        let mut done = false;
        while !done {
            self.recv(|m| match m {
                Message::Attribute(attr, v) => {
                    let att = attr.rsplit('/').next().unwrap();
                    callback(att, v)
                }
                Message::Object(_) => {
                    done = true;
                    Ok(())
                }
                Message::Show(msg) => Err(Error::parse_show(msg)),
                _ => Err(Error::UnexpectedResponse),
            })
            .await?;
        }
        Ok(())
    }
}
