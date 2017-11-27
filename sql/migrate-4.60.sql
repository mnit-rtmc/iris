\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.59.0', '4.60.0');

-- delete camera_blank_url system attribute
DELETE FROM iris.system_attribute WHERE name = 'camera_blank_url';

-- Add title_bar column to monitor_style table
ALTER TABLE iris.monitor_style ADD COLUMN title_bar BOOLEAN;
UPDATE iris.monitor_style SET title_bar = 't';
ALTER TABLE iris.monitor_style ALTER COLUMN title_bar SET NOT NULL;

-- Update monitor_style_view
DROP VIEW monitor_style_view;
CREATE VIEW monitor_style_view AS
	SELECT name, force_aspect, accent, font_sz, title_bar
	FROM iris.monitor_style;
GRANT SELECT ON monitor_style_view TO PUBLIC;

COMMIT;
