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
use crate::binned::TrafficData;
use crate::common::{Body, Error, Result};
use crate::traffic::Traffic;
use crate::vehicle::{VehLog, VehicleFilter};
use async_std::fs::{read_dir, File};
use async_std::io::ReadExt;
use async_std::path::{Path, PathBuf};
use async_std::stream::StreamExt;
use chrono::{Duration, Local, NaiveDate};
use serde::Deserialize;
use std::collections::HashSet;
use std::io::Read as _;
use std::marker::PhantomData;

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

/// Query for corridors with detectors
#[derive(Deserialize)]
pub struct CorridorQuery {
    /// District ID
    pub district: Option<String>,
    /// Date (8-character yyyyMMdd)
    pub date: String,
}

/// Query for detectors with archived data
#[derive(Deserialize)]
pub struct DetectorQuery {
    /// District ID
    pub district: Option<String>,
    /// Date (8-character yyyyMMdd)
    pub date: String,
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
    /// Minimum headway (logged vehicles)
    headway_sec_min: Option<f32>,
    /// Maximum headway (logged vehicles)
    headway_sec_max: Option<f32>,
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
    check: fn(&str, bool) -> Option<&str>,
    body: &mut Body,
) -> Result<()> {
    let mut entries = read_dir(path).await.or(Err(Error::NotFound))?;
    let mut names = HashSet::new();
    while let Some(entry) = entries.next().await {
        if let Ok(entry) = entry {
            if let Ok(tp) = entry.file_type().await {
                if !tp.is_symlink() {
                    if let Some(name) = entry.file_name().to_str() {
                        if let Some(value) = check(name, tp.is_dir()) {
                            names.insert(format!("\"{}\"", value));
                        }
                    }
                }
            }
        }
    }
    for name in names {
        body.push(&name);
    }
    Ok(())
}

/// Scan entries in a zip file
fn scan_zip(
    path: &impl AsRef<std::path::Path>,
    check: fn(&str, bool) -> Option<&str>,
    body: &mut Body,
) -> Result<()> {
    let traffic = Traffic::new(path)?;
    let names = traffic.find_files_checked(check);
    for name in names {
        body.push(&format!("\"{}\"", name));
    }
    Ok(())
}

/// Parse year parameter
fn parse_year(year: &str) -> Result<i32> {
    match year.parse() {
        Ok(y) if (1900..=9999).contains(&y) => Ok(y),
        _ => Err(Error::InvalidDate),
    }
}

/// Parse month parameter
fn parse_month(month: &str) -> Result<u32> {
    match month.parse() {
        Ok(m) if (1..=12).contains(&m) => Ok(m),
        _ => Err(Error::InvalidDate),
    }
}

