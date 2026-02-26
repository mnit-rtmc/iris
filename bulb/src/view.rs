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
use crate::alarm::Alarm;
use crate::beacon::Beacon;
use crate::cabinetstyle::CabinetStyle;
use crate::camera::Camera;
use crate::card::{Card, fetch_ancillary, uri_all, uri_one};
use crate::commconfig::CommConfig;
use crate::commlink::CommLink;
use crate::controller::Controller;
use crate::detector::Detector;
use crate::dms::Dms;
use crate::domain::Domain;
use crate::error::{Error, Result};
use crate::flowstream::FlowStream;
use crate::gatearm::GateArm;
use crate::geoloc::Loc;
use crate::gps::Gps;
use crate::incident::Incident;
use crate::lcs::Lcs;
use crate::lcsstate::LcsState;
use crate::modem::Modem;
use crate::monitorstyle::MonitorStyle;
use crate::msgpattern::MsgPattern;
use crate::permission::Permission;
use crate::rampmeter::RampMeter;
use crate::role::Role;
use crate::signconfig::SignConfig;
use crate::systemattr::SystemAttr;
use crate::tagreader::TagReader;
use crate::user::User;
use crate::util::Doc;
use crate::videomonitor::VideoMonitor;
use crate::weathersensor::WeatherSensor;
use crate::word::Word;
use hatmil::{Page, html};
use resources::Res;
use serde_json::Value;
use serde_json::map::Map;

/// Card element view
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum View {
    /// Hidden view
    Hidden,
    /// Search view
    Search,
    /// Compact Create view
    CreateCompact,
    /// Create view
    Create,
    /// Compact view
    Compact,
    /// Control view
    Control,
    /// Setup view
    Setup,
    /// Status view
    Status,
    /// Location view
    Location,
    /// Request view
    Request,
}

impl View {
    /// Get view class name
    pub const fn class_name(self) -> &'static str {
        match self {
            View::Hidden | View::Search => "no-display",
            View::CreateCompact | View::Compact => "card-compact",
            _ => "card-form",
        }
    }

    /// Is the view a form?
    pub fn is_form(self) -> bool {
        match self {
            View::Hidden
            | View::Search
            | View::CreateCompact
            | View::Compact => false,
            View::Create
            | View::Control
            | View::Setup
            | View::Status
            | View::Location
            | View::Request => true,
        }
    }

    /// Get compact view
    pub fn compact(self) -> Self {
        match self {
            View::Create => View::CreateCompact,
            _ => View::Compact,
        }
    }

    /// Get view as string slice
    pub fn as_str(self) -> &'static str {
        use View::*;
        match self {
            Hidden => "Hidden",
            Search => "Search",
            CreateCompact => "Create compact",
            Create => "🆕 Create",
            Compact => "⌄ Compact",
            Control => "🕹️ Control",
            Setup => "📝 Setup",
            Status => "☑️ Status",
            Location => "🗺️ Location",
            Request => "🙏 Request",
        }
    }
}

impl TryFrom<&str> for View {
    type Error = ();

    fn try_from(type_n: &str) -> std::result::Result<Self, Self::Error> {
        use View::*;
        match type_n {
            v if v == Hidden.as_str() => Ok(Hidden),
            v if v == Search.as_str() => Ok(Search),
            v if v == CreateCompact.as_str() => Ok(CreateCompact),
            v if v == Create.as_str() => Ok(Create),
            v if v == Compact.as_str() => Ok(Compact),
            v if v == Control.as_str() => Ok(Control),
            v if v == Setup.as_str() => Ok(Setup),
            v if v == Status.as_str() => Ok(Status),
            v if v == Location.as_str() => Ok(Location),
            v if v == Request.as_str() => Ok(Request),
            _ => Err(()),
        }
    }
}

/// Card view
#[derive(Clone, Debug, PartialEq, Eq)]
pub struct CardView {
    /// Resource type
    pub res: Res,
    /// Object name
    pub name: String,
    /// Card view
    pub view: View,
}

