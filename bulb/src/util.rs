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
use serde_json::map::Map;
use serde_json::{Number, Value};
use std::fmt;
use std::str::FromStr;
use wasm_bindgen::{JsCast, UnwrapThrowExt};
use web_sys::{
    Document, HtmlInputElement, HtmlSelectElement, HtmlTextAreaElement,
};

/// Check for items containing a search string (lower case)
pub trait ContainsLower {
    fn contains_lower(self, search: &str) -> bool;
}

impl ContainsLower for &str {
    fn contains_lower(self, search: &str) -> bool {
        self.to_lowercase().contains(search)
    }
}

impl ContainsLower for &Option<String> {
    fn contains_lower(self, search: &str) -> bool {
        self.as_deref()
            .unwrap_or("")
            .to_lowercase()
            .contains(search)
    }
}

/// An optional value which has impl Display
#[derive(Debug)]
pub struct OptVal<T>(pub Option<T>);

impl<T> fmt::Display for OptVal<T>
where
    T: fmt::Display,
{
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match &self.0 {
            Some(v) => write!(f, "{}", v),
            None => Ok(()),
        }
    }
}

impl From<OptVal<bool>> for Value {
    fn from(val: OptVal<bool>) -> Self {
        match val.0 {
            Some(b) => Value::Bool(b),
            None => Value::Null,
        }
    }
}

impl From<OptVal<u16>> for Value {
    fn from(val: OptVal<u16>) -> Self {
        match val.0 {
            Some(num) => Value::Number(num.into()),
            None => Value::Null,
        }
    }
}

impl From<OptVal<u32>> for Value {
    fn from(val: OptVal<u32>) -> Self {
        match val.0 {
            Some(num) => Value::Number(num.into()),
            None => Value::Null,
        }
    }
}

impl From<OptVal<f64>> for Value {
    fn from(val: OptVal<f64>) -> Self {
        match val.0 {
            Some(num) => match Number::from_f64(num) {
                Some(num) => Value::Number(num),
                None => Value::Null,
            },
            None => Value::Null,
        }
    }
}

impl From<OptVal<String>> for Value {
    fn from(val: OptVal<String>) -> Self {
        match val.0 {
            Some(s) => Value::String(s),
            None => Value::Null,
        }
    }
}

/// String wrapper which can be written as HTML
#[derive(Debug)]
pub struct HtmlStr<S> {
    val: S,
    len: usize,
}

impl<S> HtmlStr<S> {
    /// Create a new HTML string
    pub fn new(val: S) -> Self {
        Self {
            val,
            len: usize::MAX,
        }
    }

    /// Adjust the maximum length
    pub fn with_len(mut self, len: usize) -> Self {
        self.len = len;
        self
    }

    /// Format and encode entities
    fn fmt_encode(&self, val: &str, f: &mut fmt::Formatter) -> fmt::Result {
        for c in val.chars().take(self.len) {
            match c {
                '&' => write!(f, "&amp;")?,
                '<' => write!(f, "&lt;")?,
                '>' => write!(f, "&gt;")?,
                '"' => write!(f, "&quot;")?,
                '\'' => write!(f, "&#x27;")?,
                _ => write!(f, "{}", c)?,
            }
        }
        Ok(())
    }
}

impl fmt::Display for HtmlStr<&str> {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        self.fmt_encode(self.val, f)
    }
}

impl fmt::Display for HtmlStr<&String> {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        self.fmt_encode(self.val, f)
    }
}

impl fmt::Display for HtmlStr<String> {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        self.fmt_encode(&self.val, f)
    }
}

impl fmt::Display for HtmlStr<&Option<String>> {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self.val.as_ref() {
            Some(val) => self.fmt_encode(val, f),
            None => Ok(()),
        }
    }
}

/// Wrapper for web_sys Document
pub struct Doc(pub Document);

impl Doc {
    /// Get document
    pub fn get() -> Self {
        let window = web_sys::window().unwrap_throw();
        let doc = window.document().unwrap_throw();
        Doc(doc)
    }

    /// Get an element by ID and cast it
    pub fn elem<E: JsCast>(&self, id: &str) -> E {
        self.0
            .get_element_by_id(id)
            .ok_or_else(|| format!("Invalid element ID: {id}"))
            .unwrap()
            .dyn_into::<E>()
            .expect("Invalid element type")
    }

    /// Get and parse a `select` element value
    pub fn select_parse<T: FromStr>(&self, id: &str) -> Option<T> {
        self.elem::<HtmlSelectElement>(id).value().parse().ok()
    }

    /// Get and parse an `input` element value
    pub fn input_parse<T: FromStr>(&self, id: &str) -> Option<T> {
        self.elem::<HtmlInputElement>(id).value().trim().parse().ok()
    }

    /// Get and parse an optional `input` element string
    pub fn input_option_string(&self, id: &str) -> Option<String> {
        self.input_parse::<String>(id).filter(|v| !v.is_empty())
    }

    /// Get a boolean `input` element value
    pub fn input_bool(&self, id: &str) -> bool {
        self.elem::<HtmlInputElement>(id).checked()
    }

    /// Get and parse a `textarea` element value
    pub fn text_area_parse<T: FromStr>(&self, id: &str) -> Option<T> {
        self.elem::<HtmlTextAreaElement>(id).value().trim().parse().ok()
    }
}

/// Mapping of fields on an Edit view
pub struct Fields {
    doc: Doc,
    obj: Map<String, Value>,
}

