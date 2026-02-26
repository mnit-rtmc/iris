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

/// Create a generic resource method for CardList or CardView
macro_rules! cards_meth {
    ($cards:ident, $meth:ident) => {
        match $cards.res {
            Res::ActionPlan => $cards.$meth::<ActionPlan>().await,
            Res::Alarm => $cards.$meth::<Alarm>().await,
            Res::Beacon => $cards.$meth::<Beacon>().await,
            Res::CabinetStyle => $cards.$meth::<CabinetStyle>().await,
            Res::Camera => $cards.$meth::<Camera>().await,
            Res::CommConfig => $cards.$meth::<CommConfig>().await,
            Res::CommLink => $cards.$meth::<CommLink>().await,
            Res::Controller => $cards.$meth::<Controller>().await,
            Res::Detector => $cards.$meth::<Detector>().await,
            Res::Dms => $cards.$meth::<Dms>().await,
            Res::Domain => $cards.$meth::<Domain>().await,
            Res::FlowStream => $cards.$meth::<FlowStream>().await,
            Res::GateArm => $cards.$meth::<GateArm>().await,
            Res::Gps => $cards.$meth::<Gps>().await,
            Res::Incident => $cards.$meth::<Incident>().await,
            Res::Lcs => $cards.$meth::<Lcs>().await,
            Res::LcsState => $cards.$meth::<LcsState>().await,
            Res::Modem => $cards.$meth::<Modem>().await,
            Res::MonitorStyle => $cards.$meth::<MonitorStyle>().await,
            Res::MsgPattern => $cards.$meth::<MsgPattern>().await,
            Res::Permission => $cards.$meth::<Permission>().await,
            Res::RampMeter => $cards.$meth::<RampMeter>().await,
            Res::Role => $cards.$meth::<Role>().await,
            Res::SignConfig => $cards.$meth::<SignConfig>().await,
            Res::SystemAttribute => $cards.$meth::<SystemAttr>().await,
            Res::TagReader => $cards.$meth::<TagReader>().await,
            Res::User => $cards.$meth::<User>().await,
            Res::VideoMonitor => $cards.$meth::<VideoMonitor>().await,
            Res::WeatherSensor => $cards.$meth::<WeatherSensor>().await,
            Res::Word => $cards.$meth::<Word>().await,
            _ => unreachable!(),
        }
    };

    ($cards:ident, $meth:ident, $param:expr) => {
        match $cards.res {
            Res::ActionPlan => $cards.$meth::<ActionPlan>($param).await,
            Res::Alarm => $cards.$meth::<Alarm>($param).await,
            Res::Beacon => $cards.$meth::<Beacon>($param).await,
            Res::CabinetStyle => $cards.$meth::<CabinetStyle>($param).await,
            Res::Camera => $cards.$meth::<Camera>($param).await,
            Res::CommConfig => $cards.$meth::<CommConfig>($param).await,
            Res::CommLink => $cards.$meth::<CommLink>($param).await,
            Res::Controller => $cards.$meth::<Controller>($param).await,
            Res::Detector => $cards.$meth::<Detector>($param).await,
            Res::Dms => $cards.$meth::<Dms>($param).await,
            Res::Domain => $cards.$meth::<Domain>($param).await,
            Res::FlowStream => $cards.$meth::<FlowStream>($param).await,
            Res::GateArm => $cards.$meth::<GateArm>($param).await,
            Res::Gps => $cards.$meth::<Gps>($param).await,
            Res::Incident => $cards.$meth::<Incident>($param).await,
            Res::Lcs => $cards.$meth::<Lcs>($param).await,
            Res::LcsState => $cards.$meth::<LcsState>($param).await,
            Res::Modem => $cards.$meth::<Modem>($param).await,
            Res::MonitorStyle => $cards.$meth::<MonitorStyle>($param).await,
            Res::MsgPattern => $cards.$meth::<MsgPattern>($param).await,
            Res::Permission => $cards.$meth::<Permission>($param).await,
            Res::RampMeter => $cards.$meth::<RampMeter>($param).await,
            Res::Role => $cards.$meth::<Role>($param).await,
            Res::SignConfig => $cards.$meth::<SignConfig>($param).await,
            Res::SystemAttribute => $cards.$meth::<SystemAttr>($param).await,
            Res::TagReader => $cards.$meth::<TagReader>($param).await,
            Res::User => $cards.$meth::<User>($param).await,
            Res::VideoMonitor => $cards.$meth::<VideoMonitor>($param).await,
            Res::WeatherSensor => $cards.$meth::<WeatherSensor>($param).await,
            Res::Word => $cards.$meth::<Word>($param).await,
            _ => unreachable!(),
        }
    };
}
