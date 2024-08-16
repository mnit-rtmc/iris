// Copyright (C) 2024  Minnesota Department of Transportation
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
use crate::card::{AncillaryData, Card, View};
use crate::factor;
use crate::util::{ContainsLower, Fields, HtmlStr, OptVal, Select};
use mag::length::mm;
pub use rendzina::SignConfig;
use resources::Res;
use std::borrow::Cow;

/// Display Units
type SizeUnit = mag::length::ft;
type SizeUnitSm = mag::length::In;

/// Ancillary sign configuration
#[derive(Debug, Default)]
pub struct SignConfigAnc;

impl AncillaryData for SignConfigAnc {
    type Primary = SignConfig;

    /// Construct ancillary sign config data
    fn new(_pri: &SignConfig, _view: View) -> Self {
        SignConfigAnc
    }
}

/// Convert to compact HTML
fn to_html_compact(sc: &SignConfig) -> String {
    let name = HtmlStr::new(&sc.name);
    format!("<div class='title row'>{name}</div>")
}

/// Convert to setup HTML
fn to_html_setup(sc: &SignConfig, title: &str, footer: &str) -> String {
    let color_scheme = &sc.color_scheme;
    let monochrome = monochrome_html(sc);
    let face_width = (f64::from(sc.face_width) * mm).to::<SizeUnit>();
    let face_height = (f64::from(sc.face_height) * mm).to::<SizeUnit>();
    let border_horiz = (f64::from(sc.border_horiz) * mm).to::<SizeUnitSm>();
    let border_vert = (f64::from(sc.border_vert) * mm).to::<SizeUnitSm>();
    let pitch_horiz = (f64::from(sc.pitch_horiz) * mm).to::<SizeUnitSm>();
    let pitch_vert = (f64::from(sc.pitch_vert) * mm).to::<SizeUnitSm>();
    let pixel_width = sc.pixel_width;
    let pixel_height = sc.pixel_height;
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
    format!(
        "{title}\
        <div class='row'>\
          <label>Color Scheme</label>\
          <span class='info'>{color_scheme}</span>\
        </div>\
        {monochrome}\
        <div class='row'>\
          <label>Face Size</label>\
          <span class='info'>{face_width:.2} x {face_height:.2}</span>\
        </div>\
        <div class='row'>\
          <label>Border</label>\
          <span class='info'>{border_horiz:.2} x {border_vert:.2}</span>\
        </div>\
        <div class='row'>\
          <label>Pitch</label>\
          <span class='info'>{pitch_horiz:.2} x {pitch_vert:.2}</span>\
        </div>\
        <div class='row'>\
          <label>Pixel Size</label>\
          <span class='info'>{pixel_width} x {pixel_height} px</span>\
        </div>\
        <div class='row'>\
          <label>Character Width</label><span class='info'>{char_width}</span>\
          <label>x Height</label><span class='info'>{char_height}</span>\
        </div>\
        <div class='row'>\
          <label for='module_width'>Module Width</label>{module_width}\
          <label for='module_height'>x Height</label>{module_height}\
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

/// Create an HTML `select` element of comm configs
fn select_factors_html(id: &str, max: i32, value: Option<i32>) -> String {
    let mut html = String::new();
    html.push_str("<select id='");
    html.push_str(id);
    html.push_str("'>");
    for fact in
        std::iter::once(None).chain(factor::unique(max).map(Some))
    {
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
        self.name.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, _anc: &SignConfigAnc) -> String {
        match view {
            View::Compact => to_html_compact(self),
            View::Setup => {
                let title = self.title(View::Setup);
                let footer = self.footer(true);
                to_html_setup(self, &title, &footer)
            }
            _ => unreachable!(),
        }
    }

    /// Get changed fields from Setup form
    fn changed_setup(&self) -> String {
        let mut fields = Fields::new();
        fields.changed_select("module_width", self.module_width);
        fields.changed_select("module_height", self.module_height);
        fields.into_value().to_string()
    }
}
