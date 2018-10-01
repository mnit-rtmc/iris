/*
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
use failure::Error;
use fallible_iterator::FallibleIterator;
use postgres::{Connection, TlsMode};
use std::collections::HashSet;
use std::time::{Duration,Instant};
use resource;

//static OUTPUT_DIR: &str = "/var/www/html/iris/";
static OUTPUT_DIR: &str = "iris";

pub fn start(uds: String) -> Result<(), Error> {
    let conn = Connection::connect(uds, TlsMode::None)?;
    // The postgresql crate sets the session time zone to UTC.
    // We need to set it back to LOCAL time zone, so that row_to_json
    // can format properly (for incidents, etc).
    conn.execute("SET TIME ZONE 'US/Central'", &[])?;
    conn.execute("LISTEN tms", &[])?;
    // Initialize all the resources
    for r in ["camera_pub", "dms_pub", "dms_message", "incident", "sign_config",
              "parking_area_static", "parking_area_dynamic", "font"].iter()
    {
        fetch_resource_timed(&conn, r)?;
    }
    notify_loop(&conn)
}

fn fetch_resource_timed(conn: &Connection, n: &str)
    -> Result<(), Error>
{
    let s = Instant::now();
    if let Some(c) = fetch_resource_file(&conn, &n)? {
        println!("{}: wrote {} rows in {:?}", &n, c, s.elapsed());
    } else {
        println!("{}: unknown resource", &n);
    }
    Ok(())
}

fn fetch_resource_file(conn: &Connection, n: &str)
    -> Result<Option<u32>, Error>
{
    if let Some(r) = resource::lookup_resource(n) {
        Ok(Some(r.fetch_file(&conn, OUTPUT_DIR)?))
    } else {
        Ok(None)
    }
}

fn notify_loop(conn: &Connection) -> Result<(), Error> {
    let nots = conn.notifications();
    let mut s = HashSet::new();
    loop {
        for n in nots.timeout_iter(Duration::from_millis(300)).iterator() {
            let n = n?;
            s.insert(n.payload);
        }
        for n in s.drain() {
            fetch_resource_timed(&conn, &n)?;
        }
    }
}
