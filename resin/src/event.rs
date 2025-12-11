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
use mag::{Length, Speed, length, time};
use std::num::{NonZeroU8, NonZeroU16, NonZeroU32};
use std::path::PathBuf;
use std::time::Duration;
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
    /// Estimated by central server
    Estimated = 1,
    /// Recorded by field sensor
    SensorRecorded = 2,
    /// Recorded by field sensor after gap (missing events)
    SensorRecordedGap = 3,
    /// Recorded by central server
    ServerRecorded = 4,
    /// Recorded by central server after gap (missing events)
    ServerRecordedGap = 5,
}

impl Mode {
    /// Check if the timestamp is recorded
    pub fn is_recorded(self) -> bool {
        matches!(
            self,
            Mode::SensorRecorded
                | Mode::SensorRecordedGap
                | Mode::ServerRecorded
                | Mode::ServerRecordedGap
        )
    }
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
    length: Option<NonZeroU16>,
    /// Vehicle speed (kph)
    speed: Option<NonZeroU8>,
    /// Vehicle duration (ms)
    duration: Option<NonZeroU16>,
    /// Headway from start of previous vehicle to this one (ms)
    headway: Option<NonZeroU32>,
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

    /// Set time-of-day (ms since midnight)
    pub fn with_ms_since_midnight(mut self, ms: u32) -> Self {
        let midnight = self.0.start_of_day().unwrap();
        self.0 = midnight + Duration::from_millis(u64::from(ms));
        self
    }

    /// Check if a stamp is before another
    pub fn is_before(&self, other: &Self) -> bool {
        self.0 < other.0
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

    /// Get interval of day for a given period
    pub fn interval(&self, period_s: u32) -> u32 {
        let ms = self.ms_since_midnight();
        ms / (period_s * 1000)
    }
}

impl From<&VehEvent> for u64 {
    fn from(ev: &VehEvent) -> Self {
        let mut val = u64::from(ev.stamp.ms_since_midnight());
        val |= (ev.mode as u64) << 27;
        val |= u64::from(ev.wrong_way) << 30;
        if let Some(dm) = ev.length {
            val |= u64::from(dm.get()) << 31;
        }
        if let Some(kph) = ev.speed {
            val |= u64::from(kph.get()) << 40;
        }
        if let Some(ms) = ev.duration {
            val |= u64::from(ms.get()) << 48;
        }
        val
    }
}

impl From<VehEvent> for u64 {
    fn from(ev: VehEvent) -> Self {
        u64::from(&ev)
    }
}

impl VehEvent {
    /// Set timestamp and mode
    pub fn with_stamp_mode(mut self, stamp: Stamp, mode: Mode) -> Self {
        self.stamp = stamp;
        self.mode = mode;
        self
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
            NonZeroU16::new(dm as u16)
        } else {
            None
        };
        self
    }

    /// Set vehicle length (ft)
    pub fn with_length_ft(self, length: f32) -> Self {
        let ft = Length::<length::ft>::new(f64::from(length));
        let m = ft.to::<length::m>();
        self.with_length_m(m.quantity as f32)
    }

    /// Set vehicle speed (kph)
    pub fn with_speed_kph(mut self, speed: f32) -> Self {
        let kph = speed.round();
        self.speed = if (1.0..=255.0).contains(&kph) {
            NonZeroU8::new(kph as u8)
        } else {
            None
        };
        self
    }

    /// Set vehicle speed (mph)
    pub fn with_speed_mph(self, speed: f32) -> Self {
        let mph = Speed::<length::mi, time::h>::new(f64::from(speed));
        let kph = mph.to::<length::km, time::h>();
        self.with_speed_kph(kph.quantity as f32)
    }

    /// Set vehicle duration (ms)
    pub fn with_duration_ms(mut self, duration: u16) -> Self {
        self.duration = NonZeroU16::new(duration);
        self
    }

    /// Set headway from start of previous vehicle to this one (ms)
    pub fn with_headway_ms(mut self, headway: u32) -> Self {
        self.headway = NonZeroU32::new(headway);
        self
    }

    /// Get time stamp
    pub fn stamp(&self) -> Option<Stamp> {
        if self.mode != Mode::NoTimestamp {
            Some(self.stamp.clone())
        } else {
            None
        }
    }

    /// Get time stamp mode
    pub fn mode(&self) -> Mode {
        self.mode
    }

    /// Get vehicle length (dm)
    pub fn length_dm(&self) -> Option<u16> {
        self.length.map(NonZeroU16::get)
    }

    /// Get vehicle length (ft)
    pub fn length_ft(&self) -> Option<u8> {
        self.length_dm().and_then(|dm| {
            let dm = Length::<length::dm>::new(f64::from(dm));
            let ft = dm.to::<length::ft>().quantity.round();
            if (1.0..=255.0).contains(&ft) {
                Some(ft as u8)
            } else {
                None
            }
        })
    }

    /// Get vehicle speed (kph)
    pub fn speed_kph(&self) -> Option<u8> {
        self.speed.map(NonZeroU8::get)
    }

    /// Get vehicle speed (mph)
    pub fn speed_mph(&self) -> Option<u8> {
        self.speed_kph().and_then(|speed| {
            let kph = Speed::<length::km, time::h>::new(f64::from(speed));
            let mph = kph.to::<length::mi, time::h>().quantity.round();
            if (1.0..=255.0).contains(&mph) {
                Some(mph as u8)
            } else {
                None
            }
        })
    }

    /// Get vehicle duration (ms)
    pub fn duration_ms(&self) -> Option<u16> {
        self.duration.map(NonZeroU16::get)
    }

    /// Get headway from start of previous vehicle to this one (ms)
    pub fn headway_ms(&self) -> Option<u32> {
        self.headway.map(NonZeroU32::get)
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
    fn tod() {
        const MS: u32 = (11 * 60 * 60 * 1000) + (37 * 60 * 1000);
        let ts: Zoned = "2025-12-10 10:20[America/Chicago]".parse().unwrap();
        let stamp = Stamp(ts).with_ms_since_midnight(MS);
        assert_eq!(
            stamp,
            Stamp("2025-12-10 11:37[America/Chicago]".parse().unwrap())
        );
    }

    #[test]
    fn veh_event() {
        const MS: i64 = (14 * 60 * 60 * 1000) + (53 * 60 * 1000);
        let stamp: Zoned = "2025-12-02 14:53[America/Chicago]".parse().unwrap();
        let ev = VehEvent::default()
            .with_stamp_mode(Stamp(stamp), Mode::SensorRecorded)
            .with_length_m(3.0)
            .with_speed_kph(80.0)
            .with_duration_ms(200);
        assert_eq!(
            u64::from(ev),
            (2 << 27) + (MS as u64) + (30 << 31) + (80 << 40) + (200 << 48)
        );
    }
}
