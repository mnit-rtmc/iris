\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.36.0', '5.37.0');

ALTER TABLE iris.graphic DROP CONSTRAINT graphic_height_ck;
ALTER TABLE iris.graphic
    ADD CONSTRAINT graphic_height_ck
    CHECK (height >= 1 AND height <= 144);

ALTER TABLE iris.graphic DROP CONSTRAINT graphic_width_ck;
ALTER TABLE iris.graphic
    ADD CONSTRAINT graphic_width_ck
    CHECK (width >= 1 AND width <= 240);

COMMIT;
