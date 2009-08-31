\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.96.0' WHERE name = 'database_version';

INSERT INTO event.event_description VALUES (10, 'Comm QUEUE DRAINED');
INSERT INTO event.event_description VALUES (11, 'Comm POLL TIMEOUT');
INSERT INTO event.event_description VALUES (12, 'Comm PARSING ERROR');
INSERT INTO event.event_description VALUES (13, 'Comm CHECKSUM ERROR');
INSERT INTO event.event_description VALUES (14, 'Comm CONTROLLER ERROR');
