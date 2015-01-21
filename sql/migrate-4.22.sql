\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.22.0'
	WHERE name = 'database_version';

-- delete client email system attributes
DELETE FROM iris.system_attribute WHERE name = 'email_sender_client';
DELETE FROM iris.system_attribute WHERE name = 'email_recipient_bugs';
