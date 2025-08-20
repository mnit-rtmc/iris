// Copyright (C) 2022-2025  Minnesota Department of Transportation
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
use crate::item::{ItemState, ItemStates};
use crate::util::{ContainsLower, Fields, Input};
use hatmil::Html;
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;

/// Alarm
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Alarm {
    pub name: String,
    pub description: String,
    pub controller: Option<String>,
    pub state: bool,
    // secondary attributes
    pub pin: Option<u32>,
    pub trigger_time: Option<String>,
}

type AlarmAnc = ControllerIoAnc<Alarm>;

impl Alarm {
    /// Get the item states
    fn item_states<'a>(&'a self, anc: &'a AlarmAnc) -> ItemStates<'a> {
        let states = anc.item_states(self);
        if states.contains(ItemState::Available) && self.state {
            ItemState::Fault.into()
        } else {
            states
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &AlarmAnc) -> String {
        let mut html = Html::new();
        html.div()
            .class("title row")
            .text(self.name())
            .text(" ")
            .text(self.item_states(anc).to_string())
            .end();
        html.div().class("info fill").text(&self.description);
        html.to_string()
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &AlarmAnc) -> String {
        let mut html = self.title(View::Status);
        html.div().class("row");
        self.item_states(anc).tooltips(&mut html);
        html.end(); /* div */
        html.div().class("row");
        html.span().class("info full").text(&self.description).end();
        html.end(); /* div */
        html.div().class("row");
        html.span().text("Triggered").end();
        html.span()
            .class("info")
            .text(self.trigger_time.as_deref().unwrap_or("-"));
        html.to_string()
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &AlarmAnc) -> String {
        let mut html = self.title(View::Setup);
        html.div().class("row");
        html.label().r#for("description").text("Description").end();
        html.input()
            .id("description")
            .maxlength("24")
            .size("24")
            .value(&self.description);
        html.end(); /* div */
        anc.controller_html(self, &mut html);
        anc.pin_html(self.pin, &mut html);
        self.footer_html(true, &mut html);
        html.to_string()
    }
}

impl ControllerIo for Alarm {
    /// Get controller name
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl Card for Alarm {
    type Ancillary = AlarmAnc;

    /// Display name
    const DNAME: &'static str = "📢 Alarm";

    /// Get the resource
    fn res() -> Res {
        Res::Alarm
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
    fn is_match(&self, search: &str, anc: &AlarmAnc) -> bool {
        self.description.contains_lower(search)
            || self.name.contains_lower(search)
            || self.item_states(anc).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &AlarmAnc) -> String {
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
        fields.changed_input("description", &self.description);
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }
}
