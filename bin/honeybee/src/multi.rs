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
use std::error;
use std::fmt;
use std::iter::Peekable;
use std::str::Chars;
use std::str::FromStr;
use crate::error::Error;

/// DMS color scheme.
#[derive(Copy,Clone,PartialEq,Debug)]
pub enum ColorScheme {
    Monochrome1Bit = 1,
    Monochrome8Bit,
    ColorClassic,
    Color24Bit,
}

impl ColorScheme {
    pub fn from_str(s: &str) -> Result<Self, Error> {
        match s {
            "monochrome1Bit" => Ok(ColorScheme::Monochrome1Bit),
            "monochrome8Bit" => Ok(ColorScheme::Monochrome8Bit),
            "colorClassic"   => Ok(ColorScheme::ColorClassic),
            "color24Bit"     => Ok(ColorScheme::Color24Bit),
            _ => Err(Error::Other(format!("Unknown scheme: {:?}", s))),
        }
    }
}

/// Color for a DMS pixel.
/// Legacy colors are dependent on the DmsColorScheme.
#[derive(Copy,Clone,Debug,PartialEq)]
pub enum Color {
    Legacy(u8),      //    0-9 (colorClassic)
                     //    0-1 (monochrome1Bit)
                     // or 0-255 (monochrome8Bit)
    RGB(u8, u8, u8), // red, green and blue components
}

impl fmt::Display for Color {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            Color::Legacy(v)  => write!(f, "{}", v),
            Color::RGB(r,g,b) => write!(f, "{},{},{}", r, g, b),
        }
    }
}

impl From<i32> for Color {
    fn from(rgb: i32) -> Self {
        let r = (rgb >> 16) as u8;
        let g = (rgb >> 8) as u8;
        let b = (rgb >> 0) as u8;
        Color::RGB(r, g, b)
    }
}

impl From<ColorClassic> for Color {
    fn from(c: ColorClassic) -> Self {
        Color::Legacy(c as u8)
    }
}

/// Classic color values
#[derive(Copy,Clone,Debug,PartialEq)]
pub enum ColorClassic {
    Black,
    Red,
    Yellow,
    Green,
    Cyan,
    Blue,
    Magenta,
    White,
    Orange,
    Amber,
}

impl ColorClassic {
    /// Get RGB triplet for a classic color.
    pub fn rgb(&self) -> [u8;3] {
        match self {
            ColorClassic::Black   => [  0,  0,  0],
            ColorClassic::Red     => [255,  0,  0],
            ColorClassic::Yellow  => [255,255,  0],
            ColorClassic::Green   => [  0,255,  0],
            ColorClassic::Cyan    => [  0,255,255],
            ColorClassic::Blue    => [  0,  0,255],
            ColorClassic::Magenta => [255,  0,255],
            ColorClassic::White   => [255,255,255],
            ColorClassic::Orange  => [255,165,  0],
            ColorClassic::Amber   => [255,208,  0],
        }
    }
    /// Maybe convert a u8 into a ColorClassic
    pub fn from_u8(v: u8) -> Option<Self> {
        match v {
            v if v == ColorClassic::Black   as u8 => Some(ColorClassic::Black),
            v if v == ColorClassic::Red     as u8 => Some(ColorClassic::Red),
            v if v == ColorClassic::Yellow  as u8 => Some(ColorClassic::Yellow),
            v if v == ColorClassic::Green   as u8 => Some(ColorClassic::Green),
            v if v == ColorClassic::Cyan    as u8 => Some(ColorClassic::Cyan),
            v if v == ColorClassic::Blue    as u8 => Some(ColorClassic::Blue),
            v if v == ColorClassic::Magenta as u8 => Some(ColorClassic::Magenta),
            v if v == ColorClassic::White   as u8 => Some(ColorClassic::White),
            v if v == ColorClassic::Orange  as u8 => Some(ColorClassic::Orange),
            v if v == ColorClassic::Amber   as u8 => Some(ColorClassic::Amber),
            _                                     => None,
        }
    }
}

/// A rectangular area of a DMS.
///
/// * `x` Left edge (starting from 1).
/// * `y` Top edge (starting from 1).
/// * `w` Width (pixels).
/// * `h` Height (pixels).
#[derive(Copy,Clone,Debug,PartialEq)]
pub struct Rectangle {
    pub x: u16,
    pub y: u16,
    pub w: u16,
    pub h: u16,
}

impl fmt::Display for Rectangle {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{},{},{},{}", self.x, self.y, self.w, self.h)
    }
}

impl Rectangle {
    /// Create a new rectangle
    pub fn new(x: u16, y: u16, w: u16, h: u16) -> Self {
        Rectangle { x, y, w, h }
    }
    /// Create a rectangle matching another width and/or height
    pub fn match_width_height(&self, other: &Self) -> Self {
        let w = if self.w > 0 { self.w } else { 1 + other.w - self.x };
        let h = if self.h > 0 { self.h } else { 1 + other.h - self.y };
        Rectangle::new(self.x, self.y, w, h)
    }
    /// Check if a rectangle contains another rectangle
    pub fn contains(&self, other: &Self) -> bool {
        other.x >= self.x && other.x + other.w <= self.x + self.w &&
        other.y >= self.y && other.y + other.h <= self.y + self.h
    }
}

/// Order of flashing messages.
#[derive(Copy,Clone,Debug,PartialEq)]
pub enum FlashOrder {
    OnOff,
    OffOn,
}

/// Horizontal justification within a line.
#[derive(Copy,Clone,PartialEq,PartialOrd,Debug)]
pub enum LineJustification {
    Other = 1,
    Left,
    Center,
    Right,
    Full,
}

impl fmt::Display for LineJustification {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        let v = (*self).clone();
        write!(f, "{}", v as u8)
    }
}

impl LineJustification {
    /// Create a line justification.
    fn new(v: &str) -> Option<Self> {
        match v {
            "1" => Some(LineJustification::Other),
            "2" => Some(LineJustification::Left),
            "3" => Some(LineJustification::Center),
            "4" => Some(LineJustification::Right),
            "5" => Some(LineJustification::Full),
            _   => None,
        }
    }
}

/// Vertical justification within a page.
#[derive(Copy,Clone,PartialEq,PartialOrd,Debug)]
pub enum PageJustification {
    Other = 1,
    Top,
    Middle,
    Bottom,
}

impl fmt::Display for PageJustification {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        let v = (*self).clone();
        write!(f, "{}", v as u8)
    }
}

impl PageJustification {
    /// Create a page justification.
    fn new(v: &str) -> Option<Self> {
        match v {
            "1" => Some(PageJustification::Other),
            "2" => Some(PageJustification::Top),
            "3" => Some(PageJustification::Middle),
            "4" => Some(PageJustification::Bottom),
            _   => None,
        }
    }
}

/// Mode for moving text.
#[derive(Copy,Clone,Debug,PartialEq)]
pub enum MovingTextMode {
    Circular,
    Linear(u8),
}

impl fmt::Display for MovingTextMode {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            MovingTextMode::Circular  => write!(f, "c"),
            MovingTextMode::Linear(x) => write!(f, "l{}", x),
        }
    }
}

/// Direction for moving text.
#[derive(Copy,Clone,Debug,PartialEq)]
pub enum MovingTextDirection {
    Left,
    Right,
}

impl fmt::Display for MovingTextDirection {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            MovingTextDirection::Left => write!(f, "l"),
            MovingTextDirection::Right => write!(f, "r"),
        }
    }
}

