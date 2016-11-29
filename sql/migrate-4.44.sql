\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.44.0'
	WHERE name = 'database_version';

UPDATE iris.meter_algorithm SET description = 'SZM (obsolete)' WHERE id = 2;

-- Reserve comm protocols for GPS and NDOR gate
INSERT INTO iris.comm_protocol(36, 'GPS TAIP-TCP');
INSERT INTO iris.comm_protocol(37, 'GPS TAIP-UDP');
INSERT INTO iris.comm_protocol(38, 'GPS NMEA-TCP');
INSERT INTO iris.comm_protocol(39, 'GPS NMEA-UDP');
INSERT INTO iris.comm_protocol(40, 'GPS RedLion-TCP');
INSERT INTO iris.comm_protocol(41, 'GPS RedLion-UDP');
INSERT INTO iris.comm_protocol(42, 'Gate NDORv5-TCP');
