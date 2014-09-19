\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.16.0'
	WHERE name = 'database_version';

-- Replace iris.alarm rewrite rules with triggers
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

-- Replace iris.detector rewrite rules with triggers
DROP RULE detector_insert ON iris.detector;
DROP RULE detector_update ON iris.detector;
DROP RULE detector_delete ON iris.detector;

CREATE FUNCTION iris.detector_insert() RETURNS TRIGGER AS
	$detector_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._detector
	            (name, r_node, lane_type, lane_number, abandoned,
	             force_fail, field_length, fake, notes)
	     VALUES (NEW.name, NEW.r_node, NEW.lane_type, NEW.lane_number,
	             NEW.abandoned, NEW.force_fail, NEW.field_length, NEW.fake,
	             NEW.notes);
	RETURN NEW;
END;
$detector_insert$ LANGUAGE plpgsql;

CREATE TRIGGER detector_insert_trig
    INSTEAD OF INSERT ON iris.detector
    FOR EACH ROW EXECUTE PROCEDURE iris.detector_insert();

CREATE FUNCTION iris.detector_update() RETURNS TRIGGER AS
	$detector_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._detector
	   SET r_node = NEW.r_node,
	       lane_type = NEW.lane_type,
	       lane_number = NEW.lane_number,
	       abandoned = NEW.abandoned,
	       force_fail = NEW.force_fail,
	       field_length = NEW.field_length,
	       fake = NEW.fake,
	       notes = NEW.notes
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$detector_update$ LANGUAGE plpgsql;

CREATE TRIGGER detector_update_trig
    INSTEAD OF UPDATE ON iris.detector
    FOR EACH ROW EXECUTE PROCEDURE iris.detector_update();

CREATE FUNCTION iris.detector_delete() RETURNS TRIGGER AS
	$detector_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$detector_delete$ LANGUAGE plpgsql;

CREATE TRIGGER detector_delete_trig
    INSTEAD OF DELETE ON iris.detector
    FOR EACH ROW EXECUTE PROCEDURE iris.detector_delete();

-- Replace iris.camera rewrite rules with triggers
DROP RULE camera_insert ON iris.camera;
DROP RULE camera_update ON iris.camera;
DROP RULE camera_delete ON iris.camera;

CREATE FUNCTION iris.camera_insert() RETURNS TRIGGER AS
	$camera_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._camera (name, geo_loc, notes, encoder,
	                          encoder_channel, encoder_type, publish)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.encoder,
	             NEW.encoder_channel, NEW.encoder_type, NEW.publish);
	RETURN NEW;
END;
$camera_insert$ LANGUAGE plpgsql;

CREATE TRIGGER camera_insert_trig
    INSTEAD OF INSERT ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_insert();

CREATE FUNCTION iris.camera_update() RETURNS TRIGGER AS
	$camera_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._camera
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes,
	       encoder = NEW.encoder,
	       encoder_channel = NEW.encoder_channel,
	       encoder_type = NEW.encoder_type,
	       publish = NEW.publish
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$camera_update$ LANGUAGE plpgsql;

CREATE TRIGGER camera_update_trig
    INSTEAD OF UPDATE ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_update();

CREATE FUNCTION iris.camera_delete() RETURNS TRIGGER AS
	$camera_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$camera_delete$ LANGUAGE plpgsql;

CREATE TRIGGER camera_delete_trig
    INSTEAD OF DELETE ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.camera_delete();

-- Replace iris.ramp_meter rewrite rules with triggers
DROP RULE ramp_meter_insert ON iris.ramp_meter;
DROP RULE ramp_meter_update ON iris.ramp_meter;
DROP RULE ramp_meter_delete ON iris.ramp_meter;

CREATE FUNCTION iris.ramp_meter_insert() RETURNS TRIGGER AS
	$ramp_meter_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._ramp_meter
	            (name, geo_loc, notes, meter_type, storage, max_wait,
	             algorithm, am_target, pm_target, camera, m_lock)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.meter_type,
	             NEW.storage, NEW.max_wait, NEW.algorithm, NEW.am_target,
	             NEW.pm_target, NEW.camera, NEW.m_lock);
	RETURN NEW;
END;
$ramp_meter_insert$ LANGUAGE plpgsql;

