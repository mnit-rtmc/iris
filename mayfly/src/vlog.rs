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
use resin::event::{Mode, Stamp, VehEvent};
use std::io::BufRead as _;
use std::io::Read as BlockingRead;
use tokio::io::{AsyncBufReadExt, AsyncReadExt, BufReader};

/// Vehicle event log (`.vlog`) for one detector on one day
struct VehLogReader {
    /// Stamp at midnight
    midnight: Stamp,
    /// All vehicle events in the log
    events: Vec<VehEvent>,
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

/// Parse time-of-day from a vehicle log
fn parse_tod(s: &str) -> Result<u32> {
    if s.len() == 8 && s.get(2..3) == Some(":") && s.get(5..6) == Some(":") {
        let hour = parse_hour(s.get(..2).unwrap_or(""))?;
        let minute = parse_min_sec(s.get(3..5).unwrap_or(""))?;
        let second = parse_min_sec(s.get(6..).unwrap_or(""))?;
        let tod = hour * 3600 + minute * 60 + second;
        return Ok(tod * 1000);
    }
    Err(Error::InvalidData("stamp"))
}

/// Parse a vehicle event from a `.vlog` file
fn parse_event(line: &str, midnight: &Stamp) -> Result<VehEvent> {
    let line = line.trim();
    if line == "*" {
        return Ok(VehEvent::default().with_gap(true));
    }
    let mut val = line.split(',');
    let duration = val.next().ok_or(Error::InvalidData("duration"))?;
    let duration = duration.parse().ok();
    let headway = val.next().ok_or(Error::InvalidData("headway"))?;
    let headway = headway.parse().ok();
    let tod = match val.next() {
        Some(tod) if !tod.is_empty() => Some(parse_tod(tod)?),
        _ => None,
    };
    let speed = match val.next() {
        Some(speed) if !speed.is_empty() => speed.parse().ok(),
        _ => None,
    };
    let length = match val.next() {
        Some(length) if !length.is_empty() => length.parse().ok(),
        _ => None,
    };
    let mut ev = VehEvent::default();
    if let Some(tod) = tod {
        ev = ev.with_stamp_mode(
            midnight.clone().with_ms_since_midnight(tod),
            Mode::SensorRecorded,
        );
    }
    if let Some(headway) = headway {
        ev = ev.with_headway_ms(headway);
    }
    if let Some(speed) = speed {
        ev = ev.with_speed_mph(speed);
    }
    if let Some(length) = length {
        ev = ev.with_length_ft(length);
    }
    if let Some(duration) = duration {
        ev = ev.with_duration_ms(duration);
    }
    Ok(ev)
}

impl VehLogReader {
    /// Create a new vlog reader
    fn new(date: &str) -> Result<Self> {
        match Stamp::try_from_date(date) {
            Some(midnight) => Ok(VehLogReader {
                midnight,
                events: Vec::new(),
            }),
            None => Err(Error::InvalidQuery("date")),
        }
    }

    /// Get the last event timestamp
    fn last(&self) -> Option<Stamp> {
        self.events.last().and_then(|last| last.stamp())
    }

    /// Get time-of-day of an event
    fn event_ms(&self, i: usize) -> Option<u32> {
        self.events[i].stamp().map(|st| st.ms_since_midnight())
    }

    /// Get elapsed time between two events (ms)
    fn elapsed(&self, a: usize, b: usize) -> Option<u32> {
        let begin = self.event_ms(a);
        let end = self.event_ms(b);
        if let (Some(begin), Some(end)) = (begin, end)
            && end > begin
        {
            return Some(end - begin);
        }
        None
    }

    /// Append an event to the log
    fn append(&mut self, line: &str) -> Result<()> {
        let line = line.trim();
        if line.is_empty() {
            return Ok(());
        }
        let mut ev = parse_event(line, &self.midnight)?;
        // Add gap if time stamp went backwards
        if let Some(st) = ev.stamp()
            && let Some(last) = self.last()
        {
            ev = ev.with_gap(st.is_before(&last));
        }
        self.events.push(ev);
        Ok(())
    }

    /// Fill in event gaps
    fn finish(&mut self) {
        // first, propogate timestamps for recorded headways
        self.propogate_stamps();
        // now, interpolate missing headways
        self.interpolate_headways();
        // finally, propogate timestamps for interpolated headways
        self.propogate_stamps();
    }

