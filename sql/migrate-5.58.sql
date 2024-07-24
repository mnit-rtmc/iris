\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.57.0', '5.58.0');

-- Add exent ID for DMS MSG RESET
INSERT INTO event.event_description (event_desc_id, description)
    VALUES (83, 'DMS MSG RESET'), (209, 'Client UPDATE PASSWORD');

COMMIT;
