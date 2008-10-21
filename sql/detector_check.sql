-- README: this script can be used to check for problems related to the r_node
--         and detector changes in v3.79.  It should be run before running the
--         migrate-3.79 script.  Each SELECT statement should return 0 rows,
--         unless there is a problem.
--
-- Search for detectors with notes longer than 32 characters
SELECT det_id, notes FROM detector_view WHERE char_length(notes) > 32;

-- Search for detectors which are assigned to more than one r_node
SELECT detector FROM r_node_detector GROUP BY detector HAVING count(*) > 1
	ORDER BY detector;

-- Search for detectors which are not assigned to an r_node
SELECT det_id, label FROM detector_view LEFT JOIN r_node_detector rnd
	ON det_id = rnd.detector WHERE r_node IS NULL AND label != 'FUTURE';

-- Search for detectors whose geo_loc does not match the r_node geo_loc
SELECT rnd.r_node, rnd.detector, label FROM r_node_detector rnd
	JOIN r_node r ON rnd.r_node = r.vault_oid
	JOIN geo_loc rg ON r.geo_loc = rg.name
	JOIN detector_view d ON rnd.detector = d.det_id
	JOIN geo_loc dg ON d.geo_loc = dg.name
	WHERE rg.freeway != dg.freeway OR rg.free_dir != dg.free_dir OR
	rg.cross_street != dg.cross_street OR rg.cross_dir != dg.cross_dir OR
	rg.cross_mod != dg.cross_mod ORDER BY detector;
