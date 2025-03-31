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
use crate::asset::Asset;
use crate::cabinetstyle::CabinetStyle;
use crate::card::{AncillaryData, Card, View};
use crate::commconfig::CommConfig;
use crate::commlink::CommLink;
use crate::error::Result;
use crate::geoloc::{Loc, LocAnc};
use crate::item::{ItemState, ItemStates};
use crate::util::{ContainsLower, Fields, HtmlStr, Input, Select, TextArea};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// Controller conditions
#[derive(Debug, Deserialize)]
pub struct Condition {
    pub id: u32,
    pub description: String,
}

/// Controller IO
#[derive(Debug, Deserialize)]
pub struct Io {
    pub pin: u32,
    pub resource_n: String,
    pub name: String,
}

/// Optional setup data
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Setup {
    pub model: Option<String>,
    pub serial_num: Option<String>,
    pub version: Option<String>,
}

/// Optional status data
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct ControllerStatus {
    pub faults: Option<String>,
}

/// Controller
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Controller {
    pub name: String,
    pub location: Option<String>,
    pub comm_link: Option<String>,
    pub drop_id: u16,
    pub cabinet_style: Option<String>,
    pub condition: u32,
    pub notes: Option<String>,
    pub setup: Option<Setup>,
    pub status: Option<ControllerStatus>,
    pub fail_time: Option<String>,
    // secondary attributes
    pub geo_loc: Option<String>,
    pub password: Option<String>,
}

/// Ancillary controller data
#[derive(Debug, Default)]
pub struct ControllerAnc {
    loc: LocAnc<Controller>,
    pub conditions: Vec<Condition>,
    pub cabinet_styles: Vec<CabinetStyle>,
    pub comm_links: Vec<CommLink>,
    pub comm_configs: Vec<CommConfig>,
    pub controller_io: Vec<Io>,
}

impl AncillaryData for ControllerAnc {
    type Primary = Controller;

