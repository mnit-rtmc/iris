use serde::de::DeserializeOwned;
use serde::{Deserialize, Serialize};
use wasm_bindgen::prelude::*;
use wasm_bindgen::JsCast;
use wasm_bindgen_futures::{spawn_local, JsFuture};
use web_sys::{
    console, Document, Element, Event, HtmlElement, HtmlInputElement,
    HtmlSelectElement, Request, Response, Window,
};

/// Alarm
#[derive(Debug, Deserialize, Serialize)]
struct Alarm {
    pub name: String,
    pub description: String,
    pub controller: Option<String>,
    pub pin: u32,
    pub state: bool,
    pub trigger_time: Option<String>,
}

/// Cabinet Style
#[derive(Debug, Deserialize, Serialize)]
struct CabinetStyle {
    pub name: String,
    pub police_panel_pin_1: Option<u32>,
    pub police_panel_pin_2: Option<u32>,
    pub watchdog_reset_pin_1: Option<u32>,
    pub watchdog_reset_pin_2: Option<u32>,
    pub dip: Option<u32>,
}

/// Comm configuration
#[derive(Debug, Deserialize, Serialize)]
struct CommConfig {
    pub name: String,
    pub description: String,
    pub protocol: u32,
    pub modem: bool,
    pub timeout_ms: u32,
    pub poll_period_sec: u32,
    pub long_poll_period_sec: u32,
    pub idle_disconnect_sec: u32,
    pub no_response_disconnect_sec: u32,
}

/// Comm link
#[derive(Debug, Deserialize, Serialize)]
struct CommLink {
    pub name: String,
    pub description: String,
    pub uri: String,
    pub poll_enabled: bool,
    pub comm_config: String,
}

/// Controller
#[derive(Debug, Deserialize, Serialize)]
struct Controller {
    pub name: String,
    pub drop_id: u16,
    pub comm_link: String,
    pub cabinet: String,
    pub condition: u32,
    pub notes: String,
    pub fail_time: Option<String>,
    pub version: Option<String>,
}

/// Modem
#[derive(Debug, Deserialize, Serialize)]
struct Modem {
    pub name: String,
    pub uri: String,
    pub config: String,
    pub timeout_ms: u32,
    pub enabled: bool,
}

const TITLE: &str = "title";
const DISABLED: &str = "title disabled";
const INFO: &str = "info";

trait Card: DeserializeOwned {
    const OB_TYPE: &'static str;
    const URI: &'static str;

    fn is_match(&self, _tx: &str) -> bool {
        false
    }
    fn make_card(&self, doc: &Document) -> Result<Element, JsValue>;
}

impl Card for () {
    const OB_TYPE: &'static str = "";
    const URI: &'static str = "";

    fn make_card(&self, _doc: &Document) -> Result<Element, JsValue> {
        unreachable!()
    }
}

impl Card for Alarm {
    const OB_TYPE: &'static str = "Alarm";
    const URI: &'static str = "/iris/api/alarm";

    fn is_match(&self, tx: &str) -> bool {
        self.description.to_lowercase().contains(tx)
            || self.name.to_lowercase().contains(tx)
    }
    fn make_card(&self, doc: &Document) -> Result<Element, JsValue> {
        let card = doc.create_element("li")?;
        card.set_attribute("name", &self.name)?;
        card.set_class_name("card");
        let title = doc.create_element("span")?;
        title.set_class_name(TITLE);
        title.set_inner_html(&self.description);
        card.append_child(&title)?;
        let info = doc.create_element("span")?;
        info.set_class_name(INFO);
        info.set_inner_html(&self.name);
        card.append_child(&info)?;
        Ok(card)
    }
}

impl Card for CabinetStyle {
    const OB_TYPE: &'static str = "Cabinet Style";
    const URI: &'static str = "/iris/api/cabinet_style";

