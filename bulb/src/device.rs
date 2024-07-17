// Copyright (C) 2022-2024  Minnesota Department of Transportation
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
use crate::asset::Asset;
use crate::card::{AncillaryData, Card, View};
use crate::controller::Controller;
use crate::error::Result;
use crate::item::ItemState;
use crate::util::HtmlStr;
use std::borrow::Cow;
use std::marker::PhantomData;
use wasm_bindgen::JsValue;

/// Device requests
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

/// Device resource for controller IO
pub trait Device {
    /// Get controller
    fn controller(&self) -> Option<&str> {
        None
    }
}

/// Ancillary controller IO device data
#[derive(Debug, Default)]
pub struct DeviceAnc<D> {
    pri: PhantomData<D>,
    pub assets: Vec<Asset>,
    pub controllers: Option<Vec<Controller>>,
    pub controller: Option<Controller>,
}

impl<D: Device> DeviceAnc<D> {
    /// Find controller
    fn controller(&self, pri: &D) -> Option<&Controller> {
        if let Some(ctrl) = &self.controller {
            return Some(ctrl);
        }
        if let (Some(ctrl), Some(controllers)) =
            (pri.controller(), &self.controllers)
        {
            for c in controllers {
                if c.name == ctrl {
                    return Some(c);
                }
            }
        }
        None
    }

    /// Make controller button
    fn controller_button(&self) -> String {
        match &self.controller {
            Some(ctrl) => ctrl.button_html(),
            None => "<span></span>".into(),
        }
    }

    /// Make controller row as HTML
    pub fn controller_html(&self) -> String {
        let ctl_btn = self.controller_button();
        let controller = match &self.controller {
            Some(c) => HtmlStr::new(c.name()),
            None => HtmlStr::new(Cow::Borrowed("")),
        };
        format!(
            "<div class='row'>\
              <label for='controller'>Controller</label>\
              <input id='controller' maxlength='20' size='20' \
                     value='{controller}'>\
              {ctl_btn}\
            </div>"
        )
    }

    /// Get item state
    pub fn item_state(&self, pri: &D) -> ItemState {
        self.controller(pri)
            .map_or(ItemState::Inactive, |c| c.item_state())
    }
}

impl<D: Device> AncillaryData for DeviceAnc<D> {
    type Primary = D;

    /// Construct ancillary device data
    fn new(pri: &D, view: View) -> Self {
        let assets = match (view, &pri.controller()) {
            (View::Search, _) => vec![Asset::Controllers],
            (
                View::Hidden
                | View::Compact
                | View::Control
                | View::Setup
                | View::Status,
                Some(ctrl),
            ) => vec![Asset::Controller(ctrl.to_string())],
            _ => Vec::new(),
        };
        DeviceAnc {
            pri: PhantomData,
            assets,
            controllers: None,
            controller: None,
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        _pri: &D,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::Controllers => {
                self.controllers = Some(serde_wasm_bindgen::from_value(value)?);
            }
            Asset::Controller(_ctrl) => {
                self.controller = Some(serde_wasm_bindgen::from_value(value)?);
            }
            _ => unreachable!(),
        }
        Ok(())
    }
}
