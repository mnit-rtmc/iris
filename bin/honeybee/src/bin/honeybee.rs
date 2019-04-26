/*
 * Copyright (C) 2018-2019  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
#![forbid(unsafe_code)]

#[macro_use]
extern crate log;

use std::env;
use honeybee::fetcher;
use whoami;

fn main() {
    env_logger::Builder::from_default_env()
                        .default_format_timestamp(false)
                        .init();
    let host = env::args().nth(1);
    let username = whoami::username();
    if let Err(e) = fetcher::start(&username, host) {
        error!("fetcher: {:?}", e);
    }
}
