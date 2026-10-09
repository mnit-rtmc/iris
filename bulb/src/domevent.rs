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
use crate::error::{Error, Result};
use crate::util::Doc;
use hatmil::{Tree, css::Prop, html};
use wasm_bindgen::JsCast;
use web_sys::{DragEvent, FocusEvent, HtmlElement, MouseEvent, Node};

/// Mouse event type
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum MouseTp {
    /// `mousedown` event type
    Down,
    /// `mousemove` event type
    Move,
    /// `mouseup` event type
    Up,
}

impl TryFrom<&MouseEvent> for MouseTp {
    type Error = Error;

    fn try_from(me: &MouseEvent) -> std::result::Result<Self, Self::Error> {
        match me.type_().as_str() {
            "mousedown" => Ok(Self::Down),
            "mousemove" => Ok(Self::Move),
            "mouseup" => Ok(Self::Up),
            tp => Err(Error::UnknownEvent(tp.to_string())),
        }
    }
}

/// Drag event type
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum DragTp {
    /// `drag` event type
    Drag,
    /// `drop` event type
    Drop,
    /// `dragstart` event type
    Start,
    /// `dragend` event type
    End,
    /// `dragenter` event type
    Enter,
    /// `dragleave` event type
    Leave,
    /// `dragover` event type
    Over,
}

impl TryFrom<&DragEvent> for DragTp {
    type Error = Error;

    fn try_from(de: &DragEvent) -> std::result::Result<Self, Self::Error> {
        match de.type_().as_str() {
            "drag" => Ok(Self::Drag),
            "drop" => Ok(Self::Drop),
            "dragstart" => Ok(Self::Start),
            "dragend" => Ok(Self::End),
            "dragenter" => Ok(Self::Enter),
            "dragleave" => Ok(Self::Leave),
            "dragover" => Ok(Self::Over),
            tp => Err(Error::UnknownEvent(tp.to_string())),
        }
    }
}

impl DragTp {
    /// Handle a drag event
    pub fn handle(self, de: DragEvent, target: HtmlElement) -> Result<()> {
        match self {
            Self::Start => Self::start(de, target),
            Self::Over => Self::over(de, target),
            Self::Leave => Self::leave(de, target),
            Self::End => Self::end(de, target),
            Self::Drop => Self::drop(de, target),
            _ => Ok(()),
        }
    }

    /// Handle `dragstart`
    fn start(de: DragEvent, target: HtmlElement) -> Result<()> {
        if let Some(dt) = de.data_transfer() {
            let id = target.id();
            // custom "entry" type for play list entries
            dt.set_data("entry", &id)?;
            dt.set_effect_allowed("move");
        }
        target.class_list().add_1("dragging")?;
        Ok(())
    }

    /// Handle `dragover`
    fn over(de: DragEvent, target: HtmlElement) -> Result<()> {
        if let Some(dt) = de.data_transfer() {
            if dt.get_data("entry")?.is_empty() {
                return Ok(());
            }
            dt.set_drop_effect("move");
        }
        de.prevent_default();
        let y = f64::from(de.client_y());
        let placeholder =
            Doc::get().opt_elem::<HtmlElement>("drop-placeholder");
        if let Some(ph) = &placeholder {
            // is the cursor over existing placeholder
            let rect = ph.get_bounding_client_rect();
            if y >= rect.top() && y <= rect.bottom() {
                return Ok(());
            }
        }
        if let Some(parent) = target.parent_element() {
            let mut entry = None;
            let mut dragging = None;
            let mut height = 10; // FIXME
            let items = parent.children();
            for i in 0..items.length() {
                if let Some(ent) = items.item(i) {
                    if dragging.is_none()
                        && ent.class_name().contains("dragging")
                        && let Ok(el) = ent.clone().dyn_into::<HtmlElement>()
                    {
                        height = el.offset_height();
                        dragging = Some(i);
                    }
                    let rect = ent.get_bounding_client_rect();
                    if y >= rect.top() && y <= rect.bottom() {
                        entry = Some((i, ent));
                    }
                }
            }
            if let (Some((i, ent)), Some(d)) = (entry, dragging) {
                let position = if i < d {
                    Some("beforebegin")
                } else if i > d {
                    Some("afterend")
                } else {
                    None
                };
                match (position, placeholder) {
                    (Some(pos), Some(ph)) => {
                        ent.insert_adjacent_element(pos, &ph)?;
                    }
                    (Some(pos), None) => {
                        let ph = make_drop_placeholder(height);
                        ent.insert_adjacent_html(pos, &ph)?;
                    }
                    (None, Some(ph)) => ph.remove(),
                    _ => (),
                }
            }
        }
        Ok(())
    }

    /// Handle `dragleave`
    fn leave(de: DragEvent, target: HtmlElement) -> Result<()> {
        if let Some(parent) = target.parent_element()
            && let Some(rel) = de.related_target()
            && let Ok(nd) = rel.dyn_into::<Node>()
            && parent.contains(Some(&nd))
        {
            return Ok(());
        }
        if let Some(placeholder) =
            Doc::get().opt_elem::<HtmlElement>("drop-placeholder")
        {
            placeholder.remove();
        }
        Ok(())
    }

    /// Handle `dragend`
    fn end(_de: DragEvent, target: HtmlElement) -> Result<()> {
        if let Some(placeholder) =
            Doc::get().opt_elem::<HtmlElement>("drop-placeholder")
        {
            placeholder.remove();
        }
        target.class_list().remove_1("dragging")?;
        Ok(())
    }

    /// Handle `drop`
    fn drop(de: DragEvent, _target: HtmlElement) -> Result<()> {
        de.prevent_default();
        let placeholder =
            Doc::get().opt_elem::<HtmlElement>("drop-placeholder");
        if let Some(ph) = placeholder {
            if let Some(dt) = de.data_transfer() {
                let id = dt.get_data("entry")?;
                if let Some(dragging) = Doc::get().opt_elem::<HtmlElement>(&id)
                {
                    dragging.class_list().remove_1("dragging")?;
                    dragging.remove();
                    ph.insert_adjacent_element("beforebegin", &dragging)?;
                }
            }
            ph.remove();
        }
        Ok(())
    }
}

/// Make drop-placeholder item
fn make_drop_placeholder(height: i32) -> String {
    let mut tree = Tree::new();
    let mut li = tree.root::<html::Li>();
    li.id("drop-placeholder")
        .style(Prop::new().height(format!("{height}px")));
    String::from(tree)
}

/// Focus event type
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum FocusTp {
    /// `blur` event type
    Blur,
    /// `focus` event type
    Focus,
    /// `focusin` event type
    In,
    /// `focusout` event type
    Out,
}

impl TryFrom<&FocusEvent> for FocusTp {
    type Error = Error;

    fn try_from(fe: &FocusEvent) -> std::result::Result<Self, Self::Error> {
        match fe.type_().as_str() {
            "blur" => Ok(Self::Blur),
            "focus" => Ok(Self::Focus),
            "focusin" => Ok(Self::In),
            "focusout" => Ok(Self::Out),
            tp => Err(Error::UnknownEvent(tp.to_string())),
        }
    }
}

/// Event type
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum EventTp {
    /// Button click (w/ "Long-Press" flag)
    Click(bool),
    /// Input events
    Input,
    /// Mouse events
    Mouse(MouseTp),
    /// Focus events
    Focus(FocusTp),
}
