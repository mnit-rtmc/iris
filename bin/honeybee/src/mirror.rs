/*
 * Copyright (C) 2018  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
use failure::Error;
use ssh2::Session;
use std::collections::HashSet;
use std::fs::File;
use std::io;
use std::net::TcpStream;
use std::path::PathBuf;
use std::sync::mpsc::{Receiver,TryRecvError};
use std::thread;
use std::time::{Duration,Instant};

/// A set of paths to mirror
struct PathSet {
    set : HashSet<PathBuf>,
}

impl PathSet {
    /// Create a new PathSet
    fn new() -> Self {
        let set = HashSet::new();
        PathSet { set }
    }
    /// Receive pending paths from channel.
    ///
    /// * `rx` Channel receiver for path names.
    fn receive_pending(&mut self, rx: &Receiver<PathBuf>)
        -> Result<(), Error>
    {
        if self.set.is_empty() {
            self.set.insert(rx.recv()?);
        }
        loop {
            let p = rx.try_recv();
            if let Err(TryRecvError::Empty) = p { break; };
            self.set.insert(p?);
        }
        Ok(())
    }
}

/// An ssh session
struct SshSession {
    _tcp   : TcpStream, // must remain in scope as long as Session
    session: Session,
}

/// Authenticate an ssh session.
///
/// * `session` The ssh session.
/// * `username` User to authenticate.
fn authenticate(session: Session, username: &str) -> Result<Session, Error> {
    // Try agent first, since we don't have a pass-phrase
    if let Err(_) = session.userauth_agent(username) {
        let mut key = PathBuf::new();
        key.push("/home");
        key.push(username);
        key.push(".ssh");
        key.push("id_rsa");
        // Since agent failed, try private key with no pass-phrase
        session.userauth_pubkey_file(username, None, &key, None)?;
    }
    Ok(session)
}

impl SshSession {
    /// Create a new ssh session
    ///
    /// * `host` Host name (and port) to connect.
    /// * `username` Name of user to use for authentication.
    fn new(host: &str, username: &str) -> Result<Self, Error> {
        let tcp = TcpStream::connect(host)?;
        let mut session = Session::new()
                                  .ok_or_else(|| format_err!("No session"))?;
        session.handshake(&tcp)?;
        session = authenticate(session, username)?;
        Ok(SshSession { _tcp: tcp, session })
    }
    /// Mirror files with the session.
    ///
    /// * `rx` Channel receiver for path names.
    /// * `ps` Set of path names to mirror.
    fn do_session(&self, rx: &Receiver<PathBuf>, mut ps: &mut PathSet)
        -> Result<(), Error>
    {
        loop {
            ps.receive_pending(rx)?;
            if let Err(e) = self.mirror_all(&mut ps) {
                error!("{}", e);
                return Ok(());
            }
        }
    }
    /// Mirror all files in a path set.
    ///
    /// * `ps` Set of path names to mirror.
    fn mirror_all(&self, ps: &mut PathSet) -> Result<(), Error> {
        for p in ps.set.iter() {
            let t = Instant::now();
            self.scp_file(&p)?;
            info!("{:?}: copied in {:?}", p, t.elapsed());
        }
        // All copied successfully
        ps.set.clear();
        Ok(())
    }
    /// Mirror one file with scp.
    ///
    /// * `p` Path to file.
    fn scp_file(&self, p: &PathBuf) -> Result<(), Error> {
        let mut fi = File::open(&p)?;
        let m = fi.metadata()?;
        let mut fo = self.session.scp_send(p.as_path(), 0o644, m.len(), None)?;
        let c = io::copy(&mut fi, &mut fo)?;
        if c != m.len() {
            error!("{:?}: length mismatch {} != {}", p, c, m.len());
        }
        Ok(())
    }
}

/// Start mirroring.
///
/// * `host` Host name (and port) to connect.
/// * `username` Name of user to use for authentication.
/// * `rx` Channel receiver for path names.
pub fn start(host: Option<String>, username: &str, rx: Receiver<PathBuf>) {
    if let None = host {
        error!("No mirror host");
    }
    if let Err(e) = run_loop(host, username, rx) {
        error!("{}", e);
    }
}

/// Run mirroring loop.
///
/// * `host` Host name (and port) to connect.
/// * `username` Name of user to use for authentication.
/// * `rx` Channel receiver for path names.
fn run_loop(host: Option<String>, username: &str, rx: Receiver<PathBuf>)
    -> Result<(), Error>
{
    let mut ps = PathSet::new();
    loop {
        ps.receive_pending(&rx)?;
        if let Some(h) = &host {
            start_session(&h, username, &rx, &mut ps)?;
        }
        thread::sleep(Duration::from_secs(10));
    }
}

/// Start ssh session.
///
/// * `host` Host name (and port) to connect.
/// * `username` Name of user to use for authentication.
/// * `rx` Channel receiver for path names.
/// * `ps` Set of path names to mirror.
fn start_session(host: &str, username: &str, rx: &Receiver<PathBuf>,
    mut ps: &mut PathSet) -> Result<(), Error>
{
    match SshSession::new(host, username) {
        Ok(s)  => { s.do_session(rx, &mut ps)?; },
        Err(e) => { error!("{}", e); },
    }
    Ok(())
}
