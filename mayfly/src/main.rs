// main.rs
//
// Copyright (c) 2019-2021  Minnesota Department of Transportation
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

mod common;
mod query;
mod vehicle;

use common::Error;
use query::{
    CountData, DateQuery, DetectorQuery, DistrictQuery, OccupancyData,
    SpeedData, TrafficQuery, YearQuery,
};
use tide::{Request, Response, StatusCode};

/// Main function
#[async_std::main]
async fn main() -> tide::Result<()> {
    env_logger::builder().format_timestamp(None).init();
    let mut app = tide::new();
    let mut root = app.at("/mayfly/");
    root.at("").get(|_| handle_index());
    root.at("index.html").get(|_| handle_index());
    root.at("mayfly.css").get(|_| handle_css());
    root.at("districts").get(|req| handle_districts(req));
    root.at("years").get(|req| handle_years(req));
    root.at("dates").get(|req| handle_dates(req));
    root.at("detectors").get(|req| handle_detectors(req));
    root.at("counts").get(|req| handle_counts(req));
    root.at("speed").get(|req| handle_speed(req));
    root.at("occupancy").get(|req| handle_occupancy(req));
    app.listen("127.0.0.1:3131").await?;
    Ok(())
}

/// Handle a request for index page
async fn handle_index() -> tide::Result {
    Ok(Response::builder(StatusCode::Ok)
        .content_type("text/html")
        .body(include_str!(concat!(
            env!("CARGO_MANIFEST_DIR"),
            "/static/index.html"
        )))
        .build())
}

/// Handle a request for CSS
async fn handle_css() -> tide::Result {
    Ok(Response::builder(StatusCode::Ok)
        .content_type("text/css")
        .body(include_str!(concat!(
            env!("CARGO_MANIFEST_DIR"),
            "/static/mayfly.css"
        )))
        .build())
}

/// Handle a query
///
/// Ideally, this would be done with a Query trait, but that won't work without
/// async functions in traits.
macro_rules! handle_query {
    ($req:expr, $qtype:ty) => {
        match $req.query::<$qtype>().or(Err(Error::InvalidQuery)) {
            Ok(query) => match query.lookup().await {
                Ok(body) => body.into(),
                Err(err) => err.into(),
            },
            Err(err) => err.into(),
        }
    };
}

/// Handle a request for districts
async fn handle_districts(req: Request<()>) -> tide::Result {
    handle_query!(req, DistrictQuery)
}

/// Handle a request for years
async fn handle_years(req: Request<()>) -> tide::Result {
    handle_query!(req, YearQuery)
}

/// Handle a request for dates
async fn handle_dates(req: Request<()>) -> tide::Result {
    handle_query!(req, DateQuery)
}

/// Handle a request for detectors
async fn handle_detectors(req: Request<()>) -> tide::Result {
    handle_query!(req, DetectorQuery)
}

/// Handle a request for vehicle counts
async fn handle_counts(req: Request<()>) -> tide::Result {
    handle_query!(req, TrafficQuery<CountData>)
}

/// Handle a request for speed data
async fn handle_speed(req: Request<()>) -> tide::Result {
    handle_query!(req, TrafficQuery<SpeedData>)
}

/// Handle a request for occupancy data
async fn handle_occupancy(req: Request<()>) -> tide::Result {
    handle_query!(req, TrafficQuery<OccupancyData>)
}
