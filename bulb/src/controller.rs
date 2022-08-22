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
use crate::util::{ContainsLower, Fields, HtmlStr, Input, Select, TextArea};
use serde::{Deserialize, Serialize};
use std::borrow::{Borrow, Cow};
use std::fmt;
use wasm_bindgen::JsValue;

/// Comm state
#[derive(Clone, Copy)]
pub enum CommState {
    Inactive,
    Online,
    Failed,
}

impl CommState {
    /// Get the comm state code
    pub fn code(self) -> &'static str {
        match self {
            Self::Inactive => "❓",
            Self::Online => "👍",
            Self::Failed => "💀",
        }
    }

    /// Get the comm state description
    pub fn description(self) -> &'static str {
        match self {
            Self::Inactive => "inactive",
            Self::Online => "online",
            Self::Failed => "failed",
        }
    }
}

impl fmt::Display for CommState {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.code())
    }
}

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

/// Optional setup data
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct Setup {
    pub model: Option<String>,
    pub serial_num: Option<String>,
    pub version: Option<String>,
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
    pub setup: Option<Setup>,
    pub fail_time: Option<String>,
    // full attributes
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

    /// Get next ancillary URI
    fn next_uri(&self, view: View, pri: &Controller) -> Option<Cow<str>> {
        match (
            view,
            &self.conditions,
            &self.comm_links,
            &self.comm_configs,
            &self.cabinet_styles,
            &self.controller_io,
        ) {
            (View::Search | View::Status(_) | View::Edit, None, _, _, _, _) => {
                Some(CONDITION_URI.into())
            }
            (View::Search | View::Status(_), _, None, _, _, _) => {
                Some(COMM_LINK_URI.into())
            }
            (View::Search | View::Status(_), _, _, None, _, _) => {
                Some(COMM_CONFIG_URI.into())
            }
            (View::Edit, _, _, _, None, _) => Some(CABINET_STYLE_URI.into()),
            (View::Status(_), _, _, _, _, None) => {
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
        if let Some(uri) = self.next_uri(view, pri) {
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
        html.push_str("<select id='condition'>");
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
        html.push_str("<select id='cabinet_style'>");
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

    /// Is controller active?
    pub fn is_active(&self) -> bool {
        // condition 1 is "Active"
        self.condition == 1
    }

    /// Get comm state
    pub fn comm_state(&self) -> CommState {
        let active = self.is_active();
        let failed = self.fail_time.is_some();
        match (active, failed) {
            (false, _) => CommState::Inactive,
            (true, false) => CommState::Online,
            (true, true) => CommState::Failed,
        }
    }

    /// Get controller `link:drop`
    pub fn link_drop(&self) -> String {
        let comm_link = self.comm_link.as_deref().unwrap_or("");
        format!("{comm_link}:{}", self.drop_id)
    }

    /// Get firmware version
    fn version(&self) -> Option<&str> {
        self.setup
            .as_ref()
            .and_then(|s| s.version.as_deref())
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
            "<div class='row start'>\
              {button}\
              <span class='info'>{loc}</span>\
            </div>"
        )
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let comm_state = self.comm_state();
        let disabled = disabled_attr(self.is_active());
        let link_drop = HtmlStr::new(self.link_drop());
        format!(
            "<div class='{NAME} end'>{comm_state} {self}</div>\
            <div class='info fill{disabled}'>{link_drop}</div>"
        )
    }

    /// Convert to status HTML
    fn to_html_status(&self, anc: &ControllerAnc) -> String {
        let rname = Resource::CommLink.rname();
        let condition = anc.condition(self);
        let comm_state = self.comm_state();
        let comm_desc = comm_state.description();
        let comm_link = HtmlStr::new(&self.comm_link);
        let drop_id = self.drop_id;
        let comm_config = anc.comm_config(self);
        let location = HtmlStr::new(&self.location).with_len(64);
        let notes = HtmlStr::new(&self.notes);
        let version = self
            .version()
            .map(|v| {
                format!(
                    "<span>\
                      <span>Version</span>\
                      <span class='info'>{}</span>\
                    </span>",
                    HtmlStr::new(v).with_len(32),
                )
            })
            .unwrap_or_else(|| "".to_string());
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
              <span>{comm_state} {comm_desc}</span>\
              <span>\
                <button type='button' class='go_link' \
                        data-link='{comm_link}' data-type='{rname}'>\
                  {comm_link}\
                </button>\
                :{drop_id}\
              </span>\
            </div>\
            <div class='info end'>{comm_config}</div>\
            <div class='row'>\
              <span>{location}</span>\
              <span class='info'>{notes}</span>\
            </div>\
            <div class='row fill'>{version}</div>\
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
              <label for='comm_link'>Comm Link</label>\
              <input id='comm_link' maxlength='20' size='20' \
                     value='{comm_link}'/>\
            </div>\
            <div class='row'>\
              <label for='drop_id'>Drop ID</label>\
              <input id='drop_id' type='number' min='0'
                     max='65535' size='6' value='{drop_id}'/>\
            </div>\
            <div class='row'>\
              <label for='cabinet_style'>Cabinet Style</label>\
              {cabinet_styles}
            </div>\
            <div class='row'>\
              <label for='condition'>Condition</label>\
              {conditions}\
            </div>\
            <div class='row'>\
              <label for='notes'>Notes</label>\
              <textarea id='notes' maxlength='128' rows='2' \
                        cols='26'>{notes}</textarea>\
            </div>\
            <div class='row'>\
              <label for='password'>Password</label>\
              <input id='password' maxlength='32' size='26' \
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
            || self.comm_state().code().contains(search)
            || anc.condition(self).contains_lower(search)
            || anc.comm_config(self).contains_lower(search)
            || self.location.contains_lower(search)
            || self.notes.contains_lower(search)
            || self.cabinet_style.contains_lower(search)
            || self.version().unwrap_or("").contains_lower(search)
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
            View::Status(_) => self.to_html_status(anc),
            View::Edit => self.to_html_edit(anc),
            _ => unreachable!(),
        }
    }

    /// Get changed fields from Edit form
    fn changed_fields(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("comm_link", &self.comm_link);
        fields.changed_input("drop_id", self.drop_id);
        fields.changed_select("cabinet_style", &self.cabinet_style);
        fields.changed_select("condition", self.condition);
        fields.changed_text_area("notes", &self.notes);
        fields.changed_input("password", &self.password);
        fields.into_value().to_string()
    }
}
