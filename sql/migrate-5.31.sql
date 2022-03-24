\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.30.0', '5.31.0');

-- Add message source for standby tags
INSERT INTO iris.sign_msg_source (bit, source) VALUES (15, 'standby');

COMMIT;
