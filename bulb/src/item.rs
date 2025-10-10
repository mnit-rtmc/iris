// Copyright (C) 2022-2025  Minnesota Department of Transportation
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
use hatmil::Html;
use std::fmt;

/// Item state
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum ItemState {
    /// Available for use
    Available,
    /// Deployed by operator
    Deployed,
    /// Deployed by plan / schedule
    Planned,
    /// Deployed for incident
    Incident,
    /// Locked by operator
    Locked,
    /// Deployed by external system
    External,
    /// Dedicated purpose
    Dedicated,
    /// Hardware fault
    Fault,
    /// Communication offline
    Offline,
    /// Inactive (deactivated)
    Inactive,
    /// View (permission)
    View,
    /// Operate (permission)
    Operate,
    /// Manage (permission)
    Manage,
    /// Configure (permission)
    Configure,
    /// Opening (gate arm)
    Opening,
    /// Open (gate arm)
    Open,
    /// Closing (gate arm)
    Closing,
    /// Closed (gate arm)
    Closed,
    /// Beacon device actions
    Beacon,
    /// Camera device actions
    Camera,
    /// DMS device actions
    Dms,
    /// Ramp meter device actions
    RampMeter,
    /// Crash incident
    Crash,
    /// Stall incident
    Stall,
    /// Hazard incident
    Hazard,
    /// Roadwork incident
    Roadwork,
    /// State not known
    Unknown,
}

/// Item states
#[derive(Clone, Debug, Default)]
pub struct ItemStates<'a> {
    all: Vec<(ItemState, &'a str)>,
}

impl fmt::Display for ItemState {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.code())
    }
}

impl ItemState {
    /// Lookup an item state by code
    pub fn from_code(code: &str) -> Option<Self> {
        match code {
            "🔹" => Some(Self::Available),
            "🔶" => Some(Self::Deployed),
            "📋" => Some(Self::Planned),
            "🚨" => Some(Self::Incident),
            "🔒" => Some(Self::Locked),
            "👽" => Some(Self::External),
            "🎯" => Some(Self::Dedicated),
            "⚠️" => Some(Self::Fault),
            "🔌" => Some(Self::Offline),
            "🔺" => Some(Self::Inactive),
            "👁️" => Some(Self::View),
            "👉" => Some(Self::Operate),
            "💡" => Some(Self::Manage),
            "🔧" => Some(Self::Configure),
            "↗️" => Some(Self::Opening),
            "✔️" => Some(Self::Open),
            "↘️" => Some(Self::Closing),
            "⛔" => Some(Self::Closed),
            "🔆" => Some(Self::Beacon),
            "🎥" => Some(Self::Camera),
            "⬛" => Some(Self::Dms),
            "🚦" => Some(Self::RampMeter),
            "💥" => Some(Self::Crash),
            "⛽" => Some(Self::Stall),
            "🪨" => Some(Self::Hazard),
            "🚧" => Some(Self::Roadwork),
            "❓" => Some(Self::Unknown),
            _ => None,
        }
    }

    /// Get the item state code
    pub fn code(self) -> &'static str {
        match self {
            Self::Available => "🔹",
            Self::Deployed => "🔶",
            Self::Planned => "📋",
            Self::Incident => "🚨",
            Self::Locked => "🔒",
            Self::External => "👽",
            Self::Dedicated => "🎯",
            Self::Fault => "⚠️",
            Self::Offline => "🔌",
            Self::Inactive => "🔺",
            Self::View => "👁️",
            Self::Operate => "👉",
            Self::Manage => "💡",
            Self::Configure => "🔧",
            Self::Opening => "↗️",
            Self::Open => "✔️",
            Self::Closing => "↘️",
            Self::Closed => "⛔",
            Self::Beacon => "🔆",
            Self::Camera => "🎥",
            Self::Dms => "⬛",
            Self::RampMeter => "🚦",
            Self::Crash => "💥",
            Self::Stall => "⛽",
            Self::Hazard => "🪨",
            Self::Roadwork => "🚧",
            Self::Unknown => "❓",
        }
    }

    /// Get the item state description
    pub fn description(self) -> &'static str {
        match self {
            Self::Available => "available",
            Self::Deployed => "deployed",
            Self::Planned => "planned",
            Self::Incident => "incident",
            Self::Locked => "locked",
            Self::External => "external",
            Self::Dedicated => "dedicated",
            Self::Fault => "fault",
            Self::Offline => "offline",
            Self::Inactive => "inactive",
            Self::View => "view",
            Self::Operate => "operate",
            Self::Manage => "manage",
            Self::Configure => "configure",
            Self::Opening => "opening",
            Self::Open => "open",
            Self::Closing => "closing",
            Self::Closed => "closed",
            Self::Beacon => "beacons",
            Self::Camera => "cameras",
            Self::Dms => "dms",
            Self::RampMeter => "ramp meters",
            Self::Crash => "crash",
            Self::Stall => "stall",
            Self::Hazard => "hazard",
            Self::Roadwork => "road work",
            Self::Unknown => "unknown",
        }
    }

    /// Check if a search string matches
    pub fn is_match(self, search: &str) -> bool {
        self.code().contains(search) || self.description().contains(search)
    }
}

impl fmt::Display for ItemStates<'_> {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        let mut first = true;
        for (state, _dtl) in self.all.iter() {
            if !first {
                write!(f, " ")?;
                first = false;
            }
            write!(f, "{}", state.code())?;
        }
        Ok(())
    }
}

impl From<ItemState> for ItemStates<'_> {
    fn from(state: ItemState) -> Self {
        ItemStates {
            all: vec![(state, "")],
        }
    }
}

impl<'a> ItemStates<'a> {
    /// Include an item state
    pub fn with(mut self, state: ItemState, dtl: &'a str) -> Self {
        if !self.all.iter().any(|is| is.0 == state) {
            self.all.push((state, dtl));
        }
        self
    }

    /// Check if states contains an item state
    pub fn contains(&self, state: ItemState) -> bool {
        self.all.iter().any(|(s, _dtl)| *s == state)
    }

    /// Check if a search string matches
    pub fn is_match(&self, search: &str) -> bool {
        self.all.iter().any(|(s, _dtl)| s.is_match(search))
    }

    /// Build item state tooltips HTML
    pub fn tooltips(&self, html: &mut Html) {
        html.div();
        for (state, dtl) in self.all.iter() {
            html.span().class("tooltip");
            html.text(state.code()).text(" ").text(state.description());
            if !dtl.is_empty() {
                let mut cls = String::from("item_");
                cls.push_str(state.description());
                html.span().class(cls);
                for d in dtl.split(';') {
                    html.text(d);
                    html.text(" ");
                }
                html.end(); /* span */
            }
            html.end(); /* span */
        }
        html.end(); /* div */
    }
}
