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
extern crate palette;

use self::palette::{Blend,Pixel,LinSrgba,Srgba};
use std::ptr::write_bytes;

pub struct Raster {
    width  : u32,
    height : u32,
    pixels : Vec<u8>,
}

pub struct Mask {
    raster : Option<Raster>, // Option used as a cheap RefCell replacement
    pixels : Vec<u8>,
}

impl From<Mask> for Raster {
    fn from(mask: Mask) -> Self {
        mask.raster.unwrap()
    }
}

impl Raster {
    /// Create a new raster image.
    ///
    /// * `width` Width in pixels.
    /// * `height` Height in pixels.
    /// * `color` RGBA color (for initialization).
    pub fn new(width: u32, height: u32, color: [u8; 4]) -> Raster {
        let len = width as usize * height as usize;
        let mut pixels = Vec::with_capacity(len * 4);
        for _ in 0..len {
            pixels.extend(color.iter());
        }
        Raster { width, height, pixels }
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
    pub fn set_pixel(&mut self, x: u32, y: u32, color: [u8; 4]) {
        let i = ((y * self.width + x) * 4) as usize;
        self.pixels[i+0] = color[0];
        self.pixels[i+1] = color[1];
        self.pixels[i+2] = color[2];
        self.pixels[i+3] = color[3];
    }
    /// Get the pixel data as a slice.
    pub fn pixels(&mut self) -> &mut [u8] {
        &mut self.pixels[..]
    }
}

impl Mask {
    /// Create a new raster mask.
    ///
    /// * `raster` Raster to mask.
    pub fn new(raster: Raster) -> Self {
        let len = raster.width as usize * raster.height as usize;
        let pixels = vec![0; len];
        Mask { raster: Some(raster), pixels }
    }
    /// Get mask width.
    pub fn width(&self) -> u32 {
        self.raster.as_ref().unwrap().width()
    }
    /// Get mask height.
    pub fn height(&self) -> u32 {
        self.raster.as_ref().unwrap().height()
    }
    /// Clear the mask.
    pub fn clear(&mut self) {
        unsafe {
            let p = self.pixels.as_mut_ptr();
            write_bytes(p, 0, self.pixels.len());
        }
    }
    /// Get the mask pixel data as a slice.
    fn pixels(&self) -> &[u8] {
        &self.pixels[..]
    }
    /// Render an attenuated circle.
    ///
    /// * `cx` X-Center of circle.
    /// * `cy` Y-Center of circle.
    /// * `r` Radius of circle.
    pub fn circle(&mut self, cx: f32, cy: f32, r: f32) {
        let x0 = (cx - r).floor().max(0f32) as usize;
        let x1 = (cx + r).ceil().min(self.width() as f32) as usize;
        let y0 = (cy - r).floor().max(0f32) as usize;
        let y1 = (cy + r).ceil().min(self.height() as f32) as usize;
        let rs = r.powi(2);
        for y in y0..y1 {
            let yd = (cy - y as f32 - 0.5f32).abs();
            let ys = yd.powi(2);
            let yi = y * self.width() as usize;
            for x in x0..x1 {
                let xd = (cx - x as f32 - 0.5f32).abs();
                let xs = xd.powi(2);
                // compare distance squared with radius squared
                let ds = (xs + ys) / rs;
                let v = 1f32 - ds.powi(2).min(1f32);
                let vi = (v * 255f32) as u8;
                self.pixels[yi + x] = self.pixels[yi + x].saturating_add(vi);
            }
        }
    }
    /// Composite mask to its raster.
    pub fn composite(&mut self, clr: [u8; 3]) {
        // Temporarily take raster to avoid borrow conflicts
        let mut raster = self.raster.take().unwrap();
        for (p, m) in raster.pixels().chunks_mut(4).zip(self.pixels()) {
            let src: LinSrgba<f32> = Srgba::new(clr[0], clr[1], clr[2], *m)
                                           .into_format().into_linear();
            let dst: LinSrgba<f32> = Srgba::new(p[0], p[1], p[2], p[3])
                                           .into_format().into_linear();
            let c = src.over(dst);
            let d: [u8; 4] = Srgba::from_linear(c).into_format().into_raw();
            p[0] = d[0];
            p[1] = d[1];
            p[2] = d[2];
            p[3] = d[3];
        }
        self.raster = Some(raster);
    }
}

#[cfg(test)]
mod test {
    use super::{Mask,Raster};
    #[test]
    fn mask_circle() {
        let r = Raster::new(6, 6, [0, 0, 0, 0]);
        let mut m = Mask::new(r);
        m.circle(3f32, 3f32, 3f32);
        assert!(m.pixels[ 0..6 ] ==   [0,  27, 121, 121,  27, 0]);
        assert!(m.pixels[ 6..12] ==  [27, 191, 235, 235, 191, 27]);
        assert!(m.pixels[12..18] == [121, 235, 254, 254, 235, 121]);
        assert!(m.pixels[18..24] == [121, 235, 254, 254, 235, 121]);
        assert!(m.pixels[24..30] ==  [27, 191, 235, 235, 191, 27]);
        assert!(m.pixels[30..36] ==   [0,  27, 121, 121,  27, 0]);
    }
    #[test]
    fn raster_pixel() {
        let mut r = Raster::new(4, 4, [0, 0, 0, 0]);
        r.set_pixel(0, 0, [ 1, 2, 3, 4]);
        r.set_pixel(1, 1, [ 5, 6, 7, 8]);
        r.set_pixel(2, 2, [ 9,10,11,12]);
        r.set_pixel(3, 3, [13,14,15,16]);
        // y == 0
        assert!(r.pixels[ 0..4 ] == [1, 2, 3, 4]);
        assert!(r.pixels[ 4..8 ] == [0, 0, 0, 0]);
        assert!(r.pixels[ 8..12] == [0, 0, 0, 0]);
        assert!(r.pixels[12..16] == [0, 0, 0, 0]);
        // y == 1
        assert!(r.pixels[16..20] == [0, 0, 0, 0]);
        assert!(r.pixels[20..24] == [5, 6, 7, 8]);
        assert!(r.pixels[24..28] == [0, 0, 0, 0]);
        assert!(r.pixels[28..32] == [0, 0, 0, 0]);
        // y == 2
        assert!(r.pixels[32..36] == [0, 0, 0, 0]);
        assert!(r.pixels[36..40] == [0, 0, 0, 0]);
        assert!(r.pixels[40..44] == [9,10,11,12]);
        assert!(r.pixels[44..48] == [0, 0, 0, 0]);
        // y == 3
        assert!(r.pixels[48..52] == [0, 0, 0, 0]);
        assert!(r.pixels[52..56] == [0, 0, 0, 0]);
        assert!(r.pixels[56..60] == [0, 0, 0, 0]);
        assert!(r.pixels[60..64] == [13,14,15,16]);
    }
}
