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
use crate::util::{Dom, HtmlStr};
use crate::Result;
use serde::de::DeserializeOwned;
use serde_json::map::Map;
use serde_json::Value;
use wasm_bindgen::JsValue;
use web_sys::Document;

/// CSS class for titles
const TITLE: &str = "title";

/// CSS class for names
pub const NAME: &str = "ob_name";

/// Compact "Create" card
const CREATE_COMPACT: &str = "<span class='create'>Create 🆕</span>";

/// Type of card
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum CardType {
    /// Compact in list
    Compact,

    /// Create card
    Create,

    /// Status card
    Status,

    /// Edit card
    Edit,
}

/// A card can be displayed in a card list
pub trait Card: DeserializeOwned {
    const TNAME: &'static str;
    const ENAME: &'static str;
    const URI: &'static str;
    const HAS_STATUS: bool = false;

    /// Create from a JSON value
    fn new(json: &JsValue) -> Result<Self> {
        json.into_serde::<Self>().map_err(|e| e.to_string().into())
    }

    /// Build form using JSON value
    fn build_card(
        name: &str,
        json: &Option<JsValue>,
        ct: CardType,
    ) -> Result<String> {
        match (json, ct) {
            (Some(json), CardType::Compact) => Self::build_compact_form(json),
            (Some(json), CardType::Status) if Self::HAS_STATUS => {
                Self::build_status_form(json)
            }
            (_, CardType::Create) => Self::build_create_form(name),
            (None, _) => Ok(CREATE_COMPACT.into()),
            (Some(json), _) => Self::build_edit_form(json),
        }
    }

    /// Build a compact card
    fn build_compact_form(json: &JsValue) -> Result<String> {
        let val = Self::new(json)?;
        Ok(val.to_html_compact())
    }

    /// Build a create card
    fn build_create_form(name: &str) -> Result<String> {
        let ename = Self::ENAME;
        Ok(format!(
            "<div class='row'>\
              <div class='{TITLE}'>{ename}</div>\
              <span class='{NAME}'>🆕</span>\
            </div>\
            <div class='row'>\
             <label for='create_name'>Name</label>\
             <input id='create_name' maxlength='24' size='24' value='{name}'/>\
            </div>\
            <div class='row'>\
              <button id='ob_close' type='button'>❌ Close</button>\
              <button id='ob_save' type='button'>🖍️ Save</button>\
            </div>"
        ))
    }

    /// Get value to create a new object
    fn create_value(doc: &Document) -> Result<String> {
        if let Some(name) = doc.input_parse::<String>("create_name") {
            if !name.is_empty() {
                let mut obj = Map::new();
                obj.insert("name".to_string(), Value::String(name));
                return Ok(Value::Object(obj).to_string());
            }
        }
        Err("name missing".into())
    }

    /// Build a status card
    fn build_status_form(json: &JsValue) -> Result<String> {
        let ename = Self::ENAME;
        let val = Self::new(json)?;
        let name = HtmlStr::new(val.name());
        // could use 🌐 instead
        Ok(format!(
            "<div class='row'>\
              <div class='{TITLE}'>{ename}</div>\
              <span class='{NAME}'>{name}</span>\
            </div>\
            {}\
            <div class='row'>\
              <button id='ob_close' type='button'>❌ Close</button>\
              <button id='ob_loc' type='button'>🗺️ Location</button>\
              <button id='ob_edit' type='button'>📝 Edit</button>\
            </div>",
            val.to_html_status()
        ))
    }

    /// Build an edit card
    fn build_edit_form(json: &JsValue) -> Result<String> {
        let ename = Self::ENAME;
        let val = Self::new(json)?;
        let name = HtmlStr::new(val.name());
        Ok(format!(
            "<div class='row'>\
              <div class='{TITLE}'>{ename}</div>\
              <span class='{NAME}'>{name}</span>\
            </div>\
            {}\
            <div class='row'>\
              <button id='ob_close' type='button'>❌ Close</button>\
              <button id='ob_delete' type='button'>🗑️ Delete</button>\
              <button id='ob_save' type='button'>🖍️ Save</button>\
            </div>",
            val.to_html_edit()
        ))
    }

    /// Get changed fields from Edit form
    fn changed_fields(doc: &Document, json: &JsValue) -> Result<String>;

    /// Get the name
    fn name(&self) -> &str;

    /// Check if a search string matches
    fn is_match(&self, _tx: &str) -> bool {
        false
    }

    /// Get next suggested name
    fn next_name(_obs: &[Self]) -> String {
        "".into()
    }

    /// Build a list of cards from a JSON array
    fn build_cards(json: &JsValue, tx: &str) -> Result<String> {
        let tname = Self::TNAME;
        let mut html = String::new();
        html.push_str("<ul class='cards'>");
        let obs = json
            .into_serde::<Vec<Self>>()
            .map_err(|e| JsValue::from(e.to_string()))?;
        let next_name = Self::next_name(&obs);
        if tx.is_empty() {
            // the "Create" card has id "{tname}_" and next available name
            html.push_str(&format!(
                "<li id='{tname}_' name='{next_name}' class='card'>\
                    {CREATE_COMPACT}\
                </li>"
            ));
        }
        // TODO: split this into async calls so it can be cancelled
        for ob in obs.iter().filter(|ob| ob.is_match(tx)) {
            let name = HtmlStr::new(ob.name());
            html.push_str(&format!(
                "<li id='{tname}_{name}' name='{name}' class='card'>"
            ));
            html.push_str(&ob.to_html_compact());
            html.push_str("</li>");
        }
        html.push_str("</ul>");
        Ok(html)
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String;

    /// Convert to status HTML
    fn to_html_status(&self) -> String {
        unreachable!()
    }

    /// Convert to edit HTML
    fn to_html_edit(&self) -> String {
        unreachable!()
    }
}

/// Get attribute for disabled cards
pub fn disabled_attr(enabled: bool) -> &'static str {
    if enabled {
        ""
    } else {
        " class='disabled'"
    }
}
