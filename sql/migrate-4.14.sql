\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.14.0'
	WHERE name = 'database_version';

INSERT INTO iris.system_attribute (name, value) VALUES ('comm_event_purge_days', '14');
INSERT INTO iris.system_attribute (name, value) VALUES ('meter_event_purge_days', '14');


CREATE TABLE event.meter_phase (
	id INTEGER PRIMARY KEY,
	description VARCHAR(16) NOT NULL
);

CREATE TABLE event.meter_queue_state (
	id INTEGER PRIMARY KEY,
	description VARCHAR(16) NOT NULL
);

CREATE TABLE event.meter_limit_control (
	id INTEGER PRIMARY KEY,
	description VARCHAR(16) NOT NULL
);

CREATE TABLE event.meter_event (
	event_id SERIAL PRIMARY KEY,
	event_date timestamp WITH time zone NOT NULL,
	event_desc_id INTEGER NOT NULL
		REFERENCES event.event_description(event_desc_id),
	ramp_meter VARCHAR(10) NOT NULL REFERENCES iris._ramp_meter
		ON DELETE CASCADE,
	phase INTEGER NOT NULL REFERENCES event.meter_phase,
	q_state INTEGER NOT NULL REFERENCES event.meter_queue_state,
	q_len REAL NOT NULL,
	dem_adj REAL NOT NULL,
	limit_ctrl INTEGER NOT NULL REFERENCES event.meter_limit_control,
	min_rate INTEGER NOT NULL,
	rel_rate INTEGER NOT NULL,
	max_rate INTEGER NOT NULL,
	d_node VARCHAR(10),
	bottleneck BOOLEAN NOT NULL,
	seg_density REAL NOT NULL
);

CREATE VIEW meter_event_view AS
	SELECT event_id, event_date, event_description.description,
	       ramp_meter, meter_phase.description AS phase,
	       meter_queue_state.description AS q_state, q_len, dem_adj,
	       meter_limit_control.description AS limit_ctrl,
	       min_rate, rel_rate, max_rate, d_node, bottleneck, seg_density
	FROM event.meter_event
	JOIN event.event_description
	ON meter_event.event_desc_id = event_description.event_desc_id
	JOIN event.meter_phase ON phase = meter_phase.id
	JOIN event.meter_queue_state ON q_state = meter_queue_state.id
	JOIN event.meter_limit_control ON limit_ctrl = meter_limit_control.id;
GRANT SELECT ON meter_event_view TO PUBLIC;

COPY event.meter_phase (id, description) FROM stdin;
0	not started
1	metering
2	flushing
3	stopped
\.

COPY event.meter_queue_state (id, description) FROM stdin;
0	unknown
1	empty
2	exists
3	full
\.

COPY event.meter_limit_control (id, description) FROM stdin;
0	passage fail
1	storage limit
2	wait limit
3	target minimum
\.

INSERT INTO event.event_description (event_desc_id, description)
	VALUES (401, 'Meter event');
