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
use crate::util::{ContainsLower, Fields, HtmlStr, OptVal, Select};
use mag::length::mm;
use ntcip::dms::{FontTable, GraphicTable, tfon};
pub use rendzina::SignConfig;
use resources::Res;
use serde::Deserialize;
use std::borrow::Cow;
use wasm_bindgen::JsValue;

/// Display Units
type SizeUnit = mag::length::ft;
type SizeUnitSm = mag::length::In;

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
    /// Build fonts select element
    fn select_fonts_html(&self, font_num: u8) -> String {
        let mut html = String::new();
        html.push_str("<select id='default_font'>");
        for num in 1..=255 {
            if let Some(_f) = self.fonts.font(num) {
                html.push_str("<option");
                if num == font_num {
                    html.push_str(" selected");
                }
                html.push('>');
                html.push_str(&num.to_string());
                html.push_str("</option>");
            }
        }
        html.push_str("</select>");
        html
    }
}

/// Convert to compact HTML
fn to_html_compact(sc: &SignConfig) -> String {
    let name = HtmlStr::new(&sc.name);
    let item_states = item_states(sc);
    format!("<div class='title row'>{name} {item_states}</div>")
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
fn to_html_setup(
    sc: &SignConfig,
    anc: &SignConfigAnc,
    title: &str,
    footer: &str,
) -> String {
    let color_scheme = &sc.color_scheme;
    let monochrome = monochrome_html(sc);
    let pixel_width = sc.pixel_width;
    let pixel_height = sc.pixel_height;
    let pitch_horiz = (f64::from(sc.pitch_horiz) * mm).to::<SizeUnitSm>();
    let pitch_vert = (f64::from(sc.pitch_vert) * mm).to::<SizeUnitSm>();
    let char_width = if sc.char_width > 0 {
        format!("{} px", sc.char_width)
    } else {
        "variable".to_string()
    };
    let char_height = if sc.char_height > 0 {
        format!("{} px", sc.char_height)
    } else {
        "variable".to_string()
    };
    let module_width =
        select_factors_html("module_width", sc.pixel_width, sc.module_width);
    let module_height =
        select_factors_html("module_height", sc.pixel_height, sc.module_height);
    let default_font = anc.select_fonts_html(sc.default_font);
    let sign = render_sign(sc, anc);
    format!(
        "{title}\
        <div class='row'>\
          <label>Color Scheme</label>\
          <span class='info'>{color_scheme}</span>\
        </div>\
        {monochrome}\
        <div class='center info'>{pixel_width} x {pixel_height} px</div>\
        <div class='center'>{sign}</div>\
        <div class='row'>\
          <label>Pitch</label>\
          <span class='info'>{pitch_horiz:.2} x {pitch_vert:.2}</span>\
        </div>\
        <div class='row'>\
          <label>Character Width</label><span class='info'>{char_width}</span>\
          <label>x Height</label><span class='info'>{char_height}</span>\
        </div>\
        <div class='row'>\
          <label for='module_width'>Module Width</label>{module_width}\
          <label for='module_height'>x Height</label>{module_height}\
        </div>\
        <div class='row'>\
          <label for='default_font'>Default Font</label>{default_font}\
        </div>\
        {footer}"
    )
}

/// Make monochrome color div element
fn monochrome_html(sc: &SignConfig) -> String {
    let fg = sc.monochrome_foreground;
    let bg = sc.monochrome_background;
    if fg > 0 || bg > 0 {
        format!(
            "<div class='row'>\
              <label>FG / BG</label>\
              <span style='color: #{fg:06X}; background-color: #{bg:06X}'>\
                #{fg:06X} / #{bg:06X}\
              </span>\
            </div>"
        )
    } else {
        String::new()
    }
}

/// Render the sign
fn render_sign(sc: &SignConfig, anc: &SignConfigAnc) -> String {
    let face_width = (f64::from(sc.face_width) * mm).to::<SizeUnit>();
    let face_height = (f64::from(sc.face_height) * mm).to::<SizeUnit>();
    let border_horiz = (f64::from(sc.border_horiz) * mm).to::<SizeUnitSm>();
    let border_vert = (f64::from(sc.border_vert) * mm).to::<SizeUnitSm>();
    let sign = NtcipSign::new(sc, anc.fonts.clone(), GraphicTable::default());
    let valid = match sign {
        Some(_) => "<td>",
        None => "<td class='fault'>Invalid",
    };
    let mod_size = match (sc.module_width, sc.module_height) {
        (Some(mw), Some(mh)) if mw > 0 && mh > 0 => {
            Some((mw as u32, mh as u32))
        }
        _ => None,
    };
    let html = sign::render(&sign, "A1", 240, 80, mod_size);
    format!(
        "<table>\
          <tr>\
            <td>\
            <td style='text-align: center;'>{face_width:.2}\
            {valid}\
          <tr>\
            <td style='text-align: right;'>{face_height:.2}\
            <td>{html}\
            <td style='vertical-align: bottom;'>{border_vert:.2}\
          <tr>\
            <td>\
            <td style='text-align: right;'>{border_horiz:.2}\
            <td><span style='color:#116;'>(border)</span>\
        </table>"
    )
}

/// Create an HTML `select` element of comm configs
fn select_factors_html(id: &str, max: i32, value: Option<i32>) -> String {
    let mut html = String::new();
    html.push_str("<select id='");
    html.push_str(id);
    html.push_str("'>");
    for fact in std::iter::once(None).chain(factor::unique(max).map(Some)) {
        html.push_str("<option");
        if value == fact {
            html.push_str(" selected");
        }
        html.push('>');
        html.push_str(&OptVal(fact).to_string());
        html.push_str("</option>");
    }
    html.push_str("</select>");
    html
}

impl Card for SignConfig {
    type Ancillary = SignConfigAnc;

    /// Display name
    const DNAME: &'static str = "📐 Sign Config";

    /// All item states as html options
    const ITEM_STATES: &'static str = "<option value=''>all ↴\
         <option value='🔹'>🔹 available\
         <option value='🔺'>🔺 inactive";

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
            View::Setup => {
                let title = self.title(View::Setup).build();
                let footer = self.footer(true);
                to_html_setup(self, anc, &title, &footer)
            }
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