/// Check if an `input` element field has changed
pub trait Input<T> {
    /// Check an `input` element
    fn changed_input(&mut self, id: &str, val: T);
}

/// Check if a `textarea` element field has changed
pub trait TextArea<T> {
    /// Check a `textarea` element
    fn changed_text_area(&mut self, id: &str, val: T);
}

/// Check if a `select` element field has changed
pub trait Select<T> {
    /// Check a `select` element
    fn changed_select(&mut self, id: &str, val: T);
}

impl Fields {
    /// Create a new fields mapping
    pub fn new() -> Self {
        let doc = Doc::get();
        let obj = Map::default();
        Fields { doc, obj }
    }

    /// Convert fields into a JSON value
    pub fn into_value(self) -> Value {
        Value::Object(self.obj)
    }
}

impl Input<&String> for Fields {
    fn changed_input(&mut self, id: &str, val: &String) {
        if let Some(parsed) = self.doc.input_parse::<String>(id) {
            if &parsed != val {
                self.obj.insert(id.to_string(), Value::String(parsed));
            }
        }
    }
}

impl Input<&Option<String>> for Fields {
    fn changed_input(&mut self, id: &str, val: &Option<String>) {
        let parsed = self.doc.input_option_string(id);
        if parsed.as_deref() != val.as_deref() {
            self.obj.insert(id.to_string(), OptVal(parsed).into());
        }
    }
}

impl Input<u16> for Fields {
    fn changed_input(&mut self, id: &str, val: u16) {
        if let Some(parsed) = self.doc.input_parse::<u16>(id) {
            if parsed != val {
                self.obj
                    .insert(id.to_string(), Value::Number(parsed.into()));
            }
        }
    }
}

impl Input<bool> for Fields {
    fn changed_input(&mut self, id: &str, val: bool) {
        let parsed = self.doc.input_bool(id);
        if parsed != val {
            self.obj.insert(id.to_string(), Value::Bool(parsed));
        }
    }
}

impl Input<Option<bool>> for Fields {
    fn changed_input(&mut self, id: &str, val: Option<bool>) {
        let parsed = Some(self.doc.input_bool(id));
        if parsed != val {
            self.obj.insert(id.to_string(), OptVal(parsed).into());
        }
    }
}

impl Input<Option<u32>> for Fields {
    fn changed_input(&mut self, id: &str, val: Option<u32>) {
        let parsed = self.doc.input_parse::<u32>(id);
        if parsed != val {
            self.obj.insert(id.to_string(), OptVal(parsed).into());
        }
    }
}

impl Input<Option<f64>> for Fields {
    fn changed_input(&mut self, id: &str, val: Option<f64>) {
        let parsed = self.doc.input_parse::<f64>(id);
        if parsed != val {
            self.obj.insert(id.to_string(), OptVal(parsed).into());
        }
    }
}

impl TextArea<&String> for Fields {
    fn changed_text_area(&mut self, id: &str, val: &String) {
        if let Some(parsed) = self.doc.text_area_parse::<String>(id) {
            if &parsed != val {
                self.obj.insert(id.to_string(), Value::String(parsed));
            }
        }
    }
}

impl Select<&String> for Fields {
    fn changed_select(&mut self, id: &str, val: &String) {
        if let Some(parsed) = self.doc.select_parse::<String>(id) {
            if &parsed != val {
                self.obj.insert(id.to_string(), Value::String(parsed));
            }
        }
    }
}

impl Select<&Option<String>> for Fields {
    fn changed_select(&mut self, id: &str, val: &Option<String>) {
        let parsed = self.doc.select_parse::<String>(id);
        if &parsed != val {
            self.obj.insert(id.to_string(), OptVal(parsed).into());
        }
    }
}

impl Select<u16> for Fields {
    fn changed_select(&mut self, id: &str, val: u16) {
        if let Some(parsed) = self.doc.select_parse::<u16>(id) {
            if parsed != val {
                self.obj
                    .insert(id.to_string(), Value::Number(parsed.into()));
            }
        }
    }
}

impl Select<u32> for Fields {
    fn changed_select(&mut self, id: &str, val: u32) {
        if let Some(parsed) = self.doc.select_parse::<u32>(id) {
            if parsed != val {
                self.obj
                    .insert(id.to_string(), Value::Number(parsed.into()));
            }
        }
    }
}

impl Select<Option<u32>> for Fields {
    fn changed_select(&mut self, id: &str, val: Option<u32>) {
        let parsed = self.doc.select_parse::<u32>(id);
        if parsed != val {
            self.obj.insert(id.to_string(), OptVal(parsed).into());
        }
    }
}

#[cfg(test)]
mod test {
    use super::*;

    #[test]
    fn html() {
        assert_eq!(HtmlStr::new("<").to_string(), "&lt;");
        assert_eq!(HtmlStr::new(">").to_string(), "&gt;");
        assert_eq!(HtmlStr::new("&").to_string(), "&amp;");
        assert_eq!(HtmlStr::new("\"").to_string(), "&quot;");
        assert_eq!(HtmlStr::new("'").to_string(), "&#x27;");
        assert_eq!(
            HtmlStr::new("<script>XSS stuff</script>").to_string(),
            "&lt;script&gt;XSS stuff&lt;/script&gt;"
        );
        assert_eq!(HtmlStr::new("len").with_len(2).to_string(), "le");
    }
}
