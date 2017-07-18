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

COMMIT;
