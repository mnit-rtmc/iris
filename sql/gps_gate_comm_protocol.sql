\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.comm_protocol(36,'GPS TAIP-TCP');

INSERT INTO iris.comm_protocol(37,'GPS TAIP-UDP');

INSERT INTO iris.comm_protocol(38,'GPS NMEA-TCP');

INSERT INTO iris.comm_protocol(39,'GPS NMEA-UDP');

INSERT INTO iris.comm_protocol(40,'GPS RedLion-TCP');

INSERT INTO iris.comm_protocol(41,'GPS RedLion-UDP');

INSERT INTO iris.comm_protocol(42,'Gate NDORv5-TCP');
