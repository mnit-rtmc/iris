// Copyright (C) 2026  Minnesota Department of Transportation
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
use crate::asset::Asset;
use crate::card::{AncillaryData, Card, footer_html, uri_all, uri_one};
use crate::eid;
use crate::error::Result;
use crate::fetch::Action;
use crate::item::ItemState;
use crate::util::{ContainsLower, Doc, Fields};
use crate::view::View;
use hatmil::{Tree, html};
use resources::Res;
use serde::Deserialize;
use serde_json::Value;
use serde_json::map::Map;
use std::borrow::Cow;
use wasm_bindgen::JsValue;
use web_sys::HtmlElement;

/// Months of the year
const MONTH: &[&str] = &[
    "", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
    "Nov", "Dec",
];

/// Days of the week
const WEEKDAY: &[&str] = &["", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

/// Weeks of the month
const WEEK: &[&str] = &["Last", "", "1st", "2nd", "3rd", "4th"];

/// Day Plan
#[derive(Clone, Debug, Default, Deserialize, PartialEq)]
pub struct DayPlan {
    pub name: String,
    pub holidays: bool,
}

/// Day Matcher
#[derive(Clone, Debug, Default, Deserialize, PartialEq)]
pub struct DayMatcher {
    pub name: String,
    pub day_plan: String,
    pub month: Option<i32>,
    pub day: Option<i32>,
    pub weekday: Option<i32>,
    pub week: Option<i32>,
    pub shift: Option<i32>,
}

/// Ancillary DayPlan data
#[derive(Debug, Default)]
pub struct DayPlanAnc {
    assets: Vec<Asset>,
    pub day_matchers: Vec<DayMatcher>,
    pub next_name: String,
}

impl AncillaryData for DayPlanAnc {
    type Primary = DayPlan;

    /// Construct ancillary day plan data
    fn new(_pri: &DayPlan, view: View) -> Self {
        let assets = match view {
            View::Setup(_) | View::SaveEv => {
                vec![Asset::DayMatchers]
            }
            _ => Vec::new(),
        };
        DayPlanAnc {
            assets,
            day_matchers: Vec::new(),
            next_name: String::new(),
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &DayPlan,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::DayMatchers => {
                let mut day_matchers: Vec<DayMatcher> =
                    serde_wasm_bindgen::from_value(value)?;
                self.next_name = next_matcher_name(&day_matchers);
                day_matchers.retain(|dm| dm.day_plan == pri.name);
                self.day_matchers = day_matchers;
            }
            _ => unreachable!(),
        }
        Ok(())
    }
}

/// Create next available matcher name
fn next_matcher_name(matchers: &[DayMatcher]) -> String {
    let mut num = 1;
    for dm in matchers {
        if let Some((pre, suffix)) = dm.name.split_once('_')
            && pre == "dm"
            && let Ok(n) = suffix.parse::<u32>()
        {
            num = num.max(n + 1);
        }
    }
    format!("dm_{num}")
}

impl DayMatcher {
    /// Get ID for month `<select>`
    fn id_month(&self) -> String {
        format!("{}-month", self.name)
    }

    /// Get ID for day `<input>`
    fn id_day(&self) -> String {
        format!("{}-day", self.name)
    }

    /// Get ID for weekday `<select>`
    fn id_weekday(&self) -> String {
        format!("{}-weekday", self.name)
    }

    /// Get ID for week `<select>`
    fn id_week(&self) -> String {
        format!("{}-week", self.name)
    }

    /// Get ID for shift `<input>`
    fn id_shift(&self) -> String {
        format!("{}-shift", self.name)
    }

    /// Update from input elements
    fn update_inputs(&mut self) {
        let doc = Doc::get();
        self.month =
            doc.select_parse::<i32>(&self.id_month()).filter(|m| *m > 0);
        self.day = doc
            .input_parse::<i32>(&self.id_day())
            .filter(|d| *d >= 1 && *d <= 31);
        self.weekday = doc
            .select_parse::<i32>(&self.id_weekday())
            .filter(|d| *d > 0);
        self.week =
            doc.select_parse::<i32>(&self.id_week()).filter(|w| *w != 0);
        self.shift =
            doc.input_parse::<i32>(&self.id_shift()).filter(|s| *s != 0);
    }

    /// Check ID for input element of this day matcher
    fn is_input_id(&self, id: &str) -> bool {
        id == self.id_month()
            || id == self.id_day()
            || id == self.id_weekday()
            || id == self.id_week()
            || id == self.id_shift()
    }

    /// Check if day matcher is valid
    fn is_valid(&self) -> bool {
        (self.month.is_some() || self.day.is_some() || self.weekday.is_some())
            && self.is_week_valid()
            && self.is_shift_valid()
    }

    /// Check if week is valid
    fn is_week_valid(&self) -> bool {
        self.week.is_none() || (self.day.is_none() && self.weekday.is_some())
    }

    /// Check if shift if valid
    fn is_shift_valid(&self) -> bool {
        self.shift.is_none()
            || (self.day.is_none()
                && self.weekday.is_some()
                && self.week.is_some())
    }

    /// Update table row class with valid state
    fn update_valid(&self, id: &str) -> bool {
        if self.is_input_id(id) {
            if let Some(el) = Doc::get().opt_elem::<HtmlElement>(&self.name) {
                el.set_class_name(self.class_name());
            }
            true
        } else {
            false
        }
    }

    /// Get row element class name
    fn class_name(&self) -> &'static str {
        if self.is_valid() { "" } else { "invalid" }
    }

    /// Build HTML month `<select>`
    fn month_select<'p>(&self, select: &'p mut html::Select<'p>) {
        select.id(self.id_month());
        for m in 0..=12 {
            let mut option = select.option();
            option.value(m);
            if Some(m) == self.month {
                option.selected();
            }
            option.cdata(MONTH[m as usize]).close();
        }
        select.close();
    }

    /// Build HTML day `<input>`
    fn day_input<'p>(&self, input: &'p mut html::Input<'p>) {
        input.id(self.id_day()).r#type("number").min(1).max(31);
        if let Some(d) = self.day {
            input.value(d);
        }
    }

    /// Build HTML weekday `<select>`
    fn weekday_select<'p>(&self, select: &'p mut html::Select<'p>) {
        select.id(self.id_weekday());
        for w in 0..=7 {
            let mut option = select.option();
            option.value(w);
            if Some(w) == self.weekday {
                option.selected();
            }
            option.cdata(WEEKDAY[w as usize]).close();
        }
        select.close();
    }

    /// Build HTML week `<select>`
    fn week_select<'p>(&self, select: &'p mut html::Select<'p>) {
        select.id(self.id_week());
        for w in -1..=4 {
            let mut option = select.option();
            option.value(w);
            if Some(w) == self.week || self.week.is_none() && w == 0 {
                option.selected();
            }
            option.cdata(WEEK[(w + 1) as usize]).close();
        }
        select.close();
    }

    /// Build HTML shift `<input>`
    fn shift_input<'p>(&self, input: &'p mut html::Input<'p>) {
        input.id(self.id_shift()).r#type("number").min(-2).max(2);
        if let Some(s) = self.shift {
            input.value(s);
        }
    }

    /// Build HTML table row
    fn table_row<'p>(&self, tr: &'p mut html::Tr<'p>) {
        tr.id(self.name.to_string());
        tr.class(self.class_name());
        let mut td = tr.td();
        self.month_select(&mut td.select());
        td = tr.td();
        self.day_input(&mut td.input());
        td = tr.td();
        self.weekday_select(&mut td.select());
        td = tr.td();
        self.week_select(&mut td.select());
        td = tr.td();
        self.shift_input(&mut td.input());
        tr.close();
    }

    /// Get set of changed fields
    fn changed_fields(&self, dm: &Self) -> Fields {
        let mut fields = Fields::new();
        if self.month != dm.month {
            fields.insert_opt_num("month", self.month);
        }
        if self.day != dm.day {
            fields.insert_opt_num("day", self.day);
        }
        if self.weekday != dm.weekday {
            fields.insert_opt_num("weekday", self.weekday);
        }
        if self.week != dm.week {
            fields.insert_opt_num("week", self.week);
        }
        if self.shift != dm.shift {
            fields.insert_opt_num("shift", self.shift);
        }
        fields
    }

    /// Convert to JSON value (for POST)
    fn value(&self) -> Value {
        let mut obj = Map::new();
        obj.insert("name".to_string(), Value::String(self.name.to_string()));
        obj.insert(
            "day_plan".to_string(),
            Value::String(self.day_plan.to_string()),
        );
        Value::Object(obj)
    }
}

