// query.rs
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
use crate::common::{Body, Error, Result};
use crate::vehicle::{self, VehicleEvent, VehicleFilter};
use async_std::fs::{read_dir, File};
use async_std::io::{BufReader, ReadExt};
use async_std::path::{Path, PathBuf};
use async_std::prelude::*;
use async_std::stream::StreamExt;
use chrono::{Duration, Local, NaiveDate};
use serde::Deserialize;
use std::io::BufRead as _;
use std::io::Read as _;
use std::marker::PhantomData;
use zip::ZipArchive;

/// Base traffic archive path
const BASE_PATH: &str = "/var/lib/iris/traffic";

/// Default district ID
const DISTRICT_DEFAULT: &str = "tms";

/// Traffic file extension
const DEXT: &str = ".traffic";

/// Traffic file extension without dot
const EXT: &str = "traffic";

/// Query for districts in archive
#[derive(Default, Deserialize)]
pub struct DistrictQuery {}

/// Query for years with archived data
#[derive(Deserialize)]
pub struct YearQuery {
    /// District ID
    district: Option<String>,
}

/// Query for dates with archived data
#[derive(Deserialize)]
pub struct DateQuery {
    /// District ID
    district: Option<String>,
    /// Year to query
    year: String,
}

/// Query for detectors with archived data
#[derive(Deserialize)]
pub struct DetectorQuery {
    /// District ID
    pub district: Option<String>,
    /// Date (8-character yyyyMMdd)
    pub date: String,
}

/// Traffic data type
pub trait TrafficData: Default {
    /// Binned file extension
    fn binned_ext() -> &'static str;

    /// Number of bytes per binned value
    fn bin_bytes() -> usize;

    /// Check binned data length
    fn check_len(len: u64) -> Result<()> {
        if len == 2880 * Self::bin_bytes() as u64 {
            Ok(())
        } else {
            Err(Error::InvalidData)
        }
    }

    /// Unpack one binned value
    fn unpack(val: &[u8]) -> String {
        assert_eq!(val.len(), Self::bin_bytes());
        let value = val[0] as i8;
        if value >= 0 {
            format!("{}", value)
        } else {
            "null".to_owned()
        }
    }

    /// Set reset for traffic data
    fn reset(&mut self);

    /// Add a vehicle to traffic data
    fn vehicle(&mut self, veh: &VehicleEvent);

    /// Get traffic data value as JSON
    fn as_json(&self) -> String;
}

/// Binned vehicle count data
#[derive(Clone, Copy, Default)]
pub struct CountData {
    reset: bool,
    count: u32,
}

/// Binned speed data
#[derive(Clone, Copy, Default)]
pub struct SpeedData {
    total: u32,
    count: u32,
}

/// Binned occupancy data
#[derive(Clone, Copy, Default)]
pub struct OccupancyData {
    reset: bool,
    duration: u32,
}

/// Binned length data
#[derive(Clone, Copy, Default)]
pub struct LengthData {
    total: u32,
    count: u32,
}

/// Query for archived traffic data
#[derive(Deserialize)]
pub struct TrafficQuery<T: TrafficData> {
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
    /// Binning interval (TODO)
    _bin_secs: Option<u32>,
}

/// Get path to district directory (async_std PathBuf)
fn district_path(district: &Option<String>) -> PathBuf {
    let mut path = PathBuf::from(BASE_PATH);
    path.push(district.as_deref().unwrap_or(DISTRICT_DEFAULT));
    path
}

/// Get path to a year directory (async_std PathBuf)
fn year_path(district: &Option<String>, year: &str) -> PathBuf {
    assert_eq!(year.len(), 4);
    let mut path = district_path(district);
    path.push(year);
    path
}

/// Get path to a date directory (async_std PathBuf)
fn date_path(district: &Option<String>, date: &str) -> PathBuf {
    assert_eq!(date.len(), 8);
    let mut path = year_path(district, date.get(..4).unwrap_or(""));
    path.push(date);
    path
}

/// Get path to a date traffic (zip) file (std PathBuf)
fn zip_path(district: &Option<String>, date: &str) -> std::path::PathBuf {
    assert_eq!(date.len(), 8);
    let mut path = std::path::PathBuf::from(BASE_PATH);
    let district = district.as_deref().unwrap_or(DISTRICT_DEFAULT);
    path.push(district);
    path.push(date.get(..4).unwrap_or("")); // year
    path.push(date);
    path.set_extension(EXT);
    path
}

