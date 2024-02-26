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
use crate::database::Database;
use crate::error::Result;
use crate::resource;
use crate::segments::{receive_nodes, SegMsg};
use std::collections::HashSet;
use std::sync::mpsc::Sender;
use std::time::Duration;
use tokio_postgres::Client;

/// Start receiving notifications and fetching resources.
pub async fn start() -> Result<()> {
    let (sender, receiver) = std::sync::mpsc::channel();
    tokio::spawn(async move { receive_nodes(receiver).await });
    let db = Database::new("tms").await?;
    let mut client = db.connection().await?;
    resource::listen_all(&mut client).await?;
    resource::query_all(&mut client, &sender).await?;
    notify_loop(&mut client, sender).await
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
