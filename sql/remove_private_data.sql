\set ON_ERROR_STOP

-- This script removes (most) private / sensitive data from an IRIS database.
-- WARNING: Do not run this script on a live IRIS sysem.  Please backup and
-- restore to a testing database before running this script.
--
-- `pg_dump tms | gzip > tms-backup.sql.gz`
-- `createdb backup_tms`
-- `zcat tms-backup.sql.gz | psql backup_tms`
-- `psql backup_tms -f remove_private_data.sql`

BEGIN;

DROP SCHEMA IF EXISTS comtrol CASCADE;
DROP SCHEMA IF EXISTS mnpass CASCADE;

SET SESSION AUTHORIZATION 'tms';

DELETE FROM event.action_plan_event;
DELETE FROM event.alarm_event;
DELETE FROM event.beacon_event;
DELETE FROM event.brightness_sample;
DELETE FROM event.camera_switch_event;
DELETE FROM event.camera_video_event;
DELETE FROM event.client_event;
DELETE FROM event.comm_event;
DELETE FROM event.detector_event;
DELETE FROM event.gate_arm_event;
DELETE FROM event.incident_update;
DELETE FROM event.incident;
DELETE FROM event.meter_event;
DELETE FROM event.meter_lock_event;
DELETE FROM event.price_message_event;
DELETE FROM event.sign_event;
DELETE FROM event.tag_read_event;
DELETE FROM event.travel_time_event;
DELETE FROM event.weather_sensor_sample;
DELETE FROM event.weather_sensor_settings;

UPDATE iris.comm_link SET uri = '';
UPDATE iris.modem SET uri = '';
UPDATE iris.controller SET password = NULL;
UPDATE iris.video_monitor SET notes = NULL;
UPDATE iris.camera SET enc_address = NULL;
UPDATE iris.camera SET enc_mcast = NULL;
UPDATE iris.camera SET enc_channel = NULL;
UPDATE iris.vid_src_template SET config = '';

-- DELETE FROM iris.sign_message;
DELETE FROM iris.map_extent;

UPDATE iris.system_attribute SET value = '' WHERE name = 'clearguide_key';
UPDATE iris.system_attribute SET value = '' WHERE name = 'email_smtp_host';
UPDATE iris.system_attribute SET value = '' WHERE name = 'email_sender_server';
UPDATE iris.system_attribute SET value = '' WHERE name = 'work_request_url';
UPDATE iris.system_attribute SET value = '' WHERE name LIKE 'subnet_target_%';
UPDATE iris.system_attribute SET value = '' WHERE name LIKE 'camera_%_url';

DELETE FROM iris.user_id;
DELETE FROM iris.role_domain;
DELETE FROM iris.domain
    WHERE name NOT LIKE 'any_%' AND name NOT LIKE 'local_%';
DELETE FROM iris.permission WHERE role NOT IN ('administrator', 'operator');
DELETE FROM iris.role WHERE name NOT IN ('administrator', 'operator');

INSERT INTO iris.user_id (name, full_name, password, dn, role, enabled)
  VALUES('admin', 'IRIS Administrator', '+vAwDtk/0KGx9k+kIoKFgWWbd3Ku8e/FOHoZoHB65PAuNEiN2muHVavP0fztOi4=', '', 'administrator', 't');

INSERT INTO iris.role_domain (role, domain)
  VALUES ('administrator', 'any_ipv4'), ('administrator', 'any_ipv6');

INSERT INTO iris.map_extent (name, lat, lon, zoom)
  VALUES('Home', 44.9648, -93.2485, 11);

COMMIT;
