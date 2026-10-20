// Copyright (C) 2022-2026  Minnesota Department of Transportation
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
use crate::actionplan::ActionPlan;
use crate::asset::Asset;
use crate::card::{AncillaryData, Card};
use crate::devaction::DeviceAction;
use crate::error::Result;
use crate::query::QueryParam;
use crate::view::View;
use hatmil::html;
use resources::Res;
use std::collections::BTreeSet;
use std::fmt;
use std::marker::PhantomData;
use wasm_bindgen::JsValue;

/// Device resource
pub trait Device {
    /// Check if device has a given hashtag
    fn has_hashtag(&self, _hashtag: &str) -> bool {
        false
    }
}

/// Device action ancillary data
#[derive(Debug, Default)]
pub struct DeviceAnc<D> {
    pri: PhantomData<D>,
    assets: Vec<Asset>,
    device_actions: Vec<DeviceAction>,
    action_plans: Vec<ActionPlan>,
}

impl<D> AncillaryData for DeviceAnc<D>
where
    D: Device + Card,
{
    type Primary = D;

    /// Construct ancillary device data
    fn new(_pri: &D, view: View) -> Self {
        let assets = match view {
            View::Control => vec![Asset::DeviceActions],
            _ => Vec::new(),
        };
        DeviceAnc {
            assets,
            ..Default::default()
        }
    }

    /// Get next asset to fetch
    fn asset(&mut self) -> Option<Asset> {
        self.assets.pop()
    }

    /// Set asset value
    fn set_asset(
        &mut self,
        pri: &D,
        asset: Asset,
        value: JsValue,
    ) -> Result<()> {
        match asset {
            Asset::DeviceActions => {
                let mut act: Vec<DeviceAction> =
                    serde_wasm_bindgen::from_value(value)?;
                act.retain(|da| pri.has_hashtag(&da.hashtag));
                self.device_actions = act;
                // now that we have device actions, let's fetch the plans
                self.assets.push(Asset::ActionPlans);
            }
            Asset::ActionPlans => {
                let mut plans: Vec<ActionPlan> =
                    serde_wasm_bindgen::from_value(value)?;
                plans.retain(|p| {
                    p.active
                        && self
                            .device_actions
                            .iter()
                            .any(|da| da.action_plan == p.name)
                });
                self.action_plans = plans;
            }
            _ => (),
        }
        Ok(())
    }
}

impl<D> DeviceAnc<D>
where
    D: Device,
{
    /// Get phases for one action plan
    fn phases<'a>(
        &'a self,
        plan: &'a ActionPlan,
    ) -> impl Iterator<Item = &'a str> {
        let mut phases = BTreeSet::new();
        phases.insert(&plan.default_phase[..]);
        for da in &self.device_actions {
            if da.action_plan == plan.name {
                phases.insert(&da.phase[..]);
            }
        }
        phases.into_iter()
    }

    /// Render action plans as HTML
    pub fn render_action_plans<'p>(&self, details: &'p mut html::Details<'p>) {
        if self.action_plans.is_empty() {
            return;
        }
        details.summary().cdata("📋 Action Plans").close();
        let mut div = details.div();
        div.class("row fill");
        div.span().cdata("Name").close();
        div.span().cdata("Phase").close();
        div.close();
        for plan in &self.action_plans {
            let mut div = details.div();
            div.class("row fill");
            let query = QueryParam::new()
                .with_res(Res::ActionPlan)
                .with_sel(&plan.name);
            div.a().href(query.to_string()).cdata(&plan.name).close();
            let mut span = div.span();
            let mut select = span.select();
            select.id("phase").disabled();
            for p in self.phases(plan) {
                let mut option = select.option();
                if p == plan.phase {
                    option.selected();
                }
                option.cdata(p).close();
            }
            select.close();
            span.close();
            div.close();
        }
        details.close();
    }
}

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
