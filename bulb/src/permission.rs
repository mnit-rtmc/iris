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
use crate::card::Card;
use crate::Result;
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
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

impl Card for Permission {
    const TNAME: &'static str = "Permission";
    const ENAME: &'static str = "Perm";
    const UNAME: &'static str = "permission";

    fn is_match(&self, tx: &str) -> bool {
        self.resource_n.to_lowercase().contains(tx)
    }

    fn name(&self) -> &str {
        &self.role
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let id = self.id;
        format!("<span>{id}</span>")
    }

    /// Convert to edit HTML
    fn to_html_edit(&self) -> String {
        let id = self.id;
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
