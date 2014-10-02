\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.17.0'
	WHERE name = 'database_version';

-- Remove controller_report view
DROP VIEW controller_report;
DROP VIEW controller_device_view;
DROP VIEW iris.controller_device;
DROP VIEW iris.controller_gate_arm;
DROP VIEW iris.controller_camera;
DROP VIEW iris.controller_beacon;
DROP VIEW iris.controller_meter;
DROP VIEW iris.controller_lcs;
DROP VIEW iris.controller_weather_sensor;
DROP VIEW iris.controller_lane_marking;
DROP VIEW iris.controller_dms;

-- Add device_controller_view
CREATE VIEW device_controller_view AS
	SELECT name, controller, pin
	FROM iris._device_io;
GRANT SELECT ON device_controller_view TO PUBLIC;

-- Add preset table
CREATE TABLE iris.camera_preset (
	name VARCHAR(10) PRIMARY KEY,
	camera VARCHAR(10) NOT NULL REFERENCES iris._camera,
	preset_num INTEGER NOT NULL CHECK (preset_num > 0 AND preset_num <= 12),
	direction SMALLINT REFERENCES iris.direction(id),
	UNIQUE(camera, preset_num)
);

CREATE TABLE iris._device_preset (
	name VARCHAR(10) PRIMARY KEY,
	preset VARCHAR(10) UNIQUE REFERENCES iris.camera_preset(name)
);

-- Update beacon for camera presets
DROP VIEW beacon_view;
DROP VIEW iris.beacon;
DROP FUNCTION iris.beacon_insert();
DROP FUNCTION iris.beacon_update();
DROP FUNCTION iris.beacon_delete();

CREATE VIEW iris.beacon AS
	SELECT b.name, geo_loc, controller, pin, notes, message, preset
	FROM iris._beacon b
	JOIN iris._device_io d ON b.name = d.name
	JOIN iris._device_preset p ON b.name = p.name;

CREATE FUNCTION iris.beacon_insert() RETURNS TRIGGER AS
	$beacon_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	    VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._device_preset (name, preset)
	    VALUES (NEW.name, NEW.preset);
	INSERT INTO iris._beacon (name, geo_loc, notes, message)
	    VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.message);
	RETURN NEW;
END;
$beacon_insert$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_insert_trig
    INSTEAD OF INSERT ON iris.beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.beacon_insert();

CREATE FUNCTION iris.beacon_update() RETURNS TRIGGER AS
	$beacon_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._device_preset
	   SET preset = NEW.preset
	 WHERE name = OLD.name;
	UPDATE iris._beacon
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes,
	       message = NEW.message
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$beacon_update$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_update_trig
    INSTEAD OF UPDATE ON iris.beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.beacon_update();

CREATE FUNCTION iris.beacon_delete() RETURNS TRIGGER AS
	$beacon_delete$
BEGIN
	DELETE FROM iris._device_preset WHERE name = OLD.name;
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$beacon_delete$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_delete_trig
    INSTEAD OF DELETE ON iris.beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.beacon_delete();

CREATE VIEW beacon_view AS
	SELECT b.name, b.notes, b.message, p.camera, p.preset_num, b.geo_loc,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.lat, l.lon,
	       b.controller, b.pin, ctr.comm_link, ctr.drop_id, ctr.active
	FROM iris.beacon b
	LEFT JOIN iris.camera_preset p ON b.preset = p.name
	LEFT JOIN geo_loc_view l ON b.geo_loc = l.name
	LEFT JOIN iris.controller ctr ON b.controller = ctr.name;
GRANT SELECT ON beacon_view TO PUBLIC;

-- Update ramp_meter for camera presets
DROP VIEW ramp_meter_view;
DROP VIEW iris.ramp_meter;
DROP FUNCTION iris.ramp_meter_insert();
DROP FUNCTION iris.ramp_meter_update();
DROP FUNCTION iris.ramp_meter_delete();

CREATE VIEW iris.ramp_meter AS
	SELECT m.name, geo_loc, controller, pin, notes, meter_type, storage,
	       max_wait, algorithm, am_target, pm_target, preset, m_lock
	FROM iris._ramp_meter m
	JOIN iris._device_io d ON m.name = d.name
	JOIN iris._device_preset p ON m.name = p.name;

CREATE FUNCTION iris.ramp_meter_insert() RETURNS TRIGGER AS
	$ramp_meter_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._device_preset (name, preset)
	     VALUES (NEW.name, NEW.preset);
	INSERT INTO iris._ramp_meter
	            (name, geo_loc, notes, meter_type, storage, max_wait,
	             algorithm, am_target, pm_target, m_lock)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.meter_type,
	             NEW.storage, NEW.max_wait, NEW.algorithm, NEW.am_target,
	             NEW.pm_target, NEW.m_lock);
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
	UPDATE iris._device_preset
	   SET preset = NEW.preset
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
	DELETE FROM iris._device_preset WHERE name = OLD.name;
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

