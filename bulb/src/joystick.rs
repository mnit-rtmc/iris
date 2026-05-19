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
use crate::card::uri_one;
use crate::error::Result;
use crate::fetch::Action;
use crate::util::Doc;
use hatmil::html;
use resources::Res;
use web_sys::HtmlElement;

/// Turn the div into a joystick element
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
) {
    div.id(id);
    div.class("joystick");
    div.data_("res", res)
        .data_("name", name)
        .data_("fields", fields)
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
fn format_field(stick: &HtmlElement, x: f64, y: f64) -> Option<String> {
    let x_val = format!("{:.1}", x);
    let y_val = format!("{:.1}", y);
    if let Some(f) = stick.get_attribute("data-fields") {
        return Some(f.replacen("{}", &x_val, 1).replacen("{}", &y_val, 1));
    }
    None
}

/// Build the list of actions to perform based on normalized x and y
fn get_actions(stick: &HtmlElement, x: f64, y: f64) -> Vec<Action> {
    if let (Some(res), Some(name), Some(f)) = (
        from_attr::<Res>(stick, "data-res"),
        stick.get_attribute("data-name"),
        format_field(stick, x, y),
    ) {
        return vec![Action::Patch(uri_one(res, &name), f.into())];
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
) -> Vec<Action> {
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
            "mousemove" => handle_mouse_move(&target, x, y),
            _ => Vec::new(),
        };
        for action in actions {
            //log::debug!("Sending action {:?}", action);
            action.perform().await?;
        }
    }
    Ok(())
}
