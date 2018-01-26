\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.61.0', '4.62.0');

-- insert camera_kbd_panasonic_enable system attribute
INSERT INTO iris.system_attribute (name, value)
	VALUES ('camera_kbd_panasonic_enable', 'false');

CREATE INDEX ON event.sign_event(event_date);

-- Add hgap / vgap to monitor_style
ALTER TABLE iris.monitor_style ADD COLUMN hgap INTEGER;
UPDATE iris.monitor_style SET hgap = 0;
ALTER TABLE iris.monitor_style ALTER COLUMN hgap SET NOT NULL;
ALTER TABLE iris.monitor_style ADD COLUMN vgap INTEGER;
UPDATE iris.monitor_style SET vgap = 0;
ALTER TABLE iris.monitor_style ALTER COLUMN vgap SET NOT NULL;

CREATE OR REPLACE VIEW monitor_style_view AS
	SELECT name, force_aspect, accent, font_sz, title_bar, hgap, vgap
	FROM iris.monitor_style;
GRANT SELECT ON monitor_style_view TO PUBLIC;

COMMIT;
