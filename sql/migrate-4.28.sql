\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.28.0'
	WHERE name = 'database_version';

-- add toll price limit system attributes
INSERT INTO iris.system_attribute (name, value) VALUES ('toll_min_price', '0.25');
INSERT INTO iris.system_attribute (name, value) VALUES ('toll_max_price', '8');
