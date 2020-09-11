// lib.rs
//
// Copyright (C) 2018-2020  Minnesota Department of Transportation
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

#[macro_use]
extern crate log;
#[macro_use]
extern crate serde_derive;

/// Result type
pub type Result<T> = std::result::Result<T, Box<dyn std::error::Error>>;

pub mod fetcher;
pub mod geo;
mod resource;
mod segments;
mod signmsg;
