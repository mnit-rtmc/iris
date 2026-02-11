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
use crate::app;
use crate::card::{Card, View};
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::util::{ContainsLower, Doc, Fields, Input, opt_ref};
use hatmil::{Page, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use web_sys::HtmlElement;

/// Video Monitor
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct VideoMonitor {
    pub name: String,
    pub mon_num: u32,
    pub notes: Option<String>,
    pub controller: Option<String>,
    // secondary attributes
    pub pin: Option<u32>,
    pub restricted: Option<bool>,
}

type VideoMonitorAnc = ControllerIoAnc<VideoMonitor>;

impl VideoMonitor {
    /// Search for monitor number
    fn check_number(&self, search: &str) -> bool {
        let mon_num = self.mon_num.to_string();
        match search.strip_prefix('#') {
            Some(s) => mon_num.starts_with(s),
            None => mon_num.contains(search),
        }
    }

    /// Set this card as the selected video monitor
    fn set_selected(&self) {
        // FIXME: check role for monitor operate permission
        app::set_mon_num(Some(self.mon_num));
        let mon_num = match app::mon_num() {
            Some(num) => format!("📺 #{num} "),
            None => "📺".to_string(),
        };
        let t = Doc::get().elem::<HtmlElement>("sb_monitor");
        t.set_inner_html(&mon_num);
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &VideoMonitorAnc) -> String {
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(anc.item_states(self).to_string());
        div.span().class("info").cdata(format!("#{}", self.mon_num));
        String::from(page)
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &VideoMonitorAnc) -> String {
        self.set_selected();
        let mut page = Page::new();
        self.title(View::Status, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        anc.item_states(self).tooltips(&mut div.span());
        div.span().class("info").cdata(format!("#{}", self.mon_num));
        String::from(page)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &VideoMonitorAnc) -> String {
        let mut page = Page::new();
        self.title(View::Setup, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("mon_num").cdata("Mon Num").close();
        div.input()
            .id("mon_num")
            .r#type("number")
            .min(0)
            .max(9999)
            .size(8)
            .value(self.mon_num);
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("notes").cdata("Notes").close();
        div.textarea()
            .id("notes")
            .maxlength(255)
            .rows(4)
            .cols(24)
            .cdata(opt_ref(&self.notes))
            .close();
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label().r#for("restricted").cdata("Restricted").close();
        let mut input = div.input();
        input.id("restricted").r#type("checkbox");
        if self.restricted == Some(true) {
            input.checked();
        }
        div.close();
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
            || anc.item_states(self).is_match(search)
            || self.check_number(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &VideoMonitorAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Status => self.to_html_status(anc),
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
