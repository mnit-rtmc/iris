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
use crate::card::{AncillaryData, Card, View};
use crate::util::{ContainsLower, Fields, Input, opt_str};
use hatmil::Html;
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;

/// Cabinet Style
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct CabinetStyle {
    pub name: String,
    pub police_panel_pin_1: Option<u32>,
    pub police_panel_pin_2: Option<u32>,
    pub watchdog_reset_pin_1: Option<u32>,
    pub watchdog_reset_pin_2: Option<u32>,
    pub dip: Option<u32>,
}

/// Ancillary Cabinet Style data
#[derive(Debug, Default)]
pub struct CabinetStyleAnc;

impl AncillaryData for CabinetStyleAnc {
    type Primary = CabinetStyle;

    /// Construct ancillary cabinet style data
    fn new(_pri: &CabinetStyle, _view: View) -> Self {
        CabinetStyleAnc
    }
}

/// Build pin row HTML
fn pin_row_html(id: &str, name: &str, pin: Option<u32>, html: &mut Html) {
    html.div().class("row");
    html.label().for_(id).text(name).end();
    html.input()
        .id(id)
        .type_("number")
        .min("1")
        .max("104")
        .size("8")
        .value(opt_str(pin));
    html.end(); /* div */
}

impl CabinetStyle {
    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let mut html = Html::new();
        html.div().text(self.name());
        html.to_string()
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self) -> String {
        let mut html = self.title(View::Setup);
        pin_row_html(
            "police_panel_pin_1",
            "Police Panel Pin 1",
            self.police_panel_pin_1,
            &mut html,
        );
        pin_row_html(
            "police_panel_pin_2",
            "Police Panel Pin 2",
            self.police_panel_pin_2,
            &mut html,
        );
        pin_row_html(
            "watchdog_reset_pin_1",
            "Watchdog Reset Pin 1",
            self.watchdog_reset_pin_1,
            &mut html,
        );
        pin_row_html(
            "watchdog_reset_pin_2",
            "Watchdog Reset Pin 2",
            self.watchdog_reset_pin_2,
            &mut html,
        );
        html.div().class("row");
        html.label().for_("dip").text("Dip").end();
        html.input()
            .id("dip")
            .type_("number")
            .min("0")
            .max("255")
            .size("8")
            .value(opt_str(self.dip));
        html.end(); /* div */
        self.footer_html(true, &mut html);
        html.to_string()
    }
}

impl Card for CabinetStyle {
    type Ancillary = CabinetStyleAnc;

    /// Display name
    const DNAME: &'static str = "🗄️ Cabinet Style";

    /// Get the resource
    fn res() -> Res {
        Res::CabinetStyle
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
    fn is_match(&self, search: &str, _anc: &CabinetStyleAnc) -> bool {
        self.name.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &CabinetStyleAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Setup => self.to_html_setup(),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("police_panel_pin_1", self.police_panel_pin_1);
        fields.changed_input("police_panel_pin_2", self.police_panel_pin_2);
        fields.changed_input("watchdog_reset_pin_1", self.watchdog_reset_pin_1);
        fields.changed_input("watchdog_reset_pin_2", self.watchdog_reset_pin_2);
        fields.changed_input("dip", self.dip);
        fields.into_value().to_string()
    }
}
