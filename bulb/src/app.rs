// Copyright (C) 2022-2026  Minnesota Department of Transportation
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
use crate::card::CardList;
use crate::error::Result;
use crate::permission::AccessLevel;
use crate::query::QueryParam;
use crate::sse::NotifyState;
use crate::util;
use crate::view::CardView;
use resources::Res;
use std::cell::RefCell;
use std::collections::HashMap;

/// Interval (ms) between ticks for deferred actions
pub const TICK_INTERVAL: i32 = 500;

/// Deferred actions (called on set_interval)
#[derive(Clone, Copy, Debug, PartialEq)]
pub enum DeferredAction {
    /// Fetch station data
    FetchStationData,
    /// Hide the toast popup
    HideToast,
    /// Refresh resource list
    RefreshList,
    /// Make SSE event source
    MakeEventSource,
    /// Set notify state
    SetNotifyState(NotifyState),
}

/// Global app state
#[derive(Default)]
struct AppState {
    /// Logged-in user name
    user: Option<String>,
    /// SSE connection count
    connect_count: u32,
    /// Query parameters
    query: QueryParam,
    /// Presented map item (resource / name)
    presented_item: Option<(Res, String)>,
    /// Card list
    cards: Option<CardList>,
    /// Expanded card view
    expanded_view: Option<CardView>,
    /// Selected video monitor name (+restricted)
    vid_mon: Option<(String, bool)>,
    /// Deferred actions (with tick number)
    deferred: Vec<(i32, DeferredAction)>,
    /// Timer tick count
    tick: i32,
    /// Delete action enabled (slider transition finished)
    delete_enabled: bool,
    /// Active joystick interval IDs
    joystick_intervals: HashMap<u32, i32>,
    /// Active stream interval IDs
    stream_intervals: HashMap<String, i32>,
}

thread_local! {
    static STATE: RefCell<AppState> = RefCell::new(AppState::default());
}

/// Set logged-in user name in global app state
pub fn set_user(user: Option<String>) {
    STATE.with(|rc| rc.borrow_mut().user = user);
}

/// Get logged-in user name from global app state
pub fn user() -> Option<String> {
    STATE.with(|rc| rc.borrow().user.clone())
}

/// Set SSE connect count
pub fn set_connect_count(count: u32) {
    STATE.with(|rc| rc.borrow_mut().connect_count = count);
}

/// Get SSE connect count
pub fn connect_count() -> u32 {
    STATE.with(|rc| rc.borrow().connect_count)
}

/// Set query in global app state
pub fn set_query(query: QueryParam) {
    STATE.with(|rc| rc.borrow_mut().query = query);
}

/// Get query from global app state
pub fn query() -> QueryParam {
    STATE.with(|rc| rc.borrow().query.clone())
}

/// Set presented map item in global app state
pub fn present_item(item: Option<(Res, &str)>) {
    STATE.with(|rc| {
        rc.borrow_mut().presented_item =
            item.map(|(res, nm)| (res, nm.to_string()));
    });
}

/// Check if a map item is presented
pub fn is_presented_item(res: Res, nm: &str) -> bool {
    STATE.with(|rc| match &rc.borrow().presented_item {
        Some((r, n)) => (r, n.as_str()) == (&res, nm),
        _ => false,
    })
}

/// Get/set card list in global app state
pub fn card_list(cards: Option<CardList>) -> Option<CardList> {
    STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        let old_cards = state.cards.take();
        state.cards = cards;
        old_cards
    })
}

/// Check if card edit is allowed
pub fn can_edit_card() -> bool {
    STATE.with(|rc| {
        rc.borrow()
            .cards
            .as_ref()
            .map(|cl| cl.access_level() >= AccessLevel::Configure)
            .unwrap_or(false)
    })
}

/// Get the next suggested card name
pub fn next_card_name() -> Option<Result<String>> {
    STATE.with(|rc| rc.borrow().cards.as_ref().map(|cl| cl.next_name()))
}

/// Set expanded card view to global app state
pub fn set_expanded_view(view: Option<CardView>) {
    STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        state.expanded_view = view.filter(|cv| cv.view.is_expanded());
        // purge all deferred refresh list actions
        state
            .deferred
            .retain(|(_, a)| *a != DeferredAction::RefreshList);
        state.delete_enabled = false;
    })
}

/// Get expanded card view from global app state
pub fn expanded_view() -> Option<CardView> {
    STATE.with(|rc| rc.borrow().expanded_view.clone())
}

/// Set video monitor (+restricted) in global app state
pub fn set_vid_mon(vm: Option<(String, bool)>) {
    STATE.with(|rc| rc.borrow_mut().vid_mon = vm);
}

/// Get video monitor (+restricted) from global app state
pub fn vid_mon() -> Option<(String, bool)> {
    STATE.with(|rc| rc.borrow().vid_mon.clone())
}

/// Defer action to a future time
pub fn defer_action(action: DeferredAction, timeout_ms: i32) {
    STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        // don't defer more than one of any action
        state.deferred.retain(|(_, a)| *a != action);
        let delay = (timeout_ms + TICK_INTERVAL - 1) / TICK_INTERVAL;
        let tick = state.tick.saturating_add(delay);
        state.deferred.push((tick, action));
    });
}

/// Count one tick interval
pub fn tick_tock() {
    STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        // reset tick count if nothing's deferred
        state.tick = if state.deferred.is_empty() {
            0
        } else {
            state.tick.saturating_add(1)
        };
    });
}

/// Get the next deferred action
pub fn next_action() -> Option<DeferredAction> {
    STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        for i in 0..state.deferred.len() {
            let (tick, action) = state.deferred[i];
            if tick <= state.tick {
                state.deferred.swap_remove(i);
                return Some(action);
            }
        }
        None
    })
}

/// Set delete enabled/disabled in global app state
pub fn set_delete_enabled(enabled: bool) {
    STATE.with(|rc| rc.borrow_mut().delete_enabled = enabled);
}

/// Get delete enabled from global app state
pub fn delete_enabled() -> bool {
    STATE.with(|rc| rc.borrow().delete_enabled)
}

/// Add a joystick interval ID
pub fn add_joystick_interval_id(index: u32, id: i32) {
    STATE.with(|rc| rc.borrow_mut().joystick_intervals.insert(index, id));
}

/// Remove and return a joystick interval ID
pub fn remove_joystick_interval_id(index: &u32) -> Option<i32> {
    STATE.with(|rc| rc.borrow_mut().joystick_intervals.remove(index))
}

/// Add a stream interval ID
pub fn add_stream_interval_id(index: String, id: i32) {
    STATE.with(|rc| rc.borrow_mut().stream_intervals.insert(index, id));
}

/// Get a copy of all stream interval mappings
#[allow(dead_code)]
pub fn get_stream_intervals() -> HashMap<String, i32> {
    STATE.with(|rc| rc.borrow().stream_intervals.clone())
}

/// Stops the stream interval for the given source
pub fn stop_stream_interval(source: &String) -> crate::error::Result<()> {
    STATE.with(|rc| {
        let mut rc_mut = rc.borrow_mut();
        log::debug!("Intervals before removal: {:?}", rc_mut.stream_intervals);
        let window = util::window()?;
        if let Some(id) = rc_mut.stream_intervals.remove(source) {
            window.clear_interval_with_handle(id);
            log::debug!("Removed stream {source} (interval #{id})");
        } else {
            log::error!("No stream interval for {source}");
        }
        Ok(())
    })
}
