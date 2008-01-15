SET SESSION AUTHORIZATION 'tms';

CREATE VIEW camera_view AS
	SELECT c.id, ld.line, ld."drop", ctr.active, c.notes,
	c.encoder, c.encoder_channel, c.nvr,
	l.freeway, l.free_dir, l.cross_mod, l.cross_street, l.cross_dir,
	l.easting, l.northing, l.east_off, l.north_off
	FROM camera c
	JOIN location_view l ON c."location" = l.vault_oid
	LEFT JOIN line_drop_view ld ON c.controller = ld.vault_oid
	LEFT JOIN controller ctr ON c.controller = ctr.vault_oid;

GRANT SELECT ON camera_view TO PUBLIC;
