\set ON_ERROR_STOP

CREATE SCHEMA iris;
ALTER SCHEMA iris OWNER TO tms;

SET SESSION AUTHORIZATION 'tms';

SELECT * INTO TEMP temp_alarm FROM alarm;
SELECT * INTO TEMP temp_camera FROM camera;
SELECT * INTO temp temp_alarm_event FROM event.alarm_event;

DROP VIEW alarm_view;
DROP VIEW alarm_event_view;
DROP TABLE event.alarm_event;
DROP TABLE alarm;
DROP VIEW camera_view;
DROP TABLE camera;

CREATE TABLE iris._device_io (
	name VARCHAR(10) PRIMARY KEY,
	controller VARCHAR(20) REFERENCES controller(name),
	pin integer NOT NULL
);

CREATE UNIQUE INDEX _device_io_ctrl_pin ON iris._device_io
	USING btree (controller, pin);

CREATE TABLE iris._alarm (
	name VARCHAR(10) PRIMARY KEY,
	description VARCHAR(24) NOT NULL,
	state BOOLEAN NOT NULL
);

ALTER TABLE iris._alarm ADD CONSTRAINT _alarm_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.alarm AS
	SELECT a.name, description, controller, pin, state
	FROM iris._alarm a JOIN iris._device_io d ON a.name = d.name;

CREATE RULE alarm_insert AS ON INSERT TO iris.alarm DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._alarm VALUES (NEW.name, NEW.description, NEW.state);
);

CREATE RULE alarm_update AS ON UPDATE TO iris.alarm DO INSTEAD
(
	UPDATE iris._device_io SET
		controller = NEW.controller,
		pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._alarm SET
		description = NEW.description,
		state = NEW.state
	WHERE name = OLD.name;
);

CREATE RULE alarm_delete AS ON DELETE TO iris.alarm DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

CREATE TABLE event.alarm_event (
	event_id integer PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	alarm VARCHAR(10) NOT NULL REFERENCES iris._alarm(name)
		ON DELETE CASCADE
);

INSERT INTO iris.alarm SELECT * FROM temp_alarm;
INSERT INTO event.alarm_event SELECT * FROM temp_alarm_event;

CREATE TABLE iris._camera (
	name VARCHAR(10) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES geo_loc(name),
	notes text NOT NULL,
	encoder text NOT NULL,
	encoder_channel integer NOT NULL,
	nvr text NOT NULL,
	publish boolean NOT NULL
);

ALTER TABLE iris._camera ADD CONSTRAINT _camera_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.camera AS SELECT
	c.name, geo_loc, controller, pin, notes, encoder, encoder_channel, nvr,
		publish
	FROM iris._camera c JOIN iris._device_io d ON c.name = d.name;

CREATE RULE camera_insert AS ON INSERT TO iris.camera DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._camera VALUES (NEW.name, NEW.geo_loc, NEW.notes,
		NEW.encoder, NEW.encoder_channel, NEW.nvr, NEW.publish);
);

CREATE RULE camera_update AS ON UPDATE TO iris.camera DO INSTEAD
(
	UPDATE iris._device_io SET
		controller = NEW.controller,
		pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._camera SET
		geo_loc = NEW.geo_loc,
		notes = NEW.notes,
		encoder = NEW.encoder,
		encoder_channel = NEW.encoder_channel,
		nvr = NEW.nvr,
		publish = NEW.publish
	WHERE name = OLD.name;
);

CREATE RULE camera_delete AS ON DELETE TO iris.camera DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

INSERT INTO iris.camera SELECT * FROM temp_camera;

CREATE VIEW alarm_view AS
	SELECT a.name, a.description, a.state, a.controller, a.pin, c.comm_link,
		c.drop_id
	FROM iris.alarm a LEFT JOIN controller c ON a.controller = c.name;
GRANT SELECT ON alarm_view TO PUBLIC;

CREATE VIEW alarm_event_view AS
	SELECT e.event_id, e.event_date, ed.description AS event_description,
		e.alarm, a.description
	FROM event.alarm_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
	JOIN iris.alarm a ON e.alarm = a.name;
GRANT SELECT ON alarm_event_view TO PUBLIC;

CREATE VIEW camera_view AS
	SELECT c.name, c.notes, c.encoder, c.encoder_channel, c.nvr, c.publish,
	c.geo_loc, l.freeway, l.free_dir, l.cross_mod, l.cross_street,
	l.cross_dir, l.easting, l.northing, l.east_off, l.north_off,
	c.controller, ctr.comm_link, ctr.drop_id, ctr.active
	FROM iris.camera c
	JOIN geo_loc_view l ON c.geo_loc = l.name
	LEFT JOIN controller ctr ON c.controller = ctr.name;
GRANT SELECT ON camera_view TO PUBLIC;
