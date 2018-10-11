/*
 * Copyright (C) 2018  Minnesota Department of Transportation
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
use multi::*;
use raster::Raster;

/// Value result from parsing MULTI.
type UnitResult = Result<(), SyntaxError>;

/// Page render state
#[derive(Copy,Clone)]
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
    line_spacing    : Option<u8>,
    char_spacing    : Option<u8>,
    font            : (u8, Option<u16>),
}

/// Page splitter (iterator)
pub struct PageSplitter<'a> {
    default_state : State,
    state         : State,
    parser        : Parser<'a>,
    more          : bool,
}

/// Text span
pub struct TextSpan {
    state : State,   // render state at start of span
    text  : String,
}

impl TextSpan {
    fn new(state: State, text: String) -> Self {
        TextSpan { state, text }
    }
}

/// Page renderer
pub struct PageRenderer {
    state  : State,         // render state at start of page
    values : Vec<Value>,    // graphics / color rectangles
    spans  : Vec<TextSpan>, // text spans
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
            line_spacing : None,
            char_spacing : None,
            just_page,
            just_line,
            line_number : 0,
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
    /// Get the pixel width
    pub fn width(&self) -> u16 {
        self.text_rectangle.w
    }
    /// Get the pixel height
    pub fn height(&self) -> u16 {
        self.text_rectangle.h
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
    /// Check whether a color works for the color scheme.
    fn check_scheme(&self, c: &Color) -> UnitResult {
        match self.color_scheme {
            ColorScheme::Monochrome1Bit => self.check_monochrome_1_bit(c),
            ColorScheme::Monochrome8Bit => self.check_monochrome_8_bit(c),
            ColorScheme::ColorClassic   => self.check_classic(c),
            _                           => Ok(())
        }
    }
    /// Check color for a monochrome 1-bit scheme.
    fn check_monochrome_1_bit(&self, c: &Color) -> UnitResult {
        match c {
            Color::Legacy(0...1) => Ok(()),
            _                    => Err(SyntaxError::UnsupportedTagValue),
        }
    }
    /// Check color for a monochrome 8-bit scheme.
    fn check_monochrome_8_bit(&self, c: &Color) -> UnitResult {
        match c {
            Color::Legacy(_) => Ok(()),
            _                => Err(SyntaxError::UnsupportedTagValue),
        }
    }
    /// Check color for a classic scheme.
    fn check_classic(&self, c: &Color) -> UnitResult {
        match c {
            Color::Legacy(0...9) => Ok(()),
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
                self.check_scheme(c)?;
                self.page_background = *c;
            },
            Value::ColorForeground(None) => {
                self.color_foreground = default_state.color_foreground;
            },
            Value::ColorForeground(Some(c)) => {
                self.check_scheme(c)?;
                self.color_foreground = *c
            },
            Value::ColorRectangle(_,c) => {
                self.check_scheme(c)?;
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
            },
            Value::JustificationPage(Some(PageJustification::Other)) => {
                return Err(SyntaxError::UnsupportedTagValue);
            },
            Value::JustificationPage(jp) => {
                self.just_page = jp.unwrap_or(default_state.just_page);
                self.line_number = 0;
            },
            Value::NewLine(None) => {
                self.line_spacing = None;
                self.line_number += 1;
            },
            Value::NewLine(Some(ls)) => {
                if !self.is_full_matrix() {
                    return Err(SyntaxError::UnsupportedTagValue);
                }
                self.line_spacing = Some(*ls);
                self.line_number += 1;
            },
            Value::NewPage() => {
                self.line_number = 0;
            },
            Value::PageBackground(None) => {
                self.page_background = default_state.page_background;
            },
            Value::PageBackground(Some(c)) => {
                self.check_scheme(c)?;
                self.page_background = *c;
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
                return self.update_text_rectangle(default_state, r);
            },
            Value::Text(_) => (),
            _ => {
                // Unsupported tags: [f], [fl], [hc], [ms], [mv]
                return Err(SyntaxError::UnsupportedTag(v.to_string()));
            },
        }
        Ok(())
    }
    /// Update the text rectangle.
    fn update_text_rectangle(&mut self, default_state: &State,
        r: &Rectangle) -> UnitResult
    {
        // FIXME: handle zero width/height in rectangle
        if !default_state.text_rectangle.contains(r) {
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
        self.text_rectangle = *r;
        Ok(())
    }
    /// Get the page background color
    fn page_background(&self) -> Result<[u8;3], SyntaxError> {
        match self.page_background {
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
            0 => Ok([  0,   0,   0]), // FIXME: monochrome background color
            1 => Ok([255, 255, 255]), // FIXME: monochrome foreground color
            _ => Err(SyntaxError::UnsupportedTagValue),
        }
    }
    /// Get RGB triplet for a monochrome 8-bit color.
    fn color_rgb_monochrome_8_bit(&self, v: u8) -> Result<[u8;3], SyntaxError> {
        Ok([v,v,v])   // FIXME: use monochrome color
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
}

/*
impl<'a> Span<'a> {
    fn char_spacing(&self) -> u8 {
        let rs = self.state;
        match rs.char_spacing {
            Some(cs) => cs,
            _        => rs.font.char_spacing(),
        }
    }
    fn char_spacing_avg(&self, other: &Self) -> u8 {
        let sp0 = self.char_spacing();
        let sp1 = other.char_spacing();
        // NTCIP 1203 fontCharSpacing:
        // "... the average character spacing of the two fonts,
        // rounded up to the nearest whole pixel ..." ???
        ((sp0 + sp1) as f32 / 2f32).round() as u8
    }
    fn width(&self) -> u32 {
        let span = self.span;
        let cs = self.char_spacing();
        self.state.font.width(span, cs)
    }
    fn height(&self) -> u32 {
        self.state.font.height()
    }
    fn line_spacing(&self) -> u8 {
        let rs = self.state;
        match rs.line_spacing {
            Some(ls) => ls,
            _        => rs.font.line_spacing(),
        }
    }
    fn render(&mut self, raster: &mut Raster, left: u32, base: u32)
        -> UnitResult
    {
        let mut x = left;
        let y = base - self.height();
        let cs = self.char_spacing();
        let fg = self.state.color_foreground;
        for cp in self.span.chars() {
            let g = self.state.font.get_char(cp)?;
            raster.render_graphic(g, fg, x, y);
            x += g.width() + cs;
        }
        Ok(())
    }
}*/
/*
impl<'a> Fragment<'a> {
    fn height(&self) -> u32 {
        match self.spans.iter().map(|s| s.height()).max() {
            Some(h) => h,
            _       => 0,
        }
    }
    fn line_spacing(&self) -> u8 {
        match self.spans.iter().map(|s| s.line_spacing()).max() {
            Some(s) => s,
            _       => 0,
        }
    }
    fn render(&self, raster: &mut Raster, base: u32) -> UnitResult {
        let mut x = self.left()?;
        let pspan = None;
        for span in self.spans {
            if let Some(ps) = pspan {
                x += span.char_spacing_avg(ps);
            }
            span.render(raster, x, base)?;
            x += span.width();
            pspan = Some(&span);
        }
        Ok(())
    }
    fn left(&self) -> Result<u32, SyntaxError> {
        let ex = self.extra_width()?;
        let jl = self.state.just_line;
        let x = self.state.text_rectangle.x;
        match jl {
            // FIXME: add LineJustification::Full
            LineJustification::Left   => Ok(x),
            LineJustification::Center => Ok(x + self.char_width_floor(ex / 2)),
            LineJustification::Right  => Ok(x + ex),
            _                         => Err(SyntaxError::UnsupportedTagValue),
        }
    }
    fn extra_width(&self) -> Result<u32, SyntaxError> {
        let pw = self.state.text_rectangle.w;
        let tw = self.width();
        let cw = self.state.char_width();
        let w = pw / cw;
        let r = tw / cw;
        if w >= r {
            Ok((w - r) * cw)
        } else {
            Err(SyntaxError::TextTooBig)
        }
    }
    fn char_width_floor(&self, ex: u32) -> u32 {
        let cw = self.state.char_width();
        (ex / cw) * cw
    }
    fn width(&self) -> u32 {
        let mut w = 0;
        let pspan = None;
        for span in self.spans {
            let sw = span.width();
            if let Some(ps) = pspan {
                if sw > 0 {
                    w += sw + span.char_spacing_avg(ps);
                    pspan = Some(&span);
                }
            }
        }
        w
    }
}*/
/*
impl<'a> Line<'a> {
    fn height(&self) -> u32 {
        match self.fragments.iter().map(|f| f.height()).max() {
            Some(h) => h,
            _       => 0,
        }
    }
    fn line_spacing(&self) -> u8 {
        match self.fragments.iter().map(|f| f.line_spacing()).max() {
            Some(s) => s,
            _       => 0,
        }
    }
    fn line_spacing_avg(&self, other: &Self) -> u32 {
        let ls = self.state.line_spacing;
        match ls {
            Some(ls) => ls,
            _        => self.line_spacing_avg2(other),
        }
    }
    fn line_spacing_avg2(&self, other: &Self) -> u8 {
        let sp0 = self.line_spacing();
        let sp1 = other.line_spacing();
        // NTCIP 1203 fontLineSpacing:
        // "The number of pixels between adjacent lines
        // is the average of the 2 line spacings of each
        // line, rounded up to the nearest whole pixel."
        ((sp0 + sp1) as f32 / 2f32).round() as u32
    }
}*/
/*
impl<'a> Block<'a> {
    fn last_line(&mut self) -> &mut Line<'a> {
        let len = self.lines.len();
        if len == 0 {
            let line = Line::new(self.state);
            self.lines.push(line);
        }
        &mut self.lines[len - 1]
    }
    fn add_line(&mut self, ls: Option<u32>) {
        let line = self.last_line();
        if line.height() == 0 {
            // The line height can be zero on full-matrix
            // signs when no text has been specified.
            // Adding an empty span to the line allows the
            // height to be taken from the current font.
            line.add_span("".to_string());
        }
        self.state.line_spacing = ls;
        let line = Line::new(self.state);
        self.lines.push(line);
    }
    fn render(&mut self, raster: &mut Raster) -> UnitResult {
        let top = self.top()?;
        let mut y = 0;
        let mut pline = None;
        for line in self.lines {
            if let Some(pl) = pline {
                y += line.line_spacing_avg(pl);
            }
            y += line.height();
            line.render(raster, top + y)?;
            pline = Some(&line);
        }
        Ok(())
    }
    fn top(&self) -> Result<u32, SyntaxError> {
        let ex = self.extra_height()?;
        let jp = self.state.just_page;
        let y = self.state.text_rectangle.y;
        match jp {
            PageJustification::Top    => Ok(y),
            PageJustification::Middle => Ok(y + self.char_height_floor(ex / 2)),
            PageJustification::Bottom => Ok(y + ex),
            _                         => Err(SyntaxError::UnsupportedTagValue),
        }
    }
    fn extra_height(&self) -> Result<u32, SyntaxError> {
        let ph = self.state.text_rectangle.h;
        let ch = self.state.char_height();
        let h = ph / ch;
        let r = self.height() / ch;
        if h >= r {
            Ok((h - r) * ch)
        } else {
            Err(SyntaxError::TextTooBig)
        }
    }
    fn char_height_floor(&self, ex: u32) -> u32 {
        let ch = self.state.char_height();
        (ex / ch) * ch
    }
    fn height(&self) -> u32 {
        let mut h = 0;
        let pline = None;
        for line in self.lines {
            let lh = line.height();
            if let Some(pl) = pline {
                if lh > 0 {
                    h += lh + line.line_spacing_avg(pl);
                    pline = Some(&line);
                }
            }
        }
        h
    }
}*/
/*
impl Renderer {
    fn fill_rectangle(&mut self, r: Rectangle, clr: Color) {
        let x = r.x - 1;
        let y = r.y - 1;
        let w = r.w;
        let h = r.h;
        for yy in 0..h {
            for xx in 0..w {
                raster.set_pixel(x + xx, y + yy, clr);
            }
        }
    }
    fn set_text_rectangle(&mut self, r: Rectangle) -> UnitResult {
        self.draw_text()?;
        if self.default_state.text_rectangle.contains(&r) {
            self.state.text_rectangle = r;
            Ok(())
        } else {
            Err(SyntaxError::UnsupportedTagValue)
        }
    }
    fn draw_text(&mut self) -> UnitResult {
        for block in self.blocks {
            block.render();
        }
        self.blocks.clear();
        Ok(())
    }
}*/

