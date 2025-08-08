// Copyright (C) 2022-2025  Minnesota Department of Transportation
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
use crate::item::{ItemState, ItemStates};
use crate::util::ContainsLower;
use ntcip::dms::multi::is_blank;
use serde::{Deserialize, Serialize};

/// Sign Message
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct SignMessage {
    pub name: String,
    pub sign_config: String,
    pub multi: String,
    pub msg_owner: String,
    pub sticky: bool,
    pub flash_beacon: bool,
    pub pixel_service: bool,
    pub msg_priority: u32,
}

impl SignMessage {
    /// Get message owner
    fn owner(&self) -> &str {
        &self.msg_owner
    }

    /// Get "system" owner
    fn system(&self) -> &str {
        self.owner().split(';').next().unwrap_or("").trim()
    }

    /// Get "sources" owner
    fn sources(&self) -> &str {
        self.owner().split(';').nth(1).unwrap_or("").trim()
    }

    /// Get "user" owner
    pub fn user(&self) -> &str {
        self.owner().split(';').nth(2).unwrap_or("").trim()
    }

    /// Get item states
    pub fn item_states(&self) -> ItemStates<'_> {
        let blank = is_blank(&self.multi);
        let sources = self.sources();
        let mut states = ItemStates::default();
        if sources.contains("blank") || blank {
            states = states.with(ItemState::Available, "");
        }
        if sources.contains("operator") {
            states = states.with(ItemState::Deployed, self.user());
        }
        if sources.contains("schedule") {
            states = states.with(ItemState::Planned, self.user());
        }
        if sources.contains("external") {
            states = states.with(ItemState::External, sources);
        }
        if sources.contains("incident") {
            states = states.with(ItemState::Incident, "");
        }
        if sources.is_empty() && !blank {
            states = states.with(ItemState::External, self.system());
        }
        states
    }

    /// Check if a search string matches
    pub fn is_match(&self, search: &str) -> bool {
        // checks are ordered by "most likely to be searched"
        self.multi.contains_lower(search)
            || self.user().contains_lower(search)
            || self.system().contains_lower(search)
    }
}
