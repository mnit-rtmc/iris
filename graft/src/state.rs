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
use postgres::row::Row;
use postgres::NoTls;
use r2d2::Pool;
use r2d2_postgres::PostgresConnectionManager;
use serde::{Deserialize, Serialize};

/// Role
#[derive(Clone, Debug, Deserialize, Serialize)]
pub struct Role {
    pub name: String,
    pub enabled: bool,
}

/// Permission
#[derive(Clone, Debug, Deserialize, Serialize)]
pub struct Permission {
    pub id: i32,
    pub role: String,
    pub resource_n: String,
    pub batch: Option<String>,
    pub access_n: i32,
}

/// User
#[derive(Clone, Debug, Deserialize, Serialize)]
pub struct User {
    pub name: String,
    pub full_name: String,
    pub role: String,
    pub enabled: bool,
}

/// Db connection pool
type PostgresPool = Pool<PostgresConnectionManager<NoTls>>;

/// Application state for postgres
#[derive(Clone)]
pub struct State {
    /// Db connection pool
    pool: PostgresPool,
}

impl Permission {
    fn from_row(row: Row) -> Self {
        Permission {
            id: row.get(0),
            role: row.get(1),
            resource_n: row.get(2),
            batch: row.get(3),
            access_n: row.get(4),
        }
    }
}

/// Make postgres pool
fn make_pool() -> Result<PostgresPool> {
    let username = whoami::username();
    // Format path for unix domain socket -- not worth using percent_encode
    let uds = format!("postgres://{username}@%2Frun%2Fpostgresql/tms");
    let config = uds.parse()?;
    let manager = PostgresConnectionManager::new(config, NoTls);
    Ok(r2d2::Pool::new(manager)?)
}

/// Query one permission
const QUERY_PERM: &str = "\
SELECT id, role, resource_n, batch, access_n \
FROM iris.permission \
WHERE id = $1";

/// Query access permissions for a user
const QUERY_ACCESS: &str = "\
SELECT p.id, p.role, p.resource_n, p.batch, p.access_n \
FROM iris.i_user u \
JOIN iris.role r ON u.role = r.name \
JOIN iris.permission p ON p.role = r.name \
WHERE u.name = $1 AND u.enabled = true AND r.enabled = true;";

impl State {
    /// Create new postgres application state
    pub fn new() -> Result<Self> {
        let pool = make_pool()?;
        Ok(State { pool })
    }

    /// Get permission by ID
    pub fn permission(&self, id: i32) -> Result<Permission> {
        let mut conn = self.pool.get()?;
        let row = conn.query_one(QUERY_PERM, &[&id])?;
        Ok(Permission::from_row(row))
    }

    /// Get access permissions for a user
    pub fn access(&self, user: &str) -> Result<Vec<Permission>> {
        let mut perms = vec![];
        let mut conn = self.pool.get()?;
        for row in conn.query(QUERY_ACCESS, &[&user])? {
            perms.push(Permission::from_row(row));
        }
        Ok(perms)
    }
}
