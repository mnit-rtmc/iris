\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.23.0'
	WHERE name = 'database_version';

-- add camera wiper system attribute
INSERT INTO iris.system_attribute (name, value)
     VALUES ('camera_wiper_precip_mm_hr', 8);

-- delete kml file enable system attribute
DELETE FROM iris.system_attribute WHERE name = 'kml_file_enable';
