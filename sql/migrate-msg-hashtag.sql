\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- DROP prototype from msg_pattern
DROP VIEW msg_pattern_view;
ALTER TABLE iris.msg_pattern DROP COLUMN prototype;
CREATE VIEW msg_pattern_view AS
    SELECT name, compose_hashtag, multi, flash_beacon, pixel_service
    FROM iris.msg_pattern;
GRANT SELECT ON msg_pattern_view TO PUBLIC;

COMMIT;
