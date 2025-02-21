// xff.rs
//
// Copyright (C) 2024-2025  Minnesota Department of Transportation
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
use axum::{
    extract::FromRequestParts,
    http::{HeaderMap, StatusCode, request::Parts},
};
use std::net::IpAddr;

/// X-Forwarded-For header addresses
#[derive(Debug)]
pub struct XForwardedFor(pub Vec<IpAddr>);

impl<S> FromRequestParts<S> for XForwardedFor
where
    S: Sync,
{
    type Rejection = StatusCode;

    async fn from_request_parts(
        parts: &mut Parts,
        _state: &S,
    ) -> Result<Self, Self::Rejection> {
        Ok(Self(addrs_from_headers(&parts.headers)?))
    }
}

/// Parse addresses in XFF headers
fn addrs_from_headers(headers: &HeaderMap) -> Result<Vec<IpAddr>, StatusCode> {
    let mut ips = Vec::with_capacity(8);
    for header in headers.get_all("X-Forwarded-For") {
        let header = header.to_str().map_err(|_e| StatusCode::FORBIDDEN)?;
        for a in header.split(',') {
            let ip = a
                .trim()
                .parse::<IpAddr>()
                .map_err(|_e| StatusCode::FORBIDDEN)?;
            ips.push(ip);
        }
    }
    Ok(ips)
}
