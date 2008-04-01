SET SESSION AUTHORIZATION 'tms';

DROP VIEW camera_view;

ALTER TABLE camera ADD COLUMN publish boolean;
UPDATE camera SET publish = 'f';
ALTER TABLE camera ALTER publish SET NOT NULL;

ALTER TABLE video_monitor ADD COLUMN restricted boolean;
UPDATE video_monitor SET restricted = 'f';
ALTER TABLE video_monitor ALTER restricted SET NOT NULL;

CREATE VIEW camera_view AS
	SELECT c.id, ld.line, ld."drop", ctr.active, c.notes,
	c.encoder, c.encoder_channel, c.nvr, c.publish,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing, l.east_off, l.north_off
	FROM camera c
	JOIN location_view l ON c."location" = l.vault_oid
	LEFT JOIN line_drop_view ld ON c.controller = ld.vault_oid
	LEFT JOIN controller ctr ON c.controller = ctr.vault_oid;

GRANT SELECT ON camera_view TO PUBLIC;
