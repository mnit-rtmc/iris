// fetcher.rs
//
// Copyright (C) 2018-2019  Minnesota Department of Transportation
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
use crate::error::Result;
use crate::resource::{self, Resource};
use fallible_iterator::FallibleIterator;
use postgres::{Connection, TlsMode};
use std::collections::HashSet;
use std::env;
use std::time::{Duration, Instant};

/// Output directory to write JSON resources
static OUTPUT_DIR: &str = "/var/www/html/iris/";

/// Start receiving notifications and fetching resources.
///
/// * `username` Name of user running process.
pub fn start(username: &str) -> Result<()> {
    let conn = create_connection(username)?;
    listen_notifications(&conn)?;
    // Initialize all the resources
    for r in resource::ALL {
        fetch_resource(&conn, r)?;
    }
    notify_loop(&conn)
}

/// Create database connection
///
/// * `username` Name of user running process.
fn create_connection(username: &str) -> Result<Connection> {
    // Format path for unix domain socket -- not worth using percent_encode
    let uds = format!("postgres://{:}@%2Frun%2Fpostgresql/tms", username);
    let conn = Connection::connect(uds, TlsMode::None)?;
    // The postgres crate sets the session time zone to UTC.
    // We need to set it back to LOCAL time zone, so that row_to_json
    // can format properly (for incidents, etc).  Unfortunately,
    // the LOCAL and DEFAULT zones are also reset to UTC, so the PGTZ
    // environment variable must be used for this purpose.
    if let Some(tz) = time_zone() {
        conn.execute(&format!("SET TIME ZONE '{}'", tz), &[])?;
    }
    Ok(conn)
}

/// Postgres time zone environment variable name
const PGTZ: &'static str = "PGTZ";

/// Get time zone name for database connection
fn time_zone() -> Option<String> {
    match env::var(PGTZ) {
        Ok(tz) => Some(tz),
        Err(env::VarError::NotPresent) => None,
        Err(env::VarError::NotUnicode(_)) => {
            error!("{} env var is not unicode!", PGTZ);
            None
        }
    }
}

/// Listen for notifications on all channels we need to monitor.
///
/// * `conn` Database connection.
fn listen_notifications(conn: &Connection) -> Result<()> {
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
    conn.execute("LISTEN system_attribute", &[])?;
    Ok(())
}

/// Fetch a resource from database.
///
/// * `conn` The database connection.
/// * `r` Resource to fetch.
fn fetch_resource(conn: &Connection, r: &Resource) -> Result<()> {
    let t = Instant::now();
    let c = r.fetch(&conn, OUTPUT_DIR)?;
    info!("{}: wrote {} rows in {:?}", r.name(), c, t.elapsed());
    Ok(())
}

/// Receive PostgreSQL notifications, and fetch needed resources.
///
/// * `conn` The database connection.
fn notify_loop(conn: &Connection) -> Result<()> {
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
                fetch_resource(&conn, &r)?;
                // NOTE: when we need to fetch one, we also need the other
                if r == &resource::TPIMS_DYN_RES {
                    fetch_resource(&conn, &resource::TPIMS_ARCH_RES)?;
                }
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
        ("system_attribute", _) => Some(&resource::DMS_ATTRIBUTE_RES),
        (_, _) => {
            if let Some(r) = resource::lookup(chan) {
                Some(r)
            } else {
                warn!("unknown resource: ({}, {})", &chan, &payload);
                None
            }
        },
    }
}