CREATE TRIGGER ramp_meter_insert_trig
    INSTEAD OF INSERT ON iris.ramp_meter
    FOR EACH ROW EXECUTE PROCEDURE iris.ramp_meter_insert();

CREATE FUNCTION iris.ramp_meter_update() RETURNS TRIGGER AS
	$ramp_meter_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._ramp_meter
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes,
	       meter_type = NEW.meter_type,
	       storage = NEW.storage,
	       max_wait = NEW.max_wait,
	       algorithm = NEW.algorithm,
	       am_target = NEW.am_target,
	       pm_target = NEW.pm_target,
	       camera = NEW.camera,
	       m_lock = NEW.m_lock
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$ramp_meter_update$ LANGUAGE plpgsql;

CREATE TRIGGER ramp_meter_update_trig
    INSTEAD OF UPDATE ON iris.ramp_meter
    FOR EACH ROW EXECUTE PROCEDURE iris.ramp_meter_update();

CREATE FUNCTION iris.ramp_meter_delete() RETURNS TRIGGER AS
	$ramp_meter_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$ramp_meter_delete$ LANGUAGE plpgsql;

CREATE TRIGGER ramp_meter_delete_trig
    INSTEAD OF DELETE ON iris.ramp_meter
    FOR EACH ROW EXECUTE PROCEDURE iris.ramp_meter_delete();

-- Replace iris.dms rewrite rules with triggers
DROP RULE dms_insert ON iris.dms;
DROP RULE dms_update ON iris.dms;
DROP RULE dms_delete ON iris.dms;

CREATE FUNCTION iris.dms_insert() RETURNS TRIGGER AS
	$dms_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._dms (name, geo_loc, notes, camera, aws_allowed,
	                       aws_controlled, default_font)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.camera,
	             NEW.aws_allowed, NEW.aws_controlled, NEW.default_font);
	RETURN NEW;
END;
$dms_insert$ LANGUAGE plpgsql;

CREATE TRIGGER dms_insert_trig
    INSTEAD OF INSERT ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_insert();

CREATE FUNCTION iris.dms_update() RETURNS TRIGGER AS
	$dms_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._dms
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes,
	       camera = NEW.camera,
	       aws_allowed = NEW.aws_allowed,
	       aws_controlled = NEW.aws_controlled,
	       default_font = NEW.default_font
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$dms_update$ LANGUAGE plpgsql;

CREATE TRIGGER dms_update_trig
    INSTEAD OF UPDATE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_update();

CREATE FUNCTION iris.dms_delete() RETURNS TRIGGER AS
	$dms_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$dms_delete$ LANGUAGE plpgsql;

CREATE TRIGGER dms_delete_trig
    INSTEAD OF DELETE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.dms_delete();

-- Replace iris.lane_marking rewrite rules with triggers
DROP RULE lane_marking_insert ON iris.lane_marking;
DROP RULE lane_marking_update ON iris.lane_marking;
DROP RULE lane_marking_delete ON iris.lane_marking;

CREATE FUNCTION iris.lane_marking_insert() RETURNS TRIGGER AS
	$lane_marking_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._lane_marking (name, geo_loc, notes)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes);
	RETURN NEW;
END;
$lane_marking_insert$ LANGUAGE plpgsql;

CREATE TRIGGER lane_marking_insert_trig
    INSTEAD OF INSERT ON iris.lane_marking
    FOR EACH ROW EXECUTE PROCEDURE iris.lane_marking_insert();

CREATE FUNCTION iris.lane_marking_update() RETURNS TRIGGER AS
	$lane_marking_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._lane_marking
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$lane_marking_update$ LANGUAGE plpgsql;

CREATE TRIGGER lane_marking_update_trig
    INSTEAD OF UPDATE ON iris.lane_marking
    FOR EACH ROW EXECUTE PROCEDURE iris.lane_marking_update();

CREATE FUNCTION iris.lane_marking_delete() RETURNS TRIGGER AS
	$lane_marking_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$lane_marking_delete$ LANGUAGE plpgsql;

CREATE TRIGGER lane_marking_delete_trig
    INSTEAD OF DELETE ON iris.lane_marking
    FOR EACH ROW EXECUTE PROCEDURE iris.lane_marking_delete();

