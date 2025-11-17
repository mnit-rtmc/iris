use futures_util::StreamExt;
use serde::Deserialize;
use std::string::FromUtf8Error;
use tokio::io::AsyncWriteExt;
use tokio_tungstenite::connect_async;
use tungstenite::client::IntoClientRequest;

#[derive(Debug, thiserror::Error)]
enum Error {
    #[error("Tungstenite {0}")]
    Tungstenite(#[from] tungstenite::Error),

    #[error("Utf-8 {0}")]
    FromUtf8(#[from] FromUtf8Error),

    #[error("JSON {0}")]
    SerdeJson(#[from] serde_json::Error),

    #[error("IO {0}")]
    Io(#[from] std::io::Error),

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
    collect_vehicle_data().await.unwrap();
}

async fn collect_vehicle_data() -> Result<(), Error> {
    let request =
        "ws://127.0.0.1/api/v1/live-vehicle-data".into_client_request()?;
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
