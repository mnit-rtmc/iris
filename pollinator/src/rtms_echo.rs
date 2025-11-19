use crate::http;

use crate::error::Error;
use futures_util::StreamExt;
use hyper::StatusCode;
use serde::Deserialize;
use tokio::io::AsyncWriteExt;
use tokio_tungstenite::connect_async;
use tungstenite::client::IntoClientRequest;

/// Authentication response
#[derive(Debug, Deserialize, PartialEq)]
#[serde(rename_all = "camelCase")]
struct AuthResp {
    /// User name
    username: String,
    /// Is authenticated flag
    is_authenticated: bool,
    /// Bearer token to put in Authorization header
    bearer_token: String,
}

/// Sensor zone ID
#[derive(Debug, Deserialize, PartialEq)]
struct ZoneId {
    /// Identifier
    id: u32,
    /// Zone name
    name: String,
}

/// Vehicle detection direction
#[derive(Debug, Deserialize, PartialEq)]
enum Direction {
    /// Left-to-right from sensor perspective
    LeftToRight,
    /// Right-to-left from sensor perspective
    RightToLeft,
}

/// Vehicle event data
#[derive(Debug, Deserialize, PartialEq)]
#[serde(rename_all = "camelCase")]
struct VehicleData {
    /// Speed (kph)
    speed: f32,
    /// Vehicle length (m)
    length: f32,
    /// Detection direction
    direction: Direction,
    /// Zone identifier
    zone_id: u32,
}

/// Collect vehicle data
pub async fn collect_vehicle_data(
    host: &str,
    user: &str,
    pass: &str,
) -> Result<(), Error> {
    let mut client = http::Client::new(host);
    let body = match client.get("api/v1/zone-identifiers").await {
        Ok(body) => body,
        Err(Error::HttpStatus(status))
            if status == StatusCode::UNAUTHORIZED =>
        {
            let body = format!(
                "{{\"username\": \"{user}\", \"password\": \"{pass}\" }}"
            );
            let resp = client.post("api/v1/login", &body).await?;
            let auth: AuthResp = serde_json::from_slice(&resp)?;
            let bearer = format!("Bearer {}", auth.bearer_token);
            client.set_bearer_token(bearer);
            client.get("api/v1/zone-identifiers").await?
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
