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
use crate::card::{AncillaryData, Card, footer_html};
use crate::util::{ContainsLower, Fields, Input};
use crate::view::View;
use hatmil::{Tree, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use std::fmt;

/// Encoder Type
#[derive(Debug, Default, Deserialize, PartialEq, Eq, PartialOrd, Ord)]
pub struct EncoderType {
    pub make: String,
    pub model: String,
    pub config: String,
    // NOTE: last to allow deriving PartialOrd / Ord
    pub name: String,
}

/// Ancillary Encoder Type data
#[derive(Debug, Default)]
pub struct EncoderTypeAnc;

impl AncillaryData for EncoderTypeAnc {
    type Primary = EncoderType;

    /// Construct ancillary encoder type data
    fn new(_pri: &EncoderType, _view: View) -> Self {
        EncoderTypeAnc
    }
}

impl fmt::Display for EncoderType {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.make)?;
        if !self.model.is_empty() {
            write!(f, " {}", self.model)?;
        }
        if !self.config.is_empty() {
            write!(f, " {}", self.config)?;
        }
        Ok(())
    }
}

impl EncoderType {
    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.class("title row").cdata(self.name());
        div.span().class("info").cdata(format!("{self}"));
        String::from(tree)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, edit: bool) -> String {
        let mut tree = Tree::new();
        self.title(View::Setup(edit), &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("make").cdata("Make").close();
        div.input()
            .id("make")
            .maxlength(16)
            .size(16)
            .value(&self.make);
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("model").cdata("Model").close();
        div.input()
            .id("model")
            .maxlength(16)
            .size(16)
            .value(&self.model);
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("config").cdata("Config").close();
        div.input()
            .id("config")
            .maxlength(8)
            .size(8)
            .value(&self.config);
        div.close();
        footer_html(View::Setup(edit), true, &mut tree.root::<html::Div>());
        String::from(tree)
    }
}

impl Card for EncoderType {
    type Ancillary = EncoderTypeAnc;

    /// Get the resource
    fn res() -> Res {
        Res::EncoderType
    }

    /// Get the name
    fn name(&self) -> Cow<'_, str> {
        Cow::Borrowed(&self.name)
    }

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, _anc: &EncoderTypeAnc) -> bool {
        self.name.contains_lower(search)
            || self.make.contains_lower(search)
            || self.model.contains_lower(search)
            || self.config.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, _anc: &EncoderTypeAnc) -> String {
        match view {
            View::Create => self.to_html_create(8),
            View::Setup(edit) => self.to_html_setup(edit),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("make", &self.make);
        fields.changed_input("model", &self.model);
        fields.changed_input("config", &self.config);
        fields.into_value().to_string()
    }
}
