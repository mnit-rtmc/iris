mod error;
mod http;

use crate::error::Error;
use futures_util::StreamExt;
use hyper::header::{AUTHORIZATION, HeaderValue};
use hyper::{HeaderMap, StatusCode};
use serde::Deserialize;
use tokio::io::AsyncWriteExt;
use tokio_tungstenite::connect_async;
use tungstenite::client::IntoClientRequest;

#[derive(Debug, Deserialize, PartialEq)]
#[serde(rename_all = "camelCase")]
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
    let body = match http::get(host, "api/v1/zone-identifiers", headers).await {
        Ok(body) => body,
        Err(Error::HttpStatus(status))
            if status == StatusCode::UNAUTHORIZED =>
        {
            let body = format!(
                "{{\"username\": \"{user}\", \"password\": \"{pass}\" }}"
            );
            let resp = http::post(host, "api/v1/login", &body).await?;
            let auth: AuthResp = serde_json::from_slice(&resp)?;
            let bearer = format!("Bearer {}", auth.bearer_token);
            let mut headers = HeaderMap::new();
            headers.insert(AUTHORIZATION, HeaderValue::from_str(&bearer)?);
            http::get(host, "api/v1/zone-identifiers", headers).await?
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