/// Values returned from a parsed MULTI.
#[derive(Clone,Debug,PartialEq)]
pub enum Value {
    ColorBackground(Option<Color>), // Legacy colors only
    ColorForeground(Option<Color>),
    ColorRectangle(Rectangle, Color),
    Field(u8, Option<u8>),
    Flash(FlashOrder, Option<u8>, Option<u8>),
    FlashEnd(),
    Font(Option<(u8, Option<u16>)>),
    Graphic(u8, Option<(u16, u16, Option<u16>)>),
    HexadecimalCharacter(u16),
    JustificationLine(Option<LineJustification>),
    JustificationPage(Option<PageJustification>),
    ManufacturerSpecific(u32, Option<String>),
    ManufacturerSpecificEnd(u32, Option<String>),
    MovingText(MovingTextMode, MovingTextDirection, u16, u8, u8, String),
    NewLine(Option<u8>),
    NewPage(),
    PageBackground(Option<Color>),
    PageTime(Option<u8>, Option<u8>),
    SpacingCharacter(u8),
    SpacingCharacterEnd(),
    Text(String),
    TextRectangle(Rectangle),
}

impl fmt::Display for Value {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            Value::ColorBackground(None) => write!(f, "[cb]"),
            Value::ColorBackground(Some(c)) => write!(f, "[cb{}]", c),
            Value::ColorForeground(None) => write!(f, "[cf]"),
            Value::ColorForeground(Some(c)) => write!(f, "[cf{}]", c),
            Value::ColorRectangle(r,c) => write!(f, "[cr{},{}", r, c),
            Value::Field(i,Some(w))    => write!(f, "[f{},{}]", i, w),
            Value::Field(i,None)       => write!(f, "[f{}]", i),
            Value::Flash(FlashOrder::OnOff,Some(a),Some(b))
                                       => write!(f, "[flt{}o{}]", a, b),
            Value::Flash(FlashOrder::OnOff,Some(a),None)
                                       => write!(f, "[flt{}o]", a),
            Value::Flash(FlashOrder::OnOff,None,Some(b))
                                       => write!(f, "[flto{}]", b),
            Value::Flash(FlashOrder::OnOff,None,None)
                                       => write!(f, "[flto]"),
            Value::Flash(FlashOrder::OffOn,Some(a),Some(b))
                                       => write!(f, "[flo{}t{}]", a, b),
            Value::Flash(FlashOrder::OffOn,Some(a),None)
                                       => write!(f, "[flo{}t]", a),
            Value::Flash(FlashOrder::OffOn,None,Some(b))
                                       => write!(f, "[flot{}]", b),
            Value::Flash(FlashOrder::OffOn,None,None)
                                       => write!(f, "[flot]"),
            Value::FlashEnd()          => write!(f, "[/fl]"),
            Value::Font(None)          => write!(f, "[fo]"),
            Value::Font(Some((i,None)))=> write!(f, "[fo{}]", i),
            Value::Font(Some((i,Some(c))))
                                       => write!(f, "[fo{},{:04x}]", i, c),
            Value::Graphic(i,None)     => write!(f, "[g{}]", i),
            Value::Graphic(i,Some((x,y,None)))
                                       => write!(f, "[g{},{},{}]", i, x, y),
            Value::Graphic(i,Some((x,y,Some(c))))
                                       => write!(f, "[g{},{},{},{:04x}]",
                                                 i, x, y, c),
            Value::HexadecimalCharacter(c)
                                       => write!(f, "[hc{}]", c),
            Value::JustificationLine(Some(j))
                                       => write!(f, "[jl{}]", j),
            Value::JustificationLine(None)
                                       => write!(f, "[jl]"),
            Value::JustificationPage(Some(j))
                                       => write!(f, "[jp{}]", j),
            Value::JustificationPage(None)
                                       => write!(f, "[jp]"),
            Value::ManufacturerSpecific(i, Some(s))
                                       => write!(f, "[ms{},{}]", i, s),
            Value::ManufacturerSpecific(i, None)
                                       => write!(f, "[ms{}]", i),
            Value::ManufacturerSpecificEnd(i, Some(s))
                                       => write!(f, "[/ms{},{}]", i, s),
            Value::ManufacturerSpecificEnd(i, None)
                                       => write!(f, "[/ms{}]", i),
            Value::MovingText(t, d, w, s, r, text)
                                       => write!(f, "[mv{}{}{},{},{},{}]",
                                                 t, d, w, s, r, text),
            Value::NewLine(Some(x))    => write!(f, "[nl{}]", x),
            Value::NewLine(None)       => write!(f, "[nl]"),
            Value::NewPage()           => write!(f, "[np]"),
            Value::PageBackground(Some(c))
                                       => write!(f, "[pb{}]", c),
            Value::PageBackground(None)=> write!(f, "[pb]"),
            Value::PageTime(Some(x),Some(y))
                                       => write!(f, "[pt{}o{}]", x, y),
            Value::PageTime(Some(x),None)
                                       => write!(f, "[pt{}o]", x),
            Value::PageTime(None,Some(y))
                                       => write!(f, "[pto{}]", y),
            Value::PageTime(None,None) => write!(f, "[pto]"),
            Value::SpacingCharacter(s) => write!(f, "[sc{}]", s),
            Value::SpacingCharacterEnd()
                                       => write!(f, "[/sc]"),
            Value::Text(t)             => write!(f, "{}", t.replace('[', "[[")
                                                           .replace(']', "]]")),
            Value::TextRectangle(r)    => write!(f, "[tr{}]", r),
        }
    }
}

/// Syntax errors from parsing MULTI.
#[derive(Clone,Debug,PartialEq)]
pub enum SyntaxError {
    Other,
    UnsupportedTag(String),
    UnsupportedTagValue,
    TextTooBig,
    FontNotDefined(u8),
    CharacterNotDefined(char),
    FieldDeviceNotExist,
    FieldDeviceError,
    FlashRegionError,
    TagConflict,
    TooManyPages,
    FontVersionID,
    GraphicID,
    GraphicNotDefined(u8),
}

impl fmt::Display for SyntaxError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "syntaxError: {:?}", self)
    }
}

impl error::Error for SyntaxError { }

/// Parser for MULTI values.
pub struct Parser<'a> {
    remaining : Peekable<Chars<'a>>,
    tag       : bool,
}

/// Parse a color from a tag.
///
/// * `v` Iterator of color parameters.
fn parse_color<'a, I>(v: I) -> Result<Color, SyntaxError>
    where I: Iterator<Item = &'a str>
{
    match v.map(|i| i.parse::<u8>()).collect::<Vec<_>>().as_slice() {
        [Ok(n)]             => Ok(Color::Legacy(*n)),
        [Ok(r),Ok(g),Ok(b)] => Ok(Color::RGB(*r,*g,*b)),
        _                   => Err(SyntaxError::UnsupportedTagValue),
    }
}

/// Parse a rectangle from a tag.
///
/// * `v` Iterator of rectangle parameters.
fn parse_rectangle<'a, I>(v: &mut I) -> Result<Rectangle, SyntaxError>
    where I: Iterator<Item = &'a str>
{
    if let (Some(x),Some(y),Some(w),Some(h)) = (v.next(),v.next(),v.next(),
        v.next())
    {
        if let (Ok(x),Ok(y),Ok(w),Ok(h)) = (x.parse(),y.parse(),w.parse(),
            h.parse())
        {
            if x > 0 && y > 0 {
                return Ok(Rectangle::new(x, y, w, h));
            }
        }
    }
    Err(SyntaxError::UnsupportedTagValue)
}

/// Parse an integer value.
fn parse_int<'a, I, T>(v: &mut I) -> Result<T, SyntaxError>
    where I: Iterator<Item = &'a str>,
          T: FromStr
{
    if let Some(s) = v.next() {
        if let Ok(i) = s.parse::<T>() {
            return Ok(i);
        }
    }
    Err(SyntaxError::UnsupportedTagValue)
}

