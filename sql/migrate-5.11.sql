\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.10.0', '5.11.0');

-- Add generic device_delete function
CREATE FUNCTION iris.device_delete() RETURNS TRIGGER AS
	$device_delete$
BEGIN
	DELETE FROM iris._device_preset WHERE name = OLD.name;
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$device_delete$ LANGUAGE plpgsql;

-- Add flow stream status look up table
CREATE TABLE iris.flow_stream_status (
	id INTEGER PRIMARY KEY,
	description VARCHAR(8) NOT NULL
);

COPY iris.flow_stream_status (id, description) FROM stdin;
0	FAILED
1	STARTING
2	PLAYING
\.

-- Add video flow stream
CREATE TABLE iris._flow_stream (
	name VARCHAR(12) PRIMARY KEY,
	restricted BOOLEAN NOT NULL,
	loc_overlay BOOLEAN NOT NULL,
	quality INTEGER NOT NULL REFERENCES iris.encoding_quality,
	camera VARCHAR(20) REFERENCES iris._camera,
	mon_num INTEGER,
	address INET,
	port INTEGER CHECK (port > 0 AND port <= 65535),
	status INTEGER NOT NULL REFERENCES iris.flow_stream_status,
	CONSTRAINT camera_or_monitor CHECK (camera IS NULL OR mon_num IS NULL)
);

ALTER TABLE iris._flow_stream ADD CONSTRAINT _flow_stream_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.flow_stream AS
	SELECT f.name, controller, pin, restricted, loc_overlay, quality,
	       camera, mon_num, address, port, status
	FROM iris._flow_stream f JOIN iris._device_io d ON f.name = d.name;

CREATE FUNCTION iris.flow_stream_insert() RETURNS TRIGGER AS
	$flow_stream_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._flow_stream (name, restricted, loc_overlay, quality,
	                               camera, mon_num, address, port, status)
	     VALUES (NEW.name, NEW.restricted, NEW.loc_overlay, NEW.quality,
	             NEW.camera, NEW.mon_num, NEW.address, NEW.port,NEW.status);
	RETURN NEW;
END;
$flow_stream_insert$ LANGUAGE plpgsql;

CREATE TRIGGER flow_stream_insert_trig
    INSTEAD OF INSERT ON iris.flow_stream
    FOR EACH ROW EXECUTE PROCEDURE iris.flow_stream_insert();

CREATE FUNCTION iris.flow_stream_update() RETURNS TRIGGER AS
	$flow_stream_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._flow_stream
	   SET restricted = NEW.restricted,
	       loc_overlay = NEW.loc_overlay,
	       quality = NEW.quality,
	       camera = NEW.camera,
	       mon_num = NEW.mon_num,
	       address = NEW.address,
	       port = NEW.port,
	       status = NEW.status
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$flow_stream_update$ LANGUAGE plpgsql;

CREATE TRIGGER flow_stream_update_trig
    INSTEAD OF UPDATE ON iris.flow_stream
    FOR EACH ROW EXECUTE PROCEDURE iris.flow_stream_update();

CREATE TRIGGER flow_stream_delete_trig
    INSTEAD OF DELETE ON iris.flow_stream
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

CREATE VIEW flow_stream_view AS
	SELECT f.name, f.controller, pin, condition, comm_link, restricted,
	       loc_overlay, eq.description AS quality, camera, mon_num, address,
	       port, s.description AS status
	FROM iris.flow_stream f
	JOIN iris.flow_stream_status s ON f.status = s.id
	LEFT JOIN controller_view ctr ON controller = ctr.name
	LEFT JOIN iris.encoding_quality eq ON f.quality = eq.id;
GRANT SELECT ON flow_stream_view TO PUBLIC;

-- Use device_delete instead of separate functions for delete triggers
DROP TRIGGER camera_delete_trig ON iris.camera;
DROP FUNCTION iris.camera_delete;
CREATE TRIGGER camera_delete_trig
    INSTEAD OF DELETE ON iris.camera
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

DROP TRIGGER alarm_delete_trig ON iris.alarm;
DROP FUNCTION iris.alarm_delete;
CREATE TRIGGER alarm_delete_trig
    INSTEAD OF DELETE ON iris.alarm
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

DROP TRIGGER beacon_delete_trig ON iris.beacon;
DROP FUNCTION iris.beacon_delete;
CREATE TRIGGER beacon_delete_trig
    INSTEAD OF DELETE ON iris.beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

DROP TRIGGER detector_delete_trig ON iris.detector;
DROP FUNCTION iris.detector_delete;
CREATE TRIGGER detector_delete_trig
    INSTEAD OF DELETE ON iris.detector
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

DROP TRIGGER gps_delete_trig ON iris.gps;
DROP FUNCTION iris.gps_delete;
CREATE TRIGGER gps_delete_trig
    INSTEAD OF DELETE ON iris.gps
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

DROP TRIGGER dms_delete_trig ON iris.dms;
DROP FUNCTION iris.dms_delete;
CREATE TRIGGER dms_delete_trig
    INSTEAD OF DELETE ON iris.dms
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

