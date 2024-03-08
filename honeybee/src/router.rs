// router.rs
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
use crate::cred::Credentials;
use crate::error::{Error, Result};
use crate::permission;
use crate::restype::ResType;
use crate::sonar::{self, Error as SonarError, Name};
use crate::Database;
use axum::body::Body;
use axum::extract::{Json, Path as AxumPath, State};
use axum::http::{header, StatusCode};
use axum::routing::{delete, get, patch, post};
use axum::Router;
use axum_extra::TypedHeader;
use headers::{ETag, IfNoneMatch};
use http::header::HeaderName;
use serde_json::map::Map;
use serde_json::Value;
use std::time::SystemTime;
use time::Duration;
use tokio::fs::metadata;
use tokio_postgres::types::ToSql;
use tokio_util::io::ReaderStream;
use tower_sessions::{Expiry, Session, SessionManagerLayer};
use tower_sessions_moka_store::MokaStore;

/// No-header response result
type Resp0 = std::result::Result<StatusCode, StatusCode>;

/// Single-header response result
type Resp1 =
    std::result::Result<([(HeaderName, &'static str); 1], String), StatusCode>;

/// Two-header response result
type Resp2 = std::result::Result<([(HeaderName, String); 2], Body), StatusCode>;

/// Create an HTML response
fn html_resp(html: &str) -> Resp1 {
    Ok(([(header::CONTENT_TYPE, "text/html")], html.to_string()))
}

/// Create a JSON response
fn json_resp(json: String) -> Resp1 {
    Ok(([(header::CONTENT_TYPE, "application/json")], json))
}

/// Build app router
pub async fn build(db: Database) -> Result<Router> {
    let store = MokaStore::new(Some(100));
    let session_layer = SessionManagerLayer::new(store)
        .with_name("honeybee")
        .with_expiry(Expiry::OnInactivity(Duration::hours(4)));
    let app = Router::new()
        .merge(login_post(db.clone()))
        .merge(access_get(db.clone()))
        .merge(permission_post(db.clone()))
        .merge(permission_router(db.clone()))
        .merge(resource_file_get(db.clone()))
        .merge(sonar_post(db.clone()))
        .merge(sql_record_get(db.clone()))
        .merge(sonar_object_patch(db.clone()))
        .merge(sonar_object_delete(db.clone()))
        .layer(session_layer);
    Ok(Router::new().nest("/iris/api", app))
}

/// Handle `POST` to login page
fn login_post(db: Database) -> Router {
    async fn handler(session: Session, Json(cred): Json<Credentials>) -> Resp1 {
        log::info!("POST login");
        session
            .cycle_id()
            .await
            .map_err(|_e| StatusCode::INTERNAL_SERVER_ERROR)?;
        cred.authenticate().await?;
        cred.store(&session)
            .await
            .map_err(|_e| StatusCode::INTERNAL_SERVER_ERROR)?;
        html_resp("<html>Authenticated</html>")
    }
    Router::new().route("/login", post(handler)).with_state(db)
}

/// `GET` access permissions
fn access_get(db: Database) -> Router {
    async fn handler(session: Session, State(db): State<Database>) -> Resp1 {
        log::info!("GET access");
        let cred = Credentials::load(&session).await?;
        let perms = permission::get_by_user(&db, cred.user()).await?;
        match serde_json::to_value(perms) {
            Ok(body) => json_resp(body.to_string()),
            Err(_e) => Err(StatusCode::BAD_REQUEST),
        }
    }
    Router::new().route("/access", get(handler)).with_state(db)
}

/// `POST` one permission record
fn permission_post(db: Database) -> Router {
    async fn handler(
        session: Session,
        State(db): State<Database>,
        Json(attrs): Json<Map<String, Value>>,
    ) -> Resp0 {
        let nm = Name::from(ResType::Permission);
        log::info!("POST {nm}");
        let cred = Credentials::load(&session).await?;
        name_access(&db, cred.user(), &nm, Access::Configure).await?;
        let role = attrs.get("role");
        let resource_n = attrs.get("resource_n");
        if let (Some(Value::String(role)), Some(Value::String(resource_n))) =
            (role, resource_n)
        {
            let role = role.to_string();
            let resource_n = resource_n.to_string();
            permission::post_role_res(&db, &role, &resource_n).await?;
            return Ok(StatusCode::CREATED);
        }
        Err(sonar::Error::InvalidValue)?
    }
    Router::new()
        .route("/permission", post(handler))
        .with_state(db)
}

/// Lookup access for a name
async fn name_access(
    db: &Database,
    user: &str,
    name: &Name,
    access: Access,
) -> Result<Access> {
    let perm = permission::get_by_name(db, user, name).await?;
    let acc = Access::new(perm.access_n).ok_or(Error::Unauthorized)?;
    acc.check(access)?;
    Ok(acc)
}

/// Router for permission
fn permission_router(db: Database) -> Router {
    /// Handle `GET` request
    async fn handle_get(
        session: Session,
        State(db): State<Database>,
        AxumPath(id): AxumPath<i32>,
    ) -> Resp1 {
        let nm = Name::from(ResType::Permission).obj(&id.to_string())?;
        log::info!("GET {nm}");
        let cred = Credentials::load(&session).await?;
        name_access(&db, cred.user(), &nm, Access::View).await?;
        let perm = permission::get_by_id(&db, id).await?;
        match serde_json::to_value(perm) {
            Ok(body) => json_resp(body.to_string()),
            Err(_e) => Err(StatusCode::BAD_REQUEST),
        }
    }

    /// Handle `PATCH` request
    async fn handle_patch(
        session: Session,
        State(db): State<Database>,
        AxumPath(id): AxumPath<i32>,
        Json(attrs): Json<Map<String, Value>>,
    ) -> Resp0 {
        let nm = Name::from(ResType::Permission).obj(&id.to_string())?;
        log::info!("PATCH {nm}");
        let cred = Credentials::load(&session).await?;
        name_access(&db, cred.user(), &nm, Access::Configure).await?;
        permission::patch_by_id(&db, id, attrs).await?;
        Ok(StatusCode::NO_CONTENT)
    }

    /// Handle `DELETE` request
    async fn handle_delete(
        session: Session,
        State(db): State<Database>,
        AxumPath(id): AxumPath<i32>,
    ) -> Resp0 {
        let nm = Name::from(ResType::Permission).obj(&id.to_string())?;
        log::info!("DELETE {nm}");
        let cred = Credentials::load(&session).await?;
        name_access(&db, cred.user(), &nm, Access::Configure).await?;
        permission::delete_by_id(&db, id).await?;
        Ok(StatusCode::NO_CONTENT)
    }

    Router::new()
        .route(
            "/permission/:id",
            get(handle_get).patch(handle_patch).delete(handle_delete),
        )
        .with_state(db)
}

/// `GET` one SQL record as JSON
fn sql_record_get(db: Database) -> Router {
    async fn handler(
        session: Session,
        State(db): State<Database>,
        AxumPath((type_n, obj_n)): AxumPath<(String, String)>,
    ) -> Resp1 {
        let nm = Name::new(&type_n)?.obj(&obj_n)?;
        log::info!("GET {nm}");
        let cred = Credentials::load(&session).await?;
        name_access(&db, cred.user(), &nm, Access::View).await?;
        let sql = nm.res_type.one_sql();
        let name = nm.object_n().ok_or(StatusCode::BAD_REQUEST)?;
        let body = if nm.res_type == ResType::ControllerIo {
            get_array_by_pkey(&db, sql, &name).await
        } else {
            get_by_pkey(&db, sql, &name).await
        }?;
        json_resp(body)
    }
    Router::new()
        .route("/:type_n/:obj_n", get(handler))
        .with_state(db)
}

/// Query one row by primary key
async fn get_by_pkey<PK: ToSql + Sync>(
    db: &Database,
    sql: &'static str,
    pkey: PK,
) -> Result<String> {
    let client = db.client().await?;
    let query = format!("SELECT row_to_json(r)::text FROM ({sql}) r");
    let row = client
        .query_one(&query, &[&pkey])
        .await
        .map_err(|_e| SonarError::NotFound)?;
    Ok(row.get::<usize, String>(0))
}

/// Query rows as an array by primary key
async fn get_array_by_pkey<PK: ToSql + Sync>(
    db: &Database,
    sql: &'static str,
    pkey: PK,
) -> Result<String> {
    let client = db.client().await?;
    let query =
        format!("SELECT COALESCE(json_agg(r), '[]')::text FROM ({sql}) r");
    let row = client
        .query_one(&query, &[&pkey])
        .await
        .map_err(|_e| SonarError::NotFound)?;
    Ok(row.get::<usize, String>(0))
}

/// Invalid characters for SONAR names
const INVALID_CHARS: &[char] = &['\0', '\u{001e}', '\u{001f}'];

/// Check if a character in a Sonar name is invalid
fn invalid_char(c: char) -> bool {
    INVALID_CHARS.contains(&c)
}

/// `GET` a file resource
fn resource_file_get(db: Database) -> Router {
    async fn handler(
        session: Session,
        State(db): State<Database>,
        TypedHeader(if_none_match): TypedHeader<IfNoneMatch>,
        AxumPath(type_n): AxumPath<String>,
    ) -> Resp2 {
        let nm = Name::new(&type_n)?;
        log::info!("GET {nm}");
        let cred = Credentials::load(&session).await?;
        name_access(&db, cred.user(), &nm, Access::View).await?;
        let fname = format!("api/{type_n}");
        let etag = resource_etag(&fname).await?;
        log::trace!("ETag: {etag}");
        let tag = etag.parse::<ETag>().map_err(|_e| Error::InvalidETag)?;
        if if_none_match.precondition_passes(&tag) {
            log::trace!("opening {fname}");
            let file = match tokio::fs::File::open(fname).await {
                Ok(file) => file,
                Err(_err) => return Err(StatusCode::NOT_FOUND),
            };
            let stream = ReaderStream::new(file);
            Ok((
                [
                    (header::ETAG, etag),
                    (header::CONTENT_TYPE, "application/json".to_string()),
                ],
                Body::from_stream(stream),
            ))
        } else {
            Err(StatusCode::NOT_MODIFIED)
        }
    }
    Router::new().route("/:type_n", get(handler)).with_state(db)
}

/// Get a static file ETag
async fn resource_etag(path: &str) -> Result<String> {
    let meta = metadata(path).await?;
    let modified = meta.modified()?;
    let dur = modified.duration_since(SystemTime::UNIX_EPOCH)?.as_millis();
    Ok(format!("\"{dur:x}\""))
}

/// Create a Sonar object from a `POST` request
fn sonar_post(db: Database) -> Router {
    async fn handler(
        session: Session,
        AxumPath(type_n): AxumPath<String>,
        State(db): State<Database>,
        Json(attrs): Json<Map<String, Value>>,
    ) -> Resp0 {
        let nm = Name::new(&type_n)?;
        log::info!("POST {nm}");
        let cred = Credentials::load(&session).await?;
        name_access(&db, cred.user(), &nm, Access::Configure).await?;
        match attrs.get("name") {
            Some(Value::String(name)) => {
                let name = nm.obj(name)?;
                let mut c = cred.authenticate().await?;
                // first, set attributes on phantom object
                for (key, value) in attrs.iter() {
                    let attr = &key[..];
                    if attr != "name" {
                        let anm = name.attr(attr)?;
                        let value = att_value(value)?;
                        log::debug!("{anm} = {value} (phantom)");
                        c.update_object(&anm, &value).await?;
                    }
                }
                log::debug!("creating {name}");
                c.create_object(&name.to_string()).await?;
                Ok(StatusCode::CREATED)
            }
            _ => Err(sonar::Error::InvalidValue)?,
        }
    }
    Router::new()
        .route("/:type_n", post(handler))
        .with_state(db)
}

/// Get Sonar attribute value
fn att_value(value: &Value) -> Result<String> {
    match value {
        Value::String(value) => {
            if value.contains(invalid_char) {
                Err(sonar::Error::InvalidValue)?
            } else {
                Ok(value.to_string())
            }
        }
        Value::Bool(value) => Ok(value.to_string()),
        Value::Number(value) => Ok(value.to_string()),
        Value::Null => Ok("\0".to_string()),
        _ => Err(sonar::Error::InvalidValue)?,
    }
}

/// Update a Sonar object from a `PATCH` request
fn sonar_object_patch(db: Database) -> Router {
    async fn handler(
        session: Session,
        State(db): State<Database>,
        AxumPath((type_n, obj_n)): AxumPath<(String, String)>,
        Json(attrs): Json<Map<String, Value>>,
    ) -> Resp0 {
        let nm = Name::new(&type_n)?.obj(&obj_n)?;
        log::info!("PATCH {nm}");
        let cred = Credentials::load(&session).await?;
        // *At least* Operate access needed (further checks below)
        let access =
            name_access(&db, cred.user(), &nm, Access::Operate).await?;
        for key in attrs.keys() {
            let attr = &key[..];
            access.check(nm.res_type.access_attr(attr))?;
        }
        let mut c = cred.authenticate().await?;
        // first pass
        for (key, value) in attrs.iter() {
            let attr = &key[..];
            if nm.res_type.patch_first_pass(attr) {
                let anm = nm.attr(attr)?;
                let value = att_value(value)?;
                log::debug!("{anm} = {value}");
                c.update_object(&anm, &value).await?;
            }
        }
        // second pass
        for (key, value) in attrs.iter() {
            let attr = &key[..];
            if !nm.res_type.patch_first_pass(attr) {
                let anm = nm.attr(attr)?;
                let value = att_value(value)?;
                log::debug!("{} = {}", anm, &value);
                c.update_object(&anm, &value).await?;
            }
        }
        Ok(StatusCode::NO_CONTENT)
    }
    Router::new()
        .route("/:type_n/:obj_n", patch(handler))
        .with_state(db)
}

/// Remove a Sonar object from a `DELETE` request
fn sonar_object_delete(db: Database) -> Router {
    async fn handler(
        session: Session,
        State(db): State<Database>,
        AxumPath((type_n, obj_n)): AxumPath<(String, String)>,
    ) -> Resp0 {
        let nm = Name::new(&type_n)?.obj(&obj_n)?;
        log::info!("DELETE {nm}");
        let cred = Credentials::load(&session).await?;
        name_access(&db, cred.user(), &nm, Access::Configure).await?;
        let mut c = cred.authenticate().await?;
        c.remove_object(&nm.to_string()).await?;
        Ok(StatusCode::ACCEPTED)
    }
    Router::new()
        .route("/:type_n/:obj_n", delete(handler))
        .with_state(db)
}
