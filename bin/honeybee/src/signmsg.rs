// signmsg.rs
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
use crate::error::Result;
use crate::multi::ColorCtx;
use crate::render::{PageSplitter, State};
use crate::resource::{MsgData, SignConfig, SignMessage};
use gift::Encoder;
use gift::block::{
    Application, ColorTableConfig, ColorTableExistence, ColorTableOrdering,
    Frame, GlobalColorTable, GraphicControl, ImageData, ImageDesc,
    LogicalScreenDesc, Preamble,
};
use pix::{Gray8, Palette, Raster, RasterBuilder, Rgb8};
use std::io::Write;

/// Maximum pixel width of DMS images
const PIX_WIDTH: f32 = 450.0;

/// Maximum pixel height of DMS images
const PIX_HEIGHT: f32 = 100.0;

/// Calculate the size of rendered DMS
fn calculate_size(cfg: &SignConfig) -> Result<(u16, u16)> {
    let fw = cfg.face_width();
    let fh = cfg.face_height();
    if fw > 0.0 && fh > 0.0 {
        let sx = PIX_WIDTH / fw;
        let sy = PIX_HEIGHT / fh;
        let s = sx.min(sy);
        let w = (fw * s).round() as u16;
        let h = (fh * s).round() as u16;
        Ok((w, h))
    } else {
        Ok((PIX_WIDTH as u16, PIX_HEIGHT as u16))
    }
}

/// Make a .gif frame of sign face
fn make_face_frame(cfg: &SignConfig, page: Raster<Rgb8>,
    palette: &mut Palette<Rgb8>, w: u16, h: u16, delay: u16) -> Frame
{
    let face = make_face_raster(&cfg, page, palette, w, h);
    let mut control = GraphicControl::default();
    control.set_delay_time_cs(delay);
    let image_desc = ImageDesc::default().with_width(w).with_height(h);
    let mut image_data = ImageData::new((w * h) as usize);
    image_data.add_data(face.as_u8_slice());
    Frame::new(Some(control), image_desc, None, image_data)
}

/// Make a raster of sign face
fn make_face_raster(cfg: &SignConfig, page: Raster<Rgb8>,
    palette: &mut Palette<Rgb8>, w: u16, h: u16) -> Raster<Gray8>
{
    let dark = Rgb8::new(20, 20, 0);
    let mut face = RasterBuilder::new().with_clear(w.into(), h.into());
    let ph = page.height();
    let pw = page.width();
    let sx = w as f32 / pw as f32;
    let sy = h as f32 / ph as f32;
    let s = sx.min(sy);
    debug!("face: {:?}, scale: {}", cfg.name(), s);
    for y in 0..ph {
        let py = cfg.pixel_y(y) * h as f32;
        for x in 0..pw {
            let px = cfg.pixel_x(x) * w as f32;
            let clr = page.pixel(x, y);
            let sr: u8 = clr.red().max(clr.green()).max(clr.blue()).into();
            // Clamp radius between 0.6 and 0.8 (blooming)
            let r = s * (sr as f32 / 255.0).max(0.6).min(0.8);
            let clr = if sr > 20 { clr } else { dark };
            render_circle(&mut face, palette, px, py, r, clr);
        }
    }
    face
}

/// Render an attenuated circle.
///
/// * `raster` Indexed raster.
/// * `palette` Global color palette.
/// * `cx` X-Center of circle.
/// * `cy` Y-Center of circle.
/// * `r` Radius of circle.
/// * `clr` Color of circle.
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
                    } else {
                        warn!("Blending failed -- color palette full!");
                    }
                } else {
                    warn!("Index not found in color palette!");
                }
            }
        }
    }
}

/// Render a sign message into a .gif file
pub fn render<W: Write>(s: &SignMessage, msg_data: &MsgData, f: W) -> Result<()>
{
    let cfg = msg_data.config(s)?;
    let (preamble, frames) = render_sign_msg_cfg(s, msg_data, cfg)?;
    write_gif(f, preamble, frames)
}

/// Write a .gif file
fn write_gif<W: Write>(mut fl: W, preamble: Preamble, frames: Vec<Frame>)
    -> Result<()>
{
    let mut enc = Encoder::new(&mut fl).into_frame_encoder();
    enc.encode_preamble(&preamble)?;
    for frame in &frames {
        enc.encode_frame(frame)?;
    }
    enc.encode_trailer()?;
    Ok(())
}

/// Render a sign message to a Vec of Frames
fn render_sign_msg_cfg(s: &SignMessage, msg_data: &MsgData, cfg: &SignConfig)
    -> Result<(Preamble, Vec<Frame>)>
{
    let mut palette = Palette::new(256);
    palette.set_threshold_fn(palette_threshold_rgb8_256);
    palette.set_entry(Rgb8::default());
    let mut frames = Vec::new();
    let rs = render_state_default(msg_data, cfg)?;
    let (w, h) = calculate_size(cfg)?;
    for page in PageSplitter::new(rs, s.multi()) {
        let page = page?;
        let raster = page.render(msg_data.fonts(), msg_data.graphics())?;
        let delay = page.page_on_time_ds() * 10;
        frames.push(make_face_frame(&cfg, raster, &mut palette, w, h, delay));
        let t = page.page_off_time_ds() * 10;
        if t > 0 {
            let raster = page.render_blank()?;
            frames.push(make_face_frame(&cfg, raster, &mut palette, w, h, t));
        }
    }
    let mut preamble = make_preamble(w, h, palette);
    if frames.len() > 1 {
        preamble.loop_count_ext = Some(Application::with_loop_count(0));
    }
    Ok((preamble, frames))
}

/// Make the GIF preamble blocks
fn make_preamble(w: u16, h: u16, palette: Palette<Rgb8>) -> Preamble {
    let tbl_cfg = ColorTableConfig::new(ColorTableExistence::Present,
        ColorTableOrdering::NotSorted, palette.len() as u16);
    let desc = LogicalScreenDesc::default()
        .with_screen_width(w)
        .with_screen_height(h)
        .with_color_table_config(&tbl_cfg);
    let mut pal = palette.as_u8_slice().to_vec();
    while pal.len() < tbl_cfg.size_bytes() {
        pal.push(0);
    }
    let table = GlobalColorTable::with_colors(&pal[..]);
    let mut preamble = Preamble::default();
    preamble.logical_screen_desc = desc;
    preamble.global_color_table = Some(table);
    preamble
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

/// Create default render state for a sign config.
fn render_state_default(msg_data: &MsgData, cfg: &SignConfig) -> Result<State> {
    let color_scheme = cfg.color_scheme();
    let fg_default = cfg.foreground_default();
    let bg_default = cfg.background_default();
    let color_ctx = ColorCtx::new(color_scheme, fg_default, bg_default);
    let char_width = cfg.char_width()?;
    let char_height = cfg.char_height()?;
    let page_on_time_ds = msg_data.page_on_default_ds();
    let page_off_time_ds = msg_data.page_off_default_ds();
    let text_rectangle = cfg.text_rect_default()?;
    let just_page = msg_data.page_justification_default();
    let just_line = msg_data.line_justification_default();
    let fname = cfg.default_font();
    let font = (msg_data.font_default(fname)?, None);
    Ok(State::new(color_ctx,
                  char_width,
                  char_height,
                  page_on_time_ds,
                  page_off_time_ds,
                  text_rectangle,
                  just_page,
                  just_line,
                  font,
    ))
}
