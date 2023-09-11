// Copyright (C) 2022-2023  Minnesota Department of Transportation
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
    /// Deployed by external system
    External,
    /// Dedicated purpose
    Dedicated,
    /// Hardware fault
    Fault,
    /// Communication offline
    Offline,
    /// Disabled by administrator
    Disabled,
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
            "🕗" => Some(Self::Planned),
            "👽" => Some(Self::External),
            "🎯" => Some(Self::Dedicated),
            "⚠️" => Some(Self::Fault),
            "🔌" => Some(Self::Offline),
            "🔻" => Some(Self::Disabled),
            "❓" => Some(Self::Unknown),
            _ => None,
        }
    }

    /// Get the item state code
    pub fn code(self) -> &'static str {
        match self {
            Self::Available => "🔹",
            Self::Deployed => "🔶",
            Self::Planned => "🕗",
            Self::External => "👽",
            Self::Dedicated => "🎯",
            Self::Fault => "⚠️",
            Self::Offline => "🔌",
            Self::Disabled => "🔻",
            Self::Unknown => "❓",
        }
    }

    /// Get the item state description
    pub fn description(self) -> &'static str {
        match self {
            Self::Available => "available",
            Self::Deployed => "deployed",
            Self::Planned => "planned",
            Self::External => "external",
            Self::Dedicated => "dedicated",
            Self::Fault => "fault",
            Self::Offline => "offline",
            Self::Disabled => "disabled",
            Self::Unknown => "unknown",
        }
    }

    /// Check if a search string matches
    pub fn is_match(self, search: &str) -> bool {
        self.code().contains(search) || self.description().contains(search)
    }
}

impl<'a> fmt::Display for ItemStates<'a> {
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

impl<'a> From<ItemState> for ItemStates<'a> {
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

    /// Check if a search string matches
    pub fn is_match(&self, search: &str) -> bool {
        self.all.iter().any(|(s, _dtl)| s.is_match(search))
    }

    /// Convert item states to html
    pub fn to_html(&self) -> String {
        let mut desc = String::new();
        for (state, dtl) in self.all.iter() {
            if !desc.is_empty() {
                desc.push(' ');
            }
            desc.push_str("<div class='tooltip'>");
            desc.push_str(state.code());
            desc.push(' ');
            desc.push_str(state.description());
            if !dtl.is_empty() {
                desc.push_str("<span class='");
                desc.push_str(state.description());
                desc.push_str("'>");
                desc.push_str(dtl);
                desc.push_str("</span>");
            }
            desc.push_str("</div>");
        }
        desc
    }
}
