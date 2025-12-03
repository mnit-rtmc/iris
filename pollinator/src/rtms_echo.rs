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

use crate::error::{Error, Result};
use crate::event::{Stamp, VehEvent, VehLog};
use futures_util::stream::{SplitSink, SplitStream};
use futures_util::{SinkExt, StreamExt};
use serde::Deserialize;
use std::collections::HashMap;
use tokio::join;
use tokio::net::TcpStream;
use tokio::time::{Duration, interval};
use tokio_tungstenite::{MaybeTlsStream, WebSocketStream, connect_async};
use tungstenite::client::IntoClientRequest;
use tungstenite::{Bytes, Message};

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

/// Detection zone
struct Zone {
    /// Zone identifier
    id: u32,
    /// Observation direction
    direction: Option<ObsDirection>,
    /// Vehicle event log
    veh_log: Option<VehLog>,
}

/// RTMS Echo sensor connection
pub struct Sensor {
    /// HTTP client
    client: http::Client,
    /// Detection zones
    zones: Vec<Zone>,
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

impl Zone {
    /// Create a new zone
    fn new(id: u32) -> Self {
        Zone {
            id,
            direction: None,
            veh_log: None,
        }
    }

    /// Append a vehicle to log
    async fn log_append(&mut self, veh: &VehicleData) -> Result<()> {
        let dir = self.check_direction(veh);
        match &mut self.veh_log {
            Some(veh_log) => {
                let wrong_way = dir != veh.direction;
                let veh = veh.server_recorded(wrong_way);
                veh_log.append(&veh).await?;
            }
            None => log::warn!("No log for zone: {}", self.id),
        }
        Ok(())
    }

    /// Check direction for a vehicle
    fn check_direction(&mut self, veh: &VehicleData) -> ObsDirection {
        match self.direction {
            Some(dir) => return dir,
            None => self.direction = Some(veh.direction),
        }
        veh.direction
    }
}

impl Sensor {
    /// Create a new RTMS Echo sensor connection
    pub async fn new(host: &str) -> Result<Self> {
        let client = http::Client::new(host);
        let zones = Vec::new();
        Ok(Sensor { client, zones })
    }

    /// Login with user credentials
    pub async fn login(&mut self, user: &str, pass: &str) -> Result<()> {
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

    /// Initialize detector zones
    pub async fn init_detector_zones(
        &mut self,
        dets: &HashMap<usize, &str>,
    ) -> Result<()> {
        let zones = self.poll_zone_identifiers().await?;
        self.zones = Vec::with_capacity(zones.len());
        for (i, zone) in zones.iter().enumerate() {
            let mut zone = Zone::new(zone.id);
            let pin = i + 1;
            match dets.get(&pin) {
                Some(det) => {
                    let veh_log = VehLog::new(det).await?;
                    zone.veh_log = Some(veh_log);
                    log::info!("pin #{pin}, zone: {}, det: {det}", zone.id);
                }
                None => {
                    log::info!("pin #{pin}, zone: {}, NO detector", zone.id)
                }
            }
            self.zones.push(zone);
        }
        for (pin, det) in dets {
            if *pin < 1 || *pin > zones.len() {
                log::warn!("Invalid pin #{pin} for detector: {det}");
            }
        }
        Ok(())
    }

    /// Poll the sensor for Zone Identifiers
    async fn poll_zone_identifiers(&self) -> Result<Vec<ZoneId>> {
        let body = self.client.get("api/v1/zone-identifiers").await?;
        let zones: Vec<ZoneId> = serde_json::from_slice(&body)?;
        Ok(zones)
    }

    /// Poll the sensor for vehicle events
    pub async fn periodic_poll(
        &mut self,
        per: u32,
        _per_long: u32,
    ) -> Result<()> {
        // FIXME: use per_long interval to poll for this
        let records = self.poll_input_voltage().await?;
        for record in records {
            log::debug!("{record:?}");
        }
        self.collect_vehicle_data(per).await
    }

    /// Poll the sensor for input voltage records
    async fn poll_input_voltage(&self) -> Result<Vec<InputVoltage>> {
        let body = self.client.get("api/v1/input-voltage?count=1").await?;
        let records = serde_json::from_slice(&body)?;
        Ok(records)
    }

    /// Lookup zone for vehicle data
    fn zone_mut(&mut self, veh: &VehicleData) -> Option<&mut Zone> {
        for zone in self.zones.iter_mut() {
            if veh.zone_id == zone.id {
                return Some(zone);
            }
        }
        log::warn!("Unknown zoneId for vehicle: {veh:?}");
        None
    }

    /// Collect vehicle data
    async fn collect_vehicle_data(&mut self, per: u32) -> Result<()> {
        let host = self.client.host();
        let req = format!("ws://{host}/api/v1/live-vehicle-data")
            .into_client_request()?;
        let (stream, res) = connect_async(req).await?;
        match res.into_body() {
            Some(body) => log::warn!("{}", String::from_utf8(body)?),
            None => log::info!("WebSocket connected, waiting..."),
        }
        let (sink, stream) = stream.split();
        let (r0, r1) = join![send_pings(sink, per), self.read_messages(stream)];
        r0?;
        r1
    }

    /// Read messages from websocket stream
    async fn read_messages(
        &mut self,
        mut stream: SplitStream<WebSocketStream<MaybeTlsStream<TcpStream>>>,
    ) -> Result<()> {
        while let Some(msg) = stream.next().await {
            match msg? {
                Message::Text(bytes) => {
                    log::info!("TEXT: {} bytes", bytes.len());
                    let data = bytes.as_str();
                    // split JSON objects on ending brace
                    for ev in data.split_inclusive('}') {
                        let veh: VehicleData = serde_json::from_str(ev)?;
                        self.log_event(veh).await?;
                    }
                }
                Message::Binary(bytes) => {
                    log::info!("BINARY: {} bytes", bytes.len());
                }
                Message::Ping(bytes) => {
                    log::info!("PING: {} bytes", bytes.len());
                }
                Message::Pong(bytes) => {
                    log::info!("PONG: {} bytes", bytes.len());
                }
                _ => (),
            }
        }
        Err(Error::StreamClosed)
    }

    /// Log a vehicle event
    async fn log_event(&mut self, veh: VehicleData) -> Result<()> {
        log::debug!("veh data: {veh:?}");
        if let Some(zone) = self.zone_mut(&veh) {
            zone.log_append(&veh).await?;
        }
        Ok(())
    }
}

/// Send ping messages to websocket at a regular interval
async fn send_pings(
    mut sink: SplitSink<WebSocketStream<MaybeTlsStream<TcpStream>>, Message>,
    per: u32,
) -> Result<()> {
    let mut ticker = interval(Duration::from_secs(u64::from(per)));
    ticker.tick().await;
    loop {
        ticker.tick().await;
        log::info!("Sending PING");
        let msg = Message::Ping(Bytes::new());
        sink.send(msg).await?;
    }
}
