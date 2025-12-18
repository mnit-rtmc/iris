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
use std::pin::Pin;
use std::task::{Context, Poll};
use tokio::sync::mpsc::{
    UnboundedReceiver, UnboundedSender, unbounded_channel,
};
use tokio_postgres::tls::NoTlsStream;
use tokio_postgres::{AsyncMessage, Client, Connection, Notification, Socket};

/// Handler for Postgres NOTIFY
pub struct Notifier {
    /// DB connection
    conn: Connection<Socket, NoTlsStream>,
    /// Notification sender
    tx: UnboundedSender<Notification>,
}

/// Receiver for Postgres NOTIFY
pub struct Receiver {
    /// DB client
    client: Client,
    /// Notification receiver
    rx: UnboundedReceiver<Notification>,
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

impl Receiver {
    /// Listen for notifications on channels
    pub async fn listen(
        &self,
        channels: impl Iterator<Item = &str>,
    ) -> Result<()> {
        for channel in channels {
            self.client
                .execute(&format!("LISTEN {channel}"), &[])
                .await?;
        }
        Ok(())
    }

    /// Receive one notification
    pub async fn recv(&mut self) -> Option<Notification> {
        self.rx.recv().await
    }

    /// Check if channel is empty
    pub fn is_empty(&self) -> bool {
        self.rx.is_empty()
    }
}

impl Database {
    /// Create a new notifier / receiver
    pub async fn notifier(self) -> Result<(Notifier, Receiver)> {
        let (client, conn) = self.dedicated_client().await?;
        let (tx, rx) = unbounded_channel();
        let not = Notifier { conn, tx };
        let rcv = Receiver { client, rx };
        Ok((not, rcv))
    }
}
