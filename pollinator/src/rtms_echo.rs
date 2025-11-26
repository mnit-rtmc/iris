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
use crate::http;

use crate::error::Error;
use crate::event::VehEvent;
use futures_util::StreamExt;
use serde::Deserialize;
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
    /// Detector IDs
    detectors: Vec<String>,
    /// Zone identifiers
    zones: Vec<ZoneId>,
}

impl VehicleData {
    /// Convert to a server recorded vehicle event
    fn server_recorded(&self) -> VehEvent {
        let mut veh = VehEvent::default();
        // FIXME: use chrono Local::now() to build time stamp
        // FIXME: set wrong-way
        veh.length_m(self.length);
        veh.speed_kph(self.speed);
        veh
    }
}

impl Sensor {
    /// Create a new RTMS Echo sensor connection
    pub fn new(host: &str, detectors: &[&str]) -> Self {
        let client = http::Client::new(host);
        let detectors = detectors.iter().map(|d| d.to_string()).collect();
        let zones = Vec::new();
        Sensor {
            client,
            detectors,
            zones,
        }
    }

    /// Lookup detector ID for a zone index
    fn detector_id(&self, zone_idx: usize) -> Option<&str> {
        if zone_idx < self.detectors.len() {
            Some(&self.detectors[zone_idx])
        } else {
            log::warn!("Missing detector ID for zone: {zone_idx}");
            None
        }
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

    /// Lookup zone index for vehicle data
    fn zone_idx(&self, veh: &VehicleData) -> Option<usize> {
        for (i, zone) in self.zones.iter().enumerate() {
            if veh.zone_id == zone.id {
                return Some(i);
            }
        }
        log::warn!("Unknown zoneId for vehicle: {veh:?}");
        None
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
        let req = format!("ws://{host}/api/v1/live-vehicle-data")
            .into_client_request()?;
        let (mut stream, res) = connect_async(req).await?;
        match res.into_body() {
            Some(body) => log::warn!("{}", String::from_utf8(body)?),
            None => log::info!("WebSocket connected, waiting..."),
        }
        loop {
            let data =
                stream.next().await.ok_or(Error::StreamClosed)??.into_data();
            // split JSON objects on ending brace
            for ev in data.split_inclusive(|b| *b == b'}') {
                let veh: VehicleData = serde_json::from_slice(ev)?;
                self.log_event(veh).await?;
            }
        }
    }

    /// Log a vehicle event
    async fn log_event(&self, veh: VehicleData) -> Result<(), Error> {
        if let Some(idx) = self.zone_idx(&veh)
            && let Some(det_id) = self.detector_id(idx)
        {
            let veh = veh.server_recorded();
            veh.log_append(det_id).await?;
        }
        Ok(())
    }
}
