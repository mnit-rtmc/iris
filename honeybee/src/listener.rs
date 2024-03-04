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
use crate::error::{Error, Result};
use crate::resource::Resource;
use crate::restype::ResType;
use futures::Stream;
use std::collections::HashSet;
use std::future::Future;
use std::pin::Pin;
use std::task::{Context, Poll};
use tokio::sync::mpsc::{unbounded_channel, UnboundedSender};
use tokio_postgres::tls::NoTlsStream;
use tokio_postgres::{AsyncMessage, Connection, Notification, Socket};

/// DB notify event
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct NotifyEvent {
    /// Resource type
    pub res_type: ResType,
    /// Resource name
    pub name: Option<String>,
}

impl TryFrom<Notification> for NotifyEvent {
    type Error = Error;

    fn try_from(not: Notification) -> Result<Self> {
        if let Ok(res_type) = ResType::try_from(not.channel()) {
            let name =
                (!not.payload().is_empty()).then(|| not.payload().to_string());
            return Ok(NotifyEvent { res_type, name });
        }
        Err(Error::UnknownResource(not.channel().to_string()))
    }
}

/// DB notification handler
struct NotificationHandler {
    /// DB connection
    conn: Connection<Socket, NoTlsStream>,
    /// Notify event sender
    tx: UnboundedSender<NotifyEvent>,
}

impl Future for NotificationHandler {
    type Output = ();

    fn poll(mut self: Pin<&mut Self>, cx: &mut Context) -> Poll<Self::Output> {
        match self.conn.poll_message(cx) {
            Poll::Pending => Poll::Pending,
            Poll::Ready(None) => {
                log::warn!("DB notification stream ended");
                Poll::Ready(())
            }
            Poll::Ready(Some(Ok(AsyncMessage::Notification(n)))) => {
                match NotifyEvent::try_from(n) {
                    Ok(ne) => {
                        if let Err(e) = self.tx.send(ne) {
                            log::warn!("Send notification: {e}");
                        }
                        Poll::Ready(())
                    }
                    Err(e) => {
                        log::warn!("Notification: {e}");
                        Poll::Ready(())
                    }
                }
            }
            Poll::Ready(Some(Ok(AsyncMessage::Notice(n)))) => {
                log::warn!("DB notice: {n}");
                Poll::Ready(())
            }
            Poll::Ready(Some(Ok(_))) => {
                log::warn!("DB AsyncMessage unknown");
                Poll::Ready(())
            }
            Poll::Ready(Some(Err(e))) => {
                log::error!("DB error");
                panic!("{e}")
            }
        }
    }
}

/// Create stream to listen for DB notify events
pub async fn notify_events(
    db: &Database,
) -> Result<impl Stream<Item = NotifyEvent> + Unpin> {
    let (client, conn) = db.dedicated_client().await?;
    let (tx, mut rx) = unbounded_channel();
    let mut channels = HashSet::new();
    for res in Resource::iter() {
        if let Some(chan) = res.listen() {
            if channels.insert(chan) {
                let listen = format!("LISTEN {chan}");
                client.execute(&listen, &[]).await?;
                if let Ok(res_type) = ResType::try_from(chan) {
                    let ne = NotifyEvent {
                        res_type,
                        name: None,
                    };
                    if let Err(e) = tx.send(ne) {
                        log::warn!("Send notification: {e}");
                    }
                }
            }
        }
    }
    tokio::spawn(NotificationHandler { conn, tx });
    // create a stream from channel receiver
    Ok(Box::pin(async_stream::stream! {
        while let Some(not) = rx.recv().await {
            yield not;
        }
    }))
}
