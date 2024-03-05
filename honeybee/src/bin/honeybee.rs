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

use futures::stream::StreamExt;
use honeybee::{listener, Database, Resource, Result, SegmentState};

/// Main entry point
#[tokio::main]
async fn main() -> Result<()> {
    env_logger::builder().format_timestamp(None).init();
    let db = Database::new("tms").await?;
    let mut stream = listener::notify_events(&db).await?;
    let mut state = SegmentState::new();
    while let Some(ne) = stream.next().await {
        // FIXME: add 300 ms delay...
        let mut client = db.client().await?;
        Resource::notify(&mut client, &mut state, ne).await?;
    }
    Ok(())
}
