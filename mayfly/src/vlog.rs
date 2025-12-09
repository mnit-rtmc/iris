// vehicle.rs
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
use crate::error::{Error, Result};
use std::io::BufRead as _;
use std::io::Read as BlockingRead;
use std::num::{NonZeroU8, NonZeroU16, NonZeroU32};
use std::str::FromStr;
use tokio::io::{AsyncBufReadExt, AsyncReadExt, BufReader};

/// Time stamp
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
struct Stamp(u32);

/// Single logged vehicle event
#[derive(Clone, Debug, Default, Eq, PartialEq)]
pub struct VehicleEvent {
    /// Time stamp
    stamp: Stamp,

    /// Headway from start of previous vehicle to this one (ms)
    headway: Option<NonZeroU32>,

    /// Duration vehicle was detected (ms)
    duration: Option<NonZeroU16>,

    /// Vehicle speed (mph)
    speed: Option<NonZeroU8>,

    /// Vehicle length (ft)
    length: Option<NonZeroU8>,
}

/// Vehicle event log (`.vlog`) for one detector on one day
#[derive(Default)]
pub struct VehLogReader {
    /// All events in the log
    events: Vec<VehicleEvent>,
    /// Previous event time stamp
    previous: Option<u32>,
    /// Latest logged time stamp
    latest: Option<u32>,
}

/// Parse an hour from a time stamp
fn parse_hour(hour: &str) -> Result<u32> {
    match hour.parse() {
        Ok(h) if h < 24 => Ok(h),
        _ => Err(Error::InvalidData("hour")),
    }
}

/// Parse a minute or second from a time stamp
fn parse_min_sec(min_sec: &str) -> Result<u32> {
    match min_sec.parse() {
        Ok(ms) if ms < 60 => Ok(ms),
        _ => Err(Error::InvalidData("minute")),
    }
}

/// Parse a time stamp from a vehicle log
impl FromStr for Stamp {
    type Err = Error;

    fn from_str(s: &str) -> Result<Self> {
        if s.len() == 8 && s.get(2..3) == Some(":") && s.get(5..6) == Some(":")
        {
            let hour = parse_hour(s.get(..2).unwrap_or(""))?;
            let minute = parse_min_sec(s.get(3..5).unwrap_or(""))?;
            let second = parse_min_sec(s.get(6..).unwrap_or(""))?;
            let sec = hour * 3600 + minute * 60 + second;
            let st = Stamp::new(sec * 1000);
            if st != Stamp::default() {
                return Ok(st);
            }
        }
        Err(Error::InvalidData("stamp"))
    }
}

impl Default for Stamp {
    fn default() -> Self {
        Stamp(Stamp::NONE)
    }
}

impl Stamp {
    /// Valid time stamps range from 0 to MIDNIGHT
    const MIDNIGHT: u32 = 24 * 60 * 60 * 1000;

    /// Value indicating missing time stamp
    const NONE: u32 = u32::MAX - 1;

    /// Value indicating logging reset event
    const RESET: u32 = u32::MAX;

    /// Create a new time stamp
    fn new(value: u32) -> Self {
        if value < Stamp::MIDNIGHT {
            Stamp(value)
        } else {
            Stamp::default()
        }
    }

    /// Create a new reset stamp
    fn new_reset() -> Self {
        Stamp(Stamp::RESET)
    }

    /// Check for reset
    fn is_reset(self) -> bool {
        self.0 == Stamp::RESET
    }

    /// Check for none
    fn is_none(self) -> bool {
        self.0 == Stamp::NONE
    }

    /// Get time stamp, if valid
    fn stamp(self) -> Option<u32> {
        if self.0 < Stamp::MIDNIGHT {
            Some(self.0)
        } else {
            None
        }
    }
}

#[allow(dead_code)]
impl VehicleEvent {
    /// Create a new reset event
    fn new_reset() -> Self {
        VehicleEvent {
            stamp: Stamp::new_reset(),
            ..Default::default()
        }
    }

    /// Create a new vehicle event
    fn new(line: &str) -> Result<Self> {
        let line = line.trim();
        if line == "*" {
            return Ok(VehicleEvent::new_reset());
        }
        let mut ev = Self::default();
        let mut val = line.split(',');
        match val.next() {
            Some(dur) => {
                if dur != "?" {
                    ev.duration = dur.parse().ok();
                }
            }
            None => return Err(Error::InvalidData("duration")),
        }
        match val.next() {
            Some(hdw) => {
                if hdw != "?" {
                    ev.headway = hdw.parse().ok();
                }
            }
            None => return Err(Error::InvalidData("headway")),
        }
        if let Some(stamp) = val.next()
            && !stamp.is_empty()
        {
            ev.stamp = stamp.parse()?;
        }
        if let Some(speed) = val.next()
            && !speed.is_empty()
        {
            ev.speed = speed.parse().ok();
        }
        if let Some(length) = val.next()
            && !length.is_empty()
        {
            ev.length = length.parse().ok();
        }
        Ok(ev)
    }

