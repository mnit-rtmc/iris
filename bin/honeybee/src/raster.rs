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
pub struct Raster {
    width  : u32,
    height : u32,
    pixels : Vec<u8>,
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
    /// Get the color of one pixel.
    pub fn get_pixel(&self, x: u32, y: u32) -> [u8; 4] {
        let mut color = [0; 4];
        let i = ((y * self.width + x) * 4) as usize;
        color[0] = self.pixels[i+0];
        color[1] = self.pixels[i+1];
        color[2] = self.pixels[i+2];
        color[3] = self.pixels[i+3];
        color
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
    pub fn circle(&mut self, cx: f32, cy: f32, r: f32, clr: [u8; 3]) {
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
                // compare distance squared with radius squared
                let ds = (xs + ys) / rs;
                let v = 1f32 - ds.powi(2).min(1f32);
                let vi = (v * 255f32) as u8;
                if vi > 0 {
                    let p = self.get_pixel(x, y);
                    let sr = scale_u8(clr[0], vi);
                    let sg = scale_u8(clr[1], vi);
                    let sb = scale_u8(clr[2], vi);
                    let dr = scale_u8(p[0], p[3]);
                    let dg = scale_u8(p[1], p[3]);
                    let db = scale_u8(p[2], p[3]);
                    let d = [sr.max(dr), sg.max(dg), sb.max(db), 255];
                    self.set_pixel(x, y, d);
                }
            }
        }
    }
}

/// Scale a u8 value by another (mapping range to 0-1)
fn scale_u8(a: u8, b: u8) -> u8 {
    let aa = a as u32;
    let bb = b as u32;
    let c = (aa * bb + 255) >> 8;
    c as u8
}

#[cfg(test)]
mod test {
    use super::{Raster};
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