/// Parse an optional value.
fn parse_optional<'a, I, T>(v: &mut I) -> Result<Option<T>, SyntaxError>
    where I: Iterator<Item = &'a str>,
          T: FromStr
{
    if let Some(s) = v.next() {
        if s.len() == 0 { return Ok(None); }
        if let Ok(i) = s.parse::<T>() {
            Ok(Some(i))
        } else {
            Err(SyntaxError::UnsupportedTagValue)
        }
    } else {
        Ok(None)
    }
}

/// Parse an optional value ranging from 0 to 99.
fn parse_optional_99<'a, I>(v: &mut I) -> Result<Option<u8>, SyntaxError>
    where I: Iterator<Item = &'a str>
{
    if let Some(s) = v.next() {
        if s.len() == 0 { return Ok(None); }
        if let Ok(i) = s.parse::<u8>() {
            if i <= 99 { return Ok(Some(i)); }
        }
        return Err(SyntaxError::UnsupportedTagValue);
    }
    Ok(None)
}

/// Parse a nonzero value.
fn parse_nonzero<'a, I, T>(v: &mut I) -> Result<T, SyntaxError>
    where I: Iterator<Item = &'a str>,
          T: FromStr+PartialOrd+Default
{
    if let Some(s) = v.next() {
        if let Ok(i) = s.parse::<T>() {
            // Use default to check for nonzero
            if i != T::default() { return Ok(i); }
        }
    }
    Err(SyntaxError::UnsupportedTagValue)
}

/// Parse a version ID value.
fn parse_version_id<'a, I>(v: &mut I) -> Result<Option<u16>, SyntaxError>
    where I: Iterator<Item = &'a str>
{
    if let Some(s) = v.next() {
        // Must be exactly 4 hexadecimal characters
        if s.len() == 4 {
            if let Ok(i) = u16::from_str_radix(s, 16) {
                return Ok(Some(i));
            }
        }
        return Err(SyntaxError::UnsupportedTagValue);
    }
    return Ok(None);
}

/// Finish parsing a tag.
fn parse_done<'a, I>(v: &mut I, res: ValueResult) -> ValueResult
    where I: Iterator<Item = &'a str>
{
    if let None = v.next() { res }
    else { Err(SyntaxError::UnsupportedTagValue) }
}

/// Value result from parsing MULTI.
type ValueResult = Result<Option<Value>, SyntaxError>;

/// Parse a Color -- Background tag (cb).
fn parse_color_background(tag: &str) -> ValueResult {
    if tag.len() > 2 {
        // 1203 specifies a numeric value between 0 and 999,
        // but anything above 255 does not make sense
        match tag[2..].parse::<u8>() {
            Ok(n)  => Ok(Some(Value::ColorBackground(Some(Color::Legacy(n))))),
            Err(_) => Err(SyntaxError::UnsupportedTagValue),
        }
    } else {
        Ok(Some(Value::ColorBackground(None)))
    }
}

/// Parse a Page -- Background tag [pb].
fn parse_page_background(tag: &str) -> ValueResult {
    if tag.len() > 2 {
        let c = parse_color(tag[2..].split(","))?;
        Ok(Some(Value::PageBackground(Some(c))))
    } else {
        Ok(Some(Value::PageBackground(None)))
    }
}

/// Parse a Color -- Foreground tag [cf].
fn parse_color_foreground(tag: &str) -> ValueResult {
    if tag.len() > 2 {
        let c = parse_color(tag[2..].split(","))?;
        Ok(Some(Value::ColorForeground(Some(c))))
    } else {
        Ok(Some(Value::ColorForeground(None)))
    }
}

/// Parse a Color Rectangle tag [cr].
fn parse_color_rectangle(tag: &str) -> ValueResult {
    let mut vs = tag[2..].split(",");
    let r = parse_rectangle(&mut vs)?;
    let c = parse_color(vs)?;
    Ok(Some(Value::ColorRectangle(r, c)))
}

/// Parse a Field tag [f].
fn parse_field(tag: &str) -> ValueResult {
    // Field tag "f" must be followed by a decimal digit.
    // If not, return UnsupportedTag instead of UnsupportedTagValue to allow
    // handling non-MULTI tags starting with "f" (e.g. [feedx]).
    if let Some(c) = tag.chars().nth(1) {
        if !c.is_digit(10) {
            return Err(SyntaxError::UnsupportedTag(tag.to_string()));
        }
    }
    let mut vs = tag[1..].split(",");
    let fid = parse_int(&mut vs)?;
    if fid > 99 {
        return Err(SyntaxError::UnsupportedTagValue);
    }
    let w = parse_optional(&mut vs)?;
    parse_done(&mut vs, Ok(Some(Value::Field(fid, w))))
}

/// Parse a Flash time tag [fl].
fn parse_flash_time(tag: &str) -> ValueResult {
    if tag.len() > 2 {
        let v = &tag[2..];
        match &v[..1] {
            "t" => parse_flash_on(&v[1..]),
            "o" => parse_flash_off(&v[1..]),
            _   => Err(SyntaxError::UnsupportedTagValue),
        }
    } else {
        Ok(Some(Value::Flash(FlashOrder::OnOff, None, None)))
    }
}

/// Parse a flash on -> off tag fragment.
fn parse_flash_on(v: &str) -> ValueResult {
    let mut vs = v.split("o");
    let t = parse_optional_99(&mut vs)?;
    let o = parse_optional_99(&mut vs)?;
    parse_done(&mut vs, Ok(Some(Value::Flash(FlashOrder::OnOff, t, o))))
}

/// Parse a flash off -> on tag fragment.
fn parse_flash_off(v: &str) -> ValueResult {
    let mut vs = v.split("t");
    let o = parse_optional_99(&mut vs)?;
    let t = parse_optional_99(&mut vs)?;
    parse_done(&mut vs, Ok(Some(Value::Flash(FlashOrder::OffOn, o, t))))
}

/// Parse a flash end tag [/fl].
fn parse_flash_end(tag: &str) -> ValueResult {
    if tag.len() == 3 {
        Ok(Some(Value::FlashEnd()))
    } else {
        Err(SyntaxError::UnsupportedTagValue)
    }
}

/// Parse a Font tag [fo]
fn parse_font(tag: &str) -> ValueResult {
    if tag.len() > 2 {
        let mut vs = tag[2..].split(",");
        let n = parse_nonzero(&mut vs)?;
        let vid = parse_version_id(&mut vs)?;
        parse_done(&mut vs, Ok(Some(Value::Font(Some((n, vid))))))
    } else {
        Ok(Some(Value::Font(None)))
    }
}

/// Parse a Graphic tag [g]
fn parse_graphic(tag: &str) -> ValueResult {
    let mut vs = tag[1..].split(",");
    let n = parse_nonzero(&mut vs)?;
    let xy = parse_point(&mut vs)?;
    let r = if let Some((x, y)) = xy {
        Some((x, y, parse_version_id(&mut vs)?))
    } else {
        None
    };
    parse_done(&mut vs, Ok(Some(Value::Graphic(n, r))))
}

/// Parse a pont value.
fn parse_point<'a, I>(v: &mut I) -> Result<Option<(u16, u16)>, SyntaxError>
    where I: Iterator<Item = &'a str>
{
    match (v.next(), v.next()) {
        (Some(x), Some(y)) => parse_xy(x, y),
        (Some(_), None)    => Err(SyntaxError::UnsupportedTagValue),
        (_, _)             => Ok(None),
    }
}

