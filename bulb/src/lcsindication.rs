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
use crate::card::{AncillaryData, Card, View};
use crate::cio::{ControllerIo, ControllerIoAnc};
use crate::error::Result;
use crate::util::{ContainsLower, Fields, HtmlStr, Input};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// Lane Use Indications
#[derive(Debug, Deserialize)]
pub struct LaneUseIndication {
    pub id: u32,
    pub description: String,
}

/// LCS Indication
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct LcsIndication {
    pub name: String,
    pub controller: Option<String>,
    pub lcs: String,
    pub indication: u32,
    // secondary attributes
    pub pin: Option<u32>,
}

/// Ancillary LCS indication data
#[derive(Debug)]
pub struct LcsIndicationAnc {
    cio: ControllerIoAnc<LcsIndication>,
    pub indications: Vec<LaneUseIndication>,
}

impl LcsIndicationAnc {
    /// Get indication description
    fn indication(&self, pri: &LcsIndication) -> &str {
        for indication in &self.indications {
            if pri.indication == indication.id {
                return &indication.description;
            }
        }
        ""
    }
}

impl AncillaryData for LcsIndicationAnc {
    type Primary = LcsIndication;

    /// Construct ancillary LCS indication data
    fn new(pri: &LcsIndication, view: View) -> Self {
        let mut cio = ControllerIoAnc::new(pri, view);
        cio.assets.push(Asset::LaneUseIndications);
        LcsIndicationAnc {
            cio,
            indications: Vec::new(),
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.cio.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &LcsIndication,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::LaneUseIndications => {
                self.indications = serde_wasm_bindgen::from_value(value)?;
            }
            _ => self.cio.set_asset(pri, asset, value)?,
        }
        Ok(())
    }
}

impl ControllerIo for LcsIndication {
    /// Get controller name
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }
}

impl LcsIndication {
    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &LcsIndicationAnc) -> String {
        let name = HtmlStr::new(self.name());
        let item_states = anc.cio.item_states(self);
        let indication = anc.indication(self);
        format!(
            "<div class='title row'>{name} {item_states}</div>\
            <div class='info fill'>{indication}</div>"
        )
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &LcsIndicationAnc) -> String {
        let title = self.title(View::Setup);
        let controller = anc.cio.controller_html(self);
        let pin = anc.cio.pin_html(self.pin);
        format!("{title}{controller}{pin}")
    }
}

impl Card for LcsIndication {
    type Ancillary = LcsIndicationAnc;

    /// Display name
    const DNAME: &'static str = "🠟 LCS Indication";

    /// Get the resource
    fn res() -> Res {
        Res::LcsIndication
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
    fn is_match(&self, search: &str, anc: &LcsIndicationAnc) -> bool {
        self.name.contains_lower(search)
            || anc.cio.item_states(self).is_match(search)
            || anc.indication(self).contains(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &LcsIndicationAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }
}
