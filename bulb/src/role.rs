// Copyright (C) 2022-2026  Minnesota Department of Transportation
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
use crate::card::{AncillaryData, Card, footer_html, uri_all, uri_one};
use crate::domain::Domain;
use crate::error::Result;
use crate::fetch::Action;
use crate::item::ItemState;
use crate::permission::{Permission, access_item_state};
use crate::util::{ContainsLower, Doc, Fields, Input};
use crate::view::View;
use hatmil::{Tree, html};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use std::collections::HashSet;
use wasm_bindgen::JsValue;
use web_sys::{
    HtmlButtonElement, HtmlElement, HtmlInputElement, HtmlSelectElement,
};

/// Resource Type
#[derive(Debug, Default, Deserialize)]
pub struct ResourceType {
    pub name: String,
    pub base: Option<String>,
}

/// State for one role / permission
#[derive(Clone, Copy, Debug)]
enum PermState {
    /// Existing permission (PATCH / DELETE)
    Existing,
    /// Missing resource permission (POST)
    Missing,
    /// New hashtag permission (POST)
    Hashtag,
}

/// Role permission and state
#[derive(Clone, Debug)]
struct RolePerm {
    perm: Permission,
    state: PermState,
}

impl RolePerm {
    /// Create an existing role permission
    fn new(perm: Permission) -> Self {
        RolePerm {
            perm,
            state: PermState::Existing,
        }
    }

    /// Change state to missing
    fn missing(mut self) -> Self {
        self.state = PermState::Missing;
        self
    }

    /// Change state to hashtag
    fn hashtag(mut self) -> Self {
        self.state = PermState::Hashtag;
        self
    }

    /// Build HTML table row
    fn table_row<'p>(self, tr: &'p mut html::Tr<'p>) {
        let perm = self.perm;
        let ht_res = format!("ht_{}", perm.base_resource);
        match self.state {
            PermState::Existing => {
                match perm.hashtag.as_deref() {
                    Some(hashtag) => {
                        tr.td().close();
                        tr.td().class("hashtag").cdata(hashtag).close();
                    }
                    None => {
                        let mut td = tr.td();
                        let bid = format!("{ht_res}_btn");
                        td.button()
                            .id(bid)
                            .r#type("button")
                            .class("hashtag")
                            .cdata("#");
                        td.close();
                        tr.td().cdata(&perm.base_resource).close();
                    }
                };
            }
            PermState::Missing => {
                tr.td().close();
                tr.td().cdata(&perm.base_resource).close();
            }
            PermState::Hashtag => {
                tr.id(&ht_res).class("no-display");
                tr.td().close();
                let hid = format!("{ht_res}_inp");
                let mut td = tr.td();
                td.input()
                    .id(hid)
                    .class("hashtag")
                    .size(12)
                    .minlength(2)
                    .maxlength(16)
                    .pattern("#[A-Za-z0-9]+")
                    .value("#");
                td.close();
            }
        }
        let mut td = tr.td();
        let mut select = td.select();
        select.id(&perm.name);
        for access in 0..=4 {
            let mut option = select.option();
            option.value(access.to_string());
            if access == perm.access_level {
                option.selected();
            }
            let item = access_item_state(access);
            option
                .cdata(item.code())
                .cdata(" ")
                .cdata(item.description())
                .close();
        }
        select.close();
        tr.close();
    }

    /// Update permission access level
    fn update_access(&mut self) -> bool {
        if let Some(select) =
            Doc::get().opt_elem::<HtmlSelectElement>(&self.perm.name)
            && let Ok(access) = select.value().parse::<u32>()
            && access != self.perm.access_level
        {
            self.perm.access_level = access;
            return true;
        }
        false
    }

    /// Make actions to update role permission
    fn actions(&mut self) -> Vec<Action> {
        let mut actions = Vec::new();
        if !self.update_access() {
            return actions;
        }
        let perm = &self.perm;
        match self.state {
            PermState::Existing => {
                let uri = uri_one(Res::Permission, &perm.name);
                if perm.access_level == 0 {
                    actions.push(Action::Delete(uri));
                } else {
                    let mut fields = Fields::new();
                    fields.insert_num("access_level", perm.access_level);
                    let changed = fields.into_value().to_string();
                    actions.push(Action::Patch(uri, changed.into()));
                }
            }
            PermState::Missing => {
                let post_uri = uri_all(Res::Permission);
                let patch_uri = uri_one(Res::Permission, &perm.name);
                let mut fields = Fields::new();
                fields.insert_num("access_level", perm.access_level);
                let value = perm.value().to_string();
                actions.push(Action::Post(post_uri, value.into()));
                let changed = fields.into_value().to_string();
                actions.push(Action::Patch(patch_uri, changed.into()));
            }
            PermState::Hashtag => {
                if let Some(hashtag) = self.input_hashtag() {
                    let post_uri = uri_all(Res::Permission);
                    let patch_uri = uri_one(Res::Permission, &perm.name);
                    let mut fields = Fields::new();
                    fields.insert_str("hashtag", &hashtag);
                    fields.insert_num("access_level", perm.access_level);
                    let value = perm.value().to_string();
                    actions.push(Action::Post(post_uri, value.into()));
                    let changed = fields.into_value().to_string();
                    actions.push(Action::Patch(patch_uri, changed.into()));
                }
            }
        }
        actions
    }

    /// Get input hashtag
    fn input_hashtag(&self) -> Option<String> {
        let id = format!("ht_{}_inp", self.perm.base_resource);
        let input = Doc::get().opt_elem::<HtmlInputElement>(&id)?;
        let ht = input.value();
        if ht.starts_with("#")
            && ht[1..].chars().all(|c| c.is_ascii_alphanumeric())
        {
            return Some(ht);
        }
        None
    }
}

