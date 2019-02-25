\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.87.0', '4.88.0');

-- Find duplicate sign_detail records
CREATE TEMP TABLE sign_detail_dups AS
SELECT T1.name AS old_name, T2.name AS new_name
FROM iris.sign_detail T1
JOIN iris.sign_detail T2
  ON T1.ctid < T2.ctid
 AND T1.dms_type = T2.dms_type
 AND T1.portable = T2.portable
 AND T1.technology = T2.technology
 AND T1.sign_access = T2.sign_access
 AND T1.legend = T2.legend
 AND T1.beacon_type = T2.beacon_type
 AND T1.hardware_make = T2.hardware_make
 AND T1.hardware_model = T2.hardware_model
 AND T1.software_make = T2.software_make
 AND T1.software_model = T2.software_model;

-- Remove double duplicate records
DELETE FROM sign_detail_dups T1
      USING sign_detail_dups T2
      WHERE T1.new_name = T2.old_name;

-- Replace duplicate sign_detail on DMS records
UPDATE iris._dms d
   SET sign_detail = n.new_name
  FROM sign_detail_dups n
 WHERE d.sign_detail = n.old_name;

-- Delete duplicate sign_detail records
DELETE FROM iris.sign_detail sd
      USING sign_detail_dups d
      WHERE sd.name = d.old_name;

COMMIT;
