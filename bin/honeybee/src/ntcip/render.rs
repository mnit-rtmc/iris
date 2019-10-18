// render.rs
//
// Copyright (C) 2018-2019  Minnesota Department of Transportation
//
use crate::ntcip::font::Font;
use crate::ntcip::graphic::Graphic;
use crate::ntcip::multi::*;
use pix::{Raster, RasterBuilder, Rgb8};
use std::collections::HashMap;

/// Result type
type Result<T> = std::result::Result<T, SyntaxError>;

/// Convert BGR into Rgb8
fn bgr_to_rgb8(bgr: i32) -> Rgb8 {
    let r = (bgr >> 16) as u8;
    let g = (bgr >> 8) as u8;
    let b = (bgr >> 0) as u8;
    Rgb8::new(r, g, b)
}

/// Page render state
#[derive(Clone)]
pub struct State {
    color_ctx: ColorCtx,
    char_width: u8,
    char_height: u8,
    page_on_time_ds: u8, // deciseconds
    page_off_time_ds: u8, // deciseconds
    text_rectangle: Rectangle,
    just_page: PageJustification,
    just_line: LineJustification,
    line_number: u8,
    span_number: u8,
    line_spacing: Option<u8>,
    char_spacing: Option<u8>,
    font: (u8, Option<u16>),
}

/// Text span
struct TextSpan {
    /// Render state at start of span
    state: State,
    text: String,
}

/// Text line
struct TextLine {
    height: u16,
    font_spacing: u16,
    line_spacing: Option<u16>,
}

/// Page renderer
pub struct PageRenderer {
    /// Render state at start of page
    state: State,
    /// graphic / color rect, color context
    values: Vec<(Value, ColorCtx)>,
    /// text spans
    spans: Vec<TextSpan>,
}

/// Page splitter (iterator)
pub struct PageSplitter<'a> {
    default_state: State,
    state: State,
    parser: Parser<'a>,
    more_pages: bool,
    line_blank: bool,
}

