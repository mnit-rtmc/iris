// Copyright (C) 2022-2023  Minnesota Department of Transportation
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
use crate::controller::Controller;
use crate::error::Result;
use crate::fetch::Uri;
use crate::item::ItemState;
use crate::resource::{AncillaryData, View};
use std::marker::PhantomData;
use wasm_bindgen::JsValue;

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
    pub controllers: Option<Vec<Controller>>,
    pub controller: Option<Controller>,
}

impl<D: Device> DeviceAnc<D> {
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

    pub fn controller_button(&self) -> String {
        match &self.controller {
            Some(ctrl) => ctrl.button_html(),
            None => "<span></span>".into(),
        }
    }

    /// Get item state
    pub fn item_state(&self, pri: &D) -> ItemState {
        self.controller(pri)
            .map_or(ItemState::Disabled, |c| c.item_state())
    }
}

const CONTROLLER_URI: &str = "/iris/api/controller";

impl<D: Device> AncillaryData for DeviceAnc<D> {
    type Primary = D;

    /// Get next ancillary URI
    fn next_uri(&self, view: View, pri: &D) -> Option<Uri> {
        match (view, &self.controllers, &pri.controller(), &self.controller) {
            (View::Search, None, _, _) => Some(CONTROLLER_URI.into()),
            (View::Compact | View::Status(_), _, Some(ctrl), None) => {
                Some(format!("/iris/api/controller/{}", &ctrl).into())
            }
            _ => None,
        }
    }

    /// Put ancillary JSON data
    fn set_json(&mut self, view: View, pri: &D, json: JsValue) -> Result<()> {
        if let Some(uri) = self.next_uri(view, pri) {
            match uri.borrow() {
                CONTROLLER_URI => {
                    self.controllers =
                        Some(serde_wasm_bindgen::from_value(json)?);
                }
                _ => {
                    self.controller =
                        Some(serde_wasm_bindgen::from_value(json)?)
                }
            }
        }
        Ok(())
    }
}
