// Copyright (C) 2022  Minnesota Department of Transportation
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
use crate::card::{disabled_attr, Card, CardType};
use crate::util::HtmlStr;
use serde::{Deserialize, Serialize};

/// Modem
#[derive(Debug, Deserialize, Serialize)]
pub struct Modem {
    pub name: String,
    pub uri: String,
    pub config: String,
    pub timeout_ms: u32,
    pub enabled: bool,
}

impl Modem {
    fn to_compact_html(&self) -> String {
        let name = HtmlStr(&self.name);
        let disabled = disabled_attr(self.enabled);
        format!("<span{disabled}>{name}</span>")
    }

    fn to_edit_html(&self) -> String {
        let uri = HtmlStr(&self.uri);
        let config = HtmlStr(&self.config);
        let timeout_ms = self.timeout_ms;
        let enabled = if self.enabled { " checked" } else { "" };
        format!(
            "<div class='row'>\
              <label for='edit_uri'>URI</label>\
              <input id='edit_uri' maxlength='64' size='30' \
                     value='{uri}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_config'>Config</label>\
              <input id='edit_config' maxlength='64' size='28' \
                     value='{config}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_timeout'>Timeout (ms)</label>\
              <input id='edit_timeout' type='number' min='0' size='8' \
                     max='20000' value='{timeout_ms}'/>\
            </div>\
            <div class='row'>\
              <label for='edit_enabled'>Enabled</label>\
              <input id='edit_enabled' type='checkbox'{enabled}/>\
            </div>"
        )
    }
}

impl Card for Modem {
    const TNAME: &'static str = "Modem";
    const ENAME: &'static str = "🖀 Modem";
    const URI: &'static str = "/iris/api/modem";

    fn is_match(&self, tx: &str) -> bool {
        self.name.to_lowercase().contains(tx)
    }

    fn name(&self) -> &str {
        &self.name
    }

    fn to_html(&self, ct: CardType) -> String {
        match ct {
            CardType::Compact => self.to_compact_html(),
            CardType::Edit => self.to_edit_html(),
            _ => unreachable!(),
        }
    }
}
