// Copyright (C) 2026  Minnesota Department of Transportation
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
use crate::eid;
use crate::error::{Error, Result};
use crate::helper::spawn_future;
use crate::util::Doc;
use crate::view::make_menu_res;
use earthwyrm::{MapEvent, MapPane};
use hatmil::css::{Prop, Rule, Sel};
use hatmil::{Tree, html};
use resources::Res;
use wasm_bindgen::JsCast;
use wasm_bindgen::closure::Closure;
use web_sys::{Element, Event, HtmlElement};

/// OpenStreetMap domain URL
const OSM: &str = "https://www.openstreetmap.org/";

/// Menu action
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum MenuAction {
    /// Blank a sign
    Blank,
    /// Publish a camera
    Publish,
    /// Unpublish a camera
    Unpublish,
    /// Add a camera to scratch playlist
    PlaylistAdd,
    /// Remove a camera from scratch playlist
    PlaylistRemove,
}

impl MenuAction {
    /// Get string slice
    fn as_str(self) -> &'static str {
        match self {
            MenuAction::Blank => "Blank",
            MenuAction::Publish => "Publish",
            MenuAction::Unpublish => "Unpublish",
            MenuAction::PlaylistAdd => "Add to playlist",
            MenuAction::PlaylistRemove => "Remove from playlist",
        }
    }
}

impl TryFrom<&Element> for MenuAction {
    type Error = Error;

    fn try_from(el: &Element) -> Result<Self> {
        let action = el
            .get_attribute("data-action")
            .ok_or(Error::MissingAttribute())?;
        use MenuAction::*;
        if action == Blank.as_str() {
            Ok(Blank)
        } else if action == Publish.as_str() {
            Ok(Publish)
        } else if action == Unpublish.as_str() {
            Ok(Unpublish)
        } else if action == PlaylistAdd.as_str() {
            Ok(PlaylistAdd)
        } else if action == PlaylistRemove.as_str() {
            Ok(PlaylistRemove)
        } else {
            Err(Error::MissingAttribute())
        }
    }
}

/// Add menu event listeners
pub fn add_listeners(doc: &Doc) -> Result<()> {
    add_click_listener(doc)
}

/// Handle a `contextmenu` event
pub async fn handle_contextmenu(me: MapEvent, x: i32, y: i32) -> Result<()> {
    set_context_style(Some(&me));
    if let Some(el) = Doc::get().opt_elem::<HtmlElement>(eid::MAP_MENU) {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.id(eid::MAP_MENU);
        div.popover("auto");
        div.style(div_style(x, y));
        if let Some(title) = make_title(&me) {
            let mut menu = div.menu();
            menu.style(menu_style(x, y));
            menu.li().cdata_len(&title, 24);
            if is_osm_layer(&me) {
                let map_pane = MapPane::get(eid::MAP_PANE)
                    .ok_or(Error::MissingElement())?;
                if let Some(lonlat) = map_pane.lonlat(x, y) {
                    make_osm_link(&mut menu.li(), lonlat);
                }
            } else if let Some((res, nm)) = layer_res(&me) {
                for item in make_menu_res(res, nm).await? {
                    let mut li = menu.li();
                    let mut button = li.button();
                    button
                        .class("menu-action")
                        .data_("action", item.as_str())
                        .data_("res", res.as_str())
                        .data_("name", nm)
                        .cdata(item.as_str());
                    li.close();
                }
            }
        }
        el.set_outer_html(&String::from(tree));
    }
    // NOTE: we must retrieve the element again, or show_popover won't work!
    if let Some(el) = Doc::get().opt_elem::<HtmlElement>(eid::MAP_MENU) {
        el.show_popover()?;
    }
    Ok(())
}

/// Make link to OpenStreetMap
fn make_osm_link<'p>(li: &'p mut html::Li<'p>, lonlat: (f64, f64)) {
    li.a()
        .href(format!("{OSM}?mlat={}&mlon={}", lonlat.1, lonlat.0))
        .target("_blank")
        .rel("noopener noreferrer")
        .cdata("OpenStreetMap");
    li.close();
}

/// Set context style for a layer
pub fn set_context_style(me: Option<&MapEvent>) {
    if let Some(el) = Doc::get().opt_elem::<Element>("context-style") {
        match me {
            Some(me) if is_osm_layer(me) => {
                let prop = match me.layer.as_str() {
                    "motorway" | "trunk" | "primary" | "secondary"
                    | "tertiary" | "road" | "railway" | "path" => {
                        Prop::new().stroke("#96b")
                    }
                    _ => Prop::new().fill("#96b"),
                };
                let sel = Sel::cls(&me.target);
                let css = Rule::new(sel, prop).to_string();
                el.set_inner_html(&css);
            }
            Some(me) => {
                let prop = Prop::new().stroke("white").stroke_width(2);
                let sel = Sel::cls(&me.target);
                let css = Rule::new(sel, prop).to_string();
                el.set_inner_html(&css);
            }
            _ => el.set_inner_html(""),
        }
    }
}

/// Make title for map context menu
fn make_title(me: &MapEvent) -> Option<String> {
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

/// Make div style
fn div_style(x: i32, y: i32) -> Prop {
    let mut prop = Prop::new();
    if x > 200 {
        prop = prop.left(format!("{}px", x + 2));
    } else {
        prop = prop.left(format!("{}px", x - 2));
    }
    if y > 200 {
        prop = prop.top(format!("{}px", y + 2));
    } else {
        prop = prop.top(format!("{}px", y - 2));
    }
    prop
}

/// Make menu style
fn menu_style(x: i32, y: i32) -> Prop {
    let mut prop = Prop::new();
    if x > 200 {
        prop = prop.right("anchor(left)");
    } else {
        prop = prop.left("anchor(left)");
    }
    if y > 200 {
        prop = prop.bottom("anchor(top)");
    } else {
        prop = prop.top("anchor(top)");
    }
    prop
}

/// Check if event is on an OSM layer
fn is_osm_layer(me: &MapEvent) -> bool {
    me.data_name.is_some()
}

/// Get layer resource type
fn layer_res(me: &MapEvent) -> Option<(Res, &str)> {
    me.target.split_once('-').and_then(|(rname, nm)| {
        match Res::try_from(rname) {
            Ok(res) => Some((res, nm)),
            Err(_e) => None,
        }
    })
}

/// Add a "click" event listener to document
fn add_click_listener(doc: &Doc) -> Result<()> {
    let closure: Closure<dyn Fn(_)> = Closure::new(|e: Event| {
        // NOTE: target.class_name() will throw an error in some cases
        if let Some(Ok(target)) = e.target().map(|e| e.dyn_into::<Element>())
            && target.class_list().contains("menu-action")
        {
            spawn_future(handle_menu_action(target));
        }
    });
    doc.0.add_event_listener_with_callback(
        "click",
        closure.as_ref().unchecked_ref(),
    )?;
    // can't drop closure, just forget it to make JS happy
    closure.forget();
    Ok(())
}

/// Handle menu action click
async fn handle_menu_action(el: Element) -> Result<()> {
    let action = MenuAction::try_from(&el)?;
    let res = el
        .get_attribute("data-res")
        .ok_or(Error::MissingAttribute())?;
    let nm = el
        .get_attribute("data-name")
        .ok_or(Error::MissingAttribute())?;
    log::info!("action: {action:?}, res: {res}, name: {nm}");
    // FIXME: perform action
    if let Some(el) = Doc::get().opt_elem::<HtmlElement>(eid::MAP_MENU) {
        el.hide_popover()?;
    }
    Ok(())
}