    fn is_match(&self, tx: &str) -> bool {
        self.name.to_lowercase().contains(tx)
    }
    fn make_card(&self, doc: &Document) -> Result<Element, JsValue> {
        let card = doc.create_element("li")?;
        card.set_attribute("name", &self.name)?;
        card.set_class_name("card");
        let title = doc.create_element("span")?;
        title.set_class_name(TITLE);
        title.set_inner_html(&self.name);
        card.append_child(&title)?;
        Ok(card)
    }
}

impl Card for CommConfig {
    const OB_TYPE: &'static str = "Comm Config";
    const URI: &'static str = "/iris/api/comm_config";

    fn is_match(&self, tx: &str) -> bool {
        self.description.to_lowercase().contains(tx)
            || self.name.to_lowercase().contains(tx)
    }
    fn make_card(&self, doc: &Document) -> Result<Element, JsValue> {
        let card = doc.create_element("li")?;
        card.set_attribute("name", &self.name)?;
        card.set_class_name("card");
        let title = doc.create_element("span")?;
        title.set_class_name(TITLE);
        title.set_inner_html(&self.description);
        card.append_child(&title)?;
        let info = doc.create_element("span")?;
        info.set_class_name(INFO);
        info.set_inner_html(&self.name);
        card.append_child(&info)?;
        Ok(card)
    }
}

impl Card for CommLink {
    const OB_TYPE: &'static str = "Comm Link";
    const URI: &'static str = "/iris/api/comm_link";

    fn is_match(&self, tx: &str) -> bool {
        self.description.to_lowercase().contains(tx)
            || self.name.to_lowercase().contains(tx)
    }
    fn make_card(&self, doc: &Document) -> Result<Element, JsValue> {
        let card = doc.create_element("li")?;
        card.set_attribute("name", &self.name)?;
        card.set_class_name("card");
        let title = doc.create_element("span")?;
        if self.poll_enabled {
            title.set_class_name(TITLE);
        } else {
            title.set_class_name(DISABLED);
        }
        title.set_inner_html(&self.description);
        card.append_child(&title)?;
        let info = doc.create_element("span")?;
        info.set_class_name(INFO);
        info.set_inner_html(&self.name);
        card.append_child(&info)?;
        Ok(card)
    }
}

impl Card for Controller {
    const OB_TYPE: &'static str = "Controller";
    const URI: &'static str = "/iris/api/controller";

    fn is_match(&self, tx: &str) -> bool {
        let comm_link = self.comm_link.to_lowercase();
        comm_link.contains(tx)
            || format!("{}:{}", comm_link, self.drop_id).contains(tx)
    }
    fn make_card(&self, doc: &Document) -> Result<Element, JsValue> {
        let card = doc.create_element("li")?;
        card.set_attribute("name", &self.name)?;
        card.set_class_name("card");
        let title = doc.create_element("span")?;
        // condition 1 is "Active"
        if self.condition == 1 {
            title.set_class_name(TITLE);
        } else {
            title.set_class_name(DISABLED);
        }
        title.set_inner_html(&format!("{}:{}", self.comm_link, self.drop_id));
        card.append_child(&title)?;
        let info = doc.create_element("span")?;
        info.set_class_name(INFO);
        info.set_inner_html(&self.name);
        card.append_child(&info)?;
        Ok(card)
    }
}

impl Card for Modem {
    const OB_TYPE: &'static str = "Modem";
    const URI: &'static str = "/iris/api/modem";

    fn is_match(&self, tx: &str) -> bool {
        self.name.to_lowercase().contains(tx)
    }
    fn make_card(&self, doc: &Document) -> Result<Element, JsValue> {
        let card = doc.create_element("li")?;
        card.set_attribute("name", &self.name)?;
        card.set_class_name("card");
        let title = doc.create_element("span")?;
        if self.enabled {
            title.set_class_name(TITLE);
        } else {
            title.set_class_name(DISABLED);
        }
        title.set_inner_html(&self.name);
        card.append_child(&title)?;
        Ok(card)
    }
}

