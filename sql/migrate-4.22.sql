\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.22.0'
	WHERE name = 'database_version';

-- delete client email system attributes
DELETE FROM iris.system_attribute WHERE name = 'email_sender_client';
DELETE FROM iris.system_attribute WHERE name = 'email_recipient_bugs';

-- add new encoder types
INSERT INTO iris.encoder_type VALUES (4, 'Axis MP4 axrtsp');
INSERT INTO iris.encoder_type VALUES (5, 'Axis MP4 axrtsphttp');
INSERT INTO iris.encoder_type VALUES (6, 'Generic MMS');

-- add camera control system attributes
INSERT INTO iris.system_attribute (name, value)
     VALUES ('camera_autoplay', true);
INSERT INTO iris.system_attribute (name, value)
     VALUES ('camera_stream_controls_enable', false);
INSERT INTO iris.system_attribute (name, value)
     VALUES ('camera_ptz_blind', true);
