//
// Copyright (C) 2026  Minnesota Department of Transportation
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
use crate::error::Result;
use crate::xml::XmlWriter;

use base64::{Engine as _, engine::general_purpose::STANDARD as b64};
use chrono::{SecondsFormat, Utc};
use http_body_util::BodyExt;
use hyper::body::Incoming;
use hyper::{Request, Response};
use hyper_util::rt::TokioIo;
use sha1::{Digest, Sha1};
use std::io;
use tokio::net::TcpStream;

/// SOAP/WS-Security namespaces
const SOAP: &str = "http://www.w3.org/2003/05/soap-envelope";
const WSSE: &str = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
const WSU: &str = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

/// Return digest as tuple (password, nonce, created)
fn get_digest(pass: &str) -> Option<(String, String, String)> {
    if pass.is_empty() {
        log::debug!("Password is empty, not creating digest.");
        return None;
    }
    let mut nonce_bytes = [0u8; 16];
    rand::fill(&mut nonce_bytes[..]);
    let pass_bytes = pass.as_bytes();
    let mut hasher = Sha1::new();
    let created = Utc::now().to_rfc3339_opts(SecondsFormat::Millis, true);
    hasher.update(nonce_bytes);
    hasher.update(created.as_bytes());
    hasher.update(pass_bytes);
    let digest = hasher.finalize();
    let digest_b64 = b64.encode(digest);
    Some((digest_b64, b64.encode(nonce_bytes), created))
}

/// Writes a WSSE security header
fn add_security_header<W: io::Write>(
    writer: &mut XmlWriter<W>,
    user: &str,
    pass: &str,
) {
    if let Some((password, nonce, created)) = get_digest(pass).as_ref() {
        writer.start_element_default_ns(
            "Security",
            WSSE,
            &[("s:mustUnderstand", "1")],
        );
        writer.start_element("UsernameToken", &[]);
        writer.single_element("Username", user, &[]);
        writer.single_element("Password", password, &[
            ("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest")
        ]);
        writer.single_element("Nonce", nonce, &[
            ("EncodingType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary")
        ]);
        writer.single_element_default_ns("Created", WSU, created, &[]);
        // Don't call .finish(), since we need to write the Body
        writer.end_element(Some("UsernameToken"));
        writer.end_element(Some("Security"));
    }
}

/// Get the base document
pub fn get_base_document<W: io::Write>(
    writer: &mut XmlWriter<W>,
    userpass: Option<(&str, &str)>,
) {
    writer.start_element_ns("s:Envelope", ("s", SOAP), &[]);
    writer.start_element("s:Header", &[]);
    if let Some((user, pass)) = userpass {
        add_security_header(writer, user, pass);
    } else {
        log::debug!("No security headers added for {userpass:?}");
    }
    writer.end_element(Some("s:Header"));
    writer.start_body();
}

/// Parse HTTP response frames into bytes
async fn parse_response(mut res: Response<Incoming>) -> Result<Vec<u8>> {
    let mut body = Vec::<u8>::new();
    while let Some(next) = res.frame().await {
        let frame = next?;
        if let Some(chunk) = frame.data_ref() {
            body.extend(chunk);
        }
    }

    Ok(body)
}

/// Send a soap message to the host/path, with SOAP Action action, and SOAP Envelope envelope
pub async fn send(
    host: String,
    path: String,
    action: String,
    envelope: String,
) -> Result<Vec<u8>> {
    let hostport = format!("{}:80", host);
    let stream = TcpStream::connect(&hostport).await?;
    //let connector = tls::connector();
    //let domain = ServerName::try_from(host)
    //    .map_err(|_| {
    //        tokio::io::Error::new(io::ErrorKind::InvalidValue, "invalid dnsname")
    //    })?
    //    .to_owned();
    //let tls_stream = connector.connect(domain, tcp_stream).await?;
    let io = TokioIo::new(stream);
    let (mut sender, conn) = hyper::client::conn::http1::handshake(io).await?;

    // Don't await a conn_task, just handle in task itself
    tokio::spawn(async move {
        if let Err(err) = conn.await {
            log::error!("Connection failed: {:?}", err);
        }
    });

    let mut builder = Request::post(path.to_owned())
        .header("Accept", "application/soap+xml")
        .header("Host", host.to_owned())
        .header("Accept-Encoding", "gzip, deflate")
        .header("Connection", "Close");
    let mut content_type = String::from("application/soap+xml; charset=utf-8");

    if !action.is_empty() {
        content_type.push_str(&format!("; action=\"{}\"", action));
        builder = builder.header("SOAPAction", action);
    }

    builder = builder.header("Content-Type", content_type);
    let req = builder.body(envelope)?;

    log::debug!("Sending request: {req:#?}");
    let res = sender.send_request(req).await?;

    parse_response(res).await
}
