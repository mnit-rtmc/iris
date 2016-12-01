\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.44.0'
	WHERE name = 'database_version';

UPDATE iris.meter_algorithm SET description = 'SZM (obsolete)' WHERE id = 2;

-- Reserve comm protocols for GPS and NDOR gate
INSERT INTO iris.comm_protocol (id, description)
	VALUES (36, 'GPS TAIP-TCP');
INSERT INTO iris.comm_protocol (id, description)
	VALUES (37, 'GPS TAIP-UDP');
INSERT INTO iris.comm_protocol (id, description)
	VALUES (38, 'GPS NMEA-TCP');
INSERT INTO iris.comm_protocol (id, description)
	VALUES (39, 'GPS NMEA-UDP');
INSERT INTO iris.comm_protocol (id, description)
	VALUES (40, 'GPS RedLion-TCP');
INSERT INTO iris.comm_protocol (id, description)
	VALUES (41, 'GPS RedLion-UDP');
INSERT INTO iris.comm_protocol (id, description)
	VALUES (42, 'Gate NDORv5-TCP');

-- Add DMS type LUT
CREATE TABLE iris.dms_type (
	id INTEGER PRIMARY KEY,
	description VARCHAR(32) NOT NULL
);

-- Add sign_config table
CREATE TABLE iris.sign_config (
	name VARCHAR(12) PRIMARY KEY,
	dms_type INTEGER NOT NULL REFERENCES iris.dms_type,
	portable BOOLEAN NOT NULL,
	technology VARCHAR(12) NOT NULL,
	sign_access VARCHAR(12) NOT NULL,
	legend VARCHAR(12) NOT NULL,
	beacon_type VARCHAR(32) NOT NULL,
	face_width INTEGER NOT NULL,
	face_height INTEGER NOT NULL,
	border_horiz INTEGER NOT NULL,
	border_vert INTEGER NOT NULL,
	pitch_horiz INTEGER NOT NULL,
	pitch_vert INTEGER NOT NULL,
	pixel_width INTEGER NOT NULL,
	pixel_height INTEGER NOT NULL,
	char_width INTEGER NOT NULL,
	char_height INTEGER NOT NULL,
	default_font VARCHAR(16) REFERENCES iris.font
);

-- Add sign_config_view
CREATE VIEW sign_config_view AS
	SELECT name, description AS dms_type, portable, technology, sign_access,
	       legend, beacon_type, face_width, face_height, border_horiz,
	       border_vert, pitch_horiz, pitch_vert, pixel_width, pixel_height,
	       char_width, char_height, default_font
	FROM iris.sign_config
	JOIN iris.dms_type ON sign_config.dms_type = dms_type.id;
GRANT SELECT ON sign_config_view TO PUBLIC;

-- Populate dms_type LUT
COPY iris.dms_type (id, description) FROM stdin;
0	Unknown
1	Other
2	BOS (blank-out sign)
3	CMS (changeable message sign)
4	VMS Character-matrix
5	VMS Line-matrix
6	VMS Full-matrix
\.

-- Add sign_config to sonar_type table
INSERT INTO iris.sonar_type (name) VALUES ('sign_config');

-- Add sign_config privileges
INSERT INTO iris.privilege (name, capability, type_n, obj_n, attr_n, write)
	VALUES ('prv_cfg1', 'dms_admin', 'sign_config', '', '', true);
INSERT INTO iris.privilege (name, capability, type_n, obj_n, attr_n, write)
	VALUES ('prv_cfg2', 'dms_tab', 'sign_config', '', '', false);

-- Drop DMS views and functions
DROP VIEW dms_view;
DROP VIEW iris.dms;
DROP FUNCTION iris.dms_insert();
DROP FUNCTION iris.dms_update();
DROP FUNCTION iris.dms_delete();

-- Drop default_font from dms
ALTER TABLE iris._dms DROP COLUMN default_font;

-- Add sign_config to dms
ALTER TABLE iris._dms ADD COLUMN sign_config VARCHAR(12)
	REFERENCES iris.sign_config;

-- Create iris.dms view
CREATE VIEW iris.dms AS
	SELECT d.name, geo_loc, controller, pin, notes, beacon, preset,
	       aws_allowed, aws_controlled, sign_config
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
	INSERT INTO iris._dms (name, geo_loc, notes, beacon, aws_allowed,
	                       aws_controlled, sign_config)
	     VALUES (NEW.name, NEW.geo_loc, NEW.notes, NEW.beacon,
	             NEW.aws_allowed, NEW.aws_controlled, NEW.sign_config);
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
	       beacon = NEW.beacon,
	       aws_allowed = NEW.aws_allowed,
	       aws_controlled = NEW.aws_controlled,
	       sign_config = NEW.sign_config
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

-- Create updated dms_view
CREATE VIEW dms_view AS
	SELECT d.name, d.geo_loc, d.controller, d.pin, d.notes, d.beacon,
	       p.camera, p.preset_num, d.aws_allowed, d.aws_controlled,
	       d.sign_config, sc.default_font,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.lat, l.lon
	FROM iris.dms d
	LEFT JOIN iris.camera_preset p ON d.preset = p.name
	LEFT JOIN geo_loc_view l ON d.geo_loc = l.name
	LEFT JOIN sign_config_view sc ON d.sign_config = sc.name;
GRANT SELECT ON dms_view TO PUBLIC;
