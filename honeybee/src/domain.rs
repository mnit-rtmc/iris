// domain.rs
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
use crate::error::Result;
use cidr::IpCidr;
use resin::Database;
use serde::{Deserialize, Serialize};
use std::net::IpAddr;
use tokio_postgres::Row;

/// Query domains for a user
const QUERY_BY_USER: &str = "\
  SELECT d.name, block, d.enabled \
  FROM iris.domain d \
  JOIN iris.role_domain rd ON rd.domain = d.name \
  JOIN iris.user_id u ON u.role = rd.role \
  JOIN iris.role r ON r.name = u.role \
  WHERE d.enabled = true \
    AND r.enabled = true \
    AND u.name = $1";

/// Domain
#[derive(Clone, Debug, Eq, PartialEq, Deserialize, Serialize)]
pub struct Domain {
    pub name: String,
    pub block: IpCidr,
    pub enabled: bool,
}

impl Domain {
    /// Get Domain from a row
    fn from_row(row: Row) -> Self {
        Domain {
            name: row.get(0),
            block: row.get(1),
            enabled: row.get(2),
        }
    }

    /// Check if domain contains an address
    pub fn contains(&self, addr: IpAddr) -> Result<bool> {
        Ok(self.block.contains(&addr))
    }
}

/// Query valid domains by user
pub async fn query_by_user(db: &Database, user: &str) -> Result<Vec<Domain>> {
    let mut domains = Vec::new();
    let client = db.client().await?;
    for row in client.query(QUERY_BY_USER, &[&user]).await? {
        domains.push(Domain::from_row(row));
    }
    Ok(domains)
}

/// Check if any domain contains an address
pub fn any_contains(domains: &[Domain], addr: IpAddr) -> Result<bool> {
    let mut res = false;
    for d in domains {
        // NOTE: check all domains in case one is not valid
        if d.contains(addr)? {
            res = true;
        }
    }
    Ok(res)
}