    /// Propogate timestamps with headway values
    fn propogate_stamps(&mut self) {
        let mut stamp: Option<Stamp> = None;
        let mut mode = Mode::NoTimestamp;
        // propogate forwards
        for ev in self.events.iter_mut() {
            if !ev.gap()
                && ev.stamp().is_none()
                && let (Some(st), Some(hw)) = (stamp, ev.headway_ms())
            {
                let ms = st.ms_since_midnight() + hw;
                let st = st.with_ms_since_midnight(ms);
                *ev = ev.clone().with_stamp_mode(st, mode);
            }
            mode = ev.mode();
            stamp = if mode.is_recorded() { ev.stamp() } else { None };
        }
        // propogate backwards
        stamp = None;
        for ev in self.events.iter_mut().rev() {
            if ev.stamp().is_none()
                && let Some(st) = stamp
            {
                *ev = ev.clone().with_stamp_mode(st, mode);
            }
            stamp = None;
            mode = ev.mode();
            if mode.is_recorded()
                && !ev.gap()
                && let (Some(st), Some(hw)) = (ev.stamp(), ev.headway_ms())
            {
                let ms = st.ms_since_midnight();
                if ms >= hw {
                    stamp = Some(st.with_ms_since_midnight(ms - hw));
                }
            }
        }
    }

    /// Interpolate headways in gaps where they are missing
    fn interpolate_headways(&mut self) {
        let mut before = None; // index of event before gap
        for i in 0..self.events.len() {
            let ev = &self.events[i];
            if ev.gap() {
                before = None;
                continue;
            }
            if ev.headway_ms().is_none() && ev.stamp().is_none() {
                continue;
            }
            if let Some(b) = before
                && (i - b) > 1
                && let Some(elapsed) = self.elapsed(b, i)
            {
                let avg_headway = elapsed / ((i - b) as u32);
                for j in b + 1..=i {
                    let ev = &mut self.events[j];
                    *ev = ev.clone().with_headway_ms(avg_headway);
                }
            }
            before = Some(i);
        }
    }
}

/// Read a vehicle event log (`.vlog`) from an async reader
pub async fn read_async<R>(date: &str, reader: R) -> Result<Vec<VehEvent>>
where
    R: AsyncReadExt + Unpin,
{
    let mut vlog = VehLogReader::new(date)?;
    let mut lines = BufReader::new(reader).lines();
    while let Some(line) = lines.next_line().await? {
        vlog.append(&line)?;
    }
    vlog.finish();
    Ok(vlog.events)
}

/// Read a vehicle event log (`.vlog`) from a blocking reader
pub fn read_blocking<R>(date: &str, reader: R) -> Result<Vec<VehEvent>>
where
    R: BlockingRead,
{
    let mut vlog = VehLogReader::new(date)?;
    for line in std::io::BufReader::new(reader).lines() {
        vlog.append(&line?)?;
    }
    vlog.finish();
    Ok(vlog.events)
}

#[cfg(test)]
mod test {
    use super::*;

    fn mk_stamp(ms: u32) -> Stamp {
        let stamp = Stamp::try_from_date("20251211").unwrap();
        stamp.with_ms_since_midnight(ms)
    }

    fn mk_event(line: &str) -> VehEvent {
        let midnight = Stamp::try_from_date("20251211").unwrap();
        parse_event(line, &midnight).unwrap()
    }

    #[test]
    fn veh_events() {
        assert_eq!(mk_event("?,?"), VehEvent::default());
        assert_eq!(mk_event("37,?"), VehEvent::default().with_duration_ms(37));
        assert_eq!(mk_event("?,666"), VehEvent::default().with_headway_ms(666));
        assert_eq!(
            mk_event("55,?,12:34:56"),
            VehEvent::default()
                .with_stamp_mode(mk_stamp(45_296_000), Mode::SensorRecorded)
                .with_duration_ms(55)
        );
        assert_eq!(
            mk_event("74,1234,,61"),
            VehEvent::default()
                .with_duration_ms(74)
                .with_headway_ms(1234)
                .with_speed_mph(61.0)
        );
        assert_eq!(
            mk_event("1,4321,,,19"),
            VehEvent::default()
                .with_duration_ms(1)
                .with_headway_ms(4321)
                .with_length_ft(19.0)
        );
    }

