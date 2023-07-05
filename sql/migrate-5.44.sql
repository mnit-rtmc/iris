\set ON_ERROR_STOP

BEGIN;

ALTER SCHEMA public OWNER TO tms;

SET SESSION AUTHORIZATION 'tms';

SELECT iris.update_version('5.43.0', '5.44.0');

-- Fix catalog_insert function
DROP FUNCTION iris.catalog_insert() CASCADE;

CREATE FUNCTION iris.catalog_insert() RETURNS TRIGGER AS
    $catalog_insert$
BEGIN
    INSERT INTO iris._cam_sequence (seq_num) VALUES (NEW.seq_num);
    INSERT INTO iris._catalog (name, seq_num, description)
         VALUES (NEW.name, NEW.seq_num, NEW.description);
    RETURN NEW;
END;
$catalog_insert$ LANGUAGE plpgsql;

CREATE TRIGGER catalog_insert_trig
    INSTEAD OF INSERT ON iris.catalog
    FOR EACH ROW EXECUTE PROCEDURE iris.catalog_insert();

COMMIT;
