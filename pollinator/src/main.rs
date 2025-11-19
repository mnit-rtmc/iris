mod error;
mod http;
mod rtms_echo;

use crate::rtms_echo::Sensor;

#[tokio::main]
async fn main() {
    let mut args = std::env::args();
    let _prog = args.next();
    let host = args.next().unwrap_or(String::from("127.0.0.1"));
    let user = args.next().unwrap_or(String::from("user"));
    let pass = args.next().unwrap_or(String::from("pass"));

    let mut sensor = Sensor::new(&host);
    sensor.login(&user, &pass).await.unwrap();

    let zones = sensor.poll_zone_identifiers().await.unwrap();
    for zone in zones {
        println!("  {zone:?}");
    }

    let records = sensor.poll_input_voltage().await.unwrap();
    for record in records {
        println!("  {record:?}");
    }

    sensor.collect_vehicle_data().await.unwrap();
}
