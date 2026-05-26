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
use crate::card::{
    Card, Search, fetch_ancillary, footer_html, uri_all, uri_one,
};
use crate::commconfig::CommConfig;
use crate::commlink::CommLink;
use crate::controller::Controller;
use crate::detector::Detector;
use crate::dms::Dms;
use crate::domain::Domain;
use crate::eid;
use crate::error::Result;
use crate::eventcfg::EventConfig;
use crate::fetch::Action;
use crate::flowstream::FlowStream;
use crate::gatearm::GateArm;
use crate::gps::Gps;
use crate::incident::Incident;
use crate::lcs::Lcs;
use crate::lcsstate::LcsState;
use crate::mapextent::MapExtent;
use crate::monitorstyle::MonitorStyle;
use crate::msgpattern::MsgPattern;
use crate::planphase::PlanPhase;
use crate::rampmeter::RampMeter;
use crate::road::Road;
use crate::role::Role;
use crate::signconfig::SignConfig;
use crate::systemattr::SystemAttr;
use crate::tagreader::TagReader;
use crate::user::User;
use crate::util::Doc;
use crate::videomonitor::VideoMonitor;
use crate::weathersensor::WeatherSensor;
use crate::word::Word;
use hatmil::{Tree, html};
use resources::Res;
use serde_json::Value;
use serde_json::map::Map;

/// Card element view
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum View {
    /// Hidden view
    Hidden,
    /// Search event "view"
    SearchEv,
    /// Save event "view"
    SaveEv,
    /// Compact Create view
    CreateCompact,
    /// Create view
    Create,
    /// Compact view
    Compact,
    /// Control view
    Control,
    /// Status view
    Status,
    /// Request view
    Request,
    /// Setup view
    Setup(bool),
    /// Location view
    Location(bool),
}

impl View {
    /// Get view class name
    pub const fn class_name(self) -> &'static str {
        match self {
            View::Hidden | View::SearchEv | View::SaveEv => "no-display",
            View::CreateCompact | View::Compact => "card-compact",
            _ => "card-expanded",
        }
    }

    /// Is the view expanded?
    pub fn is_expanded(self) -> bool {
        match self {
            View::Create
            | View::Control
            | View::Status
            | View::Request
            | View::Setup(_)
            | View::Location(_) => true,
            _ => false,
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
            SearchEv => "Search",
            SaveEv => "🖍️ Save",
            CreateCompact => "Create compact",
            Create => "🆕 Create",
            Compact => "⌄ Compact",
            Control => "🕹️ Control",
            Status => "☑️ Status",
            Request => "🙏 Request",
            Setup(_edit) => "📝 Setup",
            Location(_edit) => "🗺️ Location",
        }
    }
}

impl TryFrom<&str> for View {
    type Error = ();

