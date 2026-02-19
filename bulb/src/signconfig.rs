// Copyright (C) 2024-2026  Minnesota Department of Transportation
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
use crate::error::Result;
use crate::factor;
use crate::item::{ItemState, ItemStates};
use crate::msgpattern::FontName;
use crate::rend::Renderer;
use crate::util::{ContainsLower, Fields, Select, opt_str};
use hatmil::{Page, html};
use mag::length::mm;
use ntcip::dms::{FontTable, tfon};
pub use rendzina::SignConfig;
use resources::Res;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// NTCIP sign
type NtcipDms = ntcip::dms::Dms<256, 24, 32>;

/// Length display units
type LenUnit = mag::length::ft;
type LenUnitSm = mag::length::In;

/// Format length to "normal" units (feet)
fn format_len(val: i32) -> String {
    format!("{:.2}", (f64::from(val) * mm).to::<LenUnit>())
}

/// Format length to "small" units (inches)
fn format_len_sm(val: i32) -> String {
    format!("{:.2}", (f64::from(val) * mm).to::<LenUnitSm>())
}

/// Format pixel width/height
fn format_px(val: i32) -> String {
    if val > 0 {
        format!("{val} px")
    } else {
        "variable".to_string()
    }
}

/// Ancillary sign configuration
#[derive(Default)]
pub struct SignConfigAnc {
    assets: Vec<Asset>,
    fonts: FontTable<256, 24>,
}

impl AncillaryData for SignConfigAnc {
    type Primary = SignConfig;

