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
use crate::permission::Permission;
use crate::query::QueryState;
use crate::sidebar;
use crate::util::Doc;
use chrono::{DateTime, Local};
use earthwyrm::{MapEvent, MapPane};
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
        map_pane.set_position(10, -93.2, 44.95);
        set_zoom_level(10);
        spawn_future(update_layers_all(10));
    }
    fetch_station_data();
    Ok(())
}

/// Handle a `click` event on the map
fn handle_click(me: MapEvent) {
    log::debug!("click: {me:?}");
    if let Some((rname, nm)) = me.target.split_once('-') {
        let res = Res::try_from(rname).ok();
        let query = QueryState::new().with_res(res).with_sel(nm);
        spawn_future(select_card_map(query));
    }
}

/// Select a card from a map marker click
async fn select_card_map(query: QueryState) -> Result<()> {
    if should_clear(&query) {
        set_selected_style(QueryState::new());
        if let Some(cv) = app::expanded_view() {
            let search = sidebar::search_value()?;
            sidebar::replace_card(cv.compact(), &search).await?;
        }
        return Ok(());
    }
    if let Some(res) = query.res() {
        if sidebar::selected_resource() != Some(res) {
            sidebar::set_query(query.clone()).await?;
        }
        set_selected_style(query.clone());
        let id = format!("{res}_{}", query.sel());
        click::click_card(id, query).await?;
    }
    Ok(())
}

/// Check if style should be cleared
fn should_clear(query: &QueryState) -> bool {
    let sel = query.sel();
    sel.is_empty() || query.res().is_none_or(|r| app::is_selected_item(r, sel))
}

/// Select item on map
pub fn select_item(res: Res, name: &str, lon: f64, lat: f64) {
    if !app::is_selected_item(res, name) {
        let query = QueryState::new().with_res(Some(res)).with_sel(name);
        set_selected_style(query);
        let zoom = selected_zoom(res).max(12);
        spawn_future(do_select_item(zoom, lon, lat));
    }
}

/// Get zoom level for selected resource
fn selected_zoom(res: Res) -> u32 {
    let id = format!("{res}-zoom");
    Doc::get().input_parse::<u32>(&id).unwrap_or(32)
}

/// Select item on map
async fn do_select_item(zoom: u32, lon: f64, lat: f64) -> Result<()> {
    if let Some(map_pane) = MapPane::get(MAP_PANE) {
        map_pane.set_position(zoom, lon, lat);
        set_zoom_level(zoom);
        update_layers_all(zoom).await?;
    }
    Ok(())
}

