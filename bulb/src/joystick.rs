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
use crate::app;
use crate::card::{uri_one, uri_one_direct};
use crate::error::{Error, Result};
use crate::fetch::Action;
use crate::helper::spawn_future;
use crate::util::{self, Doc};
use hatmil::html;
use resources::Res;
use wasm_bindgen::JsCast;
use wasm_bindgen::closure::Closure;
use web_sys::{Gamepad, HtmlElement};

/// Turns a div into a joystick element
/// div: the div of the joystick
/// id: id of the joystick element
/// res: resource &str to send requests for
/// name: name of the device
/// fields: a format string to use as JSON data, with a "{}" for x and y
pub fn create_joy<'a>(
    div: &'a mut html::Div<'a>,
    id: &str,
    res: &str,
    name: &str,
    fields: &str,
    fields_direct: &str,
) {
    div.id(id);
    div.class("joystick");
    div.data_("res", res)
        .data_("name", name)
        .data_("fields", fields)
        .data_("fields-direct", fields_direct)
        .data_("x", "")
        .data_("y", "")
        .data_("start-x", "")
        .data_("start-y", "")
        .data_("max-diff", 50)
        .data_("last-updated", 0)
        .close();
}

/// Parse an attribute as type
/// Return None if doesn't exist or can't be converted
fn parse_attr<T: std::str::FromStr>(
    stick: &HtmlElement,
    attr: &str,
) -> Option<T> {
    stick.get_attribute(attr).and_then(|a| a.parse::<T>().ok())
}

/// Read an attribute as type
/// Return None if doesn't exist or can't be converted (e.g. empty/wrong data)
fn from_attr<T: for<'a> TryFrom<&'a str>>(
    stick: &HtmlElement,
    attr: &str,
) -> Option<T> {
    stick.get_attribute(attr).and_then(|a| T::try_from(&a).ok())
}

/// Format the JSON data for a joystick with x and y
fn format_field(
    stick: &HtmlElement,
    x: f64,
    y: f64,
    z: f64,
    fields_direct: &str,
) -> Option<String> {
    let x_val = format!("{:.1}", x);
    let y_val = format!("{:.1}", y);
    let z_val = format!("{:.1}", z);
    if let Some(f) = stick.get_attribute("data-fields") {
        return Some(
            f.replacen("{}", &x_val, 1)
                .replacen("{}", &y_val, 1)
                .replacen("{}", &z_val, 1)
                .replacen("{}", fields_direct, 1),
        );
    }
    None
}

/// Build the list of actions to perform based on normalized x and y
fn get_actions(stick: &HtmlElement, x: f64, y: f64) -> Vec<Action> {
    let fields_direct = stick.get_attribute("data-fields-direct");
    let fields_direct = fields_direct.as_deref().unwrap_or("");
    if let (Some(res), Some(name), Some(f)) = (
        from_attr::<Res>(stick, "data-res"),
        stick.get_attribute("data-name"),
        format_field(stick, x, y, 0.0, fields_direct),
    ) {
        log::debug!("{f}");
        return vec![Action::Patch(
            if fields_direct.is_empty() {
                uri_one(res, &name)
            } else {
                uri_one_direct(res, &name)
            },
            f.into(),
        )];
    }
    Vec::new()
}

/// Update the coordinate attributes on the element
/// Rounded to help determine if it should update on later change
fn update_attrs(stick: &HtmlElement, x: f64, y: f64) {
    let x_val = format!("{:.1}", x);
    let y_val = format!("{:.1}", y);
    stick.set_attribute("data-x", &x_val).ok();
    stick.set_attribute("data-y", &y_val).ok();
}

/// Determine if changed enough to justify update
fn should_update(stick: &HtmlElement, new_x: f64, new_y: f64) -> bool {
    if let (Some(x), Some(y)) = (
        parse_attr::<f64>(stick, "data-x"),
        parse_attr::<f64>(stick, "data-y"),
    ) {
        return (new_x - x).abs() > 0.1 || (new_y - y).abs() > 0.1;
    }
    true
}

