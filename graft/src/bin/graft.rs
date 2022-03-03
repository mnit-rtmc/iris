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

use async_std::path::PathBuf;
use async_std::task::spawn_blocking;
use chrono::{Local, TimeZone};
use convert_case::{Case, Casing};
use graft::sonar::{Connection, Result, SonarError};
use graft::state::State;
use percent_encoding::percent_decode_str;
use rand::Rng;
use serde_json::map::Map;
use serde_json::Value;
use std::io;
use tide::prelude::*;
use tide::sessions::{MemoryStore, SessionMiddleware};
use tide::{Body, Request, Response, StatusCode};

/// Path for static files
const STATIC_PATH: &str = "/var/www/html/iris/api";

/// Slice of (type, attribute) tuples for JSON integer values
const INTEGERS: &[(&str, &str)] = &[
    ("alarm", "pin"),
    ("alarm", "styles"),
    ("cabinet_style", "police_panel_pin_1"),
    ("cabinet_style", "police_panel_pin_2"),
    ("cabinet_style", "watchdog_reset_pin_1"),
    ("cabinet_style", "watchdog_reset_pin_2"),
    ("cabinet_style", "dip"),
    ("comm_config", "protocol"),
    ("comm_config", "timeout_ms"),
    ("comm_config", "poll_period_sec"),
    ("comm_config", "long_poll_period_sec"),
    ("comm_config", "idle_disconnect_sec"),
    ("comm_config", "no_response_disconnect_sec"),
    ("controller", "drop_id"),
    ("controller", "condition"),
    ("controller", "controller_err"),
    ("controller", "success_ops"),
    ("modem", "timeout_ms"),
    ("modem", "state"),
];

/// Slice of (type, attribute) tuples for JSON boolean values
const BOOLS: &[(&str, &str)] = &[
    ("alarm", "state"),
    ("comm_config", "modem"),
    ("comm_link", "poll_enabled"),
    ("comm_link", "connected"),
    ("modem", "enabled"),
    ("role", "enabled"),
    ("user", "enabled"),
];

/// Slice of (type, attribute) tuples for RFC 3339 time stamp values
const STAMPS: &[(&str, &str)] = &[
    ("alarm", "trigger_time"),
    ("controller", "fail_time"),
    ("dms", "expire_time"),
];

/// Access for permission records
#[derive(Clone, Copy, Debug)]
#[allow(dead_code)]
pub enum Access {
    View,
    Operate,
    Plan,
    Configure,
}

