// state.rs
//
// Copyright (C) 2021-2022  Minnesota Department of Transportation
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
use crate::sonar::Result;
use bb8::Pool;
use bb8_postgres::tokio_postgres::NoTls;
use bb8_postgres::PostgresConnectionManager;
use flume::{Receiver, Sender};
use serde::{Deserialize, Serialize};
use tokio::runtime::{Handle, Runtime};

/// Permission
#[derive(Clone, Debug, Deserialize, Serialize)]
pub struct Permission {
    pub id: i32,
    pub role: String,
    pub resource_n: String,
    pub batch: Option<String>,
    pub access_n: i32,
}

/// Application state for Tokio
#[derive(Clone)]
pub struct State {
    /// Tokio runtime handle
    _handle: Handle,
    /// Flume sender
    tx: Sender<i32>,
    /// Flume receiver
    rx: Receiver<Permission>,
}

/// Make postgres connection pool
async fn make_pool() -> Result<Pool<PostgresConnectionManager<NoTls>>> {
    let username = whoami::username();
    // Format path for unix domain socket -- not worth using percent_encode
    let uds = format!("postgres://{username}@%2Frun%2Fpostgresql/tms");
    let config = uds.parse()?;
    let manager = PostgresConnectionManager::new(config, NoTls);
    Ok(Pool::builder().build(manager).await?)
}

/// Runner to handle requests
async fn runner(prx: Receiver<i32>, ptx: Sender<Permission>) -> Result<()> {
    let pool = make_pool().await?;
    loop {
        let id = prx.recv_async().await?;
        let perm = permission(&pool, id).await?;
        ptx.send_async(perm).await.unwrap();
    }
}

/// Query one permission
const QUERY_PERM: &str = "\
SELECT id, role, resource_n, batch, access_n \
FROM iris.permission \
WHERE id = ($1)";

/// Get permission by ID
async fn permission(
    pool: &Pool<PostgresConnectionManager<NoTls>>,
    id: i32,
) -> Result<Permission> {
    let conn = pool.get().await?;
    let row = conn.query_one(QUERY_PERM, &[&id]).await?;
    Ok(Permission {
        id: row.get(0),
        role: row.get(1),
        resource_n: row.get(2),
        batch: row.get(3),
        access_n: row.get(4),
    })
}

impl State {
    /// Create a new Tokio application state
    pub fn new() -> Result<Self> {
        let runtime = Runtime::new()?;
        let (tx, prx) = flume::bounded(8);
        let (ptx, rx) = flume::unbounded();
        runtime.spawn(runner(prx, ptx));
        let _handle = runtime.handle().clone();
        // Can't drop runtime, but can't clone it either; just forget about it
        std::mem::forget(runtime);
        Ok(State { _handle, tx, rx })
    }

    /// Get permission by ID
    pub async fn permission(&self, id: i32) -> Result<Permission> {
        self.tx.send_async(id).await.unwrap();
        Ok(self.rx.recv_async().await?)
    }
}
