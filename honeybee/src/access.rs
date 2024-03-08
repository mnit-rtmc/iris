// access.rs
//
// Copyright (C) 2021-2024  Minnesota Department of Transportation
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
use crate::sonar::Error;

/// Access for permission records
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum Access {
    View,
    Operate,
    Manage,
    Configure,
}

impl Access {
    /// Create access from level
    pub const fn new(level: i32) -> Option<Self> {
        match level {
            1 => Some(Self::View),
            2 => Some(Self::Operate),
            3 => Some(Self::Manage),
            4 => Some(Self::Configure),
            _ => None,
        }
    }

    /// Get access level
    pub const fn level(self) -> i32 {
        match self {
            Access::View => 1,
            Access::Operate => 2,
            Access::Manage => 3,
            Access::Configure => 4,
        }
    }

    /// Check for access to a resource
    pub const fn check(self, rhs: Self) -> Result<(), Error> {
        if self.level() >= rhs.level() {
            Ok(())
        } else {
            Err(Error::Forbidden)
        }
    }
}
