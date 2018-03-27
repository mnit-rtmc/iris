/*
 * honeybee -- Web service for IRIS
 * Copyright (C) 2018  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
extern crate actix_web;
extern crate chrono;
extern crate failure;
extern crate postgres;
extern crate serde;
extern crate serde_json;
#[macro_use] extern crate serde_derive;
extern crate users;

use actix_web::*;
use chrono::{DateTime, Local};
use failure::Error;
use postgres::{Connection, TlsMode};
use users::get_current_username;

trait Queryable {
    fn sql() -> &'static str;
    fn from_row(row: &postgres::rows::Row) -> Self;
}

#[derive(Serialize)]
struct Incident {
    name       : String,
    event_date : DateTime<Local>,
    description: String,
    road       : String,
    direction  : String,
    lane_type  : String,
    impact     : String,
    cleared    : bool,
    confirmed  : bool,
    camera     : Option<String>,
    detail     : Option<String>,
    replaces   : Option<String>,
    lat        : Option<f64>,
    lon        : Option<f64>,
}

impl Queryable for Incident {
    fn sql() -> &'static str {
       "SELECT name, event_date, description, road, direction, lane_type, \
               impact, cleared, confirmed, camera, detail, replaces, lat, lon \
        FROM incident_view \
        WHERE cleared = 'f'"
    }
    fn from_row(row: &postgres::rows::Row) -> Incident {
        Incident {
            name        : row.get(0),
            event_date  : row.get(1),
            description : row.get(2),
            road        : row.get(3),
            direction   : row.get(4),
            lane_type   : row.get(5),
            impact      : row.get(6),
            cleared     : row.get(7),
            confirmed   : row.get(8),
            camera      : row.get(9),
            detail      : row.get(10),
            replaces    : row.get(11),
            lat         : row.get(12),
            lon         : row.get(13),
        }
    }
}

#[derive(Serialize)]
struct SignConfig {
    name        : String,
    dms_type    : String,
    portable    : bool,
    technology  : String,
    sign_access : String,
    legend      : String,
    beacon_type : String,
    face_width  : i32,
    face_height : i32,
    border_horiz: i32,
    border_vert : i32,
    pitch_horiz : i32,
    pitch_vert  : i32,
    pixel_width : i32,
    pixel_height: i32,
    char_width  : i32,
    char_height : i32,
}

impl Queryable for SignConfig {
    fn sql() -> &'static str {
       "SELECT name, dms_type, portable, technology, sign_access, legend, \
               beacon_type, face_width, face_height, border_horiz, border_vert, \
               pitch_horiz, pitch_vert, pixel_width, pixel_height, char_width, \
               char_height \
        FROM sign_config_view"
    }
    fn from_row(row: &postgres::rows::Row) -> SignConfig {
        SignConfig {
            name        : row.get(0),
            dms_type    : row.get(1),
            portable    : row.get(2),
            technology  : row.get(3),
            sign_access : row.get(4),
            legend      : row.get(5),
            beacon_type : row.get(6),
            face_width  : row.get(7),
            face_height : row.get(8),
            border_horiz: row.get(9),
            border_vert : row.get(10),
            pitch_horiz : row.get(11),
            pitch_vert  : row.get(12),
            pixel_width : row.get(13),
            pixel_height: row.get(14),
            char_width  : row.get(15),
            char_height : row.get(16),
        }
    }
}

#[derive(Serialize)]
struct Dms {
    name        : String,
    sign_config : Option<String>,
    roadway     : Option<String>,
    road_dir    : String,
    cross_street: Option<String>,
    location    : Option<String>,
    lat         : Option<f64>,
    lon         : Option<f64>,
}

impl Queryable for Dms {
    fn sql() -> &'static str {
       "SELECT name, sign_config, roadway, road_dir, cross_street, location, \
               lat, lon \
        FROM dms_view ORDER BY name"
    }
    fn from_row(row: &postgres::rows::Row) -> Dms {
        Dms {
            name        : row.get(0),
            sign_config : row.get(1),
            roadway     : row.get(2),
            road_dir    : row.get(3),
            cross_street: row.get(4),
            location    : row.get(5),
            lat         : row.get(6),
            lon         : row.get(7),
        }
    }
}

#[derive(Serialize)]
struct DmsMessage {
    name       : String,
    multi      : Option<String>,
    sources    : Option<String>,
    duration   : Option<i32>,
    deploy_time: DateTime<Local>,
}

impl Queryable for DmsMessage {
    fn sql() -> &'static str {
       "SELECT name, multi, sources, duration, deploy_time \
        FROM dms_message_view WHERE condition = 'Active' \
        ORDER BY name"
    }
    fn from_row(row: &postgres::rows::Row) -> DmsMessage {
        DmsMessage {
            name       : row.get(0),
            multi      : row.get(1),
            sources    : row.get(2),
            duration   : row.get(3),
            deploy_time: row.get(4),
        }
    }
}

fn query_json<T>(conn: &Connection) -> Result<String, Error> where
    T: Queryable + serde::Serialize
{
    let mut first = true;
    let mut s = String::new();
    s.push_str("[\n");
    for row in &conn.query(T::sql(), &[])? {
        let t = T::from_row(&row);
        if !first { s.push_str(",\n"); } else { first = false; }
        s.push_str(&serde_json::to_string(&t)?);
    }
    s.push_str("\n]");
    Ok(s)
}

fn query_js<T>() -> Result<String, Error>
    where T: Queryable + serde::Serialize
{
    let username = get_current_username().expect("User name lookup error");
    // Format path for unix domain socket
    let uds = format!("postgres://{:}@%2Frun%2Fpostgresql/tms", username);
    let conn = Connection::connect(uds, TlsMode::None)?;
    query_json::<T>(&conn)
}

fn get_js_req<T>() -> HttpResponse
    where T: Queryable + serde::Serialize
{
    match query_js::<T>() {
        Ok(body) => HttpResponse::Ok()
                                 .content_type("application/json")
                                 .body(body).unwrap(),
        Err(_)   => HttpResponse::InternalServerError()
                                 .body("Database error").unwrap(),
    }
}

fn get_json(req: HttpRequest) -> HttpResponse {
    match req.match_info().get("v") {
        Some("dms")          => get_js_req::<Dms>(),
        Some("dms_messages") => get_js_req::<DmsMessage>(),
        Some("incidents")    => get_js_req::<Incident>(),
        Some("sign_config")  => get_js_req::<SignConfig>(),
        _                    => HttpResponse::NotFound()
                                             .body("Not found").unwrap(),
    }
}

fn main() {
    HttpServer::new(|| Application::new()
                .resource("/{v}.json", |r| r.method(Method::GET).f(get_json)))
        .bind("127.0.0.1:8088").expect("Can not bind to 127.0.0.1:8088")
        .run();
}
