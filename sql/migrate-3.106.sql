\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.106.0'
	WHERE name = 'database_version';

UPDATE event.event_description SET description = 'Incident DEBRIS'
	WHERE event_desc_id = 23;
UPDATE event.event_description SET description = 'Incident ROADWORK'
	WHERE event_desc_id = 24;
