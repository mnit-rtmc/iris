\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.24.0'
	WHERE name = 'database_version';

-- add system attributes
INSERT INTO iris.system_attribute (name, value)
	VALUES ('map_extent_name_initial', 'Home');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('speed_limit_min_mph', '45');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('speed_limit_default_mph', '55');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('speed_limit_max_mph', '75');

-- increase size of sign_message multi
ALTER TABLE iris.sign_message ALTER COLUMN multi TYPE VARCHAR(512);

-- add toll zone table
CREATE TABLE iris.toll_zone (
	name VARCHAR(20) PRIMARY KEY,
	start_id VARCHAR(10) REFERENCES iris.r_node(station_id),
	end_id VARCHAR(10) REFERENCES iris.r_node(station_id)
);

-- add toll zone view
CREATE VIEW toll_zone_view AS
	SELECT name, start_id, end_id
	FROM iris.toll_zone;
GRANT SELECT ON toll_zone_view TO PUBLIC;

-- add privileges for toll zones
INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
                            priv_d)
       VALUES ('prv_tz1', 'detection', 'toll_zone(/.*)?', true, false,
               false, false);
INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c,
                            priv_d)
       VALUES ('prv_tz2', 'device_admin', 'toll_zone/.*', false, true,
               true, true);
