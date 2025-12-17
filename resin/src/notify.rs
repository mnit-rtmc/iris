// notify.rs
//
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
use crate::database::Database;
use crate::error::Result;
use futures::Stream;
use std::pin::Pin;
use std::task::{Context, Poll};
use tokio::sync::mpsc::{UnboundedSender, unbounded_channel};
use tokio_postgres::tls::NoTlsStream;
use tokio_postgres::{AsyncMessage, Client, Connection, Notification, Socket};
use tokio_stream::wrappers::UnboundedReceiverStream;

/// Handler for Postgres NOTIFY
pub struct Notifier {
    /// DB client (kept so it's not dropped)
    #[allow(unused)]
    client: Client,
    /// DB connection
    conn: Connection<Socket, NoTlsStream>,
    /// Notification sender
    tx: UnboundedSender<Notification>,
}

impl Future for Notifier {
    type Output = bool;

    fn poll(mut self: Pin<&mut Self>, cx: &mut Context) -> Poll<Self::Output> {
        match self.conn.poll_message(cx) {
            Poll::Ready(None) => {
                log::warn!("DB notification stream ended");
                Poll::Ready(false)
            }
            Poll::Ready(Some(Err(e))) => {
                log::error!("DB error: {e}");
                Poll::Ready(false)
            }
            Poll::Ready(Some(Ok(AsyncMessage::Notification(n)))) => {
                log::debug!("Notification: {n:?}");
                match self.tx.send(n) {
                    Err(e) => {
                        log::warn!("Send notification: {e}");
                        Poll::Ready(false)
                    }
                    _ => Poll::Ready(true),
                }
            }
            _ => Poll::Pending,
        }
    }
}

impl Notifier {
    /// Run notifier handler
    pub async fn run(mut self) -> Result<()> {
        while (&mut self).await {
            log::debug!("Notifier iteration");
        }
        log::warn!("Notifier stopped");
        Ok(())
    }
}

impl Database {
    /// Create a new notifier
    pub async fn notifier(
        self,
        channels: impl Iterator<Item = &str>,
    ) -> Result<(Notifier, impl Stream<Item = Notification> + Unpin)> {
        let (client, conn) = self.dedicated_client().await?;
        let (tx, rx) = unbounded_channel();
        for channel in channels {
            client.execute(&format!("LISTEN {channel}"), &[]).await?;
        }
        let not = Notifier { client, conn, tx };
        let stream = Box::pin(UnboundedReceiverStream::new(rx));
        Ok((not, stream))
    }
}
