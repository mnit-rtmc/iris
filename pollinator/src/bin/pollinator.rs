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
use pollinator::rtms_echo::SensorCfg;
use pollinator::{Database, Error, Result};

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
    /// Get sensor configurations
    async fn sensor_configs(self) -> Result<Vec<SensorCfg>> {
        let any = self.uri.is_some()
            || self.user.is_some()
            || self.password.is_some();
        if let (Some(uri), Some(user), Some(password)) =
            (self.uri, self.user, self.password)
        {
            let cfg = SensorCfg::default()
                .with_uri(&uri)
                .with_user(&user)
                .with_password(&password);
            return Ok(vec![cfg]);
        }
        if any {
            return Err(Error::InvalidConfiguration);
        }
        Ok(vec![])
    }
}

/// Poll sensor configurations
async fn poll_sensors(
    cfgs: Vec<SensorCfg>,
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
    let cfgs = args.sensor_configs().await?;
    if cfgs.is_empty() {
        let db = Database::new("tms").await?;
        let cfgs = SensorCfg::lookup_all(db.clone()).await?;
        poll_sensors(cfgs, Some(db)).await?;
    } else {
        poll_sensors(cfgs, None).await?;
    }
    Ok(())
}
