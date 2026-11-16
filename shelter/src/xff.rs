// xff.rs
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
use http::{HeaderMap, StatusCode};
use std::net::IpAddr;

/// X-Forwarded-For header
#[derive(Debug)]
pub struct XForwardedFor(Vec<IpAddr>);

impl XForwardedFor {
    /// Parse XFF header addresses
    pub fn parse_addrs(headers: &HeaderMap) -> Result<Self, StatusCode> {
        let mut ips = Vec::with_capacity(8);
        for header in headers.get_all("X-Forwarded-For") {
            let header = header.to_str().map_err(|e| {
                log::warn!("XFF parse: {header:?}, {e}");
                StatusCode::BAD_REQUEST
            })?;
            for addr in header.split(',') {
                let ip = addr.trim().parse::<IpAddr>().map_err(|e| {
                    log::warn!("XFF parse addr: {addr}, {e}");
                    StatusCode::BAD_REQUEST
                })?;
                ips.push(ip);
            }
        }
        Ok(XForwardedFor(ips))
    }

    /// Get iterator of addresses
    pub fn iter(&self) -> impl Iterator<Item = &IpAddr> {
        self.0.iter()
    }
}
