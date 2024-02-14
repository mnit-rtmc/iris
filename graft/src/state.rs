// state.rs
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
use crate::access::Access;
use crate::error::{Error, Result};
use crate::query::PERMISSION;
use crate::sonar::{Connection, Error as SonarError, Name};
use bb8::Pool;
use bb8_postgres::PostgresConnectionManager;
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use tokio_postgres::types::ToSql;
use tokio_postgres::{NoTls, Row};
use tower_sessions::Session;

/// IRIS host name
const HOST: &str = "localhost.localdomain";

/// Session key for credentials
const CRED_KEY: &str = "cred";

/// Authentication credentials
#[derive(Clone, Debug, Eq, PartialEq, Deserialize, Serialize)]
pub struct Credentials {
    /// Sonar username
    username: String,
    /// Sonar password
    password: String,
}

impl Credentials {
    /// Load credentials from session
    pub async fn load(session: &Session) -> Result<Self> {
        session.get(CRED_KEY).await?.ok_or(Error::Unauthorized)
    }

    /// Store credentials in session
    pub async fn store(&self, session: &Session) -> Result<()> {
        Ok(session.insert(CRED_KEY, self).await?)
    }

    /// Get user name as string slice
    pub fn user(&self) -> &str {
        &self.username
    }

    /// Authenticate with IRIS server
    pub async fn authenticate(&self) -> Result<Connection> {
        let mut c = Connection::new(HOST, 1037).await?;
        c.login(&self.username, &self.password).await?;
        Ok(c)
    }
}

/// Create one permission
const INSERT_PERM: &str = "\
INSERT INTO iris.permission (role, resource_n, access_n) \
VALUES ($1, $2, $3)";

/// Update one permission
const UPDATE_PERM: &str = "\
UPDATE iris.permission \
SET (role, resource_n, hashtag, access_n) = ($2, $3, $4, $5) \
WHERE id = $1";

/// Delete one permission
const DELETE_PERM: &str = "\
DELETE \
FROM iris.permission \
WHERE id = $1";

/// Query access permissions for a user
const QUERY_ACCESS: &str = "\
SELECT p.id, p.role, p.resource_n, p.hashtag, p.access_n \
FROM iris.i_user u \
JOIN iris.role r ON u.role = r.name \
JOIN iris.permission p ON p.role = r.name \
WHERE u.enabled = true AND r.enabled = true \
AND u.name = $1 \
ORDER BY p.resource_n, p.hashtag";

/// Query permission for a user / resource (no hashtag)
const QUERY_PERMISSION: &str = "\
SELECT p.id, p.role, p.resource_n, p.hashtag, p.access_n \
FROM iris.permission p \
JOIN iris.role r ON p.role = r.name \
JOIN iris.i_user u ON u.role = r.name \
WHERE r.enabled = true \
AND u.enabled = true \
AND u.name = $1 \
AND p.resource_n = $2 \
AND p.hashtag IS NULL";

/// Query permission for a user / resource / hashtag
const QUERY_PERMISSION_TAG: &str = "\
SELECT p.id, p.role, p.resource_n, p.hashtag, p.access_n \
FROM iris.permission p \
JOIN iris.role r ON p.role = r.name \
JOIN iris.i_user u ON u.role = r.name \
WHERE r.enabled = true \
AND u.enabled = true \
AND u.name = $1 \
AND p.resource_n = $2 \
AND (\
  p.hashtag IS NULL OR \
  p.hashtag IN (\
    SELECT hashtag \
    FROM iris.hashtag h \
    WHERE h.resource_n = p.resource_n \
    AND h.name = $3 \
  )\
) \
ORDER BY access_n DESC \
LIMIT 1";

/// Permission
#[derive(Clone, Debug, Eq, PartialEq, Deserialize, Serialize)]
pub struct Permission {
    pub id: i32,
    pub role: String,
    pub resource_n: String,
    pub hashtag: Option<String>,
    pub access_n: i32,
}

impl Permission {
    fn from_row(row: Row) -> Self {
        Permission {
            id: row.get(0),
            role: row.get(1),
            resource_n: row.get(2),
            hashtag: row.get(3),
            access_n: row.get(4),
        }
    }
}

/// Db connection pool
type PostgresPool = Pool<PostgresConnectionManager<NoTls>>;

/// Make postgres pool
async fn make_pool() -> Result<PostgresPool> {
    let username = whoami::username();
    // Format path for unix domain socket -- not worth using percent_encode
    let uds = format!("postgres://{username}@%2Frun%2Fpostgresql/tms");
    let config = uds.parse()?;
    let manager = PostgresConnectionManager::new(config, NoTls);
    let pool = Pool::builder().build(manager).await?;
    Ok(pool)
}

/// Application state for postgres
#[derive(Clone)]
pub struct AppState {
    /// Db connection pool
    pool: PostgresPool,
}

impl AppState {
    /// Create new postgres application state
    pub async fn new() -> Result<Self> {
        let pool = make_pool().await?;
        Ok(AppState { pool })
    }

