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
use actix_web;
use actix_web::{HttpRequest, HttpResponse};
use chrono::{DateTime, Local, Utc};
use failure::Error;
use postgres;
use postgres::{Connection, TlsMode};
use serde;
use serde_json;

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
    fn from_row(row: &postgres::rows::Row) -> Self {
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
struct CameraPub {
    name     : String,
    publish  : bool,
    location : Option<String>,
    lat      : Option<f64>,
    lon      : Option<f64>,
}

impl Queryable for CameraPub {
    fn sql() -> &'static str {
       "SELECT name, publish, location, lat, lon \
        FROM camera_view ORDER BY name"
    }
    fn from_row(row: &postgres::rows::Row) -> Self {
        CameraPub {
            name     : row.get(0),
            publish  : row.get(1),
            location : row.get(2),
            lat      : row.get(3),
            lon      : row.get(4),
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
    color_scheme: String,
    monochrome_foreground: i32,
    monochrome_background: i32,
}

impl Queryable for SignConfig {
    fn sql() -> &'static str {
       "SELECT name, dms_type, portable, technology, sign_access, legend, \
               beacon_type, face_width, face_height, border_horiz, border_vert, \
               pitch_horiz, pitch_vert, pixel_width, pixel_height, char_width, \
               char_height, color_scheme, monochrome_foreground, \
               monochrome_background \
        FROM sign_config_view"
    }
    fn from_row(row: &postgres::rows::Row) -> Self {
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
            color_scheme: row.get(17),
            monochrome_foreground: row.get(18),
            monochrome_background: row.get(19),
        }
    }
}

#[derive(Serialize)]
struct DmsPub {
    name        : String,
    sign_config : Option<String>,
    roadway     : Option<String>,
    road_dir    : String,
    cross_street: Option<String>,
    location    : Option<String>,
    lat         : Option<f64>,
    lon         : Option<f64>,
}

impl Queryable for DmsPub {
    fn sql() -> &'static str {
       "SELECT name, sign_config, roadway, road_dir, cross_street, location, \
               lat, lon \
        FROM dms_view ORDER BY name"
    }
    fn from_row(row: &postgres::rows::Row) -> Self {
        DmsPub {
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
    msg_current: String,
    multi      : Option<String>,
    sources    : Option<String>,
    duration   : Option<i32>,
    deploy_time: DateTime<Local>,
}

impl Queryable for DmsMessage {
    fn sql() -> &'static str {
       "SELECT name, msg_current, multi, sources, duration, deploy_time \
        FROM dms_message_view WHERE condition = 'Active' \
        ORDER BY name"
    }
    fn from_row(row: &postgres::rows::Row) -> Self {
        DmsMessage {
            name       : row.get(0),
            msg_current: row.get(1),
            multi      : row.get(2),
            sources    : row.get(3),
            duration   : row.get(4),
            deploy_time: row.get(5),
        }
    }
}

#[allow(non_snake_case)]
#[derive(Serialize)]
struct Location {
    latitude : Option<f64>,
    longitude: Option<f64>,
    streetAdr: Option<String>,
    city     : Option<String>,
    state    : Option<String>,
    zip      : Option<String>,
    timeZone : Option<String>,
}

#[allow(non_snake_case)]
#[derive(Serialize)]
struct ParkingAreaStatic {
    siteId           : Option<String>,
    timeStamp        : Option<DateTime<Utc>>,
    relevantHighway  : Option<String>,
    referencePost    : Option<String>,
    exitId           : Option<String>,
    directionOfTravel: Option<String>,
    name             : Option<String>,
    location         : Location,
    ownership        : Option<String>,
    capacity         : Option<i32>,
    amenities        : Vec<String>,
    images           : Vec<String>,
    logos            : Option<Vec<String>>,
}

