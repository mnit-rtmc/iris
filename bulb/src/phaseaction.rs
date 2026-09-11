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
use crate::dayplan::DayPlan;
use hatmil::html;
use jiff::civil::Date;
use serde::Deserialize;

/// Phase action
#[derive(Debug, Default, Deserialize, PartialEq)]
pub struct PhaseAction {
    pub name: String,
    pub action_plan: String,
    pub day_plan: Option<String>,
    pub condition: u32,
    pub params: Option<String>,
    pub from_phase: Option<String>,
    pub to_phase: String,
}

/// Action conditions
const CONDITIONS: &[&str] = &["⏳", "⏰", "🚗", "🌦️", "📢"];

impl PhaseAction {
    /// Create a new phase action
    pub fn new(name: &str, ap: &str, phase: &str) -> Self {
        PhaseAction {
            name: name.to_string(),
            action_plan: ap.to_string(),
            to_phase: phase.to_string(),
            ..Default::default()
        }
    }

    /// Check if phase action is valid
    pub fn is_valid(&self) -> bool {
        // FIXME
        true
    }

    /// Check if a phase action is active on a given day
    pub fn is_active(&self, day: &Date) -> bool {
        if 1 == self.condition
            && let Some(params) = &self.params
            && let Ok(dt) = params.parse::<Date>()
        {
            dt == *day
        } else {
            true
        }
    }

    /// Make HTML table row
    pub fn table_row<'p>(&self, tr: &'p mut html::Tr<'p>) {
        let params = self.params.as_deref().unwrap_or("");
        match self.condition {
            0 => {
                tr.td().cdata(params).cdata(" sec").close();
            }
            1 if let Some((_d, t)) = params.split_once('T') => {
                tr.td().cdata(t).close();
            }
            _ => {
                tr.td().cdata(params).close();
            }
        };
        tr.td().cdata(CONDITIONS[self.condition as usize]).close();
        match &self.from_phase {
            Some(from_phase) => tr.td().cdata(from_phase).close(),
            None => tr.td().class("info").cdata("*any*").close(),
        };
        tr.td().cdata("⇨").close();
        tr.td().cdata(&self.to_phase).close();
    }

    /// Get ID for summary
    fn id_summary(&self) -> String {
        format!("{}-summary", self.name)
    }

    /// Get ID for day_plan `<select>`
    fn id_day_plan(&self) -> String {
        format!("{}-day_plan", self.name)
    }

    /// Get row element class name
    fn class_name(&self) -> &'static str {
        if self.is_valid() { "" } else { "invalid" }
    }

    /// Build HTML day_plan row
    fn day_plan_row<'p>(
        &self,
        day_plans: &[DayPlan],
        div: &'p mut html::Div<'p>,
    ) {
        let id = self.id_day_plan();
        div.label().r#for(&id).cdata("Day Plan").close();
        let mut select = div.select();
        select.id(id);
        let mut option = select.option();
        if self.day_plan.is_none() {
            option.selected();
        }
        option.close();
        for dp in day_plans {
            let mut option = select.option();
            if Some(dp.name.as_str()) == self.day_plan.as_deref() {
                option.selected();
            }
            option.cdata(&dp.name).close();
        }
        select.close();
        div.close();
    }

    /// Make HTML summary
    fn summary_html<'p>(
        &self,
        summary: &'p mut html::Summary<'p>,
        update: bool,
    ) {
        summary.id(self.id_summary());
        let (cond, label) = if self.is_valid() {
            ("?", self.params.as_deref().unwrap_or(""))
        } else if update {
            ("", "*Invalid*")
        } else {
            ("", "*New*")
        };
        summary
            .cdata(cond)
            .span()
            .class("info")
            .cdata(label)
            .close();
        summary.close();
    }

    /// Make HTML details
    pub fn details_html<'p>(
        &self,
        day_plans: &[DayPlan],
        details: &'p mut html::Details<'p>,
    ) {
        details.id(&self.name).class(self.class_name());
        self.summary_html(&mut details.summary(), false);
        self.day_plan_row(day_plans, &mut details.div());
        // FIXME: the rest
        details.close();
    }
}
