// binned.rs
//
// Copyright (c) 2021-2026  Minnesota Department of Transportation
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
use resin::event::{Mode, VehEvent};
use std::convert::TryFrom;
use std::fmt;
use std::marker::PhantomData;

/// Binned traffic data
pub trait TrafficData: Default + fmt::Display {
    /// Binned file extension
    fn binned_ext() -> &'static str;

    /// Number of bytes per binned value
    fn bin_bytes() -> usize {
        1
    }

    /// Get data value
    fn value(&self) -> Option<u16>;

    /// Unpack one binned value
    fn unpack(val: &[u8]) -> Self;

    /// Pack one binned value
    fn pack(&self, buf: &mut Vec<u8>) {
        buf.push(self.value().map(|v| v as u8).unwrap_or(0xFF))
    }

    /// Set reset for traffic data
    fn reset(&mut self) {}

    /// Bin a vehicle to traffic data
    fn bin_vehicle(&mut self, veh: &VehEvent);
}

/// Binned vehicle count data
#[derive(Clone, Copy, Default)]
pub struct CountData {
    reset: bool,
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

/// Binned speed data
#[derive(Clone, Copy, Default)]
pub struct SpeedData {
    total: u32,
    count: u32,
}

/// Vehicle event filter
#[derive(Clone, Debug, Default, PartialEq)]
pub struct VehicleFilter {
    /// Minimum vehicle length (ft)
    length_ft_min: Option<u32>,

    /// Maximum vehicle length (ft)
    length_ft_max: Option<u32>,

    /// Minimum vehicle speed (mph)
    speed_mph_min: Option<u32>,

    /// Maximum vehicle speed (mph)
    speed_mph_max: Option<u32>,

    /// Minimum headway (ms)
    headway_ms_min: Option<u32>,

    /// Maximum headway (ms)
    headway_ms_max: Option<u32>,
}

/// Vehicle event binning iterator
pub struct BinIter<'a, T: TrafficData> {
    /// Traffic data type
    _data: PhantomData<T>,
    /// Remaining vehicle events
    event_iter: std::slice::Iter<'a, VehEvent>,
    /// Future event
    future_ev: Option<&'a VehEvent>,
    /// Vehicle event filter
    filter: VehicleFilter,
    /// Binning period (s)
    period: u32,
    /// Current binning interval
    interval: u32,
    /// Reset on previous event
    reset: bool,
}

impl TrafficData for CountData {
    /// Binned file extension
    fn binned_ext() -> &'static str {
        "v30"
    }

    /// Get data value
    fn value(&self) -> Option<u16> {
        if self.reset {
            None
        } else {
            u8::try_from(self.count).ok().map(u16::from)
        }
    }

    /// Unpack one binned value
    fn unpack(val: &[u8]) -> Self {
        assert_eq!(val.len(), Self::bin_bytes());
        let value = val[0] as i8;
        let reset = value < 0;
        let count = u32::try_from(value).unwrap_or(0);
        CountData { reset, count }
    }

    /// Set reset for count data
    fn reset(&mut self) {
        self.reset = true;
    }

    /// Bin a vehicle to count data
    fn bin_vehicle(&mut self, _veh: &VehEvent) {
        self.count += 1;
    }
}

impl fmt::Display for CountData {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self.value() {
            Some(val) => write!(f, "{val}"),
            None => write!(f, "null"),
        }
    }
}

impl TrafficData for HeadwayData {
    /// Binned file extension
    fn binned_ext() -> &'static str {
        "h30"
    }

    /// Get data value
    fn value(&self) -> Option<u16> {
        if self.count > 0 {
            let headway =
                (self.total as f32 / self.count as f32).round() as i32;
            u8::try_from(headway).ok().map(u16::from)
        } else {
            None
        }
    }

    /// Unpack one binned value
    fn unpack(val: &[u8]) -> Self {
        assert_eq!(val.len(), Self::bin_bytes());
        let value = val[0] as i8;
        let total = u32::try_from(value).unwrap_or(0);
        let count = total.min(1);
        HeadwayData { total, count }
    }

    /// Bin a vehicle to headway data
    fn bin_vehicle(&mut self, veh: &VehEvent) {
        if let Some(headway) = veh.headway_ms() {
            self.total += headway;
            self.count += 1;
        }
    }
}

impl fmt::Display for HeadwayData {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self.value() {
            Some(val) => write!(f, "{val}"),
            None => write!(f, "null"),
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

