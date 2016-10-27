\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.system_attribute (name, value) VALUES ('camera_ptz_axis_comport', 1);
INSERT INTO iris.system_attribute (name, value) VALUES ('camera_ptz_axis_reset', '');
INSERT INTO iris.system_attribute (name, value) VALUES ('camera_ptz_axis_wipe', '');

