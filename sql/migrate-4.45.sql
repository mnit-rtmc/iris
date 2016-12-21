\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.45.0'
	WHERE name = 'database_version';

-- Update OVERRIDE priority
UPDATE iris.dms_action SET a_priority = 21 WHERE a_priority = 14;
UPDATE iris.dms_action SET r_priority = 21 WHERE r_priority = 14;
UPDATE iris.sign_message SET a_priority = 21 WHERE a_priority = 14;
UPDATE iris.sign_message SET r_priority = 21 WHERE r_priority = 14;

-- Update AWS priority
UPDATE iris.dms_action SET a_priority = 20 WHERE a_priority = 13;
UPDATE iris.dms_action SET r_priority = 20 WHERE r_priority = 13;
UPDATE iris.sign_message SET a_priority = 20 WHERE a_priority = 13;
UPDATE iris.sign_message SET r_priority = 20 WHERE r_priority = 13;

-- Update INCIDENT_HIGH priority
UPDATE iris.dms_action SET a_priority = 19 WHERE a_priority = 12;
UPDATE iris.dms_action SET r_priority = 19 WHERE r_priority = 12;
UPDATE iris.sign_message SET a_priority = 19 WHERE a_priority = 12;
UPDATE iris.sign_message SET r_priority = 19 WHERE r_priority = 12;

-- Update INCIDENT_MED priority
UPDATE iris.dms_action SET a_priority = 18 WHERE a_priority = 11;
UPDATE iris.dms_action SET r_priority = 18 WHERE r_priority = 11;
UPDATE iris.sign_message SET a_priority = 18 WHERE a_priority = 11;
UPDATE iris.sign_message SET r_priority = 18 WHERE r_priority = 11;

-- Update INCIDENT_LOW priority
UPDATE iris.dms_action SET a_priority = 17 WHERE a_priority = 10;
UPDATE iris.dms_action SET r_priority = 17 WHERE r_priority = 10;
UPDATE iris.sign_message SET a_priority = 17 WHERE a_priority = 10;
UPDATE iris.sign_message SET r_priority = 17 WHERE r_priority = 10;

-- Update OPERATOR priority
UPDATE iris.dms_action SET a_priority = 12 WHERE a_priority = 9;
UPDATE iris.dms_action SET r_priority = 12 WHERE r_priority = 9;
UPDATE iris.sign_message SET a_priority = 12 WHERE a_priority = 9;
UPDATE iris.sign_message SET r_priority = 12 WHERE r_priority = 9;

-- Update ALERT priority
UPDATE iris.dms_action SET a_priority = 11 WHERE a_priority = 8;
UPDATE iris.dms_action SET r_priority = 11 WHERE r_priority = 8;
UPDATE iris.sign_message SET a_priority = 11 WHERE a_priority = 8;
UPDATE iris.sign_message SET r_priority = 11 WHERE r_priority = 8;

-- Replace OTHER_SYSTEM priority with SCHED_B
--UPDATE iris.dms_action SET a_priority = 7 WHERE a_priority = 7;
--UPDATE iris.dms_action SET r_priority = 7 WHERE r_priority = 7;
--UPDATE iris.sign_message SET a_priority = 7 WHERE a_priority = 7;
--UPDATE iris.sign_message SET r_priority = 7 WHERE r_priority = 7;

-- Replace INCIDENT_HIGH priority with SCHED_D
UPDATE iris.dms_action SET a_priority = 9 WHERE a_priority = 19;
UPDATE iris.dms_action SET r_priority = 9 WHERE r_priority = 19;
UPDATE iris.sign_message SET a_priority = 9 WHERE a_priority = 19;
UPDATE iris.sign_message SET r_priority = 9 WHERE r_priority = 19;

-- Replace INCIDENT_MED priority with SCHED_C
UPDATE iris.dms_action SET a_priority = 8 WHERE a_priority = 18;
UPDATE iris.dms_action SET r_priority = 8 WHERE r_priority = 18;
UPDATE iris.sign_message SET a_priority = 8 WHERE a_priority = 18;
UPDATE iris.sign_message SET r_priority = 8 WHERE r_priority = 18;

-- Replace INCIDENT_LOW priority with SCHED_B
UPDATE iris.dms_action SET a_priority = 7 WHERE a_priority = 17;
UPDATE iris.dms_action SET r_priority = 7 WHERE r_priority = 17;
UPDATE iris.sign_message SET a_priority = 7 WHERE a_priority = 17;
UPDATE iris.sign_message SET r_priority = 7 WHERE r_priority = 17;
