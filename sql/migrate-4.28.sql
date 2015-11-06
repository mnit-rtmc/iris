\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.28.0'
	WHERE name = 'database_version';

-- add toll price limit system attributes
INSERT INTO iris.system_attribute (name, value) VALUES ('toll_min_price', '0.25');
INSERT INTO iris.system_attribute (name, value) VALUES ('toll_max_price', '8');

-- replace sign_message scheduled with source
DELETE FROM iris.sign_message;
ALTER TABLE iris.sign_message ADD COLUMN source INTEGER;
ALTER TABLE iris.sign_message ALTER COLUMN source SET NOT NULL;
ALTER TABLE iris.sign_message DROP COLUMN scheduled;
