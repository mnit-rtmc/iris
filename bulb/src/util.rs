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
use crate::start::JsResult;
use serde_json::{Number, Value};
use std::fmt;
use std::str::FromStr;
use wasm_bindgen::JsCast;
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

impl fmt::Display for HtmlStr<Option<&String>> {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match &self.val {
            Some(val) => self.fmt_encode(val, f),
            None => Ok(()),
        }
    }
}

/// Helper trait for DOM methods
pub trait Dom {
    /// Get an element by ID and cast it
    fn elem<E: JsCast>(&self, id: &str) -> JsResult<E>;

    /// Get and parse a `select` element value
    fn select_parse<T: FromStr>(&self, id: &str) -> Option<T>;

    /// Get and parse an `input` element value
    fn input_parse<T: FromStr>(&self, id: &str) -> Option<T>;

    /// Get a boolean `input` element value
    fn input_bool(&self, id: &str) -> Option<bool>;

    /// Get and parse a `textarea` element value
    fn text_area_parse<T: FromStr>(&self, id: &str) -> Option<T>;
}

impl Dom for Document {
    fn elem<E: JsCast>(&self, id: &str) -> JsResult<E> {
        Ok(self
            .get_element_by_id(id)
            .ok_or("Invalid element ID")?
            .dyn_into::<E>()?)
    }

    fn select_parse<T: FromStr>(&self, id: &str) -> Option<T> {
        self.elem::<HtmlSelectElement>(id)
            .unwrap()
            .value()
            .parse()
            .ok()
    }

    fn input_parse<T: FromStr>(&self, id: &str) -> Option<T> {
        self.elem::<HtmlInputElement>(id)
            .unwrap()
            .value()
            .parse()
            .ok()
    }

    fn input_bool(&self, id: &str) -> Option<bool> {
        Some(self.elem::<HtmlInputElement>(id).unwrap().checked())
    }

    fn text_area_parse<T: FromStr>(&self, id: &str) -> Option<T> {
        self.elem::<HtmlTextAreaElement>(id)
            .unwrap()
            .value()
            .parse()
            .ok()
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
