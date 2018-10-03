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
use std::sync::mpsc::Receiver;
use std::thread;
use std::time::{Duration,Instant};

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
    fn new(username: &str, h: &str) -> Result<Self, Error> {
        let tcp = TcpStream::connect(h)?;
        let mut session = Session::new().unwrap();
        session.handshake(&tcp)?;
        session = authenticate(session, username)?;
        Ok(SshSession { _tcp: tcp, session })
    }
    fn do_session(&self, rx: &Receiver<PathBuf>, mut ns: &mut HashSet<PathBuf>){
        loop {
            if ns.is_empty() {
                match rx.recv() {
                    Ok(r)  => { ns.insert(r); },
                    Err(_) => { return; },
                }
            }
            for r in rx.try_iter() {
                ns.insert(r);
            }
            if let Err(e) = self.copy_all(&mut ns) {
                println!("scp_file error: {}", e);
                thread::sleep(Duration::from_secs(10));
                return;
            }
        }
    }
    fn copy_all(&self, ns: &mut HashSet<PathBuf>) -> Result<(), Error> {
        for p in ns.iter() {
            let t = Instant::now();
            self.scp_file(&p)?;
            println!("    {:?}: copied in {:?}", p, t.elapsed());
        }
        // All copied successfully
        ns.clear();
        Ok(())
    }
    fn scp_file(&self, p: &PathBuf) -> Result<(), Error> {
        let mut fi = File::open(&p)?;
        let m = fi.metadata()?;
        let mut fo = self.session.scp_send(p.as_path(), 0o644, m.len(), None)?;
        let c = io::copy(&mut fi, &mut fo)?;
        if c != m.len() {
            println!("    {:?}: length mismatch {} != {}", p, c, m.len());
        }
        Ok(())
    }
}

pub fn start(host: String, username: String, rx: Receiver<PathBuf>) {
    let mut ns = HashSet::new();
    loop {
        match SshSession::new(&username, &host) {
            Ok(s)  => s.do_session(&rx, &mut ns),
            Err(e) => {
                println!("SshSession::new error: {}", e);
                thread::sleep(Duration::from_secs(10));
            },
        }
    }
}
