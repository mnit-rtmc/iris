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

CREATE FUNCTION iris.r_node_notify() RETURNS TRIGGER AS
	$r_node_notify$
BEGIN
	IF (TG_OP = 'DELETE') THEN
		PERFORM pg_notify('r_node', OLD.name);
	ELSE
		PERFORM pg_notify('r_node', NEW.name);
	END IF;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$r_node_notify$ LANGUAGE plpgsql;

CREATE TRIGGER r_node_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris.r_node
	FOR EACH ROW EXECUTE PROCEDURE iris.r_node_notify();

CREATE TRIGGER cabinet_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris.cabinet
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE TRIGGER system_attribute_notify_trig
	AFTER INSERT OR UPDATE OR DELETE ON iris.system_attribute
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

-- Add landmark, lat and lon to r_node_view
DROP VIEW r_node_view;
CREATE VIEW r_node_view AS
	SELECT n.name, n.geo_loc, roadway, road_dir, cross_mod, cross_street,
	       cross_dir, landmark, lat, lon, nt.name AS node_type, n.pickable,
	       n.above, tr.name AS transition, n.lanes, n.attach_side, n.shift,
	       n.active, n.abandoned, n.station_id, n.speed_limit, n.notes
	FROM iris.r_node n
	JOIN geo_loc_view l ON n.geo_loc = l.name
	JOIN iris.r_node_type nt ON n.node_type = nt.n_type
	JOIN iris.r_node_transition tr ON n.transition = tr.n_transition;
GRANT SELECT ON r_node_view TO PUBLIC;

-- Update notify stuff
DROP TRIGGER camera_notify_trig ON iris._camera;
DROP FUNCTION iris.camera_notify();
DROP TRIGGER dms_notify_trig ON iris._dms;
DROP FUNCTION iris.dms_notify();
DROP TRIGGER parking_area_notify_trig ON iris.parking_area;
DROP FUNCTION iris.parking_area_notify();

CREATE FUNCTION iris.camera_notify() RETURNS TRIGGER AS
	$camera_notify$
BEGIN
	IF (NEW.video_loss IS DISTINCT FROM OLD.video_loss) THEN
		NOTIFY camera, 'video_loss';
	ELSE
		NOTIFY camera;
	END IF;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$camera_notify$ LANGUAGE plpgsql;

CREATE TRIGGER camera_notify_trig
	AFTER UPDATE ON iris._camera
	FOR EACH ROW EXECUTE PROCEDURE iris.camera_notify();

CREATE TRIGGER camera_table_notify_trig
	AFTER INSERT OR DELETE ON iris._camera
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE FUNCTION iris.dms_notify() RETURNS TRIGGER AS
	$dms_notify$
BEGIN
	IF (NEW.msg_current IS DISTINCT FROM OLD.msg_current) THEN
		NOTIFY dms, 'msg_current';
	ELSIF (NEW.expire_time IS DISTINCT FROM OLD.expire_time) THEN
		NOTIFY dms, 'expire_time';
	ELSIF (NEW.msg_sched IS DISTINCT FROM OLD.msg_sched) THEN
		NOTIFY dms, 'msg_sched';
	ELSE
		NOTIFY dms;
	END IF;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$dms_notify$ LANGUAGE plpgsql;

CREATE TRIGGER dms_notify_trig
	AFTER UPDATE ON iris._dms
	FOR EACH ROW EXECUTE PROCEDURE iris.dms_notify();

CREATE TRIGGER dms_table_notify_trig
	AFTER INSERT OR DELETE ON iris._dms
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

CREATE FUNCTION iris.parking_area_notify() RETURNS TRIGGER AS
	$parking_area_notify$
BEGIN
	IF (NEW.time_stamp IS DISTINCT FROM OLD.time_stamp) THEN
		NOTIFY parking_area, 'time_stamp';
	ELSIF (NEW.time_stamp_static IS DISTINCT FROM OLD.time_stamp_static) THEN
		NOTIFY parking_area;
	END IF;
	RETURN NULL; -- AFTER trigger return is ignored
END;
$parking_area_notify$ LANGUAGE plpgsql;

CREATE TRIGGER parking_area_notify_trig
	AFTER UPDATE ON iris.parking_area
	FOR EACH ROW EXECUTE PROCEDURE iris.parking_area_notify();

CREATE TRIGGER parking_area_table_notify_trig
	AFTER INSERT OR DELETE ON iris.parking_area
	FOR EACH STATEMENT EXECUTE PROCEDURE iris.table_notify();

COMMIT;
