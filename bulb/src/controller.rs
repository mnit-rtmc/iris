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
use crate::cabinetstyle::CabinetStyle;
use crate::commconfig::CommConfig;
use crate::commlink::CommLink;
use crate::error::Result;
use crate::resource::{
    disabled_attr, AncillaryData, Card, Resource, View, EDIT_BUTTON,
    LOC_BUTTON, NAME,
};
use crate::util::{ContainsLower, Dom, HtmlStr, OptVal};
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::borrow::{Borrow, Cow};
use std::fmt;
use wasm_bindgen::JsValue;
use web_sys::Document;

/// Controller conditions
#[derive(Debug, Deserialize, Serialize)]
pub struct Condition {
    pub id: u32,
    pub description: String,
}

/// Controller IO
#[derive(Debug, Deserialize, Serialize)]
pub struct ControllerIo {
    pub pin: u32,
    pub resource_n: String,
    pub name: String,
}

/// Controller
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct Controller {
    pub name: String,
    pub location: Option<String>,
    pub comm_link: Option<String>,
    pub drop_id: u16,
    pub cabinet_style: Option<String>,
    pub condition: u32,
    pub notes: String,
    pub version: Option<String>,
    pub fail_time: Option<String>,
    pub geo_loc: Option<String>,
    pub password: Option<String>,
}

/// Ancillary controller data
#[derive(Debug, Default)]
pub struct ControllerAnc {
    pub conditions: Option<Vec<Condition>>,
    pub cabinet_styles: Option<Vec<CabinetStyle>>,
    pub comm_links: Option<Vec<CommLink>>,
    pub comm_configs: Option<Vec<CommConfig>>,
    pub controller_io: Option<Vec<ControllerIo>>,
}

const CONDITION_URI: &str = "/iris/condition";
const COMM_LINK_URI: &str = "/iris/api/comm_link";
const COMM_CONFIG_URI: &str = "/iris/api/comm_config";
const CABINET_STYLE_URI: &str = "/iris/api/cabinet_style";

impl AncillaryData for ControllerAnc {
    type Primary = Controller;

    /// Get ancillary URI
    fn uri(&self, view: View, pri: &Controller) -> Option<Cow<str>> {
        match (
            view,
            &self.conditions,
            &self.comm_links,
            &self.comm_configs,
            &self.cabinet_styles,
            &self.controller_io,
        ) {
            (View::Search | View::Status | View::Edit, None, _, _, _, _) => {
                Some(CONDITION_URI.into())
            }
            (View::Search | View::Status, _, None, _, _, _) => {
                Some(COMM_LINK_URI.into())
            }
            (View::Search | View::Status, _, _, None, _, _) => {
                Some(COMM_CONFIG_URI.into())
            }
            (View::Edit, _, _, _, None, _) => Some(CABINET_STYLE_URI.into()),
            (View::Status, _, _, _, _, None) => {
                Some(format!("/iris/api/controller_io/{}", &pri.name).into())
            }
            _ => None,
        }
    }

    /// Put ancillary JSON data
    fn set_json(
        &mut self,
        view: View,
        pri: &Controller,
        json: JsValue,
    ) -> Result<()> {
        if let Some(uri) = self.uri(view, pri) {
            match uri.borrow() {
                CONDITION_URI => {
                    self.conditions =
                        Some(json.into_serde::<Vec<Condition>>()?);
                }
                COMM_LINK_URI => {
                    self.comm_links = Some(json.into_serde::<Vec<CommLink>>()?);
                }
                COMM_CONFIG_URI => {
                    self.comm_configs =
                        Some(json.into_serde::<Vec<CommConfig>>()?);
                }
                CABINET_STYLE_URI => {
                    self.cabinet_styles =
                        Some(json.into_serde::<Vec<CabinetStyle>>()?);
                }
                _ => {
                    self.controller_io =
                        Some(json.into_serde::<Vec<ControllerIo>>()?);
                }
            }
        }
        Ok(())
    }
}

impl ControllerAnc {
    /// Get condition description
    fn condition(&self, pri: &Controller) -> &str {
        if let Some(conditions) = &self.conditions {
            for condition in conditions {
                if pri.condition == condition.id {
                    return &condition.description;
                }
            }
        }
        ""
    }

