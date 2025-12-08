// cocoon.rs
//
// Copyright (c) 2021-2025  Minnesota Department of Transportation
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
use mayfly::binned::{
    BinIter, CountData, OccupancyData, SpeedData, TrafficData, VehicleFilter,
};
use mayfly::error::Result;
use mayfly::traffic::Traffic;
use mayfly::vlog::{VehLogReader, VehicleEvent};
use std::ffi::OsString;
use std::fs::File;
use std::io::{BufWriter, ErrorKind, Write};
use std::path::{Path, PathBuf};
use zip::write::FileOptions;
use zip::{DateTime, ZipWriter};

/// Traffic archive backup path
const BACKUP_PATH: &str = "/var/lib/iris/backup";

/// Command-line arguments
#[derive(FromArgs, PartialEq, Debug)]
struct Args {
    #[argh(subcommand)]
    cmd: Command,
}

/// Sub-command enum
#[derive(FromArgs, PartialEq, Debug)]
#[argh(subcommand)]
enum Command {
    Bin(BinCommand),
}

/// Create 30-second binned data from vehicle event logs
#[derive(FromArgs, PartialEq, Debug)]
#[argh(subcommand, name = "bin")]
struct BinCommand {
    /// traffic archives to "bin" from vehicle event logs
    #[argh(positional)]
    traffic_files: Vec<OsString>,
}

/// Traffic archive binner
struct Binner {
    /// Set of files in archive
    files: Vec<String>,

    /// Destination archive
    writer: ZipWriter<BufWriter<File>>,
}

impl Args {
    /// Run the selected sub-command
    fn run(self) -> Result<()> {
        match self.cmd {
            Command::Bin(cmd) => Ok(cmd.run()?),
        }
    }
}

impl BinCommand {
    /// Run the bin sub-command
    fn run(self) -> Result<()> {
        for file in self.traffic_files {
            let traffic = Traffic::new(&file)?;
            let n_files = traffic.len();
            if traffic.needs_binning() {
                let backup = backup_path(traffic.path())?;
                let copier = Binner::new(&traffic)?;
                let n_binned = copier.add_binned(traffic)?;
                info!("archive: {file:?} {n_files} files, {n_binned} binned");
                std::fs::rename(&file, backup)?;
                std::fs::rename(temp_path(&file), file)?;
            } else {
                info!("archive: {file:?} {n_files} files, skipping");
            }
        }
        Ok(())
    }
}

impl Binner {
    /// Create a new traffic archive binner
    fn new(traffic: &Traffic) -> Result<Self> {
        let files = traffic.file_names().map(|n| n.to_string()).collect();
        let writer = make_writer(&traffic.path())?;
        Ok(Binner { files, writer })
    }

    /// Add binned files to archive
    fn add_binned(mut self, mut traffic: Traffic) -> Result<u32> {
        let mut n_binned = 0;
        for i in 0..traffic.len() {
            let zf = traffic.by_index(i)?;
            match self.vlog_det_id(zf.name()) {
                Some(det_id) => {
                    let mtime = zf.last_modified();
                    let vlog = VehLogReader::from_reader_blocking(zf)?;
                    let events = vlog.events();
                    n_binned += self.write_binned::<OccupancyData>(
                        det_id.to_string() + ".c30",
                        &events,
                        &mtime,
                    )?;
                    n_binned += self.write_binned::<SpeedData>(
                        det_id.to_string() + ".s30",
                        &events,
                        &mtime,
                    )?;
                    n_binned += self.write_binned::<CountData>(
                        det_id.to_string() + ".v30",
                        &events,
                        &mtime,
                    )?;
                    let zf = traffic.by_index(i)?;
                    self.writer.raw_copy_file(zf)?;
                }
                None => self.writer.raw_copy_file(zf)?,
            }
        }
        self.writer.finish()?;
        Ok(n_binned)
    }

    /// Check if a file is a vlog which needs binning
    fn vlog_det_id(&self, name: &str) -> Option<String> {
        let ent = Path::new(name);
        if let (Some(stem), Some(ext)) = (ent.file_stem(), ent.extension())
            && ext == "vlog"
            && let Some(det_id) = stem.to_str()
            && !self.contains(&(det_id.to_owned() + ".c30"))
            && !self.contains(&(det_id.to_owned() + ".s30"))
            && !self.contains(&(det_id.to_owned() + ".v30"))
        {
            return Some(det_id.to_owned());
        }
        None
    }

    /// Check if archive contains a file
    fn contains(&self, name: &str) -> bool {
        let path = Path::new(name);
        if let Some(name) = path.file_name()
            && let Some(name) = name.to_str()
        {
            return self.files.iter().any(|n| n == name);
        }
        false
    }

    /// Write 30-second binned data
    fn write_binned<T: TrafficData>(
        &mut self,
        name: String,
        events: &[VehicleEvent],
        mtime: &DateTime,
    ) -> Result<u32> {
        if self.contains(&name) {
            return Ok(0);
        }
        if let Some(buf) = pack_binned::<T>(events) {
            debug!("Binning {name:?}");
            let options = FileOptions::default().last_modified_time(*mtime);
            self.writer.start_file(name, options)?;
            self.writer.write_all(&buf[..])?;
            Ok(1)
        } else {
            Ok(0)
        }
    }
}

/// Make backup path name
fn backup_path(path: &Path) -> Result<PathBuf> {
    let mut backup = PathBuf::from(BACKUP_PATH);
    if backup.is_dir()
        && let Some(name) = path.file_name()
    {
        backup.push(name);
        if !backup.exists() {
            return Ok(backup);
        }
        Err(std::io::Error::new(
            ErrorKind::AlreadyExists,
            name.to_string_lossy(),
        ))?;
    }
    Err(std::io::Error::new(ErrorKind::NotFound, BACKUP_PATH))?
}

/// Make temp path name
fn temp_path(path: &impl AsRef<Path>) -> PathBuf {
    let mut path = PathBuf::from(path.as_ref());
    path.set_file_name("cocoon.traffic");
    path
}

/// Make a zip archive for writing
fn make_writer(path: &impl AsRef<Path>) -> Result<ZipWriter<BufWriter<File>>> {
    let file = File::create(temp_path(path))?;
    let buf = BufWriter::new(file);
    Ok(ZipWriter::new(buf))
}

/// Pack traffic data into 30-second bins
fn pack_binned<T: TrafficData>(events: &[VehicleEvent]) -> Option<Vec<u8>> {
    let len = 2880 * T::bin_bytes();
    let mut buf = Vec::with_capacity(len);
    let mut any_valid = false;
    for val in BinIter::<T>::new(30, events, VehicleFilter::default()) {
        if !any_valid && val.value().is_some() {
            any_valid = true;
        }
        val.pack(&mut buf);
    }
    any_valid.then_some(buf)
}

/// Main function
fn main() -> Result<()> {
    env_logger::builder().format_timestamp(None).init();
    let args: Args = argh::from_env();
    args.run()?;
    Ok(())
}
