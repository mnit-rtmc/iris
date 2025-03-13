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
use crate::error::Result;
use crate::util::{ContainsLower, HtmlStr};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// LCS locks
#[derive(Debug, Deserialize)]
pub struct LcsLock {
    pub id: u32,
    pub description: String,
}

/// LCS Array
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct LcsArray {
    pub name: String,
    pub notes: Option<String>,
    pub lcs_lock: Option<u32>,
    // secondary attributes
    pub shift: Option<u32>,
}

/// Ancillary LCS array data
#[derive(Debug, Default)]
pub struct LcsArrayAnc {
    assets: Vec<Asset>,
    pub locks: Vec<LcsLock>,
}

impl LcsArrayAnc {
    /// Get lock description
    fn lock(&self, pri: &LcsArray) -> &str {
        if let Some(lcs_lock) = pri.lcs_lock {
            for lock in &self.locks {
                if lcs_lock == lock.id {
                    return &lock.description;
                }
            }
        }
        ""
    }
}

impl AncillaryData for LcsArrayAnc {
    type Primary = LcsArray;

    /// Construct ancillary LCS array data
    fn new(_pri: &LcsArray, _view: View) -> Self {
        LcsArrayAnc {
            assets: vec![Asset::LcsLocks],
            locks: Vec::new(),
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        _pri: &LcsArray,
        _asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        self.locks = serde_wasm_bindgen::from_value(value)?;
        Ok(())
    }
}

impl LcsArray {
    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &LcsArrayAnc) -> String {
        let name = HtmlStr::new(self.name());
        let lock = anc.lock(self);
        format!(
            "<div class='title row'>\
              <span>{name}</span>\
              <span>{lock}</span>\
            </div>"
        )
    }

    /// Convert to Control HTML
    fn to_html_control(&self, anc: &LcsArrayAnc) -> String {
        let title = self.title(View::Control);
        let lock = anc.lock(self);
        format!(
            "{title}\
            <div class='row'>\
              <span class='info'>{lock}</span>\
            </div>"
        )
    }
}

impl Card for LcsArray {
    type Ancillary = LcsArrayAnc;

    /// Display name
    const DNAME: &'static str = "🠟✖🠟 LCS Array";

    /// Get the resource
    fn res() -> Res {
        Res::LcsArray
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
    fn is_match(&self, search: &str, anc: &LcsArrayAnc) -> bool {
        self.name.contains_lower(search) || anc.lock(self).contains(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &LcsArrayAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Control => self.to_html_control(anc),
            _ => self.to_html_compact(anc),
        }
    }
}
