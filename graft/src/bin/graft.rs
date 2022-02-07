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
use chrono::{Local, TimeZone};
use convert_case::{Case, Casing};
use graft::sonar::{Connection, Result, SonarError};
use percent_encoding::percent_decode_str;
use rand::Rng;
use serde_json::map::Map;
use serde_json::Value;
use std::io;
use tide::prelude::*;
use tide::sessions::{MemoryStore, SessionMiddleware};
use tide::{Body, Request, Response, StatusCode};

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
];

/// Slice of (type, attribute) tuples for RFC 3339 time stamp values
const STAMPS: &[(&str, &str)] = &[
    ("alarm", "trigger_time"),
    ("controller", "fail_time"),
];

/// Hack to rename IRIS attributes that don't match DB names
fn rename_att(tp: &str, att: &str) -> String {
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
            Self::InvalidValue => StatusCode::UnprocessableEntity,
            Self::Forbidden => StatusCode::Forbidden,
            Self::NotFound => StatusCode::NotFound,
            Self::IO(e) => e.status_code(),
            Self::Unauthorized => StatusCode::Unauthorized,
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
        $app.at(concat!("/", $tp)).get(|req| list_objects($tp, req));
        $app.at(concat!("/", $tp))
            .post(|req| create_sonar_object($tp, req));
        $app.at(concat!("/", $tp, "/:name"))
            .get(|req| get_sonar_object($tp, req));
        $app.at(concat!("/", $tp, "/:name"))
            .patch(|req| update_sonar_object($tp, req));
        $app.at(concat!("/", $tp, "/:name"))
            .delete(|req| remove_sonar_object($tp, req));
    };
}

/// Main entry point
#[async_std::main]
async fn main() -> tide::Result<()> {
    env_logger::builder().format_timestamp(None).init();
    let mut app = tide::new();
    app.with(
        SessionMiddleware::new(
            MemoryStore::new(),
            &rand::thread_rng().gen::<[u8; 32]>(),
        )
        .with_cookie_name("graft"),
    );
    let mut route = app.at("/iris/api");
    route.at("/login").get(get_login);
    route.at("/login").post(post_login);
    route
        .at("/comm_link_stat")
        .get(|req| list_objects("comm_link_stat", req));
    route
        .at("/comm_protocol")
        .get(|req| list_objects("comm_protocol", req));
    route
        .at("/condition")
        .get(|req| list_objects("condition", req));
    route
        .at("/controller_stat")
        .get(|req| list_objects("controller_stat", req));
    add_routes!(route, "alarm");
    add_routes!(route, "cabinet_style");
    add_routes!(route, "comm_config");
    add_routes!(route, "comm_link");
    add_routes!(route, "controller");
    add_routes!(route, "modem");
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
async fn get_login(req: Request<()>) -> tide::Result {
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
async fn post_login(mut req: Request<()>) -> tide::Result {
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
        .body("<html>Posted!</html>")
        .content_type("text/html;charset=utf-8")
        .build())
}

/// IRIS host name
const HOST: &str = "localhost.localdomain";

/// Create a Sonar connection for a request
async fn connection(req: &Request<()>) -> Result<Connection> {
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

/// Get Sonar object name from a request
fn obj_name(tp: &str, req: &Request<()>) -> Result<String> {
    match req.param("name") {
        Ok(name) => {
            let name = percent_decode_str(name)
                .decode_utf8()
                .or(Err(SonarError::InvalidName))?;
            if name.len() > 64
                || name.contains(invalid_char)
                || name.contains('/')
            {
                Err(SonarError::InvalidName)
            } else {
                Ok(format!("{}/{}", tp, name))
            }
        }
        Err(_) => Err(SonarError::InvalidName),
    }
}

/// Get Sonar attribute name
fn att_name(nm: &str, att: &str) -> Result<String> {
    if att.len() > 64 || att.contains(invalid_char) || att.contains('/') {
        Err(SonarError::InvalidName)
    } else {
        Ok(format!("{}/{}", nm, att.to_case(Case::Camel)))
    }
}

/// `GET` list of objects and return JSON result
async fn list_objects(tp: &str, req: Request<()>) -> tide::Result {
    log::info!("GET {}", req.url());
    get_json_file(tp, req).await
}

/// Path for static files
const STATIC_PATH: &str = "/var/www/html/iris/api";

/// Get a static JSON file
async fn get_json_file(nm: &str, req: Request<()>) -> tide::Result {
    let session = req.session();
    let _auth: AuthMap =
        resp!(session.get("auth").ok_or(SonarError::Unauthorized));
    // FIXME: check Sonar for read permission first
    let file = format!("{}/{}", STATIC_PATH, nm);
    let path = PathBuf::from(file);
    let body = resp!(Body::from_file(&path).await);
    Ok(Response::builder(StatusCode::Ok)
        .body(body)
        .content_type("application/json")
        .build())
}

/// `GET` a Sonar object and return JSON result
async fn get_sonar_object(tp: &str, req: Request<()>) -> tide::Result {
    log::info!("GET {}", req.url());
    let nm = resp!(obj_name(tp, &req));
    let mut c = resp!(connection(&req).await);
    let mut res = Map::new();
    res.insert(
        "name".to_string(),
        Value::String(nm.split_once('/').unwrap().1.to_string()),
    );
    resp!(
        c.enumerate_object(&nm, |att, val| {
            let att = rename_att(tp, att);
            if let Some(val) = make_json(&(tp, &att), val) {
                res.insert(att, val);
            }
            Ok(())
        })
        .await
    );
    let body = Value::Object(res).to_string();
    Ok(Response::builder(StatusCode::Ok)
        .body(&body[..])
        .content_type("application/json")
        .build())
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
async fn create_sonar_object(tp: &str, req: Request<()>) -> tide::Result {
    log::info!("POST {}", req.url());
    let nm = resp!(obj_name(tp, &req));
    let mut c = resp!(connection(&req).await);
    resp!(c.create_object(&nm).await);
    Ok(Response::builder(StatusCode::Created).build())
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
async fn update_sonar_object(tp: &str, mut req: Request<()>) -> tide::Result {
    log::info!("PATCH {}", req.url());
    let nm = resp!(obj_name(tp, &req));
    let body: Value = req.body_json().await?;
    if !body.is_object() {
        return bad_request("body must be a JSON object");
    }
    let mut c = resp!(connection(&req).await);
    for (key, value) in body.as_object().unwrap() {
        let anm = resp!(att_name(&nm, &key));
        let value = resp!(att_value(value));
        log::debug!("{} = {}", anm, &value);
        resp!(c.update_object(&anm, &value).await);
    }
    Ok(Response::builder(StatusCode::NoContent).build())
}

/// Remove a Sonar object from a `DELETE` request
async fn remove_sonar_object(tp: &str, req: Request<()>) -> tide::Result {
    log::info!("DELETE {}", req.url());
    let nm = resp!(obj_name(tp, &req));
    let mut c = resp!(connection(&req).await);
    resp!(c.remove_object(&nm).await);
    Ok(Response::builder(StatusCode::NoContent).build())
}