impl State {
    /// Create a new render state.
    pub fn new(color_ctx        : ColorCtx,
               char_width       : u8,
               char_height      : u8,
               page_on_time_ds  : u8,
               page_off_time_ds : u8,
               text_rectangle   : Rectangle,
               just_page        : PageJustification,
               just_line        : LineJustification,
               font             : (u8, Option<u16>)) -> Self
    {
        State {
            color_ctx,
            char_width,
            char_height,
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
        r: Rectangle, v: &Value) -> Result<()>
    {
        let r = r.match_width_height(&default_state.text_rectangle);
        if !default_state.text_rectangle.contains(&r) {
            return Err(SyntaxError::UnsupportedTagValue(v.into()));
        }
        let cw = self.char_width();
        if cw > 0 {
            // Check text rectangle matches character boundaries
            let x = r.x - 1;
            if x % cw != 0 || r.w % cw != 0 {
                return Err(SyntaxError::UnsupportedTagValue(v.into()));
            }
        }
        let lh = self.char_height();
        if lh > 0 {
            // Check text rectangle matches line boundaries
            let y = r.y - 1;
            if y % lh != 0 || r.h % lh != 0 {
                return Err(SyntaxError::UnsupportedTagValue(v.into()));
            }
        }
        self.text_rectangle = r;
        Ok(())
    }
    /// Get the background RGB color.
    fn background_rgb(&self) -> Rgb8 {
        bgr_to_rgb8(self.color_ctx.background_bgr())
    }
    /// Get the foreground RGB color.
    fn foreground_rgb(&self) -> Rgb8 {
        bgr_to_rgb8(self.color_ctx.foreground_bgr())
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
    fn font(&self, fonts: &'a HashMap<u8, Font>) -> Result<&'a Font> {
        let fnum = self.state.font.0;
        match fonts.get(&fnum) {
            Some(f) => Ok(f),
            None => Err(SyntaxError::FontNotDefined(self.state.font.0)),
        }
    }
    /// Get the width of a text span
    fn width(&self, fonts: &HashMap<u8, Font>) -> Result<u16> {
        let font = self.font(fonts)?;
        let cs = self.char_spacing_fonts(fonts)?.into();
        Ok(font.text_width(&self.text, Some(cs))?)
    }
    /// Get the char spacing
    fn char_spacing_fonts(&self, fonts: &HashMap<u8, Font>) -> Result<u16> {
        match self.state.char_spacing {
            Some(s) => Ok(s as u16),
            None => Ok(self.font(fonts)?.char_spacing().into()),
        }
    }
    /// Get the char spacing
    fn char_spacing_font(&self, font: &Font) -> u32 {
        match self.state.char_spacing {
            Some(s) => s.into(),
            None    => font.char_spacing().into(),
        }
    }
    /// Get the char spacing from a previous span
    fn char_spacing_between(&self, prev: &TextSpan, fonts: &HashMap<u8, Font>)
        -> Result<u16>
    {
        if let Some(c) = self.state.char_spacing {
            Ok(c as u16)
        } else {
            // NTCIP 1203 fontCharSpacing:
            // "... the average character spacing of the two fonts,
            // rounded up to the nearest whole pixel ..." ???
            let psc = prev.char_spacing_fonts(fonts)?;
            let sc = self.char_spacing_fonts(fonts)?;
            Ok(((psc + sc) as f32 / 2.0).round() as u16)
        }
    }
    /// Get the height of a text span
    fn height(&self, fonts: &HashMap<u8, Font>) -> Result<u16> {
        Ok(self.font(fonts)?.height().into())
    }
    /// Get the font line spacing
    fn font_spacing(&self, fonts: &HashMap<u8, Font>) -> Result<u16> {
        Ok(self.font(fonts)?.line_spacing().into())
    }
    /// Get the line spacing
    fn line_spacing(&self) -> Option<u16> {
        match self.state.line_spacing {
            Some(s) => Some(s as u16),
            None    => None,
        }
    }
    /// Render the text span
    fn render_text(&self, page: &mut Raster<Rgb8>, font: &Font, x: u32, y: u32)
        -> Result<()>
    {
        let cs = self.char_spacing_font(font);
        let cf = self.state.foreground_rgb();
        Ok(font.render_text(page, &self.text, x, y, cs, cf)?)
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
            (s as f32 / 2.0).round() as u16
        }
    }
}

impl PageRenderer {
    /// Create a new page renderer
    fn new(state: State) -> Self {
        let values = vec![];
        let spans = vec![];
        PageRenderer { state, values, spans }
    }
    /// Check page and line justification ordering
    fn check_justification(&self) -> Result<()> {
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
    pub fn render_blank(&self) -> Raster<Rgb8> {
        let rs = &self.state;
        let w = rs.text_rectangle.w;
        let h = rs.text_rectangle.h;
        let clr = rs.background_rgb();
        RasterBuilder::new().with_color(w.into(), h.into(), clr)
    }
    /// Render the page.
    pub fn render_page(&self, fonts: &HashMap<u8, Font>,
        graphics: &HashMap<u8, Graphic>) -> Result<Raster<Rgb8>>
    {
        let rs = &self.state;
        let w = rs.text_rectangle.w.into();
        let h = rs.text_rectangle.h.into();
        let clr = rs.background_rgb();
        let mut page = RasterBuilder::new().with_color(w, h, clr);
        for (v, ctx) in &self.values {
            match v {
                Value::ColorRectangle(r, _) => {
                    let clr = bgr_to_rgb8(ctx.foreground_bgr());
                    self.render_rect(&mut page, *r, clr, v)?;
                },
                Value::Graphic(gn, None) => {
                    let n = *gn;
                    let g = graphics.get(&n)
                                    .ok_or(SyntaxError::GraphicNotDefined(*gn))?;
                    g.render_graphic(&mut page, 1, 1, ctx)?;
                },
                Value::Graphic(gn, Some((x,y,_))) => {
                    let n = *gn;
                    let g = graphics.get(&n)
                                    .ok_or(SyntaxError::GraphicNotDefined(*gn))?;
                    let x = *x as u32;
                    let y = *y as u32;
                    g.render_graphic(&mut page, x, y, ctx)?;
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
    fn render_rect(&self, page: &mut Raster<Rgb8>, r: Rectangle, clr: Rgb8,
        v: &Value) -> Result<()>
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
        Err(SyntaxError::UnsupportedTagValue(v.into()))
    }
    /// Get the X position of a text span.
    fn span_x(&self, s: &TextSpan, fonts: &HashMap<u8, Font>) -> Result<u16> {
        match s.state.just_line {
            LineJustification::Left   => self.span_x_left(s, fonts),
            LineJustification::Center => self.span_x_center(s, fonts),
            LineJustification::Right  => self.span_x_right(s, fonts),
            _                         => unreachable!(),
        }
    }
    /// Get the X position of a left-justified text span.
    fn span_x_left(&self, span: &TextSpan, fonts: &HashMap<u8, Font>)
        -> Result<u16>
    {
        let left = span.state.text_rectangle.x - 1;
        let (before, _) = self.offset_horiz(span, fonts)?;
        Ok(left + before)
    }
    /// Get the X position of a center-justified text span.
    fn span_x_center(&self, span: &TextSpan, fonts: &HashMap<u8, Font>)
        -> Result<u16>
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
    fn span_x_right(&self, span: &TextSpan, fonts: &HashMap<u8, Font>)
        -> Result<u16>
    {
        let left = span.state.text_rectangle.x - 1;
        let w = span.state.text_rectangle.w;
        let (_, after) = self.offset_horiz(span, fonts)?;
        Ok(left + w - after)
    }
    /// Calculate horizontal offsets of a span.
    ///
    /// Returns a tuple of (before, after) widths of matching spans.
    fn offset_horiz(&self, span: &TextSpan, fonts: &HashMap<u8, Font>)
        -> Result<(u16, u16)>
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
    fn span_y(&self, s: &TextSpan, fonts: &HashMap<u8, Font>) -> Result<u16> {
        let b = self.baseline(s, fonts)?;
        let h = s.height(fonts)?;
        debug_assert!(b >= h);
        Ok(b - h)
    }
    /// Get the baseline of a text span.
    fn baseline(&self, s: &TextSpan, fonts: &HashMap<u8, Font>) -> Result<u16> {
        match s.state.just_page {
            PageJustification::Top    => self.baseline_top(s, fonts),
            PageJustification::Middle => self.baseline_middle(s, fonts),
            PageJustification::Bottom => self.baseline_bottom(s, fonts),
            _                         => unreachable!(),
        }
    }
    /// Get the baseline of a top-justified span
    fn baseline_top(&self, span: &TextSpan, fonts: &HashMap<u8, Font>)
        -> Result<u16>
    {
        let top = span.state.text_rectangle.y - 1;
        let (above, _) = self.offset_vert(span, fonts)?;
        Ok(top + above)
    }
    /// Get the baseline of a middle-justified span
    fn baseline_middle(&self, span: &TextSpan, fonts: &HashMap<u8, Font>)
        -> Result<u16>
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
    fn baseline_bottom(&self, span: &TextSpan, fonts: &HashMap<u8, Font>)
        -> Result<u16>
    {
        let top = span.state.text_rectangle.y - 1;
        let h = span.state.text_rectangle.h;
        let (_, below) = self.offset_vert(span, fonts)?;
        Ok(top + h - below)
    }
    /// Calculate vertical offset of a span.
    ///
    /// Returns a tuple of (above, below) heights of matching lines.
    fn offset_vert(&self, span: &TextSpan, fonts: &HashMap<u8, Font>)
        -> Result<(u16, u16)>
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
    fn make_page(&mut self) -> Result<PageRenderer> {
        self.more_pages = false;
        self.line_blank = true;
        let mut page = PageRenderer::new(self.page_state());
        while let Some(v) = self.parser.next() {
            self.update_state(v?, &mut page)?;
            if self.more_pages { break; }
        }
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
    fn update_state(&mut self, v: Value, page: &mut PageRenderer) -> Result<()> {
        let ds = &self.default_state;
        let mut rs = &mut self.state;
        match v {
            Value::ColorBackground(c) => {
                // This tag remains for backward compatibility with 1203v1
                rs.color_ctx.set_background(c, &v)?;
                page.state.color_ctx.set_background(c, &v)?;
            },
            Value::ColorForeground(c) => {
                rs.color_ctx.set_foreground(c, &v)?;
            },
            Value::ColorRectangle(_, c) => {
                let mut ctx = rs.color_ctx.clone();
                // only set foreground color in cloned context
                ctx.set_foreground(Some(c), &v)?;
                page.values.push((v, ctx));
            },
            Value::Font(None) => { rs.font = ds.font },
            Value::Font(Some(f)) => { rs.font = f },
            Value::Graphic(_, _) =>  {
                page.values.push((v, rs.color_ctx.clone()));
            },
            Value::JustificationLine(Some(LineJustification::Other)) => {
                return Err(SyntaxError::UnsupportedTagValue(v.into()));
            },
            Value::JustificationLine(Some(LineJustification::Full)) => {
                return Err(SyntaxError::UnsupportedTagValue(v.into()));
            },
            Value::JustificationLine(jl) => {
                rs.just_line = jl.unwrap_or(ds.just_line);
                rs.span_number = 0;
            },
            Value::JustificationPage(Some(PageJustification::Other)) => {
                return Err(SyntaxError::UnsupportedTagValue(v.into()));
            },
            Value::JustificationPage(jp) => {
                rs.just_page = jp.unwrap_or(ds.just_page);
                rs.line_number = 0;
                rs.span_number = 0;
            },
            Value::NewLine(ls) => {
                if !rs.is_full_matrix() {
                    if let Some(_) = ls {
                        return Err(SyntaxError::UnsupportedTagValue(v.into()));
                    }
                }
                // Insert an empty text span for blank lines.
                if self.line_blank {
                    page.spans.push(TextSpan::new(rs.clone(), "".into()));
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
            Value::PageBackground(c) => {
                rs.color_ctx.set_background(c, &v)?;
                page.state.color_ctx.set_background(c, &v)?;
            },
            Value::PageTime(on, off) => {
                rs.page_on_time_ds = on.unwrap_or(ds.page_on_time_ds);
                rs.page_off_time_ds = off.unwrap_or(ds.page_off_time_ds);
                page.state.page_on_time_ds = on.unwrap_or(ds.page_on_time_ds);
                page.state.page_off_time_ds = off.unwrap_or(ds.page_off_time_ds);
            },
            Value::SpacingCharacter(sc) => {
                if rs.is_char_matrix() {
                    return Err(SyntaxError::UnsupportedTag(v.into()));
                }
                rs.char_spacing = Some(sc);
            },
            Value::SpacingCharacterEnd() => {
                if rs.is_char_matrix() {
                    return Err(SyntaxError::UnsupportedTag(v.into()));
                }
                rs.char_spacing = None;
            },
            Value::TextRectangle(r) => {
                self.line_blank = true;
                rs.line_number = 0;
                rs.span_number = 0;
                rs.update_text_rectangle(ds, r, &v)?;
            },
            Value::Text(t) => {
                page.spans.push(TextSpan::new(rs.clone(), t));
                rs.span_number += 1;
                self.line_blank = false;
            },
            Value::HexadecimalCharacter(hc) => {
                match std::char::from_u32(hc.into()) {
                    Some(c) => {
                        let mut t = String::new();
                        t.push(c);
                        page.spans.push(TextSpan::new(rs.clone(), t));
                        rs.span_number += 1;
                        self.line_blank = false;
                    },
                    None => {
                        // Invalid code point (surrogate in D800-DFFF range)
                        return Err(SyntaxError::UnsupportedTagValue(v.into()));
                    },
                }
            },
            _ => {
                // Unsupported tags: [f], [fl], [ms], [mv]
                return Err(SyntaxError::UnsupportedTag(v.into()));
            },
        }
        Ok(())
    }
}

impl<'a> Iterator for PageSplitter<'a> {
    type Item = Result<PageRenderer>;

    fn next(&mut self) -> Option<Result<PageRenderer>> {
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
    fn make_full_matrix() -> State {
        State::new(ColorCtx::new(ColorScheme::Color24Bit,
                                 ColorClassic::White.rgb(),
                                 ColorClassic::Black.rgb()),
                   0, 0,
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
        State::new(ColorCtx::new(ColorScheme::Monochrome1Bit,
                                 ColorClassic::White.rgb(),
                                 ColorClassic::Black.rgb()),
                   5, 7,
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
        if let Some(Err(SyntaxError::UnsupportedTagValue(_))) = pages.next() {
            assert!(true);
        } else { assert!(false) }
        let mut pages = PageSplitter::new(rs.clone(), "[tr1,1,50,12]");
        if let Some(Err(SyntaxError::UnsupportedTagValue(_))) = pages.next() {
            assert!(true);
        } else { assert!(false) }
        let mut pages = PageSplitter::new(rs.clone(), "[tr1,1,12,14]");
        if let Some(Err(SyntaxError::UnsupportedTagValue(_))) = pages.next() {
            assert!(true);
        } else { assert!(false) }
        let mut pages = PageSplitter::new(rs.clone(), "[tr1,1,50,14]");
        if let Some(Ok(_)) = pages.next() { assert!(true); }
        else { assert!(false) }
        let mut pages = PageSplitter::new(rs.clone(), "[pb9]");
        if let Some(Err(SyntaxError::UnsupportedTagValue(_))) = pages.next() {
            assert!(true);
        } else { assert!(false) }
    }
}
