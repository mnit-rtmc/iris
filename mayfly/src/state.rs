// Copyright (c) 2026  Minnesota Department of Transportation
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
use axum::Router;
use axum::body::Body;
use axum::extract::{Request, State};
use axum::middleware::Next;
use axum::response::Response;
use http::StatusCode;
use http::header::HeaderMap;
use shelter::{Domain, forwarded_addrs};
use std::net::IpAddr;
use std::ops::{Deref, DerefMut};
use std::path::PathBuf;
use std::sync::{Arc, Mutex, OnceLock};
use std::thread::available_parallelism;

/// Limit of in-progress requests (restricted domains)
const IN_PROGRESS_LIMIT: usize = 128;

/// Mayfly application state
#[derive(Clone, Default)]
pub struct MayState {
    /// Network domains
    domains: OnceLock<Vec<Domain>>,
    /// Count of in-progress requests (restricted)
    in_progress: Arc<Mutex<usize>>,
    /// Limit of in-progress requests
    in_progress_limit: OnceLock<usize>,
}

/// Mayfly router
pub type MayRouter = Router<MayState>;

impl MayState {
    /// Create mayfly app state
    pub fn new() -> Self {
        MayState::default()
    }

    /// Load domain configurations
    pub async fn load_domains(&self) -> Result<()> {
        let mut path = PathBuf::new();
        path.push("/var/lib/iris/web/api/domain");
        let buf = tokio::fs::read(path).await?;
        let domains: Vec<Domain> = serde_json::from_slice(&buf)?;
        self.domains
            .set(domains)
            .map_err(|_e| Error::AlreadyInitialized())?;
        Ok(())
    }

    /// Check domains before processing request
    fn precheck(&self, addrs: &[IpAddr]) -> Option<StatusCode> {
        let Some(domains) = self.domains.get() else {
            log::error!("Domains not initialized");
            return Some(StatusCode::INTERNAL_SERVER_ERROR);
        };
        for addr in addrs {
            if !Domain::contains_any_enabled(domains, *addr) {
                log::warn!("Invalid domain for IP: {addr}");
                return Some(StatusCode::FORBIDDEN);
            }
        }
        // NOTE: we can only trust the last XFF address (from our proxy)
        let restricted = match addrs.last() {
            Some(addr) => match Domain::best_match(domains, *addr) {
                Some(domain) => domain.is_restricted(),
                None => true,
            },
            None => true,
        };
        if restricted {
            match self.get_in_progress() {
                Some(in_progress) => {
                    if in_progress > self.get_in_progress_limit() {
                        if let Some(addr) = addrs.last() {
                            log::warn!(
                                "Too many requests: {addr} ({in_progress})"
                            );
                        }
                        return Some(StatusCode::TOO_MANY_REQUESTS);
                    }
                }
                None => return Some(StatusCode::INTERNAL_SERVER_ERROR),
            }
        }
        None
    }

    /// Get in-progress limit based on available CPUs
    fn get_in_progress_limit(&self) -> usize {
        *self.in_progress_limit.get_or_init(|| {
            let para = match available_parallelism() {
                Ok(p) => p.get(),
                _ => 1,
            };
            (para * 64).min(IN_PROGRESS_LIMIT)
        })
    }

    /// Get in-progress count
    fn get_in_progress(&self) -> Option<usize> {
        match self.in_progress.lock() {
            Ok(in_progress) => Some(*in_progress.deref()),
            Err(_e) => {
                log::error!("Poisoned in_progress");
                None
            }
        }
    }

    /// Increment in-progress count
    fn increment_in_progress(&self) {
        if let Ok(mut in_progress) = self.in_progress.lock() {
            *in_progress.deref_mut() += 1;
        }
    }

    /// Decrement in-progress count
    fn decrement_in_progress(&self) {
        if let Ok(mut in_progress) = self.in_progress.lock() {
            *in_progress.deref_mut() -= 1;
        }
    }
}

/// Middleware to limit requests
pub async fn limiter_middleware(
    headers: HeaderMap,
    State(state): State<MayState>,
    request: Request,
    next: Next,
) -> Response {
    // NOTE: only forwarded addresses, ConnectInfo is the proxy server
    let addrs = match forwarded_addrs(&headers) {
        Ok(addrs) => addrs,
        Err(sc) => return err_response(sc),
    };
    if let Some(sc) = state.precheck(&addrs) {
        return err_response(sc);
    }
    state.increment_in_progress();
    let response = next.run(request).await;
    state.decrement_in_progress();
    response
}

/// Build error response
fn err_response(sc: StatusCode) -> Response {
    Response::builder().status(sc).body(Body::empty()).unwrap()
}
