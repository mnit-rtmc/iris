use crate::error::Error;
use bytes::Bytes;
use http_body_util::{BodyExt, Empty};
use hyper::{HeaderMap, Request, Uri};
use hyper_util::rt::TokioIo;
use tokio::net::TcpStream;

/// Make an http `GET` request
pub async fn get(
    host: &str,
    path: &str,
    headers: HeaderMap,
) -> Result<Vec<u8>, Error> {
    let addr = format!("{host}:80");
    let stream = TcpStream::connect(addr).await?;
    let io = TokioIo::new(stream);
    let (mut sender, conn) = hyper::client::conn::http1::handshake(io).await?;
    let conn_task = tokio::spawn(conn);
    let uri = format!("http://{host}/{path}").parse::<Uri>()?;
    let mut builder = Request::get(uri);
    for (key, value) in headers {
        if let Some(key) = key {
            builder = builder.header(key, value);
        }
    }
    let req = builder.body(Empty::<Bytes>::new())?;

    let mut res = sender.send_request(req).await?;
    let status = res.status();
    if !status.is_success() {
        eprintln!("Headers: {:#?}\n", res.headers());
        return Err(Error::HttpStatus(status));
    }
    conn_task.await??;

    let mut body = Vec::<u8>::new();
    while let Some(next) = res.frame().await {
        let frame = next?;
        if let Some(chunk) = frame.data_ref() {
            body.extend(chunk);
        }
    }
    Ok(body)
}

/// Make an http `POST` request
pub async fn post(
    host: &str,
    path: &str,
    body: &str,
) -> Result<Vec<u8>, Error> {
    let addr = format!("{host}:80");
    let stream = TcpStream::connect(addr).await?;
    let io = TokioIo::new(stream);
    let (mut sender, conn) = hyper::client::conn::http1::handshake(io).await?;
    let conn_task = tokio::spawn(conn);
    let uri = format!("http://{host}/{path}").parse::<Uri>()?;
    let req = Request::post(uri)
        .header("content-type", "application/json")
        .body(body.to_string())?;

    let mut res = sender.send_request(req).await?;
    let status = res.status();
    if !status.is_success() {
        eprintln!("Headers: {:#?}\n", res.headers());
        return Err(Error::HttpStatus(status));
    }
    conn_task.await??;

    let mut body = Vec::<u8>::new();
    while let Some(next) = res.frame().await {
        let frame = next?;
        if let Some(chunk) = frame.data_ref() {
            body.extend(chunk);
        }
    }
    Ok(body)
}