DROP TRIGGER gate_arm_array_delete_trig ON iris.gate_arm_array;
DROP FUNCTION iris.gate_arm_array_delete;
CREATE TRIGGER gate_arm_array_delete_trig
    INSTEAD OF DELETE ON iris.gate_arm_array
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

DROP TRIGGER lane_marking_delete_trig ON iris.lane_marking;
DROP FUNCTION iris.lane_marking_delete;
CREATE TRIGGER lane_marking_delete_trig
    INSTEAD OF DELETE ON iris.lane_marking
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

DROP TRIGGER lcs_array_delete_trig ON iris.lcs_array;
DROP FUNCTION iris.lcs_array_delete;
CREATE TRIGGER lcs_array_delete_trig
    INSTEAD OF DELETE ON iris.lcs_array
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

DROP TRIGGER lcs_indication_delete_trig ON iris.lcs_indication;
DROP FUNCTION iris.lcs_indication_delete;
CREATE TRIGGER lcs_indication_delete_trig
    INSTEAD OF DELETE ON iris.lcs_indication
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

DROP TRIGGER ramp_meter_delete_trig ON iris.ramp_meter;
DROP FUNCTION iris.ramp_meter_delete;
CREATE TRIGGER ramp_meter_delete_trig
    INSTEAD OF DELETE ON iris.ramp_meter
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

DROP TRIGGER tag_reader_delete_trig ON iris.tag_reader;
DROP FUNCTION iris.tag_reader_delete;
CREATE TRIGGER tag_reader_delete_trig
    INSTEAD OF DELETE ON iris.tag_reader
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

DROP TRIGGER video_monitor_delete_trig ON iris.video_monitor;
DROP FUNCTION iris.video_monitor_delete;
CREATE TRIGGER video_monitor_delete_trig
    INSTEAD OF DELETE ON iris.video_monitor
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

DROP TRIGGER weather_sensor_delete_trig ON iris.weather_sensor;
DROP FUNCTION iris.weather_sensor_delete;
CREATE TRIGGER weather_sensor_delete_trig
    INSTEAD OF DELETE ON iris.weather_sensor
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

-- Change gate arm triggers to better style
DROP TRIGGER gate_arm_update_trig ON iris.gate_arm;
DROP FUNCTION iris.gate_arm_update;

CREATE FUNCTION iris.gate_arm_insert() RETURNS TRIGGER AS
	$gate_arm_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._gate_arm (name, ga_array, idx, notes)
	     VALUES (NEW.name, NEW.ga_array, NEW.idx, NEW.notes);
	RETURN NEW;
END;
$gate_arm_insert$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_insert_trig
    INSTEAD OF INSERT ON iris.gate_arm
    FOR EACH ROW EXECUTE PROCEDURE iris.gate_arm_insert();

CREATE FUNCTION iris.gate_arm_update() RETURNS TRIGGER AS
	$gate_arm_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller, pin = NEW.pin
	WHERE name = OLD.name;
	UPDATE iris._gate_arm
	   SET ga_array = NEW.ga_array, idx = NEW.idx, notes = NEW.notes
	WHERE name = OLD.name;
	RETURN NEW;
END;
$gate_arm_update$ LANGUAGE plpgsql;

CREATE TRIGGER gate_arm_update_trig
    INSTEAD OF UPDATE ON iris.gate_arm
    FOR EACH ROW EXECUTE PROCEDURE iris.gate_arm_update();

CREATE TRIGGER gate_arm_delete_trig
    INSTEAD OF DELETE ON iris.gate_arm
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

-- Add flow_stream column to encoder stream
DROP VIEW encoder_stream_view;
ALTER TABLE iris.encoder_stream ADD COLUMN flow_stream BOOLEAN;
UPDATE iris.encoder_stream SET flow_stream = false;
ALTER TABLE iris.encoder_stream ALTER COLUMN flow_stream SET NOT NULL;
CREATE VIEW encoder_stream_view AS
	SELECT es.name, encoder_type, make, model, config, view_num,flow_stream,
	       enc.description AS encoding, eq.description AS quality,
	       uri_scheme, uri_path, mcast_port, latency
	FROM iris.encoder_stream es
	LEFT JOIN iris.encoder_type et ON es.encoder_type = et.name
	LEFT JOIN iris.encoding enc ON es.encoding = enc.id
	LEFT JOIN iris.encoding_quality eq ON es.quality = eq.id;
GRANT SELECT ON encoder_stream_view TO PUBLIC;

-- Add flow_stream sonar_type
INSERT INTO iris.sonar_type (name) VALUES ('flow_stream');

-- Add privileges for flow_stream
INSERT INTO iris.privilege (name, capability, type_n, write)
	VALUES ('PRV_fs0', 'camera_admin', 'flow_stream', true),
	       ('PRV_fs1', 'camera_tab', 'flow_stream', false);

-- Reserve Streambed comm protocol value
INSERT INTO iris.comm_protocol (id, description) VALUES (41, 'Streambed');

-- Add word view
CREATE VIEW word_view AS
	SELECT name, abbr, allowed
	FROM iris.word;
GRANT SELECT ON word_view TO PUBLIC;

COMMIT;
