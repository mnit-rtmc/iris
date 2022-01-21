use serde::de::DeserializeOwned;
use serde::{Deserialize, Serialize};
use wasm_bindgen::prelude::*;
use wasm_bindgen::JsCast;
use wasm_bindgen_futures::{spawn_local, JsFuture};
use web_sys::{
    console, Document, Element, Event, HtmlSelectElement, Request, Response,
    Window,
};

trait Card: DeserializeOwned {
    const TITLE: &'static str = "title";
    const DISABLED: &'static str = "title disabled";
    const INFO: &'static str = "info";
    const OB_TYPE: &'static str;
    const URI: &'static str;

    fn make_card(&self, doc: &Document) -> Result<Element, JsValue>;
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

/// Modem
#[derive(Debug, Deserialize, Serialize)]
struct Modem {
    pub name: String,
    pub uri: String,
    pub config: String,
    pub timeout_ms: u32,
    pub enabled: bool,
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

impl Card for () {
    const OB_TYPE: &'static str = "";
    const URI: &'static str = "";

    fn make_card(&self, _doc: &Document) -> Result<Element, JsValue> {
        unreachable!()
    }
}

impl Card for CommConfig {
    const OB_TYPE: &'static str = "Comm Config";
    const URI: &'static str = "/iris/api/comm_config";

    fn make_card(&self, doc: &Document) -> Result<Element, JsValue> {
        let card = doc.create_element("li")?;
        card.set_class_name("card");
        let title = doc.create_element("span")?;
        title.set_class_name(CommConfig::TITLE);
        title.set_inner_html(&self.description);
        card.append_child(&title)?;
        let info = doc.create_element("span")?;
        info.set_class_name(CommConfig::INFO);
        info.set_inner_html(&self.name);
        card.append_child(&info)?;
        Ok(card)
    }
}

impl Card for CommLink {
    const OB_TYPE: &'static str = "Comm Link";
    const URI: &'static str = "/iris/api/comm_link";

    fn make_card(&self, doc: &Document) -> Result<Element, JsValue> {
        let card = doc.create_element("li")?;
        card.set_class_name("card");
        let title = doc.create_element("span")?;
        if self.poll_enabled {
            title.set_class_name(CommLink::TITLE);
        } else {
            title.set_class_name(CommLink::DISABLED);
        }
        title.set_inner_html(&self.description);
        card.append_child(&title)?;
        let info = doc.create_element("span")?;
        info.set_class_name(CommLink::INFO);
        info.set_inner_html(&self.name);
        card.append_child(&info)?;
        Ok(card)
    }
}

impl Card for Modem {
    const OB_TYPE: &'static str = "Modem";
    const URI: &'static str = "/iris/api/modem";

    fn make_card(&self, doc: &Document) -> Result<Element, JsValue> {
        let card = doc.create_element("li")?;
        card.set_class_name("card");
        let title = doc.create_element("span")?;
        if self.enabled {
            title.set_class_name(Modem::TITLE);
        } else {
            title.set_class_name(Modem::DISABLED);
        }
        title.set_inner_html(&self.name);
        card.append_child(&title)?;
        Ok(card)
    }
}

impl Card for CabinetStyle {
    const OB_TYPE: &'static str = "Cabinet Style";
    const URI: &'static str = "/iris/api/cabinet_style";

    fn make_card(&self, doc: &Document) -> Result<Element, JsValue> {
        let card = doc.create_element("li")?;
        card.set_class_name("card");
        let title = doc.create_element("span")?;
        title.set_class_name(CabinetStyle::TITLE);
        title.set_inner_html(&self.name);
        card.append_child(&title)?;
        Ok(card)
    }
}

impl Card for Controller {
    const OB_TYPE: &'static str = "Controller";
    const URI: &'static str = "/iris/api/controller";

    fn make_card(&self, doc: &Document) -> Result<Element, JsValue> {
        let card = doc.create_element("li")?;
        card.set_class_name("card");
        let title = doc.create_element("span")?;
        // condition 1 is "Active"
        if self.condition == 1 {
            title.set_class_name(Controller::TITLE);
        } else {
            title.set_class_name(Controller::DISABLED);
        }
        title.set_inner_html(&format!("{}:{}", self.comm_link, self.drop_id));
        card.append_child(&title)?;
        let info = doc.create_element("span")?;
        info.set_class_name(Controller::INFO);
        info.set_inner_html(&self.name);
        card.append_child(&info)?;
        Ok(card)
    }
}

/// Object types
const OB_TYPES: &[&str] = &[
    <()>::OB_TYPE,
    CommConfig::OB_TYPE,
    CommLink::OB_TYPE,
    Modem::OB_TYPE,
    CabinetStyle::OB_TYPE,
    Controller::OB_TYPE,
];

/// Fetch a JSON array and deserialize into a Vec
async fn fetch_json_vec<C: Card>(window: &Window) -> Result<Vec<C>, JsValue> {
    let req = Request::new_with_str(C::URI)?;
    req.headers().set("Accept", "application/json")?;
    let resp = JsFuture::from(window.fetch_with_request(&req)).await?;
    let resp: Response = resp.dyn_into().unwrap_throw();
    let json = JsFuture::from(resp.json()?).await?;
    let obs: Vec<C> = json.into_serde().unwrap_throw();
    Ok(obs)
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

    for ob in OB_TYPES {
        let opt = doc.create_element("option")?;
        opt.append_with_str_1(ob)?;
        ob_type.append_child(&opt)?;
    }

    let cb = Closure::wrap(Box::new(|e: Event| {
        let value = e
            .current_target()
            .unwrap()
            .dyn_into::<HtmlSelectElement>()
            .unwrap()
            .value();
        populate_list_str(&value);
    }) as Box<dyn FnMut(_)>);

    ob_type.add_event_listener_with_callback(
        "input",
        cb.as_ref().unchecked_ref(),
    )?;
    cb.forget();

    Ok(())
}

fn populate_list_str(value: &str) {
    match value {
        CommConfig::OB_TYPE => spawn_local(populate_list::<CommConfig>()),
        CommLink::OB_TYPE => spawn_local(populate_list::<CommLink>()),
        Modem::OB_TYPE => spawn_local(populate_list::<Modem>()),
        CabinetStyle::OB_TYPE => spawn_local(populate_list::<CabinetStyle>()),
        Controller::OB_TYPE => spawn_local(populate_list::<Controller>()),
        _ => spawn_local(populate_list::<()>()),
    }
    console::log_1(&value.into());
}

async fn populate_list<C: Card>() {
    populate_list_a::<C>().await.unwrap();
}

async fn populate_list_a<C: Card>() -> Result<(), JsValue> {
    let window = web_sys::window().unwrap_throw();
    let doc = window.document().unwrap_throw();
    let ob_list = doc.get_element_by_id("ob_list").unwrap_throw();
    let list = ob_list.clone_node()?;
    if !C::URI.is_empty() {
        let cards = doc.create_element("ul")?;
        cards.set_class_name("cards");
        let obs: Vec<C> = fetch_json_vec(&window).await?;
        for ob in obs {
            cards.append_child(&*ob.make_card(&doc)?)?;
        }
        list.append_child(&cards)?;
    }
    ob_list.replace_with_with_node_1(&list)?;
    Ok(())
}