    fn try_from(type_n: &str) -> std::result::Result<Self, Self::Error> {
        use View::*;
        match type_n {
            v if v == Hidden.as_str() => Ok(Hidden),
            v if v == SearchEv.as_str() => Ok(SearchEv),
            v if v == SaveEv.as_str() => Ok(SaveEv),
            v if v == CreateCompact.as_str() => Ok(CreateCompact),
            v if v == Create.as_str() => Ok(Create),
            v if v == Compact.as_str() => Ok(Compact),
            v if v == Control.as_str() => Ok(Control),
            v if v == Status.as_str() => Ok(Status),
            v if v == Request.as_str() => Ok(Request),
            v if v == Setup(false).as_str() => Ok(Setup(false)),
            v if v == Location(false).as_str() => Ok(Location(false)),
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
    pub async fn fetch_one(&mut self, search: &str) -> Result<String> {
        let html = match self.view {
            View::CreateCompact => {
                let mut tree = Tree::new();
                let mut span = tree.root::<html::Span>();
                span.class("create").cdata("Create 🆕");
                String::from(tree)
            }
            View::Create => {
                let html = self.fetch_one_res(search).await?;
                html_card_create(self.res, &html)
            }
            _ => self.fetch_one_res(search).await?,
        };
        Ok(html)
    }

    /// Fetch a card view
    async fn fetch_one_res(&mut self, search: &str) -> Result<String> {
        cards_meth!(self, fetch_one_x, search)
    }

    /// Fetch a card view
    async fn fetch_one_x<C: Card>(&mut self, search: &str) -> Result<String> {
        let pri = if self.view == View::Create {
            C::default().with_name(&self.name)
        } else {
            self.fetch_primary::<C>().await?
        };
        let anc = fetch_ancillary(&pri, self.view).await?;
        if !search.is_empty() && !Search::new(search).is_match(&pri, &anc) {
            self.view = View::Hidden;
        }
        Ok(pri.to_html(self.view, &anc))
    }

    /// Fetch primary JSON resource
    async fn fetch_primary<C: Card>(&self) -> Result<C> {
        let uri = uri_one(C::res(), &self.name);
        let json = uri.get().await?;
        C::new(json)
    }

    /// Handle click event for a button owned by the resource
    pub async fn handle_click(&self, id: &str) -> Result<Option<View>> {
        cards_meth!(self, handle_click_x, id)
    }

    /// Handle click event for a button on a card
    async fn handle_click_x<C: Card>(&self, id: &str) -> Result<Option<View>> {
        if eid::CREATE == id {
            for action in self.handle_create(C::res()) {
                action.perform().await?;
            }
            return Ok(Some(self.view.compact()));
        }
        let view = match id {
            eid::SAVE => View::SaveEv,
            _ => self.view,
        };
        let pri = self.fetch_primary::<C>().await?;
        let anc = fetch_ancillary(&pri, view).await?;
        for action in pri.handle_click(anc, id) {
            action.perform().await?;
        }
        if let View::SaveEv | View::Location(_) = view {
            Ok(Some(self.view.compact()))
        } else {
            Ok(None)
        }
    }

    /// Handle click event for the create button
    fn handle_create(&self, res: Res) -> Vec<Action> {
        let doc = Doc::get();
        if let Some(name) = doc.input_option_string("create_name") {
            let mut obj = Map::new();
            obj.insert("name".to_string(), Value::String(name));
            let value = Value::Object(obj).to_string();
            let uri = uri_all(res);
            vec![Action::Post(uri, value.into())]
        } else {
            Vec::new()
        }
    }

    /// Handle mouse event for a card
    pub async fn handle_mouse(&self, id: &str, mouse_down: bool) -> Result<()> {
        #[allow(clippy::single_match)]
        match (self.res, self.view) {
            (Res::Camera, View::Control) => {
                let pri = self.fetch_primary::<Camera>().await?;
                let anc = fetch_ancillary(&pri, self.view).await?;
                for action in pri.handle_mouse(anc, id, mouse_down) {
                    action.perform().await?;
                }
            }
            _ => (),
        }
        Ok(())
    }

    /// Handle input event for an element owned by the resource
    pub async fn handle_input(&self, id: &str) -> Result<()> {
        match (self.res, self.view) {
            (Res::ActionPlan, View::Control) => {
                self.handle_input_x::<ActionPlan>(id).await
            }
            (Res::Dms, View::Control) => self.handle_input_x::<Dms>(id).await,
            (Res::Domain, View::Control) => {
                self.handle_input_x::<Domain>(id).await
            }
            (Res::Lcs, View::Control) => self.handle_input_x::<Lcs>(id).await,
            (Res::MsgPattern, View::Setup(true)) => {
                self.handle_input_x::<MsgPattern>(id).await
            }
            (Res::RampMeter, View::Control) => {
                self.handle_input_x::<RampMeter>(id).await
            }
            _ => Ok(()),
        }
    }

    /// Handle input event for an element on a card
    async fn handle_input_x<C: Card>(&self, id: &str) -> Result<()> {
        let pri = self.fetch_primary::<C>().await?;
        let anc = fetch_ancillary(&pri, self.view).await?;
        for action in pri.handle_input(anc, id) {
            action.perform().await?;
        }
        Ok(())
    }

    /// Handle updating a card in response to an SSE notification
    pub async fn handle_update(&self) -> Result<()> {
        if self.view != View::Control {
            return Ok(());
        }
        match self.res {
            Res::Dms => self.handle_update_x::<Dms>().await,
            _ => Ok(()),
        }
    }

    /// Handle updating a card in response to an SSE notification
    async fn handle_update_x<C: Card>(&self) -> Result<()> {
        let pri = self.fetch_primary::<C>().await?;
        let anc = fetch_ancillary(&pri, self.view).await?;
        pri.handle_update(anc);
        Ok(())
    }

    /// Delete a resource by name
    pub async fn handle_delete(&self) -> Result<()> {
        // TODO: add Card::handle_delete?
        let uri = uri_one(self.res, &self.name);
        let actions = vec![Action::Delete(uri)];
        for action in actions {
            action.perform().await?;
        }
        Ok(())
    }
}

/// Build a create card
fn html_card_create(res: Res, create: &str) -> String {
    let mut tree = Tree::new();
    let mut div = tree.root::<html::Div>();
    div.class("title row");
    div.span().cdata(res.symbol()).cdata(" 🆕").close();
    let mut select = div.select();
    select.id(eid::VIEW);
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
    div = tree.root::<html::Div>();
    div.raw(create);
    div.close();
    footer_html(View::Create, false, &mut tree.root::<html::Div>());
    String::from(tree)
}
