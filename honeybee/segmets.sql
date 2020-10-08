-- Script to create segments table in earthwyrm DB

BEGIN;

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
