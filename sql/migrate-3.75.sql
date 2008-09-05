\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

CREATE TEMP TABLE temp_alarm (
	name VARCHAR(10),
	description VARCHAR(24),
	controller VARCHAR(20),
	pin integer
);

CREATE TEMP SEQUENCE __a_seq MINVALUE 1;
INSERT INTO temp_alarm (name, description, controller, pin)
	(SELECT 'A' || trim(both FROM to_char(nextval('__a_seq'), '009')),
		notes::VARCHAR(24), controller, pin FROM alarm);

DROP TABLE alarm;

CREATE TABLE alarm (
	name VARCHAR(10) PRIMARY KEY,
	description VARCHAR(24) NOT NULL,
	controller VARCHAR(20) REFERENCES controller(name),
	pin integer NOT NULL,
	state BOOLEAN NOT NULL
);

INSERT INTO alarm (name, description, controller, pin, state)
	(SELECT name, description, controller, pin, false FROM temp_alarm);

CREATE VIEW alarm_view AS
	SELECT a.name, a.description, a.state, a.controller, a.pin, c.comm_link,
		c.drop_id
	FROM alarm a LEFT JOIN controller c ON a.controller = c.name;
GRANT SELECT ON alarm_view TO PUBLIC;

DELETE FROM vault_types WHERE "table" = 'alarm';

SET search_path = event, public, pg_catalog;

CREATE TABLE event.alarm_event (
	event_id integer PRIMARY KEY DEFAULT nextval('event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	alarm VARCHAR(10) NOT NULL REFERENCES alarm(name)
		ON DELETE CASCADE
);

INSERT INTO event.event_description (event_desc_id, description)
	VALUES (1, 'Alarm TRIGGERED');
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (2, 'Alarm CLEARED');

SET search_path = public, pg_catalog;

CREATE VIEW alarm_event_view AS
	SELECT e.event_id, e.event_date, ed.description AS event_description,
		e.alarm, a.description
	FROM event.alarm_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
	JOIN alarm a ON e.alarm = a.name;
GRANT SELECT ON alarm_event_view TO PUBLIC;

DROP VIEW controller_report;
DROP VIEW controller_device_view;
DROP VIEW camera_view;
DROP VIEW detector_view;
DROP VIEW dms_view;
DROP VIEW ramp_meter_view;
DROP VIEW device_loc_view;

REVOKE ALL ON controller FROM PUBLIC;
REVOKE ALL ON device FROM PUBLIC;
REVOKE ALL ON traffic_device FROM PUBLIC;
REVOKE ALL ON camera FROM PUBLIC;
REVOKE ALL ON detector FROM PUBLIC;
REVOKE ALL ON dms FROM PUBLIC;
REVOKE ALL ON ramp_meter FROM PUBLIC;

CREATE VIEW camera_view AS
	SELECT c.name, c.notes, c.encoder, c.encoder_channel, c.nvr, c.publish,
	c.geo_loc, l.freeway, l.free_dir, l.cross_mod, l.cross_street,
	l.cross_dir, l.easting, l.northing, l.east_off, l.north_off,
	c.controller, ctr.comm_link, ctr.drop_id, ctr.active
	FROM camera c
	JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN controller ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

CREATE VIEW controller_device_view AS
	SELECT d.id, d.controller, d.pin, d.geo_loc,
	trim(l.freeway || ' ' || l.free_dir) AS freeway,
	trim(trim(' @' FROM l.cross_mod || ' ' || l.cross_street)
		|| ' ' || l.cross_dir) AS cross_street
	FROM traffic_device d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON controller_device_view TO PUBLIC;

CREATE VIEW controller_report AS
	SELECT c.name, c.comm_link, c.drop_id, cab.mile, cab.geo_loc,
	trim(l.freeway || ' ' || l.free_dir) || ' ' || l.cross_mod || ' ' ||
		trim(l.cross_street || ' ' || l.cross_dir) AS "location",
	cab.style AS "type", d1.id AS "id (meter1)",
	d1.cross_street AS "from (meter1)", d1.freeway AS "to (meter1)",
	d2.id AS meter2, d2.cross_street AS "from (meter2)",
	d2.freeway AS "to (meter2)", c.notes
	FROM controller c
	LEFT JOIN cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name
	LEFT JOIN controller_device_view d1 ON
		d1.pin = 2 AND d1.controller = c.name
	LEFT JOIN controller_device_view d2 ON
		d2.pin = 3 AND d2.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

CREATE VIEW detector_view AS
	SELECT d."index" AS det_id, d.controller, c.comm_link, c.drop_id, d.pin,
	detector_label(l.fwy, l.fdir, l.xst, l.cross_dir, l.xmod,
		d."laneType", d."laneNumber", d.abandoned) AS label,
	d.geo_loc, l.freeway, l.free_dir, l.cross_mod, l.cross_street,
	l.cross_dir, d."laneNumber" AS lane_number,
	d."fieldLength" AS field_length, ln.description AS lane_type,
	boolean_converter(d.abandoned) AS abandoned,
	boolean_converter(d."forceFail") AS force_fail,
	boolean_converter(c.active) AS active, d.fake, d.notes
	FROM detector d
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN lane_type ln ON d."laneType" = ln.id
	LEFT JOIN controller c ON d.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

CREATE VIEW device_loc_view AS
	SELECT d.vault_oid, d.controller, d.geo_loc,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir
	FROM device d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON device_loc_view TO PUBLIC;

CREATE VIEW dms_view AS
	SELECT d.id, d.notes, d.camera, d.mile, d.travel, d.geo_loc,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.east_off, l.northing, l.north_off, d.controller
	FROM dms d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON dms_view TO PUBLIC;

CREATE VIEW ramp_meter_view AS
	SELECT m.vault_oid, m.id, m.notes,
	m."controlMode" AS control_mode, m."singleRelease" AS single_release,
	m."storage", m."maxWait" AS max_wait, m.camera, m.geo_loc,
	l.fwy, l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing, l.east_off, l.north_off, m.controller
	FROM ramp_meter m
	JOIN geo_loc_view l ON m.geo_loc = l.name;
GRANT SELECT ON ramp_meter_view TO PUBLIC;

CREATE VIEW controller_view AS
	SELECT c.name, drop_id, comm_link, cabinet, active, notes, cab.geo_loc
	FROM controller c
	JOIN cabinet cab ON c.cabinet = cab.name;
GRANT SELECT ON controller_view TO PUBLIC;
