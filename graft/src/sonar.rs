use anyhow::{bail, Result};
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
    tls_stream: TlsStream<TcpStream>,
    timeout: Duration,
    buf: Vec<u8>,
    offset: usize,
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
        if msg.len() < 1 {
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
            timeout: Duration::from_secs(5),
            buf: vec![0; 32768],
            offset: 0,
            count: 0,
        })
    }

    /// Send a message to the server
    pub async fn send(&mut self, req: &[u8]) -> Result<()> {
        self.tls_stream.write_all(req).await?;
        Ok(())
    }

    /// Receive a message from the server
    pub async fn recv<F>(&mut self, mut callback: F) -> Result<()>
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
                None => io::timeout(self.timeout, self.read()).await?,
            }
        }
    }

    /// Check for an error message from the server
    async fn check_error(&mut self) -> Result<()> {
        match io::timeout(Duration::from_millis(10), self.read()).await {
            Err(ref e) if e.kind() == io::ErrorKind::TimedOut => Ok(()),
            Err(e) => Err(e),
            Ok(()) => Ok(()),
        }?;
        match Message::decode(self.received()) {
            Some((res, _c)) => bail!("{:?}", res),
            None => Ok(()),
        }
    }

    /// Get buffer of received data
    fn received(&self) -> &[u8] {
        &self.buf[self.offset..self.offset + self.count]
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
        self.count += self.tls_stream.read(&mut self.buf[offset..]).await?;
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
            Message::Show(txt) => bail!("{}", txt),
            _ => bail!("Expected `Type` message"),
        })
        .await?;
        self.recv(|m| match m {
            Message::Show(txt) => {
                msg.push_str(txt);
                Ok(())
            }
            _ => bail!("Expected `Show` message"),
        })
        .await?;
        Ok(msg)
    }

    /// Create an object
    pub async fn create_object(&mut self, nm: &str) -> Result<()> {
        let mut buf = vec![];
        Message::Object(nm).encode(&mut buf);
        self.check_error().await?;
        self.send(&buf[..]).await?;
        Ok(())
    }

    /// Remove an object
    pub async fn remove_object(&mut self, nm: &str) -> Result<()> {
        let mut buf = vec![];
        Message::Remove(nm).encode(&mut buf);
        self.check_error().await?;
        self.send(&buf[..]).await?;
        Ok(())
    }
}
