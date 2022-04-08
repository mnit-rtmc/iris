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
use crate::controller::Controller;
use crate::error::Result;
use crate::resource::{AncillaryData, Card, View};
use std::borrow::Cow;
use std::marker::PhantomData;
use wasm_bindgen::JsValue;

/// Device resource for controller IO
pub trait Device: Card {
    /// Get controller
    fn controller(&self) -> Option<&str> {
        None
    }
}

/// Ancillary controller IO device data
#[derive(Debug, Default)]
pub struct DeviceAnc<D> {
    pri: PhantomData<D>,
    pub controller: Option<Controller>,
}

impl<D> DeviceAnc<D> {
    pub fn controller_button(&self) -> String {
        match &self.controller {
            Some(ctrl) => ctrl.button_html(),
            None => "<span></span>".into(),
        }
    }
}

impl<D: Device> AncillaryData for DeviceAnc<D> {
    type Primary = D;

    /// Get ancillary URI
    fn uri(&self, view: View, pri: &D) -> Option<Cow<str>> {
        match (view, &pri.controller(), &self.controller) {
            (View::Status(true), Some(ctrl), None) => {
                Some(format!("/iris/api/controller/{}", &ctrl).into())
            }
            _ => None,
        }
    }

    /// Put ancillary JSON data
    fn set_json(&mut self, _view: View, _pri: &D, json: JsValue) -> Result<()> {
        self.controller = Some(json.into_serde::<Controller>()?);
        Ok(())
    }
}
