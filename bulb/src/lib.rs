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

#[derive(Clone, Copy, Debug)]
enum ObType {
    Unknown,
    Alarm,
    CabinetStyle,
    CommConfig,
    CommLink,
    Controller,
    Modem,
}

impl From<&str> for ObType {
    fn from(tp: &str) -> Self {
        match tp {
            "Alarm" => ObType::Alarm,
            "Cabinet Style" => ObType::CabinetStyle,
            "Comm Config" => ObType::CommConfig,
            "Comm Link" => ObType::CommLink,
            "Controller" => ObType::Controller,
            "Modem" => ObType::Modem,
            _ => ObType::Unknown,
        }
    }
}

impl ObType {
    const ALL: &'static [ObType] = &[
        ObType::Alarm,
        ObType::CabinetStyle,
        ObType::CommConfig,
        ObType::CommLink,
        ObType::Controller,
        ObType::Modem,
    ];

    fn name(self) -> &'static str {
        match self {
            Self::Unknown => "",
            Self::Alarm => "Alarm",
            Self::CabinetStyle => "Cabinet Style",
            Self::CommConfig => "Comm Config",
            Self::CommLink => "Comm Link",
            Self::Controller => "Controller",
            Self::Modem => "Modem",
        }
    }

    fn uri(self) -> &'static str {
        match self {
            Self::Unknown => "",
            Self::Alarm => "/iris/api/alarm",
            Self::CabinetStyle => "/iris/api/cabinet_style",
            Self::CommConfig => "/iris/api/comm_config",
            Self::CommLink => "/iris/api/comm_link",
            Self::Controller => "/iris/api/controller",
            Self::Modem => "/iris/api/modem",
        }
    }

    /// Populate cards in list
    fn populate_cards(self, tx: String) {
        spawn_local(self.populate_list(tx));
    }

    async fn populate_list(self, tx: String) {
        if let Err(e) = self.try_populate_list(tx).await {
            // unauthorized (401) should be handled here
            console::log_1(&e);
        }
    }

    async fn try_populate_list(self, tx: String) -> Result<(), JsValue> {
        let window = web_sys::window().unwrap_throw();
        let doc = window.document().unwrap_throw();
        let ob_list = doc.get_element_by_id("ob_list").unwrap_throw();
        remove_children(&ob_list);
        if !self.uri().is_empty() {
            let cards = doc.create_element("ul")?;
            cards.set_class_name("cards");
            if tx.is_empty() {
                cards.append_child(&*make_new_elem(&doc)?)?;
            }
            let json = self.fetch_json(&window).await?;
            self.append_cards(json, &tx, &doc, &cards)?;
            ob_list.append_child(&cards)?;
        }
        Ok(())
    }

    /// Fetch a JSON document
    async fn fetch_json(self, window: &Window) -> Result<JsValue, JsValue> {
        let req = Request::new_with_str(self.uri())?;
        req.headers().set("Accept", "application/json")?;
        let resp = JsFuture::from(window.fetch_with_request(&req)).await?;
        let resp: Response = resp.dyn_into().unwrap_throw();
        match resp.status() {
            200 => Ok(JsFuture::from(resp.json()?).await?),
            401 => Err(resp.status_text().into()),
            _ => Err(resp.status_text().into()),
        }
    }

    fn append_cards(
        self,
        json: JsValue,
        tx: &str,
        doc: &Document,
        cards: &Element,
    ) -> Result<(), JsValue> {
        match self {
            Self::Alarm => Alarm::make_cards(json, tx, doc, cards),
            Self::CabinetStyle => {
                CabinetStyle::make_cards(json, tx, doc, cards)
            }
            Self::CommConfig => CommConfig::make_cards(json, tx, doc, cards),
            Self::CommLink => CommLink::make_cards(json, tx, doc, cards),
            Self::Controller => Controller::make_cards(json, tx, doc, cards),
            Self::Modem => Modem::make_cards(json, tx, doc, cards),
            _ => Ok(()),
        }
    }
}

trait Card: DeserializeOwned {
    fn is_match(&self, _tx: &str) -> bool {
        false
    }

    fn make_card(&self, doc: &Document) -> Result<Element, JsValue>;

    fn make_cards(
        json: JsValue,
        tx: &str,
        doc: &Document,
        cards: &Element,
    ) -> Result<(), JsValue> {
        let obs = json.into_serde::<Vec<Self>>().unwrap_throw();
        for ob in obs.iter().filter(|ob| ob.is_match(tx)) {
            cards.append_child(&*ob.make_card(doc)?)?;
        }
        Ok(())
    }
}

impl Card for () {
    fn make_card(&self, _doc: &Document) -> Result<Element, JsValue> {
        unreachable!()
    }
}

impl Card for Alarm {
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
    for ob in ObType::ALL {
        let opt = doc.create_element("option")?;
        opt.append_with_str_1(ob.name())?;
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
    let tp: ObType = tp.into();
    tp.populate_cards(tx);
}

/// Handle an event from "ob_input" `input` element
fn handle_search_ev(tx: String) {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    let tp = selected_type(&doc).unwrap_throw();
    let tp = ObType::from(&tp[..]);
    tp.populate_cards(tx);
}

fn selected_type(doc: &Document) -> Result<String, JsValue> {
    Ok(doc
        .get_element_by_id("ob_type")
        .ok_or("`ob_type` not found")?
        .dyn_into::<HtmlSelectElement>()?
        .value())
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
    let tp = selected_type(&doc).unwrap_throw();
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
