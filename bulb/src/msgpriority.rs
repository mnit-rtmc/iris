// Copyright (C) 2025-2026  Minnesota Department of Transportation
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

/// Message priority values
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum MsgPriority {
    /// Low 1 (blank)
    Low1 = 1,
    /// Low 2
    Low2 = 2,
    /// Low 3
    Low3 = 3,
    /// Low 4
    Low4 = 4,
    /// Low system priority (cleared incidents)
    LowSys = 5,
    /// Medium 1
    Medium1 = 6,
    /// Medium 2
    Medium2 = 7,
    /// Medium 3
    Medium3 = 8,
    /// Medium 4
    Medium4 = 9,
    /// Medium (other system)
    MediumSys = 10,
    /// High 1 (operator)
    High1 = 11,
    /// High 2
    High2 = 12,
    /// High 3
    High3 = 13,
    /// High 4
    High4 = 14,
    /// High system priority
    HighSys = 15,
}

impl TryFrom<u8> for MsgPriority {
    type Error = ();

    fn try_from(p: u8) -> std::result::Result<Self, Self::Error> {
        match p {
            1 => Ok(Self::Low1),
            2 => Ok(Self::Low2),
            3 => Ok(Self::Low3),
            4 => Ok(Self::Low4),
            5 => Ok(Self::LowSys),
            6 => Ok(Self::Medium1),
            7 => Ok(Self::Medium2),
            8 => Ok(Self::Medium3),
            9 => Ok(Self::Medium4),
            10 => Ok(Self::MediumSys),
            11 => Ok(Self::High1),
            12 => Ok(Self::High2),
            13 => Ok(Self::High3),
            14 => Ok(Self::High4),
            15 => Ok(Self::HighSys),
            _ => Err(()),
        }
    }
}

impl MsgPriority {
    pub fn as_str(self) -> &'static str {
        match self {
            Self::Low1 => "low 1",
            Self::Low2 => "low 2",
            Self::Low3 => "low 3",
            Self::Low4 => "low 4",
            Self::LowSys => "low system",
            Self::Medium1 => "medium 1",
            Self::Medium2 => "medium 2",
            Self::Medium3 => "medium 3",
            Self::Medium4 => "medium 4",
            Self::MediumSys => "medium system",
            Self::High1 => "high 1",
            Self::High2 => "high 2",
            Self::High3 => "high 3",
            Self::High4 => "high 4",
            Self::HighSys => "high system",
        }
    }
}
