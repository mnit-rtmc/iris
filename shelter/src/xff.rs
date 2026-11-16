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

/// Parse XFF header addresses
fn parse_xff(headers: &HeaderMap) -> Result<Vec<IpAddr>, StatusCode> {
    let mut ips = Vec::with_capacity(8);
    for header in headers.get_all("X-Forwarded-For") {
        let header = header.to_str().map_err(|e| {
            log::warn!("XFF parse: {header:?}, {e}");
            StatusCode::BAD_REQUEST
        })?;
        for value in header.split(',') {
            ips.push(parse_addr(value)?);
        }
    }
    Ok(ips)
}

/// Parse `Forwarded` header addresses
fn parse_forwarded(headers: &HeaderMap) -> Result<Vec<IpAddr>, StatusCode> {
    let mut ips = Vec::with_capacity(8);
    for header in headers.get_all("Forwarded") {
        let header = header.to_str().map_err(|e| {
            log::warn!("Forwarded parse: {header:?}, {e}");
            StatusCode::BAD_REQUEST
        })?;
        for directive in header.split(',') {
            for kv in directive.split(';') {
                let Some((key, value)) = kv.split_once('=') else {
                    log::warn!("Forwarded invalid kv: {kv}");
                    return Err(StatusCode::BAD_REQUEST);
                };
                if key.trim().eq_ignore_ascii_case("for") {
                    ips.push(parse_addr(value)?);
                }
            }
        }
    }
    Ok(ips)
}

/// Parse an IP address in an XFF or Forwarded header
fn parse_addr(addr: &str) -> Result<IpAddr, StatusCode> {
    let mut addr = addr.trim();
    // NOTE: in `Forwarded` headers, IPv6 addresses are quoted and
    //       surrounded by square brackets
    if let Some(ad) = addr.strip_circumfix("\"[", "]\"") {
        addr = ad;
    }
    addr.parse::<IpAddr>().map_err(|e| {
        log::warn!("parse_addr: {addr}, {e}");
        StatusCode::BAD_REQUEST
    })
}

/// Get forwarded addresses from header map
///
/// Checks both `X-Forwarded-For` and `Forwarded` headers
pub fn forwarded_addrs(headers: &HeaderMap) -> Result<Vec<IpAddr>, StatusCode> {
    let xff = parse_xff(headers)?;
    let fwd = parse_forwarded(headers)?;
    if xff.is_empty() {
        Ok(fwd)
    } else if fwd.is_empty() {
        Ok(xff)
    } else {
        let mut fwd = fwd;
        fwd.extend(xff);
        Ok(fwd)
    }
}
