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

/// Resource type
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
enum ResType {
    Alarm,
    Beacon,
    CabinetStyle,
    Camera,
    CommConfig,
    CommLink,
    Controller,
    ControllerIo,
    Detector,
    Dms,
    FlowStream,
    Font,
    GateArm,
    GateArmArray,
    GeoLoc,
    Gps,
    Graphic,
    LaneMarking,
    Lcs,
    LcsArray,
    LcsIndication,
    Modem,
    MsgLine,
    MsgPattern,
    Permission,
    RampMeter,
    Role,
    SignConfig,
    SignDetail,
    SignMessage,
    TagReader,
    User,
    VideoMonitor,
    WeatherSensor,
    Word,
}

impl ResType {
    /// Get resource type name
    const fn type_n(self) -> &'static str {
        use ResType::*;
        match self {
            Alarm => "alarm",
            Beacon => "beacon",
            CabinetStyle => "cabinet_style",
            Camera => "camera",
            CommConfig => "comm_config",
            CommLink => "comm_link",
            Controller => "controller",
            ControllerIo => "controller_io",
            Detector => "detector",
            Dms => "dms",
            FlowStream => "flow_stream",
            Font => "font",
            GateArm => "gate_arm",
            GateArmArray => "gate_arm_array",
            GeoLoc => "geo_loc",
            Gps => "gps",
            Graphic => "graphic",
            LaneMarking => "lane_marking",
            Lcs => "lcs",
            LcsArray => "lcs_array",
            LcsIndication => "lcs_indication",
            Modem => "modem",
            MsgLine => "msg_line",
            MsgPattern => "msg_pattern",
            Permission => "permission",
            RampMeter => "ramp_meter",
            Role => "role",
            SignConfig => "sign_config",
            SignDetail => "sign_detail",
            SignMessage => "sign_message",
            TagReader => "tag_reader",
            User => "user",
            VideoMonitor => "video_monitor",
            WeatherSensor => "weather_sensor",
            Word => "word",
        }
    }

    /// Get verifier resource type
    fn verifier(&self) -> Self {
        use ResType::*;
        match self {
            // Camera resources
            FlowStream => Camera,
            // DMS resources
            Font | Graphic | MsgLine | MsgPattern | SignConfig | SignDetail
            | SignMessage | Word => Dms,
            // Gate arm resources
            GateArmArray => GateArm,
            // LCS resources
            LcsArray | LcsIndication | LaneMarking => Lcs,
            // associated controller
            ControllerIo => Controller,
            _ => self,
        }
    }

    /// Get SQL query string
    fn sql_query(self) -> &'static str {
        use ResType::*;
        match self {
            Alarm => query::ALARM,
            Beacon => query::BEACON,
            CabinetStyle => query::CABINET_STYLE,
            Camera => query::CAMERA,
            CommConfig => query::COMM_CONFIG,
            CommLink => query::COMM_LINK,
            Controller => query::CONTROLLER,
            ControllerIo => query::CONTROLLER_IO,
            Detector => query::DETECTOR,
            Dms => query::DMS,
            FlowStream => query::FLOW_STREAM,
            Font => query::FONT,
            GateArm => query::GATE_ARM,
            GateArmArray => query::GATE_ARM_ARRAY,
            GeoLoc => query::GEO_LOC,
            Gps => query::GPS,
            Graphic => query::GRAPHIC,
            LaneMarking => query::LANE_MARKING,
            Lcs => "", // FIXME
            LcsArray => query::LCS_ARRAY,
            LcsIndication => query::LCS_INDICATION,
            Modem => query::MODEM,
            MsgLine => query::MSG_LINE,
            MsgPattern => query::MSG_PATTERN,
            Permission => query::PERMISSION,
            RampMeter => query::RAMP_METER,
            Role => query::ROLE,
            SignConfig => query::SIGN_CONFIG,
            SignDetail => query::SIGN_DETAIL,
            SignMessage => query::SIGN_MSG,
            TagReader => query::TAG_READER,
            User => query::USER,
            VideoMonitor => query::VIDEO_MONITOR,
            WeatherSensor => query::WEATHER_SENSOR,
            Word => query::WORD,
        }
    }

    /// Get required access to update an attribute
    fn access_attr(self, att: &str) -> Access {
        use ResType::*;
        match (self, att) {
            (Beacon, "flashing")
            | (Camera, "ptz")
            | (Camera, "recall_preset")
            | (Controller, "download")
            | (Controller, "device_req")
            | (Detector, "field_length")
            | (Detector, "force_fail")
            | (Dms, "msg_user")
            | (LaneMarking, "deployed") => Access::Operate,
            (Beacon, "message")
            | (Beacon, "notes")
            | (Beacon, "preset")
            | (Camera, "store_preset")
            | (CommConfig, "timeout_ms")
            | (CommConfig, "idle_disconnect_sec")
            | (CommConfig, "no_response_disconnect_sec")
            | (CommLink, "poll_enabled")
            | (Controller, "condition")
            | (Controller, "notes")
            | (Detector, "abandoned")
            | (Detector, "notes")
            | (Dms, "device_req")
            | (LaneMarking, "notes")
            | (Modem, "enabled")
            | (Modem, "timeout_ms")
            | (Role, "enabled")
            | (User, "enabled")
            | (WeatherSensor, "site_id")
            | (WeatherSensor, "alt_id")
            | (WeatherSensor, "notes") => Access::Manage,
            _ => Access::Configure,
        }
    }
}

