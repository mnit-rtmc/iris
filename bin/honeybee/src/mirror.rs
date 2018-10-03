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
use std::sync::mpsc::{Receiver,RecvError};
use std::thread;
use std::time::{Duration,Instant};

struct PathSet {
    set : HashSet<PathBuf>,
}

impl PathSet {
    fn new() -> Self {
        let set = HashSet::new();
        PathSet { set }
    }
    fn receive_pending(&mut self, rx: &Receiver<PathBuf>)
        -> Result<(), RecvError>
    {
        if self.set.is_empty() {
            let p = rx.recv()?;
            self.set.insert(p);
        }
        for p in rx.try_iter() {
            self.set.insert(p);
        }
        Ok(())
    }
}

struct SshSession {
    _tcp   : TcpStream, // must remain in scope as long as Session
    session: Session,
}

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
    fn new(host: &str, username: &str) -> Result<Self, Error> {
        let tcp = TcpStream::connect(host)?;
        let mut session = Session::new().unwrap();
        session.handshake(&tcp)?;
        session = authenticate(session, username)?;
        Ok(SshSession { _tcp: tcp, session })
    }
    fn do_session(&self, rx: &Receiver<PathBuf>, mut ps: &mut PathSet)
        -> Result<(), RecvError>
    {
        loop {
            ps.receive_pending(rx)?;
            if let Err(e) = self.copy_all(&mut ps) {
                println!("  scp_file error: {}", e);
                return Ok(());
            }
        }
    }
    fn copy_all(&self, ps: &mut PathSet) -> Result<(), Error> {
        for p in ps.set.iter() {
            let t = Instant::now();
            self.scp_file(&p)?;
            println!("  {:?}: copied in {:?}", p, t.elapsed());
        }
        // All copied successfully
        ps.set.clear();
        Ok(())
    }
    fn scp_file(&self, p: &PathBuf) -> Result<(), Error> {
        let mut fi = File::open(&p)?;
        let m = fi.metadata()?;
        let mut fo = self.session.scp_send(p.as_path(), 0o644, m.len(), None)?;
        let c = io::copy(&mut fi, &mut fo)?;
        if c != m.len() {
            println!("  {:?}: length mismatch {} != {}", p, c, m.len());
        }
        Ok(())
    }
}

pub fn start(host: Option<String>, username: &str, rx: Receiver<PathBuf>) {
    if let None = host {
        println!("  mirror::start: No host");
    }
    if let Err(e) = do_start(host, username, rx) {
        println!("  mirror::start: {}", e);
    }
}

fn do_start(host: Option<String>, username: &str, rx: Receiver<PathBuf>)
    -> Result<(), RecvError>
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

fn start_session(host: &str, username: &str, rx: &Receiver<PathBuf>,
    mut ps: &mut PathSet) -> Result<(), RecvError>
{
    match SshSession::new(host, username) {
        Ok(s)  => { s.do_session(rx, &mut ps)?; },
        Err(e) => { println!("  SshSession::new error: {}", e); },
    }
    Ok(())
}
