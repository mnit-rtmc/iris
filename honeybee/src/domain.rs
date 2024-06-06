// domain.rs
//
// Copyright (C) 2024  Minnesota Department of Transportation
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
use cidr::IpCidr;
use serde::{Deserialize, Serialize};
use std::net::SocketAddr;
use std::str::FromStr;
use tokio_postgres::Row;

/// Query domains for a user
const QUERY_BY_USER: &str = "\
  SELECT name, cidr, enabled \
  FROM iris.domain d \
  JOIN iris.user_id_domain ud ON ud.domain = d.name \
  WHERE d.enabled = true \
    AND ud.user_id = $1";

/// Domain
#[derive(Clone, Debug, Eq, PartialEq, Deserialize, Serialize)]
pub struct Domain {
    pub name: String,
    pub cidr: String,
    pub enabled: bool,
}

impl Domain {
    fn from_row(row: Row) -> Self {
        Domain {
            name: row.get(0),
            cidr: row.get(1),
            enabled: row.get(2),
        }
    }

    /// Parse domain CIDR
    fn cidr(&self) -> Result<IpCidr> {
        IpCidr::from_str(&self.cidr).map_err(|_e| Error::Forbidden)
    }
}

/// Check a user / address for a valid domain
pub async fn check_user_addr(
    db: &Database,
    user: &str,
    addr: SocketAddr,
) -> Result<()> {
    let client = db.client().await?;
    for row in client.query(QUERY_BY_USER, &[&user]).await? {
        let domain = Domain::from_row(row);
        let cidr = domain.cidr().map_err(|_e| Error::Forbidden)?;
        if cidr.contains(&addr.ip()) {
            return Ok(());
        }
    }
    Err(Error::Forbidden)
}
