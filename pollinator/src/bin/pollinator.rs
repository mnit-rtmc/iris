// Copyright (C) 2025  Minnesota Department of Transportation
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
use argh::FromArgs;
use pollinator::CommLinkCfg;
use resin::{Database, Error, Result};
use std::collections::HashMap;
use tokio::task::{AbortHandle, JoinSet};
use tokio::time::Duration;
use tokio_postgres::Notification;

/// Command-line arguments
#[derive(FromArgs)]
struct Args {
    /// uri address or host name
    #[argh(option, short = 'a')]
    uri: Option<String>,
    /// user name
    #[argh(option, short = 'u')]
    user: Option<String>,
    /// password
    #[argh(option, short = 'p')]
    password: Option<String>,
}

impl Args {
    /// Get comm link configuration
    async fn comm_link_config(self) -> Result<Option<CommLinkCfg>> {
        let any = self.uri.is_some()
            || self.user.is_some()
            || self.password.is_some();
        if let (Some(uri), Some(user), Some(password)) =
            (self.uri, self.user, self.password)
        {
            let cfg = CommLinkCfg::default()
                .with_protocol(31)
                .with_uri(&uri)
                .with_user(&user)
                .with_password(&password);
            return Ok(Some(cfg));
        }
        if any {
            return Err(Error::InvalidConfig("arguments"));
        }
        Ok(None)
    }
}

/// Comm link task
struct CommLinkTask {
    /// Configuration
    cfg: CommLinkCfg,
    /// Task abort handle
    handle: AbortHandle,
}

/// Poll comm links
async fn poll_comm_links(db: Database) -> Result<()> {
    let (notifier, mut stream) = db
        .clone()
        .notifier(
            ["comm_config", "comm_link", "controller", "detector"].into_iter(),
        )
        .await?;
    let mut tasks: HashMap<String, CommLinkTask> = HashMap::new();
    let mut set = JoinSet::new();
    set.spawn(notifier.run());
    loop {
        let cfgs = CommLinkCfg::lookup_all(db.clone()).await?;
        if cfgs.is_empty() {
            break;
        }
        tasks.retain(|name, task| {
            match cfgs.iter().find(|cfg| cfg.name() == name) {
                Some(cfg) => {
                    let changed = *cfg != task.cfg;
                    if changed {
                        log::info!("{name}: changed, aborting");
                        // comm link changed: abort task
                        task.handle.abort();
                    }
                    !changed
                }
                None => {
                    log::info!("{name}: removed, aborting");
                    // no comm link: abort task
                    task.handle.abort();
                    false
                }
            }
        });
        // spawn tasks for new comm links
        for cfg in &cfgs {
            if !tasks.contains_key(cfg.name()) {
                let name = cfg.name().to_string();
                let db = Some(db.clone());
                log::info!("{name}: spawning");
                let handle = set.spawn(cfg.clone().run(db));
                let task = CommLinkTask {
                    cfg: cfg.clone(),
                    handle,
                };
                tasks.insert(name, task);
            }
        }
        while let Some(not) = stream.recv().await {
            if should_reload(&not, &cfgs) {
                log::info!(
                    "{} {}: reloading configuration",
                    not.channel(),
                    not.payload()
                );
                tokio::time::sleep(Duration::from_secs(2)).await;
                // Empty the notification stream
                while !stream.is_empty() {
                    stream.recv().await;
                }
                break;
            }
        }
    }
    Ok(())
}

/// Check if a notification should trigger reloading the configuration
fn should_reload(not: &Notification, cfgs: &[CommLinkCfg]) -> bool {
    // FIXME: comm_link "connected" / controller "fail_time" should not reload
    match (not.channel(), not.payload()) {
        ("comm_config", _) => true,
        ("comm_link", nm) => {
            nm.is_empty() || cfgs.iter().any(|c| c.name() == nm)
        }
        ("controller", nm) => {
            nm.is_empty() || cfgs.iter().any(|c| c.controller() == nm)
        }
        ("detector", nm) => {
            nm.is_empty() || cfgs.iter().any(|c| c.has_detector(nm))
        }
        _ => false,
    }
}

/// Main entry point
#[tokio::main]
async fn main() -> Result<()> {
    env_logger::builder().format_timestamp(None).init();
    let args: Args = argh::from_env();
    if let Some(cfg) = args.comm_link_config().await? {
        tokio::spawn(cfg.run(None)).await??;
        return Ok(());
    }
    let db = Database::new("tms").await?;
    loop {
        poll_comm_links(db.clone()).await?;
        tokio::time::sleep(Duration::from_secs(60)).await;
    }
}
