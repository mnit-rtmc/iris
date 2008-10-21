\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

CREATE UNIQUE INDEX r_node_type_pkey ON r_node_type USING btree (n_type);
CREATE UNIQUE INDEX r_node_transition_pkey ON r_node_transition
	USING btree (n_transition);

CREATE TABLE iris.r_node (
	name VARCHAR(10) PRIMARY KEY,
	geo_loc VARCHAR(20) NOT NULL REFERENCES geo_loc(name),
	node_type integer NOT NULL REFERENCES r_node_type(n_type),
	pickable boolean NOT NULL,
	transition integer NOT NULL REFERENCES r_node_transition(n_transition),
	lanes integer NOT NULL,
	attach_side boolean NOT NULL,
	shift integer NOT NULL,
	station_id VARCHAR(10),
	speed_limit integer NOT NULL,
	notes text NOT NULL
);

CREATE TABLE iris._detector (
	name VARCHAR(10) PRIMARY KEY,
	r_node VARCHAR(10) REFERENCES iris.r_node(name),
	lane_type smallint NOT NULL REFERENCES lane_type(id),
	lane_number smallint NOT NULL,
	abandoned boolean NOT NULL,
	force_fail boolean NOT NULL,
	field_length real NOT NULL,
	fake VARCHAR(32),
	notes VARCHAR(32)
);

ALTER TABLE iris._detector ADD CONSTRAINT _detector_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.detector AS SELECT
	det.name, controller, pin, r_node, lane_type, lane_number, abandoned,
	force_fail, field_length, fake, notes
	FROM iris._detector det JOIN iris._device_io d ON det.name = d.name;

CREATE RULE detector_insert AS ON INSERT TO iris.detector DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._detector VALUES (NEW.name, NEW.r_node, NEW.lane_type,
		NEW.lane_number, NEW.abandoned, NEW.force_fail,
		NEW.field_length, NEW.fake, NEW.notes);
);

CREATE RULE detector_update AS ON UPDATE TO iris.detector DO INSTEAD
(
	UPDATE iris._device_io SET
		controller = NEW.controller,
		pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._detector SET
		r_node = NEW.r_node,
		lane_type = NEW.lane_type,
		lane_number = NEW.lane_number,
		abandoned = NEW.abandoned,
		force_fail = NEW.force_fail,
		field_length = NEW.field_length,
		fake = NEW.fake,
		notes = NEW.notes
	WHERE name = OLD.name;
);

CREATE RULE detector_delete AS ON DELETE TO iris.detector DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

INSERT INTO iris.r_node SELECT 'rnd_' || vault_oid, geo_loc, node_type,
	pickable, transition, lanes, attach_side, shift, station_id,
	speed_limit, notes FROM r_node;

UPDATE iris.r_node SET station_id = NULL WHERE station_id = '';

CREATE UNIQUE INDEX r_node_station_idx ON iris.r_node USING btree (station_id);

UPDATE detector SET controller = NULL WHERE controller = '';

INSERT INTO iris.detector SELECT "index"::VARCHAR(10), controller, pin,
	'rnd_' || rnd.r_node, "laneType", "laneNumber", abandoned, "forceFail",
	"fieldLength", fake, notes::VARCHAR(32)
	FROM detector d
	LEFT JOIN r_node_detector rnd ON d."index" = rnd.detector;

DELETE FROM geo_loc WHERE name IN (SELECT geo_loc FROM detector);

DROP VIEW r_node_view;
DROP VIEW freeway_station_view;
DROP TABLE r_node_detector;
DROP TABLE r_node;
DROP VIEW detector_view;
DROP VIEW detector_event_view;
DROP VIEW detector_label_view;
DROP FUNCTION detector_fieldlength_log;

ALTER TABLE event.detector_event ADD COLUMN did VARCHAR(10);
UPDATE event.detector_event SET did = device_id::VARCHAR(10);
ALTER TABLE event.detector_event DROP COLUMN device_id;
ALTER TABLE event.detector_event ADD COLUMN device_id VARCHAR(10);
UPDATE event.detector_event SET device_id = did;
ALTER TABLE event.detector_event DROP COLUMN did;
ALTER TABLE event.detector_event ADD CONSTRAINT _detector_event_device_id_fkey
	FOREIGN KEY (device_id) REFERENCES iris._detector(name);

DROP TABLE detector;

CREATE VIEW r_node_view AS
	SELECT n.name, freeway, free_dir, cross_mod, cross_street,
	cross_dir, nt.name AS node_type, n.pickable, tr.name AS transition,
	n.lanes, n.attach_side, n.shift, n.station_id, n.speed_limit, n.notes
	FROM iris.r_node n
	JOIN geo_loc_view l ON n.geo_loc = l.name
	JOIN r_node_type nt ON n.node_type = nt.n_type
	JOIN r_node_transition tr ON n.transition = tr.n_transition;
GRANT SELECT ON r_node_view TO PUBLIC;

CREATE VIEW freeway_station_view AS
	SELECT station_id, freeway, free_dir, cross_mod, cross_street,
	speed_limit
	FROM iris.r_node r, geo_loc_view l
	WHERE r.geo_loc = l.name AND station_id IS NOT NULL;
GRANT SELECT ON freeway_station_view TO PUBLIC;

CREATE VIEW detector_label_view AS
	SELECT d.name AS det_id,
	detector_label(l.fwy, l.fdir, l.xst, l.cross_dir, l.xmod,
		d.lane_type, d.lane_number, d.abandoned) AS label
	FROM iris.detector d
	LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
	LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name;
GRANT SELECT ON detector_label_view TO PUBLIC;

CREATE VIEW detector_event_view AS
	SELECT e.event_id, e.event_date, ed.description, e.device_id, dl.label
	FROM event.detector_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
	JOIN detector_label_view dl ON e.device_id = dl.det_id;
GRANT SELECT ON detector_event_view TO PUBLIC;

CREATE VIEW detector_view AS
	SELECT d.name AS det_id, d.controller, c.comm_link, c.drop_id, d.pin,
	detector_label(l.fwy, l.fdir, l.xst, l.cross_dir, l.xmod,
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
	LEFT JOIN controller c ON d.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

UPDATE system_attribute SET value = '3.79.0' WHERE name = 'database_version';
