use crate::http;

use crate::error::Error;
use futures_util::StreamExt;
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
pub struct ZoneId {
    /// Identifier
    id: u32,
    /// Zone name
    name: String,
}

/// Input Voltage record
#[derive(Debug, Deserialize, PartialEq)]
pub struct InputVoltage {
    /// Time stamp
    time: String,
    /// Input voltage
    voltage: f32,
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

/// RTMS Echo sensor connection
pub struct Sensor {
    /// HTTP client
    client: http::Client,
    /// Zone identifiers
    zones: Vec<ZoneId>,
}

impl Sensor {
    /// Create a new RTMS Echo sensor connection
    pub fn new(host: &str) -> Self {
        let client = http::Client::new(host);
        let zones = Vec::new();
        Sensor { client, zones }
    }

    /// Login with user credentials
    pub async fn login(&mut self, user: &str, pass: &str) -> Result<(), Error> {
        let body =
            format!("{{\"username\": \"{user}\", \"password\": \"{pass}\" }}");
        let resp = self.client.post("api/v1/login", &body).await?;
        let auth: AuthResp = serde_json::from_slice(&resp)?;
        if !auth.is_authenticated {
            return Err(Error::AuthFailed());
        }
        let bearer = format!("Bearer {}", auth.bearer_token);
        self.client.set_bearer_token(bearer);
        Ok(())
    }

    /// Poll the sensor for Zone Identifiers
    pub async fn poll_zone_identifiers(&mut self) -> Result<&[ZoneId], Error> {
        let body = self.client.get("api/v1/zone-identifiers").await?;
        self.zones = serde_json::from_slice(&body)?;
        Ok(&self.zones)
    }

    /// Poll the sensor for input voltage records
    pub async fn poll_input_voltage(&self) -> Result<Vec<InputVoltage>, Error> {
        let body = self.client.get("api/v1/input-voltage?count=1").await?;
        let records = serde_json::from_slice(&body)?;
        Ok(records)
    }

    /// Collect vehicle data
    pub async fn collect_vehicle_data(&self) -> Result<(), Error> {
        let host = self.client.host();
        let request = format!("ws://{host}/api/v1/live-vehicle-data")
            .into_client_request()?;
        let (mut stream, response) = connect_async(request).await?;
        print!("Connected, ");
        match response.into_body() {
            Some(body) => println!("{}", String::from_utf8(body)?),
            None => println!("waiting..."),
        }

        loop {
            let data =
                stream.next().await.ok_or(Error::StreamClosed)??.into_data();
            let veh: VehicleData = serde_json::from_slice(&data)?;
            let msg = format!(
                "speed {}, length {}, direction: {:?}, zoneId: {}\n",
                veh.speed, veh.length, veh.direction, veh.zone_id
            );
            tokio::io::stdout().write_all(msg.as_bytes()).await?;
        }
    }
}
