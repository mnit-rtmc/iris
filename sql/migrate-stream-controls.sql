\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.system_attribute (name, value) VALUES ('camera_autoplay', true);
INSERT INTO iris.system_attribute (name, value) VALUES ('camera_stream_controls_enable', false);