/// Parse an x/y pair.
fn parse_xy(x: &str, y: &str) -> Result<Option<(u16, u16)>, SyntaxError> {
    if let (Ok(x), Ok(y)) = (x.parse(), y.parse()) {
        if x > 0 && y > 0 {
            return Ok(Some((x, y)));
        }
    }
    Err(SyntaxError::UnsupportedTagValue)
}

/// Parse a hexadecimal character tag [hc].
fn parse_hexadecimal_character(tag: &str) -> ValueResult {
    let mut vs = tag[2..].split(",");
    let hc = parse_hexadecimal(&mut vs)?;
    parse_done(&mut vs, Ok(Some(Value::HexadecimalCharacter(hc))))
}

/// Parse a hexadecimal value.
fn parse_hexadecimal<'a, I>(v: &mut I) -> Result<u16, SyntaxError>
    where I: Iterator<Item = &'a str>
{
    if let Some(s) = v.next() {
        // May be 1 to 4 hexadecimal digits
        if let Ok(i) = u16::from_str_radix(s, 16) {
            return Ok(i);
        }
    }
    return Err(SyntaxError::UnsupportedTagValue);
}

/// Parse a Justification -- Line tag [jl].
fn parse_justification_line(tag: &str) -> ValueResult {
    if tag.len() > 2 {
        if let Some(jl) = LineJustification::new(&tag[2..]) {
            Ok(Some(Value::JustificationLine(Some(jl))))
        } else {
            Err(SyntaxError::UnsupportedTagValue)
        }
    } else {
        Ok(Some(Value::JustificationLine(None)))
    }
}

/// Parse a Justification -- Page tag [jp].
fn parse_justification_page(tag: &str) -> ValueResult {
    if tag.len() > 2 {
        if let Some(jl) = PageJustification::new(&tag[2..]) {
            Ok(Some(Value::JustificationPage(Some(jl))))
        } else {
            Err(SyntaxError::UnsupportedTagValue)
        }
    } else {
        Ok(Some(Value::JustificationPage(None)))
    }
}

/// Parse a Manufacturer Specific tag [ms].
fn parse_manufacturer_specific(tag: &str) -> ValueResult {
    let mut vs = tag[2..].split(",");
    let m: u32 = parse_int(&mut vs)?;
    let p = vs.next();
    if let None = vs.next() {
        if let Some(t) = p {
            Ok(Some(Value::ManufacturerSpecific(m, Some(t.to_string()))))
        } else {
            Ok(Some(Value::ManufacturerSpecific(m, None)))
        }
    } else {
        Err(SyntaxError::UnsupportedTagValue)
    }
}

/// Parse a Manufacturer Specific end tag [/ms].
fn parse_manufacturer_specific_end(tag: &str) -> ValueResult {
    let mut vs = tag[3..].split(",");
    let m: u32 = parse_int(&mut vs)?;
    let p = vs.next();
    if let None = vs.next() {
        if let Some(t) = p {
            Ok(Some(Value::ManufacturerSpecificEnd(m, Some(t.to_string()))))
        } else {
            Ok(Some(Value::ManufacturerSpecificEnd(m, None)))
        }
    } else {
        Err(SyntaxError::UnsupportedTagValue)
    }
}

/// Parse a Moving text tag [mv].
fn parse_moving_text(tag: &str) -> ValueResult {
    if tag.len() > 2 {
        let t = &tag[3..];
        match &tag[2..3] {
            "c" | "C" => parse_moving_text_mode(t, MovingTextMode::Circular),
            "l" | "L" => parse_moving_text_linear(t),
            _         => Err(SyntaxError::UnsupportedTagValue),
        }
    } else {
        Err(SyntaxError::UnsupportedTagValue)
    }
}

/// Parse a moving text linear fragment.
fn parse_moving_text_linear(tag: &str) -> ValueResult {
    if tag.len() > 0 {
        let t = &tag[1..];
        if let Ok(i) = &tag[..1].parse::<u8>() {
            parse_moving_text_mode(t, MovingTextMode::Linear(*i))
        } else {
            parse_moving_text_mode(tag, MovingTextMode::Linear(0))
        }
    } else {
        Err(SyntaxError::UnsupportedTagValue)
    }
}

/// Parse a moving text mode fragment.
fn parse_moving_text_mode(tag: &str, m: MovingTextMode) -> ValueResult {
    if tag.len() > 0 {
        let d = match &tag[..1] {
            "l" | "L" => Ok(MovingTextDirection::Left),
            "r" | "R" => Ok(MovingTextDirection::Right),
            _         => Err(SyntaxError::UnsupportedTagValue),
        }?;
        let mut vs = tag[1..].split(",");
        let w = parse_int(&mut vs)?;
        let s = parse_int(&mut vs)?;
        let r = parse_int(&mut vs)?;
        if let (Some(t), None) = (vs.next(), vs.next()) {
            return Ok(Some(Value::MovingText(m, d, w, s, r, String::from(t))));
        }
    }
    Err(SyntaxError::UnsupportedTagValue)
}

/// Parse a New Line tag [nl].
fn parse_new_line(tag: &str) -> ValueResult {
    // 1203 only specifies a single digit parameter for "nl" tag (0-9)
    match tag.len() {
        2 => Ok(Some(Value::NewLine(None))),
        3 => match tag[2..].parse::<u8>() {
                 Ok(n)  => Ok(Some(Value::NewLine(Some(n)))),
                 Err(_) => Err(SyntaxError::UnsupportedTagValue),
             },
        _ => Err(SyntaxError::UnsupportedTagValue),
    }
}

/// Parse a New Page tag [np].
fn parse_new_page(tag: &str) -> ValueResult {
    if tag.len() == 2 {
        Ok(Some(Value::NewPage()))
    } else {
        Err(SyntaxError::UnsupportedTagValue)
    }
}

/// Parse a Page Time tag [pt].
fn parse_page_time(tag: &str) -> ValueResult {
    let mut vs = tag[2..].split("o");
    let t = parse_optional(&mut vs)?;
    let o = parse_optional(&mut vs)?;
    parse_done(&mut vs, Ok(Some(Value::PageTime(t, o))))
}

/// Parse a Spacing -- Character tag [sc].
fn parse_spacing_character(tag: &str) -> ValueResult {
    // Not really looking for commas -- just need an iterator
    let mut vs = tag[2..].split(",");
    let s = parse_int(&mut vs)?;
    if s <= 99 {
        parse_done(&mut vs, Ok(Some(Value::SpacingCharacter(s))))
    } else {
        Err(SyntaxError::UnsupportedTagValue)
    }
}

/// Parse a Spacing -- Character end tag [/sc].
fn parse_spacing_character_end(tag: &str) -> ValueResult {
    if tag.len() == 3 {
        Ok(Some(Value::SpacingCharacterEnd()))
    } else {
        Err(SyntaxError::UnsupportedTagValue)
    }
}

/// Parse a Text Rectangle tag [tr].
fn parse_text_rectangle(tag: &str) -> ValueResult {
    let mut vs = tag[2..].split(",");
    let r = parse_rectangle(&mut vs)?;
    parse_done(&mut vs, Ok(Some(Value::TextRectangle(r))))
}

/// Parse a tag (without brackets).
fn parse_tag(tag: &str) -> ValueResult {
    match tag.len() {
        0 => Err(SyntaxError::UnsupportedTag(tag.to_string())),
        1 => parse_tag1(tag),
        _ => parse_tag2(tag),
    }
}

