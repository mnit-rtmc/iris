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
use crate::rtms_echo;
use futures_util::{TryStreamExt, pin_mut};
use resin::{Database, Error, Result};
use serde::Deserialize;
use std::collections::HashMap;
use tokio::time::{Duration, MissedTickBehavior, interval};

/// SQL query for comm links
const QUERY: &str = r#"
SELECT row_to_json(row)::text FROM (
       SELECT l.name, protocol, uri, poll_period_sec AS per_s,
              long_poll_period_sec AS long_per_s, controller,
              split_part(c.password, ':', 1) AS user,
              split_part(c.password, ':', 2) AS password, pins, detectors
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
       WHERE pollinator = true AND poll_enabled = true AND condition = 1
) row"#;

/// Comm protocol
#[derive(Clone, Copy, Debug, PartialEq, Eq, Hash)]
enum CommProtocol {
    RtmsEcho,
}

impl CommProtocol {
    /// Get protocol from ID
    fn from_id(id: u32) -> Option<Self> {
        match id {
            31 => Some(Self::RtmsEcho),
            _ => None,
        }
    }
}

/// Comm link configuration
#[derive(Clone, Debug, Deserialize, PartialEq)]
pub struct CommLinkCfg {
    /// Comm link name
    name: String,
    /// Protocol ID
    protocol: u32,
    /// URI address or host name
    uri: String,
    /// Poll period
    per_s: u32,
    /// Long poll period
    long_per_s: u32,
    /// Controller name
    controller: String,
    /// User name
    user: Option<String>,
    /// Password
    password: Option<String>,
    /// Detector pins
    pins: Vec<usize>,
    /// Detector pin mapping
    detectors: Vec<String>,
}

impl Default for CommLinkCfg {
    fn default() -> Self {
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
        CommLinkCfg {
            name: String::from("default comm link"),
            protocol: 0,
            uri: String::new(),
            per_s: 30,
            long_per_s: 300,
            controller: String::from("default controller"),
            user: None,
            password: None,
            pins,
            detectors,
        }
    }
}

impl CommLinkCfg {
    /// Lookup all comm link configurations in database
    pub async fn lookup_all(db: Database) -> Result<Vec<Self>> {
        let client = db.client().await?;
        let params: &[&str] = &[];
        let mut cfgs = Vec::new();
        let it = client.query_raw(QUERY, params).await?;
        pin_mut!(it);
        while let Some(row) = it.try_next().await? {
            let json = row.get::<usize, String>(0);
            let cfg: CommLinkCfg = serde_json::from_str(&json)?;
            cfgs.push(cfg);
        }
        if cfgs.is_empty() {
            log::warn!("no comm links configured");
        }
        Ok(cfgs)
    }

    /// Get comm link name
    pub fn name(&self) -> &str {
        &self.name
    }

    /// Set protocol ID
    pub fn with_protocol(mut self, protocol: u32) -> Self {
        self.protocol = protocol;
        self
    }

    /// Set comm link URI
    pub fn with_uri(mut self, uri: &str) -> Self {
        self.uri = uri.to_string();
        self
    }

    /// Get comm link URI
    pub fn uri(&self) -> &str {
        &self.uri
    }

    /// Get period (s)
    pub fn per_s(&self) -> u32 {
        self.per_s
    }

    /// Get long period (s)
    pub fn long_per_s(&self) -> u32 {
        self.long_per_s
    }

    /// Set user name
    pub fn with_user(mut self, user: &str) -> Self {
        self.user = Some(user.to_string());
        self
    }

    /// Get user name
    pub fn user(&self) -> Option<&str> {
        self.user.as_deref()
    }

    /// Set password
    pub fn with_password(mut self, password: &str) -> Self {
        self.password = Some(password.to_string());
        self
    }

    /// Get password
    pub fn password(&self) -> Option<&str> {
        self.password.as_deref()
    }

    /// Make detector hashmap
    pub fn make_detectors(&self) -> HashMap<usize, &str> {
        let mut detectors = HashMap::new();
        for (pin, det) in self.pins.iter().zip(&self.detectors) {
            detectors.insert(*pin, &det[..]);
        }
        detectors
    }

    /// Run comm link polling
    pub async fn run(self, db: Option<Database>) -> Result<()> {
        let mut ticker = interval(Duration::from_secs(u64::from(self.per_s)));
        ticker.set_missed_tick_behavior(MissedTickBehavior::Skip);
        loop {
            ticker.tick().await;
            log::info!("{}: connecting", &self.name);
            let res = try_run_link(&self, db.clone()).await;
            log::info!("{}: disconnected", &self.name);
            if let Err(Error::Bb8(_) | Error::Postgres(_)) = res {
                log::warn!("{}: database error", &self.name);
                return res;
            }
            self.log_disconnect(&db).await?;
            match res {
                Err(Error::StreamDisconnected) => (),
                Err(err) => log::warn!("{}: {err:?}", &self.name),
                _ => (),
            }
        }
    }

    /// Log controller connect in database
    pub async fn log_connect(&self, db: &Option<Database>) -> Result<()> {
        if let Some(db) = db {
            db.clone().log_connect(&self.name, &self.controller).await?;
        }
        Ok(())
    }

    /// Log controller disconnect in database
    pub async fn log_disconnect(&self, db: &Option<Database>) -> Result<()> {
        if let Some(db) = db {
            db.clone()
                .log_disconnect(&self.name, &self.controller)
                .await?;
        }
        Ok(())
    }
}

/// Try to run a comm link
async fn try_run_link(cfg: &CommLinkCfg, db: Option<Database>) -> Result<()> {
    match CommProtocol::from_id(cfg.protocol) {
        Some(CommProtocol::RtmsEcho) => {
            let sensor = rtms_echo::Sensor::new(cfg);
            sensor.run(cfg, db).await
        }
        _ => Err(Error::InvalidConfig("protocol")),
    }
}
