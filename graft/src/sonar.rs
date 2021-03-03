use anyhow::Result;
use async_std::io;
use async_std::net::{TcpStream, ToSocketAddrs};
use async_std::prelude::*;
use async_tls::{client::TlsStream, TlsConnector};
use rustls::{
    Certificate, ClientConfig, RootCertStore, ServerCertVerified,
    ServerCertVerifier, TLSError,
};
use std::sync::Arc;
use std::time::Duration;
use webpki::DNSNameRef;

struct Verifier {}

impl ServerCertVerifier for Verifier {
    fn verify_server_cert(
        &self,
        _: &RootCertStore,
        _: &[Certificate],
        _: DNSNameRef,
        _: &[u8],
    ) -> std::result::Result<ServerCertVerified, TLSError> {
        // TODO: check cert
        return Ok(ServerCertVerified::assertion());
    }
}

#[allow(dead_code)]
#[derive(Debug)]
pub enum Message<'a> {
    Login(&'a str, &'a str),
    Quit(),
    Enumerate(&'a str),
    Ignore(&'a str),
    Password(&'a str, &'a str),
    Object(&'a str),
    Attribute(&'a str, &'a str),
    Remove(&'a str),
    Type(&'a str),
    Show(&'a str),
}

pub struct Connection {
    tls_stream: TlsStream<TcpStream>,
    buf: Vec<u8>,
    offset: usize,
    count: usize,
}

impl<'a> Message<'a> {
    pub fn decode(buf: &'a [u8]) -> Option<(Self, usize)> {
        if let Some(rec_sep) = buf.iter().position(|b| *b == b'\x1E') {
            if let Some(msg) = Self::decode_one(&buf[..rec_sep]) {
                return Some((msg, rec_sep));
            }
        }
        None
    }

    fn decode_one(buf: &'a [u8]) -> Option<Self> {
        let msg: Vec<&[u8]> = buf.split(|b| *b == b'\x1F').collect();
        if msg.len() < 1 {
            return None;
        }
        let code = msg[0];
        match code {
            b"l" if msg.len() == 3 => {
                let name = std::str::from_utf8(msg[1]).unwrap();
                let pword = std::str::from_utf8(msg[2]).unwrap();
                Some(Message::Login(name, pword))
            }
            b"t" if msg.len() == 1 => Some(Message::Type("")),
            b"t" if msg.len() == 2 => {
                let nm = std::str::from_utf8(msg[1]).unwrap();
                Some(Message::Type(nm))
            }
            b"s" if msg.len() == 2 => {
                let txt = std::str::from_utf8(msg[1]).unwrap();
                Some(Message::Show(txt))
            }
            _ => None,
        }
    }

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
            Message::Type(nm) => {
                buf.push(b't');
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
            Message::Show(msg) => {
                buf.push(b's');
                buf.push(b'\x1F');
                buf.extend(msg.as_bytes());
            }
        }
        buf.push(b'\x1E');
    }
}

impl Connection {
    pub async fn new(host: &str, port: u16) -> Result<Self> {
        let addr = (host, port)
            .to_socket_addrs()
            .await?
            .next()
            .ok_or_else(|| io::Error::from(io::ErrorKind::NotFound))?;
        let tcp_stream = TcpStream::connect(&addr).await?;
        let mut cfg = ClientConfig::new();
        cfg.dangerous()
            .set_certificate_verifier(Arc::new(Verifier {}));
        let connector = TlsConnector::from(Arc::new(cfg));
        let tls_stream = connector.connect(host, tcp_stream).await?;
        Ok(Connection {
            tls_stream,
            buf: vec![0; 32768],
            offset: 0,
            count: 0,
        })
    }

    pub async fn send(&mut self, req: &[u8]) -> Result<()> {
        self.tls_stream.write_all(req).await?;
        Ok(())
    }

    pub async fn recv(
        &mut self,
        callback: fn(Message) -> Result<()>,
    ) -> Result<()> {
        loop {
            let buf = &self.buf[self.offset..self.offset + self.count];
            match Message::decode(&buf) {
                Some((res, c)) => {
                    callback(res)?;
                    self.count -= c;
                    if self.count > 0 {
                        self.offset += c + 1;
                    } else {
                        self.offset = 0;
                    }
                    return Ok(());
                }
                None => {
                    let offset = self.offset;
                    self.count += io::timeout(Duration::from_secs(5), async {
                        self.tls_stream.read(&mut self.buf[offset..]).await
                    })
                    .await?;
                }
            }
        }
    }
}
