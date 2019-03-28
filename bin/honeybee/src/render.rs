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
use std::collections::HashMap;
use crate::error::Error;
use crate::font::{Font, Graphic};
use crate::multi::*;
use crate::raster::{Raster, Rgb24};

/// Page render state
#[derive(Clone)]
pub struct State {
    color_scheme    : ColorScheme,
    char_width      : u8,
    char_height     : u8,
    fg_default      : Color,
    bg_default      : Color,
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
    values : Vec<(Value, Rgb24)>,  // graphic / color rect, foreground color
    spans  : Vec<TextSpan>,        // text spans
}

/// Page splitter (iterator)
pub struct PageSplitter<'a> {
    default_state : State,
    state         : State,
    parser        : Parser<'a>,
    more_pages    : bool,
    line_blank    : bool,
}

/// Scale between two color components
fn scale_component(bg: u8, fg: u8, v: u8) -> u8 {
    let d = bg.max(fg) - bg.min(fg);
    let c = d as u32 * v as u32;
    // cheap alternative to divide by 255
    let r = (((c + 1) + (c >> 8)) >> 8) as u8;
    bg.min(fg) + r
}

impl State {
    /// Create a new render state.
    pub fn new(color_scheme     : ColorScheme,
               char_width       : u8,
               char_height      : u8,
               fg_default       : Color,
               bg_default       : Color,
               page_on_time_ds  : u8,
               page_off_time_ds : u8,
               text_rectangle   : Rectangle,
               just_page        : PageJustification,
               just_line        : LineJustification,
               font             : (u8, Option<u16>)) -> Self
    {
        let color_foreground = fg_default;
        let page_background = bg_default;
        State {
            color_scheme,
            char_width,
            char_height,
            fg_default,
            bg_default,
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
    /// Update the text rectangle.
    fn update_text_rectangle(&mut self, default_state: &State,
        r: Rectangle) -> Result<(), SyntaxError>
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
    /// Get the background RGB color.
    fn background_rgb(&self) -> Result<Rgb24, SyntaxError> {
        self.color_rgb(self.page_background)
    }
    /// Get the foreground RGB color.
    fn foreground_rgb(&self) -> Result<Rgb24, SyntaxError> {
        self.color_rgb(self.color_foreground)
    }
    /// Get RGB for the specified color.
    fn color_rgb(&self, c: Color) -> Result<Rgb24, SyntaxError> {
        match (self.color_scheme, c) {
            (ColorScheme::Monochrome1Bit, Color::Legacy(v)) => {
                self.color_rgb_monochrome_1(v)
            },
            (ColorScheme::Monochrome8Bit, Color::Legacy(v)) => {
                self.color_rgb_monochrome_8(v)
            },
            (_, Color::Legacy(v)) => self.color_rgb_classic(v),
            (_, Color::RGB(r, g, b)) => Ok(Rgb24::new(r, g, b)),
        }
    }
    /// Get RGB for a monochrome 1-bit color.
    fn color_rgb_monochrome_1(&self, v: u8) -> Result<Rgb24, SyntaxError> {
        match v {
            0 => self.color_rgb_default(self.bg_default),
            1 => self.color_rgb_default(self.fg_default),
            _ => Err(SyntaxError::UnsupportedTagValue),
        }
    }
    /// Get RGB for a default color.
    fn color_rgb_default(&self, c: Color) -> Result<Rgb24, SyntaxError> {
        match c {
            Color::RGB(r, g, b) => Ok(Rgb24::new(r, g, b)),
            Color::Legacy(v) => self.color_rgb_classic(v),
        }
    }
    /// Get RGB for a monochrome 8-bit color.
    fn color_rgb_monochrome_8(&self, v: u8) -> Result<Rgb24, SyntaxError> {
        let bg = self.color_rgb_default(self.bg_default)?;
        let fg = self.color_rgb_default(self.fg_default)?;
        let r = scale_component(bg.r(), fg.r(), v);
        let g = scale_component(bg.g(), fg.g(), v);
        let b = scale_component(bg.b(), fg.b(), v);
        Ok(Rgb24::new(r, g, b))
    }
    /// Get RGB for a classic color.
    ///
    /// * `v` Color value (0-9).
    fn color_rgb_classic(&self, v: u8) -> Result<Rgb24, SyntaxError> {
        match ColorClassic::from_u8(v) {
            Some(c) => Ok(c.rgb().into()),
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
    fn render_text(&self, page: &mut Raster, font: &Font, x: u32, y: u32)
        -> Result<(), Error>
    {
        let cs = self.char_spacing_font(font);
        let cf = self.state.foreground_rgb()?;
        font.render_text(page, &self.text, x, y, cs, cf)
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
    pub fn new(state: State) -> Self {
        let values = vec![];
        let spans = vec![];
        PageRenderer { state, values, spans }
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
        let rs = &self.state;
        let w = rs.text_rectangle.w;
        let h = rs.text_rectangle.h;
        let clr = rs.background_rgb()?;
        let page = Raster::new(w.into(), h.into(), clr);
        Ok(page)
    }
    /// Render the page.
    pub fn render(&self, fonts: &HashMap<i32, Font>,
        graphics: &HashMap<i32, Graphic>) -> Result<Raster, Error>
    {
        let rs = &self.state;
        let w = rs.text_rectangle.w;
        let h = rs.text_rectangle.h;
        let clr = rs.background_rgb()?;
        let mut page = Raster::new(w.into(), h.into(), clr);
        for (v, cf) in &self.values {
            match v {
                Value::ColorRectangle(r, c) => {
                    let clr = rs.color_rgb(*c)?;
                    self.render_rect(&mut page, *r, clr)?;
                },
                Value::Graphic(gn, None) => {
                    let n = *gn as i32;
                    let g = graphics.get(&n)
                                    .ok_or(SyntaxError::GraphicNotDefined(*gn))?;
                    g.onto_raster(&mut page, 1, 1, *cf)?;
                },
                Value::Graphic(gn, Some((x,y,_))) => {
                    let n = *gn as i32;
                    let g = graphics.get(&n)
                                    .ok_or(SyntaxError::GraphicNotDefined(*gn))?;
                    let x = *x as u32;
                    let y = *y as u32;
                    g.onto_raster(&mut page, x, y, *cf)?;
                },
                _ => unreachable!(),
            }
        }
        for s in &self.spans {
            let x = self.span_x(s, fonts)? as u32;
            let y = self.span_y(s, fonts)? as u32;
            let font = s.font(fonts)?;
            s.render_text(&mut page, &font, x, y)?;
        }
        Ok(page)
    }
    /// Render a color rectangle
    fn render_rect(&self, page: &mut Raster, r: Rectangle, clr: Rgb24)
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
        let rs = &span.state;
        let mut before = 0;
        let mut after = 0;
        let mut pspan = None;
        for s in self.spans.iter().filter(|s| rs.matches_span(&s.state)) {
            if let Some(ps) = pspan {
                let w = s.char_spacing_between(ps, fonts)?;
                if s.state.span_number <= rs.span_number { before += w }
                else { after += w }
                debug!("  spacing {} before {} after {}", w, before, after);
            }
            let w = s.width(fonts)?;
            if s.state.span_number < rs.span_number { before += w }
            else { after += w }
            debug!("  span '{}'  before {} after {}", s.text, before, after);
            pspan = Some(s);
        }
        if before + after <= rs.text_rectangle.w {
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
        let rs = &span.state;
        let mut lines = vec!();
        for s in self.spans.iter().filter(|s| rs.matches_line(&s.state)) {
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
        let sln = rs.line_number as usize;
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
        let state = default_state.clone();
        let more_pages = true;
        let line_blank = true;
        PageSplitter { default_state, state, parser, more_pages, line_blank }
    }
    /// Make the next page.
    fn make_page(&mut self) -> Result<PageRenderer, SyntaxError> {
        self.more_pages = false;
        self.line_blank = true;
        let mut page = PageRenderer::new(self.page_state());
        while let Some(v) = self.parser.next() {
            self.update_state(v?, &mut page)?;
            if self.more_pages { break; }
        }
        // These values affect the entire page
        page.state.page_background = self.state.page_background;
        page.state.page_on_time_ds = self.state.page_on_time_ds;
        page.state.page_off_time_ds = self.state.page_off_time_ds;
        page.check_justification()?;
        Ok(page)
    }
    /// Get the current page state.
    fn page_state(&self) -> State {
        let mut rs = self.state.clone();
        // Set these back to default values
        rs.text_rectangle = self.default_state.text_rectangle;
        rs.line_spacing = self.default_state.line_spacing;
        rs
    }
    /// Update the render state with one MULTI value.
    ///
    /// * `v` MULTI value.
    /// * `page` Page renderer.
    fn update_state(&mut self, v: Value, page: &mut PageRenderer)
        -> Result<(), SyntaxError>
    {
        let ds = &self.default_state;
        let mut rs = &mut self.state;
        match v {
            Value::ColorBackground(None) => {
                // This tag remains for backward compatibility with 1203v1
                rs.page_background = rs.bg_default;
            },
            Value::ColorBackground(Some(c)) => {
                // This tag remains for backward compatibility with 1203v1
                rs.page_background = rs.color_scheme.validate(c)?;
            },
            Value::ColorForeground(None) => {
                rs.color_foreground = rs.fg_default;
            },
            Value::ColorForeground(Some(c)) => {
                rs.color_foreground = rs.color_scheme.validate(c)?;
            },
            Value::ColorRectangle(_, c) => {
                // foreground color is not changed by [cr]
                rs.color_scheme.validate(c)?;
                let cf = rs.foreground_rgb()?;
                page.values.push((v, cf));
            },
            Value::Font(None) => { rs.font = ds.font },
            Value::Font(Some(f)) => { rs.font = f },
            Value::Graphic(_, _) =>  {
                let cf = rs.foreground_rgb()?;
                page.values.push((v, cf));
            },
            Value::JustificationLine(Some(LineJustification::Other)) => {
                return Err(SyntaxError::UnsupportedTagValue);
            },
            Value::JustificationLine(Some(LineJustification::Full)) => {
                return Err(SyntaxError::UnsupportedTagValue);
            },
            Value::JustificationLine(jl) => {
                rs.just_line = jl.unwrap_or(ds.just_line);
                rs.span_number = 0;
            },
            Value::JustificationPage(Some(PageJustification::Other)) => {
                return Err(SyntaxError::UnsupportedTagValue);
            },
            Value::JustificationPage(jp) => {
                rs.just_page = jp.unwrap_or(ds.just_page);
                rs.line_number = 0;
                rs.span_number = 0;
            },
            Value::NewLine(ls) => {
                if ls.is_some() && !rs.is_full_matrix() {
                    return Err(SyntaxError::UnsupportedTagValue);
                }
                // Insert an empty text span for blank lines.
                if self.line_blank {
                    page.spans.push(TextSpan::new(rs.clone(), "".to_string()));
                }
                self.line_blank = true;
                rs.line_spacing = ls;
                rs.line_number += 1;
                rs.span_number = 0;
            },
            Value::NewPage() => {
                rs.line_number = 0;
                rs.span_number = 0;
                self.more_pages = true;
            },
            Value::PageBackground(None) => {
                rs.page_background = rs.bg_default;
            },
            Value::PageBackground(Some(c)) => {
                rs.page_background = rs.color_scheme.validate(c)?;
            },
            Value::PageTime(on, off) => {
                rs.page_on_time_ds = on.unwrap_or(ds.page_on_time_ds);
                rs.page_off_time_ds = off.unwrap_or(ds.page_off_time_ds);
            },
            Value::SpacingCharacter(sc) => {
                if rs.is_char_matrix() {
                    return Err(SyntaxError::UnsupportedTag("sc".to_string()));
                }
                rs.char_spacing = Some(sc);
            },
            Value::SpacingCharacterEnd() => { rs.char_spacing = None; },
            Value::TextRectangle(r) => {
                self.line_blank = true;
                rs.line_number = 0;
                rs.span_number = 0;
                rs.update_text_rectangle(ds, r)?;
            },
            Value::Text(t) => {
                page.spans.push(TextSpan::new(rs.clone(), t));
                rs.span_number += 1;
                self.line_blank = false;
            },
            _ => {
                // Unsupported tags: [f], [fl], [hc], [ms], [mv]
                return Err(SyntaxError::UnsupportedTag(v.to_string()));
            },
        }
        Ok(())
    }
}

impl<'a> Iterator for PageSplitter<'a> {
    type Item = Result<PageRenderer, SyntaxError>;

    fn next(&mut self) -> Option<Result<PageRenderer, SyntaxError>> {
        if self.more_pages {
            Some(self.make_page())
        } else {
            None
        }
    }
}

#[cfg(test)]
mod test {
    use super::*;
    #[test]
    fn color_component() {
        assert!(scale_component(0, 255, 0) == 0);
        assert!(scale_component(0, 255, 128) == 128);
        assert!(scale_component(0, 255, 255) == 255);
        assert!(scale_component(0, 128, 0) == 0);
        assert!(scale_component(0, 128, 128) == 64);
        assert!(scale_component(0, 128, 255) == 128);
        assert!(scale_component(128, 255, 0) == 128);
        assert!(scale_component(128, 255, 128) == 191);
        assert!(scale_component(128, 255, 255) == 255);
    }
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
        let pages: Vec<_> = PageSplitter::new(rs.clone(), "").collect();
        assert!(pages.len() == 1);
        let pages: Vec<_> = PageSplitter::new(rs.clone(), "1").collect();
        assert!(pages.len() == 1);
        let pages: Vec<_> = PageSplitter::new(rs.clone(), "[np]").collect();
        assert!(pages.len() == 2);
        let pages: Vec<_> = PageSplitter::new(rs.clone(), "1[NP]").collect();
        assert!(pages.len() == 2);
        let pages: Vec<_> = PageSplitter::new(rs.clone(), "1[Np]2").collect();
        assert!(pages.len() == 2);
        let pages: Vec<_> = PageSplitter::new(rs.clone(), "1[np]2[nP]").collect();
        assert!(pages.len() == 3);
        let pages: Vec<_> = PageSplitter::new(rs.clone(), "[fo6][nl]\
            [jl2][cf255,255,255]RAMP A[jl4][cf255,255,0]FULL[nl]\
            [jl2][cf255,255,255]RAMP B[jl4][cf255,255,0]FULL[nl]\
            [jl2][cf255,255,255]RAMP C[jl4][cf255,255,0]FULL").collect();
        assert!(pages.len() == 1);
    }
    #[test]
    fn page_full_matrix() {
        let rs = make_full_matrix();
        let mut pages = PageSplitter::new(rs.clone(), "");
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
        let mut pages = PageSplitter::new(rs.clone(), "[pt10o2][cb9][pb5][cf3]\
            [jp3][jl4][tr1,1,10,10][nl4][fo3,1234][sc2][np][pb][pt][cb][/sc]");
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
        let mut pages = PageSplitter::new(rs.clone(), "[tr1,1,12,12]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = pages.next() {
            assert!(true);
        } else { assert!(false) }
        let mut pages = PageSplitter::new(rs.clone(), "[tr1,1,50,12]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = pages.next() {
            assert!(true);
        } else { assert!(false) }
        let mut pages = PageSplitter::new(rs.clone(), "[tr1,1,12,14]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = pages.next() {
            assert!(true);
        } else { assert!(false) }
        let mut pages = PageSplitter::new(rs.clone(), "[tr1,1,50,14]");
        if let Some(Ok(_)) = pages.next() { assert!(true); }
        else { assert!(false) }
        let mut pages = PageSplitter::new(rs.clone(), "[pb9]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = pages.next() {
            assert!(true);
        } else { assert!(false) }
    }
}
