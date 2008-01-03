SET SESSION AUTHORIZATION 'tms';

DROP VIEW green_detector_view;
DROP VIEW ramp_meter_view;

CREATE VIEW ramp_meter_view AS
	SELECT m.vault_oid, m.id, m.notes, d."index" AS green_det,
	m."controlMode" AS control_mode, m."singleRelease" AS single_release,
	m."storage", m."maxWait" AS max_wait, c.id AS camera,
	l.fwy, l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing, l.east_off, l.north_off
	FROM ramp_meter m
	JOIN location_view l ON m."location" = l.vault_oid
	LEFT JOIN detector d ON m.detector = d.vault_oid
	LEFT JOIN camera c ON m.camera = c.vault_oid;

GRANT SELECT ON ramp_meter_view TO PUBLIC;
GRANT SELECT ON dms_message TO PUBLIC;
