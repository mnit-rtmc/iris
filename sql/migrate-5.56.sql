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

-- Rename domain "cidr" => "block" (and change to CIDR type)
ALTER TABLE iris.domain ADD COLUMN block CIDR;
UPDATE iris.domain SET block = cidr::CIDR;
ALTER TABLE iris.domain ALTER COLUMN block SET NOT NULL;
ALTER TABLE iris.domain DROP COLUMN cidr;

-- Replace user_domain with role_domain
CREATE TABLE iris.role_domain (
    role VARCHAR(15) NOT NULL REFERENCES iris.role,
    domain VARCHAR(15) NOT NULL REFERENCES iris.domain
);
ALTER TABLE iris.role_domain ADD PRIMARY KEY (role, domain);

INSERT INTO iris.role_domain (role, domain)
    SELECT DISTINCT role, domain
        FROM iris.user_id_domain ud
        JOIN iris.user_id u ON ud.user_id = u.name;

CREATE FUNCTION iris.role_domain_notify() RETURNS TRIGGER AS
    $role_domain_notify$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        PERFORM pg_notify('role', OLD.role);
    ELSE
        PERFORM pg_notify('role', NEW.role);
    END IF;
    RETURN NULL; -- AFTER trigger return is ignored
END;
$role_domain_notify$ LANGUAGE plpgsql;

CREATE TRIGGER role_domain_notify_trig
    AFTER INSERT OR DELETE ON iris.role_domain
    FOR EACH ROW EXECUTE FUNCTION iris.role_domain_notify();

DROP TABLE iris.user_id_domain;
DROP FUNCTION iris.user_domain_notify();

COMMIT;