/// Scan entries in a directory
async fn scan_dir(
    path: &Path,
    check: fn(&str, bool) -> Option<String>,
    body: &mut Body,
) -> Result<()> {
    let mut entries = read_dir(path).await.or(Err(Error::NotFound))?;
    while let Some(entry) = entries.next().await {
        if let Ok(entry) = entry {
            if let Ok(tp) = entry.file_type().await {
                if !tp.is_symlink() {
                    if let Some(name) = entry.file_name().to_str() {
                        if let Some(value) = check(name, tp.is_dir()) {
                            body.push(value);
                        }
                    }
                }
            }
        }
    }
    Ok(())
}

/// Get a list of entries in a zip file
fn scan_zip(
    path: &std::path::Path,
    check: fn(&str, bool) -> Option<String>,
    body: &mut Body,
) -> Result<()> {
    let file = std::fs::File::open(path)?;
    let mut zip = ZipArchive::new(file)?;
    for i in 0..zip.len() {
        let zf = zip.by_index(i)?;
        let ent = std::path::Path::new(zf.name());
        if let Some(name) = ent.file_name() {
            if let Some(name) = name.to_str() {
                if let Some(e) = check(name, false) {
                    body.push(e);
                }
            }
        }
    }
    Ok(())
}

/// Parse year parameter
fn parse_year(year: &str) -> Result<i32> {
    match year.parse() {
        Ok(y) if y >= 1900 && y <= 9999 => Ok(y),
        _ => Err(Error::InvalidDate),
    }
}

/// Parse month parameter
fn parse_month(month: &str) -> Result<u32> {
    match month.parse() {
        Ok(m) if m >= 1 && m <= 12 => Ok(m),
        _ => Err(Error::InvalidDate),
    }
}

/// Parse day parameter
fn parse_day(day: &str) -> Result<u32> {
    match day.parse() {
        Ok(d) if d >= 1 && d <= 31 => Ok(d),
        _ => Err(Error::InvalidDate),
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
    Err(Error::InvalidDate)
}

/// Get max age for cache control heder
fn max_age(date: &str) -> Option<u64> {
    if let Ok(date) = parse_date(date) {
        let today = Local::today().naive_local();
        if today > date + Duration::days(2) {
            Some(7 * 24 * 60 * 60)
        } else {
            Some(30)
        }
    } else {
        None
    }
}

impl DistrictQuery {
    /// Lookup all districts in archive
    pub async fn lookup(&self) -> Result<Body> {
        let path = PathBuf::from(BASE_PATH);
        let mut body = Body::default();
        scan_dir(&path, check_district, &mut body).await?;
        Ok(body)
    }
}

/// Check for valid district
fn check_district(name: &str, dir: bool) -> Option<String> {
    if dir {
        Some(name.to_string())
    } else {
        None
    }
}

impl YearQuery {
    /// Lookup years with archived data
    pub async fn lookup(&self) -> Result<Body> {
        let path = district_path(&self.district);
        let mut body = Body::default();
        scan_dir(&path, check_year, &mut body).await?;
        Ok(body)
    }
}

/// Check for valid year
fn check_year(name: &str, dir: bool) -> Option<String> {
    if dir {
        parse_year(name).ok().and_then(|_| Some(name.to_string()))
    } else {
        None
    }
}

impl DateQuery {
    /// Lookup dates with archived data
    pub async fn lookup(&self) -> Result<Body> {
        parse_year(&self.year)?;
        let path = year_path(&self.district, &self.year);
        let mut body = Body::default();
        scan_dir(&path, check_date, &mut body).await?;
        Ok(body)
    }
}

/// Check for valid date files
fn check_date(name: &str, dir: bool) -> Option<String> {
    let dt = if dir {
        name
    } else if name.len() == 16 && name.ends_with(DEXT) {
        name.get(..8).unwrap_or("")
    } else {
        &""
    };
    parse_date(dt).ok().and_then(|_| Some(dt.to_string()))
}

impl DetectorQuery {
    /// Lookup detectors with archived data
    pub async fn lookup(&self) -> Result<Body> {
        parse_date(&self.date)?;
        let mut body = Body::default();
        match scan_dir(&self.date_path(), check_detector, &mut body).await {
            Ok(_) | Err(Error::NotFound) => {
                // NOTE: the zip crate requires blocking calls
                scan_zip(&self.zip_path(), check_detector, &mut body)?;
            }
            Err(e) => Err(e)?,
        }
        Ok(body)
    }

    /// Get path to directory containing archived data
    fn date_path(&self) -> PathBuf {
        date_path(&self.district, &self.date)
    }

    /// Get path to (zip) file (std PathBuf)
    fn zip_path(&self) -> std::path::PathBuf {
        zip_path(&self.district, &self.date)
    }
}

/// Check for detector IDs
fn check_detector(name: &str, dir: bool) -> Option<String> {
    if !dir {
        let path = Path::new(name);
        path.extension()
            .and_then(|ext| ext.to_str())
            .and_then(|ext| file_ext(ext))
            .and_then(|_| path.file_stem())
            .and_then(|f| f.to_str())
            .and_then(|f| Some(f.to_string()))
    } else {
        None
    }
}

/// Check a archive file extension
fn file_ext(ext: &str) -> Option<&str> {
    const EXTS: &[&str] = &["vlog", "v30", "c30", "s30"];
    if EXTS.contains(&ext) {
        Some(ext)
    } else {
        None
    }
}

impl TrafficData for CountData {
    /// Binned file extension
    fn binned_ext() -> &'static str {
        "v30"
    }

    /// Number of bytes per binned value
    fn bin_bytes() -> usize {
        1
    }

    /// Set reset for count data
    fn reset(&mut self) {
        self.reset = true;
    }

    /// Add a vehicle to count data
    fn vehicle(&mut self, _veh: &VehicleEvent) {
        self.count += 1;
    }

    /// Get count data value as JSON
    fn as_json(&self) -> String {
        if self.reset {
            "null".to_owned()
        } else {
            format!("{}", self.count)
        }
    }
}

