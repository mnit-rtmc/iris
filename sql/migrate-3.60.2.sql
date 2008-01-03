SET SESSION AUTHORIZATION 'tms';

CREATE VIEW dms_message_view AS
	SELECT d.id AS dms, m.dms IS NULL AS global,
	line, message, abbrev, priority
	FROM dms d, dms_message m WHERE d.id = m.dms OR m.dms IS NULL;

REVOKE SELECT ON dms_message FROM PUBLIC;
GRANT SELECT ON dms_message_view TO PUBLIC;
