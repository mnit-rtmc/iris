// Copyright (C) 2022-2024  Minnesota Department of Transportation
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
use crate::card::{inactive_attr, AncillaryData, Card, View};
use crate::controller::Controller;
use crate::error::Result;
use crate::fetch::Uri;
use crate::util::{ContainsLower, Fields, HtmlStr, Input, OptVal};
use resources::Res;
use serde::{Deserialize, Serialize};
use std::borrow::Cow;
use std::iter::{empty, once};
use wasm_bindgen::JsValue;

/// Lane Use Indications
#[derive(Debug, Deserialize, Serialize)]
pub struct LaneUseIndication {
    pub id: u32,
    pub description: String,
}

/// LCS Indication
#[derive(Debug, Default, Deserialize, Serialize, PartialEq)]
pub struct LcsIndication {
    pub name: String,
    pub controller: Option<String>,
    pub lcs: String,
    pub indication: u32,
    // secondary attributes
    pub pin: Option<u32>,
}

/// Ancillary gate arm data
#[derive(Debug, Default)]
pub struct LcsIndicationAnc {
    pub controller: Option<Controller>,
    pub indications: Option<Vec<LaneUseIndication>>,
}

impl LcsIndicationAnc {
    fn controller_button(&self) -> String {
        match &self.controller {
            Some(ctrl) => ctrl.button_html(),
            None => "<span></span>".into(),
        }
    }

    /// Get indication description
    fn indication(&self, pri: &LcsIndication) -> &str {
        if let Some(indications) = &self.indications {
            for indication in indications {
                if pri.indication == indication.id {
                    return &indication.description;
                }
            }
        }
        ""
    }
}

const LANE_USE_INDICATION_URI: &str = "/iris/lut/lane_use_indication";

impl AncillaryData for LcsIndicationAnc {
    type Primary = LcsIndication;

    /// Get URI iterator
    fn uri_iter(
        &self,
        pri: &LcsIndication,
        view: View,
    ) -> Box<dyn Iterator<Item = Uri>> {
        match (view, &pri.controller()) {
            (View::Control, Some(ctrl)) => {
                let mut uri = Uri::from("/iris/api/controller/");
                uri.push(ctrl);
                Box::new([LANE_USE_INDICATION_URI.into(), uri].into_iter())
            }
            (View::Control | View::Search | View::Compact, _) => {
                Box::new(once(LANE_USE_INDICATION_URI.into()))
            }
            _ => Box::new(empty()),
        }
    }

    /// Put ancillary data
    fn set_data(
        &mut self,
        _pri: &LcsIndication,
        uri: Uri,
        data: JsValue,
    ) -> Result<bool> {
        if uri.as_str() == LANE_USE_INDICATION_URI {
            self.indications = Some(serde_wasm_bindgen::from_value(data)?);
        } else {
            self.controller = Some(serde_wasm_bindgen::from_value(data)?);
        }
        Ok(false)
    }
}

impl LcsIndication {
    /// Get controller
    fn controller(&self) -> Option<&str> {
        self.controller.as_deref()
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &LcsIndicationAnc) -> String {
        let name = HtmlStr::new(self.name());
        let inactive = inactive_attr(self.controller.is_some());
        let indication = anc.indication(self);
        format!(
            "<div class='title row'>{name}</div>\
            <div class='info fill{inactive}'>{indication}</div>"
        )
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &LcsIndicationAnc) -> String {
        let title = self.title(View::Setup);
        let ctl_btn = anc.controller_button();
        let controller = HtmlStr::new(&self.controller);
        let pin = OptVal(self.pin);
        format!(
            "{title}\
            <div class='row'>\
               {ctl_btn}\
               <label for='controller'>Controller</label>\
               <input id='controller' maxlength='20' size='20' \
                      value='{controller}'>\
             </div>\
             <div class='row'>\
               <label for='pin'>Pin</label>\
               <input id='pin' type='number' min='1' max='104' \
                      size='8' value='{pin}'>\
             </div>"
        )
    }
}

impl Card for LcsIndication {
    type Ancillary = LcsIndicationAnc;

    /// Display name
    const DNAME: &'static str = "🡇 LCS Indication";

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
    fn changed_fields(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("controller", &self.controller);
        fields.changed_input("pin", self.pin);
        fields.into_value().to_string()
    }
}
