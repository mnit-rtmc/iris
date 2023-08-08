// files.rs
//
// Copyright (C) 2023  Minnesota Department of Transportation
//
use crate::Result;
use std::collections::HashSet;
use std::fs::{create_dir_all, read_dir, rename, remove_file, File};
use std::io::BufWriter;
use std::path::{Path, PathBuf};

/// Make a PathBuf for a backup file
fn make_backup_name(path: &Path) -> PathBuf {
    let mut path = PathBuf::from(path);
    path.as_mut_os_string().push("~");
    path
}

/// A file which is written, then renamed atomically
pub struct AtomicFile {
    path: PathBuf,
}

/// Cache of files in a directory
pub struct Cache {
    /// Cache directory
    dir: PathBuf,
    /// Cached files
    files: HashSet<PathBuf>,
}

impl AtomicFile {
    /// Create a new atomic file
    pub fn new(dir: &Path, name: &str) -> Result<Self> {
        let mut path = PathBuf::new();
        path.push(dir);
        path.push(name);
        log::debug!("AtomicFile::new {name}");
        if let Some(dir) = path.parent() {
            if !dir.is_dir() {
                create_dir_all(dir)?;
            }
        }
        Ok(AtomicFile { path })
    }

    /// Create the file and get writer
    pub fn writer(&self) -> Result<BufWriter<File>> {
        let mut path = self.path.clone();
        path.as_mut_os_string().push("~");
        Ok(BufWriter::new(File::create(path)?))
    }

    /// Cancel writing file
    pub fn cancel(&self) -> Result<()> {
        let name = make_backup_name(&self.path);
        log::debug!("cancel: {name:?}");
        remove_file(&name)?;
        Ok(())
    }
}

impl Drop for AtomicFile {
    fn drop(&mut self) {
        let backup = make_backup_name(&self.path);
        log::debug!("AtomicFile::drop: {backup:?}");
        if let Err(e) = rename(backup, &self.path) {
            log::error!("AtomicFile::drop rename: {e:?}");
        }
    }
}

impl Cache {
    /// Create a set of files
    pub fn new(dir: &Path, ext: &str) -> Result<Self> {
        let files = Cache::files(dir, ext)?;
        let dir = dir.into();
        Ok(Cache {
            dir,
            files,
        })
    }

    /// Lookup a listing of files with a given extension
    fn files(dir: &Path, ext: &str) -> Result<HashSet<PathBuf>> {
        let mut files = HashSet::new();
        if dir.is_dir() {
            for f in read_dir(dir)? {
                let f = f?;
                if f.file_type()?.is_file() {
                    let p = PathBuf::from(f.file_name());
                    if let Some(e) = p.extension() {
                        if e == ext {
                            files.insert(p);
                        }
                    }
                }
            }
        }
        Ok(files)
    }

    /// Check if cache contains a file
    pub fn contains(&self, name: &str) -> bool {
        self.files.contains(&PathBuf::from(name))
    }

    /// Keep a file in cache
    pub fn keep(&mut self, name: &str) -> bool {
        self.files.remove(&PathBuf::from(name))
    }

    /// Create a cached file
    pub fn file(&self, name: &str) -> Result<AtomicFile> {
        AtomicFile::new(&self.dir, name)
    }
}

impl Drop for Cache {
    fn drop(&mut self) {
        for name in self.files.drain() {
            log::debug!("Cache::drop: {name:?}");
            let mut p = PathBuf::new();
            p.push(&self.dir);
            p.push(name);
            if let Err(e) = remove_file(&p) {
                log::error!("Cache::drop: {e:?}");
            }
        }
    }
}
