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
use std::collections::HashMap;

#[tokio::main]
async fn main() -> Result<(), Error> {
    env_logger::builder().format_timestamp(None).init();
    let mut args = std::env::args();
    let _prog = args.next();
    let host = args.next().unwrap_or(String::from("127.0.0.1"));
    let user = args.next().unwrap_or(String::from("user"));
    let pass = args.next().unwrap_or(String::from("pass"));

    let mut sensor = Sensor::new(&host).await?;
    sensor.login(&user, &pass).await?;
    let mut detectors = HashMap::new();
    detectors.insert(1, "X1");
    detectors.insert(2, "X2");
    detectors.insert(3, "X3");
    detectors.insert(4, "X4");
    detectors.insert(5, "X5");
    detectors.insert(6, "X6");
    detectors.insert(7, "X7");
    detectors.insert(8, "X8");
    detectors.insert(9, "X9");
    sensor.init_detector_zones(&detectors).await?;
    sensor.periodic_poll(30, 300).await
}
