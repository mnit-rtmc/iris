// binned.rs
//
// Copyright (c) 2021-2025  Minnesota Department of Transportation
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
use crate::vehicle::VehicleEvent;
use std::convert::TryFrom;
use std::fmt;

/// Traffic data type
pub trait TrafficData: Default + fmt::Display {
    /// Binned file extension
    fn binned_ext() -> &'static str;

    /// Number of bytes per binned value
    fn bin_bytes() -> usize {
        1
    }

    /// Unpack one binned value
    fn unpack(val: &[u8]) -> Self;

    /// Pack one binned value
    fn pack(&self, buf: &mut Vec<u8>);

    /// Set reset for traffic data
    fn reset(&mut self) {}

    /// Bin a vehicle to traffic data
    fn bin_vehicle(&mut self, veh: &VehicleEvent);
}

/// Binned vehicle count data
#[derive(Clone, Copy, Default)]
pub struct CountData {
    reset: bool,
    count: u32,
}

/// Binned speed data
#[derive(Clone, Copy, Default)]
pub struct SpeedData {
    total: u32,
    count: u32,
}

/// Binned headway data
#[derive(Clone, Copy, Default)]
pub struct HeadwayData {
    total: u32,
    count: u32,
}

/// Binned occupancy data
#[derive(Clone, Copy, Default)]
pub struct OccupancyData {
    reset: bool,
    /// Duration (ms) ranges from 0 - 30_000 (100%)
    duration: u32,
}

/// Binned length data
#[derive(Clone, Copy, Default)]
pub struct LengthData {
    total: u32,
    count: u32,
}

impl TrafficData for CountData {
    /// Binned file extension
    fn binned_ext() -> &'static str {
        "v30"
    }

    /// Unpack one binned value
    fn unpack(val: &[u8]) -> Self {
        assert_eq!(val.len(), Self::bin_bytes());
        let value = val[0] as i8;
        let reset = value < 0;
        let count = u32::try_from(value).unwrap_or(0);
        CountData { reset, count }
    }

    /// Pack one binned value
    fn pack(&self, buf: &mut Vec<u8>) {
        if self.reset {
            buf.push(0xFF);
        } else {
            buf.push(self.count as u8);
        }
    }

    /// Set reset for count data
    fn reset(&mut self) {
        self.reset = true;
    }

    /// Bin a vehicle to count data
    fn bin_vehicle(&mut self, _veh: &VehicleEvent) {
        self.count += 1;
    }
}

impl fmt::Display for CountData {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        if self.reset {
            write!(f, "null")
        } else {
            write!(f, "{}", self.count)
        }
    }
}

impl TrafficData for SpeedData {
    /// Binned file extension
    fn binned_ext() -> &'static str {
        "s30"
    }

    /// Unpack one binned value
    fn unpack(val: &[u8]) -> Self {
        assert_eq!(val.len(), Self::bin_bytes());
        let value = val[0] as i8;
        let total = u32::try_from(value).unwrap_or(0);
        let count = total.min(1);
        SpeedData { total, count }
    }

    /// Pack one binned value
    fn pack(&self, buf: &mut Vec<u8>) {
        if self.count > 0 {
            let speed = (self.total as f32 / self.count as f32).round() as i32;
            let speed = u8::try_from(speed).unwrap_or(0xFF);
            buf.push(speed);
        } else {
            buf.push(0xFF);
        }
    }

    /// Bin a vehicle to speed data
    fn bin_vehicle(&mut self, veh: &VehicleEvent) {
        if let Some(speed) = veh.speed() {
            self.total += speed;
            self.count += 1;
        }
    }
}

impl fmt::Display for SpeedData {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        if self.count > 0 {
            let speed = (self.total as f32 / self.count as f32).round();
            write!(f, "{}", speed as u32)
        } else {
            write!(f, "null")
        }
    }
}

