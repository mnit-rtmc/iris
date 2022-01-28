use once_cell::sync::Lazy;
use percent_encoding::{utf8_percent_encode, NON_ALPHANUMERIC};
use serde::de::DeserializeOwned;
use serde::{Deserialize, Serialize};
use std::fmt;
use std::sync::Mutex;
use wasm_bindgen::prelude::*;
use wasm_bindgen::JsCast;
use wasm_bindgen_futures::{spawn_local, JsFuture};
use web_sys::{
    console, Document, Element, Event, HtmlElement, HtmlInputElement,
    HtmlSelectElement, Request, Response, ScrollBehavior,
    ScrollIntoViewOptions, ScrollLogicalPosition, Window,
};

type Result<T> = std::result::Result<T, JsValue>;

#[derive(Debug)]
struct OptVal<T>(Option<T>);

impl<T> fmt::Display for OptVal<T>
where
    T: fmt::Display,
{
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match &self.0 {
            Some(v) => write!(f, "{}", v),
            None => Ok(()),
        }
    }
}

#[derive(Debug)]
struct HtmlStr<S>(S);

impl<S> HtmlStr<S> {
    fn fmt_encode(s: &str, f: &mut fmt::Formatter) -> fmt::Result {
        for c in s.chars() {
            match c {
                '&' => write!(f, "&amp;")?,
                '<' => write!(f, "&lt;")?,
                '>' => write!(f, "&gt;")?,
                '"' => write!(f, "&quot;")?,
                '\'' => write!(f, "&#x27;")?,
                _ => write!(f, "{}", c)?,
            }
        }
        Ok(())
    }
}

impl fmt::Display for HtmlStr<&str> {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        Self::fmt_encode(self.0, f)
    }
}

impl fmt::Display for HtmlStr<&String> {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        Self::fmt_encode(self.0, f)
    }
}

impl fmt::Display for HtmlStr<Option<&String>> {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match &self.0 {
            Some(val) => Self::fmt_encode(val, f),
            None => Ok(()),
        }
    }
}

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
    pub password: Option<String>,
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
    fn tname(self) -> &'static str {
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
            ob_list.set_inner_html("");
        } else {
            let json = fetch_json(&window, self.uri()).await?;
            let html = self.build_cards(json, &tx)?;
            ob_list.set_inner_html(&html);
        }
        Ok(())
    }

    /// Build cards for list
    fn build_cards(self, json: JsValue, tx: &str) -> Result<String> {
        let tname = self.tname();
        match self {
            Self::Alarm => Alarm::build_cards(tname, json, tx),
            Self::CabinetStyle => CabinetStyle::build_cards(tname, json, tx),
            Self::CommConfig => CommConfig::build_cards(tname, json, tx),
            Self::CommLink => CommLink::build_cards(tname, json, tx),
            Self::Controller => Controller::build_cards(tname, json, tx),
            Self::Modem => Modem::build_cards(tname, json, tx),
            _ => Ok("".into()),
        }
    }

    /// Build form using JSON value
    fn build_form_json(self, json: JsValue) -> Result<String> {
        let tname = self.tname();
        match self {
            Self::Alarm => Alarm::build_form_json(tname, json),
            Self::CabinetStyle => CabinetStyle::build_form_json(tname, json),
            Self::CommConfig => CommConfig::build_form_json(tname, json),
            Self::CommLink => CommLink::build_form_json(tname, json),
            Self::Controller => Controller::build_form_json(tname, json),
            Self::Modem => Modem::build_form_json(tname, json),
            _ => Ok("".into()),
        }
    }

    /// Expand a card to a full form
    async fn expand_card(self, name: String) {
        let window = web_sys::window().unwrap_throw();
        if name.is_empty() {
            // todo: make "new" card?
            return;
        }
        let uri = format!(
            "{}/{}",
            self.uri(),
            utf8_percent_encode(&name, NON_ALPHANUMERIC)
        );
        let json = fetch_json(&window, &uri).await.unwrap_throw();
        console::log_1(&json);
        let doc = window.document().unwrap_throw();
        let id = format!("{}_{}", self.tname(), &name);
        let elem: HtmlElement = doc.elem(&id).unwrap_throw();
        let html = elem.inner_html();
        let mut state = STATE.lock().unwrap_throw();
        state.selected = Some((self, name, html));
        elem.set_class_name("form");
        elem.set_inner_html(&self.build_form_json(json).unwrap_throw());
        let mut opt = ScrollIntoViewOptions::new();
        opt.behavior(ScrollBehavior::Smooth)
            .block(ScrollLogicalPosition::Nearest);
        elem.scroll_into_view_with_scroll_into_view_options(&opt);
    }
}

