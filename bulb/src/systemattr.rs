// Copyright (C) 2026  Minnesota Department of Transportation
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
use crate::card::{AncillaryData, Card, footer_html, uri_one};
use crate::error::Result;
use crate::item::ItemState;
use crate::util::{ContainsLower, Fields, Input};
use crate::view::View;
use hatmil::{Tree, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;

/// System Attribute
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct SystemAttr {
    pub name: String,
    pub value: String,
}

/// Ancillary system attribute data
#[derive(Debug, Default)]
pub struct SystemAttrAnc;

impl AncillaryData for SystemAttrAnc {
    type Primary = SystemAttr;

    /// Construct ancillary system attribute data
    fn new(_pri: &SystemAttr, _view: View) -> Self {
        SystemAttrAnc
    }
}

impl SystemAttr {
    /// Fetch one system attribute
    pub async fn fetch_one(name: &str) -> Result<Self> {
        let json = uri_one(Res::SystemAttribute, name).get().await?;
        Ok(serde_wasm_bindgen::from_value::<SystemAttr>(json)?)
    }

    /// Render a work request URL
    pub fn render_work_req<'p>(
        sys_attrs: &[Self],
        a: &mut html::A<'p>,
        nm: &str,
        lonlat: Option<(f64, f64)>,
    ) {
        for sa in sys_attrs {
            if sa.name == "work_request_url" {
                let url = sa.build_work_req_url(nm, lonlat);
                return Self::render_work_req_link(a, &url, nm);
            }
        }
        log::warn!("work_request_url not found");
        Self::render_work_req_link(a, "", nm);
    }

    /// Render a work request URL
    fn build_work_req_url(
        &self,
        nm: &str,
        lonlat: Option<(f64, f64)>,
    ) -> String {
        let mut url = String::with_capacity(100);
        let host = &self.value;
        if !host.is_empty() {
            url.push_str("https://");
            url.push_str(host);
            url.push_str("/3s/forms/tams/");
            url.push_str("?asset-type=720");
            url.push_str("&inventory-id-field=its_device_name");
            url.push_str("&inventory-id=");
            url.push_str(nm);
            if let Some((lon, lat)) = lonlat {
                url.push_str("&longitude=");
                url.push_str(&lon.to_string());
                url.push_str("&latitude=");
                url.push_str(&lat.to_string());
            }
        }
        url
    }

    /// Render a work request link
    fn render_work_req_link<'p>(a: &mut html::A<'p>, mut url: &str, nm: &str) {
        if url.is_empty() {
            url = "http://example.com";
        }
        a.href(url)
            .target("_blank")
            .rel("noopener noreferrer")
            .cdata("🔗 ")
            .cdata(nm);
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.cdata(self.name());
        String::from(tree)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, edit: bool) -> String {
        let mut tree = Tree::new();
        self.title(View::Setup(edit), &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("value").cdata("Value").close();
        let mut input = div.input();
        input.id("value").maxlength(64).size(24).value(&self.value);
        div.close();
        footer_html(View::Setup(edit), false, &mut tree.root::<html::Div>());
        String::from(tree)
    }
}

impl Card for SystemAttr {
    type Ancillary = SystemAttrAnc;

    /// Get the resource
    fn res() -> Res {
        Res::SystemAttribute
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[]
    }

    /// Get the name
    fn name(&self) -> Cow<'_, str> {
        Cow::Borrowed(&self.name)
    }

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, _anc: &SystemAttrAnc) -> bool {
        self.name.contains_lower(search) || self.value.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, _anc: &SystemAttrAnc) -> String {
        match view {
            View::Setup(edit) => self.to_html_setup(edit),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("value", &self.value);
        fields.into()
    }
}
