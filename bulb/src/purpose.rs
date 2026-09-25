// Copyright (C) 2022-2026  Minnesota Department of Transportation
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

/// Dedicated Purposes
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum DedicatedPurpose {
    /// Lane-Use (LCS, lane marking, etc.)
    LaneUse,
    /// Parking Sign
    Parking,
    /// Tolling (HOT lane, etc.)
    Tolling,
    /// Travel Time Sign
    TravelTime,
    /// Wayfinding Route Sign
    Wayfinding,
    /// Safety Sign
    Safety,
    /// Variable Speed Limit / Advisory
    Vsl,
    /// Hidden (not shown on map)
    Hidden,
}

impl DedicatedPurpose {
    /// Get iterator of all variants
    pub fn iter() -> impl Iterator<Item = DedicatedPurpose> {
        use DedicatedPurpose::*;
        [
            LaneUse, Parking, Tolling, TravelTime, Wayfinding, Safety, Vsl,
            Hidden,
        ]
        .iter()
        .cloned()
    }

    /// Get hashtag
    pub fn hashtag(self) -> &'static str {
        use DedicatedPurpose::*;
        match self {
            LaneUse => "#LaneUse",
            Parking => "#Parking",
            Tolling => "#Tolling",
            TravelTime => "#TravelTime",
            Wayfinding => "#Wayfinding",
            Safety => "#Safety",
            Vsl => "#Vsl",
            Hidden => "#Hidden",
        }
    }

    /// Get CSS variable name
    pub fn css_var(self) -> &'static str {
        use DedicatedPurpose::*;
        match self {
            LaneUse => "lane-use-display",
            Parking => "parking-display",
            Tolling => "tolling-display",
            TravelTime => "travel-time-display",
            Wayfinding => "wayfinding-display",
            Safety => "safety-display",
            Vsl => "vsl-display",
            Hidden => "hidden-display",
        }
    }
}
