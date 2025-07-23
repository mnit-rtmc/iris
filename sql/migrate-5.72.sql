\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.71.0', '5.72.0');

ALTER TABLE iris.device_action ADD CONSTRAINT device_action_msg_priority_check
    CHECK (msg_priority >= 1 AND msg_priority <= 15);

COMMIT;
