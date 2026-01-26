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
use crate::card::{Card, View};
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::util::{ContainsLower, Fields, Input};
use hatmil::{Page, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;

/// Video Monitor
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct VideoMonitor {
    pub name: String,
    pub mon_num: u32,
    pub controller: Option<String>,
    // secondary attributes
    pub pin: Option<u32>,
}

type VideoMonitorAnc = ControllerIoAnc<VideoMonitor>;

impl VideoMonitor {
    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &VideoMonitorAnc) -> String {
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(anc.item_states(self).to_string())
            .close();
        div = page.frag::<html::Div>();
        div.class("info fill").cdata(self.mon_num);
        String::from(page)
    }

    /// Convert to Status HTML
    fn to_html_status(&self) -> String {
        let mut page = Page::new();
        self.title(View::Status, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.span().class("info").cdata(self.mon_num);
        String::from(page)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &VideoMonitorAnc) -> String {
        let mut page = Page::new();
        self.title(View::Setup, &mut page.frag::<html::Div>());
        anc.controller_html(self, &mut page.frag::<html::Div>());
        anc.pin_html(self.pin, &mut page.frag::<html::Div>());
        self.footer_html(true, &mut page.frag::<html::Div>());
        String::from(page)
    }
}

impl ControllerIo for VideoMonitor {
    /// Get controller name
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Card for VideoMonitor {
    type Ancillary = VideoMonitorAnc;

    /// Display name
    const DNAME: &'static str = "📺 Video Monitor";

    /// Get the resource
    fn res() -> Res {
        Res::VideoMonitor
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
    fn is_match(&self, search: &str, anc: &VideoMonitorAnc) -> bool {
        self.name.contains_lower(search)
            || self.mon_num.to_string().contains(search)
            || anc.item_states(self).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &VideoMonitorAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Status => self.to_html_status(),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }
}
