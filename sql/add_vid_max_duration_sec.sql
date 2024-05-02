\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.system_attribute (name, value) VALUES ('vid_max_duration_sec', '0');

COMMIT;
