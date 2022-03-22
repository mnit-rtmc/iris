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
use crate::sonar::{Result, SonarError};
use postgres::row::Row;
use postgres::types::ToSql;
use postgres::NoTls;
use r2d2::Pool;
use r2d2_postgres::PostgresConnectionManager;
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;

/// Permission
#[derive(Clone, Debug, Deserialize, Serialize)]
pub struct Permission {
    pub id: i32,
    pub role: String,
    pub resource_n: String,
    pub batch: Option<String>,
    pub access_n: i32,
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

/// Create one permission
const INSERT_PERM: &str = "\
INSERT INTO iris.permission (role, resource_n, access_n) \
VALUES ($1, $2, $3)";

/// Update one permission
const UPDATE_PERM: &str = "\
UPDATE iris.permission \
SET (role, resource_n, batch, access_n) = ($2, $3, $4, $5) \
WHERE id = $1";

/// Delete one permission
const DELETE_PERM: &str = "\
DELETE \
FROM iris.permission \
WHERE id = $1";

/// Query access permissions for a user
const QUERY_ACCESS: &str = "\
SELECT p.id, p.role, p.resource_n, p.batch, p.access_n \
FROM iris.i_user u \
JOIN iris.role r ON u.role = r.name \
JOIN iris.permission p ON p.role = r.name \
WHERE u.enabled = true AND r.enabled = true \
AND u.name = $1 \
ORDER BY p.resource_n, p.batch";

/// Query permissions for a user / resource
const QUERY_PERMISSIONS: &str = "\
SELECT p.id, p.role, p.resource_n, p.batch, p.access_n \
FROM iris.i_user u \
JOIN iris.role r ON u.role = r.name \
JOIN iris.permission p ON p.role = r.name \
WHERE u.enabled = true AND r.enabled = true \
AND u.name = $1 AND resource_n = $2";

impl State {
    /// Create new postgres application state
    pub fn new() -> Result<Self> {
        let pool = make_pool()?;
        Ok(State { pool })
    }

    /// Get permission by ID
    pub fn permission(&self, id: i32) -> Result<Permission> {
        let mut client = self.pool.get()?;
        let row = client.query_one(QUERY_PERM, &[&id])?;
        Ok(Permission::from_row(row))
    }

    /// Create a permission
    pub fn permission_post(&self, role: &str, resource_n: &str) -> Result<()> {
        let access_n = 1; // View is a good default
        let mut client = self.pool.get()?;
        let rows = client
            .execute(INSERT_PERM, &[&role, &resource_n, &access_n])
            .map_err(|_e| SonarError::InvalidValue)?;
        if rows == 1 {
            Ok(())
        } else {
            Err(SonarError::InvalidValue)
        }
    }

    /// Patch permission
    pub fn permission_patch(
        &self,
        id: i32,
        mut obj: Map<String, Value>,
    ) -> Result<()> {
        let mut client = self.pool.get()?;
        let mut transaction = client.transaction()?;
        let row = transaction
            .query_one(QUERY_PERM, &[&id])
            .map_err(|_e| SonarError::InvalidName)?;
        let Permission {
            id,
            mut role,
            mut resource_n,
            mut batch,
            mut access_n,
        } = Permission::from_row(row);
        if let Some(Value::String(r)) = obj.remove("role") {
            role = r;
        }
        if let Some(Value::String(r)) = obj.remove("resource_n") {
            resource_n = r;
        }
        match obj.remove("batch") {
            Some(Value::String(b)) => batch = Some(b),
            Some(Value::Null) => batch = None,
            _ => (),
        };
        if let Some(Value::Number(a)) = obj.remove("access_n") {
            if let Some(a) = a.as_i64() {
                access_n = a as i32;
            }
        }
        let rows = transaction
            .execute(UPDATE_PERM, &[&id, &role, &resource_n, &batch, &access_n])
            .map_err(|_e| SonarError::Conflict)?;
        if rows == 1 {
            transaction.commit()?;
            Ok(())
        } else {
            Err(SonarError::Conflict)
        }
    }

    /// Delete permission by ID
    pub fn permission_delete(&self, id: i32) -> Result<()> {
        let mut client = self.pool.get()?;
        let rows = client.execute(DELETE_PERM, &[&id])?;
        if rows == 1 {
            Ok(())
        } else {
            Err(SonarError::NotFound)
        }
    }

    /// Get permissions for a user
    pub fn permissions_user(&self, user: &str) -> Result<Vec<Permission>> {
        let mut perms = vec![];
        let mut client = self.pool.get()?;
        for row in client.query(QUERY_ACCESS, &[&user])? {
            perms.push(Permission::from_row(row));
        }
        Ok(perms)
    }

    /// Get user permission for a resource
    pub fn permission_user_res(
        &self,
        user: &str,
        res: &str,
    ) -> Result<Permission> {
        let mut client = self.pool.get()?;
        for row in client.query(QUERY_PERMISSIONS, &[&user, &res])? {
            let perm = Permission::from_row(row);
            if perm.batch.is_none() {
                return Ok(perm);
            }
        }
        Err(SonarError::Forbidden)
    }

    /// Query one row by primary key
    pub fn get_by_pkey<PK: ToSql + Sync>(
        &self,
        sql: &'static str,
        pkey: PK,
    ) -> Result<String> {
        let mut client = self.pool.get()?;
        let query = format!("SELECT row_to_json(r)::text FROM ({sql}) r");
        let row = client.query_one(&query, &[&pkey])?;
        Ok(row.get::<usize, String>(0))
    }

    /// Query rows as an array by primary key
    pub fn get_array_by_pkey<PK: ToSql + Sync>(
        &self,
        sql: &'static str,
        pkey: PK,
    ) -> Result<String> {
        let mut client = self.pool.get()?;
        let query =
            format!("SELECT COALESCE(json_agg(r), '[]')::text FROM ({sql}) r");
        let row = client.query_one(&query, &[&pkey])?;
        Ok(row.get::<usize, String>(0))
    }
}
