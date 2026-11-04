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
use crate::attr::Attr;
use crate::card::{Card, uri_one};
use crate::eid;
use crate::error::{Error, Result};
use crate::fetch::Action;
use crate::helper::spawn_future;
use crate::playlist::PlayList;
use crate::util::Doc;
use crate::view::make_menu_res;
use earthwyrm::{MapEvent, MapPane};
use hatmil::css::{Prop, Rule, Sel};
use hatmil::{Tree, html};
use resources::Res;
use std::fmt;
use wasm_bindgen::closure::Closure;
use wasm_bindgen::{JsCast, JsValue};
use web_sys::{Element, Event, HtmlElement};

/// OpenStreetMap domain URL
const OSM: &str = "https://www.openstreetmap.org/";

/// Menu action
#[derive(Clone, PartialEq, Eq)]
pub enum MenuAction {
    /// Link to URL
    Link(String, String),
    /// Blank a sign
    Blank(String),
    /// Publish a camera
    Publish(String),
    /// Unpublish a camera
    Unpublish(String),
    /// Add to a scratch list (camera, playlist)
    ScratchAdd(String, String),
    /// Remove from a scratch list (camera, playlist)
    ScratchRemove(String, String),
}

impl MenuAction {
    /// Get as string slice
    fn as_str(&self) -> &'static str {
        use MenuAction::*;
        match self {
            Link(_, _) => "Link",
            Blank(_) => "Blank",
            Publish(_) => "Publish",
            Unpublish(_) => "Unpublish",
            ScratchAdd(_, _) => "ScratchAdd",
            ScratchRemove(_, _) => "ScratchRemove",
        }
    }

    /// Get resource name
    fn name(&self) -> &str {
        use MenuAction::*;
        match self {
            Link(nm, _)
            | Blank(nm)
            | Publish(nm)
            | Unpublish(nm)
            | ScratchAdd(nm, _)
            | ScratchRemove(nm, _) => nm,
        }
    }

    /// Get extra parameter (if any)
    fn extra(&self) -> Option<&str> {
        use MenuAction::*;
        match self {
            Link(_, ex) | ScratchAdd(_, ex) | ScratchRemove(_, ex) => Some(ex),
            _ => None,
        }
    }

    /// Make a link for the action
    fn link<'p>(&self, a: &'p mut html::A<'p>) {
        a.href(self.name())
            .target("_blank")
            .rel("noopener noreferrer")
            .cdata(self.extra().unwrap_or("Link"));
    }

    /// Make a button for the action
    fn button<'p>(&self, button: &'p mut html::Button<'p>) {
        button
            .class("menu-action")
            .data_("action", format!("{self:?}"))
            .cdata(self.to_string());
    }

    /// Perform the menu action
    async fn perform(self) -> Result<()> {
        log::debug!("action: {self:?}");
        use MenuAction::*;
        match self {
            Publish(nm) => {
                let uri = uri_one(Res::Camera, &nm);
                let mut attr = Attr::new();
                attr.bool("publish", true);
                let value = JsValue::from(attr);
                Action::Patch(uri, value).perform().await
            }
            Unpublish(nm) => {
                let uri = uri_one(Res::Camera, &nm);
                let mut attr = Attr::new();
                attr.bool("publish", false);
                let value = JsValue::from(attr);
                Action::Patch(uri, value).perform().await
            }
            ScratchAdd(nm, pl) => {
                let plist = PlayList::fetch_one(&pl).await?;
                if let Some(mut entries) = plist.entries {
                    entries.push(nm);
                    set_playlist_entries(&pl, entries).await
                } else {
                    log::debug!("no entries: {pl}");
                    Ok(())
                }
            }
            ScratchRemove(nm, pl) => {
                let plist = PlayList::fetch_one(&pl).await?;
                if let Some(mut entries) = plist.entries {
                    entries.retain(|e| *e != nm);
                    set_playlist_entries(&pl, entries).await
                } else {
                    log::debug!("no entries: {pl}");
                    Ok(())
                }
            }
            _ => unimplemented!(),
        }
    }
}

/// Set entries on a playlist
async fn set_playlist_entries(pl: &str, entries: Vec<String>) -> Result<()> {
    let uri = uri_one(Res::PlayList, pl);
    let mut attr = Attr::new();
    attr.array("entries", entries);
    let value = JsValue::from(attr);
    Action::Patch(uri, value).perform().await
}

impl fmt::Debug for MenuAction {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}+{}", self.as_str(), self.name())?;
        if let Some(extra) = self.extra() {
            write!(f, "+{extra}")?;
        }
        Ok(())
    }
}

impl fmt::Display for MenuAction {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        use MenuAction::*;
        match self {
            Link(_, ex) => write!(f, "{ex}"),
            Blank(_) => write!(f, "Blank"),
            Publish(_) => write!(f, "Publish"),
            Unpublish(_) => write!(f, "Unpublish"),
            ScratchAdd(_, pl) => write!(f, "Add to {pl}"),
            ScratchRemove(_, pl) => write!(f, "Remove from {pl}"),
        }
    }
}

impl TryFrom<&Element> for MenuAction {
    type Error = Error;

    fn try_from(el: &Element) -> Result<Self> {
        use MenuAction::*;
        let action = el
            .get_attribute("data-action")
            .ok_or(Error::MissingAttribute())?;
        let mut params = action.split('+');
        let var = params.next().ok_or(Error::MissingAttribute())?;
        let nm = params.next().ok_or(Error::MissingAttribute())?.to_string();
        if "Link" == var {
            let ex = params.next().ok_or(Error::MissingAttribute())?;
            Ok(Link(nm, ex.to_string()))
        } else if "Blank" == var {
            Ok(Blank(nm))
        } else if "Publish" == var {
            Ok(Publish(nm))
        } else if "Unpublish" == var {
            Ok(Unpublish(nm))
        } else if "ScratchAdd" == var {
            let pl = params.next().ok_or(Error::MissingAttribute())?;
            Ok(ScratchAdd(nm, pl.to_string()))
        } else if "ScratchRemove" == var {
            let pl = params.next().ok_or(Error::MissingAttribute())?;
            Ok(ScratchRemove(nm, pl.to_string()))
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
                    let item = make_osm_link(lonlat);
                    let mut li = menu.li();
                    item.link(&mut li.a());
                    li.close();
                }
            } else if let Some((res, nm)) = layer_res(&me) {
                for item in make_menu_res(res, nm).await? {
                    let mut li = menu.li();
                    if let MenuAction::Link(_, _) = item {
                        item.link(&mut li.a());
                    } else {
                        item.button(&mut li.button());
                    }
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
fn make_osm_link(lonlat: (f64, f64)) -> MenuAction {
    let href = format!("{OSM}?mlat={:.5}&mlon={:.5}", lonlat.1, lonlat.0);
    let ex = String::from("OpenStreetMap");
    MenuAction::Link(href, ex)
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
    action.perform().await?;
    if let Some(el) = Doc::get().opt_elem::<HtmlElement>(eid::MAP_MENU) {
        el.hide_popover()?;
    }
    Ok(())
}
