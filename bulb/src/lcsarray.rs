// Copyright (C) 2022-2024  Minnesota Department of Transportation
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
use crate::fetch::Uri;
use crate::resource::{AncillaryData, Card, View, EDIT_BUTTON, NAME};
use crate::util::{ContainsLower, Fields, HtmlStr};
use serde::{Deserialize, Serialize};
use std::fmt;
use std::iter::once;
use wasm_bindgen::JsValue;

/// LCS locks
#[derive(Debug, Deserialize, Serialize)]
pub struct LcsLock {
    pub id: u32,
    pub description: String,
}

/// LCS Array
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct LcsArray {
    pub name: String,
    pub notes: String,
    pub lcs_lock: Option<u32>,
    // full attributes
    pub shift: Option<u32>,
}

/// Ancillary LCS array data
#[derive(Debug, Default)]
pub struct LcsArrayAnc {
    pub locks: Option<Vec<LcsLock>>,
}

impl LcsArrayAnc {
    /// Get lock description
    fn lock(&self, pri: &LcsArray) -> &str {
        if let (Some(lcs_lock), Some(locks)) = (pri.lcs_lock, &self.locks) {
            for lock in locks {
                if lcs_lock == lock.id {
                    return &lock.description;
                }
            }
        }
        ""
    }
}

const LCS_LOCK_URI: &str = "/iris/lut/lcs_lock";

impl AncillaryData for LcsArrayAnc {
    type Primary = LcsArray;

    /// Get URI iterator
    fn uri_iter(
        &self,
        _pri: &LcsArray,
        _view: View,
    ) -> Box<dyn Iterator<Item = Uri>> {
        Box::new(once(LCS_LOCK_URI.into()))
    }

    /// Put ancillary data
    fn set_data(
        &mut self,
        _pri: &LcsArray,
        _uri: Uri,
        data: JsValue,
    ) -> Result<bool> {
        self.locks = Some(serde_wasm_bindgen::from_value(data)?);
        Ok(false)
    }
}

impl LcsArray {
    pub const RESOURCE_N: &'static str = "lcs_array";

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &LcsArrayAnc) -> String {
        let lock = anc.lock(self);
        format!(
            "<span>{lock}</span>\
            <span class='{NAME}'>{self}</span>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &LcsArrayAnc, config: bool) -> String {
        let lock = anc.lock(self);
        let mut status = format!(
            "<div class='row'>\
              <span class='info'>{lock}</span>\
            </div>"
        );
        if config {
            status.push_str("<div class='row'>");
            status.push_str("<span></span>");
            status.push_str(EDIT_BUTTON);
            status.push_str("</div>");
        }
        status
    }

    /// Convert to Edit HTML
    fn to_html_edit(&self) -> String {
        String::new()
    }
}

impl fmt::Display for LcsArray {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl Card for LcsArray {
    type Ancillary = LcsArrayAnc;

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
            View::Compact => self.to_html_compact(anc),
            View::Status(config) => self.to_html_status(anc, config),
            View::Edit => self.to_html_edit(),
            _ => unreachable!(),
        }
    }

    /// Get changed fields from Edit form
    fn changed_fields(&self) -> String {
        let fields = Fields::new();
        fields.into_value().to_string()
    }
}
