\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.78.0', '4.79.0');

INSERT INTO iris.tag_type (id, description) VALUES (2, 'IAG');
INSERT INTO iris.tag_type (id, description) VALUES (3, 'ASTMv6');
UPDATE event.tag_read_event SET tag_type = 3 WHERE tag_type = 0;
UPDATE iris.tag_type SET description = 'Unknown' WHERE id = 0;

COMMIT;
