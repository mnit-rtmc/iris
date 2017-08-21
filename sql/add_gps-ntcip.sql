\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.system_attribute (name, value) VALUES ('gps_ntcip_enable', false);
INSERT INTO iris.system_attribute (name, value) VALUES ('gps_ntcip_jitter_m', '100');

