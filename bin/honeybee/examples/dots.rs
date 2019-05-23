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
use gif::{Frame, Encoder, Repeat, SetParameter};
use pix::{Gray8, Palette, Raster, RasterBuilder, Rgb8};
use std::fs::File;
use std::io;

/// Write raster to a GIF file.
///
/// * `filename` Name of file to write.
pub fn write_gif(palette: &[u8], rasters: &[Raster<Gray8>], filename: &str)
    -> io::Result<()>
{
    let h = rasters[0].height() as u16;
    let w = rasters[0].width() as u16;
    let mut fl = File::create(filename)?;
    let mut enc = Encoder::new(&mut fl, w, h, palette)?;
    enc.set(Repeat::Infinite)?;
    for raster in rasters {
        let pix = raster.as_u8_slice();
        let mut frame = Frame::from_indexed_pixels(w, h, &pix[..], None);
        frame.delay = 200; // 2 seconds
        enc.write_frame(&frame)?;
    }
    Ok(())
}

fn page1(p: &mut Palette<Rgb8>) -> Raster<Gray8> {
    let amber = Rgb8::new(255, 208, 0);
    let red = Rgb8::new(255, 0, 0);
    let mut r = RasterBuilder::new().with_clear(32, 32);
    render_circle(&mut r, p, 12.0, 12.0, 3.0, amber);
    render_circle(&mut r, p, 20.0, 12.0, 3.0, amber);
    render_circle(&mut r, p, 12.0, 20.0, 3.0, amber);
    render_circle(&mut r, p, 20.0, 20.0, 3.0, amber);
    render_circle(&mut r, p, 16.0, 16.0, 3.0, red);
    r
}

fn page2(p: &mut Palette<Rgb8>) -> Raster<Gray8> {
    let amber = Rgb8::new(255, 208, 0);
    let mut r = RasterBuilder::new().with_clear(32, 32);
    render_circle(&mut r, p, 12.0, 12.0, 3.0, amber);
    render_circle(&mut r, p, 20.0, 12.0, 3.0, amber);
    render_circle(&mut r, p, 12.0, 20.0, 3.0, amber);
    render_circle(&mut r, p, 20.0, 20.0, 3.0, amber);
    r
}

fn render_circle(raster: &mut Raster<Gray8>, palette: &mut Palette<Rgb8>,
    cx: f32, cy: f32, r: f32, clr: Rgb8)
{
    let x0 = (cx - r).floor().max(0.0) as u32;
    let x1 = (cx + r).ceil().min(raster.width() as f32) as u32;
    let y0 = (cy - r).floor().max(0.0) as u32;
    let y1 = (cy + r).ceil().min(raster.height() as f32) as u32;
    let rs = r.powi(2);
    for y in y0..y1 {
        let yd = (cy - y as f32 - 0.5).abs();
        let ys = yd.powi(2);
        for x in x0..x1 {
            let xd = (cx - x as f32 - 0.5).abs();
            let xs = xd.powi(2);
            let mut ds = xs + ys;
            // If center is within this pixel, make it brighter
            if ds < 1.0 {
                ds = ds.powi(2);
            }
            // compare distance squared with radius squared
            let drs = ds / rs;
            let v = 1.0 - drs.powi(2).min(1.0);
            if v > 0.0 {
                // blend with existing pixel
                let i = u8::from(raster.pixel(x, y).value());
                if let Some(p) = palette.entry(i as usize) {
                    let red = (clr.red() * v).max(p.red());
                    let green = (clr.green() * v).max(p.green());
                    let blue = (clr.blue() * v).max(p.blue());
                    let rgb = Rgb8::new(red, green, blue);
                    if let Some(d) = palette.set_entry(rgb) {
                        raster.set_pixel(x, y, Gray8::new(d as u8));
                    }
                }
            }
        }
    }
}

/// Get the difference threshold for Rgb8 with 256 capacity palette
fn palette_threshold_rgb8_256(v: usize) -> Rgb8 {
    match v as u8 {
        0x00...0x0F => Rgb8::new(0x00, 0x00, 0x00),
        0x10...0x1E => Rgb8::new(0x04, 0x04, 0x05),
        0x1F...0x2D => Rgb8::new(0x08, 0x08, 0x0A),
        0x2E...0x3B => Rgb8::new(0x0C, 0x0C, 0x0F),
        0x3C...0x49 => Rgb8::new(0x10, 0x10, 0x14),
        0x4A...0x56 => Rgb8::new(0x14, 0x14, 0x19),
        0x57...0x63 => Rgb8::new(0x18, 0x18, 0x1E),
        0x64...0x6F => Rgb8::new(0x1C, 0x1C, 0x23),
        0x70...0x7B => Rgb8::new(0x20, 0x20, 0x28),
        0x7C...0x86 => Rgb8::new(0x24, 0x24, 0x2D),
        0x87...0x91 => Rgb8::new(0x28, 0x28, 0x32),
        0x92...0x9B => Rgb8::new(0x2C, 0x2C, 0x37),
        0x9C...0xA5 => Rgb8::new(0x30, 0x30, 0x3C),
        0xA6...0xAE => Rgb8::new(0x34, 0x34, 0x41),
        0xAF...0xB7 => Rgb8::new(0x38, 0x38, 0x46),
        0xB8...0xBF => Rgb8::new(0x3C, 0x3C, 0x4B),
        0xC0...0xC7 => Rgb8::new(0x40, 0x40, 0x50),
        0xC8...0xCE => Rgb8::new(0x44, 0x44, 0x55),
        0xCF...0xD5 => Rgb8::new(0x48, 0x48, 0x5A),
        0xD6...0xDB => Rgb8::new(0x4C, 0x4C, 0x5F),
        0xDC...0xE1 => Rgb8::new(0x50, 0x50, 0x64),
        0xE2...0xE6 => Rgb8::new(0x54, 0x54, 0x69),
        0xE7...0xEB => Rgb8::new(0x58, 0x58, 0x6E),
        0xEC...0xEF => Rgb8::new(0x5C, 0x5C, 0x73),
        0xF0...0xF3 => Rgb8::new(0x60, 0x60, 0x78),
        0xF4...0xF6 => Rgb8::new(0x64, 0x64, 0x7D),
        0xF7...0xF9 => Rgb8::new(0x68, 0x68, 0x82),
        0xFA...0xFB => Rgb8::new(0x6C, 0x6C, 0x87),
        0xFC...0xFD => Rgb8::new(0x70, 0x70, 0x8C),
        0xFE...0xFE => Rgb8::new(0x74, 0x74, 0x91),
        0xFF...0xFF => Rgb8::new(0x78, 0x78, 0x96),
    }
}

fn main() -> io::Result<()> {
    let mut palette = Palette::new(256);
    palette.set_entry(Rgb8::default());
    palette.set_threshold_fn(palette_threshold_rgb8_256);
    let mut rasters = Vec::new();
    rasters.push(page1(&mut palette));
    rasters.push(page2(&mut palette));
    write_gif(palette.as_u8_slice(), &rasters[..], "dots.gif")
}
