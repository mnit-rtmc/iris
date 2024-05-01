// Copyright (C) 2022-2024  Minnesota Department of Transportation
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
use crate::card::{CardList, CardView};
use std::cell::RefCell;

/// Interval (ms) between ticks for deferred actions
pub const TICK_INTERVAL: i32 = 500;

/// Deferred actions (called on set_interval)
#[derive(Clone, Copy, Debug, PartialEq)]
pub enum DeferredAction {
    /// Hide the toast popup
    HideToast,
    /// Refresh resource list
    RefreshList,
    /// Set refresh text
    SetRefreshText(&'static str),
}

/// Global app state
#[derive(Default)]
struct AppState {
    /// Have permissions been initialized?
    initialized: bool,
    /// Delete action enabled (slider transition finished)
    delete_enabled: bool,
    /// Logged-in user name
    user: Option<String>,
    /// Deferred actions (with tick number)
    deferred: Vec<(i32, DeferredAction)>,
    /// Timer tick count
    tick: i32,
    /// Card list
    cards: Option<CardList>,
}

thread_local! {
    static STATE: RefCell<AppState> = RefCell::new(AppState::default());
}

/// Set initialized app state
pub fn set_initialized() {
    STATE.with(|rc| rc.borrow_mut().initialized = true);
}

/// Get initialized app state
pub fn initialized() -> bool {
    STATE.with(|rc| rc.borrow().initialized)
}

/// Set card view to global app state
pub fn set_view(cv: CardView) {
    STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        let cards = state.cards.take();
        if let Some(mut cards) = cards {
            cards.set_view(cv);
            state.cards = Some(cards);
        }
        // purge all deferred refresh list actions
        state
            .deferred
            .retain(|(_, a)| *a != DeferredAction::RefreshList);
        state.delete_enabled = false;
    })
}

/// Get form card from global app state
pub fn form() -> Option<CardView> {
    STATE.with(|rc| rc.borrow().cards.as_ref().and_then(|cards| cards.form()))
}

/// Set delete enabled/disabled in global app state
pub fn set_delete_enabled(enabled: bool) {
    STATE.with(|rc| rc.borrow_mut().delete_enabled = enabled);
}

/// Get delete enabled from global app state
pub fn delete_enabled() -> bool {
    STATE.with(|rc| rc.borrow().delete_enabled)
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

/// Set logged-in user name in global app state
pub fn set_user(user: Option<String>) {
    STATE.with(|rc| rc.borrow_mut().user = user);
}

/// Get logged-in user name from global app state
pub fn user() -> Option<String> {
    STATE.with(|rc| rc.borrow().user.clone())
}

/// Defer action to a future time
pub fn defer_action(action: DeferredAction, timeout_ms: i32) {
    STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        // don't defer more than one refresh list action
        state
            .deferred
            .retain(|(_, a)| *a != DeferredAction::RefreshList);
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
