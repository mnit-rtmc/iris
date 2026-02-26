// routes.rs
//
// Copyright (c) 2019-2026  Minnesota Department of Transportation
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
    BinIter, CountData, HeadwayData, LengthData, OccupancyData, SpeedData,
    TrafficData, VehicleFilter,
};
use crate::error::{Error, Result};
use crate::traffic::Traffic;
use crate::{vlg, vlog};
use axum::Router;
use axum::extract::Query;
use axum::response::IntoResponse;
use axum::routing::get;
use resin::event::{Stamp, VehEvent};
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

/// Intervals per hour (30-second)
const INTERVALS_PER_HOUR: u16 = 2 * 60;

/// Number of feet per mile
const FEET_PER_MILE: f32 = 5280.0;

/// Recent age for max-age cache control header (2 days)
const RECENT_AGE_MS: i128 = 2 * 24 * 60 * 60 * 1000;

/// Detector configuration
#[allow(unused)]
#[derive(Deserialize)]
struct DetectorConfig {
    name: String,
    r_node: String,
    cor_id: Option<String>,
    lane_number: u16,
    lane_code: String,
    speed_limit: u16,
}

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

/// Check if a date is valid
fn parse_date(date: &str) -> Result<Stamp> {
    match Stamp::try_from_date(date) {
        Some(stamp) => Ok(stamp),
        None => Err(Error::InvalidQuery("date")),
    }
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
    const EXTS: &[&str] = &["vlg", "vlog", "v30", "c30", "s30"];
    EXTS.contains(&ext)
}

