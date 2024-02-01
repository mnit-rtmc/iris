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
use graft::query;
use graft::sonar::{Connection, Result, Error as SonarError};
use graft::state::AppState;
use http::header::HeaderName;
use percent_encoding::percent_decode_str;
use rand::Rng;
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::io;
use std::time::SystemTime;

/// Path for static files
const STATIC_PATH: &str = "/var/www/html/iris/api";

/// Slice of (type/attribute) tuples requiring Operate or higher permission
const OPERATE: &[(&str, &str)] = &[
    ("beacon", "flashing"),
    ("camera", "ptz"),
    ("camera", "recall_preset"),
    ("controller", "download"),
    ("controller", "device_req"),
    ("detector", "field_length"),
    ("detector", "force_fail"),
    ("dms", "msg_user"),
    ("lane_marking", "deployed"),
];

/// Slice of (type/attribute) tuples requiring Manage or higher permission
const MANAGE: &[(&str, &str)] = &[
    ("beacon", "message"),
    ("beacon", "notes"),
    ("beacon", "preset"),
    ("camera", "store_preset"),
    ("comm_config", "timeout_ms"),
    ("comm_config", "idle_disconnect_sec"),
    ("comm_config", "no_response_disconnect_sec"),
    ("comm_link", "poll_enabled"),
    ("controller", "condition"),
    ("controller", "notes"),
    ("detector", "abandoned"),
    ("detector", "notes"),
    ("dms", "device_req"),
    ("lane_marking", "notes"),
    ("modem", "enabled"),
    ("modem", "timeout_ms"),
    ("role", "enabled"),
    ("user", "enabled"),
    ("weather_sensor", "site_id"),
    ("weather_sensor", "alt_id"),
    ("weather_sensor", "notes"),
];

/// Check for type/key pairs in PATCH first pass
const PATCH_FIRST_PASS: &[(&str, &str)] = &[
    ("alarm", "pin"),
    ("beacon", "pin"),
    ("beacon", "verify_pin"),
    ("detector", "pin"),
    ("dms", "pin"),
    ("lane_marking", "pin"),
    ("ramp_meter", "pin"),
    ("weather_sensor", "pin"),
];

/// Access for permission records
#[derive(Clone, Copy, Debug)]
pub enum Access {
    View,
    Operate,
    Manage,
    Configure,
}

/// Authentication information
#[derive(Debug, Deserialize, Serialize)]
struct AuthMap {
    /// Sonar username
    username: String,
    /// Sonar password
    password: String,
}

/// Sonar resource
#[derive(Debug, Deserialize)]
struct Resource {
    type_n: String,
    obj_n: Option<String>,
}

/// Trait to get HTTP status code from an error
trait ErrorStatus {
    fn status_code(&self) -> StatusCode;
}

impl ErrorStatus for io::Error {
    fn status_code(&self) -> StatusCode {
        if self.kind() == io::ErrorKind::NotFound {
            StatusCode::NotFound
        } else if self.kind() == io::ErrorKind::TimedOut {
            StatusCode::GatewayTimeout
        } else {
            StatusCode::InternalServerError
        }
    }
}

impl ErrorStatus for SonarError {
    fn status_code(&self) -> StatusCode {
        match self {
            Self::NotModified => StatusCode::NotModified,
            Self::InvalidName => StatusCode::BadRequest,
            Self::InvalidJson => StatusCode::BadRequest,
            Self::Unauthorized => StatusCode::Unauthorized,
            Self::Forbidden => StatusCode::Forbidden,
            Self::NotFound => StatusCode::NotFound,
            Self::Conflict => StatusCode::Conflict,
            Self::InvalidValue => StatusCode::UnprocessableEntity,
            Self::IO(e) => e.status_code(),
            _ => StatusCode::InternalServerError,
        }
    }
}

/// No-header response result
type Resp0 = std::result::Result<StatusCode, StatusCode>;

/// Single-header response result
type Resp1<'a> = std::result::Result<((HeaderName, &'static str), &'a str), StatusCode>;

fn html_resp(html: &str) -> Resp1 {
    Ok((
        (header::CONTENT_TYPE, "text/html"),
        html,
    ))
}

fn json_resp(json: &str) -> Resp1 {
    Ok((
        (header::CONTENT_TYPE, "application/json"),
        json,
    ))
}


impl std::fmt::Display for Resource {
    
}

