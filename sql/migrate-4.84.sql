\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.83.0', '4.84.0');

-- Add road affix table
CREATE TABLE iris.road_affix (
	name VARCHAR(12) PRIMARY KEY,
	prefix BOOLEAN NOT NULL,
	fixup VARCHAR(12)
);

-- Add road_affix to sonar type lut
INSERT INTO iris.sonar_type (name) VALUES ('road_affix');

-- Add default values to privilege columns
ALTER TABLE iris.privilege ALTER COLUMN obj_n SET DEFAULT ''::VARCHAR;
ALTER TABLE iris.privilege ALTER COLUMN group_n SET DEFAULT ''::VARCHAR;
ALTER TABLE iris.privilege ALTER COLUMN attr_n SET DEFAULT ''::VARCHAR;

-- Add privileges for road_affix
INSERT INTO iris.privilege (name, capability, type_n, write)
	(SELECT 'prv_ra' || ROW_NUMBER() OVER (ORDER BY name), capability,
	 'road_affix', write
	 FROM iris.privilege
	 WHERE type_n = 'road');

COMMIT;
