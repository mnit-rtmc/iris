\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.88.0', '4.89.0');

CREATE FUNCTION iris.ramp_meter_notify() RETURNS TRIGGER AS
	$ramp_meter_notify$
BEGIN
	NOTIFY ramp_meter;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$ramp_meter_notify$ LANGUAGE plpgsql;

CREATE TRIGGER ramp_meter_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris._ramp_meter
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.ramp_meter_notify();

CREATE FUNCTION iris.gate_arm_array_notify() RETURNS TRIGGER AS
	$gate_arm_array_notify$
BEGIN
	NOTIFY gate_arm_array;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$gate_arm_array_notify$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_array_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris._gate_arm_array
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.gate_arm_array_notify();

CREATE FUNCTION iris.weather_sensor_notify() RETURNS TRIGGER AS
	$weather_sensor_notify$
BEGIN
	NOTIFY weather_sensor;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$weather_sensor_notify$ LANGUAGE plpgsql;

CREATE TRIGGER weather_sensor_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris._weather_sensor
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.weather_sensor_notify();

CREATE FUNCTION iris.tag_reader_notify() RETURNS TRIGGER AS
	$tag_reader_notify$
BEGIN
	NOTIFY tag_reader;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$tag_reader_notify$ LANGUAGE plpgsql;

CREATE TRIGGER tag_reader_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris._tag_reader
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.tag_reader_notify();

CREATE FUNCTION iris.beacon_notify() RETURNS TRIGGER AS
	$beacon_notify$
BEGIN
	NOTIFY beacon;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$beacon_notify$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris._beacon
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.beacon_notify();

CREATE TRIGGER r_node_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris.r_node
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE TRIGGER cabinet_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris.cabinet
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE TRIGGER system_attribute_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris.system_attribute
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

COMMIT;
