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
extern crate actix_web;
extern crate chrono;
extern crate failure;
extern crate postgres;
extern crate serde;
extern crate serde_json;
#[macro_use] extern crate serde_derive;
extern crate users;

mod iris_req;

use actix_web::{http::Method, server, App};
use users::get_current_username;

fn main() {
    let username = get_current_username().expect("User name lookup error");
    // Format path for unix domain socket
    let uds = format!("postgres://{:}@%2Frun%2Fpostgresql/tms", username);
    server::new(move || {
        let uds = uds.clone();
        App::new().resource("/iris/{v}", move |r| {
            r.method(Method::GET).h(iris_req::Handler::new(uds))
        })
    }).bind("127.0.0.1:8088").expect("Can not bind to 127.0.0.1:8088")
      .run();
}
