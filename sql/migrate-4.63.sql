\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.62.0', '4.63.0');

-- Add Banner DXM protocol
UPDATE iris.comm_protocol SET description = 'Banner DXM' WHERE id = 12;

INSERT INTO iris.lane_type (id, description, dcode) VALUES (17, 'Parking','PK');

COMMIT;
