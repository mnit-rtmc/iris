use serde::de::DeserializeOwned;
use serde::{Deserialize, Serialize};
use wasm_bindgen::prelude::*;
use wasm_bindgen::JsCast;
use wasm_bindgen_futures::{spawn_local, JsFuture};
use web_sys::{
    console, Document, Element, Event, HtmlElement, HtmlInputElement,
    HtmlSelectElement, Request, Response, Window,
};

type Result<T> = std::result::Result<T, JsValue>;

trait ElemCast {
    /// Get an element by ID and cast it
    fn elem<E: JsCast>(&self, id: &str) -> Result<E>;
}

impl ElemCast for Document {
    fn elem<E: JsCast>(&self, id: &str) -> Result<E> {
        Ok(self
            .get_element_by_id(id)
            .ok_or("id not found")?
            .dyn_into::<E>()?)
    }
}

/// Fetch a JSON document
async fn fetch_json(window: &Window, uri: &str) -> Result<JsValue> {
    let req = Request::new_with_str(uri)?;
    req.headers().set("Accept", "application/json")?;
    let resp = JsFuture::from(window.fetch_with_request(&req)).await?;
    let resp: Response = resp.dyn_into().unwrap_throw();
    match resp.status() {
        200 => Ok(JsFuture::from(resp.json()?).await?),
        401 => Err(resp.status_text().into()),
        _ => Err(resp.status_text().into()),
    }
}

/// Alarm
#[derive(Debug, Deserialize, Serialize)]
struct Alarm {
    pub name: String,
    pub description: String,
    pub controller: Option<String>,
    pub pin: u32,
    pub state: bool,
    //pub trigger_time: Option<String>,
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

/// CSS class for titles
const TITLE: &str = "title";

/// CSS class for disabled cards
const DISABLED: &str = " class='disabled'";

/// CSS class for info
const INFO: &str = "info";

/// Card for "Create New"
const CREATE_NEW_CARD: &str = "\
    <li name='' class='card'>\
        <span class='notes'>Create New</span>\
    </li>";

/// IRIS object types
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
    /// Slice of all valid types
    const ALL: &'static [ObType] = &[
        ObType::Alarm,
        ObType::CabinetStyle,
        ObType::CommConfig,
        ObType::CommLink,
        ObType::Controller,
        ObType::Modem,
    ];

    /// Get type name
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

    /// Get the type URI
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

    /// Populate cards in `ob_list`
    async fn populate_cards(self, tx: String) {
        if let Err(e) = self.try_populate_cards(tx).await {
            // unauthorized (401) should be handled here
            console::log_1(&e);
        }
    }

    /// Try to populate cards in `ob_list`
    async fn try_populate_cards(self, tx: String) -> Result<()> {
        let window = web_sys::window().unwrap_throw();
        let doc = window.document().unwrap_throw();
        let ob_list = doc.elem::<Element>("ob_list")?;
        if self.uri().is_empty() {
            ob_list.set_inner_html(&"");
        } else {
            let json = fetch_json(&window, self.uri()).await?;
            let html = self.build_cards(json, &tx)?;
            ob_list.set_inner_html(&format!(
                "<ul id='ob_cards' class='cards'>{html}</ul>"
            ));
        }
        Ok(())
    }

    /// Build cards for list
    fn build_cards(self, json: JsValue, tx: &str) -> Result<String> {
        match self {
            Self::Alarm => Alarm::build_cards(json, tx),
            Self::CabinetStyle => CabinetStyle::build_cards(json, tx),
            Self::CommConfig => CommConfig::build_cards(json, tx),
            Self::CommLink => CommLink::build_cards(json, tx),
            Self::Controller => Controller::build_cards(json, tx),
            Self::Modem => Modem::build_cards(json, tx),
            _ => Ok("".into()),
        }
    }

    /// Build form using JSON value
    fn build_form(self, json: JsValue) -> Result<String> {
        match self {
            Self::Alarm => Alarm::build_form(json),
            Self::CabinetStyle => CabinetStyle::build_form(json),
            Self::CommConfig => CommConfig::build_form(json),
            Self::CommLink => CommLink::build_form(json),
            Self::Controller => Controller::build_form(json),
            Self::Modem => Modem::build_form(json),
            _ => Ok("".into()),
        }
    }

    /// Add form for the given name
    async fn add_form(self, name: String) {
        let window = web_sys::window().unwrap_throw();
        let uri = format!("{}/{}", self.uri(), &name);
        let json = fetch_json(&window, &uri).await.unwrap_throw();
        let doc = window.document().unwrap_throw();
        let ob_form: HtmlElement = doc.elem("ob_form").unwrap_throw();
        ob_form.set_inner_html(&self.build_form(json).unwrap_throw());
        let style = ob_form.style();
        style.set_property("max-height", "50%").unwrap_throw();
    }
}

