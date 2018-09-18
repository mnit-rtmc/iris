/*
 * honeybee -- Web service for IRIS
 * Copyright (C) 2018  Minnesota Department of Transportation
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
extern crate honeybee;
extern crate users;

use honeybee::req_server;
use users::get_current_username;

fn main() {
    let username = get_current_username().expect("User name lookup error");
    // Format path for unix domain socket
    let uds = format!("postgres://{:}@%2Frun%2Fpostgresql/tms", username);
    req_server::start(uds);
}
