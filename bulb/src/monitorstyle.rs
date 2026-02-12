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
use crate::card::{AncillaryData, Card, View};
use crate::util::{ContainsLower, Fields, Input, opt_ref, opt_str};
use hatmil::{Page, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;

/// Monitor Style
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct MonitorStyle {
    pub name: String,
    // secondary attributes
    pub force_aspect: Option<bool>,
    pub accent: Option<String>,
    pub font_sz: Option<u32>,
    pub title_bar: Option<bool>,
    pub auto_expand: Option<bool>,
    pub hgap: Option<u32>,
    pub vgap: Option<u32>,
}

/// Ancillary Monitor Style data
#[derive(Debug, Default)]
pub struct MonitorStyleAnc;

impl AncillaryData for MonitorStyleAnc {
    type Primary = MonitorStyle;

    /// Construct ancillary monitor style data
    fn new(_pri: &MonitorStyle, _view: View) -> Self {
        MonitorStyleAnc
    }
}

impl MonitorStyle {
    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.cdata(self.name());
        String::from(page)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self) -> String {
        let mut page = Page::new();
        self.title(View::Setup, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("force_aspect")
            .cdata("Force Aspect")
            .close();
        let mut input = div.input();
        input.id("force_aspect").r#type("checkbox");
        if self.force_aspect == Some(true) {
            input.checked();
        }
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("accent").cdata("Accent").close();
        let mut input = div.input();
        input
            .id("accent")
            .maxlength(6)
            .size(6)
            .value(opt_ref(&self.accent));
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("font_sz").cdata("Font Size").close();
        div.input()
            .id("font_sz")
            .r#type("number")
            .min(1)
            .max(200)
            .size(4)
            .value(opt_str(self.font_sz));
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("title_bar").cdata("Title Bar").close();
        let mut input = div.input();
        input.id("title_bar").r#type("checkbox");
        if self.title_bar == Some(true) {
            input.checked();
        }
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("auto_expand")
            .cdata("Auto Expand")
            .close();
        let mut input = div.input();
        input.id("auto_expand").r#type("checkbox");
        if self.auto_expand == Some(true) {
            input.checked();
        }
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("hgap").cdata("Horizontal Gap").close();
        div.input()
            .id("hgap")
            .r#type("number")
            .min(0)
            .max(100)
            .size(3)
            .value(opt_str(self.hgap));
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("vgap").cdata("Vertical Gap").close();
        div.input()
            .id("vgap")
            .r#type("number")
            .min(0)
            .max(100)
            .size(3)
            .value(opt_str(self.vgap));
        div.close();
        self.footer_html(true, &mut page.frag::<html::Div>());
        String::from(page)
    }
}

impl Card for MonitorStyle {
    type Ancillary = MonitorStyleAnc;

    /// Display name
    const DNAME: &'static str = "🖵 Monitor Style";

    /// Get the resource
    fn res() -> Res {
        Res::MonitorStyle
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
    fn is_match(&self, search: &str, _anc: &MonitorStyleAnc) -> bool {
        self.name.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &MonitorStyleAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Setup => self.to_html_setup(),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("force_aspect", self.force_aspect);
        fields.changed_input("accent", &self.accent);
        fields.changed_input("font_sz", self.font_sz);
        fields.changed_input("title_bar", self.title_bar);
        fields.changed_input("auto_expand", self.auto_expand);
        fields.changed_input("hgap", self.hgap);
        fields.changed_input("vgap", self.vgap);
        fields.into_value().to_string()
    }
}
