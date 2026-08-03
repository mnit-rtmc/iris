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
use crate::app::{self, DeferredAction};
use crate::asset::Asset;
use crate::card::{self, CardList, CardState};
use crate::click;
use crate::eid;
use crate::error::Result;
use crate::fetch::Uri;
use crate::helper::spawn_future;
use crate::item::ItemState;
use crate::sidebar;
use crate::sse;
use crate::util::Doc;
use chrono::{DateTime, Local};
use earthwyrm::{MapPane, Target};
use hatmil::css::{Prop, Rule, Sel};
use hatmil::{Tree, html};
use resources::Res;
use serde::Deserialize;
use std::collections::HashMap;
use std::time::Duration;
use wasm_bindgen::JsCast;
use wasm_bindgen::closure::Closure;
use web_sys::{Element, Event, HtmlElement};

/// Map pane ID
const MAP_PANE: &str = "map-pane";

/// Layer groups
const GROUPS: &[&str] = &["tile", "tms"];

/// Anchor X position
const ANCHOR_X: f64 = 0.32;

/// Anchor Y position
const ANCHOR_Y: f64 = 0.5;

/// Binned station data
#[derive(Deserialize)]
struct StationData {
    /// Data collection time
    time_stamp: String,
    /// Binning period (s)
    #[allow(unused)]
    period: u32,
    /// Data samples
    samples: HashMap<String, [Option<u32>; 2]>,
}

/// Add event listeners
pub fn add_listeners() -> Result<()> {
    let doc = Doc::new()?;
    let layer_menu: HtmlElement = doc.elem("layer-menu")?;
    add_change_listener(&layer_menu)?;
    MapPane::new(MAP_PANE)
        .with_anchor(ANCHOR_X, ANCHOR_Y)
        .with_groups(GROUPS)
        .with_zoom_handler(handle_zoom)
        .with_click_handler(handle_click)
        .with_contextmenu_handler(handle_contextmenu)
        .register();
    if let Some(map_pane) = MapPane::get(MAP_PANE) {
        set_selected_style(None);
        map_pane.set_position(10, -93.2, 44.95);
        set_zoom_level(10);
    }
    fetch_station_data();
    Ok(())
}

/// Handle a `click` event on the map
fn handle_click(target: Target) {
    log::debug!("click: {target:?}");
    if let Some((rname, nm)) = target.cls.split_once('-') {
        let res = Res::try_from(rname).ok();
        spawn_future(select_card_map(res, nm.to_string()));
    }
}

/// Select a card from a map marker click
async fn select_card_map(res: Option<Res>, name: String) -> Result<()> {
    let clear = name.is_empty()
        || match (res, &name) {
            (Some(res), name) => app::is_selected_item(res, name),
            (None, _name) => true,
        };
    if clear {
        set_selected_style(None);
        if let Some(cv) = app::expanded_view() {
            let search = sidebar::search_value()?;
            sidebar::replace_card(cv.compact(), &search).await?;
        }
        return Ok(());
    }
    let changed = res != sidebar::selected_resource();
    if let Some(res) = res {
        if changed {
            sidebar::set_resource(Some(res), "").await?;
        }
        set_selected_style(Some((res, &name)));
        let id = format!("{res}_{name}");
        click::click_card(res, name, id).await?;
    }
    if changed {
        sse::post_req(res).await
    } else {
        Ok(())
    }
}

/// Select item on map
pub fn select_item(res: Res, name: &str, lon: f64, lat: f64) {
    if !app::is_selected_item(res, name) {
        set_selected_style(Some((res, name)));
        let zoom = selected_zoom(res).max(12);
        spawn_future(do_select_item(zoom, lon, lat));
    }
}

/// Select item on map
async fn do_select_item(zoom: u32, lon: f64, lat: f64) -> Result<()> {
    if let Some(map_pane) = MapPane::get(MAP_PANE) {
        map_pane.set_position(zoom, lon, lat);
        set_zoom_level(zoom);
        update_states_all(zoom).await?;
        update_osm_style(zoom).await?;
    }
    Ok(())
}

/// Get zoom level for selected resource
fn selected_zoom(res: Res) -> u32 {
    let layer = format!("layer-{res}");
    Doc::get().input_parse::<u32>(&layer).unwrap_or(32)
}

/// Set selected style (CSS)
pub fn set_selected_style(res_name: Option<(Res, &str)>) {
    if let Some(el) = Doc::get().opt_elem::<Element>("selected-style") {
        match res_name {
            Some((res, name)) => {
                // FIXME: maybe this shouldn't be a side-effect here
                app::set_selected_item(res, name);
                let sel = Sel::cls(format!("{}-{name}", res.as_str()));
                let prop = Prop::new().stroke("white").stroke_width(2);
                let css = Rule::new(sel, prop).to_string();
                el.set_inner_html(&css);
            }
            None => {
                app::clear_selected_item();
                el.set_inner_html("");
            }
        }
    }
}

