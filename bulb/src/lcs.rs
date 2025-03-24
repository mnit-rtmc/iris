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
use crate::asset::Asset;
use crate::card::{AncillaryData, Card, View};
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::error::Result;
use crate::geoloc::{Loc, LocAnc};
use crate::item::{ItemState, ItemStates};
use crate::start::fly_map_item;
use crate::util::{ContainsLower, HtmlStr};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use std::fmt;
use wasm_bindgen::JsValue;

/// LCS types
#[derive(Debug, Deserialize)]
#[allow(dead_code)]
pub struct LcsType {
    pub id: u32,
    pub description: String,
}

/// LCS lock reason
#[derive(Clone, Copy, Debug, PartialEq)]
enum LockReason {
    Unlocked,
    Incident,
    Testing,
    Indication,
    Maintenance,
    Construction,
}

/// LCS Lock
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct LcsLock {
    pub reason: String,
    pub indications: Option<Vec<u32>>,
    pub expires: Option<String>,
    pub user_id: Option<String>,
}

/// LCS Status
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct LcsStatus {
    pub indications: Option<Vec<u32>>,
    pub faults: Option<String>,
}

/// LCS Array
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Lcs {
    pub name: String,
    pub location: Option<String>,
    pub controller: Option<String>,
    pub notes: Option<String>,
    pub lock: Option<LcsLock>,
    pub status: Option<LcsStatus>,
    // secondary attributes
    pub geo_loc: Option<String>,
    pub pin: Option<u32>,
    pub shift: Option<u32>,
}

/// Ancillary LCS array data
#[derive(Debug, Default)]
pub struct LcsAnc {
    cio: ControllerIoAnc<Lcs>,
    loc: LocAnc<Lcs>,
    pub types: Vec<LcsType>,
}

impl AncillaryData for LcsAnc {
    type Primary = Lcs;

    /// Construct ancillary LCS array data
    fn new(pri: &Lcs, view: View) -> Self {
        let mut cio = ControllerIoAnc::new(pri, view);
        cio.assets.push(Asset::LcsTypes);
        let loc = LocAnc::new(pri, view);
        LcsAnc {
            cio,
            loc,
            types: Vec::new(),
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.cio.assets.pop().or_else(|| self.loc.assets.pop())
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &Lcs,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::Controllers => self.cio.set_asset(pri, asset, value)?,
            Asset::LcsTypes => {
                self.types = serde_wasm_bindgen::from_value(value)?;
            }
            _ => self.loc.set_asset(pri, asset, value)?,
        }
        Ok(())
    }
}

impl From<&str> for LockReason {
    fn from(r: &str) -> Self {
        match r {
            "incident" => Self::Incident,
            "testing" => Self::Testing,
            "indication" => Self::Indication,
            "maintenance" => Self::Maintenance,
            "construction" => Self::Construction,
            _ => Self::Unlocked,
        }
    }
}

impl fmt::Display for LockReason {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.as_str())
    }
}

impl LockReason {
    /// Get lock reason as a string slice
    fn as_str(&self) -> &'static str {
        use LockReason::*;
        match self {
            Unlocked => "unlocked",
            Incident => "incident",
            Testing => "testing",
            Indication => "indication",
            Maintenance => "maintenance",
            Construction => "construction",
        }
    }
}

impl ControllerIo for Lcs {
    /// Get controller name
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Loc for Lcs {
    /// Get geo location name
    fn geoloc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }
}

impl Lcs {
    /// Get faults, if any
    fn faults(&self) -> Option<&str> {
        self.status.as_ref().and_then(|s| s.faults.as_deref())
    }

    /// Get item states
    fn item_states<'a>(&'a self, anc: &'a LcsAnc) -> ItemStates<'a> {
        let mut states = anc.cio.item_states(self);
        if states.contains(ItemState::Available) {
            states = self.item_states_lock();
        }
        if let Some(faults) = self.faults() {
            states = states.with(ItemState::Fault, faults);
        }
        states
    }

    /// Get lock reason
    fn lock_reason(&self) -> LockReason {
        self.lock
            .as_ref()
            .map(|lk| LockReason::from(lk.reason.as_str()))
            .unwrap_or(LockReason::Unlocked)
    }

    /// Get item states from status/lock
    fn item_states_lock(&self) -> ItemStates<'_> {
        let deployed = match &self.status {
            Some(st) if st.indications.is_some() => true,
            _ => false,
        };
        let reason = self.lock_reason();
        let states = if reason == LockReason::Incident {
            ItemStates::default()
                .with(ItemState::Incident, LockReason::Incident.as_str())
        } else {
            ItemStates::default()
        };
        match (deployed, reason) {
            (true, LockReason::Unlocked) => states
                .with(ItemState::Deployed, "deployed"),
            (true, _) => states
                .with(ItemState::Deployed, "deployed")
                .with(ItemState::Locked, reason.as_str()),
            (false, LockReason::Unlocked) => ItemState::Available.into(),
            (false, _) => states.with(ItemState::Locked, reason.as_str()),
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, _anc: &LcsAnc) -> String {
        let name = HtmlStr::new(self.name());
        format!(
            "<div class='title row'>\
              <span>{name}</span>\
            </div>"
        )
    }

    /// Convert to Control HTML
    fn to_html_control(&self, anc: &LcsAnc) -> String {
        if let Some((lat, lon)) = anc.loc.latlon() {
            fly_map_item(&self.name, lat, lon);
        }
        let title = self.title(View::Control);
        format!(
            "{title}\
            <div class='row'>\
            </div>"
        )
    }
}

impl Card for Lcs {
    type Ancillary = LcsAnc;

    /// Display name
    const DNAME: &'static str = "🠟✖🠟 LCS";

    /// Get the resource
    fn res() -> Res {
        Res::Lcs
    }

    /// Get the name
    fn name(&self) -> Cow<str> {
        Cow::Borrowed(&self.name)
    }

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &LcsAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || self
                .notes
                .as_ref()
                .is_some_and(|n| n.contains_lower(search))
            || self.item_states(anc).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &LcsAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Control => self.to_html_control(anc),
            View::Location => anc.loc.to_html_loc(self),
            _ => self.to_html_compact(anc),
        }
    }
}
