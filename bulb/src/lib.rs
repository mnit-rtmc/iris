use serde::de::DeserializeOwned;
use serde::{Deserialize, Serialize};
use wasm_bindgen::prelude::*;
use wasm_bindgen::JsCast;
use wasm_bindgen_futures::{spawn_local, JsFuture};
use web_sys::{
    console, Document, Element, Event, HtmlElement, HtmlInputElement,
    HtmlLabelElement, HtmlSelectElement, Request, Response, Window,
};

trait ElemCast {
    /// Get an element by ID and cast it
    fn elem<E: JsCast>(&self, id: &str) -> Result<E, JsValue>;

    /// Make an element and cast it
    fn make_elem<E: JsCast>(&self, local_name: &str) -> Result<E, JsValue>;
}

impl ElemCast for Document {
    fn elem<E: JsCast>(&self, id: &str) -> Result<E, JsValue> {
        Ok(self
            .get_element_by_id(id)
            .ok_or("id not found")?
            .dyn_into::<E>()?)
    }

    fn make_elem<E: JsCast>(&self, local_name: &str) -> Result<E, JsValue> {
        Ok(self.create_element(local_name)?.dyn_into::<E>()?)
    }
}

/// Fetch a JSON document
async fn fetch_json(window: &Window, uri: &str) -> Result<JsValue, JsValue> {
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
const DISABLED: &str = "disabled";

/// CSS class for info
const INFO: &str = "info";

/// CSS class for form
const FORM: &str = "form";

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
    async fn try_populate_cards(self, tx: String) -> Result<(), JsValue> {
        let window = web_sys::window().unwrap_throw();
        let doc = window.document().unwrap_throw();
        let ob_list = doc.elem("ob_list")?;
        remove_children(&ob_list);
        if !self.uri().is_empty() {
            let cards = doc.create_element("ul")?;
            cards.set_class_name("cards");
            if tx.is_empty() {
                cards.append_child(&*make_new_elem(&doc)?)?;
            }
            let json = fetch_json(&window, self.uri()).await?;
            self.append_cards(json, &tx, &doc, &cards)?;
            ob_list.append_child(&cards)?;
        }
        Ok(())
    }

    /// Append cards to a list element
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

    fn make_form(
        self,
        doc: &Document,
        form: &HtmlElement,
        json: JsValue,
    ) -> Result<(), JsValue> {
        match self {
            Self::Alarm => Alarm::make_form(doc, form, json),
            Self::CabinetStyle => CabinetStyle::make_form(doc, form, json),
            Self::CommConfig => CommConfig::make_form(doc, form, json),
            Self::CommLink => CommLink::make_form(doc, form, json),
            Self::Controller => Controller::make_form(doc, form, json),
            Self::Modem => Modem::make_form(doc, form, json),
            _ => Ok(()),
        }
    }

    /// Add form for the given name
    async fn add_form(self, name: String) {
        let window = web_sys::window().unwrap_throw();
        let doc = window.document().unwrap_throw();
        let ob_form: HtmlElement = doc.elem("ob_form").unwrap_throw();
        let uri = format!("{}/{}", self.uri(), &name);
        let json = fetch_json(&window, &uri).await.unwrap_throw();
        remove_children(&ob_form);
        self.make_form(&doc, &ob_form, json).unwrap_throw();
        let style = ob_form.style();
        style.set_property("max-height", "50%").unwrap_throw();
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

    fn make_form(
        _doc: &Document,
        _form: &HtmlElement,
        _json: JsValue,
    ) -> Result<(), JsValue> {
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
        title.set_inner_html(&self.description);
        card.append_child(&title)?;
        let info = doc.create_element("span")?;
        info.set_class_name(INFO);
        info.set_inner_html(&self.name);
        card.append_child(&info)?;
        Ok(card)
    }

    fn make_form(
        doc: &Document,
        form: &HtmlElement,
        json: JsValue,
    ) -> Result<(), JsValue> {
        let val = json.into_serde::<Self>().unwrap_throw();

        let div = doc.create_element("div")?;
        div.set_class_name("row");
        let title = doc.create_element("div")?;
        title.set_class_name(TITLE);
        title.set_inner_html(&"Alarm");
        div.append_child(&title)?;
        let info = doc.create_element("span")?;
        info.set_class_name(INFO);
        info.set_inner_html(&val.name);
        div.append_child(&info)?;
        form.append_child(&div)?;

        let desc: HtmlLabelElement = doc.make_elem("label")?;
        desc.set_html_for("form_description");
        desc.set_inner_html(&"Description");
        form.append_child(&desc)?;

        let div = doc.create_element("div")?;
        div.set_class_name("row");
        let input = doc.create_element("input")?;
        input.set_attribute("id", "form_description")?;
        input.set_attribute("maxlength", "24")?;
        input.set_attribute("size", "24")?;
        input.set_attribute("value", &val.description)?;
        div.append_child(&input).unwrap_throw();
        form.append_child(&div).unwrap_throw();
        form.set_class_name(FORM);

        Ok(())
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
        if !self.poll_enabled {
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
        if self.condition != 1 {
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
        if !self.enabled {
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
    let input: HtmlInputElement = doc.elem("ob_input").unwrap_throw();
    let tx = input.value().to_lowercase();
    let tp: ObType = tp.into();
    spawn_local(tp.populate_cards(tx));
}

/// Handle an event from "ob_input" `input` element
fn handle_search_ev(tx: String) {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    let tp = selected_type(&doc).unwrap_throw();
    spawn_local(tp.populate_cards(tx));
}

fn selected_type(doc: &Document) -> Result<ObType, JsValue> {
    let ob_type: HtmlSelectElement = doc.elem("ob_type")?;
    let tp = ob_type.value();
    Ok(ObType::from(tp.as_str()))
}

fn remove_children(elem: &Element) {
    let children = elem.children();
    while let Some(child) = children.get_with_index(0) {
        child.remove();
    }
}

/// Make card for "Create New"
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
    elem: &HtmlSelectElement,
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
    elem: &HtmlInputElement,
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
    console::log_1(&JsValue::from(tp.name()));
    if let Some(card) = elem.closest(".card").unwrap_throw() {
        card.set_class_name("card selected");
        if let Some(name) = card.get_attribute("name") {
            spawn_local(tp.add_form(name));
        }
    }
}
