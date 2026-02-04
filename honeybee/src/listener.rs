// listener.rs
//
// Copyright (C) 2018-2026  Minnesota Department of Transportation
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
use crate::error::Result;
use crate::resource::Resource;
use crate::sonar::Name;
use futures::Stream;
use resin::Database;
use std::collections::HashSet;
use std::pin::Pin;
use std::task::{Context, Poll};
use tokio::sync::mpsc::{UnboundedSender, unbounded_channel};
use tokio_postgres::tls::NoTlsStream;
use tokio_postgres::{AsyncMessage, Connection, Notification, Socket};
use tokio_stream::wrappers::UnboundedReceiverStream;

/// DB notification handler
struct NotificationHandler {
    /// DB connection
    conn: Connection<Socket, NoTlsStream>,
    /// Notify event sender
    tx: UnboundedSender<Name>,
}

/// Run notification handler
async fn run_handler(
    conn: Connection<Socket, NoTlsStream>,
    tx: UnboundedSender<Name>,
) {
    let mut handler = NotificationHandler {
        conn,
        tx: tx.clone(),
    };
    while (&mut handler).await {
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
                log::debug!("Notification: {} {}", n.channel(), n.payload());
                self.notify_channel(n);
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

impl NotificationHandler {
    /// Notify channel with an event
    fn notify_channel(&self, n: Notification) {
        match Name::new(n.channel()) {
            Ok(nm) => {
                let obj_n = n.payload();
                let nm = if obj_n.is_empty() {
                    nm
                } else {
                    match nm.clone().obj(obj_n) {
                        Ok(nm) => nm,
                        Err(_e) => {
                            log::warn!("Invalid payload: {obj_n}");
                            nm
                        }
                    }
                };
                send_event(&self.tx, nm);
            }
            Err(_) => log::warn!("Unknown channel: {}", n.channel()),
        }
    }
}

/// Send initial notify event
fn send_event(tx: &UnboundedSender<Name>, nm: Name) {
    if let Err(e) = tx.send(nm) {
        log::warn!("Send notification: {e}");
    }
}

/// Create stream to listen for DB notify events
pub async fn notify_events(
    db: &Database,
) -> Result<impl Stream<Item = Name> + Unpin> {
    let (client, conn) = db.dedicated_client().await?;
    let (tx, rx) = unbounded_channel();
    let mut channels = HashSet::new();
    tokio::spawn(run_handler(conn, tx.clone()));
    for res in Resource::iter() {
        if let Some(chan) = res.listen()
            && channels.insert(chan)
            && let Ok(nm) = Name::new(chan)
        {
            if !nm.res_type.is_lut() {
                log::debug!("LISTEN to '{chan}' for {res:?}");
                let listen = format!("LISTEN {chan}");
                client.execute(&listen, &[]).await?;
            }
            send_event(&tx, nm);
        }
    }
    // leak client so that it's not dropped
    Box::leak(Box::new(client));
    // create a stream from channel receiver
    Ok(Box::pin(UnboundedReceiverStream::new(rx)))
}
