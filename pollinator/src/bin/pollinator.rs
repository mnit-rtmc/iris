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
use tokio::time::{Duration, interval};

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
    /// Get comm link configurations
    async fn comm_link_configs(self) -> Result<Vec<CommLinkCfg>> {
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
            return Ok(vec![cfg]);
        }
        if any {
            return Err(Error::InvalidConfig("arguments"));
        }
        Ok(vec![])
    }
}

/// Poll comm links
async fn poll_comm_links(
    cfgs: Vec<CommLinkCfg>,
    db: Option<Database>,
) -> Result<()> {
    let mut handles = Vec::with_capacity(cfgs.len());
    for cfg in cfgs {
        handles.push(tokio::spawn(cfg.run(db.clone())));
    }
    for handle in handles {
        handle.await??;
    }
    Ok(())
}

/// Main entry point
#[tokio::main]
async fn main() -> Result<()> {
    env_logger::builder().format_timestamp(None).init();
    let args: Args = argh::from_env();
    let cfgs = args.comm_link_configs().await?;
    if cfgs.is_empty() {
        let db = Database::new("tms").await?;
        loop {
            let cfgs = CommLinkCfg::lookup_all(db.clone()).await?;
            if !cfgs.is_empty() {
                poll_comm_links(cfgs, Some(db.clone())).await?;
            }
            let mut ticker = interval(Duration::from_secs(60));
            // apparently, the first tick completes immediately
            ticker.tick().await;
            ticker.tick().await;
        }
    } else {
        poll_comm_links(cfgs, None).await?;
    }
    Ok(())
}