impl<T> Traf<T>
where
    T: TrafficData + Sync + Send + 'static,
{
    /// Create new traf query from another data type
    fn new_from<O>(other: &Traf<O>) -> Self
    where
        O: TrafficData + Sync + Send + 'static,
    {
        Traf {
            _data: None,
            district: other.district.clone(),
            date: other.date.clone(),
            detector: other.detector.clone(),
            length_ft_min: other.length_ft_min,
            length_ft_max: other.length_ft_max,
            speed_mph_min: other.speed_mph_min,
            speed_mph_max: other.speed_mph_max,
            headway_sec_min: other.headway_sec_min,
            headway_sec_max: other.headway_sec_max,
            _bin_secs: other._bin_secs,
        }
    }

    /// Lookup traffic data
    async fn lookup(self) -> Result<Vec<T>> {
        let path = self.zip_path()?;
        match Traffic::new(&path) {
            Ok(traffic) => self.lookup_zipped(traffic).await,
            _ => self.lookup_unzipped().await,
        }
    }

    /// Lookup traffic data from a zip archive
    async fn lookup_zipped(self, traffic: Traffic) -> Result<Vec<T>> {
        task::spawn_blocking(|| self.lookup_zipped_blocking(traffic)).await?
    }

    /// Lookup data from a zip archive (blocking)
    fn lookup_zipped_blocking(self, mut traffic: Traffic) -> Result<Vec<T>> {
        if !self.filter().is_filtered() {
            match self.lookup_zipped_bin(&mut traffic) {
                Err(Error::Zip(ZipError::FileNotFound)) => (),
                res => return res,
            }
        }
        let events = self.lookup_zipped_events(&mut traffic)?;
        log::debug!("binning events for {} on {}", self.detector, self.date);
        let bi = BinIter::new(30, &events, self.filter());
        Ok(bi.collect())
    }

    /// Lookup archived data from 30-second binned data (blocking)
    fn lookup_zipped_bin(&self, traffic: &mut Traffic) -> Result<Vec<T>> {
        let name = self.binned_file_name();
        let mut zf = traffic.by_name(&name)?;
        log::debug!("opened {name} in {}.{EXT}", self.date);
        let mut buf = Self::make_bin_buffer(zf.size())?;
        zf.read_exact(&mut buf)?;
        Ok(buf
            .chunks_exact(T::bin_bytes())
            .map(|v| T::unpack(v))
            .collect())
    }

    /// Lookup vehicle events from a zip file (blocking)
    fn lookup_zipped_events(
        &self,
        traffic: &mut Traffic,
    ) -> Result<Vec<VehEvent>> {
        match self.lookup_zipped_vlg(traffic) {
            Err(Error::Zip(ZipError::FileNotFound)) => (),
            res => return res,
        }
        self.lookup_zipped_vlog(traffic)
    }

    /// Lookup events from a zipped `.vlg` file (blocking)
    fn lookup_zipped_vlg(
        &self,
        traffic: &mut Traffic,
    ) -> Result<Vec<VehEvent>> {
        let name = self.vlg_file_name();
        let zf = traffic.by_name(&name)?;
        log::debug!("opened {name} in {}.{EXT}", self.date);
        vlg::read_blocking(&self.date, zf)
    }

    /// Lookup events from a zipped `.vlog` file (blocking)
    fn lookup_zipped_vlog(
        &self,
        traffic: &mut Traffic,
    ) -> Result<Vec<VehEvent>> {
        let name = self.vlog_file_name();
        let zf = traffic.by_name(&name)?;
        log::debug!("opened {name} in {}.{EXT}", self.date);
        vlog::read_blocking(&self.date, zf)
    }

    /// Lookup data from file system (unzipped)
    async fn lookup_unzipped(&self) -> Result<Vec<T>> {
        if !self.filter().is_filtered() {
            match self.lookup_unzipped_bin().await {
                Err(Error::Io(e)) if e.kind() == ErrorKind::NotFound => (),
                res => return res,
            }
        }
        let events = self.lookup_unzipped_events().await?;
        log::debug!("binning events for {} on {}", self.detector, self.date);
        let bi = BinIter::new(30, &events, self.filter());
        Ok(bi.collect())
    }

    /// Lookup unzipped data from 30-second binned data
    async fn lookup_unzipped_bin(&self) -> Result<Vec<T>> {
        let mut path = self.date_path()?;
        path.push(self.binned_file_name());
        let mut file = File::open(&path).await?;
        let metadata = file.metadata().await?;
        log::debug!("opened {path:?}");
        let mut buf = Self::make_bin_buffer(metadata.len())?;
        file.read_exact(&mut buf).await?;
        Ok(buf
            .chunks_exact(T::bin_bytes())
            .map(|v| T::unpack(v))
            .collect())
    }

    /// Lookup vehicle events from an unzipped file
    async fn lookup_unzipped_events(&self) -> Result<Vec<VehEvent>> {
        match self.lookup_unzipped_vlg().await {
            Err(Error::Io(e)) if e.kind() == ErrorKind::NotFound => (),
            res => return res,
        }
        self.lookup_unzipped_vlog().await
    }

    /// Lookup events from an unzipped `.vlg` file
    async fn lookup_unzipped_vlg(&self) -> Result<Vec<VehEvent>> {
        let mut path = self.date_path()?;
        path.push(self.vlg_file_name());
        let file = File::open(&path).await?;
        log::debug!("opened {path:?}");
        vlg::read_async(&self.date, file).await
    }

    /// Lookup events from an unzipped `.vlog` file
    async fn lookup_unzipped_vlog(&self) -> Result<Vec<VehEvent>> {
        let mut path = self.date_path()?;
        path.push(self.vlog_file_name());
        let file = File::open(&path).await?;
        log::debug!("opened {path:?}");
        vlog::read_async(&self.date, file).await
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

    /// Get `.vlg` file name
    fn vlg_file_name(&self) -> String {
        format!("{}.vlg", self.detector)
    }

    /// Get `.vlog` file name
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
        let elapsed = Stamp::now().elapsed(&date);
        Ok(elapsed < RECENT_AGE_MS)
    }

    /// Load detector free-flow speed
    async fn load_free_flow(&self) -> Result<u16> {
        let configs = load_detector_configs().await?;
        for det in configs {
            if det.name == self.detector {
                let limit = det.speed_limit;
                return Ok(match det.lane_number {
                    0 => limit,
                    1 => limit + 5,
                    _ => limit + 10,
                });
            }
        }
        Err(Error::InvalidData("detector_pub"))
    }
}