impl Resource {
    /// Get associated or dependent type name
    fn type_name(&self) -> &str {
        match self.type_n {
            // Camera dependent resources
            "flow_stream" => "camera",
            // DMS dependent resources
            "font" | "graphic" | "msg_line" | "msg_pattern" | "sign_config"
            | "sign_detail" | "sign_message" | "word" => "dms",
            // Gate arm dependent resources
            "gate_arm_array" => "gate_arm",
            // LCS dependent resources
            "lcs_array" | "lcs_indication" | "lane_marking" => "lcs",
            // associated controller
            "controller_io" => "controller",
            _ => self.type_n,
        }
    }

    /// Get object name
    fn obj_name(&self) -> Result<String> {
        let name = percent_decode_str(self.obj_n)
            .decode_utf8()
            .or(Err(SonarError::InvalidName))?;
        if name.len() > 64 || name.contains(invalid_char) || name.contains('/') {
            Err(SonarError::InvalidName)
        } else {
            Ok(name.to_string())
        }
    }

    /// Make a Sonar name (with validation)
    fn make_name(&self, nm: &str) -> Result<String> {
        if nm.len() > 64 || nm.contains(invalid_char) || nm.contains('/') {
            Err(SonarError::InvalidName)
        } else {
            Ok(format!("{}/{nm}", self.type_n))
        }
    }

    /// Lookup authorized access for a resource
    fn auth_access(&self, state: &AppState) -> Result<Access> {
        let name = self.obj_name()?;
        let session = state.session();
        let auth: AuthMap = session.get("auth").ok_or(SonarError::Unauthorized)?;
        let perm = state.permission_user_res(&auth.username, self.type_name(), name)?;
        Access::new(perm.access_n).ok_or(SonarError::Unauthorized)
    }

    /// Get SQL query string
    fn sql_query(&self) -> &'static str {
        match self.type_n {
            "alarm" => query::ALARM,
            "beacon" => query::BEACON,
            "cabinet_style" => query::CABINET_STYLE,
            "camera" => query::CAMERA,
            "comm_config" => query::COMM_CONFIG,
            "comm_link" => query::COMM_LINK,
            "controller" => query::CONTROLLER,
            "controller_io" => query::CONTROLLER_IO,
            "detector" => query::DETECTOR,
            "dms" => query::DMS,
            "flow_stream" => query::FLOW_STREAM,
            "font" => query::FONT,
            "gate_arm" => query::GATE_ARM,
            "gate_arm_array" => query::GATE_ARM_ARRAY,
            "geo_loc" => query::GEO_LOC,
            "gps" => query::GPS,
            "graphic" => query::GRAPHIC,
            "lane_marking" => query::LANE_MARKING,
            "lcs_array" => query::LCS_ARRAY,
            "lcs_indication" => query::LCS_INDICATION,
            "modem" => query::MODEM,
            "msg_line" => query::MSG_LINE,
            "msg_pattern" => query::MSG_PATTERN,
            "ramp_meter" => query::RAMP_METER,
            "role" => query::ROLE,
            "sign_config" => query::SIGN_CONFIG,
            "sign_detail" => query::SIGN_DETAIL,
            "sign_message" => query::SIGN_MSG,
            "tag_reader" => query::TAG_READER,
            "user" => query::USER,
            "video_monitor" => query::VIDEO_MONITOR,
            "weather_sensor" => query::WEATHER_SENSOR,
            "word" => query::WORD,
        }
    }
}

impl Access {
    /// Get required access from type/att pair
    fn from_type_key(tp_att: &(&'static str, &str)) -> Self {
        if OPERATE.contains(tp_att) {
            Access::Operate
        } else if MANAGE.contains(tp_att) {
            Access::Manage
        } else {
            Access::Configure
        }
    }

    /// Create access from level
    const fn new(level: i32) -> Option<Self> {
        match level {
            1 => Some(Self::View),
            2 => Some(Self::Operate),
            3 => Some(Self::Manage),
            4 => Some(Self::Configure),
            _ => None,
        }
    }

    /// Get access level
    const fn level(self) -> i32 {
        match self {
            Access::View => 1,
            Access::Operate => 2,
            Access::Manage => 3,
            Access::Configure => 4,
        }
    }

    /// Check for access to a resource
    fn check(self, rhs: Self) -> Result<()> {
        if self.level() >= rhs.level() {
            Ok(())
        } else {
            Err(SonarError::Forbidden)
        }
    }
}

/// Main entry point
#[tokio::main]
async fn main() -> Result<()> {
    env_logger::builder().format_timestamp(None).init();
    let state = AppState::new().await?;
    /*
    app.with(
        SessionMiddleware::new(
            MemoryStore::new(),
            &rand::thread_rng().gen::<[u8; 32]>(),
        )
        .with_cookie_name("graft")
        .with_session_ttl(Some(Duration::from_secs(8 * 60 * 60))),
    );*/
    let app = Router::new()
        .merge(login_post())
        .merge(access_get())
        .route("/permission", post(permission_post))
        .route(
            "/permission/:id",
            get(permission_get)
                .patch(permission_patch)
                .delete(permission_delete),
        )
        .merge(resource_file_get(state.clone()))
        .merge(sonar_post)
        .route(
            "/controller_io/:name",
            get(|req| {
                sql_get_array("controller_io", query::CONTROLLER_IO, req)
            }),
        )
        .merge(sql_record_get)
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
fn login_post() -> Router {
    async fn handler<'a>(
        Json(auth): Json<AuthMap>,
        State(state): State<AppState>,
    ) -> Resp1<'a> {
        log::info!("POST /login");
        auth.authenticate().await?;
        let session = state.session_mut();
        // serialization error should never happen; unwrap OK
        session.insert("auth", auth)?;
        html_resp("<html>Authenticated</html>")
    }
    Router::new().route("/login", post(handler))
}