/// Parse a tag with exactly one character.
fn parse_tag1(tag: &str) -> ValueResult {
    match tag {
        "f"|"F"|"g"|"G" => Err(SyntaxError::UnsupportedTagValue),
        _               => Err(SyntaxError::UnsupportedTag(tag.to_string())),
    }
}

/// Parse a tag with 2 or more characters.
fn parse_tag2(tag: &str) -> ValueResult {
    let tl = &tag.to_ascii_lowercase();
    let t = tl.as_str();
    match &t[..2] {
        // Sorted by most likely occurrence
        "nl" => parse_new_line(t),
        "np" => parse_new_page(t),
        "fo" => parse_font(t),
        "jl" => parse_justification_line(t),
        "jp" => parse_justification_page(t),
        "pt" => parse_page_time(t),
        "pb" => parse_page_background(t),
        "cf" => parse_color_foreground(t),
        "cr" => parse_color_rectangle(t),
        "tr" => parse_text_rectangle(t),
        "cb" => parse_color_background(t),
        "sc" => parse_spacing_character(t),
        "hc" => parse_hexadecimal_character(t),
        "fl" => parse_flash_time(t),
        "mv" => parse_moving_text(tag),
        "ms" => parse_manufacturer_specific(tag),
        _    => parse_tag_special(tag, t),
    }
}

/// Parse a special tag (single character or end tag).
///
/// * `tag` Tag value (without brackets).
/// * `t` Tag value converted to lower case.
fn parse_tag_special(tag: &str, t: &str) -> ValueResult {
    match &t[..1] {
        "g" => parse_graphic(tag),
        "f" => parse_field(tag),
        "/" => parse_tag_end(tag, t),
        _   => Err(SyntaxError::UnsupportedTag(tag.to_string())),
    }
}

/// Parse an end tag.
fn parse_tag_end(tag: &str, t: &str) -> ValueResult {
    match &t[..3] {
        "/sc" => parse_spacing_character_end(tag),
        "/fl" => parse_flash_end(tag),
        "/ms" => parse_manufacturer_specific_end(tag),
        _     => Err(SyntaxError::UnsupportedTag(tag.to_string())),
    }
}

impl<'a> Parser<'a> {
    /// Create a new MULTI parser.
    pub fn new(m: &'a str) -> Self {
        Parser { remaining: m.chars().peekable(), tag: false }
    }

    /// Peek at the next character.
    fn peek_char(&mut self) -> Option<char> {
        if let Some(c) = self.remaining.peek() {
            Some(*c)
        } else {
            None
        }
    }

    /// Get the next character.
    fn next_char(&mut self) -> Result<Option<char>, SyntaxError> {
        if let Some(c) = self.remaining.next() {
            // NTCIP 1203 mentions Extended ASCII (codepage 437) -- don't do it!
            match c {
                ' '...'~' => Ok(Some(c)),
                _         => Err(SyntaxError::CharacterNotDefined(c)),
            }
        } else {
            Ok(None)
        }
    }

    /// Parse a tag starting at the current position.
    fn parse_tag(&mut self) -> ValueResult {
        let mut s = String::new();
        while let Some(c) = self.next_char()? {
            match c {
                '[' => return Err(SyntaxError::UnsupportedTagValue),
                ']' => return parse_tag(&s),
                _   => s.push(c),
            }
        }
        Err(SyntaxError::UnsupportedTag(s))
    }

    /// Parse a value at the current position.
    fn parse_value(&mut self) -> ValueResult {
        if self.tag {
            self.tag = false;
            return self.parse_tag();
        }
        let mut s = String::new();
        while let Some(c) = self.next_char()? {
            if c == '[' {
                if let Some('[') = self.peek_char() {
                    self.next_char()?;
                } else if s.len() > 0 {
                    self.tag = true;
                    break;
                } else {
                    return self.parse_tag();
                }
            } else if c == ']' {
                if let Some(']') = self.peek_char() {
                    self.next_char()?;
                } else {
                    return Err(SyntaxError::UnsupportedTag(s));
                }
            }
            s.push(c);
        }
        if s.len() > 0 {
            Ok(Some(Value::Text(s)))
        } else {
            Ok(None)
        }
    }
}

impl<'a> Iterator for Parser<'a> {
    type Item = Result<Value, SyntaxError>;

    fn next(&mut self) -> Option<Result<Value, SyntaxError>> {
        match self.parse_value() {
            // turn Result/Option inside-out
            Ok(v) => match v {
                Some(s) => Some(Ok(s)),
                None => None,
            },
            Err(e) => Some(Err(e)),
        }
    }
}

/// Normalize a MULTI string.
pub fn normalize(ms: &str) -> String {
    let mut s = String::with_capacity(ms.len());
    for t in Parser::new(ms) {
        if let Ok(v) = t {
            s.push_str(&v.to_string());
        }
    }
    s
}

