// Copyright (C) 2022-2026  Minnesota Department of Transportation
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
use wasm_bindgen::JsValue;

/// Mapping of attributes for an object
pub struct Attr {
    obj: Map<String, Value>,
}

impl From<Attr> for String {
    fn from(attr: Attr) -> String {
        attr.into_value().to_string()
    }
}

impl From<Attr> for JsValue {
    fn from(attr: Attr) -> JsValue {
        JsValue::from(attr.into_value().to_string())
    }
}

impl Attr {
    /// Create a new attribute mapping
    pub fn new() -> Self {
        Attr {
            obj: Map::default(),
        }
    }

    /// Insert an attribute
    pub fn insert(&mut self, id: &str, val: Value) {
        self.obj.insert(id.to_string(), val);
    }

    /// Insert a string attribute
    pub fn str(&mut self, id: &str, val: &str) {
        self.insert(id, Value::String(val.into()));
    }

    /// Insert a string attribute, if changed
    pub fn str2(&mut self, id: &str, prev: &str, val: &str) {
        if prev != val {
            self.insert(id, Value::String(val.into()));
        }
    }

    /// Insert an optional string attribute
    pub fn opt_str(&mut self, id: &str, val: Option<&str>) {
        match val {
            Some(val) => self.str(id, val),
            None => self.insert(id, Value::Null),
        }
    }

    /// Insert an optional string attribute, if changed
    pub fn opt_str2(
        &mut self,
        id: &str,
        prev: Option<&str>,
        val: Option<&str>,
    ) {
        if prev != val {
            match val {
                Some(val) => self.str(id, val),
                None => self.insert(id, Value::Null),
            }
        }
    }

    /// Insert a number attribute
    pub fn num<T>(&mut self, id: &str, val: T)
    where
        T: Into<Number>,
    {
        self.insert(id, Value::Number(val.into()));
    }

    /// Insert a number attribute, if changed
    pub fn num2<T>(&mut self, id: &str, prev: T, val: T)
    where
        T: Into<Number> + PartialEq,
    {
        if prev != val {
            self.insert(id, Value::Number(val.into()));
        }
    }

    /// Insert an optional number attribute
    pub fn opt_num<T>(&mut self, id: &str, val: Option<T>)
    where
        T: Into<Number>,
    {
        match val {
            Some(val) => self.num(id, val),
            None => self.insert(id, Value::Null),
        }
    }

    /// Insert an optional number attribute, if changed
    pub fn opt_num2<T>(&mut self, id: &str, prev: Option<T>, val: Option<T>)
    where
        T: Into<Number> + PartialEq,
    {
        if prev != val {
            match val {
                Some(val) => self.num(id, val),
                None => self.insert(id, Value::Null),
            }
        }
    }

    /// Insert a bool attribute
    pub fn bool(&mut self, id: &str, val: bool) {
        self.insert(id, Value::Bool(val));
    }

    /// Insert a bool attribute, if changed
    pub fn bool2(&mut self, id: &str, prev: bool, val: bool) {
        if prev != val {
            self.insert(id, Value::Bool(val));
        }
    }

    /// Insert an array attribute
    pub fn array<T>(&mut self, id: &str, val: Vec<T>)
    where
        T: Into<Value>,
    {
        self.insert(id, val.into());
    }

    /// Check if empty
    pub fn is_empty(&self) -> bool {
        self.obj.is_empty()
    }

    /// Convert attributes into a JSON value
    pub fn into_value(self) -> Value {
        Value::Object(self.obj)
    }
}
