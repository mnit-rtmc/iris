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
use crate::domevent::{EventTp, FocusTp};
use crate::error::Result;
use crate::sidebar::handle_input;
use crate::util::Doc;
use wasm_bindgen::JsCast;
use wasm_bindgen::closure::Closure;
use web_sys::{
    Element, Event, HtmlElement, HtmlInputElement, HtmlOptionElement,
    KeyboardEvent,
};

pub fn add_listeners(el: &Element) -> Result<()> {
    add_key_listener(el)?;
    add_mouse_listener(el)?;
    Ok(())
}

/// Prevent dropdown from stealing mouse focus
fn add_mouse_listener(el: &Element) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        if let Some(Ok(target)) =
            e.target().map(|e| e.dyn_into::<HtmlElement>())
            && let Ok(Some(_)) =
                target.closest("[data-dropdown-field] > option")
        {
            e.prevent_default();
        }
    });
    el.add_event_listener_with_callback(
        "mousedown",
        closure.as_ref().unchecked_ref(),
    )?;
    closure.forget();
    Ok(())
}

/// Add a keyboard listener to dropdown element
fn add_key_listener(el: &Element) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        if let (Some(Ok(target)), Ok(ev)) = (
            e.target().map(|e| e.dyn_into::<HtmlInputElement>()),
            e.dyn_into::<KeyboardEvent>(),
        ) && let Some(list_id) = target.get_attribute("data-dropdown-list")
        {
            let (ev, sel) = match ev.key().as_ref() {
                "ArrowUp" => (
                    EventTp::KeyPress("ArrowUp"),
                    Doc::get().query::<HtmlOptionElement>(&format!(
                        "#{list_id} > .active"
                    )),
                ),
                "ArrowDown" => (
                    EventTp::KeyPress("ArrowDown"),
                    Doc::get().query::<HtmlOptionElement>(&format!(
                        "#{list_id} > .active"
                    )),
                ),
                "Enter" => (
                    EventTp::KeyPress("Enter"),
                    Doc::get().query::<HtmlOptionElement>(&format!(
                        "#{list_id} > .active"
                    )),
                ),
                _ => return,
            };
            handle_event(list_id, ev, sel);
        }
    });
    el.add_event_listener_with_callback(
        "keydown",
        closure.as_ref().unchecked_ref(),
    )?;
    closure.forget();
    Ok(())
}

/// Handle a `click` event to fill input field
pub fn handle_choose_option(
    input: &HtmlInputElement,
    list: &HtmlElement,
    sel: &HtmlOptionElement,
) {
    input.set_value(&sel.value());
    // Now handle the input as if it was typed
    handle_input(input.id());
    // Remove 'active' option label, hide dropdown (clicks don't focusout)
    let _ = sel.class_list().remove_1("active");
    let _ = list.style().set_property("display", "none");
}

/// Reset the filtered/active options
pub fn reset_filter(input: &HtmlInputElement) {
    if let Some(list) = input.get_attribute("data-dropdown-list")
        && let Some(list) = Doc::get().opt_elem::<HtmlElement>(&list)
    {
        let options = list.children();
        (0..options.length()).for_each(|i| {
            options
                .item(i)
                .map(|el| el.class_list().remove_2("active", "no-display"));
        });
    }
}

/// Get the next visible option in dropdown list
fn get_next_option(
    list: HtmlElement,
    active: &Option<HtmlOptionElement>,
    backwards: bool,
) -> Option<Element> {
    let init_val = active.as_ref().map(|a| a.value()).unwrap_or("".to_owned());
    let active = active.clone().map(Into::into);
    let mut current = active.clone().unwrap_or(if backwards {
        list.first_element_child()?
    } else {
        list.last_element_child()?
    });

    while let Some(el) = if backwards {
        current
            .previous_element_sibling()
            .or(list.last_element_child())
    } else {
        current
            .next_element_sibling()
            .or(list.first_element_child())
    } {
        if !el.class_list().contains("no-display") {
            return Some(el);
        } else if let Ok(el) = el.dyn_into::<HtmlOptionElement>() {
            if el.value() == init_val {
                return active;
            }
            current = el.into();
        } else {
            log::warn!("Non-option element found in dropdown list");
            return None;
        }
    }
    None
}

/// Handle dropdown events for input field
pub fn handle_event(
    list_id: String,
    ev: EventTp,
    active: Option<HtmlOptionElement>,
) {
    let Some(list) = Doc::get().opt_elem::<HtmlElement>(&list_id) else {
        log::warn!("No list of dropdown options");
        return;
    };
    let Some(input_id) = list.get_attribute("data-dropdown-field") else {
        log::warn!("No associated dropdown field");
        return;
    };
    let Some(input) = Doc::get().opt_elem::<HtmlInputElement>(&input_id) else {
        log::warn!("Could not find field #{input_id}");
        return;
    };

    match ev {
        EventTp::Focus(FocusTp::In) => {
            // Always start with no filter on dropdown
            reset_filter(&input);
            let _ = list.style().set_property("display", "flex");
        }
        EventTp::Focus(FocusTp::Out) => {
            let _ = list.style().set_property("display", "none");
        }
        EventTp::Click(_) | EventTp::KeyPress("Enter")
            if let Some(sel) = active =>
        {
            handle_choose_option(&input, &list, &sel)
        }
        EventTp::KeyPress(key) => {
            let Some(el) = (match key {
                "ArrowUp" => get_next_option(list, &active, true),
                "ArrowDown" => get_next_option(list, &active, false),
                _ => return,
            }) else {
                return;
            };

            let _ = el.class_list().add_1("active");
            let _ = active.map(|a| a.class_list().remove_1("active"));
        }
        EventTp::Input => {
            // Show if typing again after selection
            let _ = list.style().set_property("display", "flex");

            // Input filtering:
            let options = list.children();
            for i in 0..options.length() {
                if let Some(Ok(option)) =
                    options.item(i).map(|e| e.dyn_into::<HtmlOptionElement>())
                {
                    if !option
                        .value()
                        .to_uppercase()
                        .contains(&input.value().to_uppercase())
                    {
                        let _ = option.class_list().add_1("no-display");
                    } else {
                        let _ = option.class_list().remove_1("no-display");
                    }
                }
            }
            handle_input(input_id);
        }
        _ => (),
    }
}