/// Handle mouseup for a joystick
fn handle_mouse_up(stick: &HtmlElement) -> Vec<Action> {
    if let (Some(start_x), Some(start_y)) = (
        stick.get_attribute("data-start-x"),
        stick.get_attribute("data-start-y"),
    ) {
        // Haven't started moving the joystick
        if start_x.is_empty() || start_y.is_empty() {
            return Vec::new();
        }

        // Return element to 0px, 0px
        stick.style().set_property("transition", ".15s").ok();
        stick
            .style()
            .set_property("transform", "translate3d(0px, 0px, 0px)")
            .ok();

        // Clear attributes for next move
        stick.set_attribute("data-start-x", "").ok();
        stick.set_attribute("data-start-y", "").ok();
        stick.set_attribute("data-x", "").ok();
        stick.set_attribute("data-y", "").ok();
    }

    // Always update on mouseup
    get_actions(stick, 0.0, 0.0)
}

/// Handle mousedown for a joystick
fn handle_mouse_down(stick: &HtmlElement, x: i32, y: i32) -> Vec<Action> {
    stick.style().set_property("transition", "0s").ok();

    // Set start coords to see that user is dragging stick
    stick.set_attribute("data-start-x", &x.to_string()).ok();
    stick.set_attribute("data-start-y", &y.to_string()).ok();
    Vec::new()
}

/// Handle mousemove for a joystick
fn handle_mouse_move(
    stick: &HtmlElement,
    mouse_x: i32,
    mouse_y: i32,
    physical: bool,
) -> Vec<Action> {
    // If triggered by physical gamepad, only visual transform
    if physical
        && let Some(max_diff) = parse_attr::<f64>(stick, "data-max-diff")
    {
        let x = (mouse_x as f64 / i32::MAX as f64) * max_diff;
        let y = (mouse_y as f64 / i32::MAX as f64) * max_diff;
        let t = format!("translate3d({}px, {}px, 0px)", x, y);
        stick.style().set_property("transform", &t).ok();
        return vec![];
    }

    // Otherwise, handle Action too
    if let (Some(start_x), Some(start_y), Some(max_diff)) = (
        parse_attr::<i32>(stick, "data-start-x"),
        parse_attr::<i32>(stick, "data-start-y"),
        parse_attr::<f64>(stick, "data-max-diff"),
    ) {
        // Always transform the UI
        // Get input distances in pixels
        let x_diff: f64 = (mouse_x - start_x).into();
        let y_diff: f64 = (mouse_y - start_y).into();
        // Angle above positive x-axis
        let angle = y_diff.atan2(x_diff);
        // Clamp distance in pixels to a max_diff-radius circle
        let distance = max_diff.min(x_diff.hypot(y_diff));
        // Build the new components using angle, to match clamp
        let x = distance * angle.cos();
        let y = distance * angle.sin();
        // Now update the UI element
        let t = format!("translate3d({}px, {}px, 0px)", x, y);
        stick.style().set_property("transform", &t).ok();

        // Map components to range [-1.0, 1.0] for request
        // Also map screenspace (y++ moves down) to input (y++ moves up)
        // TODO: use (1.0, 1.0) at circumference, not (cos, sin)
        // Consider square boundary instead
        let (norm_x, norm_y) = (x / max_diff, -y / max_diff);

        // Only send an action if moved far enough
        if should_update(stick, norm_x, norm_y) {
            // Sending request, so update tracking attributes
            update_attrs(stick, norm_x, norm_y);
            // Use -y in request to convert from screenspace coords
            return get_actions(stick, norm_x, norm_y);
        }
    }

    // Haven't started moving stick, send nothing
    Vec::new()
}

/// Handle mouse move event for a joystick
pub async fn handle_mouse_event(
    id: String,
    type_: String,
    x: i32,
    y: i32,
) -> Result<()> {
    if let Some(target) = Doc::get().opt_elem::<HtmlElement>(&id) {
        let actions = match type_.as_str() {
            "mouseup" => handle_mouse_up(&target),
            "mousedown" => handle_mouse_down(&target, x, y),
            "mousemove" => handle_mouse_move(&target, x, y, false),
            _ => Vec::new(),
        };
        for action in actions {
            action.perform().await?;
        }
    }
    Ok(())
}

