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
use std::sync::mpsc::{channel, Sender};
use std::thread;
use std::time::{Duration, Instant};
use crate::error::Error;
use crate::mere;
use crate::resource::{self, Resource};

/// Output directory to write JSON resources
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
    conn.execute("LISTEN r_node", &[])?;
    conn.execute("LISTEN sign_config", &[])?;
    conn.execute("LISTEN sign_detail", &[])?;
    conn.execute("LISTEN sign_message", &[])?;
    // Initialize all the resources
    for r in resource::ALL {
        fetch_resource(&conn, &tx, r)?;
    }
    notify_loop(&conn, tx)
}

/// Fetch a resource from database.
///
/// * `conn` The database connection.
/// * `tx` Channel sender for resource file names.
/// * `r` Resource to fetch.
fn fetch_resource(conn: &Connection, tx: &Sender<PathBuf>, r: &Resource)
    -> Result<(), Error>
{
    let t = Instant::now();
    let c = r.fetch(&conn, OUTPUT_DIR, tx)?;
    info!("{}: wrote {} rows in {:?}", r.name(), c, t.elapsed());
    Ok(())
}

/// Receive PostgreSQL notifications, and fetch needed resources.
///
/// * `conn` The database connection.
/// * `tx` Channel sender for resource file names.
fn notify_loop(conn: &Connection, tx: Sender<PathBuf>) -> Result<(), Error> {
    let nots = conn.notifications();
    let mut ns = HashSet::new();
    loop {
        // Collect until 300 ms have elapsed with no new notifications
        for n in nots.timeout_iter(Duration::from_millis(300)).iterator() {
            let n = n?;
            ns.insert((n.channel, n.payload));
        }
        for n in ns.drain() {
            if let Some(r) = lookup_resource(&n.0, &n.1) {
                fetch_resource(&conn, &tx, &r)?;
                // NOTE: when we need to fetch one, we also need the other
                if r == &resource::TPIMS_DYN_RES {
                    fetch_resource(&conn, &tx, &resource::TPIMS_ARCH_RES)?;
                }
            } else {
                warn!("unknown resource: ({}, {})", &n.0, &n.1);
            }
        }
    }
}

/// Lookup resource from PostgreSQL notification channel / payload
fn lookup_resource(chan: &str, payload: &str) -> Option<&'static Resource> {
    match (chan, payload) {
        ("camera", "video_loss") => None,
        ("dms", "expire_time") => None,
        ("dms", "msg_sched") => None,
        ("dms", "msg_current") => Some(&resource::DMS_MSG_RES),
        ("glyph", _) => Some(&resource::FONT_RES),
        ("parking_area", "time_stamp") => Some(&resource::TPIMS_DYN_RES),
        (_, _) => resource::lookup(chan),
    }
}
