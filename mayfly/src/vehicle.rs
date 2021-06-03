// vehicle.rs
//
// Copyright (c) 2021  Minnesota Department of Transportation
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
use crate::common::{Error, Result};
use crate::query::TrafficData;

/// Single logged vehicle event
#[derive(Clone, Debug, Default, Eq, PartialEq)]
pub struct VehicleEvent {
    /// Duration vehicle was detected (ms)
    pub duration: Option<u32>,

    /// Headway from start of previous vehicle to this one (ms)
    pub headway: Option<u32>,

    /// Time stamp (ms of day; 0 - 86.4 million)
    pub stamp: Option<u32>,

    /// Vehicle speed (mph)
    pub speed: Option<u32>,

    /// Vehicle length (ft)
    pub length: Option<u32>,
}

/// Event from vehicle log
#[derive(Clone, Debug, Eq, PartialEq)]
pub enum Event {
    /// Collection data reset
    Reset,
    /// Vehicle event
    Vehicle(VehicleEvent),
}

/// Vehicle event log for one detector on one day
#[derive(Default)]
pub struct Log {
    /// All events in the log
    events: Vec<Event>,
    /// Previous event time stamp
    previous: Option<u32>,
    /// Latest logged time stamp
    latest: Option<u32>,
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
}

/// Vehicle event data binning
#[derive(Default)]
pub struct Bin<T: TrafficData> {
    /// Reset on previous event
    reset: bool,
    /// Binned traffic data periods
    periods: Vec<T>,
}

/// Parse an integer from a vehicle log
fn parse_u32(v: &str) -> Result<u32> {
    match v.parse() {
        Ok(v) => Ok(v),
        _ => Err(Error::InvalidData),
    }
}

/// Parse an hour from a time stamp
fn parse_hour(hour: &str) -> Result<u32> {
    match hour.parse() {
        Ok(h) if h < 24 => Ok(h),
        _ => Err(Error::InvalidData),
    }
}

/// Parse a minute or second from a time stamp
fn parse_min_sec(min_sec: &str) -> Result<u32> {
    match min_sec.parse() {
        Ok(ms) if ms < 60 => Ok(ms),
        _ => Err(Error::InvalidData),
    }
}

/// Parse a time stamp from a vehicle log
fn parse_stamp(stamp: &str) -> Result<u32> {
    if stamp.len() == 8
        && stamp.get(2..3) == Some(":")
        && stamp.get(5..6) == Some(":")
    {
        let hour = parse_hour(stamp.get(..2).unwrap_or(""))?;
        let minute = parse_min_sec(stamp.get(3..5).unwrap_or(""))?;
        let second = parse_min_sec(stamp.get(6..).unwrap_or(""))?;
        let sec = hour * 3600 + minute * 60 + second;
        Ok(sec * 1000)
    } else {
        Err(Error::InvalidData)
    }
}

impl VehicleEvent {
    /// Create a new vehicle event
    fn new(line: &str) -> Result<Self> {
        let mut veh = Self::default();
        let mut val = line.split(',');
        match val.next() {
            Some(dur) => {
                if dur != "?" {
                    veh.duration = Some(parse_u32(dur)?);
                }
            }
            None => return Err(Error::InvalidData),
        }
        match val.next() {
            Some(hdw) => {
                if hdw != "?" {
                    veh.headway = Some(parse_u32(hdw)?);
                }
            }
            None => return Err(Error::InvalidData),
        }
        if let Some(stamp) = val.next() {
            if !stamp.is_empty() {
                veh.stamp = Some(parse_stamp(stamp)?);
            }
        }
        if let Some(speed) = val.next() {
            if !speed.is_empty() {
                veh.speed = Some(parse_u32(speed)?);
            }
        }
        if let Some(length) = val.next() {
            if !length.is_empty() {
                veh.length = Some(parse_u32(length)?);
            }
        }
        Ok(veh)
    }

    /// Get a (near) time stamp for the previous vehicle event
    fn previous(&self) -> Option<u32> {
        if let (Some(headway), Some(stamp)) = (self.headway, self.stamp) {
            if stamp >= headway {
                return Some(stamp - headway);
            }
        }
        None
    }

    /// Set time stamp or headway from previous stamp
    fn set_previous(&mut self, st: u32) {
        match (self.headway, self.stamp) {
            (Some(headway), None) => self.stamp = Some(st + headway),
            (None, Some(stamp)) if stamp >= st => {
                self.headway = Some(stamp - st)
            }
            _ => (),
        }
    }
}

impl Event {
    /// Create a new log event
    pub fn new(line: &str) -> Result<Self> {
        let line = line.trim();
        if line == "*" {
            Ok(Event::Reset)
        } else {
            Ok(Event::Vehicle(VehicleEvent::new(line)?))
        }
    }

    /// Get event time stamp
    fn stamp(&self) -> Option<u32> {
        match self {
            Event::Reset => None,
            Event::Vehicle(veh) => veh.stamp,
        }
    }

    /// Propogate time stamp from previous event
    fn propogate_stamp(&mut self, previous: Option<u32>) {
        if let Event::Vehicle(veh) = self {
            if let Some(pr) = previous {
                veh.set_previous(pr);
            }
        }
    }
}

