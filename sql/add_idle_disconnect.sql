\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

INSERT INTO iris.system_attribute (name, value) VALUES ('comm_idle_disconnect_dms_sec', '-1');
INSERT INTO iris.system_attribute (name, value) VALUES ('comm_idle_disconnect_modem_sec', '20');

