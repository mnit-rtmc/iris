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
use crate::comm_link::CommLink;
use crate::http;

use futures_util::stream::{SplitSink, SplitStream};
use futures_util::{SinkExt, StreamExt};
use resin::event::{Mode, Stamp, VehEvent, VehEventWriter};
use resin::{Database, Error, Result};
use serde::Deserialize;
use tokio::join;
use tokio::net::TcpStream;
use tokio::time::{Duration, MissedTickBehavior, interval};
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
    /// Controller pin
    pin: usize,
    /// Observation direction
    direction: Option<ObsDirection>,
    /// Vehicle event log writer
    vlg_writer: Option<VehEventWriter>,
}

/// RTMS Echo sensor connection
#[derive(Clone)]
pub struct Sensor {
    /// Comm link
    link: CommLink,
    /// HTTP client
    client: http::Client,
    /// Detection zones
    zones: Vec<Zone>,
}

impl VehicleData {
    /// Convert to a gap vehicle event
    fn with_gap(&self) -> VehEvent {
        VehEvent::default()
            .with_stamp_mode(Stamp::now(), Mode::ServerRecorded)
            .with_gap(true)
            .with_length_m(self.length)
            .with_speed_kph(self.speed)
    }

    /// Convert to a server recorded vehicle event
    fn server_recorded(&self, wrong_way: bool) -> VehEvent {
        VehEvent::default()
            .with_stamp_mode(Stamp::now(), Mode::ServerRecorded)
            .with_wrong_way(wrong_way)
            .with_length_m(self.length)
            .with_speed_kph(self.speed)
    }
}

impl Clone for Zone {
    fn clone(&self) -> Self {
        Zone::new(self.id, self.pin)
    }
}

impl Zone {
    /// Create a new zone
    fn new(id: u32, pin: usize) -> Self {
        Zone {
            id,
            pin,
            direction: None,
            vlg_writer: None,
        }
    }

    /// Make a vehicle event from vehicle data
    fn make_event(&mut self, veh: &VehicleData) -> VehEvent {
        let gap = self.direction.is_none();
        if gap {
            veh.with_gap()
        } else {
            let dir = self.check_direction(veh);
            let wrong_way = dir != veh.direction;
            veh.server_recorded(wrong_way)
        }
    }

    /// Check direction for a vehicle
    fn check_direction(&mut self, veh: &VehicleData) -> ObsDirection {
        match self.direction {
            Some(dir) => return dir,
            None => self.direction = Some(veh.direction),
        }
        veh.direction
    }

    /// Append a vehicle event to log
    async fn log_append(&mut self, ev: &VehEvent) -> Result<()> {
        match &mut self.vlg_writer {
            Some(vlg_writer) => vlg_writer.append(ev).await?,
            None => log::warn!("{}: no detector on pin {}", self.id, self.pin),
        }
        Ok(())
    }
}

impl Sensor {
    /// Create a RTMS Echo sensor
    pub fn new(link: CommLink) -> Self {
        let client = http::Client::new(link.uri());
        let zones = Vec::new();
        Sensor {
            link,
            client,
            zones,
        }
    }

    /// Get controller name
    fn controller(&self) -> &str {
        self.link.controller()
    }

    /// Poll sensor continuously
    pub async fn run(mut self, db: Option<Database>) -> Result<()> {
        self.login().await?;
        self.link.log_connect(&db).await?;
        self.init_detector_zones().await?;
        self.continuous_poll().await?;
        Ok(())
    }