/// Graft error
#[derive(Debug, thiserror::Error)]
enum Error {
    /// Unauthorized request
    #[error("Unauthorized")]
    Unauthorized,

    /// Resource not modified
    #[error("Not modified")]
    NotModified,

    /// Sonar error
    #[error("Sonar {0}")]
    Sonar(#[from] graft::sonar::Error),
}

impl From<Error> for StatusCode {
    fn from(e: Error) -> Self {
        match e {
            Error::Unauthorized => StatusCode::UNAUTHORIZED,
            Error::NotModified => StatusCode::NOT_MODIFIED,
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
    std::result::Result<((HeaderName, &'static str), String), StatusCode>;

fn html_resp(html: &str) -> Resp1 {
    Ok(((header::CONTENT_TYPE, "text/html"), html.to_string()))
}

fn json_resp(json: Value) -> Resp1 {
    Ok(((header::CONTENT_TYPE, "application/json"), json.to_string()))
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
        match self.obj_n {
            Some(obj_n) => write!(f, "{}/{obj_n}", self.res_type),
            None => write!(f, "{}", self.res_type),
        }
    }
}

impl Resource {
    /// Create a resource from a type
    const fn from_type(res_type: ResType) -> Self {
        Resource { res_type }
    }

    /// Create a new resource
    fn new(type_n: String) -> Result<Self> {
        let res_type = ResType::try_from(type_n)
            .map_err(|_e| sonar::Error::InvalidValue)?;
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
            Ok(format!("{}/{nm}", self.res_type.type_name()))
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
            .permission_user_res(&auth.username, self.type_name(), name)
            .await?;
        let acc = Access::new(perm.access_n).ok_or(Error::Unauthorized)?;
        acc.check(access)?;
        Ok(acc)
    }
}

impl Access {
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
            Err(graft::sonar::Error::Forbidden)?
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
        .merge(permission_post(state.clone()))
        .route(
            "/permission/:id",
            get(permission_get)
                .patch(permission_patch)
                .delete(permission_delete),
        )
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
fn login_post() -> Router {
    async fn handler(
        Json(auth): Json<AuthMap>,
        State(state): State<AppState>,
    ) -> Resp1 {
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
    Router::new().route("/login", post(handler))
}

/// `POST` one permission record
fn permission_post(state: AppState) -> Router {
    async fn handler(
        Json(obj): Json<Map<String, Value>>,
        State(state): State<AppState>,
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

/// `GET` one permission record
async fn permission_get(
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

/// `PATCH` one permission record
async fn permission_patch(
    AxumPath(id): AxumPath<i32>,
    Json(obj): Json<Map<String, Value>>,
    State(state): State<AppState>,
) -> Resp0 {
    let res = Resource::from_type(ResType::Permission).obj(id.to_string());
    log::info!("PATCH {res}");
    res.auth_access(&state, Access::Configure).await?;
    state.permission_patch(id, obj).await?;
    Ok(StatusCode::NO_CONTENT)
}

/// `DELETE` one permission record
async fn permission_delete(
    AxumPath(id): AxumPath<i32>,
    State(state): State<AppState>,
) -> Resp0 {
    let res = Resource::from_type(ResType::Permission).obj(id.to_string());
    log::info!("DELETE {res}");
    res.auth_access(&state, Access::Configure).await?;
    state.permission_delete(id).await?;
    Ok(StatusCode::NO_CONTENT)
}

/// `GET` one SQL record as JSON
fn sql_record_get(state: AppState) -> Router {
    async fn handler(
        AxumPath((type_n, obj_n)): AxumPath<(String, String)>,
        State(state): State<AppState>,
    ) -> Resp1 {
        let res = Resource::new(type_n)?.obj(obj_n);
        log::info!("GET {res}");
        res.auth_access(&state, Access::View).await?;
        let sql = res.sql_query();
        let name = res.obj_name()?;
        let body = if res.res_type == ResType::ControllerIo {
            state.get_array_by_pkey(sql, &name).await
        } else {
            state.get_by_pkey(sql, &name).await
        };
        json_resp(body)
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
        AxumPath(type_n): AxumPath<String>,
        State(state): State<AppState>,
    ) -> impl IntoResponse {
        let res = Resource::new(type_n)?;
        log::info!("GET {res}");
        res.auth_access(&state, Access::View).await?;
        let path = PathBuf::from(res.to_string());
        let etag = resource_etag(&path).await?;
        if let Some(values) = req.header("If-None-Match") {
            if values.iter().any(|v| v == &etag) {
                return Err(sonar::Error::NotModified);
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
        AxumPath(type_n): AxumPath<String>,
        Json(obj): Json<Map<String, Value>>,
        State(state): State<AppState>,
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
            _ => Err(sonar::Error::InvalidValue),
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
        AxumPath((type_n, obj_n)): AxumPath<(String, String)>,
        Json(obj): Json<Map<String, Value>>,
        State(state): State<AppState>,
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
        Ok(StatusCode::NO_CONTENT)
    }
    Router::new()
        .route("/:type_n/:obj_n", patch(handler))
        .with_state(state)
}

/// Remove a Sonar object from a `DELETE` request
fn sonar_object_delete(state: AppState) -> Router {
    async fn handler(
        AxumPath((type_n, obj_n)): AxumPath<(String, String)>,
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
