\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.16.0'
	WHERE name = 'database_version';

DROP RULE alarm_insert ON iris.alarm;
DROP RULE alarm_update ON iris.alarm;
DROP RULE alarm_delete ON iris.alarm;

CREATE FUNCTION iris.alarm_insert() RETURNS TRIGGER AS
	$alarm_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	    VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._alarm (name, description, state, trigger_time)
	    VALUES (NEW.name, NEW.description, NEW.state, NEW.trigger_time);
	RETURN NEW;
END;
$alarm_insert$ LANGUAGE plpgsql;

CREATE TRIGGER alarm_insert_trig
    INSTEAD OF INSERT ON iris.alarm
    FOR EACH ROW EXECUTE PROCEDURE iris.alarm_insert();

CREATE FUNCTION iris.alarm_update() RETURNS TRIGGER AS
	$alarm_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._alarm
	   SET description = NEW.description,
	       state = NEW.state,
	       trigger_time = NEW.trigger_time
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$alarm_update$ LANGUAGE plpgsql;

CREATE TRIGGER alarm_update_trig
    INSTEAD OF UPDATE ON iris.alarm
    FOR EACH ROW EXECUTE PROCEDURE iris.alarm_update();

CREATE FUNCTION iris.alarm_delete() RETURNS TRIGGER AS
	$alarm_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$alarm_delete$ LANGUAGE plpgsql;

CREATE TRIGGER alarm_delete_trig
    INSTEAD OF DELETE ON iris.alarm
    FOR EACH ROW EXECUTE PROCEDURE iris.alarm_delete();
