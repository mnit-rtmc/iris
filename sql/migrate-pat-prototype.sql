\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Add prototype to msg_pattern
DROP VIEW msg_pattern_view;
DROP VIEW msg_line_view;

ALTER TABLE iris.msg_pattern
    ADD COLUMN prototype VARCHAR(20) REFERENCES iris.msg_pattern;

INSERT INTO iris.msg_pattern (
    name, prototype, multi, flash_beacon, pixel_service, compose_hashtag
) (
    SELECT DISTINCT
        (msg_pattern || '.' || ltrim(restrict_hashtag, '#'))::VARCHAR(20),
        msg_pattern,
        p.multi,
        flash_beacon,
        pixel_service,
        restrict_hashtag
    FROM iris.msg_line
    JOIN iris.msg_pattern p ON msg_pattern = p.name
    WHERE restrict_hashtag IS NOT NULL
);

UPDATE iris.msg_line
    SET msg_pattern = (msg_pattern || '.' || ltrim(restrict_hashtag, '#'))::VARCHAR(20)
    WHERE restrict_hashtag IS NOT NULL;

ALTER TABLE iris.msg_line DROP COLUMN restrict_hashtag;

CREATE VIEW msg_pattern_view AS
    SELECT name, compose_hashtag, prototype, multi, flash_beacon, pixel_service
    FROM iris.msg_pattern;
GRANT SELECT ON msg_pattern_view TO PUBLIC;

CREATE VIEW msg_line_view AS
    SELECT name, msg_pattern, line, rank, multi
    FROM iris.msg_line;
GRANT SELECT ON msg_line_view TO PUBLIC;

COMMIT;
