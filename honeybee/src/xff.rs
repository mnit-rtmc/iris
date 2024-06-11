// xff.rs
//
// Copyright (C) 2024  Minnesota Department of Transportation
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
    async_trait,
    extract::FromRequestParts,
    http::{request::Parts, HeaderMap, StatusCode},
};
use std::convert::Infallible;
use std::net::IpAddr;

/// X-Forwarded-For header addresses
#[derive(Debug)]
pub struct XForwardedFor(pub Vec<IpAddr>);

#[async_trait]
impl<S> FromRequestParts<S> for XForwardedFor
where
    S: Sync,
{
    type Rejection = (StatusCode, Infallible);

    async fn from_request_parts(
        parts: &mut Parts,
        _state: &S,
    ) -> Result<Self, Self::Rejection> {
        // FIXME: if XFF header is unparseable, reject with StatusCode
        Ok(Self(addrs_from_headers(&parts.headers)))
    }
}

/// Parse addresses in XFF headers
fn addrs_from_headers(headers: &HeaderMap) -> Vec<IpAddr> {
    headers
        .get_all("X-Forwarded-For")
        .iter()
        .filter_map(|h| h.to_str().ok())
        .flat_map(|h| {
            h.split(',').filter_map(|a| a.trim().parse::<IpAddr>().ok())
        })
        .collect()
}
