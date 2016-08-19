\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.37.0'
	WHERE name = 'database_version';

-- Add camera_auth_ system attributes
INSERT INTO iris.system_attribute('camera_auth_password', '');
INSERT INTO iris.system_attribute('camera_auth_username', '');

-- Add stream_type look-up table
CREATE TABLE iris.stream_type (
	id integer PRIMARY KEY,
	description VARCHAR(20) NOT NULL
);
COPY iris.stream_type (id, description) FROM stdin;
0	UNKNOWN
1	MJPEG
2	MPEG4
3	H264
4	H265
\.
