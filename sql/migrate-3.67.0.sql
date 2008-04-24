SET SESSION AUTHORIZATION 'tms';

ALTER TABLE roadway DROP COLUMN segment1;
ALTER TABLE roadway DROP COLUMN segment2;

DELETE FROM vault_types WHERE "table" = 'segment_list';
DELETE FROM vault_types WHERE "table" = 'station_segment';
DELETE FROM vault_types WHERE "table" = 'off_ramp';
DELETE FROM vault_types WHERE "table" = 'on_ramp';
DELETE FROM vault_types WHERE "table" = 'meterable_cd';
DELETE FROM vault_types WHERE "table" = 'meterable';
DELETE FROM vault_types WHERE "table" = 'segment';

DELETE FROM location WHERE vault_oid IN (SELECT location FROM segment);

DROP TABLE segment_detector;
DROP TABLE segment_list;
DROP TABLE station_segment;
DROP TABLE off_ramp;
DROP TABLE on_ramp;
DROP TABLE meterable_cd;
DROP TABLE meterable;
DROP TABLE segment;