/// Object types
const OB_TYPES: &[&str] = &[
    Alarm::OB_TYPE,
    CabinetStyle::OB_TYPE,
    CommConfig::OB_TYPE,
    CommLink::OB_TYPE,
    Controller::OB_TYPE,
    Modem::OB_TYPE,
];

/// Fetch a JSON array and deserialize into a Vec
async fn fetch_json_vec<C: Card>(window: &Window) -> Result<Vec<C>, JsValue> {
    let req = Request::new_with_str(C::URI)?;
    req.headers().set("Accept", "application/json")?;
    let resp = JsFuture::from(window.fetch_with_request(&req)).await?;
    let resp: Response = resp.dyn_into().unwrap_throw();
    match resp.status() {
        200 => {
            let json = JsFuture::from(resp.json()?).await?;
            Ok(json.into_serde::<Vec<C>>().unwrap_throw())
        }
        401 => Err(resp.status_text().into()),
        _ => Err(resp.status_text().into()),
    }
}

/// Set global allocator to `wee_alloc`
#[global_allocator]
static ALLOC: wee_alloc::WeeAlloc = wee_alloc::WeeAlloc::INIT;

#[wasm_bindgen(start)]
pub async fn main() -> Result<(), JsValue> {
    // this should be debug only
    console_error_panic_hook::set_once();

    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    let ob_type = doc.get_element_by_id("ob_type").unwrap_throw();
    let opt = doc.create_element("option")?;
    opt.append_with_str_1("")?;
    ob_type.append_child(&opt)?;
    let group = doc.create_element("optgroup")?;
    group.set_attribute("label", "Maintenance")?;
    for ob in OB_TYPES {
        let opt = doc.create_element("option")?;
        opt.append_with_str_1(ob)?;
        group.append_child(&opt)?;
    }
    ob_type.append_child(&group)?;
    add_select_event_listener(&ob_type, handle_type_ev)?;
    let ob_input = doc.get_element_by_id("ob_input").unwrap_throw();
    add_input_event_listener(&ob_input, handle_search_ev)?;
    let ob_list = doc.get_element_by_id("ob_list").unwrap_throw();
    add_click_event_listener(&ob_list, handle_click_ev)?;
    Ok(())
}

/// Handle an event from "ob_type" `select` element
fn handle_type_ev(tp: &str) {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    let tx = doc
        .get_element_by_id("ob_input")
        .unwrap_throw()
        .dyn_into::<HtmlInputElement>()
        .unwrap_throw()
        .value()
        .to_lowercase();
    populate_cards(tp, tx);
}

/// Handle an event from "ob_input" `input` element
fn handle_search_ev(tx: String) {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    let tp = selected_type(&doc);
    populate_cards(&tp, tx);
}

fn selected_type(doc: &Document) -> String {
    doc.get_element_by_id("ob_type")
        .unwrap_throw()
        .dyn_into::<HtmlSelectElement>()
        .unwrap_throw()
        .value()
}

/// Populate cards in list
fn populate_cards(tp: &str, tx: String) {
    match tp {
        Alarm::OB_TYPE => spawn_local(populate_list::<Alarm>(tx)),
        CabinetStyle::OB_TYPE => spawn_local(populate_list::<CabinetStyle>(tx)),
        CommConfig::OB_TYPE => spawn_local(populate_list::<CommConfig>(tx)),
        CommLink::OB_TYPE => spawn_local(populate_list::<CommLink>(tx)),
        Controller::OB_TYPE => spawn_local(populate_list::<Controller>(tx)),
        Modem::OB_TYPE => spawn_local(populate_list::<Modem>(tx)),
        _ => spawn_local(populate_list::<()>(tx)),
    }
}

async fn populate_list<C: Card>(tx: String) {
    if let Err(e) = try_populate_list::<C>(tx).await {
        // unauthorized (401) should be handled here
        console::log_1(&e);
    }
}

