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