/// Parse day parameter
fn parse_day(day: &str) -> Result<u32> {
    match day.parse() {
        Ok(d) if (1..=31).contains(&d) => Ok(d),
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

/// Max age for caching resources (100 weeks)
const MAX_AGE_SEC: u64 = 100 * 7 * 24 * 60 * 60;

/// Get max age for cache control heder
fn max_age(date: &str) -> Option<u64> {
    if let Ok(date) = parse_date(date) {
        let today = Local::today().naive_local();
        if today > date + Duration::days(2) {
            Some(MAX_AGE_SEC)
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
fn check_district(name: &str, dir: bool) -> Option<&str> {
    if dir {
        Some(name)
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
fn check_year(name: &str, dir: bool) -> Option<&str> {
    if dir {
        parse_year(name).ok().map(|_| name)
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
fn check_date(name: &str, dir: bool) -> Option<&str> {
    let dt = if dir {
        name
    } else if name.len() == 16 && name.ends_with(DEXT) {
        name.get(..8).unwrap_or("")
    } else {
        &""
    };
    parse_date(dt).ok().map(|_| dt)
}

impl CorridorQuery {
    /// Lookup corridors with detectors
    pub async fn lookup(&self) -> Result<Body> {
        parse_date(&self.date)?;
        let mut body = Body::default();
        match scan_zip(&self.zip_path(), check_corridor, &mut body) {
            Ok(_) => Ok(body),
            Err(Error::NotFound) => {
                match scan_dir(&self.date_path(), check_corridor, &mut body)
                    .await
                {
                    Ok(_) | Err(Error::NotFound) => Ok(body),
                    Err(e) => Err(e),
                }
            }
            Err(e) => Err(e),
        }
    }

    /// Get path to directory containing archived data
    fn date_path(&self) -> PathBuf {
        let mut path = date_path(&self.district, &self.date);
        path.push("corridors");
        path
    }

    /// Get path to (zip) file (std PathBuf)
    fn zip_path(&self) -> std::path::PathBuf {
        zip_path(&self.district, &self.date)
    }
}

/// Check for corridor IDs
fn check_corridor(name: &str, dir: bool) -> Option<&str> {
    if !dir && !name.contains('.') && name.contains('_') {
        Some(name)
    } else {
        None
    }
}

impl DetectorQuery {
    /// Lookup detectors with archived data
    pub async fn lookup(&self) -> Result<Body> {
        parse_date(&self.date)?;
        let mut body = Body::default();
        match scan_zip(&self.zip_path(), check_detector, &mut body) {
            Ok(_) => Ok(body),
            Err(Error::NotFound) => {
                match scan_dir(&self.date_path(), check_detector, &mut body)
                    .await
                {
                    Ok(_) | Err(Error::NotFound) => Ok(body),
                    Err(e) => Err(e),
                }
            }
            Err(e) => Err(e),
        }
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
fn check_detector(name: &str, dir: bool) -> Option<&str> {
    if !dir {
        let path = Path::new(name);
        path.extension()
            .and_then(|ext| ext.to_str())
            .and_then(|ext| file_ext(ext))
            .and_then(|_| path.file_stem())
            .and_then(|f| f.to_str())
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
    /// Lookup archived traffic data.
    ///
    /// Check for binned data first, since it will be faster than scanning a
    /// vehicle event log.
    pub async fn lookup(&self) -> Result<Body> {
        parse_date(&self.date)?;
        match Traffic::new(&self.zip_path()) {
            Ok(traffic) => self.lookup_zipped(traffic),
            _ => self.lookup_unzipped().await,
        }
    }

    /// Lookup data from a zip archive
    fn lookup_zipped(&self, mut traffic: Traffic) -> Result<Body> {
        if !self.filter().is_filtered() {
            match self.lookup_zipped_bin(&mut traffic) {
                Err(Error::NotFound) => (),
                res => return res,
            }
        }
        self.lookup_zipped_vlog(&mut traffic)
    }

    /// Lookup archived data from 30-second binned data
    fn lookup_zipped_bin(&self, traffic: &mut Traffic) -> Result<Body> {
        let name = self.binned_file_name();
        match traffic.by_name(&name) {
            Ok(mut zf) => {
                log::info!("opened {} in {}.{}", name, self.date, EXT);
                let mut buf = Self::make_buffer(zf.size())?;
                zf.read_exact(&mut buf)?;
                Ok(self.make_binned_body(buf))
            }
            _ => Err(Error::NotFound),
        }
    }

    /// Read vehicle log data from a zip file
    fn lookup_zipped_vlog(&self, traffic: &mut Traffic) -> Result<Body> {
        let name = self.vlog_file_name();
        match traffic.by_name(&name) {
            Ok(zf) => {
                log::info!("opened {} in {}.{}", name, self.date, EXT);
                let vlog = VehLog::from_blocking_reader(zf)?;
                Ok(self.make_vlog_body(vlog))
            }
            _ => Err(Error::NotFound),
        }
    }

    /// Lookup data from file system (unzipped)
    async fn lookup_unzipped(&self) -> Result<Body> {
        if !self.filter().is_filtered() {
            match self.lookup_unzipped_bin().await {
                Err(Error::NotFound) => (),
                res => return res,
            }
        }
        self.lookup_unzipped_vlog().await
    }

    /// Lookup unzipped data from 30-second binned data
    async fn lookup_unzipped_bin(&self) -> Result<Body> {
        let mut path = self.date_path();
        path.push(self.binned_file_name());
        if let Ok(mut file) = File::open(&path).await {
            if let Ok(metadata) = file.metadata().await {
                log::info!("opened {:?}", &path);
                let mut buf = Self::make_buffer(metadata.len())?;
                file.read_exact(&mut buf).await?;
                return Ok(self.make_binned_body(buf));
            }
        }
        Err(Error::NotFound)
    }

    /// Lookup unzipped data from vehicle log file
    async fn lookup_unzipped_vlog(&self) -> Result<Body> {
        let mut path = self.date_path();
        path.push(self.vlog_file_name());
        match File::open(&path).await {
            Ok(file) => {
                log::info!("opened {:?}", &path);
                let vlog = VehLog::from_async_reader(file).await?;
                Ok(self.make_vlog_body(vlog))
            }
            _ => Err(Error::NotFound),
        }
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
    fn make_buffer(len: u64) -> Result<Vec<u8>> {
        let sz = 2880 * T::bin_bytes();
        if len == sz as u64 {
            Ok(vec![0; sz])
        } else {
            Err(Error::InvalidData)
        }
    }

    /// Make a JSON result body
    fn make_body(&self) -> Body {
        Body::default().with_max_age(max_age(&self.date))
    }

    /// Make body from binned buffer
    fn make_binned_body(&self, buf: Vec<u8>) -> Body {
        let mut body = self.make_body();
        for val in buf.chunks_exact(T::bin_bytes()) {
            body.push(&format!("{}", T::unpack(val)));
        }
        body
    }

    /// Make body from vehicle log
    fn make_vlog_body(&self, vlog: VehLog) -> Body {
        let mut body = self.make_body();
        for val in vlog.binned_iter::<T>(30, self.filter()) {
            body.push(&format!("{}", val));
        }
        body
    }

    /// Get path to (zip) file (std PathBuf)
    fn zip_path(&self) -> std::path::PathBuf {
        zip_path(&self.district, &self.date)
    }

    /// Get binned file name
    fn binned_file_name(&self) -> String {
        format!("{}.{}", self.detector, T::binned_ext())
    }

    /// Get path containing archive data for one date
    fn date_path(&self) -> PathBuf {
        date_path(&self.district, &self.date)
    }
}
