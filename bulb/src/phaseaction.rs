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
use crate::planphase::PlanPhase;
use hatmil::html;
use jiff::civil::Date;
use serde::Deserialize;

/// Action conditions
#[derive(Debug, Deserialize, PartialEq)]
pub struct ActCondition {
    pub id: u32,
    pub description: String,
    pub symbol: String,
}

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

/// Fields allowed for traffic threshold condition
const TRAFFIC_FIELDS: &[&str] = &["speed", "flow", "density", "occupancy"];

/// Fields allowed for RWIS threshold condition
const RWIS_FIELDS: &[&str] = &[
    "friction",
    "surface_temp",
    "wind_gust",
    "visibility",
    "precipitation",
];

/// Check if an action condition is valid
fn is_condition_valid(condition: u32, params: Option<&str>) -> bool {
    match (condition, params) {
        (0, Some(params)) => is_hold_time_valid(params),
        (1, Some(params)) => is_clock_time_valid(params),
        (2, Some(params)) => is_date_time_valid(params),
        (3, Some(params)) => is_threshold_valid(TRAFFIC_FIELDS, params),
        (4, Some(params)) => is_threshold_valid(RWIS_FIELDS, params),
        (5, Some(params)) => is_alarm_valid(params),
        _ => false,
    }
}

/// Check if hold time condition is valid
fn is_hold_time_valid(params: &str) -> bool {
    for p in params.splitn(3, ':') {
        // FIXME: p < 24 for HH:mm:ss
        match p.parse::<u8>() {
            Ok(p) if p < 60 => (),
            _ => return false,
        }
    }
    true
}

/// Check if clock time condition is valid
fn is_clock_time_valid(params: &str) -> bool {
    if let Some((hh, mm)) = params.split_once(':')
        && let (Ok(h), Ok(m)) = (hh.parse::<u8>(), mm.parse::<u8>())
        && h < 24
        && m < 60
    {
        return true;
    }
    false
}

/// Check if date-time condition is valid
fn is_date_time_valid(params: &str) -> bool {
    // FIXME: is it in the past?
    params.parse::<Date>().is_ok()
}

/// Check if threshold condition is valid
fn is_threshold_valid(fields: &[&str], params: &str) -> bool {
    if let Some((_sid, fval)) = params.split_once(',') {
        if let Some((f, val)) = fval.split_once('<') {
            return is_field_val_valid(fields, f, val);
        }
        if let Some((f, val)) = fval.split_once('>') {
            return is_field_val_valid(fields, f, val);
        }
    }
    false
}

/// Check if threshold field/value is valid
fn is_field_val_valid(fields: &[&str], f: &str, val: &str) -> bool {
    if val.parse::<u8>().is_err() {
        return false;
    }
    for field in fields {
        if field.starts_with(f) {
            return true;
        }
    }
    false
}

