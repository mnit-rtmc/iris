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
use crate::commconfig::CommConfig;
use crate::commlink::CommLink;
use crate::error::Result;
use crate::util::{Dom, HtmlStr, OptVal};
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::fmt;
use wasm_bindgen::JsValue;
use web_sys::Document;

/// Controller conditions
#[derive(Debug, Deserialize, Serialize)]
pub struct Condition {
    pub id: u32,
    pub description: String,
}

/// Controller
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct Controller {
    pub name: String,
    pub location: Option<String>,
    pub comm_link: String,
    pub drop_id: u16,
    pub cabinet_style: Option<String>,
    pub condition: u32,
    pub notes: String,
    pub version: Option<String>,
    pub fail_time: Option<String>,
    pub geo_loc: Option<String>,
    pub password: Option<String>,

    /// Ancillary data
    pub conditions: Option<Vec<Condition>>,
    pub comm_links: Option<Vec<CommLink>>,
    pub comm_configs: Option<Vec<CommConfig>>,
}

impl fmt::Display for Controller {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl Controller {
    /// Get comm state
    fn comm_state(&self, long: bool) -> &'static str {
        let active = self.condition == 1;
        let failed = self.fail_time.is_some();
        match (active, failed, long) {
            (true, false, false) => "👍",
            (true, false, true) => "ok 👍",
            (true, true, false) => "☠️",
            (true, true, true) => "failed ☠️",
            (false, _, false) => "❓",
            (false, _, true) => "inactive ❓",
        }
    }

    /// Get condition description
    fn condition(&self) -> &str {
        if let Some(conditions) = &self.conditions {
            for condition in conditions {
                if self.condition == condition.id {
                    return &condition.description;
                }
            }
        }
        ""
    }

    /// Create an HTML `select` element of controller conditions
    fn conditions_html(&self) -> String {
        let mut html = String::new();
        html.push_str("<select id='edit_condition'>");
        if let Some(conditions) = &self.conditions {
            for condition in conditions {
                html.push_str("<option value='");
                html.push_str(&condition.id.to_string());
                html.push('\'');
                if self.condition == condition.id {
                    html.push_str(" selected");
                }
                html.push('>');
                html.push_str(&condition.description);
                html.push_str("</option>");
            }
        }
        html.push_str("</select>");
        html
    }

    /// Get the comm config
    fn comm_config(&self) -> &str {
        if let (Some(comm_links), Some(comm_configs)) =
            (&self.comm_links, &self.comm_configs)
        {
            if let Some(comm_link) =
                comm_links.iter().find(|cl| cl.name == self.comm_link)
            {
                if let Some(comm_config) = comm_configs
                    .iter()
                    .find(|cc| cc.name == comm_link.comm_config)
                {
                    return &comm_config.description[..];
                }
            }
        }
        ""
    }
}