impl DayPlan {
    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.cdata(self.name());
        String::from(tree)
    }

    /// Convert to Setup HTML
    #[allow(clippy::field_reassign_with_default)]
    fn to_html_setup(&self, anc: &DayPlanAnc, edit: bool) -> String {
        let mut tree = Tree::new();
        self.title(View::Setup(edit), &mut tree.root::<html::Div>());
        let mut div = tree.root::<html::Div>();
        div.class("row");
        if self.holidays {
            div.label().cdata("Holidays (inactive):");
        } else {
            div.label().cdata("Valid days (active):");
        }
        div.close();
        div = tree.root::<html::Div>();
        let mut table = div.table();
        let mut tr = table.tr();
        tr.th().cdata("Month");
        tr.th().cdata("Day");
        tr.th().cdata("Weekday");
        tr.th().cdata("Week");
        tr.th().cdata("Shift");
        tr.close();
        for dm in &anc.day_matchers {
            dm.table_row(&mut table.tr());
        }
        let mut dm = DayMatcher::default();
        dm.name = anc.next_name.clone();
        dm.day_plan = self.name.clone();
        dm.table_row(&mut table.tr());
        div.close();
        footer_html(View::Setup(edit), true, &mut tree.root::<html::Div>());
        String::from(tree)
    }
}

