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
use web_sys::{DragEvent, MouseEvent};

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
