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
        let name = doc.create_element("span")?;
        name.set_class_name("name");
        name.set_inner_html(&self.description);
        card.append_child(&name)?;
        let info = doc.create_element("span")?;
        info.set_class_name("info");
        info.set_inner_html(&self.name);
        card.append_child(&info)?;
        Ok(card)
    }
}

/// Fetch a JSON array and deserialize into a Vec
async fn fetch_json_vec<C: Card>(window: &Window) -> Result<Vec<C>, JsValue> {
    let req = Request::new_with_str(C::URI)?;
    req.headers().set("Accept", "application/json")?;
    let resp = JsFuture::from(window.fetch_with_request(&req)).await?;
    let resp: Response = resp.dyn_into().unwrap();
    let json = JsFuture::from(resp.json()?).await?;
    let obs: Vec<C> = json.into_serde().unwrap();
    Ok(obs)
}

/// Object types
const OB_TYPES: &[&str] = &["", "Comm Config", "Comm Link", "Controller"];

#[wasm_bindgen(start)]
pub async fn main() -> Result<(), JsValue> {
    let window = web_sys::window().unwrap();
    let doc = window.document().unwrap();
    let ob_type = doc.get_element_by_id("ob_type").unwrap();

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
        &cb.as_ref().unchecked_ref(),
    )?;
    cb.forget();

    Ok(())
}

fn populate_list_str(value: &str) {
    match value {
        "Comm Config" => spawn_local(populate_list::<CommConfig>()),
        _ => spawn_local(populate_list::<()>()),
    }
    console::log_1(&value.into());
}

async fn populate_list<C: Card>() {
    populate_list_a::<C>().await.unwrap();
}

async fn populate_list_a<C: Card>() -> Result<(), JsValue> {
    let window = web_sys::window().unwrap();
    let doc = window.document().unwrap();
    let ob_list = doc.get_element_by_id("ob_list").unwrap();
    let list = ob_list.clone_node()?;
    if !C::URI.is_empty() {
        let obs: Vec<C> = fetch_json_vec(&window).await?;
        let cards = doc.create_element("ul")?;
        cards.set_class_name("cards");
        for ob in obs {
            cards.append_child(&*ob.make_card(&doc)?)?;
        }
        list.append_child(&cards)?;
    }
    ob_list.replace_with_with_node_1(&list)?;
    Ok(())
}
