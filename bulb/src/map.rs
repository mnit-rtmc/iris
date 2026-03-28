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
use crate::util::Doc;
use std::cell::RefCell;
use wasm_bindgen::JsCast;
use wasm_bindgen::closure::Closure;
use wasm_bindgen::prelude::UnwrapThrowExt;
use web_sys::{HtmlElement, MouseEvent};

/// Global map state
struct MapState {
    /// Map pane
    map: earthwyrm::Map,
    /// Mousedown callback
    mousedown: Closure<dyn Fn(MouseEvent)>,
    /// Mouseup (and mouseleave) callback
    mouseup: Closure<dyn Fn(MouseEvent)>,
    /// Mousemove callback
    mousemove: Closure<dyn Fn(MouseEvent)>,
    /// Pan point
    pan_point: (i32, i32),
    /// Is panning flag
    is_panning: bool,
    /// Most recent point
    point: (i32, i32),
}

thread_local! {
    static MAP_STATE: RefCell<Option<MapState>> = const { RefCell::new(None) };
}

impl MapState {
    /// Make a new map state
    fn new(map: earthwyrm::Map) -> Self {
        MapState {
            map,
            mousedown: Closure::new(handle_map_mousedown),
            mouseup: Closure::new(handle_map_mouseup),
            mousemove: Closure::new(handle_map_mousemove),
            pan_point: (0, 0),
            is_panning: false,
            point: (0, 0),
        }
    }

    /// Start or stop panning
    fn set_panning(&mut self, panning: bool) {
        if panning != self.is_panning {
            let (x, y) = self.point;
            self.pan_point = if panning {
                (self.pan_point.0 + x, self.pan_point.1 + y)
            } else {
                (self.pan_point.0 - x, self.pan_point.1 - y)
            };
            self.is_panning = panning;
        }
    }

    /// Set pointer position
    fn set_point(&mut self, x: i32, y: i32) {
        self.point = (x, y);
    }

    /// Get translated pointer position
    fn point(&self) -> (i32, i32) {
        (
            self.point.0 - self.pan_point.0,
            self.point.1 - self.pan_point.1,
        )
    }
}

/// Handle a `mousedown` event
fn handle_map_mousedown(me: MouseEvent) {
    if me.button() == 0 {
        set_pan_point(true, me.client_x(), me.client_y());
    }
}

/// Handle a `mouseup` or `mouseleave` event
fn handle_map_mouseup(me: MouseEvent) {
    if me.button() == 0 {
        set_pan_point(false, me.client_x(), me.client_y());
    }
}

/// Handle a `mousemove` event
fn handle_map_mousemove(me: MouseEvent) {
    if let Some(map_pane) = panning_pane() {
        let (x, y) = translate(me.client_x(), me.client_y());
        let _ =
            map_pane.set_style(&format!("transform: translate({x}px, {y}px);"));
    }
}

/// Initialize map state
pub fn init(id: &str, groups: &'static [&'static str]) {
    let mp: HtmlElement = Doc::get().elem(id);
    let map = earthwyrm::Map::new(id, groups);
    MAP_STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        let ms = MapState::new(map);
        mp.add_event_listener_with_callback(
            "mousedown",
            ms.mousedown.as_ref().unchecked_ref(),
        )
        .unwrap_throw();
        mp.add_event_listener_with_callback(
            "mouseup",
            ms.mouseup.as_ref().unchecked_ref(),
        )
        .unwrap_throw();
        mp.add_event_listener_with_callback(
            "mouseleave",
            ms.mouseup.as_ref().unchecked_ref(),
        )
        .unwrap_throw();
        mp.add_event_listener_with_callback(
            "mousemove",
            ms.mousemove.as_ref().unchecked_ref(),
        )
        .unwrap_throw();
        *state = Some(ms);
    });
}

/// Get map pane
pub fn pane() -> Option<earthwyrm::Map> {
    MAP_STATE.with(|rc| {
        if let Some(ref mut state) = *rc.borrow_mut() {
            state.map.next_cycle();
            Some(state.map.clone())
        } else {
            None
        }
    })
}

/// Reset pan point
pub fn reset_pan() {
    MAP_STATE.with(|rc| {
        if let Some(ref mut state) = *rc.borrow_mut() {
            state.pan_point = (0, 0);
            state.is_panning = false;
        }
    });
}

/// Set map pan point
fn set_pan_point(start: bool, x: i32, y: i32) {
    MAP_STATE.with(|rc| {
        if let Some(ref mut state) = *rc.borrow_mut() {
            if start {
                state.set_point(x, y);
            }
            state.set_panning(start);
        }
    });
}

/// Get map pane if it's being panned
fn panning_pane() -> Option<earthwyrm::Map> {
    MAP_STATE.with(|rc| {
        if let Some(ref state) = *rc.borrow() {
            if state.is_panning {
                Some(state.map.clone())
            } else {
                None
            }
        } else {
            None
        }
    })
}

/// Translate map position
fn translate(x: i32, y: i32) -> (i32, i32) {
    MAP_STATE.with(|rc| {
        if let Some(ref mut state) = *rc.borrow_mut() {
            state.set_point(x, y);
            state.point()
        } else {
            (0, 0)
        }
    })
}