impl CardView {
    /// Create a new card view
    pub fn new<N: Into<String>>(res: Res, name: N, view: View) -> Self {
        let name = name.into();
        CardView { res, name, view }
    }

    /// Get HTML element ID of card
    pub fn id(&self) -> String {
        let res = self.res;
        let nm = match self.view {
            View::CreateCompact | View::Create => "",
            _ => &self.name,
        };
        format!("{res}_{nm}")
    }

    /// Set the view to compact
    pub fn compact(mut self) -> Self {
        self.view = self.view.compact();
        self
    }

    /// Set the view
    pub fn view(mut self, v: View) -> Self {
        self.view = v;
        self
    }

    /// Fetch a card for a given view
    pub async fn fetch_one(&self) -> Result<String> {
        let html = match self.view {
            View::CreateCompact => {
                let mut page = Page::new();
                let mut span = page.frag::<html::Span>();
                span.class("create").cdata("Create 🆕");
                String::from(page)
            }
            View::Create => {
                let html = self.fetch_one_res().await?;
                html_card_create(self.res, &html)
            }
            _ => self.fetch_one_res().await?,
        };
        Ok(html)
    }

    /// Fetch a card view
    async fn fetch_one_res(&self) -> Result<String> {
        cards_meth!(self, fetch_one_x)
    }

    /// Fetch a card view
    async fn fetch_one_x<C: Card>(&self) -> Result<String> {
        let pri = if self.view == View::Create {
            C::default().with_name(&self.name)
        } else {
            self.fetch_primary::<C>().await?
        };
        let anc = fetch_ancillary(&pri, self.view).await?;
        Ok(pri.to_html(self.view, &anc))
    }

    /// Fetch primary JSON resource
    async fn fetch_primary<C: Card>(&self) -> Result<C> {
        let uri = uri_one(C::res(), &self.name);
        let json = uri.get().await?;
        C::new(json)
    }

    /// Patch changed fields on Setup / Location view
    pub async fn patch_changed(&self) -> Result<()> {
        match self.view {
            View::Setup => self.patch_setup().await,
            View::Location => self.patch_loc().await,
            _ => unreachable!(),
        }
    }

    /// Patch changed fields from a Setup view
    async fn patch_setup(&self) -> Result<()> {
        cards_meth!(self, patch_setup_x)
    }

    /// Patch changed fields from a Setup view
    async fn patch_setup_x<C: Card>(&self) -> Result<()> {
        let pri = self.fetch_primary::<C>().await?;
        let changed = pri.changed_setup();
        if !changed.is_empty() {
            uri_one(C::res(), &self.name).patch(&changed.into()).await?;
        }
        Ok(())
    }

    /// Patch changed fields from a Location view
    async fn patch_loc(&self) -> Result<()> {
        match self.res {
            Res::Beacon => self.patch_loc_x::<Beacon>().await,
            Res::Camera => self.patch_loc_x::<Camera>().await,
            Res::Controller => self.patch_loc_x::<Controller>().await,
            Res::Dms => self.patch_loc_x::<Dms>().await,
            Res::GateArm => self.patch_loc_x::<GateArm>().await,
            Res::RampMeter => self.patch_loc_x::<RampMeter>().await,
            Res::TagReader => self.patch_loc_x::<TagReader>().await,
            Res::WeatherSensor => self.patch_loc_x::<WeatherSensor>().await,
            _ => unreachable!(),
        }
    }

    /// Patch changed fields from a Location view
    async fn patch_loc_x<C>(&self) -> Result<()>
    where
        C: Card + Loc,
    {
        let pri = self.fetch_primary::<C>().await?;
        if let Some(geoloc) = pri.geoloc() {
            let anc = fetch_ancillary(&pri, View::Location).await?;
            let changed = pri.changed_location(anc);
            if !changed.is_empty() {
                let mut uri = uri_one(Res::GeoLoc, geoloc);
                uri.query("res", self.res.as_str());
                uri.patch(&changed.into()).await?;
            }
        }
        Ok(())
    }

