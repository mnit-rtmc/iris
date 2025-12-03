// database.rs
//
// Copyright (C) 2021-2025  Minnesota Department of Transportation
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
use crate::error;
use bb8::{CustomizeConnection, Pool, PooledConnection};
use bb8_postgres::PostgresConnectionManager;
use std::env;
use std::pin::Pin;
use tokio_postgres::tls::NoTlsStream;
use tokio_postgres::{Client, Config, Connection, NoTls, Socket};

/// Database state
#[derive(Clone)]
pub struct Database {
    /// Connection configuration
    config: Config,
    /// Db connection pool
    pool: Pool<Manager>,
}

/// Make database configuration
fn make_config(db: &str) -> error::Result<Config> {
    let username = whoami::username();
    // Format path for unix domain socket -- not worth using percent_encode
    let uds = format!("postgres://{username}@%2Frun%2Fpostgresql/{db}");
    Ok(uds.parse()?)
}

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

/// The postgres crate sets the session time zone to UTC.
/// We need to set it back to LOCAL time zone, so that row_to_json
/// can format properly (for incidents, etc).  Unfortunately,
/// the LOCAL and DEFAULT zones are also reset to UTC, so the PGTZ
/// environment variable must be used for this purpose.
fn sql_init() -> Option<String> {
    time_zone().map(|tz| format!("SET TIME ZONE '{tz}'"))
}

/// Postgres connection customizer to fix time zone
#[derive(Debug)]
struct TimeZoneCorrecter;

impl CustomizeConnection<Client, tokio_postgres::Error> for TimeZoneCorrecter {
    fn on_acquire<'a>(
        &'a self,
        client: &'a mut Client,
    ) -> Pin<
        Box<dyn Future<Output = Result<(), tokio_postgres::Error>> + Send + 'a>,
    > {
        Box::pin(async move {
            if let Some(sql) = &sql_init() {
                client.execute(sql, &[]).await?;
            }
            Ok(())
        })
    }
}

/// Postgres connection manager
type Manager = PostgresConnectionManager<NoTls>;

/// Make postgres pool for connecting to a Db
async fn make_pool(config: Config) -> error::Result<Pool<Manager>> {
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
    pub async fn new(db: &str) -> error::Result<Self> {
        let config = make_config(db)?;
        let pool = make_pool(config.clone()).await?;
        Ok(Database { config, pool })
    }

    /// Get database client
    pub async fn client(&self) -> error::Result<PooledConnection<'_, Manager>> {
        Ok(self.pool.get().await?)
    }

    /// Get a dedicated client/connection, not managed by the pool
    pub async fn dedicated_client(
        &self,
    ) -> error::Result<(Client, Connection<Socket, NoTlsStream>)> {
        Ok(self.config.connect(NoTls).await?)
    }
}