impl Queryable for ParkingAreaStatic {
    fn sql() -> &'static str {
       "SELECT site_id, time_stamp_static, relevant_highway, reference_post, \
               exit_id, road_dir, facility_name, lat, lon, street_adr, city, \
               state, zip, time_zone, ownership, capacity, amenities, \
               camera_1, camera_2, camera_3, camera_image_base_url \
        FROM parking_area_view"
    }
    fn from_row(row: &postgres::rows::Row) -> Self {
        let amenities = if let Some(a) = row.get::<usize, Option<String>>(16) {
            a.split(", ").map(|s| s.to_string()).collect()
        } else {
            vec!()
        };
        let mut images = vec!();
        if let Some(url) = row.get::<usize, Option<String>>(20) {
            if url.len() > 0 {
                if let Some(c1) = row.get::<usize, Option<String>>(17) {
                    let mut c = url.to_owned();
                    c.push_str(&c1);
                    images.push(c)
                }
                if let Some(c2) = row.get::<usize, Option<String>>(18) {
                    let mut c = url.to_owned();
                    c.push_str(&c2);
                    images.push(c)
                }
                if let Some(c3) = row.get::<usize, Option<String>>(19) {
                    let mut c = url.to_owned();
                    c.push_str(&c3);
                    images.push(c)
                }
            }
        }
        ParkingAreaStatic {
            siteId           : row.get(0),
            timeStamp        : row.get(1),
            relevantHighway  : row.get(2),
            referencePost    : row.get(3),
            exitId           : row.get(4),
            directionOfTravel: row.get(5),
            name             : row.get(6),
            location         : Location {
                latitude : row.get(7),
                longitude: row.get(8),
                streetAdr: row.get(9),
                city     : row.get(10),
                state    : row.get(11),
                zip      : row.get(12),
                timeZone : row.get(13),
            },
            ownership        : row.get(14),
            capacity         : row.get(15),
            amenities        : amenities,
            images           : images,
            logos            : Some(vec!()),
        }
    }
}

#[allow(non_snake_case)]
#[derive(Serialize)]
struct ParkingAreaDynamic {
    siteId           : Option<String>,
    timeStamp        : Option<DateTime<Utc>>,
    timeStampStatic  : Option<DateTime<Utc>>,
    reportedAvailable: Option<String>,
    trend            : Option<String>,
    open             : Option<bool>,
    trustData        : Option<bool>,
    capacity         : Option<i32>,
}

impl Queryable for ParkingAreaDynamic {
    fn sql() -> &'static str {
       "SELECT site_id, time_stamp, time_stamp_static, reported_available, \
               trend, open, trust_data, capacity \
        FROM parking_area_view"
    }
    fn from_row(row: &postgres::rows::Row) -> Self {
        ParkingAreaDynamic {
            siteId           : row.get(0),
            timeStamp        : row.get(1),
            timeStampStatic  : row.get(2),
            reportedAvailable: row.get(3),
            trend            : row.get(4),
            open             : row.get(5),
            trustData        : row.get(6),
            capacity         : row.get(7),
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

pub struct Handler {
    uds: String,
}

impl<S> actix_web::dev::Handler<S> for Handler {
    type Result = HttpResponse;

    fn handle(&self, req: &HttpRequest<S>) -> Self::Result {
        match req.match_info().get("v") {
            Some("camera_pub")    => self.get_json::<CameraPub>(),
            Some("dms_pub")       => self.get_json::<DmsPub>(),
            Some("dms_message")   => self.get_json::<DmsMessage>(),
            Some("incident")      => self.get_json::<Incident>(),
            Some("sign_config")   => self.get_json::<SignConfig>(),
            Some("TPIMS_static")  => self.get_json::<ParkingAreaStatic>(),
            Some("TPIMS_dynamic") => self.get_json::<ParkingAreaDynamic>(),
            _                     => HttpResponse::NotFound()
                                                  .body("Not found"),
        }
    }
}

impl Handler {
    pub fn new(uds: String) -> Self {
        Self { uds }
    }
    fn get_json<T>(&self) -> HttpResponse
        where T: Queryable + serde::Serialize
    {
        match self.query_json::<T>() {
            Ok(body) => HttpResponse::Ok()
                                     .content_type("application/json")
                                     .body(body),
            Err(_)   => HttpResponse::InternalServerError()
                                     .body("Database error"),
        }
    }
    fn query_json<T>(&self) -> Result<String, Error>
        where T: Queryable + serde::Serialize
    {
        let conn = Connection::connect(self.uds.clone(), TlsMode::None)?;
        query_json::<T>(&conn)
    }
}
