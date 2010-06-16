\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.119.0'
	WHERE name = 'database_version';

ALTER TABLE iris.i_user ADD COLUMN _dn VARCHAR(64);
UPDATE iris.i_user SET _dn = dn::VARCHAR(64);
ALTER TABLE iris.i_user DROP COLUMN dn;
ALTER TABLE iris.i_user ADD COLUMN dn VARCHAR(64);
UPDATE iris.i_user SET dn = _dn;
ALTER TABLE iris.i_user ALTER COLUMN dn SET NOT NULL;
ALTER TABLE iris.i_user DROP COLUMN _dn;

ALTER TABLE iris.i_user ADD COLUMN role VARCHAR(15) REFERENCES iris.role;
ALTER TABLE iris.i_user ADD COLUMN enabled BOOLEAN;
UPDATE iris.i_user SET enabled = false;
ALTER TABLE iris.i_user ALTER COLUMN enabled SET NOT NULL;
UPDATE iris.i_user SET enabled = true WHERE length(dn) > 0;

CREATE TABLE iris.capability (
	name VARCHAR(16) PRIMARY KEY,
	enabled BOOLEAN NOT NULL
);
INSERT INTO iris.capability (name, enabled)
	(SELECT name, enabled FROM iris.role);
ALTER TABLE iris.privilege ADD COLUMN capability VARCHAR(16)
	REFERENCES iris.capability;
UPDATE iris.privilege SET capability = role;
ALTER TABLE iris.privilege ALTER COLUMN capability SET NOT NULL;
ALTER TABLE iris.privilege DROP COLUMN role;

CREATE TABLE iris.role_capability (
	role VARCHAR(15) NOT NULL,
	capability VARCHAR(16) NOT NULL REFERENCES iris.capability
);

INSERT INTO iris.role_capability (role, capability)
	(SELECT i_user, role FROM iris.i_user_role);
DROP TABLE iris.i_user_role;

DELETE FROM iris.role;
INSERT INTO iris.role (name, enabled)
	(SELECT name, true FROM iris.i_user WHERE i_user.enabled = true);
UPDATE iris.i_user SET role = name WHERE enabled = true;
DELETE FROM iris.role_capability WHERE role NOT IN (SELECT name FROM iris.role);

ALTER TABLE iris.role_capability ADD CONSTRAINT role_capability_role_fkey
	FOREIGN KEY (role) REFERENCES iris.role;

INSERT INTO iris.privilege (name, capability, pattern, priv_r, priv_w, priv_c, priv_d) VALUES ('prv_cap', 'login', 'capability(/.*)?', true, false, false, false);
