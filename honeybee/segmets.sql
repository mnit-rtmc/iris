-- Script to create segments table in earthwyrm DB

\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

GRANT SELECT ON planet_osm_polygon TO PUBLIC;
GRANT SELECT ON planet_osm_line TO PUBLIC;
GRANT SELECT ON planet_osm_roads TO PUBLIC;
GRANT SELECT ON planet_osm_point TO PUBLIC;

DROP TABLE IF EXISTS segments;

CREATE TABLE segments (
    sid BIGINT NOT NULL,
    name TEXT,
    station TEXT,
    detector TEXT,
    zoom INTEGER,
    way GEOMETRY (Geometry, 3857)
);

CREATE INDEX segments_way_idx
    ON segments
    USING gist (way)
    WITH (fillfactor='100');

GRANT SELECT ON segments TO PUBLIC;

COMMIT;
