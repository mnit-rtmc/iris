SET SESSION AUTHORIZATION 'tms';

DROP VIEW dms_view;

ALTER TABLE dms ADD COLUMN travel text;
UPDATE dms SET travel = '';
ALTER TABLE dms ALTER COLUMN travel SET NOT NULL;

UPDATE dms SET travel = 'FREEWAY TIME TO:[nl]' || replace(travel1, '%TIME', '[ttS' || TO_CHAR(dest1, 'FM9999') || ']') || '[nl]' || replace(travel2, '%TIME', '[ttS' || TO_CHAR(dest2, 'FM9999') || ']') WHERE dest1 > 0;

ALTER TABLE dms DROP COLUMN travel1;
ALTER TABLE dms DROP COLUMN dest1;
ALTER TABLE dms DROP COLUMN travel2;
ALTER TABLE dms DROP COLUMN dest2;

CREATE VIEW dms_view AS
    SELECT d.id, d.notes, d.mile, d.travel, l_view.northing, l_view.north_off, l_view.easting, l_view.east_off, l_view.freeway, l_view.free_dir, l_view.cross_street, l_view.cross_dir FROM dms d, location_view l_view WHERE (d."location" = l_view.vault_oid);

REVOKE ALL ON TABLE dms_view FROM PUBLIC;
GRANT SELECT ON TABLE dms_view TO PUBLIC;
