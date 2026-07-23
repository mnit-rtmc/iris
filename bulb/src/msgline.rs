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
use crate::card::{AncillaryData, Card, footer_html, uri_all};
use crate::eid;
use crate::error::Result;
use crate::fetch::Action;
use crate::item::{ItemState, ItemStates};
use crate::msgpattern::FontName;
use crate::notes::contains_hashtag;
use crate::rend::{Renderer, replace_action_tags};
use crate::signconfig::NtcipDms;
use crate::util::{ContainsLower, Doc, Fields, Input, opt_str};
use crate::view::View;
use hatmil::{Tree, html};
use ntcip::dms::{FontTable, tfon};
use rendzina::SignConfig;
use resources::Res;
use serde::Deserialize;
use serde_json::Value;
use serde_json::map::Map;
use std::borrow::Cow;
use wasm_bindgen::JsValue;
use web_sys::{HtmlElement, HtmlInputElement, HtmlSelectElement};

/// Message Line
#[derive(Debug, Default, Deserialize, PartialEq, Eq, PartialOrd, Ord)]
pub struct MsgLine {
    // NOTE: ordered to allow deriving PartialOrd / Ord
    pub line: u16,
    // NOTE: secondary
    pub rank: Option<u16>,
    pub multi: String,
    pub hashtag: String,
    pub name: String,
}

/// Hashtag sign configs
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct HashtagSignCfg {
    hashtag: String,
    sign_config: String,
}

/// Ancillary event configuration data
#[derive(Default)]
pub struct MsgLineAnc {
    assets: Vec<Asset>,
    sign_cfgs: Vec<String>,
    configs: Vec<SignConfig>,
    fonts: FontTable<256, 24>,
}

impl AncillaryData for MsgLineAnc {
    type Primary = MsgLine;

    /// Construct ancillary message line data
    fn new(_pri: &MsgLine, view: View) -> Self {
        let assets = match view {
            View::Setup(_edit) => {
                vec![Asset::HashtagSignCfgs, Asset::Fonts]
            }
            _ => vec![],
        };
        MsgLineAnc {
            assets,
            ..Default::default()
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &MsgLine,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::HashtagSignCfgs => {
                let mut cfgs: Vec<HashtagSignCfg> =
                    serde_wasm_bindgen::from_value(value)?;
                cfgs.retain(|c| contains_hashtag(&c.hashtag, &pri.hashtag));
                self.sign_cfgs =
                    cfgs.into_iter().map(|c| c.sign_config).collect();
                self.assets.push(Asset::SignConfigs);
            }
            Asset::SignConfigs => {
                let mut cfgs: Vec<SignConfig> =
                    serde_wasm_bindgen::from_value(value)?;
                cfgs.retain(|sc| self.sign_cfgs.contains(&sc.name));
                self.configs = cfgs;
            }
            Asset::Fonts => {
                let fnames: Vec<FontName> =
                    serde_wasm_bindgen::from_value(value)?;
                for fname in fnames {
                    self.assets.push(Asset::Font(fname.name));
                }
            }
            Asset::Font(_nm) => {
                let font: String = serde_wasm_bindgen::from_value(value)?;
                let font = tfon::read(font.as_bytes())?;
                if let Some(f) = self.fonts.font_mut(font.number) {
                    *f = font;
                } else if let Some(f) = self.fonts.font_mut(0) {
                    *f = font;
                }
            }
            _ => unreachable!(),
        }
        Ok(())
    }
}

impl MsgLineAnc {
    /// Find a sign config
    fn sign_config(&self, cfg: Option<&String>) -> Option<&SignConfig> {
        cfg.and_then(|cfg| self.configs.iter().find(|c| c.name == *cfg))
    }

    /// Get selected sign configuration
    fn selected_config(&self) -> Option<String> {
        if let Some(el) = Doc::get().opt_elem::<HtmlSelectElement>("ml_config")
        {
            return Some(el.value());
        }
        self.configs.first().map(|sc| sc.name.clone())
    }

    /// Make an NTCIP sign
    fn make_dms(&self, sc: &SignConfig) -> Option<NtcipDms> {
        NtcipDms::builder()
            .with_font_definition(self.fonts.clone())
            .with_sign_cfg(sc.sign_cfg())
            .with_vms_cfg(sc.vms_cfg())
            .with_multi_cfg(sc.multi_cfg())
            .build()
            .ok()
    }
}

