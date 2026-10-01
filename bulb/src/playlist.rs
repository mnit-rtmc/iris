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
use crate::asset::Asset;
use crate::attr::Attr;
use crate::camera::Camera;
use crate::card::{AncillaryData, Card, footer_html, uri_one};
use crate::error::Result;
use crate::fetch::Action;
use crate::item::ItemState;
use crate::util::{ContainsLower, Fields, Input, opt_str};
use crate::view::View;
use hatmil::{Tree, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// Playlist
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct PlayList {
    pub name: String,
    pub meta: bool,
    pub seq_num: Option<u32>,
    pub notes: String,
    // secondary attributes
    pub entries: Option<Vec<String>>,
}

/// Ancillary playlist data
#[derive(Debug, Default)]
pub struct PlayListAnc {
    assets: Vec<Asset>,
    cameras: Vec<Camera>,
}

impl AncillaryData for PlayListAnc {
    type Primary = PlayList;

    /// Construct ancillary role data
    fn new(_pri: &PlayList, view: View) -> Self {
        let assets = match view {
            View::Setup(_) => vec![Asset::Cameras],
            _ => Vec::new(),
        };
        PlayListAnc {
            assets,
            cameras: Vec::new(),
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        _pri: &PlayList,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::Cameras => {
                self.cameras = serde_wasm_bindgen::from_value(value)?;
            }
            _ => unreachable!(),
        }
        Ok(())
    }
}

impl PlayList {
    /// Search for sequence number
    fn check_number(&self, search: &str) -> bool {
        match self.seq_num {
            Some(sn) => {
                let sn = sn.to_string();
                match search.strip_prefix('#') {
                    Some(s) => sn.starts_with(s),
                    None => sn.contains(search),
                }
            }
            None => false,
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &PlayListAnc) -> String {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(self.item_state_main(anc).to_string());
        if let Some(sn) = &self.seq_num {
            div.span().class("info").cdata(format!("#{sn}")).close();
        }
        String::from(tree)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, _anc: &PlayListAnc, edit: bool) -> String {
        let mut tree = Tree::new();
        self.title(View::Setup(edit), &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row").label().r#for("meta").cdata("Meta").close();
        let mut input = div.input();
        input.id("meta").r#type("checkbox").disabled();
        if self.meta {
            input.checked();
        }
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("seq_num").cdata("Seq Num").close();
        div.input()
            .id("seq_num")
            .r#type("number")
            .min(0)
            .max(9999)
            .size(6)
            .value(opt_str(self.seq_num));
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("notes").cdata("Notes").close();
        div.textarea()
            .id("notes")
            .maxlength(63)
            .rows(3)
            .cols(22)
            .cdata(&self.notes)
            .close();
        div.close();
        let can_delete = match &self.entries {
            Some(entries) => entries.is_empty(),
            None => false,
        };
        footer_html(
            View::Setup(edit),
            can_delete,
            &mut tree.root::<html::Div>(),
        );
        String::from(tree)
    }

    /// Get changed attributes from Setup form
    fn changed_attr(&self, _anc: &PlayListAnc) -> Option<Attr> {
        let mut fields = Fields::new();
        fields.changed_input("seq_num", self.seq_num);
        fields.changed_input("notes", &self.notes);
        let attr = Attr::from(fields);
        /*
        if let Some(entries) = anc.entries_changed(self) {
            attr.array("entries", entries);
        }*/
        if !attr.is_empty() { Some(attr) } else { None }
    }
}

impl Card for PlayList {
    type Ancillary = PlayListAnc;

    /// Get the resource
    fn res() -> Res {
        Res::PlayList
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[ItemState::Simple, ItemState::Meta]
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

    /// Get the main item state
    fn item_state_main(&self, _anc: &Self::Ancillary) -> ItemState {
        if self.meta {
            ItemState::Meta
        } else {
            ItemState::Simple
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &PlayListAnc) -> bool {
        self.name.contains_lower(search)
            || self.check_number(search)
            || self.item_state_main(anc).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &PlayListAnc) -> String {
        match view {
            View::Create => self.to_html_create(15),
            View::Setup(edit) => self.to_html_setup(anc, edit),
            _ => self.to_html_compact(anc),
        }
    }

    /// Handle click event for the save button
    fn handle_save(&self, anc: Self::Ancillary) -> Vec<Action> {
        let mut actions = Vec::new();
        if let Some(changed) = self.changed_attr(&anc) {
            let uri = uri_one(Self::res(), &self.name());
            actions.push(Action::Patch(uri, changed.into()));
        }
        actions
    }
}
