// fetcher.rs
//
// Copyright (C) 2018-2024  Minnesota Department of Transportation
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
use crate::resource;
use crate::segments::{receive_nodes, SegMsg};
use postgres::fallible_iterator::FallibleIterator;
use postgres::{Client, NoTls};
use std::collections::HashSet;
use std::env;
use std::sync::mpsc::Sender;
use std::time::Duration;

/// Start receiving notifications and fetching resources.
pub async fn start() -> Result<()> {
    let (sender, receiver) = std::sync::mpsc::channel();
    tokio::spawn(async move { receive_nodes(receiver).await });
    let mut client = create_client("tms")?;
    resource::listen_all(&mut client)?;
    resource::query_all(&mut client, &sender).await?;
    notify_loop(&mut client, sender).await
}

/// Create database client
pub fn create_client(db: &str) -> Result<Client> {
    let username = whoami::username();
    // Format path for unix domain socket -- not worth using percent_encode
    let uds = format!("postgres://{username}@%2Frun%2Fpostgresql/{db}");
    let mut client = Client::connect(&uds, NoTls)?;
    // The postgres crate sets the session time zone to UTC.
    // We need to set it back to LOCAL time zone, so that row_to_json
    // can format properly (for incidents, etc).  Unfortunately,
    // the LOCAL and DEFAULT zones are also reset to UTC, so the PGTZ
    // environment variable must be used for this purpose.
    if let Some(tz) = time_zone() {
        let time_zone = format!("SET TIME ZONE '{tz}'");
        client.execute(&time_zone[..], &[])?;
    }
    Ok(client)
}

/// Postgres time zone environment variable name
const PGTZ: &str = "PGTZ";

/// Get time zone name for database client
fn time_zone() -> Option<String> {
    match env::var(PGTZ) {
        Ok(tz) => Some(tz),
        Err(env::VarError::NotPresent) => None,
        Err(env::VarError::NotUnicode(_)) => {
            log::error!("{} env var is not unicode!", PGTZ);
            None
        }
    }
}

/// Receive PostgreSQL notifications, and fetch needed resources.
///
/// * `client` The database connection.
/// * `sender` Sender for segment messages.
async fn notify_loop(
    client: &mut Client,
    sender: Sender<SegMsg>,
) -> Result<()> {
    loop {
        let mut nots = pending_notifications(client)?;
        for (channel, payload) in nots.drain() {
            log::debug!("notify on {channel}");
            resource::notify(client, &channel, &payload, &sender).await?;
        }
    }
}

/// Get pending notifications from PostgreSQL.
///
/// Collect until 300 ms have elapsed with no new notifications
fn pending_notifications(
    client: &mut Client,
) -> Result<HashSet<(String, String)>> {
    let mut ns = HashSet::new();
    let mut nots = client.notifications();
    for n in nots.timeout_iter(Duration::from_millis(300)).iterator() {
        let n = n?;
        // Discard notification if we're not listening for it
        if resource::is_listening(n.channel()) {
            ns.insert((n.channel().to_string(), n.payload().to_string()));
        }
    }
    Ok(ns)
}