/// Set the map zoom level
fn set_zoom_level(zoom: u32) {
    if let Some(el) = Doc::get().opt_elem::<Element>("marker-style") {
        let sel = Sel::cls("wyrm-tile").descendant(Sel::tp("use"));
        let prop = Prop::new().scale(zoom_scale(zoom));
        let css = Rule::new(sel, prop).to_string();
        el.set_inner_html(&css);
    }
    if let Some(el) = Doc::get().opt_elem::<Element>("zoom-level") {
        el.set_inner_html(&zoom.to_string());
    }
}

/// Get marker scale for a zoom level
fn zoom_scale(zoom: u32) -> &'static str {
    match zoom {
        1 => "0.003",
        2 => "0.006",
        3 => "0.012",
        4 => "0.025",
        5 => "0.05",
        6 => "0.1",
        7 => "0.2",
        8 => "0.3",
        9 => "0.4",
        10 => "0.5",
        11 => "0.6",
        12 => "0.8",
        _ => "1.0",
    }
}

/// Add a "change" event listener to an element
fn add_change_listener(el: &Element) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        if let Some(Ok(target)) = e.target().map(|e| e.dyn_into::<Element>()) {
            let id = target.id();
            spawn_future(handle_layer_zoom(id));
        }
    });
    el.add_event_listener_with_callback(
        "change",
        closure.as_ref().unchecked_ref(),
    )?;
    // can't drop closure, just forget it to make JS happy
    closure.forget();
    Ok(())
}

/// Handle layer zoom threshold change
async fn handle_layer_zoom(id: String) -> Result<()> {
    if let Some((layer, rname)) = id.split_once('-')
        && layer == "layer"
        && let Ok(res) = Res::try_from(rname)
    {
        // FIXME: only call when crossing zoom threshold
        update_states(res, None).await?;
    }
    Ok(())
}

/// Get current map zoom level
fn current_zoom() -> u32 {
    MapPane::get(MAP_PANE).map(|mp| mp.zoom()).unwrap_or(0)
}

/// Fetch binned station data
pub fn fetch_station_data() {
    log::debug!("fetch_station_data");
    app::defer_action(DeferredAction::FetchStationData, 30_000);
    spawn_future(do_fetch_station_data());
}

/// Actually fetch binned station data
async fn do_fetch_station_data() -> Result<()> {
    if let Some(el) = Doc::new()?.opt_elem::<Element>("segment-style") {
        let data = StationData::fetch().await?;
        let css = data.make_style();
        el.set_inner_html(&css);
    }
    Ok(())
}

impl StationData {
    /// Fetch current station data
    async fn fetch() -> Result<Self> {
        let stat = Uri::from("/iris/station_sample").get().await?;
        Ok(serde_wasm_bindgen::from_value(stat)?)
    }

    /// Make station segment style
    fn make_style(&self) -> String {
        let now: DateTime<Local> = Local::now();
        let oldest = now - Duration::from_secs(300);
        match DateTime::parse_from_rfc3339(&self.time_stamp) {
            Ok(dt) if dt > oldest && dt < now => self.do_make_style(),
            _ => {
                log::warn!("bad station_sample timestamp: {}", self.time_stamp);
                String::new()
            }
        }
    }

    /// Make station segment style
    fn do_make_style(&self) -> String {
        let len = self.samples.len();
        let mut style = String::with_capacity(32 * (len + 1));
        style.push_str(".wyrm-segment { fill: #aaa; }\n");
        for (sid, data) in &self.samples {
            let flow = data.first();
            let speed = data.get(1);
            if let (Some(Some(fl)), Some(Some(sp))) = (flow, speed) {
                let density = ((*fl as f32) / (*sp as f32)).round() as u32;
                style.push_str(".segment-");
                style.push_str(sid);
                style.push_str(" { fill: ");
                style.push_str(density_color(density));
                style.push_str("; }\n");
            }
        }
        style
    }
}

/// Get color based on density (veh/mi)
fn density_color(density: u32) -> &'static str {
    match density {
        0 => "#aaa",
        1..30 => "#2c2",
        30..50 => "#fc0",
        50..200 => "#d00",
        200.. => "#c0f",
    }
}

/// Handle map zoom
fn handle_zoom(zoom: u32) {
    spawn_future(do_handle_zoom(zoom));
}

/// Handle map zoom
async fn do_handle_zoom(zoom: u32) -> Result<()> {
    set_zoom_level(zoom);
    update_states_all(zoom).await?;
    update_osm_style(zoom).await?;
    Ok(())
}

/// Update map item states
async fn update_states_zoom(
    res: Res,
    cards: Option<&CardList>,
    zoom: u32,
) -> Result<()> {
    // NOTE: resource must have locations
    let doc = Doc::new()?;
    if let Some(el) = doc.opt_elem::<Element>(&format!("{res}-style")) {
        let displayed = is_layer_displayed(res, zoom);
        let css = if displayed {
            let states_all = card::item_states_all(res);
            let items = match cards {
                Some(cards) => cards.states_main().await?,
                None => {
                    let access = Asset::Access.uri().get_val().await?;
                    let mut cards = CardList::new(res, access);
                    cards.fetch_all().await?;
                    cards.states_main().await?
                }
            };
            item_states_css(states_all, &items)
        } else {
            let sel = Sel::cls(format!("wyrm-{res}"));
            let prop = Prop::new().display("none");
            Rule::new(sel, prop).to_string()
        };
        el.set_inner_html(&css);
    }
    if let Some(el) = doc.opt_elem::<Element>(&format!("layer-{res}")) {
        let mut prop = Prop::new();
        if zoom < selected_zoom(res) {
            prop = prop.background_color("#aaa");
        }
        el.set_attribute("style", &String::from(prop))?;
    }
    Ok(())
}