impl Card for DayPlan {
    type Ancillary = DayPlanAnc;

    /// Get the resource
    fn res() -> Res {
        Res::DayPlan
    }

    /// Get all item states
    fn item_states_all() -> &'static [ItemState] {
        &[ItemState::Planned]
    }

    /// Get the name
    fn name(&self) -> Cow<'_, str> {
        Cow::Borrowed(&self.name)
    }

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Get the main item state
    fn item_state_main(&self, _anc: &Self::Ancillary) -> ItemState {
        ItemState::Planned
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &DayPlanAnc) -> bool {
        self.name.contains_lower(search)
            || self.item_state_main(anc).is_match(search)
    }

    /// Convert to Create HTML
    fn to_html_create(&self, len: u32) -> String {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.class("row");
        div.label().r#for(eid::NAME).cdata("Name").close();
        div.input()
            .id(eid::NAME)
            .maxlength(len)
            .size(len.min(24))
            .value(self.name());
        div.close();
        div = tree.root::<html::Div>();
        div.class("row");
        div.label()
            .r#for("holidays")
            .cdata("Holidays (inactive)")
            .close();
        let mut input = div.input();
        input.id("holidays").r#type("checkbox");
        div.close();
        String::from(tree)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &DayPlanAnc) -> String {
        match view {
            View::Create => self.to_html_create(10),
            View::Setup(edit) => self.to_html_setup(anc, edit),
            _ => self.to_html_compact(),
        }
    }

    /// Handle input event for an element on the card
    #[allow(clippy::field_reassign_with_default)]
    fn handle_input(&self, anc: DayPlanAnc, id: &str) -> Vec<Action> {
        for dm in &anc.day_matchers {
            let mut ndm = dm.clone();
            ndm.update_inputs();
            if ndm.update_valid(id) {
                break;
            }
        }
        let mut dm = DayMatcher::default();
        dm.name = anc.next_name.clone();
        dm.day_plan = self.name.clone();
        dm.update_inputs();
        dm.update_valid(id);
        Vec::new()
    }

    /// Handle click event for the save button
    #[allow(clippy::field_reassign_with_default)]
    fn handle_save(&self, anc: Self::Ancillary) -> Vec<Action> {
        let mut actions = Vec::new();
        for dm in &anc.day_matchers {
            let mut ndm = dm.clone();
            ndm.update_inputs();
            if !ndm.is_valid() {
                let uri = uri_one(Res::DayMatcher, &dm.name);
                actions.push(Action::Delete(uri));
                continue;
            }
            if ndm != *dm {
                let fields = ndm.changed_fields(dm);
                let uri = uri_one(Res::DayMatcher, &dm.name);
                let val = fields.into_value().to_string();
                actions.push(Action::Patch(uri, val.into()));
            }
        }
        let mut dm = DayMatcher::default();
        dm.name = anc.next_name.clone();
        dm.day_plan = self.name.clone();
        let mut ndm = dm.clone();
        ndm.update_inputs();
        if ndm.is_valid() {
            let post_uri = uri_all(Res::DayMatcher);
            let patch_uri = uri_one(Res::DayMatcher, &ndm.name);
            let mut fields = ndm.changed_fields(&dm);
            fields.insert_str("name", &ndm.name);
            let value = ndm.value().to_string();
            actions.push(Action::Post(post_uri, value.into()));
            let changed = fields.into_value().to_string();
            actions.push(Action::Patch(patch_uri, changed.into()));
        }
        actions
    }
}
