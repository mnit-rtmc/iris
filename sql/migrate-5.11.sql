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

-- Add flow status look up table
CREATE TABLE iris.flow_status (
	id INTEGER PRIMARY KEY,
	description VARCHAR(8) NOT NULL
);

COPY iris.flow_status (id, description) FROM stdin;
0	FAILED
1	STARTING
2	PLAYING
\.

-- Add video flow
CREATE TABLE iris._flow (
	name VARCHAR(12) PRIMARY KEY,
	restricted BOOLEAN NOT NULL,
	loc_overlay BOOLEAN NOT NULL,
	quality INTEGER NOT NULL REFERENCES iris.encoding_quality,
	camera VARCHAR(20) REFERENCES iris._camera,
	mon_num INTEGER,
	address INET,
	port INTEGER CHECK (port > 0 AND port <= 65535),
	status INTEGER NOT NULL REFERENCES iris.flow_status,
	CONSTRAINT camera_or_monitor CHECK (camera IS NULL OR mon_num IS NULL)
);

ALTER TABLE iris._flow ADD CONSTRAINT _flow_fkey
	FOREIGN KEY (name) REFERENCES iris._device_io(name) ON DELETE CASCADE;

CREATE VIEW iris.flow AS
	SELECT f.name, controller, pin, restricted, loc_overlay, quality,
	       camera, mon_num, address, port, status
	FROM iris._flow f JOIN iris._device_io d ON f.name = d.name;

CREATE FUNCTION iris.flow_insert() RETURNS TRIGGER AS
	$flow_insert$
BEGIN
	INSERT INTO iris._device_io (name, controller, pin)
	     VALUES (NEW.name, NEW.controller, NEW.pin);
	INSERT INTO iris._flow (name, restricted, loc_overlay, quality, camera,
	                        mon_num, address, port, status)
	     VALUES (NEW.name, NEW.restricted, NEW.loc_overlay, NEW.quality,
	             NEW.camera, NEW.mon_num, NEW.address, NEW.port,NEW.status);
	RETURN NEW;
END;
$flow_insert$ LANGUAGE plpgsql;

CREATE TRIGGER flow_insert_trig
    INSTEAD OF INSERT ON iris.flow
    FOR EACH ROW EXECUTE PROCEDURE iris.flow_insert();

CREATE FUNCTION iris.flow_update() RETURNS TRIGGER AS
	$flow_update$
BEGIN
	UPDATE iris._device_io
	   SET controller = NEW.controller,
	       pin = NEW.pin
	 WHERE name = OLD.name;
	UPDATE iris._flow
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
$flow_update$ LANGUAGE plpgsql;

CREATE TRIGGER flow_update_trig
    INSTEAD OF UPDATE ON iris.flow
    FOR EACH ROW EXECUTE PROCEDURE iris.flow_update();

CREATE TRIGGER flow_delete_trig
    INSTEAD OF DELETE ON iris.flow
    FOR EACH ROW EXECUTE PROCEDURE iris.device_delete();

CREATE VIEW flow_view AS
	SELECT f.name, f.controller, pin, condition, comm_link, restricted,
	       loc_overlay, eq.description AS quality, camera, mon_num, address,
	       port, s.description AS status
	FROM iris.flow f
	JOIN iris.flow_status s ON f.status = s.id
	LEFT JOIN controller_view ctr ON controller = ctr.name
	LEFT JOIN iris.encoding_quality eq ON f.quality = eq.id;
GRANT SELECT ON flow_view TO PUBLIC;

COMMIT;
