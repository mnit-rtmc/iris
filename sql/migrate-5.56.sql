\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.55.0', '5.56.0');

-- DEFAULT all event_date columns to NOW()
ALTER TABLE event.client_event ALTER COLUMN event_date SET DEFAULT NOW();
ALTER TABLE event.action_plan_event ALTER COLUMN event_date SET DEFAULT NOW();
ALTER TABLE event.comm_event ALTER COLUMN event_date SET DEFAULT NOW();
ALTER TABLE event.camera_switch_event ALTER COLUMN event_date SET DEFAULT NOW();
ALTER TABLE event.camera_video_event ALTER COLUMN event_date SET DEFAULT NOW();
ALTER TABLE event.alarm_event ALTER COLUMN event_date SET DEFAULT NOW();
ALTER TABLE event.beacon_event ALTER COLUMN event_date SET DEFAULT NOW();
ALTER TABLE event.detector_event ALTER COLUMN event_date SET DEFAULT NOW();
ALTER TABLE event.sign_event ALTER COLUMN event_date SET DEFAULT NOW();
ALTER TABLE event.travel_time_event ALTER COLUMN event_date SET DEFAULT NOW();
ALTER TABLE event.brightness_sample ALTER COLUMN event_date SET DEFAULT NOW();
ALTER TABLE event.gate_arm_event ALTER COLUMN event_date SET DEFAULT NOW();
ALTER TABLE event.incident ALTER COLUMN event_date SET DEFAULT NOW();
ALTER TABLE event.incident_update ALTER COLUMN event_date SET DEFAULT NOW();
ALTER TABLE event.meter_event ALTER COLUMN event_date SET DEFAULT NOW();
ALTER TABLE event.tag_read_event ALTER COLUMN event_date SET DEFAULT NOW();
ALTER TABLE event.price_message_event ALTER COLUMN event_date SET DEFAULT NOW();
ALTER TABLE event.weather_sensor_settings ALTER COLUMN event_date SET DEFAULT NOW();
ALTER TABLE event.weather_sensor_sample ALTER COLUMN event_date SET DEFAULT NOW();

-- Add exent ID for DOMAIN FAIL X-Forwarded-For
INSERT INTO event.event_description (event_desc_id, description)
    VALUES (208, 'Client FAIL DOMAIN XFF');

COMMIT;
