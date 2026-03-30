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
use web_sys::{Element, HtmlElement, PointerEvent};

/// Global map state
struct MapState {
    /// Map pane
    map: earthwyrm::Map,
    /// Pointerdown callback
    pointerdown: Closure<dyn Fn(PointerEvent)>,
    /// Pointerup (and pointercancel) callback
    pointerup: Closure<dyn Fn(PointerEvent)>,
    /// Pointermove callback
    pointermove: Closure<dyn Fn(PointerEvent)>,
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
            pointerdown: Closure::new(handle_map_pointerdown),
            pointerup: Closure::new(handle_map_pointerup),
            pointermove: Closure::new(handle_map_pointermove),
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

    /// Reset the map state
    fn reset(&mut self) {
        self.map.next_cycle();
        self.pan_point = (0, 0);
        self.is_panning = false;
        self.point = (0, 0);
    }
}

/// Handle a `pointerdown` event
fn handle_map_pointerdown(pe: PointerEvent) {
    if pe.button() == 0 {
        set_pan_point(true, pe.client_x(), pe.client_y());
        if let Some(target) = pe.target()
            && let Ok(elem) = target.dyn_into::<Element>()
            && let Err(e) = elem.set_pointer_capture(0)
        {
            log::warn!("set_pointer_capture: {e:?}");
        }
    }
}

/// Handle a `pointerup` or `pointercancel` event
fn handle_map_pointerup(pe: PointerEvent) {
    if pe.button() == 0 {
        set_pan_point(false, pe.client_x(), pe.client_y());
    }
}

/// Handle a `pointermove` event
fn handle_map_pointermove(pe: PointerEvent) {
    if let Some(map_pane) = panning_pane() {
        let (x, y) = translate(pe.client_x(), pe.client_y());
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
            "pointerdown",
            ms.pointerdown.as_ref().unchecked_ref(),
        )
        .unwrap_throw();
        mp.add_event_listener_with_callback(
            "pointerup",
            ms.pointerup.as_ref().unchecked_ref(),
        )
        .unwrap_throw();
        mp.add_event_listener_with_callback(
            "pointercancel",
            ms.pointerup.as_ref().unchecked_ref(),
        )
        .unwrap_throw();
        mp.add_event_listener_with_callback(
            "pointermove",
            ms.pointermove.as_ref().unchecked_ref(),
        )
        .unwrap_throw();
        *state = Some(ms);
    });
}

/// Get map pane
pub fn get_pane() -> Option<earthwyrm::Map> {
    MAP_STATE.with(|rc| {
        if let Some(ref mut state) = *rc.borrow_mut() {
            state.reset();
            Some(state.map.clone())
        } else {
            None
        }
    })
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