async fn try_populate_list<C: Card>(tx: String) -> Result<(), JsValue> {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    let ob_list = doc.get_element_by_id("ob_list").unwrap_throw();
    remove_children(&ob_list);
    if !C::URI.is_empty() {
        let cards = doc.create_element("ul")?;
        cards.set_class_name("cards");
        let obs: Vec<C> = fetch_json_vec(&window).await?;
        if tx.is_empty() {
            cards.append_child(&*make_new_elem(&doc)?)?;
        }
        for ob in obs.iter().filter(|ob| ob.is_match(&tx)) {
            cards.append_child(&*ob.make_card(&doc)?)?;
        }
        ob_list.append_child(&cards)?;
    }
    Ok(())
}

fn remove_children(elem: &Element) {
    let children = elem.children();
    for i in 0..children.length() {
        if let Some(child) = children.get_with_index(i) {
            child.remove();
        }
    }
}

fn make_new_elem(doc: &Document) -> Result<Element, JsValue> {
    let card = doc.create_element("li")?;
    card.set_attribute("name", "")?;
    card.set_class_name("card");
    let title = doc.create_element("span")?;
    title.set_class_name("notes");
    title.set_inner_html("Create New");
    card.append_child(&title)?;
    Ok(card)
}

/// Add an "input" event listener to an element
fn add_select_event_listener(
    elem: &Element,
    handle_ev: fn(&str),
) -> Result<(), JsValue> {
    let closure = Closure::wrap(Box::new(move |e: Event| {
        let value = e
            .current_target()
            .unwrap()
            .dyn_into::<HtmlSelectElement>()
            .unwrap()
            .value();
        handle_ev(&value);
    }) as Box<dyn FnMut(_)>);
    elem.add_event_listener_with_callback(
        "input",
        closure.as_ref().unchecked_ref(),
    )?;
    // can't drop closure, just forget it to make JS happy
    closure.forget();
    Ok(())
}

/// Add an "input" event listener to an element
fn add_input_event_listener(
    elem: &Element,
    handle_ev: fn(String),
) -> Result<(), JsValue> {
    let closure = Closure::wrap(Box::new(move |e: Event| {
        let value = e
            .current_target()
            .unwrap()
            .dyn_into::<HtmlInputElement>()
            .unwrap()
            .value();
        handle_ev(value);
    }) as Box<dyn FnMut(_)>);
    elem.add_event_listener_with_callback(
        "input",
        closure.as_ref().unchecked_ref(),
    )?;
    // can't drop closure, just forget it to make JS happy
    closure.forget();
    Ok(())
}

/// Add a "click" event listener to an element
fn add_click_event_listener(
    elem: &Element,
    handle_ev: fn(&Element),
) -> Result<(), JsValue> {
    let closure = Closure::wrap(Box::new(move |e: Event| {
        let value = e.target().unwrap().dyn_into::<Element>().unwrap();
        handle_ev(&value);
    }) as Box<dyn FnMut(_)>);
    elem.add_event_listener_with_callback(
        "click",
        closure.as_ref().unchecked_ref(),
    )?;
    // can't drop closure, just forget it to make JS happy
    closure.forget();
    Ok(())
}

/// Handle an event from "ob_list" `click` element
fn handle_click_ev(elem: &Element) {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    let tp = selected_type(&doc);
    console::log_1(&tp.into());
    if let Some(card) = elem.closest(".card").unwrap_throw() {
        if let Some(name) = card.get_attribute("name") {
            let ob_form = doc.get_element_by_id("ob_form").unwrap_throw();
            remove_children(&ob_form);
            let title = doc.create_element("span").unwrap_throw();
            title.set_inner_html(&format!("name: {}", name));
            ob_form.append_child(&title).unwrap_throw();
            let style =
                ob_form.dyn_into::<HtmlElement>().unwrap_throw().style();
            style.set_property("max-height", "50%").unwrap_throw();
        }
    }
}
