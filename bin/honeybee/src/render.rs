/*
 * Copyright (C) 2018-2019  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
use base64::{Config, CharacterSet, LineWrap, decode_config_slice};
use std::collections::HashMap;
use crate::error::Error;
use crate::font::{Font, Graphic};
use crate::multi::*;
use crate::raster::Raster;

/// Value result from parsing MULTI.
type UnitResult = Result<(), SyntaxError>;

/// Length of base64 output buffer for glyphs.
/// Encoded glyphs are restricted to 128 bytes.
const GLYPH_LEN: usize = (128 + 3) / 4 * 3;

/// Page render state
#[derive(Copy, Clone)]
pub struct State {
    color_scheme    : ColorScheme,
    char_width      : u8,
    char_height     : u8,
    color_foreground: Color,
    page_background : Color,
    page_on_time_ds : u8,       // deciseconds
    page_off_time_ds: u8,       // deciseconds
    text_rectangle  : Rectangle,
    just_page       : PageJustification,
    just_line       : LineJustification,
    line_number     : u8,
    span_number     : u8,
    line_spacing    : Option<u8>,
    char_spacing    : Option<u8>,
    font            : (u8, Option<u16>),
}

/// Text span
pub struct TextSpan {
    state : State,   // render state at start of span
    text  : String,
}

/// Text line
struct TextLine {
    height       : u16,
    font_spacing : u16,
    line_spacing : Option<u16>,
}

/// Page renderer
pub struct PageRenderer {
    state  : State,                // render state at start of page
    values : Vec<([u8;3], Value)>, // foreground color, graphic / color rect
    spans  : Vec<TextSpan>,        // text spans
}

/// Page splitter (iterator)
pub struct PageSplitter<'a> {
    default_state : State,
    state         : State,
    parser        : Parser<'a>,
    more          : bool,
}

/// Scale a u8 value by another (mapping range to 0-1)
fn scale_u8(a: u8, b: u8) -> u8 {
    let c = a as u32 * b as u32;
    // cheap alternative to divide by 255
    (((c + 1) + (c >> 8)) >> 8) as u8
}

impl State {
    /// Create a new render state.
    pub fn new(color_scheme     : ColorScheme,
               char_width       : u8,
               char_height      : u8,
               color_foreground : Color,
               page_background  : Color,
               page_on_time_ds  : u8,
               page_off_time_ds : u8,
               text_rectangle   : Rectangle,
               just_page        : PageJustification,
               just_line        : LineJustification,
               font             : (u8, Option<u16>)) -> Self
    {
        State {
            color_scheme,
            char_width,
            char_height,
            color_foreground,
            page_background,
            page_on_time_ds,
            page_off_time_ds,
            text_rectangle,
            just_page,
            just_line,
            line_number : 0,
            span_number : 0,
            line_spacing : None,
            char_spacing : None,
            font,
        }
    }
    /// Check if the sign is a character-matrix.
    fn is_char_matrix(&self) -> bool {
        self.char_width > 0
    }
    /// Check if the sign is a full-matrix.
    fn is_full_matrix(&self) -> bool {
        self.char_width == 0 && self.char_height == 0
    }
    /// Get the character width (1 for variable width).
    fn char_width(&self) -> u16 {
        if self.is_char_matrix() {
            self.char_width.into()
        } else {
            1
        }
    }
    /// Get the character height (1 for variable height).
    fn char_height(&self) -> u16 {
        if self.char_height > 0 {
            self.char_height.into()
        } else {
            1
        }
    }
    /// Get a color appropriate for the color scheme.
    ///
    /// * `c` Color value.
    /// * `ds` Default state.
    fn get_color(&self, c: Color, ds: &State) -> Result<Color, SyntaxError> {
        match self.color_scheme {
            ColorScheme::Monochrome1Bit => self.get_monochrome_1_bit(c, ds),
            ColorScheme::Monochrome8Bit => self.get_monochrome_8_bit(c, ds),
            ColorScheme::ColorClassic   => self.get_classic(c),
            ColorScheme::Color24Bit     => self.get_color_24_bit(c),
        }
    }
    /// Get color for a monochrome 1-bit scheme.
    ///
    /// * `c` Color value.
    /// * `ds` Default state.
    fn get_monochrome_1_bit(&self, c: Color, ds: &State)
        -> Result<Color, SyntaxError>
    {
        match c {
            Color::Legacy(0) => Ok(ds.page_background),
            Color::Legacy(1) => Ok(ds.color_foreground),
            _                => Err(SyntaxError::UnsupportedTagValue),
        }
    }
    /// Get color for a monochrome 8-bit scheme.
    ///
    /// * `c` Color value.
    /// * `ds` Default state.
    fn get_monochrome_8_bit(&self, c: Color, ds: &State)
        -> Result<Color, SyntaxError>
    {
        if let Color::Legacy(v) = c {
            let rgb = self.color_rgb(ds.color_foreground)?;
            let r = scale_u8(rgb[0], v);
            let g = scale_u8(rgb[1], v);
            let b = scale_u8(rgb[2], v);
            Ok(Color::RGB(r,g,b))
        } else {
            Err(SyntaxError::UnsupportedTagValue)
        }
    }
    /// Get color for a classic scheme.
    ///
    /// * `c` Color value.
    fn get_classic(&self, c: Color) -> Result<Color, SyntaxError> {
        match c {
            Color::Legacy(0...9) => Ok(c),
            _                    => Err(SyntaxError::UnsupportedTagValue),
        }
    }
    /// Get color for a 24-bit scheme.
    ///
    /// * `c` Color value.
    fn get_color_24_bit(&self, c: Color) -> Result<Color, SyntaxError> {
        match c {
            Color::RGB(_,_,_)    => Ok(c),
            Color::Legacy(0...9) => Ok(c), // allow classic colors only
            _                    => Err(SyntaxError::UnsupportedTagValue),
        }
    }
    /// Update the render state with a MULTI value.
    ///
    /// * `default_state` Default render state.
    /// * `v` MULTI value.
    fn update(&mut self, default_state: &State, v: &Value) -> UnitResult {
        match v {
            Value::ColorBackground(None) => {
                // This tag remains for backward compatibility with 1203v1
                self.page_background = default_state.page_background;
            },
            Value::ColorBackground(Some(c)) => {
                // This tag remains for backward compatibility with 1203v1
                self.page_background = self.get_color(*c, default_state)?;
            },
            Value::ColorForeground(None) => {
                self.color_foreground = default_state.color_foreground;
            },
            Value::ColorForeground(Some(c)) => {
                self.color_foreground = self.get_color(*c, default_state)?;
            },
            Value::ColorRectangle(_,c) => {
                self.get_color(*c, default_state)?;
            },
            Value::Font(None) => { self.font = default_state.font },
            Value::Font(Some(f)) => { self.font = *f },
            Value::Graphic(_, _) => (),
            Value::JustificationLine(Some(LineJustification::Other)) => {
                return Err(SyntaxError::UnsupportedTagValue);
            },
            Value::JustificationLine(Some(LineJustification::Full)) => {
                return Err(SyntaxError::UnsupportedTagValue);
            },
            Value::JustificationLine(jl) => {
                self.just_line = jl.unwrap_or(default_state.just_line);
                self.span_number = 0;
            },
            Value::JustificationPage(Some(PageJustification::Other)) => {
                return Err(SyntaxError::UnsupportedTagValue);
            },
            Value::JustificationPage(jp) => {
                self.just_page = jp.unwrap_or(default_state.just_page);
                self.line_number = 0;
                self.span_number = 0;
            },
            Value::NewLine(None) => {
                self.line_spacing = None;
                self.line_number += 1;
                self.span_number = 0;
            },
            Value::NewLine(Some(ls)) => {
                if !self.is_full_matrix() {
                    return Err(SyntaxError::UnsupportedTagValue);
                }
                self.line_spacing = Some(*ls);
                self.line_number += 1;
                self.span_number = 0;
            },
            Value::NewPage() => {
                self.line_number = 0;
                self.span_number = 0;
            },
            Value::PageBackground(None) => {
                self.page_background = default_state.page_background;
            },
            Value::PageBackground(Some(c)) => {
                self.page_background = self.get_color(*c, default_state)?;
            },
            Value::PageTime(on, off) => {
                self.page_on_time_ds = on.unwrap_or(
                    default_state.page_on_time_ds
                );
                self.page_off_time_ds = off.unwrap_or(
                    default_state.page_off_time_ds
                );
            },
            Value::SpacingCharacter(sc) => {
                if self.is_char_matrix() {
                    return Err(SyntaxError::UnsupportedTag("sc".to_string()));
                }
                self.char_spacing = Some(*sc);
            },
            Value::SpacingCharacterEnd() => { self.char_spacing = None; },
            Value::TextRectangle(r) => {
                self.line_number = 0;
                self.span_number = 0;
                return self.update_text_rectangle(default_state, *r);
            },
            Value::Text(_) => {
                self.span_number += 1;
            },
            _ => {
                // Unsupported tags: [f], [fl], [hc], [ms], [mv]
                return Err(SyntaxError::UnsupportedTag(v.to_string()));
            },
        }
        Ok(())
    }
    /// Update the text rectangle.
    fn update_text_rectangle(&mut self, default_state: &State,
        r: Rectangle) -> UnitResult
    {
        let r = r.match_width_height(&default_state.text_rectangle);
        if !default_state.text_rectangle.contains(&r) {
            return Err(SyntaxError::UnsupportedTagValue);
        }
        let cw = self.char_width();
        if cw > 0 {
            // Check text rectangle matches character boundaries
            let x = r.x - 1;
            if x % cw != 0 || r.w % cw != 0 {
                return Err(SyntaxError::UnsupportedTagValue);
            }
        }
        let lh = self.char_height();
        if lh > 0 {
            // Check text rectangle matches line boundaries
            let y = r.y - 1;
            if y % lh != 0 || r.h % lh != 0 {
                return Err(SyntaxError::UnsupportedTagValue);
            }
        }
        self.text_rectangle = r;
        Ok(())
    }
    /// Get the page background color
    fn page_background(&self) -> Result<[u8;3], SyntaxError> {
        self.color_rgb(self.page_background)
    }
    /// Get the foreground color
    fn color_foreground(&self) -> Result<[u8;3], SyntaxError> {
        self.color_rgb(self.color_foreground)
    }
    /// Get RGB triplet for a color.
    fn color_rgb(&self, c: Color) -> Result<[u8;3], SyntaxError> {
        match c {
            Color::RGB(r,g,b) => Ok([r,g,b]),
            Color::Legacy(v)  => self.color_rgb_legacy(v),
        }
    }
    /// Get RGB triplet for a legacy color value.
    ///
    /// * `v` Color value (0-255).
    fn color_rgb_legacy(&self, v: u8) -> Result<[u8;3], SyntaxError> {
        match self.color_scheme {
            ColorScheme::Monochrome1Bit => self.color_rgb_monochrome_1_bit(v),
            ColorScheme::Monochrome8Bit => self.color_rgb_monochrome_8_bit(v),
            ColorScheme::ColorClassic |
            ColorScheme::Color24Bit     => self.color_rgb_classic(v),
        }
    }
    /// Get RGB triplet for a monochrome 1-bit color.
    fn color_rgb_monochrome_1_bit(&self, v: u8) -> Result<[u8;3], SyntaxError> {
        match v {
            0 => Ok([  0,   0,   0]),
            1 => Ok([255, 255, 255]),
            _ => Err(SyntaxError::UnsupportedTagValue),
        }
    }
    /// Get RGB triplet for a monochrome 8-bit color.
    fn color_rgb_monochrome_8_bit(&self, v: u8) -> Result<[u8;3], SyntaxError> {
        Ok([v,v,v])
    }
    /// Get RGB triplet for a classic color.
    ///
    /// * `v` Color value (0-9).
    fn color_rgb_classic(&self, v: u8) -> Result<[u8;3], SyntaxError> {
        match ColorClassic::from_u8(v) {
            Some(c) => Ok(c.rgb()),
            None    => Err(SyntaxError::UnsupportedTagValue),
        }
    }
    /// Check if states match for text spans
    fn matches_span(&self, other: &State) -> bool {
        self.text_rectangle == other.text_rectangle &&
        self.just_page      == other.just_page &&
        self.line_number    == other.line_number &&
        self.just_line      == other.just_line
    }
    /// Check if states match for lines
    fn matches_line(&self, other: &State) -> bool {
        self.text_rectangle == other.text_rectangle &&
        self.just_page      == other.just_page
    }
}

impl<'a> TextSpan {
    /// Create a new text span
    fn new(state: State, text: String) -> Self {
        TextSpan { state, text }
    }
    /// Get the font of a text span
    fn font(&self, fonts: &'a HashMap<i32, Font>)
        -> Result<&'a Font, SyntaxError>
    {
        let fnum = self.state.font.0 as i32;
        match fonts.get(&fnum) {
            Some(f) => Ok(f),
            None    => Err(SyntaxError::FontNotDefined(self.state.font.0)),
        }
    }
    /// Get the width of a text span
    fn width(&self, fonts: &HashMap<i32, Font>) -> Result<u16, SyntaxError> {
        let mut width = 0;
        let font = self.font(fonts)?;
        let cs = self.char_spacing_fonts(fonts)?;
        for c in self.text.chars() {
            let g = font.glyph(c)?;
            if width > 0 {
                width += cs;
            }
            width += g.width() as u16;
        }
        Ok(width)
    }
    /// Get the char spacing
    fn char_spacing_fonts(&self, fonts: &HashMap<i32, Font>)
        -> Result<u16, SyntaxError>
    {
        match self.state.char_spacing {
            Some(s) => Ok(s as u16),
            None    => Ok(self.font(fonts)?.char_spacing()),
        }
    }
    /// Get the char spacing
    fn char_spacing_font(&self, font: &Font) -> u32 {
        match self.state.char_spacing {
            Some(s) => s as u32,
            None    => font.char_spacing() as u32,
        }
    }
    /// Get the char spacing from a previous span
    fn char_spacing_between(&self, prev: &TextSpan, fonts: &HashMap<i32, Font>)
        -> Result<u16, SyntaxError>
    {
        if let Some(c) = self.state.char_spacing {
            Ok(c as u16)
        } else {
            // NTCIP 1203 fontCharSpacing:
            // "... the average character spacing of the two fonts,
            // rounded up to the nearest whole pixel ..." ???
            let psc = prev.char_spacing_fonts(fonts)?;
            let sc = self.char_spacing_fonts(fonts)?;
            Ok(((psc + sc) as f32 / 2f32).round() as u16)
        }
    }
    /// Get the height of a text span
    fn height(&self, fonts: &HashMap<i32, Font>) -> Result<u16, SyntaxError> {
        Ok(self.font(fonts)?.height())
    }
    /// Get the font line spacing
    fn font_spacing(&self, fonts: &HashMap<i32, Font>)
        -> Result<u16, SyntaxError>
    {
        Ok(self.font(fonts)?.line_spacing())
    }
    /// Get the line spacing
    fn line_spacing(&self) -> Option<u16> {
        match self.state.line_spacing {
            Some(s) => Some(s as u16),
            None    => None,
        }
    }
    /// Render the text span
    fn render(&self, page: &mut Raster, font: &Font, mut x: u32, y: u32)
        -> Result<(), Error>
    {
        let cf = self.state.color_foreground()?;
        let cf = [cf[0], cf[1], cf[2]];
        let h = font.height() as u32;
        let cs = self.char_spacing_font(font);
        debug!("span: {}, left: {}, top: {}, height: {}", self.text, x, y, h);
        let config = Config::new(CharacterSet::Standard, false, true,
            LineWrap::NoWrap);
        let mut buf = [0; GLYPH_LEN];
        for c in self.text.chars() {
            let g = font.glyph(c)?;
            let w = g.width() as u32;
            let n = decode_config_slice(&g.pixels, config, &mut buf)?;
            debug!("char: {}, width: {}, len: {}", c, w, n);
            for yy in 0..h {
                for xx in 0..w {
                    let p = yy * w + xx;
                    let by = p as usize / 8;
                    let bi = 7 - (p & 7);
                    let lit = ((buf[by] >> bi) & 1) != 0;
                    if lit {
                        page.set_pixel(x + xx, y + yy, cf);
                    }
                }
            }
            x += w + cs;
        }
        Ok(())
    }
}

impl TextLine {
    /// Create a new text line
    fn new(height: u16, font_spacing: u16, line_spacing: Option<u16>) -> Self {
        TextLine { height, font_spacing, line_spacing }
    }
    /// Combine a text line with another
    fn combine(&mut self, other: &TextLine) {
        self.height = self.height.max(other.height);
        self.font_spacing = self.font_spacing.max(other.font_spacing);
        self.line_spacing = self.line_spacing.or(other.line_spacing);
    }
    /// Get the spacing between two text lines
    fn spacing(&self, other: &TextLine) -> u16 {
        if let Some(ls) = self.line_spacing {
            ls
        } else {
            // NTCIP 1203 fontLineSpacing:
            // "The number of pixels between adjacent lines
            // is the average of the 2 line spacings of each
            // line, rounded up to the nearest whole pixel."
            let s = self.font_spacing + other.font_spacing;
            (s as f32 / 2f32).round() as u16
        }
    }
}

impl PageRenderer {
    /// Create a new page renderer
    pub fn new(state: State, values: Vec<([u8;3], Value)>, spans: Vec<TextSpan>)
        -> Self
    {
        PageRenderer {
            state,
            values,
            spans,
        }
    }
    /// Check page and line justification ordering
    fn check_justification(&self) -> Result<(), SyntaxError> {
        let mut tr = Rectangle::new(0, 0, 0, 0);
        let mut jp = PageJustification::Other;
        let mut jl = LineJustification::Other;
        let mut ln = 0;
        for s in &self.spans {
            let text_rectangle = s.state.text_rectangle;
            let just_page = s.state.just_page;
            let just_line = s.state.just_line;
            let line_number = s.state.line_number;
            if text_rectangle == tr &&
              (just_page < jp ||
              (just_page == jp && line_number == ln && just_line < jl))
            {
                return Err(SyntaxError::TagConflict);
            }
            tr = text_rectangle;
            jp = just_page;
            jl = just_line;
            ln = line_number;
        }
        Ok(())
    }
    /// Get the page-on time (deciseconds)
    pub fn page_on_time_ds(&self) -> u16 {
        self.state.page_on_time_ds.into()
    }
    /// Get the page-off time (deciseconds)
    pub fn page_off_time_ds(&self) -> u16 {
        self.state.page_off_time_ds.into()
    }
    /// Render a blank page.
    pub fn render_blank(&self) -> Result<Raster, SyntaxError> {
        let rs = self.state;
        let w = rs.text_rectangle.w;
        let h = rs.text_rectangle.h;
        let clr = rs.page_background()?;
        let rgb = [clr[0], clr[1], clr[2]];
        let page = Raster::new(w.into(), h.into(), rgb);
        Ok(page)
    }
    /// Render the page.
    pub fn render(&self, fonts: &HashMap<i32, Font>,
        graphics: &HashMap<i32, Graphic>) -> Result<Raster, Error>
    {
        let rs = self.state;
        let w = rs.text_rectangle.w;
        let h = rs.text_rectangle.h;
        let clr = rs.page_background()?;
        let mut page = Raster::new(w.into(), h.into(), clr);
        for (cf, v) in &self.values {
            match v {
                Value::ColorRectangle(r,c) => {
                    let clr = rs.color_rgb(*c)?;
                    self.render_rect(&mut page, *r, clr)?;
                },
                Value::Graphic(gn,None) => {
                    let n = *gn as i32;
                    let g = graphics.get(&n)
                                    .ok_or(SyntaxError::GraphicNotDefined(*gn))?;
                    g.render(&mut page, *cf, 1, 1)?;
                },
                Value::Graphic(gn,Some((x,y,_))) => {
                    let n = *gn as i32;
                    let g = graphics.get(&n)
                                    .ok_or(SyntaxError::GraphicNotDefined(*gn))?;
                    let x = *x as u32;
                    let y = *y as u32;
                    g.render(&mut page, *cf, x, y)?;
                },
                _ => unreachable!(),
            }
        }
        for s in &self.spans {
            let x = self.span_x(s, fonts)?;
            let y = self.span_y(s, fonts)?;
            let font = s.font(fonts)?;
            s.render(&mut page, &font, x as u32, y as u32)?;
        }
        Ok(page)
    }
    /// Render a color rectangle
    fn render_rect(&self, page: &mut Raster, r: Rectangle, clr: [u8;3])
        ->Result<(), SyntaxError>
    {
        let rx = r.x as u32 - 1; // r.x must be > 0
        let ry = r.y as u32 - 1; // r.y must be > 0
        let rw = r.w as u32;
        let rh = r.h as u32;
        if rx + rw <= page.width() && ry + rh <= page.height() {
            for y in 0..rh {
                for x in 0..rw {
                    page.set_pixel(rx + x, ry + y, clr);
                }
            }
            return Ok(());
        }
        Err(SyntaxError::UnsupportedTagValue)
    }
    /// Get the X position of a text span.
    fn span_x(&self, s: &TextSpan, fonts: &HashMap<i32, Font>)
        ->Result<u16, SyntaxError>
    {
        match s.state.just_line {
            LineJustification::Left   => self.span_x_left(s, fonts),
            LineJustification::Center => self.span_x_center(s, fonts),
            LineJustification::Right  => self.span_x_right(s, fonts),
            _                         => unreachable!(),
        }
    }
    /// Get the X position of a left-justified text span.
    fn span_x_left(&self, span: &TextSpan, fonts: &HashMap<i32, Font>)
        ->Result<u16, SyntaxError>
    {
        let left = span.state.text_rectangle.x - 1;
        let (before, _) = self.offset_horiz(span, fonts)?;
        Ok(left + before)
    }
    /// Get the X position of a center-justified text span.
    fn span_x_center(&self, span: &TextSpan, fonts: &HashMap<i32, Font>)
        ->Result<u16, SyntaxError>
    {
        let left = span.state.text_rectangle.x - 1;
        let w = span.state.text_rectangle.w;
        let (before, after) = self.offset_horiz(span, fonts)?;
        let offset = (w - before - after) / 2; // offset for centering
        let x = left + offset + before;
        let cw = self.state.char_width();
        // Truncate to character-width boundary
        Ok((x / cw) * cw)
    }
    /// Get the X position of a right-justified span
    fn span_x_right(&self, span: &TextSpan, fonts: &HashMap<i32, Font>)
        -> Result<u16, SyntaxError>
    {
        let left = span.state.text_rectangle.x - 1;
        let w = span.state.text_rectangle.w;
        let (_, after) = self.offset_horiz(span, fonts)?;
        Ok(left + w - after)
    }
    /// Calculate horizontal offsets of a span.
    ///
    /// Returns a tuple of (before, after) widths of matching spans.
    fn offset_horiz(&self, span: &TextSpan, fonts: &HashMap<i32, Font>)
        -> Result<(u16, u16), SyntaxError>
    {
        debug!("offset_horiz '{}'", span.text);
        let state = span.state;
        let mut before = 0;
        let mut after = 0;
        let mut pspan = None;
        for s in self.spans.iter().filter(|s| state.matches_span(&s.state)) {
            if let Some(ps) = pspan {
                let w = s.char_spacing_between(ps, fonts)?;
                if s.state.span_number <= state.span_number { before += w }
                else { after += w }
                debug!("  spacing {} before {} after {}", w, before, after);
            }
            let w = s.width(fonts)?;
            if s.state.span_number < state.span_number { before += w }
            else { after += w }
            debug!("  span '{}'  before {} after {}", s.text, before, after);
            pspan = Some(s);
        }
        if before + after <= state.text_rectangle.w {
            Ok((before, after))
        } else {
            Err(SyntaxError::TextTooBig)
        }
    }
    /// Get the Y position of a text span.
    fn span_y(&self, s: &TextSpan, fonts: &HashMap<i32, Font>)
        -> Result<u16, SyntaxError>
    {
        let b = self.baseline(s, fonts)?;
        let h = s.height(fonts)?;
        debug_assert!(b >= h);
        Ok(b - h)
    }
    /// Get the baseline of a text span.
    fn baseline(&self, s: &TextSpan, fonts: &HashMap<i32, Font>)
        -> Result<u16, SyntaxError>
    {
        match s.state.just_page {
            PageJustification::Top    => self.baseline_top(s, fonts),
            PageJustification::Middle => self.baseline_middle(s, fonts),
            PageJustification::Bottom => self.baseline_bottom(s, fonts),
            _                         => unreachable!(),
        }
    }
    /// Get the baseline of a top-justified span
    fn baseline_top(&self, span: &TextSpan, fonts: &HashMap<i32, Font>)
        -> Result<u16, SyntaxError>
    {
        let top = span.state.text_rectangle.y - 1;
        let (above, _) = self.offset_vert(span, fonts)?;
        Ok(top + above)
    }
    /// Get the baseline of a middle-justified span
    fn baseline_middle(&self, span: &TextSpan, fonts: &HashMap<i32, Font>)
        -> Result<u16, SyntaxError>
    {
        let top = span.state.text_rectangle.y - 1;
        let h = span.state.text_rectangle.h;
        let (above, below) = self.offset_vert(span, fonts)?;
        let offset = (h - above - below) / 2; // offset for centering
        let y = top + offset + above;
        let ch = self.state.char_height();
        // Truncate to line-height boundary
        Ok((y / ch) * ch)
    }
    /// Get the baseline of a bottom-justified span
    fn baseline_bottom(&self, span: &TextSpan, fonts: &HashMap<i32, Font>)
        -> Result<u16, SyntaxError>
    {
        let top = span.state.text_rectangle.y - 1;
        let h = span.state.text_rectangle.h;
        let (_, below) = self.offset_vert(span, fonts)?;
        Ok(top + h - below)
    }
    /// Calculate vertical offset of a span.
    ///
    /// Returns a tuple of (above, below) heights of matching lines.
    fn offset_vert(&self, span: &TextSpan, fonts: &HashMap<i32, Font>)
        -> Result<(u16, u16), SyntaxError>
    {
        debug!("offset_vert '{}'", span.text);
        let state = span.state;
        let mut lines = vec!();
        for s in self.spans.iter().filter(|s| state.matches_line(&s.state)) {
            let ln = s.state.line_number as usize;
            let h = s.height(fonts)?;
            let fs = s.font_spacing(fonts)?;
            let ls = s.line_spacing();
            let line = TextLine::new(h, fs, ls);
            if ln >= lines.len() {
                lines.push(line);
            } else {
                &lines[ln].combine(&line);
            }
        }
        let sln = state.line_number as usize;
        let mut above = 0;
        let mut below = 0;
        for ln in 0..lines.len() {
            let line = &lines[ln];
            if ln > 0 {
                let h = line.spacing(&lines[ln - 1]);
                if ln <= sln { above += h }
                else { below += h }
                debug!("  spacing {}  above {} below {}", h, above, below);
            }
            let h = line.height;
            if ln <= sln { above += h }
            else { below += h }
            debug!("  line {}  above {} below {}", ln, above, below);
        }
        if above + below <= span.state.text_rectangle.h {
            Ok((above, below))
        } else {
            Err(SyntaxError::TextTooBig)
        }
    }
}

impl<'a> PageSplitter<'a> {
    /// Create a new page splitter.
    ///
    /// * `default_state` Default render state.
    /// * `ms` MULTI string to parse.
    pub fn new(default_state: State, ms: &'a str) -> Self {
        let parser = Parser::new(ms);
        let state = default_state;
        let more = true;
        PageSplitter { default_state, state, parser, more }
    }
    /// Make the next page.
    fn make_page(&mut self) -> Result<PageRenderer, SyntaxError> {
        self.more = false;
        let mut blank = true;   // blank line
        let mut rs = self.page_state();
        let mut values = vec!();
        let mut spans = vec!();
        while let Some(t) = self.parser.next() {
            let v = t?;
            self.state.update(&self.default_state, &v)?;
            match v {
                Value::NewPage() => {
                    self.more = true;
                    break;
                },
                Value::NewLine(_) => {
                    if blank {
                        let ts = TextSpan::new(self.state, "".to_string());
                        spans.push(ts);
                    }
                    blank = true;
                },
                Value::TextRectangle(_) => {
                    blank = true;
                }
                Value::Text(t) => {
                    let ts = TextSpan::new(self.state, t);
                    spans.push(ts);
                    blank = false;
                },
                Value::Graphic(_,_)|
                Value::ColorRectangle(_,_) => {
                    let cf = self.state.color_foreground()?;
                    values.push((cf, v));
                },
                _ => (),
            }
        }
        // These values affect the entire page
        rs.page_background = self.state.page_background;
        rs.page_on_time_ds = self.state.page_on_time_ds;
        rs.page_off_time_ds = self.state.page_off_time_ds;
        let page = PageRenderer::new(rs, values, spans);
        page.check_justification()?;
        Ok(page)
    }
    /// Get the current page state.
    fn page_state(&self) -> State {
        let mut rs = self.state;
        // Set these back to default values
        rs.text_rectangle = self.default_state.text_rectangle;
        rs.line_spacing = self.default_state.line_spacing;
        rs
    }
}

impl<'a> Iterator for PageSplitter<'a> {
    type Item = Result<PageRenderer, SyntaxError>;

    fn next(&mut self) -> Option<Result<PageRenderer, SyntaxError>> {
        if self.more {
            Some(self.make_page())
        } else {
            None
        }
    }
}

#[cfg(test)]
mod test {
    use super::*;
    fn make_full_matrix() -> State {
        State::new(ColorScheme::Color24Bit,
                   0, 0,
                   Color::Legacy(1), Color::Legacy(0),
                   20, 0,
                   Rectangle::new(1, 1, 60, 30),
                   PageJustification::Top,
                   LineJustification::Left,
                   (1, None))
    }
    #[test]
    fn page_count() {
        let rs = make_full_matrix();
        let pages: Vec<_> = PageSplitter::new(rs, "").collect();
        assert!(pages.len() == 1);
        let pages: Vec<_> = PageSplitter::new(rs, "1").collect();
        assert!(pages.len() == 1);
        let pages: Vec<_> = PageSplitter::new(rs, "[np]").collect();
        assert!(pages.len() == 2);
        let pages: Vec<_> = PageSplitter::new(rs, "1[NP]").collect();
        assert!(pages.len() == 2);
        let pages: Vec<_> = PageSplitter::new(rs, "1[Np]2").collect();
        assert!(pages.len() == 2);
        let pages: Vec<_> = PageSplitter::new(rs, "1[np]2[nP]").collect();
        assert!(pages.len() == 3);
    }
    #[test]
    fn page_full_matrix() {
        let rs = make_full_matrix();
        let mut pages = PageSplitter::new(rs, "");
        let p = pages.next().unwrap().unwrap();
        let rs = p.state;
        assert!(rs.color_scheme == ColorScheme::Color24Bit);
        assert!(rs.color_foreground == Color::Legacy(1));
        assert!(rs.page_background == Color::Legacy(0));
        assert!(rs.page_on_time_ds == 20);
        assert!(rs.page_off_time_ds == 0);
        assert!(rs.text_rectangle == Rectangle::new(1,1,60,30));
        assert!(rs.just_page == PageJustification::Top);
        assert!(rs.just_line == LineJustification::Left);
        assert!(rs.line_spacing == None);
        assert!(rs.char_spacing == None);
        assert!(rs.char_width == 0);
        assert!(rs.char_height == 0);
        assert!(rs.font == (1, None));
        let mut pages = PageSplitter::new(rs, "[pt10o2][cb9][pb5][cf3][jp3]\
            [jl4][tr1,1,10,10][nl4][fo3,1234][sc2][np][pb][pt][cb][/sc]");
        let p = pages.next().unwrap().unwrap();
        let rs = p.state;
        assert!(rs.color_foreground == Color::Legacy(1));
        assert!(rs.page_background == Color::Legacy(5));
        assert!(rs.page_on_time_ds == 10);
        assert!(rs.page_off_time_ds == 2);
        assert!(rs.text_rectangle == Rectangle::new(1,1,60,30));
        assert!(rs.just_page == PageJustification::Top);
        assert!(rs.just_line == LineJustification::Left);
        assert!(rs.line_spacing == None);
        assert!(rs.char_spacing == None);
        assert!(rs.font == (1, None));
        let p = pages.next().unwrap().unwrap();
        let rs = p.state;
        assert!(rs.color_foreground == Color::Legacy(3));
        assert!(rs.page_background == Color::Legacy(0));
        assert!(rs.page_on_time_ds == 20);
        assert!(rs.page_off_time_ds == 0);
        assert!(rs.text_rectangle == Rectangle::new(1,1,60,30));
        assert!(rs.just_page == PageJustification::Middle);
        assert!(rs.just_line == LineJustification::Right);
        assert!(rs.line_spacing == None);
        assert!(rs.char_spacing == Some(2));
        assert!(rs.font == (3, Some(0x1234)));
    }
    fn make_char_matrix() -> State {
        State::new(ColorScheme::Monochrome1Bit,
                   5, 7,
                   Color::Legacy(1), Color::Legacy(0),
                   20, 0,
                   Rectangle::new(1, 1, 100, 21),
                   PageJustification::Top,
                   LineJustification::Left,
                   (1, None))
    }
    #[test]
    fn page_char_matrix() {
        let rs = make_char_matrix();
        let mut pages = PageSplitter::new(rs, "[tr1,1,12,12]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = pages.next() {
            assert!(true);
        } else { assert!(false) }
        let mut pages = PageSplitter::new(rs, "[tr1,1,50,12]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = pages.next() {
            assert!(true);
        } else { assert!(false) }
        let mut pages = PageSplitter::new(rs, "[tr1,1,12,14]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = pages.next() {
            assert!(true);
        } else { assert!(false) }
        let mut pages = PageSplitter::new(rs, "[tr1,1,50,14]");
        if let Some(Ok(_)) = pages.next() { assert!(true); }
        else { assert!(false) }
        let mut pages = PageSplitter::new(rs, "[pb9]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = pages.next() {
            assert!(true);
        } else { assert!(false) }
    }
}
