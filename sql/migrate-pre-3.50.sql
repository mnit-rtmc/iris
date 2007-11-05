DROP VIEW fake_det;
DROP TABLE fake_detector;

DROP VIEW iris_det;
DROP VIEW iris_detector;

ALTER TABLE detector DROP COLUMN fake;
ALTER TABLE detector ADD fake text;
UPDATE detector SET fake = '';
ALTER TABLE detector ALTER fake SET NOT NULL;

CREATE VIEW iris_det AS
    SELECT d."index" AS det_no, get_roadway_abbre(l.freeway) AS freeway_abbre, get_roadway_abbre(l.cross_street) AS crossstreet_abbre, get_roadway(l.freeway) AS freeway, get_dir(l.free_dir) AS free_dir, get_roadway(l.cross_street) AS cross_street, get_dir(l.cross_dir) AS cross_dir, boolean_converter(d.hov) AS hov, d."laneNumber" AS lane_number, d."fieldLength" AS field_length, ln.description AS lane_type, boolean_converter(d.abandoned) AS abandoned, boolean_converter(d."forceFail") AS force_fail, boolean_converter(c.active) AS active, d.fake, d.notes FROM (((detector d LEFT JOIN "location" l ON ((d."location" = l.vault_oid))) LEFT JOIN lane_type_description ln ON ((d."laneType" = ln.lane_type_id))) LEFT JOIN controller c ON ((d.controller = c.vault_oid))) ORDER BY d."index";

GRANT SELECT ON TABLE iris_det TO PUBLIC;

CREATE VIEW iris_detector AS
    SELECT d.pin, d."index" AS det_no, ld.line, c."drop", get_roadway_abbre(l.freeway) AS freeway_abbre, get_roadway_abbre(l.cross_street) AS crossstreet_abbre, get_roadway(l.freeway) AS freeway, get_dir(l.free_dir) AS free_dir, get_roadway(l.cross_street) AS cross_street, get_dir(l.cross_dir) AS cross_dir, boolean_converter(d.hov) AS hov, d."laneNumber" AS lane_number, d."fieldLength" AS field_length, ln.description AS lane_type, boolean_converter(d.abandoned) AS abandoned, boolean_converter(d."forceFail") AS force_fail, boolean_converter(c.active) AS active, d.fake, d.notes FROM ((((detector d LEFT JOIN "location" l ON ((d."location" = l.vault_oid))) LEFT JOIN lane_type_description ln ON ((d."laneType" = ln.lane_type_id))) LEFT JOIN controller c ON ((d.controller = c.vault_oid))) LEFT JOIN line_drop ld ON ((d.controller = ld.vault_oid))) ORDER BY d."index";

GRANT SELECT ON TABLE iris_detector TO PUBLIC;