impl TrafficData for SpeedData {
    /// Binned file extension
    fn binned_ext() -> &'static str {
        "s30"
    }

    /// Number of bytes per binned value
    fn bin_bytes() -> usize {
        1
    }

    /// Set reset for speed data
    fn reset(&mut self) {}

    /// Add a vehicle to speed data
    fn vehicle(&mut self, veh: &VehicleEvent) {
        if let Some(speed) = veh.speed {
            self.total += speed;
            self.count += 1;
        }
    }

    /// Get speed data value as JSON
    fn as_json(&self) -> String {
        if self.count > 0 {
            let speed = (self.total as f32 / self.count as f32).round();
            format!("{}", speed as u32)
        } else {
            "null".to_owned()
        }
    }
}

impl TrafficData for OccupancyData {
    /// Binned file extension
    fn binned_ext() -> &'static str {
        "c30"
    }

    /// Number of bytes per binned value
    fn bin_bytes() -> usize {
        2
    }

    /// Unpack one binned value
    fn unpack(val: &[u8]) -> String {
        assert_eq!(val.len(), Self::bin_bytes());
        let value = (u16::from(val[0]) << 8 | u16::from(val[1])) as i16;
        if value < 0 {
            "null".to_owned()
        } else if value % 18 == 0 {
            // Whole number; use integer to prevent .0 at end
            format!("{}", value / 18)
        } else {
            format!("{:.2}", value as f32 / 18.0)
        }
    }

    /// Set reset for occupancy data
    fn reset(&mut self) {
        self.reset = true;
    }

    /// Add a vehicle to occupancy data
    fn vehicle(&mut self, veh: &VehicleEvent) {
        if let Some(duration) = veh.duration {
            self.duration += duration;
        }
    }

    /// Get occupancy data value as JSON
    fn as_json(&self) -> String {
        if self.reset {
            "null".to_owned()
        } else {
            // Ranges from 0 - 30_000 (100%)
            let val = self.duration;
            if val % 300 == 0 {
                // Whole number; use integer to prevent .0 at end
                format!("{}", val / 300)
            } else {
                format!("{:.2}", val as f32 / 300.0)
            }
        }
    }
}

impl TrafficData for LengthData {
    /// Binned file extension
    fn binned_ext() -> &'static str {
        "L30"
    }

    /// Number of bytes per binned value
    fn bin_bytes() -> usize {
        1
    }

    /// Unpack one binned value
    fn unpack(val: &[u8]) -> String {
        assert_eq!(val.len(), Self::bin_bytes());
        // There is no binned length format!
        "null".to_owned()
    }

    /// Set reset for length data
    fn reset(&mut self) {}

    /// Add a vehicle to length data
    fn vehicle(&mut self, veh: &VehicleEvent) {
        if let Some(length) = veh.length {
            self.total += length;
            self.count += 1;
        }
    }

    /// Get length data value as JSON
    fn as_json(&self) -> String {
        if self.count > 0 {
            let length = (self.total as f32 / self.count as f32).round();
            format!("{}", length as u32)
        } else {
            "null".to_owned()
        }
    }
}

impl<T: TrafficData> TrafficQuery<T> {
    /// Lookup archived traffic data
    pub async fn lookup(&self) -> Result<Body> {
        parse_date(&self.date)?;
        match self.lookup_vlog().await {
            Ok(body) => Ok(body),
            Err(Error::NotFound) => self.lookup_binned().await,
            Err(e) => Err(e),
        }
    }

