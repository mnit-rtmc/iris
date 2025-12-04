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
use argh::FromArgs;
use futures_util::{TryStreamExt, pin_mut};
use pollinator::rtms_echo::Sensor;
use pollinator::{Database, Result};
use serde::Deserialize;
use std::collections::HashMap;

/// SQL query for RTMS Echo sensors
const QUERY: &str = r#"
SELECT row_to_json(row)::text FROM (
       SELECT l.name AS comm_link, uri, split_part(c.password, ':', 1) AS user,
              split_part(c.password, ':', 2) AS password,
              poll_period_sec AS per_s, long_poll_period_sec AS long_per_s,
              pins, detectors
       FROM iris.comm_link l
       JOIN iris.comm_config cc ON cc.name = l.comm_config
       JOIN iris.controller c ON c.comm_link = l.name
       INNER JOIN (
              SELECT controller,
                     json_agg(pin ORDER BY pin) AS pins,
                     json_agg(name ORDER BY pin) AS detectors
              FROM iris.detector
              GROUP BY controller
       ) d ON d.controller = c.name
       WHERE protocol = 31 AND poll_enabled = true AND condition = 1
) row"#;

/// Sensor connector configuration
#[derive(Debug, Deserialize, PartialEq)]
struct Connector {
    /// Host name or IP address
    host: String,
    /// User name
    user: Option<String>,
    /// Password
    password: Option<String>,
    /// Poll period
    per_s: u32,
    /// Long poll period
    long_per_s: u32,
    /// Detector pins
    pins: Vec<usize>,
    /// Detector pin mapping
    detectors: Vec<String>,
}

/// Command-line arguments
#[derive(FromArgs)]
struct Args {
    /// host name or IP address
    #[argh(option, short = 'h')]
    host: Option<String>,
    /// user name
    #[argh(option, short = 'u')]
    user: Option<String>,
    /// password
    #[argh(option, short = 'p')]
    password: Option<String>,
}

impl Connector {
    /// Create a new connector
    fn new(host: String, user: String, password: String) -> Self {
        let pins = vec![1, 2, 3, 4, 5, 6, 7, 8, 9];
        let detectors = vec![
            "X1".to_string(),
            "X2".to_string(),
            "X3".to_string(),
            "X4".to_string(),
            "X5".to_string(),
            "X6".to_string(),
            "X7".to_string(),
            "X8".to_string(),
            "X9".to_string(),
        ];
        Connector {
            host,
            user: Some(user),
            password: Some(password),
            per_s: 30,
            long_per_s: 300,
            pins,
            detectors,
        }
    }

    /// Make sample detectors
    fn make_detectors(&self) -> HashMap<usize, &str> {
        let mut detectors = HashMap::new();
        for (pin, det) in self.pins.iter().zip(&self.detectors) {
            detectors.insert(*pin, &det[..]);
        }
        detectors
    }

    /// Run requested polling
    async fn run(&self) -> Result<()> {
        log::info!("connecting to {}", &self.host);
        let mut sensor = Sensor::new(&self.host).await?;
        let user = &self.user.as_ref().map_or("", |u| u);
        let password = &self.password.as_ref().map_or("", |p| p);
        sensor.login(user, password).await?;
        sensor.init_detector_zones(&self.make_detectors()).await?;
        sensor.periodic_poll(self.per_s, self.long_per_s).await?;
        log::warn!("disconnected from {}", &self.host);
        Ok(())
    }
}

impl Args {
    /// Get connector
    async fn connector(self) -> Result<Option<Connector>> {
        let any = self.host.is_some()
            || self.user.is_some()
            || self.password.is_some();
        if let (Some(host), Some(user), Some(password)) =
            (self.host, self.user, self.password)
        {
            return Ok(Some(Connector::new(host, user, password)));
        }
        if any {
            panic!("Invalid arguments");
        }
        let db = Database::new("tms").await?;
        let client = db.client().await?;
        let params: &[&str] = &[];
        let it = client.query_raw(QUERY, params).await?;
        pin_mut!(it);
        while let Some(row) = it.try_next().await? {
            let json = row.get::<usize, String>(0);
            let conn: Connector = serde_json::from_str(&json)?;
            return Ok(Some(conn));
        }
        Ok(None)
    }
}

/// Main entry point
#[tokio::main]
async fn main() -> Result<()> {
    env_logger::builder().format_timestamp(None).init();
    let args: Args = argh::from_env();
    if let Some(conn) = args.connector().await? {
        conn.run().await?;
    }
    Ok(())
}
