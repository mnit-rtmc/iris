\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

UPDATE iris.system_attribute SET value = '4.56.0'
	WHERE name = 'database_version';

-- Rearrange comm protocols for GPS
UPDATE iris.comm_link SET protocol=37 WHERE protocol=36;
UPDATE iris.comm_link SET protocol=38 WHERE protocol=39;
UPDATE iris.comm_link SET protocol=39 WHERE protocol=40;
UPDATE iris.comm_link SET protocol=39 WHERE protocol=41;
UPDATE iris.comm_link SET protocol=36 WHERE protocol=42;

UPDATE iris.comm_protocol SET description='Gate NDORv5' where id=36;
UPDATE iris.comm_protocol SET description='GPS TAIP' where id=37;
UPDATE iris.comm_protocol SET description='GPS NMEA' where id=38;
UPDATE iris.comm_protocol SET description='GPS RedLion' where id=39;

DELETE FROM iris.comm_protocol WHERE id=40;
DELETE FROM iris.comm_protocol WHERE id=41;
DELETE FROM iris.comm_protocol WHERE id=42;

-- Add GPS table, view and trigger
CREATE TABLE iris._gps (
	name VARCHAR(24) PRIMARY KEY,
	gps_enable BOOLEAN NOT NULL DEFAULT false,
	device_name VARCHAR(20) NOT NULL REFERENCES iris.geo_loc,
	device_class VARCHAR(20) NOT NULL,
	poll_datetime timestamp with time zone,
	sample_datetime timestamp with time zone,
	sample_lat double precision DEFAULT 0.0,
	sample_lon double precision DEFAULT 0.0,
	comm_status VARCHAR(25),
	error_status VARCHAR(50),
	jitter_tolerance_meters smallint DEFAULT 0
);

CREATE VIEW iris.gps AS
	SELECT g.name, d.controller, d.pin, g.gps_enable, g.device_name,
	       g.device_class, g.poll_datetime, g.sample_datetime, g.sample_lat,
	       g.sample_lon, g.comm_status, g.error_status,
	       g.jitter_tolerance_meters
	FROM iris._gps g
	JOIN iris._device_io d ON g.name = d.name;

CREATE FUNCTION iris.gps_insert() RETURNS trigger AS
	$gps_insert$
BEGIN
	INSERT INTO iris._gps
	            (name, gps_enable, device_name, device_class,
	             sample_datetime, sample_lat, sample_lon,
	             comm_status, error_status,
	             jitter_tolerance_meters)
	     VALUES (NEW.name, NEW.gps_enable, NEW.device_name,NEW.device_class,
	             NEW.sample_datetime, NEW.sample_lat, NEW.sample_lon,
	             NEW.comm_status, NEW.error_status,
	             NEW.jitter_tolerance_meters);
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, 0);
	RETURN NEW;
END;
$gps_insert$ LANGUAGE plpgsql;

CREATE TRIGGER gps_insert_trig
	INSTEAD OF INSERT ON iris.gps
	FOR EACH ROW EXECUTE PROCEDURE iris.gps_insert();

CREATE FUNCTION iris.gps_update() RETURNS trigger AS
	$gps_update$
BEGIN
        UPDATE iris._device_io
           SET controller = NEW.controller,
               pin = NEW.pin
         WHERE name = OLD.name;
	UPDATE iris._gps
	   SET gps_enable = NEW.gps_enable,
	       device_name = NEW.device_name,
	       device_class = NEW.device_class,
	       poll_datetime = NEW.poll_datetime,
	       sample_datetime = NEW.sample_datetime,
	       sample_lat = NEW.sample_lat,
	       sample_lon = NEW.sample_lon,
	       comm_status = NEW.comm_status,
	       error_status = NEW.error_status,
	       jitter_tolerance_meters = NEW.jitter_tolerance_meters
	 WHERE name = OLD.name;
	RETURN NEW;
END;
$gps_update$ LANGUAGE plpgsql;

CREATE TRIGGER gps_update_trig
	INSTEAD OF UPDATE ON iris.gps
	FOR EACH ROW EXECUTE PROCEDURE iris.gps_update();

CREATE FUNCTION iris.gps_delete() RETURNS TRIGGER AS
	$gps_delete$
BEGIN
	DELETE FROM iris._device_io WHERE name = OLD.name;
	IF FOUND THEN
		RETURN OLD;
	ELSE
		RETURN NULL;
	END IF;
END;
$gps_delete$ LANGUAGE plpgsql;

CREATE TRIGGER gps_delete_trig
	INSTEAD OF DELETE ON iris.gps
	FOR EACH ROW EXECUTE PROCEDURE iris.gps_delete();

-- Add comm_idle_disconnect_gps_sec system attribute
INSERT INTO iris.system_attribute (name, value)
	VALUES ('comm_idle_disconnect_gps_sec', '5');

COMMIT;
