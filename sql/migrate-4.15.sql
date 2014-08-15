\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.15.0'
	WHERE name = 'database_version';

DROP VIEW meter_event_view;

ALTER TABLE event.meter_event DROP COLUMN bottleneck;
ALTER TABLE event.meter_event ADD COLUMN wait_secs INTEGER;
UPDATE event.meter_event SET wait_secs = 0;
ALTER TABLE event.meter_event ALTER COLUMN wait_secs SET NOT NULL;

CREATE VIEW meter_event_view AS
	SELECT event_id, event_date, event_description.description,
	       ramp_meter, meter_phase.description AS phase,
	       meter_queue_state.description AS q_state, q_len, dem_adj,
	       wait_secs, meter_limit_control.description AS limit_ctrl,
	       min_rate, rel_rate, max_rate, d_node, seg_density
	FROM event.meter_event
	JOIN event.event_description
	ON meter_event.event_desc_id = event_description.event_desc_id
	JOIN event.meter_phase ON phase = meter_phase.id
	JOIN event.meter_queue_state ON q_state = meter_queue_state.id
	JOIN event.meter_limit_control ON limit_ctrl = meter_limit_control.id;
GRANT SELECT ON meter_event_view TO PUBLIC;
