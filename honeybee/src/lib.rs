// lib.rs
//
// Copyright (C) 2018-2025  Minnesota Department of Transportation
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
#![forbid(unsafe_code)]
#![allow(clippy::match_like_matches_macro)]

mod access;
mod cred;
mod domain;
mod error;
mod event;
mod files;
mod honey;
mod listener;
mod permission;
mod query;
mod resource;
mod segments;
mod signmsg;
pub mod sonar;
mod tls;
mod xff;

pub use error::{Error, Result};
pub use honey::Honey;
pub use listener::notify_events;
pub use resource::Resource;
pub use segments::SegmentState;
