// graft.rs
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
#![forbid(unsafe_code)]

use async_std::fs::metadata;
use async_std::path::{Path, PathBuf};
use async_std::task::spawn_blocking;
use convert_case::{Case, Casing};
use core::time::Duration;
use graft::query;
use graft::sonar::{Connection, Result, SonarError};
use graft::state::State;
use percent_encoding::percent_decode_str;
use rand::Rng;
use serde_json::map::Map;
use serde_json::Value;
use std::io;
use std::time::SystemTime;
use tide::convert::DeserializeOwned;
use tide::prelude::*;
use tide::sessions::{MemoryStore, SessionMiddleware};
use tide::{Body, Request, Response, StatusCode};

/// Path for static files
const STATIC_PATH: &str = "/var/www/html/iris/api";

/// Slice of (type/attribute) tuples requiring Operate or higher permission
const OPERATE: &[(&str, &str)] = &[
    ("beacon", "flashing"),
    ("controller", "download"),
    ("controller", "device_req"),
    ("detector", "field_length"),
    ("detector", "force_fail"),
    ("lane_marking", "deployed"),
];

/// Slice of (type/attribute) tuples requiring Plan or higher permission
const PLAN: &[(&str, &str)] = &[
    ("beacon", "message"),
    ("beacon", "notes"),
    ("beacon", "preset"),
    ("comm_config", "timeout_ms"),
    ("comm_config", "idle_disconnect_sec"),
    ("comm_config", "no_response_disconnect_sec"),
    ("comm_link", "poll_enabled"),
    ("controller", "condition"),
    ("controller", "notes"),
    ("detector", "abandoned"),
    ("detector", "notes"),
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
    ("lane_marking", "pin"),
    ("ramp_meter", "pin"),
    ("weather_sensor", "pin"),
];

