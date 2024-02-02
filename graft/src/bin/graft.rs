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
use convert_case::{Case, Casing};
use core::time::Duration;
use graft::access::Access;
use graft::restype::ResType;
use graft::sonar::{self, Connection};
use graft::state::AppState;
use http::header::HeaderName;
use percent_encoding::percent_decode_str;
use rand::Rng;
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::fmt;
use std::io;
use std::time::SystemTime;

/// Path for static files
const STATIC_PATH: &str = "/var/www/html/iris/api";

/// Authentication information
#[derive(Debug, Deserialize, Serialize)]
struct AuthMap {
    /// Sonar username
    username: String,
    /// Sonar password
    password: String,
}

/// Graft error
#[derive(Debug, thiserror::Error)]
enum Error {
    /// Unauthorized request
    #[error("Unauthorized")]
    Unauthorized,

    /// Sonar error
    #[error("Sonar {0}")]
    Sonar(#[from] graft::sonar::Error),
}

impl From<Error> for StatusCode {
    fn from(e: Error) -> Self {
        match e {
            Error::Unauthorized => StatusCode::UNAUTHORIZED,
            Error::Sonar(e) => e.into(),
        }
    }
}

/// Graft result
type Result<T> = std::result::Result<T, Error>;

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

/// Sonar resource
#[derive(Debug)]
struct Resource {
    res_type: ResType,
    obj_n: Option<String>,
}

impl fmt::Display for Resource {
    fn fmt(
        &self,
        f: &mut fmt::Formatter<'_>,
    ) -> std::result::Result<(), fmt::Error> {
        let type_n = self.res_type.type_n();
        match self.obj_n {
            Some(obj_n) => write!(f, "{type_n}/{obj_n}"),
            None => write!(f, "{type_n}"),
        }
    }
}

impl Resource {
    /// Create a resource from a type
    const fn from_type(res_type: ResType) -> Self {
        Resource {
            res_type,
            obj_n: None,
        }
    }

    /// Create a new resource
    fn new(type_n: &str) -> Result<Self> {
        let res_type = ResType::try_from(type_n)?;
        Ok(Resource {
            res_type,
            obj_n: None,
        })
    }

    /// Add object name
    fn obj(mut self, obj_n: String) -> Self {
        self.obj_n = Some(obj_n);
        self
    }

    /// Get object name
    fn obj_name(&self) -> Result<String> {
        let name = percent_decode_str(self.obj_n)
            .decode_utf8()
            .or(Err(sonar::Error::InvalidValue))?;
        if name.len() > 64 || name.contains(invalid_char) || name.contains('/')
        {
            Err(sonar::Error::InvalidValue)?
        } else {
            Ok(name.to_string())
        }
    }

    /// Make a Sonar name (with validation)
    fn make_name(&self, nm: &str) -> Result<String> {
        if nm.len() > 64 || nm.contains(invalid_char) || nm.contains('/') {
            Err(sonar::Error::InvalidValue)?
        } else {
            Ok(format!("{}/{nm}", self.res_type.type_n()))
        }
    }