/// Check if alarm condition is valid
fn is_alarm_valid(params: &str) -> bool {
    if let Some((_aid, state)) = params.split_once(',') {
        return "triggered".starts_with(state) || "cleared".starts_with(state);
    }
    false
}

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
        // Don't allow both day_plan and date-time condition
        if self.day_plan.is_some() && 2 == self.condition {
            return false;
        }
        is_condition_valid(self.condition, self.params.as_deref())
    }

    /// Check if a phase action is active on a given day
    pub fn is_active(&self, day: &Date) -> bool {
        // condition 2 is "date-time"
        if 2 == self.condition
            && let Some(params) = &self.params
            && let Ok(dt) = params.parse::<Date>()
        {
            dt == *day
        } else {
            true
        }
    }

    /// Make HTML table row
    pub fn table_row<'p>(
        &self,
        conditions: &[ActCondition],
        tr: &'p mut html::Tr<'p>,
    ) {
        let sym = conditions
            .iter()
            .find(|c| c.id == self.condition)
            .map_or("❓", |c| &c.symbol);
        tr.td().cdata(sym).close();
        let params = self.params.as_deref().unwrap_or("");
        match self.condition {
            // clock time
            1 => {
                let mut td = tr.td();
                td.input().r#type("time").value(params).readonly();
                td.close();
            }
            // date-time
            2 => {
                let mut td = tr.td();
                td.input().r#type("datetime-local").value(params).readonly();
                td.close();
            }
            _ => {
                tr.td().cdata(params).close();
            }
        };
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

    /// Get ID for condition `<select>`
    fn id_condition(&self) -> String {
        format!("{}-condition", self.name)
    }

    /// Get ID for params input
    fn id_params(&self) -> String {
        format!("{}-params", self.name)
    }

    /// Get ID for from_phase `<select>`
    fn id_from_phase(&self) -> String {
        format!("{}-from_phase", self.name)
    }

    /// Get ID for to_phase `<select>`
    fn id_to_phase(&self) -> String {
        format!("{}-to_phase", self.name)
    }

    /// Get row element class name
    fn class_name(&self, changed: bool) -> &'static str {
        if self.is_valid() {
            if changed { "changed" } else { "" }
        } else {
            "invalid"
        }
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

    /// Build HTML condition row
    fn condition_row<'p>(
        &self,
        conditions: &[ActCondition],
        div: &'p mut html::Div<'p>,
    ) {
        let id = self.id_condition();
        div.label().r#for(&id).cdata("Condition").close();
        let mut select = div.select();
        select.id(id);
        for ac in conditions {
            let mut option = select.option();
            if self.condition == ac.id {
                option.selected();
            }
            option
                .value(ac.id)
                .cdata(&ac.symbol)
                .cdata(&ac.description)
                .close();
        }
        select.close();
        div.close();
    }

    /// Build HTML params row
    fn params_row<'p>(&self, div: &'p mut html::Div<'p>) {
        let id = self.id_params();
        div.label().r#for(&id).cdata("Params").close();
        let params = self.params.as_deref().unwrap_or("");
        let mut input = div.input();
        input.id(id).maxlength(16).value(params);
        div.close();
    }

    /// Build HTML from_phase row
    fn _from_phase_row<'p>(
        &self,
        phases: &[PlanPhase],
        div: &'p mut html::Div<'p>,
    ) {
        let id = self.id_from_phase();
        div.label().r#for(&id).cdata("From Phase").close();
        let mut select = div.select();
        select.id(id);
        let mut option = select.option();
        if self.from_phase.is_none() {
            option.selected();
        }
        option.close();
        for p in phases {
            let mut option = select.option();
            if Some(p.name.as_str()) == self.from_phase.as_deref() {
                option.selected();
            }
            option.cdata(&p.name).close();
        }
        select.close();
        div.close();
    }

    /// Build HTML to_phase row
    fn to_phase_row<'p>(
        &self,
        phases: &[PlanPhase],
        div: &'p mut html::Div<'p>,
    ) {
        let id = self.id_to_phase();
        div.label().r#for(&id).cdata("To Phase").close();
        let mut select = div.select();
        select.id(id);
        for p in phases {
            let mut option = select.option();
            if p.name == self.to_phase {
                option.selected();
            }
            option.cdata(&p.name).close();
        }
        select.close();
        div.close();
    }

    /// Make HTML summary
    fn summary_html<'p>(
        &self,
        conditions: &[ActCondition],
        summary: &'p mut html::Summary<'p>,
        changed: bool,
    ) {
        summary.id(self.id_summary());
        if self.is_valid() {
            self.summary_html_valid(conditions, summary);
        } else if changed {
            summary.class("info").cdata("*Invalid*").close();
        } else {
            summary.class("info").cdata("*New*").close();
        };
    }

    /// Make HTML summary (valid)
    fn summary_html_valid<'p>(
        &self,
        conditions: &[ActCondition],
        summary: &'p mut html::Summary<'p>,
    ) {
        let sym = conditions
            .iter()
            .find(|c| c.id == self.condition)
            .map_or("❓", |c| &c.symbol);
        summary.cdata(sym);
        let params = self.params.as_deref().unwrap_or("");
        summary.cdata(params);
        let fp = if self.from_phase.is_some() { "_" } else { "*" };
        summary.span().class("info").cdata(fp).close();
        summary.cdata("⇨").cdata(&self.to_phase).close();
    }

    /// Make HTML details
    pub fn details_html<'p>(
        &self,
        conditions: &[ActCondition],
        day_plans: &[DayPlan],
        phases: &[PlanPhase],
        details: &'p mut html::Details<'p>,
    ) {
        details.id(&self.name).class(self.class_name(false));
        self.summary_html(conditions, &mut details.summary(), false);
        self.day_plan_row(day_plans, &mut details.div());
        self.condition_row(conditions, &mut details.div());
        self.params_row(&mut details.div());
        self._from_phase_row(phases, &mut details.div());
        self.to_phase_row(phases, &mut details.div());
        details.close();
    }
}
