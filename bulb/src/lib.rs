use wasm_bindgen::prelude::*;

#[wasm_bindgen(start)]
pub fn main() -> Result<(), JsValue> {
    let window = web_sys::window().unwrap();
    let document = window.document().unwrap();
    let my_circle = document.get_element_by_id("my_circle").unwrap();
    my_circle.set_attribute("stroke", "black")?;
    let sidebar = document.get_element_by_id("sidebar").unwrap();
    for _ in 0..20 {
        let p = document.create_element("p")?;
        p.set_inner_html("paragraph");
        sidebar.append_child(&p)?;
    }
    Ok(())
}
