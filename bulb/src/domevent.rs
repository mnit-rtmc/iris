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
use crate::error::Error;
use web_sys::{FocusEvent, MouseEvent};

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
    /// Update (from SSE notification)
    Update,
}
