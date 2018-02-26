\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

BEGIN;

SELECT iris.update_version('4.63.0', '4.64.0');

-- Add parking area view
CREATE VIEW parking_area_view AS
	SELECT pa.name, site_id, time_stamp_static, relevant_highway,
	       reference_post, exit_id, facility_name, street_adr, city, state,
	       zip, time_zone, ownership, capacity, low_threshold, amenities,
	       time_stamp, reported_available, true_available, trend, open,
	       trust_data, last_verification_check, verification_check_amplitude,
	       p1.camera AS camera_1, p2.camera AS camera_2,
	       p3.camera AS camera_3,
	       l.roadway, l.road_dir, l.cross_mod, l.cross_street, l.cross_dir,
	       l.lat, l.lon
	FROM iris.parking_area pa
	LEFT JOIN iris.camera_preset p1 ON preset_1 = p1.name
	LEFT JOIN iris.camera_preset p2 ON preset_2 = p2.name
	LEFT JOIN iris.camera_preset p3 ON preset_3 = p3.name
	LEFT JOIN geo_loc_view l ON pa.geo_loc = l.name;
GRANT SELECT ON parking_area_view TO PUBLIC;

-- Add action_plan_event
CREATE TABLE event.action_plan_event (
	event_id integer PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	action_plan VARCHAR(16) NOT NULL,
	detail VARCHAR(15) NOT NULL
);

-- Add action_plan_event_view
CREATE VIEW action_plan_event_view AS
	SELECT e.event_id, e.event_date, ed.description AS event_description,
		e.action_plan, e.detail
	FROM event.action_plan_event e
	JOIN event.event_description ed ON e.event_desc_id = ed.event_desc_id;
GRANT SELECT ON action_plan_event_view TO PUBLIC;

-- Add event descriptions for action plan events
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (900, 'Action Plan ACTIVATED');
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (901, 'Action Plan DEACTIVATED');
INSERT INTO event.event_description (event_desc_id, description)
	VALUES (902, 'Action Plan Phase CHANGED');

-- Add action_plan_event_purge_days system attribute
INSERT INTO iris.system_attribute (name, value)
	VALUES ('action_plan_event_purge_days', '90');

-- Add action plan alert system attributes
INSERT INTO iris.system_attribute (name, value)
	VALUES ('action_plan_alert_list', '');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('email_recipient_action_plan', '');

COMMIT;
