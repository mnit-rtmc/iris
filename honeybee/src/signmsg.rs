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
use ntcip::dms::font::FontTable;
use ntcip::dms::graphic::GraphicTable;
use ntcip::dms::Dms;
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
    dms: Dms,
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
fn load_fonts(dir: &Path) -> Result<FontTable> {
    log::debug!("load_fonts");
    let mut path = PathBuf::new();
    path.push(dir);
    path.push("api");
    path.push("font");
    let mut cache = Cache::new(&path, "ifnt")?;
    let mut fonts = FontTable::default();
    path.push("_placeholder_.ifnt");
    for nm in cache.drain() {
        path.set_file_name(nm);
        let file =
            File::open(&path).with_context(|| format!("font {path:?}"))?;
        let reader = BufReader::new(file);
        fonts.push(load_font(reader)?)?;
    }
    fonts.sort();
    Ok(fonts)
}

/// Load graphics from a JSON file
fn load_graphics(dir: &Path) -> Result<GraphicTable> {
    log::debug!("load_graphics");
    let mut path = PathBuf::new();
    path.push(dir);
    path.push("api");
    path.push("img");
    let mut cache = Cache::new(&path, "gif")?;
    let mut graphics = GraphicTable::default();
    path.push("_placeholder_.gif");
    for nm in cache.drain() {
        let number: u8 = nm
            .as_os_str()
            .to_str()
            .unwrap()
            .replace(|c: char| !c.is_numeric(), "")
            .parse()?;
        path.set_file_name(&nm);
        let file = File::open(&path)
            .with_context(|| format!("load_graphics {path:?}"))?;
        let reader = BufReader::new(file);
        let graphic = load_graphic(reader, number)?;
        graphics.push(graphic)?;
    }
    graphics.sort();
    Ok(graphics)
}

impl MsgData {
    /// Load message data from a file path
    fn load(dir: &Path) -> Result<Self> {
        log::debug!("MsgData::load");
        let dms = Dms::builder()
            .with_font_definition(load_fonts(dir)?)
            .with_graphic_definition(load_graphics(dir)?)
            .build();
        let mut path = PathBuf::new();
        path.push(dir);
        path.push("sign_config");
        let reader = BufReader::new(
            File::open(&path).with_context(|| format!("load {path:?}"))?,
        );
        let configs = SignConfig::load_all(reader)?;
        Ok(MsgData { dms, configs })
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
        self.dms = self
            .dms
            .clone()
            .into_builder()
            .with_sign_cfg(cfg.sign_cfg())
            .with_vms_cfg(cfg.vms_cfg())
            .with_multi_cfg(cfg.multi_cfg())
            .build();
        if let Err(e) = rendzina::render(writer, &self.dms, &msg.multi) {
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
