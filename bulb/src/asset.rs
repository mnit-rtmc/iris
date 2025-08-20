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
use crate::error::{Error, Result};
use crate::fetch::{ContentType, Uri};
use resources::Res;
use wasm_bindgen::JsValue;

/// Fetchable assets for ancillary card data
#[derive(Clone, Debug, PartialEq)]
pub enum Asset {
    ActionPlans,
    BeaconStates,
    CabinetStyles,
    CommConfigs,
    CommLinks,
    CommProtocols,
    Conditions,
    // For all CIO pins for one controller
    ControllerIo(String),
    Controllers,
    DeviceActions,
    Directions,
    EncoderTypes,
    Font(String),
    Fonts,
    GateArmStates,
    GeoLoc(String, Res),
    Graphic(String),
    Graphics,
    HashtagResources,
    LcsIndications,
    LcsStates,
    LcsTypes,
    MeterAlgorithms,
    MeterTypes,
    MsgLines,
    MsgPatterns,
    PlanPhases,
    ResourceTypes,
    RoadModifiers,
    Roads,
    Roles,
    SignConfigs,
    SignMessages,
    TimeActions,
    Words,
}

impl Asset {
    /// Get asset Uri
    fn uri(&self) -> Uri {
        use Asset::*;
        match self {
            ActionPlans => "/iris/api/action_plan".into(),
            BeaconStates => "/iris/lut/beacon_state".into(),
            CabinetStyles => "/iris/api/cabinet_style".into(),
            CommConfigs => "/iris/api/comm_config".into(),
            CommLinks => "/iris/api/comm_link".into(),
            CommProtocols => "/iris/lut/comm_protocol".into(),
            Conditions => "/iris/lut/condition".into(),
            ControllerIo(nm) => {
                let mut uri = Uri::from("/iris/api/controller_io/");
                uri.push(nm);
                uri
            }
            Controllers => "/iris/api/controller".into(),
            DeviceActions => "/iris/api/device_action".into(),
            Directions => "/iris/lut/direction".into(),
            EncoderTypes => "/iris/api/encoder_type".into(),
            Font(nm) => {
                let mut uri = Uri::from("/iris/tfon/")
                    .with_content_type(ContentType::Text);
                uri.push(nm);
                uri.add_extension(".tfon");
                uri
            }
            Fonts => "/iris/api/font".into(),
            GateArmStates => "/iris/lut/gate_arm_state".into(),
            GeoLoc(nm, assoc) => {
                let mut uri = Uri::from("/iris/api/geo_loc");
                uri.push(nm);
                uri.query("res", assoc.as_str());
                uri
            }
            Graphic(nm) => {
                let mut uri =
                    Uri::from("/iris/gif/").with_content_type(ContentType::Gif);
                uri.push(nm);
                uri.add_extension(".gif");
                uri
            }
            Graphics => "/iris/api/graphic".into(),
            HashtagResources => "/iris/api/hashtag".into(),
            LcsIndications => "/iris/lut/lcs_indication".into(),
            LcsStates => "/iris/api/lcs_state".into(),
            LcsTypes => "/iris/lut/lcs_type".into(),
            MeterAlgorithms => "/iris/lut/meter_algorithm".into(),
            MeterTypes => "/iris/lut/meter_type".into(),
            MsgLines => "/iris/api/msg_line".into(),
            MsgPatterns => "/iris/api/msg_pattern".into(),
            PlanPhases => "/iris/api/plan_phase".into(),
            ResourceTypes => "/iris/lut/resource_type".into(),
            RoadModifiers => "/iris/lut/road_modifier".into(),
            Roads => "/iris/api/road".into(),
            Roles => "/iris/api/role".into(),
            SignConfigs => "/iris/api/sign_config".into(),
            SignMessages => "/iris/sign_message".into(),
            TimeActions => "/iris/api/time_action".into(),
            Words => "/iris/api/word".into(),
        }
    }

    /// Fetch the asset value
    pub async fn fetch(self) -> Result<Option<(Self, JsValue)>> {
        match self.uri().get().await {
            Ok(value) => Ok(Some((self, value))),
            Err(Error::FetchResponseNotFound())
            | Err(Error::FetchResponseForbidden()) => Ok(None),
            Err(e) => Err(e),
        }
    }
}
