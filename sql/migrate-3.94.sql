\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.94.0' WHERE name = 'database_version';

CREATE TABLE iris.map_extent (
	name VARCHAR(20) PRIMARY KEY,
	easting INTEGER NOT NULL,
	east_span INTEGER NOT NULL,
	northing INTEGER NOT NULL,
	north_span INTEGER NOT NULL
);

CREATE TABLE iris.cabinet_style (
	name VARCHAR(20) PRIMARY KEY,
	dip integer
);

CREATE TABLE iris.cabinet (
	name VARCHAR(20) PRIMARY KEY,
	style VARCHAR(20) REFERENCES iris.cabinet_style(name),
	geo_loc VARCHAR(20) NOT NULL REFERENCES geo_loc(name),
	mile real
);

CREATE TABLE iris.controller (
	name VARCHAR(20) PRIMARY KEY,
	drop_id smallint NOT NULL,
	comm_link VARCHAR(20) NOT NULL REFERENCES comm_link(name),
	cabinet VARCHAR(20) NOT NULL REFERENCES iris.cabinet(name),
	active boolean NOT NULL,
	password VARCHAR(16),
	notes VARCHAR(128) NOT NULL
);

CREATE UNIQUE INDEX ctrl_link_drop_idx ON iris.controller
	USING btree (comm_link, drop_id);

INSERT INTO iris.cabinet_style (name, dip)
	(SELECT name, dip FROM cabinet_style);
INSERT INTO iris.cabinet (name, style, geo_loc, mile)
	(SELECT name, style, geo_loc, mile FROM cabinet);
INSERT INTO iris.controller (name, drop_id, comm_link, cabinet, active, notes)
	(SELECT name, drop_id, comm_link, cabinet, active, notes
	 FROM controller);

ALTER TABLE iris._device_io DROP CONSTRAINT _device_io_controller_fkey;
ALTER TABLE iris._device_io ADD CONSTRAINT _device_io_controller_fkey
	FOREIGN KEY (controller) REFERENCES iris.controller;
ALTER TABLE event.comm_event DROP CONSTRAINT comm_event_controller_fkey;
ALTER TABLE event.comm_event ADD CONSTRAINT comm_event_controller_fkey
	FOREIGN KEY (controller) REFERENCES iris.controller;

DROP VIEW comm_event_view;
DROP VIEW controller_report;
DROP VIEW warning_sign_view;
DROP VIEW controller_view;
DROP VIEW detector_view;
DROP VIEW camera_view;
DROP VIEW controller_loc_view;
DROP VIEW alarm_view;

DROP TABLE controller;
DROP TABLE cabinet;
DROP TABLE cabinet_style;

CREATE VIEW controller_loc_view AS
	SELECT c.name, c.drop_id, c.comm_link, c.cabinet, c.active, c.notes,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir
	FROM iris.controller c
	LEFT JOIN iris.cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name;
GRANT SELECT ON controller_loc_view TO PUBLIC;

CREATE VIEW alarm_view AS
	SELECT a.name, a.description, a.state, a.controller, a.pin, c.comm_link,
		c.drop_id
	FROM iris.alarm a LEFT JOIN iris.controller c ON a.controller = c.name;
GRANT SELECT ON alarm_view TO PUBLIC;

CREATE VIEW detector_view AS
	SELECT d.name AS det_id, d.r_node, d.controller, c.comm_link, c.drop_id,
	d.pin, detector_label(l.fwy, l.fdir, l.xst, l.cross_dir, l.xmod,
		d.lane_type, d.lane_number, d.abandoned) AS label,
	rnd.geo_loc, l.freeway, l.free_dir, l.cross_mod, l.cross_street,
	l.cross_dir, d.lane_number, d.field_length, ln.description AS lane_type,
	boolean_converter(d.abandoned) AS abandoned,
	boolean_converter(d.force_fail) AS force_fail,
	boolean_converter(c.active) AS active, d.fake, d.notes
	FROM iris.detector d
	LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
	LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name
	LEFT JOIN lane_type ln ON d.lane_type = ln.id
	LEFT JOIN iris.controller c ON d.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

CREATE VIEW controller_view AS
	SELECT c.name, drop_id, comm_link, cabinet, active, notes, cab.geo_loc
	FROM iris.controller c
	JOIN iris.cabinet cab ON c.cabinet = cab.name;
GRANT SELECT ON controller_view TO PUBLIC;

CREATE VIEW camera_view AS
	SELECT c.name, c.notes, c.encoder, c.encoder_channel, c.nvr, c.publish,
	c.geo_loc, l.freeway, l.free_dir, l.cross_mod, l.cross_street,
	l.cross_dir, l.easting, l.northing, l.east_off, l.north_off,
	c.controller, ctr.comm_link, ctr.drop_id, ctr.active
	FROM iris.camera c
	JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN iris.controller ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;

CREATE VIEW warning_sign_view AS
	SELECT w.name, w.notes, w.message, w.camera, w.geo_loc,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing, l.east_off, l.north_off,
	w.controller, w.pin, ctr.comm_link, ctr.drop_id, ctr.active
	FROM iris.warning_sign w
	LEFT JOIN geo_loc_view l ON w.geo_loc = l.name
	LEFT JOIN iris.controller ctr ON w.controller = ctr.name;
GRANT SELECT ON warning_sign_view TO PUBLIC;

CREATE VIEW controller_report AS
	SELECT c.name, c.comm_link, c.drop_id, cab.mile, cab.geo_loc,
	trim(l.freeway || ' ' || l.free_dir) || ' ' || l.cross_mod || ' ' ||
		trim(l.cross_street || ' ' || l.cross_dir) AS "location",
	cab.style AS "type", d.name AS device, d.pin,
	d.cross_street AS cross_street, d.freeway AS freeway, c.notes
	FROM iris.controller c
	LEFT JOIN iris.cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name
	LEFT JOIN controller_device_view d ON d.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

CREATE VIEW comm_event_view AS
	SELECT e.event_id, e.event_date, ed.description,
		e.controller, c.comm_link, c.drop_id
	FROM event.comm_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
	LEFT JOIN iris.controller c ON e.controller = c.name;
GRANT SELECT ON comm_event_view TO PUBLIC;