/// Rename Sonar attributes to DB names
fn rename_sonar_to_db(tp: &str, att: &str) -> String {
    if tp == "controller" && att == "drop" {
        "drop_id".to_string()
    } else {
        att.to_case(Case::Snake)
    }
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
            Self::InvalidName => StatusCode::BadRequest,
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

/// Build response to a bad request
fn bad_request(msg: &str) -> tide::Result {
    log::warn!("bad request: {}", msg);
    Ok(Response::builder(StatusCode::BadRequest).body(msg).build())
}

/// Add `POST` / `GET` / `PATCH` / `DELETE` routes for a Sonar object type
macro_rules! add_routes {
    ($app:expr, $tp:expr) => {
        $app.at(concat!("/", $tp))
            .get(|req| resource_get($tp, req))
            .post(|req| sonar_object_post($tp, req));
        $app.at(concat!("/", $tp, "/:name"))
            .get(|req| sonar_object_get($tp, req))
            .patch(|req| sonar_object_patch($tp, req))
            .delete(|req| sonar_object_delete($tp, req));
    };
}

/// Add `POST` / `PATCH` / `DELETE` routes for a Sonar object type
macro_rules! add_routes_except_get {
    ($app:expr, $tp:expr) => {
        $app.at(concat!("/", $tp))
            .get(|req| resource_get($tp, req))
            .post(|req| sonar_object_post($tp, req));
        $app.at(concat!("/", $tp, "/:name"))
            .patch(|req| sonar_object_patch($tp, req))
            .delete(|req| sonar_object_delete($tp, req));
    };
}

impl Access {
    /// Get access level
    fn level(self) -> i32 {
        match self {
            Access::View => 1,
            Access::Operate => 2,
            Access::Plan => 3,
            Access::Configure => 4,
        }
    }

    /// Check for access to a resource
    fn check(self, nm: &str, req: &Request<State>) -> Result<()> {
        let session = req.session();
        let auth: AuthMap =
            session.get("auth").ok_or(SonarError::Unauthorized)?;
        let perm = req.state().permission_user_res(&auth.username, nm)?;
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
        .with_cookie_name("graft"),
    );
    let mut route = app.at("/iris/api");
    route.at("/login").get(login_get).post(login_post);
    route.at("/access").get(access_get);
    add_routes!(route, "alarm");
    add_routes!(route, "cabinet_style");
    add_routes!(route, "comm_config");
    add_routes!(route, "comm_link");
    add_routes!(route, "controller");
    add_routes!(route, "modem");
    route
        .at("/permission")
        .get(|req| resource_get("permission", req))
        .post(permission_post);
    route
        .at("/permission/:id")
        .get(permission_get)
        .patch(permission_patch)
        .delete(permission_delete);
    // can't use sonar_object_get due to capabilities array
    add_routes_except_get!(route, "role");
    route.at("/role/:name").get(role_get);
    // can't use sonar_object_get due to domains array
    add_routes_except_get!(route, "user");
    route.at("/user/:name").get(user_get);
    app.listen("127.0.0.1:3737").await?;
    Ok(())
}

/// Login form
const LOGIN: &str = r#"<html>
<form method="POST" action="/iris/api/login">
  <label>username
    <input type="text" name="username" autocomplete="username" required>
  </label>
  <label>password
    <input type="password" name="password" autocomplete="current-password" required>
  </label>
  <button type="submit">Login</button>
</form>
</html>"#;

/// `GET` login form
async fn login_get(req: Request<State>) -> tide::Result {
    log::info!("GET {}", req.url());
    Ok(Response::builder(200)
        .body(LOGIN)
        .content_type("text/html;charset=utf-8")
        .build())
}

/// Auth information
#[derive(Debug, Deserialize, Serialize)]
struct AuthMap {
    /// Sonar username
    username: String,
    /// Sonar password
    password: String,
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
async fn login_post(mut req: Request<State>) -> tide::Result {
    log::info!("POST {}", req.url());
    let auth: AuthMap = match req.body_form().await {
        Ok(auth) => auth,
        Err(e) => return bad_request(&e.to_string()),
    };
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
async fn permission_post(mut req: Request<State>) -> tide::Result {
    log::info!("POST {}", req.url());
    let body: Value = req.body_json().await?;
    resp!(permission_post2(req, body).await);
    Ok(Response::builder(StatusCode::Created).build())
}

/// `POST` one permission record
async fn permission_post2(req: Request<State>, body: Value) -> Result<()> {
    Access::Configure.check("permission", &req)?;
    if let Some(obj) = body.as_object() {
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
    Access::View.check("permission", &req)?;
    let id = obj_id(&req)?;
    let perm = spawn_blocking(move || req.state().permission(id)).await?;
    Ok(serde_json::to_value(perm)?.to_string())
}

/// `PATCH` one permission record
async fn permission_patch(mut req: Request<State>) -> tide::Result {
    log::info!("PATCH {}", req.url());
    let body: Value = req.body_json().await?;
    resp!(permission_patch2(req, body).await);
    Ok(Response::builder(StatusCode::NoContent).build())
}

/// `PATCH` one permission record
async fn permission_patch2(req: Request<State>, body: Value) -> Result<()> {
    Access::Configure.check("permission", &req)?;
    let id = obj_id(&req)?;
    if let Value::Object(obj) = body {
        return spawn_blocking(move || req.state().permission_patch(id, obj))
            .await;
    }
    Err(SonarError::InvalidName)
}

/// `DELETE` one permission record
async fn permission_delete(req: Request<State>) -> tide::Result {
    log::info!("DELETE {}", req.url());
    resp!(permission_delete2(req).await);
    Ok(Response::builder(StatusCode::NoContent).build())
}

/// `DELETE` one permission record
async fn permission_delete2(req: Request<State>) -> Result<()> {
    Access::Configure.check("permission", &req)?;
    let id = obj_id(&req)?;
    spawn_blocking(move || req.state().permission_delete(id)).await
}

/// Get object ID from a request
fn obj_id(req: &Request<State>) -> Result<i32> {
    let id = req.param("id").map_err(|_e| SonarError::InvalidName)?;
    id.parse::<i32>().map_err(|_e| SonarError::InvalidName)
}

/// `GET` one role record
async fn role_get(req: Request<State>) -> tide::Result {
    log::info!("GET {}", req.url());
    let body = resp!(role_get_json(req).await);
    Ok(Response::builder(StatusCode::Ok)
        .body(body)
        .content_type("application/json")
        .build())
}

/// Get role record as JSON
async fn role_get_json(req: Request<State>) -> Result<String> {
    Access::View.check("role", &req)?;
    let name = req
        .param("name")
        .map_err(|_e| SonarError::InvalidName)?
        .to_string();
    let role = spawn_blocking(move || req.state().role(&name)).await?;
    Ok(serde_json::to_value(role)?.to_string())
}

/// `GET` one user record
async fn user_get(req: Request<State>) -> tide::Result {
    log::info!("GET {}", req.url());
    let body = resp!(user_get_json(req).await);
    Ok(Response::builder(StatusCode::Ok)
        .body(body)
        .content_type("application/json")
        .build())
}

/// Get user record as JSON
async fn user_get_json(req: Request<State>) -> Result<String> {
    Access::View.check("user", &req)?;
    let name = req
        .param("name")
        .map_err(|_e| SonarError::InvalidName)?
        .to_string();
    let user = spawn_blocking(move || req.state().user(&name)).await?;
    Ok(serde_json::to_value(user)?.to_string())
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
fn make_name(tp: &str, nm: &str) -> Result<String> {
    if nm.len() > 64 || nm.contains(invalid_char) || nm.contains('/') {
        Err(SonarError::InvalidName)
    } else {
        Ok(format!("{}/{}", tp, nm))
    }
}

/// Make a Sonar attribute (with validation)
fn make_att(tp: &str, nm: &str, att: &str) -> Result<String> {
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
fn obj_name(tp: &str, req: &Request<State>) -> Result<String> {
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
async fn resource_get(tp: &str, req: Request<State>) -> tide::Result {
    log::info!("GET {}", req.url());
    let body = resp!(resource_get_json(tp, req).await);
    Ok(Response::builder(StatusCode::Ok)
        .body(body)
        .content_type("application/json")
        .build())
}

/// Get a static JSON file
async fn resource_get_json(nm: &str, req: Request<State>) -> Result<Body> {
    Access::View.check(nm, &req)?;
    let file = format!("{STATIC_PATH}/{nm}");
    let path = PathBuf::from(file);
    Ok(Body::from_file(&path).await?)
}

/// `GET` a Sonar object and return JSON result
async fn sonar_object_get(tp: &str, req: Request<State>) -> tide::Result {
    log::info!("GET {}", req.url());
    let body = resp!(sonar_object_get_json(tp, req).await);
    Ok(Response::builder(StatusCode::Ok)
        .body(&body[..])
        .content_type("application/json")
        .build())
}

/// `GET` a Sonar object as JSON
async fn sonar_object_get_json(
    tp: &str,
    req: Request<State>,
) -> Result<String> {
    Access::View.check(tp, &req)?;
    let nm = obj_name(tp, &req)?;
    let mut c = connection(&req).await?;
    let mut res = Map::new();
    res.insert(
        "name".to_string(),
        Value::String(nm.split_once('/').unwrap().1.to_string()),
    );
    c.enumerate_object(&nm, |att, val| {
        let att = rename_sonar_to_db(tp, att);
        if let Some(val) = make_json(&(tp, &att), val) {
            res.insert(att, val);
        }
        Ok(())
    })
    .await?;
    Ok(Value::Object(res).to_string())
}

/// Make a JSON attribute value
fn make_json(tp_att: &(&str, &str), val: &str) -> Option<Value> {
    if INTEGERS.contains(tp_att) {
        val.parse::<i64>().ok().map(|v| v.into())
    } else if BOOLS.contains(tp_att) {
        val.parse::<bool>().ok().map(|v| v.into())
    } else if STAMPS.contains(tp_att) {
        val.parse::<i64>().ok().map(|ms| {
            let sec = ms / 1_000;
            let ns = (ms % 1_000) as u32 * 1_000_000;
            Local.timestamp(sec, ns).to_rfc3339().into()
        })
    } else if val != "\0" {
        Some(val.into())
    } else {
        Some(Value::Null)
    }
}

/// Create a Sonar object from a `POST` request
async fn sonar_object_post(tp: &str, mut req: Request<State>) -> tide::Result {
    log::info!("POST {}", req.url());
    resp!(Access::Configure.check(tp, &req));
    let body: Value = req.body_json().await?;
    match body.as_object() {
        Some(obj) => match obj.get("name") {
            Some(Value::String(name)) => {
                let nm = resp!(make_name(tp, name));
                let mut c = resp!(connection(&req).await);
                log::debug!("creating {}", nm);
                resp!(c.create_object(&nm).await);
                Ok(Response::builder(StatusCode::Created).build())
            }
            Some(_) => bad_request("invalid name"),
            None => bad_request("missing name"),
        },
        None => bad_request("body must be a JSON object"),
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
async fn sonar_object_patch(tp: &str, mut req: Request<State>) -> tide::Result {
    log::info!("PATCH {}", req.url());
    resp!(Access::Configure.check(tp, &req));
    let nm = resp!(obj_name(tp, &req));
    let mut body: Value = req.body_json().await?;
    if !body.is_object() {
        return bad_request("body must be a JSON object");
    }
    let object = body.as_object_mut().unwrap();
    let mut c = resp!(connection(&req).await);
    // "pin" attribute must be set before "controller"
    if let Some(value) = object.remove("pin") {
        let anm = resp!(make_att(tp, &nm, "pin"));
        let value = resp!(att_value(&value));
        log::debug!("{} = {}", anm, &value);
        resp!(c.update_object(&anm, &value).await);
    }
    for (key, value) in object.iter() {
        let anm = resp!(make_att(tp, &nm, key));
        let value = resp!(att_value(value));
        log::debug!("{} = {}", anm, &value);
        resp!(c.update_object(&anm, &value).await);
    }
    Ok(Response::builder(StatusCode::NoContent).build())
}

/// Remove a Sonar object from a `DELETE` request
async fn sonar_object_delete(tp: &str, req: Request<State>) -> tide::Result {
    log::info!("DELETE {}", req.url());
    resp!(Access::Configure.check(tp, &req));
    let nm = resp!(obj_name(tp, &req));
    let mut c = resp!(connection(&req).await);
    resp!(c.remove_object(&nm).await);
    Ok(Response::builder(StatusCode::NoContent).build())
}
