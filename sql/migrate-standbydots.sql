\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Uncomment and change if/as needed
-- SELECT iris.update_version('5.22.4', '5.22.5');

INSERT INTO iris.sign_msg_source (bit, source) VALUES (14, 'standby');

COMMIT;
