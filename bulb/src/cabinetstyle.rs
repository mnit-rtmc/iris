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
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal};
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

impl CabinetStyle {
    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let name = HtmlStr::new(self.name());
        format!("<div>{name}</div>")
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self) -> String {
        let title = self.title(View::Setup).build();
        let police_panel_pin_1 = OptVal(self.police_panel_pin_1);
        let police_panel_pin_2 = OptVal(self.police_panel_pin_2);
        let watchdog_reset_pin_1 = OptVal(self.watchdog_reset_pin_1);
        let watchdog_reset_pin_2 = OptVal(self.watchdog_reset_pin_2);
        let dip = OptVal(self.dip);
        let footer = self.footer(true);
        format!(
            "{title}\
            <div class='row'>\
              <label for='police_panel_pin_1'>Police Panel Pin 1</label>\
              <input id='police_panel_pin_1' type='number' min='1' max='104' \
                     size='8' value='{police_panel_pin_1}'>\
            </div>\
            <div class='row'>\
              <label for='police_panel_pin_2'>Police Panel Pin 2</label>\
              <input id='police_panel_pin_2' type='number' min='1' max='104' \
                     size='8' value='{police_panel_pin_2}'>\
            </div>\
            <div class='row'>\
              <label for='watchdog_reset_pin_1'>Watchdog Reset Pin 1</label>\
              <input id='watchdog_reset_pin_1' type='number' min='1' max='104' \
                     size='8' value='{watchdog_reset_pin_1}'>\
            </div>\
            <div class='row'>\
              <label for='watchdog_reset_pin_2'>Watchdog Reset Pin 2</label>\
              <input id='watchdog_reset_pin_2' type='number' min='1' max='104' \
                     size='8' value='{watchdog_reset_pin_2}'>\
            </div>\
            <div class='row'>\
              <label for='dip'>Dip</label>\
              <input id='dip' type='number' min='0' max='255' \
                     size='8' value='{dip}'>\
            </div>\
            {footer}"
        )
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
    fn name(&self) -> Cow<str> {
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
