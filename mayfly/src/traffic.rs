// traffic.rs
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
use crate::common::{Error, Result};
use log::debug;
use std::collections::HashSet;
use std::fs::File;
use std::io::BufReader;
use std::path::{Path, PathBuf};
use zip::read::ZipFile;
use zip::ZipArchive;

/// One daily traffic file
pub struct Traffic {
    /// Path to traffic archive
    path: PathBuf,

    /// Original zip archive
    archive: ZipArchive<BufReader<File>>,
}

impl Traffic {
    /// Create a traffic archive
    pub fn new(fname: &impl AsRef<Path>) -> Result<Self> {
        Traffic::open(PathBuf::from(fname.as_ref()))
    }

    /// Open a traffic archive
    fn open(path: PathBuf) -> Result<Self> {
        let file = File::open(&path).or(Err(Error::NotFound))?;
        let buf = BufReader::new(file);
        let archive = ZipArchive::new(buf)?;
        Ok(Traffic { path, archive })
    }

    /// Get the path to the archive
    pub fn path(&self) -> &Path {
        &self.path
    }

    /// Check if any vehicle log needs binning
    pub fn needs_binning(&self) -> bool {
        let mut det_ids = HashSet::new();
        for name in self.archive.file_names() {
            let p = Path::new(name);
            if let (Some(det_id), Some(ext)) = (p.file_stem(), p.extension()) {
                if ext == "vlog" {
                    det_ids.insert(det_id);
                }
            }
        }
        for name in self.archive.file_names() {
            let p = Path::new(name);
            if let (Some(det_id), Some(ext)) = (p.file_stem(), p.extension()) {
                if ext == "v30" {
                    det_ids.remove(det_id);
                }
            }
        }
        !det_ids.is_empty()
    }

    /// Find files in archive
    pub fn find_file_names(&self) -> HashSet<String> {
        let mut files = HashSet::new();
        for name in self.archive.file_names() {
            let path = Path::new(name);
            if let Some(name) = path.file_name() {
                if let Some(name) = name.to_str() {
                    files.insert(name.to_owned());
                }
            }
        }
        debug!("found {} files in {:?}", files.len(), self.path);
        files
    }

    /// Find checked files in archive
    pub fn find_files_checked(
        &self,
        check: fn(&str, bool) -> Option<&str>,
    ) -> HashSet<String> {
        let mut files = HashSet::new();
        for name in self.archive.file_names() {
            let path = Path::new(name);
            if let Some(name) = path.file_name() {
                if let Some(name) = name.to_str() {
                    if let Some(name) = check(name, false) {
                        files.insert(name.to_owned());
                    }
                }
            }
        }
        debug!("found {} files in {:?}", files.len(), self.path);
        files
    }

    /// Get the number of files in the archive
    pub fn len(&self) -> usize {
        self.archive.len()
    }

    /// Check if archive is empty
    pub fn is_empty(&self) -> bool {
        self.archive.is_empty()
    }

    /// Get an archive entry by index
    pub fn by_index(&mut self, i: usize) -> Result<ZipFile> {
        Ok(self.archive.by_index(i)?)
    }

    /// Get an archive entry by name
    pub fn by_name<'a>(&'a mut self, nm: &str) -> Result<ZipFile<'a>> {
        Ok(self.archive.by_name(nm)?)
    }
}