/// Access for permission records
#[derive(Clone, Copy, Debug)]
pub enum Access {
    View,
    Operate,
    Plan,
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

/// Convert a Sonar result to a Response
macro_rules! resp {
    ($rslt:expr) => {
        match $rslt {
            Ok(r) => r,
            Err(e) => {
                log::warn!("response: {e:?}");
                return Ok(Response::builder(e.status_code())
                    .body(e.to_string())
                    .build());
            }
        }
    };
}

/// Add `POST` / `GET` / `PATCH` / `DELETE` routes for a Sonar object type
macro_rules! add_routes {
    ($app:expr, $res:expr, $sql:expr) => {
        $app.at(concat!("/", $res))
            .get(|req| resource_get($res, req))
            .post(|req| sonar_object_post($res, req));
        $app.at(concat!("/", $res, "/:name"))
            .get(|req| sql_get($res, $sql, req))
            .patch(|req| sonar_object_patch($res, req))
            .delete(|req| sonar_object_delete($res, req));
    };
}

/// Lookup authorized access for a resource
fn auth_access(res: &'static str, req: &Request<State>) -> Result<Access> {
    let session = req.session();
    let auth: AuthMap = session.get("auth").ok_or(SonarError::Unauthorized)?;
    let perm = req.state().permission_user_res(&auth.username, res)?;
    Access::new(perm.access_n).ok_or(SonarError::Unauthorized)
}

impl Access {
    /// Get required access from type/att pair
    fn from_type_key(tp_att: &(&'static str, &str)) -> Self {
        if OPERATE.contains(tp_att) {
            Access::Operate
        } else if PLAN.contains(tp_att) {
            Access::Plan
        } else {
            Access::Configure
        }
    }

    /// Create access from level
    const fn new(level: i32) -> Option<Self> {
        match level {
            1 => Some(Self::View),
            2 => Some(Self::Operate),
            3 => Some(Self::Plan),
            4 => Some(Self::Configure),
            _ => None,
        }
    }

    /// Get access level
    const fn level(self) -> i32 {
        match self {
            Access::View => 1,
            Access::Operate => 2,
            Access::Plan => 3,
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
#[async_std::main]
async fn main() -> tide::Result<()> {
    env_logger::builder().format_timestamp(None).init();
    let state = State::new()?;
    let mut app = tide::with_state(state);
    app.with(
        SessionMiddleware::new(
            MemoryStore::new(),
            &rand::thread_rng().gen::<[u8; 32]>(),
        )
        .with_cookie_name("graft")
        .with_session_ttl(Some(Duration::from_secs(8 * 60 * 60))),
    );
    let mut route = app.at("/iris/api");
    route.at("/login").post(login_post);
    route.at("/access").get(access_get);
    add_routes!(route, "alarm", query::ALARM);
    add_routes!(route, "beacon", query::BEACON);
    add_routes!(route, "cabinet_style", query::CABINET_STYLE);
    add_routes!(route, "camera", query::CAMERA);
    add_routes!(route, "comm_config", query::COMM_CONFIG);
    add_routes!(route, "comm_link", query::COMM_LINK);
    add_routes!(route, "controller", query::CONTROLLER);
    route
        .at("/controller_io/:name")
        .get(|req| sql_get_array("controller_io", query::CONTROLLER_IO, req));
    add_routes!(route, "detector", query::DETECTOR);
    add_routes!(route, "gate_arm", query::GATE_ARM);
    add_routes!(route, "gate_arm_array", query::GATE_ARM_ARRAY);
    route
        .at("/geo_loc/:name")
        .get(|req| sql_get("geo_loc", query::GEO_LOC, req))
        .patch(|req| sonar_object_patch("geo_loc", req));
    add_routes!(route, "lane_marking", query::LANE_MARKING);
    add_routes!(route, "lcs_indication", query::LCS_INDICATION);
    add_routes!(route, "modem", query::MODEM);
    add_routes!(route, "ramp_meter", query::RAMP_METER);
    route.at("/road").get(|req| resource_get("road", req));
    route
        .at("/permission")
        .get(|req| resource_get("permission", req))
        .post(permission_post);
    route
        .at("/permission/:id")
        .get(permission_get)
        .patch(permission_patch)
        .delete(permission_delete);
    add_routes!(route, "role", query::ROLE);
    add_routes!(route, "tag_reader", query::TAG_READER);
    add_routes!(route, "user", query::USER);
    add_routes!(route, "video_monitor", query::VIDEO_MONITOR);
    add_routes!(route, "weather_sensor", query::WEATHER_SENSOR);
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

/// Get request body as JSON
async fn body_json<T: DeserializeOwned>(req: &mut Request<State>) -> Result<T> {
    req.body_json().await.map_err(|e| {
        log::warn!("body_json: {e:?}");
        SonarError::InvalidJson
    })
}

/// Get request body as JSON object map
async fn body_json_obj(req: &mut Request<State>) -> Result<Map<String, Value>> {
    match body_json::<Value>(req).await? {
        Value::Object(obj) => Ok(obj),
        _ => {
            log::warn!("body_json_obj: Not object");
            Err(SonarError::InvalidJson)
        }
    }
}

/// Handle `POST` to login page
async fn login_post(mut req: Request<State>) -> tide::Result {
    log::info!("POST {}", req.url());
    let auth: AuthMap = resp!(body_json(&mut req).await);
    resp!(auth.authenticate().await);
    let session = req.session_mut();
    // serialization error should never happen; unwrap OK
    session.insert("auth", auth).unwrap();
    Ok(Response::builder(200)
        .body("<html>Authenticated</html>")
        .content_type("text/html;charset=utf-8")
        .build())
}

/// `GET` access permissions
async fn access_get(req: Request<State>) -> tide::Result {
    log::info!("GET {}", req.url());
    let body = resp!(access_get_json(req).await);
    Ok(Response::builder(StatusCode::Ok)
        .body(body)
        .content_type("application/json")
        .build())
}

/// `GET` access permissions as JSON
async fn access_get_json(req: Request<State>) -> Result<String> {
    let session = req.session();
    let auth: AuthMap = session.get("auth").ok_or(SonarError::Unauthorized)?;
    let perms =
        spawn_blocking(move || req.state().permissions_user(&auth.username))
            .await?;
    Ok(serde_json::to_value(perms)?.to_string())
}

/// `POST` one permission record
async fn permission_post(req: Request<State>) -> tide::Result {
    log::info!("POST {}", req.url());
    resp!(permission_post2(req).await);
    Ok(Response::builder(StatusCode::Created).build())
}

/// `POST` one permission record
async fn permission_post2(mut req: Request<State>) -> Result<()> {
    auth_access("permission", &req)?.check(Access::Configure)?;
    let obj = body_json_obj(&mut req).await?;
    let role = obj.get("role");
    let resource_n = obj.get("resource_n");
    if let (Some(Value::String(role)), Some(Value::String(resource_n))) =
        (role, resource_n)
    {
        let role = role.to_string();
        let resource_n = resource_n.to_string();
        return spawn_blocking(move || {
            req.state().permission_post(&role, &resource_n)
        })
        .await;
    }
    Err(SonarError::InvalidName)
}

/// `GET` one permission record
async fn permission_get(req: Request<State>) -> tide::Result {
    log::info!("GET {}", req.url());
    let body = resp!(permission_get_json(req).await);
    Ok(Response::builder(StatusCode::Ok)
        .body(body)
        .content_type("application/json")
        .build())
}

/// Get permission record as JSON
async fn permission_get_json(req: Request<State>) -> Result<String> {
    auth_access("permission", &req)?.check(Access::View)?;
    let id = obj_id(&req)?;
    let perm = spawn_blocking(move || req.state().permission(id)).await?;
    Ok(serde_json::to_value(perm)?.to_string())
}

/// `PATCH` one permission record
async fn permission_patch(req: Request<State>) -> tide::Result {
    log::info!("PATCH {}", req.url());
    resp!(permission_patch2(req).await);
    Ok(Response::builder(StatusCode::NoContent).build())
}

/// `PATCH` one permission record
async fn permission_patch2(mut req: Request<State>) -> Result<()> {
    auth_access("permission", &req)?.check(Access::Configure)?;
    let id = obj_id(&req)?;
    let obj = body_json_obj(&mut req).await?;
    return spawn_blocking(move || req.state().permission_patch(id, obj)).await;
}

/// `DELETE` one permission record
async fn permission_delete(req: Request<State>) -> tide::Result {
    log::info!("DELETE {}", req.url());
    resp!(permission_delete2(req).await);
    Ok(Response::builder(StatusCode::NoContent).build())
}

/// `DELETE` one permission record
async fn permission_delete2(req: Request<State>) -> Result<()> {
    auth_access("permission", &req)?.check(Access::Configure)?;
    let id = obj_id(&req)?;
    spawn_blocking(move || req.state().permission_delete(id)).await
}

/// Get object ID from a request
fn obj_id(req: &Request<State>) -> Result<i32> {
    let id = req.param("id").map_err(|_e| SonarError::InvalidName)?;
    id.parse::<i32>().map_err(|_e| SonarError::InvalidName)
}

/// `GET` one SQL record as JSON
async fn sql_get(
    resource_n: &'static str,
    sql: &'static str,
    req: Request<State>,
) -> tide::Result {
    log::info!("GET {}", req.url());
    let body = resp!(sql_get_by_name(resource_n, sql, req).await);
    Ok(Response::builder(StatusCode::Ok)
        .body(body)
        .content_type("application/json")
        .build())
}

/// Get one SQL record by name
async fn sql_get_by_name(
    resource_n: &'static str,
    sql: &'static str,
    req: Request<State>,
) -> Result<String> {
    auth_access(res_name(resource_n), &req)?.check(Access::View)?;
    let name = req_name(&req)?;
    Ok(spawn_blocking(move || req.state().get_by_pkey(sql, &name)).await?)
}

/// `GET` array of SQL records as JSON
async fn sql_get_array(
    resource_n: &'static str,
    sql: &'static str,
    req: Request<State>,
) -> tide::Result {
    log::info!("GET {}", req.url());
    let body = resp!(sql_get_array_by_name(resource_n, sql, req).await);
    Ok(Response::builder(StatusCode::Ok)
        .body(body)
        .content_type("application/json")
        .build())
}

/// Get array of SQL records by name
async fn sql_get_array_by_name(
    resource_n: &'static str,
    sql: &'static str,
    req: Request<State>,
) -> Result<String> {
    auth_access(res_name(resource_n), &req)?.check(Access::View)?;
    let name = req_name(&req)?;
    Ok(
        spawn_blocking(move || req.state().get_array_by_pkey(sql, &name))
            .await?,
    )
}

/// Get name from a request
fn req_name(req: &Request<State>) -> Result<String> {
    let name = req.param("name").map_err(|_e| SonarError::InvalidName)?;
    let name = percent_decode_str(name)
        .decode_utf8()
        .or(Err(SonarError::InvalidName))?;
    if name.len() > 64 || name.contains(invalid_char) || name.contains('/') {
        Err(SonarError::InvalidName)
    } else {
        Ok(name.to_string())
    }
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

/// Make a Sonar name (with validation)
fn make_name(res: &'static str, nm: &str) -> Result<String> {
    if nm.len() > 64 || nm.contains(invalid_char) || nm.contains('/') {
        Err(SonarError::InvalidName)
    } else {
        Ok(format!("{}/{}", res, nm))
    }
}

/// Make a Sonar attribute (with validation)
fn make_att(res: &'static str, nm: &str, att: &str) -> Result<String> {
    if att.len() > 64 || att.contains(invalid_char) || att.contains('/') {
        Err(SonarError::InvalidName)
    } else {
        let att = if res == "controller" && att == "drop_id" {
            "drop".to_string()
        } else {
            att.to_case(Case::Camel)
        };
        Ok(format!("{}/{}", nm, att))
    }
}

/// Get Sonar object name from a request
fn obj_name(res: &'static str, req: &Request<State>) -> Result<String> {
    match req.param("name") {
        Ok(name) => {
            let name = percent_decode_str(name)
                .decode_utf8()
                .or(Err(SonarError::InvalidName))?;
            make_name(res, &name)
        }
        Err(_) => Err(SonarError::InvalidName),
    }
}

/// `GET` a file resource
async fn resource_get(res: &'static str, req: Request<State>) -> tide::Result {
    log::info!("GET {}", req.url());
    let (etag, body) = resp!(resource_get_json(res, req).await);
    Ok(Response::builder(StatusCode::Ok)
        .header("ETag", &etag)
        .body(body)
        .content_type("application/json")
        .build())
}

/// Get a static JSON file
async fn resource_get_json(
    resource_n: &'static str,
    req: Request<State>,
) -> Result<(String, Body)> {
    auth_access(res_name(resource_n), &req)?.check(Access::View)?;
    let path = PathBuf::from(format!("{STATIC_PATH}/{resource_n}"));
    let etag = resource_etag(&path).await?;
    if let Some(values) = req.header("If-None-Match") {
        if values.iter().any(|v| v == &etag) {
            return Err(SonarError::NotModified);
        }
    }
    let body = Body::from_file(&path).await?;
    Ok((etag, body))
}

/// Get associated resource name
fn res_name(resource_n: &'static str) -> &'static str {
    match resource_n {
        "controller_io" => "controller",
        _ => resource_n,
    }
}

/// Get a static file ETag
async fn resource_etag(path: &Path) -> Result<String> {
    let meta = metadata(path).await?;
    let modified = meta.modified()?;
    let dur = modified.duration_since(SystemTime::UNIX_EPOCH)?.as_millis();
    Ok(format!("{dur:x}"))
}

/// Create a Sonar object from a `POST` request
async fn sonar_object_post(
    res: &'static str,
    req: Request<State>,
) -> tide::Result {
    log::info!("POST {}", req.url());
    resp!(sonar_object_post2(res, req).await);
    Ok(Response::builder(StatusCode::Created).build())
}

/// Create a Sonar object from a `POST` request
async fn sonar_object_post2(
    res: &'static str,
    mut req: Request<State>,
) -> Result<()> {
    auth_access(res, &req)?.check(Access::Configure)?;
    let obj = body_json_obj(&mut req).await?;
    match obj.get("name") {
        Some(Value::String(name)) => {
            let nm = make_name(res, name)?;
            let mut c = connection(&req).await?;
            log::debug!("creating {nm}");
            c.create_object(&nm).await?;
            Ok(())
        }
        _ => Err(SonarError::InvalidName),
    }
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
async fn sonar_object_patch(
    res: &'static str,
    req: Request<State>,
) -> tide::Result {
    log::info!("PATCH {}", req.url());
    resp!(sonar_object_patch2(res, req).await);
    Ok(Response::builder(StatusCode::NoContent).build())
}

/// Update a Sonar object from a `PATCH` request
async fn sonar_object_patch2(
    res: &'static str,
    mut req: Request<State>,
) -> Result<()> {
    let nm = obj_name(res, &req)?;
    let access = auth_access(res, &req)?;
    // *At least* Operate access needed (further checks below)
    access.check(Access::Operate)?;
    let obj = body_json_obj(&mut req).await?;
    for key in obj.keys() {
        let key = &key[..];
        access.check(Access::from_type_key(&(res, key)))?;
    }
    let mut c = connection(&req).await?;
    // first pass
    for (key, value) in obj.iter() {
        let key = &key[..];
        if PATCH_FIRST_PASS.contains(&(res, key)) {
            let anm = make_att(res, &nm, key)?;
            let value = att_value(&value)?;
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
    Ok(())
}

/// Remove a Sonar object from a `DELETE` request
async fn sonar_object_delete(
    res: &'static str,
    req: Request<State>,
) -> tide::Result {
    log::info!("DELETE {}", req.url());
    let access = resp!(auth_access(res, &req));
    resp!(access.check(Access::Configure));
    let nm = resp!(obj_name(res, &req));
    let mut c = resp!(connection(&req).await);
    resp!(c.remove_object(&nm).await);
    Ok(Response::builder(StatusCode::Accepted).build())
}
