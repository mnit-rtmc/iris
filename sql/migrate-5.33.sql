\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.32.0', '5.33.0');

-- Shift CBW beacon verify_pin values up 20
WITH t AS (
  SELECT b.name, verify_pin + 20 AS vp
  FROM iris.beacon b
  JOIN iris.controller c ON b.controller = c.name
  JOIN iris.comm_link cl ON c.comm_link = cl.name
  JOIN iris.comm_config cc ON cl.comm_config = cc.name
  JOIN iris.comm_protocol cp ON cc.protocol = cp.id
  WHERE cp.description = 'CBW' AND verify_pin IS NOT NULL
) 
UPDATE iris.beacon b
SET verify_pin = t.vp
FROM t
WHERE b.name = t.name;

COMMIT;