    /// Get data value
    fn value(&self) -> Option<u16> {
        if self.reset {
            None
        } else {
            u16::try_from(self.duration).ok()
        }
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
        match self.value() {
            Some(val) => {
                let percent = (val as f32) / 30_000.0;
                let scans = (percent * 1_800.0).round() as u16;
                buf.push(((scans >> 8) & 0xFF) as u8);
                buf.push((scans & 0xFF) as u8);
            }
            None => {
                buf.push(0xFF);
                buf.push(0xFF);
            }
        }
    }

    /// Set reset for occupancy data
    fn reset(&mut self) {
        self.reset = true;
    }

    /// Bin a vehicle to occupancy data
    fn bin_vehicle(&mut self, veh: &VehEvent) {
        if let Some(duration) = veh.duration_ms() {
            self.duration += u32::from(duration);
        }
    }
}

impl fmt::Display for OccupancyData {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self.value() {
            Some(val) => {
                // Ranges from 0 - 30_000 (100%)
                if val.is_multiple_of(300) {
                    // Whole number; use integer to prevent .0 at end
                    write!(f, "{}", val / 300)
                } else {
                    write!(f, "{:.2}", val as f32 / 300.0)
                }
            }
            None => write!(f, "null"),
        }
    }
}

impl TrafficData for LengthData {
    /// Binned file extension
    fn binned_ext() -> &'static str {
        "L30"
    }

    /// Get data value
    fn value(&self) -> Option<u16> {
        if self.count > 0 {
            let length = (self.total as f32 / self.count as f32).round() as i32;
            u8::try_from(length).ok().map(u16::from)
        } else {
            None
        }
    }

    /// Unpack one binned value
    fn unpack(val: &[u8]) -> Self {
        assert_eq!(val.len(), Self::bin_bytes());
        let value = val[0] as i8;
        let total = u32::try_from(value).unwrap_or(0);
        let count = total.min(1);
        LengthData { total, count }
    }

    /// Bin a vehicle to length data
    fn bin_vehicle(&mut self, veh: &VehEvent) {
        if let Some(length) = veh.length_ft() {
            self.total += u32::from(length);
            self.count += 1;
        }
    }
}

impl fmt::Display for LengthData {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self.value() {
            Some(val) => write!(f, "{val}"),
            None => write!(f, "null"),
        }
    }
}

impl SpeedData {
    /// Create new speed data
    pub fn new(speed: u16) -> Self {
        SpeedData {
            total: u32::from(speed),
            count: 1,
        }
    }
}

impl TrafficData for SpeedData {
    /// Binned file extension
    fn binned_ext() -> &'static str {
        "s30"
    }

    /// Get data value
    fn value(&self) -> Option<u16> {
        if self.count > 0 {
            let speed = (self.total as f32 / self.count as f32).round() as i32;
            u8::try_from(speed).ok().map(u16::from)
        } else {
            None
        }
    }

    /// Unpack one binned value
    fn unpack(val: &[u8]) -> Self {
        assert_eq!(val.len(), Self::bin_bytes());
        let value = val[0] as i8;
        let total = u32::try_from(value).unwrap_or(0);
        let count = total.min(1);
        SpeedData { total, count }
    }

    /// Bin a vehicle to speed data
    fn bin_vehicle(&mut self, veh: &VehEvent) {
        if let Some(speed) = veh.speed_mph() {
            self.total += u32::from(speed);
            self.count += 1;
        }
    }
}

impl fmt::Display for SpeedData {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self.value() {
            Some(val) => write!(f, "{val}"),
            None => write!(f, "null"),
        }
    }
}

impl VehicleFilter {
    /// Check if any filters are in effect
    pub fn is_filtered(&self) -> bool {
        self != &Self::default()
    }

    /// Set minimum vehicle length (ft)
    pub fn with_length_ft_min(mut self, m: Option<u32>) -> Self {
        self.length_ft_min = m;
        self
    }

    /// Set maximum vehicle length (ft)
    pub fn with_length_ft_max(mut self, m: Option<u32>) -> Self {
        self.length_ft_max = m;
        self
    }

    /// Set minimum vehicle speed (mph)
    pub fn with_speed_mph_min(mut self, m: Option<u32>) -> Self {
        self.speed_mph_min = m;
        self
    }

    /// Set maximum vehicle speed (mph)
    pub fn with_speed_mph_max(mut self, m: Option<u32>) -> Self {
        self.speed_mph_max = m;
        self
    }

