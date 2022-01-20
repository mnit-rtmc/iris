use serde::de::DeserializeOwned;
use serde::{Deserialize, Serialize};
use wasm_bindgen::prelude::*;
use wasm_bindgen::JsCast;
use wasm_bindgen_futures::JsFuture;
use web_sys::{console, Event, HtmlSelectElement, Request, Response, Window};

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

/// Fetch a JSON array and deserialize into a Vec
async fn fetch_json_vec<T>(
    window: &Window,
    url: &str,
) -> Result<Vec<T>, JsValue>
where
    T: DeserializeOwned,
{
    let req = Request::new_with_str(url)?;
    req.headers().set("Accept", "application/json")?;
    let resp = JsFuture::from(window.fetch_with_request(&req)).await?;
    let resp: Response = resp.dyn_into().unwrap();
    let json = JsFuture::from(resp.json()?).await?;
    let configs: Vec<T> = json.into_serde().unwrap();
    Ok(configs)
}

/// Object types
const OB_TYPES: &[&str] = &["", "Comm Config", "Comm Link", "Controller"];

#[wasm_bindgen(start)]
pub async fn main() -> Result<(), JsValue> {
    let window = web_sys::window().unwrap();
    let document = window.document().unwrap();
    let ob_list = document.get_element_by_id("ob_list").unwrap();
    let ob_type = document.get_element_by_id("ob_type").unwrap();

    for ob in OB_TYPES {
        let opt = document.create_element("option")?;
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
        console::log_1(&value.into());
    }) as Box<dyn FnMut(_)>);

    ob_type.add_event_listener_with_callback(
        "input",
        &cb.as_ref().unchecked_ref(),
    )?;

    cb.forget();

    let configs: Vec<CommConfig> =
        fetch_json_vec(&window, "/iris/api/comm_config").await?;

    let cards = document.create_element("ul")?;
    cards.set_class_name("cards");
    for config in configs {
        let card = document.create_element("li")?;
        card.set_class_name("card");
        let name = document.create_element("span")?;
        name.set_class_name("name");
        name.set_inner_html(&config.description);
        card.append_child(&name)?;
        let info = document.create_element("span")?;
        info.set_class_name("info");
        info.set_inner_html(&config.name);
        card.append_child(&info)?;
        cards.append_child(&card)?;
    }
    ob_list.append_child(&cards)?;
    Ok(())
}
