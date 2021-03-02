use async_std::io;
use async_std::net::{TcpStream, ToSocketAddrs};
use async_std::prelude::*;
use async_std::task;
use async_tls::TlsConnector;
use rustls::{
    Certificate, ClientConfig, RootCertStore, ServerCertVerified,
    ServerCertVerifier, TLSError,
};
use std::sync::Arc;
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
        return Ok(ServerCertVerified::assertion());
    }
}

const HOST: &str = &"localhost.localdomain";
const REQUEST: &[u8] = b"l\x1Fadmin\x1Fatms_242\x1E";

fn main() -> io::Result<()> {
    task::block_on(async {
        let addr = (HOST, 1037)
            .to_socket_addrs()
            .await?
            .next()
            .ok_or_else(|| io::Error::from(io::ErrorKind::NotFound))?;
        let tcp_stream = TcpStream::connect(&addr).await?;
        let mut cfg = ClientConfig::new();
        cfg.dangerous()
            .set_certificate_verifier(Arc::new(SonarVerifier {}));
        let connector = TlsConnector::from(Arc::new(cfg));
        let mut tls_stream = connector.connect(HOST, tcp_stream).await?;
        tls_stream.write_all(REQUEST).await?;
        let mut buf = vec![0; 32768];
        let count = tls_stream.read(&mut buf).await?;
        let res = std::str::from_utf8(&buf[..count]).unwrap();
        println!("{}", res);
        Ok(())
    })
}
