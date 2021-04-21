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
use async_std::fs::{read_dir, File};
use async_std::io::ReadExt;
use async_std::path::{Path, PathBuf};
use async_std::stream::StreamExt;
use chrono::{Duration, Local, NaiveDate};
use serde::Deserialize;
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
                            body.push(value)?;
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
                    body.push(e)?;
                }
            }
        }
    }
    Ok(())
}

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
pub trait TrafficData {
    /// Archive file extension
    fn ext() -> &'static str;

    /// Check file length
    fn check_len(len: u64) -> Result<()> {
        if len == Self::len() {
            Ok(())
        } else {
            Err(Error::InvalidData)
        }
    }

    /// Length of binned data
    fn len() -> u64;

    /// Build JSON body from binned data
    fn build_body(data: Vec<u8>, max_age: Option<u64>) -> Result<Body> {
        let mut body = Body::default().with_max_age(max_age);
        for value in data {
            let value = value as i8;
            let value = if value >= 0 { Some(value) } else { None };
            body.push(value)?;
        }
        Ok(body)
    }
}

/// Binned vehicle count data
#[derive(Clone, Copy)]
pub struct CountData;

/// Binned speed data
#[derive(Clone, Copy)]
pub struct SpeedData;

/// Binned occupancy data
#[derive(Clone, Copy)]
pub struct OccupancyData;

impl TrafficData for CountData {
    /// Archive file extension
    fn ext() -> &'static str {
        "v30"
    }

    /// Length of binned data
    fn len() -> u64 {
        2880
    }
}

impl TrafficData for SpeedData {
    /// Archive file extension
    fn ext() -> &'static str {
        "s30"
    }

    /// Length of binned data
    fn len() -> u64 {
        2880
    }
}

impl TrafficData for OccupancyData {
    /// Archive file extension
    fn ext() -> &'static str {
        "c30"
    }

    /// Length of binned data
    fn len() -> u64 {
        2880 * 2
    }

    /// Build JSON body from binned data
    fn build_body(data: Vec<u8>, max_age: Option<u64>) -> Result<Body> {
        let mut body = Body::default().with_max_age(max_age);
        for val in data.chunks_exact(2) {
            let value = (u16::from(val[0]) << 8 | u16::from(val[1])) as i16;
            if value < 0 {
                body.push::<Option<f32>>(None)?;
            } else if value % 18 == 0 {
                // Whole number; use integer to prevent .0 at end
                let occ = i32::from(value) * 100 / 1800;
                body.push(occ)?;
            } else {
                let occ = (value as f32 * 100.0 / 18.0).round() / 100.0;
                body.push(occ)?;
            }
        }
        Ok(body)
    }
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
    /// Binning interval
    _bin_secs: Option<u32>,
    /// Minimum vehicle length (logged vehicles)
    _length_ft_min: Option<u32>,
    /// Maximum vehicle length (logged vehicles)
    _length_ft_max: Option<u32>,
    /// Minimum recorded speed (logged vehicles)
    _speed_mph_min: Option<u32>,
    /// Maximum recorded speed (logged vehicles)
    _speed_mph_max: Option<u32>,
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

impl<T: TrafficData> TrafficQuery<T> {
    /// Lookup archived traffic data
    pub async fn lookup(&self) -> Result<Body> {
        parse_date(&self.date)?;
        match self.lookup_from_vlog().await {
            Ok(body) => Ok(body),
            Err(Error::NotFound) => self.lookup_from_binned().await,
            Err(e) => Err(e),
        }
    }

    /// Lookup archived data from vehicle log
    async fn lookup_from_vlog(&self) -> Result<Body> {
        // FIXME: check .traffic, then open .vlog
        Err(Error::NotFound)
    }

    /// Lookup archived data from 30-second binned data
    async fn lookup_from_binned(&self) -> Result<Body> {
        let data = match self.read_from_zip() {
            Ok(data) => data,
            Err(_) => self.read_from_file().await?,
        };
        T::build_body(data, self.max_age())
    }

    /// Get max age for caching
    fn max_age(&self) -> Option<u64> {
        if let Ok(date) = parse_date(&self.date) {
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

    /// Read archived data from a zip file
    fn read_from_zip(&self) -> Result<Vec<u8>> {
        let path = self.zip_path();
        if let Ok(file) = std::fs::File::open(path) {
            if let Ok(mut zip) = ZipArchive::new(file) {
                let name = self.file_name();
                if let Ok(mut zf) = zip.by_name(&name) {
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

    /// Get file name
    fn file_name(&self) -> String {
        format!("{}.{}", self.detector, T::ext())
    }

    /// Read archived data from a file
    async fn read_from_file(&self) -> Result<Vec<u8>> {
        let mut path = self.date_path();
        path.push(self.file_name());
        if let Ok(mut file) = File::open(&path).await {
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