    /// Set the stamp
    pub fn with_stamp(mut self, stamp: u32) -> Self {
        self.stamp = Stamp::new(stamp);
        self
    }

    /// Set the duration
    pub fn with_duration(mut self, duration: u16) -> Self {
        self.duration = NonZeroU16::new(duration);
        self
    }

    /// Set the headway
    pub fn with_headway(mut self, headway: u32) -> Self {
        self.headway = NonZeroU32::new(headway);
        self
    }

    /// Set the speed
    pub fn with_speed(mut self, speed: u8) -> Self {
        self.speed = NonZeroU8::new(speed);
        self
    }

    /// Set the length
    pub fn with_length(mut self, length: u8) -> Self {
        self.length = NonZeroU8::new(length);
        self
    }

    /// Check for reset event
    pub fn is_reset(&self) -> bool {
        self.stamp.is_reset()
    }

    /// Get a (near) time stamp for the previous vehicle event
    fn previous(&self) -> Stamp {
        if let (Some(headway), Some(stamp)) = (self.headway(), self.stamp())
            && stamp >= headway
        {
            return Stamp::new(stamp - headway);
        }
        Stamp::default()
    }

    /// Set time stamp or headway from previous stamp
    fn set_previous(&mut self, st: u32) {
        match (self.headway(), self.stamp()) {
            (Some(headway), None) => {
                self.stamp = Stamp::new(st + headway);
            }
            (None, Some(stamp)) if stamp >= st => {
                self.headway = NonZeroU32::new(stamp - st)
            }
            _ => (),
        }
    }

    /// Propogate time stamp from previous event
    fn propogate_stamp(&mut self, previous: Option<u32>) {
        if !self.is_reset()
            && let Some(pr) = previous
        {
            self.set_previous(pr);
        }
    }

    /// Get event time stamp
    pub fn stamp(&self) -> Option<u32> {
        self.stamp.stamp()
    }

    /// Get the duration (ms)
    pub fn duration(&self) -> Option<u32> {
        self.duration.map(|d| d.get().into())
    }

    /// Get the headway (ms)
    pub fn headway(&self) -> Option<u32> {
        self.headway.map(|h| h.get())
    }

    /// Get the speed (mph)
    pub fn speed(&self) -> Option<u32> {
        self.speed.map(|s| s.get().into())
    }

    /// Get the length (ft)
    pub fn length(&self) -> Option<u32> {
        self.length.map(|f| f.get().into())
    }
}

impl VehLogReader {
    /// Create a vehicle event log (`.vlog`) from an async reader
    pub async fn from_reader_async<R>(reader: R) -> Result<Self>
    where
        R: AsyncReadExt + Unpin,
    {
        let mut log = Self::default();
        let mut lines = BufReader::new(reader).lines();
        while let Some(line) = lines.next_line().await? {
            log.append(&line)?;
        }
        log.finish();
        Ok(log)
    }

    /// Create a vehicle event log from a reader (blocking)
    pub fn from_reader_blocking<R>(reader: R) -> Result<Self>
    where
        R: BlockingRead,
    {
        let mut log = Self::default();
        for line in std::io::BufReader::new(reader).lines() {
            log.append(&line?)?;
        }
        log.finish();
        Ok(log)
    }

    /// Append an event to the log
    fn append(&mut self, line: &str) -> Result<()> {
        let line = line.trim();
        if line.is_empty() {
            return Ok(());
        }
        let mut ev = VehicleEvent::new(line)?;
        ev.propogate_stamp(self.previous);
        // Add Reset if time stamp went backwards
        if let Some(latest) = self.latest
            && let Some(stamp) = ev.stamp()
            && stamp < latest
        {
            self.events.push(VehicleEvent::new_reset());
            self.latest = Some(stamp);
        }
        self.previous = ev.stamp();
        if self.previous.is_some() {
            self.latest = self.previous;
        }
        self.events.push(ev);
        Ok(())
    }

    /// Fill in event gaps
    fn finish(&mut self) {
        self.propogate_backward();
        self.interpolate_missing_stamps();
    }

    /// Propogate timestamps backward to previous events
    fn propogate_backward(&mut self) {
        let mut stamp = Stamp::default();
        let mut it = self.events.iter_mut();
        while let Some(ev) = it.next_back() {
            if ev.is_reset() {
                stamp = Stamp::default();
            } else {
                if ev.stamp.is_none() {
                    ev.stamp = stamp;
                }
                stamp = ev.previous();
            }
        }
    }

    /// Interpolate timestamps in gaps where they are missing
    fn interpolate_missing_stamps(&mut self) {
        let mut before = None; // index of event before gap
        for i in 0..self.events.len() {
            if self.events[i].is_reset() {
                before = None;
            }
            let stamp = &self.events[i].stamp();
            if let Some(stamp) = stamp {
                if let Some(b) = before {
                    let total = (i - b) as u32;
                    if total > 1 {
                        // interpolate
                        let cev: &VehicleEvent = &self.events[b];
                        let mut st = cev.stamp().unwrap();
                        let gap = stamp - st;
                        let headway = gap / total;
                        for j in b..i {
                            let v = &mut self.events[j + 1];
                            if !v.is_reset() {
                                if v.headway.is_none() {
                                    v.headway = NonZeroU32::new(headway);
                                }
                                v.set_previous(st);
                            }
                            st += headway;
                        }
                    }
                }
                before = Some(i);
            }
        }
    }