#[derive(Clone, Copy, Debug, PartialEq, Eq)]
enum CardType {
    Compact,
    Form,
}

trait Card: DeserializeOwned {
    fn new(json: JsValue) -> Result<Self> {
        Ok(json.into_serde::<Self>().unwrap_throw())
    }

    /// Build form using JSON value
    fn build_form_json(tname: &str, json: JsValue) -> Result<String> {
        let val = Self::new(json)?;
        let name = HtmlStr(val.name());
        Ok(format!(
            "<div class='row'>\
              <div class='{TITLE}'>{tname}</div>\
              <span class='{INFO}'>{name}</span>\
            </div>\
            {}\
            <div class='row'>\
              <button type='button'>Delete</button>\
              <button type='button'>Save</button>\
            </div>",
            val.to_html(CardType::Form)
        ))
    }

    fn is_match(&self, _tx: &str) -> bool {
        false
    }

    fn name(&self) -> &str;

    fn build_cards(tname: &str, json: JsValue, tx: &str) -> Result<String> {
        let mut html = String::new();
        html.push_str("<ul id='ob_cards' class='cards'>");
        if tx.is_empty() {
            // the "New" card has id "{tname}_" and blank name
            html.push_str(&format!(
                "<li id='{tname}_' name='' class='card'>\
                    <span class='notes'>New</span>\
                </li>"
            ));
        }
        let obs = json.into_serde::<Vec<Self>>().unwrap_throw();
        for ob in obs.iter().filter(|ob| ob.is_match(tx)) {
            let name = HtmlStr(ob.name());
            html.push_str(&format!(
                "<li id='{tname}_{name}' name='{name}' class='card'>"
            ));
            html.push_str(&ob.to_html(CardType::Compact));
            html.push_str("</li>");
        }
        html.push_str("</ul>");
        Ok(html)
    }

    fn to_html(&self, _ct: CardType) -> String {
        String::new()
    }
}

impl Card for () {
    fn new(_json: JsValue) -> Result<Self> {
        unreachable!()
    }

    fn name(&self) -> &str {
        ""
    }
}

impl Card for Alarm {
    fn is_match(&self, tx: &str) -> bool {
        self.description.to_lowercase().contains(tx)
            || self.name.to_lowercase().contains(tx)
    }

    fn name(&self) -> &str {
        &self.name
    }

    fn to_html(&self, ct: CardType) -> String {
        let description = HtmlStr(&self.description);
        match ct {
            CardType::Compact => {
                let name = HtmlStr(&self.name);
                format!(
                    "<span>{description}</span>\
                    <span class='{INFO}'>{name}</span>"
                )
            }
            CardType::Form => {
                let controller = HtmlStr(self.controller.as_ref());
                let pin = self.pin;
                format!(
                    "<div class='row'>\
                      <label for='form_description'>Description</label>\
                      <input id='form_description' maxlength='24' size='24' \
                             value='{description}'/>\
                    </div>\
                    <div class='row'>\
                      <label for='form_controller'>Controller</label>\
                      <input id='form_controller' maxlength='20' size='20' \
                             value='{controller}'/>\
                    </div>\
                    <div class='row'>\
                      <label for='form_pin'>Pin</label>\
                      <input id='form_pin' type='number' min='1' max='104' \
                             size='8' value='{pin}'/>\
                    </div>"
                )
            }
        }
    }
}