    /// Construct ancillary sign config data
    fn new(_pri: &SignConfig, view: View) -> Self {
        let mut assets = Vec::new();
        if let View::Setup = view {
            assets.push(Asset::Fonts);
        }
        SignConfigAnc {
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
        _pri: &SignConfig,
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

impl SignConfigAnc {
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

/// Get item states
fn item_states(sc: &SignConfig, anc: &SignConfigAnc) -> ItemStates<'static> {
    let mut states = ItemStates::default();
    states = if sc.sign_count > 0 {
        states.with(ItemState::Available, "")
    } else {
        states.with(ItemState::Inactive, "")
    };
    if anc.make_dms(sc).is_none() {
        states = states.with(ItemState::Fault, "Invalid");
    }
    states
}

/// Convert to compact HTML
fn to_html_compact(sc: &SignConfig, anc: &SignConfigAnc) -> String {
    let mut page = Page::new();
    let mut div = page.frag::<html::Div>();
    div.class("title row")
        .cdata(&sc.name)
        .cdata(" ")
        .cdata(item_states(sc, anc).to_string());
    String::from(page)
}

/// Convert to setup HTML
fn to_html_setup(sc: &SignConfig, anc: &SignConfigAnc) -> String {
    let mut page = Page::new();
    sc.title(View::Setup, &mut page.frag::<html::Div>());
    let mut div = page.frag::<html::Div>();
    div.class("row");
    div.label().cdata("Color Scheme").close();
    div.span().class("info").cdata(&sc.color_scheme).close();
    div.close();
    monochrome_html(sc, &mut page.frag::<html::Div>());
    div = page.frag::<html::Div>();
    div.class("center info")
        .cdata(sc.pixel_width)
        .cdata(" x ")
        .cdata(sc.pixel_height)
        .cdata(" px")
        .close();
    div = page.frag::<html::Div>();
    div.class("center");
    render_sign(sc, anc, &mut div.table());
    div.close();
    div = page.frag::<html::Div>();
    div.class("row").label().cdata("Pitch").close();
    div.span()
        .class("info")
        .cdata(format_len_sm(sc.pitch_horiz))
        .cdata(" x ")
        .cdata(format_len_sm(sc.pitch_vert))
        .close();
    div.close();
    div = page.frag::<html::Div>();
    div.class("row").label().cdata("Character Width").close();
    div.span()
        .class("info")
        .cdata(format_px(sc.char_width))
        .close();
    div.label().cdata("x Height").close();
    div.span()
        .class("info")
        .cdata(format_px(sc.char_height))
        .close();
    div.close();
    div = page.frag::<html::Div>();
    div.class("row")
        .label()
        .r#for("module_width")
        .cdata("Module Width")
        .close();
    select_factors_html(
        "module_width",
        sc.pixel_width,
        sc.module_width,
        &mut div.select(),
    );
    div.label()
        .r#for("module_height")
        .cdata("Module Height")
        .close();
    select_factors_html(
        "module_height",
        sc.pixel_height,
        sc.module_height,
        &mut div.select(),
    );
    div.close();
    div = page.frag::<html::Div>();
    div.class("row")
        .label()
        .r#for("default_font")
        .cdata("Default Font")
        .close();
    anc.select_fonts_html(sc.default_font, &mut div.select());
    div.close();
    sc.footer_html(true, &mut page.frag::<html::Div>());
    String::from(page)
}

/// Build monochrome color HTML
fn monochrome_html<'p>(sc: &SignConfig, div: &'p mut html::Div<'p>) {
    let fg = sc.monochrome_foreground;
    let bg = sc.monochrome_background;
    if fg > 0 || bg > 0 {
        div.class("row");
        div.label().cdata("FG / BG").close();
        div.span()
            .style(format!("color: #{fg:06X}; background-color: #{bg:06X}"))
            .cdata(format!("#{fg:06X} / #{bg:06X}"))
            .close();
    }
    div.close();
}

/// Render the sign HTML
fn render_sign<'p>(
    sc: &SignConfig,
    anc: &SignConfigAnc,
    table: &'p mut html::Table<'p>,
) {
    let dms = anc.make_dms(sc);
    // make a 3x3 cell table
    let mut tr = table.tr();
    tr.td().close(); // empty cell
    tr.td()
        .style("text-align: center;")
        .cdata(format_len(sc.face_width))
        .close();
    if dms.is_none() {
        tr.td().class("fault").cdata("Invalid").close();
    } else {
        tr.td().close(); // empty cell
    }
    tr.close();
    tr = table.tr();
    tr.td()
        .style("text-align: right;")
        .cdata(format_len(sc.face_height))
        .close();
    let mod_size = match (sc.module_width, sc.module_height) {
        (Some(mw), Some(mh)) if mw > 0 && mh > 0 => {
            Some((mw as u32, mh as u32))
        }
        _ => None,
    };
    let mut td = tr.td();
    let mut img = td.img();
    match &dms {
        Some(dms) => {
            let mut rend = Renderer::new()
                .with_dms(dms)
                .with_max_width(240)
                .with_max_height(80)
                .with_mod_size(mod_size);
            rend.render_multi("A1", &mut img);
        }
        None => {
            img.width(240).height(80);
        }
    }
    td.close();
    tr.td()
        .style("vertical-align: bottom;")
        .cdata("↤")
        .cdata(format_len_sm(sc.border_horiz))
        .close();
    tr.close();
    tr = table.tr();
    tr.td().close(); // empty cell
    tr.td()
        .style("text-align: right;")
        .cdata(format_len_sm(sc.border_vert))
        .cdata("↥")
        .close();
    tr.td()
        .style("text-align: left; color:#116;")
        .cdata("(border)");
    table.close();
}

/// Build factors HTML
fn select_factors_html<'p>(
    id: &str,
    max: i32,
    value: Option<i32>,
    select: &'p mut html::Select<'p>,
) {
    select.id(id);
    for fact in std::iter::once(None).chain(factor::unique(max).map(Some)) {
        let mut option = select.option();
        if value == fact {
            option.selected();
        }
        option.cdata(opt_str(fact)).close();
    }
    select.close();
}

impl Card for SignConfig {
    type Ancillary = SignConfigAnc;

    /// Get the resource
    fn res() -> Res {
        Res::SignConfig
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[ItemState::Available, ItemState::Fault, ItemState::Inactive]
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
        let states = item_states(self, anc);
        if states.contains(ItemState::Inactive) {
            ItemState::Inactive
        } else if states.contains(ItemState::Fault) {
            ItemState::Fault
        } else {
            ItemState::Available
        }
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &SignConfigAnc) -> bool {
        self.name.contains_lower(search)
            || item_states(self, anc).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &SignConfigAnc) -> String {
        match view {
            View::Setup => to_html_setup(self, anc),
            _ => to_html_compact(self, anc),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_select("module_width", self.module_width);
        fields.changed_select("module_height", self.module_height);
        fields.changed_select("default_font", self.default_font);
        fields.into_value().to_string()
    }
}
