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
use cidr::{Family, IpCidr};
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

    /// Find domain that best matches a given address
    pub fn best_match(domains: &[Self], addr: IpAddr) -> Option<&Domain> {
        let mut dom = None;
        let mut score = 0;
        for d in domains {
            if d.enabled
                && d.contains(addr)
                && (dom.is_none() || d.score() > score)
            {
                dom = Some(d);
                score = d.score();
            }
        }
        dom
    }

    /// Get domain "score", based on specificity
    fn score(&self) -> u8 {
        let len = self.block.network_length();
        match self.block.family() {
            Family::Ipv4 => len << 2,
            Family::Ipv6 => len,
        }
    }

    /// Check if domain contains a given address
    pub fn contains(&self, addr: IpAddr) -> bool {
        self.block.contains(&addr)
    }

    /// Check if domain is restricted
    pub fn is_restricted(&self) -> bool {
        let len = self.block.network_length();
        match self.block.family() {
            Family::Ipv4 => len < 16,
            Family::Ipv6 => len < 64,
        }
    }
}
