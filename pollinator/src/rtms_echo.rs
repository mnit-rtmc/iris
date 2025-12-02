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
use crate::event::{Stamp, VehEvent, VehLog};
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
#[derive(Clone, Debug, Deserialize, PartialEq)]
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

/// Vehicle observation direction
#[derive(Clone, Copy, Debug, Deserialize, PartialEq)]
enum ObsDirection {
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
    /// Observation direction
    direction: ObsDirection,
    /// Zone identifier
    zone_id: u32,
}

/// RTMS Echo sensor connection
pub struct Sensor {
    /// HTTP client
    client: http::Client,
    /// Vehicle event logs
    veh_logs: Vec<VehLog>,
    /// Zone identifiers and directions
    zones: Vec<(ZoneId, Option<ObsDirection>)>,
}

impl VehicleData {
    /// Convert to a server recorded vehicle event
    fn server_recorded(&self, wrong_way: bool) -> VehEvent {
        let mut veh = VehEvent::default();
        veh.server_recorded(Stamp::now());
        veh.wrong_way(wrong_way);
        veh.length_m(self.length);
        veh.speed_kph(self.speed);
        veh
    }
}

impl Sensor {
    /// Create a new RTMS Echo sensor connection
    pub async fn new(host: &str, detectors: &[&str]) -> Result<Self, Error> {
        let client = http::Client::new(host);
        let mut veh_logs = Vec::with_capacity(detectors.len());
        for det_id in detectors {
            veh_logs.push(VehLog::new(det_id).await?);
        }
        let zones = Vec::new();
        Ok(Sensor {
            client,
            veh_logs,
            zones,
        })
    }

    /// Lookup vehicle event log for a zone index
    fn veh_log(&mut self, zone_idx: usize) -> Option<&mut VehLog> {
        if zone_idx < self.veh_logs.len() {
            Some(&mut self.veh_logs[zone_idx])
        } else {
            log::warn!("No vehicle log for zone: {zone_idx}");
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
    pub async fn poll_zone_identifiers(
        &mut self,
    ) -> Result<Vec<ZoneId>, Error> {
        let body = self.client.get("api/v1/zone-identifiers").await?;
        let zones: Vec<ZoneId> = serde_json::from_slice(&body)?;
        self.zones = zones.iter().map(|z| (z.clone(), None)).collect();
        Ok(zones)
    }

    /// Check direction for a vehicle
    fn check_direction(&mut self, veh: &VehicleData) -> ObsDirection {
        for (zone, dir) in self.zones.iter_mut() {
            if veh.zone_id == zone.id {
                match dir {
                    Some(dir) => return *dir,
                    None => *dir = Some(veh.direction),
                }
            }
        }
        veh.direction
    }

    /// Lookup zone index for vehicle data
    fn zone_idx(&self, veh: &VehicleData) -> Option<usize> {
        for (i, (zone, _dir)) in self.zones.iter().enumerate() {
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
    pub async fn collect_vehicle_data(&mut self) -> Result<(), Error> {
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
    async fn log_event(&mut self, veh: VehicleData) -> Result<(), Error> {
        log::info!("veh data: {veh:?}");
        let dir = self.check_direction(&veh);
        if let Some(idx) = self.zone_idx(&veh)
            && let Some(veh_log) = self.veh_log(idx)
        {
            let wrong_way = dir != veh.direction;
            let veh = veh.server_recorded(wrong_way);
            veh_log.append(&veh).await?;
        }
        Ok(())
    }
}
