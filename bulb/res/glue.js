// Set the selected resource
export function js_set_selected(res, name) {
  selected_resource = res;
  selected_name = name;
  if (name) {
    select_tms_feature(name, name);
  } else {
    select_tms_feature(null, null);
  }
}

// Update TMS main item states
export function js_update_item_states(data) {
  if (item_states) {
    item_states = { ...item_states, ...JSON.parse(data) };
  } else {
    item_states = JSON.parse(data);
  }
}

// Redraw the map
export function js_redraw_map() {
  tms_layers.redraw();
}

// Update station data JSON and redraw the map
export function js_update_stat_sample(data) {
  stat_sample = data;
  tms_layers.redraw();
}

// Fly map to given item
export function js_fly_map_to(fid, lat, lng) {
  if (fly_enabled) {
    select_tms_feature(fid, fid);
    map.flyTo([lat, lng]);
  }
}

// Enable/disable flying map
export function js_fly_enable(enable) {
  fly_enabled = enable;
}
