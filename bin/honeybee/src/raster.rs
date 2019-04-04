// raster.rs
//
// Copyright (C) 2018-2019  Minnesota Department of Transportation
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
/// 24-bit RGB color
#[derive(Copy, Clone, Debug, PartialEq)]
pub struct Rgb24 {
    rgb: [u8;3],
}

/// An uncompressed 24-bit color image.
pub struct Raster24 {
    width  : u32,
    height : u32,
    pixels : Vec<u8>,
}

impl From<i32> for Rgb24 {
    fn from(rgb: i32) -> Self {
        let r = (rgb >> 16 & 0xFF) as u8;
        let g = (rgb >>  8 & 0xFF) as u8;
        let b = (rgb >>  0 & 0xFF) as u8;
        Rgb24::new(r, g, b)
    }
}

impl Rgb24 {
    /// Create a new 24-bit RGB color
    pub fn new(r: u8, g: u8, b: u8) -> Self {
        let rgb = [r, g, b];
        Rgb24 { rgb }
    }
    /// Get the red component
    pub fn r(self) -> u8 {
        self.rgb[0]
    }
    /// Get the green component
    pub fn g(self) -> u8 {
        self.rgb[1]
    }
    /// Get the blue component
    pub fn b(self) -> u8 {
        self.rgb[2]
    }
    /// Scale a color
    fn scale(self, vi: u8) -> Rgb24 {
        let r = scale_u8(self.r(), vi);
        let g = scale_u8(self.g(), vi);
        let b = scale_u8(self.b(), vi);
        Rgb24::new(r, g, b)
    }
    /// Blend two 24-bit colors (lighten only)
    fn blend_lighten(self, dest: Rgb24) -> Rgb24 {
        let r = self.r().max(dest.r());
        let g = self.g().max(dest.g());
        let b = self.b().max(dest.b());
        Rgb24::new(r, g, b)
    }
}

/// Scale a u8 value by another (mapping range to 0-1)
fn scale_u8(a: u8, b: u8) -> u8 {
    let aa = a as u32;
    let bb = b as u32;
    let c = (aa * bb + 255) >> 8;
    c as u8
}

impl Raster24 {
    /// Create a new raster image.
    ///
    /// * `width` Width in pixels.
    /// * `height` Height in pixels.
    /// * `color` RGB color (for initialization).
    pub fn new(width: u32, height: u32, color: Rgb24) -> Raster24 {
        let len = width as usize * height as usize;
        let mut pixels = Vec::with_capacity(len * 3);
        for _ in 0..len {
            pixels.extend(color.rgb.iter());
        }
        Raster24 { width, height, pixels }
    }
    /// Get raster width.
    pub fn width(&self) -> u32 {
        self.width
    }
    /// Get raster height.
    pub fn height(&self) -> u32 {
        self.height
    }
    /// Set the color of one pixel.
    pub fn set_pixel(&mut self, x: u32, y: u32, color: Rgb24) {
        debug_assert!(x < self.width, "x: {}, width: {}", x, self.width);
        debug_assert!(y < self.height, "y: {}, height: {}", y, self.height);
        let i = ((y * self.width + x) * 3) as usize;
        self.pixels[i+0] = color.r();
        self.pixels[i+1] = color.g();
        self.pixels[i+2] = color.b();
    }
    /// Get the color of one pixel.
    pub fn get_pixel(&self, x: u32, y: u32) -> Rgb24 {
        debug_assert!(x < self.width, "x: {}, width: {}", x, self.width);
        debug_assert!(y < self.height, "y: {}, height: {}", y, self.height);
        let i = ((y * self.width + x) * 3) as usize;
        let r = self.pixels[i+0];
        let g = self.pixels[i+1];
        let b = self.pixels[i+2];
        Rgb24::new(r, g, b)
    }
    /// Get the pixel data as a slice.
    pub fn pixels(&mut self) -> &mut [u8] {
        &mut self.pixels[..]
    }
    /// Render an attenuated circle.
    ///
    /// * `cx` X-Center of circle.
    /// * `cy` Y-Center of circle.
    /// * `r` Radius of circle.
    /// * `clr` Color of circle.
    pub fn circle(&mut self, cx: f32, cy: f32, r: f32, clr: Rgb24) {
        let x0 = (cx - r).floor().max(0f32) as u32;
        let x1 = (cx + r).ceil().min(self.width() as f32) as u32;
        let y0 = (cy - r).floor().max(0f32) as u32;
        let y1 = (cy + r).ceil().min(self.height() as f32) as u32;
        let rs = r.powi(2);
        for y in y0..y1 {
            let yd = (cy - y as f32 - 0.5f32).abs();
            let ys = yd.powi(2);
            for x in x0..x1 {
                let xd = (cx - x as f32 - 0.5f32).abs();
                let xs = xd.powi(2);
                let mut ds = xs + ys;
                // If center is within this pixel, make it brighter
                if ds < 1f32 {
                    ds = ds.powi(2);
                }
                // compare distance squared with radius squared
                let drs = ds / rs;
                let v = 1f32 - drs.powi(2).min(1f32);
                let vi = (v * 255f32) as u8;
                if vi > 0 {
                    let p = self.get_pixel(x, y);
                    let d = clr.scale(vi).blend_lighten(p);
                    self.set_pixel(x, y, d);
                }
            }
        }
    }
}

