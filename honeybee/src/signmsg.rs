// signmsg.rs
//
// Copyright (C) 2018-2025  Minnesota Department of Transportation
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
use crate::error::{Error, Result};
use crate::files::Cache;
use ntcip::dms::{Dms, FontTable, GraphicTable};
use rendzina::{SignConfig, load_font, load_graphic};
use serde::Deserialize;
use std::collections::HashMap;
use std::path::{Path, PathBuf};
use std::time::Instant;

/// Sign message
#[allow(unused)]
#[derive(Deserialize)]
struct SignMessage {
    name: String,
    sign_config: String,
    multi: String,
    msg_owner: String,
    sticky: bool,
    flash_beacon: bool,
    pixel_service: bool,
    msg_priority: i32,
}

/// Data needed for rendering sign messages
struct MsgData {
    fonts: FontTable<256, 24>,
    graphics: GraphicTable<32>,
    configs: HashMap<String, SignConfig>,
}

impl SignMessage {
    /// Load sign messages from a JSON file
    async fn load_all(dir: &Path) -> Result<Vec<SignMessage>> {
        log::trace!("SignMessage::load_all");
        let mut path = PathBuf::new();
        path.push(dir);
        path.push("sign_message");
        let buf = tokio::fs::read(path).await?;
        Ok(serde_json::from_slice(&buf)?)
    }
}

/// Load fonts from a JSON file
async fn load_fonts(dir: &Path) -> Result<FontTable<256, 24>> {
    let mut path = PathBuf::new();
    path.push(dir);
    path.push("tfon");
    log::trace!("load_fonts {path:?}");
    let mut cache = Cache::new(&path, "tfon").await?;
    let mut fonts = FontTable::default();
    path.push("_placeholder_.tfon");
    for nm in cache.drain() {
        path.set_file_name(nm);
        let buf = tokio::fs::read(&path).await?;
        let font = load_font(&*buf)?;
        if let Some(f) = fonts.font_mut(font.number) {
            *f = font;
        } else if let Some(f) = fonts.font_mut(0) {
            *f = font;
        }
    }
    Ok(fonts)
}

/// Load graphics from a JSON file
async fn load_graphics(dir: &Path) -> Result<GraphicTable<32>> {
    log::trace!("load_graphics");
    let mut path = PathBuf::new();
    path.push(dir);
    path.push("gif");
    let mut cache = Cache::new(&path, "gif").await?;
    let mut graphics = GraphicTable::default();
    path.push("_placeholder_.gif");
    for nm in cache.drain() {
        if let Ok(number) = nm
            .as_os_str()
            .to_str()
            .unwrap()
            .replace(|c: char| !c.is_numeric(), "")
            .parse::<u8>()
        {
            path.set_file_name(&nm);
            let buf = tokio::fs::read(&path).await?;
            let graphic = load_graphic(&*buf, number)?;
            if let Some(g) = graphics.graphic_mut(graphic.number) {
                *g = graphic;
            } else if let Some(g) = graphics.graphic_mut(0) {
                *g = graphic;
            }
        }
    }
    Ok(graphics)
}

impl MsgData {
    /// Load message data from a file path
    async fn load(dir: &Path) -> Result<Self> {
        log::trace!("MsgData::load");
        let fonts = load_fonts(dir).await?;
        let graphics = load_graphics(dir).await?;
        let mut path = PathBuf::new();
        path.push(dir);
        path.push("api");
        path.push("sign_config");
        let buf = tokio::fs::read(&path).await?;
        let configs = SignConfig::load_all(&*buf)?;
        Ok(MsgData {
            fonts,
            graphics,
            configs,
        })
    }

    /// Lookup a config
    fn config(&self, msg: &SignMessage) -> Result<&SignConfig> {
        let cfg = &msg.sign_config;
        match self.configs.get(cfg) {
            Some(c) => Ok(c),
            None => Err(Error::UnknownResource(format!("Config: {cfg}"))),
        }
    }

    /// Render sign message .gif
    fn render_sign_msg(
        &self,
        msg: &SignMessage,
        name: &str,
    ) -> Result<Option<Vec<u8>>> {
        log::trace!("render_sign_msg: {name}");
        let t = Instant::now();
        let cfg = self.config(msg)?;
        let dms = Dms::builder()
            .with_font_definition(self.fonts.clone())
            .with_graphic_definition(self.graphics.clone())
            .with_sign_cfg(cfg.sign_cfg())
            .with_vms_cfg(cfg.vms_cfg())
            .with_multi_cfg(cfg.multi_cfg())
            .build()?;
        let mut buf = Vec::with_capacity(1024);
        match rendzina::render(&mut buf, &dms, &msg.multi, 450, 100, None) {
            Ok(()) => {
                log::info!("{name} rendered in {:?}", t.elapsed());
                Ok(Some(buf))
            }
            Err(e) => {
                log::warn!("{name}, {e:?} multi={}", msg.multi);
                Ok(None)
            }
        }
    }
}

/// Render all sign messages.
///
/// * `dir` Output file directory.
pub async fn render_all() -> Result<()> {
    log::trace!("render_all");
    let dir = Path::new("");
    let msg_data = MsgData::load(dir).await?;
    let mut path = PathBuf::new();
    path.push(dir);
    path.push("img");
    let mut cache = Cache::new(path.as_path(), "gif").await?;
    for sign_msg in SignMessage::load_all(dir).await? {
        let mut name = sign_msg.name.clone();
        name.push_str(".gif");
        if cache.contains(&name) {
            cache.keep(&name);
        } else {
            match msg_data.render_sign_msg(&sign_msg, &name) {
                Ok(Some(buf)) => {
                    let file = cache.file(&name).await?;
                    file.write_buf(&buf).await?;
                }
                Ok(None) => (),
                Err(e) => log::warn!("render {name}, {e:?}"),
            }
        }
    }
    cache.clear().await;
    Ok(())
}
