\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.62.0', '4.63.0');

-- Add Banner DXM protocol
UPDATE iris.comm_protocol SET description = 'Banner DXM' WHERE id = 12;

INSERT INTO iris.lane_type (id, description, dcode) VALUES (17, 'Parking','PK');

-- Add parking area
CREATE TABLE iris.parking_area (
	name VARCHAR(20) PRIMARY KEY,
	geo_loc VARCHAR(20) NOT NULL REFERENCES iris.geo_loc(name),
	preset_1 VARCHAR(20) REFERENCES iris.camera_preset(name),
	preset_2 VARCHAR(20) REFERENCES iris.camera_preset(name),
	preset_3 VARCHAR(20) REFERENCES iris.camera_preset(name),
	-- static site data
	site_id VARCHAR(25) UNIQUE,
	time_stamp_static timestamp WITH time zone,
	relevant_highway VARCHAR(10),
	reference_post VARCHAR(10),
	exit_id VARCHAR(10),
	facility_name VARCHAR(30),
	street_adr VARCHAR(30),
	city VARCHAR(30),
	state VARCHAR(2),
	zip VARCHAR(10),
	time_zone VARCHAR(10),
	ownership VARCHAR(2),
	capacity INTEGER,
	low_threshold INTEGER,
	amenities VARCHAR(30),
	-- dynamic site data
	time_stamp timestamp WITH time zone,
	reported_available VARCHAR(8),
	true_available INTEGER,
	trend VARCHAR(8),
	open BOOLEAN,
	trust_data BOOLEAN,
	last_verification_check timestamp WITH time zone,
	verification_check_amplitude INTEGER
);

-- Add parking_area to sonar_type table
INSERT INTO iris.sonar_type (name) VALUES ('parking_area');

-- Add parking capabilities
INSERT INTO iris.capability (name, enabled)
	VALUES ('parking_admin', true),
	       ('parking_tab', true);

-- Add privileges for parking_area
INSERT INTO iris.privilege (name, capability, type_n, obj_n, group_n, attr_n,
                            write)
	VALUES ('PRV_015A', 'parking_admin', 'parking_area', '', '', '', true),
	       ('PRV_015B', 'parking_tab', 'parking_area', '', '', '', false);

COMMIT;
