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
use crate::{disabled_attr, Card, CardType, HtmlStr, NAME};
use serde::{Deserialize, Serialize};

/// Controller
#[derive(Debug, Deserialize, Serialize)]
pub struct Controller {
    pub name: String,
    pub drop_id: u16,
    pub comm_link: String,
    pub cabinet_style: Option<String>,
    pub geo_loc: String,
    pub condition: u32,
    pub notes: String,
    pub password: Option<String>,
    pub fail_time: Option<String>,
    pub version: Option<String>,
}

impl Controller {
    fn to_compact_html(&self) -> String {
        let comm_link = HtmlStr(&self.comm_link);
        let drop_id = self.drop_id;
        let name = HtmlStr(&self.name);
        // condition 1 is "Active"
        let disabled = disabled_attr(self.condition == 1);
        format!(
            "<span{disabled}>{comm_link}:{drop_id}</span>\
            <span class='{NAME}'>{name}</span>"
        )
    }

    fn to_status_html(&self) -> String {
        let comm_link = HtmlStr(&self.comm_link);
        let drop_id = self.drop_id;
        let version = self.version.as_deref().unwrap_or("-");
        let fail_time = self.fail_time.as_deref().unwrap_or("-");
        format!(
            "<div class='row'>\
              <span>Comm Link:Drop ID</span>\
              <span class='info'>{comm_link}:{drop_id}</span>\
            </div>\
            <div class='row'>\
              <span>Version</span>\
              <span class='info'>{version}</span>\
            </div>\
            <div class='row'>\
              <span>Fail Time</span>\
              <span class='info'>{fail_time}</span>\
            </div>"
        )
    }

    fn to_edit_html(&self) -> String {
        let comm_link = HtmlStr(&self.comm_link);
        let drop_id = self.drop_id;
        let cabinet_style = HtmlStr(self.cabinet_style.as_ref());
        let geo_loc = HtmlStr(&self.geo_loc);
        let notes = HtmlStr(&self.notes);
        let password = HtmlStr(self.password.as_ref());
        format!(
            "<div class='row'>\
              <label for='form_comm_link'>Comm Link</label>\
              <input id='form_comm_link' maxlength='20' size='20' \
                     value='{comm_link}'/>\
            </div>\
            <div class='row'>\
              <label for='form_drop_id'>Drop ID</label>\
              <input id='form_drop_id' type='number' min='0'
                     max='65535' size='6' value='{drop_id}'/>\
            </div>\
            <div class='row'>\
              <label for='form_cabinet'>Cabinet Style</label>\
              <input id='form_cabinet' maxlength='20' size='20' \
                     value='{cabinet_style}'/>\
            </div>\
            <div class='row'>\
              <label for='form_geo_loc'>Geo Loc</label>\
              <input id='form_geo_loc' maxlength='20' size='20' \
                     value='{geo_loc}'/>\
            </div>\
            <div class='row'>\
              <label for='form_notes'>Notes</label>\
              <textarea id='form_notes' maxlength='128' rows='2' \
                        cols='26'/>{notes}</textarea>\
            </div>\
            <div class='row'>\
              <label for='form_password'>Password</label>\
              <input id='form_password' maxlength='32' size='26' \
                     value='{password}'/>\
            </div>"
        )
    }
}

impl Card for Controller {
    const ENAME: &'static str = "🎛️ Controller";

    fn is_match(&self, tx: &str) -> bool {
        self.name.contains(tx)
            || {
                let comm_link = self.comm_link.to_lowercase();
                comm_link.contains(tx)
                    || format!("{}:{}", comm_link, self.drop_id).contains(tx)
            }
            || self.notes.to_lowercase().contains(tx)
            || self
                .cabinet_style
                .as_deref()
                .unwrap_or("")
                .to_lowercase()
                .contains(tx)
            || self
                .version
                .as_deref()
                .unwrap_or("")
                .to_lowercase()
                .contains(tx)
    }

    fn name(&self) -> &str {
        &self.name
    }

    fn to_html(&self, ct: CardType) -> String {
        match ct {
            CardType::Compact => self.to_compact_html(),
            CardType::Status => self.to_status_html(),
            CardType::Edit => self.to_edit_html(),
            _ => unreachable!(),
        }
    }
}