/// Load detector configurations
async fn load_detector_configs() -> Result<Vec<DetectorConfig>> {
    let mut path = PathBuf::new();
    path.push("/var/lib/iris/web/detector_pub");
    let buf = tokio::fs::read(path).await?;
    Ok(serde_json::from_slice(&buf)?)
}

/// Handle a generic request for traffic data
async fn traf_handler<T>(traf: Traf<T>) -> impl IntoResponse
where
    T: TrafficData + Sync + Send + 'static,
{
    let is_recent = traf.is_recent()?;
    let data = traf.lookup().await?;
    let body = make_body(&data)?;
    Result::<_>::Ok(json_resp(body, is_recent))
}

/// Make body from traffic data
fn make_body<T: TrafficData>(data: &[T]) -> Result<String> {
    let mut vec = JsonVec::new();
    let mut any_valid = false;
    for val in data {
        if !any_valid && val.value().is_some() {
            any_valid = true;
        }
        vec.write(val);
    }
    any_valid
        .then(|| String::from(vec))
        .ok_or(Error::Io(ErrorKind::NotFound.into()))
}

/// Lookup archived count data.
pub fn counts_get() -> Router {
    async fn handler(traf: Query<Traf<CountData>>) -> impl IntoResponse {
        log::info!("GET /counts for {} on {}", traf.detector, traf.date);
        traf_handler(traf.0).await
    }
    Router::new().route("/counts", get(handler))
}

/// Lookup archived headway data.
pub fn headway_get() -> Router {
    async fn handler(traf: Query<Traf<HeadwayData>>) -> impl IntoResponse {
        log::info!("GET /headway for {} on {}", traf.detector, traf.date);
        traf_handler(traf.0).await
    }
    Router::new().route("/headway", get(handler))
}

/// Lookup archived occupancy data.
pub fn occupancy_get() -> Router {
    async fn handler(traf: Query<Traf<OccupancyData>>) -> impl IntoResponse {
        log::info!("GET /occupancy for {} on {}", traf.detector, traf.date);
        traf_handler(traf.0).await
    }
    Router::new().route("/occupancy", get(handler))
}

/// Lookup archived length data.
pub fn length_get() -> Router {
    async fn handler(traf: Query<Traf<LengthData>>) -> impl IntoResponse {
        log::info!("GET /length for {} on {}", traf.detector, traf.date);
        traf_handler(traf.0).await
    }
    Router::new().route("/length", get(handler))
}

/// Lookup archived speed data.
pub fn speed_get() -> Router {
    async fn handler(traf: Query<Traf<SpeedData>>) -> impl IntoResponse {
        log::info!("GET /speed for {} on {}", traf.detector, traf.date);
        traf_handler(traf.0).await
    }
    Router::new().route("/speed", get(handler))
}

/// Lookup estimated speed data.
pub fn espeed_get() -> Router {
    async fn handler(traf: Query<Traf<SpeedData>>) -> impl IntoResponse {
        log::info!("GET /espeed for {} on {}", traf.detector, traf.date);
        let is_recent = traf.is_recent()?;
        let free_flow = traf.load_free_flow().await?;
        let traf_count = Traf::<CountData>::new_from(&traf.0);
        let traf_occ = Traf::<OccupancyData>::new_from(&traf.0);
        let counts = traf_count.lookup().await?;
        let occ = traf_occ.lookup().await?;
        // guess traffic conditions
        let conditions: Vec<_> = counts
            .iter()
            .zip(occ.iter())
            .map(|(c, o)| TrafficCondition::guess(free_flow, c, o))
            .collect();
        // calculate adjusted field length
        let mut occ_free = 0.0;
        let mut dens_free = 0.0;
        let mut intervals: u16 = 0;
        conditions
            .iter()
            .zip(counts.iter())
            .zip(occ.iter())
            .for_each(|((con, c), o)| {
                if let (Some(TrafficCondition::FreeFlow), Some(c), Some(o)) =
                    (con, c.value(), o.value())
                {
                    occ_free += f32::from(o) / 30_000.0;
                    dens_free += guess_density(free_flow, c);
                    intervals += 1;
                }
            });
        if intervals > 0 {
            let intervals = f32::from(intervals);
            occ_free /= intervals;
            dens_free /= intervals;
            // calculate adjusted field length
            if let Some(field_len) = field_len_ft(occ_free, dens_free) {
                // estimate speeds
                let speeds: Vec<_> = conditions
                    .iter()
                    .zip(counts.iter())
                    .zip(occ.iter())
                    .map(|((con, c), o)| estimate_speed(field_len, con, c, o))
                    .collect();
                let body = make_body(&speeds)?;
                return Result::<_>::Ok(json_resp(body, is_recent));
            }
        }
        Err(Error::Io(ErrorKind::NotFound.into()))
    }
    Router::new().route("/espeed", get(handler))
}

