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
}
