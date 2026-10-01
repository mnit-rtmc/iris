\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

-- Rename protocol 36 from 'Gate NDORv5' to 'NDOT Gate'
UPDATE iris.comm_protocol SET description = 'NDOT Gate' WHERE id = 36;
