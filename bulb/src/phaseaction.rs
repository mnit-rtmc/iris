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
use crate::util::Doc;
use hatmil::{Tree, html};
use jiff::Zoned;
use jiff::civil::{Date, DateTime};
use serde::Deserialize;
use web_sys::HtmlElement;

/// Action conditions
#[derive(Debug, Deserialize, PartialEq)]
pub struct ActCondition {
    pub id: u32,
    pub description: String,
    pub symbol: String,
}

/// Phase action
#[derive(Clone, Debug, Default, Deserialize, PartialEq)]
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
    let params: Vec<_> = params
        .splitn(3, ':')
        .map(|p| p.parse::<u8>().ok())
        .collect();
    for p in params {
        match p {
            None => return false,
            Some(p) if p > 59 => return false,
            _ => (),
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
    if let Ok(dt) = params.parse::<DateTime>() {
        return dt >= Zoned::now().datetime();
    }
    false
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
    if f.is_empty() || val.parse::<u8>().is_err() {
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
        return !state.is_empty()
            && ("triggered".starts_with(state)
                || "cleared".starts_with(state));
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
        if self.day_plan.is_some() && self.is_condition_date_time() {
            return false;
        }
        is_condition_valid(self.condition, self.params.as_deref())
    }

    /// Check if condition is date-time
    fn is_condition_date_time(&self) -> bool {
        // condition 2 is "date-time"
        2 == self.condition
    }

    /// Check if date-time condition is expired
    fn is_date_time_expired(&self) -> bool {
        if self.is_condition_date_time()
            && let Some(params) = &self.params
            && let Ok(dt) = params.parse::<DateTime>()
        {
            return dt < Zoned::now().datetime();
        }
        false
    }

    /// Check if a phase action is active on a given day
    pub fn is_active(&self, day: &Date) -> bool {
        if self.is_condition_date_time()
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
        let mut td = tr.td();
        self.params_input(&mut td.input(), true);
        td.close();
        match &self.from_phase {
            Some(from_phase) => tr.td().class("info").cdata(from_phase).close(),
            None => tr.td().close(),
        };
        tr.td().cdata("🡆").close();
        tr.td().class("info").cdata(&self.to_phase).close();
    }

    /// Get ID for details
    fn id_details(&self) -> String {
        format!("pa-{}", self.name)
    }

    /// Get ID for summary
    fn id_summary(&self) -> String {
        format!("pa-{}-summary", self.name)
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

    /// Update from DOM
    pub fn update_from_dom(&mut self) {
        let doc = Doc::get();
        if let Some(dp) = doc.select_parse::<String>(&self.id_day_plan()) {
            self.day_plan = Some(dp).filter(|dp| !dp.is_empty());
        }
        if let Some(condition) = doc.select_parse(&self.id_condition()) {
            self.condition = condition;
        }
        if let Some(p) = doc.input_parse::<String>(&self.id_params()) {
            self.params = Some(p).filter(|p| !p.is_empty());
        }
        if let Some(p) = doc.select_parse::<String>(&self.id_from_phase()) {
            self.from_phase = Some(p).filter(|p| !p.is_empty());
        }
        if let Some(to_phase) = doc.select_parse(&self.id_to_phase()) {
            self.to_phase = to_phase;
        }
    }

    /// Check ID for input element of this phase action
    fn is_input_id(&self, id: &str) -> bool {
        id == self.id_day_plan()
            || id == self.id_condition()
            || id == self.id_params()
            || id == self.id_from_phase()
            || id == self.id_to_phase()
    }

    /// Update class with valid state
    pub fn update_class(
        &self,
        conditions: &[ActCondition],
        prev: &Self,
        id: &str,
    ) -> bool {
        let changed = prev != self;
        if self.is_input_id(id) {
            let doc = Doc::get();
            if let Some(el) = doc.opt_elem::<HtmlElement>(&self.id_details()) {
                el.set_class_name(self.class_name(changed));
            }
            if let Some(el) = doc.opt_elem::<HtmlElement>(&self.id_summary()) {
                let mut tree = Tree::new();
                let mut summary = tree.root::<html::Summary>();
                self.summary_html(conditions, &mut summary, changed);
                el.set_outer_html(&String::from(tree));
            }
            if self.id_condition() == id
                && let Some(el) = doc.opt_elem::<HtmlElement>(&self.id_params())
            {
                let mut tree = Tree::new();
                let mut input = tree.root::<html::Input>();
                self.params_input(&mut input, false);
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
        div.label().r#for(self.id_params()).cdata("Params").close();
        self.params_input(&mut div.input(), false);
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
        if self.is_valid() || self.is_date_time_expired() {
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
        self.params_input(&mut summary.input(), true);
        if self.from_phase.is_some() {
            summary.span().class("info").cdata("…").close();
        }
        summary
            .cdata("🡆")
            .span()
            .class("info")
            .cdata(&self.to_phase);
        summary.close();
    }

    /// Make params input HTML
    fn params_input<'p>(&self, input: &'p mut html::Input<'p>, readonly: bool) {
        if !readonly {
            input.id(self.id_params());
        }
        match self.condition {
            // clock time
            1 => input.r#type("time"),
            // date-time
            2 => {
                match Zoned::now()
                    .datetime()
                    .with()
                    .second(0)
                    .subsec_nanosecond(0)
                    .build()
                {
                    Ok(min) => {
                        input.r#type("datetime-local").min(min.to_string())
                    }
                    _ => input,
                }
            }
            _ => input,
        };
        let params = self.params.as_deref().unwrap_or("");
        input.value(params);
        if readonly {
            input.readonly().disabled();
        }
    }

    /// Make HTML details
    pub fn details_html<'p>(
        &self,
        conditions: &[ActCondition],
        day_plans: &[DayPlan],
        phases: &[PlanPhase],
        details: &'p mut html::Details<'p>,
    ) {
        details.id(self.id_details()).class(self.class_name(false));
        self.summary_html(conditions, &mut details.summary(), false);
        self.day_plan_row(day_plans, &mut details.div());
        self.condition_row(conditions, &mut details.div());
        self.params_row(&mut details.div());
        self._from_phase_row(phases, &mut details.div());
        self.to_phase_row(phases, &mut details.div());
        details.close();
    }
}
