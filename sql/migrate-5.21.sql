\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.20.0', '5.21.0');

INSERT INTO cap.event (code, description) VALUES
	('WCW', 'Wind Chill Warning'),
	('ZFY', 'Freezing Fog Advisory');

COMMIT;
