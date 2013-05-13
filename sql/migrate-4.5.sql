\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '4.5.0'
	WHERE name = 'database_version';

CREATE OR REPLACE VIEW cabinet_view AS
	SELECT name, style, geo_loc, mile
	FROM iris.cabinet;
GRANT SELECT ON cabinet_view TO PUBLIC;

ALTER TABLE ONLY event.comm_event
 	DROP CONSTRAINT comm_event_controller_fkey;
ALTER TABLE ONLY event.comm_event
	ADD CONSTRAINT comm_event_controller_fkey FOREIGN KEY (controller) REFERENCES iris.controller(name) ON DELETE CASCADE;

ALTER TABLE ONLY event.detector_event
 	DROP CONSTRAINT _detector_event_device_id_fkey;
ALTER TABLE ONLY event.detector_event
    ADD CONSTRAINT _detector_event_device_id_fkey FOREIGN KEY (device_id) REFERENCES iris._detector(name) ON DELETE CASCADE;
