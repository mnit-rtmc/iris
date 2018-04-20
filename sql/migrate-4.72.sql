\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.71.0', '4.72.0');

-- Add capability to role_privilege_view
DROP VIEW role_privilege_view;
CREATE VIEW role_privilege_view AS
	SELECT role, role_capability.capability, type_n, obj_n, group_n, attr_n,
	       write
	FROM iris.role
	JOIN iris.role_capability ON role.name = role_capability.role
	JOIN iris.capability ON role_capability.capability = capability.name
	JOIN iris.privilege ON privilege.capability = capability.name
	WHERE role.enabled = 't' AND capability.enabled = 't';
GRANT SELECT ON role_privilege_view TO PUBLIC;

-- Add i_user_view
CREATE VIEW i_user_view AS
	SELECT name, full_name, dn, role, enabled
	FROM iris.i_user;
GRANT SELECT ON i_user_view TO PUBLIC;

-- Add toll_density_alpha and toll_density_beta system attributes
INSERT INTO iris.system_attribute (name, value)
	VALUES ('toll_density_alpha', '0.045');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('toll_density_beta', '1.1');

-- Drop old view
DROP VIEW toll_zone_view;

-- Add columns to toll_zone table
ALTER TABLE iris.toll_zone ADD COLUMN alpha REAL;
ALTER TABLE iris.toll_zone ADD COLUMN beta REAL;
ALTER TABLE iris.toll_zone ADD COLUMN max_price REAL;

-- Create new toll_zone_view
CREATE VIEW toll_zone_view AS
	SELECT name, start_id, end_id, tollway, alpha, beta, max_price
	FROM iris.toll_zone;
GRANT SELECT ON toll_zone_view TO PUBLIC;

-- Add testing controller condition
INSERT INTO iris.condition (id, description) VALUES (4, 'Testing');

COMMIT;
