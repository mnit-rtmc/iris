// graft.rs
//
// Copyright (C) 2021  Minnesota Department of Transportation
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
use tide::{Request, Response, StatusCode};

trait ErrorStatus {
    fn status_code(&self) -> StatusCode;
}

impl ErrorStatus for SonarError {
    fn status_code(&self) -> StatusCode {
        match self {
            Self::Msg(_) => StatusCode::Conflict,
            Self::NameMissing => StatusCode::BadRequest,
            Self::IO(e) if e.kind() == TimedOut => StatusCode::GatewayTimeout,
            _ => StatusCode::InternalServerError,
        }
    }
}

macro_rules! resp {
    ($res:expr) => {
        match $res {
            Ok(r) => r,
            Err(e) => {
                return Ok(Response::builder(e.status_code())
                    .body(e.to_string())
                    .build());
            }
        }
    };
}

macro_rules! add_routes {
    ($app:expr, $tp:expr) => {
        $app.at(concat!("/", $tp))
            .get(|req| get_sonar_object($tp, req));
        $app.at(concat!("/", $tp))
            .post(|req| create_sonar_object($tp, req));
        $app.at(concat!("/", $tp))
            .delete(|req| delete_sonar_object($tp, req));
    };
}

#[async_std::main]
async fn main() -> tide::Result<()> {
    env_logger::builder().format_timestamp(None).init();
    let mut app = tide::new();
    add_routes!(app, "comm_config");
    add_routes!(app, "comm_link");
    add_routes!(app, "controller");
    app.listen("127.0.0.1:8080").await?;
    Ok(())
}

const HOST: &str = &"localhost.localdomain";

async fn connection(_req: &Request<()>) -> Result<Connection> {
    let mut c = Connection::new(HOST, 1037).await?;
    c.login("admin", "atms_242").await?;
    Ok(c)
}

fn req_name(tp: &str, req: &Request<()>) -> Result<String> {
    for pair in req.url().query_pairs() {
        if pair.0 == "name" {
            return Ok(format!("{}/{}", tp, pair.1));
        }
    }
    Err(SonarError::NameMissing)
}

async fn get_sonar_object(tp: &str, req: Request<()>) -> tide::Result {
    let mut c = resp!(connection(&req).await);
    let nm = resp!(req_name(tp, &req));
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

async fn create_sonar_object(tp: &str, req: Request<()>) -> tide::Result {
    let nm = resp!(req_name(tp, &req));
    let mut c = resp!(connection(&req).await);
    resp!(c.create_object(&nm).await);
    Ok(Response::builder(StatusCode::Created).build())
}

async fn delete_sonar_object(tp: &str, req: Request<()>) -> tide::Result {
    let nm = resp!(req_name(tp, &req));
    let mut c = resp!(connection(&req).await);
    resp!(c.remove_object(&nm).await);
    Ok(Response::builder(StatusCode::NoContent).build())
}