/// Guess density, assuming free-flow speed
fn guess_density(free_flow_mph: u16, count: u16) -> f32 {
    let flow = f32::from(count * INTERVALS_PER_HOUR);
    // density = flow / speed
    flow / f32::from(free_flow_mph)
}

/// Calculate adjusted field length, based on "free-flow" density
fn field_len_ft(occ: f32, dens_free: f32) -> Option<f32> {
    // length (ft/veh) = occupancy (%) * 5280 (ft/mi) / density (veh/mi)
    let len_ft_free = (occ * FEET_PER_MILE) / dens_free;
    if len_ft_free > 0.0 && len_ft_free < 65_535.0 {
        return Some(len_ft_free);
    }
    None
}

/// Estimate speed for one 30-second interval
fn estimate_speed(
    avg_field_len: f32,
    con: &Option<TrafficCondition>,
    count: &CountData,
    occ: &OccupancyData,
) -> SpeedData {
    if let (Some(count), Some(occ)) = (count.value(), occ.value()) {
        let field_len = avg_field_len
            + con.unwrap_or(TrafficCondition::FreeFlow).len_adjust();
        // convert occ from ms to percent
        let occ = f32::from(occ) / 30_000.0;
        // density (veh/mi) = occupancy (%) * 5280 (ft/mi) / field_len (ft/veh)
        let density = (occ * FEET_PER_MILE) / field_len;
        if density > 0.0 {
            // speed (mi/hr) = flow (veh/hr) / density (veh/mi)
            let speed = f32::from(count * INTERVALS_PER_HOUR) / density;
            if speed > 0.0 && speed < 65_535.0 {
                let speed = speed.round() as u16;
                return SpeedData::new(speed);
            }
        }
    }
    SpeedData::default()
}

/// Traffic conditions
#[derive(Clone, Copy, Debug, PartialEq)]
enum TrafficCondition {
    /// Light traffic with vehicles less than 24 ft
    FreeFlow,
    /// Moderate traffic or large vehicles (24 to 36 ft)
    Moderate,
    /// Heavy traffic or very large vehicles (more than 36 ft)
    Heavy,
    /// Congested traffic
    Congested,
}

impl From<u16> for TrafficCondition {
    fn from(len_ft: u16) -> Self {
        match len_ft {
            0..24 => TrafficCondition::FreeFlow,
            24..36 => TrafficCondition::Moderate,
            36..64 => TrafficCondition::Heavy,
            _ => TrafficCondition::Congested,
        }
    }
}

impl TrafficCondition {
    /// Guess the traffic condition
    fn guess(
        free_flow_mph: u16,
        count: &CountData,
        occ: &OccupancyData,
    ) -> Option<Self> {
        if let (Some(c), Some(o)) = (count.value(), occ.value()) {
            let dens_free = guess_density(free_flow_mph, c);
            if dens_free > 0.0 {
                // convert occ from ms to percent
                let occ = f32::from(o) / 30_000.0;
                if let Some(len) = field_len_ft(occ, dens_free) {
                    let len = len.round() as u16;
                    return Some(TrafficCondition::from(len));
                }
            }
        }
        None
    }

    /// Get length adjustment (ft)
    ///
    /// NOTE: Since vehicle length estimates are affected by congestion,
    /// these adjustments are less than expected based on the definitions.
    fn len_adjust(self) -> f32 {
        match self {
            TrafficCondition::FreeFlow => 0.0,
            TrafficCondition::Moderate => 4.0,
            TrafficCondition::Heavy => 9.0,
            TrafficCondition::Congested => 14.0,
        }
    }
}
