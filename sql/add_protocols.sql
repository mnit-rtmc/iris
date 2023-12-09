\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

-- Reserve protocol values
INSERT INTO iris.comm_protocol (id, description) VALUES (44, 'GPS Digi WR');
INSERT INTO iris.comm_protocol (id, description) VALUES (45, 'ONVIF PTZ');

COMMIT;
