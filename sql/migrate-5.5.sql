\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.4.0', '5.5.0');

-- Drop width and height from lane_use_multi
ALTER TABLE iris.lane_use_multi DROP COLUMN width;
ALTER TABLE iris.lane_use_multi DROP COLUMN height;

CREATE VIEW lane_use_multi_view AS
	SELECT name, indication, msg_num, quick_message
	FROM iris.lane_use_multi;
GRANT SELECT ON lane_use_multi_view TO PUBLIC;

COMMIT;
