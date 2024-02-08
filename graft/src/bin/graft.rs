// graft.rs
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
#![forbid(unsafe_code)]

use axum::{
    body::Body,
    extract::{Json, Path as AxumPath, State},
    http::{header, StatusCode},
    response::IntoResponse,
    routing::{delete, get, patch, post},
    Router,
};
use core::time::Duration;
use graft::access::Access;
use graft::error::{Error, Result};
use graft::restype::ResType;
use graft::sonar::{self, Connection, Name};
use graft::state::AppState;
use http::header::HeaderName;
use rand::Rng;
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::fmt;
use std::io;
use std::time::SystemTime;
use tokio::net::TcpListener;

/// Path for static files
const STATIC_PATH: &str = "/var/www/html/iris/api";

/// No-header response result
type Resp0 = std::result::Result<StatusCode, StatusCode>;

/// Single-header response result
type Resp1 =
    std::result::Result<([(HeaderName, &'static str); 1], String), StatusCode>;

/// Two-header response result
type Resp2 =
    std::result::Result<([(HeaderName, &'static str); 2], String), StatusCode>;

/// Create an HTML response
fn html_resp(html: &str) -> Resp1 {
    Ok(([(header::CONTENT_TYPE, "text/html")], html.to_string()))
}

/// Create a JSON response
fn json_resp(json: Value) -> Resp1 {
    Ok((
        [(header::CONTENT_TYPE, "application/json")],
        json.to_string(),
    ))
}

/// Main entry point
#[tokio::main]
async fn main() -> Result<()> {
    env_logger::builder().format_timestamp(None).init();
    let state = AppState::new().await?;
    /*
    FIXME: use axum-login / tower-sessions
    app.with(
        SessionMiddleware::new(
            MemoryStore::new(),
            &rand::thread_rng().gen::<[u8; 32]>(),
        )
        .with_cookie_name("graft")
        .with_session_ttl(Some(Duration::from_secs(8 * 60 * 60))),
    );*/
    let app = Router::new()
        .merge(login_post(state.clone()))
        .merge(access_get(state.clone()))
        .merge(permission_post(state.clone()))
        .merge(permission_router(state.clone()))
        .merge(resource_file_get(state.clone()))
        .merge(sonar_post(state.clone()))
        .merge(sql_record_get(state.clone()))
        .merge(sonar_object_patch(state.clone()))
        .merge(sonar_object_delete(state.clone()));
    let app = Router::new().nest("/iris/api", app);
    let listener = TcpListener::bind("127.0.0.1:3737").await?;
    axum::serve(listener, app).await?;
    Ok(())
}

/// Handle `POST` to login page
fn login_post(state: AppState) -> Router {
    async fn handler(
        State(state): State<AppState>,
        Json(cred): Json<Credentials>,
    ) -> Resp1 {
        log::info!("POST /login");
        cred.authenticate().await?;
        let session = state.session_mut();
        session.insert("cred", cred)?;
        html_resp("<html>Authenticated</html>")
    }
    Router::new()
        .route("/login", post(handler))
        .with_state(state)
}

/// `GET` access permissions
fn access_get(state: AppState) -> Router {
    async fn handler(State(state): State<AppState>) -> Resp1 {
        log::info!("GET /access");
        let session = state.session();
        let cred: Credentials =
            session.get("cred").ok_or(Error::Unauthorized)?;
        let perms = state.permissions_user(&cred.username).await?;
        match serde_json::to_value(perms) {
            Ok(body) => json_resp(body),
            Err(e) => Err(StatusCode::BAD_REQUEST),
        }
    }
    Router::new()
        .route("/access", get(handler))
        .with_state(state)
}

/// `POST` one permission record
fn permission_post(state: AppState) -> Router {
    async fn handler(
        State(state): State<AppState>,
        Json(attrs): Json<Map<String, Value>>,
    ) -> Resp0 {
        let nm = Name::from_type(ResType::Permission);
        log::info!("POST {nm}");
        state.name_access(&nm, Access::Configure).await?;
        let role = attrs.get("role");
        let resource_n = attrs.get("resource_n");
        if let (Some(Value::String(role)), Some(Value::String(resource_n))) =
            (role, resource_n)
        {
            let role = role.to_string();
            let resource_n = resource_n.to_string();
            state.permission_post(&role, &resource_n).await;
            return Ok(StatusCode::CREATED);
        }
        Err(sonar::Error::InvalidValue)?
    }
    Router::new()
        .route("/permission", post(handler))
        .with_state(state)
}

/// Router for permission
fn permission_router(state: AppState) -> Router {
    /// Handle `GET` request
    async fn handle_get(
        AxumPath(id): AxumPath<i32>,
        State(state): State<AppState>,
    ) -> Resp1 {
        let nm = Name::from_type(ResType::Permission).obj(id.to_string())?;
        log::info!("GET {nm}");
        state.name_access(&nm, Access::View).await?;
        let perm = state.permission(id).await?;
        match serde_json::to_value(perm) {
            Ok(body) => json_resp(body),
            Err(_e) => Err(StatusCode::BAD_REQUEST),
        }
    }

    /// Handle `PATCH` request
    async fn handle_patch(
        AxumPath(id): AxumPath<i32>,
        State(state): State<AppState>,
        Json(attrs): Json<Map<String, Value>>,
    ) -> Resp0 {
        let nm = Name::from_type(ResType::Permission).obj(id.to_string())?;
        log::info!("PATCH {nm}");
        state.name_access(&nm, Access::Configure).await?;
        state.permission_patch(id, attrs).await?;
        Ok(StatusCode::NO_CONTENT)
    }

    /// Handle `DELETE` request
    async fn handle_delete(
        AxumPath(id): AxumPath<i32>,
        State(state): State<AppState>,
    ) -> Resp0 {
        let nm = Name::from_type(ResType::Permission).obj(id.to_string())?;
        log::info!("DELETE {nm}");
        state.name_access(&nm, Access::Configure).await?;
        state.permission_delete(id).await?;
        Ok(StatusCode::NO_CONTENT)
    }

    Router::new()
        .route(
            "/permission/:id",
            get(handle_get).patch(handle_patch).delete(handle_delete),
        )
        .with_state(state)
}

/// `GET` one SQL record as JSON
fn sql_record_get(state: AppState) -> Router {
    async fn handler(
        AxumPath((type_n, obj_n)): AxumPath<(&str, String)>,
        State(state): State<AppState>,
    ) -> Resp1 {
        let nm = Name::new(type_n)?.obj(obj_n)?;
        log::info!("GET {nm}");
        state.name_access(&nm, Access::View).await?;
        let sql = nm.res_type.sql_query();
        let name = nm.object_n().ok_or(Error::InvalidValue)?;
        let body = if nm.res_type == ResType::ControllerIo {
            state.get_array_by_pkey(sql, &name).await
        } else {
            state.get_by_pkey(sql, &name).await
        }?;
        match serde_json::to_value(body) {
            Ok(body) => json_resp(body),
            Err(_e) => Err(StatusCode::INTERNAL_SERVER_ERROR),
        }
    }
    Router::new()
        .route("/:type_n/:obj_n", get(handler))
        .with_state(state)
}

/// Invalid characters for SONAR names
const INVALID_CHARS: &[char] = &['\0', '\u{001e}', '\u{001f}'];

/// Check if a character in a Sonar name is invalid
fn invalid_char(c: char) -> bool {
    INVALID_CHARS.contains(&c)
}

/// `GET` a file resource
fn resource_file_get(state: AppState) -> Router {
    async fn handler(
        AxumPath(type_n): AxumPath<&str>,
        State(state): State<AppState>,
    ) -> Resp2 {
        let nm = Name::new(type_n)?;
        log::info!("GET {nm}");
        state.name_access(&nm, Access::View).await?;
        let path = PathBuf::from(nm.to_string());
        let etag = resource_etag(&path).await?;
        if let Some(values) = req.header("If-None-Match") {
            if values.iter().any(|v| v == &etag) {
                return Err(StatusCode::NOT_MODIFIED);
            }
        }
        let body = Body::from_file(&path).await?;
        // FIXME: use tower ServeFile instead (if ETag changed)
        Ok((
            [
                (header::ETAG, &etag),
                (header::CONTENT_TYPE, "application/json"),
            ],
            body,
        ))
    }
    Router::new()
        .route("/:type_n", get(handler))
        .with_state(state)
}

/// Get a static file ETag
async fn resource_etag(path: &Path) -> Result<String> {
    let meta = metadata(path).await?;
    let modified = meta.modified()?;
    let dur = modified.duration_since(SystemTime::UNIX_EPOCH)?.as_millis();
    Ok(format!("{dur:x}"))
}

/// Create a Sonar object from a `POST` request
fn sonar_post(state: AppState) -> Router {
    async fn handler(
        AxumPath(type_n): AxumPath<&str>,
        State(state): State<AppState>,
        Json(attrs): Json<Map<String, Value>>,
    ) -> Resp0 {
        let nm = Name::new(type_n)?;
        log::info!("POST {nm}");
        state.name_access(&nm, Access::Configure).await?;
        match attrs.get("name") {
            Some(Value::String(name)) => {
                let name = nm.obj(name)?;
                let mut c = state.connection().await?;
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
                c.create_object(&name).await?;
                Ok(StatusCode::CREATED)
            }
            _ => Err(sonar::Error::InvalidValue)?,
        }
    }
    Router::new()
        .route("/:type_n", post(handler))
        .with_state(state)
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
fn sonar_object_patch(state: AppState) -> Router {
    async fn handler(
        AxumPath((type_n, obj_n)): AxumPath<(&str, String)>,
        State(state): State<AppState>,
        Json(attrs): Json<Map<String, Value>>,
    ) -> Resp0 {
        let nm = Name::new(type_n)?.obj(obj_n)?;
        log::info!("PATCH {nm}");
        // *At least* Operate access needed (further checks below)
        let access = state.name_access(&nm, Access::Operate).await?;
        for key in attrs.keys() {
            let attr = &key[..];
            access.check(nm.res_type.access_attr(attr))?;
        }
        let mut c = state.connection().await?;
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
        .with_state(state)
}

/// Remove a Sonar object from a `DELETE` request
fn sonar_object_delete(state: AppState) -> Router {
    async fn handler(
        AxumPath((type_n, obj_n)): AxumPath<(&str, String)>,
        State(state): State<AppState>,
    ) -> Resp0 {
        let nm = Name::new(type_n)?.obj(obj_n)?;
        log::info!("DELETE {nm}");
        state.name_access(&nm, Access::Configure).await?;
        let mut c = state.connection().await?;
        c.remove_object(nm.to_str()).await?;
        Ok(StatusCode::ACCEPTED)
    }
    Router::new()
        .route("/:type_n/:obj_n", delete(handler))
        .with_state(state)
}
