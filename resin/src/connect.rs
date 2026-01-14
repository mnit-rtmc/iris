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
use crate::error::{Error, Result};

/// SQL to connect comm_link
const COMM_LINK_CONNECT: &str = "\
  UPDATE iris.comm_link \
  SET connected = true \
  WHERE name = $1";

/// SQL to connect controller
const CONTROLLER_CONNECT: &str = "\
  UPDATE iris.controller \
  SET comm_state = 1, fail_time = NULL \
  WHERE name = $1";

/// SQL to disconnect comm_link
const COMM_LINK_DISCONNECT: &str = "\
  UPDATE iris.comm_link \
  SET connected = false \
  WHERE name = $1";

/// SQL to disconnect controller
const CONTROLLER_DISCONNECT: &str = "\
  UPDATE iris.controller \
  SET comm_state = 2, fail_time = now() \
  WHERE fail_time IS NULL \
  AND name = $1";

impl Database {
    /// Log controller connect in database
    pub async fn log_connect(
        self,
        comm_link: &str,
        controller: &str,
    ) -> Result<()> {
        let mut client = self.client().await?;
        let transaction = client.transaction().await?;
        let rows = transaction
            .execute(COMM_LINK_CONNECT, &[&comm_link])
            .await?;
        if rows != 1 {
            return Err(Error::DbUpdate);
        }
        let rows = transaction
            .execute(CONTROLLER_CONNECT, &[&controller])
            .await?;
        if rows != 1 {
            return Err(Error::DbUpdate);
        }
        transaction.commit().await?;
        Ok(())
    }

    /// Log controller disconnect in database
    pub async fn log_disconnect(
        self,
        comm_link: &str,
        controller: &str,
    ) -> Result<()> {
        let mut client = self.client().await?;
        let transaction = client.transaction().await?;
        let rows = transaction
            .execute(COMM_LINK_DISCONNECT, &[&comm_link])
            .await?;
        if rows != 1 {
            return Err(Error::DbUpdate);
        }
        let rows = transaction
            .execute(CONTROLLER_DISCONNECT, &[&controller])
            .await?;
        if rows != 1 {
            return Err(Error::DbUpdate);
        }
        transaction.commit().await?;
        Ok(())
    }
}
