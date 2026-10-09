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
use crate::item::{ItemState, ItemStates};
use crate::permission::{AccessLevel, Permission};
use crate::query::QueryParam;
use crate::util::{
    ContainsLower, Doc, Fields, Input, TextArea, opt_ref, opt_str,
};
use crate::view::View;
use hatmil::{Tree, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use wasm_bindgen::JsValue;
use web_sys::HtmlElement;

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
    access: Vec<Permission>,
    cameras: Vec<Camera>,
    playlists: Vec<PlayList>,
}

impl AncillaryData for PlayListAnc {
    type Primary = PlayList;

    /// Construct ancillary role data
    fn new(_pri: &PlayList, view: View) -> Self {
        let assets = match view {
            View::Setup(_) => {
                vec![Asset::Access, Asset::Cameras, Asset::PlayLists]
            }
            _ => vec![Asset::Access],
        };
        PlayListAnc {
            assets,
            access: Vec::new(),
            cameras: Vec::new(),
            playlists: Vec::new(),
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
            Asset::Access => {
                self.access = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::Cameras => {
                self.cameras = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::PlayLists => {
                self.playlists = serde_wasm_bindgen::from_value(value)?;
            }
            _ => unreachable!(),
        }
        Ok(())
    }
}

impl PlayListAnc {
    /// Get permission access level
    fn access_level(&self, pri: &PlayList) -> AccessLevel {
        Permission::access_level_notes(
            self.access.as_slice(),
            Res::VideoMonitor,
            Some(&pri.notes),
        )
    }

    /// Find a camera
    fn camera(&self, nm: &str) -> Option<&Camera> {
        self.cameras.iter().find(|c| c.name == *nm)
    }

    /// Find a playlist
    fn playlist(&self, nm: &str) -> Option<&PlayList> {
        self.playlists.iter().find(|p| p.name == *nm)
    }

    /// Get selected entries
    fn entries_selected(&self) -> Vec<String> {
        let mut entries = Vec::new();
        if let Some(ent) = Doc::get().opt_elem::<HtmlElement>("pl-entries") {
            let children = ent.children();
            for i in 0..children.length() {
                if let Some(c) = children.item(i) {
                    entries.push(c.id());
                }
            }
        }
        entries
    }

    /// Get actions to update entries
    fn entries_changed(&self, pri: &PlayList) -> Option<Vec<String>> {
        let entries = pri.entries.as_ref()?;
        let sel = self.entries_selected();
        (entries.as_slice() != sel.as_slice()).then_some(sel)
    }
}

impl PlayList {
    /// Get item states
    fn item_states<'a>(&'a self, anc: &'a PlayListAnc) -> ItemStates<'a> {
        let mut states = if self.meta {
            ItemStates::from(ItemState::Meta)
        } else {
            ItemStates::from(ItemState::Simple)
        };
        if anc.access_level(self) <= AccessLevel::View {
            states = states.with(ItemState::Prohibited, "");
        }
        states
    }

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

    /// Build HTML entries list
    fn entries_html<'p>(&self, anc: &PlayListAnc, div: &'p mut html::Div<'p>) {
        if let Some(entries) = &self.entries {
            let mut ul = div.ul();
            ul.id("pl-entries").class("draggable-item");
            for ent in entries {
                let mut li = ul.li();
                if self.meta {
                    match anc.playlist(ent) {
                        Some(pl) => {
                            li.id(&pl.name).draggable(true);
                            let query = QueryParam::new()
                                .with_res(Res::PlayList)
                                .with_sel(&pl.name);
                            li.a()
                                .href(query.to_string())
                                .cdata(&pl.name)
                                .close();
                            if let Some(num) = pl.seq_num {
                                li.span()
                                    .class("info")
                                    .cdata(format!("#{num} "))
                                    .close();
                            }
                            li.cdata(" ").cdata_len(&pl.notes, 64);
                        }
                        None => {
                            li.cdata(ent);
                        }
                    }
                } else {
                    match anc.camera(ent) {
                        Some(c) => {
                            li.id(&c.name).draggable(true);
                            let query = QueryParam::new()
                                .with_res(Res::Camera)
                                .with_sel(&c.name);
                            li.a()
                                .href(query.to_string())
                                .cdata(&c.name)
                                .close();
                            if let Some(num) = c.cam_num {
                                li.span()
                                    .class("info")
                                    .cdata(format!("#{num} "))
                                    .close();
                            }
                            li.cdata(" ").cdata_len(opt_ref(&c.location), 64);
                        }
                        None => {
                            li.cdata(ent);
                        }
                    }
                }
            }
            ul.close();
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self, anc: &PlayListAnc) -> String {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(self.item_states(anc).to_string());
        if let Some(sn) = &self.seq_num {
            div.span().class("info").cdata(format!("#{sn}")).close();
        }
        String::from(tree)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &PlayListAnc, edit: bool) -> String {
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
        div.label().r#for("seq_num").cdata("Sequence Num").close();
        div.input()
            .id("seq_num")
            .r#type("number")
            .min(0)
            .max(9999)
            .size(5)
            .value(opt_str(self.seq_num));
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("notes").cdata("Notes").close();
        div.textarea()
            .id("notes")
            .maxlength(63)
            .rows(2)
            .cols(22)
            .cdata(&self.notes)
            .close();
        div.close();
        self.entries_html(anc, &mut tree.root::<html::Div>());
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
    fn changed_attr(&self, anc: &PlayListAnc) -> Option<Attr> {
        let mut fields = Fields::new();
        fields.changed_input("seq_num", self.seq_num);
        fields.changed_text_area("notes", &self.notes);
        let mut attr = Attr::from(fields);
        if let Some(entries) = anc.entries_changed(self) {
            attr.array("entries", entries);
        }
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
        &[ItemState::Simple, ItemState::Meta, ItemState::Prohibited]
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
    fn item_state_main(&self, anc: &Self::Ancillary) -> ItemState {
        let states = self.item_states(anc);
        if states.contains(ItemState::Prohibited) {
            ItemState::Prohibited
        } else if states.contains(ItemState::Meta) {
            ItemState::Meta
        } else {
            ItemState::Simple
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &PlayListAnc) -> bool {
        self.name.contains_lower(search)
            || self.check_number(search)
            || self.item_states(anc).is_match(search)
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
