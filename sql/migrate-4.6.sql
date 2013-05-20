\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.6.0'
	WHERE name = 'database_version';

DROP VIEW sign_text_view;

ALTER TABLE ONLY iris.sign_text
 	DROP CONSTRAINT sign_text_priority;
ALTER TABLE iris.sign_text ADD COLUMN rank smallint;
UPDATE iris.sign_text SET rank = priority;
ALTER TABLE iris.sign_text ALTER COLUMN rank SET NOT NULL;
ALTER TABLE iris.sign_text DROP COLUMN priority;
ALTER TABLE ONLY iris.sign_text
	ADD CONSTRAINT sign_text_rank CHECK ((rank >= 1) AND (rank <= 99));

CREATE VIEW sign_text_view AS
	SELECT dms, local, line, multi, rank
	FROM iris.dms_sign_group dsg
	JOIN iris.sign_group sg ON dsg.sign_group = sg.name
	JOIN iris.sign_text st ON sg.name = st.sign_group;
GRANT SELECT ON sign_text_view TO PUBLIC;

