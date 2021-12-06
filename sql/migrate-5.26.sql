\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.25.0', '5.26.0');

INSERT INTO iris.sign_msg_source (bit, source) VALUES (14, 'exit warning');

COMMIT;