    /// Lookup authorized access for a resource
    async fn auth_access(
        &self,
        state: &AppState,
        access: Access,
    ) -> Result<Access> {
        let name = self.obj_name()?;
        let session = state.session();
        let auth: AuthMap = session.get("auth").ok_or(Error::Unauthorized)?;
        let perm = state
            .permission_user_res(&auth.username, self.res_type.type_n(), name)
            .await?;
        let acc = Access::new(perm.access_n).ok_or(Error::Unauthorized)?;
        acc.check(access)?;
        Ok(acc)
    }
}

/// Main entry point
#[tokio::main]
async fn main() -> Result<()> {
    env_logger::builder().format_timestamp(None).init();
    let state = AppState::new().await?;
    /*
    FIXME
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
    app.listen("127.0.0.1:3737").await?;
    Ok(())
}

impl AuthMap {
    /// Authenticate with IRIS server
    async fn authenticate(&self) -> Result<Connection> {
        let mut c = Connection::new(HOST, 1037).await?;
        c.login(&self.username, &self.password).await?;
        Ok(c)
    }
}

/// Handle `POST` to login page
fn login_post(state: AppState) -> Router {
    async fn handler(
        State(state): State<AppState>,
        Json(auth): Json<AuthMap>,
    ) -> Resp1 {
        log::info!("POST /login");
        auth.authenticate().await?;
        let session = state.session_mut();
        // serialization error should never happen; unwrap OK
        session.insert("auth", auth)?;
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
        let auth: AuthMap = session.get("auth").ok_or(Error::Unauthorized)?;
        let perms = state.permissions_user(&auth.username).await?;
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
        Json(obj): Json<Map<String, Value>>,
    ) -> Resp0 {
        let res = Resource::from_type(ResType::Permission);
        log::info!("POST {res}");
        res.auth_access(&state, Access::Configure).await?;
        let role = obj.get("role");
        let resource_n = obj.get("resource_n");
        if let (Some(Value::String(role)), Some(Value::String(resource_n))) =
            (role, resource_n)
        {
            let role = role.to_string();
            let resource_n = resource_n.to_string();
            state.permission_post(&role, &resource_n).await;
            return Ok(StatusCode::CREATED);
        }
        Err(graft::sonar::Error::InvalidValue)?
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
        let res = Resource::from_type(ResType::Permission).obj(id.to_string());
        log::info!("GET {res}");
        res.auth_access(&state, Access::View).await?;
        let perm = state.permission(id).await?;
        match serde_json::to_value(perm) {
            Ok(body) => json_resp(body),
            Err(e) => Err(StatusCode::BAD_REQUEST),
        }
    }

    /// Handle `PATCH` request
    async fn handle_patch(
        AxumPath(id): AxumPath<i32>,
        State(state): State<AppState>,
        Json(obj): Json<Map<String, Value>>,
    ) -> Resp0 {
        let res = Resource::from_type(ResType::Permission).obj(id.to_string());
        log::info!("PATCH {res}");
        res.auth_access(&state, Access::Configure).await?;
        state.permission_patch(id, obj).await?;
        Ok(StatusCode::NO_CONTENT)
    }

    /// Handle `DELETE` request
    async fn handle_delete(
        AxumPath(id): AxumPath<i32>,
        State(state): State<AppState>,
    ) -> Resp0 {
        let res = Resource::from_type(ResType::Permission).obj(id.to_string());
        log::info!("DELETE {res}");
        res.auth_access(&state, Access::Configure).await?;
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
        let res = Resource::new(type_n)?.obj(obj_n);
        log::info!("GET {res}");
        res.auth_access(&state, Access::View).await?;
        let sql = res.res_type.sql_query();
        let name = res.obj_name()?;
        let body = if res.res_type == ResType::ControllerIo {
            state.get_array_by_pkey(sql, &name).await
        } else {
            state.get_by_pkey(sql, &name).await
        }?;
        match serde_json::to_value(body) {
            Ok(body) => json_resp(body),
            Err(e) => Err(StatusCode::INTERNAL_SERVER_ERROR),
        }
    }
    Router::new()
        .route("/:type_n/:obj_n", get(handler))
        .with_state(state)
}

/// IRIS host name
const HOST: &str = "localhost.localdomain";

/// Create a Sonar connection for a request
async fn connection(state: &AppState) -> Result<Connection> {
    let session = state.session();
    let auth: AuthMap = session.get("auth").ok_or(Error::Unauthorized)?;
    auth.authenticate().await
}

/// Invalid characters for SONAR names
const INVALID_CHARS: &[char] = &['\0', '\u{001e}', '\u{001f}'];

/// Check if a character in a Sonar name is invalid
fn invalid_char(c: char) -> bool {
    INVALID_CHARS.contains(&c)
}

/// Make a Sonar attribute (with validation)
fn make_att(res: &'static str, nm: &str, att: &str) -> Result<String> {
    if att.len() > 64 || att.contains(invalid_char) || att.contains('/') {
        Err(graft::sonar::Error::InvalidValue)?
    } else if res == "controller" && att == "drop_id" {
        Ok(format!("{nm}/drop"))
    } else if res == "sign_message" {
        // sign_message attributes are in snake case
        Ok(format!("{nm}/{att}"))
    } else {
        // most IRIS attributes are in camel case (Java)
        Ok(format!("{nm}/{}", att.to_case(Case::Camel)))
    }
}

/// `GET` a file resource
fn resource_file_get(state: AppState) -> Router {
    async fn handler(
        AxumPath(type_n): AxumPath<&str>,
        State(state): State<AppState>,
    ) -> Resp2 {
        let res = Resource::new(type_n)?;
        log::info!("GET {res}");
        res.auth_access(&state, Access::View).await?;
        let path = PathBuf::from(res.to_string());
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
        Json(obj): Json<Map<String, Value>>,
    ) -> Resp0 {
        let res = Resource::new(type_n)?;
        log::info!("POST {res}");
        res.auth_access(&state, Access::Configure).await?;
        match obj.get("name") {
            Some(Value::String(name)) => {
                let nm = res.make_name(name)?;
                let mut c = connection(&state).await?;
                // first, set attributes on phantom object
                for (key, value) in obj.iter() {
                    let key = &key[..];
                    if key != "name" {
                        let anm = make_att(res, &nm, key)?;
                        let value = att_value(value)?;
                        log::debug!("{anm} = {value} (phantom)");
                        c.update_object(&anm, &value).await?;
                    }
                }
                log::debug!("creating {nm}");
                c.create_object(&nm).await?;
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
        Json(obj): Json<Map<String, Value>>,
    ) -> Resp0 {
        let res = Resource::new(type_n)?.obj(obj_n);
        log::info!("PATCH {res}");
        // *At least* Operate access needed (further checks below)
        let access = res.auth_access(&state, Access::Operate).await?;
        for key in obj.keys() {
            let key = &key[..];
            access.check(res.res_type.access_attr(key))?;
        }
        let mut c = connection(&state).await?;
        let name = res.obj_name()?;
        let nm = format!("{res}/{name}");
        // first pass
        for (key, value) in obj.iter() {
            let key = &key[..];
            if res.res_type.patch_first_pass(key) {
                let anm = make_att(res, &nm, key)?;
                let value = att_value(value)?;
                log::debug!("{anm} = {value}");
                c.update_object(&anm, &value).await?;
            }
        }
        // second pass
        for (key, value) in obj.iter() {
            let key = &key[..];
            if !res.res_type.patch_first_pass(key) {
                let anm = make_att(res, &nm, key)?;
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
        let res = Resource::new(type_n)?.obj(obj_n);
        log::info!("DELETE {res}");
        res.auth_access(&state, Access::Configure).await?;
        let name = res.obj_name()?;
        let nm = res.to_string();
        let mut c = connection(&state).await?;
        c.remove_object(&nm).await?;
        Ok(StatusCode::ACCEPTED)
    }
    Router::new()
        .route("/:type_n/:obj_n", delete(handler))
        .with_state(state)
}
