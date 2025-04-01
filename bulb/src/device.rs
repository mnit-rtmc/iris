// Copyright (C) 2022-2025  Minnesota Department of Transportation
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
use std::fmt;

/// Device requests
#[repr(u8)]
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
#[allow(dead_code)]
pub enum DeviceReq {
    NoRequest,
    QueryConfiguration,
    QuerySettings,
    SendSettings,
    QueryMessage,
    QueryStatus,
    QueryPixelFailures,
    TestPixels,
    TestRwis1,
    TestRwis2,
    BrightnessGood,
    BrightnessTooDim,
    BrightnessTooBright,
    ResetDevice,
    ResetStatus,
    QueryGpsLocation,
    DisableSystem,
    CameraFocusStop,
    CameraFocusNear,
    CameraFocusFar,
    CameraFocusManual,
    CameraFocusAuto,
    CameraIrisStop,
    CameraIrisClose,
    CameraIrisOpen,
    CameraIrisManual,
    CameraIrisAuto,
    CameraWiperOneShot,
    CameraWasher,
    CameraPowerOn,
    CameraPowerOff,
    CameraMenuOpen,
    CameraMenuEnter,
    CameraMenuCancel,
}

impl fmt::Display for DeviceReq {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        // format as JSON for setting device request
        write!(f, "{{\"device_request\":{}}}", *self as u8)
    }
}
