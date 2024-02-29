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
use crate::resource::channels_all;
use futures::Stream;
use std::future::Future;
use std::pin::Pin;
use std::task::{Context, Poll};
use tokio::sync::mpsc::{unbounded_channel, UnboundedSender};
use tokio_postgres::tls::NoTlsStream;
use tokio_postgres::{AsyncMessage, Connection, Notification, Socket};

/// DB notification handler
struct NotificationHandler {
    /// DB connection
    conn: Connection<Socket, NoTlsStream>,
    /// Notification sender
    tx: UnboundedSender<Notification>,
}

impl Future for NotificationHandler {
    type Output = ();

    fn poll(self: Pin<&mut Self>, cx: &mut Context) -> Poll<Self::Output> {
        match self.conn.poll_message(cx) {
            Poll::Pending => Poll::Pending,
            Poll::Ready(None) => {
                log::warn!("DB notification stream ended");
                Poll::Ready(())
            }
            Poll::Ready(Some(Ok(AsyncMessage::Notice(n)))) => {
                log::warn!("DB notice: {n}");
                Poll::Pending
            }
            Poll::Ready(Some(Ok(AsyncMessage::Notification(n)))) => {
                self.tx.send(n);
                Poll::Ready(())
            }
            Poll::Ready(Some(Ok(_))) => {
                log::warn!("DB AsyncMessage unknown");
                Poll::Pending
            }
            Poll::Ready(Some(Err(e))) => {
                log::error!("DB error");
                panic!("{e}")
            }
        }
    }
}

/// Create stream to listen for DB notifications
pub async fn notification_stream(
    db: &Database,
) -> Result<impl Stream<Item = Notification>> {
    let (client, conn) = db.dedicated_client().await?;
    let (tx, mut rx) = unbounded_channel();
    for chan in channels_all() {
        let listen = format!("LISTEN {chan}");
        client.execute(&listen, &[]).await?;
        // FIXME: add notification to sender for query_all
    }
    tokio::spawn(NotificationHandler { conn, tx });
    // create a stream from channel receiver
    Ok(async_stream::stream! {
        while let Some(not) = rx.recv().await {
            yield not;
        }
    })
}
