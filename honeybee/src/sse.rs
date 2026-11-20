// Copyright (C) 2021-2026  Minnesota Department of Transportation
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
use crate::sonar::Name;
use axum::response::sse::Event;
use std::convert::Infallible;
use std::time::SystemTime;
use tokio::sync::mpsc::{
    UnboundedReceiver, UnboundedSender, unbounded_channel,
};
use tower_sessions::session::Id;

/// Expiration for notifier activity
const EXPIRATION: std::time::Duration =
    std::time::Duration::from_secs(4 * 3600);

/// SSE event result
type EventResult = std::result::Result<Event, Infallible>;

/// Client SSE notifier
pub struct Notifier {
    /// Session Id
    id: Id,
    /// Listening channel names
    channels: Vec<Name>,
    /// Sse stream sender
    tx: Option<UnboundedSender<EventResult>>,
    /// Previous activity time
    activity: SystemTime,
}

impl Notifier {
    /// Make a new SSE notifier
    pub fn new(id: Id) -> Self {
        Notifier {
            id,
            channels: Vec::new(),
            tx: None,
            activity: SystemTime::now(),
        }
    }

    /// Get mutable channels
    pub fn channels_mut(&mut self) -> &mut Vec<Name> {
        &mut self.channels
    }

    /// Make channel
    pub fn make_channel(&mut self) -> UnboundedReceiver<EventResult> {
        if self.tx.is_some() {
            // NOTE: this happens when multiple tabs
            //       are open on the same browser
            log::warn!("make_channel: SSE sender {} exists", self.id);
        }
        let (tx, rx) = unbounded_channel();
        self.tx = Some(tx);
        rx
    }

    /// Check if notifier is expired
    pub fn is_expired(&self, now: SystemTime) -> bool {
        match now.duration_since(self.activity) {
            Ok(dur) => dur < EXPIRATION,
            Err(_e) => true,
        }
    }

    /// Check if notifier is listening to a channel
    fn is_listening(&self, nm: &Name) -> bool {
        for chan in &self.channels {
            if nm.res_type == chan.res_type {
                match (nm.object_n(), chan.object_n()) {
                    (Some(nn), Some(cn)) if nn == cn => {
                        return true;
                    }
                    (None, _) => return true,
                    _ => (),
                }
            }
        }
        false
    }

    /// Send an SSE event
    pub fn send_event_checked(&mut self, nm: &Name) {
        log::trace!("checking {nm} for {}", self.id);
        if self.is_listening(nm) {
            self.send_event(nm);
        }
    }

    /// Send an SSE event
    fn send_event(&mut self, nm: &Name) {
        log::trace!("send_event SSE: {nm} to {}", self.id);
        if let Some(tx) = &self.tx {
            self.activity = SystemTime::now();
            let ev = Event::default().data(nm.to_string());
            if let Err(e) = tx.send(Ok(ev)) {
                log::warn!("send_event SSE: {e}");
                self.tx = None;
            }
        }
    }

    /// Send an SSE event for all listening channels
    pub fn send_channels(&mut self) {
        log::debug!("send_channels SSE {} {}", self.id, self.channels.len());
        for nm in &self.channels.clone() {
            self.send_event(nm);
        }
    }
}
