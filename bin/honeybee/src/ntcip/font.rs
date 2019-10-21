// font.rs
//
// Copyright (C) 2018-2019  Minnesota Department of Transportation
//
//! This module is for NTCIP 1203 DMS bitmap fonts.
//!
use crate::ntcip::multi::SyntaxError;
use pix::{Raster, Rgb8};

/// A character for a bitmap font
#[derive(Deserialize, Serialize)]
pub struct Character {
    /// Character number (code point)
    number: u16,
    /// Width in pixels
    width: u8,
    /// Bitmap data (by rows)
    #[serde(with = "super::base64")]
    bitmap: Vec<u8>,
}

/// A bitmap font
#[derive(Deserialize, Serialize)]
pub struct Font {
    /// Font number
    number: u8,
    /// Name (max 64 characters)
    name: String,
    /// Height in pixels
    height: u8,
    /// Default pixel spacing between characters
    char_spacing: u8,
    /// Default pixel spacing between lines
    line_spacing: u8,
    /// Characters in font
    characters: Vec<Character>,
    /// Version ID hash
    version_id: u16,
}

impl Character {
    /// Get number (code point)
    pub fn number(&self) -> u16 {
        self.number
    }
    /// Get width in pixels
    pub fn width(&self) -> u8 {
        self.width
    }
    /// Render the character to a raster
    ///
    /// * `page` Raster to render on.
    /// * `x` Left position of character (0-based).
    /// * `y` Top position of character (0-based).
    /// * `height` Font height in pixels.
    /// * `cf` Foreground color.
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
    /// Get font number
    pub fn number(&self) -> u8 {
        self.number
    }
    /// Get font name
    pub fn name(&self) -> &str {
        &self.name
    }
    /// Get font height
    pub fn height(&self) -> u8 {
        self.height
    }
    /// Get default pixel spacing between characters
    pub fn char_spacing(&self) -> u8 {
        self.char_spacing
    }
    /// Get default pixel spacing between lines
    pub fn line_spacing(&self) -> u8 {
        self.line_spacing
    }
    /// Get a character
    pub fn character(&'a self, ch: char) -> Result<&'a Character, SyntaxError> {
        let code_point: u32 = ch.into();
        if code_point <= std::u16::MAX.into() {
            let n = code_point as u16;
            if let Some(c) = self.characters.iter().find(|c| c.number == n) {
                return Ok(c)
            }
        }
        Err(SyntaxError::CharacterNotDefined(ch))
    }
    /// Calculate the width of a span of text.
    ///
    /// * `text` Span of text.
    /// * `cs` Character spacing in pixels.
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
    /// Render a span of text.
    ///
    /// * `page` Raster to render on.
    /// * `text` Span of text.
    /// * `x` Left position of character (0-based).
    /// * `y` Top position of character (0-based).
    /// * `cs` Character spacing in pixels.
    /// * `cf` Foreground color.
    pub fn render_text(&self, page: &mut Raster<Rgb8>, text: &str, x: u32,
        y: u32, cs: u32, cf: Rgb8) -> Result<(), SyntaxError>
    {
        let height = self.height().into();
        debug!("render_text: {} @ {},{} height: {}", text, x, y, height);
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
    /// Get version ID hash
    pub fn version_id(&self) -> u16 {
        self.version_id
    }
}
