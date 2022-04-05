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
use graft::sonar::{Connection, Result, SonarError};
use graft::state::{Permission, State};
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
    ($res:expr) => {
        match $res {
            Ok(r) => r,
            Err(e) => {
                log::warn!("response: {:?}", e);
                return Ok(Response::builder(e.status_code())
                    .body(e.to_string())
                    .build());
            }
        }
    };
}

/// Add `POST` / `GET` / `PATCH` / `DELETE` routes for a Sonar object type
macro_rules! add_routes {
    ($app:expr, $tp:expr) => {
        $app.at(concat!("/", $tp))
            .get(|req| resource_get($tp, req))
            .post(|req| sonar_object_post($tp, req));
        $app.at(concat!("/", $tp, "/:name"))
            .patch(|req| sonar_object_patch($tp, req))
            .delete(|req| sonar_object_delete($tp, req));
    };
}

/// Lookup authorized permission for a resource
fn auth_perm(req: &Request<State>, res: &str) -> Result<Permission> {
    let session = req.session();
    let auth: AuthMap = session.get("auth").ok_or(SonarError::Unauthorized)?;
    let perm = req.state().permission_user_res(&auth.username, res)?;
    Ok(perm)
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
    fn check(self, perm: &Permission) -> Result<()> {
        if perm.access_n >= self.level() {
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
    add_routes!(route, "alarm");
    route.at("/alarm/:name").get(|req| {
        sql_get(
            "alarm",
            "SELECT name, description, controller, pin, state, trigger_time \
            FROM iris.alarm \
            WHERE name = $1",
            req,
        )
    });
    add_routes!(route, "beacon");
    route.at("/beacon/:name").get(|req| {
        sql_get(
            "beacon",
            "SELECT b.name, location, controller, pin, verify_pin, geo_loc, \
                    message, notes, preset, flashing \
            FROM iris.beacon b \
            LEFT JOIN geo_loc_view gl ON b.geo_loc = gl.name \
            WHERE b.name = $1",
            req,
        )
    });
    add_routes!(route, "cabinet_style");
    route.at("/cabinet_style/:name").get(|req| {
        sql_get(
            "cabinet_style",
            "SELECT name, police_panel_pin_1, police_panel_pin_2, \
                    watchdog_reset_pin_1, watchdog_reset_pin_2, dip \
            FROM iris.cabinet_style \
            WHERE name = $1",
            req,
        )
    });
    add_routes!(route, "camera");
    route
        .at("/camera/:name")
        .get(|req| {
            sql_get(
                "camera",
                "SELECT c.name, location, geo_loc, controller, pin, notes, \
                        cam_num, publish, streamable, cam_template, \
                        encoder_type, enc_address, enc_port, enc_mcast, \
                        enc_channel, video_loss \
                FROM iris.camera c \
                LEFT JOIN geo_loc_view gl ON c.geo_loc = gl.name \
                WHERE c.name = $1",
                req,
            )
        });
    add_routes!(route, "comm_config");
    route.at("/comm_config/:name").get(|req| {
        sql_get(
            "comm_config",
            "SELECT name, description, protocol, modem, poll_period_sec, \
                    long_poll_period_sec, timeout_ms, idle_disconnect_sec, \
                    no_response_disconnect_sec \
            FROM iris.comm_config \
            WHERE name = $1",
            req,
        )
    });
    add_routes!(route, "comm_link");
    route.at("/comm_link/:name").get(|req| {
        sql_get(
            "comm_link",
            "SELECT name, description, uri, comm_config, poll_enabled, \
                    connected \
            FROM iris.comm_link \
            WHERE name = $1",
            req,
        )
    });
    add_routes!(route, "controller");
    route.at("/controller/:name").get(|req| {
        sql_get(
            "controller",
            "SELECT c.name, location, geo_loc, comm_link, drop_id, \
                    cabinet_style, condition, notes, password, version, \
                    fail_time \
            FROM iris.controller c \
            LEFT JOIN geo_loc_view gl ON c.geo_loc = gl.name \
            WHERE c.name = $1",
            req,
        )
    });
    route.at("/controller_io/:name").get(|req| {
        sql_get_array(
            "controller_io",
            "SELECT pin, name, resource_n \
            FROM iris.controller_io \
            WHERE controller = $1 \
            ORDER BY pin",
            req,
        )
    });
    add_routes!(route, "detector");
    route
        .at("/detector/:name")
        .get(|req| {
            sql_get(
                "detector",
                "SELECT d.name, label, r_node, controller, pin, notes, \
                        lane_code, lane_number, abandoned, fake, field_length, \
                        force_fail, auto_fail \
                FROM iris.detector d \
                LEFT JOIN detector_label_view dl ON d.name = dl.det_id \
                WHERE d.name = $1",
                req,
            )
        });
    add_routes!(route, "lane_marking");
    route.at("/lane_marking/:name").get(|req| {
        sql_get(
            "lane_marking",
            "SELECT m.name, location, geo_loc, controller, pin, notes, \
                    deployed \
            FROM iris.lane_marking m \
            LEFT JOIN geo_loc_view gl ON m.geo_loc = gl.name \
            WHERE m.name = $1",
            req,
        )
    });
    add_routes!(route, "modem");
    route.at("/modem/:name").get(|req| {
        sql_get(
            "modem",
            "SELECT name, uri, config, enabled, timeout_ms \
            FROM iris.modem \
            WHERE name = $1",
            req,
        )
    });
    route
        .at("/geo_loc/:name")
        .get(|req| {
            sql_get(
                "geo_loc",
                "SELECT name, resource_n, roadway, road_dir, cross_street, \
                        cross_dir, cross_mod, landmark, lat, lon \
                FROM iris.geo_loc \
                WHERE name = $1",
                req,
            )
        })
        .patch(|req| sonar_object_patch("geo_loc", req));
    add_routes!(route, "ramp_meter");
    route
        .at("/ramp_meter/:name")
        .get(|req| {
            sql_get(
                "ramp_meter",
                "SELECT m.name, location, geo_loc, controller, pin, notes, \
                        meter_type, beacon, preset, storage, max_wait, \
                        algorithm, am_target, pm_target, m_lock \
                FROM iris.ramp_meter m \
                LEFT JOIN geo_loc_view gl ON m.geo_loc = gl.name \
                WHERE m.name = $1",
                req,
            )
        });
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
    add_routes!(route, "role");
    route.at("/role/:name").get(|req| {
        sql_get(
            "role",
            "SELECT name, enabled FROM iris.role WHERE name = $1",
            req,
        )
    });
    add_routes!(route, "tag_reader");
    route
        .at("/tag_reader/:name")
        .get(|req| {
            sql_get(
                "tag_reader",
                "SELECT t.name, location, geo_loc, controller, pin, notes, \
                        toll_zone \
                FROM iris.tag_reader t \
                LEFT JOIN geo_loc_view gl ON t.geo_loc = gl.name \
                WHERE t.name = $1",
                req,
            )
        });
    add_routes!(route, "user");
    route.at("/user/:name").get(|req| {
        sql_get(
            "user",
            "SELECT name, full_name, role, enabled \
            FROM iris.i_user \
            WHERE name = $1",
            req,
        )
    });
    add_routes!(route, "video_monitor");
    route
        .at("/video_monitor/:name")
        .get(|req| {
            sql_get(
                "video_monitor",
                "SELECT name, mon_num, controller, pin, notes, restricted, \
                        monitor_style, camera \
                FROM iris.video_monitor \
                WHERE name = $1",
                req,
            )
        });
    add_routes!(route, "weather_sensor");
    route.at("/weather_sensor/:name").get(|req| {
        sql_get(
            "weather_sensor",
            "SELECT ws.name, location, geo_loc, controller, pin, site_id, \
                    alt_id, notes, settings, sample, sample_time \
            FROM iris.weather_sensor ws \
            LEFT JOIN geo_loc_view gl ON ws.geo_loc = gl.name \
            WHERE ws.name = $1",
            req,
        )
    });
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
    Access::Configure.check(&auth_perm(&req, "permission")?)?;
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
    Access::View.check(&auth_perm(&req, "permission")?)?;
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
    Access::Configure.check(&auth_perm(&req, "permission")?)?;
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
    Access::Configure.check(&auth_perm(&req, "permission")?)?;
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
    Access::View.check(&auth_perm(&req, resource_tp(resource_n))?)?;
    let name = resource_name(&req)?;
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
    Access::View.check(&auth_perm(&req, resource_tp(resource_n))?)?;
    let name = resource_name(&req)?;
    Ok(
        spawn_blocking(move || req.state().get_array_by_pkey(sql, &name))
            .await?,
    )
}

/// Get resource name from a request
fn resource_name(req: &Request<State>) -> Result<String> {
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
fn make_name(tp: &'static str, nm: &str) -> Result<String> {
    if nm.len() > 64 || nm.contains(invalid_char) || nm.contains('/') {
        Err(SonarError::InvalidName)
    } else {
        Ok(format!("{}/{}", tp, nm))
    }
}

/// Make a Sonar attribute (with validation)
fn make_att(tp: &'static str, nm: &str, att: &str) -> Result<String> {
    if att.len() > 64 || att.contains(invalid_char) || att.contains('/') {
        Err(SonarError::InvalidName)
    } else {
        let att = if tp == "controller" && att == "drop_id" {
            "drop".to_string()
        } else {
            att.to_case(Case::Camel)
        };
        Ok(format!("{}/{}", nm, att))
    }
}

/// Get Sonar object name from a request
fn obj_name(tp: &'static str, req: &Request<State>) -> Result<String> {
    match req.param("name") {
        Ok(name) => {
            let name = percent_decode_str(name)
                .decode_utf8()
                .or(Err(SonarError::InvalidName))?;
            make_name(tp, &name)
        }
        Err(_) => Err(SonarError::InvalidName),
    }
}

/// `GET` a file resource
async fn resource_get(tp: &'static str, req: Request<State>) -> tide::Result {
    log::info!("GET {}", req.url());
    let (etag, body) = resp!(resource_get_json(tp, req).await);
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
    Access::View.check(&auth_perm(&req, resource_tp(resource_n))?)?;
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

/// Get "main" associated resource type
fn resource_tp(resource_n: &'static str) -> &'static str {
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
    tp: &'static str,
    req: Request<State>,
) -> tide::Result {
    log::info!("POST {}", req.url());
    resp!(sonar_object_post2(tp, req).await);
    Ok(Response::builder(StatusCode::Created).build())
}

/// Create a Sonar object from a `POST` request
async fn sonar_object_post2(
    tp: &'static str,
    mut req: Request<State>,
) -> Result<()> {
    Access::Configure.check(&auth_perm(&req, tp)?)?;
    let obj = body_json_obj(&mut req).await?;
    match obj.get("name") {
        Some(Value::String(name)) => {
            let nm = make_name(tp, name)?;
            let mut c = connection(&req).await?;
            log::debug!("creating {}", nm);
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
    tp: &'static str,
    req: Request<State>,
) -> tide::Result {
    log::info!("PATCH {}", req.url());
    resp!(sonar_object_patch2(tp, req).await);
    Ok(Response::builder(StatusCode::NoContent).build())
}

/// Update a Sonar object from a `PATCH` request
async fn sonar_object_patch2(
    tp: &'static str,
    mut req: Request<State>,
) -> Result<()> {
    let perm = auth_perm(&req, tp)?;
    // *At least* Operate access needed (further checks below)
    Access::Operate.check(&perm)?;
    let obj = body_json_obj(&mut req).await?;
    let nm = obj_name(tp, &req)?;
    for key in obj.keys() {
        let key = &key[..];
        Access::from_type_key(&(tp, key)).check(&perm)?;
    }
    let mut c = connection(&req).await?;
    // first pass
    for (key, value) in obj.iter() {
        let key = &key[..];
        if PATCH_FIRST_PASS.contains(&(tp, key)) {
            let anm = make_att(tp, &nm, key)?;
            let value = att_value(&value)?;
            log::debug!("{} = {}", anm, &value);
            c.update_object(&anm, &value).await?;
        }
    }
    // second pass
    for (key, value) in obj.iter() {
        let key = &key[..];
        if !PATCH_FIRST_PASS.contains(&(tp, key)) {
            let anm = make_att(tp, &nm, key)?;
            let value = att_value(value)?;
            log::debug!("{} = {}", anm, &value);
            c.update_object(&anm, &value).await?;
        }
    }
    Ok(())
}

/// Remove a Sonar object from a `DELETE` request
async fn sonar_object_delete(
    tp: &'static str,
    req: Request<State>,
) -> tide::Result {
    log::info!("DELETE {}", req.url());
    let perm = resp!(auth_perm(&req, tp));
    resp!(Access::Configure.check(&perm));
    let nm = resp!(obj_name(tp, &req));
    let mut c = resp!(connection(&req).await);
    resp!(c.remove_object(&nm).await);
    Ok(Response::builder(StatusCode::Accepted).build())
}