/// Handle movement for a physical joystick/gamepad
pub async fn handle_gamepad(id: String, axes: js_sys::Array) -> Result<()> {
    if let Some(stick) = Doc::get().opt_elem::<HtmlElement>(&id) {
        let fields_direct = stick.get_attribute("data-fields-direct");
        let fields_direct = fields_direct.as_deref().unwrap_or("");

        // Get PTZ axes clamped to [-1, 1]
        let mut x = axes.get(0).as_f64().unwrap_or(0.0).clamp(-1.0, 1.0);
        let mut y = axes.get(1).as_f64().unwrap_or(0.0).clamp(-1.0, 1.0);
        // Axis 4 seems to be up/down on second stick, with reversed direction
        let mut z = axes
            .get(3)
            .as_f64()
            .unwrap_or(-axes.get(2).as_f64().unwrap_or(0.0))
            .clamp(-1.0, 1.0);

        // Determine if joystick should be ignored, because physical input is stopped
        let dead_zone = 0.1;
        if x.abs() < dead_zone {
            x = 0.0;
        }
        if y.abs() < dead_zone {
            y = 0.0;
        }
        if z.abs() < dead_zone {
            z = 0.0;
        }

        // Whether physical input was stopped already
        let stopped = stick
            .get_attribute("stopped")
            .unwrap_or(String::from("true"))
            == "true";

        if let (Some(res), Some(name), Some(f)) = (
            from_attr::<Res>(&stick, "data-res"),
            stick.get_attribute("data-name"),
            format_field(&stick, x, -y, -z, fields_direct),
        ) {
            let x = (x * (i32::MAX as f64)) as i32;
            let y = (y * (i32::MAX as f64)) as i32;
            let z = (z * (i32::MAX as f64)) as i32;
            let sending_stop = x == 0 && y == 0 && z == 0;
            if stopped && sending_stop {
                // We were already stopped, and there's no significant stick input
                // Do nothing
                return Ok(());
            }
            // Update attribute and handle animation
            let sending_stop = &format!("{}", sending_stop);
            stick.set_attribute("stopped", sending_stop).ok();
            handle_mouse_move(&stick, x, y, true);

            // Now actually send PTZ action
            Action::Patch(
                if fields_direct.is_empty() {
                    uri_one(res, &name)
                } else {
                    uri_one_direct(res, &name)
                },
                f.into(),
            )
            .perform()
            .await?;
        }
    }
    Ok(())
}

/// Starts polling a gamepad
pub fn start_gamepad_poll(gamepad: Gamepad) -> Result<()> {
    let index = gamepad.index();
    let closure: Closure<dyn Fn()> = Closure::new(move || {
        if let Some(cv) = app::expanded_view()
            && cv.res == Res::Camera
        {
            let _ = update_gamepad_status(true);
            let axes = gamepad.axes();
            let sticks = Doc::get().0.get_elements_by_class_name("joystick");
            for i in 0..sticks.length() {
                if let Some(stick) = sticks.item(i) {
                    spawn_future(handle_gamepad(stick.id(), axes.clone()));
                }
            }
        }
    });
    let window = util::window()?;
    let id = window.set_interval_with_callback_and_timeout_and_arguments_0(
        closure.as_ref().unchecked_ref(),
        50,
    )?;
    app::add_joystick_interval_id(index, id);
    closure.forget();
    Ok(())
}

/// Stops polling a gamepad
pub fn stop_gamepad_poll(gamepad: Gamepad) -> Result<()> {
    if let Some(id) = app::remove_joystick_interval_id(&gamepad.index()) {
        util::window()?.clear_interval_with_handle(id);
    }
    Ok(())
}

/// Updates the UI with the status of the gamepad
/// Should only be called if there ever was a joystick connected
pub fn update_gamepad_status(connected: bool) -> Result<()> {
    // Get or create the status element
    let status_elem = if let Some(status) =
        Doc::get().opt_elem::<HtmlElement>("joystick-status")
    {
        status
    } else if let Some(ptz_controls) =
        Doc::get().opt_elem::<HtmlElement>("ptz-controls")
        && let Ok(status) = Doc::get()
            .0
            .create_element("div")?
            .dyn_into::<HtmlElement>()
    {
        status.set_class_name("row");
        status.set_id("joystick-status");
        ptz_controls.append_child(&status).ok();
        status
    } else {
        return Err(Error::JsValue(
            "couldn't get or create joystick status element".to_owned(),
        ));
    };

    let msg = if connected {
        status_elem.style().set_property("color", "#66C").ok();
        "Joystick connected"
    } else {
        status_elem.style().set_property("color", "#224").ok();
        "Joystick disconnected"
    };

    status_elem.set_text_content(Some(msg));
    Ok(())
}