impl TrafficData for HeadwayData {
    /// Binned file extension
    fn binned_ext() -> &'static str {
        "h30"
    }

    /// Unpack one binned value
    fn unpack(val: &[u8]) -> Self {
        assert_eq!(val.len(), Self::bin_bytes());
        let value = val[0] as i8;
        let total = u32::try_from(value).unwrap_or(0);
        let count = total.min(1);
        HeadwayData { total, count }
    }

    /// Pack one binned value
    fn pack(&self, buf: &mut Vec<u8>) {
        if self.count > 0 {
            let headway =
                (self.total as f32 / self.count as f32).round() as i32;
            let headway = u8::try_from(headway).unwrap_or(0xFF);
            buf.push(headway);
        } else {
            buf.push(0xFF);
        }
    }

    /// Bin a vehicle to headway data
    fn bin_vehicle(&mut self, veh: &VehicleEvent) {
        if let Some(headway) = veh.headway() {
            self.total += headway;
            self.count += 1;
        }
    }
}

impl fmt::Display for HeadwayData {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        if self.count > 0 {
            let headway = (self.total as f32 / self.count as f32).round();
            write!(f, "{}", headway as u32)
        } else {
            write!(f, "null")
        }
    }
}

impl TrafficData for OccupancyData {
    /// Binned file extension
    fn binned_ext() -> &'static str {
        "c30"
    }

    /// Number of bytes per binned value
    fn bin_bytes() -> usize {
        2
    }

    /// Unpack one binned value
    fn unpack(val: &[u8]) -> Self {
        assert_eq!(val.len(), Self::bin_bytes());
        // Scans (60 Hz * 30 s)
        let scans = ((u16::from(val[0]) << 8) | u16::from(val[1])) as i16;
        let reset = scans < 0;
        let percent = scans.max(0) as f32 / 1_800.0;
        let duration = (percent * 30_000.0).round() as u32;
        OccupancyData { reset, duration }
    }

    /// Pack one binned value
    fn pack(&self, buf: &mut Vec<u8>) {
        if self.reset {
            buf.push(0xFF);
            buf.push(0xFF);
        } else {
            let percent = (self.duration as f32) / 30_000.0;
            let scans = (percent * 1_800.0).round() as u16;
            buf.push(((scans >> 8) & 0xFF) as u8);
            buf.push((scans & 0xFF) as u8);
        }
    }

    /// Set reset for occupancy data
    fn reset(&mut self) {
        self.reset = true;
    }

    /// Bin a vehicle to occupancy data
    fn bin_vehicle(&mut self, veh: &VehicleEvent) {
        if let Some(duration) = veh.duration() {
            self.duration += duration;
        }
    }
}

impl fmt::Display for OccupancyData {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        if self.reset {
            write!(f, "null")
        } else {
            // Ranges from 0 - 30_000 (100%)
            let val = self.duration;
            if val.is_multiple_of(300) {
                // Whole number; use integer to prevent .0 at end
                write!(f, "{}", val / 300)
            } else {
                write!(f, "{:.2}", val as f32 / 300.0)
            }
        }
    }
}

impl TrafficData for LengthData {
    /// Binned file extension
    fn binned_ext() -> &'static str {
        "L30"
    }

    /// Unpack one binned value
    fn unpack(val: &[u8]) -> Self {
        assert_eq!(val.len(), Self::bin_bytes());
        let value = val[0] as i8;
        let total = u32::try_from(value).unwrap_or(0);
        let count = total.min(1);
        LengthData { total, count }
    }

    /// Pack one binned value
    fn pack(&self, buf: &mut Vec<u8>) {
        if self.count > 0 {
            let length = (self.total as f32 / self.count as f32).round() as i32;
            let length = u8::try_from(length).unwrap_or(0xFF);
            buf.push(length);
        } else {
            buf.push(0xFF);
        }
    }

    /// Bin a vehicle to length data
    fn bin_vehicle(&mut self, veh: &VehicleEvent) {
        if let Some(length) = veh.length() {
            self.total += length;
            self.count += 1;
        }
    }
}

impl fmt::Display for LengthData {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        if self.count > 0 {
            let length = (self.total as f32 / self.count as f32).round();
            write!(f, "{}", length as u32)
        } else {
            write!(f, "null")
        }
    }
}