/// Update item states for all map layers
async fn update_states_all(zoom: u32) -> Result<()> {
    // FIXME: only call these when crossing zoom threshold
    update_states_zoom(Res::Incident, None, zoom).await?;
    update_states_zoom(Res::Dms, None, zoom).await?;
    update_states_zoom(Res::Lcs, None, zoom).await?;
    update_states_zoom(Res::Camera, None, zoom).await?;
    update_states_zoom(Res::RampMeter, None, zoom).await?;
    update_states_zoom(Res::Beacon, None, zoom).await?;
    update_states_zoom(Res::WeatherSensor, None, zoom).await?;
    update_states_zoom(Res::TagReader, None, zoom).await?;
    update_states_zoom(Res::Controller, None, zoom).await?;
    Ok(())
}

/// Update map item states with a list of cards
pub async fn update_states(res: Res, cards: Option<&CardList>) -> Result<()> {
    let zoom = current_zoom();
    update_states_zoom(res, cards, zoom).await
}

/// Check if a resource layer is displayed
fn is_layer_displayed(res: Res, zoom: u32) -> bool {
    (sidebar::selected_resource() == Some(res)) || zoom >= selected_zoom(res)
}

/// Build resource item states style
fn item_states_css(
    states_all: &'static [ItemState],
    card_states: &[CardState],
) -> String {
    let mut css = String::with_capacity(32 * card_states.len());
    for st in states_all {
        let mut sel: Option<Sel> = None;
        for cs in card_states {
            if cs.state == *st {
                let s = Sel::cls(format!("{}-{}", cs.res.as_str(), cs.name));
                sel = Some(match sel {
                    Some(sel) => sel.list(s),
                    None => s,
                });
            }
        }
        if let Some(sel) = sel {
            let prop = Prop::new().fill(st.fill_css());
            css.push_str(&Rule::new(sel, prop).to_string());
        }
    }
    css
}

/// Update map OSM style
async fn update_osm_style(zoom: u32) -> Result<()> {
    let doc = Doc::new()?;
    let displayed = zoom >= doc.input_parse::<u32>("layer-osm").unwrap_or(32);
    let css = if displayed {
        ""
    } else {
        ".wyrm-county,.wyrm-city,.wyrm-lake,.wyrm-river,.wyrm-pond,\
         .wyrm-wetland,.wyrm-motorway,.wyrm-trunk,.wyrm-primary,\
         .wyrm-secondary { display: none; }"
    };
    doc.elem::<Element>("osm-style")?.set_inner_html(css);
    let mut prop = Prop::new();
    if !displayed {
        prop = prop.background_color("#aaa");
    }
    doc.elem::<Element>("layer-osm")?
        .set_attribute("style", &String::from(prop))?;
    Ok(())
}

/// Get title for map context menu
fn menu_title(target: &Target) -> Option<String> {
    if !target.name.is_empty() {
        let mut title = target.name.to_string();
        if !target.osm_ref.is_empty() {
            title.push_str(&format!(" ({})", target.osm_ref));
        }
        Some(title)
    } else if let Some((rname, nm)) = target.cls.split_once('-')
        && let Ok(res) = Res::try_from(rname)
    {
        Some(format!("{}: {nm}", res.as_str()))
    } else {
        None
    }
}

/// Handle a `contextmenu` event
fn handle_contextmenu(target: Target, x: i32, y: i32) {
    log::info!("contextmenu: {target:?} {x} {y}");
    app::clear_selected_item();
    if let Some(el) = Doc::get().opt_elem::<Element>("selected-style") {
        let prop = Prop::new().fill("#96a");
        let sel = Sel::cls(&target.cls);
        let css = Rule::new(sel, prop).to_string();
        el.set_inner_html(&css);
    }
    if let Some(el) = Doc::get().opt_elem::<HtmlElement>(eid::MAP_MENU) {
        let title = menu_title(&target);
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.id(eid::MAP_MENU);
        if let Some(title) = title {
            div.style(Prop::new().left(format!("{x}px")).top(format!("{y}px")));
            let mut menu = div.menu();
            menu.style(Prop::new().left("0px").bottom("0px"));
            menu.li().cdata(&title);
        } else {
            div.class("no-display");
        }
        el.set_outer_html(&String::from(tree));
    }
}

/// Dismiss the map context menu
pub fn dismiss_context_menu() {
    if let Some(el) = Doc::get().opt_elem::<HtmlElement>(eid::MAP_MENU) {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.id(eid::MAP_MENU).class("no-display");
        el.set_outer_html(&String::from(tree));
    }
}
