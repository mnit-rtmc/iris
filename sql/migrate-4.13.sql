\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.13.0'
	WHERE name = 'database_version';

UPDATE iris.graphic SET width = 2, pixels = 'AAAA' WHERE name = '12_full_32';

UPDATE iris.privilege SET patterm = 'gate_arm_array/.*/armStateNext'
	WHERE pattern = 'gate_arm_array/.*/armState';
