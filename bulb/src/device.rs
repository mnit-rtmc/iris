// Copyright (C) 2022  Minnesota Department of Transportation
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
use crate::controller::{CommState, Controller};
use crate::error::Result;
use crate::resource::{AncillaryData, View};
use std::borrow::{Borrow, Cow};
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

    /// Get comm state
    pub fn comm_state(&self, pri: &D) -> CommState {
        match self.controller(pri) {
            Some(ctrl) => ctrl.comm_state(),
            None => CommState::Disabled,
        }
    }

    /// Is device active?
    pub fn is_active(&self, pri: &D) -> bool {
        match self.controller(pri) {
            Some(ctrl) => ctrl.is_active(),
            None => false,
        }
    }
}

const CONTROLLER_URI: &str = "/iris/api/controller";

impl<D: Device> AncillaryData for DeviceAnc<D> {
    type Primary = D;

    /// Get next ancillary URI
    fn next_uri(&self, view: View, pri: &D) -> Option<Cow<str>> {
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
                        Some(json.into_serde::<Vec<Controller>>()?);
                }
                _ => self.controller = Some(json.into_serde::<Controller>()?),
            }
        }
        Ok(())
    }
}
