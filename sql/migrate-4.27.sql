\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.27.0'
	WHERE name = 'database_version';

-- add tollway column to toll_zone
ALTER TABLE iris.toll_zone ADD COLUMN tollway VARCHAR(16);

-- add tollway to toll_zone_view
CREATE OR REPLACE VIEW toll_zone_view AS
	SELECT name, start_id, end_id, tollway
	FROM iris.toll_zone;
GRANT SELECT ON toll_zone_view TO PUBLIC;

-- rename dms_op_status_enable sys attr to device_op_status_enable
UPDATE iris.system_attribute SET name = 'device_op_status_enable'
	WHERE name = 'dms_op_status_enable'