-- Replace iris.weather_sensor rewrite rules with triggers
DROP RULE weather_sensor_insert ON iris.weather_sensor;
DROP RULE weather_sensor_update ON iris.weather_sensor;
DROP RULE weather_sensor_delete ON iris.weather_sensor;

CREATE FUNCTION iris.weather_sensor_insert() RETURNS TRIGGER AS
	$weather_sensor_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._weather_sensor (name, geo_loc, notes)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes);
	RETURN NEW;
END;
$weather_sensor_insert$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_insert_trig
    INSTEAD OF INSERT ON iris.weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.weather_sensor_insert();

CREATE FUNCTION iris.weather_sensor_update() RETURNS TRIGGER AS
	$weather_sensor_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._weather_sensor
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$weather_sensor_update$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_update_trig
    INSTEAD OF UPDATE ON iris.weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.weather_sensor_update();

CREATE FUNCTION iris.weather_sensor_delete() RETURNS TRIGGER AS
	$weather_sensor_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$weather_sensor_delete$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_delete_trig
    INSTEAD OF DELETE ON iris.weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.weather_sensor_delete();

-- Replace iris.lcs_array rewrite rules with triggers
DROP RULE lcs_array_insert ON iris.lcs_array;
DROP RULE lcs_array_update ON iris.lcs_array;
DROP RULE lcs_array_delete ON iris.lcs_array;

CREATE FUNCTION iris.lcs_array_insert() RETURNS TRIGGER AS
	$lcs_array_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._lcs_array(name, notes, shift, lcs_lock)
	     VALUES (NEW.name, NEW.notes, NEW.shift, NEW.lcs_lock);
	RETURN NEW;
END;
$lcs_array_insert$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_array_insert_trig
    INSTEAD OF INSERT ON iris.lcs_array
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_array_insert();

CREATE FUNCTION iris.lcs_array_update() RETURNS TRIGGER AS
	$lcs_array_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._lcs_array
	   SET notes = NEW.notes,
	       shift = NEW.shift,
	       lcs_lock = NEW.lcs_lock
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$lcs_array_update$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_array_update_trig
    INSTEAD OF UPDATE ON iris.lcs_array
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_array_update();

CREATE FUNCTION iris.lcs_array_delete() RETURNS TRIGGER AS
	$lcs_array_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$lcs_array_delete$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_array_delete_trig
    INSTEAD OF DELETE ON iris.lcs_array
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_array_delete();

-- Replace iris.lcs_indication rewrite rules with triggers
DROP RULE lcs_indication_insert ON iris.lcs_indication;
DROP RULE lcs_indication_update ON iris.lcs_indication;
DROP RULE lcs_indication_delete ON iris.lcs_indication;

CREATE FUNCTION iris.lcs_indication_insert() RETURNS TRIGGER AS
	$lcs_indication_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._lcs_indication(name, lcs, indication)
	     VALUES (NEW.name, NEW.lcs, NEW.indication);
	RETURN NEW;
END;
$lcs_indication_insert$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_indication_insert_trig
    INSTEAD OF INSERT ON iris.lcs_indication
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_indication_insert();

CREATE FUNCTION iris.lcs_indication_update() RETURNS TRIGGER AS
	$lcs_indication_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._lcs_indication
	   SET lcs = NEW.lcs,
	       indication = NEW.indication
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$lcs_indication_update$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_indication_update_trig
    INSTEAD OF UPDATE ON iris.lcs_indication
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_indication_update();

CREATE FUNCTION iris.lcs_indication_delete() RETURNS TRIGGER AS
	$lcs_indication_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$lcs_indication_delete$ LANGUAGE plpgsql;

CREATE TRIGGER lcs_indication_delete_trig
    INSTEAD OF DELETE ON iris.lcs_indication
    FOR EACH ROW EXECUTE PROCEDURE iris.lcs_indication_delete();

-- Add backup limit control for KAdaptiveAlgorithm
INSERT INTO event.meter_limit_control (id, description)
	VALUES (4, 'backup limit');

-- Reserve Pelco P comm protocol value
INSERT INTO iris.comm_protocol (id, description)
	VALUES (30, 'Pelco P');