impl MsgLine {
    /// Get item states
    fn item_states(&self) -> ItemStates<'static> {
        ItemState::Available.into()
    }

    /// Get entered MULTI string
    fn multi_string(&self) -> String {
        match Doc::get().opt_elem::<HtmlInputElement>("multi") {
            Some(el) => el.value(),
            None => self.multi.clone(),
        }
    }

    /// Get MULTI string with action tags replaced with a filler character
    fn multi(&self) -> String {
        replace_action_tags(&self.multi_string())
    }

    /// Make a select element for sign configs
    fn configs_select<'p>(
        &self,
        anc: &MsgLineAnc,
        select: &'p mut html::Select<'p>,
    ) {
        select.id("ml_config").size(4);
        let sc = anc.sign_cfgs.first();
        for cfg in &anc.sign_cfgs {
            let mut option = select.option();
            if sc == Some(cfg) {
                option.selected();
            }
            option.cdata(cfg).close();
        }
        select.close();
    }

    /// Render the message pattern image preview
    fn render_preview<'p>(&self, anc: &MsgLineAnc, img: &'p mut html::Img<'p>) {
        img.id("ml_preview");
        let sc = anc.selected_config();
        if let Some(cfg) = anc.sign_config(sc.as_ref())
            && let Some(dms) = &anc.make_dms(cfg)
        {
            let mut rend =
                Renderer::new().with_dms(dms).with_alt("FAILED TO RENDER");
            rend.render_multi(&self.multi(), img);
            return;
        }
        img.alt("BAD CONFIGURATION");
    }

    /// Replace preview image
    fn replace_preview(&self, anc: &MsgLineAnc) {
        if let Some(el) = Doc::get().opt_elem::<HtmlElement>("ml_preview") {
            let mut tree = Tree::new();
            let mut img = tree.root::<html::Img>();
            self.render_preview(anc, &mut img);
            el.set_outer_html(&String::from(tree));
        }
    }

    /// Convert to compact HTML
    fn to_html_compact(&self) -> String {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.class("title row").cdata(&self.multi);
        String::from(tree)
    }

    /// Convert to setup HTML
    fn to_html_setup(&self, anc: &MsgLineAnc, edit: bool) -> String {
        let mut tree = Tree::new();
        self.title(View::Setup(edit), &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row");
        div.span().class("info fill").cdata(&self.hashtag);
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("line").cdata("Ln").close();
        div.input()
            .id("line")
            .r#type("number")
            .min(1)
            .max(12)
            .size(2)
            .value(self.line);
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("rank").cdata("Rank").close();
        div.input()
            .id("rank")
            .r#type("number")
            .min(1)
            .max(99)
            .size(2)
            .value(opt_str(self.rank));
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for("multi").cdata("MULTI").close();
        div.input()
            .id("multi")
            .class("multi")
            .autocorrect("off")
            .autocomplete("off")
            .spellcheck("false")
            .maxlength(64)
            .size(24)
            .value(&self.multi)
            .close();
        div.close();
        let mut fs = tree.root::<html::FieldSet>();
        let mut legend = fs.legend();
        legend
            .input()
            .id("tab_preview")
            .class("toggle")
            .r#type("radio")
            .name("pattern_tab")
            .checked();
        legend.label().r#for("tab_preview").cdata("Preview").close();
        legend.close();
        div = fs.div();
        div.id("ml_preview_div");
        let mut div2 = div.div();
        div2.class("row");
        self.configs_select(anc, &mut div2.select());
        self.render_preview(anc, &mut div2.img());
        div.close();
        fs.close();
        footer_html(View::Setup(edit), true, &mut tree.root::<html::Div>());
        String::from(tree)
    }
}

impl Card for MsgLine {
    type Ancillary = MsgLineAnc;

    /// Suggested name prefix
    const PREFIX: &'static str = "ml";

    /// Get the resource
    fn res() -> Res {
        Res::MsgLine
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[ItemState::Available]
    }

    /// Handle click event for the create button
    fn handle_create() -> Vec<Action> {
        let doc = Doc::get();
        if let (Some(name), Some(hashtag)) = (
            doc.input_option_string(eid::NAME),
            doc.input_option_string("ob_hashtag"),
        ) {
            let mut obj = Map::new();
            obj.insert("name".to_string(), Value::String(name));
            obj.insert("hashtag".to_string(), Value::String(hashtag));
            obj.insert("line".to_string(), Value::Number(1.into()));
            obj.insert("multi".to_string(), Value::String(String::new()));
            obj.insert("rank".to_string(), Value::Number(50.into()));
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
    fn item_state_main(&self, _anc: &Self::Ancillary) -> ItemState {
        ItemState::Available
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, _anc: &MsgLineAnc) -> bool {
        self.name.contains_lower(search)
            || self.item_states().is_match(search)
            || self.hashtag.contains_lower(search)
            || self.multi.contains_lower(search)
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
        div.label().r#for("ob_hashtag").cdata("#Tag").close();
        div.input()
            .id("ob_hashtag")
            .maxlength(16)
            .size(16)
            .value("#");
        String::from(tree)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &MsgLineAnc) -> String {
        match view {
            View::Create => self.to_html_create(10),
            View::Setup(edit) => self.to_html_setup(anc, edit),
            _ => self.to_html_compact(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_input("line", self.line);
        fields.changed_input("rank", self.rank);
        fields.changed_input("multi", &self.multi);
        fields.into_value().to_string()
    }

    /// Handle input event for an element on the card
    fn handle_input(&self, anc: MsgLineAnc, id: &str) -> Vec<Action> {
        if "ml_config" == id || "multi" == id {
            self.replace_preview(&anc);
        }
        Vec::new()
    }
}
