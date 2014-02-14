\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.11.0'
	WHERE name = 'database_version';

DELETE FROM iris.meter_algorithm WHERE id = 4;

-- Create new beacon table to replace warning_sign
CREATE TABLE iris._beacon (
	name VARCHAR(10) PRIMARY KEY,
	geo_loc VARCHAR(20) REFERENCES iris.geo_loc(name),
	notes text NOT NULL,
	message text NOT NULL,
	camera VARCHAR(10) REFERENCES iris._camera(name)
);

ALTER TABLE iris._beacon ADD CONSTRAINT _beacon_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.beacon AS SELECT
	b.name, geo_loc, controller, pin, notes, message, camera
	FROM iris._beacon b JOIN iris._device_io d ON b.name = d.name;

CREATE FUNCTION iris.beacon_update() RETURNS TRIGGER AS
	$beacon_update$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO iris._device_io (name, controller, pin)
            VALUES (NEW.name, NEW.controller, NEW.pin);
        INSERT INTO iris._beacon (name, geo_loc, notes, message, camera)
            VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.message, NEW.camera);
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
	UPDATE iris._device_io SET controller = NEW.controller, pin = NEW.pin
	WHERE name = OLD.name;
        UPDATE iris._beacon
		SET geo_loc = NEW.geo_loc,
	            notes = NEW.notes,
	            message = NEW.message,
	            camera = NEW.camera
	WHERE name = OLD.name;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM iris._device_io WHERE name = OLD.name;
        IF FOUND THEN
            RETURN OLD;
        ELSE
            RETURN NULL;
	END IF;
    END IF;
    RETURN NEW;
END;
$beacon_update$ LANGUAGE plpgsql;

CREATE TRIGGER beacon_update_trig
    INSTEAD OF INSERT OR UPDATE OR DELETE ON iris.beacon
    FOR EACH ROW EXECUTE PROCEDURE iris.beacon_update();

CREATE TEMP TABLE temp_beacon AS
	SELECT name, geo_loc, controller, pin, notes, message, camera
	FROM iris.warning_sign;

-- Delete warning signs
DELETE FROM iris.warning_sign;

-- Copy warning sign data to new beacon table
INSERT INTO iris.beacon (name, geo_loc, controller, pin, notes, message, camera)
	(SELECT name, geo_loc, controller, pin, notes, message, camera
	 FROM temp_beacon);

DROP TABLE temp_beacon;

CREATE VIEW beacon_view AS
	SELECT b.name, b.notes, b.message, b.camera, b.geo_loc,
	l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.lat, l.lon,
	b.controller, b.pin, ctr.comm_link, ctr.drop_id, ctr.active
	FROM iris.beacon b
	LEFT JOIN geo_loc_view l ON b.geo_loc = l.name
	LEFT JOIN iris.controller ctr ON b.controller = ctr.name;
GRANT SELECT ON beacon_view TO PUBLIC;

-- Update controller report
DROP VIEW controller_report;
DROP VIEW controller_device_view;
DROP VIEW iris.controller_device;
DROP VIEW iris.controller_warning_sign;

CREATE VIEW iris.controller_beacon AS
	SELECT dio.name, dio.controller, dio.pin, b.geo_loc
	FROM iris._device_io dio
	JOIN iris.beacon b ON dio.name = b.name;

CREATE VIEW iris.controller_device AS
	SELECT * FROM iris.controller_dms UNION ALL
	SELECT * FROM iris.controller_lane_marking UNION ALL
	SELECT * FROM iris.controller_weather_sensor UNION ALL
	SELECT * FROM iris.controller_lcs UNION ALL
	SELECT * FROM iris.controller_meter UNION ALL
	SELECT * FROM iris.controller_beacon UNION ALL
	SELECT * FROM iris.controller_camera UNION ALL
	SELECT * FROM iris.controller_gate_arm;

CREATE VIEW controller_device_view AS
	SELECT d.name, d.controller, d.pin, d.geo_loc,
	trim(l.roadway || ' ' || l.road_dir) AS corridor,
	trim(trim(' @' FROM l.cross_mod || ' ' || l.cross_street)
		|| ' ' || l.cross_dir) AS cross_loc
	FROM iris.controller_device d
	JOIN geo_loc_view l ON d.geo_loc = l.name;
GRANT SELECT ON controller_device_view TO PUBLIC;

CREATE VIEW controller_report AS
	SELECT c.name, c.comm_link, c.drop_id, cab.mile, cab.geo_loc,
	trim(l.roadway || ' ' || l.road_dir) || ' ' || l.cross_mod || ' ' ||
		trim(l.cross_street || ' ' || l.cross_dir) AS "location",
	cab.style AS "type", d.name AS device, d.pin,
	d.cross_loc, d.corridor, c.notes
	FROM iris.controller c
	LEFT JOIN iris.cabinet cab ON c.cabinet = cab.name
	LEFT JOIN geo_loc_view l ON cab.geo_loc = l.name
	LEFT JOIN controller_device_view d ON d.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

-- Drop warning sign tables and views
DROP VIEW warning_sign_view;
DROP VIEW iris.warning_sign;
DROP TABLE iris._warning_sign;

-- Replace warning_sign privilege patterns with beacon
UPDATE iris.privilege SET pattern = replace(pattern, 'warning_sign', 'beacon');
