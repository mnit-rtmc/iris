use bytes::Bytes;
use futures_util::StreamExt;
use http_body_util::{BodyExt, Empty};
use hyper::{Request, Uri};
use hyper_util::rt::TokioIo;
use serde::Deserialize;
use std::string::FromUtf8Error;
use tokio::io::AsyncWriteExt;
use tokio::net::TcpStream;
use tokio_tungstenite::connect_async;
use tungstenite::client::IntoClientRequest;

#[derive(Debug, thiserror::Error)]
enum Error {
    #[error("Hyper {0}")]
    Hyper(#[from] hyper::Error),

    #[error("HTTP {0}")]
    Http(#[from] hyper::http::Error),

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
    collect_vehicle_data("127.0.0.1").await.unwrap();
}

async fn collect_vehicle_data(host: &str) -> Result<(), Error> {
    let addr = format!("{}:80", host);
    let stream = TcpStream::connect(addr).await?;
    let io = TokioIo::new(stream);

    let (mut sender, conn) = hyper::client::conn::http1::handshake(io).await?;
    tokio::task::spawn(async move {
        if let Err(err) = conn.await {
            println!("Connection failed: {:?}", err);
        }
    });

    let uri = "http://{host}/api/v1/zone-identifiers".parse::<Uri>()?;
    let authority = uri.authority().ok_or(Error::MissingAuthority)?;
    let path = uri.path();
    let req = Request::builder()
        .uri(path)
        .header(hyper::header::HOST, authority.as_str())
        .body(Empty::<Bytes>::new())?;

    let mut res = sender.send_request(req).await?;

    println!("Response: {}", res.status());
    println!("Headers: {:#?}\n", res.headers());

    let mut body = Vec::<u8>::new();
    while let Some(next) = res.frame().await {
        let frame = next?;
        if let Some(chunk) = frame.data_ref() {
            body.extend(chunk);
        }
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
