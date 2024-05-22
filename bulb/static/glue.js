// Update station data JSON
export function update_stat_sample(data) {
  stat_sample = data;
  tms_layers.redraw();
}

// Update TMS main item states
export function update_item_states(data) {
  item_states = JSON.parse(data);
  tms_layers.redraw();
}