    /// Set minimum headway (sec)
    pub fn with_headway_sec_min(mut self, m: Option<f32>) -> Self {
        self.headway_ms_min = m.map(sec_to_ms);
        self
    }

    /// Set maximum headway (sec)
    pub fn with_headway_sec_max(mut self, m: Option<f32>) -> Self {
        self.headway_ms_max = m.map(sec_to_ms);
        self
    }

    /// Check if vehicle should be binned
    fn check(&self, ev: &VehEvent) -> bool {
        if let Some(mn) = self.length_ft_min {
            // use 0 for unknown length
            if u32::from(ev.length_ft().unwrap_or(0)) < mn {
                return false;
            }
        }
        if let Some(mx) = self.length_ft_max {
            // use MAX value for unknown length
            if u32::from(ev.length_ft().unwrap_or(u8::MAX)) >= mx {
                return false;
            }
        }
        if let Some(mn) = self.speed_mph_min {
            // use 0 for unknown speed
            if u32::from(ev.speed_mph().unwrap_or(0)) < mn {
                return false;
            }
        }
        if let Some(mx) = self.speed_mph_max {
            // use MAX value for unknown speed
            if u32::from(ev.speed_mph().unwrap_or(u8::MAX)) >= mx {
                return false;
            }
        }
        if let Some(mn) = self.headway_ms_min {
            // use 0 for unknown headway
            if ev.headway_ms().unwrap_or(0) < mn {
                return false;
            }
        }
        if let Some(mx) = self.headway_ms_max {
            // use MAX value for unknown headway
            if ev.headway_ms().unwrap_or(u32::MAX) >= mx {
                return false;
            }
        }
        true
    }
}

/// Convert a value in seconds to milliseconds
fn sec_to_ms(m: f32) -> u32 {
    (m * 1000.0).round() as u32
}

impl<T: TrafficData> Iterator for BinIter<'_, T> {
    type Item = T;

    fn next(&mut self) -> Option<Self::Item> {
        if self.interval < self.max_interval() {
            let data = self.interval_data();
            self.interval += 1;
            Some(data)
        } else {
            None
        }
    }
}

impl<'a, T: TrafficData> BinIter<'a, T> {
    /// Create a new binning iterator
    pub fn new(
        period: u32,
        events: &'a [VehEvent],
        filter: VehicleFilter,
    ) -> Self {
        BinIter {
            _data: PhantomData,
            event_iter: events.iter(),
            future_ev: None,
            filter,
            period,
            interval: 0,
            reset: false,
        }
    }

    /// Get the maximum interval number
    fn max_interval(&self) -> u32 {
        (24 * 60 * 60) / self.period
    }

    /// Get the maximum allowed gap (intervals) between events
    fn max_gap(&self) -> u32 {
        // 30 minutes
        (30 * 60) / self.period
    }

    /// Get the interval number for an event
    fn event_interval(&self, ev: &VehEvent) -> Option<u32> {
        ev.stamp().map(|st| st.interval(self.period))
    }

    /// Get the current interval data
    fn interval_data(&mut self) -> T {
        let mut data = self.make_data();
        if let Some(ev) = &self.future_ev {
            if self.is_future_event(ev) {
                return data;
            }
            if self.filter.check(ev) {
                data.bin_vehicle(ev);
            }
        }
        self.future_ev = None;
        while let Some(ev) = self.event_iter.next() {
            if ev.mode() == Mode::NoTimestamp || ev.gap() {
                self.reset = true;
                data.reset();
            } else {
                if self.is_future_event(ev) {
                    self.future_ev = Some(ev);
                    let interval =
                        self.event_interval(ev).unwrap_or(self.interval);
                    // reset if the gap between events is too long
                    if interval > self.interval + self.max_gap() {
                        self.reset = true;
                        data.reset();
                    }
                    return data;
                }
                self.reset = false;
                if self.filter.check(ev) {
                    data.bin_vehicle(ev);
                }
            }
        }
        // no more events
        if self.interval < self.max_interval() - self.max_gap() {
            self.reset = true;
            data.reset();
        }
        data
    }

    /// Make binned traffic data
    fn make_data(&self) -> T {
        let mut data = T::default();
        if self.reset {
            data.reset();
        }
        data
    }

    /// Check if an event is for a future interval
    fn is_future_event(&self, ev: &VehEvent) -> bool {
        match self.event_interval(ev) {
            Some(interval) => interval > self.interval,
            None => false,
        }
    }
}
