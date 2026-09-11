// Copyright (C) 2025-2026  Minnesota Department of Transportation
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
use crate::msgpattern::MsgPattern;
use crate::msgpriority::MsgPriority;
use crate::planphase::PlanPhase;
use crate::util::{Doc, Fields};
use hatmil::{Tree, html};
use serde::Deserialize;
use serde_json::Value;
use serde_json::map::Map;
use web_sys::HtmlElement;

/// Device action
#[derive(Clone, Debug, Deserialize, PartialEq)]
pub struct DeviceAction {
    pub name: String,
    pub action_plan: String,
    pub hashtag: String,
    pub phase: String,
    pub msg_pattern: Option<String>,
    pub msg_priority: u8,
    pub sticky: bool,
    pub ignore_auto_fail: bool,
}

impl DeviceAction {
    /// Create a new device action
    pub fn new(name: &str, ap: &str, phase: &str) -> Self {
        DeviceAction {
            name: name.to_string(),
            action_plan: ap.to_string(),
            hashtag: String::new(),
            phase: phase.to_string(),
            msg_pattern: None,
            msg_priority: MsgPriority::Medium1 as u8,
            sticky: false,
            ignore_auto_fail: false,
        }
    }

    /// Check if device action is valid
    pub fn is_valid(&self) -> bool {
        self.hashtag.len() > 1
            && self.hashtag.starts_with('#')
            && self.hashtag[1..].chars().all(|c| c.is_alphanumeric())
    }

    /// Get set of changed fields
    pub fn changed_fields(&self, da: &Self) -> Fields {
        let mut fields = Fields::new();
        if self.hashtag != da.hashtag {
            fields.insert_str("hashtag", &self.hashtag);
        }
        if self.phase != da.phase {
            fields.insert_str("phase", &self.phase);
        }
        if self.msg_pattern != da.msg_pattern {
            fields.insert_opt_str("msg_pattern", self.msg_pattern.as_deref());
        }
        if self.msg_priority != da.msg_priority {
            fields.insert_num("msg_priority", self.msg_priority);
        }
        if self.sticky != da.sticky {
            fields.insert_bool("sticky", self.sticky);
        }
        if self.ignore_auto_fail != da.ignore_auto_fail {
            fields.insert_bool("ignore_auto_fail", self.ignore_auto_fail);
        }
        fields
    }

    /// Convert to JSON value (for POST)
    pub fn value(&self) -> Value {
        let mut obj = Map::new();
        obj.insert("name".to_string(), Value::String(self.name.to_string()));
        obj.insert(
            "action_plan".to_string(),
            Value::String(self.action_plan.to_string()),
        );
        Value::Object(obj)
    }

    /// Get ID for summary
    fn id_summary(&self) -> String {
        format!("{}-summary", self.name)
    }

    /// Get ID for hashtag `<input>`
    fn id_hashtag(&self) -> String {
        format!("{}-hashtag", self.name)
    }

    /// Get ID for phase `<select>`
    fn id_phase(&self) -> String {
        format!("{}-phase", self.name)
    }

    /// Get ID for msg_pattern `<select>`
    fn id_msg_pattern(&self) -> String {
        format!("{}-msg_pattern", self.name)
    }

    /// Get ID for msg_priority `<input>`
    fn id_msg_priority(&self) -> String {
        format!("{}-msg_priority", self.name)
    }

    /// Get ID for sticky `<input>`
    fn id_sticky(&self) -> String {
        format!("{}-sticky", self.name)
    }

    /// Get ID for ignore_auto_fail `<input>`
    fn id_ignore_auto_fail(&self) -> String {
        format!("{}-ignore_auto_fail", self.name)
    }

    /// Update from input elements
    pub fn update_from_inputs(&mut self) {
        let doc = Doc::get();
        if let Some(hashtag) = doc.input_parse::<String>(&self.id_hashtag()) {
            self.hashtag = hashtag;
        }
        if let Some(phase) = doc.select_parse::<String>(&self.id_phase()) {
            self.phase = phase;
        }
        if let Some(pat) = doc.select_parse::<String>(&self.id_msg_pattern()) {
            self.msg_pattern = Some(pat).filter(|p| !p.is_empty());
        }
        if let Some(prio) = doc.select_parse::<u8>(&self.id_msg_priority()) {
            self.msg_priority = prio;
        }
        self.sticky = doc.input_bool(&self.id_sticky());
        self.ignore_auto_fail = doc.input_bool(&self.id_ignore_auto_fail());
    }

    /// Check ID for input element of this device action
    fn is_input_id(&self, id: &str) -> bool {
        id == self.id_hashtag()
            || id == self.id_phase()
            || id == self.id_msg_pattern()
            || id == self.id_msg_priority()
            || id == self.id_sticky()
            || id == self.id_ignore_auto_fail()
    }

