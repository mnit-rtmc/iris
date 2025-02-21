use ntcip::dms::{Dms, FontTable, GraphicTable};
use rendzina::{SignConfig, load_font, load_graphic};
use std::fs::File;
use std::io::BufWriter;

const SIGN_CFGS: &'static str = r#"[{
  "name":"sc_150x56_1",
  "face_width":5696,
  "face_height":2348,
  "border_horiz":300,
  "border_vert":377,
  "pitch_horiz":33,
  "pitch_vert":33,
  "pixel_width":150,
  "pixel_height":56,
  "char_width":0,
  "char_height":0,
  "monochrome_foreground":0,
  "monochrome_background":0,
  "color_scheme":"color24Bit",
  "default_font":13,
  "module_width":null,
  "module_height":null
}]"#;

const MULTI: &'static str = "\
[g24,1,6]\
[cr1,38,150,1,255,255,255]\
[cf255,255,255]\
[tr70,8,80,30]\
[fo8]\
[jl2]100[jl4]$0.50[nl8]\
[jl2]94[jl4]$0.75\
[tr1,44,150,10]\
[jl3]HOV 2+ FREE";

fn main() {
    let configs = SignConfig::load_all(SIGN_CFGS.as_bytes()).unwrap();
    let mut fonts = FontTable::default();
    let f = fonts.font_mut(0).unwrap();
    *f = load_font(&include_bytes!("../../tfon/F08.tfon")[..]).unwrap();
    let mut graphics = GraphicTable::default();
    let g = graphics.graphic_mut(0).unwrap();
    *g = load_graphic(&include_bytes!("g24.gif")[..], 24).unwrap();
    let config = configs.get("sc_150x56_1").unwrap();
    let dms = Dms::builder()
        .with_font_definition(fonts)
        .with_graphic_definition(graphics)
        .with_sign_cfg(config.sign_cfg())
        .with_vms_cfg(config.vms_cfg())
        .with_multi_cfg(config.multi_cfg())
        .build()
        .unwrap();
    let file = File::create("render.gif").unwrap();
    let writer = BufWriter::new(file);
    rendzina::render(writer, &dms, MULTI, 240, 80, None).unwrap();
}
