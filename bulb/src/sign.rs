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
use ntcip::dms::{FontTable, GraphicTable};
use rendzina::SignConfig;
use web_sys::console;

/// Ntcip DMS sign
pub struct NtcipSign {
    pub dms: ntcip::dms::Dms<256, 24, 32>,
    id: Option<String>,
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
            Ok(dms) => Some(NtcipSign { dms, id: None }),
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
}

/// Render sign image
pub fn render(
    sign: &Option<NtcipSign>,
    multi: &str,
    mut width: u16,
    mut height: u16,
    mod_size: Option<(u32, u32)>,
) -> String {
    let mut html = String::new();
    html.push_str("<img");
    if let Some(sign) = &sign {
        (width, height) = rendzina::face_size(&sign.dms, width, height);
        if let Some(id) = &sign.id {
            html.push_str(" id='");
            html.push_str(id);
            html.push('\'');
        }
    }
    html.push_str(" width='");
    html.push_str(&width.to_string());
    html.push_str("' height='");
    html.push_str(&height.to_string());
    html.push_str("' ");
    if let Some(sign) = &sign {
        let mut buf = Vec::with_capacity(4096);
        match rendzina::render(
            &mut buf, &sign.dms, multi, width, height, mod_size,
        ) {
            Ok(()) => {
                html.push_str("src='data:image/gif;base64,");
                b64enc.encode_string(buf, &mut html);
                html.push('\'');
            }
            Err(e) => console::log_1(&format!("render: {e:?}").into()),
        }
    }
    html.push_str("/>");
    html
}
