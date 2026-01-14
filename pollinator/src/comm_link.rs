// Copyright (C) 2025-2026  Minnesota Department of Transportation
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
use crate::binner::DetEvent;
use crate::rtms_echo;
use futures_util::{TryStreamExt, pin_mut};
use resin::event::VehEvent;
use resin::{Database, Error, Result};
use serde::Deserialize;
use std::collections::HashMap;
use tokio::sync::mpsc::UnboundedSender;
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

/// Comm link
#[derive(Clone)]
pub struct CommLink {
    /// Configuration
    cfg: CommLinkCfg,
    /// Event sender
    sender: UnboundedSender<DetEvent>,
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
        Ok(cfgs)
    }

    /// Get comm link name
    pub fn name(&self) -> &str {
        &self.name
    }

    /// Get controller name
    pub fn controller(&self) -> &str {
        &self.controller
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

    /// Set user name
    pub fn with_user(mut self, user: &str) -> Self {
        self.user = Some(user.to_string());
        self
    }

    /// Set password
    pub fn with_password(mut self, password: &str) -> Self {
        self.password = Some(password.to_string());
        self
    }

    /// Check if configuration has a detector
    pub fn has_detector(&self, det_id: &str) -> bool {
        self.detectors.iter().any(|d| d == det_id)
    }
}

impl CommLink {
    /// Create a new comm link
    pub fn new(cfg: CommLinkCfg, sender: UnboundedSender<DetEvent>) -> Self {
        CommLink { cfg, sender }
    }

    /// Get configuration
    pub fn cfg(&self) -> &CommLinkCfg {
        &self.cfg
    }

    /// Get comm link name
    pub fn name(&self) -> &str {
        &self.cfg.name
    }

    /// Get controller name
    pub fn controller(&self) -> &str {
        &self.cfg.controller
    }

    /// Get comm link URI
    pub fn uri(&self) -> &str {
        &self.cfg.uri
    }

    /// Get user name
    pub fn user(&self) -> Option<&str> {
        self.cfg.user.as_deref()
    }

    /// Get password
    pub fn password(&self) -> Option<&str> {
        self.cfg.password.as_deref()
    }

    /// Get period (s)
    pub fn per_s(&self) -> u32 {
        self.cfg.per_s
    }

    /// Get long period (s)
    pub fn long_per_s(&self) -> u32 {
        self.cfg.long_per_s
    }

    /// Send vehicle event to interval binner
    pub fn bin_event(&self, pin: usize, ev: VehEvent) {
        match self.cfg.detectors.get(pin - 1) {
            Some(det) => {
                if let Err(e) = self.sender.send(DetEvent::new(det, ev)) {
                    log::warn!("send failed: {e}");
                }
            }
            None => log::warn!("no detector on pin {pin}"),
        }
    }

    /// Make detector hashmap
    pub fn make_detectors(&self) -> HashMap<usize, &str> {
        let mut detectors = HashMap::new();
        for (pin, det) in self.cfg.pins.iter().zip(&self.cfg.detectors) {
            detectors.insert(*pin, &det[..]);
        }
        detectors
    }

    /// Run comm link polling
    pub async fn run(self, db: Option<Database>) -> Result<()> {
        let mut ticker = interval(Duration::from_secs(u64::from(self.per_s())));
        ticker.set_missed_tick_behavior(MissedTickBehavior::Skip);
        loop {
            ticker.tick().await;
            log::info!("{}: connecting", self.name());
            let res = try_run_link(&self, db.clone()).await;
            log::info!("{}: disconnected", self.name());
            match &res {
                Err(Error::Bb8(err)) => {
                    log::warn!("{}: pool {err}", self.name());
                    return res;
                }
                Err(Error::Postgres(err)) => {
                    log::warn!("{}: postgres {err}", self.name());
                    return res;
                }
                _ => (),
            }
            self.log_disconnect(&db).await?;
            match res {
                Err(Error::StreamDisconnected) => (),
                Err(err) => log::warn!("{}: {err}", self.name()),
                _ => (),
            }
        }
    }

    /// Log controller connect in database
    pub async fn log_connect(&self, db: &Option<Database>) -> Result<()> {
        if let Some(db) = db {
            db.clone()
                .log_connect(self.name(), self.controller())
                .await?;
        }
        Ok(())
    }

    /// Log controller disconnect in database
    pub async fn log_disconnect(&self, db: &Option<Database>) -> Result<()> {
        if let Some(db) = db {
            db.clone()
                .log_disconnect(self.name(), self.controller())
                .await?;
        }
        Ok(())
    }
}

/// Try to run a comm link
async fn try_run_link(link: &CommLink, db: Option<Database>) -> Result<()> {
    match CommProtocol::from_id(link.cfg.protocol) {
        Some(CommProtocol::RtmsEcho) => {
            let sensor = rtms_echo::Sensor::new(link.clone());
            sensor.run(db).await
        }
        _ => Err(Error::InvalidConfig("protocol")),
    }
}