impl Card for CabinetStyle {
    fn is_match(&self, tx: &str) -> bool {
        self.name.to_lowercase().contains(tx)
    }

    fn name(&self) -> &str {
        &self.name
    }

    fn to_html(&self, ct: CardType) -> String {
        match ct {
            CardType::Compact => {
                let name = HtmlStr(&self.name);
                format!("<span>{name}</span>")
            }
            CardType::Form => {
                let police_panel_pin_1 = OptVal(self.police_panel_pin_1);
                let police_panel_pin_2 = OptVal(self.police_panel_pin_2);
                let watchdog_reset_pin_1 = OptVal(self.watchdog_reset_pin_1);
                let watchdog_reset_pin_2 = OptVal(self.watchdog_reset_pin_2);
                let dip = OptVal(self.dip);
                format!(
                    "<div class='row'>\
                      <label for='form_pp1'>Police Panel Pin 1</label>\
                      <input id='form_pp1' type='number' min='1' max='104' \
                             size='8' value='{police_panel_pin_1}'/>\
                    </div>\
                    <div class='row'>\
                      <label for='form_pp2'>Police Panel Pin 2</label>\
                      <input id='form_pp2' type='number' min='1' max='104' \
                             size='8' value='{police_panel_pin_2}'/>\
                    </div>\
                    <div class='row'>\
                      <label for='form_wr1'>Watchdog Reset Pin 1</label>\
                      <input id='form_wr1' type='number' min='1' max='104' \
                             size='8' value='{watchdog_reset_pin_1}'/>\
                    </div>\
                    <div class='row'>\
                      <label for='form_wr2'>Watchdog Reset Pin 2</label>\
                      <input id='form_wr2' type='number' min='1' max='104' \
                             size='8' value='{watchdog_reset_pin_2}'/>\
                    </div>\
                    <div class='row'>\
                      <label for='form_dip'>Dip</label>\
                      <input id='form_dip' type='number' min='0' max='255' \
                             size='8' value='{dip}'/>\
                    </div>"
                )
            }
        }
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

    fn to_html(&self, ct: CardType) -> String {
        let description = HtmlStr(&self.description);
        match ct {
            CardType::Compact => {
                let name = HtmlStr(&self.name);
                format!(
                    "<span>{description}</span>\
                    <span class='{INFO}'>{name}</span>"
                )
            }
            CardType::Form => {
                let timeout_ms = self.timeout_ms;
                let poll_period_sec = self.poll_period_sec;
                let long_poll_period_sec = self.long_poll_period_sec;
                let idle_disconnect_sec = self.idle_disconnect_sec;
                let no_response_disconnect_sec =
                    self.no_response_disconnect_sec;
                format!(
                    "<div class='row'>\
                      <label for='form_description'>Description</label>\
                      <input id='form_description' maxlength='20' size='20' \
                             value='{description}'/>\
                    </div>\
                    <div class='row'>\
                      <label for='form_timeout'>Timeout (ms)</label>\
                      <input id='form_timeout' type='number' min='0' size='8' \
                             max='20000' value='{timeout_ms}'/>\
                    </div>\
                    <div class='row'>\
                      <label for='form_poll_period'>Poll Period (s)</label>\
                      <input id='form_poll_period' type='number' min='0' \
                             size='8' value='{poll_period_sec}'/>\
                    </div>\
                    <div class='row'>\
                      <label for='form_long_poll'>Long Poll Period (s)</label>\
                      <input id='form_long_poll' type='number' min='0' \
                             size='8' value='{long_poll_period_sec}'/>\
                    </div>\
                    <div class='row'>\
                      <label for='form_idle'>Idle Disconnect (s)</label>\
                      <input id='form_idle' type='number' min='0' size='8' \
                             value='{idle_disconnect_sec}'/>\
                    </div>\
                    <div class='row'>\
                      <label for='form_no_resp'>No Response Disconnect (s)\
                      </label>\
                      <input id='form_no_resp' type='number' min='0' size='8' \
                             value='{no_response_disconnect_sec}'/>\
                    </div>"
                )
            }
        }
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

    fn to_html(&self, ct: CardType) -> String {
        let description = HtmlStr(&self.description);
        match ct {
            CardType::Compact => {
                let name = HtmlStr(&self.name);
                let disabled = if self.poll_enabled { "" } else { DISABLED };
                format!(
                    "<span{disabled}>{description}</span>\
                    <span class='{INFO}'>{name}</span>"
                )
            }
            CardType::Form => {
                let uri = HtmlStr(&self.uri);
                let enabled = if self.poll_enabled { " checked" } else { "" };
                let comm_config = HtmlStr(&self.comm_config);
                format!(
                    "<div class='row'>\
                      <label for='form_description'>Description</label>\
                      <input id='form_description' maxlength='32' size='26' \
                             value='{description}'/>\
                    </div>\
                    <div class='row'>\
                      <label for='form_uri'>URI</label>\
                      <input id='form_uri' maxlength='256' size='32' \
                             value='{uri}'/>\
                    </div>\
                    <div class='row'>\
                      <label for='form_enabled'>Poll Enabled</label>\
                      <input id='form_enabled' type='checkbox'{enabled}/>\
                    </div>\
                    <div class='row'>\
                      <label for='form_config'>Comm Config</label>\
                      <input id='form_config' maxlength='10' size='10' \
                             value='{comm_config}'/>\
                    </div>"
                )
            }
        }
    }
}

impl Card for Controller {
    fn is_match(&self, tx: &str) -> bool {
        let comm_link = self.comm_link.to_lowercase();
        comm_link.contains(tx)
            || format!("{}:{}", comm_link, self.drop_id).contains(tx)
            || self.notes.to_lowercase().contains(tx)
    }

    fn name(&self) -> &str {
        &self.name
    }

    fn to_html(&self, ct: CardType) -> String {
        let comm_link = HtmlStr(&self.comm_link);
        let drop_id = self.drop_id;
        match ct {
            CardType::Compact => {
                let name = HtmlStr(&self.name);
                // condition 1 is "Active"
                let disabled = if self.condition == 1 { "" } else { DISABLED };
                format!(
                    "<span{disabled}>{comm_link}:{drop_id}</span>\
                    <span class='{INFO}'>{name}</span>"
                )
            }
            CardType::Form => {
                let cabinet = HtmlStr(&self.cabinet);
                let notes = HtmlStr(&self.notes);
                let password = HtmlStr(self.password.as_ref());
                format!(
                    "<div class='row'>\
                      <label for='form_comm_link'>Comm Link</label>\
                      <input id='form_comm_link' maxlength='20' size='20' \
                             value='{comm_link}'/>\
                    </div>\
                    <div class='row'>\
                      <label for='form_drop_id'>Drop ID</label>\
                      <input id='form_drop_id' type='number' min='0'
                             max='65535' size='6' value='{drop_id}'/>\
                    </div>\
                    <div class='row'>\
                      <label for='form_cabinet'>Cabinet</label>\
                      <input id='form_cabinet' maxlength='20' size='20' \
                             value='{cabinet}'/>\
                    </div>\
                    <div class='row'>\
                      <label for='form_notes'>Notes</label>\
                      <textarea id='form_notes' maxlength='128' rows='2' \
                                cols='28'/>{notes}</textarea>\
                    </div>\
                    <div class='row'>\
                      <label for='form_password'>Password</label>\
                      <input id='form_password' maxlength='32' size='26' \
                             value='{password}'/>\
                    </div>"
                )
            }
        }
    }
}

impl Card for Modem {
    fn is_match(&self, tx: &str) -> bool {
        self.name.to_lowercase().contains(tx)
    }

    fn name(&self) -> &str {
        &self.name
    }

    fn to_html(&self, ct: CardType) -> String {
        match ct {
            CardType::Compact => {
                let name = HtmlStr(&self.name);
                let disabled = if self.enabled { "" } else { DISABLED };
                format!("<span{disabled}>{name}</span>")
            }
            CardType::Form => {
                let uri = HtmlStr(&self.uri);
                let config = HtmlStr(&self.config);
                let timeout_ms = self.timeout_ms;
                let enabled = if self.enabled { " checked" } else { "" };
                format!(
                    "<div class='row'>\
                      <label for='form_uri'>URI</label>\
                      <input id='form_uri' maxlength='64' size='32' \
                             value='{uri}'/>\
                    </div>\
                    <div class='row'>\
                      <label for='form_config'>Config</label>\
                      <input id='form_config' maxlength='64' size='32' \
                             value='{config}'/>\
                    </div>\
                    <div class='row'>\
                      <label for='form_timeout'>Timeout (ms)</label>\
                      <input id='form_timeout' type='number' min='0' size='8' \
                             max='20000' value='{timeout_ms}'/>\
                    </div>\
                    <div class='row'>\
                      <label for='form_enabled'>Enabled</label>\
                      <input id='form_enabled' type='checkbox'{enabled}/>\
                    </div>"
                )
            }
        }
    }
}

#[derive(Default)]
struct State {
    selected: Option<(ObType, String, String)>,
}

static STATE: Lazy<Mutex<State>> = Lazy::new(|| Mutex::new(State::default()));

/// Set global allocator to `wee_alloc`
#[global_allocator]
static ALLOC: wee_alloc::WeeAlloc = wee_alloc::WeeAlloc::INIT;

#[wasm_bindgen(start)]
pub async fn start() -> Result<()> {
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
        opt.append_with_str_1(ob.tname())?;
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
    deselect_card(&doc).unwrap_throw();
    let input: HtmlInputElement = doc.elem("ob_input").unwrap_throw();
    let tx = input.value().to_lowercase();
    let tp: ObType = tp.into();
    spawn_local(tp.populate_cards(tx));
}

/// Handle an event from "ob_input" `input` element
fn handle_search_ev(tx: String) {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    deselect_card(&doc).unwrap_throw();
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
    if let Some(card) = elem.closest(".card").unwrap_throw() {
        if let Some(name) = card.get_attribute("name") {
            deselect_card(&doc).unwrap_throw();
            spawn_local(tp.expand_card(name));
        }
    }
}

fn deselect_card(doc: &Document) -> Result<()> {
    let mut state = STATE.lock().unwrap_throw();
    if let Some((tp, name, html)) = state.selected.take() {
        let id = format!("{}_{}", tp.tname(), &name);
        if let Ok(elem) = doc.elem::<HtmlElement>(&id) {
            elem.set_inner_html(&html);
            elem.set_class_name("card");
        }
    }
    Ok(())
}

#[cfg(test)]
mod test {
    use super::*;

    #[test]
    fn html() {
        assert_eq!(HtmlStr("<").to_string(), "&lt;");
        assert_eq!(HtmlStr(">").to_string(), "&gt;");
        assert_eq!(HtmlStr("&").to_string(), "&amp;");
        assert_eq!(HtmlStr("\"").to_string(), "&quot;");
        assert_eq!(HtmlStr("'").to_string(), "&#x27;");
        assert_eq!(
            HtmlStr("<script>XSS stuff</script>").to_string(),
            "&lt;script&gt;XSS stuff&lt;/script&gt;"
        );
    }
}
