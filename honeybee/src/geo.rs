// geo.rs
//
// Copyright (C) 2019-2020  Minnesota Department of Transportation
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
use pointy::Pt64;

/// WGS-84 (EPSG:4326) position.
#[derive(Clone, Copy, Debug)]
pub struct Wgs84Pos {
    /// Latitude (radians)
    lat: f64,
    /// Longitude (radians)
    lon: f64,
}

/// Web mercator (EPSG:3857) position.
#[derive(Clone, Copy, Debug)]
pub struct WebMercatorPos {
    /// X coordinate (meters)
    x: f64,
    /// Y coordinate (meters)
    y: f64,
}

/// Limit a value to valid range
fn limit_value(val: f64, limit: f64) -> f64 {
    val.min(limit).max(-limit)
}

impl Wgs84Pos {
    /// Equatorial radius of Earth as defined by WGS-84
    const EQUATORIAL_RADIUS_M: f64 = 6378137.0;

    /// Polar radius of Earth as defined by WGS-84
    const POLAR_RADIUS_M: f64 = 6356752.314245;

    /// Mean radius (meters) as defined by IUGG (1980)
    fn mean_radius_m() -> f64 {
        (Wgs84Pos::EQUATORIAL_RADIUS_M * 2.0 + Wgs84Pos::POLAR_RADIUS_M) / 3.0
    }

    /// Create a new WGS-84 position
    pub fn new(lat_deg: f64, lon_deg: f64) -> Self {
        let lat_deg = limit_value(lat_deg, 90.0);
        let lon_deg = limit_value(lon_deg, 180.0);
        let lat = lat_deg.to_radians();
        let lon = lon_deg.to_radians();
        Wgs84Pos { lat, lon }
    }

    /// Get the latitude in degrees
    pub fn lat_deg(&self) -> f64 {
        self.lat.to_degrees()
    }

    /// Get the longitude in degrees
    pub fn lon_deg(&self) -> f64 {
        self.lon.to_degrees()
    }

    /// Calculate the distance to another position (meters).
    pub fn distance_haversine(&self, other: &Self) -> f64 {
        let dlat = other.lat - self.lat;
        let dlon = other.lon - self.lon;
        let sdlat2 = (dlat / 2.0).sin();
        let coslat = self.lat.cos() * other.lat.cos();
        let sdlon2 = (dlon / 2.0).sin();
        let a = sdlat2 * sdlat2 + coslat * sdlon2 * sdlon2;
        let c = 2.0 * a.sqrt().asin();
        c * Wgs84Pos::mean_radius_m()
    }
}

impl WebMercatorPos {
    /// Maximum latitude for web mercator
    const MAX_LATITUDE: f64 = 85.05112878;

    /// Create a new web mercator position
    pub fn new(x: f64, y: f64) -> Self {
        WebMercatorPos { x, y }
    }
}

impl From<Wgs84Pos> for WebMercatorPos {
    fn from(pos: Wgs84Pos) -> Self {
        let radius = Wgs84Pos::EQUATORIAL_RADIUS_M;
        let x = pos.lon * radius;
        let lat = limit_value(pos.lat_deg(), WebMercatorPos::MAX_LATITUDE);
        let rlat = (lat + 90.0).to_radians() / 2.0;
        let y = rlat.tan().ln() * radius;
        WebMercatorPos::new(x, y)
    }
}

impl From<WebMercatorPos> for Wgs84Pos {
    fn from(pos: WebMercatorPos) -> Self {
        let radius = Wgs84Pos::EQUATORIAL_RADIUS_M;
        let rlat = (pos.y / radius).exp().atan();
        let lat = (rlat * 2.0).to_degrees() - 90.0;
        let lon = (pos.x / radius).to_degrees();
        debug_assert!(lat >= -WebMercatorPos::MAX_LATITUDE);
        debug_assert!(lat <= WebMercatorPos::MAX_LATITUDE);
        Wgs84Pos::new(lat, lon)
    }
}

impl From<WebMercatorPos> for Pt64 {
    fn from(pos: WebMercatorPos) -> Self {
        Self(pos.x, pos.y)
    }
}

#[cfg(test)]
mod test {
    use super::*;

    const EPSILON: f64 = 0.000000002;

    fn near(v0: f64, v1: f64) -> bool {
        v0 - EPSILON <= v1 && v0 + EPSILON >= v1
    }

    #[test]
    fn mean_radius() {
        let r = Wgs84Pos::mean_radius_m();
        assert!(near(r, 6371008.771415));
    }

    #[test]
    fn positions() {
        // Minnesotaa
        check_pos(45.0, -93.0, -10352712.643774442, 5621521.486192066);
        // Minnesotaa
        check_pos(45.0, -94.0, -10464032.134567715, 5621521.486192066);
        // California
        check_pos(39.0, -122.0, -13580977.876779376, 4721671.572580107);
        // New Zealand
        check_pos(-45.0, 173.0, 19258271.907236326, -5621521.486192067);
    }

    fn check_pos(lat: f64, lon: f64, x: f64, y: f64) {
        let pos: WebMercatorPos = Wgs84Pos::new(lat, lon).into();
        assert!(near(pos.x, x));
        assert!(near(pos.y, y));
        let pos: Wgs84Pos = pos.into();
        assert!(near(pos.lat_deg(), lat));
        assert!(near(pos.lon_deg(), lon));
    }

    #[test]
    fn distance() {
        let p = Wgs84Pos::new(45.0, -93.0);
        check_dist(&p, 45.0, -93.1, 7862.678992510984);
        check_dist(&p, 44.9, -93.1, 13622.518673490680);
        check_dist(&p, 44.9, -93.0, 11119.507973463069);
        check_dist(&p, 45.1, -93.0, 11119.507973463777);
    }

    fn check_dist(p: &Wgs84Pos, lat: f64, lon: f64, dist: f64) {
        let po = Wgs84Pos::new(lat, lon);
        let dh = p.distance_haversine(&po);
        assert!(near(dist, dh));
    }
}
