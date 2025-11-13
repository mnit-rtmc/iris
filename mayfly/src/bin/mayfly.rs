// mayfly.rs
//
// Copyright (c) 2019-2025  Minnesota Department of Transportation
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

use axum::Router;
use axum::response::IntoResponse;
use axum::routing::get;
use http::header;
use mayfly::error::Result;
use mayfly::routes;
use std::net::SocketAddr;
use tokio::net::TcpListener;

/// Main function
#[tokio::main]
async fn main() -> Result<()> {
    env_logger::builder().format_timestamp(None).init();
    let app = route_root().into_make_service_with_connect_info::<SocketAddr>();
    let listener = TcpListener::bind("127.0.0.1:3131").await?;
    axum::serve(listener, app).await?;
    log::warn!("Axum serve ended");
    Ok(())
}

/// Build root route
fn route_root() -> Router {
    Router::new()
        .merge(root_get())
        .nest("/mayfly", route_mayfly())
}

/// Build route for index html
fn root_get() -> Router {
    Router::new().route("/mayfly/", get(index_handler))
}

/// Handler for index page
async fn index_handler() -> impl IntoResponse {
    (
        [(header::CONTENT_TYPE, "text/html; charset=utf-8")],
        include_str!(concat!(env!("CARGO_MANIFEST_DIR"), "/static/index.html")),
    )
}

/// Build route for index html
fn index_get() -> Router {
    Router::new().route("/index.html", get(index_handler))
}

/// Build route for CSS
fn css_get() -> Router {
    async fn handler() -> impl IntoResponse {
        (
            [(header::CONTENT_TYPE, "text/css")],
            include_str!(concat!(
                env!("CARGO_MANIFEST_DIR"),
                "/static/mayfly.css"
            )),
        )
    }
    Router::new().route("/mayfly.css", get(handler))
}

/// Build mayfly route
fn route_mayfly() -> Router {
    Router::new()
        .merge(index_get())
        .merge(css_get())
        .merge(routes::districts_get())
        .merge(routes::years_get())
        .merge(routes::dates_get())
        .merge(routes::corridors_get())
        .merge(routes::detectors_get())
        .merge(routes::counts_get())
        .merge(routes::headway_get())
        .merge(routes::occupancy_get())
        .merge(routes::length_get())
        .merge(routes::speed_get())
        .merge(routes::espeed_get())
}
