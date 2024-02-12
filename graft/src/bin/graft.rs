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
    extract::{Json, Path as AxumPath, State},
    http::{header, StatusCode},
    routing::{delete, get, patch, post},
    Router,
};
use graft::access::Access;
use graft::error::{Error, Result};
use graft::restype::ResType;
use graft::sonar::{self, Name};
use graft::state::{AppState, Credentials};
use http::header::HeaderName;
use serde_json::map::Map;
use serde_json::Value;
use time::Duration;
use tokio::net::TcpListener;
use tower_sessions::{Expiry, Session, SessionManagerLayer};
use tower_sessions_moka_store::MokaStore;

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
    let store = MokaStore::new(Some(100));
    let session_layer = SessionManagerLayer::new(store)
        .with_name("graft")
        .with_expiry(Expiry::OnInactivity(Duration::hours(4)));
    let app = Router::new()
        .merge(login_post(state.clone()))
        .merge(access_get(state.clone()))
        .merge(permission_post(state.clone()))
        .merge(permission_router(state.clone()))
        .merge(resource_file_get(state.clone()))
        .merge(sonar_post(state.clone()))
        .merge(sql_record_get(state.clone()))
        .merge(sonar_object_patch(state.clone()))
        .merge(sonar_object_delete(state.clone()))
        .layer(session_layer);
    let app = Router::new().nest("/iris/api", app);
    let listener = TcpListener::bind("127.0.0.1:3737").await?;
    axum::serve(listener, app).await?;
    Ok(())
}

/// Handle `POST` to login page
fn login_post(state: AppState) -> Router {
    async fn handler(session: Session, Json(cred): Json<Credentials>) -> Resp1 {
        log::info!("POST /login");
        cred.authenticate().await?;
        cred.store(&session)
            .await
            .map_err(|_e| StatusCode::INTERNAL_SERVER_ERROR)?;
        html_resp("<html>Authenticated</html>")
    }
    Router::new()
        .route("/login", post(handler))
        .with_state(state)
}

/// `GET` access permissions
fn access_get(state: AppState) -> Router {
    async fn handler(session: Session, State(state): State<AppState>) -> Resp1 {
        log::info!("GET /access");
        let cred = Credentials::load(&session).await?;
        let perms = state.permissions_user(cred.user()).await?;
        match serde_json::to_value(perms) {
            Ok(body) => json_resp(body),
            Err(_e) => Err(StatusCode::BAD_REQUEST),
        }
    }
    Router::new()
        .route("/access", get(handler))
        .with_state(state)
}

