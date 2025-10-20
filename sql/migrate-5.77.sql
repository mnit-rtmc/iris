\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.76.0', '5.77.0');

-- Add drop_id check constraint
ALTER TABLE iris.controller
    ADD CONSTRAINT controller_drop_id_check
    CHECK (drop_id >= 0 AND drop_id <= 65535);

-- Add symbol column to lcs_indication
ALTER TABLE iris.lcs_indication ADD COLUMN symbol VARCHAR;

UPDATE iris.lcs_indication SET symbol = '?' WHERE id = 0;
UPDATE iris.lcs_indication SET symbol = '⍽' WHERE id = 1;
UPDATE iris.lcs_indication SET symbol = '↓' WHERE id = 2;
UPDATE iris.lcs_indication SET symbol = '⇣' WHERE id = 3;
UPDATE iris.lcs_indication SET symbol = '✕' WHERE id = 4;
UPDATE iris.lcs_indication SET symbol = '✖' WHERE id = 5;
UPDATE iris.lcs_indication SET symbol = '》' WHERE id = 6;
UPDATE iris.lcs_indication SET symbol = '《' WHERE id = 7;
UPDATE iris.lcs_indication SET symbol = '⤷' WHERE id = 8;
UPDATE iris.lcs_indication SET symbol = '⤶' WHERE id = 9;
UPDATE iris.lcs_indication SET symbol = '◊' WHERE id = 10;
UPDATE iris.lcs_indication SET symbol = 'A' WHERE id = 11;
UPDATE iris.lcs_indication SET symbol = 'L' WHERE id = 12;

ALTER TABLE iris.lcs_indication ALTER COLUMN symbol SET NOT NULL;

-- Rename event.incident_detail to iris.inc_detail
CREATE TABLE iris.inc_detail (
    name VARCHAR(8) PRIMARY KEY,
    description VARCHAR(32) NOT NULL
);

INSERT INTO iris.inc_detail (name, description) (
    SELECT name, description FROM event.incident_detail
);

CREATE TRIGGER inc_detail_notify_trig
    AFTER INSERT OR UPDATE OR DELETE ON iris.inc_detail
    FOR EACH STATEMENT EXECUTE FUNCTION iris.table_notify();

ALTER TABLE event.incident
    DROP CONSTRAINT incident_detail_fkey;
ALTER TABLE event.incident
    ADD CONSTRAINT incident_detail_fkey
    FOREIGN KEY (detail) REFERENCES iris.inc_detail;
ALTER TABLE iris.inc_descriptor
    DROP CONSTRAINT inc_descriptor_detail_fkey;
ALTER TABLE iris.inc_descriptor
    ADD CONSTRAINT inc_descriptor_detail_fkey
    FOREIGN KEY (detail) REFERENCES iris.inc_detail;

DROP TABLE event.incident_detail;

COMMIT;
