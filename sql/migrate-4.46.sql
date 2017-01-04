\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.46.0'
	WHERE name = 'database_version';

-- Add travel time event table
CREATE TABLE event.travel_time_event (
	event_id SERIAL PRIMARY KEY,
	event_date timestamp WITH time zone NOT NULL,
	event_desc_id INTEGER NOT NULL
		REFERENCES event.event_description(event_desc_id),
	device_id VARCHAR(10)
);

-- Add travel time event view
CREATE VIEW travel_time_event_view AS
	SELECT event_id, event_date, event_description.description, device_id
	FROM event.travel_time_event
	JOIN event.event_description
	ON travel_time_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON travel_time_event_view TO PUBLIC;

-- Add travel time event descriptions
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (701, 'TT Link too long');
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (702, 'TT No data');
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (703, 'TT No destination data');
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (704, 'TT No origin data');
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (705, 'TT No route');

-- Add camera switch event table
CREATE TABLE event.camera_switch_event (
	event_id SERIAL PRIMARY KEY,
	event_date timestamp WITH time zone NOT NULL,
	event_desc_id INTEGER NOT NULL
		REFERENCES event.event_description(event_desc_id),
	monitor_id VARCHAR(12),
	camera_id VARCHAR(10),
	source VARCHAR(20)
);

-- Add camera switch event view
CREATE VIEW camera_switch_event_view AS
	SELECT event_id, event_date, event_description.description, monitor_id,
	       camera_id, source
	FROM event.camera_switch_event
	JOIN event.event_description
	ON camera_switch_event.event_desc_id = event_description.event_desc_id;
GRANT SELECT ON camera_switch_event_view TO PUBLIC;

-- Add camera switched event description
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (801, 'Camera SWITCHED');

-- Add quick_message_view
CREATE VIEW quick_message_view AS
	SELECT name, sign_group, multi FROM iris.quick_message;
GRANT SELECT ON quick_message_view TO PUBLIC;

-- Drop video_monitor views and functions
DROP VIEW video_monitor_view;
DROP VIEW iris.video_monitor;
DROP FUNCTION iris.video_monitor_insert();
DROP FUNCTION iris.video_monitor_update();
DROP FUNCTION iris.video_monitor_delete();

-- Add mon_num and direct
ALTER TABLE iris._video_monitor ADD COLUMN mon_num INTEGER;
ALTER TABLE iris._video_monitor ADD COLUMN direct BOOLEAN;
UPDATE iris._video_monitor
	SET mon_num = COALESCE(substring(name FROM '\d+'), '0')::INTEGER;
UPDATE iris._video_monitor SET direct = false;
ALTER TABLE iris._video_monitor ALTER COLUMN mon_num SET NOT NULL;
ALTER TABLE iris._video_monitor ALTER COLUMN direct SET NOT NULL;

CREATE VIEW iris.video_monitor AS SELECT
	m.name, controller, pin, notes, mon_num, direct, restricted, camera
	FROM iris._video_monitor m JOIN iris._device_io d ON m.name = d.name;

CREATE FUNCTION iris.video_monitor_insert() RETURNS TRIGGER AS
	$video_monitor_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._video_monitor (name, notes, mon_num, direct,
	                                 restricted, camera)
	     VALUES (NEW.name, NEW.notes, NEW.mon_num, NEW.direct,
	             NEW.restricted, NEW.camera);
	RETURN NEW;
END;
$video_monitor_insert$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_insert_trig
    INSTEAD OF INSERT ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_insert();

CREATE FUNCTION iris.video_monitor_update() RETURNS TRIGGER AS
	$video_monitor_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._video_monitor
	   SET notes = NEW.notes,
	       mon_num = NEW.mon_num,
	       direct = NEW.direct,
	       restricted = NEW.restricted,
	       camera = NEW.camera
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$video_monitor_update$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_update_trig
    INSTEAD OF UPDATE ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_update();

CREATE FUNCTION iris.video_monitor_delete() RETURNS TRIGGER AS
	$video_monitor_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$video_monitor_delete$ LANGUAGE plpgsql;

CREATE TRIGGER video_monitor_delete_trig
    INSTEAD OF DELETE ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.video_monitor_delete();

CREATE VIEW video_monitor_view AS
	SELECT m.name, m.notes, mon_num, direct, restricted, m.controller,
	       ctr.condition, ctr.comm_link, camera
	FROM iris.video_monitor m
	LEFT JOIN controller_view ctr ON m.controller = ctr.name;
GRANT SELECT ON video_monitor_view TO PUBLIC;