/// `GET` access permissions
fn access_get() -> Router {
    async fn handler<'a>(
        State(state): State<AppState>,
    ) -> Resp1<'a> {
        log::info!("GET /access");
        let session = state.session();
        let auth: AuthMap = session.get("auth").ok_or(SonarError::Unauthorized)?;
        let perms = state.permissions_user(&auth.username).await?;
        let body = serde_json::to_value(perms)?.to_string();
        json_resp(body)
    }
    Router::new().route("/login", post(handler))
}

/// `POST` one permission record
async fn permission_post(
    Json(obj): Json<Map<String, Value>>,
    State(state): State<AppState>,
) -> impl IntoResponse {
    log::info!("POST /permission");
    auth_access("permission", &state, None)?.check(Access::Configure)?;
    let role = obj.get("role");
    let resource_n = obj.get("resource_n");
    if let (Some(Value::String(role)), Some(Value::String(resource_n))) =
        (role, resource_n)
    {
        let role = role.to_string();
        let resource_n = resource_n.to_string();
        return state.permission_post(&role, &resource_n).await;
        //StatusCode::Created
    }
    Err(SonarError::InvalidName)
}

/// `GET` one permission record
async fn permission_get(
    AxumPath(id): AxumPath<i32>,
    State(state): State<AppState>,
) -> impl IntoResponse {
    log::info!("GET /permission/{id}");
    auth_access("permission", &state, None)?.check(Access::View)?;
    let perm = state.permission(id).await?;
    let body = serde_json::to_value(perm)?.to_string()?;
    ((header::CONTENT_TYPE, "application/json"), body)
}

/// `PATCH` one permission record
async fn permission_patch(
    AxumPath(id): AxumPath<i32>,
    Json(obj): Json<Map<String, Value>>,
    State(state): State<AppState>,
) -> impl IntoResponse {
    log::info!("PATCH /permission/{id}");
    auth_access("permission", &req, None)?.check(Access::Configure)?;
    state.permission_patch(id, obj).await?;
    StatusCode::NoContent
}

/// `DELETE` one permission record
async fn permission_delete(
    AxumPath(id): AxumPath<i32>,
    State(state): State<AppState>,
) -> impl IntoResponse {
    log::info!("DELETE /permission/{id}");
    auth_access("permission", &req, None)?.check(Access::Configure)?;
    state.permission_delete(id).await?;
    StatusCode::NoContent
}

/// `GET` one SQL record as JSON
fn sql_record_get() -> Router {
    async fn handler(
        AxumPath(res): AxumPath<Resource>,
        State(state): State<AppState>,
    ) -> impl IntoResponse {
        log::info!("GET {res}");
        res.auth_access(&req)?.check(Access::View)?;
        let sql = res.sql_query();
        let name = res.obj_name()?;
        let body = state.get_by_pkey(sql, &name).await;
        ((header::CONTENT_TYPE, "application/json"), body)
    }
    Router::new().route("/:type_n/:obj_n", get(handler))
}

/// `GET` array of SQL records as JSON
async fn sql_get_array(
    AxumPath(res): AxumPath<Resource>,
    State(state): State<AppState>,
) -> impl IntoResponse {
    log::info!("GET {res}");
    res.auth_access(&state)?.check(Access::View)?;
    let sql = res.sql_query();
    let name = res.obj_name()?;
    let body = state.get_array_by_pkey(sql, &name).await;
    ((header::CONTENT_TYPE, "application/json"), body)
}

/// IRIS host name
const HOST: &str = "localhost.localdomain";

