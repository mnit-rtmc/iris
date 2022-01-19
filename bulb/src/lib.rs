use serde::de::DeserializeOwned;
use serde::{Deserialize, Serialize};
use wasm_bindgen::prelude::*;
use wasm_bindgen::JsCast;
use wasm_bindgen_futures::JsFuture;
use web_sys::{Request, Response};

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

async fn fetch_json_vec<T>(url: &str) -> Result<Vec<T>, JsValue>
where
    T: DeserializeOwned,
{
    let window = web_sys::window().unwrap();
    let req = Request::new_with_str(&url)?;
    req.headers().set("Accept", "application/json")?;
    let resp = JsFuture::from(window.fetch_with_request(&req)).await?;
    let resp: Response = resp.dyn_into().unwrap();
    let json = JsFuture::from(resp.json()?).await?;
    let configs: Vec<T> = json.into_serde().unwrap();
    Ok(configs)
}

#[wasm_bindgen(start)]
pub async fn main() -> Result<(), JsValue> {
    let window = web_sys::window().unwrap();
    let document = window.document().unwrap();
    let sidebar = document.get_element_by_id("sidebar").unwrap();

    let configs: Vec<CommConfig> =
        fetch_json_vec("/iris/api/comm_config").await?;

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
    sidebar.append_child(&cards)?;
    Ok(())
}
