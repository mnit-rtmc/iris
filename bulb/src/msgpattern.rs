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
use crate::card::{AncillaryData, Card, View};
use crate::dms::FontName;
use crate::error::Result;
use crate::fetch::Action;
use crate::item::{ItemState, ItemStates};
use crate::rend::Renderer;
use crate::util::{
    ContainsLower, Doc, Fields, Input, Select, TextArea, opt_ref, opt_str,
};
use hatmil::{Page, html};
use ntcip::dms::multi::{
    join_text, normalize as multi_normalize, split as multi_split,
};
use ntcip::dms::{FontTable, tfon};
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use std::cmp::Ordering;
use wasm_bindgen::JsValue;
use web_sys::HtmlElement;

/// NTCIP sign
type NtcipDms = ntcip::dms::Dms<256, 24, 32>;

/// Ancillary message pattern data
#[derive(Default)]
pub struct MsgPatternAnc {
    assets: Vec<Asset>,
    fonts: FontTable<256, 24>,
}

/// Message Pattern
#[derive(Debug, Default, Deserialize, PartialEq, Eq)]
pub struct MsgPattern {
    pub name: String,
    pub compose_hashtag: Option<String>,
    pub multi: String,
    // secondary attributes
    pub flash_beacon: Option<bool>,
    pub pixel_service: Option<bool>,
}

impl AncillaryData for MsgPatternAnc {
    type Primary = MsgPattern;

    /// Construct ancillary message pattern data
    fn new(_pri: &MsgPattern, view: View) -> Self {
        let mut assets = Vec::new();
        if let View::Setup = view {
            assets.push(Asset::Fonts);
        }
        MsgPatternAnc {
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
        _pri: &MsgPattern,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
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

impl MsgPatternAnc {
    /// Build fonts HTML
    fn select_fonts_html<'p>(
        &self,
        font_num: u8,
        select: &'p mut html::Select<'p>,
    ) {
        select.id("default_font");
        for num in 1..=255 {
            if let Some(_f) = self.fonts.font(num) {
                let mut option = select.option();
                if num == font_num {
                    option.selected();
                }
                option.cdata(num).close();
            }
        }
        select.close();
    }

    /// Make an NTCIP sign
    fn make_dms(&self, pri: &MsgPattern) -> Option<NtcipDms> {
        None
        // FIXME: find sign config...
        /*
        NtcipDms::builder()
            .with_font_definition(self.fonts.clone())
            .with_sign_cfg(sc.sign_cfg())
            .with_vms_cfg(sc.vms_cfg())
            .with_multi_cfg(sc.multi_cfg())
            .build()
            .ok()*/
    }
}

impl PartialOrd for MsgPattern {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        Some(self.cmp(other))
    }
}

impl Ord for MsgPattern {
    fn cmp(&self, other: &Self) -> Ordering {
        if self == other {
            return Ordering::Equal;
        }
        // prefer patterns which can be conbined (shared)
        let self_combine = self.can_combine_shared_second();
        let other_combine = other.can_combine_shared_second();
        if self_combine && !other_combine {
            return Ordering::Less;
        } else if other_combine && !self_combine {
            return Ordering::Greater;
        }
        let len_ord = self.multi.len().cmp(&other.multi.len());
        if len_ord != Ordering::Equal {
            return len_ord;
        }
        let ms_ord = self.multi.cmp(&other.multi);
        if ms_ord != Ordering::Equal {
            ms_ord
        } else {
            self.name.cmp(&other.name)
        }
    }
}

impl MsgPattern {
    // Check if pattern can combine (shared) in second position
    fn can_combine_shared_second(&self) -> bool {
        let mut it = multi_split(&self.multi);
        // check that:
        // - the first value is a text rectangle
        // - the same text rectangle starts every page
        // - there are no other text rectangles
        if let Some(first) = it.next()
            && first.starts_with("[tr")
        {
            let mut tr_this_page = true;
            for val in it {
                if tr_this_page {
                    if val.starts_with("[tr") {
                        return false;
                    } else if val == "[np]" {
                        tr_this_page = false;
                    }
                } else if val == first {
                    tr_this_page = true;
                } else {
                    return false;
                }
            }
            return tr_this_page;
        }
        false
    }

    /// Get item states
    fn item_states(&self, _anc: &MsgPatternAnc) -> ItemStates<'static> {
        let mut states = ItemStates::default();
        if self.compose_hashtag.is_some() {
            states = states.with(ItemState::Available, "");
        }
        // FIXME: Planned : if device actions use pattern
        // FIXME: Fault : if pattern doesn't fit on a sign config
        states
    }