#[cfg(test)]
mod test {
    use super::*;
    fn single_text(v: &str) {
        let mut m = Parser::new(v);
        if let Some(Ok(Value::Text(t))) = m.next() { assert!(t == v) }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_text() {
        single_text("THIS IS A TEST");
    }
    #[test]
    fn parse_lower() {
        single_text("this is lower case");
    }
    #[test]
    fn parse_bracket() {
        let mut m = Parser::new("[[a]]b[[[[c]][[]]]]d");
        if let Some(Ok(Value::Text(t))) = m.next() {
            assert!(t == "[a]b[[c][]]d");
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_bracket2() {
        let mut m = Parser::new("[[[[[[[[");
        if let Some(Ok(Value::Text(t))) = m.next() {
            assert!(t == "[[[[");
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn norm_cb() {
		assert!(normalize("[cb1][CB255]") == "[cb1][cb255]");
		assert!(normalize("[cb][cb256]") == "[cb]");
    }
    #[test]
    fn parse_cb1() {
        let mut m = Parser::new("[cb0][CB1][cB255][cb256][cb]");
        if let Some(Ok(Value::ColorBackground(Some(n)))) = m.next() {
            assert!(n == Color::Legacy(0));
        } else { assert!(false) }
        if let Some(Ok(Value::ColorBackground(Some(n)))) = m.next() {
            assert!(n == Color::Legacy(1));
        } else { assert!(false) }
        if let Some(Ok(Value::ColorBackground(Some(n)))) = m.next() {
            assert!(n == Color::Legacy(255));
        } else { assert!(false) }
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        if let Some(Ok(Value::ColorBackground(n))) = m.next() {
            assert!(n == None);
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_cb2() {
        let mut m = Parser::new("[cbX]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn norm_pb() {
		assert!(normalize("[pb0][PB255]") == "[pb0][pb255]");
		assert!(normalize("[pb][pb256]") == "[pb]");
		assert!(normalize("[pb0,0,0][PB255,255,255]") ==
                          "[pb0,0,0][pb255,255,255]");
		assert!(normalize("[pb256,0,0][PBx]") == "");
    }
    #[test]
    fn parse_pb1() {
        let mut m = Parser::new("[pb0][PB1][pB255][pb256][pb]");
        if let Some(Ok(Value::PageBackground(Some(n)))) = m.next() {
            assert!(n == Color::Legacy(0));
        } else { assert!(false) }
        if let Some(Ok(Value::PageBackground(Some(n)))) = m.next() {
            assert!(n == Color::Legacy(1));
        } else { assert!(false) }
        if let Some(Ok(Value::PageBackground(Some(n)))) = m.next() {
            assert!(n == Color::Legacy(255));
        } else { assert!(false) }
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        if let Some(Ok(Value::PageBackground(n))) = m.next() {
            assert!(n == None)
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_pb2() {
        let mut m = Parser::new("[pb0,0]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_pb3() {
        let mut m = Parser::new("[pb50,150,200]");
        if let Some(Ok(Value::PageBackground(Some(n)))) = m.next() {
            assert!(n == Color::RGB(50,150,200));
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_pb4() {
        let mut m = Parser::new("[pb0,0,255,0]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_pb5() {
        let mut m = Parser::new("[pb0,0.5,255]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn norm_cf() {
		assert!(normalize("[cf0][CF255]") == "[cf0][cf255]");
		assert!(normalize("[cf][cf256]") == "[cf]");
		assert!(normalize("[cf0,0,0][CF255,255,255]") ==
                          "[cf0,0,0][cf255,255,255]");
		assert!(normalize("[cf256,0,0][CFx]") == "");
    }
    #[test]
    fn parse_cf1() {
        let mut m = Parser::new("[cf0][CF1][cF255][cf256][cf]");
        if let Some(Ok(Value::ColorForeground(Some(n)))) = m.next() {
            assert!(n == Color::Legacy(0));
        } else { assert!(false) }
        if let Some(Ok(Value::ColorForeground(Some(n)))) = m.next() {
            assert!(n == Color::Legacy(1));
        } else { assert!(false) }
        if let Some(Ok(Value::ColorForeground(Some(n)))) = m.next() {
            assert!(n == Color::Legacy(255));
        } else { assert!(false) }
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        if let Some(Ok(Value::ColorForeground(None))) = m.next()
        { assert!(true) } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_cf2() {
        let mut m = Parser::new("[cf0,0]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_cf3() {
        let mut m = Parser::new("[cf255,0,208]");
        if let Some(Ok(Value::ColorForeground(Some(n)))) = m.next() {
            assert!(n == Color::RGB(255,0,208));
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_cf4() {
        let mut m = Parser::new("[cf0,0,255,0]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_cf5() {
        let mut m = Parser::new("[cf0,0.5,255]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_cr() {
        let mut m = Parser::new("[cr1,1,10,10,0]");
        if let Some(Ok(Value::ColorRectangle(r,c))) = m.next() {
            assert!(r.x == 1 && r.y == 1 && r.w == 10 && r.h == 10);
            assert!(c == Color::Legacy(0));
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_cr2() {
        let mut m = Parser::new("[CR1,0,10,10,0]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_cr3() {
        let mut m = Parser::new("[cR1,1,100,100,0,1]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_cr4() {
        let mut m = Parser::new("[Cr5,7,100,80,100,150,200]");
        if let Some(Ok(Value::ColorRectangle(r,c))) = m.next() {
            assert!(r.x == 5 && r.y == 7 && r.w == 100 && r.h == 80);
            assert!(c == Color::RGB(100,150,200));
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_cr5() {
        let mut m = Parser::new("[cr1,1,100,100,0,1,2,3]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_cr6() {
        let mut m = Parser::new("[cr100,200,1000,2000,255,208,0]");
        if let Some(Ok(Value::ColorRectangle(r,c))) = m.next() {
            assert!(r.x == 100 && r.y == 200 && r.w == 1000 && r.h == 2000);
            assert!(c == Color::RGB(255,208,0));
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_f() {
        let mut m = Parser::new("[F]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_f1() {
        let mut m = Parser::new("[f1]");
        if let Some(Ok(Value::Field(1,None))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_f2() {
        let mut m = Parser::new("[f99]");
        if let Some(Ok(Value::Field(99,None))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_f3() {
        let mut m = Parser::new("[f100]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_f4() {
        let mut m = Parser::new("[F4,1]");
        if let Some(Ok(Value::Field(4,Some(1)))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_fl() {
        let mut m = Parser::new("[flto]");
        if let Some(Ok(Value::Flash(FlashOrder::OnOff,None,None))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_fl2() {
        let mut m = Parser::new("[FLOT]");
        if let Some(Ok(Value::Flash(FlashOrder::OffOn,None,None))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_fl3() {
        let mut m = Parser::new("[Flt10o5]");
        if let Some(Ok(Value::Flash(FlashOrder::OnOff,Some(10),Some(5)))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_fl4() {
        let mut m = Parser::new("[fLo0t99]");
        if let Some(Ok(Value::Flash(FlashOrder::OffOn,Some(0),Some(99)))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_fl5() {
        let mut m = Parser::new("[flt10o5x]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_fl6() {
        let mut m = Parser::new("[flt10o100]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_fl7() {
        let mut m = Parser::new("[flt10o10o10]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_fle() {
        let mut m = Parser::new("[/fl]");
        if let Some(Ok(Value::FlashEnd())) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_fle1() {
        let mut m = Parser::new("[/fl1]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_fo() {
        let mut m = Parser::new("[fo]");
        if let Some(Ok(Value::Font(None))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_fo1() {
        let mut m = Parser::new("[fo1]");
        if let Some(Ok(Value::Font(Some((1,None))))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_fo2() {
        let mut m = Parser::new("[fO2,0000]");
        if let Some(Ok(Value::Font(Some((2,Some(0)))))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_fo3() {
        let mut m = Parser::new("[Fo3,FFFF]");
        if let Some(Ok(Value::Font(Some((3,Some(0xFFFF)))))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_fo4() {
        let mut m = Parser::new("[FO4,FFFFF]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_fo5() {
        let mut m = Parser::new("[fo5,xxxx]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_fo6() {
        let mut m = Parser::new("[fo6,0000,0]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_fo7() {
        let mut m = Parser::new("[Fo7,abcd]");
        if let Some(Ok(Value::Font(Some((7,Some(0xabcd)))))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_fo8() {
        let mut m = Parser::new("[fo0]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_g() {
        let mut m = Parser::new("[G]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_g1() {
        let mut m = Parser::new("[g1]");
        if let Some(Ok(Value::Graphic(1,None))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_g2() {
        let mut m = Parser::new("[g2,1,1]");
        if let Some(Ok(Value::Graphic(2,Some((1,1,None))))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_g3() {
        let mut m = Parser::new("[g3,1]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_g4() {
        let mut m = Parser::new("[g4,1,1,0123]");
        if let Some(Ok(Value::Graphic(4,Some((1,1,Some(0x0123)))))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_g5() {
        let mut m = Parser::new("[g5,1,0,0123]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_g6() {
        let mut m = Parser::new("[g6,300,300,12345]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_g7() {
        let mut m = Parser::new("[g7,30,30,1245,]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_g8() {
        let mut m = Parser::new("[G8,50,50,Beef]");
        if let Some(Ok(Value::Graphic(8,Some((50,50,Some(0xbeef)))))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_hc() {
        let mut m = Parser::new("[hc]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_hc1() {
        let mut m = Parser::new("[HC1]");
        if let Some(Ok(Value::HexadecimalCharacter(0x0001))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_hc2() {
        let mut m = Parser::new("[hcFFFF]");
        if let Some(Ok(Value::HexadecimalCharacter(0xFFFF))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_hc3() {
        let mut m = Parser::new("[hc1FFFF]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_hc4() {
        let mut m = Parser::new("[hcXXxx]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_hc5() {
        let mut m = Parser::new("[hc7f]");
        if let Some(Ok(Value::HexadecimalCharacter(0x7F))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_jl() {
        let mut m = Parser::new("[jl]");
        if let Some(Ok(Value::JustificationLine(None))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_jl0() {
        let mut m = Parser::new("[JL0]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_jl15() {
        let mut m = Parser::new("[jL1][Jl2][JL3][jl4][JL5]");
        if let Some(Ok(Value::JustificationLine(Some(LineJustification::Other)))) = m.next() {
        } else { assert!(false) }
        if let Some(Ok(Value::JustificationLine(Some(LineJustification::Left)))) = m.next() {
        } else { assert!(false) }
        if let Some(Ok(Value::JustificationLine(Some(LineJustification::Center)))) = m.next() {
        } else { assert!(false) }
        if let Some(Ok(Value::JustificationLine(Some(LineJustification::Right)))) = m.next() {
        } else { assert!(false) }
        if let Some(Ok(Value::JustificationLine(Some(LineJustification::Full)))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_jp() {
        let mut m = Parser::new("[jp]");
        if let Some(Ok(Value::JustificationPage(None))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_jp0() {
        let mut m = Parser::new("[JP0]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_jp14() {
        let mut m = Parser::new("[jP1][Jp2][JP3][jp4]");
        if let Some(Ok(Value::JustificationPage(Some(PageJustification::Other)))) = m.next() {
        } else { assert!(false) }
        if let Some(Ok(Value::JustificationPage(Some(PageJustification::Top)))) = m.next() {
        } else { assert!(false) }
        if let Some(Ok(Value::JustificationPage(Some(PageJustification::Middle)))) = m.next() {
        } else { assert!(false) }
        if let Some(Ok(Value::JustificationPage(Some(PageJustification::Bottom)))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_ms() {
        let mut m = Parser::new("[ms0]");
        if let Some(Ok(Value::ManufacturerSpecific(0, None))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_ms1() {
        let mut m = Parser::new("[Ms1,Test]");
        if let Some(Ok(Value::ManufacturerSpecific(1, Some(t)))) = m.next() {
            assert!(t == "Test");
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_ms2() {
        let mut m = Parser::new("[Ms999,RANDOM junk]");
        if let Some(Ok(Value::ManufacturerSpecific(999, Some(t)))) = m.next() {
            assert!(t == "RANDOM junk");
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_ms3() {
        let mut m = Parser::new("[Ms9x9]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_mse() {
        let mut m = Parser::new("[/ms0]");
        if let Some(Ok(Value::ManufacturerSpecificEnd(0, None))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_mse1() {
        let mut m = Parser::new("[/Ms1,Test]");
        if let Some(Ok(Value::ManufacturerSpecificEnd(1, Some(t)))) = m.next() {
            assert!(t == "Test");
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_mse2() {
        let mut m = Parser::new("[/Ms999,RANDOM junk]");
        if let Some(Ok(Value::ManufacturerSpecificEnd(999, Some(t)))) = m.next() {
            assert!(t == "RANDOM junk");
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_mse3() {
        let mut m = Parser::new("[/Ms9x9]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_mv() {
        let mut m = Parser::new("[mv]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_mv1() {
        let mut m = Parser::new("[mvc]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_mv2() {
        let mut m = Parser::new("[mvcl]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_mv3() {
        let mut m = Parser::new("[mvcl100]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_mv4() {
        let mut m = Parser::new("[mvcl100,1]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_mv5() {
        let mut m = Parser::new("[mvcl100,1,10]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_mv6() {
        let mut m = Parser::new("[mvcl100,1,10,Text]");
        if let Some(Ok(Value::MovingText(MovingTextMode::Circular,
            MovingTextDirection::Left, 100, 1, 10, t))) = m.next()
        {
            assert!(t == "Text");
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_mv7() {
        let mut m = Parser::new("[mvcr150,2,5,*MOVING*]");
        if let Some(Ok(Value::MovingText(MovingTextMode::Circular,
            MovingTextDirection::Right, 150, 2, 5, t))) = m.next()
        {
            assert!(t == "*MOVING*");
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_mv8() {
        let mut m = Parser::new("[mvll75,3,4,Linear]");
        if let Some(Ok(Value::MovingText(MovingTextMode::Linear(0),
            MovingTextDirection::Left, 75, 3, 4, t))) = m.next()
        {
            assert!(t == "Linear");
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_mv9() {
        let mut m = Parser::new("[mvlr1000,4,5,right]");
        if let Some(Ok(Value::MovingText(MovingTextMode::Linear(0),
            MovingTextDirection::Right, 1000, 4, 5, t))) = m.next()
        {
            assert!(t == "right");
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_mv10() {
        let mut m = Parser::new("[mvl2l100,5,1,left]");
        if let Some(Ok(Value::MovingText(MovingTextMode::Linear(2),
            MovingTextDirection::Left, 100, 5, 1, t))) = m.next()
        {
            assert!(t == "left");
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_mv11() {
        let mut m = Parser::new("[mvl4x100,5,1,left]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_mv12() {
        let mut m = Parser::new("[mvl4r100,5,300,left]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_pt() {
        let mut m = Parser::new("[pt]");
        if let Some(Ok(Value::PageTime(None,None))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_pt1() {
        let mut m = Parser::new("[pt10]");
        if let Some(Ok(Value::PageTime(Some(10),None))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_pt2() {
        let mut m = Parser::new("[pt10o]");
        if let Some(Ok(Value::PageTime(Some(10),None))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_pt3() {
        let mut m = Parser::new("[pt10o2]");
        if let Some(Ok(Value::PageTime(Some(10),Some(2)))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_pt4() {
        let mut m = Parser::new("[pt10o2o]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_pt5() {
        let mut m = Parser::new("[pt255O255]");
        if let Some(Ok(Value::PageTime(Some(255),Some(255)))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_pt6() {
        let mut m = Parser::new("[PTO]");
        if let Some(Ok(Value::PageTime(None,None))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_pt7() {
        let mut m = Parser::new("[pt256o256]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_pt8() {
        let mut m = Parser::new("[pt%%%]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_sc() {
        let mut m = Parser::new("[sc]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_sc1() {
        let mut m = Parser::new("[SC1]");
        if let Some(Ok(Value::SpacingCharacter(1))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_sc2() {
        let mut m = Parser::new("[Sc99]");
        if let Some(Ok(Value::SpacingCharacter(99))) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_sc3() {
        let mut m = Parser::new("[sc100]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_sc4() {
        let mut m = Parser::new("[sc2,1]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_sce() {
        let mut m = Parser::new("[/sc]");
        if let Some(Ok(Value::SpacingCharacterEnd())) = m.next() {
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_sce1() {
        let mut m = Parser::new("[/sc1]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_tr() {
        let mut m = Parser::new("[tr1,1,10,10]");
        if let Some(Ok(Value::TextRectangle(r))) = m.next() {
            assert!(r.x == 1 && r.y == 1 && r.w == 10 && r.h == 10);
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_tr2() {
        let mut m = Parser::new("[TR1,0,10,10]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_tr3() {
        let mut m = Parser::new("[tR1,1,100,100,1]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_tr4() {
        let mut m = Parser::new("[Tr5,7,100,80]");
        if let Some(Ok(Value::TextRectangle(r))) = m.next() {
            assert!(r.x == 5 && r.y == 7 && r.w == 100 && r.h == 80);
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_tr5() {
        let mut m = Parser::new("[tr1,1,,100]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_tr6() {
        let mut m = Parser::new("[tr1,1,0,0]");
        if let Some(Ok(Value::TextRectangle(r))) = m.next() {
            assert!(r.x == 1 && r.y == 1 && r.w == 0 && r.h == 0);
        } else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_new_line() {
        let mut m = Parser::new("[nl][NL0][Nl1][nL9][nl10]");
        if let Some(Ok(Value::NewLine(n))) = m.next() { assert!(n == None) }
        else { assert!(false) }
        if let Some(Ok(Value::NewLine(n))) = m.next() { assert!(n == Some(0)) }
        else { assert!(false) }
        if let Some(Ok(Value::NewLine(n))) = m.next() { assert!(n == Some(1)) }
        else { assert!(false) }
        if let Some(Ok(Value::NewLine(n))) = m.next() { assert!(n == Some(9)) }
        else { assert!(false) }
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_multi() {
        let mut m = Parser::new("[[TEST[nl]TEST 2[np]TEST 3XX[NL]TEST 4]]");
        if let Some(Ok(Value::Text(t))) = m.next() { assert!(t == "[TEST") }
        else { assert!(false) }
        if let Some(Ok(Value::NewLine(n))) = m.next() { assert!(n == None) }
        else { assert!(false) }
        if let Some(Ok(Value::Text(t))) = m.next() { assert!(t == "TEST 2") }
        else { assert!(false) }
        if let Some(Ok(Value::NewPage())) = m.next() { }
        else { assert!(false) }
        if let Some(Ok(Value::Text(t))) = m.next() { assert!(t == "TEST 3XX") }
        else { assert!(false) }
        if let Some(Ok(Value::NewLine(n))) = m.next() { assert!(n == None) }
        else { assert!(false) }
        if let Some(Ok(Value::Text(t))) = m.next() { assert!(t == "TEST 4]") }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_control_char() {
        let mut m = Parser::new("\n");
        if let Some(Err(SyntaxError::CharacterNotDefined('\n'))) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_rustacean() {
        let mut m = Parser::new("🦀🦀");
        if let Some(Err(SyntaxError::CharacterNotDefined('🦀'))) = m.next() { }
        else { assert!(false) }
        if let Some(Err(SyntaxError::CharacterNotDefined('🦀'))) = m.next() { }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_tag() {
        let mut m = Parser::new("[x[x]");
        if let Some(Err(SyntaxError::UnsupportedTagValue)) = m.next() { }
        else { assert!(false) }
        if let Some(Err(SyntaxError::UnsupportedTag(s))) = m.next() {
            assert!(s == "x");
        }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_tag2() {
        let mut m = Parser::new("]");
        if let Some(Err(SyntaxError::UnsupportedTag(s))) = m.next() {
            assert!(s == "");
        }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_tag3() {
        let mut m = Parser::new("[nl");
        if let Some(Err(SyntaxError::UnsupportedTag(s))) = m.next() {
            assert!(s == "nl");
        }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_tag4() {
        let mut m = Parser::new("[");
        if let Some(Err(SyntaxError::UnsupportedTag(s))) = m.next() {
            assert!(s == "");
        }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_tag5() {
        let mut m = Parser::new("[x]");
        if let Some(Err(SyntaxError::UnsupportedTag(s))) = m.next() {
            assert!(s == "x");
        }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_tag6() {
        let mut m = Parser::new("bad]");
        if let Some(Err(SyntaxError::UnsupportedTag(s))) = m.next() {
            assert!(s == "bad");
        }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_tag7() {
        let mut m = Parser::new("[ttS123][vsa][slow45,10][feedL123][tz1,2,3]");
        if let Some(Err(SyntaxError::UnsupportedTag(s))) = m.next() {
            assert!(s == "ttS123");
        }
        else { assert!(false) }
        if let Some(Err(SyntaxError::UnsupportedTag(s))) = m.next() {
            assert!(s == "vsa");
        }
        if let Some(Err(SyntaxError::UnsupportedTag(s))) = m.next() {
            assert!(s == "slow45,10");
        }
        else { assert!(false) }
        if let Some(Err(SyntaxError::UnsupportedTag(s))) = m.next() {
            assert!(s == "feedL123");
        }
        else { assert!(false) }
        if let Some(Err(SyntaxError::UnsupportedTag(s))) = m.next() {
            assert!(s == "tz1,2,3");
        }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn parse_tag8() {
        let mut m = Parser::new("[pa1,LOW,CLOSED][loca,b,c,d]");
        if let Some(Err(SyntaxError::UnsupportedTag(s))) = m.next() {
            assert!(s == "pa1,LOW,CLOSED");
        }
        else { assert!(false) }
        if let Some(Err(SyntaxError::UnsupportedTag(s))) = m.next() {
            assert!(s == "loca,b,c,d");
        }
        else { assert!(false) }
        assert!(m.next() == None);
    }
    #[test]
    fn norm() {
        assert!(normalize("01234567890") == "01234567890");
        assert!(normalize("ABC") == "ABC");
        assert!(normalize("ABC_DEF") == "ABC_DEF");
        assert!(normalize("abc") == "abc");
        assert!(normalize("DON'T") == "DON'T");
        assert!(normalize("SPACE SPACE") == "SPACE SPACE");
        assert!(normalize("AB|C") == "AB|C");
        assert!(normalize("AB|{}{}C{}") == "AB|{}{}C{}");
		assert!(normalize("!\"#$%&\'()*+,-./") == "!\"#$%&\'()*+,-./");
		assert!(normalize(":;<=>?@\\^_`{|}~") == ":;<=>?@\\^_`{|}~");
		assert!(normalize("[[") == "[[");
		assert!(normalize("]]") == "]]");
		assert!(normalize("[[NOT TAG]]") == "[[NOT TAG]]");
		assert!(normalize("\t\n\rTAIL") == "TAIL");
    }
    #[test]
    fn norm_2() {
		assert!(normalize("ABC[NL]DEF") == "ABC[nl]DEF");
		assert!(normalize("ABC[nl3]DEF") == "ABC[nl3]DEF");
		assert!(normalize("ABC[np]DEF") == "ABC[np]DEF");
		assert!(normalize("ABC[jl4]DEF") == "ABC[jl4]DEF");
		assert!(normalize("ABC[jl6]DEF") == "ABCDEF");
		assert!(normalize("ABC[jp4]DEF") == "ABC[jp4]DEF");
		assert!(normalize("[fo3]ABC DEF") == "[fo3]ABC DEF");
		assert!(normalize("[fo3,beef]ABC DEF") == "[fo3,beef]ABC DEF");
		assert!(normalize("[g1]") == "[g1]");
		assert!(normalize("[g1_]") == "");
		assert!(normalize("[g1,5,5]") == "[g1,5,5]");
		assert!(normalize("[g1,5,5,beef]") == "[g1,5,5,beef]");
		assert!(normalize("[g1,4,4,BEEF]") == "[g1,4,4,beef]");
		assert!(normalize("[cf255,255,255]") == "[cf255,255,255]");
		assert!(normalize("[cf0,255,255]") == "[cf0,255,255]");
		assert!(normalize("[cf0,255,0]") == "[cf0,255,0]");
		assert!(normalize("[pto]") == "[pto]");
		assert!(normalize("[pt10o]") == "[pt10o]");
		assert!(normalize("[pt10o5]") == "[pt10o5]");
		assert!(normalize("[pto5]") == "[pto5]");
		assert!(normalize("ABC[sc3]DEF") == "ABC[sc3]DEF");
		assert!(normalize("ABC[sc3]DEF[/sc]GHI") == "ABC[sc3]DEF[/sc]GHI");
		assert!(normalize("[tr1,1,40,20]") == "[tr1,1,40,20]");
		assert!(normalize("[tr1,1,0,0]") == "[tr1,1,0,0]");
		assert!(normalize("[pb0,128,255]") == "[pb0,128,255]");
    }
    #[test]
    fn norm_3() {
		assert!(normalize("[") == "");
		assert!(normalize("]") == "");
		assert!(normalize("[bad tag") == "");
		assert!(normalize("bad tag]") == "");
		assert!(normalize("bad[tag") == "bad");
		assert!(normalize("bad]tag") == "tag");
		assert!(normalize("bad[ [nl] tag") == "bad tag");
		assert!(normalize("bad ]tag [nl]") == "tag [nl]");
		assert!(normalize("[ttS123]") == "");
    }
}
