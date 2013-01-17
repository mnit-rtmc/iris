\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.2.0'
	WHERE name = 'database_version';

DELETE FROM iris.system_attribute WHERE name = 'camera_ptz_panel_enable';
DELETE FROM iris.system_attribute WHERE name = 'camera_stream_duration_secs';

DROP VIEW alarm_event_view;
DROP VIEW alarm_view;
DROP VIEW iris.alarm;

ALTER TABLE iris._alarm ADD COLUMN trigger_time timestamp WITH time zone;
UPDATE iris._alarm SET trigger_time = (
	SELECT event_date FROM event.alarm_event WHERE event_desc_id = 1
	AND alarm = name ORDER BY event_id DESC LIMIT 1
) WHERE state = 't';

CREATE VIEW iris.alarm AS
	SELECT a.name, description, controller, pin, state, trigger_time
	FROM iris._alarm a JOIN iris._device_io d ON a.name = d.name;

CREATE RULE alarm_insert AS ON INSERT TO iris.alarm DO INSTEAD
(
	INSERT INTO iris._device_io VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._alarm VALUES (NEW.name, NEW.description, NEW.state,
		NEW.trigger_time);
);

CREATE RULE alarm_update AS ON UPDATE TO iris.alarm DO INSTEAD
(
	UPDATE iris._device_io SET
		controller = NEW.controller,
		pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._alarm SET
		description = NEW.description,
		state = NEW.state,
		trigger_time = NEW.trigger_time
	WHERE name = OLD.name;
);

CREATE RULE alarm_delete AS ON DELETE TO iris.alarm DO INSTEAD
	DELETE FROM iris._device_io WHERE name = OLD.name;

CREATE VIEW alarm_view AS
	SELECT a.name, a.description, a.state, a.trigger_time, a.controller,
		a.pin, c.comm_link, c.drop_id
	FROM iris.alarm a LEFT JOIN iris.controller c ON a.controller = c.name;
GRANT SELECT ON alarm_view TO PUBLIC;

CREATE VIEW alarm_event_view AS
	SELECT e.event_id, e.event_date, ed.description AS event_description,
		e.alarm, a.description
	FROM event.alarm_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id
	JOIN iris.alarm a ON e.alarm = a.name;
GRANT SELECT ON alarm_event_view TO PUBLIC;
