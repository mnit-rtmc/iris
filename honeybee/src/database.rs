// database.rs
//
// Copyright (C) 2021-2024  Minnesota Department of Transportation
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
use crate::error::{Error, Result};
use async_trait::async_trait;
use bb8::{CustomizeConnection, Pool, PooledConnection};
use bb8_postgres::PostgresConnectionManager;
use std::env;
use tokio_postgres::{Client, NoTls};

/// Get time zone from environment variable
fn time_zone() -> Option<String> {
    match env::var("PGTZ") {
        Ok(tz) => Some(tz),
        Err(env::VarError::NotPresent) => None,
        Err(env::VarError::NotUnicode(_)) => {
            log::error!("PGTZ env var is not unicode!");
            None
        }
    }
}

/// Postgres connection customizer to fix time zone
#[derive(Debug)]
struct TimeZoneCorrecter;

#[async_trait]
impl CustomizeConnection<Client, tokio_postgres::Error> for TimeZoneCorrecter {
    async fn on_acquire(
        &self,
        client: &mut Client,
    ) -> std::result::Result<(), tokio_postgres::Error> {
        // The postgres crate sets the session time zone to UTC.
        // We need to set it back to LOCAL time zone, so that row_to_json
        // can format properly (for incidents, etc).  Unfortunately,
        // the LOCAL and DEFAULT zones are also reset to UTC, so the PGTZ
        // environment variable must be used for this purpose.
        if let Some(tz) = time_zone() {
            let time_zone = format!("SET TIME ZONE '{tz}'");
            client.execute(&time_zone[..], &[]).await?;
        }
        Ok(())
    }
}

/// Postgres connection manager
type Manager = PostgresConnectionManager<NoTls>;

/// Database state
#[derive(Clone)]
pub struct Database {
    /// Db connection pool
    pool: Pool<Manager>,
}

/// Make postgres pool for connecting to a Db
async fn make_pool(db: &str) -> Result<Pool<Manager>> {
    let username = whoami::username();
    // Format path for unix domain socket -- not worth using percent_encode
    let uds = format!("postgres://{username}@%2Frun%2Fpostgresql/{db}");
    let config = uds.parse()?;
    let manager = PostgresConnectionManager::new(config, NoTls);
    let correcter = TimeZoneCorrecter;
    let pool = Pool::builder()
        .connection_customizer(Box::new(correcter))
        .build(manager)
        .await?;
    Ok(pool)
}

impl Database {
    /// Create database state
    pub async fn new(db: &str) -> Result<Self> {
        let pool = make_pool(db).await?;
        Ok(Database { pool })
    }

    /// Get database connection
    pub async fn connection<M>(&self) -> Result<PooledConnection<Manager>> {
        Ok(self.pool.get().await?)
    }
}
