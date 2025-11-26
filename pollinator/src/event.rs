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
use crate::error::Error;
use tokio::io::AsyncWriteExt;

/// Time stamp
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct Stamp(u32);

/// Timestamp mode
#[derive(Clone, Debug, Default, Eq, PartialEq)]
pub enum Mode {
    /// No timestamp recorded
    #[default]
    NoTimestamp,
    /// Timestamp recorded by field sensor
    SensorRecorded,
    /// Timestamp recorded by central server
    ServerRecorded,
    /// Timestamp estimated by central server
    Estimated,
    /// Gap in event collection (missing events)
    GapEvent,
}

/// Vehicle event
#[derive(Clone, Debug, Default, Eq, PartialEq)]
pub struct VehEvent {
    /// Event time stamp
    stamp: Stamp,
    /// Time stamp mode
    mode: Mode,
    /// Wrong way vehicle flag
    wrong_way: bool,
    /// Vehicle length (dm)
    length: u16,
    /// Vehicle speed (kph)
    speed: u8,
    /// Vehicle duration (ms)
    duration: u16,
}

impl VehEvent {
    /// Set sensor-recorded timestamp
    pub fn sensor_recorded(&mut self, stamp: impl Into<Stamp>) {
        self.stamp = stamp.into();
        self.mode = Mode::SensorRecorded;
    }

    /// Set server-recorded timestamp
    pub fn server_recorded(&mut self, stamp: impl Into<Stamp>) {
        self.stamp = stamp.into();
        self.mode = Mode::ServerRecorded;
    }

    /// Set estimated timestamp
    pub fn estimated(&mut self, stamp: impl Into<Stamp>) {
        self.stamp = stamp.into();
        self.mode = Mode::Estimated;
    }

    /// Set gap event
    pub fn gap_event(&mut self, stamp: Option<impl Into<Stamp>>) {
        self.stamp = stamp.map(|s| s.into()).unwrap_or_default();
        self.mode = Mode::GapEvent;
    }

    /// Set wrong-way vehicle
    pub fn wrong_way(&mut self, wrong_way: bool) {
        self.wrong_way = wrong_way;
    }

    /// Set vehicle length (m)
    pub fn length_m(&mut self, length: f32) {
        let dm = (length * 10.0).round();
        self.length = if (1.0..=511.0).contains(&dm) {
            dm as u16
        } else {
            0
        };
    }

    /// Set vehicle speed (kph)
    pub fn speed_kph(&mut self, speed: f32) {
        let kph = speed.round();
        self.speed = if (1.0..=255.0).contains(&kph) {
            kph as u8
        } else {
            0
        };
    }

    /// Set vehicle duration (ms)
    pub fn duration_ms(&mut self, duration: u16) {
        self.duration = duration;
    }

    /// Append vehicle data to `.vev` log file
    pub async fn log_append(&self, det_id: &str) -> Result<(), Error> {
        // FIXME
        let msg = format!(
            "{det_id}: speed {}, length {}\n",
            self.speed, self.length,
        );
        tokio::io::stdout().write_all(msg.as_bytes()).await?;
        Ok(())
    }
}
