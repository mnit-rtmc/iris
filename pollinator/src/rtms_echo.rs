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

use crate::database::Database;
use crate::error::{Error, Result};
use crate::event::{Stamp, VehEvent, VehLog};
use futures_util::stream::{SplitSink, SplitStream};
use futures_util::{SinkExt, StreamExt, TryStreamExt, pin_mut};
use serde::Deserialize;
use std::collections::HashMap;
use tokio::join;
use tokio::net::TcpStream;
use tokio::time::{Duration, interval};
use tokio_tungstenite::{MaybeTlsStream, WebSocketStream, connect_async};
use tungstenite::client::IntoClientRequest;
use tungstenite::{Bytes, Message};

/// SQL query for RTMS Echo sensors
const QUERY: &str = r#"
SELECT row_to_json(row)::text FROM (
       SELECT l.name AS comm_link, uri, poll_period_sec AS per_s,
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
       WHERE protocol = 31 AND poll_enabled = true AND condition = 1
) row"#;

/// SQL to connect comm_link
const COMM_LINK_CONNECT: &str = "\
  UPDATE iris.comm_link \
  SET connected = true \
  WHERE name = $1";

/// SQL to connect controller
const CONTROLLER_CONNECT: &str = "\
  UPDATE iris.controller \
  SET fail_time = NULL \
  WHERE name = $1";

/// SQL to disconnect comm_link
const COMM_LINK_DISCONNECT: &str = "\
  UPDATE iris.comm_link \
  SET connected = false \
  WHERE name = $1";

/// SQL to disconnect controller
const CONTROLLER_DISCONNECT: &str = "\
  UPDATE iris.controller \
  SET fail_time = now() \
  WHERE name = $1";

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

/// Sensor configuration
#[derive(Debug, Deserialize, PartialEq)]
pub struct SensorCfg {
    /// Comm link name
    comm_link: String,
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

    /// Append a gap to log
    async fn log_gap(&mut self, stamp: &Stamp) -> Result<()> {
        match &mut self.veh_log {
            Some(veh_log) => {
                let mut veh = VehEvent::default();
                veh.gap_event(stamp.clone());
                veh_log.append(&veh).await?;
            }
            None => log::warn!("No log for zone: {}", self.id),
        }
        Ok(())
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

impl Default for SensorCfg {
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
        SensorCfg {
            comm_link: String::from("default comm link"),
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

impl SensorCfg {
    /// Lookup all sensor configurations in database
    pub async fn lookup_all(db: Database) -> Result<Vec<Self>> {
        let client = db.client().await?;
        let params: &[&str] = &[];
        let mut cfgs = Vec::new();
        let it = client.query_raw(QUERY, params).await?;
        pin_mut!(it);
        while let Some(row) = it.try_next().await? {
            let json = row.get::<usize, String>(0);
            let cfg: SensorCfg = serde_json::from_str(&json)?;
            cfgs.push(cfg);
        }
        if cfgs.is_empty() {
            log::warn!("no sensors configured");
        }
        Ok(cfgs)
    }

    /// Set sensor URI
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

    /// Make sample detectors
    fn make_detectors(&self) -> HashMap<usize, &str> {
        let mut detectors = HashMap::new();
        for (pin, det) in self.pins.iter().zip(&self.detectors) {
            detectors.insert(*pin, &det[..]);
        }
        detectors
    }

    /// Run requested polling
    pub async fn run(self, db: Option<Database>) -> Result<()> {
        loop {
            log::info!("connecting to {}", &self.comm_link);
            let res = self.do_run(db.clone()).await;
            log::info!("disconnected from {}", &self.comm_link);
            if let Some(db) = &db {
                self.log_disconnect(db.clone()).await?;
            }
            if let Err(Error::StreamDisconnected) = &res {
                continue;
            }
            if let Err(err) = res {
                log::warn!("Sensor err: {err:?}");
                return Err(err);
            }
        }
    }

    /// Run requested sensor polling
    async fn do_run(&self, db: Option<Database>) -> Result<()> {
        let mut sensor = Sensor::new(&self.uri).await?;
        let user = &self.user.as_ref().map_or("", |u| u);
        let password = &self.password.as_ref().map_or("", |p| p);
        sensor.login(user, password).await?;
        if let Some(db) = &db {
            self.log_connect(db.clone()).await?;
        }
        sensor.init_detector_zones(&self.make_detectors()).await?;
        sensor.periodic_poll(self.per_s, self.long_per_s).await?;
        Ok(())
    }

    /// Log sensor connect in database
    async fn log_connect(&self, db: Database) -> Result<()> {
        let mut client = db.client().await?;
        let transaction = client.transaction().await?;
        let rows = transaction
            .execute(COMM_LINK_CONNECT, &[&self.comm_link])
            .await?;
        if rows != 1 {
            return Err(Error::DbUpdate);
        }
        let rows = transaction
            .execute(CONTROLLER_CONNECT, &[&self.controller])
            .await?;
        if rows != 1 {
            return Err(Error::DbUpdate);
        }
        transaction.commit().await?;
        Ok(())
    }

    /// Log sensor disconnect in database
    async fn log_disconnect(&self, db: Database) -> Result<()> {
        let mut client = db.client().await?;
        let transaction = client.transaction().await?;
        let rows = transaction
            .execute(COMM_LINK_DISCONNECT, &[&self.comm_link])
            .await?;
        if rows != 1 {
            return Err(Error::DbUpdate);
        }
        let rows = transaction
            .execute(CONTROLLER_DISCONNECT, &[&self.controller])
            .await?;
        if rows != 1 {
            return Err(Error::DbUpdate);
        }
        transaction.commit().await?;
        Ok(())
    }
}

impl Sensor {
    /// Create a new RTMS Echo sensor connection
    pub async fn new(uri: &str) -> Result<Self> {
        let client = http::Client::new(uri);
        let zones = Vec::new();
        Ok(Sensor { client, zones })
    }

    /// Login with user credentials
    async fn login(&mut self, user: &str, pass: &str) -> Result<()> {
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
    async fn init_detector_zones(
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
    async fn periodic_poll(&mut self, per: u32, _per_long: u32) -> Result<()> {
        // FIXME: use per_long interval to poll for this
        let records = self.poll_input_voltage().await?;
        for record in records {
            // FIXME: store in controller status
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
        let hostport = self.client.hostport()?;
        let req = format!("ws://{hostport}/api/v1/live-vehicle-data")
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
        self.log_gap().await?;
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
        Err(Error::StreamDisconnected)
    }

    /// Log a gap event
    async fn log_gap(&mut self) -> Result<()> {
        let stamp = Stamp::now();
        log::debug!("log gap: {stamp:?}");
        for zone in &mut self.zones {
            zone.log_gap(&stamp).await?;
        }
        Ok(())
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
    // FIXME: first, wait until start of next interval
    let mut ticker = interval(Duration::from_secs(u64::from(per)));
    ticker.tick().await;
    loop {
        ticker.tick().await;
        log::info!("Sending PING");
        let msg = Message::Ping(Bytes::new());
        sink.send(msg).await?;
    }
}
