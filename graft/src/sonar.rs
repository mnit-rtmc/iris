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
use rustls::client::danger::{
    HandshakeSignatureValid, ServerCertVerified, ServerCertVerifier,
};
use rustls::pki_types::{CertificateDer, ServerName, UnixTime};
use rustls::{DigitallySignedStruct, SignatureScheme};
use std::net::ToSocketAddrs;
use std::sync::Arc;
use std::time::Duration;
use tokio::io::{self, AsyncReadExt, AsyncWriteExt, ErrorKind};
use tokio::net::TcpStream;
use tokio::time::timeout;
use tokio_rustls::client::TlsStream;
use tokio_rustls::TlsConnector;

/// Sonar protocol error
#[derive(Debug, thiserror::Error)]
pub enum Error {
    /// Unexpected response to a message
    #[error("unexpected response")]
    UnexpectedResponse,

    /// Unexpected message received
    #[error("unexpected message")]
    UnexpectedMessage,

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

/// Sonar result
pub type Result<T> = std::result::Result<T, Error>;

/// TLS certificate verifier for IRIS server
#[derive(Debug)]
struct Verifier {}

impl ServerCertVerifier for Verifier {
    fn verify_server_cert(
        &self,
        _: &CertificateDer,
        _: &[CertificateDer],
        _: &ServerName,
        _: &[u8],
        _: UnixTime,
    ) -> std::result::Result<ServerCertVerified, rustls::Error> {
        // NOTE: we are *NOT* checking the server cert here!  This is only
        //       secure when graft is running on the same host as IRIS.
        Ok(ServerCertVerified::assertion())
    }

    fn verify_tls12_signature(
        &self,
        _: &[u8],
        _: &CertificateDer<'_>,
        _: &DigitallySignedStruct,
    ) -> std::result::Result<HandshakeSignatureValid, rustls::Error> {
        Ok(HandshakeSignatureValid::assertion())
    }

    fn verify_tls13_signature(
        &self,
        _: &[u8],
        _: &CertificateDer<'_>,
        _: &DigitallySignedStruct,
    ) -> std::result::Result<HandshakeSignatureValid, rustls::Error> {
        Ok(HandshakeSignatureValid::assertion())
    }

    fn supported_verify_schemes(&self) -> Vec<SignatureScheme> {
        use SignatureScheme::*;
        [
            RSA_PKCS1_SHA1,
            RSA_PKCS1_SHA256,
            ECDSA_NISTP256_SHA256,
            RSA_PKCS1_SHA384,
            ECDSA_NISTP384_SHA384,
            RSA_PKCS1_SHA512,
            ECDSA_NISTP521_SHA512,
            RSA_PSS_SHA256,
            RSA_PSS_SHA384,
            RSA_PSS_SHA512,
            ED25519,
            ED448,
        ]
        .into()
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
            Self::UnexpectedMessage
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

impl Connection {
    /// Create a new connection to a sonar server
    pub async fn new(host: &str, port: u16) -> Result<Self> {
        let addr = (host, port)
            .to_socket_addrs()?
            .next()
            .ok_or_else(|| io::Error::from(ErrorKind::NotFound))?;
        let tcp_stream = TcpStream::connect(&addr).await?;
        let cfg = rustls::ClientConfig::builder()
            .dangerous()
            .with_custom_certificate_verifier(Arc::new(Verifier {}))
            .with_no_client_auth();
        let connector = TlsConnector::from(Arc::new(cfg));
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
            _ => Err(Error::UnexpectedMessage),
        })
        .await?;
        self.recv(|m| match m {
            Message::Show(txt) => {
                msg.push_str(txt);
                Ok(())
            }
            _ => Err(Error::UnexpectedMessage),
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
                _ => Err(Error::UnexpectedMessage),
            })
            .await?;
        }
        Ok(())
    }
}