    /// Lookup archived data from vehicle log
    async fn lookup_vlog(&self) -> Result<Body> {
        let data = match self.read_vlog_zip() {
            Ok(data) => data,
            Err(_) => self.read_vlog_file().await?,
        };
        let mut body = Body::default().with_max_age(max_age(&self.date));
        for val in data {
            body.push(val);
        }
        Ok(body)
    }

    /// Read vehicle log data from a zip file
    fn read_vlog_zip(&self) -> Result<Vec<String>> {
        let path = self.zip_path();
        if let Ok(file) = std::fs::File::open(path) {
            if let Ok(mut zip) = ZipArchive::new(file) {
                let name = self.vlog_file_name();
                if let Ok(zf) = zip.by_name(&name) {
                    log::info!("opened {} in {}.{}", name, self.date, EXT);
                    let mut log = vehicle::Log::default();
                    let mut lines = std::io::BufReader::new(zf).lines();
                    while let Some(line) = lines.next() {
                        log.append(&line?)?;
                    }
                    log.finish();
                    let bin = log.bin_30_seconds::<T>(self.filter())?;
                    return Ok(bin.iter().map(|d| d.as_json()).collect());
                }
            }
        }
        Err(Error::NotFound)
    }

    /// Read vehicle log data from a file
    async fn read_vlog_file(&self) -> Result<Vec<String>> {
        let mut path = self.date_path();
        path.push(self.vlog_file_name());
        if let Ok(file) = File::open(&path).await {
            log::info!("opened {:?}", &path);
            let mut log = vehicle::Log::default();
            let mut lines = BufReader::new(file).lines();
            while let Some(line) = lines.next().await {
                log.append(&line?)?;
            }
            log.finish();
            let bin = log.bin_30_seconds::<T>(self.filter())?;
            Ok(bin.iter().map(|d| d.as_json()).collect())
        } else {
            Err(Error::NotFound)
        }
    }

    /// Create a vehicle filter
    fn filter(&self) -> VehicleFilter {
        VehicleFilter::default()
            .with_length_ft_min(self.length_ft_min)
            .with_length_ft_max(self.length_ft_max)
            .with_speed_mph_min(self.speed_mph_min)
            .with_speed_mph_max(self.speed_mph_max)
    }

    /// Get vehicle log file name
    fn vlog_file_name(&self) -> String {
        format!("{}.vlog", self.detector)
    }

    /// Lookup archived data from 30-second binned data
    async fn lookup_binned(&self) -> Result<Body> {
        let data = match self.read_binned_zip() {
            Ok(data) => data,
            Err(_) => self.read_binned_file().await?,
        };
        let mut body = Body::default().with_max_age(max_age(&self.date));
        for val in data.chunks_exact(T::bin_bytes()) {
            body.push(T::unpack(val));
        }
        Ok(body)
    }

    /// Read binned data from a zip file
    fn read_binned_zip(&self) -> Result<Vec<u8>> {
        let path = self.zip_path();
        if let Ok(file) = std::fs::File::open(path) {
            if let Ok(mut zip) = ZipArchive::new(file) {
                let name = self.binned_file_name();
                if let Ok(mut zf) = zip.by_name(&name) {
                    log::info!("opened {} in {}.{}", name, self.date, EXT);
                    let len = zf.size();
                    T::check_len(len)?;
                    let mut data = vec![0; len as usize];
                    zf.read_exact(&mut data)?;
                    return Ok(data);
                }
            }
        }
        Err(Error::NotFound)
    }

    /// Get path to (zip) file (std PathBuf)
    fn zip_path(&self) -> std::path::PathBuf {
        zip_path(&self.district, &self.date)
    }

    /// Get binned file name
    fn binned_file_name(&self) -> String {
        format!("{}.{}", self.detector, T::binned_ext())
    }

    /// Read binned data from a file
    async fn read_binned_file(&self) -> Result<Vec<u8>> {
        let mut path = self.date_path();
        path.push(self.binned_file_name());
        if let Ok(mut file) = File::open(&path).await {
            log::info!("opened {:?}", &path);
            if let Ok(metadata) = file.metadata().await {
                let len = metadata.len();
                T::check_len(len)?;
                let mut data = vec![0; len as usize];
                file.read_exact(&mut data).await?;
                return Ok(data);
            }
        }
        Err(Error::NotFound)
    }

    /// Get path containing archive data for one date
    fn date_path(&self) -> PathBuf {
        date_path(&self.district, &self.date)
    }
}
