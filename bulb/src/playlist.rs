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
use crate::card::{AncillaryData, Card, footer_html, uri_all, uri_one};
use crate::domevent::EventTp;
use crate::eid;
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
use serde_json::Value;
use serde_json::map::Map;
use std::borrow::Cow;
use wasm_bindgen::{JsCast, JsValue};
use web_sys::{HtmlElement, HtmlInputElement};

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
            View::Control | View::Setup(_) => {
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

    /// Render a camera entry (control)
    fn render_camera_control<'p>(&self, li: &mut html::Li<'p>, id: &str) {
        match self.camera(id) {
            Some(c) => {
                li.button().r#type("button").cdata("▶︎ ").close();
                let query =
                    QueryParam::new().with_res(Res::Camera).with_sel(id);
                li.a().href(query.to_string()).cdata(id).close();
                if let Some(num) = c.cam_num {
                    li.span().class("info").cdata(format!("#{num} ")).close();
                }
                li.cdata(" ").cdata_len(opt_ref(&c.location), 64);
            }
            None => {
                log::warn!("camera not found: {id}");
            }
        }
    }

    /// Render a camera entry (setup)
    fn render_camera_setup<'p>(
        &self,
        li: &mut html::Li<'p>,
        id: &str,
        cls: &str,
        i: usize,
    ) {
        match self.camera(id) {
            Some(c) => {
                li.id(format!("entry-{i}")).class(cls).draggable(true);
                li.input().r#type("checkbox").checked();
                let query =
                    QueryParam::new().with_res(Res::Camera).with_sel(id);
                li.a().href(query.to_string()).cdata(id).close();
                if let Some(num) = c.cam_num {
                    li.span().class("info").cdata(format!("#{num} ")).close();
                }
                li.cdata(" ").cdata_len(opt_ref(&c.location), 64);
            }
            None => {
                log::warn!("camera not found: {id}");
            }
        }
    }

    /// Find a playlist
    fn playlist(&self, nm: &str) -> Option<&PlayList> {
        self.playlists.iter().find(|p| p.name == *nm)
    }

    /// Render a meta playlist entry as HTML (control)
    fn render_meta_control<'p>(&self, li: &mut html::Li<'p>, id: &str) {
        match self.playlist(id) {
            Some(pl) => {
                let query =
                    QueryParam::new().with_res(Res::PlayList).with_sel(id);
                li.a().href(query.to_string()).cdata(id).close();
                if let Some(num) = pl.seq_num {
                    li.span().class("info").cdata(format!("#{num} ")).close();
                }
                li.cdata(" ").cdata_len(&pl.notes, 64);
            }
            None => {
                log::warn!("playlist not found: {id}");
            }
        }
    }

    /// Render a meta playlist entry as HTML (setup)
    fn render_meta_setup<'p>(
        &self,
        li: &mut html::Li<'p>,
        id: &str,
        cls: &str,
        i: usize,
    ) {
        match self.playlist(id) {
            Some(pl) => {
                li.id(format!("entry-{i}")).class(cls).draggable(true);
                li.input().r#type("checkbox").checked();
                let query =
                    QueryParam::new().with_res(Res::PlayList).with_sel(id);
                li.a().href(query.to_string()).cdata(id).close();
                if let Some(num) = pl.seq_num {
                    li.span().class("info").cdata(format!("#{num} ")).close();
                }
                li.cdata(" ").cdata_len(&pl.notes, 64);
            }
            None => {
                log::warn!("playlist not found: {id}");
            }
        }
    }

    /// Get selected entries
    fn entries_selected(&self) -> Vec<String> {
        let mut entries = Vec::new();
        if let Some(ent) = Doc::get().opt_elem::<HtmlElement>("pl-entries") {
            let children = ent.children();
            for i in 0..children.length() {
                if let Some(c) = children.item(i) {
                    if let Some(keep) = c.first_element_child()
                        && let Ok(chk) = keep.dyn_into::<HtmlInputElement>()
                        && !chk.checked()
                    {
                        continue;
                    }
                    for e in c.class_name().split(' ') {
                        if e != "changed" && e != "dragging" {
                            entries.push(e.to_string());
                            break;
                        }
                    }
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
        if anc.access_level(self) <= AccessLevel::View {
            ItemStates::from(ItemState::Prohibited)
        } else {
            ItemStates::from(ItemState::Available)
        }
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

    /// Render setup entries list as HTML
    fn render_entries_setup<'p>(
        &self,
        anc: &PlayListAnc,
        div: &'p mut html::Div<'p>,
    ) {
        if let Some(entries) = &self.entries {
            let mut ul = div.ul();
            ul.id("pl-entries").class("draggable-item");
            for (i, ent) in entries.iter().enumerate() {
                let mut li = ul.li();
                if self.meta {
                    anc.render_meta_setup(&mut li, ent, ent, i);
                } else {
                    anc.render_camera_setup(&mut li, ent, ent, i);
                }
            }
            ul.close();
        }
        div.close();
    }

    /// Build HTML "add entry" select
    fn add_entry_html<'p>(
        &self,
        anc: &PlayListAnc,
        span: &'p mut html::Span<'p>,
    ) {
        span.class("single-line");
        span.button()
            .id("add-entry-btn")
            .r#type("button")
            .cdata("➕")
            .close();
        // FIXME: use customizable select
        let mut select = span.select();
        select.id("add-entry-sel");
        if self.meta {
            for pl in &anc.playlists {
                if !pl.meta {
                    let mut option = select.option();
                    option.value(&pl.name);
                    option.cdata(&pl.name);
                    if let Some(num) = pl.seq_num {
                        option.cdata(format!(" #{num}"));
                    }
                    option.cdata(" ").cdata_len(&pl.notes, 32);
                }
            }
        } else {
            for c in &anc.cameras {
                let mut option = select.option();
                option.value(&c.name);
                option.cdata(&c.name);
                if let Some(num) = c.cam_num {
                    option.cdata(format!(" #{num}"));
                }
                option.cdata(" ").cdata_len(opt_ref(&c.location), 32);
            }
        }
        select.close();
        span.close();
    }

    /// Add the currently selected entry to the play list
    fn add_entry(&self, anc: &PlayListAnc) -> Vec<Action> {
        let doc = Doc::get();
        let Some(sel) = doc.select_parse::<String>("add-entry-sel") else {
            return Vec::new();
        };
        let Some(entries) = doc.opt_elem::<HtmlElement>("pl-entries") else {
            return Vec::new();
        };
        let Ok(i) = usize::try_from(entries.children().length()) else {
            return Vec::new();
        };
        let cls = format!("{sel} changed");
        let mut tree = Tree::new();
        let mut li = tree.root::<html::Li>();
        if self.meta {
            anc.render_meta_setup(&mut li, &sel, &cls, i);
        } else {
            anc.render_camera_setup(&mut li, &sel, &cls, i);
        }
        let li = String::from(tree);
        match doc.0.create_element("li") {
            Ok(el) => {
                if let Err(e) = entries.append_with_node_1(&el) {
                    log::warn!("add_entry failed: {e:?}");
                }
                el.set_outer_html(&li);
            }
            Err(e) => log::warn!("create_element failed: {e:?}"),
        }
        Vec::new()
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

    /// Convert to Control HTML
    fn to_html_control(&self, anc: &PlayListAnc) -> String {
        let mut tree = Tree::new();
        self.title(View::Control, &mut tree.root::<html::Div>());
        self.render_state_row(anc, &mut tree.root::<html::Div>());
        self.render_entries_control(anc, &mut tree.root::<html::Div>());
        String::from(tree)
    }

    /// Render item state row as an HTML div element
    fn render_state_row<'p>(
        &self,
        anc: &PlayListAnc,
        div: &'p mut html::Div<'p>,
    ) {
        div.id("state-row").class("row fill");
        self.item_states(anc).spans(&mut div.span());
        if let Some(num) = self.seq_num {
            div.span().class("info").cdata(format!("#{num}"));
        }
        div.close();
    }

    /// Render control entries list as HTML
    fn render_entries_control<'p>(
        &self,
        anc: &PlayListAnc,
        div: &'p mut html::Div<'p>,
    ) {
        if let Some(entries) = &self.entries {
            let mut ul = div.ul();
            ul.class("playlist");
            for ent in entries {
                let mut li = ul.li();
                if self.meta {
                    anc.render_meta_control(&mut li, ent);
                } else {
                    anc.render_camera_control(&mut li, ent);
                }
            }
            ul.close();
        }
        div.close();
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
        self.render_entries_setup(anc, &mut tree.root::<html::Div>());
        self.add_entry_html(anc, &mut tree.root::<html::Span>());
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
        &[ItemState::Available, ItemState::Prohibited]
    }

    /// Handle click event for the create button
    fn handle_create() -> Vec<Action> {
        let doc = Doc::get();
        if let Some(name) = doc.input_option_string(eid::NAME) {
            let meta = doc.input_bool("meta");
            let mut obj = Map::new();
            obj.insert("name".to_string(), Value::String(name));
            obj.insert("meta".to_string(), Value::Bool(meta));
            let value = Value::Object(obj).to_string();
            let uri = uri_all(Self::res());
            vec![Action::Post(uri, value.into())]
        } else {
            Vec::new()
        }
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
        } else {
            ItemState::Available
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &PlayListAnc) -> bool {
        self.name.contains_lower(search)
            || self.check_number(search)
            || self.item_states(anc).is_match(search)
    }

    /// Convert to Create HTML
    fn to_html_create(&self, len: u32) -> String {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for(eid::NAME).cdata("Name").close();
        div.input()
            .id(eid::NAME)
            .maxlength(len)
            .size(len.min(24))
            .value(self.name());
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label()
            .r#for("meta")
            .cdata("Meta (contains other play lists)")
            .close();
        let mut input = div.input();
        input.id("meta").r#type("checkbox");
        div.close();
        String::from(tree)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &PlayListAnc) -> String {
        match view {
            View::Create => self.to_html_create(15),
            View::Control => self.to_html_control(anc),
            View::Setup(edit) => self.to_html_setup(anc, edit),
            _ => self.to_html_compact(anc),
        }
    }

    /// Handle event for the card
    fn handle_event(
        &self,
        anc: Self::Ancillary,
        id: &str,
        tp: EventTp,
    ) -> Vec<Action> {
        match (id, tp) {
            ("add-entry-btn", EventTp::Click(_)) => self.add_entry(&anc),
            _ => self.handle_event_fallback(anc, id, tp),
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
