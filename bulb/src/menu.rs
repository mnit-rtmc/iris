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
use crate::util::Doc;
use earthwyrm::{MapEvent, MapPane};
use hatmil::css::{Prop, Rule, Sel};
use hatmil::{Tree, html};
use resources::Res;
use web_sys::{Element, HtmlElement};

/// Handle a `contextmenu` event
pub async fn handle_contextmenu(me: MapEvent, x: i32, y: i32) -> Result<()> {
    set_context_style(Some(&me));
    if let Some(el) = Doc::get().opt_elem::<HtmlElement>(eid::MAP_MENU) {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.id(eid::MAP_MENU);
        if let Some(title) = menu_title(&me) {
            div.style(Prop::new().left(format!("{x}px")).top(format!("{y}px")));
            let mut menu = div.menu();
            menu.style(menu_style(x, y));
            menu.li().cdata(&title);
        } else {
            div.class("no-display");
        }
        el.set_outer_html(&String::from(tree));
    }
    Ok(())
}

/// Set context style for a layer
fn set_context_style(me: Option<&MapEvent>) {
    if let Some(el) = Doc::get().opt_elem::<Element>("context-style") {
        match me {
            Some(me) => {
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
            None => el.set_inner_html(""),
        }
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

/// Build context menu style
fn menu_style(x: i32, y: i32) -> Prop {
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
    prop
}

/// Dismiss the map context menu
pub fn dismiss_context_menu() {
    if let Some(el) = Doc::get().opt_elem::<HtmlElement>(eid::MAP_MENU) {
        let mut tree = Tree::new();
        let mut div = tree.root::<html::Div>();
        div.id(eid::MAP_MENU).class("no-display");
        el.set_outer_html(&String::from(tree));
    }
    set_context_style(None);
}