    /// Get permission by ID
    pub async fn permission(&self, id: i32) -> Result<Permission> {
        let client = self.pool.get().await?;
        let row = client
            .query_one(PERMISSION, &[&id])
            .await
            .map_err(|_e| SonarError::NotFound)?;
        Ok(Permission::from_row(row))
    }

    /// Create a permission
    pub async fn permission_post(
        &self,
        role: &str,
        resource_n: &str,
    ) -> Result<()> {
        let access_n = 1; // View is a good default
        let client = self.pool.get().await?;
        let rows = client
            .execute(INSERT_PERM, &[&role, &resource_n, &access_n])
            .await
            .map_err(|_e| SonarError::InvalidValue)?;
        if rows == 1 {
            Ok(())
        } else {
            Err(SonarError::InvalidValue.into())
        }
    }

    /// Patch permission
    pub async fn permission_patch(
        &self,
        id: i32,
        mut obj: Map<String, Value>,
    ) -> Result<()> {
        let mut client = self.pool.get().await?;
        let transaction = client.transaction().await?;
        let row = transaction
            .query_one(PERMISSION, &[&id])
            .await
            .map_err(|_e| SonarError::NotFound)?;
        let Permission {
            id,
            mut role,
            mut resource_n,
            mut hashtag,
            mut access_n,
        } = Permission::from_row(row);
        if let Some(Value::String(r)) = obj.remove("role") {
            role = r;
        }
        if let Some(Value::String(r)) = obj.remove("resource_n") {
            resource_n = r;
        }
        match obj.remove("hashtag") {
            Some(Value::String(ht)) => hashtag = Some(ht),
            Some(Value::Null) => hashtag = None,
            _ => (),
        };
        if let Some(Value::Number(a)) = obj.remove("access_n") {
            if let Some(a) = a.as_i64() {
                access_n = a as i32;
            }
        }
        let rows = transaction
            .execute(
                UPDATE_PERM,
                &[&id, &role, &resource_n, &hashtag, &access_n],
            )
            .await
            .map_err(|_e| SonarError::Conflict)?;
        if rows == 1 {
            transaction.commit().await?;
            Ok(())
        } else {
            Err(SonarError::Conflict.into())
        }
    }

    /// Delete permission by ID
    pub async fn permission_delete(&self, id: i32) -> Result<()> {
        let client = self.pool.get().await?;
        let rows = client.execute(DELETE_PERM, &[&id]).await?;
        if rows == 1 {
            Ok(())
        } else {
            Err(SonarError::NotFound.into())
        }
    }

    /// Get permissions for a user
    pub async fn permissions_user(
        &self,
        user: &str,
    ) -> Result<Vec<Permission>> {
        let mut perms = Vec::new();
        let client = self.pool.get().await?;
        for row in client.query(QUERY_ACCESS, &[&user]).await? {
            perms.push(Permission::from_row(row));
        }
        Ok(perms)
    }

    /// Get user permission for a Sonar name
    async fn permission_user_res(
        &self,
        user: &str,
        name: &Name,
    ) -> Result<Permission> {
        let client = self.pool.get().await?;
        let type_n = name.res_type.dependent().as_str();
        match name.object_n() {
            Some(tag) => {
                let row = client
                    .query_one(QUERY_PERMISSION_TAG, &[&user, &type_n, &tag])
                    .await
                    .map_err(|_e| SonarError::Forbidden)?;
                Ok(Permission::from_row(row))
            }
            None => {
                let row = client
                    .query_one(QUERY_PERMISSION, &[&user, &type_n])
                    .await
                    .map_err(|_e| SonarError::Forbidden)?;
                Ok(Permission::from_row(row))
            }
        }
    }

    /// Query one row by primary key
    pub async fn get_by_pkey<PK: ToSql + Sync>(
        &self,
        sql: &'static str,
        pkey: PK,
    ) -> Result<String> {
        let client = self.pool.get().await?;
        let query = format!("SELECT row_to_json(r)::text FROM ({sql}) r");
        let row = client
            .query_one(&query, &[&pkey])
            .await
            .map_err(|_e| SonarError::NotFound)?;
        Ok(row.get::<usize, String>(0))
    }

    /// Query rows as an array by primary key
    pub async fn get_array_by_pkey<PK: ToSql + Sync>(
        &self,
        sql: &'static str,
        pkey: PK,
    ) -> Result<String> {
        let client = self.pool.get().await?;
        let query =
            format!("SELECT COALESCE(json_agg(r), '[]')::text FROM ({sql}) r");
        let row = client
            .query_one(&query, &[&pkey])
            .await
            .map_err(|_e| SonarError::NotFound)?;
        Ok(row.get::<usize, String>(0))
    }

    /// Lookup access for a name
    pub async fn name_access(
        &self,
        user: &str,
        name: &Name,
        access: Access,
    ) -> Result<Access> {
        let perm = self.permission_user_res(user, name).await?;
        let acc = Access::new(perm.access_n).ok_or(Error::Unauthorized)?;
        acc.check(access)?;
        Ok(acc)
    }
}
