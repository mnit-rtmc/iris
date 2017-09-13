\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.58.0', '4.59.0');

DROP VIEW detector_view;
DROP VIEW detector_fail_view;

CREATE VIEW detector_fail_view AS SELECT DISTINCT ON (device_id)
	device_id, description AS fail_reason, event_date AS fail_date
	FROM event.detector_event de
	JOIN event.event_description ed ON de.event_desc_id = ed.event_desc_id
	ORDER BY device_id, event_id DESC;
GRANT SELECT ON detector_fail_view TO PUBLIC;

CREATE VIEW detector_view AS
	SELECT d.name, d.r_node, d.controller, c.comm_link, c.drop_id, d.pin,
	       iris.detector_label(l.rd, l.rdir, l.xst, l.cross_dir, l.xmod,
	       d.lane_type, d.lane_number, d.abandoned) AS label,
	       rnd.geo_loc, l.roadway, l.road_dir, l.cross_mod, l.cross_street,
	       l.cross_dir, d.lane_number, d.field_length,
	       ln.description AS lane_type, d.abandoned, d.force_fail,
	       df.fail_reason, df.fail_date, c.condition, d.fake, d.notes
	FROM (iris.detector d
	LEFT OUTER JOIN detector_fail_view df
		ON d.name = df.device_id AND force_fail = 't')
	LEFT JOIN iris.r_node rnd ON d.r_node = rnd.name
	LEFT JOIN geo_loc_view l ON rnd.geo_loc = l.name
	LEFT JOIN iris.lane_type ln ON d.lane_type = ln.id
	LEFT JOIN controller_view c ON d.controller = c.name;
GRANT SELECT ON detector_view TO PUBLIC;

-- Add group_n column to privilege table
ALTER TABLE iris.privilege ADD COLUMN group_n VARCHAR(16);
UPDATE iris.privilege SET group_n = '';
ALTER TABLE iris.privilege ALTER COLUMN group_n SET NOT NULL;

DROP VIEW role_privilege_view;

CREATE VIEW role_privilege_view AS
	SELECT role, type_n, obj_n, group_n, attr_n, write
	FROM iris.role
	JOIN iris.role_capability ON role.name = role_capability.role
	JOIN iris.capability ON role_capability.capability = capability.name
	JOIN iris.privilege ON privilege.capability = role_capability.capability
	WHERE role.enabled = 't' AND capability.enabled = 't';
GRANT SELECT ON role_privilege_view TO PUBLIC;

COMMIT;
