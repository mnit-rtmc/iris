// Copyright (C) 2024-2025  Minnesota Department of Transportation
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
use crate::sign::{self, NtcipSign};
use crate::util::{ContainsLower, Fields, Select, opt_str};
use hatmil::Html;
use mag::length::mm;
use ntcip::dms::{FontTable, GraphicTable, tfon};
pub use rendzina::SignConfig;
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

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
        format!("{} px", val)
    } else {
        "variable".to_string()
    }
}

/// Font name
/// FIXME: share with dms module
#[derive(Debug, Default, Deserialize)]
#[allow(dead_code)]
pub struct FontName {
    pub font_number: u8,
    pub name: String,
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
    fn select_fonts_html(&self, font_num: u8, html: &mut Html) {
        html.select().id("default_font");
        for num in 1..=255 {
            if let Some(_f) = self.fonts.font(num) {
                let option = html.option();
                if num == font_num {
                    option.attr_bool("selected");
                }
                html.text(num.to_string()).end();
            }
        }
        html.end(); /* select */
    }
}

/// Convert to compact HTML
fn to_html_compact(sc: &SignConfig) -> String {
    let mut html = Html::new();
    html.div()
        .class("title row")
        .text(&sc.name)
        .text(" ")
        .text(item_states(sc).to_string());
    html.into()
}

/// Get item states
fn item_states(sc: &SignConfig) -> ItemStates {
    if sc.sign_count > 0 {
        ItemState::Available.into()
    } else {
        ItemState::Inactive.into()
    }
}

/// Convert to setup HTML
fn to_html_setup(sc: &SignConfig, anc: &SignConfigAnc) -> String {
    let mut html = sc.title(View::Setup);
    html.div().class("row");
    html.label().text("Color Scheme").end();
    html.span().class("info").text(&sc.color_scheme).end();
    html.end(); /* div */
    monochrome_html(sc, &mut html);
    html.div()
        .class("center info")
        .text(sc.pixel_width.to_string())
        .text(" x ")
        .text(sc.pixel_height.to_string())
        .text(" px")
        .end();
    html.div().class("center");
    render_sign(sc, anc, &mut html);
    html.end(); /* div */
    html.div().class("row");
    html.label().text("Pitch").end();
    html.span()
        .class("info")
        .text(format_len_sm(sc.pitch_horiz))
        .text(" x ")
        .text(format_len_sm(sc.pitch_vert))
        .end();
    html.end(); /* div */
    html.div().class("row");
    html.label().text("Character Width").end();
    html.span()
        .class("info")
        .text(format_px(sc.char_width))
        .end();
    html.label().text("x Height").end();
    html.span()
        .class("info")
        .text(format_px(sc.char_height))
        .end();
    html.end(); /* div */
    html.div().class("row");
    html.label().for_("module_width").text("Module Width").end();
    select_factors_html(
        "module_width",
        sc.pixel_width,
        sc.module_width,
        &mut html,
    );
    html.label()
        .for_("module_height")
        .text("Module Height")
        .end();
    select_factors_html(
        "module_height",
        sc.pixel_height,
        sc.module_height,
        &mut html,
    );
    html.end(); /* div */
    html.div().class("row");
    html.label().for_("default_font").text("Default Font").end();
    anc.select_fonts_html(sc.default_font, &mut html);
    html.end(); /* div */
    sc.footer_html(true, &mut html);
    html.into()
}

/// Build monochrome color HTML
fn monochrome_html(sc: &SignConfig, html: &mut Html) {
    let fg = sc.monochrome_foreground;
    let bg = sc.monochrome_background;
    if fg > 0 || bg > 0 {
        let style = format!("color: #{fg:06X}; background-color: #{bg:06X}");
        let text = format!("#{fg:06X} / #{bg:06X}");
        html.div().class("row");
        html.label().text("FG / BG").end();
        html.span().attr("style", style).text(text).end();
        html.end(); /* div */
    }
}

/// Render the sign HTML
fn render_sign(sc: &SignConfig, anc: &SignConfigAnc, html: &mut Html) {
    let sign = NtcipSign::new(sc, anc.fonts.clone(), GraphicTable::default());
    html.table();
    html.tr().td().end();
    html.td()
        .attr("style", "text-align: center;")
        .text(format_len(sc.face_width))
        .end();
    let td = html.td();
    if sign.is_none() {
        td.class("fault").text("Invalid");
    }
    html.end().end(); /* td; tr */
    html.tr()
        .td()
        .attr("style", "text-align: right;")
        .text(format_len(sc.face_height))
        .end();
    html.td();
    let mod_size = match (sc.module_width, sc.module_height) {
        (Some(mw), Some(mh)) if mw > 0 && mh > 0 => {
            Some((mw as u32, mh as u32))
        }
        _ => None,
    };
    html.raw(sign::render(&sign, "A1", 240, 80, mod_size));
    html.end(); /* td */
    html.td()
        .attr("style", "vertical-align: bottom;")
        .text(format_len_sm(sc.border_horiz))
        .end();
    html.tr().td().end();
    html.td()
        .attr("style", "text-align: right;")
        .text(format_len_sm(sc.border_vert))
        .end();
    html.td()
        .span()
        .attr("style", "color:#116")
        .text("(border)")
        .end();
    html.end().end().end(); /* td; tr; table */
}

/// Build factors HTML
fn select_factors_html(
    id: &str,
    max: i32,
    value: Option<i32>,
    html: &mut Html,
) {
    html.select().id(id);
    for fact in std::iter::once(None).chain(factor::unique(max).map(Some)) {
        let option = html.option();
        if value == fact {
            option.attr_bool("selected");
        }
        html.text(opt_str(fact)).end();
    }
    html.end(); /* select */
}

impl Card for SignConfig {
    type Ancillary = SignConfigAnc;

    /// Display name
    const DNAME: &'static str = "📐 Sign Config";

    /// Get the resource
    fn res() -> Res {
        Res::SignConfig
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
    fn is_match(&self, search: &str, _anc: &SignConfigAnc) -> bool {
        self.name.contains_lower(search) || item_states(self).is_match(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &SignConfigAnc) -> String {
        match view {
            View::Compact => to_html_compact(self),
            View::Setup => to_html_setup(self, anc),
            _ => unreachable!(),
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