/// Set selected style (CSS)
pub fn set_selected_style(query: QueryState) {
    if let Some(el) = Doc::get().opt_elem::<Element>("selected-style") {
        match query.res_sel() {
            Some((res, name)) => {
                app::set_selected_item(res, name);
                let sel = Sel::cls(format!("{res}-{name}"));
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
    if let Some(el) = Doc::get().opt_elem::<Element>("current-zoom") {
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
    if let Some((rname, "zoom")) = id.split_once('-')
        && let Ok(res) = Res::try_from(rname)
    {
        let access: Vec<_> = Asset::Access.uri().get_val().await?;
        // FIXME: only call when crossing zoom threshold
        update_layer(res, &access).await?;
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
        let mut css = String::with_capacity(32 * (len + 1));
        let sel = Sel::cls("wyrm-segment");
        let prop = Prop::new().fill("#aaa");
        css.push_str(&Rule::new(sel, prop).to_string());
        for (sid, data) in &self.samples {
            let flow = data.first();
            let speed = data.get(1);
            if let (Some(Some(fl)), Some(Some(sp))) = (flow, speed) {
                let density = ((*fl as f32) / (*sp as f32)).round() as u32;
                css.push('\n');
                let sel = Sel::cls(format!("segment-{sid}"));
                let prop = Prop::new().fill(density_color(density));
                css.push_str(&Rule::new(sel, prop).to_string());
            }
        }
        css
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
    dismiss_context_menu();
    set_zoom_level(zoom);
    update_layers_all(zoom).await?;
    Ok(())
}

/// Update all map layer styles
async fn update_layers_all(zoom: u32) -> Result<()> {
    // FIXME: only call these when crossing zoom threshold
    let access: Vec<_> = Asset::Access.uri().get_val().await?;
    update_layer_style(Res::Incident, &access, zoom).await?;
    update_layer_style(Res::Dms, &access, zoom).await?;
    update_layer_style(Res::Lcs, &access, zoom).await?;
    update_layer_style(Res::Camera, &access, zoom).await?;
    update_layer_style(Res::RampMeter, &access, zoom).await?;
    update_layer_style(Res::GateArm, &access, zoom).await?;
    update_layer_style(Res::Beacon, &access, zoom).await?;
    update_layer_style(Res::WeatherSensor, &access, zoom).await?;
    update_layer_style(Res::TagReader, &access, zoom).await?;
    update_layer_style(Res::Controller, &access, zoom).await?;
    update_osm_style(zoom).await?;
    Ok(())
}

/// Update styles for one map layer
async fn update_layer_style(
    res: Res,
    access: &[Permission],
    zoom: u32,
) -> Result<()> {
    let doc = Doc::new()?;
    if let Some(el) = doc.opt_elem::<Element>(&format!("{res}-style")) {
        let css = layer_style_css(res, access, zoom).await?;
        el.set_inner_html(&css);
    }
    let permitted = Permission::is_view_permitted(access, res);
    if permitted
        && let Some(el) = doc.opt_elem::<Element>(&format!("{res}-zoom"))
    {
        let mut prop = Prop::new();
        if zoom < selected_zoom(res) {
            prop = prop.background_color("#aaa");
        }
        el.set_attribute("style", &String::from(prop))?;
    }
    Ok(())
}

/// Build layer style CSS for a resource type
async fn layer_style_css(
    res: Res,
    access: &[Permission],
    zoom: u32,
) -> Result<String> {
    let permitted = Permission::is_view_permitted(access, res);
    let displayed =
        sidebar::selected_resource() == Some(res) || zoom >= selected_zoom(res);
    if permitted && displayed {
        let mut cards = CardList::new(res, access);
        cards.fetch_all().await?;
        let states = cards.states_main().await?;
        Ok(res_states_css(res, &states))
    } else {
        let mut sel = Sel::cls(format!("wyrm-{res}"));
        if !permitted {
            sel = sel.list(Sel::cls(format!("menu-{res}")));
        }
        let prop = Prop::new().display("none");
        Ok(Rule::new(sel, prop).to_string())
    }
}

/// Build resource style CSS from card item states
fn res_states_css(res: Res, card_states: &[CardState]) -> String {
    let states_all = card::item_states_all(res);
    let mut css = String::with_capacity(32 * card_states.len());
    for st in states_all {
        let mut sel: Option<Sel> = None;
        for cs in card_states {
            if cs.state == *st {
                let s = Sel::cls(format!("{res}-{}", cs.name));
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
    let displayed = zoom >= doc.input_parse::<u32>("osm-zoom").unwrap_or(32);
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
    doc.elem::<Element>("osm-zoom")?
        .set_attribute("style", &String::from(prop))?;
    Ok(())
}

/// Update layer for a resource type
pub async fn update_layer(res: Res, access: &[Permission]) -> Result<()> {
    if Res::Incident == res || res.has_location() {
        let zoom = current_zoom();
        update_layer_style(res, access, zoom).await
    } else {
        Ok(())
    }
}

/// Get title for map context menu
fn menu_title(me: &MapEvent) -> Option<String> {
    if let Some(data_name) = &me.data_name {
        let mut title = data_name.to_string();
        if let Some(data_ref) = &me.data_ref {
            title.push_str(&format!(" ({})", data_ref));
        }
        Some(title)
    } else if let Some((rname, nm)) = me.target.split_once('-')
        && let Ok(res) = Res::try_from(rname)
    {
        let mut title = format!("{} {nm}", res.symbol());
        if let Some(data_ref) = &me.data_ref {
            title.push_str(&format!(" ({})", data_ref));
        }
        Some(title)
    } else {
        None
    }
}

/// Handle a `contextmenu` event
fn handle_contextmenu(me: MapEvent, x: i32, y: i32) {
    log::debug!("contextmenu: {me:?} {x} {y}");
    spawn_future(do_handle_contextmenu(me, x, y));
}

/// Handle a `contextmenu` event
async fn do_handle_contextmenu(me: MapEvent, x: i32, y: i32) -> Result<()> {
    if let Some((rname, nm)) = me.target.split_once('-')
        && let Ok(res) = Res::try_from(rname)
    {
        let query = QueryState::new().with_res(Some(res)).with_sel(nm);
        select_card_map(query).await?;
    } else if let Some(el) = Doc::get().opt_elem::<Element>("selected-style") {
        select_card_map(QueryState::new()).await?;
        let prop = match me.layer.as_str() {
            "motorway" | "trunk" | "primary" | "secondary" | "tertiary"
            | "road" | "railway" | "path" => Prop::new().stroke("#96b"),
            _ => Prop::new().fill("#96b"),
        };
        let sel = Sel::cls(&me.target);
        let css = Rule::new(sel, prop).to_string();
        el.set_inner_html(&css);
    }
    if let Some(el) = Doc::get().opt_elem::<HtmlElement>(eid::MAP_MENU) {
        let title = menu_title(&me);
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.id(eid::MAP_MENU);
        if let Some(title) = title {
            div.style(Prop::new().left(format!("{x}px")).top(format!("{y}px")));
            let mut menu = div.menu();
            let mut prop = Prop::new();
            if x < 200 {
                prop = prop.left("0px");
            } else {
                prop = prop.right("0px");
            }
            if y < 200 {
                prop = prop.top("0px");
            } else {
                prop = prop.bottom("0px");
            }
            menu.style(prop);
            menu.li().cdata(&title);
        } else {
            div.class("no-display");
        }
        el.set_outer_html(&String::from(tree));
    }
    Ok(())
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
