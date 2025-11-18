use bytes::Bytes;
use futures_util::StreamExt;
use http_body_util::{BodyExt, Empty};
use hyper::body::Body;
use hyper::header::{AUTHORIZATION, HeaderValue, InvalidHeaderValue};
use hyper::{HeaderMap, Request, StatusCode, Uri};
use hyper_util::rt::TokioIo;
use serde::Deserialize;
use serde::ser::StdError;
use std::string::FromUtf8Error;
use tokio::io::AsyncWriteExt;
use tokio::net::TcpStream;
use tokio_tungstenite::connect_async;
use tungstenite::client::IntoClientRequest;

#[derive(Debug, thiserror::Error)]
enum Error {
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

    #[error("Missing Authority")]
    MissingAuthority,

    #[error("Stream Closed")]
    StreamClosed,
}

#[derive(Debug, Deserialize, PartialEq)]
struct AuthResp {
    username: String,
    is_authenticated: bool,
    bearer_token: String,
}

#[derive(Debug, Deserialize, PartialEq)]
struct ZoneId {
    id: u32,
    name: String,
}

#[derive(Debug, Deserialize, PartialEq)]
enum Direction {
    LeftToRight,
    RightToLeft,
}

#[derive(Debug, Deserialize, PartialEq)]
#[serde(rename_all = "camelCase")]
struct VehicleData {
    speed: f32,
    length: f32,
    direction: Direction,
    zone_id: u32,
}

#[tokio::main]
async fn main() {
    let mut args = std::env::args();
    let _prog = args.next();
    let host = args.next().unwrap_or(String::from("127.0.0.1"));
    let user = args.next().unwrap_or(String::from("user"));
    let pass = args.next().unwrap_or(String::from("pass"));
    collect_vehicle_data(&host, &user, &pass).await.unwrap();
}

async fn collect_vehicle_data(
    host: &str,
    user: &str,
    pass: &str,
) -> Result<(), Error> {
    let headers = HeaderMap::new();
    let body = match http_get(host, "api/v1/zone-identifiers", headers).await {
        Ok(body) => body,
        Err(Error::HttpStatus(status))
            if status == StatusCode::UNAUTHORIZED =>
        {
            let body = format!(
                "{{\"username\": \"{user}\", \"password\": \"{pass}\" }}"
            );
            let resp = http_post(host, "login", body).await?;
            let auth: AuthResp = serde_json::from_slice(&resp)?;
            let bearer = HeaderValue::from_str(&format!(
                "BEARER {}",
                auth.bearer_token
            ))?;
            let mut headers = HeaderMap::new();
            headers.insert(AUTHORIZATION, bearer);
            http_get(host, "api/v1/zone-identifiers", headers).await?
        }
        Err(err) => return Err(err),
    };

    let zones: Vec<ZoneId> = serde_json::from_slice(&body)?;
    for zone in zones {
        println!("  {zone:?}");
    }

    let request = format!("ws://{host}/api/v1/live-vehicle-data")
        .into_client_request()?;
    let (mut stream, response) = connect_async(request).await?;
    print!("Connected, ");
    match response.into_body() {
        Some(body) => println!("{}", String::from_utf8(body)?),
        None => println!("waiting..."),
    }

    loop {
        let data = stream.next().await.ok_or(Error::StreamClosed)??.into_data();
        let veh: VehicleData = serde_json::from_slice(&data)?;
        let msg = format!(
            "speed {}, length {}, direction: {:?}, zoneId: {}\n",
            veh.speed, veh.length, veh.direction, veh.zone_id
        );
        tokio::io::stdout().write_all(msg.as_bytes()).await?;
    }
}

async fn http_get(
    host: &str,
    endpoint: &str,
    headers: HeaderMap,
) -> Result<Vec<u8>, Error> {
    let addr = format!("{host}:80");
    let stream = TcpStream::connect(addr).await?;
    let io = TokioIo::new(stream);
    let (mut sender, conn) = hyper::client::conn::http1::handshake(io).await?;
    let conn_task = tokio::spawn(async move { conn.await });
    let uri = format!("http://{host}/{endpoint}").parse::<Uri>()?;
    //let authority = uri.authority().ok_or(Error::MissingAuthority)?;
    //let path = uri.path();
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

async fn http_post<B>(
    host: &'_ str,
    endpoint: &'_ str,
    body: B,
) -> Result<Vec<u8>, Error>
where
    B: hyper::body::Body + Send + 'static,
    <B as Body>::Data: Send + 'static,
    <B as Body>::Error: StdError + Send + Sync,
{
    let addr = format!("{host}:80");
    let stream = TcpStream::connect(addr).await?;
    let io = TokioIo::new(stream);
    let (mut sender, conn) = hyper::client::conn::http1::handshake(io).await?;
    let conn_task = tokio::spawn(async move { conn.await });
    let uri = format!("http://{host}/{endpoint}").parse::<Uri>()?;
    //let path = uri.path();
    let req = Request::post(uri)
        .header("content-type", "application/json")
        .body(body)?;

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