    /// Construct ancillary controller data
    fn new(pri: &Controller, view: View) -> Self {
        let mut loc = LocAnc::new(pri, view);
        match view {
            View::Search => {
                loc.assets.push(Asset::Conditions);
                loc.assets.push(Asset::CommLinks);
                loc.assets.push(Asset::CommConfigs);
            }
            View::Status => {
                loc.assets.push(Asset::Conditions);
                loc.assets.push(Asset::CommLinks);
                loc.assets.push(Asset::CommConfigs);
                loc.assets.push(Asset::ControllerIo(pri.name.to_string()));
            }
            View::Setup => {
                loc.assets.push(Asset::Conditions);
                loc.assets.push(Asset::CabinetStyles);
            }
            _ => (),
        };
        ControllerAnc {
            loc,
            ..Default::default()
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.loc.asset()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &Controller,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::CabinetStyles => {
                self.cabinet_styles = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::CommConfigs => {
                self.comm_configs = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::CommLinks => {
                self.comm_links = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::Conditions => {
                self.conditions = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::ControllerIo(_ctrl) => {
                self.controller_io = serde_wasm_bindgen::from_value(value)?;
            }
            _ => self.loc.set_asset(pri, asset, value)?,
        }
        Ok(())
    }
}

impl ControllerAnc {
    /// Get condition description
    fn condition(&self, pri: &Controller) -> &str {
        for condition in &self.conditions {
            if pri.condition == condition.id {
                return &condition.description;
            }
        }
        ""
    }

    /// Create an HTML `select` element of controller conditions
    fn conditions_html(&self, pri: &Controller) -> String {
        let mut html = String::new();
        html.push_str("<select id='condition'>");
        for condition in &self.conditions {
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
        html.push_str("</select>");
        html
    }

    /// Create an HTML `select` element of cabinet styles
    fn cabinet_styles_html(&self, pri: &Controller) -> String {
        let mut html = String::new();
        html.push_str("<select id='cabinet_style'>");
        html.push_str("<option></option>");
        for cabinet_style in &self.cabinet_styles {
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
        html.push_str("</select>");
        html
    }

    /// Get the comm config
    fn comm_config(&self, pri: &Controller) -> &str {
        if let Some(comm_link) = &pri.comm_link {
            if let Some(cl) =
                &self.comm_links.iter().find(|cl| &cl.name == comm_link)
            {
                if let Some(comm_config) = &self
                    .comm_configs
                    .iter()
                    .find(|cc| cc.name == cl.comm_config)
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
        if !self.controller_io.is_empty() {
            html.push_str("<ul class='pins'>");
            for cio in &self.controller_io {
                html.push_str(&cio.button_link_html());
            }
            html.push_str("</ul>");
        }
        html
    }
}

impl Io {
    /// Create a button to select the controller IO
    pub fn button_link_html(&self) -> String {
        let pin = self.pin;
        match Res::try_from(self.resource_n.as_str()) {
            Ok(res) => {
                let symbol = res.symbol();
                let name = HtmlStr::new(&self.name);
                format!(
                    "<li class='row'>\
                      <span>#{pin}</span>\
                      <span>{symbol} \
                        <button type='button' class='go_link' \
                                data-link='{name}' data-type='{res}'>\
                                {name}\
                        </button>\
                      </span>\
                    </li>"
                )
            }
            _ => String::new(),
        }
    }
}

impl Loc for Controller {
    /// Get geo location name
    fn geoloc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }
}

impl Controller {
    /// Is controller active?
    pub fn is_active(&self) -> bool {
        // condition 1 is "Active"
        self.condition == 1
    }

    /// Get item states
    pub fn item_states(&self) -> ItemStates {
        match (self.is_active(), self.fail_time.is_some()) {
            (true, true) => ItemStates::default()
                .with(ItemState::Offline, "FIXME: since fail time"),
            (true, false) => ItemState::Available.into(),
            (false, _) => ItemState::Inactive.into(),
        }
    }

    /// Get controller `link:drop`
    pub fn link_drop(&self) -> String {
        let comm_link = self.comm_link.as_deref().unwrap_or("");
        format!("{comm_link}:{}", self.drop_id)
    }

    /// Get controller model
    fn model(&self) -> Option<&str> {
        self.setup.as_ref().and_then(|s| s.model.as_deref())
    }

    /// Get firmware version
    fn version(&self) -> Option<&str> {
        self.setup.as_ref().and_then(|s| s.version.as_deref())
    }

    /// Get serial number
    fn serial_num(&self) -> Option<&str> {
        self.setup.as_ref().and_then(|s| s.serial_num.as_deref())
    }

    /// Create a button to select the controller
    pub fn button_html(&self) -> String {
        let res = Res::Controller;
        let link_drop = HtmlStr::new(self.link_drop());
        format!(
            "<button type='button' class='go_link' \
                     data-link='{link_drop}' data-type='{res}'>\
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
        let name = HtmlStr::new(self.name());
        let item_states = self.item_states();
        let link_drop = HtmlStr::new(self.link_drop());
        format!(
            "<div class='title row'>{name} {item_states}</div>\
            <div class='info fill'>{link_drop}</div>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, anc: &ControllerAnc) -> String {
        let title = self.title(View::Status);
        let res = Res::CommLink;
        let condition = anc.condition(self);
        let item_states = self.item_states().to_html();
        let comm_link = HtmlStr::new(&self.comm_link);
        let drop_id = self.drop_id;
        let comm_config = anc.comm_config(self);
        let location = HtmlStr::new(&self.location).with_len(64);
        let notes = HtmlStr::new(&self.notes);
        let model = self
            .model()
            .map(|m| {
                format!(
                    "<div class='row fill'>\
                      <span>Model</span>\
                      <span class='info'>{}</span>\
                    </div>",
                    HtmlStr::new(m).with_len(32),
                )
            })
            .unwrap_or_default();
        let version = self
            .version()
            .map(|v| {
                format!(
                    "<div class='row fill'>\
                      <span>Version</span>\
                      <span class='info'>{}</span>\
                    </div>",
                    HtmlStr::new(v).with_len(32),
                )
            })
            .unwrap_or_default();
        let serial_num = self
            .serial_num()
            .map(|sn| {
                format!(
                    "<div class='row fill'>\
                      <span>S/N</span>\
                      <span class='info'>{}</span>\
                    </div>",
                    HtmlStr::new(sn).with_len(32),
                )
            })
            .unwrap_or_default();
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
            "{title}\
            <div class='row'>\
              <span>{item_states}</span>\
              <span>{condition}</span>\
              <span>\
                <button type='button' class='go_link' \
                        data-link='{comm_link}' data-type='{res}'>\
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
            {model}\
            {version}\
            {serial_num}\
            <div class='row'>{fail_time}</div>\
            {io_pins}"
        )
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &ControllerAnc) -> String {
        let title = self.title(View::Setup);
        let comm_link = HtmlStr::new(&self.comm_link);
        let drop_id = self.drop_id;
        let cabinet_styles = anc.cabinet_styles_html(self);
        let conditions = anc.conditions_html(self);
        let notes = HtmlStr::new(&self.notes);
        let password = HtmlStr::new(&self.password);
        let footer = self.footer(true);
        format!(
            "{title}\
            <div class='row'>\
              <label for='comm_link'>Comm Link</label>\
              <input id='comm_link' maxlength='20' size='20' \
                     value='{comm_link}'>\
            </div>\
            <div class='row'>\
              <label for='drop_id'>Drop ID</label>\
              <input id='drop_id' type='number' min='0'
                     max='65535' size='6' value='{drop_id}'>\
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
                     value='{password}'>\
            </div>\
            {footer}"
        )
    }
}

impl Card for Controller {
    type Ancillary = ControllerAnc;

    /// Display name
    const DNAME: &'static str = "🎛️ Controller";

    /// Suggested name prefix
    const PREFIX: &'static str = "ctl";

    /// Get the resource
    fn res() -> Res {
        Res::Controller
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
    fn is_match(&self, search: &str, anc: &ControllerAnc) -> bool {
        self.name.contains_lower(search)
            || self.link_drop().contains_lower(search)
            || self.item_states().is_match(search)
            || anc.condition(self).contains_lower(search)
            || anc.comm_config(self).contains_lower(search)
            || self.location.contains_lower(search)
            || self.notes.contains_lower(search)
            || self.cabinet_style.contains_lower(search)
            || self.version().unwrap_or("").contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &ControllerAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Location => anc.loc.to_html_loc(self),
            View::Setup => self.to_html_setup(anc),
            View::Status => self.to_html_status(anc),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("comm_link", &self.comm_link);
        fields.changed_input("drop_id", self.drop_id);
        fields.changed_select("cabinet_style", &self.cabinet_style);
        fields.changed_select("condition", self.condition);
        fields.changed_text_area("notes", &self.notes);
        fields.changed_input("password", &self.password);
        fields.into_value().to_string()
    }

    /// Get changed fields on Location view
    fn changed_location(&self, anc: ControllerAnc) -> String {
        anc.loc.changed_location()
    }
}