/// Role
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct Role {
    pub name: String,
    pub enabled: bool,
    // secondary attributes
    pub domains: Option<Vec<String>>,
}

/// Ancillary role data
#[derive(Debug, Default)]
pub struct RoleAnc {
    assets: Vec<Asset>,
    pub resource_types: Vec<ResourceType>,
    pub permissions: Vec<Permission>,
    /// Existing permission names
    pub perm_names: HashSet<String>,
    pub domains: Vec<Domain>,
}

impl AncillaryData for RoleAnc {
    type Primary = Role;

    /// Construct ancillary role data
    fn new(_pri: &Role, view: View) -> Self {
        let assets = match view {
            View::Setup(_) | View::SaveEv => {
                vec![Asset::ResourceTypes, Asset::Permissions, Asset::Domains]
            }
            _ => Vec::new(),
        };
        let resource_types = Vec::new();
        let permissions = Vec::new();
        let perm_names = HashSet::new();
        let domains = Vec::new();
        RoleAnc {
            assets,
            resource_types,
            permissions,
            perm_names,
            domains,
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &Role,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::ResourceTypes => {
                self.resource_types = serde_wasm_bindgen::from_value(value)?;
            }
            Asset::Permissions => {
                let mut permissions: Vec<Permission> =
                    serde_wasm_bindgen::from_value(value)?;
                // NOTE: perm_names only needed for SaveEv view
                self.perm_names =
                    permissions.iter().map(|p| p.name.to_string()).collect();
                permissions.retain(|p| p.role == pri.name);
                permissions.sort();
                self.permissions = permissions;
            }
            Asset::Domains => {
                self.domains = serde_wasm_bindgen::from_value(value)?;
            }
            _ => unreachable!(),
        }
        Ok(())
    }
}

impl RoleAnc {
    /// Make next permission name
    fn perm_name(&self, num: &mut u32) -> String {
        while *num < u32::MAX {
            let nm = format!("prm_{num}");
            *num += 1;
            if !self.perm_names.contains(&nm) {
                return nm;
            }
        }
        String::from("perm_overrun")
    }

    /// Make a permission for role setup card
    fn make_perm(
        &self,
        pri: &Role,
        res: &ResourceType,
        num: &mut u32,
    ) -> RolePerm {
        let nm = self.perm_name(num);
        let p = Permission::new(nm, &pri.name, &res.name);
        RolePerm::new(p)
    }

    /// Build a `Vec` of all role permissions
    fn role_permissions(&self, pri: &Role) -> Vec<RolePerm> {
        let mut perms = Vec::new();
        let mut num = 1;
        for res in &self.resource_types {
            if res.base.is_some() {
                continue;
            }
            let mut first = true;
            for perm in &self.permissions {
                if perm.base_resource != res.name {
                    continue;
                }
                // Is there a hashtag without base permission?
                if first && perm.hashtag.is_some() {
                    let rp = self.make_perm(pri, res, &mut num);
                    perms.push(rp.hashtag());
                    let rp = self.make_perm(pri, res, &mut num);
                    perms.push(rp.missing());
                }
                perms.push(RolePerm::new(perm.clone()));
                if first && perm.hashtag.is_none() {
                    let rp = self.make_perm(pri, res, &mut num);
                    perms.push(rp.hashtag());
                }
                first = false;
            }
            // Missing permissions for this resource
            if first {
                let rp = self.make_perm(pri, res, &mut num);
                perms.push(rp.missing());
            }
        }
        perms
    }

