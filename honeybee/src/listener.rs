// listener.rs
//
// Copyright (C) 2018-2024  Minnesota Department of Transportation
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
use crate::resource::Resource;
use crate::restype::ResType;
use futures::Stream;
use std::collections::HashSet;
use std::fmt;
use std::future::Future;
use std::pin::Pin;
use std::task::{Context, Poll};
use tokio::sync::mpsc::{unbounded_channel, UnboundedSender};
use tokio_postgres::tls::NoTlsStream;
use tokio_postgres::{AsyncMessage, Connection, Notification, Socket};
use tokio_stream::wrappers::UnboundedReceiverStream;

/// DB notify event
#[derive(Clone, Debug, Eq, Hash, PartialEq)]
pub struct NotifyEvent {
    /// Notification channel
    pub channel: String,
    /// Resource name
    pub name: Option<String>,
}

impl fmt::Display for NotifyEvent {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match &self.name {
            Some(name) => write!(f, "{} {}", self.channel, name),
            None => write!(f, "{} (None)", self.channel),
        }
    }
}

impl From<Notification> for NotifyEvent {
    fn from(not: Notification) -> Self {
        let channel = not.channel().to_string();
        let name =
            (!not.payload().is_empty()).then(|| not.payload().to_string());
        NotifyEvent { channel, name }
    }
}

/// DB notification handler
struct NotificationHandler {
    /// DB connection
    conn: Connection<Socket, NoTlsStream>,
    /// Notify event sender
    tx: UnboundedSender<NotifyEvent>,
}

/// Run notification handler
async fn run_handler(
    conn: Connection<Socket, NoTlsStream>,
    tx: UnboundedSender<NotifyEvent>,
) {
    let mut handler = NotificationHandler {
        conn,
        tx: tx.clone(),
    };
    while let true = (&mut handler).await {
        log::trace!("run_handler iteration");
    }
    log::warn!("run_handler stopped");
}

impl Future for NotificationHandler {
    type Output = bool;

    fn poll(mut self: Pin<&mut Self>, cx: &mut Context) -> Poll<Self::Output> {
        match self.conn.poll_message(cx) {
            Poll::Pending => Poll::Pending,
            Poll::Ready(None) => {
                log::warn!("DB notification stream ended");
                Poll::Ready(false)
            }
            Poll::Ready(Some(Ok(AsyncMessage::Notification(n)))) => {
                let ne = NotifyEvent::from(n);
                if let Err(e) = self.tx.send(ne) {
                    log::warn!("Send notification: {e}");
                }
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

/// Send initial notify event
fn send_event(tx: &UnboundedSender<NotifyEvent>, chan: &str) {
    let ne = NotifyEvent {
        channel: chan.to_string(),
        name: None,
    };
    if let Err(e) = tx.send(ne) {
        log::warn!("Send notification: {e}");
    }
}

/// Create stream to listen for DB notify events
pub async fn notify_events(
    db: &Database,
) -> Result<impl Stream<Item = NotifyEvent> + Unpin> {
    let (client, conn) = db.dedicated_client().await?;
    let (tx, rx) = unbounded_channel();
    let mut channels = HashSet::new();
    tokio::spawn(run_handler(conn, tx.clone()));
    for res in Resource::iter() {
        if let Some(chan) = res.listen() {
            if channels.insert(chan) {
                if let Ok(res_type) = ResType::try_from(chan) {
                    if res_type.lut_channel().is_none() {
                        log::debug!("LISTEN to '{chan}' for {res:?}");
                        let listen = format!("LISTEN {chan}");
                        client.execute(&listen, &[]).await?;
                    }
                    send_event(&tx, chan);
                }
            }
        }
    }
    // leak client so that it's not dropped
    Box::leak(Box::new(client));
    // create a stream from channel receiver
    Ok(Box::pin(UnboundedReceiverStream::new(rx)))
}