/// `POST` one permission record
fn permission_post(state: AppState) -> Router {
    async fn handler(
        session: Session,
        State(state): State<AppState>,
        Json(attrs): Json<Map<String, Value>>,
    ) -> Resp0 {
        let nm = Name::from(ResType::Permission);
        log::info!("POST {nm}");
        let cred = Credentials::load(&session).await?;
        state
            .name_access(cred.user(), &nm, Access::Configure)
            .await?;
        let role = attrs.get("role");
        let resource_n = attrs.get("resource_n");
        if let (Some(Value::String(role)), Some(Value::String(resource_n))) =
            (role, resource_n)
        {
            let role = role.to_string();
            let resource_n = resource_n.to_string();
            state.permission_post(&role, &resource_n).await?;
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
        session: Session,
        State(state): State<AppState>,
        AxumPath(id): AxumPath<i32>,
    ) -> Resp1 {
        let nm = Name::from(ResType::Permission).obj(&id.to_string())?;
        log::info!("GET {nm}");
        let cred = Credentials::load(&session).await?;
        state.name_access(cred.user(), &nm, Access::View).await?;
        let perm = state.permission(id).await?;
        match serde_json::to_value(perm) {
            Ok(body) => json_resp(body),
            Err(_e) => Err(StatusCode::BAD_REQUEST),
        }
    }

    /// Handle `PATCH` request
    async fn handle_patch(
        session: Session,
        State(state): State<AppState>,
        AxumPath(id): AxumPath<i32>,
        Json(attrs): Json<Map<String, Value>>,
    ) -> Resp0 {
        let nm = Name::from(ResType::Permission).obj(&id.to_string())?;
        log::info!("PATCH {nm}");
        let cred = Credentials::load(&session).await?;
        state
            .name_access(cred.user(), &nm, Access::Configure)
            .await?;
        state.permission_patch(id, attrs).await?;
        Ok(StatusCode::NO_CONTENT)
    }

    /// Handle `DELETE` request
    async fn handle_delete(
        session: Session,
        State(state): State<AppState>,
        AxumPath(id): AxumPath<i32>,
    ) -> Resp0 {
        let nm = Name::from(ResType::Permission).obj(&id.to_string())?;
        log::info!("DELETE {nm}");
        let cred = Credentials::load(&session).await?;
        state
            .name_access(cred.user(), &nm, Access::Configure)
            .await?;
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
        session: Session,
        State(state): State<AppState>,
        AxumPath((type_n, obj_n)): AxumPath<(String, String)>,
    ) -> Resp1 {
        let nm = Name::new(&type_n)?.obj(&obj_n)?;
        log::info!("GET {nm}");
        let cred = Credentials::load(&session).await?;
        state.name_access(cred.user(), &nm, Access::View).await?;
        let sql = nm.res_type.sql_query();
        let name = nm.object_n().ok_or(StatusCode::BAD_REQUEST)?;
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
        session: Session,
        State(state): State<AppState>,
        AxumPath(type_n): AxumPath<String>,
    ) -> Resp2 {
        let nm = Name::new(&type_n)?;
        log::info!("GET {nm}");
        let cred = Credentials::load(&session).await?;
        state.name_access(cred.user(), &nm, Access::View).await?;
        todo!()
        /*
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
        ))*/
    }
    Router::new()
        .route("/:type_n", get(handler))
        .with_state(state)
}

/*
/// Get a static file ETag
async fn resource_etag(path: &Path) -> Result<String> {
    let meta = metadata(path).await?;
    let modified = meta.modified()?;
    let dur = modified.duration_since(SystemTime::UNIX_EPOCH)?.as_millis();
    Ok(format!("{dur:x}"))
}*/

/// Create a Sonar object from a `POST` request
fn sonar_post(state: AppState) -> Router {
    async fn handler(
        session: Session,
        AxumPath(type_n): AxumPath<String>,
        State(state): State<AppState>,
        Json(attrs): Json<Map<String, Value>>,
    ) -> Resp0 {
        let nm = Name::new(&type_n)?;
        log::info!("POST {nm}");
        let cred = Credentials::load(&session).await?;
        state
            .name_access(cred.user(), &nm, Access::Configure)
            .await?;
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
        session: Session,
        State(state): State<AppState>,
        AxumPath((type_n, obj_n)): AxumPath<(String, String)>,
        Json(attrs): Json<Map<String, Value>>,
    ) -> Resp0 {
        let nm = Name::new(&type_n)?.obj(&obj_n)?;
        log::info!("PATCH {nm}");
        let cred = Credentials::load(&session).await?;
        // *At least* Operate access needed (further checks below)
        let access =
            state.name_access(cred.user(), &nm, Access::Operate).await?;
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
        .with_state(state)
}

/// Remove a Sonar object from a `DELETE` request
fn sonar_object_delete(state: AppState) -> Router {
    async fn handler(
        session: Session,
        State(state): State<AppState>,
        AxumPath((type_n, obj_n)): AxumPath<(String, String)>,
    ) -> Resp0 {
        let nm = Name::new(&type_n)?.obj(&obj_n)?;
        log::info!("DELETE {nm}");
        let cred = Credentials::load(&session).await?;
        state
            .name_access(cred.user(), &nm, Access::Configure)
            .await?;
        let mut c = cred.authenticate().await?;
        c.remove_object(&nm.to_string()).await?;
        Ok(StatusCode::ACCEPTED)
    }
    Router::new()
        .route("/:type_n/:obj_n", delete(handler))
        .with_state(state)
}
