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
use crate::card::Card;
use crate::util::{HtmlStr, OptVal};
use serde::{Deserialize, Serialize};

/// Cabinet Style
#[derive(Debug, Deserialize, Serialize)]
pub struct CabinetStyle {
    pub name: String,
    pub police_panel_pin_1: Option<u32>,
    pub police_panel_pin_2: Option<u32>,
    pub watchdog_reset_pin_1: Option<u32>,
    pub watchdog_reset_pin_2: Option<u32>,
    pub dip: Option<u32>,
}

impl Card for CabinetStyle {
    const TNAME: &'static str = "Cabinet Style";
    const ENAME: &'static str = "🗄️ Cabinet Style";
    const URI: &'static str = "/iris/api/cabinet_style";

    fn is_match(&self, tx: &str) -> bool {
        self.name.to_lowercase().contains(tx)
    }

    fn name(&self) -> &str {
        &self.name
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let name = HtmlStr(&self.name);
        format!("<span>{name}</span>")
    }

    /// Convert to edit HTML
    fn to_html_edit(&self) -> String {
        let police_panel_pin_1 = OptVal(self.police_panel_pin_1);
        let police_panel_pin_2 = OptVal(self.police_panel_pin_2);
        let watchdog_reset_pin_1 = OptVal(self.watchdog_reset_pin_1);
        let watchdog_reset_pin_2 = OptVal(self.watchdog_reset_pin_2);
        let dip = OptVal(self.dip);
        format!(
            "<div class='row'>\
              <label for='form_pp1'>Police Panel Pin 1</label>\
              <input id='form_pp1' type='number' min='1' max='104' \
                     size='8' value='{police_panel_pin_1}'/>\
            </div>\
            <div class='row'>\
              <label for='form_pp2'>Police Panel Pin 2</label>\
              <input id='form_pp2' type='number' min='1' max='104' \
                     size='8' value='{police_panel_pin_2}'/>\
            </div>\
            <div class='row'>\
              <label for='form_wr1'>Watchdog Reset Pin 1</label>\
              <input id='form_wr1' type='number' min='1' max='104' \
                     size='8' value='{watchdog_reset_pin_1}'/>\
            </div>\
            <div class='row'>\
              <label for='form_wr2'>Watchdog Reset Pin 2</label>\
              <input id='form_wr2' type='number' min='1' max='104' \
                     size='8' value='{watchdog_reset_pin_2}'/>\
            </div>\
            <div class='row'>\
              <label for='form_dip'>Dip</label>\
              <input id='form_dip' type='number' min='0' max='255' \
                     size='8' value='{dip}'/>\
            </div>"
        )
    }
}
