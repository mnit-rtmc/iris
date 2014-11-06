\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.19.0'
	WHERE name = 'database_version';

-- Replace obsolete Vicon protocol (id 5) with Pelco P (formerly id 30)
UPDATE iris.comm_protocol SET description = 'Pelco P' WHERE id = 5;
DELETE FROM iris.comm_protocol WHERE id = 30;
