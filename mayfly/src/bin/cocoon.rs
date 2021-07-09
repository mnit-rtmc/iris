// cocoon.rs
//
// Copyright (c) 2021  Minnesota Department of Transportation
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

use argh::FromArgs;
use log::{debug, info};
use mayfly::binned::{CountData, OccupancyData, SpeedData, TrafficData};
use mayfly::common::{Error, Result};
use mayfly::vehicle::{VehLog, VehicleFilter};
use std::collections::HashMap;
use std::ffi::{OsStr, OsString};
use std::fs::File;
use std::io::{BufReader, BufWriter, Write};
use std::path::{Path, PathBuf};
use zip::write::FileOptions;
use zip::{DateTime, ZipArchive, ZipWriter};

/// List of traffic files to convert
#[derive(FromArgs)]
struct Files {
    #[argh(positional)]
    files: Vec<OsString>,
}

/// Traffic data for one detector
#[derive(Default)]
struct DetData {
    /// Flag for 30-second scans
    c30: bool,

    /// Flag for 30-second speed
    s30: bool,

    /// Flag for 30-second vehicle counts
    v30: bool,
}

impl DetData {
    /// Set flag when binned extensions are found
    fn add_ext(&mut self, ext: Option<&str>) -> bool {
        match ext {
            Some("c30") => {
                let ret = self.c30;
                self.c30 = true;
                ret
            }
            Some("s30") => {
                let ret = self.s30;
                self.s30 = true;
                ret
            }
            Some("v30") => {
                let ret = self.v30;
                self.v30 = true;
                ret
            }
            _ => false,
        }
    }
}

/// One daily traffic file
struct Traffic {
    /// Path to traffic archive
    path: PathBuf,

    /// Original zip archive
    archive: ZipArchive<BufReader<File>>,

    /// Mapping of (vlog) detector IDs to data
    vlogs: HashMap<OsString, DetData>,
}

impl Files {
    /// Convert traffic files
    fn convert(self) -> Result<()> {
        for file in self.files {
            let mut traffic = Traffic::new(&file)?;
            if traffic.check_vlogs() {
                traffic.convert()?;
            }
        }
        Ok(())
    }
}

impl Traffic {
    /// Open a traffic archive
    fn new(fname: &OsStr) -> Result<Self> {
        info!("Traffic::new: {:?}", fname);
        let path = PathBuf::from(fname);
        let file = File::open(&path).or(Err(Error::NotFound))?;
        let buf = BufReader::new(file);
        let archive = ZipArchive::new(buf)?;
        let vlogs = HashMap::new();
        Ok(Traffic {
            path,
            archive,
            vlogs,
        })
    }

    /// Check for vlog entries
    fn check_vlogs(&mut self) -> bool {
        debug!("Traffic::check_vlogs: {:?}", self.path);
        self.vlogs.clear();
        for name in self.archive.file_names() {
            debug!("  entry: {:?}", name);
            let ent = Path::new(name);
            if let Some(ext) = ent.extension() {
                if ext == "vlog" {
                    if let Some(stem) = ent.file_stem() {
                        self.vlogs
                            .insert(stem.to_os_string(), DetData::default());
                    }
                }
            }
        }
        !self.vlogs.is_empty()
    }

    /// Make a zip archive for writing
    fn make_writer(&self) -> Result<ZipWriter<BufWriter<File>>> {
        let mut path = self.path.clone();
        path.set_file_name("cocoon.traffic");
        let file = File::create(path)?;
        let buf = BufWriter::new(file);
        Ok(ZipWriter::new(buf))
    }

    /// Convert vlog entries
    fn convert(&mut self) -> Result<()> {
        info!("Traffic::convert: {:?}", self.path);
        let mut writer = self.make_writer()?;
        for i in 0..self.archive.len() {
            let zf = self.archive.by_index(i)?;
            let ent = Path::new(zf.name());
            if let (Some(stem), Some(ext)) = (ent.file_stem(), ent.extension())
            {
                if let Some(data) = self.vlogs.get_mut(stem) {
                    if ext == "vlog" {
                        let stem = stem.to_str().unwrap().to_owned();
                        let mtime = zf.last_modified();
                        let vlog = VehLog::from_blocking_reader(zf)?;
                        if !data.add_ext(Some("c30")) {
                            write_binned::<OccupancyData>(
                                &mut writer,
                                stem.to_string() + ".c30",
                                &vlog,
                                &mtime,
                            )?;
                        }
                        if !data.add_ext(Some("s30")) {
                            write_binned::<SpeedData>(
                                &mut writer,
                                stem.to_string() + ".s30",
                                &vlog,
                                &mtime,
                            )?;
                        }
                        if !data.add_ext(Some("v30")) {
                            write_binned::<CountData>(
                                &mut writer,
                                stem.to_string() + ".v30",
                                &vlog,
                                &mtime,
                            )?;
                        }
                        debug!("Converting vlog: {:?}", stem);
                        // Replace vlog with vehicle event (ve)
                        continue;
                    }
                    if data.add_ext(ext.to_str()) {
                        info!("Found {:?} after vlog, ignoring", ent);
                        continue;
                    }
                }
            }
            writer.raw_copy_file(zf)?;
        }
        writer.finish()?;
        // Rename old file and replace with new file
        Ok(())
    }
}

/// Write 30-second binned data
fn write_binned<T: TrafficData>(
    writer: &mut ZipWriter<BufWriter<File>>,
    name: String,
    vlog: &VehLog,
    mtime: &DateTime,
) -> Result<()> {
    if let Some(buf) = pack_binned::<T>(&vlog) {
        debug!("Binning {:?}", name);
        let options = FileOptions::default().last_modified_time(mtime.clone());
        writer.start_file(name, options)?;
        writer.write(&buf[..])?;
    }
    Ok(())
}

/// Pack traffic data into 30-second bins
fn pack_binned<T: TrafficData>(vlog: &VehLog) -> Option<Vec<u8>> {
    let len = 2880 * T::bin_bytes();
    let mut buf = Vec::with_capacity(len);
    for val in vlog.binned_iter::<T>(30, VehicleFilter::default()) {
        val.pack(&mut buf);
    }
    if buf.iter().any(|v| v != &0xFF) {
        Some(buf)
    } else {
        None
    }
}

/// Main function
#[async_std::main]
async fn main() -> Result<()> {
    env_logger::builder().format_timestamp(None).init();
    let files: Files = argh::from_env();
    files.convert()?;
    Ok(())
}
