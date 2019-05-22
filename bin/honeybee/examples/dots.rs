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
extern crate honeybee;
extern crate gif;

use gif::{Frame, Encoder, Repeat, SetParameter};
use pix::{Raster, RasterBuilder, Rgb8};
use std::fs::File;
use std::io;

/// Write raster to a GIF file.
///
/// * `filename` Name of file to write.
pub fn write_gif(rasters: &mut [Raster<Rgb8>], filename: &str)
    -> io::Result<()>
{
    let h = rasters[0].height() as u16;
    let w = rasters[0].width() as u16;
    let mut fl = File::create(filename)?;
    let mut enc = Encoder::new(&mut fl, w, h, &[])?;
    enc.set(Repeat::Infinite)?;
    for raster in rasters {
        let pix = raster.as_u8_slice_mut();
        let mut frame = Frame::from_rgb(w, h, &mut pix[..]);
        frame.delay = 200; // 2 seconds
        enc.write_frame(&frame)?;
    }
    Ok(())
}

fn page1() -> Raster<Rgb8> {
    let amber = Rgb8::new(255, 208, 0);
    let red = Rgb8::new(255, 0, 0);
    let mut r = RasterBuilder::new().with_clear(32, 32);
    render_circle(&mut r, 12.0, 12.0, 3.0, amber);
    render_circle(&mut r, 20.0, 12.0, 3.0, amber);
    render_circle(&mut r, 12.0, 20.0, 3.0, amber);
    render_circle(&mut r, 20.0, 20.0, 3.0, amber);
    render_circle(&mut r, 16.0, 16.0, 3.0, red);
    r
}

fn page2() -> Raster<Rgb8> {
    let amber = Rgb8::new(255, 208, 0);
    let mut r = RasterBuilder::new().with_clear(32, 32);
    render_circle(&mut r, 12.0, 12.0, 3.0, amber);
    render_circle(&mut r, 20.0, 12.0, 3.0, amber);
    render_circle(&mut r, 12.0, 20.0, 3.0, amber);
    render_circle(&mut r, 20.0, 20.0, 3.0, amber);
    r
}

fn render_circle(raster: &mut Raster<Rgb8>, cx: f32, cy: f32, r: f32,
    clr: Rgb8)
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
                let p = raster.pixel(x, y);
                let red = (clr.red() * v).max(p.red());
                let green = (clr.green() * v).max(p.green());
                let blue = (clr.blue() * v).max(p.blue());
                let d = Rgb8::new(red, green, blue);
                raster.set_pixel(x, y, d);
            }
        }
    }
}

fn main() -> io::Result<()> {
    let mut rasters = Vec::new();
    rasters.push(page1());
    rasters.push(page2());
    write_gif(&mut rasters[..], "dots.gif")
}
