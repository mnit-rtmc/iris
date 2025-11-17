use futures_util::StreamExt;
use serde::Deserialize;
use tokio::io::AsyncWriteExt;
use tokio_tungstenite::connect_async;
use tungstenite::client::IntoClientRequest;

#[derive(Debug, Deserialize, PartialEq)]
#[serde(rename_all = "camelCase")]
struct VehicleData {
    speed: f32,
    length: f32,
    direction: String,
    zone_id: String,
}

#[tokio::main]
async fn main() {
    let request = "ws://127.0.0.1/api/v1/live-vehicle-data"
        .into_client_request()
        .unwrap();
    let (mut stream, response) =
        connect_async(request).await.expect("Connection failed");
    print!("Connected, ");
    match response.into_body() {
        Some(body) => {
            println!("{}", String::from_utf8(body).expect("Invalid UTF-8"))
        }
        None => println!("waiting..."),
    }

    loop {
        let data = stream.next().await.unwrap().unwrap().into_data();
        let veh: VehicleData = serde_json::from_slice(&data).unwrap();
        let msg = format!(
            "speed {}, length {}, direction: {}, zoneId: {}\n",
            veh.speed, veh.length, veh.direction, veh.zone_id
        );
        tokio::io::stdout()
            .write_all(msg.as_bytes())
            .await
            .unwrap();
    }
}
