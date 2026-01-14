// Copyright (C) 2026  Minnesota Department of Transportation
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
use resin::Result;
use resin::event::{Stamp, VehEvent};
use std::collections::HashMap;
use std::num::NonZeroU8;
use tokio::sync::mpsc::{
    UnboundedReceiver, UnboundedSender, unbounded_channel,
};
use tokio::time::{
    Duration, Instant, MissedTickBehavior, interval, sleep_until,
};

/// Detector vehicle event
pub struct DetEvent {
    /// Detector ID
    det: String,
    /// Vehicle event
    ev: VehEvent,
}

/// Periodic interval binner
pub struct IntervalBinner {
    /// Event receiver
    rx: UnboundedReceiver<DetEvent>,
    /// Event sender
    tx: UnboundedSender<DetEvent>,
    /// Detector speeds for previous interval
    speeds_prev: HashMap<String, Vec<Option<NonZeroU8>>>,
    /// Detector speeds for current interval
    speeds_curr: HashMap<String, Vec<Option<NonZeroU8>>>,
}

impl DetEvent {
    /// Create a new detector vehicle event
    pub fn new(det: &str, ev: VehEvent) -> Self {
        DetEvent {
            det: det.to_string(),
            ev,
        }
    }

    /// Get event interval
    fn interval(&self) -> Option<u32> {
        self.ev.stamp().map(|st| st.interval(30))
    }

    /// Bin vehicle in map
    fn bin(self, map: &mut HashMap<String, Vec<Option<NonZeroU8>>>) {
        let mph = self.ev.speed_mph().and_then(NonZeroU8::new);
        if let Some(val) = map.get_mut(&self.det) {
            val.push(mph);
        } else {
            let spds = vec![mph];
            map.insert(self.det, spds);
        }
    }
}

impl Default for IntervalBinner {
    fn default() -> Self {
        Self::new()
    }
}

impl IntervalBinner {
    /// Create a new interval binner
    pub fn new() -> Self {
        let (tx, rx) = unbounded_channel();
        IntervalBinner {
            rx,
            tx,
            speeds_prev: HashMap::new(),
            speeds_curr: HashMap::new(),
        }
    }

    /// Get event sender
    pub fn sender(&self) -> UnboundedSender<DetEvent> {
        self.tx.clone()
    }

    /// Run interval binner
    pub async fn run(mut self) -> Result<()> {
        // delay until :05 / :35 after next interval
        let ms = Stamp::now().ms_since_midnight() % 30_000;
        let delay = u64::from(35_000 - ms);
        sleep_until(Instant::now() + Duration::from_millis(delay)).await;
        // 30 second interval ticker
        let mut ticker = interval(Duration::from_secs(30));
        ticker.set_missed_tick_behavior(MissedTickBehavior::Skip);
        loop {
            ticker.tick().await;
            if self.tally_vehicle_events().await {
                break;
            }
            self.write_json().await?;
            self.speeds_prev = self.speeds_curr;
            self.speeds_curr = HashMap::new();
        }
        log::warn!("interval binner stopped!");
        Ok(())
    }

    /// Tally vehicle events for previous interval
    async fn tally_vehicle_events(&mut self) -> bool {
        let st = Stamp::now();
        let curr = st.interval(30);
        log::debug!("interval {curr} at {}", st.ms_since_midnight());
        let prev = if curr > 0 {
            curr - 1
        } else {
            (24 * 60 * 2) - 1
        };
        while !self.rx.is_empty() {
            match self.rx.recv().await {
                Some(dev) => {
                    if let Some(i) = dev.interval() {
                        if i == prev {
                            dev.bin(&mut self.speeds_prev);
                        } else if i == curr {
                            dev.bin(&mut self.speeds_curr);
                        } else {
                            log::info!("unexpected interval {i} != {curr}");
                        }
                    }
                }
                None => return true,
            }
        }
        false
    }

    /// Write binned interval as JSON
    async fn write_json(&self) -> Result<()> {
        // FIXME
        unimplemented!();
    }
}