impl Log {
    /// Append an event to the log
    pub fn append(&mut self, line: &str) -> Result<()> {
        let line = line.trim();
        if line.is_empty() {
            return Ok(());
        }
        let mut ev = Event::new(line)?;
        ev.propogate_stamp(self.previous);
        // Add Reset if time stamp went backwards
        if let Some(latest) = self.latest {
            if let Some(stamp) = ev.stamp() {
                if stamp < latest {
                    self.events.push(Event::Reset);
                    self.latest = Some(stamp);
                }
            }
        }
        self.previous = ev.stamp();
        if self.previous.is_some() {
            self.latest = self.previous;
        }
        self.events.push(ev);
        Ok(())
    }

    /// Fill in event gaps
    pub fn finish(&mut self) {
        self.propogate_backward();
        self.interpolate_missing_stamps();
    }

    /// Propogate timestamps backward to previous events
    fn propogate_backward(&mut self) {
        let mut stamp = None;
        let mut it = self.events.iter_mut();
        while let Some(ev) = it.next_back() {
            match ev {
                Event::Reset => stamp = None,
                Event::Vehicle(veh) => {
                    if veh.stamp.is_none() {
                        veh.stamp = stamp;
                    }
                    stamp = veh.previous();
                }
            }
        }
    }

    /// Interpolate timestamps in gaps where they are missing
    fn interpolate_missing_stamps(&mut self) {
        let mut before = None; // index of event before gap
        for i in 0..self.events.len() {
            if let Event::Reset = &self.events[i] {
                before = None;
            }
            let stamp = &self.events[i].stamp();
            if let Some(stamp) = stamp {
                if let Some(b) = before {
                    let total = (i - b) as u32;
                    if total > 1 {
                        // interpolate
                        let cev: &Event = &self.events[b];
                        let mut st = cev.stamp().unwrap();
                        let gap = stamp - st;
                        let headway = gap / total;
                        for j in b..i {
                            if let Event::Vehicle(v) = &mut self.events[j + 1] {
                                if v.headway.is_none() {
                                    v.headway = Some(headway);
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

    /// Put vehicle events into 30 second bins
    pub fn bin_30_seconds<T: TrafficData>(
        &self,
        filter: VehicleFilter,
    ) -> Result<Vec<T>> {
        let mut bin = Bin::default();
        for ev in &self.events {
            match ev {
                Event::Reset => bin.reset(),
                Event::Vehicle(veh) => {
                    if filter.check(veh) {
                        bin.vehicle(veh)?;
                    }
                }
            }
        }
        Ok(bin.finish())
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

    /// Check if vehicle should be binned
    fn check(&self, veh: &VehicleEvent) -> bool {
        if let Some(m) = self.length_ft_min {
            // use 0 for unknown length
            if veh.length.unwrap_or(0) < m {
                return false;
            }
        }
        if let Some(m) = self.length_ft_max {
            // use MAX value for unknown length
            if veh.length.unwrap_or(u32::MAX) >= m {
                return false;
            }
        }
        if let Some(m) = self.speed_mph_min {
            // use 0 for unknown speed
            if veh.speed.unwrap_or(0) < m {
                return false;
            }
        }
        if let Some(m) = self.speed_mph_max {
            // use MAX value for unknown speed
            if veh.speed.unwrap_or(u32::MAX) >= m {
                return false;
            }
        }
        true
    }
}

impl<T: TrafficData> Bin<T> {
    /// Reset event log
    fn reset(&mut self) {
        self.reset = true;
    }

    /// Add a vehicle event
    fn vehicle(&mut self, veh: &VehicleEvent) -> Result<()> {
        match veh.stamp {
            Some(stamp) => {
                let per = period_30_second(stamp);
                if per >= 2880 {
                    return Err(Error::InvalidData);
                }
                while self.periods.len() <= per {
                    let mut data = T::default();
                    if self.reset {
                        data.reset();
                    }
                    self.periods.push(data);
                }
                let data = &mut self.periods[per];
                data.vehicle(veh);
                self.reset = false;
                Ok(())
            }
            None => {
                // No timestamp; add to last period and hope for the best!
                let len = self.periods.len();
                if len > 0 {
                    let data = &mut self.periods[len - 1];
                    data.vehicle(veh);
                    data.reset();
                }
                Ok(())
            }
        }
    }

    /// Finish binning
    fn finish(self) -> Vec<T> {
        let mut periods = self.periods;
        while periods.len() < 2880 {
            let mut data = T::default();
            if self.reset {
                data.reset();
            }
            periods.push(data);
        }
        periods
    }
}

/// Get the 30-second period for the given timestamp (ms)
fn period_30_second(ms: u32) -> usize {
    (ms as usize) / 30_000
}

#[cfg(test)]
mod test {
    use super::*;

    #[test]
    fn veh_events() {
        let mut veh = VehicleEvent::default();
        assert_eq!(VehicleEvent::new("?,?").unwrap(), veh);
        veh.duration = Some(37);
        assert_eq!(VehicleEvent::new("37,?").unwrap(), veh);
        let mut veh = VehicleEvent::default();
        veh.headway = Some(666);
        assert_eq!(VehicleEvent::new("?,666").unwrap(), veh);
        let mut veh = VehicleEvent::default();
        veh.duration = Some(55);
        veh.stamp = Some(45_296_000);
        assert_eq!(VehicleEvent::new("55,?,12:34:56").unwrap(), veh);
        let mut veh = VehicleEvent::default();
        veh.duration = Some(74);
        veh.headway = Some(1234);
        veh.speed = Some(61);
        assert_eq!(VehicleEvent::new("74,1234,,61").unwrap(), veh);
        let mut veh = VehicleEvent::default();
        veh.duration = Some(1);
        veh.headway = Some(4321);
        veh.length = Some(19);
        assert_eq!(VehicleEvent::new("1,4321,,,19").unwrap(), veh);
    }

    #[test]
    fn bad_veh_events() {
        assert!(VehicleEvent::new("").is_err());
        assert!(VehicleEvent::new("1").is_err());
        assert!(VehicleEvent::new("?,").is_err());
        assert!(VehicleEvent::new(",?").is_err());
        assert!(VehicleEvent::new("?,?,?").is_err());
        assert!(VehicleEvent::new("*").is_err());
        assert!(VehicleEvent::new("10,?,24:59:59").is_err());
        assert!(VehicleEvent::new("15,?,23:60:59").is_err());
        assert!(VehicleEvent::new("25,?,23:59:60").is_err());
        assert!(VehicleEvent::new("25,50,,-5").is_err());
        assert!(VehicleEvent::new("25,50,,,X").is_err());
    }

    #[test]
    fn events() {
        let ev = Event::Reset;
        assert_eq!(Event::new("*").unwrap(), ev);
        assert_eq!(Event::new("\t*  ").unwrap(), ev);
        assert_eq!(Event::new("*\n").unwrap(), ev);
        let mut veh = VehicleEvent::default();
        veh.duration = Some(37);
        let ev = Event::Vehicle(veh);
        assert_eq!(Event::new("37,?").unwrap(), ev);
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
        let mut log = Log::default();
        for line in LOG.split('\n') {
            log.append(line).unwrap();
        }
        log.finish();
        assert_eq!(log.events[0], Event::Reset);
        let mut veh = VehicleEvent::default();
        veh.duration = Some(296);
        veh.headway = Some(9930);
        veh.stamp = Some(64_176_000);
        assert_eq!(log.events[1], Event::Vehicle(veh));
        let mut veh = VehicleEvent::default();
        veh.duration = Some(231);
        veh.headway = Some(14_069);
        veh.stamp = Some(64_190_069);
        assert_eq!(log.events[2], Event::Vehicle(veh));
        let mut veh = VehicleEvent::default();
        veh.duration = Some(240);
        veh.headway = Some(453);
        veh.stamp = Some(64_190_522);
        veh.speed = Some(45);
        veh.length = Some(18);
        assert_eq!(log.events[3], Event::Vehicle(veh));
        let mut veh = VehicleEvent::default();
        veh.duration = Some(496);
        veh.headway = Some(23_510);
        veh.stamp = Some(64_214_032);
        veh.speed = Some(53);
        veh.length = Some(62);
        assert_eq!(log.events[4], Event::Vehicle(veh));
        let mut veh = VehicleEvent::default();
        veh.duration = Some(259);
        veh.headway = Some(1321);
        veh.stamp = Some(64_215_353);
        assert_eq!(log.events[5], Event::Vehicle(veh));
        let mut veh = VehicleEvent::default();
        veh.headway = Some(4004);
        veh.stamp = Some(64_219_357);
        assert_eq!(log.events[6], Event::Vehicle(veh));
        let mut veh = VehicleEvent::default();
        veh.duration = Some(249);
        veh.headway = Some(4004);
        veh.stamp = Some(64_223_362);
        assert_eq!(log.events[7], Event::Vehicle(veh));
        let mut veh = VehicleEvent::default();
        veh.duration = Some(323);
        veh.headway = Some(4638);
        veh.stamp = Some(64_228_000);
        assert_eq!(log.events[8], Event::Vehicle(veh));
        let mut veh = VehicleEvent::default();
        veh.duration = Some(258);
        veh.headway = Some(5967);
        veh.stamp = Some(64_233_967);
        veh.speed = Some(55);
        assert_eq!(log.events[9], Event::Vehicle(veh));
        let mut veh = VehicleEvent::default();
        veh.duration = Some(111);
        veh.headway = Some(1542);
        veh.stamp = Some(64_235_509);
        assert_eq!(log.events[10], Event::Vehicle(veh));
        let mut veh = VehicleEvent::default();
        veh.duration = Some(304);
        veh.headway = Some(12_029);
        veh.stamp = Some(64_247_538);
        assert_eq!(log.events[11], Event::Vehicle(veh));
    }

    #[test]
    fn log_bad() {
        let mut log = Log::default();
        assert!(log.append("296,9930,17:49:36").is_ok());
        assert!(log.append("231,1234,17:45:00").is_err());
        log.finish();
    }
}