    /// Login with user credentials
    async fn login(&mut self) -> Result<()> {
        let user = self.link.user().ok_or(Error::InvalidConfig("user"))?;
        let pass = self
            .link
            .password()
            .ok_or(Error::InvalidConfig("password"))?;
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
    async fn init_detector_zones(&mut self) -> Result<()> {
        let dets = self.link.make_detectors();
        let zones = self.poll_zone_identifiers().await?;
        self.zones = Vec::with_capacity(zones.len());
        for (i, zone) in zones.iter().enumerate() {
            let pin = i + 1;
            let mut zone = Zone::new(zone.id, pin);
            match dets.get(&pin) {
                Some(det) => {
                    let vlg_writer = VehEventWriter::new(det).await?;
                    zone.vlg_writer = Some(vlg_writer);
                    log::debug!(
                        "{}: pin #{pin}, zone {}, det {det}",
                        self.controller(),
                        zone.id
                    );
                }
                None => {
                    log::warn!(
                        "{}: pin #{pin}, zone {}, NO detector",
                        self.controller(),
                        zone.id
                    )
                }
            }
            self.zones.push(zone);
        }
        for (pin, det) in dets {
            if pin < 1 || pin > zones.len() {
                log::warn!(
                    "{}: invalid pin #{pin} for detector: {det}",
                    self.controller()
                );
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
    async fn continuous_poll(&mut self) -> Result<()> {
        // FIXME: use link.per_long interval to poll for this
        let records = self.poll_input_voltage().await?;
        for record in records {
            // FIXME: store in controller status
            log::debug!("{}: {record:?}", self.controller());
        }
        self.collect_vehicle_data().await
    }

    /// Poll the sensor for input voltage records
    async fn poll_input_voltage(&self) -> Result<Vec<InputVoltage>> {
        let body = self.client.get("api/v1/input-voltage?count=1").await?;
        let records = serde_json::from_slice(&body)?;
        Ok(records)
    }

    /// Lookup zone for vehicle data
    fn zone_mut(&mut self, veh: &VehicleData) -> Option<&mut Zone> {
        self.zones.iter_mut().find(|zone| veh.zone_id == zone.id)
    }

    /// Collect vehicle data
    async fn collect_vehicle_data(&mut self) -> Result<()> {
        let per = self.link.per_s();
        let hostport = self.client.hostport()?;
        let req = format!("ws://{hostport}/api/v1/live-vehicle-data")
            .into_client_request()?;
        let (stream, res) = connect_async(req).await?;
        match res.into_body() {
            Some(body) => log::warn!(
                "{}: resp {}",
                self.controller(),
                String::from_utf8(body)?
            ),
            None => log::info!(
                "{}: WebSocket connected, waiting...",
                self.controller()
            ),
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
                    log::debug!(
                        "{}: TEXT, {} bytes",
                        self.controller(),
                        bytes.len()
                    );
                    let data = bytes.as_str();
                    // split JSON objects on ending brace
                    for ev in data.split_inclusive('}') {
                        let veh: VehicleData = serde_json::from_str(ev)?;
                        self.log_event(veh).await?;
                    }
                }
                Message::Binary(bytes) => {
                    log::debug!(
                        "{}: BINARY, {} bytes, ignoring",
                        self.controller(),
                        bytes.len()
                    );
                }
                Message::Ping(bytes) => {
                    log::debug!(
                        "{}: PING, {} bytes",
                        self.controller(),
                        bytes.len()
                    );
                }
                Message::Pong(bytes) => {
                    log::debug!(
                        "{}: PONG, {} bytes",
                        self.controller(),
                        bytes.len()
                    );
                }
                _ => (),
            }
        }
        Err(Error::StreamDisconnected)
    }

    /// Log a vehicle event
    async fn log_event(&mut self, veh: VehicleData) -> Result<()> {
        log::debug!("{}: veh data: {veh:?}", self.controller());
        match self.zone_mut(&veh) {
            Some(zone) => {
                let ev = zone.make_event(&veh);
                zone.log_append(&ev).await?;
                let pin = zone.pin;
                self.link.bin_event(pin, ev);
            }
            None => {
                let ctrl = self.controller();
                log::warn!("{ctrl}: unknown zoneId for vehicle: {veh:?}");
            }
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
    ticker.set_missed_tick_behavior(MissedTickBehavior::Skip);
    loop {
        ticker.tick().await;
        log::debug!("sending PING");
        let msg = Message::Ping(Bytes::new());
        sink.send(msg).await?;
    }
}