trait Card: DeserializeOwned {
    /// Build form using JSON value
    fn build_form(_json: JsValue) -> Result<String> {
        Ok("".into())
    }

    fn is_match(&self, _tx: &str) -> bool {
        false
    }

    fn name(&self) -> &str;

    fn build_card(&self) -> Result<String>;

    fn build_cards(json: JsValue, tx: &str) -> Result<String> {
        let mut html = String::new();
        if tx.is_empty() {
            html.push_str(CREATE_NEW_CARD);
        }
        let obs = json.into_serde::<Vec<Self>>().unwrap_throw();
        for ob in obs.iter().filter(|ob| ob.is_match(tx)) {
            let name = ob.name();
            html.push_str(&format!("<li name='{name}' class='card'>"));
            html.push_str(&ob.build_card()?);
            html.push_str("</li>");
        }
        Ok(html)
    }
}

impl Card for () {
    fn name(&self) -> &str {
        unreachable!()
    }

    fn build_card(&self) -> Result<String> {
        unreachable!()
    }
}

impl Card for Alarm {
    /// Build form using JSON value
    fn build_form(json: JsValue) -> Result<String> {
        let val = json.into_serde::<Self>().unwrap_throw();
        let name = &val.name;
        let description = &val.description;
        Ok(format!(
            "<div class='row'>\
                <div class='{TITLE}'>Alarm</div>\
                <span class='{INFO}'>{name}</span>\
            </div>\
            <label for='form_description'>Description</label>\
            <div class='row'>\
                <input id='form_description' maxlength='24' size='24' \
                       value='{description}'/>\
            </div>"
        ))
    }

    fn is_match(&self, tx: &str) -> bool {
        self.description.to_lowercase().contains(tx)
            || self.name.to_lowercase().contains(tx)
    }

    fn name(&self) -> &str {
        &self.name
    }

    fn build_card(&self) -> Result<String> {
        let name = &self.name;
        let description = &self.description;
        Ok(format!(
            "<span>{description}</span>\
            <span class='{INFO}'>{name}</span>"
        ))
    }
}

impl Card for CabinetStyle {
    fn is_match(&self, tx: &str) -> bool {
        self.name.to_lowercase().contains(tx)
    }

    fn name(&self) -> &str {
        &self.name
    }

    fn build_card(&self) -> Result<String> {
        let name = &self.name;
        Ok(format!("<span>{name}</span>"))
    }
}

impl Card for CommConfig {
    fn is_match(&self, tx: &str) -> bool {
        self.description.to_lowercase().contains(tx)
            || self.name.to_lowercase().contains(tx)
    }

    fn name(&self) -> &str {
        &self.name
    }

    fn build_card(&self) -> Result<String> {
        let name = &self.name;
        let description = &self.description;
        Ok(format!(
            "<span>{description}</span>\
            <span class='{INFO}'>{name}</span>"
        ))
    }
}

impl Card for CommLink {
    fn is_match(&self, tx: &str) -> bool {
        self.description.to_lowercase().contains(tx)
            || self.name.to_lowercase().contains(tx)
    }

    fn name(&self) -> &str {
        &self.name
    }

