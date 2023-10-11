// signmsg.rs
//
// Copyright (C) 2018-2023  Minnesota Department of Transportation
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
use crate::files::{AtomicFile, Cache};
use crate::Result;
use anyhow::Context;
use ntcip::dms::{Dms, FontTable, GraphicTable};
use rendzina::{load_font, load_graphic, SignConfig};
use serde_derive::Deserialize;
use std::collections::HashMap;
use std::fmt;
use std::fs::File;
use std::io::BufReader;
use std::path::{Path, PathBuf};
use std::time::Instant;

/// Unknown resource error
#[derive(Debug)]
pub struct UnknownResourceError(String);

impl fmt::Display for UnknownResourceError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "Unknown resource: {}", self.0)
    }
}

impl std::error::Error for UnknownResourceError {
    fn source(&self) -> Option<&(dyn std::error::Error + 'static)> {
        None
    }
}

impl UnknownResourceError {
    fn new(msg: String) -> Box<Self> {
        Box::new(UnknownResourceError(msg))
    }
}

/// Sign message
#[allow(unused)]
#[derive(Deserialize)]
struct SignMessage {
    name: String,
    sign_config: String,
    incident: Option<String>,
    multi: String,
    msg_owner: String,
    flash_beacon: bool,
    msg_priority: i32,
    duration: Option<i32>,
}

/// Data needed for rendering sign messages
struct MsgData {
    fonts: FontTable<24>,
    graphics: GraphicTable<32>,
    configs: HashMap<String, SignConfig>,
}

impl SignMessage {
    /// Load sign messages from a JSON file
    fn load_all(dir: &Path) -> Result<Vec<SignMessage>> {
        log::debug!("SignMessage::load_all");
        let mut path = PathBuf::new();
        path.push(dir);
        path.push("sign_message");
        let file =
            File::open(&path).with_context(|| format!("load_all {path:?}"))?;
        let reader = BufReader::new(file);
        Ok(serde_json::from_reader(reader)?)
    }
}

/// Load fonts from a JSON file
fn load_fonts(dir: &Path) -> Result<FontTable<24>> {
    log::debug!("load_fonts");
    let mut path = PathBuf::new();
    path.push(dir);
    path.push("api");
    path.push("ifnt");
    let mut cache = Cache::new(&path, "ifnt")?;
    let mut fonts = FontTable::default();
    path.push("_placeholder_.ifnt");
    for nm in cache.drain() {
        path.set_file_name(nm);
        let file =
            File::open(&path).with_context(|| format!("font {path:?}"))?;
        let reader = BufReader::new(file);
        let font = load_font(reader)?;
        if let Some(f) = fonts.font_mut(font.number) {
            *f = font;
        } else if let Some(f) = fonts.font_mut(0) {
            *f = font;
        }
    }
    Ok(fonts)
}

/// Load graphics from a JSON file
fn load_graphics(dir: &Path) -> Result<GraphicTable<32>> {
    log::debug!("load_graphics");
    let mut path = PathBuf::new();
    path.push(dir);
    path.push("api");
    path.push("gif");
    let mut cache = Cache::new(&path, "gif")?;
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
            let file = File::open(&path)
                .with_context(|| format!("load_graphics {path:?}"))?;
            let reader = BufReader::new(file);
            let graphic = load_graphic(reader, number)?;
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
    fn load(dir: &Path) -> Result<Self> {
        log::debug!("MsgData::load");
        let fonts = load_fonts(dir)?;
        let graphics = load_graphics(dir)?;
        let mut path = PathBuf::new();
        path.push(dir);
        path.push("api");
        path.push("sign_config");
        let reader = BufReader::new(
            File::open(&path).with_context(|| format!("load {path:?}"))?,
        );
        let configs = SignConfig::load_all(reader)?;
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
            None => Err(UnknownResourceError::new(format!("Config: {cfg}"))),
        }
    }

    /// Render sign message .gif
    fn render_sign_msg(
        &mut self,
        msg: &SignMessage,
        file: AtomicFile,
    ) -> Result<()> {
        log::debug!("render_sign_msg: {:?}", file.path());
        let t = Instant::now();
        let writer = file.writer()?;
        let cfg = self.config(msg)?;
        let dms = Dms::builder()
            .with_font_definition(self.fonts.clone())
            .with_graphic_definition(self.graphics.clone())
            .with_sign_cfg(cfg.sign_cfg())
            .with_vms_cfg(cfg.vms_cfg())
            .with_multi_cfg(cfg.multi_cfg())
            .build()?;
        if let Err(e) = rendzina::render(writer, &dms, &msg.multi, None, None) {
            log::warn!("{:?}, multi={} {e:?}", file.path(), msg.multi);
            file.cancel()?;
            return Ok(());
        };
        log::info!("{:?} rendered in {:?}", file.path(), t.elapsed());
        Ok(())
    }
}

/// Fetch all sign messages.
///
/// * `dir` Output file directory.
pub fn render_all(dir: &Path) -> Result<()> {
    let mut msg_data = MsgData::load(dir)?;
    let mut path = PathBuf::new();
    path.push(dir);
    path.push("img");
    let mut cache = Cache::new(path.as_path(), "gif")?;
    for sign_msg in SignMessage::load_all(dir)? {
        let mut name = sign_msg.name.clone();
        name.push_str(".gif");
        if cache.contains(&name) {
            cache.keep(&name);
        } else {
            let file = cache.file(&name)?;
            msg_data.render_sign_msg(&sign_msg, file)?;
        }
    }
    Ok(())
}