CREATE VIEW ramp_meter_view AS
	SELECT m.name, geo_loc, controller, pin, notes,
	       mt.description AS meter_type, storage, max_wait,
	       alg.description AS algorithm, am_target, pm_target, camera,
	       preset_num, ml.description AS meter_lock,
	       l.rd, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
	       l.cross_dir, l.lat, l.lon
	FROM iris.ramp_meter m
	LEFT JOIN iris.meter_type mt ON m.meter_type = mt.id
	LEFT JOIN iris.meter_algorithm alg ON m.algorithm = alg.id
	LEFT JOIN iris.camera_preset p ON m.preset = p.name
	LEFT JOIN iris.meter_lock ml ON m.m_lock = ml.id
	LEFT JOIN geo_loc_view l ON m.geo_loc = l.name;
GRANT SELECT ON ramp_meter_view TO PUBLIC;

-- Update dms for camera presets
DROP VIEW dms_view;
DROP VIEW iris.dms;
DROP FUNCTION iris.dms_insert();
DROP FUNCTION iris.dms_update();
DROP FUNCTION iris.dms_delete();

CREATE VIEW iris.dms AS
	SELECT d.name, geo_loc, controller, pin, notes, preset, aws_allowed,
	       aws_controlled, default_font
	FROM iris._dms dms
	JOIN iris._device_io d ON dms.name = d.name
	JOIN iris._device_preset p ON dms.name = p.name;

CREATE FUNCTION iris.dms_insert() RETURNS TRIGGER AS
	$dms_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._device_preset (name, preset)
	     VALUES (NEW.name, NEW.preset);
	INSERT INTO iris._dms (name, geo_loc, notes, aws_allowed,
	                       aws_controlled, default_font)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes,
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
	UPDATE iris._device_preset
	   SET preset = NEW.preset
	 WHERE name = OLD.name;
	UPDATE iris._dms
	   SET geo_loc = NEW.geo_loc,
	       notes = NEW.notes,
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
	DELETE FROM iris._device_preset WHERE name = OLD.name;
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

CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, p.camera,
	       p.preset_num, d.aws_allowed, d.aws_controlled, d.default_font,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.lat, l.lon
	FROM iris.dms d
	LEFT JOIN iris.camera_preset p ON d.preset = p.name
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON dms_view TO PUBLIC;

-- Add device preset records
INSERT INTO iris._device_preset SELECT name FROM iris._dms;
INSERT INTO iris._device_preset SELECT name FROM iris._ramp_meter;
INSERT INTO iris._device_preset SELECT name FROM iris._beacon;

CREATE TEMP TABLE _tpreset (
	name VARCHAR(10) PRIMARY KEY,
	dname VARCHAR(10) NOT NULL,
	camera VARCHAR(10) NOT NULL,
	preset_num INTEGER NOT NULL CHECK (preset_num > 0)
);

CREATE TEMP VIEW _tpview AS
SELECT name, camera FROM iris._dms UNION ALL
SELECT name, camera FROM iris._ramp_meter UNION ALL
SELECT name, camera FROM iris._beacon;

CREATE TEMP VIEW _tp2 AS
SELECT name, camera, row_number() OVER (PARTITION BY camera) AS rn
FROM _tpview
WHERE camera IS NOT NULL;

-- Add device presets
INSERT INTO _tpreset (name, dname, camera, preset_num)
	SELECT 'PRE_' || row_number() OVER (), _tp2.name, camera, rn + 4
	FROM _tp2;
INSERT INTO iris.camera_preset (name, camera, preset_num)
	SELECT name, camera, preset_num FROM _tpreset;
UPDATE iris._device_preset AS d
   SET preset = p.name
  FROM _tpreset AS p
 WHERE d.name = p.dname;
DELETE FROM _tpreset;

DROP VIEW _tp2;
DROP VIEW _tpview;
DROP TABLE _tpreset;

-- Finally, drop camera from device tables
ALTER TABLE iris._beacon DROP COLUMN camera;
ALTER TABLE iris._ramp_meter DROP COLUMN camera;
ALTER TABLE iris._dms DROP COLUMN camera;

CREATE VIEW camera_preset_view AS
	SELECT cp.name, camera, preset_num, direction, dp.name AS device
	FROM iris.camera_preset cp
	JOIN iris._device_preset dp ON cp.name = dp.preset;
GRANT SELECT ON camera_preset_view TO PUBLIC;

-- Add privileges for camera presets
INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
                            priv_d)
       VALUES ('prv_cp1', 'camera_tab', 'camera_preset(/.*)?', true, false,
               false, false);
INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
                            priv_d)
       VALUES ('prv_cp2', 'device_admin', 'camera_preset/.*', false, true,
               true, true);
