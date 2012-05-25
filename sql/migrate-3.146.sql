\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.146.0'
	WHERE name = 'database_version';

INSERT INTO iris.comm_protocol (id, description) values (18, 'EIS RTMS');
INSERT INTO iris.comm_protocol (id, description) values (19, 'Infotek Wizard');
INSERT INTO iris.comm_protocol (id, description) values (20, 'Sensys');
INSERT INTO iris.comm_protocol (id, description) values (21, 'PeMS');
INSERT INTO iris.comm_protocol (id, description) values (22, 'SSI');
INSERT INTO iris.comm_protocol (id, description) values (23, 'CHP Incidents');
INSERT INTO iris.comm_protocol (id, description) values (24, 'URMS');