/// Create a Sonar connection for a request
async fn connection(req: &Request<State>) -> Result<Connection> {
    let session = req.session();
    let auth: AuthMap = session.get("auth").ok_or(SonarError::Unauthorized)?;
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
        Err(SonarError::InvalidName)
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
        AxumPath(res): AxumPath<Resource>,
        State(state): State<AppState>,
    ) -> impl IntoResponse {
        log::info!("GET {res}");
        res.auth_access(&state)?.check(Access::View)?;
        let path = PathBuf::from(res.to_string());
        let etag = resource_etag(&path).await?;
        if let Some(values) = req.header("If-None-Match") {
            if values.iter().any(|v| v == &etag) {
                return Err(SonarError::NotModified);
            }
        }
        let body = Body::from_file(&path).await?;
        // FIXME: use tower ServeFile instead (if ETag changed)
        (
            [
                (header::ETAG, etag),
                (header::CONTENT_TYPE, "application/json"),
            ],
            body,
        )
    }
    Router::new().route("/:type_n", get(handler)).with_state(state)
}

/// Get a static file ETag
async fn resource_etag(path: &Path) -> Result<String> {
    let meta = metadata(path).await?;
    let modified = meta.modified()?;
    let dur = modified.duration_since(SystemTime::UNIX_EPOCH)?.as_millis();
    Ok(format!("{dur:x}"))
}

/// Create a Sonar object from a `POST` request
fn sonar_post() -> Router {
    async fn handler(
        AxumPath(res): AxumPath<Resource>,
        Json(obj): Json<Map<String, Value>>,
        State(state): State<AppState>,
    ) -> impl IntoResponse {
        log::info!("POST {res}");
        res.auth_access(&state)?.check(Access::Configure)?;
        match obj.get("name") {
            Some(Value::String(name)) => {
                let nm = res.make_name(name)?;
                let mut c = connection(&req).await?;
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
                StatusCode::Created
            }
            _ => Err(SonarError::InvalidName),
        }
    }
    Router::new().route("/:type_n", post(handler))
}

/// Get Sonar attribute value
fn att_value(value: &Value) -> Result<String> {
    match value {
        Value::String(value) => {
            if value.contains(invalid_char) {
                Err(SonarError::InvalidValue)
            } else {
                Ok(value.to_string())
            }
        }
        Value::Bool(value) => Ok(value.to_string()),
        Value::Number(value) => Ok(value.to_string()),
        Value::Null => Ok("\0".to_string()),
        _ => Err(SonarError::InvalidValue),
    }
}

/// Update a Sonar object from a `PATCH` request
fn sonar_object_patch(state: AppState) -> Router {
    async fn handler(
        AxumPath(res): AxumPath<Resource>,
        Json(obj): Json<Map<String, Value>>,
        State(state): State<AppState>,
    ) -> Resp0 {
        log::info!("PATCH {res}");
        let access = res.auth_access(&state)?;
        // *At least* Operate access needed (further checks below)
        access.check(Access::Operate)?;
        for key in obj.keys() {
            let key = &key[..];
            access.check(Access::from_type_key(&(res, key)))?;
        }
        let mut c = connection(&req).await?;
        let name = res.obj_name()?;
        let nm = format!("{res}/{name}");
        // first pass
        for (key, value) in obj.iter() {
            let key = &key[..];
            if PATCH_FIRST_PASS.contains(&(res, key)) {
                let anm = make_att(res, &nm, key)?;
                let value = att_value(value)?;
                log::debug!("{anm} = {value}");
                c.update_object(&anm, &value).await?;
            }
        }
        // second pass
        for (key, value) in obj.iter() {
            let key = &key[..];
            if !PATCH_FIRST_PASS.contains(&(res, key)) {
                let anm = make_att(res, &nm, key)?;
                let value = att_value(value)?;
                log::debug!("{} = {}", anm, &value);
                c.update_object(&anm, &value).await?;
            }
        }
        Ok(StatusCode::NoContent)
    }
    Router::new().route("/:type_n/:obj_n", patch(handler)).with_state(state)
}

/// Remove a Sonar object from a `DELETE` request
fn sonar_object_delete(state: AppState) -> Router {
    async fn handler(
        AxumPath(res): AxumPath<Resource>,
        State(state): State<AppState>,
    ) -> Resp0 {
        log::info!("DELETE {res}");
        let access = res.auth_access(&state)?;
        access.check(Access::Configure)?;
        let name = res.obj_name()?;
        let nm = res.to_string();
        let mut c = connection(&req).await?;
        c.remove_object(&nm).await?;
        Ok(StatusCode::Accepted)
    }
    Router::new().route("/:type_n/:obj_n", delete(handler)).with_state(state)
}
