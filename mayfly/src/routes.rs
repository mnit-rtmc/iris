// routes.rs
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
use crate::binned::{
    CountData, HeadwayData, LengthData, OccupancyData, SpeedData, TrafficData,
};
use crate::error::{Error, Result};
use crate::traffic::Traffic;
use crate::vehicle::{VehLog, VehicleFilter};
use axum::Router;
use axum::extract::Query;
use axum::response::IntoResponse;
use axum::routing::get;
use chrono::{Local, NaiveDate, TimeDelta};
use serde::Deserialize;
use std::collections::HashSet;
use std::fmt::{Display, Write};
use std::io::{ErrorKind, Read as _};
use std::marker::PhantomData;
use std::path::{Path, PathBuf};
use tokio::fs::{File, read_dir};
use tokio::io::AsyncReadExt;
use tokio::task;
use zip::result::ZipError;

/// Base traffic archive path
const BASE_PATH: &str = "/var/lib/iris/traffic";

/// Default district ID
const DISTRICT_DEFAULT: &str = "tms";

/// Traffic file extension
const DEXT: &str = ".traffic";

/// Traffic file extension without dot
const EXT: &str = "traffic";

/// JSON vec of values
struct JsonVec {
    value: String,
}

/// Query parameters for years
#[derive(Deserialize)]
struct Years {
    /// District ID
    district: Option<String>,
}

/// Query parameters for dates
#[derive(Deserialize)]
struct Dates {
    /// District ID
    district: Option<String>,
    /// Year to query
    year: String,
}

/// Query parameters for corridors with detectors
#[derive(Deserialize)]
struct Corridors {
    /// District ID
    district: Option<String>,
    /// Date (8-character yyyyMMdd)
    date: String,
}

/// Query parameters for detectors with archived data
#[derive(Deserialize)]
struct Detectors {
    /// District ID
    district: Option<String>,
    /// Date (8-character yyyyMMdd)
    date: String,
}

/// Query parameters for archived traffic data
#[derive(Deserialize)]
struct Traf<T: TrafficData> {
    /// Traffic data type
    _data: Option<PhantomData<T>>,
    /// District ID
    district: Option<String>,
    /// Date (8-character yyyyMMdd)
    date: String,
    /// Detector ID
    detector: String,
    /// Minimum vehicle length (logged vehicles)
    length_ft_min: Option<u32>,
    /// Maximum vehicle length (logged vehicles)
    length_ft_max: Option<u32>,
    /// Minimum recorded speed (logged vehicles)
    speed_mph_min: Option<u32>,
    /// Maximum recorded speed (logged vehicles)
    speed_mph_max: Option<u32>,
    /// Minimum headway (logged vehicles)
    headway_sec_min: Option<f32>,
    /// Maximum headway (logged vehicles)
    headway_sec_max: Option<f32>,
    /// Binning interval (TODO)
    _bin_secs: Option<u32>,
}

impl Default for JsonVec {
    fn default() -> Self {
        JsonVec {
            value: String::with_capacity(2880 * 4),
        }
    }
}

impl JsonVec {
    /// Create a new JSON vec
    fn new() -> Self {
        Self::default()
    }

    /// Write delimiter
    fn write_delim(&mut self) {
        if self.value.is_empty() {
            self.value.push('[');
        } else {
            self.value.push(',');
        }
    }

    /// Write one value
    fn write<D: Display>(&mut self, value: D) {
        self.write_delim();
        write!(self.value, "{value}").unwrap();
    }

    /// Write one quoted value
    fn write_quoted<D: Display>(&mut self, value: D) {
        self.write_delim();
        write!(self.value, "\"{value}\"").unwrap();
    }
}

impl From<JsonVec> for String {
    fn from(vec: JsonVec) -> Self {
        let mut v = vec.value;
        if v.is_empty() {
            v.push_str("[]");
        } else {
            v.push(']');
        }
        v
    }
}

