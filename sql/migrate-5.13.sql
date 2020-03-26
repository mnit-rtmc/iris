\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.12.0', '5.13.0');

CREATE VIEW sign_group_text_view AS
	SELECT sign_group, line, multi, rank
	FROM iris.sign_group sg
	JOIN iris.sign_text st ON sg.name = st.sign_group;
GRANT SELECT ON sign_group_text_view TO PUBLIC;

COMMIT;
