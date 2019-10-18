// font.rs
//
// Copyright (C) 2018-2019  Minnesota Department of Transportation
//
use crate::ntcip::multi::SyntaxError;
use pix::{Raster, Rgb8};

/// A character for a bitmap font
#[derive(Deserialize, Serialize)]
pub struct Character {
    number: u16,
    width: u8,
    #[serde(with = "super::base64")]
    bitmap: Vec<u8>,
}

/// A bitmap font
#[derive(Deserialize, Serialize)]
pub struct Font {
    number: u8,
    name: String,
    height: u8,
    char_spacing: u8,
    line_spacing: u8,
    characters: Vec<Character>,
    version_id: u16,
}

impl Character {
    /// Get the width
    fn width(&self) -> u8 {
        self.width
    }
    /// Render the character to a raster
    fn render_char(&self, page: &mut Raster<Rgb8>, x: u32, y: u32, height: u32,
        cf: Rgb8)
    {
        let width = self.width.into();
        debug!("render_char: {} @ {},{}", self.number, x, y);
        let mut xx = 0;
        let mut yy = 0;
        for by in &self.bitmap {
            for bi in 0..8 {
                if by >> (7 - bi) & 1u8 != 0u8 {
                    page.set_pixel(x + xx, y + yy, cf);
                }
                xx += 1;
                if xx >= width {
                    xx = 0;
                    yy += 1;
                    if yy >= height {
                        break;
                    }
                }
            }
        }
    }
}

impl<'a> Font {
    /// Get font name
    pub fn name(&self) -> &str {
        &self.name
    }
    /// Get font number
    pub fn number(&self) -> u8 {
        self.number
    }
    /// Get font height
    pub fn height(&self) -> u8 {
        self.height
    }
    /// Get line spacing
    pub fn line_spacing(&self) -> u8 {
        self.line_spacing
    }
    /// Get character spacing
    pub fn char_spacing(&self) -> u8 {
        self.char_spacing
    }
    /// Get a character
    fn character(&'a self, ch: char) -> Result<&'a Character, SyntaxError> {
        let code_point: u32 = ch.into();
        if code_point <= std::u16::MAX.into() {
            let n = code_point as u16;
            if let Some(c) = self.characters.iter().find(|c| c.number == n) {
                return Ok(c)
            }
        }
        Err(SyntaxError::CharacterNotDefined(ch))
    }
    /// Calculate the width of a span of text
    pub fn text_width(&self, text: &str, cs: Option<u16>)
        -> Result<u16, SyntaxError>
    {
        let mut width = 0;
        let cs = cs.unwrap_or(self.char_spacing.into());
        for ch in text.chars() {
            let c = self.character(ch)?;
            if width > 0 {
                width += cs;
            }
            width += <u16>::from(c.width());
        }
        Ok(width)
    }
    /// Render a text span
    pub fn render_text(&self, page: &mut Raster<Rgb8>, text: &str, x: u32,
        y: u32, cs: u32, cf: Rgb8) -> Result<(), SyntaxError>
    {
        let height = self.height() as u32;
        debug!("span: {}, left: {}, top: {}, height: {}", text, x, y, height);
        let mut xx = 0;
        for ch in text.chars() {
            let c = self.character(ch)?;
            if xx > 0 {
                xx += cs;
            }
            c.render_char(page, x + xx, y, height, cf);
            xx += <u32>::from(c.width());
        }
        Ok(())
    }
}
