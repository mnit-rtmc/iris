\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.37.0', '5.38.0');

-- Remove DMS query message enable system attribute
DELETE FROM iris.system_attribute WHERE name = 'dms_querymsg_enable';

-- Rename sign_text to msg_line
INSERT INTO iris.resource_type (name) VALUES ('msg_line');

UPDATE iris.privilege SET type_n = 'msg_line'
    WHERE type_n = 'sign_text';

DELETE FROM iris.resource_type WHERE name = 'sign_text';

DROP VIEW sign_group_text_view;
DROP VIEW sign_text_view;

CREATE TABLE iris.msg_line (
    name VARCHAR(10) PRIMARY KEY,
    msg_pattern VARCHAR(20) NOT NULL REFERENCES iris.msg_pattern,
    line SMALLINT NOT NULL,
    multi VARCHAR(64) NOT NULL,
    rank SMALLINT NOT NULL,
    CONSTRAINT msg_line_line CHECK ((line >= 1) AND (line <= 12)),
    CONSTRAINT msg_line_rank CHECK ((rank >= 1) AND (rank <= 99))
);

DROP TABLE iris.sign_text;

CREATE VIEW msg_line_view AS
    SELECT name, msg_pattern, line, multi, rank
    FROM iris.msg_line;
GRANT SELECT ON msg_line_view TO PUBLIC;

COMMIT;
