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
    res: PhantomData<D>,
    pub controller: Option<Controller>,
}

impl<D> DeviceAnc<D> {
    pub fn controller_loc_html(&self) -> String {
        match &self.controller {
            Some(ctrl) => ctrl.button_link_html(),
            None => "".into(),
        }
    }
}

impl<D: Device> AncillaryData for DeviceAnc<D> {
    type Resource = D;

    /// Get ancillary URI
    fn uri(&self, view: View, res: &D) -> Option<Cow<str>> {
        match (view, &res.controller(), &self.controller) {
            (View::Edit, Some(ctrl), None) => {
                Some(format!("/iris/api/controller/{}", &ctrl).into())
            }
            _ => None,
        }
    }

    /// Put ancillary JSON data
    fn set_json(&mut self, _view: View, _res: &D, json: JsValue) -> Result<()> {
        self.controller = Some(json.into_serde::<Controller>()?);
        Ok(())
    }
}
