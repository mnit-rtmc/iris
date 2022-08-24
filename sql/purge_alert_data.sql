\set ON_ERROR_STOP

-- This SQL script will purge records related to CAP alerts.
-- Do not run this script while the IRIS server is online.  Also, please backup
-- the database before running this script.
--
-- psql tms -f purge_alert_data.sql

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT action_plan
  INTO TEMP purge_alert_action_plan
  FROM iris.time_action
  WHERE action_plan LIKE 'ALERT_%'
    AND sched_date + make_interval(weeks => 2) < CURRENT_DATE
  GROUP BY action_plan;

SELECT sign_group
  INTO TEMP purge_alert_sign_group_all
  FROM cap.alert_info
  WHERE end_date + make_interval(weeks => 2) < CURRENT_DATE
  GROUP by sign_group;

SELECT sign_group
  INTO TEMP purge_alert_sign_group_act
  FROM iris.dms_action
  WHERE action_plan IN (SELECT action_plan FROM purge_alert_action_plan)
  GROUP by sign_group;

DELETE FROM iris.dms_sign_group
  WHERE sign_group IN (SELECT sign_group FROM purge_alert_sign_group_all);
DELETE FROM iris.dms_sign_group
  WHERE sign_group IN (SELECT sign_group FROM purge_alert_sign_group_act);
DELETE FROM iris.dms_action
  WHERE action_plan IN (SELECT action_plan FROM purge_alert_action_plan);
DELETE FROM cap.alert_info
  WHERE action_plan IN (SELECT action_plan FROM purge_alert_action_plan);
DELETE FROM iris.sign_group
  WHERE name IN (SELECT sign_group FROM purge_alert_sign_group_all);
DELETE FROM iris.sign_group
  WHERE name IN (SELECT sign_group FROM purge_alert_sign_group_act);
DELETE FROM iris.time_action
  WHERE action_plan IN (SELECT action_plan FROM purge_alert_action_plan);
DELETE FROM iris.action_plan
  WHERE name IN (SELECT action_plan FROM purge_alert_action_plan);

COMMIT;