    /// Handle click event for a button owned by the resource
    pub async fn handle_click(&self, id: String) -> Result<()> {
        if self.view != View::Control && self.view != View::Request {
            return Ok(());
        }
        match self.res {
            Res::Beacon => self.handle_click_x::<Beacon>(id).await,
            Res::Camera => self.handle_click_x::<Camera>(id).await,
            Res::Dms => self.handle_click_x::<Dms>(id).await,
            Res::RampMeter => self.handle_click_x::<RampMeter>(id).await,
            _ => Ok(()),
        }
    }

    /// Handle click event for a button on a card
    async fn handle_click_x<C: Card>(&self, id: String) -> Result<()> {
        let pri = self.fetch_primary::<C>().await?;
        let anc = fetch_ancillary(&pri, self.view).await?;
        for action in pri.handle_click(anc, id) {
            action.perform().await?;
        }
        Ok(())
    }

    /// Handle input event for an element owned by the resource
    pub async fn handle_input(&self, id: String) -> Result<()> {
        match (self.res, self.view) {
            (Res::ActionPlan, View::Control) => {
                self.handle_input_x::<ActionPlan>(id).await
            }
            (Res::Dms, View::Control) => self.handle_input_x::<Dms>(id).await,
            (Res::Domain, View::Control) => {
                self.handle_input_x::<Domain>(id).await
            }
            (Res::Lcs, View::Control) => self.handle_input_x::<Lcs>(id).await,
            (Res::MsgPattern, View::Setup) => {
                self.handle_input_x::<MsgPattern>(id).await
            }
            (Res::RampMeter, View::Control) => {
                self.handle_input_x::<RampMeter>(id).await
            }
            _ => Ok(()),
        }
    }

    /// Handle input event for an element on a card
    async fn handle_input_x<C: Card>(&self, id: String) -> Result<()> {
        let pri = self.fetch_primary::<C>().await?;
        let anc = fetch_ancillary(&pri, self.view).await?;
        for action in pri.handle_input(anc, id) {
            action.perform().await?;
        }
        Ok(())
    }

    /// Create a new object
    pub async fn create_and_post(&self) -> Result<()> {
        let doc = Doc::get();
        let value = match self.res {
            Res::Permission => Permission::create_value(&doc)?,
            _ => create_value(&doc)?,
        };
        uri_all(self.res).post(&value.into()).await?;
        Ok(())
    }

    /// Delete a resource by name
    pub async fn delete_one(&self) -> Result<()> {
        uri_one(self.res, &self.name).delete().await
    }
}

/// Build a create card
fn html_card_create(res: Res, create: &str) -> String {
    let mut page = Page::new();
    let mut div = page.frag::<html::Div>();
    div.class("title row");
    div.span().cdata(res.symbol()).cdata(" 🆕").close();
    let mut select = div.select();
    select.id("ob_view");
    select
        .option()
        .value(View::CreateCompact.as_str())
        .cdata(View::Compact.as_str())
        .close();
    select
        .option()
        .selected()
        .cdata(View::Create.as_str())
        .close();
    div.close();
    div = page.frag::<html::Div>();
    div.raw(create);
    div.close();
    div = page.frag::<html::Div>();
    div.class("row end");
    div.button().id("ob_save").r#type("button").cdata("🖍️ Save");
    String::from(page)
}

/// Create a name value
fn create_value(doc: &Doc) -> Result<String> {
    let name = doc
        .input_option_string("create_name")
        .ok_or(Error::ElemIdNotFound("create_name"))?;
    let mut obj = Map::new();
    obj.insert("name".to_string(), Value::String(name));
    Ok(Value::Object(obj).to_string())
}
