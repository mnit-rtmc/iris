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
use std::pin::Pin;
use std::task::{Context, Poll};
use tokio::task::{AbortHandle, JoinSet};
use tokio::time::{Duration, interval};
use tokio_postgres::tls::NoTlsStream;
use tokio_postgres::{AsyncMessage, Connection, Socket};

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

/// Handler for Postgres NOTIFY
struct NotifyHandler {
    /// DB connection
    conn: Connection<Socket, NoTlsStream>,
}

impl Future for NotifyHandler {
    type Output = bool;

    fn poll(mut self: Pin<&mut Self>, cx: &mut Context) -> Poll<Self::Output> {
        match self.conn.poll_message(cx) {
            Poll::Pending => Poll::Pending,
            Poll::Ready(None) => {
                log::warn!("DB notification stream ended");
                Poll::Ready(false)
            }
            Poll::Ready(Some(Ok(AsyncMessage::Notification(n)))) => {
                log::info!("NOTIFY: {} {}", n.channel(), n.payload());
                Poll::Ready(true)
            }
            Poll::Ready(Some(Ok(AsyncMessage::Notice(n)))) => {
                log::warn!("DB notice: {n}");
                Poll::Pending
            }
            Poll::Ready(Some(Ok(_))) => {
                log::warn!("DB AsyncMessage unknown");
                Poll::Pending
            }
            Poll::Ready(Some(Err(e))) => {
                log::error!("DB error: {e}");
                Poll::Ready(false)
            }
        }
    }
}

/// Poll comm links
async fn poll_comm_links(db: Database) -> Result<()> {
    let (client, conn) = db.dedicated_client().await?;
    client.execute("LISTEN comm_config", &[]).await?;
    client.execute("LISTEN comm_link", &[]).await?;
    client.execute("LISTEN controller", &[]).await?;
    client.execute("LISTEN detector", &[]).await?;
    let mut notifier = NotifyHandler { conn };
    let mut tasks: HashMap<String, CommLinkTask> = HashMap::new();
    let mut set = JoinSet::new();
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
                        // comm link changed: abort task
                        task.handle.abort();
                    }
                    !changed
                }
                None => {
                    // no comm link: abort task
                    task.handle.abort();
                    false
                }
            }
        });
        // spawn tasks for new comm links
        for cfg in cfgs {
            if !tasks.contains_key(cfg.name()) {
                let name = cfg.name().to_string();
                let db = Some(db.clone());
                let handle = set.spawn(cfg.clone().run(db));
                let task = CommLinkTask { cfg, handle };
                tasks.insert(name, task);
            }
        }
        if !(&mut notifier).await {
            break;
        }
    }
    Ok(())
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
        let mut ticker = interval(Duration::from_secs(60));
        // apparently, the first tick completes immediately
        ticker.tick().await;
        ticker.tick().await;
    }
}