impl Card for Controller {
    const TNAME: &'static str = "Controller";
    const ENAME: &'static str = "🎛️ Controller";
    const UNAME: &'static str = "controller";
    const HAS_STATUS: bool = true;

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Get geo location of card
    fn geo_loc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str) -> bool {
        self.name.contains(search)
            || {
                let comm_link = self.comm_link.to_lowercase();
                format!("{comm_link}:{}", self.drop_id).contains(search)
            }
            || self.comm_state(true).contains(search)
            || self.notes.to_lowercase().contains(search)
            || self
                .location
                .as_deref()
                .unwrap_or("")
                .to_lowercase()
                .contains(search)
            || self.condition().to_lowercase().contains(search)
            || self
                .cabinet_style
                .as_deref()
                .unwrap_or("")
                .to_lowercase()
                .contains(search)
            || self
                .version
                .as_deref()
                .unwrap_or("")
                .to_lowercase()
                .contains(search)
    }

    /// Get ancillary URI
    fn ancillary_uri(&self) -> Option<&str> {
        match (&self.conditions, &self.comm_links, &self.comm_configs) {
            (None, _, _) => Some("/iris/condition"),
            (_, None, _) => Some("/iris/api/comm_link"),
            (_, _, None) => Some("/iris/api/comm_config"),
            _ => None,
        }
    }

    /// Put ancillary JSON data
    fn ancillary_json(&mut self, json: JsValue) -> Result<()> {
        match (&self.conditions, &self.comm_links) {
            (None, _) => {
                let conditions = json.into_serde::<Vec<Condition>>()?;
                self.conditions = Some(conditions);
            }
            (_, None) => {
                let comm_links = json.into_serde::<Vec<CommLink>>()?;
                self.comm_links = Some(comm_links);
            }
            _ => {
                let comm_configs = json.into_serde::<Vec<CommConfig>>()?;
                self.comm_configs = Some(comm_configs);
            }
        }
        Ok(())
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
        let comm_state = self.comm_state(false);
        let comm_link = HtmlStr::new(&self.comm_link);
        let drop_id = self.drop_id;
        // condition 1 is "Active"
        let disabled = disabled_attr(self.condition == 1);
        format!(
            "<span>{comm_state}</span>\
            <span{disabled}>{comm_link}:{drop_id}</span>\
            <span class='{NAME}'>{self}</span>"
        )
    }

    /// Convert to status HTML
    fn to_html_status(&self) -> String {
        let tname = CommLink::TNAME;
        let condition = self.condition();
        let comm_state = self.comm_state(true);
        let comm_link = HtmlStr::new(&self.comm_link);
        let comm_config = self.comm_config();
        let drop_id = self.drop_id;
        let location = HtmlStr::new(self.location.as_ref()).with_len(64);
        let notes = HtmlStr::new(&self.notes);
        let version = match &self.version {
            Some(version) => {
                let version = HtmlStr::new(version).with_len(32);
                format!(
                    "<span>\
                      <span>Version</span>\
                      <span class='info'>{version}</span>\
                    </span>\
                    <span></span>"
                )
            }
            None => "".to_string(),
        };
        let fail_time = match &self.fail_time {
            Some(fail_time) => {
                format!(
                    "<span>Fail Time</span>\
                    <span class='info'>{fail_time}</span>"
                )
            }
            None => "".to_string(),
        };
        format!(
            "<div class='row'>\
              <span>{condition}</span>\
              <span>{comm_state}</span>\
              <span>{comm_link}:{drop_id} \
                <button class='go_link' type='button' \
                        data-link='{comm_link}' data-type='{tname}'>🖇️</button>\
              </span>\
            </div>\
            <div class='row'>\
              <span></span>\
              <span class='info'>{comm_config}</span>\
            </div>\
            <div class='row'>\
              <span>{location}</span>\
              <span class='info'>{notes}</span>\
            </div>\
            <div class='row'>{version}</div>\
            <div class='row'>{fail_time}</div>"
        )
    }

    /// Convert to edit HTML
    fn to_html_edit(&self) -> String {
        let comm_link = HtmlStr::new(&self.comm_link);
        let drop_id = self.drop_id;
        let cabinet_style = HtmlStr::new(self.cabinet_style.as_ref());
        let conditions = self.conditions_html();
        let notes = HtmlStr::new(&self.notes);
        let password = HtmlStr::new(self.password.as_ref());
        format!(
            "<div class='row'>\
              <label for='edit_link'>Comm Link</label>\
              <input id='edit_link' maxlength='20' size='20' \
                     value='{comm_link}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_drop'>Drop ID</label>\
              <input id='edit_drop' type='number' min='0'
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
        if let Some(comm_link) = doc.input_parse::<String>("edit_link") {
            if comm_link != val.comm_link {
                obj.insert("comm_link".to_string(), Value::String(comm_link));
            }
        }
        if let Some(drop_id) = doc.input_parse::<u16>("edit_drop") {
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
                OptVal(cabinet_style).into(),
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
            obj.insert("password".to_string(), OptVal(password).into());
        }
        Ok(Value::Object(obj).to_string())
    }
}
