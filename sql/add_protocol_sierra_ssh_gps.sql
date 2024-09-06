\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

-- Reserve protocol value for Sierra SSH GPS modem
INSERT INTO iris.comm_protocol (id, description) VALUES (46, 'Sierra SSH GPS');

COMMIT;
