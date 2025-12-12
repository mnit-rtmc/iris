// vlg.rs
//
// Copyright (c) 2025  Minnesota Department of Transportation
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
use resin::event::{Stamp, VehEvent};
use std::io::Read as BlockingRead;
use tokio::io::{AsyncReadExt, BufReader};

/// Event log (`.vlg`) for one detector on one day
struct LogReader {
    /// Stamp at midnight
    midnight: Stamp,
    /// All vehicle events in the log
    events: Vec<VehEvent>,
}

impl LogReader {
    /// Create a new log reader
    fn new(date: &str) -> Result<Self> {
        match Stamp::try_from_date(date) {
            Some(midnight) => Ok(LogReader {
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

    /// Append an event to the log
    fn append(&mut self, val: u64) -> Result<()> {
        let mut ev = VehEvent::with_value(&self.midnight, val);
        // Add gap if time stamp went backwards
        if let Some(st) = ev.stamp()
            && let Some(last) = self.last()
        {
            ev = ev.with_gap(st.is_before(&last));
        }
        self.events.push(ev);
        Ok(())
    }
}

/// Read a vehicle event log (`.vlg`) from an async reader
pub async fn read_async<R>(date: &str, reader: R) -> Result<Vec<VehEvent>>
where
    R: AsyncReadExt + Unpin,
{
    let mut vlg = LogReader::new(date)?;
    let mut reader = BufReader::new(reader);
    let mut buf: [u8; 8] = [0; 8];
    while reader.read(&mut buf).await? == 8 {
        let val = u64::from_le_bytes(buf);
        vlg.append(val)?;
    }
    Ok(vlg.events)
}

/// Read a vehicle event log (`.vlg`) from a blocking reader
pub fn read_blocking<R>(date: &str, reader: R) -> Result<Vec<VehEvent>>
where
    R: BlockingRead,
{
    let mut vlg = LogReader::new(date)?;
    let mut reader = std::io::BufReader::new(reader);
    let mut buf: [u8; 8] = [0; 8];
    while reader.read(&mut buf)? == 8 {
        let val = u64::from_le_bytes(buf);
        vlg.append(val)?;
    }
    Ok(vlg.events)
}
