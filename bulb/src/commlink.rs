// Copyright (C) 2022-2023  Minnesota Department of Transportation
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
use crate::commconfig::CommConfig;
use crate::controller::Controller;
use crate::error::Result;
use crate::fetch::Uri;
use crate::resource::{
    disabled_attr, AncillaryData, Card, View, EDIT_BUTTON, NAME,
};
use crate::util::{ContainsLower, Fields, HtmlStr, Input, Select};
use serde::{Deserialize, Serialize};
use std::fmt;
use wasm_bindgen::JsValue;

/// Comm link
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct CommLink {
    pub name: String,
    pub description: String,
    pub uri: String,
    pub comm_config: String,
    pub poll_enabled: bool,
    pub connected: bool,
}

/// Ancillary comm link data
#[derive(Debug, Default)]
pub struct CommLinkAnc {
    pub controllers: Option<Vec<Controller>>,
    pub comm_configs: Option<Vec<CommConfig>>,
}

const CONTROLLER_URI: &str = "/iris/api/controller";
const COMM_CONFIG_URI: &str = "/iris/api/comm_config";

impl AncillaryData for CommLinkAnc {
    type Primary = CommLink;

    /// Get next ancillary URI
    fn next_uri(&self, view: View, _pri: &CommLink) -> Option<Uri> {
        match (view, &self.controllers, &self.comm_configs) {
            (View::Status(_), None, _) => Some(CONTROLLER_URI.into()),
            (View::Status(_) | View::Edit | View::Search, _, None) => {
                Some(COMM_CONFIG_URI.into())
            }
            _ => None,
        }
    }

    /// Put ancillary JSON data
    fn set_json(
        &mut self,
        view: View,
        pri: &CommLink,
        json: JsValue,
    ) -> Result<()> {
        if let Some(uri) = self.next_uri(view, pri) {
            match uri.as_str() {
                CONTROLLER_URI => {
                    let mut controllers: Vec<Controller> =
                        serde_wasm_bindgen::from_value(json)?;
                    controllers
                        .retain(|c| c.comm_link.as_deref() == Some(&pri.name));
                    self.controllers = Some(controllers);
                }
                _ => {
                    self.comm_configs =
                        Some(serde_wasm_bindgen::from_value(json)?);
                }
            }
        }
        Ok(())
    }
}

impl CommLinkAnc {
    /// Get comm config description
    fn comm_config_desc(&self, pri: &CommLink) -> &str {
        if let Some(comm_configs) = &self.comm_configs {
            for config in comm_configs {
                if pri.comm_config == config.name {
                    return &config.description;
                }
            }
        }
        ""
    }

    /// Create an HTML `select` element of comm configs
    fn comm_configs_html(&self, pri: &CommLink) -> String {
        let mut html = String::new();
        html.push_str("<select id='comm_config'>");
        if let Some(comm_configs) = &self.comm_configs {
            for config in comm_configs {
                html.push_str("<option value='");
                html.push_str(&config.name);
                html.push('\'');
                if pri.comm_config == config.name {
                    html.push_str(" selected");
                }
                html.push('>');
                html.push_str(&config.description);
                html.push_str("</option>");
            }
        }
        html.push_str("</select>");
        html
    }

    /// Build controller links as HTML
    fn controllers_html(&self) -> String {
        let mut html = String::new();
        if let Some(controllers) = &self.controllers {
            for ctrl in controllers {
                html.push_str(&ctrl.button_loc_html());
            }
        }
        html
    }
}

impl fmt::Display for CommLink {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl CommLink {
    pub const RESOURCE_N: &'static str = "comm_link";

    /// Get connected state to display
    fn connected(&self, long: bool) -> &'static str {
        match (self.poll_enabled, self.connected, long) {
            (true, true, false) => "👍",
            (true, true, true) => "online 👍",
            (true, false, false) => "🔌",
            (true, false, true) => "offline 🔌",
            (false, _, false) => "🔻",
            (false, _, true) => "disabled 🔻",
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let connected = self.connected(false);
        let disabled = disabled_attr(self.poll_enabled);
        let description = HtmlStr::new(&self.description);
        format!(
            "<div class='{NAME} end'>{connected} {self}</div>\
            <div class='info fill{disabled}'>{description}</div>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &CommLinkAnc) -> String {
        let connected = self.connected(true);
        let disabled = disabled_attr(self.poll_enabled);
        let description = HtmlStr::new(&self.description);
        let comm_config = anc.comm_config_desc(self);
        let config = HtmlStr::new(comm_config);
        let controllers = anc.controllers_html();
        format!(
            "<div class='row'>\
              <span>{connected}</span>\
              <span class='info end{disabled}'>{description}</span>\
            </div>\
            <div class='row'>\
              <span>{config}</span>\
            </div>\
            {controllers}\
            <div class='row end'>\
              {EDIT_BUTTON}\
            </div>"
        )
    }

    /// Convert to Edit HTML
    fn to_html_edit(&self, anc: &CommLinkAnc) -> String {
        let description = HtmlStr::new(&self.description);
        let uri = HtmlStr::new(&self.uri);
        let enabled = if self.poll_enabled { " checked" } else { "" };
        let comm_configs = anc.comm_configs_html(self);
        format!(
            "<div class='row'>\
              <label for='description'>Description</label>\
              <input id='description' maxlength='32' size='24' \
                     value='{description}'>\
            </div>\
            <div class='row'>\
              <label for='uri'>URI</label>\
              <input id='uri' maxlength='256' size='28' value='{uri}'>\
            </div>\
            <div class='row'>\
              <label for='comm_config'>Comm Config</label>\
              {comm_configs}\
            </div>\
            <div class='row'>\
              <label for='poll_enabled'>Poll Enabled</label>\
              <input id='poll_enabled' type='checkbox'{enabled}>\
            </div>"
        )
    }
}

impl Card for CommLink {
    type Ancillary = CommLinkAnc;

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &CommLinkAnc) -> bool {
        self.description.contains_lower(search)
            || self.name.contains_lower(search)
            || anc.comm_config_desc(self).contains_lower(search)
            || self.uri.contains_lower(search)
            || self.connected(true).contains(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &CommLinkAnc) -> String {
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
        fields.changed_input("description", &self.description);
        fields.changed_input("uri", &self.uri);
        fields.changed_select("comm_config", &self.comm_config);
        fields.changed_input("poll_enabled", self.poll_enabled);
        fields.into_value().to_string()
    }
}
