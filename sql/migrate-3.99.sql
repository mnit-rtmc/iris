\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE system_attribute SET value = '3.99.0' WHERE name = 'database_version';
