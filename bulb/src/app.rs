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
use crate::card::{CardList, View};
use resources::Res;
use std::cell::RefCell;

/// Interval (ms) between ticks for deferred actions
pub const TICK_INTERVAL: i32 = 500;

/// Selected card state
#[derive(Clone, Debug)]
pub struct SelectedCard {
    /// Resource type
    pub res: Res,
    /// Card view
    pub view: View,
    /// Object name
    pub name: String,
    /// Delete action enabled (slider transition finished)
    pub delete_enabled: bool,
}

impl SelectedCard {
    /// Create a new blank selected card
    pub fn new(res: Res, view: View, name: String) -> Self {
        SelectedCard {
            res,
            view,
            name,
            delete_enabled: false,
        }
    }

    /// Get card element ID
    pub fn id(&self) -> String {
        let res = self.res;
        if self.view.is_create() {
            format!("{res}_")
        } else {
            format!("{res}_{}", &self.name)
        }
    }

    /// Set the card view to compact
    pub fn compact(mut self) -> Self {
        self.view = self.view.compact();
        self
    }

    /// Set the card view
    pub fn view(mut self, v: View) -> Self {
        self.view = v;
        self
    }
}

/// Deferred actions (called on set_interval)
#[derive(Clone, Copy, Debug, PartialEq)]
pub enum DeferredAction {
    /// Refresh resource list
    RefreshList,
    /// Hide the toast popup
    HideToast,
}

/// Global app state
#[derive(Default)]
struct AppState {
    /// Have permissions been initialized?
    initialized: bool,
    /// Logged-in user name
    user: Option<String>,
    /// Deferred actions (with tick number)
    deferred: Vec<(i32, DeferredAction)>,
    /// Timer tick count
    tick: i32,
    /// Selected card
    selected_card: Option<SelectedCard>,
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

/// Set selected card to global app state
pub fn set_selected_card(card: Option<SelectedCard>) -> Option<SelectedCard> {
    STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        let cs = state.selected_card.take();
        state.selected_card = card;
        // clear any deferred refresh actions
        state
            .deferred
            .retain(|(_, a)| *a != DeferredAction::RefreshList);
        cs
    })
}

/// Get selected card from global app state
pub fn selected_card() -> Option<SelectedCard> {
    STATE.with(|rc| rc.borrow().selected_card.clone())
}

/// Set delete action enabled/disabled
pub fn set_delete_enabled(enabled: bool) {
    STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        if let Some(selected_card) = &mut state.selected_card {
            selected_card.delete_enabled = enabled;
        }
    });
}

/// Set card list in global app state
pub fn set_card_list(cards: Option<CardList>) {
    STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        state.cards = cards;
    });
}

/// Get card list from global app state
pub fn card_list() -> Option<CardList> {
    STATE.with(|rc| rc.borrow().cards.clone())
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