    /// Convert to compact HTML
    fn to_html_compact(&self, anc: &MsgPatternAnc) -> String {
        let mut page = Page::new();
        let mut div = page.frag::<html::Div>();
        div.class("title row")
            .cdata(&self.name)
            .cdata(" ")
            .cdata(self.item_states(anc).to_string());
        String::from(page)
    }

    /// Convert to setup HTML
    fn to_html_setup(&self, anc: &MsgPatternAnc) -> String {
        let mut page = Page::new();
        self.title(View::Setup, &mut page.frag::<html::Div>());
        let mut div = page.frag::<html::Div>();
        div.class("center");
        self.render_sign(anc, &mut div.div());
        div.close();
        div = page.frag::<html::Div>();
        div.class("sb_row_left");
        div.input()
            .id("tab_wysiwyg")
            .class("toggle")
            .r#type("radio")
            .name("pattern_tab")
            .checked();
        div.label().r#for("tab_wysiwyg").cdata("WYSIWYG").close();
        div.input()
            .id("tab_multi")
            .class("toggle")
            .r#type("radio")
            .name("pattern_tab");
        div.label().r#for("tab_multi").cdata("MULTI").close();
        div.close();
        div = page.frag::<html::Div>();
        div.id("div_wysiwyg").class("row");
        div.close();
        div = page.frag::<html::Div>();
        div.id("div_multi").class("row hidden");
        div.textarea()
            .id("multi")
            .autocorrect("off")
            .autocomplete("off")
            .spellcheck("false")
            .style("word-break: break-all; font-family: monospace;")
            .maxlength(1024)
            .rows(5)
            .cols(30)
            .cdata(&self.multi)
            .close();
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("compose_hashtag")
            .cdata("Compose Hashtag")
            .close();
        div.input()
            .id("compose_hashtag")
            .maxlength(16)
            .size(16)
            .value(opt_ref(&self.compose_hashtag));
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("flash_beacon")
            .cdata("Flash Beacon")
            .close();
        let mut input = div.input();
        input.id("flash_beacon").r#type("checkbox");
        if let Some(true) = self.flash_beacon {
            input.checked();
        }
        div.close();
        div = page.frag::<html::Div>();
        div.class("row");
        div.label()
            .r#for("pixel_service")
            .cdata("Pixel Service")
            .close();
        let mut input = div.input();
        input.id("pixel_service").r#type("checkbox");
        if let Some(true) = self.pixel_service {
            input.checked();
        }
        div.close();
        self.footer_html(true, &mut page.frag::<html::Div>());
        String::from(page)
    }

    /// Render the message pattern image HTML
    fn render_sign<'p>(&self, anc: &MsgPatternAnc, div: &'p mut html::Div<'p>) {
        // FIXME
        /*
        let dms = anc.make_dms(mp);
        if let Some(dms) = &dms {
            let mut rend = Renderer::new()
                .with_dms(dms)
                .with_max_width(240)
                .with_max_height(80)
                .with_mod_size(mod_size);
            rend.render_multi("A1", &mut td.img());
        }*/
    }
}

impl Card for MsgPattern {
    type Ancillary = MsgPatternAnc;

    /// Get the resource
    fn res() -> Res {
        Res::MsgPattern
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[ItemState::Available, ItemState::Planned, ItemState::Fault]
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
    fn is_match(&self, search: &str, anc: &MsgPatternAnc) -> bool {
        self.name.contains_lower(search)
            || self.item_states(anc).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &MsgPatternAnc) -> String {
        match view {
            View::Setup => self.to_html_setup(anc),
            _ => self.to_html_compact(anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_text_area("multi", &self.multi);
        fields.changed_input("flash_beacon", self.flash_beacon);
        fields.changed_input("pixel_service", self.pixel_service);
        fields.into_value().to_string()
    }

    /// Handle input event for an element on the card
    fn handle_input(&self, anc: MsgPatternAnc, id: String) -> Vec<Action> {
        let tab = match id.as_str() {
            "tab_wysiwyg" => Some(true),
            "tab_multi" => Some(false),
            _ => None,
        };
        if let Some(wysiwyg) = tab {
            let doc = Doc::get();
            doc.elem::<HtmlElement>("div_wysiwyg")
                .set_class_name(row_class(wysiwyg));
            doc.elem::<HtmlElement>("div_multi")
                .set_class_name(row_class(!wysiwyg));
        }
        Vec::new()
    }
}

/// Get class name for a row
fn row_class(show: bool) -> &'static str {
    if show { "row" } else { "row hidden" }
}