    fn build_card(&self) -> Result<String> {
        let name = &self.name;
        let description = &self.description;
        let disabled = if self.poll_enabled { "" } else { DISABLED };
        Ok(format!(
            "<span{disabled}>{description}</span>\
            <span class='{INFO}'>{name}</span>"
        ))
    }
}

impl Card for Controller {
    fn is_match(&self, tx: &str) -> bool {
        let comm_link = self.comm_link.to_lowercase();
        comm_link.contains(tx)
            || format!("{}:{}", comm_link, self.drop_id).contains(tx)
    }

    fn name(&self) -> &str {
        &self.name
    }

    fn build_card(&self) -> Result<String> {
        let name = &self.name;
        // condition 1 is "Active"
        let disabled = if self.condition == 1 { "" } else { DISABLED };
        let comm_link = &self.comm_link;
        let drop_id = self.drop_id;
        Ok(format!(
            "<span{disabled}>{comm_link}:{drop_id}</span>\
            <span class='{INFO}'>{name}</span>"
        ))
    }
}

impl Card for Modem {
    fn is_match(&self, tx: &str) -> bool {
        self.name.to_lowercase().contains(tx)
    }

    fn name(&self) -> &str {
        &self.name
    }

    fn build_card(&self) -> Result<String> {
        let name = &self.name;
        let disabled = if self.enabled { "" } else { DISABLED };
        Ok(format!("<span{disabled}>{name}</span>"))
    }
}

/// Set global allocator to `wee_alloc`
#[global_allocator]
static ALLOC: wee_alloc::WeeAlloc = wee_alloc::WeeAlloc::INIT;

#[wasm_bindgen(start)]
pub async fn main() -> Result<()> {
    // this should be debug only
    console_error_panic_hook::set_once();

    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    let ob_type: HtmlSelectElement = doc.elem("ob_type")?;
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
    let ob_input = doc.elem("ob_input")?;
    add_input_event_listener(&ob_input, handle_search_ev)?;
    let ob_list = doc.elem("ob_list")?;
    add_click_event_listener(&ob_list, handle_click_ev)?;
    Ok(())
}

/// Handle an event from "ob_type" `select` element
fn handle_type_ev(tp: &str) {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    deselect_cards(&doc).unwrap_throw();
    let input: HtmlInputElement = doc.elem("ob_input").unwrap_throw();
    let tx = input.value().to_lowercase();
    let tp: ObType = tp.into();
    spawn_local(tp.populate_cards(tx));
}

/// Handle an event from "ob_input" `input` element
fn handle_search_ev(tx: String) {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    deselect_cards(&doc).unwrap_throw();
    let tp = selected_type(&doc).unwrap_throw();
    spawn_local(tp.populate_cards(tx));
}

fn selected_type(doc: &Document) -> Result<ObType> {
    let ob_type: HtmlSelectElement = doc.elem("ob_type")?;
    let tp = ob_type.value();
    Ok(ObType::from(tp.as_str()))
}

/// Add an "input" event listener to an element
fn add_select_event_listener(
    elem: &HtmlSelectElement,
    handle_ev: fn(&str),
) -> Result<()> {
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
    elem: &HtmlInputElement,
    handle_ev: fn(String),
) -> Result<()> {
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
) -> Result<()> {
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
    console::log_1(&JsValue::from(tp.name()));
    deselect_cards(&doc).unwrap_throw();
    if let Some(card) = elem.closest(".card").unwrap_throw() {
        if let Some(name) = card.get_attribute("name") {
            card.set_class_name("card selected");
            spawn_local(tp.add_form(name));
        }
    }
}

fn deselect_cards(doc: &Document) -> Result<()> {
    if let Ok(ob_cards) = doc.elem::<Element>("ob_cards") {
        let cards = ob_cards.children();
        for i in 0..cards.length() {
            if let Some(card) = cards.get_with_index(i) {
                card.set_class_name("card");
            }
        }
    }
    let ob_form: HtmlElement = doc.elem("ob_form")?;
    let style = ob_form.style();
    style.set_property("max-height", "0")?;
    Ok(())
}
