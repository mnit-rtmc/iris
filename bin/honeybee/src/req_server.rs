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
use actix_web::{http::Method,server,App,HttpRequest,HttpResponse};
use failure::Error;
use postgres::{Connection, TlsMode};
use serde;
use iris_req::{CameraPub,DmsPub,DmsMessage,Incident,SignConfig,
               ParkingAreaStatic,ParkingAreaDynamic,Queryable,query_json};

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

pub fn start(uds: String) {
    server::new(move || {
        let uds = uds.clone();
        App::new().resource("/iris/{v}", move |r| {
            r.method(Method::GET).h(Handler::new(uds))
        })
    }).bind("127.0.0.1:8088").expect("Can not bind to 127.0.0.1:8088")
      .run();
}
