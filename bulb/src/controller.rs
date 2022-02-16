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
use crate::card::{disabled_attr, Card, NAME};
use crate::util::{Dom, HtmlStr};
use crate::{conditions_html, Result};
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use wasm_bindgen::JsValue;
use web_sys::Document;

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

impl Card for Controller {
    const TNAME: &'static str = "Controller";
    const ENAME: &'static str = "🎛️ Controller";
    const URI: &'static str = "/iris/api/controller";
    const HAS_STATUS: bool = true;

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

    /// Get next suggested name
    fn next_name(obs: &[Self]) -> String {
        let mut num = 1;
        for ob in obs {
            if let Some(("ctl", suffix)) = ob.name.split_once('_') {
                if let Ok(n) = suffix.parse::<u32>() {
                    num = num.max(n + 1);
                }
            }
        }
        format!("ctl_{num}")
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
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

    /// Convert to status HTML
    fn to_html_status(&self) -> String {
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

    /// Convert to edit HTML
    fn to_html_edit(&self) -> String {
        let comm_link = HtmlStr(&self.comm_link);
        let drop_id = self.drop_id;
        let cabinet_style = HtmlStr(self.cabinet_style.as_ref());
        let conditions = conditions_html(self.condition);
        let geo_loc = HtmlStr(&self.geo_loc);
        let notes = HtmlStr(&self.notes);
        let password = HtmlStr(self.password.as_ref());
        format!(
            "<div class='row'>\
              <label for='edit_comm_link'>Comm Link</label>\
              <input id='edit_comm_link' maxlength='20' size='20' \
                     value='{comm_link}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_drop_id'>Drop ID</label>\
              <input id='edit_drop_id' type='number' min='0'
                     max='65535' size='6' value='{drop_id}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_cabinet'>Cabinet Style</label>\
              <input id='edit_cabinet' maxlength='20' size='20' \
                     value='{cabinet_style}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_condition'>Condition</label>\
              {conditions}\
            </div>\
            <div class='row'>\
              <label for='edit_geo_loc'>Geo Loc</label>\
              <input id='edit_geo_loc' maxlength='20' size='20' \
                     value='{geo_loc}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_notes'>Notes</label>\
              <textarea id='edit_notes' maxlength='128' rows='2' \
                        cols='26'/>{notes}</textarea>\
            </div>\
            <div class='row'>\
              <label for='edit_password'>Password</label>\
              <input id='edit_password' maxlength='32' size='26' \
                     value='{password}'/>\
            </div>"
        )
    }

    /// Get changed fields from Edit form
    fn changed_fields(doc: &Document, json: &JsValue) -> Result<String> {
        let val = Self::new(json)?;
        let mut obj = Map::new();
        if let Some(comm_link) = doc.input_parse::<String>("edit_comm_link") {
            if comm_link != val.comm_link {
                obj.insert("comm_link".to_string(), Value::String(comm_link));
            }
        }
        if let Some(drop_id) = doc.input_parse::<u16>("edit_drop_id") {
            if drop_id != val.drop_id {
                obj.insert(
                    "drop_id".to_string(),
                    Value::Number(drop_id.into()),
                );
            }
        }
        let cabinet_style = doc
            .input_parse::<String>("edit_cabinet")
            .filter(|c| !c.is_empty());
        if cabinet_style != val.cabinet_style {
            obj.insert(
                "cabinet_style".to_string(),
                match cabinet_style {
                    Some(cab) => Value::String(cab),
                    None => Value::Null,
                },
            );
        }
        if let Some(condition) = doc.select_parse::<u32>("edit_condition") {
            if condition != val.condition {
                obj.insert(
                    "condition".to_string(),
                    Value::Number(condition.into()),
                );
            }
        }
        if let Some(notes) = doc.text_area_parse::<String>("edit_notes") {
            if notes != val.notes {
                obj.insert("notes".to_string(), Value::String(notes));
            }
        }
        let password = doc
            .input_parse::<String>("edit_password")
            .filter(|p| !p.is_empty());
        if password != val.password {
            obj.insert(
                "password".to_string(),
                match password {
                    Some(password) => Value::String(password),
                    None => Value::Null,
                },
            );
        }
        Ok(Value::Object(obj).to_string())
    }
}
