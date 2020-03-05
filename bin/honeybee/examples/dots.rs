// dots.rs
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
use gift::*;
use gift::block::*;
use pix::{Gray8, Palette, Raster, RasterBuilder, Rgb8};
use std::error::Error;
use std::fs::File;

/// Write raster to a GIF file.
///
/// * `filename` Name of file to write.
pub fn write_gif(palette: &[u8], rasters: &[Raster<Gray8>], filename: &str)
    -> Result<(), Box<dyn Error>>
{
    let w = rasters[0].width() as u16;
    let h = rasters[0].height() as u16;
    let mut fl = File::create(filename)?;
    let mut enc = Encoder::new(&mut fl);
    let g_tbl = ColorTableConfig::new(ColorTableExistence::Present,
        ColorTableOrdering::NotSorted, palette.len() as u16 / 3);
    let mut pal = palette.to_vec();
    while pal.len() < g_tbl.size_bytes() {
        pal.push(0);
    }
    enc.encode(&Header::with_version(*b"89a").into())?;
    enc.encode(&LogicalScreenDesc::default()
        .with_screen_width(w)
        .with_screen_height(h)
        .with_color_table_config(&g_tbl).into())?;
    enc.encode(&GlobalColorTable::with_colors(&pal[..]).into())?;
    enc.encode(&Application::with_loop_count(0).into())?;
    for raster in rasters {
        let mut control = GraphicControl::default();
        control.set_delay_time_cs(200);
        enc.encode(&control.into())?;
        enc.encode(&ImageDesc::default().with_width(w).with_height(h).into())?;
        let pix = raster.as_u8_slice();
        let mut image = ImageData::new((w * h) as usize);
        image.add_data(pix);
        enc.encode(&image.into())?;
    }
    enc.encode(&Trailer::default().into())?;
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
    let i = match v as u8 {
        0x00..=0x0F => 0,
        0x10..=0x1E => 1,
        0x1F..=0x2D => 2,
        0x2E..=0x3B => 3,
        0x3C..=0x49 => 4,
        0x4A..=0x56 => 5,
        0x57..=0x63 => 6,
        0x64..=0x6F => 7,
        0x70..=0x7B => 8,
        0x7C..=0x86 => 9,
        0x87..=0x91 => 10,
        0x92..=0x9B => 11,
        0x9C..=0xA5 => 12,
        0xA6..=0xAE => 13,
        0xAF..=0xB7 => 14,
        0xB8..=0xBF => 15,
        0xC0..=0xC7 => 16,
        0xC8..=0xCE => 17,
        0xCF..=0xD5 => 18,
        0xD6..=0xDB => 19,
        0xDC..=0xE1 => 20,
        0xE2..=0xE6 => 21,
        0xE7..=0xEB => 22,
        0xEC..=0xEF => 23,
        0xF0..=0xF3 => 24,
        0xF4..=0xF6 => 25,
        0xF7..=0xF9 => 26,
        0xFA..=0xFB => 27,
        0xFC..=0xFD => 28,
        0xFE..=0xFE => 29,
        0xFF..=0xFF => 30,
    };
    Rgb8::new(i * 4, i * 4, i * 5)
}

fn main() -> Result<(), Box<dyn Error>> {
    let mut palette = Palette::new(256);
    palette.set_entry(Rgb8::default());
    palette.set_threshold_fn(palette_threshold_rgb8_256);
    let mut rasters = Vec::new();
    rasters.push(page1(&mut palette));
    rasters.push(page2(&mut palette));
    write_gif(palette.as_u8_slice(), &rasters[..], "dots.gif")
}
