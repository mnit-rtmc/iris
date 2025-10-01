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
use ntcip::dms::{FontTable, GraphicTable};
use rendzina::SignConfig;
use web_sys::console;

/// Ntcip DMS sign
pub struct NtcipSign {
    pub dms: ntcip::dms::Dms<256, 24, 32>,
    id: Option<String>,
    class: Option<String>,
}

impl NtcipSign {
    /// Make an ntcip sign
    pub fn new(
        cfg: &SignConfig,
        fonts: FontTable<256, 24>,
        graphics: GraphicTable<32>,
    ) -> Option<NtcipSign> {
        match ntcip::dms::Dms::builder()
            .with_font_definition(fonts)
            .with_graphic_definition(graphics)
            .with_sign_cfg(cfg.sign_cfg())
            .with_vms_cfg(cfg.vms_cfg())
            .with_multi_cfg(cfg.multi_cfg())
            .build()
        {
            Ok(dms) => Some(NtcipSign {
                dms,
                id: None,
                class: None,
            }),
            Err(e) => {
                console::log_1(&format!("make_sign: {e:?}").into());
                None
            }
        }
    }

    /// Set the element id
    pub fn with_id(mut self, id: &str) -> Self {
        self.id = Some(id.to_string());
        self
    }

    /// Set the element class
    pub fn with_class(mut self, class: &str) -> Self {
        self.class = Some(class.to_string());
        self
    }
}

/// Render sign MULTI to a GIF image
pub fn render_multi(
    sign: Option<&NtcipSign>,
    multi: &str,
    mut width: u16,
    mut height: u16,
    mod_size: Option<(u32, u32)>,
) -> String {
    let mut html = Html::new();
    let mut img = html.img();
    if let Some(sign) = sign {
        (width, height) = rendzina::face_size(&sign.dms, width, height);
        if let Some(id) = &sign.id {
            img = img.id(id);
        }
        if let Some(class) = &sign.class {
            img = img.class(class);
        }
    }
    img = img.width(width.to_string()).height(height.to_string());
    if let Some(sign) = &sign {
        let mut buf = Vec::with_capacity(4096);
        match rendzina::render_multi(
            &mut buf, &sign.dms, multi, width, height, mod_size,
        ) {
            Ok(()) => {
                img.src(encode_gif(&buf[..]));
            }
            Err(e) => console::log_1(&format!("render_multi: {e:?}").into()),
        }
    }
    html.to_string()
}

/// Encode an inline GIF image
fn encode_gif(buf: &[u8]) -> String {
    let mut src = "data:image/gif;base64,".to_owned();
    b64enc.encode_string(buf, &mut src);
    src
}

/// Render sign pixels to a GIF image
pub fn render_pixels(
    sign: Option<&NtcipSign>,
    pix: &[u32],
    mut width: u16,
    mut height: u16,
) -> String {
    let mut html = Html::new();
    let mut img = html.img();
    if let Some(sign) = &sign {
        (width, height) = rendzina::face_size(&sign.dms, width, height);
        if let Some(id) = &sign.id {
            img = img.id(id);
        }
        if let Some(class) = &sign.class {
            img = img.class(class);
        }
    }
    img = img.width(width.to_string()).height(height.to_string());
    if let Some(sign) = &sign {
        let mut buf = Vec::with_capacity(4096);
        match rendzina::render_pixels(&mut buf, &sign.dms, pix, width, height) {
            Ok(()) => {
                img.src(encode_gif(&buf[..]));
            }
            Err(e) => console::log_1(&format!("render_pixels: {e:?}").into()),
        }
    }
    html.to_string()
}
