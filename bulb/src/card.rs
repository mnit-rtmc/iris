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
use crate::util::HtmlStr;
use crate::Result;
use serde::de::DeserializeOwned;
use wasm_bindgen::JsValue;

/// CSS class for titles
const TITLE: &str = "title";

/// CSS class for names
pub const NAME: &str = "ob_name";

/// Type of card
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum CardType {
    /// Compact in list
    Compact,

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
    fn build_card(json: &JsValue, ct: CardType) -> Result<String> {
        match ct {
            CardType::Compact => Self::build_compact_form(json),
            CardType::Status if Self::HAS_STATUS => {
                Self::build_status_form(json)
            }
            _ => Self::build_edit_form(json),
        }
    }

    /// Build a compact card
    fn build_compact_form(json: &JsValue) -> Result<String> {
        let val = Self::new(json)?;
        Ok(val.to_html(CardType::Compact))
    }

    /// Build a status card
    fn build_status_form(json: &JsValue) -> Result<String> {
        let ename = Self::ENAME;
        let val = Self::new(json)?;
        let name = HtmlStr(val.name());
        Ok(format!(
            "<div class='row'>\
              <div class='{TITLE}'>{ename}</div>\
              <span class='{NAME}'>{name}</span>\
            </div>\
            {}\
            <div class='row'>\
              <button id='ob_edit' type='button'>📝 Edit</button>\
            </div>",
            val.to_html(CardType::Status)
        ))
    }

    /// Build an edit card
    fn build_edit_form(json: &JsValue) -> Result<String> {
        let ename = Self::ENAME;
        let val = Self::new(json)?;
        let name = HtmlStr(val.name());
        let status = if Self::HAS_STATUS {
            "<button id='ob_status' type='button'>📄 Status</button>"
        } else {
            ""
        };
        Ok(format!(
            "<div class='row'>\
              <div class='{TITLE}'>{ename}</div>\
              <span class='{NAME}'>{name}</span>\
            </div>\
            {}\
            <div class='row'>\
              {status}
              <button id='ob_delete' type='button'>🗑️ Delete</button>\
              <button id='ob_save' type='button'>🖍️ Save</button>\
            </div>",
            val.to_html(CardType::Edit)
        ))
    }

    /// Get the name
    fn name(&self) -> &str;

    /// Check if a search string matches
    fn is_match(&self, _tx: &str) -> bool {
        false
    }

    /// Build a list of cards from a JSON array
    fn build_cards(json: &JsValue, tx: &str) -> Result<String> {
        let tname = Self::TNAME;
        let mut html = String::new();
        html.push_str("<ul class='cards'>");
        if tx.is_empty() {
            // the "New" card has id "{tname}_" and blank name
            html.push_str(&format!(
                "<li id='{tname}_' name='' class='card'>\
                    <span class='create'>Create 🆕</span>\
                </li>"
            ));
        }
        let obs = json
            .into_serde::<Vec<Self>>()
            .map_err(|e| JsValue::from(e.to_string()))?;
        // TODO: split this into async calls so it can be cancelled
        for ob in obs.iter().filter(|ob| ob.is_match(tx)) {
            let name = HtmlStr(ob.name());
            html.push_str(&format!(
                "<li id='{tname}_{name}' name='{name}' class='card'>"
            ));
            html.push_str(&ob.to_html(CardType::Compact));
            html.push_str("</li>");
        }
        html.push_str("</ul>");
        Ok(html)
    }

    /// Convert to HTML
    fn to_html(&self, ct: CardType) -> String {
        match ct {
            CardType::Compact => self.to_html_compact(),
            CardType::Status => self.to_html_status(),
            CardType::Edit => self.to_html_edit(),
        }
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String;

    /// Convert to status HTML
    fn to_html_status(&self) -> String {
        unreachable!()
    }

    /// Convert to status HTML
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
