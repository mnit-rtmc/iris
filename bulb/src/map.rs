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
    /// Mouseup callback
    mouseup: Closure<dyn Fn(MouseEvent)>,
    /// Mousemove callback
    mousemove: Closure<dyn Fn(MouseEvent)>,
    /// Panning flag
    panning: bool,
    /// Pan X
    pan_x: i32,
    /// Pan Y
    pan_y: i32,
}

thread_local! {
    static MAP_STATE: RefCell<Option<MapState>> = const { RefCell::new(None) };
}

/// Initialize map state
pub fn init(id: &str, groups: &'static [&'static str]) {
    let mp: HtmlElement = Doc::get().elem(id);
    let map = earthwyrm::Map::new(id, groups);
    MAP_STATE.with(|rc| {
        let mut state = rc.borrow_mut();
        let ms = MapState {
            map,
            mousedown: Closure::new(handle_map_mousedown),
            mouseup: Closure::new(handle_map_mouseup),
            mousemove: Closure::new(handle_map_mousemove),
            panning: false,
            pan_x: 0,
            pan_y: 0,
        };
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
            "mousemove",
            ms.mousemove.as_ref().unchecked_ref(),
        )
        .unwrap_throw();
        *state = Some(ms);
    });
}

/// Get map pane
pub fn pane() -> Option<earthwyrm::Map> {
    MAP_STATE.with(|rc| rc.borrow().as_ref().map(|ms| ms.map.clone()))
}

/// Reset pan point
pub fn reset_pan() {
    MAP_STATE.with(|rc| {
        if let Some(ref mut state) = *rc.borrow_mut() {
            state.pan_x = 0;
            state.pan_y = 0;
        }
    });
}

/// Set map panning flag
fn set_map_panning(panning: bool) {
    MAP_STATE.with(|rc| {
        if let Some(ref mut state) = *rc.borrow_mut() {
            state.panning = panning;
        }
    });
}

/// Check if map is being panned
fn is_map_panning() -> bool {
    MAP_STATE
        .with(|rc| rc.borrow().as_ref().map(|ms| ms.panning).unwrap_or(false))
}

/// Translate map position
fn translate(x: i32, y: i32) -> (i32, i32) {
    MAP_STATE.with(|rc| {
        if let Some(ref mut state) = *rc.borrow_mut() {
            state.pan_x += x;
            state.pan_y += y;
            (state.pan_x, state.pan_y)
        } else {
            (x, y)
        }
    })
}

/// Handle a `mousedown` event
fn handle_map_mousedown(me: MouseEvent) {
    if me.button() == 0 {
        set_map_panning(true);
    }
}

/// Handle a `mouseup` event
fn handle_map_mouseup(me: MouseEvent) {
    if me.button() == 0 {
        set_map_panning(false);
    }
}

/// Handle a `mousemove` event
fn handle_map_mousemove(me: MouseEvent) {
    if is_map_panning() {
        let (x, y) = translate(me.movement_x(), me.movement_y());
        if let Some(map_pane) = pane() {
            let _ = map_pane
                .set_style(&format!("transform: translate({x}px, {y}px);"));
        }
    }
}
