\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

-- Reserve protocol value for GPS Digi WR modem
INSERT INTO iris.comm_protocol (id, description) VALUES (44, 'GPS Digi RW');

COMMIT;
