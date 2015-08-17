\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.25.0'
	WHERE name = 'database_version';

-- Reserve DR-500 comm protocol value
INSERT INTO iris.comm_protocol (id, description) VALUES (30, 'DR-500');

-- Reserve ADDCO comm protocol value
INSERT INTO iris.comm_protocol (id, description) VALUES (31, 'ADDCO');

-- Rename travel_time_max_legs to route_max_legs
UPDATE iris.system_attribute SET name = 'route_max_legs'
	WHERE name = 'travel_time_max_legs';

-- Rename travel_time_max_miles to route_max_miles
UPDATE iris.system_attribute SET name = 'route_max_miles'
	WHERE name = 'travel_time_max_miles';
