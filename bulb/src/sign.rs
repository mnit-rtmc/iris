// Copyright (C) 2022-2024  Minnesota Department of Transportation
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
use base64::{engine::general_purpose::STANDARD_NO_PAD as b64enc, Engine as _};
use ntcip::dms::{FontTable, GraphicTable};
use rendzina::SignConfig;
use web_sys::console;

/// Ntcip DMS sign
pub struct NtcipSign {
    pub dms: ntcip::dms::Dms<256, 24, 32>,
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
            Ok(dms) => Some(NtcipSign { dms }),
            Err(e) => {
                console::log_1(&format!("make_sign: {e:?}").into());
                None
            }
        }
    }

    /// Render sign preview image
    pub fn render(&self, html: &mut String, multi: &str) {
        html.push_str("<img id='mc_preview' width='240' height='80' ");
        let mut buf = Vec::with_capacity(4096);
        match rendzina::render(&mut buf, &self.dms, multi, Some(240), Some(80))
        {
            Ok(()) => {
                html.push_str("src='data:image/gif;base64,");
                b64enc.encode_string(buf, html);
                html.push_str("'/>");
            }
            Err(e) => {
                console::log_1(&format!("render_preview: {e:?}").into());
                html.push_str("src=''/>");
            }
        }
    }
}
