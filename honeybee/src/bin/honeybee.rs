// honeybee.rs
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
#![forbid(unsafe_code)]

use honeybee::{
    notify_events, Database, Honey, Resource, Result, SegmentState,
};
use std::collections::HashSet;
use std::time::Duration;
use tokio::net::TcpListener;
use tokio_stream::StreamExt;

/// Main entry point
#[tokio::main]
async fn main() -> Result<()> {
    env_logger::builder().format_timestamp(None).init();
    let db = Database::new("tms").await?;
    let honey = Honey::new(&db);
    tokio::spawn(serve_routes(honey.clone()));
    let mut state = SegmentState::new();
    let mut events = HashSet::new();
    let stream = notify_events(&db).await?;
    let stream = stream.timeout(Duration::from_millis(300));
    tokio::pin!(stream);
    loop {
        match stream.next().await {
            None => break,
            Some(Ok(nm)) => {
                // hold event until timeout passes
                events.insert(nm);
            }
            Some(Err(_)) => {
                // timeout has passed, deliver all events
                let mut client = db.client().await?;
                for nm in events.drain() {
                    Resource::notify(&mut client, &mut state, &nm).await?;
                    let hon = honey.clone();
                    tokio::spawn(async move { hon.notify_sse(nm).await });
                }
            }
        }
    }
    log::warn!("Notification stream ended");
    Ok(())
}

/// Serve routes
async fn serve_routes(honey: Honey) -> Result<()> {
    let app = honey.route_root();
    let listener = TcpListener::bind("127.0.0.1:3737").await?;
    axum::serve(listener, app).await?;
    log::warn!("Axum serve ended");
    Ok(())
}
