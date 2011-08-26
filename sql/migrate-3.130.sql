\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.130.0'
	WHERE name = 'database_version';

DROP VIEW sign_text_view;

ALTER TABLE iris.sign_text ADD COLUMN multi VARCHAR(64);
UPDATE iris.sign_text SET multi = message;
ALTER TABLE iris.sign_text ALTER COLUMN multi SET NOT NULL;
ALTER TABLE iris.sign_text DROP COLUMN message;

CREATE VIEW sign_text_view AS
	SELECT dms, local, line, multi, priority
	FROM iris.dms_sign_group dsg
	JOIN iris.sign_group sg ON dsg.sign_group = sg.name
	JOIN iris.sign_text st ON sg.name = st.sign_group;
GRANT SELECT ON sign_text_view TO PUBLIC;