    #[test]
    fn bad_veh_events() {
        let midnight = Stamp::try_from_date("20251211").unwrap();
        assert!(parse_event("", &midnight).is_err());
        assert!(parse_event("1", &midnight).is_err());
        assert!(parse_event("?,?,?", &midnight).is_err());
        assert!(parse_event("10,?,24:59:59", &midnight).is_err());
        assert!(parse_event("15,?,23:60:59", &midnight).is_err());
        assert!(parse_event("25,?,23:59:60", &midnight).is_err());
    }

    #[test]
    fn events() {
        let midnight = Stamp::try_from_date("20251211").unwrap();
        let ev = VehEvent::default().with_gap(true);
        assert_eq!(parse_event("*", &midnight).unwrap(), ev);
        assert_eq!(parse_event("\t*  ", &midnight).unwrap(), ev);
        assert_eq!(parse_event("*\n", &midnight).unwrap(), ev);
        assert_eq!(
            parse_event("37,?", &midnight).unwrap(),
            VehEvent::default().with_duration_ms(37)
        );
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
        let events = read_blocking("20251211", LOG.as_bytes()).unwrap();
        assert_eq!(
            events[0],
            VehEvent::default()
                .with_gap(true)
                .with_stamp_mode(mk_stamp(64_166_070), Mode::SensorRecorded)
        );
        assert_eq!(
            events[1],
            VehEvent::default()
                .with_stamp_mode(mk_stamp(64_176_000), Mode::SensorRecorded)
                .with_duration_ms(296)
                .with_headway_ms(9930)
        );
        assert_eq!(
            events[2],
            VehEvent::default()
                .with_stamp_mode(mk_stamp(64_190_069), Mode::SensorRecorded)
                .with_duration_ms(231)
                .with_headway_ms(14_069)
        );
        assert_eq!(
            events[3],
            VehEvent::default()
                .with_stamp_mode(mk_stamp(64_190_522), Mode::SensorRecorded)
                .with_duration_ms(240)
                .with_headway_ms(453)
                .with_speed_mph(45.0)
                .with_length_ft(18.0)
        );
        assert_eq!(
            events[4],
            VehEvent::default()
                .with_stamp_mode(mk_stamp(64_214_032), Mode::SensorRecorded)
                .with_duration_ms(496)
                .with_headway_ms(23_510)
                .with_speed_mph(53.0)
                .with_length_ft(62.0)
        );
        assert_eq!(
            events[5],
            VehEvent::default()
                .with_stamp_mode(mk_stamp(64_215_353), Mode::SensorRecorded)
                .with_duration_ms(259)
                .with_headway_ms(1321)
        );
        assert_eq!(
            events[6],
            VehEvent::default()
                .with_stamp_mode(mk_stamp(64_219_357), Mode::SensorRecorded)
                .with_headway_ms(4004)
        );
        assert_eq!(
            events[7],
            VehEvent::default()
                .with_stamp_mode(mk_stamp(64_223_362), Mode::SensorRecorded)
                .with_duration_ms(249)
                .with_headway_ms(4004)
        );
        assert_eq!(
            events[8],
            VehEvent::default()
                .with_stamp_mode(mk_stamp(64_228_000), Mode::SensorRecorded)
                .with_duration_ms(323)
                .with_headway_ms(4638)
        );
        assert_eq!(
            events[9],
            VehEvent::default()
                .with_stamp_mode(mk_stamp(64_233_967), Mode::SensorRecorded)
                .with_duration_ms(258)
                .with_headway_ms(5967)
                .with_speed_mph(55.0)
        );
        assert_eq!(
            events[10],
            VehEvent::default()
                .with_stamp_mode(mk_stamp(64_235_509), Mode::SensorRecorded)
                .with_duration_ms(111)
                .with_headway_ms(1542)
        );
        assert_eq!(
            events[11],
            VehEvent::default()
                .with_stamp_mode(mk_stamp(64_247_538), Mode::SensorRecorded)
                .with_duration_ms(304)
                .with_headway_ms(12_029)
        );
    }
}
