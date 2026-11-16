// domain.rs
//
// Copyright (C) 2024-2026  Minnesota Department of Transportation
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
use cidr::IpCidr;
use serde::{Deserialize, Serialize};
use std::net::IpAddr;

/// Domain
#[derive(Clone, Debug, Eq, PartialEq, Deserialize, Serialize)]
pub struct Domain {
    pub name: String,
    pub enabled: bool,
    pub block: IpCidr,
}

impl Domain {
    /// Check if any enabled domain contains a given address
    pub fn contains_any_enabled(domains: &[Self], addr: IpAddr) -> bool {
        for d in domains {
            if d.enabled && d.contains(addr) {
                return true;
            }
        }
        false
    }

    /// Check if domain contains a given address
    pub fn contains(&self, addr: IpAddr) -> bool {
        self.block.contains(&addr)
    }
}
