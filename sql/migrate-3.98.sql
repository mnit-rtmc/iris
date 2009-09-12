\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.98.0' WHERE name = 'database_version';

CREATE TABLE iris._lane_marking (
	name VARCHAR(10) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES iris.geo_loc(name),
	notes VARCHAR(64) NOT NULL
);

ALTER TABLE iris._lane_marking ADD CONSTRAINT _lane_marking_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.lane_marking AS SELECT
	m.name, geo_loc, controller, pin, notes
	FROM iris._lane_marking m JOIN iris._device_io d ON m.name = d.name;

CREATE RULE lane_marking_insert AS ON INSERT TO iris.lane_marking DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._lane_marking VALUES (NEW.name, NEW.geo_loc,NEW.notes);
);

CREATE RULE lane_marking_update AS ON UPDATE TO iris.lane_marking DO INSTEAD
(
	UPDATE iris._device_io SET
		controller = NEW.controller,
		pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._lane_marking SET
		geo_loc = NEW.geo_loc,
		notes = NEW.notes
	WHERE name = OLD.name;
);

CREATE RULE lane_marking_delete AS ON DELETE TO iris.lane_marking DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

CREATE TABLE iris.lane_action (
	name VARCHAR(20) PRIMARY KEY,
	action_plan VARCHAR(16) NOT NULL REFERENCES iris.action_plan,
	lane_marking VARCHAR(10) NOT NULL REFERENCES iris._lane_marking
);
