// Copyright (C) 2025  Minnesota Department of Transportation
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
#![forbid(unsafe_code)]

mod error;
mod event;
mod http;
mod rtms_echo;

use crate::error::Error;
use crate::rtms_echo::Sensor;

#[tokio::main]
async fn main() -> Result<(), Error> {
    env_logger::builder().format_timestamp(None).init();
    let mut args = std::env::args();
    let _prog = args.next();
    let host = args.next().unwrap_or(String::from("127.0.0.1"));
    let user = args.next().unwrap_or(String::from("user"));
    let pass = args.next().unwrap_or(String::from("pass"));

    let detectors =
        ["X1", "X2", "X3", "X4", "X5", "X6", "X7", "X8", "X9", "X10"];
    let mut sensor = Sensor::new(&host, &detectors).await?;
    sensor.login(&user, &pass).await?;

    let zones = sensor.poll_zone_identifiers().await?;
    for zone in zones {
        log::debug!("{zone:?}");
    }

    let records = sensor.poll_input_voltage().await?;
    for record in records {
        log::debug!("{record:?}");
    }

    sensor.collect_vehicle_data().await?;
    Ok(())
}
