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

use honeybee::{listener, router, Database, Resource, Result, SegmentState};
use std::collections::HashSet;
use std::time::Duration;
use tokio::net::TcpListener;
use tokio_stream::StreamExt;

/// Main entry point
#[tokio::main]
async fn main() -> Result<()> {
    env_logger::builder().format_timestamp(None).init();
    let db = Database::new("tms").await?;
    tokio::spawn(serve_routes(db.clone()));
    let mut state = SegmentState::new();
    let mut events = HashSet::new();
    let stream = listener::notify_events(&db).await?;
    let stream = stream.timeout(Duration::from_millis(250));
    tokio::pin!(stream);
    loop {
        match stream.next().await {
            None => break,
            Some(Ok(ne)) => {
                // hold event until timeout passes
                events.insert(ne);
            }
            Some(Err(_)) => {
                // timeout has passed, deliver all events
                let mut client = db.client().await?;
                for ne in events.drain() {
                    Resource::notify(&mut client, &mut state, ne).await?;
                }
            }
        }
    }
    log::warn!("Notification stream ended");
    Ok(())
}

/// Serve routes
async fn serve_routes(db: Database) -> Result<()> {
    let app = router::build(db).await?;
    let listener = TcpListener::bind("127.0.0.1:3737").await?;
    axum::serve(listener, app).await?;
    log::warn!("Axum serve ended");
    Ok(())
}
