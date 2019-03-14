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
use fallible_iterator::FallibleIterator;
use postgres::{Connection, TlsMode};
use std::collections::HashSet;
use std::path::PathBuf;
use std::sync::mpsc::{channel,Sender};
use std::thread;
use std::time::{Duration,Instant};
use crate::error::Error;
use crate::mere;
use crate::resource::{lookup_resource, ALL};

static OUTPUT_DIR: &str = "/var/www/html/iris/";

/// Start receiving notifications and fetching resources.
///
/// * `username` Name of user running process.
/// * `host` Host name and port to mirror fetched resources.
pub fn start(username: String, host: Option<String>) -> Result<(), Error> {
    // Format path for unix domain socket -- not worth using percent_encode
    let uds = format!("postgres://{:}@%2Frun%2Fpostgresql/tms", username);
    let (tx, rx) = channel();
    let db = thread::spawn(move || {
        if let Err(e) = db_thread(uds, tx) {
            error!("{:?}", e);
        }
    });
    mere::start(host, &username, rx);
    if let Err(e) = db.join() {
        error!("db_thread panicked: {:?}", e);
    }
    Ok(())
}

/// Connect to database and fetch resources as notifications are received.
///
/// * `uds` Unix domain socket for database.
/// * `tx` Channel sender for resource file names.
fn db_thread(uds: String, tx: Sender<PathBuf>) -> Result<(), Error> {
    let conn = Connection::connect(uds, TlsMode::None)?;
    // The postgresql crate sets the session time zone to UTC.
    // We need to set it back to LOCAL time zone, so that row_to_json
    // can format properly (for incidents, etc).
    conn.execute("SET TIME ZONE 'US/Central'", &[])?;
    // Listen for notifications on all channels we need to monitor
    conn.execute("LISTEN camera", &[])?;
    conn.execute("LISTEN dms", &[])?;
    conn.execute("LISTEN font", &[])?;
    conn.execute("LISTEN glyph", &[])?;
    conn.execute("LISTEN graphic", &[])?;
    conn.execute("LISTEN incident", &[])?;
    conn.execute("LISTEN parking_area", &[])?;
    conn.execute("LISTEN sign_config", &[])?;
    conn.execute("LISTEN sign_detail", &[])?;
    conn.execute("LISTEN sign_message", &[])?;
    // FIXME: remove this after DB has been updated
    conn.execute("LISTEN tms", &[])?;
    // Initialize all the resources
    for r in ALL {
        fetch_resource_timed(&conn, &tx, r)?;
    }
    notify_loop(&conn, tx)
}

/// Fetch a named resource from database and print timing information.
///
/// * `conn` The database connection.
/// * `tx` Channel sender for resource file names.
/// * `n` Resource name.
fn fetch_resource_timed(conn: &Connection, tx: &Sender<PathBuf>, n: &str)
    -> Result<(), Error>
{
    let t = Instant::now();
    if let Some(c) = fetch_resource(&conn, tx, &n)? {
        info!("{}: wrote {} rows in {:?}", &n, c, t.elapsed());
    } else {
        warn!("{}: unknown resource", &n);
    }
    Ok(())
}

/// Fetch a named resource from database.
///
/// * `conn` The database connection.
/// * `tx` Channel sender for resource file names.
/// * `n` Resource name.
fn fetch_resource(conn: &Connection, tx: &Sender<PathBuf>, n: &str)
    -> Result<Option<u32>, Error>
{
    if let Some(r) = lookup_resource(n) {
        Ok(Some(r.fetch(&conn, OUTPUT_DIR, tx)?))
    } else {
        Ok(None)
    }
}

/// Receive PostgreSQL notifications, and fetch needed resources.
///
/// * `conn` The database connection.
/// * `tx` Channel sender for resource file names.
fn notify_loop(conn: &Connection, tx: Sender<PathBuf>) -> Result<(), Error> {
    let nots = conn.notifications();
    let mut ns = HashSet::new();
    loop {
        for n in nots.timeout_iter(Duration::from_millis(300)).iterator() {
            let n = n?;
            ns.insert((n.channel, n.payload));
        }
        for n in ns.drain() {
            let r = get_resource_name(&n);
            fetch_resource_timed(&conn, &tx, &r)?;
            // NOTE: when we need to fetch one, we also need the other
            if r == "parking_area_dynamic" {
                fetch_resource_timed(&conn, &tx, &"parking_area_archive")?;
            }
        }
    }
}

fn get_resource_name(n: &(String, String)) -> &str {
    let (chan, payload) = (&n.0, &n.1);
    // FIXME: remove this after DB has been updated
    if chan == "tms" {
        &payload
    } else {
        get_resource_name_new(&chan, &payload)
    }
}

fn get_resource_name_new<'a>(channel: &'a str, payload: &'a str) -> &'a str {
    // FIXME: combine this with lookup_resource
    match (channel, payload) {
        ("dms", "msg_current") => &"dms_message",
        ("parking_area", "time_stamp") => &"parking_area_dynamic",
        ("glyph", _) => &"font",
        (_, _) => channel,
    }
}
