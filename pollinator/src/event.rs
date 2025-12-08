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
use crate::error::Result;
use jiff::Zoned;
use std::path::PathBuf;
use tokio::fs::{File, create_dir_all, try_exists};
use tokio::io::AsyncWriteExt;

/// Time stamp
#[derive(Clone, Debug, Default, Eq, PartialEq)]
pub struct Stamp(Zoned);

/// Timestamp mode
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub enum Mode {
    /// No timestamp recorded
    #[default]
    NoTimestamp = 0,
    /// Timestamp recorded by field sensor
    SensorRecorded = 1,
    /// Timestamp recorded by central server
    ServerRecorded = 2,
    /// Timestamp estimated by central server
    Estimated = 3,
    /// Gap in event collection (missing events)
    GapEvent = 4,
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

/// Vehicle event (`.vev`) log writer
pub struct VehEventWriter {
    /// Detector ID
    det_id: String,
    /// Timestamp
    stamp: Stamp,
    /// File opened in append mode
    file: Option<File>,
}

impl Stamp {
    /// Maximum milliseconds since midnight (with DST)
    const MAX_MS: i128 = 25 * 60 * 60 * 1000;

    /// Get stamp of the current time
    pub fn now() -> Self {
        Stamp(Zoned::now())
    }

    /// Get the number of milliseconds since midnight
    pub fn ms_since_midnight(&self) -> u32 {
        let midnight = self.0.start_of_day().unwrap();
        let delta = self.0.duration_since(&midnight);
        let ms = delta.as_millis();
        if (0..Self::MAX_MS).contains(&ms) {
            ms as u32
        } else {
            log::warn!("Invalid timestamp: {ms}");
            0
        }
    }

    /// Build path to traffic archive directory
    fn build_path(&self) -> PathBuf {
        let year = self.0.year().to_string();
        let date = format!(
            "{:04}{:02}{:02}",
            self.0.year(),
            self.0.month(),
            self.0.day()
        );
        let mut path = PathBuf::new();
        // FIXME: use IRIS server district property
        path.push("tms");
        path.push(&year);
        path.push(&date);
        path
    }
}

impl From<&VehEvent> for u64 {
    fn from(ev: &VehEvent) -> Self {
        let mut val = u64::from(ev.stamp.ms_since_midnight());
        val |= (ev.mode as u64) << 27;
        val |= u64::from(ev.wrong_way) << 30;
        val |= u64::from(ev.length) << 31;
        val |= u64::from(ev.speed) << 40;
        val |= u64::from(ev.duration) << 48;
        val
    }
}

impl From<VehEvent> for u64 {
    fn from(ev: VehEvent) -> Self {
        u64::from(&ev)
    }
}

impl VehEvent {
    /// Set sensor-recorded timestamp
    pub fn sensor_recorded(stamp: Stamp) -> Self {
        VehEvent {
            stamp,
            mode: Mode::SensorRecorded,
            ..Default::default()
        }
    }

    /// Set server-recorded timestamp
    pub fn server_recorded(stamp: Stamp) -> Self {
        VehEvent {
            stamp,
            mode: Mode::ServerRecorded,
            ..Default::default()
        }
    }

    /// Set estimated timestamp
    pub fn estimated(stamp: Stamp) -> Self {
        VehEvent {
            stamp,
            mode: Mode::Estimated,
            ..Default::default()
        }
    }

    /// Set gap event
    pub fn gap_event(stamp: Stamp) -> Self {
        VehEvent {
            stamp,
            mode: Mode::GapEvent,
            ..Default::default()
        }
    }

    /// Set wrong-way vehicle
    pub fn with_wrong_way(mut self, wrong_way: bool) -> Self {
        self.wrong_way = wrong_way;
        self
    }

    /// Set vehicle length (m)
    pub fn with_length_m(mut self, length: f32) -> Self {
        let dm = (length * 10.0).round();
        self.length = if (1.0..=511.0).contains(&dm) {
            dm as u16
        } else {
            0
        };
        self
    }

    /// Set vehicle speed (kph)
    pub fn with_speed_kph(mut self, speed: f32) -> Self {
        let kph = speed.round();
        self.speed = if (1.0..=255.0).contains(&kph) {
            kph as u8
        } else {
            0
        };
        self
    }

    /// Set vehicle duration (ms)
    pub fn with_duration(mut self, duration: u16) -> Self {
        self.duration = duration;
        self
    }
}

impl VehEventWriter {
    /// Make a vehicle event (`.vev`) log writer
    pub async fn new(det_id: &str) -> Result<Self> {
        let det_id = det_id.to_string();
        Ok(VehEventWriter {
            det_id,
            stamp: Stamp::now(),
            file: None,
        })
    }

    /// Append a vehicle event to `.vev` log file
    pub async fn append(&mut self, ev: &VehEvent) -> Result<()> {
        if self.file.is_none() || self.stamp.0.date() != ev.stamp.0.date() {
            self.stamp = ev.stamp.clone();
            let mut path = self.stamp.build_path();
            if !try_exists(&path).await? {
                log::info!("creating dir: {path:?}");
                create_dir_all(&path).await?;
            }
            path.push(&self.det_id);
            path.set_extension("vev");
            let file =
                File::options().append(true).create(true).open(path).await?;
            self.file = Some(file);
        }
        if let Some(file) = &mut self.file {
            file.write_u64_le(u64::from(ev)).await?;
        }
        log::debug!("veh ev: {ev:?}");
        Ok(())
    }
}

#[cfg(test)]
mod test {
    use super::*;

    #[test]
    fn veh_event() {
        const MS: i64 = (14 * 60 * 60 * 1000) + (53 * 60 * 1000);
        let stamp: Zoned = "2025-12-02 14:53[America/Chicago]".parse().unwrap();
        let ev = VehEvent::sensor_recorded(Stamp(stamp))
            .with_length_m(3.0)
            .with_speed_kph(80.0)
            .with_duration(200);
        assert_eq!(
            u64::from(ev),
            (1 << 27) + (MS as u64) + (30 << 31) + (80 << 40) + (200 << 48)
        );
    }
}
