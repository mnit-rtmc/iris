// event.rs
//
// Copyright (C) 2024-2025  Minnesota Department of Transportation
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
use crate::error::{Error, Result};
use resin::Database;

/// Event types
#[derive(Clone, Copy, Debug, PartialEq, Eq, Hash)]
pub enum EventTp {
    FailDomain,
    FailDomainXff,
}

/// Insert a client event
const INSERT_CLIENT: &str = "\
  INSERT INTO event.client_event (\
    event_desc_id, host_port, iris_user\
  ) VALUES ($1, $2, $3)";

impl EventTp {
    fn id(self) -> i32 {
        use EventTp::*;
        match self {
            FailDomain => 207,
            FailDomainXff => 208,
        }
    }
}

/// Insert a client event
pub async fn insert_client(
    db: &Database,
    event_tp: EventTp,
    host_port: &str,
    iris_user: &str,
) -> Result<()> {
    let client = db.client().await?;
    let rows = client
        .execute(INSERT_CLIENT, &[&event_tp.id(), &host_port, &iris_user])
        .await
        .map_err(|_e| Error::InvalidValue)?;
    if rows == 1 {
        Ok(())
    } else {
        Err(Error::InvalidValue)
    }
}
