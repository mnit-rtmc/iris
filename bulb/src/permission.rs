// Copyright (C) 2022  Minnesota Department of Transportation
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
use crate::card::{Card, NAME};
use crate::util::HtmlStr;
use crate::Result;
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::fmt;
use wasm_bindgen::JsValue;
use web_sys::Document;

/// Permission
#[derive(Debug, Deserialize, Serialize)]
pub struct Permission {
    pub id: u32,
    pub role: String,
    pub resource_n: String,
    pub batch: Option<String>,
    pub access_n: u32,
}

impl fmt::Display for Permission {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.id)
    }
}

impl Card for Permission {
    const TNAME: &'static str = "Permission";
    const ENAME: &'static str = "🗝️ Permission";
    const UNAME: &'static str = "permission";

    fn is_match(&self, tx: &str) -> bool {
        self.id.to_string().contains(tx) |
        self.role.to_lowercase().contains(tx) |
        self.resource_n.contains(tx)
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let role = HtmlStr::new(&self.role).with_len(4);
        let resource = HtmlStr::new(&self.resource_n).with_len(9);
        let emo = match self.access_n {
            1 => "👁️",
            2 => "👉",
            3 => "💡",
            4 => "🔧",
            _ => "?",
        };
        format!(
            "<span>{role}{emo}{resource}</span>\
            <span class='{NAME}'>{self}</span>"
        )
    }

    /// Convert to edit HTML
    fn to_html_edit(&self) -> String {
        format!(
            "<div class='row'>\
            </div>"
        )
    }

    /// Get changed fields from Edit form
    fn changed_fields(doc: &Document, json: &JsValue) -> Result<String> {
        let mut obj = Map::new();
        Ok(Value::Object(obj).to_string())
    }
}