    /// Create an HTML `select` element of controller conditions
    fn conditions_html(&self, pri: &Controller) -> String {
        let mut html = String::new();
        html.push_str("<select id='edit_condition'>");
        if let Some(conditions) = &self.conditions {
            for condition in conditions {
                html.push_str("<option value='");
                html.push_str(&condition.id.to_string());
                html.push('\'');
                if pri.condition == condition.id {
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

    /// Create an HTML `select` element of cabinet styles
    fn cabinet_styles_html(&self, pri: &Controller) -> String {
        let mut html = String::new();
        html.push_str("<select id='edit_cabinet'>");
        html.push_str("<option></option>");
        if let Some(cabinet_styles) = &self.cabinet_styles {
            for cabinet_style in cabinet_styles {
                html.push_str("<option");
                if let Some(cab) = &pri.cabinet_style {
                    if cab == &cabinet_style.name {
                        html.push_str(" selected");
                    }
                }
                html.push('>');
                html.push_str(&cabinet_style.name);
                html.push_str("</option>");
            }
        }
        html.push_str("</select>");
        html
    }

    /// Get the comm config
    fn comm_config(&self, pri: &Controller) -> &str {
        if let (Some(comm_link), Some(comm_links), Some(comm_configs)) =
            (&pri.comm_link, &self.comm_links, &self.comm_configs)
        {
            if let Some(cl) = comm_links.iter().find(|cl| &cl.name == comm_link)
            {
                if let Some(comm_config) =
                    comm_configs.iter().find(|cc| cc.name == cl.comm_config)
                {
                    return &comm_config.description[..];
                }
            }
        }
        ""
    }

    /// Build IO links as HTML
    fn io_pins_html(&self) -> String {
        let mut html = String::new();
        if let Some(controller_io) = &self.controller_io {
            html.push_str("<ul class='pins'>");
            for cio in controller_io {
                html.push_str(&cio.button_link_html());
            }
            html.push_str("</ul>");
        }
        html
    }
}

impl ControllerIo {
    /// Create a button to select the controller IO
    pub fn button_link_html(&self) -> String {
        let pin = self.pin;
        let res = Resource::from_name(&self.resource_n);
        let symbol = res.symbol();
        let rname = res.rname();
        let name = HtmlStr::new(&self.name);
        format!(
            "<li class='row'>\
              <span>#{pin}</span>\
              <span>{symbol} \
                <button type='button' class='go_link' \
                        data-link='{name}' data-type='{rname}'>\
                        {name}\
                </button>\
              </span>\
            </li>"
        )
    }
}

impl fmt::Display for Controller {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl Controller {
    pub const RESOURCE_N: &'static str = "controller";

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

    /// Get controller `link:drop`
    pub fn link_drop(&self) -> String {
        let comm_link = self.comm_link.as_deref().unwrap_or("");
        format!("{comm_link}:{}", self.drop_id)
    }

    /// Create a button to select the controller
    pub fn button_html(&self) -> String {
        let rname = Resource::Controller.rname();
        let link_drop = HtmlStr::new(self.link_drop());
        format!(
            "<button type='button' class='go_link' \
                     data-link='{link_drop}' data-type='{rname}'>\
                     {link_drop}\
            </button>"
        )
    }

    /// Create a controller button and location
    pub fn button_loc_html(&self) -> String {
        let button = self.button_html();
        let loc = HtmlStr::new(&self.location).with_len(32);
        format!(
            "<div class='row left'>\
              {button}\
              <span class='info'>{loc}</span>\
            </div>"
        )
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let comm_state = self.comm_state(false);
        let link_drop = HtmlStr::new(self.link_drop());
        // condition 1 is "Active"
        let disabled = disabled_attr(self.condition == 1);
        format!(
            "<span>{comm_state}</span>\
            <span{disabled}>{link_drop}</span>\
            <span class='{NAME}'>{self}</span>"
        )
    }

    /// Convert to status HTML
    fn to_html_status(&self, anc: &ControllerAnc) -> String {
        let rname = Resource::CommLink.rname();
        let condition = anc.condition(self);
        let comm_state = self.comm_state(true);
        let comm_link = HtmlStr::new(&self.comm_link);
        let drop_id = self.drop_id;
        let comm_config = anc.comm_config(self);
        let location = HtmlStr::new(&self.location).with_len(64);
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
        let io_pins = anc.io_pins_html();
        format!(
            "<div class='row'>\
              <span>{condition}</span>\
              <span>{comm_state}</span>\
              <span>\
                <button type='button' class='go_link' \
                        data-link='{comm_link}' data-type='{rname}'>\
                  {comm_link}\
                </button>\
                :{drop_id}\
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
            <div class='row'>{fail_time}</div>\
            {io_pins}\
            <div class='row'>\
              <span></span>\
              {LOC_BUTTON}\
              {EDIT_BUTTON}\
            </div>"
        )
    }

    /// Convert to edit HTML
    fn to_html_edit(&self, anc: &ControllerAnc) -> String {
        let comm_link = HtmlStr::new(&self.comm_link);
        let drop_id = self.drop_id;
        let cabinet_styles = anc.cabinet_styles_html(self);
        let conditions = anc.conditions_html(self);
        let notes = HtmlStr::new(&self.notes);
        let password = HtmlStr::new(&self.password);
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
              {cabinet_styles}
            </div>\
            <div class='row'>\
              <label for='edit_condition'>Condition</label>\
              {conditions}\
            </div>\
            <div class='row'>\
              <label for='edit_notes'>Notes</label>\
              <textarea id='edit_notes' maxlength='128' rows='2' \
                        cols='26'>{notes}</textarea>\
            </div>\
            <div class='row'>\
              <label for='edit_password'>Password</label>\
              <input id='edit_password' maxlength='32' size='26' \
                     value='{password}'/>\
            </div>"
        )
    }
}

impl Card for Controller {
    type Ancillary = ControllerAnc;

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Get geo location name
    fn geo_loc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &ControllerAnc) -> bool {
        self.name.contains_lower(search)
            || self.link_drop().contains_lower(search)
            || self.comm_state(true).contains(search)
            || anc.condition(self).contains_lower(search)
            || anc.comm_config(self).contains_lower(search)
            || self.location.contains_lower(search)
            || self.notes.contains_lower(search)
            || self.cabinet_style.contains_lower(search)
            || self.version.contains_lower(search)
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

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &ControllerAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Compact => self.to_html_compact(),
            View::Status => self.to_html_status(anc),
            View::Edit => self.to_html_edit(anc),
            _ => unreachable!(),
        }
    }

    /// Get changed fields from Edit form
    fn changed_fields(doc: &Document, json: &JsValue) -> Result<String> {
        let val = Self::new(json)?;
        let mut obj = Map::new();
        let comm_link = doc
            .input_parse::<String>("edit_link")
            .filter(|cl| !cl.is_empty());
        if comm_link != val.comm_link {
            obj.insert("comm_link".to_string(), OptVal(comm_link).into());
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
            .select_parse::<String>("edit_cabinet")
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
