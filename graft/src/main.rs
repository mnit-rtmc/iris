use async_std::io;
use async_std::net::{TcpStream, ToSocketAddrs};
use async_std::prelude::*;
use async_std::task;
use async_tls::{TlsConnector, client::TlsStream};
use rustls::{
    Certificate, ClientConfig, RootCertStore, ServerCertVerified,
    ServerCertVerifier, TLSError,
};
use std::sync::Arc;
use std::time::Duration;
use webpki::DNSNameRef;

struct SonarVerifier {}

impl ServerCertVerifier for SonarVerifier {
    fn verify_server_cert(
        &self,
        _: &RootCertStore,
        _: &[Certificate],
        _: DNSNameRef,
        _: &[u8],
    ) -> Result<ServerCertVerified, TLSError> {
        // TODO: check cert
        return Ok(ServerCertVerified::assertion());
    }
}

#[derive(Debug)]
enum SonarMessage<'a> {
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

impl<'a> SonarMessage<'a> {
    fn decode(buf: &'a [u8]) -> (Option<Self>, &'a [u8]) {
        let msgs: Vec<&[u8]> = buf.splitn(2, |b| *b == b'\x1E').collect();
        if msgs.len() < 2 {
            return (None, buf);
        }
        let buf = msgs[1];
        let msg: Vec<&[u8]> = msgs[0].split(|b| *b == b'\x1F').collect();
        if msg.len() < 1 {
            return (None, buf);
        }
        let code = msg[0];
        match code {
            b"l" if msg.len() == 3 => {
                let name = std::str::from_utf8(msg[1]).unwrap();
                let pword = std::str::from_utf8(msg[2]).unwrap();
                (Some(SonarMessage::Login(name, pword)), buf)
            }
            b"t" if msg.len() == 1 => (Some(SonarMessage::Type("")), buf),
            b"t" if msg.len() == 2 => {
                let nm = std::str::from_utf8(msg[1]).unwrap();
                (Some(SonarMessage::Type(nm)), buf)
            }
            b"s" if msg.len() == 2 => {
                let txt = std::str::from_utf8(msg[1]).unwrap();
                (Some(SonarMessage::Show(txt)), buf)
            }
            _ => (None, buf)
        }
    }
    fn encode(&'a self, buf: &mut Vec<u8>) {
        match self {
            SonarMessage::Login(name, pword) => {
                buf.push(b'l');
                buf.push(b'\x1F');
                buf.extend(name.as_bytes());
                buf.push(b'\x1F');
                buf.extend(pword.as_bytes());
            }
            SonarMessage::Quit() => buf.push(b'q'),
            SonarMessage::Enumerate(nm) => {
                buf.push(b'e');
                buf.push(b'\x1F');
                buf.extend(nm.as_bytes());
            }
            SonarMessage::Type(nm) => {
                buf.push(b't');
                buf.push(b'\x1F');
                buf.extend(nm.as_bytes());
            }
            SonarMessage::Show(msg) => {
                buf.push(b's');
                buf.push(b'\x1F');
                buf.extend(msg.as_bytes());
            }
            _ => panic!()
        }
        buf.push(b'\x1E');
    }
}

struct SonarConnection {
    tls_stream: TlsStream<TcpStream>,
    buf: Vec<u8>,
}

impl SonarConnection {
    async fn new(host: &str, port: u16) -> io::Result<Self> {
        let addr = (host, port)
            .to_socket_addrs()
            .await?
            .next()
            .ok_or_else(|| io::Error::from(io::ErrorKind::NotFound))?;
        let tcp_stream = TcpStream::connect(&addr).await?;
        let mut cfg = ClientConfig::new();
        cfg.dangerous()
            .set_certificate_verifier(Arc::new(SonarVerifier {}));
        let connector = TlsConnector::from(Arc::new(cfg));
        let tls_stream = connector.connect(host, tcp_stream).await?;
        let buf = vec![0; 32768];
        Ok(SonarConnection { tls_stream, buf })
    }

    async fn send(&mut self, req: &[u8]) -> io::Result<()> {
        self.tls_stream.write_all(req).await?;
        Ok(())
    }

    async fn recv(&mut self) -> io::Result<()> {
        let count = io::timeout(Duration::from_secs(5), async {
            self.tls_stream.read(&mut self.buf).await
        }).await?;
        let mut buf = &self.buf[..count];
        while !buf.is_empty() {
            let (res, b) = SonarMessage::decode(&buf);
            if res.is_none() {
                break;
            }
            println!("{:?}", res);
            buf = b;
        }
        Ok(())
    }
}

const HOST: &str = &"localhost.localdomain";

fn main() -> io::Result<()> {
    task::block_on(async {
        let mut c = SonarConnection::new(HOST, 1037).await?;
        let mut buf = vec![];
        SonarMessage::Login("admin", "atms_242").encode(&mut buf);
        c.send(&buf[..]).await?;
        c.recv().await
    })
}