    /// Get all vehicle events
    pub fn events(self) -> Vec<VehicleEvent> {
        self.events
    }
}

#[cfg(test)]
mod test {
    use super::*;

    #[test]
    fn veh_events() {
        assert_eq!(VehicleEvent::new("?,?").unwrap(), VehicleEvent::default());
        assert_eq!(
            VehicleEvent::new("37,?").unwrap(),
            VehicleEvent::default().with_duration(37)
        );
        assert_eq!(
            VehicleEvent::new("?,666").unwrap(),
            VehicleEvent::default().with_headway(666)
        );
        assert_eq!(
            VehicleEvent::new("55,?,12:34:56").unwrap(),
            VehicleEvent::default()
                .with_stamp(45_296_000)
                .with_duration(55)
        );
        assert_eq!(
            VehicleEvent::new("74,1234,,61").unwrap(),
            VehicleEvent::default()
                .with_duration(74)
                .with_headway(1234)
                .with_speed(61)
        );
        assert_eq!(
            VehicleEvent::new("1,4321,,,19").unwrap(),
            VehicleEvent::default()
                .with_duration(1)
                .with_headway(4321)
                .with_length(19)
        );
    }

    #[test]
    fn bad_veh_events() {
        assert!(VehicleEvent::new("").is_err());
        assert!(VehicleEvent::new("1").is_err());
        assert!(VehicleEvent::new("?,?,?").is_err());
        assert!(VehicleEvent::new("10,?,24:59:59").is_err());
        assert!(VehicleEvent::new("15,?,23:60:59").is_err());
        assert!(VehicleEvent::new("25,?,23:59:60").is_err());
    }

    #[test]
    fn events() {
        let ev = VehicleEvent::new_reset();
        assert_eq!(VehicleEvent::new("*").unwrap(), ev);
        assert_eq!(VehicleEvent::new("\t*  ").unwrap(), ev);
        assert_eq!(VehicleEvent::new("*\n").unwrap(), ev);
        assert_eq!(
            VehicleEvent::new("37,?").unwrap(),
            VehicleEvent::default().with_duration(37)
        );
        assert_eq!(std::mem::size_of::<VehicleEvent>(), 12);
    }

    const LOG: &str = "*
    296,9930,17:49:36
    231,14069
    240,453,,45,18
    496,23510,,53,62
    259,1321
    ?,?
    249,?
    323,4638,17:50:28
    258,5967,,55
    111,1542
    304,12029
    ";

    #[test]
    fn log() {
        let log = VehLogReader::from_reader_blocking(LOG.as_bytes()).unwrap();
        assert_eq!(log.events[0], VehicleEvent::new_reset());
        assert_eq!(
            log.events[1],
            VehicleEvent::default()
                .with_stamp(64_176_000)
                .with_duration(296)
                .with_headway(9930)
        );
        assert_eq!(
            log.events[2],
            VehicleEvent::default()
                .with_stamp(64_190_069)
                .with_duration(231)
                .with_headway(14_069)
        );
        assert_eq!(
            log.events[3],
            VehicleEvent::default()
                .with_stamp(64_190_522)
                .with_duration(240)
                .with_headway(453)
                .with_speed(45)
                .with_length(18)
        );
        assert_eq!(
            log.events[4],
            VehicleEvent::default()
                .with_stamp(64_214_032)
                .with_duration(496)
                .with_headway(23_510)
                .with_speed(53)
                .with_length(62)
        );
        assert_eq!(
            log.events[5],
            VehicleEvent::default()
                .with_stamp(64_215_353)
                .with_duration(259)
                .with_headway(1321)
        );
        assert_eq!(
            log.events[6],
            VehicleEvent::default()
                .with_stamp(64_219_357)
                .with_headway(4004)
        );
        assert_eq!(
            log.events[7],
            VehicleEvent::default()
                .with_stamp(64_223_362)
                .with_duration(249)
                .with_headway(4004)
        );
        assert_eq!(
            log.events[8],
            VehicleEvent::default()
                .with_stamp(64_228_000)
                .with_duration(323)
                .with_headway(4638)
        );
        assert_eq!(
            log.events[9],
            VehicleEvent::default()
                .with_stamp(64_233_967)
                .with_duration(258)
                .with_headway(5967)
                .with_speed(55)
        );
        assert_eq!(
            log.events[10],
            VehicleEvent::default()
                .with_stamp(64_235_509)
                .with_duration(111)
                .with_headway(1542)
        );
        assert_eq!(
            log.events[11],
            VehicleEvent::default()
                .with_stamp(64_247_538)
                .with_duration(304)
                .with_headway(12_029)
        );
    }
}