impl PageRenderer {
    /// Create a new page renderer
    pub fn new(state: State, values: Vec<Value>, spans: Vec<TextSpan>) -> Self {
        PageRenderer {
            state,
            values,
            spans,
        }
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
        let rgba = [clr[0], clr[1], clr[2], 255];
        let page = Raster::new(w.into(), h.into(), rgba);
        Ok(page)
    }
    /// Render the page.
    pub fn render(&self) -> Result<Raster, SyntaxError> {
        let rs = self.state;
        let w = rs.text_rectangle.w;
        let h = rs.text_rectangle.h;
        let clr = rs.page_background()?;
        let rgba = [clr[0], clr[1], clr[2], 255];
        let mut page = Raster::new(w.into(), h.into(), rgba);
        for v in &self.values {
            match v {
                Value::ColorRectangle(_,_) => (), // FIXME
                Value::Graphic(_,_)        => (), // FIXME
                _                          => unreachable!(),
            }
        }
        let jp = PageJustification::Other;
        let jl = LineJustification::Other;
        for s in &self.spans {
            // FIXME: handle text rectangles
            let rs = s.state;
            if rs.just_page < jp || (rs.just_page == jp && rs.just_line < jl) {
                return Err(SyntaxError::TagConflict);
            }
            let jp = rs.just_page;
            let jl = rs.just_line;
            // FIXME: render text
println!("span: {}, {:?} {:?} : ln: {}", s.text, jp, jl, rs.line_number);
        }
        Ok(page)
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
                Value::Text(t) => {
                    let ts = TextSpan::new(self.state, t);
                    spans.push(ts);
                },
                Value::Graphic(_,_)|
                Value::ColorRectangle(_,_) => { values.push(v); },
                _ => (),
            }
        }
        // These values affect the entire page
        rs.page_background = self.state.page_background;
        rs.page_on_time_ds = self.state.page_on_time_ds;
        rs.page_off_time_ds = self.state.page_off_time_ds;
        Ok(PageRenderer::new(rs, values, spans))
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
