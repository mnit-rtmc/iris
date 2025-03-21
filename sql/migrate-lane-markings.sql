\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Remove lane markings
DROP VIEW controller_report;
DROP VIEW controller_device_view;
DROP VIEW iris.device_geo_loc_view;
CREATE VIEW iris.device_geo_loc_view AS
    SELECT name, geo_loc FROM iris._beacon UNION ALL
    SELECT name, geo_loc FROM iris._camera UNION ALL
    SELECT name, geo_loc FROM iris._dms UNION ALL
    SELECT name, geo_loc FROM iris._ramp_meter UNION ALL
    SELECT name, geo_loc FROM iris._tag_reader UNION ALL
    SELECT name, geo_loc FROM iris._weather_sensor UNION ALL
    SELECT d.name, geo_loc FROM iris._detector d
    JOIN iris.r_node rn ON d.r_node = rn.name UNION ALL
    SELECT g.name, geo_loc FROM iris._gate_arm g
    JOIN iris._gate_arm_array ga ON g.ga_array = ga.name;

CREATE VIEW controller_device_view AS
    SELECT cio.name, cio.controller, cio.pin, g.geo_loc,
           trim(l.roadway || ' ' || l.road_dir) AS corridor,
           trim(trim(' @' FROM l.cross_mod || ' ' || l.cross_street)
           || ' ' || l.cross_dir) AS cross_loc
      FROM iris.controller_io cio
      JOIN iris.device_geo_loc_view g ON cio.name = g.name
      JOIN geo_loc_view l ON g.geo_loc = l.name;
GRANT SELECT ON controller_device_view TO PUBLIC;

CREATE VIEW controller_report AS
    SELECT c.name, c.comm_link, c.drop_id, l.landmark, c.geo_loc, l.location,
           cabinet_style, d.name AS device, d.pin, d.cross_loc, d.corridor,
           c.notes
    FROM iris.controller c
    LEFT JOIN geo_loc_view l ON c.geo_loc = l.name
    LEFT JOIN controller_device_view d ON d.controller = c.name;
GRANT SELECT ON controller_report TO PUBLIC;

DROP VIEW lane_marking_view;
DROP VIEW iris.lane_marking;
DROP FUNCTION iris.lane_marking_insert();
DROP FUNCTION iris.lane_marking_update();
DROP TABLE iris._lane_marking;

DELETE FROM iris.resource_type WHERE name = 'lane_marking';

COMMIT;
