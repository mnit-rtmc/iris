// files.rs
//
// Copyright (C) 2023-2025  Minnesota Department of Transportation
//
use crate::error::Result;
use std::collections::HashSet;
use std::path::{Path, PathBuf};
use tokio::fs::{File, create_dir_all, read_dir, remove_file, rename};
use tokio::io::{AsyncWriteExt, BufWriter};

/// Make a PathBuf for a backup file
fn backup_path(path: &Path) -> PathBuf {
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
    pub async fn new(dir: &Path, name: &str) -> Result<Self> {
        let mut path = dir.to_path_buf();
        path.push(name);
        log::trace!("AtomicFile::new {path:?}");
        // Use parent in case name contains path separators
        if let Some(dir) = path.parent() {
            if !dir.is_dir() {
                create_dir_all(dir).await?;
            }
        }
        Ok(AtomicFile { path })
    }

    /// Get file path
    #[allow(unused)]
    pub fn path(&self) -> &Path {
        &self.path
    }

    /// Create the file and get writer
    pub async fn writer(&self) -> Result<BufWriter<File>> {
        let path = backup_path(&self.path);
        Ok(BufWriter::new(File::create(&path).await?))
    }

    /// Commit file change
    pub async fn commit(self) -> Result<()> {
        let path = backup_path(&self.path);
        log::trace!("AtomicFile::commit: {path:?}");
        rename(path, &self.path).await?;
        Ok(())
    }

    /// Rollback writing file
    pub async fn rollback(self) -> Result<()> {
        let path = backup_path(&self.path);
        log::trace!("AtomicFile::rollback: {path:?}");
        remove_file(path).await?;
        Ok(())
    }

    /// Write a buffer to the file
    pub async fn write_buf(self, buf: &[u8]) -> Result<()> {
        let mut writer = self.writer().await?;
        match writer.write_all(buf).await {
            Ok(()) => {
                writer.flush().await?;
                self.commit().await
            }
            Err(e) => {
                let _ = self.rollback().await;
                Err(e)?
            }
        }
    }
}

impl Cache {
    /// Lookup a listing of files with a given extension
    async fn read_dir(dir: &Path, ext: &str) -> Result<HashSet<PathBuf>> {
        let mut files = HashSet::new();
        if dir.is_dir() {
            let mut rd = read_dir(dir).await?;
            while let Some(f) = rd.next_entry().await? {
                if f.file_type().await?.is_file() {
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

    /// Create a set of files
    pub async fn new(dir: &Path, ext: &str) -> Result<Self> {
        let files = Cache::read_dir(dir, ext).await?;
        let dir = dir.into();
        Ok(Cache { dir, files })
    }

    /// Get a draining iterator of paths
    pub fn drain(&mut self) -> impl Iterator<Item = PathBuf> + '_ {
        self.files.drain()
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
    pub async fn file(&self, name: &str) -> Result<AtomicFile> {
        AtomicFile::new(&self.dir, name).await
    }

    /// Clear the cache
    pub async fn clear(mut self) {
        for name in self.files.drain() {
            log::trace!("Cache::clear: {name:?}");
            let mut path = self.dir.clone();
            path.push(name);
            if let Err(e) = remove_file(&path).await {
                log::error!("Cache::clear: {e:?}");
            }
        }
    }
}
