// Copyright (C) 2026  Minnesota Department of Transportation
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
use crate::util;
use http::uri::{InvalidUri, Uri};
use percent_encoding::{
    NON_ALPHANUMERIC, percent_decode_str, utf8_percent_encode,
};
use resources::Res;
use std::fmt;

/// Query state (resource, search, selection)
#[derive(Clone, Debug, Default, PartialEq, Eq)]
pub struct QueryState {
    /// Resource type
    res: Option<Res>,
    /// Selected item (not percent-encoded)
    sel: String,
}

impl std::str::FromStr for QueryState {
    type Err = InvalidUri;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let uri = s.parse::<Uri>()?;
        let mut res = None;
        let mut sel = String::new();
        if let Some(query) = uri.query() {
            for part in query.split('&') {
                if let Some(("res", val)) = part.split_once('=')
                    && let Ok(rs) = Res::try_from(val)
                {
                    res = Some(rs);
                }
                if let Some(("sel", val)) = part.split_once('=') {
                    sel =
                        percent_decode_str(val).decode_utf8_lossy().to_string();
                }
            }
        }
        Ok(QueryState { res, sel })
    }
}

impl fmt::Display for QueryState {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        if self.res.is_some() || !self.sel.is_empty() {
            let mut first = true;
            write!(f, "?")?;
            if let Some(res) = self.res {
                write!(f, "res={res}")?;
                first = false;
            }
            if !self.sel.is_empty() {
                if first {
                    write!(f, "?")?;
                } else {
                    write!(f, "&")?;
                }
                let sel = utf8_percent_encode(&self.sel, NON_ALPHANUMERIC);
                write!(f, "sel={sel}")?;
            }
        }
        fmt::Result::Ok(())
    }
}

impl QueryState {
    /// Create a new query state
    pub fn new() -> Self {
        Self::default()
    }

    /// Get query state from Navigation API current entry
    pub fn current_entry() -> Self {
        if let Ok(window) = util::window() {
            let navigation = window.navigation();
            if let Some(ent) = navigation.current_entry()
                && let Some(url) = ent.url()
                && let Ok(qs) = url.parse::<QueryState>()
            {
                return qs;
            }
        }
        Self::default()
    }

    /// Set the resource type
    pub fn with_res(mut self, res: Option<Res>) -> Self {
        self.res = res;
        self
    }

    /// Set the selected item
    pub fn with_sel(mut self, sel: &str) -> Self {
        self.sel = sel.to_string();
        self
    }

    /// Get resource type
    pub fn res(&self) -> Option<Res> {
        self.res
    }

    /// Get selected item
    pub fn sel(&self) -> &str {
        &self.sel
    }

    /// Get resource and selected item
    pub fn res_sel(&self) -> Option<(Res, &str)> {
        self.res.map(|res| (res, self.sel.as_str()))
    }
}
