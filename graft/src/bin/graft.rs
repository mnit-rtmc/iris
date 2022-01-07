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

use async_std::io::ErrorKind::TimedOut;
use convert_case::{Case, Casing};
use graft::sonar::{Connection, Result, SonarError};
use rand::Rng;
use tide::prelude::*;
use tide::sessions::{MemoryStore, SessionMiddleware};
use tide::{Request, Response, StatusCode};

/// Trait to get HTTP status code from an error
trait ErrorStatus {
    fn status_code(&self) -> StatusCode;
}

impl ErrorStatus for SonarError {
    fn status_code(&self) -> StatusCode {
        match self {
            Self::Msg(msg) if msg.starts_with("Permission") => {
                StatusCode::Forbidden
            }
            Self::Msg(_) => StatusCode::Conflict,
            Self::NameMissing => StatusCode::BadRequest,
            Self::IO(e) if e.kind() == TimedOut => StatusCode::GatewayTimeout,
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
    app.at("/login").get(get_login);
    app.at("/login").post(post_login);
    add_routes!(app, "comm_config");
    add_routes!(app, "comm_link");
    add_routes!(app, "controller");
    app.listen("127.0.0.1:3737").await?;
    Ok(())
}

/// Login form
const LOGIN: &str = r#"<html>
<form method="POST" action="/login">
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

/// Handle `POST` to login page
async fn post_login(mut req: Request<()>) -> tide::Result {
    log::info!("POST {}", req.url());
    let auth: AuthMap = match req.body_form().await {
        Ok(auth) => auth,
        Err(e) => {
            log::warn!("response: {:?}", e);
            return Ok(Response::builder(StatusCode::BadRequest)
                .body(e.to_string())
                .build());
        }
    };
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
    let mut c = Connection::new(HOST, 1037).await?;
    c.login(&auth.username, &auth.password).await?;
    Ok(c)
}

/// Get Sonar object name from a request
fn obj_name(tp: &str, req: &Request<()>) -> Result<String> {
    match req.param("name") {
        Ok(name) => Ok(format!("{}/{}", tp, name)),
        Err(_) => Err(SonarError::NameMissing),
    }
}

/// `GET` list of objects and return JSON result
async fn list_objects(_tp: &str, req: Request<()>) -> tide::Result {
    log::info!("GET {}", req.url());
    todo!("read file created by honeybee");
}

/// `GET` a Sonar object and return JSON result
async fn get_sonar_object(tp: &str, req: Request<()>) -> tide::Result {
    log::info!("GET {}", req.url());
    let mut c = resp!(connection(&req).await);
    let nm = resp!(obj_name(tp, &req));
    let mut res = json::object!();
    resp!(
        c.enumerate_object(&nm, |att, val| {
            let att = att.to_case(Case::Snake);
            res[att] = val.into();
            Ok(())
        })
        .await
    );
    Ok(Response::builder(StatusCode::Ok)
        .body(res.to_string())
        .content_type("application/json")
        .build())
}

/// Create a Sonar object from a `POST` request
async fn create_sonar_object(tp: &str, req: Request<()>) -> tide::Result {
    log::info!("POST {}", req.url());
    let nm = resp!(obj_name(tp, &req));
    let mut c = resp!(connection(&req).await);
    resp!(c.create_object(&nm).await);
    Ok(Response::builder(StatusCode::Created).build())
}

/// Update a Sonar object from a `PATCH` request
async fn update_sonar_object(tp: &str, req: Request<()>) -> tide::Result {
    log::info!("PATCH {}", req.url());
    let mut c = resp!(connection(&req).await);
    match req.param("name") {
        Ok(name) => {
            for pair in req.url().query_pairs() {
                let att = pair.0.to_case(Case::Camel);
                let nm = format!("{}/{}/{}", tp, name, att);
                log::info!("{} = {}", nm, &pair.1);
                resp!(c.update_object(&nm, &pair.1).await);
            }
            Ok(Response::builder(StatusCode::NoContent).build())
        }
        Err(_) => resp!(Err(SonarError::NameMissing)),
    }
}

/// Remove a Sonar object from a `DELETE` request
async fn remove_sonar_object(tp: &str, req: Request<()>) -> tide::Result {
    log::info!("DELETE {}", req.url());
    let nm = resp!(obj_name(tp, &req));
    let mut c = resp!(connection(&req).await);
    resp!(c.remove_object(&nm).await);
    Ok(Response::builder(StatusCode::NoContent).build())
}
