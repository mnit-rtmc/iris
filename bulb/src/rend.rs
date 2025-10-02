// Copyright (C) 2022-2025  Minnesota Department of Transportation
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
use base64::{Engine as _, engine::general_purpose::STANDARD_NO_PAD as b64enc};
use hatmil::Html;
use ntcip::dms::multi::join_text;
use web_sys::console;

/// NTCIP sign
type NtcipDms = ntcip::dms::Dms<256, 24, 32>;

/// Ntcip DMS renderer
pub struct Renderer<'r> {
    html: &'r mut Html,
    dms: Option<&'r NtcipDms>,
    gif: Option<&'r str>,
    id: Option<&'r str>,
    class: Option<&'r str>,
    max_width: u16,
    max_height: u16,
    mod_size: Option<(u32, u32)>,
}

impl<'r> Renderer<'r> {
    /// Make an NTCIP renderer
    pub fn new(html: &'r mut Html) -> Self {
        Renderer {
            html,
            dms: None,
            gif: None,
            id: None,
            class: None,
            max_width: u16::MAX,
            max_height: u16::MAX,
            mod_size: None,
        }
    }

    /// Set the DMS
    pub fn with_dms(mut self, dms: &'r NtcipDms) -> Self {
        self.dms = Some(dms);
        self
    }

    /// Set the GIF URI
    pub fn with_gif(mut self, gif: &'r str) -> Self {
        self.gif = Some(gif);
        self
    }

    /// Set the element id
    pub fn with_id(mut self, id: &'r str) -> Self {
        self.id = Some(id);
        self
    }

    /// Set the element class
    pub fn with_class(mut self, class: &'r str) -> Self {
        self.class = Some(class);
        self
    }

    /// Set the max width
    pub fn with_max_width(mut self, width: u16) -> Self {
        self.max_width = width;
        self
    }

    /// Set the max height
    pub fn with_max_height(mut self, height: u16) -> Self {
        self.max_height = height;
        self
    }

    /// Set the module size
    pub fn with_mod_size(mut self, mod_size: Option<(u32, u32)>) -> Self {
        self.mod_size = mod_size;
        self
    }

    /// Calculate the image size
    pub fn size(&self) -> (u16, u16) {
        self.dms.map_or((self.max_width, self.max_height), |dms| {
            rendzina::face_size(dms, self.max_width, self.max_height)
        })
    }

    /// Render sign MULTI to a GIF image
    pub fn render_multi(&mut self, multi: &str) {
        let (width, height) = self.size();
        let mut img = self
            .html
            .img()
            .alt(join_text(multi, " "))
            .width(width.to_string())
            .height(height.to_string());
        if let Some(id) = &self.id {
            img = img.id(id);
        }
        if let Some(class) = &self.class {
            img = img.class(class);
        }
        match (&self.dms, &self.gif) {
            (_, Some(gif)) => {
                img = img.src(gif);
            }
            (Some(dms), _) => {
                let mut buf = Vec::with_capacity(4096);
                match rendzina::render_multi(
                    &mut buf,
                    &dms,
                    multi,
                    width,
                    height,
                    self.mod_size,
                ) {
                    Ok(()) => {
                        img = img.src(encode_gif(&buf[..]));
                    }
                    Err(e) => {
                        console::log_1(&format!("render_multi: {e:?}").into())
                    }
                }
            }
            _ => console::log_1(&format!("render_multi: no image").into()),
        }
        img.end();
    }

    /// Render sign pixels to a GIF image
    pub fn render_pixels(&mut self, pix: &[u32]) {
        let (width, height) = self.size();
        let mut img = self
            .html
            .img()
            .width(width.to_string())
            .height(height.to_string());
        if let Some(id) = &self.id {
            img = img.id(id);
        }
        if let Some(class) = &self.class {
            img = img.class(class);
        }
        if let Some(dms) = &self.dms {
            let mut buf = Vec::with_capacity(4096);
            match rendzina::render_pixels(&mut buf, &dms, pix, width, height) {
                Ok(()) => {
                    img = img.src(encode_gif(&buf[..]));
                }
                Err(e) => {
                    console::log_1(&format!("render_pixels: {e:?}").into())
                }
            }
        }
        img.end();
    }
}

/// Encode an inline GIF image
fn encode_gif(buf: &[u8]) -> String {
    let mut src = "data:image/gif;base64,".to_owned();
    b64enc.encode_string(buf, &mut src);
    src
}