    /// Make permissions HTML table
    fn permissions_html<'p>(&self, pri: &Role, div: &'p mut html::Div<'p>) {
        let mut details = div.details();
        details.summary().cdata("🗝️ Permissions").close();
        let mut table = details.table();
        for rp in self.role_permissions(pri) {
            rp.table_row(&mut table.tr());
        }
        details.close();
    }

    /// Get actions for changed permissions
    fn permissions_changed(&self, pri: &Role) -> Vec<Action> {
        let mut actions = Vec::new();
        for mut rp in self.role_permissions(pri) {
            for act in rp.actions() {
                actions.push(act);
            }
        }
        actions
    }

    /// Make domains HTML table
    fn domains_html<'p>(&self, pri: &Role, div: &'p mut html::Div<'p>) {
        let mut details = div.details();
        details.summary().cdata("🖧 Domains").close();
        if let Some(domains) = pri.domains.as_ref() {
            for (i, dom) in self.domains.iter().enumerate() {
                let mut label = details.label();
                let mut input = label.input();
                input.id(format!("dom_{i}")).r#type("checkbox");
                if domains.contains(&dom.name) {
                    input.checked();
                }
                label.cdata(&dom.name).close();
                details.br().close();
            }
        }
        details.close();
    }

    /// Get selected domains
    fn domains_selected(&self) -> Vec<String> {
        let doc = Doc::get();
        let mut domains = Vec::new();
        for (i, dom) in self.domains.iter().enumerate() {
            let id = format!("dom_{i}");
            if let Some(input) = doc.opt_elem::<HtmlInputElement>(&id)
                && input.checked()
            {
                domains.push(dom.name.to_string());
            }
        }
        domains
    }

    /// Check if domains are different
    fn compare_domains(&self, domains: &[String], sel: &[String]) -> bool {
        if domains.len() != sel.len() {
            return true;
        }
        for dom in sel {
            if !domains.contains(dom) {
                return true;
            }
        }
        false
    }

    /// Get actions to update role domains
    fn domains_changed(&self, pri: &Role) -> Option<Vec<String>> {
        let domains = pri.domains.as_ref()?;
        let sel = self.domains_selected();
        self.compare_domains(&domains[..], &sel[..]).then_some(sel)
    }
}

impl Role {
    /// Get item state
    pub fn item_state(&self) -> ItemState {
        if self.enabled {
            ItemState::Available
        } else {
            ItemState::Prohibited
        }
    }

    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let mut tree = Tree::new();
        tree.root::<html::Div>()
            .class("title row")
            .cdata(self.name())
            .cdata(" ")
            .cdata(self.item_state().to_string());
        String::from(tree)
    }

    /// Convert to Setup HTML
    fn to_html_setup(&self, anc: &RoleAnc, edit: bool) -> String {
        let mut tree = Tree::new();
        self.title(View::Setup(edit), &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row")
            .label()
            .r#for("enabled")
            .cdata("Enabled")
            .close();
        let mut input = div.input();
        input.id("enabled").r#type("checkbox");
        if self.enabled {
            input.checked();
        }
        div.close();
        div = tree.root::<html::Div>();
        anc.permissions_html(self, &mut div);
        div = tree.root::<html::Div>();
        anc.domains_html(self, &mut div);
        footer_html(View::Setup(edit), true, &mut tree.root::<html::Div>());
        String::from(tree)
    }

    /// Get changed fields from Setup form
    fn changed_setup_x(&self, anc: &RoleAnc) -> Option<String> {
        let mut fields = Fields::new();
        fields.changed_input("enabled", self.enabled);
        if let Some(domains) = anc.domains_changed(self) {
            fields.insert_arr("domains", domains);
        }
        if !fields.is_empty() {
            Some(fields.into_value().to_string())
        } else {
            None
        }
    }
}

impl Card for Role {
    type Ancillary = RoleAnc;

    /// Get the resource
    fn res() -> Res {
        Res::Role
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[ItemState::Available, ItemState::Prohibited]
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
    fn is_match(&self, search: &str, _anc: &RoleAnc) -> bool {
        self.name.contains_lower(search) || self.item_state().is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &RoleAnc) -> String {
        match view {
            View::Create => self.to_html_create(15),
            View::Setup(edit) => self.to_html_setup(anc, edit),
            _ => self.to_html_compact(),
        }
    }

    /// Handle click event for the save button
    fn handle_save(&self, anc: Self::Ancillary) -> Vec<Action> {
        let mut actions = Vec::new();
        if let Some(changed) = self.changed_setup_x(&anc) {
            let uri = uri_one(Self::res(), &self.name());
            actions.push(Action::Patch(uri, changed.into()));
        }
        for act in anc.permissions_changed(self) {
            actions.push(act);
        }
        actions
    }

    /// Handle click event for a button on the card
    fn handle_click(&self, anc: RoleAnc, id: &str) -> Vec<Action> {
        if id.starts_with("ht_") && id.ends_with("_btn") {
            if let Some(ht) = id.strip_suffix("_btn") {
                if let Some(ht) = Doc::get().opt_elem::<HtmlElement>(ht) {
                    ht.set_class_name("");
                }
                if let Some(btn) = Doc::get().opt_elem::<HtmlButtonElement>(id)
                {
                    btn.set_disabled(true);
                }
            }
            Vec::new()
        } else {
            self.handle_click_common(anc, id)
        }
    }
}