    /// Update table row class with valid state
    pub fn update_class(&self, changed: bool, id: &str) -> bool {
        if self.is_input_id(id) {
            let doc = Doc::get();
            if let Some(el) = doc.opt_elem::<HtmlElement>(&self.name) {
                el.set_class_name(self.class_name(changed));
            }
            if let Some(el) = doc.opt_elem::<HtmlElement>(&self.id_summary()) {
                let mut tree = Tree::new();
                let mut summary = tree.root::<html::Summary>();
                self.summary_html(&mut summary, changed);
                el.set_outer_html(&String::from(tree));
            }
            true
        } else {
            false
        }
    }

    /// Get row element class name
    fn class_name(&self, changed: bool) -> &'static str {
        if self.is_valid() {
            if changed { "changed" } else { "" }
        } else {
            "invalid"
        }
    }

    /// Build HTML hashtag row
    fn hashtag_row<'p>(&self, div: &'p mut html::Div<'p>) {
        let id = self.id_hashtag();
        div.label().r#for(&id).cdata("Device #Tag").close();
        let mut input = div.input();
        input.id(id).maxlength(16).value(&self.hashtag);
        div.close();
    }

    /// Build HTML phase row
    fn phase_row<'p>(&self, phases: &[PlanPhase], div: &'p mut html::Div<'p>) {
        let id = self.id_phase();
        div.label().r#for(&id).cdata("Phase").close();
        let mut select = div.select();
        select.id(id);
        for p in phases {
            let mut option = select.option();
            if p.name == self.phase {
                option.selected();
            }
            option.cdata(&p.name).close();
        }
        select.close();
        div.close();
    }

    /// Build HTML msg_pattern row
    fn msg_pattern_row<'p>(
        &self,
        msg_patterns: &[MsgPattern],
        div: &'p mut html::Div<'p>,
    ) {
        let id = self.id_msg_pattern();
        div.label().r#for(&id).cdata("Msg Pattern").close();
        let mut select = div.select();
        select.id(id);
        let mut option = select.option();
        if self.msg_pattern.is_none() {
            option.selected();
        }
        option.close();
        for p in msg_patterns {
            let mut option = select.option();
            if Some(p.name.as_str()) == self.msg_pattern.as_deref() {
                option.selected();
            }
            option.cdata(&p.name).close();
        }
        select.close();
        div.close();
    }

    /// Build HTML msg_priority row
    fn msg_priority_row<'p>(&self, div: &'p mut html::Div<'p>) {
        let prio = MsgPriority::try_from(self.msg_priority)
            .unwrap_or(MsgPriority::Medium1);
        let id = self.id_msg_priority();
        div.label().r#for(&id).cdata("Priority").close();
        let mut select = div.select();
        select.id(id);
        for p in 1..=15 {
            if let Ok(p) = MsgPriority::try_from(p) {
                let mut option = select.option();
                option.value(p as u8);
                if p == prio {
                    option.selected();
                }
                option.cdata(p.as_str()).close();
            }
        }
        select.close();
        div.close();
    }

    /// Build HTML sticky row
    fn sticky_row<'p>(&self, div: &'p mut html::Div<'p>) {
        let id = self.id_sticky();
        div.label().r#for(&id).cdata("Sticky").close();
        let mut input = div.input();
        input.id(id).r#type("checkbox");
        if self.sticky {
            input.checked();
        }
        div.close();
    }

    /// Build HTML ignore_auto_fail row
    fn ignore_auto_fail_row<'p>(&self, div: &'p mut html::Div<'p>) {
        let id = self.id_ignore_auto_fail();
        div.label().r#for(&id).cdata("Ignore Auto-Fail").close();
        let mut input = div.input();
        input.id(id).r#type("checkbox");
        if self.ignore_auto_fail {
            input.checked();
        }
        div.close();
    }

    /// Make HTML summary
    fn summary_html<'p>(
        &self,
        summary: &'p mut html::Summary<'p>,
        changed: bool,
    ) {
        summary.id(self.id_summary());
        let hashtag = if self.is_valid() {
            &self.hashtag
        } else if changed {
            "*Invalid*"
        } else {
            "*New*"
        };
        summary.span().class("info").cdata(hashtag).close();
        if let Some(msg_pattern) = &self.msg_pattern {
            summary.cdata(": ").cdata(msg_pattern);
        }
        summary.close();
    }

    /// Make HTML details
    pub fn details_html<'p>(
        &self,
        phases: &[PlanPhase],
        msg_patterns: &[MsgPattern],
        details: &'p mut html::Details<'p>,
    ) {
        details.id(&self.name).class(self.class_name(false));
        self.summary_html(&mut details.summary(), false);
        self.hashtag_row(&mut details.div());
        self.phase_row(phases, &mut details.div());
        self.msg_pattern_row(msg_patterns, &mut details.div());
        self.msg_priority_row(&mut details.div());
        self.sticky_row(&mut details.div());
        self.ignore_auto_fail_row(&mut details.div());
        details.close();
    }
}
