\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.13.0', '5.14.0');

-- Add OCC SPIKE event type
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (98, 'OCC SPIKE');

-- Add detector occ spike enable attribute
INSERT INTO iris.system_attribute (name, value)
	VALUES ('detector_occ_spike_enable', 'true');

COMMIT;
