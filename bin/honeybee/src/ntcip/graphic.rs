// graphic.rs
//
// Copyright (C) 2018-2019  Minnesota Department of Transportation
//
use crate::ntcip::multi::{Color, ColorCtx, ColorScheme, SyntaxError};
use pix::{Raster, Rgb8};

/// An uncompressed graphic
#[derive(Deserialize, Serialize)]
pub struct Graphic {
    number: u8,
    name: String,
    height: u8,
    width: u16,
    /// Color scheme, or dmsGraphicType from NTCIP 1203
    color_scheme: String,
    transparent_color: Option<i32>,
    #[serde(with = "super::base64")]
    bitmap: Vec<u8>,
}

/// Function to lookup a pixel from a graphic buffer
type PixFn = dyn Fn(&Graphic, u32, u32, &ColorCtx, &[u8]) -> Option<Rgb8>;

impl Graphic {
    /// Get the graphic number
    pub fn number(&self) -> u8 {
        self.number
    }
    /// Get the graphic width
    pub fn width(&self) -> u32 {
        self.width as u32
    }
    /// Get the graphic height
    pub fn height(&self) -> u32 {
        self.height as u32
    }
    /// Render a graphic onto a Raster
    pub fn render_graphic(&self, page: &mut Raster<Rgb8>, x: u32, y: u32,
        ctx: &ColorCtx) -> Result<(), SyntaxError>
    {
        let x = x - 1; // x must be > 0
        let y = y - 1; // y must be > 0
        let w = self.width();
        let h = self.height();
        if x + w > page.width() || y + h > page.height() {
            // There is no GraphicTooBig syntax error
            return Err(SyntaxError::Other.into());
        }
        let pix_fn = self.pixel_fn();
        for yy in 0..h {
            for xx in 0..w {
                if let Some(clr) = pix_fn(self, xx, yy, ctx, &self.bitmap) {
                    page.set_pixel(x + xx, y + yy, clr);
                }
            }
        }
        Ok(())
    }
    /// Get pixel lookup function for the color scheme
    fn pixel_fn(&self) -> &PixFn {
        match self.color_scheme[..].into() {
            ColorScheme::Monochrome1Bit => &Graphic::pixel_1,
            ColorScheme::Monochrome8Bit |
            ColorScheme::ColorClassic => &Graphic::pixel_8,
            ColorScheme::Color24Bit => &Graphic::pixel_24,
        }
    }
    /// Get one pixel of a monochrome 1-bit graphic
    fn pixel_1(&self, x: u32, y: u32, ctx: &ColorCtx, buf: &[u8])
        -> Option<Rgb8>
    {
        let p = y * self.width() + x;
        let by = p as usize / 8;
        let bi = 7 - (p & 7);
        let lit = ((buf[by] >> bi) & 1) != 0;
        match (lit, self.transparent_color) {
            (false, Some(0)) => None,
            (true, Some(1)) => None,
            (false, _) => {
                let (r, g, b) = ctx.rgb(ctx.background())?;
                Some(Rgb8::new(r, g, b))
            }
            (true, _) => {
                let (r, g, b) = ctx.rgb(ctx.foreground())?;
                Some(Rgb8::new(r, g, b))
            }
        }
    }
    /// Get one pixel of an 8-bit (monochrome or classic) color graphic
    fn pixel_8(&self, x: u32, y: u32, ctx: &ColorCtx, buf: &[u8])
        -> Option<Rgb8>
    {
        let p = y * self.width() + x;
        let v = buf[p as usize];
        if let Some(tc) = self.transparent_color {
            if tc == v as i32 {
                return None;
            }
        }
        match ctx.rgb(Color::Legacy(v)) {
            Some((r, g, b)) => Some(Rgb8::new(r, g, b)),
            None => {
                debug!("pixel_8 -- Bad color {}", v);
                None
            },
        }
    }
    /// Get one pixel of a 24-bit color graphic
    fn pixel_24(&self, x: u32, y: u32, _ctx: &ColorCtx, buf: &[u8])
        -> Option<Rgb8>
    {
        let p = 3 * (y * self.width() + x) as usize;
        let r = buf[p + 0];
        let g = buf[p + 1];
        let b = buf[p + 2];
        if let Some(tc) = self.transparent_color {
            let rgb = ((r as i32) << 16) + ((g as i32) << 8) + b as i32;
            if rgb == tc {
                return None;
            }
        }
        Some(Rgb8::new(r, g, b))
    }
}
