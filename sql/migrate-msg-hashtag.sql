\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Add hashtag to msg_line
ALTER TABLE iris.msg_line ADD COLUMN hashtag VARCHAR(16);
ALTER TABLE iris.msg_line ADD
    CONSTRAINT hashtag_ck CHECK (hashtag ~ '^#[A-Za-z0-9]+$');
UPDATE iris.msg_line
   SET hashtag = compose_hashtag
  FROM iris.msg_pattern mp
 WHERE mp.name = msg_pattern;
UPDATE iris.msg_line SET hashtag = '#Test' WHERE hashtag IS NULL;
ALTER TABLE iris.msg_line ALTER COLUMN hashtag SET NOT NULL;

-- DROP msg_pattern from msg_line
DROP VIEW msg_line_view;
ALTER TABLE iris.msg_line DROP COLUMN msg_pattern;
CREATE VIEW msg_line_view AS
    SELECT name, hashtag, line, rank, multi
    FROM iris.msg_line;
GRANT SELECT ON msg_line_view TO PUBLIC;

-- DROP prototype from msg_pattern
DROP VIEW msg_pattern_view;
ALTER TABLE iris.msg_pattern DROP COLUMN prototype;
CREATE VIEW msg_pattern_view AS
    SELECT name, compose_hashtag, multi, flash_beacon, pixel_service
    FROM iris.msg_pattern;
GRANT SELECT ON msg_pattern_view TO PUBLIC;

-- DELETE superfluous msg_pattern records
DELETE FROM iris.msg_pattern
  WHERE (multi = '' OR multi = '[np]')
    AND name NOT IN ('.1.LINE', '.2.LINE', '.3.LINE', '.4.LINE')
    AND name NOT IN (
        SELECT DISTINCT msg_pattern
        FROM iris.device_action
        WHERE msg_pattern IS NOT NULL);

COMMIT;
