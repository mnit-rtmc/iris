\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.110.0'
	WHERE name = 'database_version';
UPDATE iris.system_attribute SET name = 'dms_page_on_selection_enable'
	WHERE name = 'dms_pgontime_selection_enable';
UPDATE iris.system_attribute SET name = 'dms_page_on_default_secs'
	WHERE name = 'dms_page_on_secs';
UPDATE iris.system_attribute SET name = 'dms_page_off_default_secs'
	WHERE name = 'dms_page_off_secs';

ALTER TABLE iris.quick_message ADD COLUMN sign_group VARCHAR(16);
ALTER TABLE iris.quick_message ADD CONSTRAINT quick_message_sign_group_fkey
	FOREIGN KEY (sign_group) REFERENCES iris.sign_group;