#[cfg(test)]
mod test {
    use super::*;
    #[test]
    fn scale_rgb24() {
        let a = Rgb24::new(255, 0, 0);
        let b = Rgb24::new(0, 255, 0);
        let c = Rgb24::new(0, 0, 255);
        assert!(a.scale(0) == Rgb24::new(0, 0, 0));
        assert!(b.scale(128) == Rgb24::new(0, 128, 0));
        assert!(c.scale(255) == Rgb24::new(0, 0, 255));
    }
    #[test]
    fn lighten_rgb24() {
        let a = Rgb24::new(255, 0, 0);
        let b = Rgb24::new(0, 255, 0);
        let c = Rgb24::new(0, 0, 255);
        assert!(a.blend_lighten(b) == Rgb24::new(255, 255, 0));
        assert!(a.blend_lighten(c) == Rgb24::new(255, 0, 255));
        assert!(b.blend_lighten(c) == Rgb24::new(0, 255, 255));
    }
    #[test]
    fn raster_pixel() {
        let mut r = Raster24::new(4, 4, Rgb24::new(0, 0, 0));
        r.set_pixel(0, 0, Rgb24::new( 1, 2, 3));
        r.set_pixel(1, 1, Rgb24::new( 4, 5, 6));
        r.set_pixel(2, 2, Rgb24::new( 7, 8, 9));
        r.set_pixel(3, 3, Rgb24::new(10,11,12));
        // y == 0
        assert!(r.pixels[ 0..3 ] == [1, 2, 3]);
        assert!(r.pixels[ 3..6 ] == [0, 0, 0]);
        assert!(r.pixels[ 6..9 ] == [0, 0, 0]);
        assert!(r.pixels[ 9..12] == [0, 0, 0]);
        // y == 1
        assert!(r.pixels[12..15] == [0, 0, 0]);
        assert!(r.pixels[15..18] == [4, 5, 6]);
        assert!(r.pixels[18..21] == [0, 0, 0]);
        assert!(r.pixels[21..24] == [0, 0, 0]);
        // y == 2
        assert!(r.pixels[24..27] == [0, 0, 0]);
        assert!(r.pixels[27..30] == [0, 0, 0]);
        assert!(r.pixels[30..33] == [7, 8, 9]);
        assert!(r.pixels[33..36] == [0, 0, 0]);
        // y == 3
        assert!(r.pixels[36..39] == [0, 0, 0]);
        assert!(r.pixels[39..42] == [0, 0, 0]);
        assert!(r.pixels[42..45] == [0, 0, 0]);
        assert!(r.pixels[45..48] == [10,11,12]);
    }
}