/// Create a JSON response
fn json_resp(
    json: String,
    is_recent: bool,
) -> ([(http::header::HeaderName, &'static str); 2], String) {
    let max_age = if is_recent {
        "max-age=30"
    } else {
        // 100 weeks => 100 * 7 * 24 * 60 * 60
        "max-age=60480000"
    };
    (
        [
            (http::header::CACHE_CONTROL, max_age),
            (http::header::CONTENT_TYPE, "application/json"),
        ],
        json,
    )
}

/// Get path to district directory
fn district_path(district: &Option<String>) -> PathBuf {
    let mut path = PathBuf::from(BASE_PATH);
    path.push(district.as_deref().unwrap_or(DISTRICT_DEFAULT));
    path
}

/// Get path to a year directory
fn year_path(district: &Option<String>, year: &str) -> PathBuf {
    assert_eq!(year.len(), 4);
    let mut path = district_path(district);
    path.push(year);
    path
}

/// Get path to a date directory
fn date_path(district: &Option<String>, date: &str) -> Result<PathBuf> {
    parse_date(date)?;
    let mut path = year_path(district, date.get(..4).unwrap_or(""));
    path.push(date);
    Ok(path)
}

/// Get path to a date traffic (zip) file
fn zip_path(district: &Option<String>, date: &str) -> Result<PathBuf> {
    parse_date(date)?;
    let mut path = date_path(district, date)?;
    path.set_extension(EXT);
    Ok(path)
}

/// Get file stem
fn file_stem(nm: &str) -> Option<&str> {
    Path::new(nm).file_stem().and_then(|st| st.to_str())
}

/// Scan entries in a directory
async fn scan_dir<P>(path: &P, check: fn(&str, bool) -> bool) -> Result<String>
where
    P: AsRef<Path>,
{
    let mut entries = read_dir(path).await?;
    let mut names = HashSet::new();
    while let Some(entry) = entries.next_entry().await? {
        let tp = entry.file_type().await?;
        if !tp.is_symlink()
            && let Some(nm) = entry.file_name().to_str()
            && check(nm, tp.is_dir())
            && let Some(stem) = file_stem(nm)
        {
            names.insert(stem.to_string());
        }
    }
    let mut json = JsonVec::new();
    for name in names {
        json.write_quoted(name);
    }
    Ok(String::from(json))
}

/// Scan entries in a zip file
async fn scan_zip<P>(path: &P, check: fn(&str, bool) -> bool) -> Result<String>
where
    P: AsRef<Path>,
{
    let path = PathBuf::from(path.as_ref());
    task::spawn_blocking(move || scan_zip_blocking(path, check)).await?
}

/// Scan entries in a zip file (blocking)
fn scan_zip_blocking(
    path: PathBuf,
    check: fn(&str, bool) -> bool,
) -> Result<String> {
    let traffic = Traffic::new(&path)?;
    let mut names = HashSet::new();
    for nm in traffic.file_names() {
        if check(nm, false)
            && let Some(stem) = file_stem(nm)
        {
            names.insert(stem.to_string());
        }
    }
    let mut json = JsonVec::new();
    for name in names {
        json.write_quoted(name);
    }
    Ok(String::from(json))
}

/// Parse year parameter
fn parse_year(year: &str) -> Result<i32> {
    match year.parse() {
        Ok(y) if (1900..=9999).contains(&y) => Ok(y),
        _ => Err(Error::InvalidQuery("year")),
    }
}

/// Parse month parameter
fn parse_month(month: &str) -> Result<u32> {
    match month.parse() {
        Ok(m) if (1..=12).contains(&m) => Ok(m),
        _ => Err(Error::InvalidQuery("month")),
    }
}

/// Parse day parameter
fn parse_day(day: &str) -> Result<u32> {
    match day.parse() {
        Ok(d) if (1..=31).contains(&d) => Ok(d),
        _ => Err(Error::InvalidQuery("day")),
    }
}

/// Check if a date is valid
fn parse_date(date: &str) -> Result<NaiveDate> {
    if date.len() == 8 {
        let year = parse_year(date.get(..4).unwrap_or(""))?;
        let month = parse_month(date.get(4..6).unwrap_or(""))?;
        let day = parse_day(date.get(6..8).unwrap_or(""))?;
        if let Some(date) = NaiveDate::from_ymd_opt(year, month, day) {
            return Ok(date);
        }
    }
    Err(Error::InvalidQuery("date"))
}

/// Build route for districts
pub fn districts_get() -> Router {
    async fn handler() -> impl IntoResponse {
        log::info!("GET /districts");
        let path = Path::new(BASE_PATH);
        scan_dir(&path, |_nm, is_dir| is_dir).await
    }
    Router::new().route("/districts", get(handler))
}

/// Build route for years
pub fn years_get() -> Router {
    async fn handler(years: Query<Years>) -> impl IntoResponse {
        log::info!("GET /years");
        let path = district_path(&years.0.district);
        scan_dir(&path, check_year).await
    }
    Router::new().route("/years", get(handler))
}

/// Check for valid year
fn check_year(nm: &str, is_dir: bool) -> bool {
    is_dir && parse_year(nm).is_ok()
}

impl Dates {
    /// Get path to directory
    fn path(&self) -> Result<PathBuf> {
        parse_year(&self.year)?;
        Ok(year_path(&self.district, &self.year))
    }
}

/// Build route for dates
pub fn dates_get() -> Router {
    async fn handler(dates: Query<Dates>) -> impl IntoResponse {
        log::info!("GET /dates");
        let path = dates.0.path()?;
        scan_dir(&path, check_date).await
    }
    Router::new().route("/dates", get(handler))
}

/// Check for valid date files
fn check_date(nm: &str, is_dir: bool) -> bool {
    let dt = if is_dir {
        nm
    } else if nm.len() == 16 && nm.ends_with(DEXT) {
        nm.get(..8).unwrap_or("")
    } else {
        ""
    };
    parse_date(dt).is_ok()
}

impl Corridors {
    /// Get path to directory
    fn path(&self) -> Result<PathBuf> {
        let mut path = date_path(&self.district, &self.date)?;
        path.push("corridors");
        Ok(path)
    }

    /// Get path to (zip) file
    fn zip_path(&self) -> Result<PathBuf> {
        zip_path(&self.district, &self.date)
    }
}

/// Build route for corridors
pub fn corridors_get() -> Router {
    async fn handler(corridors: Query<Corridors>) -> impl IntoResponse {
        log::info!("GET /corridors");
        let path = corridors.0.zip_path()?;
        match scan_zip(&path, check_corridor).await {
            Err(Error::Io(e)) if e.kind() == ErrorKind::NotFound => {
                let path = corridors.0.path()?;
                scan_dir(&path, check_corridor).await
            }
            res => res,
        }
    }
    Router::new().route("/corridors", get(handler))
}

/// Check for corridor IDs
fn check_corridor(nm: &str, dir: bool) -> bool {
    !dir && !nm.contains('.') && nm.contains('_')
}

impl Detectors {
    /// Get path to directory
    fn path(&self) -> Result<PathBuf> {
        date_path(&self.district, &self.date)
    }

    /// Get path to (zip) file
    fn zip_path(&self) -> Result<PathBuf> {
        zip_path(&self.district, &self.date)
    }
}

/// Lookup detectors with archived data
pub fn detectors_get() -> Router {
    async fn handler(detectors: Query<Detectors>) -> impl IntoResponse {
        log::info!("GET /detectors");
        let path = detectors.0.zip_path()?;
        match scan_zip(&path, check_detector).await {
            Err(Error::Io(e)) if e.kind() == ErrorKind::NotFound => {
                let path = detectors.0.path()?;
                scan_dir(&path, check_detector).await
            }
            res => res,
        }
    }
    Router::new().route("/detectors", get(handler))
}

/// Check for detector IDs
fn check_detector(nm: &str, is_dir: bool) -> bool {
    !is_dir && {
        let path = Path::new(nm);
        path.extension()
            .and_then(|ext| ext.to_str())
            .is_some_and(is_ext_valid)
    }
}

/// Check a archive file extension
fn is_ext_valid(ext: &str) -> bool {
    const EXTS: &[&str] = &["vlog", "v30", "c30", "s30"];
    EXTS.contains(&ext)
}

impl<T> Traf<T>
where
    T: TrafficData + Sync + Send + 'static,
{
    /// Lookup data from a zip archive
    async fn lookup_zipped(self, traffic: Traffic) -> Result<String> {
        task::spawn_blocking(|| self.lookup_zipped_blocking(traffic)).await?
    }

    /// Lookup data from a zip archive (blocking)
    fn lookup_zipped_blocking(self, mut traffic: Traffic) -> Result<String> {
        if !self.filter().is_filtered() {
            match self.lookup_zipped_bin(&mut traffic) {
                Err(Error::Zip(ZipError::FileNotFound)) => (),
                res => return res,
            }
        }
        self.lookup_zipped_vlog(&mut traffic)
    }

    /// Lookup archived data from 30-second binned data (blocking)
    fn lookup_zipped_bin(&self, traffic: &mut Traffic) -> Result<String> {
        let name = self.binned_file_name();
        let mut zf = traffic.by_name(&name)?;
        log::info!("opened {name} in {}.{EXT}", self.date);
        let mut buf = Self::make_bin_buffer(zf.size())?;
        zf.read_exact(&mut buf)?;
        Ok(self.make_binned_body(buf))
    }

    /// Read vehicle log data from a zip file (blocking)
    fn lookup_zipped_vlog(&self, traffic: &mut Traffic) -> Result<String> {
        let name = self.vlog_file_name();
        let zf = traffic.by_name(&name)?;
        log::info!("opened {name} in {}.{EXT}", self.date);
        let vlog = VehLog::from_reader_blocking(zf)?;
        self.make_vlog_body(vlog)
    }

    /// Lookup data from file system (unzipped)
    async fn lookup_unzipped(&self) -> Result<String> {
        if !self.filter().is_filtered() {
            match self.lookup_unzipped_bin().await {
                Err(Error::Io(e)) if e.kind() == ErrorKind::NotFound => (),
                res => return res,
            }
        }
        self.lookup_unzipped_vlog().await
    }

    /// Lookup unzipped data from 30-second binned data
    async fn lookup_unzipped_bin(&self) -> Result<String> {
        let mut path = self.date_path()?;
        path.push(self.binned_file_name());
        let mut file = File::open(&path).await?;
        let metadata = file.metadata().await?;
        log::info!("opened {path:?}");
        let mut buf = Self::make_bin_buffer(metadata.len())?;
        file.read_exact(&mut buf).await?;
        Ok(self.make_binned_body(buf))
    }

    /// Lookup unzipped data from vehicle log file
    async fn lookup_unzipped_vlog(&self) -> Result<String> {
        let mut path = self.date_path()?;
        path.push(self.vlog_file_name());
        let file = File::open(&path).await?;
        log::info!("opened {path:?}");
        let vlog = VehLog::from_reader_async(file).await?;
        self.make_vlog_body(vlog)
    }

    /// Create a vehicle filter
    fn filter(&self) -> VehicleFilter {
        VehicleFilter::default()
            .with_length_ft_min(self.length_ft_min)
            .with_length_ft_max(self.length_ft_max)
            .with_speed_mph_min(self.speed_mph_min)
            .with_speed_mph_max(self.speed_mph_max)
            .with_headway_sec_min(self.headway_sec_min)
            .with_headway_sec_max(self.headway_sec_max)
    }

    /// Get vehicle log file name
    fn vlog_file_name(&self) -> String {
        format!("{}.vlog", self.detector)
    }

    /// Make buffer to hold 30-second binned data
    fn make_bin_buffer(len: u64) -> Result<Vec<u8>> {
        let sz = 2880 * T::bin_bytes();
        if len == sz as u64 {
            Ok(vec![0; sz])
        } else {
            Err(Error::InvalidData("bin"))
        }
    }

    /// Make body from binned buffer
    fn make_binned_body(&self, buf: Vec<u8>) -> String {
        let mut vec = JsonVec::new();
        for val in buf.chunks_exact(T::bin_bytes()) {
            vec.write(T::unpack(val));
        }
        String::from(vec)
    }

    /// Make body from vehicle log
    fn make_vlog_body(&self, vlog: VehLog) -> Result<String> {
        let mut vec = JsonVec::new();
        let mut valid = false;
        for val in vlog.binned_iter::<T>(30, self.filter()) {
            if !valid {
                if !format!("{}", val).contains("null") {
                    valid = true;
                };
            };
            vec.write(val);
        }
        if valid {
            return Ok(String::from(vec));
        }

        // Return "Not Found" to match pre-vlog behavior
        Err(Error::Zip(ZipError::FileNotFound))
    }

    /// Get path to (zip) file
    fn zip_path(&self) -> Result<PathBuf> {
        zip_path(&self.district, &self.date)
    }

    /// Get binned file name
    fn binned_file_name(&self) -> String {
        format!("{}.{}", self.detector, T::binned_ext())
    }

    /// Get path containing archive data for one date
    fn date_path(&self) -> Result<PathBuf> {
        date_path(&self.district, &self.date)
    }

    /// Check if date is "recent" for max-age cache control heder
    fn is_recent(&self) -> Result<bool> {
        let date = parse_date(&self.date)?;
        let today = Local::now().date_naive();
        Ok(today < date + TimeDelta::try_days(2).unwrap())
    }
}

/// Handle a generic request for traffic data
async fn traf_handler<T>(traf: Traf<T>) -> impl IntoResponse
where
    T: TrafficData + Sync + Send + 'static,
{
    let path = traf.zip_path()?;
    let is_recent = traf.is_recent()?;
    let body = match Traffic::new(&path) {
        Ok(traffic) => traf.lookup_zipped(traffic).await,
        _ => traf.lookup_unzipped().await,
    }?;
    Result::<_>::Ok(json_resp(body, is_recent))
}

/// Lookup archived count data.
pub fn counts_get() -> Router {
    async fn handler(traf: Query<Traf<CountData>>) -> impl IntoResponse {
        log::info!("GET /counts");
        traf_handler(traf.0).await
    }
    Router::new().route("/counts", get(handler))
}

/// Lookup archived headway data.
pub fn headway_get() -> Router {
    async fn handler(traf: Query<Traf<HeadwayData>>) -> impl IntoResponse {
        log::info!("GET /headway");
        traf_handler(traf.0).await
    }
    Router::new().route("/headway", get(handler))
}

/// Lookup archived length data.
pub fn length_get() -> Router {
    async fn handler(traf: Query<Traf<LengthData>>) -> impl IntoResponse {
        log::info!("GET /length");
        traf_handler(traf.0).await
    }
    Router::new().route("/length", get(handler))
}

/// Lookup archived occupancy data.
pub fn occupancy_get() -> Router {
    async fn handler(traf: Query<Traf<OccupancyData>>) -> impl IntoResponse {
        log::info!("GET /occupancy");
        traf_handler(traf.0).await
    }
    Router::new().route("/occupancy", get(handler))
}

/// Lookup archived speed data.
pub fn speed_get() -> Router {
    async fn handler(traf: Query<Traf<SpeedData>>) -> impl IntoResponse {
        log::info!("GET /speed");
        traf_handler(traf.0).await
    }
    Router::new().route("/speed", get(handler))
}
