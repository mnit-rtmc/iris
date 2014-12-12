\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.20.0'
	WHERE name = 'database_version';

-- add milepoint column to geo_loc
ALTER TABLE iris.geo_loc ADD COLUMN milepoint VARCHAR(16);

-- copy cabinet mile data to geo_loc milepoint
UPDATE iris.geo_loc AS gl
	SET milepoint = c.mile
	FROM iris.cabinet AS c
	WHERE gl.name = c.geo_loc;

-- redefine cabinet_view without cabinet.mile
DROP VIEW cabinet_view;
CREATE VIEW cabinet_view AS
	SELECT name, style, geo_loc
	FROM iris.cabinet;
GRANT SELECT ON cabinet_view TO PUBLIC;

-- add milepoint to geo_loc_view
CREATE OR REPLACE VIEW geo_loc_view AS
	SELECT l.name, r.abbrev AS rd, l.roadway,
	r_dir.direction AS road_dir, r_dir.dir AS rdir,
	m.modifier AS cross_mod, m.mod AS xmod, c.abbrev as xst,
	l.cross_street, c_dir.direction AS cross_dir,
	l.lat, l.lon, l.milepoint
	FROM iris.geo_loc l
	LEFT JOIN iris.road r ON l.roadway = r.name
	LEFT JOIN iris.road_modifier m ON l.cross_mod = m.id
	LEFT JOIN iris.road c ON l.cross_street = c.name
	LEFT JOIN iris.direction r_dir ON l.road_dir = r_dir.id
	LEFT JOIN iris.direction c_dir ON l.cross_dir = c_dir.id;
GRANT SELECT ON geo_loc_view TO PUBLIC;

-- redefine controller_report with geo_loc.milepoint instead of cabinet.mile
DROP VIEW controller_report;
CREATE VIEW controller_report AS
	SELECT c.name, c.comm_link, c.drop_id, l.milepoint, cab.geo_loc,
	trim(l.roadway || ' ' || l.road_dir) || ' ' || l.cross_mod || ' ' ||
		trim(l.cross_street || ' ' || l.cross_dir) AS "location",
	cab.style AS "type", d.name AS device, d.pin,
	d.cross_loc, d.corridor, c.notes
	FROM iris.controller c
	LEFT JOIN iris.cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name
	LEFT JOIN controller_device_view d ON d.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

-- drop mile column from cabinet
ALTER TABLE iris.cabinet DROP COLUMN mile;

-- add dms_quickmsg_store_enable system attribute
INSERT INTO iris.system_attribute (name, value)
	VALUES ('dms_quickmsg_store_enable', false);

-- add beacon events
CREATE TABLE event.beacon_event (
	event_id SERIAL PRIMARY KEY,
	event_date timestamp WITH time zone NOT NULL,
	event_desc_id INTEGER NOT NULL
		REFERENCES event.event_description(event_desc_id),
	beacon VARCHAR(10) NOT NULL REFERENCES iris._beacon
		ON DELETE CASCADE
);

-- add beacon_event_view
CREATE VIEW beacon_event_view AS
	SELECT event_id, event_date, event_description.description, beacon
	FROM event.beacon_event
	JOIN event.event_description
	ON beacon_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON beacon_event_view TO PUBLIC;

-- added beacon event descriptions
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (501, 'Beacon ON');
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (502, 'Beacon OFF');
