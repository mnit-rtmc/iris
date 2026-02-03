// Update station data JSON
export function update_stat_sample(data) {
  stat_sample = data;
  tms_layers.redraw();
}

// Update TMS main item states
export function update_item_states(res, data) {
  selected_resource = res;
  if (item_states) {
    item_states = { ...item_states, ...JSON.parse(data) };
  } else {
    item_states = JSON.parse(data);
  }
  tms_layers.redraw();
}

// Fly map to given item
export function fly_map_to(fid, lat, lng) {
  if (fly_enabled) {
    select_tms_feature(fid, fid);
    map.flyTo([lat, lng]);
  }
}

// Enable/disable flying map
export function fly_enable(enable) {
  fly_enabled = enable;
}
