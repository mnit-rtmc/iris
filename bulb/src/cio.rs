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
use crate::asset::Asset;
use crate::card::{AncillaryData, Card, View};
use crate::controller::Controller;
use crate::error::Result;
use crate::item::{ItemState, ItemStates};
use crate::util::opt_str;
use hatmil::Html;
use std::marker::PhantomData;
use wasm_bindgen::JsValue;

/// Controller IO resource
pub trait ControllerIo {
    /// Get controller name
    fn controller(&self) -> Option<&str> {
        None
    }
}

/// Ancillary controller IO data
#[derive(Debug, Default)]
pub struct ControllerIoAnc<C> {
    pri: PhantomData<C>,
    pub assets: Vec<Asset>,
    controllers: Vec<Controller>,
}

impl<C> ControllerIoAnc<C>
where
    C: ControllerIo,
{
    /// Find controller
    fn controller(&self, pri: &C) -> Option<&Controller> {
        if let Some(nm) = pri.controller() {
            for c in &self.controllers {
                if c.name == nm {
                    return Some(c);
                }
            }
        }
        None
    }

    /// Make controller row as HTML
    pub fn controller_html(&self, pri: &C, html: &mut Html) {
        html.div().class("row");
        html.label().r#for("controller").text("Controller").end();
        let input = html.input().id("controller").maxlength(20).size(20);
        match self.controller(pri) {
            Some(c) => {
                input.value(c.name());
                c.button_html(html);
            }
            None => {
                html.span().end(); /* empty */
            }
        }
        html.end(); /* div */
    }

    /// Make pin row as HTML
    pub fn pin_html(&self, pin: Option<u32>, html: &mut Html) {
        html.div().class("row");
        html.label().r#for("pin").text("Pin").end();
        html.input()
            .id("pin")
            .r#type("number")
            .min(1)
            .max(104)
            .size(8)
            .value(opt_str(pin));
        html.end(); /* div */
    }

    /// Get item states
    pub fn item_states<'a>(&'a self, pri: &'a C) -> ItemStates<'a> {
        self.controller(pri)
            .map_or(ItemState::Inactive.into(), |c| c.item_states())
    }
}

impl<C> AncillaryData for ControllerIoAnc<C>
where
    C: ControllerIo,
{
    type Primary = C;

    /// Construct ancillary controller IO data
    fn new(pri: &C, view: View) -> Self {
        let assets = match (view, &pri.controller()) {
            (View::Search, _) => vec![Asset::Controllers],
            (
                View::Hidden
                | View::Compact
                | View::Control
                | View::Setup
                | View::Status,
                Some(_nm),
            ) => vec![Asset::Controllers],
            _ => Vec::new(),
        };
        ControllerIoAnc {
            pri: PhantomData,
            assets,
            controllers: Vec::new(),
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        _pri: &C,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::Controllers => {
                self.controllers = serde_wasm_bindgen::from_value(value)?;
            }
            _ => unreachable!(),
        }
        Ok(())
    }
}
