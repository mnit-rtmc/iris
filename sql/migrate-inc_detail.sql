\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

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
